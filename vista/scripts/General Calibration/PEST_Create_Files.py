import os, glob, datetime, random
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
    return str.upper().split('/')[2]
def obsDataCParts(str):
    return str.upper().split('/')[3]
def parseInpSects(DSM2InpFile,Section):
    # parse the given DSM2 Input file for Section,
    # return as a list of lists (the rows/columns)
    try: EID = open(DSM2InpFile,'r')
    except: 
        print 'Unable to open file', DSM2InpFile
        return None
    inSection = False
    sectList = []
    for line in EID:
        if line.upper().strip() == Section.upper():
            inSection = True
            continue
        if inSection:
            if line.upper().strip() == 'END':
                break
            if not re.search('^#',line.lstrip()):
                # not a comment or blank line, use it
                sectList.append(line.split())
    EID.close()
    return(sectList)
#                
if __name__ == '__main__':
    TF = TimeFactory.getInstance()
    filter = Constants.DEFAULT_FLAG_FILTER
    random.seed()
    # Create a .pst file (PEST Control File), a PEST Template File (.tpl),
    # and a .ins file (PEST Instruction File) for the PEST calibration of DSM2
    #
    # DSM2 dates; these should match the DSM2 calibration run config file
    runStartDateStr = '01SEP2008 2400'
    runEndDateStr = '30SEP2009 2400'
    runStartDateObj = TF.createTime(runStartDateStr)
    runEndDateObj = TF.createTime(runEndDateStr)
    RunTSWin = TF.createTimeWindow(runStartDateObj, runEndDateObj)
    # Calibration start and end dates should be within the run dates, 
    # and are used for observed and DSM2 comparison data. 
    # A delayed calibration date, for instance, allows DSM2 to
    # equilibrate. Later these could be modified
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
    QualEchoFile = BaseRunDir + 'qual_ec_echo_HIST-CLB2K-BASE-v81_1Beta_0.inp'
    DivRtnQFile = TimeSeriesDir + 'dicu_201203.dss'
    RtnECFile = TimeSeriesDir + 'dicuwq_200611_expand.dss'
    ChanInpFile = 'channel_std_delta_grid_NAVD_20121214.inp'
    GateInpFile = 'gate_std_delta_grid_NAVD_20121214.inp'
    ResInpFile = 'reservoir_std_delta_grid_NAVD_20121214.inp'
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
    # Use either Width or Elev, not both
    ParamGroups = ['MANN', 'DISP', 'LENGTH', \
                   'GATE', \
                   'RESERCF', \
 #                  'WIDTH', \
                   'ELEV', \
                   'DIV-FLOW', 'DRAIN-FLOW', 'DRAIN-EC', \
                   ]
    # make sure these elements agree with ParamGroups above
    ParamDERINCLB = [0.05, 10.0, 50.0, \
                     0.05, \
                     1.0, \
                     0.01, \
#                     0.01, \
                     0.01, 0.01, 0.01, \
                     ]
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
    #
    # Pest directories and files
    #
    PESTDir = CalibDir + 'PEST/Calib/'
    PESTChanTplFile = ChanInpFile.split('.')[0] + '.tpl'
    PESTGateTplFile = GateInpFile.split('.')[0] + '.tpl'
    PESTResTplFile = ResInpFile.split('.')[0] + '.tpl'
    PESTInsFile = DSM2OutFile.split('.')[0] + '.ins'
    #
    # 'Dummy' input/template files for Ag div/drainage/EC
    # calibration (multiplier) factors; stores floating-point
    # numbers used to multiply the time-series values
    PESTInpAgFile = 'AgCalibCoeffs.inp'
    PESTTplAgFile = PESTInpAgFile.split('.')[0] + '.tpl'
    PIAFId = open(PESTDir + PESTInpAgFile,'w')
    PTAFId = open(PESTDir + PESTTplAgFile,'w')
    PTAFId.write('%s\n' % ('ptf @'))
    # 'Dummy' input/template files for cross-section calibration
    # (multiplier) factors; stores floating-point
    # numbers used to multiply the cross-section
    # width and elevation values
    # PEST control file name
    PESTInpXCFile = 'XCCalibCoeffs.inp'
    PESTTplXCFile = PESTInpXCFile.split('.')[0] + '.tpl'
    PIXFId = open(PESTDir + PESTInpXCFile,'w')
    PTXFId = open(PESTDir + PESTTplXCFile,'w')
    PTXFId.write('ptf @\n')
    #
    PCFId = open(PESTDir + 'DSM2.pst','w')
    #
    ObsTempFile1 = PESTDir + 'temp1.txt'
    OTF1Id = open(ObsTempFile1,'w')
    #
    # read DSM2 base run info
    p = Parser()
    tablesHydro = p.parseModel(HydroEchoFile)
    tablesQual = p.parseModel(QualEchoFile)
    Channels = tablesHydro.toChannels()
    bndryInputsHydro = tablesHydro.toBoundaryInputs()
    srcAgInputsHydro = bndryInputsHydro.getSourceFlowInputs()
    bndryInputsQual = tablesQual.toBoundaryInputs()
    #srcAgInputsQual = bndryInputsQual.getSourceFlowInputs()
    tableNodeConc = tablesQual.getTableNamed('NODE_CONCENTRATION')
    # read the gate input file for gate data
    gatePipeList = parseInpSects(CommonDir + GateInpFile,'GATE_PIPE_DEVICE')
    gateWeirList = parseInpSects(CommonDir + GateInpFile,'GATE_WEIR_DEVICE')
    # read the reservoir input file for reservoir connection data
    resCFList = parseInpSects(CommonDir + ResInpFile,'RESERVOIR_CONNECTION')
    #
    # read observed data file for desired locations and date range
    # write observed data to a temporary file for later inclusion in
    # the .pst file.
    #
    # Also produce the DSM2 Qual and Qual output files for PEST calibration
    dss_group = opendss(ObsDataFile)
    nObs = 0
    obsGroups = []
    #
    DSM2HydroId = open(PESTDir + DSM2DSSOutHydroFile,'w')
    DSM2QualId = open(PESTDir + DSM2DSSOutQualFile,'w')
    DSM2HydroId.write('# PEST Calibration output for HYDRO.\n' \
                      'OUTPUT_CHANNEL\n' \
                      'NAME CHAN_NO DISTANCE VARIABLE INTERVAL PERIOD_OP FILE\n')
    DSM2QualId.write('# PEST Calibration output for QUAL.\n' \
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
            OTF1Id.write("%s%s%s%s %s %3.1f %s\n" % (staName, obsGroup, dateStr, timeStr, valStr, 1.0, obsGroup))
        #
        # write the corresponding DSM2 output line for the observed data path
        tup = [t for t in DSM2ObsLoc if t[0] == staName][0]
        chan_No = tup[1]
        chan_Dist = tup[2]
        fmtStr = '%s    %3d %8d   %s     %s      inst  %s\n'
        if obsGroup.lower() == 'stage' or \
           obsGroup.lower() == 'flow':
            DSM2HydroId.write(fmtStr % (staName, chan_No, chan_Dist, '1HOUR', obsGroup, DSM2DSSOutFile))
        else:
            DSM2QualId.write( fmtStr % (staName, chan_No, chan_Dist, '1HOUR', obsGroup, DSM2DSSOutFile))
        #
    DSM2HydroId.write('END')
    DSM2QualId.write('END')
    OTF1Id.close()
    DSM2HydroId.close()
    DSM2QualId.close()
    obsGroups.sort()    # ensure the data groups are alphabetical order
    #
    RSTFLE = 'restart'
    PESTMODE = 'estimation'
    # number of calibration parameters
    NPAR = 0
    if 'MANN' in ParamGroups:
        NPAR += len(Channels.getChannels())
    if 'DISP' in ParamGroups:
        NPAR += len(Channels.getChannels())
    if 'LENGTH' in ParamGroups:
        NPAR += len(Channels.getChannels())
    if 'ELEV' in ParamGroups or \
        'WIDTH' in ParamGroups:
        for chan in Channels.getChannels():
                NPAR += len(chan.getXsections())
    if 'GATE' in ParamGroups:
        NPAR += (len(gateWeirList)-1) * 2   # for to/from flow coeffs
        NPAR += (len(gatePipeList)-1) * 2
    if 'RESERCF' in ParamGroups:
        NPAR += (len(resCFList)-1) * 2    # for in/out flow coeffs
    paramUp = 'DIV-FLOW'
    if paramUp in ParamGroups:
        for srcInput in srcAgInputsHydro:
            try: CPartUp = srcInput.path.upper().split('/')[3]
            except: continue
            if CPartUp == paramUp:
                NPAR += 1
    paramUp = 'DRAIN-FLOW'
    if paramUp in ParamGroups:
        for srcInput in srcAgInputsHydro:
            try: CPartUp = srcInput.path.upper().split('/')[3]
            except: continue
            if CPartUp == paramUp:
                NPAR += 1
    paramUp = 'DRAIN-EC'
    if paramUp in ParamGroups:
        for row in tableNodeConc.getValues():
            path = row[5]
            if path.find('/') >= 0:
                pathParts = path.split('/')
                CPartUp = pathParts[3]
                try: node3 = "%03d" % int(pathParts[2])
                except: continue
                if CPartUp == paramUp:
                    NPAR += 1
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
    # print the header and PEST control info to the PEST control (.pst) file
    PCFId.write('* control data\n')
    PCFId.write('%s %s\n' % (RSTFLE, PESTMODE))
    PCFId.write('%d %d %d %d %d\n' % (NPAR, NOBS, NPARGP, NPRIOR, NOBSGP))
    PCFId.write('%d %d %s %s %d %d %d\n' % (NTPLFLE, NINSFLE, PRECIS, DPOINT, NUMCOM, JACFILE, MESSFILE))
    PCFId.write('%f %f %f %f %d\n' % (RLAMBDA1, RLAMFAC, PHIRATSUF, PHIREDLAM, NUMLAM))
    PCFId.write('%f %f %f\n' % (RELPARMAX, FACPARMAX, FACORIG))
    PCFId.write('%f\n' % (PHIREDSWH))
    PCFId.write('%d %f %d %d %f %d\n' % (NOPTMAX, PHIREDSTP, NPHISTP, NPHINORED, RELPARSTP, NRELPAR))
    PCFId.write('%d %d %d\n' % (ICOV, ICOR, IEIG))
    #
    # parameter groups
    PCFId.write('* parameter groups\n')
    INCTYP = 'relative'
    DERINC = 0.01
    FORCEN = 'switch'
    DERINCMUL = 1.2
    DERMTHD = 'best_fit'
    #
    for param in ParamGroups:
        DERINCLB = ParamDERINCLB[ParamGroups.index(param)]
        PCFId.write('%s %s %4.3f %5.4f %s %3.1f %s\n' % \
                    (param.upper(),INCTYP,DERINC,DERINCLB,FORCEN,DERINCMUL,DERMTHD))
    #
    # parameter data
    PCFId.write('* parameter data\n')
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
            # adjust channel parameters directly
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
                PCFId.write('%s %s %s %10.3f %10.3f %10.3f %s %5.2f %5.2f %1d\n' % \
                (PARNME,PARTRANS,PARCHGLIM,PARVAL1,PARLBND,PARUBND,PARGP,SCALE,OFFSET,DERCOM))
        #
        if paramUp == 'GATE':
            # adjust gate coefficients directly, similar to channel parameters
            # unfortunately gate data is not in the DSM2 Input methods by Nicky,
            # so it has to be read from the Hydro echo file.
            # since DSM2 gates are split between pipes and weirs,
            # we'll follow the same pattern
            #
            headersList = gateWeirList[0]
            # find fields which combined will give a unique row name
            uniq1 = headersList.index('GATE_NAME')
            uniq2 = headersList.index('DEVICE')
            # find fields with the gate flow coeffs (CF_FROM_NODE and CF_TO_NODE)
            CF_FromLoc = headersList.index('CF_FROM_NODE')
            CF_ToLoc = headersList.index('CF_TO_NODE')
            # now write to PEST .pst file
            for row in gateWeirList:
                try: PARVAL1 = float(row[CF_FromLoc])
                except: continue    # headers, just continue
                PARLBND = PARVAL1 * 0.5
                PARUBND = PARVAL1 * 1.5
                PARNME = 'GATECFFROM:' + row[uniq1] + ':' + row[uniq2]
                PCFId.write('%s %s %s %10.3f %10.3f %10.3f %s %5.2f %5.2f %1d\n' % \
                    (PARNME,PARTRANS,PARCHGLIM,PARVAL1,PARLBND,PARUBND,PARGP,SCALE,OFFSET,DERCOM))
                PARVAL1 = float(row[CF_ToLoc])
                PARLBND = PARVAL1 * 0.5
                PARUBND = PARVAL1 * 1.5
                PARNME = 'GATECFTO:' + row[uniq1] + ':' + row[uniq2]
                PCFId.write('%s %s %s %10.3f %10.3f %10.3f %s %5.2f %5.2f %1d\n' % \
                    (PARNME,PARTRANS,PARCHGLIM,PARVAL1,PARLBND,PARUBND,PARGP,SCALE,OFFSET,DERCOM))
            headersList = gatePipeList[0]
            uniq = headersList.index('GATE_NAME')
            CF_FromLoc = headersList.index('CF_FROM_NODE')
            CF_ToLoc = headersList.index('CF_TO_NODE')
            for row in gatePipeList:
                try: PARVAL1 = float(row[CF_FromLoc])
                except: continue    # headers, just continue
                PARLBND = PARVAL1 * 0.5
                PARUBND = PARVAL1 * 1.5
                PARNME = 'GATECFFROM:' + row[uniq]
                PCFId.write('%s %s %s %10.3f %10.3f %10.3f %s %5.2f %5.2f %1d\n' % \
                    (PARNME,PARTRANS,PARCHGLIM,PARVAL1,PARLBND,PARUBND,PARGP,SCALE,OFFSET,DERCOM))
                PARVAL1 = float(row[CF_ToLoc])
                PARLBND = PARVAL1 * 0.5
                PARUBND = PARVAL1 * 1.5
                PARNME = 'GATECFTO:' + row[uniq]
                PCFId.write('%s %s %s %10.3f %10.3f %10.3f %s %5.2f %5.2f %1d\n' % \
                    (PARNME,PARTRANS,PARCHGLIM,PARVAL1,PARLBND,PARUBND,PARGP,SCALE,OFFSET,DERCOM))
#
        if paramUp == 'RESERCF':
            # adjust reservoir flow coefficients directly, similar to gate flow coeffs
            headersList = resCFList[0]
            # find fields which combined will give a unique row name
            uniq1 = headersList.index('RES_NAME')
            uniq2 = headersList.index('NODE')
            # find fields with the reservoir flow coeffs ( COEF_IN and COEF_OUT)
            CF_InLoc = headersList.index('COEF_IN')
            CF_OutLoc = headersList.index('COEF_OUT')
            # now write to PEST .pst file
            for row in resCFList:
                try: PARVAL1 = float(row[CF_InLoc])
                except: continue    # headers, just continue
                PARLBND = PARVAL1 * 0.5
                PARUBND = PARVAL1 * 1.5
                PARNME = 'RESERCFIN:' + row[uniq1] + ':' + row[uniq2]
                PCFId.write('%s %s %s %10.3f %10.3f %10.3f %s %5.2f %5.2f %1d\n' % \
                    (PARNME,PARTRANS,PARCHGLIM,PARVAL1,PARLBND,PARUBND,PARGP,SCALE,OFFSET,DERCOM))
                PARVAL1 = float(row[CF_OutLoc])
                PARLBND = PARVAL1 * 0.5
                PARUBND = PARVAL1 * 1.5
                PARNME = 'RESERCFOUT:' + row[uniq1] + ':' + row[uniq2]
                PCFId.write('%s %s %s %10.3f %10.3f %10.3f %s %5.2f %5.2f %1d\n' % \
                    (PARNME,PARTRANS,PARCHGLIM,PARVAL1,PARLBND,PARUBND,PARGP,SCALE,OFFSET,DERCOM))
#
        if paramUp == 'ELEV' or paramUp == 'WIDTH':
            # cross-sections in channels;
            # instead of PEST adjusting the elevations or widths directly,
            # PEST will change coefficients for each cross-section which
            # will multiply the elevations or widths for each layer in a
            # pre-processor vscript before each DSM2 run. 
            PARVAL1 = 1.0
            PARLBND = 0.8  
            PARUBND = 1.2
            for chan in Channels.getChannels():
                chan3 = "%03d" % int(chan.getId())
                for xs in chan.getXsections():
                    xsdist3 = "%03d" % int(xs.getDistance()*1000.)
                    PARNME = paramUp + chan3 + ":" + xsdist3
                    PCFId.write('%s %s %s %10.3f %10.3f %10.3f %s %5.2f %5.2f %1d\n' % \
                    (PARNME,PARTRANS,PARCHGLIM,PARVAL1,PARLBND,PARUBND,PARGP,SCALE,OFFSET,DERCOM))
                    # create 'dummy' input/template files for x-sect calibration factors
                    PIXFId.write('%s %10.4f\n' % (PARNME,random.uniform(PARLBND, PARUBND)))
                    PTXFId.write('%s %s\n' % (PARNME,'@' + PARNME + '  @'))
        if paramUp == 'DIV-FLOW' or \
            paramUp == 'DRAIN-FLOW':
            # these calibration parameters, being timeseries, will be updated by a pre-processor
            # vscript before each DSM2 run. As with channel cross-sections, PEST will calibrate
            # 3 coefficients for each node.
            PARVAL1 = 1.0
            PARLBND = 0.5  
            PARUBND = 1.5
            for srcInput in srcAgInputsHydro:
                try: CPartUp = obsDataCParts(srcInput)
                except: continue
                if CPartUp == paramUp:
                    node3 = "%03d" % int(srcInput.nodeId)
                    PARNME = paramUp+node3
                    PCFId.write('%s %s %s %10.3f %10.3f %10.3f %s %5.2f %5.2f %1d\n' % \
                    (PARNME,PARTRANS,PARCHGLIM,PARVAL1,PARLBND,PARUBND,PARGP,SCALE,OFFSET,DERCOM))
                    # create 'dummy' input/template files for Ag calibration factors
                    PIAFId.write('%s %10.4f\n' % (PARNME,random.uniform(PARLBND, PARUBND)))
                    PTAFId.write('%s %s\n' % (PARNME,'@' + PARNME + '  @'))
        if paramUp == 'DRAIN-EC':
            # similar to Div/Drain flows, but have to use rows from the input table
            PARVAL1 = 1.0
            PARLBND = 0.5  
            PARUBND = 1.5
            for row in tableNodeConc.getValues():
                path = row[5]
                if path.find('/') >= 0:
                    pathParts = path.split('/')
                    CPartUp = pathParts[3]
                    try: node3 = "%03d" % int(pathParts[2])
                    except: continue
                    if CPartUp == paramUp:
                        PARNME = paramUp + node3
                        PCFId.write('%s %s %s %10.3f %10.3f %10.3f %s %5.2f %5.2f %1d\n' % \
                        (PARNME,PARTRANS,PARCHGLIM,PARVAL1,PARLBND,PARUBND,PARGP,SCALE,OFFSET,DERCOM))
                        # create 'dummy' input/template files for Ag calibration factors
                        PIAFId.write('%s %d\n' % (PARNME,random.uniform(0.5, 1.5)))
                        PTAFId.write('%s %s\n' % (PARNME,'@' + PARNME + '  @'))
    #
    PIAFId.close()
    PIXFId.close()
    PTAFId.close()
    PTXFId.close()
    # Observed data groups
    PCFId.write('* observation groups\n')
    for obsGroup in obsGroups:
        PCFId.write('%s\n' % (obsGroup))
    #
    # append the temp observed data file previously created
    OTF1Id = open(ObsTempFile1,'r')
    PCFId.write('* observation data\n')
    PCFId.write(OTF1Id.read())
    OTF1Id.close()
    os.remove(ObsTempFile1)
    #
    # Model command line and I/O files
    PCFId.write('%s\n%s\n' % \
                ('* model command line', 'condor_dsm2.bat hydro.inp qual_ec.inp'))
    PCFId.write('%s\n%s %s\n%s %s\n%s %s\n%s %s\n%s %s' % \
                ('* model input/output', \
                PESTChanTplFile, ChanInpFile, \
                PESTGateTplFile, GateInpFile, \
                PESTResTplFile, ResInpFile, \
                PESTTplAgFile, PESTInpAgFile, \
                PESTInsFile, DSM2OutFile))
    PCFId.close()
    print 'Wrote file',PCFId.name
    ##
    ## Create PEST Template Files (.tpl)
    # DSM2 channel input template
    PTFId = open(PESTDir + PESTChanTplFile,'w')
    DSM2InpId = open(CommonDir + ChanInpFile, 'r')
    PTFId.write('ptf @\n')
    # read each line from the DSM2 grid input file;
    # for channel lines, replace Length, Manning, and Dispersion
    # with PEST placeholder names
    channelLines = False
    for line in DSM2InpId:
        if line.upper().find('END') != -1:
            # end of channel lines
            channelLines = False
        if not channelLines:
            PTFId.write(line)
        else:
            lineParts = line.split()
            # CHAN_NO  LENGTH  MANNING  DISPERSION  UPNODE  DOWNNODE
            chanNo = int(lineParts[0])
            upNode = int(lineParts[4])
            downNode = int(lineParts[5])
            PTFId.write('%3d @LENGTH%03d@ @MANN%03d @ @DISP%03d @ %3d %3d\n' % \
                        (chanNo, chanNo, chanNo, chanNo, upNode, downNode))   
        if re.search('CHAN_NO +LENGTH +MANNING +DISPERSION',line,re.I):
            # channel block header line, channel lines follow
            channelLines = True
    PTFId.close()
    DSM2InpId.close()
    # DSM2 Gate input template
    PTFId = open(PESTDir + PESTGateTplFile,'w')
    DSM2InpId = open(CommonDir + GateInpFile, 'r')
    PTFId.write('ptf |\n')
    # read each line from the DSM2 gate input file;
    # for gate weir & pipe device lines, replace To and From flow coefficients 
    # with PEST placeholder names
    gateLines = False
    for line in DSM2InpId:
        if line.upper().find('END') != -1:
            # end of gate lines
            gateLines = False
        if not gateLines:
            PTFId.write(line.rstrip()+'\n')
        else:
            lineParts = line.split()
            # GATE_NAME DEVICE NDUPLICATE (RADIUS|HEIGHT) ELEV CF_FROM_NODE CF_TO_NODE DEFAULT_OP
            gateName = lineParts[headersList.index('GATE_NAME')]
            devName = lineParts[headersList.index('DEVICE')]
            # find which fields have the gate flow coeffs (CF_FROM_NODE and CF_TO_NODE)
            lineParts[CF_FromLoc] = '|GATECFFROM:' + gateName + ':' + devName + '|' 
            lineParts[CF_ToLoc] = '|GATECFTO:' + gateName + ':' + devName + '|'
            for i in range(len(lineParts)): 
                PTFId.write('%s ' % lineParts[i])
            PTFId.write('\n')
        # Pipe or Weir section?   
        if re.search('GATE_PIPE_DEVICE', line, re.I):
            headersList = gatePipeList[0]
        if re.search('GATE_WEIR_DEVICE',line,re.I):
            headersList = gateWeirList[0]
        if re.search('GATE_NAME +.*CF_(FROM|TO)_NODE +.*CF_(FROM|TO)_NODE',line,re.I):
            # gate block header line, gate lines follow
            gateLines = True
            CF_FromLoc = headersList.index('CF_FROM_NODE')
            CF_ToLoc = headersList.index('CF_TO_NODE')
    PTFId.close()
    DSM2InpId.close()
    # DSM2 Reservoir input template
    PTFId = open(PESTDir + PESTResTplFile,'w')
    DSM2InpId = open(CommonDir + ResInpFile, 'r')
    PTFId.write('ptf |\n')
    # read each line from the DSM2 reservoir input file;
    # for reservoir coefficient lines, replace In and Out flow coefficients 
    # with PEST placeholder names
    resCFLines = False
    for line in DSM2InpId:
        if line.upper().find('END') != -1:
            # end of reservoir coefficient lines
            resCFLines = False
        if not resCFLines:
            PTFId.write(line.rstrip()+'\n')
        else:
            lineParts = line.split()
            # RES_NAME NODE COEF_IN COEF_OUT
            resName = lineParts[headersList.index('RES_NAME')]
            resNode = lineParts[headersList.index('NODE')]
            # find which fields have the reservoir flow coeffs (COEF_IN and COEF_OUT)
            lineParts[CF_InLoc] = '|RESCFIN:' + resName + ':' + resNode + '|' 
            lineParts[CF_OutLoc] = '|RESCFOUT:' + resName + ':' + resNode + '|'
            for i in range(len(lineParts)): 
                PTFId.write('%s ' % lineParts[i])
            PTFId.write('\n')
        if re.search('RES_NAME +.*COEF_IN',line,re.I):
            # reservoir coefficient block header line, reservoir lines follow
            resCFLines = True
            headersList = resCFList[0]
            CF_InLoc = headersList.index('COEF_IN')
            CF_OutLoc = headersList.index('COEF_OUT')
    PTFId.close()
    DSM2InpId.close()
    ##
    # Create the writeDSM2Output.py file for post-processing DSM2 calibration runs.
    # The post-processing generates text output of the DSS calibration stations,
    # and the PEST instruction (.ins) file. 
    sq = "'"
    dq = '"'
    obsGroupsStr =  dq + '", "'.join(obsGroups) + dq
    WDSM2Id = open(PESTDir + 'DSM2PESTPostProcess.py', 'w')
    WDSM2Id.write('import sys, os\n')
    WDSM2Id.write('from vtimeseries import *\n')
    WDSM2Id.write('from vdss import *\n')
    WDSM2Id.write('from vista.set import *\n')
    WDSM2Id.write('from vista.db.dss import *\n')
    WDSM2Id.write('from vutils import *\n')
    WDSM2Id.write('from vista.time import TimeFactory\n')
    WDSM2Id.write('TF = TimeFactory.getInstance()\n')
    WDSM2Id.write("tw = TF.createTimeWindow('" + calibStartDateStr + " - " + \
                  calibEndDateStr + "')\n")
    WDSM2Id.write("# This post-processor was generated by PEST_pre_DSM2Run.py\n" + \
                  "# It translates DSM2 DSS output for calibration to a text file,\n" + \
                  "# then generates the matching PEST instruction file for the output.\n")
    WDSM2Id.write("tempfile = 'temp.out'\n")
    WDSM2Id.write("fid = open(" + sq + DSM2OutFile + sq + ", 'w')\n")
    WDSM2Id.write("for dataType in [" + obsGroupsStr + "]:\n")
    WDSM2Id.write("    dssgrp = opendss('" + DSM2DSSOutFile + "')\n")
    WDSM2Id.write("    dssgrp.filterBy('/'+dataType+'/')\n")
    WDSM2Id.write("    for dssdr in dssgrp.getAllDataReferences():\n")
    WDSM2Id.write("        dssdr = DataReference.create(dssdr,tw)\n")
    WDSM2Id.write("        writeascii(tempfile, dssdr.getData())\n")
    WDSM2Id.write("        tid = open(tempfile, 'r')\n")
    WDSM2Id.write("        fid.write(tid.read())\n")
    WDSM2Id.write("        tid.close()\n")
    WDSM2Id.write("fid.close()\n")
    WDSM2Id.write("if os.path.exists(tempfile):\n")
    WDSM2Id.write("   os.remove(tempfile)\n")
    WDSM2Id.write("# generate PEST instruction (.ins) file\n")
    WDSM2Id.write("fid = open('" + PESTInsFile + "', 'w')\n")
    WDSM2Id.write("tid = open('" + DSM2OutFile + "', 'r')\n")
    WDSM2Id.write("fid.write('pif @\\n')\n")
    WDSM2Id.write("for line in tid:\n")
    WDSM2Id.write("    if re.search('^$', line):\n")
    WDSM2Id.write("        fid.write('@Units :\\n')\n")
    WDSM2Id.write("        continue\n")
    WDSM2Id.write("    lineSplit = line.split()\n")
    WDSM2Id.write("    if line.find('Location: ') > -1:\n")
    WDSM2Id.write("        locStr = lineSplit[1].upper()\n")
    WDSM2Id.write("        continue\n")
    WDSM2Id.write("    if line.find('Type: ') > -1:\n")
    WDSM2Id.write("        typeStr = lineSplit[1].upper()\n")
    WDSM2Id.write("        continue\n")
    WDSM2Id.write("    if re.search('^[0-9][0-9][A-Z][A-Z][A-Z][12][90][78901][0-9] [0-2][0-9][0-9][0-9][ \t]+[0-9.-]+$',line) > -1:\n")
    WDSM2Id.write("        dateStr = lineSplit[0]\n")
    WDSM2Id.write("        timeStr = lineSplit[1]\n")
    WDSM2Id.write("        dataID = 'L1 (' + locStr + typeStr + dateStr + 'T' + timeStr + ')14:30'\n")
    WDSM2Id.write("        fid.write(dataID + '\\n')\n")
    WDSM2Id.write("fid.close()\n")
    WDSM2Id.write("tid.close()\n")
    WDSM2Id.write("sys.exit()\n")
    #
    WDSM2Id.close()
    #
    print 'End processing all files', datetime.today()
    sys.exit()
#