import sys, os, shutil
import vdss, vutils, vdisplay, vtimeseries, vdiff
import logging

def open_dss(dssfile):
    group = vutils.opendss(dssfile)
    if group == None:
        print "No dss file named: %s found!" % (dssfile)
    return group

def build_data_array(ref0, ref1, ref2):
    from vista.set import Constants
    from vtimeseries import time
    import math
    if (ref0==None and ref1==None and ref2==None):
        return []
    if (ref0!=None and ref1==None and ref2==None):
        iterator = multi_iterator([ref0.data], Constants.DEFAULT_FLAG_FILTER)
    if (ref0==None and ref1!=None and ref2==None):
        iterator = multi_iterator([ref1.data], Constants.DEFAULT_FLAG_FILTER)
    if (ref0==None and ref1!=None and ref2!=None):
        iterator = multi_iterator([ref1.data,ref2.data], Constants.DEFAULT_FLAG_FILTER)
    if (ref0!=None and ref1!=None and ref2==None):
        iterator = multi_iterator([ref0.data,ref1.data], Constants.DEFAULT_FLAG_FILTER)
    if (ref0!=None and ref1!=None and ref2!=None):
        iterator = multi_iterator([ref0.data,ref1.data,ref2.data], Constants.DEFAULT_FLAG_FILTER)
    darray=[]
    time_str = None
    while not iterator.atEnd():
        index = iterator.getIndex();
        e = iterator.getElement();
        date = convert_to_date(time(e.getXString()))
        if ((ref0!=None and ref1==None and ref2==None) or (ref0==None and ref1!=None and ref2==None)):
            darray.append((date.time, e.getY(0),-999.99))
        if ((ref0==None and ref1!=None and ref2!=None) or (ref0!=None and ref1!=None and ref2==None)):
            darray.append((date.time, e.getY(0),e.getY(1)))
        if (ref0!=None and ref1!=None and ref2!=None):
            darray.append((date.time, e.getY(0),e.getY(1),e.getY(2)))
        iterator.advance();
    return darray

def build_obs_array(ref):
    from vista.set import Constants
    from vtimeseries import time
    import math
    if (ref==None):
        return []
    iterator = multi_iterator([ref.data], Constants.DEFAULT_FLAG_FILTER)
    darray=[]
    time_str = None
    while not iterator.atEnd():
        index = iterator.getIndex();
        e = iterator.getElement();
        date = convert_to_date(time(e.getXString()))
        darray.append((date.time, e.getY(0)))
        iterator.advance();
    return darray    
    
def build_exceedance_array(ref1, ref2):
    from java.lang import Math
    x1=sort(ref1)
    x2=sort(ref2)
    darray=[]
    i=0
    n=int(Math.min(len(x1),len(x2)))
    while i < n:
        darray.append((100.0-100.0*float(i)/(n+1),x1[i],x2[i]))
        i=i+1
    return darray

def cfs2taf(data):
    from vista.report import TSMath
    data_taf = TSMath.createCopy(data)
    TSMath.cfs2taf(data_taf)
    return data_taf

def column(matrix, i):
    return [row[i] for row in matrix]

def format_timewindow(tw):
    from vista.time import SubTimeFormat
    year_format = SubTimeFormat('yyyy')
    return tw.startTime.format(year_format) + "-" + tw.endTime.format(year_format)
def format_time_as_year_month_day(t):  # t in the format of ddMMMyyyy hhmm
    d=str(t)
    return "%d,%d,%d"%(int(d[5:9]),int(get_month(d[2:5])),int(d[0:2]))

def get_interval_of_ref(ref):
    p = ref.pathname
    part_e = p.getPart(p.E_PART)
    if part_e[-3:]=='MIN':
        a = int(part_e[:-3])
    if part_e[-4:]=='HOUR':
        a = int(part_e[:-4])*60
    if part_e[-3:]=='DAY':
        a = int(part_e[:-3])*1440
    if part_e[-3:]=='MON':
        a = int(part_e[:-3])*1440*30
    if part_e[-5:]=='MONTH':
        a = int(part_e[:-5])*1440*30
    return a

def get_bpart_list(group,cpart):
    a = []
    g = vdss.findparts(group,c=cpart)
    for ref in g:
        p = ref.pathname
        a.append(p.getPart(p.B_PART))
    return list(set(a))

def get_cpart_list(group):
    a = []
    for ref in group:
        p = ref.pathname
        c_part_name=p.getPart(p.C_PART).encode('ascii')
        if c_part_name not in a:
            if c_part_name=='FLOW' or c_part_name=='EC' or c_part_name=='STAGE':
                a.insert(0,c_part_name)
            else:
                a.append(c_part_name)
    return a

def get_bpart_from_refvar(refvar):
    a = refvar.split("/")
    return a[2]

