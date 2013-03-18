import os, glob, datetime
from vtimeseries import *
#from vista.time import *
from vdss import *
from vista.set import *
#from vista.set import Group
from vista.set import PathnamePredicate
from vista.db.dss import *
from vutils import *
from vista.time import TimeFactory, TimeFormat, DefaultTimeFormat
from gov.ca.dsm2.input.parser import Parser
from gov.ca.dsm2.input.parser import Tables
from gov.ca.dsm2.input.model import *

def obsDataBParts(str):
    return str.split('/')[2]
def obsDataCParts(str):
    return str.split('/')[3]
if __name__ == '__main__':
    TF = TimeFactory.getInstance()
    filter = Constants.DEFAULT_FLAG_FILTER
    # Create a .pst file (PEST Control File), a PEST Template File (.tpl),
    # and a .ins file (PEST Instruction File) for the PEST calibration of DSM2
    #
    # DSM2 dates; these should match the DSM2 calibration run config file
    runStartDateStr = '01SEP2008 2400'
    runEndDateStr = '30SEP2009 2400'
    runStartDateObj = TF.createTime(runStartDateStr)
    runEndDateObj = TF.createTime(runEndDateStr)
    RunTSWin = TF.createTimeWindow(runStartDateObj, runEndDateObj)
    # These should be within the run dates, and are used
    # for observed and DSM2 comparison data. A delayed
    # calibration date, for instance, allows DSM2 to
    # equilibrate some. Later these could be modified
    # to allow for a list of multiple start/end calibration dates.
    calibTimeOffsetObj = TF.createTimeInterval('56DAY')
    calibStartDateObj = runEndDateObj - calibTimeOffsetObj
    calibEndDateObj = runEndDateObj
    calibStartDateStr = calibStartDateObj.format()
    calibEndDateStr = calibEndDateObj.format()
    calibTW = TF.createTimeWindow(calibStartDateObj, calibEndDateObj)
    # DSM2 directories and files
    BaseDir = 'D:/delta/models/Historical_v81_Beta_Release/'
    CommonDir = BaseDir + 'common_input/NAVD/'
    CalibDir = BaseDir + '201X-Calibration/'
    TimeSeriesDir = BaseDir + 'timeseries/'
    BaseRunDir = CalibDir + 'BaseRun-1/Output/'
    HydroEchoFile = BaseRunDir + 'hydro_echo_HIST-CLB2K-BASE-v81_1Beta_1.inp'
    DivRtnQFile = TimeSeriesDir + 'dicu_201203.dss'
    RtnECFile = TimeSeriesDir + 'dicuwq_200611_expand.dss'
    DSM2InpFile = 'channel_std_delta_grid_NAVD_20121214.inp'
    # PEST outputs for Hydro and Qual runs, these contain output paths
    # matching observed data paths
    DSM2DSSOutHydroFile = 'PEST_Hydro_Out.inp'
    DSM2DSSOutQualFile = 'PEST_Qual_Out.inp'
    # The DSS file containing combined Hydro and Qual output
    DSM2DSSOutFile = 'PESTCalibOut.dss'
    # The text equivalent of the DSS output, necessary for PEST
    DSM2OutFile = 'PESTCalib.out'
    # DSM2 output locations
    # Each observed B part (location) must have a corresponding
    # DSM2 channel/length in this list of tuples
    DSM2ObsLoc = [ \
                ('ANC', 52, 366), \
                ('ANH', 52, 366), \
                ('CLL', 436, 5733), \
                ('FAL', 279, 4500), \
                ('HLT', 155, 0), \
                ('HOL', 117, 2670), \
                ('JER', 83, 4213), \
                ('MRZ', 441, 5398), \
                ('OBI', 106, 2718), \
                ('OH4', 90, 3021), \
                ('OLD', 71, 3116), \
                ('PRI', 42, 286), \
                ('SJG', 14, 3281), \
                ('SJJ', 83, 4213), \
                ('SSS', 383, 9454), \
                ('SUT', 379, 500), \
                   ]
    #
    ParamGroups = ['Mann', 'Disp', 'Length', 'DivQ', 'RtnQ', 'RtnEC']
    ParamDERINCLB = [0.05, 10.0, 50.0, 0.01, 0.01, 0.01]
    #
    # Observed data files, etc.
    # Observed data paths; the DSM2 output paths are determined from these
    # To ensure that observed and DSM2 output data are always synched,
    #
    ObsPaths = [ \
            '/CDEC/ANC/EC/.*/15MIN/USBR/', \
            '/CDEC/ANH/EC/.*/1HOUR/DWR-OM/', \
            '/CDEC/JER/EC/.*/1HOUR/USBR/', \
            '/CDEC/CLL/EC/.*/1HOUR/USBR/', \
            '/CDEC/SUT/FLOW/.*/15MIN/USGS/', \
            '/CDEC/HLT/FLOW/.*/15MIN/USGS/', \
            '/CDEC/HOL/FLOW/.*/15MIN/USGS/', \
            '/CDEC/OBI/FLOW/.*/1HOUR/USGS/', \
            '/CDEC/PRI/FLOW/.*/15MIN/USGS/', \
            '/CDEC/SJJ/FLOW/.*/15MIN/USGS/', \
            '/CDEC/ANH/STAGE/.*/1HOUR/DWR-OM/', \
            '/CDEC/FAL/STAGE/.*/15MIN/USGS/', \
            '/CDEC/HOL/STAGE/.*/15MIN/USGS/', \
            '/CDEC/MRZ/STAGE/.*/1HOUR/DWR-OM/', \
            '/CDEC/OBI/STAGE/.*/1HOUR/USGS/', \
            '/CDEC/OH4/STAGE/.*/15MIN/USGS/', \
            '/CDEC/OLD/STAGE/.*/1HOUR/DWR-OM/', \
            '/CDEC/PRI/STAGE/.*/15MIN/USGS/', \
            '/CDEC/SJG/STAGE/.*/15MIN/USGS/', \
            '/CDEC/SJJ/STAGE/.*/15MIN/USGS/', \
            '/CDEC/SSS/STAGE/.*/15MIN/USGS/', \
            '/CDEC/SUT/STAGE/.*/15MIN/USGS/', \
            ]
    # sort the paths to reproduce them in the same order
    # from the DSM2 output paths; sorting will be alphabetically
    # by C part (data type), then B part (location)
    ObsPaths = sorted(ObsPaths, key=obsDataBParts)
    ObsPaths = sorted(ObsPaths, key=obsDataCParts)
    #
    ObsDataDir = CalibDir + 'Observed Data/'
    ObsDataFile = ObsDataDir + 'CalibObsData.dss'
    # Pest directories and files
    PESTDir = CalibDir + 'PEST/Calib/'
    PESTTplFile = DSM2InpFile.split('.')[0] + '.tpl'
    PESTInsFile = DSM2OutFile.split('.')[0] + '.ins'
    # 'Dummy' input file for Ag diversion/return/water quality
    # calibration (multiplier) factors; stores floating-point
    # numbers used to multiply the time-series values
    PESTTplAgFile = 'AgCalibCoeffs.tpl'
    PESTInpAgFile = 'AgCalibCoeffs.inp'
    # PEST control file name
    PCF = PESTDir + 'DSM2.pst'
    PCFID = open(PCF,'w')
    #
    ObsTempFile1 = PESTDir + 'temp1.txt'
    OTF1ID = open(ObsTempFile1,'w')
    # read DSM2 base run info
    p = Parser()
    tables = p.parseModel(HydroEchoFile)
    Channels = tables.toChannels()
    nChans = len(Channels.getChannels())
    bndryInputs = tables.toBoundaryInputs()
    SrcQInputs = bndryInputs.getSourceFlowInputs()
    nAgNodes = len(SrcQInputs)
    #
    # read observed data file for desired locations and date range
    # write observed data to a temporary file for later inclusion in
    # the .pst file.
    #
    # Also produce the DSM2 Hydro and Qual output files for PEST calibration
    dss_group = opendss(ObsDataFile)
    nObs = 0
    obsGroups = []
    #
    DSM2HydroID = open(PESTDir + DSM2DSSOutHydroFile,'w')
    DSM2QualID = open(PESTDir + DSM2DSSOutQualFile,'w')
    DSM2HydroID.write('# PEST Calibration output for HYDRO.\n' \
                      'OUTPUT_CHANNEL\n' \
                      'NAME CHAN_NO DISTANCE VARIABLE INTERVAL PERIOD_OP FILE\n')
    DSM2QualID.write('# PEST Calibration output for QUAL.\n' \
                      'OUTPUT_CHANNEL\n' \
                      'NAME CHAN_NO DISTANCE VARIABLE INTERVAL PERIOD_OP FILE\n')
    for obsPath in ObsPaths:
        g = find(dss_group,obsPath)
        dataref = g.getAllDataReferences()
        if len(dataref) > 1:
            print 'Error, too many observed DSS paths for',obsPath
            sys.exit()
        if len(dataref) < 1:
            print 'Error, no observed DSS paths for', obsPath
            sys.exit()
        obsGroup = dataref[0].getPathname().getPart(Pathname.C_PART)
        if obsGroup not in obsGroups:
            obsGroups = obsGroups + [obsGroup]
        staName = dataref[0].getPathname().getPart(Pathname.B_PART)
        dataset = dataref[0].getData()
        # average 15MIN data to 1HOUR
        if dataref[0].getPathname().getPart(Pathname.E_PART) == '15MIN':
            dataset = per_avg(dataset,'1HOUR')
        dataset = dataset.createSlice(calibTW)
        sti = dsIndex(dataset, calibStartDateObj)
        eti = dsIndex(dataset, calibEndDateObj)
        print 'Writing path', obsPath
        for ndx in range(sti, eti):
            nObs += 1
            el = dataset.getElementAt(ndx)
            timeObj = TF.createTime(long(el.getX()))
            dateStr = timeObj.format(DefaultTimeFormat('yyyyMMdd'))
            timeStr = timeObj.format(DefaultTimeFormat('HHmm'))
            if filter.isAcceptable(el):
                valStr = '%15.3f' % (el.getY())
            else:
                valStr = '%15s' % ('DUM')    
            OTF1ID.write("%s%s%s%s %s %3.1f %s\n" % (staName, obsGroup, dateStr, timeStr, valStr, 1.0, obsGroup))
        #
        # write the corresponding DSM2 output line for the observed data path
        tup = [t for t in DSM2ObsLoc if t[0] == staName][0]
        chan_No = tup[1]
        chan_Dist = tup[2]
        fmtStr = '%s    %3d %8d   %s     %s      inst  %s\n'
        if obsGroup.lower() == 'stage' or \
           obsGroup.lower() == 'flow':
            DSM2HydroID.write(fmtStr % (staName, chan_No, chan_Dist, '1HOUR', obsGroup, DSM2DSSOutFile))
        else:
            DSM2QualID.write( fmtStr % (staName, chan_No, chan_Dist, '1HOUR', obsGroup, DSM2DSSOutFile))
        #
    DSM2HydroID.write('END')
    DSM2QualID.write('END')
    OTF1ID.close()
    DSM2HydroID.close()
    DSM2QualID.close()
    obsGroups.sort()    # ensure the data groups are alphabetical order
    #
    RSTFLE = 'restart'
    PESTMODE = 'estimation'
    xcalc = 23
    NPAR = nChans + nAgNodes
    NOBS = nObs
    NPARGP = len(ParamGroups)
    NPRIOR = 0
    NOBSGP = len(obsGroups)
    NTPLFLE = 2
    NINSFLE = NOBSGP
    PRECIS = 'single'
    DPOINT = 'point'
    NUMCOM = 1
    JACFILE = 0
    MESSFILE = 0
    RLAMBDA1 = 1.0
    RLAMFAC = 2.0
    PHIRATSUF = 0.3
    PHIREDLAM = 0.01
    if RLAMBDA1 == 0:
        NUMLAM = 1
    else:
        NUMLAM = 7    
    RELPARMAX = 5.0
    FACPARMAX = 5.0
    FACORIG = 0.001
    PHIREDSWH = 0.1
    NOPTMAX = 30
    PHIREDSTP = 0.005
    NPHISTP = 4
    NPHINORED = 3
    RELPARSTP = 0.01
    NRELPAR = 3
    if PESTMODE != 'regularisation':
        ICOV = 1
        ICOR = 1
        IEIG = 1
    else:
        ICOV = 0
        ICOR = 0
        IEIG = 0
    # print the header and PEST control info to the PCF (.pst) file
    PCFID.write('* control data\n')
    PCFID.write('%s %s\n' % (RSTFLE, PESTMODE))
    PCFID.write('%d %d %d %d %d\n' % (NPAR, NOBS, NPARGP, NPRIOR, NOBSGP))
    PCFID.write('%d %d %s %s %d %d %d\n' % (NTPLFLE, NINSFLE, PRECIS, DPOINT, NUMCOM, JACFILE, MESSFILE))
    PCFID.write('%f %f %f %f %d\n' % (RLAMBDA1, RLAMFAC, PHIRATSUF, PHIREDLAM, NUMLAM))
    PCFID.write('%f %f %f\n' % (RELPARMAX, FACPARMAX, FACORIG))
    PCFID.write('%f\n' % (PHIREDSWH))
    PCFID.write('%d %f %d %d %f %d\n' % (NOPTMAX, PHIREDSTP, NPHISTP, NPHINORED, RELPARSTP, NRELPAR))
    PCFID.write('%d %d %d\n' % (ICOV, ICOR, IEIG))
    #
    # parameter groups
    PCFID.write('* parameter groups\n')
    INCTYP = 'relative'
    DERINC = 0.01
    FORCEN = 'switch'
    DERINCMUL = 1.2
    DERMTHD = 'best_fit'
    #
    for param in ParamGroups:
        DERINCLB = ParamDERINCLB[ParamGroups.index(param)]
        PCFID.write('%s %s %4.3f %5.4f %s %3.1f %s\n' % \
                    (param.upper(),INCTYP,DERINC,DERINCLB,FORCEN,DERINCMUL,DERMTHD))
    #
    # parameter data
    PCFID.write('* parameter data\n')
    PARTRANS = 'none'
    PARCHGLIM = 'relative'
    SCALE = 1.0
    OFFSET = 0.0
    DERCOM = 1
    for param in ParamGroups:
        paramUp = param.upper()
        PARGP = paramUp
        if paramUp == 'MANN' or \
            paramUp == 'DISP' or \
            paramUp == 'LENGTH':
            for chan in Channels.getChannels():
                chan3 = "%03d" % int(chan.getId())
                PARNME = paramUp + chan3
                if paramUp == 'MANN':
                    PARVAL1 = chan.getMannings()
                    PARLBND = 0.005  
                    PARUBND = 0.05
                if paramUp == 'DISP':
                    PARVAL1 = chan.getDispersion()
                    PARLBND = 50.0  
                    PARUBND = 5000.0
                if paramUp == 'LENGTH':
                    PARVAL1 = chan.getLength()
                    PARLBND = PARVAL1 / 1.2   
                    PARUBND = PARVAL1 * 1.2
                PCFID.write('%s %s %s %10.3f %10.3f %10.3f %s %5.2f %5.2f %1d\n' % \
                (PARNME,PARTRANS,PARCHGLIM,PARVAL1,PARLBND,PARUBND,PARGP,SCALE,OFFSET,DERCOM))
        if paramUp == 'DIVQ' or \
            paramUp == 'RTNQ' or \
            paramUp == 'RTNEC':
            # these calibration parameters, being timeseries, will be updated by a pre-processor
            # before each DSM2 run.
            PARNME = paramUp
            PARVAL1 = 1.0
            PARLBND = 0.5  
            PARUBND = 1.5
            PCFID.write('%s %s %s %10.3f %10.3f %10.3f %s %5.2f %5.2f %1d\n' % \
            (PARNME,PARTRANS,PARCHGLIM,PARVAL1,PARLBND,PARUBND,PARGP,SCALE,OFFSET,DERCOM))
    #
    # Observed data groups
    PCFID.write('* observation groups\n')
    for obsGroup in obsGroups:
        PCFID.write('%s\n' % (obsGroup))
    #
    # append the temp observed data file previously created
    OTF1ID = open(ObsTempFile1,'r')
    PCFID.write('* observation data\n')
    PCFID.write(OTF1ID.read())
    OTF1ID.close()
    os.remove(ObsTempFile1)
    #
    # Model command line and I/O files
    PCFID.write('%s\n%s\n' % \
                ('* model command line', 'condor_dsm2.bat hydro.inp qual_ec.inp'))
    PCFID.write('%s\n%s %s\n%s %s\n%s %s' % \
                ('* model input/output', \
                PESTTplFile, DSM2InpFile, \
                PESTTplAgFile, PESTInpAgFile, \
                PESTInsFile, DSM2OutFile))
    PCFID.close()
    print 'Wrote file',PCFID.name
    ##
    # Create PEST Template File (.tpl)
    PTFID = open(PESTDir + PESTTplFile,'w')
    DSM2InpID = open(CommonDir + DSM2InpFile, 'r')
    PTFID.write('ptf @\n')
    # read each line from the DSM2 grid input file;
    # for channel lines, replace Length, Manning, and Dispersion
    # with PEST placeholder names
    channelLines = False
    for line in DSM2InpID:
        if line.upper().find('END') != -1:
            # end of channel lines
            channelLines = False
        if not channelLines:
            PTFID.write(line)
        else:
            lineParts = line.split()
            # CHAN_NO  LENGTH  MANNING  DISPERSION  UPNODE  DOWNNODE
            chanNo = int(lineParts[0])
            upNode = int(lineParts[4])
            downNode = int(lineParts[5])
            PTFID.write('%3d @LENGTH%03d@ @MANN%03d @ @DISP%03d @ %3d %3d\n' % \
                        (chanNo, chanNo, chanNo, chanNo, upNode, downNode))   
        if re.search('CHAN_NO +LENGTH +MANNING +DISPERSION',line,re.I):
            # channel block header line, channel lines follow
            channelLines = True
    PTFID.close()
    DSM2InpID.close()
    ##
    # Create the writeDSM2.py file for post-processing DSM2 calibration runs.
    # The post-processing generates text output of the DSS calibration stations,
    # and the PEST instruction (.ins) file. 
    sq = "'"
    dq = '"'
    obsGroupsStr =  dq + '", "'.join(obsGroups) + dq
    WDSM2ID = open(PESTDir + 'writeDSM2.py', 'w')
    WDSM2ID.write('import sys, os\n')
    WDSM2ID.write('from vtimeseries import *\n')
    WDSM2ID.write('from vdss import *\n')
    WDSM2ID.write('from vista.set import *\n')
    WDSM2ID.write('from vista.db.dss import *\n')
    WDSM2ID.write('from vutils import *\n')
    WDSM2ID.write('from vista.time import TimeFactory\n')
    WDSM2ID.write('TF = TimeFactory.getInstance()\n')
    WDSM2ID.write("tw = TF.createTimeWindow('" + calibStartDateStr + " - " + \
                  calibEndDateStr + "')\n")
    WDSM2ID.write("tempfile = 'temp.out'\n")
    WDSM2ID.write("fid = open(" + sq + DSM2OutFile + sq + ", 'w')\n")
    WDSM2ID.write("for dataType in [" + obsGroupsStr + "]:\n")
    WDSM2ID.write("    dssgrp = opendss('" + DSM2DSSOutFile + "')\n")
    WDSM2ID.write("    dssgrp.filterBy('/'+dataType+'/')\n")
    WDSM2ID.write("    for dssdr in dssgrp.getAllDataReferences():\n")
    WDSM2ID.write("        dssdr = DataReference.create(dssdr,tw)\n")
    WDSM2ID.write("        writeascii(tempfile, dssdr.getData())\n")
    WDSM2ID.write("        tid = open(tempfile, 'r')\n")
    WDSM2ID.write("        fid.write(tid.read())\n")
    WDSM2ID.write("        tid.close()\n")
    WDSM2ID.write("fid.close()\n")
    WDSM2ID.write("if os.path.exists(tempfile):\n")
    WDSM2ID.write("   os.remove(tempfile)\n")
    WDSM2ID.write("# generate PEST instruction (.ins) file\n")
    WDSM2ID.write("fid = open('" + PESTInsFile + "', 'w')\n")
    WDSM2ID.write("tid = open('" + DSM2OutFile + "', 'r')\n")
    WDSM2ID.write("fid.write('pif @\\n')\n")
    WDSM2ID.write("for line in tid:\n")
    WDSM2ID.write("    if re.search('^$', line):\n")
    WDSM2ID.write("        fid.write('@Units :\\n')\n")
    WDSM2ID.write("        continue\n")
    WDSM2ID.write("    lineSplit = line.split()\n")
    WDSM2ID.write("    if line.find('Location: ') > -1:\n")
    WDSM2ID.write("        locStr = lineSplit[1].upper()\n")
    WDSM2ID.write("        continue\n")
    WDSM2ID.write("    if line.find('Type: ') > -1:\n")
    WDSM2ID.write("        typeStr = lineSplit[1].upper()\n")
    WDSM2ID.write("        continue\n")
    WDSM2ID.write("    if re.search('^[0-9][0-9][A-Z][A-Z][A-Z][12][90][78901][0-9] [0-2][0-9][0-9][0-9][ \t]+[0-9.-]+$',line) > -1:\n")
    WDSM2ID.write("        dateStr = lineSplit[0]\n")
    WDSM2ID.write("        timeStr = lineSplit[1]\n")
    WDSM2ID.write("        dataID = 'L1 (' + locStr + typeStr + dateStr + 'T' + timeStr + ')14:30'\n")
    WDSM2ID.write("        fid.write(dataID + '\\n')\n")
    WDSM2ID.write("fid.close()\n")
    WDSM2ID.write("tid.close()\n")
    WDSM2ID.write("sys.exit()\n")
    #
    WDSM2ID.close()
    #
    print 'End processing all files', datetime.today()
    sys.exit()
#