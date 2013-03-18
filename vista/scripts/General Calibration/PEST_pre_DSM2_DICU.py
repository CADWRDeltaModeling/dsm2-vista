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
# Pre-processor for DSM2 Hydro and Qual runs for PEST calibration.
# Since DIV, DRAIN (both flows), and DRAIN-EC are considered calibration
# parameters, their values will be adjusted during the PEST calibration
# with this pre-processor before each pair of Hydro/Qual runs.

def updateDSSAgVals(In_DSSFile, Out_DSSFile, AgCoeffs):
    count = 0
    try: os.remove(Out_DSSFile)
    except: pass
    dss_group = opendss(In_DSSFile)
    for dataref in dss_group.getAllDataReferences():
        dataset = dataref.getData()
        inpath = dataref.getPathname()
        C = inpath.getPart(inpath.C_PART)
        if C in AgCoeffs:
            dataref = dataref * float(AgCoeffs.get(C))
            # write updated value to new DSS file
            writedss(Out_DSSFile, inpath.getFullPath(), dataref.getData())
            count+=1
    return count
    
if __name__ == '__main__':
    TF = TimeFactory.getInstance()
    filter = Constants.DEFAULT_FLAG_FILTER
    # Pre-processor for DSM2 Hydro and Qual runs for PEST calibration.
    # Since DIV, DRAIN (both flows), and DRAIN-EC are considered calibration
    # parameters, their values will be adjusted during the PEST calibration
    # with this pre-processor before each pair of Hydro/Qual runs.
    #
    BaseDir = 'D:/delta/models/Historical_v81_Beta_Release/'
    CommonDir = BaseDir + 'common_input/NAVD/'
    TSDir = BaseDir + 'timeseries/'
    CalibDir = BaseDir + '201X-Calibration/'
    PESTDir = CalibDir + 'PEST/Calib/'
    # this name must be the same as in PEST_Create_Files
    PESTInpAgFile = 'AgCalibCoeffs.inp'
    # DICU-related names must be the same as the DSM2 calibration run
    In_DICUfile = 'dicu_201203.dss'
    In_DICUECfile = 'dicuwq_200611_expand.dss'
    Out_DICUfile = 'dicu_201203-calib.dss'
    Out_DICUECfile = 'dicuwq_200611_expand-calib.dss'
    # read the Ag calibration coefficients, 3 lines, format:
    # 'DIV-FLOW|DRAIN-FLOW|DRAIN-EC' <whitespace> CoeffValue
    PIAFID = open(PESTDir + PESTInpAgFile,'r')
    AgCoeffs = []
    for line in PIAFID:
        AgCoeff = line.split()
        AgCoeffs.append(AgCoeff)
    PIAFID.close()
    AgCoeffs = dict(AgCoeffs)
    countQ = updateDSSAgVals(TSDir + In_DICUfile, TSDir + Out_DICUfile, AgCoeffs)
    countEC = updateDSSAgVals(TSDir + In_DICUECfile, TSDir + Out_DICUECfile, AgCoeffs)
    print 'Updated', countQ, 'Ag diversion/drainage flow values and', countEC, 'Ag return quality values.'
    sys.exit()
    