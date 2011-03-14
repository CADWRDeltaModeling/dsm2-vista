import string, re, os, posixpath, sys, math
import glob
from vtimeseries import *
#from vista.time import *
from vdss import *
from vista.set import *
#from vista.set import Group
from vista.set import PathnamePredicate
from vista.db.dss import *
from vutils import *
from calcAmplPhaseAve import calcAmplPhaseAve
from vista.time import TimeFactory

if __name__ == '__main__':
    if len(sys.argv) <= 1:
        PFilter = None
    else:
        PFilter = sys.argv[1]
 
    TF = TimeFactory.getInstance()
    
    # Trim perturbation test output DSS files and consolidate into
    # a single file
    # Then calculate the 4 desired averaged metrics:
    # stage amplitude
    # flow amplitude
    # flow net tidal
    # EC
    #
    STDir = 'd:/delta/models/studies/2010-Calibration/SensitivityTests/'
#    outPattern = 'HIST-CLB2K-Len_Ch'
    outPattern = 'HIST-CLB2K-ManN_Ch'
#    outPattern = 'HIST-CLB2K-Disp_Ch'
#    outPattern = 'HIST-CLB2K-XTopW_Ch'
#    outPattern = 'HIST-CLB2K-XElev_Ch'
#    outPattern = 'HIST-CLB2K-Depth_Res'
#    outPattern = 'HIST-CLB2K-DICU-QDIV_Nd'
#    outPattern = 'HIST-CLB2K-DICU-QRET_Nd'
#    outPattern = 'HIST-CLB2K-DICU-ECRET_Nd'
    DSSConsolRoot = STDir + 'Consolidated-v81'
    intervalAnalysis = timeinterval('28DAY')    # interval to analyze at end of run
    PTDir = STDir + 'condor/'
    # create DSS output filename from previously generated runnames,
    # checking existence of DSS output file (condor occasionally drops a run)
    tw=False
    if outPattern.find('_Res') >= 0:
        filelist = [PTDir+'mildred',PTDir+'franks_tract',PTDir+'discovery_bay', \
                    PTDir+'clifton_court',PTDir+'bethel']
    else:
        filelist = glob.glob(PTDir+'???')
    for envFilePath in filelist:
        objName = os.path.basename(envFilePath)
        DSSFile = STDir + 'condor/' + outPattern + objName + '.dss'
        if not os.path.isfile(DSSFile):
            print 'Note: no output for '+outPattern+objName
            continue
        dss_group=opendss(DSSFile)
        if PFilter:
            dss_group.filterBy(PFilter)
        for dataref in dss_group.getAllDataReferences():
            if not tw:      # first path of first file
                tw=dataref.getTimeWindow()
                # assume that all paths are same time window
                if (tw.getEndTime() - intervalAnalysis).compare(tw.getStartTime()) < 0:
                    raise 'Error; DSM2 run length shorter than desired analysis interval.'
                twAnalysis=TF.createTimeWindow(tw.getEndTime() - intervalAnalysis,
                                               tw.getEndTime())
            ipsu=str(dataref.getPathname()).upper()
            newDataref=DataReference.create(dataref,twAnalysis)
            #print ipsu
            DSSConsolFile = DSSConsolRoot + '-' + \
                dataref.getPathname().getPart(Pathname.C_PART) + '.dss'
            #print DSSConsolFile
            writedss(DSSConsolFile, ipsu, newDataref.getData())
        print 'Trimmed',os.path.basename(DSSFile)
#        for file in glob.glob(DSSFile.replace('.dss','.ds[cd]')):
#            os.remove(file.replace('\\','/'))
        try: 
            os.remove(envFilePath.replace('\\','/'))
        except:
            pass
    ##
    # Now calculate amplitudes and averages,
    # average to a single number, and write
    # to ascii file
    ResultsOut = STDir + 'Results.txt'
    fp = open(ResultsOut, 'w')
    BOld = ''
    # reject Base and these stations
    rejStas = '((base)|(rsan112|bnd_sacr).*ec)|(bnd_sacr|bnd_yolo|calaveras|CHCCC006|cosumnes|/cvp/|/mtz/|/sac/|rsan112|slbar002|tompn_sl|vernalis|yolo).*(flow|stage)'
    now = datetime.now()        
    for DSSConsolFile in glob.glob(DSSConsolRoot + '-[SFE]*.dss'):
        print DSSConsolFile
        dss_group = opendss(DSSConsolFile)
        if PFilter:
            dss_group.filterBy(PFilter)
        dss_group.filterBy(False, PathnamePredicate(rejStas))
        for dataref in dss_group.getAllDataReferences():
            pn = dataref.getPathname()
            B = pn.getPart(Pathname.B_PART)
            if B != BOld:
                print 'Processing', B
                #print str(pn)
                BOld = B
            C = pn.getPart(Pathname.C_PART)
            # typical F parts:
            # HIST-CLB2K-MANN_CH014+FROM-ALL    [EC]
            # HIST-CLB2K-DICU-QDIV_ND006+FROM-  [EC]
            # HIST-CLB2K-DICU-QRET_ND001+FROM-  [EC]
            # HIST-CLB2K-XELEV_CH129+FROM-ALL   [EC]
            # HIST-CLB2K-MANN_CH480             [FLOW]
            # HIST-CLB2K-DICU-QDIV_ND001        [FLOW]
            # HIST-CLB2K-DICU-QRET_ND145        [FLOW]
            # HIST-CLB2K-DISP_CH010             [FLOW]
            # HIST-CLB2K-XELEV_CH284            [FLOW]
            # HIST-CLB2K-Depth_Resfranks_tract  [EC]
            # extract parameter, object, and ID
            FPartUse = pn.getPart(Pathname.F_PART).replace('HIST-CLB2K-','')
            FSplit = re.split('[-_+]',FPartUse)
            if FPartUse.find('DICU') < 0: 
                param = FSplit[0]
                objTypeLen = len(re.split('_',outPattern)[1])
                objType = FSplit[1][0:objTypeLen]
                objID = FSplit[1][objTypeLen:]
#                print objLen,objType,objID
            else: 
                param = FSplit[0] + '-' + FSplit[1]
                objType = FSplit[2][0:2]
                objID = FSplit[2][2:]
            #print B, C, param, objType, objID
            tsA = None
            tsA = calcAmplPhaseAve(dataref,True,False)
            # Average the time series to return a single value
            if tsA != None: 
                avgVal = avg(tsA)
                fp.write(str(avgVal) + \
                         '\t' + B + \
                         '\t' + C + \
                         '\t' + param + \
                         '\t' + objType + \
                         '\t' + objID + \
                         '\n')
    #print datetime.now() - now
    fp.close()
    print 'End processing all files'
    sys.exit()
#
