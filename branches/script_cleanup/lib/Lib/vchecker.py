import string
from vista.set import Constants,CompositeFilter,DataReference
from vtimeseries import timewindow
from vdss import findpath,opendss,writedss
from vista.db.dss import DSSUtil
from vtimeseries import *
from vdss import *
from vista.set import *
from vista.db.dss import *

__doc__="""
vchecker:
This module contains the functions for checking data based on its value, rate of
change, quality flag values etcetra.
"""
def flagData(ftype, dataset, valArray, log = 'flag.log',Special = False):
    """
    flagData(ftype, dataset, valArray, log = 'flag.log', Special = False):
    Flags a datastream's values for bad data:
    ftype='R': datavalue not within valArray[0] to valArray[1] marked as reject.
         ='D': datavalue difference from previous value not within
               valArray[0] to valArray[1] range marked as reject.
         ='M': datavalue equals or very close to val[0:], an array of Missing Value markers;
               DSS flag set to missing.
    All values marked are written to log file, with optional Special treatment.
    Flags added to timeseries if needed.
    """
    def nearVal (val,target,tol=.0001):
        # return True if val is "near" target within tol
        return (val > 0 and val*(1.-tol) < target and val*(1.+tol) > target) \
            or (val <= 0 and val*(1.-tol) > target and val*(1.+tol) < target)
    if ftype == 'R':
        if len(valArray)!=2: raise 'Two values must be given for Range check.'
        rej_head='Check range ' + str(valArray[0]) + ' - ' + str(valArray[1])
        rej_note='Range reject @ '
    elif ftype == 'D':
        if len(valArray)!=1: raise 'One value must be given for Diff check.'
        rej_head='Check diff w/ prev & next values ' + str(valArray[0])
        rej_note='Diff reject @ '
    elif ftype == 'M':
        if len(valArray)<1: raise 'At least one value must be given for Missing check.'
        rej_head='Check Missing value marker ' + str(valArray)
        rej_note='Missing @ '
    else: raise 'First arg must be a single character R, D or M.'
    # a flag to check if any flag was changed
    changedFlag=False
    # get the filter for missing values
    filter = Constants.DEFAULT_FILTER
    # check if ds already has flags, if not, make them
    # open log file
    logfile = open(log,'a')
    logfile.write('\n\n' + 'Name: ' + dataset.getName())
    logfile.write('\n' + 'Units: ' + dataset.getAttributes().getYUnits())
    logfile.write('\n' + rej_head)
    if dataset.isFlagged(): ds = dataset
    else: ds=dsAddFlags(dataset)
    # get user id for setting flags
    uId = DSSUtil.getUserId('datachecker')
    # create a reject element
    ex=dataset.getElementAt(0)
    ex.setY(Constants.MISSING_VALUE)
    ex.setFlag(FlagUtils.MISSING_FLAG)
    for i in range(dataset.size()):
        changedEl=False
        # get the data element at the i-1, i, and i+1 positions
        if i>0: e0=dataset.getElementAt(i-1)
        else: e0=ex
        e1 = dataset.getElementAt(i)
        if i<len(dataset)-1: e2=dataset.getElementAt(i+1)
        else: e2=ex
        if ftype=='R':
            if not filter.isAcceptable(e1): continue
            if e1.y < valArray[0] or e1.y > valArray[1] : 
                FlagUtils.setQualityFlag(e1,FlagUtils.REJECT_FLAG,uId)
                changedEl=True
        elif ftype=='D':
            if not filter.isAcceptable(e0) or \
               not filter.isAcceptable(e1) or \
               not filter.isAcceptable(e2): continue
            diff1=abs(e0.y-e1.y)
            diff2=abs(e2.y-e1.y)
            if diff1 > valArray[0] and diff2 > valArray[0]: 
                FlagUtils.setQualityFlag(e1,FlagUtils.REJECT_FLAG,uId)
                changedEl=True
        elif ftype=='M':
            for vA in valArray:
                if nearVal(vA,e1.y): 
                    if not Special or (Special and \
                    # Special treatment for Missing values that are within the
                    # normal operating range of the parameter; check that the value
                    # before or after is also Missing or not acceptable before
                    # marking this value as Missing
                     (not filter.isAcceptable(e0) or not filter.isAcceptable(e2)) or \
                     (nearVal(vA,e0.y) or nearVal(vA,e2.y))):
                        FlagUtils.setQualityFlag(e1,FlagUtils.MISSING_FLAG,uId)
                        #e1.y=Constants.MISSING_VALUE
                        changedEl=True
        if changedEl:
            changedFlag = True
            dataset.putElementAt(i,e1) # put the element back into the data set
            logfile.write('\n' + rej_note + e1.getXString() + " : " + e1.getYString())
   # end the for loop
    logfile.close()
    if changedFlag:
        return ds
    else:
        return None
    
def display_missing(ds):
    """
    display_missing(ds)
    where
    ds is a data set or time series
    displays missing value ranges for given time series
    """
    dsi = ds.getIterator()
    while not dsi.atEnd():
	el = dsi.getElement()
	begin_date = None
	while not Constants.DEFAULT_FLAG_FILTER.isAcceptable(el):
	    if begin_date == None: begin_date = el.getXString()
	    end_date = el.getXString()
	    dsi.advance()
	    if dsi.atEnd():
		break
	    el = dsi.getElement()
	    #print el
	if begin_date != None:
	    print 'Missing for %s to %s'%(begin_date,end_date)
	if dsi.atEnd(): break
	dsi.advance()
#
def diff(rts1,rts2,outfile=None):
    '''
    diff(rts1,rts2,outfile=None):
    Prints to stdout the differences between rts1 and rts2 to
    outfile or if outfile is None to standard out. When writing
    to file it appends to existing outfile if any.
    '''
    if outfile ==None:
	fh=sys.stdout
    else:
	fh=open(outfile,'a+')
    if rts1.getTimeInterval().compare(rts2.getTimeInterval()) !=0 :
	raise "Incompatible time intervals for %s and %s"%(rts1.getName(),rts2.getName())
    tw = rts1.getTimeWindow()
    if not tw.isSameAs(rts2.getTimeWindow()):
	fh.write('TimeWindow for %s is %s & %s is %s\n'\
		 %(rts1.getName(),str(rts1.getTimeWindow()),\
		   rts2.getName(),str(rts2.getTimeWindow())))
	tw = tw.intersection(rts2.getTimeWindow())
    if tw == None:
	raise "No intersecting time window for %s and %s"%(rts1.getName(),rts2.getName())
    else:
	rts1 = rts1.createSlice(tw)
	rts2 = rts2.createSlice(tw)
    dsi1 = rts1.getIterator()
    dsi2 = rts2.getIterator()
    while not dsi1.atEnd():
	e1 = dsi1.getElement()
	e2 = dsi2.getElement()
	if e1.y != e2.y:
	    fh.write('Value difference @ %s , 1: %f , 2: %f\n'\
		     %(e1.getXString(),e1.y,e2.y))
	if e1.flag != e2.flag:
	    fh.write('Flag difference @ %s , 1: %s, 2: %s\n'\
		     %(e1.getXString(), e1.getFlagString(), e2.getFlagString()))
	dsi1.advance()
	dsi2.advance()
#
