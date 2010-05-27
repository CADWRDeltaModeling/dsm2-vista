_doc__ = """

"""
import jarray, string, re
from vista.time import *
from vista.set import ProxyFactory, TimeSeriesMath, \
     MovingAverageProxy, Constants, Stats, DataReference, \
     RegularTimeSeries, IrregularTimeSeries, FlagUtils
from vdss import wrap_data
from vtimeseries import *
from datetime import *
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

    The mechanical difference between godin and tidal_avg, 
    is this filter removes 24-hour constituents more
    completely. A practical difference is this returned average 
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
def per_oper(dsref, oper, interval):
    """
    per_oper(dataset-or-reference, operation, interval='1mon'):
    Period operations (average, max, min) given regular or irregular time series
    or data reference within each interval, over the entire TS. Always
    return a RTS.
    """
    ti = TimeFactory.getInstance().createTimeInterval(interval)
    #
    if isinstance(dsref, DataReference):
        ds = dsref.getData();
        isRef = True
    else:
        ds = dsref
        isRef = False
    # check for regular time series
    if isinstance(ds,RegularTimeSeries):
        isRTS = True
    else:
        isRTS = False
    #
    if oper[:2].lower() == 'av': 
        OPER = TimeSeriesMath.PERIOD_AVERAGE
    elif oper[:3].lower() == 'max':
        OPER = TimeSeriesMath.PERIOD_MAX
    elif oper[:3].lower() == 'min':            
        OPER = TimeSeriesMath.PERIOD_MIN
    else:
        raise 'Operation must be ave, max, or min.'
    if isRTS:
        if isRef:
            pn = dsref.getPathname()
            pn.setPart(Pathname.E_PART,interval)
            pn = str(pn)
            return wrap_data(TimeSeriesMath.doPeriodOperation(ds, ti, OPER), \
                             filename=dsref.getFilename(),pathname=pn)
        else:
            return TimeSeriesMath.doPeriodOperation(ds, ti, OPER)
    else:   # ITS
        if isRef:
            pn = Pathname.createPathname(dsref.getPathname())
            pn.setPart(Pathname.E_PART,interval)
            pn = str(pn)
            return wrap_data(doPeriodOp(ds, ti, OPER), \
                             filename=dsref.getFilename(),pathname=pn)
        else:
            return doPeriodOp(ds, ti, OPER)
#
def doPeriodOp(ds, ti, OPER):
    TF = TimeFactory.getInstance()
    dsi = ds.getIterator()
    if OPER == TimeSeriesMath.PERIOD_AVERAGE: perVal = 0.
    elif OPER == TimeSeriesMath.PERIOD_MAX: perVal = 1.e-10
    elif OPER == TimeSeriesMath.PERIOD_MIN: perVal = 1.e+10
    filter = Constants.DEFAULT_FLAG_FILTER
    e = dsi.getElement()
    xPrev = long(e.getX())
    nPers = 0
    nGood = 0
    xar = []
    yar = []
    flar = []
    tNext = TF.createTime(xPrev).ceiling(ti)
    xNext = tNext.getTimeInMinutes()
    while (not dsi.atEnd()):
        e = dsi.getElement()
        y = e.getY()
        x = long(e.getX())
        if x < xNext:
            if filter.isAcceptable(e):
                nGood += 1
                if OPER == TimeSeriesMath.PERIOD_AVERAGE: 
                    perVal += y
                elif OPER == TimeSeriesMath.PERIOD_MAX: 
                    if y > perVal: xMM = x; perVal = y
                elif OPER == TimeSeriesMath.PERIOD_MIN: 
                    if y < perVal: xMM = x; perVal = y
        else:
            nPers += 1
            if nGood == 0:
                # use "end of period" for time of Missing or Average Value 
                xar.append(float(xNext))
                perVal = Constants.MISSING_VALUE
#                flar.append(FlagUtils.MISSING_FLAG)
                flar.append(5)
            else:
                if OPER == TimeSeriesMath.PERIOD_AVERAGE:
                    xar.append(float(xNext))
                    perVal = perVal / nGood
                else:
                    # use its time instant for time of Max or Min Value
                    xar.append(float(xMM))
