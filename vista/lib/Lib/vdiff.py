import vutils
from vutils import *
from math import sqrt
import xyz 
from xyz import GetDsm2Model
    
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
    ds3 = ds2 - ds1
    ds4 = ds3 * ds3 /len(ds3)
    sum_mse = Stats.total(ds4)
    root_mse = sqrt(sum_mse)    
    return root_mse
      
def dss_ts_diff_metric(dss_file1, dss_file2, difference_metric=rmse, tw_string=None \
                       ,c_part=None, save_dss=None, diff_dssfile=None):
    '''
    dss_ts_diff_metric(dss_file1,dss_file2,difference_metric=rmse,tw_string=None)
    - This function computes the difference between two DSS file (dss_file2 - dss_file1)
    - Arguments:
      dss_file1         first DSS file name 
      dss_file2         second DSS file name
      difference_metric method used to compute the measure of difference between two time series
                        rmse: root mean square error
      tw_string         time window
                        If not specified, the default window is obtained from dss_file1.
      c_part            filter for PART C in the dss file (e.g.'FLOW','STAGE','EC')
                        If not specified, it returns all available time series
      save_dss          If you plan to save the difference to a DSS file, put 'y'; otherwise, leave it blank from this point on.
      diff_dssfile      If you put 'y' for save_dss, specify the output file name here.  
    - Return value:
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
        if c_part == None:
            g3 = findparts(g2,a=a[1],b=a[2],c=a[3])
        else:
            g3 = findparts(g2,a=a[1],b=a[2],c=c_part)
        if g3 is not None and (a[3] == c_part or c_part == None):  
            ref1 = DataReference.create(ref,tw)
            ref2 = DataReference.create(g3[0],tw)
            try:
                ref3 = ref2 - ref1
            except:
                print 'Error: The time window ' + tw_string + ' is out of range for' + a[2] + '! Please specify the correct time window.'
                break         
            rootmse = difference_metric(ref1,ref2)
            metric[outPath] = rootmse 
            if save_dss == 'y':
                if diff_dssfile is not None:
                    try:
                        writedss(diff_dssfile,outPath,ref3.getData())
                    except:
                        print 'Make sure the file path ' + diff_dssfile + ' is correct.'
                        break
                else:
                    print 'To save the difference to a DSS file, you need to specify the output file name.'
                    break
    return metric      

def get_metric_xy(metric,hydro_echo_file,gis_inp_file,output_file):
    '''
    get_metric_xy(metric,hydro_echo_file,gis_inp_file,output_file)
    - This function is used to retrieve the lat/lng information for the metric computed 
      from dss_ts_diff_metric.
    - Arguments:
      metric           metric obtained from dss_ts_diff_metric()
      hydro_echo_file  hydro echo file from DSM input 
      gis_inp_file     gis input file downloaded from DSM2 Grid Map interface
      output_file      output file path and name
    - Return:
      A text file and the channels that cannot retrieve a location
    - Usage Example:
      get_metric_xy(ms,'D:\DSM2-SensTest\hydro_echo.inp','D:\DSM2-SensTest\gis.inp','D:\DSM2-SensTest\metric_xy.txt')
    '''  
    dsm2model = GetDsm2Model(hydro_echo_file,gis_inp_file)
    f = open(output_file, 'w')
    f.write('ID,Name,Longitude,Latitude,Val\n')
    logstr = 'Stations failed to locate: '
    chkarr = []
    for mpath,val in metric.iteritems():
        pname = get_name_from_path(mpath)
        try:
            if if_id_not_exist(dsm2model.get_id_by_name(pname.lower()),chkarr):
                a = dsm2model.get_xy_by_name(pname.lower())
                f.write(a['channel_id'] + ',' + a['channel_name'].upper() + ',' + str(a['longitude'])+ ',' + str(a['latitude']) + ',' + str(val) +'\n')
                chkarr.append(dsm2model.get_id_by_name(pname.lower()))
        except:
            logstr += pname + ','
    f.close()
    print logstr
    return chkarr

def if_id_not_exist(id,exist_arr):
    for k in exist_arr:
        if id == k:
            return False
    return True
    
def get_name_from_path(pathname):
    a = pathname.split('/')
    return a[2] 
