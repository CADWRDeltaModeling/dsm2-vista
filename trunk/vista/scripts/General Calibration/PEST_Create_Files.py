import os
import glob
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

if __name__ == '__main__':
    TF = TimeFactory.getInstance()
    filter = Constants.DEFAULT_FLAG_FILTER
    #TFI = TimeFormat.dateInstance()
    # Create a .pst file (PEST Control File) and a .ins file (PEST Instruction File)
    # for the PEST calibration of DSM2
    #
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
    DSM2OutFile = 'PESTCalib.out'
    StartDate = '01SEP2008 2400'
    EndDate = '30SEP2009 2400'
    TSWin = TF.createTimeWindow(StartDate+' - '+EndDate)
    ParamGroups = ['Mann', 'Disp', 'Length', 'DivQ', 'RtnQ', 'RtnEC']
    #
    # Observed data files, etc.
    # Observed data paths; the DSM2 output paths are determined from these
    ObsPaths = ['/CDEC/ANC/EC/.*/15MIN/USBR/', \
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
    #
    ObsDataDir = CalibDir + 'Observed Data/'
    ObsDataFile = ObsDataDir + 'CalibObsData.dss'
    # Pest directories and files
    PESTDir = CalibDir + 'PEST/Calib/'
    PESTTmplFile = DSM2InpFile.split('.')[0] + '.tpl'
    PESTInsFile = DSM2OutFile.split('.')[0] + '.ins'
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
    
    dss_group = opendss(ObsDataFile)
    nObs = 0
    obsGroups = []
    for obsPath in ObsPaths:
        g = find(dss_group,obsPath)
        dataref = g.getAllDataReferences()
        if len(dataref) > 1:
            print 'Error, too many observed DSS paths for',obsPath
            sys.exit()
        if len(dataref) < 1:
            print 'Error, no observed Dss paths for', obsPath
            sys.exit()
        obsGroup = dataref[0].getPathname().getPart(Pathname.C_PART)
        if obsGroup not in obsGroups:
            obsGroups = obsGroups + [obsGroup]
        staName = dataref[0].getPathname().getPart(Pathname.B_PART)
        dataset = dataref[0].getData()
        # average 15MIN data to 1HOUR
        if dataref[0].getPathname().getPart(Pathname.E_PART) == '15MIN':
            dataset = per_avg(dataset,'1HOUR')
        dataset = dataset.createSlice(StartDate, EndDate)
        sti = dsIndex(dataset, StartDate)
        eti = dsIndex(dataset, EndDate)
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
    OTF1ID.close()
    #
    RSTFLE = 'restart'
    PESTMODE = 'estimation'
    xcalc = 23
    NPAR = nChans + nAgNodes
    NOBS = nObs
    NPARGP = len(ParamGroups)
    NPRIOR = 0
    NOBSGP = len(obsGroups)
    NTPLFLE = 1
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
    if RLAMBDA1==0:
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
    DERINCLB = 0.005    # should vary with parameter type
    FORCEN = 'switch'
    DERINCMUL = 1.2
    DERMTHD = 'best_fit'
    #
    for param in ParamGroups:
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
#        if paramUp == 'DIVQ' or \
#            paramUp == 'RTNQ' or \
#            paramUp == 'RTNEC':
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
    PCFID.write('%s\n%s %s\n%s %s' % \
                ('* model input/output', \
                PESTTmplFile, DSM2InpFile, \
                PESTInsFile, DSM2OutFile))
    PCFID.close()
    print 'Wrote file',PCFID.name
    print 'End processing all files'
    sys.exit()
#
