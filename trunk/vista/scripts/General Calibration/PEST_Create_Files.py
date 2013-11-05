import re, os, glob, datetime, random
from vtimeseries import *
from vdss import *
from vista.set import *
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
def countDevParams(devList,coeffList):
    '''
    Count the number of device parameters for PEST in devList,
    using the flow coefficient headers in coeffList. Don't count
    flow coefficients==0
    '''
    count = 0
    # find fields with the flow coeffs
    for rowNo in range(1,len(devList)):
        for name in coeffList:
            loc = devList[0].index(name)
            if float(devList[rowNo][loc]) != 0.0:
                count += 1
    return count
def shortenName(nameList,longName,maxChars,Prefix=False):
    """
    Shorten the long name longName using the following rules:
    * If longName is less than or equal to maxChars, just
      return longName
    * Else, create a short name using optional Prefix, or nothing if empty or False,
      and a counting integer. Try the resulting short name in nameList. If it doesn't
      exist yet, store it in the dictionary and return the short name as the value.
      If it exists, increment the counter and try again until a unique short name is
      produced.
    """
    if len(longName) <= maxChars:
        return longName
    if Prefix:
        prefx = Prefix
    else:
        prefx = ''
    if not longName in nameList:
        shortName = prefx + str(len(nameList))
        nameList.append(longName)
    else:
        shortName = prefx + str(nameList.index(longName))
    if len(shortName) > maxChars:
        raise "Short name for",longName,"is too long, try shorter prefix"
    return shortName