#                flar.append(FlagUtils.OK_FLAG)
                flar.append(3)
            yar.append(perVal)
            if OPER == TimeSeriesMath.PERIOD_AVERAGE:
                perVal = 0.
            elif OPER == TimeSeriesMath.PERIOD_MAX:
                perVal = 1.e-10
            elif OPER == TimeSeriesMath.PERIOD_MIN:
                perVal = 1.e+10
            tNext.incrementBy(ti)
            xPrev = xNext
            xNext = tNext.getTimeInMinutes()
            nGood = 0
        #
        dsi.advance()
    dsOp = IrregularTimeSeries(ds.getName(), xar, yar, flar, ds.getAttributes())
    if OPER == TimeSeriesMath.PERIOD_AVERAGE:
        dsOp.getAttributes().setYType('PER-AVER')
    elif OPER == TimeSeriesMath.PERIOD_MAX:
        dsOp.getAttributes().setYType('INST-VAL')
    elif OPER == TimeSeriesMath.PERIOD_MIN:
        dsOp.getAttributes().setYType('INST-VAL')
    return ITS2RTS(dsOp,None,str(ti))
#
def per_avg(ds, interval='1mon'):
    return per_oper(ds, 'ave', interval)
#
def per_max(ds, interval='1mon'):
    return per_oper(ds, 'max', interval)
#
def per_min(ds, interval='1mon'):
    return per_oper(ds, 'min', interval)
#
#def mov_avg(ts, backLength, forwardLength):
#    '''
#    mov_avg(ts,backLength,forwardLength):
#    Does a moving average of the time series (dataset or ref) with 
#    backLength previous points and forwardLength future points and
#    the present point. Returns the result as a new time series.
#    '''
#    if isinstance(ts, DataReference):
#        return ProxyFactory.createMovingAverageProxy(ts, backLength, forwardLength)
#    else:
#        return ProxyFactory.createMovingAverageProxy(wrap_data(ds), backLength, forwardLength).getData()
#  
def mov_avg(dsref, backLength, forwardLength):
    '''
    mov_avg(ts,backLength,forwardLength):
    Does a moving average of the time series (dataset or ref) with 
    backLength previous points and forwardLength future points and
    the present point. Returns the result as a new time series.
    '''
    TF = TimeFactory.getInstance()
    filter = Constants.DEFAULT_FLAG_FILTER
    if isinstance(dsref, DataReference):
        ds = dsref.getData()
        ref = dsref
        isRef = True
    else:   # dataset
        if isinstance(dsref, RegularTimeSeries):    #RTS
            ds = dsref
        else:       # ITS
            ds = IrregularTimeSeries(dsref)
            ds.setAttributes(dsref.getAttributes())
        isRef = False
    # first fill a list with back and forward y values
    # then MAs are then computed by
    # adding the newest value, removing the oldest,
    # divide by the number of good values in the vector
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
    if isRef:
        return wrap_data(ds,filename=dsref.getFilename(),pathname=str(dsref.getPathname()))
    else:
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
    from vista.set import MovingAverageProxy, ProxyFactory, DataSet, DataReference
    if len(args) == 0: raise "Nothing to merge"
    if len(args) == 1: return args[0]
    refs = []
    any_ds = False # will become true if any array objects are data sets
    for arg in args:
    	if isinstance(arg, DataReference): # arg is a data ref
    	    refs.append(arg)
    	elif isinstance(arg, DataSet): # arg is a data set
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
    if isinstance(dsref, DataReference):
        data = ts.getData()
    else:   # dataset
        data = ts
    return Stats.max(data)
#
def tsmin(ts):
    """
    The minimum value of a time series
    """
    data = None
    if isinstance(dsref, DataReference):
        data = ts.getData()
    else:   # dataset
        data = ts
    return Stats.min(data)
#
def avg(ts):
    """
    The average of a time series
    """
    data = None
    if isinstance(ts, DataReference):
        data = ts.getData()
    else:   # dataset
        data = ts
    return Stats.avg(data)
#
def sdev(ts):
    """
    The standard deviation of a time series
    """
    data = None
    if isinstance(dsref, DataReference):
        data = ts.getData()
    else:   # dataset
        data = ts
    return Stats.sdev(data)
#
def total(ts):
    """
    The total of a time series
    """
    data = None
    if isinstance(dsref, DataReference):
        data = ts.getData()
    else:   # dataset
        data = ts
    return Stats.total(data)
#
