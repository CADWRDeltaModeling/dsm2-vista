__doc__ = """

"""
import jarray, string, re, datetime
from vista.time import *
from vista.set import ProxyFactory, TimeSeriesMath, \
     MovingAverageProxy, Constants, Stats, DataReference, \
     RegularTimeSeries, IrregularTimeSeries
from vdss import wrap_data
from vtimeseries import *
#
def tidal_avg(ref):
    """
    tidal_avg(ref):
    calculates the tidal average of the given reference and returns it in
    another reference. The returning reference can be treated just like any
    other reference.
    A tidal cycle is assumed to be 24hours_45 minutes but for 1hour time interval
    data it is rounded off to 25hours.
    """
    ti = ref.getTimeInterval();
    ti_tidal = TimeFactory.getInstance().createTimeInterval('24hour_45min')
    if ti_tidal.compare(ti) < 0 :
	if hasattr(ref, 'getPathname'):
	    path = ref.getPathname().toString()
	else:
	    path = ref.getName()
        raise 'Time interval of ' + path + ' is greater than tidal cycle of ' + str(ti_tidal)
    num_intervals = (ti_tidal / ti - 1) / 2
    return mov_avg(ref, num_intervals, num_intervals)
#
def godin(ref):
    """
    Tidal average using a filter similar to the Godin 25-24-24 tidal average.
    Arguments:
    ref     reference to series to be averaged
    Return value:
    reference to averaged filter

    This is the Godin filter discussed in the G-model literature 
    and popular in the tidal literature (though
    not necessarily under the name Godin filter).

    For hourly data, the 25-24-24 filter is applied exactly 
   (25-hour moving average, then two 24-hour moving
    averages). When the input series uses time steps, 
    the lunar tidal cycle is assumed to be 24hours_45 minutes
    (e.g. 99 values for 15 min data).

    The mechanical difference between tide_avg and tidal_avg, 
    is that this filter removes 24-hour constituents more
    completely. A practical difference is that this returned average 
    is period-centered. The shift operator can
    be easily used to adjust this to the statutory version 
    """
    isRef = False
    if isinstance(ref, DataReference):
        data = ref.getData()
        isRef = True
    else:
        data = ref
        ref = wrap_data(data)
        isRef = False
    
    # check for regular time series
    if not isinstance(data, RegularTimeSeries):
        print ref, " is not a regular time-series data set"
        return None  

    ti = ref.getTimeInterval();
    tf = TimeFactory.getInstance()
    ti_day = tf.createTimeInterval('24hours')
    ti_tidal = tf.createTimeInterval('24hour_45min')
    if ti_tidal.compare(ti) < 0 :
        raise 'Time interval of ' + ref.getPathname.toString() + ' is greater than tidal cycle of ' + ti_tidal
    #24.75 lunar constituents
    num_intervals = (ti_tidal / ti - 1) / 2;
    ma = MovingAverageProxy(ref, num_intervals, num_intervals)
    # 24 hour solar constituents
    num_intervals = (ti_day / ti) / 2
    ma = MovingAverageProxy(ma, num_intervals - 1, num_intervals)
    ma = MovingAverageProxy(ma, num_intervals, num_intervals - 1)
    if isRef:
	return ma
    else:
	return ma.getData()
#
def per_avg(ds, interval='1mon'):
    """
    per_avg(dataset, interval='1mon'):
    Period averages given regular time series or data reference to the given interval
    """
    if hasattr(ds, 'getServername'):
        ti = TimeFactory.getInstance().createTimeInterval(interval)
        return ProxyFactory.createPeriodOperationProxy(ProxyFactory.PERIOD_AVERAGE, ds, ti);
    elif hasattr(ds, 'getTimeInterval'):
        ti = TimeFactory.getInstance().createTimeInterval(interval)
        return TimeSeriesMath.doPeriodOperation(ds, ti,
                                                TimeSeriesMath.PERIOD_AVERAGE)
    else:
	return None
#
def per_max(ds, interval='1mon'):
    """
    per_max(dataset, interval='1mon'):
    Period maximums for a given regular time series or data reference for the given interval
    """
    if hasattr(ds, 'getServername'):
        ti = TimeFactory.getInstance().createTimeInterval(interval)
        return ProxyFactory.createPeriodOperationProxy(ProxyFactory.PERIOD_MAX, ds, ti);
    elif hasattr(ds, 'getTimeInterval'):
        ti = TimeFactory.getInstance().createTimeInterval(interval)
        return TimeSeriesMath.doPeriodOperation(ds, ti,
                                                TimeSeriesMath.PERIOD_MAX)
    else:
        return None