#
if __name__ == '__main__':
    TF = TimeFactory.getInstance()
    filter = Constants.DEFAULT_FLAG_FILTER
    random.seed()
    # Create a .pst file (PEST Control File), a PEST Template File (.tpl),
    # and a .ins file (PEST Instruction File) for the PEST calibration of DSM2
    #
    sq = "'"
    dq = '"'
    bs = "\\"
    useRestart = True
    # DSM2 run dates; these must match the DSM2 calibration BaseRun-1 or BaseRun-2 config file
    if useRestart:
        runStartDateStr = '01OCT2008 0000'
    else:
        runStartDateStr = '01OCT2007 0000'
    runEndDateStr = '01OCT2009 0000'
    #
    runStartDateObj = TF.createTime(runStartDateStr)
    runEndDateObj = TF.createTime(runEndDateStr)
    # create the strings again to ensure that midnight times
    # are consistent (0000 vs 2400)
    runStartDateStr = runStartDateObj.format()
    runEndDateStr = runEndDateObj.format()
    RunTSWin = TF.createTimeWindow(runStartDateObj, runEndDateObj)
    # Qual start date is one day after Hydro
    runStartDateObj_Qual = runStartDateObj + TF.createTimeInterval('1DAY')
    runStartDateStr_Qual = runStartDateObj_Qual.format()
    runEndDateObj_Qual = runEndDateObj - TF.createTimeInterval('1DAY')
    runEndDateStr_Qual = runEndDateObj_Qual.format()
    # Calibration start and end dates must be within the run dates, 
    # and are used for observed and DSM2 comparison data. 
    # A delayed calibration date allows DSM2 to equilibrate. 
    # Later these could be modified to allow for a list of 
    # multiple start/end calibration dates.
    calibStartDateObj = runEndDateObj - TF.createTimeInterval('56DAY')
    calibEndDateObj = runEndDateObj_Qual - TF.createTimeInterval('1DAY')
    calibStartDateStr = calibStartDateObj.format()
    calibEndDateStr = calibEndDateObj.format()
    calibTW = TF.createTimeWindow(calibStartDateObj, calibEndDateObj)
    # DSM2 directories and files
    DSM2Mod = 'HIST-CLB2K'
    RootDir = 'D:/delta/models/'
    CommonDir = RootDir + 'common_input/NAVD/'
    CalibDir = RootDir + '201X-Calibration/'
    TimeSeriesDir = RootDir + 'timeseries/'
    BaseRun0Dir = CalibDir + 'BaseRun-0/Output/'
    BaseRun1Dir = CalibDir + 'BaseRun-1/Output/'
    HydroEchoFile = BaseRun0Dir + 'hydro_echo_' + DSM2Mod + '-BASE-v81_2_0.inp'
    QualEchoFile = BaseRun0Dir + 'qual_ec_echo_' + DSM2Mod + '-BASE-v81_2_0.inp'
    DivRtnQFile = TimeSeriesDir + 'dicu_201203.dss'
    RtnECFile = TimeSeriesDir + 'dicuwq_200611_expand.dss'
    ChanInpFile = 'channel_std_delta_grid_NAVD_20121214.inp'
    GateInpFile = 'gate_std_delta_grid_NAVD_20121214.inp'
    ResInpFile = 'reservoir_std_delta_grid_NAVD_20121214.inp'
    ChanCalibFile = 'Calib-channels.inp'
    GateCalibFile = 'Calib-gates.inp'
    ResCalibFile = 'Calib-reservoirs.inp'
    # PEST outputs for Hydro and Qual runs, these contain output paths
    # matching observed data paths
    DSM2DSSOutHydroFile = 'PEST_Hydro_Out.inp'
    DSM2DSSOutQualFile = 'PEST_Qual_Out.inp'
    # The DSS file containing combined Hydro and Qual output
    DSM2DSSOutFile = 'PESTCalib.dss'
    # The text equivalent of the DSS output, necessary for PEST
    DSM2OutFile = 'PESTCalib.out'
    # DSM2 output locations
    # Each observed B part (location) must have a corresponding
    # DSM2 channel/length in this list of tuples
    # Note: MRZ below is RSAC054, with name changed to fit
    # into PEST's 20-char limit for observation names
    DSM2ObsLoc = [ \
                ('ANC', 52, 366), \
                ('ANH', 52, 366), \
                ('CLL', 436, 5733), \
                ('FAL', 279, 4500), \
                ('HLT', 155, 0), \
                ('HOL', 117, 2670), \
                ('JER', 83, 4213), \
                ('OBI', 106, 2718), \
                ('OH4', 90, 3021), \
                ('OLD', 71, 3116), \
                ('PRI', 42, 286), \
                ('MRZ', 441, 5398), \
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
    ParamDERINCLB = [0.001, 10.0, 50.0, \
                     0.05, \
                     0.0, \
#                     0.01, \
                     0.01, \
                     0.1, 0.1, 0.1, \
                     ]
    #
    # Observed data files, etc.
    # Observed data paths; the DSM2 output paths are determined from these.
    #
    ObsPaths = [ \
            '/CDEC/ANC/EC/.*/15MIN/USBR/', \
            '/CDEC/ANH/EC/.*/1HOUR/DWR-OM/', \
            '/CDEC/JER/EC/.*/1HOUR/USBR/', \
            '/CDEC/CLL/EC/.*/1HOUR/USBR/', \
            '/FILL\+CHAN/MRZ/EC/.*/1HOUR/DWR-DMS-201203_CORRECTED/', \
            '/CDEC/SUT/FLOW/.*/15MIN/USGS/', \
            '/CDEC/HLT/FLOW/.*/15MIN/USGS/', \
            '/CDEC/HOL/FLOW/.*/15MIN/USGS/', \
            '/CDEC/OBI/FLOW/.*/1HOUR/USGS/', \
            '/CDEC/PRI/FLOW/.*/15MIN/USGS/', \
            '/CDEC/SJJ/FLOW/.*/15MIN/USGS/', \
            '/CDEC/ANH/STAGE/.*/1HOUR/DWR-OM/', \
            '/CDEC/FAL/STAGE/.*/15MIN/USGS/', \
            '/CDEC/HOL/STAGE/.*/15MIN/USGS/', \
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
    PESTFile = 'DSM2.pst'
    PESTChanTplFile = ChanCalibFile.split('.')[0] + '.tpl'
    PESTGateTplFile = GateCalibFile.split('.')[0] + '.tpl'
    PESTResTplFile = ResCalibFile.split('.')[0] + '.tpl'
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
    PCFId = open(PESTDir + PESTFile,'w')
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
    # Also produce the DSM2 Hydro and Qual output files for PEST calibration
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
        eti = dsIndex(dataset, calibEndDateObj) + 1
#        print 'Writing path', obsPath
        for ndx in range(sti, eti):
            nObs += 1
            el = dataset.getElementAt(ndx)
            timeObj = TF.createTime(long(el.getX()))
            dateStr = timeObj.format(DefaultTimeFormat('yyyyMMdd'))
            timeStr = timeObj.format(DefaultTimeFormat('HHmm'))
            val = el.getY()
            if dataset.getAttributes().getYUnits().upper() == 'MS/CM':
                val *= 1000.
            valStr = '%15.3f' % (val)
            if filter.isAcceptable(el):
                weight = 1.0
            else:
                weight = 0.0
            OTF1Id.write("%s%s%s%s %s %3.1f %s\n" % (staName, obsGroup, dateStr, timeStr, valStr, weight, obsGroup))
        #
        # write the corresponding DSM2 output line for the observed data path
        tup = [t for t in DSM2ObsLoc if t[0] == staName][0]
        chan_No = tup[1]
        chan_Dist = tup[2]
        fmtStr = '%s    %3d %8d   %s     %s      %s  %s\n'
        # special case for Martinez EC output, to match observed data period type
        if staName == 'MRZ' and obsGroup == 'EC':
            perType = 'ave'
        else:
            perType = 'inst'
        if obsGroup.lower() == 'stage' or \
           obsGroup.lower() == 'flow':
            DSM2HydroId.write(fmtStr % (staName, chan_No, chan_Dist, \
                                        obsGroup, '1HOUR', perType, DSM2DSSOutFile))
        else:
            DSM2QualId.write( fmtStr % (staName, chan_No, chan_Dist, \
                                        obsGroup, '1HOUR', perType, DSM2DSSOutFile))
        #
    DSM2HydroId.write('END')
    DSM2QualId.write('END')
    OTF1Id.close()
    DSM2HydroId.close()
    DSM2QualId.close()
    print 'Wrote ', DSM2HydroId.name
    print 'Wrote ', DSM2QualId.name
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
    if 'GATE' in ParamGroups:
        # count only gates with non-zero flow coefficients
        NPAR += countDevParams(gateWeirList,['CF_FROM_NODE','CF_TO_NODE'])
        NPAR += countDevParams(gatePipeList,['CF_FROM_NODE','CF_TO_NODE'])
    if 'RESERCF' in ParamGroups:
        NPAR += countDevParams(resCFList,['COEF_IN','COEF_OUT'])
    if 'ELEV' in ParamGroups or \
        'WIDTH' in ParamGroups:
        for chan in Channels.getChannels():
                NPAR += len(chan.getXsections())
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
    NTPLFLE = 5
    NINSFLE = 1
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
    PCFId.write('pcf\n')
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
            # Adjust gate coefficients directly, similar to channel parameters.
            # Unfortunately gate data is not in the DSM2 Input methods by Nicky,
            # so it has to be read from the Hydro echo file.
            # Since DSM2 gates are split between pipes and weirs,
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
            gateList = []
            for row in gateWeirList:
                try: PARVAL1 = float(row[CF_FromLoc])
                except: continue    # headers, just continue
                # skip zero-value gate coeffs
                if PARVAL1 != 0.0:
                    PARLBND = PARVAL1 * 0.5
                    PARUBND = PARVAL1 * 1.5
                    fullName = 'WEIRCFFR:' + row[uniq1] + ':' + row[uniq2]
                    # PEST requires the PARNME be less than or equal to 12 chars;
                    # pass through function to ensure that
                    PARNME = shortenName(gateList,fullName,12,'WEIRCFFR:')
                    PCFId.write('%s %s %s %10.3f %10.3f %10.3f %s %5.2f %5.2f %1d\n' % \
                        (PARNME,PARTRANS,PARCHGLIM,PARVAL1,PARLBND,PARUBND,PARGP,SCALE,OFFSET,DERCOM))
                PARVAL1 = float(row[CF_ToLoc])
                if PARVAL1 != 0.0:
                    PARLBND = PARVAL1 * 0.5
                    PARUBND = PARVAL1 * 1.5
                    fullName = 'WEIRCFTO:' + row[uniq1] + ':' + row[uniq2]
                    PARNME = shortenName(gateList,fullName,12,'WEIRCFTO:')
                    PCFId.write('%s %s %s %10.3f %10.3f %10.3f %s %5.2f %5.2f %1d\n' % \
                        (PARNME,PARTRANS,PARCHGLIM,PARVAL1,PARLBND,PARUBND,PARGP,SCALE,OFFSET,DERCOM))
            headersList = gatePipeList[0]
            uniq1 = headersList.index('GATE_NAME')
            uniq2 = headersList.index('DEVICE')
            CF_FromLoc = headersList.index('CF_FROM_NODE')
            CF_ToLoc = headersList.index('CF_TO_NODE')
            for row in gatePipeList:
                try: PARVAL1 = float(row[CF_FromLoc])
                except: continue    # headers, just continue
                if PARVAL1 != 0.0:
                    PARLBND = PARVAL1 * 0.5
                    PARUBND = PARVAL1 * 1.5
                    fullName = 'PIPECFFR:' + row[uniq1] + ':' + row[uniq2]
                    PARNME = shortenName(gateList,fullName,12,'PIPECFFR:')
                    PCFId.write('%s %s %s %10.3f %10.3f %10.3f %s %5.2f %5.2f %1d\n' % \
                        (PARNME,PARTRANS,PARCHGLIM,PARVAL1,PARLBND,PARUBND,PARGP,SCALE,OFFSET,DERCOM))
                PARVAL1 = float(row[CF_ToLoc])
                if PARVAL1 != 0.0:
                    PARLBND = PARVAL1 * 0.5
                    PARUBND = PARVAL1 * 1.5
                    fullName = 'PIPECFTO:' + row[uniq1] + ':' + row[uniq2]
                    PARNME = shortenName(gateList,fullName,12,'PIPECFTO:')
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
            resList = []
            for row in resCFList:
                try: PARVAL1 = float(row[CF_InLoc])
                except: continue    # headers, just continue
                if PARVAL1 != 0.0:
                    PARLBND = PARVAL1 * 0.5
                    PARUBND = PARVAL1 * 1.5
                    fullName = 'RESCFIN:' + row[uniq1] + ':' + row[uniq2]
                    PARNME = shortenName(resList,fullName,12,'RESCFIN:')
                    PCFId.write('%s %s %s %10.3f %10.3f %10.3f %s %5.2f %5.2f %1d\n' % \
                        (PARNME,PARTRANS,PARCHGLIM,PARVAL1,PARLBND,PARUBND,PARGP,SCALE,OFFSET,DERCOM))
                PARVAL1 = float(row[CF_OutLoc])
                if PARVAL1 != 0.0:
                    PARLBND = PARVAL1 * 0.5
                    PARUBND = PARVAL1 * 1.5
                    fullName = 'RESCFOUT:' + row[uniq1] + ':' + row[uniq2]
                    PARNME = shortenName(resList,fullName,12,'RESCFOUT:')
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
                try: CPartUp = srcInput.path.upper().split('/')[3]
                except: continue
                if CPartUp == paramUp:
                    node3 = "%03d" % int(srcInput.nodeId)
                    if paramUp == 'DRAIN-FLOW':
                        shortName = 'DRN-Q'
                    else:
                        shortName = 'DIV-Q'  
                    PARNME = shortName+node3  # shorten param name to be less than 12 chars
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
                        PARNME = 'DRN-EC' + node3
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
    print 'Wrote', PIAFId.name
    print 'Wrote', PIXFId.name
    print 'Wrote', PTAFId.name
    print 'Wrote', PTXFId.name
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
                ('* model command line', 'dsm2run.bat'))
    PCFId.write('%s\n%s %s\n%s %s\n%s %s\n%s %s\n%s %s\n%s %s\n%s\n' % \
                ('* model input/output', \
                PESTChanTplFile, ChanCalibFile, \
                PESTGateTplFile, GateCalibFile, \
                PESTResTplFile, ResCalibFile, \
                PESTTplAgFile, PESTInpAgFile, \
                PESTTplXCFile, PESTInpXCFile, \
                PESTInsFile, DSM2OutFile, \
                '* prior information'))
    PCFId.close()
    print 'Wrote',PCFId.name
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
#         if re.search('^ *CHANNEL *$',line,re.I):
#             PTFId.write(line)
#             continue
        if re.search('^ *END',line,re.I):
            # end of section
            channelLines = False
#             if channelLines:
#                 PTFId.write(line)
#                 break
        if channelLines:
            lineParts = line.split()
            # CHAN_NO  LENGTH  MANNING  DISPERSION  UPNODE  DOWNNODE
            chanNo = int(lineParts[0])
            upNode = int(lineParts[4])
            downNode = int(lineParts[5])
            PTFId.write('%3d @LENGTH%03d@ @MANN%03d @ @DISP%03d @ %3d %3d\n' % \
                        (chanNo, chanNo, chanNo, chanNo, upNode, downNode))
        else:
            PTFId.write(line)
        if re.search('CHAN_NO +LENGTH +MANNING +DISPERSION',line,re.I):
            # channel block header line, channel lines follow
            channelLines = True
#            PTFId.write(line)
    PTFId.close()
    DSM2InpId.close()
    print 'Wrote',PTFId.name
    # DSM2 Gate input template
    PTFId = open(PESTDir + PESTGateTplFile,'w')
    DSM2InpId = open(CommonDir + GateInpFile, 'r')
    PTFId.write('ptf |\n')
    # read each line from the DSM2 gate input file;
    # for gate weir & pipe device lines, replace To and From flow coefficients 
    # with PEST placeholder names
    gateLines = False
    for line in DSM2InpId:
        if re.search('^ *GATE_[A-Z]+_DEVICE *$',line,re.I):
            PTFId.write(line)
            # Pipe or Weir section?   
            if re.search('GATE_PIPE_DEVICE', line, re.I):
                headersList = gatePipeList[0]
                name = 'PIPE'
            if re.search('GATE_WEIR_DEVICE',line,re.I):
                headersList = gateWeirList[0]
                name = 'WEIR'
        if re.search('^ *END',line,re.I):
            # end of section
            if gateLines:
                PTFId.write(line)
                gateLines = False
        if gateLines:
            lineParts = line.split()
            # GATE_NAME DEVICE NDUPLICATE (RADIUS|HEIGHT) ELEV CF_FROM_NODE CF_TO_NODE DEFAULT_OP
            gateName = lineParts[headersList.index('GATE_NAME')]
            devName = lineParts[headersList.index('DEVICE')]
            # accept only non-zero coeffs
            if float(lineParts[CF_FromLoc]) != 0.0:
                # recreate shortened gate name
                fullName = name+'CFFR:'+gateName+':'+devName
                shortName = shortenName(gateList,fullName,12,name+'CFFR:')
                lineParts[CF_FromLoc] = '|' + shortName + '|' 
            # accept only non-zero coeffs
            if float(lineParts[CF_ToLoc]) != 0.0:
                fullName = name+'CFTO:'+gateName+':'+devName
                shortName = shortenName(gateList,fullName,12,name+'CFTO:')
                lineParts[CF_ToLoc] = '|' + shortName + '|'
            #
            for i in range(len(lineParts)): 
                PTFId.write('%s ' % lineParts[i])
            PTFId.write('\n')
        if re.search('GATE_NAME +.*CF_(FROM|TO)_NODE +.*CF_(FROM|TO)_NODE',line,re.I):
            # gate block header line, gate lines follow
            gateLines = True
            PTFId.write(line)
            # find which fields have the gate flow coeffs (CF_FROM_NODE and CF_TO_NODE)
            CF_FromLoc = headersList.index('CF_FROM_NODE')
            CF_ToLoc = headersList.index('CF_TO_NODE')
    PTFId.close()
    DSM2InpId.close()
    print 'Wrote',PTFId.name
    # DSM2 Reservoir input template
    PTFId = open(PESTDir + PESTResTplFile,'w')
    DSM2InpId = open(CommonDir + ResInpFile, 'r')
    PTFId.write('ptf |\n')
    # read each line from the DSM2 reservoir input file;
    # for reservoir coefficient lines, replace In and Out flow coefficients 
    # with PEST placeholder names
    resCFLines = False
    for line in DSM2InpId:
        if re.search('^ *RESERVOIR_CONNECTION *$',line,re.I):
            PTFId.write(line)
        if re.search('^ *END',line,re.I):
            if resCFLines:
                # end of reservoir coefficient lines
                PTFId.write(line)
                resCFLines = False
        if resCFLines:
            lineParts = line.split()
            # RES_NAME NODE COEF_IN COEF_OUT
            resName = lineParts[headersList.index('RES_NAME')]
            resNode = lineParts[headersList.index('NODE')]
            # accept only non-zero coeffs
            if float(lineParts[CF_InLoc]) != 0.0:
                shortName = shortenName(resList,'RESCFIN:' + resName + ':' + resNode,12,'RESCFIN:')
                lineParts[CF_InLoc] = '|' + shortName + '|' 
            if float(lineParts[CF_OutLoc]) != 0.0:
                shortName = shortenName(resList,'RESCFOUT:' + resName + ':' + resNode,12,'RESCFOUT:')
                lineParts[CF_OutLoc] = '|' + shortName + '|'
            for i in range(len(lineParts)): 
                PTFId.write('%s ' % lineParts[i])
            PTFId.write('\n')
        if re.search('RES_NAME +.*COEF_IN',line,re.I):
            # reservoir coefficient block header line, reservoir lines follow
            resCFLines = True
            PTFId.write(line)
            headersList = resCFList[0]
            # find which fields have the reservoir flow coeffs (COEF_IN and COEF_OUT)
            CF_InLoc = headersList.index('COEF_IN')
            CF_OutLoc = headersList.index('COEF_OUT')
    PTFId.close()
    DSM2InpId.close()
    print 'Wrote',PTFId.name
    ##
    # Create the PEST_post_DSM2Run.py.py file for post-processing DSM2 calibration runs.
    # The post-processing generates text output of the DSS calibration stations,
    # and the PEST instruction (.ins) file. 
    preProcFile = 'PEST_pre_DSM2Run.py'
    postProcFile = 'PEST_post_DSM2Run.py'
    sq = "'"
    dq = '"'
    obsGroupsStr =  dq + '", "'.join(obsGroups) + dq
    WDSM2Id = open(PESTDir + postProcFile, 'w')
    WDSM2Id.write('import sys, os\n')
    WDSM2Id.write('from vtimeseries import *\n')
    WDSM2Id.write('from vdss import *\n')
    WDSM2Id.write('from vista.set import *\n')
    WDSM2Id.write('#from vista.db.dss import *\n')
    WDSM2Id.write('from vutils import *\n')
    WDSM2Id.write('from vista.time import TimeFactory\n')
    WDSM2Id.write('if __name__ == "__main__":\n')
    WDSM2Id.write("    TF = TimeFactory.getInstance()\n")
    WDSM2Id.write("    tw = TF.createTimeWindow('" + calibStartDateStr + " - " + \
                  calibEndDateStr + "')\n")
    WDSM2Id.write("    # This post-processor was generated by PEST_Create_Files.py\n" + \
                  "    # It translates DSM2 DSS output for calibration to a text file,\n" + \
                  "    # then generates the matching PEST instruction file for the output.\n" + \
                  "    # On initial PEST start, run this using the base-1 dss output.\n")
    WDSM2Id.write("    DSM2DSSOutFile = sys.argv[1]\n")
    WDSM2Id.write("    tempfile = 'temp.out'\n")
    WDSM2Id.write("    try: fid = open(" + sq + DSM2OutFile + sq + ", 'w')\n")
    WDSM2Id.write("    except: raise 'Error opening ' + " + DSM2OutFile + "\n")
    WDSM2Id.write("    for dataType in [" + obsGroupsStr + "]:\n")
    WDSM2Id.write("        dssgrp = opendss(DSM2DSSOutFile)\n")
    WDSM2Id.write("        # use only 3-letter CDEC-style stations for PEST 20-char limit in .ins file\n")
    WDSM2Id.write("        dssgrp.filterBy('/[A-Z0-9][A-Z0-9][A-Z0-9]/'+dataType+'/')\n")
    WDSM2Id.write("        for dssdr in dssgrp.getAllDataReferences():\n")
    WDSM2Id.write("            try: dssdr = DataReference.create(dssdr,tw)\n")
    WDSM2Id.write("            except: raise 'Error with DataReference(dssdr)'\n")
    WDSM2Id.write("            writeascii(tempfile, dssdr.getData())\n")
    WDSM2Id.write("            tid = open(tempfile, 'r')\n")
    WDSM2Id.write("            fid.write(tid.read().replace('\t','    '))\n")
    WDSM2Id.write("            tid.close()\n")
    WDSM2Id.write("    fid.close()\n")
    WDSM2Id.write("    print 'Wrote', fid.name\n")
    WDSM2Id.write("    if os.path.exists(tempfile):\n")
    WDSM2Id.write("       os.remove(tempfile)\n")
    WDSM2Id.write("    # generate PEST instruction (.ins) file\n")
    WDSM2Id.write("    fid = open('" + PESTInsFile + "', 'w')\n")
    WDSM2Id.write("    tid = open('" + DSM2OutFile + "', 'r')\n")
    WDSM2Id.write("    fid.write('pif @\\n')\n")
    WDSM2Id.write("    for line in tid:\n")
    WDSM2Id.write("        if re.search('^$', line):\n")
    WDSM2Id.write("            fid.write('@Units :@\\n')\n")
    WDSM2Id.write("            continue\n")
    WDSM2Id.write("        lineSplit = line.split()\n")
    WDSM2Id.write("        if line.find('Location: ') > -1:\n")
    WDSM2Id.write("            locStr = lineSplit[1].upper()\n")
    WDSM2Id.write("            continue\n")
    WDSM2Id.write("        if line.find('Type: ') > -1:\n")
    WDSM2Id.write("            typeStr = lineSplit[1].upper()\n")
    WDSM2Id.write("            continue\n")
    WDSM2Id.write("        if re.search('^[0-9][0-9][A-Z][A-Z][A-Z][12][90][78901][0-9] [0-2][0-9][0-9][0-9][ \t]+[0-9.-]+$',line) > -1:\n")
    WDSM2Id.write("            timeObj = TF.createTime(lineSplit[0]+' '+lineSplit[1])\n")
    WDSM2Id.write("            dateStr = timeObj.format(DefaultTimeFormat('yyyyMMdd'))\n")
    WDSM2Id.write("            timeStr = timeObj.format(DefaultTimeFormat('HHmm'))\n")
    WDSM2Id.write("            dataID = 'L1 (' + locStr + typeStr + dateStr + timeStr + ')15:30'\n")
    WDSM2Id.write("            fid.write(dataID + '\\n')\n")
    WDSM2Id.write("    fid.close()\n")
    WDSM2Id.write("    tid.close()\n")
    WDSM2Id.write("    print 'Wrote', fid.name\n")
    WDSM2Id.write("    sys.exit(0)\n")
    print 'Wrote', WDSM2Id.name
    WDSM2Id.close()
    #
    ## Create the run-time *.bat file for the Condor runs of PEST;
    ## use BEOPest, not the Parallel Pest.
    WDSM2Id = open(PESTDir + 'dsm2-run4BEOPEST.bat', 'w')
    WCONId = open(PESTDir + 'dsm2run.bat', 'w')
    # Create condor_dsm2.bat file for Hydro and Qual runs
    WCONId.write("@echo off\n")
    WCONId.write("set VISTABINDIR=c:\\condor\\vista\\bin\\\n")
    WCONId.write("set PESTBINDIR=c:\\condor\\PEST\\bin\\\n")
    WCONId.write("setlocal\n")
    WCONId.write("set /a bigdelay=(%random% %% 100)+50\n")
    WCONId.write("echo Delay %bigdelay% seconds\n")
    WCONId.write("ping -n %bigdelay% 127.0.0.1 > nul\n")
    WCONId.write("echo Running on %COMPUTERNAME%\n")
    WCONId.write("echo Running Pre-Processor\n")
    WCONId.write("call %VISTABINDIR%vscript.bat " + preProcFile + "\n")
    WCONId.write("if ERRORLEVEL 1 exit /b 1\n")
    WCONId.write("if exist PESTCalib.dss del /f/q PESTCalib.dss\n")
    WCONId.write("set /a smalldelay=(%random% %% 10)+5\n")
    WCONId.write("ping -n %smalldelay% 127.0.0.1 > nul\n")
    WCONId.write("rem run times\n")
    WCONId.write("set START_DATE=" + runStartDateStr.split()[0] + "\n")
    WCONId.write("set START_TIME=" + runStartDateStr.split()[1] + "\n")
    WCONId.write("set END_DATE=" + runEndDateStr.split()[0] + "\n")
    WCONId.write("set END_TIME=" + runEndDateStr.split()[1] + "\n")
    WCONId.write("set QUAL_START_DATE=" + runStartDateStr_Qual.split()[0] + "\n")
    WCONId.write("set QUAL_END_DATE=" + runEndDateStr_Qual.split()[0] + "\n")
    WCONId.write("echo Running hydro\n")
    WCONId.write("time /t\n")
    WCONId.write("hydro.exe hydro.inp\n")
    WCONId.write("if ERRORLEVEL 1 exit /b 1\n")
    WCONId.write("time /t\n")
    WCONId.write("echo Running qual\n")
    WCONId.write("time /t\n")
    WCONId.write("qual.exe qual_ec.inp\n")
    WCONId.write("if ERRORLEVEL 1 exit /b 1\n")
    WCONId.write("time /t\n")
    WCONId.write("set /a smalldelay=(%random% %% 10)+5\n")
    WCONId.write("ping -n %smalldelay% 127.0.0.1 > nul\n")
    WCONId.write("\n")
    WCONId.write("rem post-process to prepare output for PEST\n\n")
    WCONId.write("echo Running Post-Processor\n")
    WCONId.write("call %VISTABINDIR%vscript.bat " + postProcFile + \
                  " " + DSM2DSSOutFile + "\n")
    WCONId.write("if ERRORLEVEL 1 exit /b 1\n")
    WCONId.write("rem Idiotic MS equivalent of touch\n")
    WCONId.write("copy /b dummy.txt +,,\n")
    WCONId.write("set /a smalldelay=(%random% %% 10)+5\n")
    WCONId.write("ping -n %smalldelay% 127.0.0.1 > nul\n")
    WCONId.write("call %PESTBINDIR%pestchek.exe DSM2\n")
    WCONId.write("if ERRORLEVEL 1 exit /b 1\n")
    WCONId.write("exit /b 0\n")
    WCONId.close()
    print 'Wrote', WCONId.name
    WDSM2Id.write("echo off\n")
    WDSM2Id.write("Rem This file created by PEST_Create_Files.py\n")
    WDSM2Id.write("Rem Calibrate DSM2 using BEOPEST & HTCondor.\n")
    WDSM2Id.write("setlocal\n")
    WDSM2Id.write("set HYDROEXE=" + RootDir.replace("/","\\") + "hydro.exe\n")
    WDSM2Id.write("set QUALEXE=" + RootDir.replace("/","\\") + "qual.exe\n")
    WDSM2Id.write("set PESTDIR=" + PESTDir.replace("/","\\") + "\n")
    WDSM2Id.write("set CONDORBINDIR=c:\\condor\\bin\\\n")
    WDSM2Id.write("set VISTABINDIR=c:\\condor\\vista\\bin\\\n")
    WDSM2Id.write("set PESTBINDIR=c:\\condor\\PEST\\bin\\\n")
    WDSM2Id.write("set DSM2RUN0=BASE-v81_2_0\n")
    WDSM2Id.write("set STUDYNAME=" + PESTFile.replace(".pst","") + "\n")
    WDSM2Id.write("\n")
    WDSM2Id.write("set TSFILES=dicu_201203-calib.dss, dicu_201203.dss, dicuwq_200611_expand-calib.dss, " + \
                  "dicuwq_200611_expand.dss, dicuwq_3vals_extended.dss, events.dss, " + \
                  "gates-v8-06212012.dss, hist_19902012.dss\n")
    WDSM2Id.write("set CMNFILES=boundary_flow_delta_historical_20090715.inp, " + \
                  "boundary_stage_delta_historical_NAVD_20121214.inp, channel_ic_std_delta_grid_NAVD_20121214.inp, " + \
                  "channel_std_delta_grid_NAVD_20121214.inp, gate_std_delta_grid_NAVD_20121214.inp, " + \
                  "group_sourcetracking_20090715.inp, node_concentration_delta_historical_qual_20121214.inp, " + \
                  "node_concentration_dicu_ec_20090715.inp, node_concentration_jones_qual_20090715.inp, " + \
                  "oprule_hist_temp_barriers_NAVD_20121214.inp, oprule_historical_gate_20110418.inp, " + \
                  "output_channel_hydro_v81_calibration_20121214.inp, output_channel_std_hydro_boundary_20090715.inp, " + \
                  "output_channel_std_hydro_rki_20090715.inp, output_channel_std_qual_named_20090715.inp, " + \
                  "output_channel_std_qual_rki_20090715.inp, output_channel_std_qual_rki_addmore_20121214.inp, " + \
                  "output_reservoir_std_hydro_named_20090715.inp, output_reservoir_std_qual_named_20090715.inp, " + \
                  "reservoir_concentration_dicu_ec_20090715.inp, reservoir_ic_std_delta_grid_NAVD_20121214.inp, " + \
                  "reservoir_std_delta_grid_NAVD_20121214.inp, source_flow_delta_historical_20110708.inp, " + \
                  "source_flow_dicu_historical_20090715.inp, source_flow_jones_hydro_20090806.inp\n")
    WDSM2Id.write("set PESTFILES=" + PESTFile + ", " + PESTChanTplFile + ", " + PESTGateTplFile + ", " + \
                  PESTResTplFile + ", " + PESTInsFile + ", " + PESTInpAgFile + ", " + PESTTplAgFile + ", " + \
                  PESTInpXCFile + ", " + PESTTplXCFile + "\n")
    WDSM2Id.write("\n")
    WDSM2Id.write("rem create a scratch directory for condor runs\n")
    WDSM2Id.write("set RUNDIR=%PESTDIR%Condor\n")
    WDSM2Id.write("\n")
    WDSM2Id.write("if not exist %RUNDIR% mkdir %RUNDIR%\n")
    WDSM2Id.write("del /q %RUNDIR%\\*\n")
    WDSM2Id.write("\n")
    WDSM2Id.write("@copy /b %HYDROEXE% %RUNDIR%\\\n")
    WDSM2Id.write("@copy /b %QUALEXE% %RUNDIR%\\\n")
    WDSM2Id.write("@copy /a dsm2run.bat %RUNDIR%\\\n")
    WDSM2Id.write("@xcopy /q " + re.sub("/$","",TimeSeriesDir).replace("/",bs) + " %RUNDIR%\\\n")
    WDSM2Id.write("@xcopy /q " + re.sub("/$", "", CommonDir).replace("/",bs) + " %RUNDIR%\\\n")
    WDSM2Id.write("\n")
    WDSM2Id.write("@copy /y *.py %RUNDIR%\\\n")
    WDSM2Id.write("@copy /y *.pst %RUNDIR%\\\n")
    WDSM2Id.write("@copy /y *.tpl %RUNDIR%\\\n")
    WDSM2Id.write("@copy /y *.inp %RUNDIR%\\\n")
    WDSM2Id.write("@copy /y dummy.txt %RUNDIR%\\\n")

    WDSM2Id.write("@copy /y dsm2-base.sub %RUNDIR%\\dsm2.sub\n")
    WDSM2Id.write("@copy /y " + BaseRun0Dir.replace("/","\\") + "*%DSM2RUN0%.?rf %RUNDIR%\\\n")
    WDSM2Id.write("@copy /b /y " + BaseRun1Dir.replace("/","\\") + DSM2DSSOutFile + " %RUNDIR%\\\n")
    WDSM2Id.write("\n")
    WDSM2Id.write("cd %RUNDIR%\n")
    WDSM2Id.write("@ren config_calib.inp config.inp\n")
    if useRestart:
        WDSM2Id.write("@ren hydro_calib_restart.inp hydro.inp\n")
        WDSM2Id.write("@ren qual_ec_calib_restart.inp qual_ec.inp\n")
    else:
        WDSM2Id.write("@ren hydro_calib_cold.inp hydro.inp\n")
        WDSM2Id.write("@ren qual_ec_calib_cold.inp qual_ec.inp\n")
    WDSM2Id.write("rem Add calibration output to Hydro and Qual .inp files\n")
    WDSM2Id.write("echo OUTPUT_TIME_SERIES >> hydro.inp\n")
    WDSM2Id.write("echo " + DSM2DSSOutHydroFile + " >> hydro.inp\n")
    WDSM2Id.write("echo END >> hydro.inp\n")
    WDSM2Id.write("echo OUTPUT_TIME_SERIES >> qual_ec.inp\n")
    WDSM2Id.write("echo " + DSM2DSSOutQualFile + " >> qual_ec.inp\n")
    WDSM2Id.write("echo END >> qual_ec.inp\n")
    WDSM2Id.write("\n")
    WDSM2Id.write("rem finish Condor submit file for PEST\n")
    WDSM2Id.write("echo transfer_input_files = dsm2run.bat, " + \
                  preProcFile + ", " + postProcFile + ", " + \
                  DSM2DSSOutHydroFile + ", " + DSM2DSSOutQualFile + ", %PESTFILES%, " + \
                  "hydro.exe, qual.exe, " \
                  + DSM2Mod + "-%DSM2RUN0%.qrf, " \
                  + DSM2Mod + "-%DSM2RUN0%.hrf, config.inp, " + \
                  "calib-xsects.inp, calib-reservoirs.inp, calib-gates.inp, dummy.txt, " + \
                  "hydro.inp, qual_ec.inp, %CMNFILES%, %TSFILES% >> %RUNDIR%\\dsm2.sub\n")
    WDSM2Id.write("\n")
    WDSM2Id.write("echo arguments = %STUDYNAME% /H bdomo-002:4004 >> %RUNDIR%\\dsm2.sub\n")
    WDSM2Id.write("echo queue 15 >> %RUNDIR%\\dsm2.sub\n")
    WDSM2Id.write("\n")
    WDSM2Id.write("rem prepare base run output for PEST/DSM2 run\n")
    WDSM2Id.write("call %VISTABINDIR%vscript.bat " + preProcFile + "\n")
    WDSM2Id.write("call %VISTABINDIR%vscript.bat " + postProcFile + \
                  " " + DSM2DSSOutFile + "\n")
    WDSM2Id.write("\n")
    WDSM2Id.write("%CONDORBINDIR%condor_submit dsm2.sub\n")
    WDSM2Id.write("echo Submitted beopest slaves jobs to condor.\n")
    WDSM2Id.write("popd\n")
    WDSM2Id.write("%PESTBINDIR%beopest64.exe %STUDYNAME% /H :4004\n")
    #
    WDSM2Id.close()
    print 'Wrote', WDSM2Id.name
    print 'End processing all files', datetime.today()
    sys.exit()
#