__doc__ ="""

"""
"""
A collection of utilities for vista
"""
import sys
from vista.set import TimeSeriesMath
from java.lang import System

TimeSeriesMath.DUMB_PATCH = 0 # disable dumb patches per Eli's recommendation
# check for jnios
vh = System.getProperty("vista.home")
try:
    if not vh:
	System.loadLibrary("errno")
	System.loadLibrary("posix")
    else:
	osname = System.getProperty("os.name")
	fs = System.getProperty("file.separator")
	if string.find(osname,"Sun") != -1:
	    System.load(vh+fs+"lib"+fs+"liberrno.so")
	    System.load(vh+fs+"lib"+fs+"libposix.so")
	elif string.find(osname,"Win") != -1:
	    System.load(vh+fs+"lib"+fs+"errno.dll")
	    System.load(vh+fs+"lib"+fs+"posix.dll")
	else:
	    System.loadLibrary("errno")
	    System.loadLibrary("posix")
except:
    pass
# check for display
from java.awt import Toolkit
display = 1
try :
    tk = Toolkit.getDefaultToolkit()
except:
    print 'Problem with display'
    display = 0
#
from vista.db.dss import DSSUtil
from vista.app import MainProperties
DSSUtil.setAccessProperties(MainProperties.getProperties())
#
from vtidefile import *
from vdss import *
from vmath import *
from vtimeseries import *
from vdisplay import *
from vchecker import *
#
def exit():
    sys.exit()
#
def mergeWithFlags(ds1,ds2):
    """
    mergeWithFlags(ds1,ds2)
    Merge dataset1 into dataset2 using flags.  Priority:
    FlagUtils.OK_FLAG, QUESTIONABLE_FLAG, MISSING_FLAG, REJECT_FLAG, UNSCREENED_FLAG
    If ds1Flag priority >= ds2Flag priority, use ds1 value;
    If ds1Flag priority < ds2Flag priority, use ds2 value.
    Return the merged dataset. If a dataset does not have flags,
    UNSCREENED_FLAG is assumed.
    """
    # datasets should be similar
    ds1_rts=isinstance(ds1,RegularTimeSeries)
    ds2_rts=isinstance(ds2,RegularTimeSeries)
    if ds1_rts != ds2_rts:
        raise "Datasets must both be RS or ITS timeseries for mergeWithFlags."
    if ds1_rts and ds2_rts:
        if ds1.getTimeInterval().compare(ds2.getTimeInterval()) !=0 :
            raise "Incompatible time intervals for %s and %s"%(ds1.getName(),ds2.getName())
    tw = ds1.getTimeWindow()
    if not tw.isSameAs(ds2.getTimeWindow()):
        raise 'TimeWindow for %s is %s & %s is %s\n'\
            %(ds1.getName(),str(ds1.getTimeWindow()),\
            ds2.getName(),str(ds2.getTimeWindow()))
    # perform merge
    dsm=ds1
    pri_dict={\
        FlagUtils.UNSCREENED_FLAG:1, \
        FlagUtils.REJECT_FLAG:2,\
        FlagUtils.MISSING_FLAG:3,\
        FlagUtils.QUESTIONABLE_FLAG:4,\
        FlagUtils.OK_FLAG:5\
        }
    for i in range(len(ds1)):
        # get the data element
        e1 = ds1.getElementAt(i)
        e2 = ds2.getElementAt(i)
        if e1.getX != e2.getX: continue
        e1fp=pri_dict[FlagUtils.getQualityFlag(e1)]
        e2fp=pri_dict[FlagUtils.getQualityFlag(e2)]
        if e1fp < e2fp: dsm.putElementAt(i,e2) # use element e2 in the merged dataset
    return dsm
def dsAddFlags(dataset):
    """
    dsAddFlags(dataset)
    Add UNSCREENED_FLAG to dataset that does not have any flags
    """
    if dataset.isFlagged(): return dataset
    # create copy of incoming dataset but with flags
    fa = jarray.zeros(len(dataset),'i')
    if dataset.getAttributes().getType() == DataType.REGULAR_TIME_SERIES:    #RTS
        dataset=RegularTimeSeries(dataset.getName(),str(dataset.getStartTime()),\
                             str(dataset.getTimeInterval()),dataset.getYArray(),\
                             fa, dataset.getAttributes())
    else:   # ITS
        xa=jarray.zeros(len(dataset),'d')
        ya=jarray.zeros(len(dataset),'d')
        for i in range(len(dataset)):
            xa[i]=dataset[i].getX()
            ya[i]=dataset[i].getY()
        dataset=IrregularTimeSeries(dataset.getName(),xa,ya,fa,dataset.getAttributes())
    return dataset