#
def per_min(ds, interval='1mon'):
    """
    per_min(dataset, interval='1mon'):
    Period maximums for a given regular time series or data reference for the given interval
    """
    if hasattr(ds, 'getServername'):
        ti = TimeFactory.getInstance().createTimeInterval(interval)
        return ProxyFactory.createPeriodOperationProxy(ProxyFactory.PERIOD_MIN, ds, ti);
    elif hasattr(ds, 'getTimeInterval'):
        ti = TimeFactory.getInstance().createTimeInterval(interval)
        return TimeSeriesMath.doPeriodOperation(ds, ti,
                                                TimeSeriesMath.PERIOD_MIN)
    else:
        return None
#
#
def findTidalPT(dsref,dsrefSm=None):
    """
    findTidalPT(dataset-or-reference, [dsrefSm], [dsSmOffset]):
    Find tidal Peaks and Troughs within a dataset or reference (either RTS or ITS).
    Return a list of two irregular time series of the computed values;
    for each element the time is the time of the peak or trough;
    y is the value of the peak or trough. 
    Optional dsrefSm is a smoothed version of dsref, helpful for noisy
    timeseries such as observed EC.  The smoothed version should be padded
    and extended with Constants.MISSING_VALUE if needed.
    """
    TF = TimeFactory.getInstance()
    if isinstance(dsref, DataReference):
        ds = dsref.getData()
        ref = dsref
        isRef = True
    else:
        ds = dsref
        ref = wrap_data(dsref)
        isRef = False
    # initialize
    # amount by which a potential peak or trough must be higher or lower than
    # values tiFBWindow back and ahead
    FBFactor = 1.03
    tiFBWindow = TF.createTimeInterval('1HOUR_45MIN') # time to look forward/back for lesser/greater vals for P/T
    pn = ds.getName()
    # use smoothed dataset if given
    if (dsrefSm):
        dsUse=dsrefSm
    else:
        dsUse=ds
    filter = Constants.DEFAULT_FLAG_FILTER
    bigNumber = 1.e8
    xpar = []  # x peak time array
    xtar = []  # x trough time array
    ypar = []  # y peak values array
    ytar = []  # y trough values array
    yp = -bigNumber
    yt = bigNumber
    dsi = dsUse.getIterator()
    e1 = dsi.getElement()
    x1 = e1.getX()
    t1 = TF.createTime(long(x1))
    xp = x1
    xt = x1
    dsi.resetIterator()
    before = datetime.now()
    while (t1 + tiFBWindow).compare(dsUse.getEndTime()) < 0:
        if dsi.getIndex() % 5000 == 0:
            print 5000./(datetime.now()-before),t1
            before = datetime.now()
        # advance iterator to allow room for backward tiFBWindow
        if (t1 - tiFBWindow).compare(dsUse.getStartTime()) < 0:
            dsi.advance()
            e1 = dsi.getElement()
            x1 = e1.getX()
            t1 = TF.createTime(long(x1))
            continue
        # 1 or 2 hours behind
        e0 = dsUse.getElementAt((t1 - tiFBWindow).format())
        #  1 or 2 hours head
        e2 = dsUse.getElementAt((t1 + tiFBWindow).format())
        e0OK = filter.isAcceptable(e0)
        e1OK = filter.isAcceptable(e1)
        e2OK = filter.isAcceptable(e2)
        if e0OK and e1OK and e2OK:
            y0 = e0.getY()
            y1 = e1.getY()
            y2 = e2.getY()
            # Checks for Peak
            if y1 > yp and y1 > y0 * FBFactor and y1 > y2 * FBFactor:   # new peak
                    yp = y1
                    xp = x1
                    tp = t1
                    ndxp = dsi.getIndex()
            if yp != -bigNumber and x1 > xp and y0 > y1 > y2:
                # tiFBWindow time after highest peak, on downslope: record highest peak
                if (dsrefSm):   # use y value from original dataset for peak
                    yp = ds.getElementAt(ndxp).getY()
                xpar.append(xp)
                ypar.append(yp)
                yp = -bigNumber
            # Checks for Trough
            if y1 < yt and y1 < y0 / FBFactor and y1 < y2 / FBFactor:   # new trough
                    yt = y1
                    xt = x1
                    tt = t1
                    ndxt = dsi.getIndex()
            if yt != bigNumber and x1 > xt and y0 < y1 < y2:
                # tiFBWindow time after lowest trough, on upslope: record lowest trough
                if (dsrefSm):   # use y value from original dataset for trough
                    yt = ds.getElementAt(ndxt).getY()
                xtar.append(xt)
                ytar.append(yt)
                yt = bigNumber
        dsi.advance()
        e1 = dsi.getElement()
        x1 = e1.getX()
        t1 = TF.createTime(long(x1))
    return [IrregularTimeSeries(ds.getName(), xpar, ypar), \
            IrregularTimeSeries(ds.getName(), xtar, ytar)]
#

