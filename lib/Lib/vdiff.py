import vutils
from vutils import *
from math import sqrt

def ref2ds(ref):
    if isinstance(ref, DataReference):
        ds = ref.getData()
    else:
        ds = ref
    if not isinstance(ds,RegularTimeSeries):
        print ref, " is not a regular time-series data set"
        return None
    return ds     
    
def rmse(ref1,ref2):
    ''' return the root mean square error from two references '''
    ds1 = ref2ds(ref1)
    ds2 = ref2ds(ref2)
    ds3 = (ds2-ds1)*(ds2-ds1)/len(ds1)
    sumMSE = Stats.total(ds3)
    rootMSE = sqrt(sumMSE)    
    return rootMSE
      
def dss_ts_diff_metric(dss_file1,dss_file2,difference_metric=rmse,tw_string=None,c_part=None):
   '''
    dss_ts_diff_metric(dss_file1,dss_file2,difference_metric=rmse,tw_string=None)
    This function computes the difference between two DSS file (dss_file2 - dss_file1)
    Arguments:
      dss_file1          first DSS file name 
      dss_file2          second DSS file name
      difference_metric  method used to compute the measure of difference between two time series
                         rmse: root mean square error
      tw_string          time window
                         If not specified, the default window is obtained from dss_file1.
      c_part             filter for PART C in the dss file (e.g.'FLOW','STAGE','EC')
                         If not specified, it returns all available time series
    Return value:
      An associated array that contains information of the difference measure 
      and its corresponding pathname
   '''
   g1 = opendss(dss_file1)
   g2 = opendss(dss_file2)
   metric = {}
   for ref in g1:
        if tw_string == None: 
            tw = ref.timeWindow
        else:
            tw = timewindow(tw_string)
        outPath = str(ref.pathname)
        ''' find out the matched time series in dssFile2'''
        a = outPath.split('/') 
        #a = map(re.escape,a)      
        if c_part == None:
            g3 = find(find(find(g2,a[1],'a'),a[2],'b'),a[3],'c')
        else:
            g3 = find(find(find(g2,a[1],'a'),a[2],'b'),c_part,'c')  
        if len(g3) > 0 and a[3] == c_part:  
            ref1 = DataReference.create(ref,tw)
            ref2 = DataReference.create(g3[0],tw)
            try:
                ref3 = ref2 - ref1
                print a[1],a[2],a[3]
            except:
                print 'Error: The time window is out of range! Please specify the correct time window.'
                break         
            rootMSE = difference_metric(ref1,ref2)
            metric[outPath] = rootMSE  
   return metric


def dss_ts_diff(dss_file1,dss_file2,out_dssfile,tw_string=None):
   '''
    dss_ts_diff(dss_file1,dss_file2,out_dssfile,tw_string=None)
    This function computes the difference between two DSS file and write it to a new DSS file
    (dss_file2 - dss_file1)
    Arguments:
      dss_file1          first DSS file name 
      dss_file2          second DSS file name
      out_dssfile        output DSS file
      tw_string          time window. If not specified, the default window is obtained from dss_file1.
    Return:
      new DSS file which contains the time series of difference
   '''
   g1 = opendss(dss_file1)
   g2 = opendss(dss_file2)
   metric = {}
   for ref in g1:
        if tw_string == None: 
            tw = ref.timeWindow
        else:
            tw = timewindow(tw_string)
        outPath = str(ref.pathname)
        ''' find out the matched time series in dss_file2'''
        a = outPath.split('/')
        g3 = find(find(find(g2,a[1],'a'),a[2],'b'),a[3],'c')
        ref1 = DataReference.create(ref,tw)
        ref2 = DataReference.create(g3[0],tw)       
        try:
            ref3 = ref2 - ref1
        except:
            print 'Error: The time window is out of range! Please specify the correct time window.'
            break         
        writedss(out_dssfile,outPath,ref3.getData())
   return None

 