def get_cpart_from_refvar(refvar):
    a = refvar.split("/")
    return a[3]    

def get_cpart_from_spec(var_values, output_values):
    cpart1 = get_cpart_from_var(var_values)
    cpart2 = get_cpart_from_output(output_values)
    for i in cpart2:
        if i not in cpart1:
            cpart1.append(i)
    return cpart1
    
def get_cpart_from_var(var_values):
    cpart = []
    for i in var_values:
        for j in range(1,3):
            a = i[j].split("/")
            if len(a)>5:
                b = a[len(a)-5].encode('ascii')
            else:
                b = ""
            if b not in cpart:
                cpart.append(b)
    return cpart

def get_cpart_from_output(output_values):
    cpart = []
    for i in output_values:
        a = i.split("_")
        if len(a)>1:
            b = a[len(a)-1].encode('ascii')
            if b not in cpart:
                cpart.append(b)
    return cpart                 

def get_name_of_ref(ref):
    if ref != None:
        p=ref.pathname
        return p.getPart(p.B_PART)

def get_name(ref1,ref2):
    if ref1==None:
        if ref2==None:
            return ""
        else:
            return get_name_of_ref(ref2)
    else:
        return get_name_of_ref(ref1)
def get_prop_of_ref(ref):
    intv = get_interval_of_ref(ref)
    var_name = get_name_of_ref(ref)
    data_units = get_units_of_ref(ref)
    data_type = get_type_of_ref(ref)
    return intv, var_name, data_units, data_type

def get_ref(group, path, calculate_dts=0):
    if calculate_dts==1:
        return None # TBD:
    refs = vdss.findpath(group, path)
    if refs == None:
        print "No data found for %s and %s" % (group, path)
    else:
        return refs[0]

def get_type_of_ref(ref):
    if ref != None:
        p=ref.pathname
        return p.getPart(p.C_PART)
    return ""

def get_type(ref1,ref2):
    if ref1==None:
        if ref2==None:
            return ""
        else:
            return get_type_of_ref(ref2)
    else:
        return get_type_of_ref(ref1)

def get_units_of_ref(ref):
    if ref != None:
        d=ref.data
        return d.attributes.YUnits
    return ""

def get_units(ref1, ref2):
    if ref1==None:
        if ref2==None:
            return ""
        else:
            return get_units_of_ref(ref2)
    else:
        return get_units_of_ref(ref1)

def get_path_of_file(dssfile_full_path):
    a=dssfile_full_path.split("\\")
    print a.replace(dssfile_full_path,a[len(a)-1]) 
    return a.replace(dssfile_full_path,a[len(a)-1])
        
    
        
def get_latlng(tbl,searchfor):
    for row in tbl:
        if searchfor.upper()==row[1]:
            return row
            break
    return None

def get_not_sort_refname(refname,output_values):
    new_not_out = []
    for name in refname:
        if name not in output_values:
            new_not_out.append(name)
    return output_values+new_not_out

def get_series_str(scalars,refvar):
    series0 = get_filename_from_refvar(refvar[0],scalars)+"::"+get_bpart_from_refvar(refvar[0])+"_"+get_cpart_from_refvar(refvar[0])
    series1 = get_filename_from_refvar(refvar[1],scalars)+"::"+get_bpart_from_refvar(refvar[1])+"_"+get_cpart_from_refvar(refvar[1])
    series2 = get_filename_from_refvar(refvar[2],scalars)+"::"+get_bpart_from_refvar(refvar[2])+"_"+get_cpart_from_refvar(refvar[2])
    return series0, series1, series2                   

def get_series_name(mode,scalars,refvar):
    if refvar[0][4]=='0' and refvar[1][4]=='1' and refvar[2][4]=='2':
        if get_bpart_from_refvar(refvar[0])==get_bpart_from_refvar(refvar[1]) and get_bpart_from_refvar(refvar[1])==get_bpart_from_refvar(refvar[2]):
            if get_cpart_from_refvar(refvar[0])==get_cpart_from_refvar(refvar[1]) and get_cpart_from_refvar(refvar[1])==get_cpart_from_refvar(refvar[2]):
                series0 = scalars['NAME0']
                series1 = scalars['NAME1']
                series2 = scalars['NAME2']
            else:
                series0, series1, series2  = get_series_str(scalars,refvar)
        else:
            series0, series1, series2  = get_series_str(scalars,refvar)
    else:
        series0, series1, series2  = get_series_str(scalars,refvar)
    if mode=='1':
        return [series0,"",""]
    if mode=='2':
        return ["",series1,""]
    if mode=='3':
        return ["",series1,series2]
    if mode=='4':
        return [series0,series1,""]        
    if mode=='5':
        return [series0,series1,series2]
        
def get_filename_from_refvar(refvar,scalars):
    return scalars['NAME'+refvar[4]]
    