#
#def mov_avg(dsref, backLength, forwardLength):
#    '''
#    mov_avg(dsref,backLength,forwardLength):
#    Does a moving average of the time series (dataset or ref) with 
#    backLength previous points and forwardLength future points and
#    the present point. Returns the result as a new time series.
#    '''
#    if isinstance(dsref, DataReference):
#        return ProxyFactory.createMovingAverageProxy(dsref, backLength, forwardLength)
#    else:
#        return ProxyFactory.createMovingAverageProxy(wrap_data(ds), backLength, forwardLength).getData()
#  
def mov_avg(dsref, backLength, forwardLength):
    '''
    mov_avg(dsref,backLength,forwardLength):
    Does a moving average of the time series (dataset or ref) with 
    backLength previous points and forwardLength future points and
    the present point. Returns the result as a new time series.
    '''
    TF = TimeFactory.getInstance()
    filter = Constants.DEFAULT_FLAG_FILTER
    ds = None
    if isinstance(dsref, DataReference):
        ds = dsref.getData()
        ref = dsref
        isRef = True
    else:
        ref = wrap_data(dsref)
        ds = dsref
        isRef = False
    # first fill a list with back and forward y values
    # then MAs are then computed by
    # adding the new value, subtracting the oldest,
    # divide by n
    smallNumber = 1.e-10
    totLength = backLength + forwardLength + 1
    vecY = jarray.zeros(totLength, 'd')
    for ndx in range(totLength - 1):
        el1 = ds.getElementAt(ndx)
        if filter.isAcceptable(el1): vecY[ndx + 1] = el1.getY()
        else: vecY[ndx + 1] = smallNumber
    for ndx in range(ds.size()):
        el1 = ds.getElementAt(ndx)  # element now
        if ndx < backLength or ndx >= (ds.size() - forwardLength):
            el1.setY(Constants.MISSING_VALUE)
        else:   # compute the MA centered at ndx
            el2 = ds.getElementAt(ndx + forwardLength)    # farthest future element
            # update vector with new value
            if filter.isAcceptable(el2): vecY.append(el2.getY())
            else: vecY.append(smallNumber)
            vecY.pop(0)
            if vecY.count(smallNumber) < totLength:
                aveY = sum(vecY) / (totLength - vecY.count(smallNumber))
            else:
                aveY = Constants.MISSING_VALUE
            el1.setY(aveY)
        ds.putElementAt(ndx, el1)
    return ds
#  
def merge(args, filter=Constants.DEFAULT_FLAG_FILTER):
    """
    merge(args,filter=Constants.DEFAULT_FLAG_FILTER):
    where args is an array of data references or data sets
    & filter is the filter for accepting or rejecting values
    (type vista.set.ElementFilter)
    It returns a merged reference if given an array of data references or
    a merged data set if given an array of data sets
    (or if any array object is a data set).
    
    """
    from vista.set import MovingAverageProxy, ProxyFactory
    if len(args) == 0: raise "Nothing to merge"
    if len(args) == 1: return args[0]
    refs = []
    any_ds = False # will become true if any array objects are data sets
    for arg in args:
    	if hasattr(arg, 'getServername'): # arg is a data ref
    	    refs.append(arg)
    	elif hasattr(arg, 'getPathname'): # arg is a data set
    	    any_ds = True
    	    refs.append(wrap_data(arg))
    	else:
    	    raise "arg %s is neither a data reference or a data set" % str(arg)
        result = ProxyFactory.createMergingProxy(refs)
        result.setFilter(filter)
    if any_ds:
        return result.getData()
    else:
        return result
#
# a function to convert a given reference to monthly
def tsmax(ts):
    """
    The maximum of a time series
    """
    data = None
    if hasattr(ts, 'getServername'):
	   data = ts.getData()
    elif hasattr(ts, 'getTimeInterval'):
        data = ts
    else:
        return None
    return Stats.max(data)
#
def tsmin(ts):
    """
    The minimum value of a time series
    """
    data = None
    if hasattr(ts, 'getServername'):
        data = ts.getData()
    elif hasattr(ts, 'getTimeInterval'):
        data = ts
    else:
        return None
    return Stats.min(data)
#
def avg(ts):
    """
    The average of a time series
    """
    data = None
    if hasattr(ts, 'getServername'):
	   data = ts.getData()
    elif hasattr(ts, 'getTimeInterval'):
        data = ts
    else:
        return avg(ts)  #fixme, is this recursive?
    return Stats.avg(data)
#
def sdev(ts):
    """
    The standard deviation of a time series
    """
    data = None
    if hasattr(ts, 'getServername'):
	   data = ts.getData()
    elif hasattr(ts, 'getTimeInterval'):
	   data = ts
    else:
        return avg(ts)  #fixme, should this be sdev?
    return Stats.sdev(data)
#
def total(ts):
    """
    The total of a time series
    """
    data = None
    if hasattr(ts, 'getServername'):
        data = ts.getData()
    elif hasattr(ts, 'getTimeInterval'):
        data = ts
    else:
        return total(ts)    #fixme, recursive?
    return Stats.total(data)
#
