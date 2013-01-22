import os, sys, glob
from vtimeseries import *
from vdss import *
from vista.set import *
from vista.set import PathnamePredicate
from vista.db.dss import *
from vutils import *
from vista.time import TimeFactory
if __name__ == '__main__':
    """
    Compare a series of RTS paths for good data;
    return a list of start-end dates when all paths have good data.
    Input is a DSS filename, all RTS paths of similar time intervals
    will be grouped and compared. 
    """
    TF = TimeFactory.getInstance()
    # Start by creating an initial, dummy RTS path with unscreened
    # data. The path length will be the earlies and latest start/end
    # dates in the file.
    if len(sys.argv) != 2:
        print " **** Please specify the input DSS file ****"
        sys.exit()
    else:
        DSSFile = sys.argv[1]
        if not os.path.isfile(DSSFile):
            print 'Error: DSS file not found: '+DSSFile
            sys.exit()
    # look for only 15MIN, 1HOUR, and 1DAY time intervals
    for PFilter in ['15MIN', '1HOUR', '1DAY']:
        dss_group=opendss(DSSFile)
        dss_group.filterBy(PFilter)
        if len(dss_group) == 0:
            continue
        startDate = TF.createTime('01JAN2100 0000')
        endDate = TF.createTime('01JAN1900 0000')
        for dataref in dss_group.getAllDataReferences():
            sDate=dataref.getTimeWindow().getStartTime()
            if sDate.compare(startDate) < 0:
                startDate = sDate
            eDate=dataref.getTimeWindow().getEndTime()
            if eDate.compare(endDate) > 0:
                endDate = eDate
        print PFilter, startDate, endDate
    sys.exit()
#