def get_str_refvar(refvar,compare_mode):
    if compare_mode=='1': str_refvar = refvar[0]+",,"
    if compare_mode=='2': str_refvar = ","+refvar[1]+","
    if compare_mode=='3': str_refvar = ","+refvar[1]+","+refvar[2]
    if compare_mode=='4': str_refvar = refvar[0]+","+refvar[1]+","
    if compare_mode=='5': str_refvar = refvar[0]+","+refvar[1]+","+refvar[2]
    return str_refvar

def get_wyt_array(lines,start_yr,end_yr):
    wyt = {}
    wyt['W']=[]
    wyt['AN']=[]
    wyt['BN']=[]
    wyt['D']=[]
    wyt['C']=[]
    for line in lines:
        a=line.split(",")
        if a[0][22:26]>start_yr and a[0][5:9]<end_yr:
            wy = a[1].replace("\n","")
            wyt[wy].append(a[0])
    return wyt
        
def get_month(value):
    valueDic= {"JAN":"01", "FEB":"02", "MAR":"03", "APR":"04", "MAY":"05", "JUN":"06", "JUL":"07", "AUG":"08", "SEP":"09", "OCT":"10", "NOV":"11", "DEC":"12"}
    return valueDic[value]

def is_match(b_arr,c_arr,search_b,search_c):
    for i in range(len(b_arr)):
        if b_arr[i]==search_b and c_arr[i]==search_c:
            return 1
            break
    return 0
  
def ref2ds(ref,tw=None,time_interval=None):
    if isinstance(ref, DataReference):
        ds = ref.getData()
    else:
        ds = ref
    if not isinstance(ds,RegularTimeSeries):
        print ref, " is not a regular time-series data set and has been converted to a regular time series."
        ds = toreg(ds,tw,time_interval)
    return ds     
    
def sum(data, tw):
    try:
        return vtimeseries.total(data.createSlice(tw))
    except:
        return float('nan')
    
def avg(data, tw):
    try:
        ds = data.createSlice(tw)
        return vtimeseries.total(ds)/len(ds)
    except:
        return float('nan')    

def _logpath(path, names):
    logging.info('Working in %s' % path)
    return []   # nothing will be ignored
    
def convert_to_date(time_val):
    from java.util import TimeZone, Date
    return Date(time_val.date.time - TimeZone.getDefault().getRawOffset())

def multi_iterator(dsarray, filter=None):
    from vista.set import MultiIterator, Constants
    if filter == None:
        iterator = MultiIterator(dsarray)
    else:
        iterator = MultiIterator(dsarray, filter)
    return iterator

def extract_name_from_ref(ref):
    p = ref.pathname
    return "%s @ %s" % (p.getPart(p.C_PART), p.getPart(p.B_PART))

def lines2table(filename):
    f = open(filename,'r')
    b = []
    lines = f.readlines()
    i=0
    for line in lines:
        line = line.replace("\n","")
        a = line.split(",")
        if i>0:
            b.append(a)
        i+=1
    f.close()
    return b

def sort(ref):
    from vista.set import Constants
    from vista.set import ElementFilterIterator
    dx=[]
    iter=ElementFilterIterator(ref.data.iterator, Constants.DEFAULT_FLAG_FILTER)
    while not iter.atEnd():
        dx.append(iter.element.y)
        iter.advance()
    dx.sort()
    return dx

def sort_alphabetically(arr):
    new_arr=[]
    for i in arr:
        new_arr.append(i.encode('ascii').upper())
    new_arr.sort()
    return new_arr

def sum(data, tw):
    try:
        return vtimeseries.total(data.createSlice(tw))
    except:
        return float('nan')

def select_distinct(input_arr):
  output_arr = []
  for x in input_arr:
    if x not in output_arr:
      output_arr.append(x)
  output_arr.sort()
  return output_arr

def timewindow_option_value(tw):
    return format_time_as_year_month_day(tw.startTime)+"-"+format_time_as_year_month_day(tw.endTime)

def show_gui():
    """
    Shows a GUI to select dss files to compare and select an input file
    """
    from javax.swing import JPanel, JFrame, JButton, SpringLayout, JTextBox
    from javax.swing.border import LineBorder
    textBox1 = JTextBox()
    textBox2 = JTextBox()
    file1ChooseButton = JButton("Choose File")
    file2ChooseButton = JButton("Choose File")
    contentPane = JPanel(SpringLayout())
    contentPane.setBorder(LineBorder(Color.blue))
    contentPane.add(JLabel("Alternative DSS File"))
    contentPane.add(textBox1)
    contentPane.add(file1ChooseButton)
    contentPane.add(JLabel("Base DSS File"))
    contentPane.add(textBox2)
    contentPane.add(file2ChooseButton)
    fr = JFrame("Calsim Report Generator")
    fr.contentPane().add(contentPane)
    fr.pack();fr.show();
#
