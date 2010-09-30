import sys, os, shutil
import vdss, vutils, vdisplay, vtimeseries, vdiff
import js_data, js_data_list
import logging
from shutil import rmtree, copytree
class PlotType:
    TIME_SERIES="timeseries"
    EXCEEDANCE="exceedance"
class PathnameMap:
    def __init__(self, name):
        self.var_name = name;
        self.report_type = "Average"
        self.path1 = None
        self.path2 = None
        self.var_category = ""
            
def open_dss(dssfile):
    group = vutils.opendss(dssfile)
    if group == None:
        print "No dss file named: %s found!" % (dssfile)
    return group
#
def get_ref(group, path, calculate_dts=0):
    if calculate_dts==1:
        return None # TBD:
    refs = vdss.findpath(group, path)
    if refs == None:
        print "No data found for %s and %s" % (group, path)
    else:
        return refs[0]
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
def column(matrix, i):
    return [row[i] for row in matrix]    
def get_cpart_list(group):
    a = []
    for ref in group:
        p = ref.pathname
        a.append(p.getPart(p.C_PART))
    return list(set(a))
def get_bpart_list(group,cpart):
    a = []
    g = vdss.findparts(group,c=cpart)
    for ref in g:
        p = ref.pathname
        a.append(p.getPart(p.B_PART))
    return list(set(a))
def is_match(b_arr,c_arr,search_b,search_c):
    for i in range(len(b_arr)):
        if b_arr[i]==search_b and c_arr[i]==search_c:
            return 1
            break
    return 0
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
def get_latlng(tbl,searchfor):
    for row in tbl:
        if searchfor.upper()==row[1]:
            return row
            break
    return None
def build_data_array(ref1, ref2):
    from vista.set import Constants
    from vtimeseries import time
    import math
    if (ref1==None and ref2==None):
        return []
    iterator = multi_iterator([ref1.data, ref2.data], Constants.DEFAULT_FLAG_FILTER)
    darray=[]
    time_str = None
    while not iterator.atEnd():
        index = iterator.getIndex();
        e = iterator.getElement();
        date = convert_to_date(time(e.getXString()))
        darray.append((date.time, e.getY(0), e.getY(1)))
        iterator.advance();
    return darray
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
def copy_basic_files(output_path):
    if output_path[-19:-1]=='scripts\\compare_ds':
        print "*************************************"
        print "  Please select the other directory  "
        print "*************************************"
    else:
        if os.path.isdir(output_path+"//js"):
            shutil.rmtree(output_path+"//js")
        if not os.path.isdir(output_path+"//data"):
            os.mkdir(output_path+"//data")
        copytree(str(os.getenv('SCRIPT_HOME'))+"/js",str(output_path)+"/js",[])
        #copytree("../scripts/compare_dss/js",str(output_path)+"/js",[])
        #os.system('cmd /c xcopy "..\scripts\compare_dss\js" "'+output_path+'/js"')
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

def parse_template_file(template_file):
    from gov.ca.dsm2.input.parser import Parser
    p = Parser()                                 
    tables = p.parseModel(template_file)
    scalar_table = tables.getTableNamed("SCALAR")
    scalar_values = scalar_table.getValues()
    nscalars = scalar_values.size()
    scalars = {}
    for i in range(nscalars):
        name = scalar_table.getValue(i, "NAME")
        value = scalar_table.getValue(i, "VALUE")
        scalars[name] = value
    output_table = tables.getTableNamed("OUTPUT")
    output_values = output_table.getValues()
    timewindows = []
    timewindow_table = tables.getTableNamed("TIME_PERIODS")
    tw_values = timewindow_table.getValues();
    return scalars, output_values, tw_values

def do_processing(scalars, output_values, tw_values):
    # open files 1 and file 2 and loop over to plot
    from java.util import Date
    dss_group1 = vutils.opendss(scalars['FILE1'])
    dss_group2 = vutils.opendss(scalars['FILE2'])
    output_dir = scalars['OUTDIR']
    output_file = scalars['OUTFILE']
    copy_basic_files(output_dir)    # copy the /js folder over
    out_name = column(output_values,0)
    out_type = column(output_values,1)
    type_arr = get_cpart_list(dss_group1)
    len_group = len(dss_group1)
    #type_arr = ['spec','flow','stage','ec','others']
    series_name = [scalars['NAME1'],scalars['NAME2']]
    if dss_group1 == None or dss_group2 == None:
        sys.exit(2);
    if output_dir[-1]!='/': output_dir=output_dir+"/"
    time_windows = map(lambda val: val[1].replace('"',''), tw_values)
    tws = map(lambda x: vtimeseries.timewindow(x), time_windows)
    dIndex = 0
    dataIndex = {}
    data_output = {}
    spec_output_davg = {}
    spec_output_dmax = {}
    spec_output_dmin = {}
    spec_output_mavg = {}
    fm = {}
    fs_davg = {}
    fs_dmax = {}
    fs_dmin = {}
    fs_mavg = {}
    tbl_latlng = lines2table(output_dir+"js/latlng.txt")
    fl = open(output_dir+"data/data_list.js",'w')
    for i in type_arr:
        data_output[i] = output_dir+"data/"+output_file.split(".")[0]+"_"+i+".js"
        spec_output_davg[i] = output_dir+"data/"+output_file.split(".")[0]+"_spec_davg_"+i+".js"
        spec_output_dmax[i] = output_dir+"data/"+output_file.split(".")[0]+"_spec_dmax_"+i+".js"
        spec_output_dmin[i] = output_dir+"data/"+output_file.split(".")[0]+"_spec_dmin_"+i+".js"
        spec_output_mavg[i] = output_dir+"data/"+output_file.split(".")[0]+"_spec_mavg_"+i+".js"
        fm[i] = open(data_output[i],'w')
        fs_davg[i] = open(spec_output_davg[i],'w')
        fs_dmax[i] = open(spec_output_dmax[i],'w')
        fs_dmin[i] = open(spec_output_dmin[i],'w')
        fs_mavg[i] = open(spec_output_mavg[i],'w')
        print >> fm[i], """/* Comparison Output File Generated on : %s */"""%(str(Date()))
        print >> fs_davg[i], """/* Comparison Output File Generated on : %s */"""%(str(Date()))
        print >> fs_dmax[i], """/* Comparison Output File Generated on : %s */"""%(str(Date()))
        print >> fs_dmin[i], """/* Comparison Output File Generated on : %s */"""%(str(Date()))
        print >> fs_mavg[i], """/* Comparison Output File Generated on : %s */"""%(str(Date()))        
        dataIndex[i] = 0
        dataIndex['spec_davg_'+i] = 0
        dataIndex['spec_dmax_'+i] = 0
        dataIndex['spec_dmin_'+i] = 0
        dataIndex['spec_mavg_'+i] = 0
        js_data.write_begin_data_array(fm[i])
        js_data.write_begin_data_array(fs_davg[i])
        js_data.write_begin_data_array(fs_dmax[i])
        js_data.write_begin_data_array(fs_dmin[i])
        js_data.write_begin_data_array(fs_mavg[i])
    js_data_list.write_begin_data_array(fl);
    for ref1 in dss_group1:
        p = ref1.pathname   
        g = vdss.findparts(dss_group2,b=p.getPart(p.B_PART),c=p.getPart(p.C_PART),e=p.getPart(p.E_PART))
        if g==None:
            continue
        ref2 = g[0]
        diff_arr = []
        if (ref1==None or ref2==None): 
            continue
        for i in range(len(tws)):
            ref1_tw = ref1.getData().createSlice(tws[i])
            ref2_tw = ref2.getData().createSlice(tws[i])        
            rmse_val = vdiff.rmse(ref1_tw, ref2_tw)
            perc_rmse_val = vdiff.perc_rmse(ref1_tw,ref2_tw)
            if rmse_val!=0:
                diff_arr.append([rmse_val, perc_rmse_val])
            else:
                diff_arr.append([rmse_val, float('nan')])
        ref1_godin = vtimeseries.godin(ref1)
        ref1_daily = vtimeseries.per_avg(ref1_godin,'1day')
        ref1_dmax = vtimeseries.per_max(ref1,'1day')
        ref1_dmin = vtimeseries.per_min(ref1,'1day')
        ref1_mavg = vtimeseries.per_avg(ref1,'1month')
        ref2_godin = vtimeseries.godin( ref2)
        ref2_daily = vtimeseries.per_avg(ref2_godin,'1day')
        ref2_dmax = vtimeseries.per_max(ref2,'1day')
        ref2_dmin = vtimeseries.per_min(ref2,'1day')
        ref2_mavg = vtimeseries.per_avg(ref2,'1month')        
        logging.debug('Working on index: %d/%d '%(dIndex+1,len_group))         
        var_name = get_name(ref1,ref2)
        data_units=get_units(ref1,ref2)
        data_type=get_type(ref1,ref2)
        dIndex = dIndex + 1
        latlng = get_latlng(tbl_latlng,p.getPart(p.B_PART))
        if latlng==None:
            latlng=['nan','nan','nan','nan']
            print "can't find the Lat/Lng for ",p.getPart(p.B_PART)
        if dIndex>1: 
            fl.write(",")
        if (is_match(out_name,out_type,p.getPart(p.B_PART),p.getPart(p.C_PART))):
            dataIndex['spec_davg_'+p.getPart(p.C_PART)]=dataIndex['spec_davg_'+p.getPart(p.C_PART)]+1
            dataIndex['spec_dmax_'+p.getPart(p.C_PART)]=dataIndex['spec_dmax_'+p.getPart(p.C_PART)]+1
            dataIndex['spec_dmin_'+p.getPart(p.C_PART)]=dataIndex['spec_dmin_'+p.getPart(p.C_PART)]+1
            dataIndex['spec_mavg_'+p.getPart(p.C_PART)]=dataIndex['spec_mavg_'+p.getPart(p.C_PART)]+1               
            if dataIndex['spec_davg_'+p.getPart(p.C_PART)]>1:
                fs_davg[p.getPart(p.C_PART)].write(",") 
            if dataIndex['spec_dmax_'+p.getPart(p.C_PART)]>1:
                fs_dmax[p.getPart(p.C_PART)].write(",")
            if dataIndex['spec_dmin_'+p.getPart(p.C_PART)]>1:
                fs_dmin[p.getPart(p.C_PART)].write(",")
            if dataIndex['spec_mavg_'+p.getPart(p.C_PART)]>1:
                fs_mavg[p.getPart(p.C_PART)].write(",")   
            write_list_data(fl,p.getPart(p.B_PART), p.getPart(p.C_PART), 1, diff_arr,latlng)
            write_plot_data(fs_davg[p.getPart(p.C_PART)], build_data_array(ref1_daily,ref2_daily), dataIndex['spec_davg_'+p.getPart(p.C_PART)], "%s"%var_name, series_name, "%s(%s)"%(data_type,data_units), "Time", PlotType.TIME_SERIES, p.getPart(p.C_PART),'Daily Average')           
            write_plot_data(fs_dmax[p.getPart(p.C_PART)], build_data_array(ref1_dmax,ref2_dmax), dataIndex['spec_dmax_'+p.getPart(p.C_PART)], "%s"%var_name, series_name, "%s(%s)"%(data_type,data_units), "Time", PlotType.TIME_SERIES, p.getPart(p.C_PART),'Daily Maximum')           
            write_plot_data(fs_dmin[p.getPart(p.C_PART)], build_data_array(ref1_dmin,ref2_dmin), dataIndex['spec_dmin_'+p.getPart(p.C_PART)], "%s"%var_name, series_name, "%s(%s)"%(data_type,data_units), "Time", PlotType.TIME_SERIES, p.getPart(p.C_PART),'Daily Minimum')           
            write_plot_data(fs_mavg[p.getPart(p.C_PART)], build_data_array(ref1_mavg,ref2_mavg), dataIndex['spec_mavg_'+p.getPart(p.C_PART)], "%s"%var_name, series_name, "%s(%s)"%(data_type,data_units), "Time", PlotType.TIME_SERIES, p.getPart(p.C_PART),'Monthly Average')           
        else:          
            write_list_data(fl,p.getPart(p.B_PART), p.getPart(p.C_PART), 0, diff_arr,latlng)
    logging.debug('Writing end of data array')
    for i in type_arr:
        js_data.write_end_data_array(fm[i]);
        js_data.write_end_data_array(fs_davg[i]);
        js_data.write_end_data_array(fs_dmax[i]);
        js_data.write_end_data_array(fs_dmin[i]);
        js_data.write_end_data_array(fs_mavg[i]);
        fm[i].close()
        fs_davg[i].close()
        fs_dmax[i].close()
        fs_dmin[i].close()
        fs_mavg[i].close()
    js_data_list.write_end_data_array(fl)
    fl.close()
    # Generate the main html file
    fh = open(output_dir+scalars['OUTFILE'],'w')
    print >> fh, """ 
<html>
<head>
<title>DSM2 Report: %s vs %s</title>
<script type="text/javascript" src="%s"></script>
<script type="text/javascript" src="data/data_list.js"></script>
<script type="text/javascript" src="js/protovis-d3.3.js"></script>
<script type="text/javascript" src="js/plots.js"></script>
<script type="text/javascript" src="js/jquery-1.4.2.min.js"></script>
<script type="text/javascript" src="js/calendar.js"></script>
<link rel="stylesheet" type="text/css" media="print" href="js/print.css" /> 
<link rel="stylesheet" type="text/css" media="screen" href="js/screen.css" />
<link rel="stylesheet" type="text/css"  href="js/calendar.css" />
<script type="text/javascript" src="js/common.js"></script>
<script type="text/javascript" src="js/tabber.js"></script>
<link rel="stylesheet" href="js/tabber.css" TYPE="text/css" MEDIA="screen">
<link rel="stylesheet" href="js/tabber-print.css" TYPE="text/css" MEDIA="print">
<script src="http://maps.google.com/maps?file=js" type="text/javascript"></script>
<script type="text/javascript">
document.write('<style type="text/css">.tabber{display:none;}<\/style>');
var pre_tab="FLOW";
var pre_period="davg";
</script>
</head><body onunload="GUnload()">
"""%(scalars['NAME1'],scalars['NAME2'],spec_output_davg['FLOW'])
    tws = write_summary_table(fh,dss_group1, dss_group2, scalars, tw_values)
    part_c = get_cpart_list(dss_group1)
    dt_arr = "dt_arr=["
    peri_name = "period_name=["
    peri_range = "period_range=["    
    for type_item in part_c:
        dt_arr+= "'"+type_item+"',"
    for i in range(len(tws)):
        peri_name+= '"'+tw_values[i][0].replace('"','')+'",'
    for tw in tws:
        peri_range+= '"'+format_timewindow(tw)+'",'
    print >> fh, '<script type="text/javascript">'+dt_arr+"];"  
    print >> fh, peri_name+"];"
    print >> fh, peri_range+"]; </script>" 
    write_js_block(fh)
    fh.close()
    fireup(output_dir+scalars['OUTFILE'])
    logging.debug('Closed out data file')
def cfs2taf(data):
    from vista.report import TSMath
    data_taf = TSMath.createCopy(data)
    TSMath.cfs2taf(data_taf)
    return data_taf
def sum(data, tw):
    try:
        return vtimeseries.total(data.createSlice(tw))
    except:
        return float('nan')
def format_timewindow(tw):
    from vista.time import SubTimeFormat
    year_format = SubTimeFormat('yyyy')
    return tw.startTime.format(year_format) + "-" + tw.endTime.format(year_format)
def format_time_as_year_month_day(t):  # t in the format of ddMMMyyyy hhmm
    d=str(t)
    return "%d,%d,%d"%(int(d[5:9]),int(get_month(d[2:5])),int(d[0:2]))

def get_month(value):
    valueDic= {"JAN":"01", "FEB":"02", "MAR":"03", "APR":"04", "MAY":"05", "JUN":"06", "JUL":"07", "AUG":"08", "SEP":"09", "OCT":"10", "NOV":"11", "DEC":"12"}
    return valueDic[value]
    
def timewindow_option_value(tw):
    return format_time_as_year_month_day(tw.startTime)+"-"+format_time_as_year_month_day(tw.endTime)
#
def write_summary_table(fh, dss_group1,dss_group2,scalars,tw_values):
    import vtimeseries
    print >> fh, "<h1><center>DSM2 Output Comparison Report<br> %s <n>vs</n> %s</center></h1>"%(scalars['NAME1'], scalars['NAME2'])
    print >> fh, '<div id="note">Note: %s</div>'%(scalars['NOTE'].replace('"',''))
    print >> fh, '<div id="assumptions">Assumptions: %s</div>'%(scalars['ASSUMPTIONS'].replace('"',''))   
    print >> fh, """<div id="control-panel"> 
<form>
Data Conversion for Plot: <select name="datatype" onChange="change_period()" id="data-conversion">
    <option value="davg"> Daily Average</option>
    <option value="dmax"> Daily Maximum</option>
    <option value="dmin"> Daily Minimim</option>
    <option value="mavg"> Monthly Average</option>
</select><br> 

Use Defined Time Window: <select name="tw" id="time-window-select"> 
"""
    for i in range(len(tw_values)):
        if i==0:
            selected = 'selected="selected"'
        else:
            selected = ""
        print >> fh, '<option value="%s" %s>%s</option>'%(timewindow_option_value(vtimeseries.timewindow(tw_values[i][1].replace('"',''))), selected,tw_values[i][0].replace('"','')) 
    print >> fh, """ 
</select> 
<div>
Customize the Time Window:
"""
    twstr = timewindow_option_value(vtimeseries.timewindow(tw_values[0][1].replace('"',''))).split("-")
    a1=twstr[0].split(",")
    a2=twstr[1].split(",")
    print >> fh, """Start Date: <input name="SDate" id="SDate" value="%s" onclick="displayDatePicker('SDate');" size=12>"""%(a1[1]+"/"+a1[2]+"/"+a1[0])
    print >> fh, """End Date: <input name="EDate" id="EDate" value="%s" onclick="displayDatePicker('EDate');" size=12>"""%(a2[1]+"/"+a2[2]+"/"+a2[0])
    print >> fh, """
  <input type=button id="calendar" value="Re-draw">
</div>
<div>
    Show differences on plot:<input type="checkbox" name="diff_plot" value="1"/>
</div>
<div> 
    Threshold value to highlight differences
    <input type="text" id="threshold" value="50"/> 
</div>
<div id="warning" style="color:red;font-weight:bold"></div> 
<input type="hidden" name="ta" id="ta" value=""> 
</form> 
</div> 
    """
    time_windows = map(lambda val: val[1].replace('"',''), tw_values)
    tws = map(lambda x: vtimeseries.timewindow(x), time_windows)
    print >> fh, '<div class="tabber">'
    part_c = get_cpart_list(dss_group1)
    for type_item in part_c:
        part_b = get_bpart_list(dss_group1,type_item)
        print >> fh, '<div class="tabbertab" id="%s"><h2>%s</h2><p id="%s_p">'%(type_item,type_item,type_item)
        print >> fh, '</p></div>'
    print >> fh, '</div>'
    return tws 
        
#
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
def write_plot_data(fh, data, dataIndex,  title, series_name, yaxis, xaxis, plot_type, data_type, per_opt):
    js_data.write_file(fh, data, dataIndex, title, series_name, yaxis, xaxis, plot_type, data_type,per_opt);
def write_list_data(fh, name, data_type, checked, diff,latlng):
    js_data_list.write_file(fh, name, data_type, checked, diff,latlng);
#

def write_js_block(fh):
    print >> fh, """<script type="text/javascript">
    function reload_js(){
        tab_name = document.getElementById('ta').value; 
        per_name = document.getElementById('data-conversion').value;
        replacejscssfile("data/DSM2_compare_spec_"+pre_period+"_"+pre_tab+".js", "data/DSM2_compare_spec_"+per_name+"_"+tab_name+".js", "js");
        if (document.getElementById('ta').value=='STAGE' && document.getElementById('data-conversion').value=='davg')
           {document.getElementById('warning').innerHTML='Daily Average Stage is meaningless! Please select daily max/min for plotting.';}
        else {document.getElementById('warning').innerHTML=''; }
        pre_tab = tab_name;
        pre_period = per_name;       
    }
    function clear_and_draw(sdate, edate){
        $('.plot').empty();
        tab_name = document.getElementById('ta').value; 
        n=data.length
        plot_diff = $('input[name=diff_plot]').is(':checked') ? 1 : 0 ;
        for(i=0; i < n; i++){
          if (data[i].data_type==tab_name){
            var div_id = "fig"+"_"+data[i].data_type+"_"+data[i].title;
            if (data[i]==null) continue;
            if ($("#"+div_id).length==0){
                $("#"+data[i].data_type).append('<a href="#'+div_id+'"><div class="plot" id="'+div_id+'"></div></a>');
            }
            if (data[i].plot_type=="timeseries"){
                plots.time_series_plot(div_id,data[i],plot_diff,sdate,edate);
            }else if (data[i].plot_type=="exceedance"){
                plots.exceedance_plot(div_id,data[i],null,sdate,edate);
            }
          }
        }      
    };
    function hideDiv(div_name) {
     if ($("#img").attr("src")=="js/open.JPG") {
       $("#"+div_name).show("slow");
       $("#img").attr("src","js/close.JPG");
     }else{ 
       $("#"+div_name).hide("slow"); 
       $("#img").attr("src","js/open.JPG");
     }
    }
    function location_list(){
       ns=data_list.length;
       tab_name = document.getElementById('ta').value;
       $("#"+tab_name+"_p").empty();
       i = dt_arr.indexOf(tab_name);
       sd=extract_date(to_date_comma($("#SDate").val()));
       ed=extract_date(to_date_comma($("#EDate").val()));
       tbl_sel='<a href="#" onClick="clear_and_draw(extract_date(to_date_comma($(\\'#SDate\\').val())),extract_date(to_date_comma($(\\'#EDate\\').val())));" onMouseover="this.style.background=\\'#C8F526\\'" onMouseout="this.style.background=\\'\\'"><img src="js/chart.JPG" width="20px" height="19px"> Show the time series plots</a>';       
       tbl_sel+='<table class="alt-highlight" id="tbl_sel'+i+'"><tr><th colspan=7>DSM2 Output Comparison - RMSE Statistics (<a href="#" onClick="initialize(this)"> View Map </a>)<br>This is calculated from the original time series in dss file based on its output time interval.'
       tbl_sel+='<br><center><div id="map_canvas'+tab_name+'" style="width: 500px; height: 450px;display:none"></div></center></th></tr>'; k1=0;
       tbl_sel+='<tr><td></td>';
       for(k=0;k<(data_list[0].diff).length;k++) tbl_sel+='<td colspan=2 class="timewindow">'+period_name[k]+'</td>';
       tbl_sel+='</tr>';
       tbl_sel+='<tr><td></td>';
       for(k=0;k<(data_list[0].diff).length;k++) tbl_sel+='<td colspan=2 class="timewindow">'+period_range[k]+'</td>';
       tbl_sel+='</tr><tr><td></td>';
       for(k=0;k<(data_list[0].diff).length;k++) tbl_sel+='<td class="timewindow" width=80>RMSE</td><td class="timewindow" width=80>Percentage RMSE</td>';
       tbl_sel+='</tr>';
       tbl_unsel='<div id="all'+i+'" style="display:none"><table class="alt-highlight" id="tbl_unsel'+i+'"><tr><td colspan=7></td></tr>';k2=0;
       for(j=0;j<ns;j++){
        if(data_list[j].data_type==dt_arr[i] && data_list[j].checked=='1'){ k1++;
          tbl_sel+='<tr class="d'+k1%2+'"><td width="20%"><input type=checkbox checked><a href="#fig_'+ data_list[j].data_type+'_'+data_list[j].name+'">'+data_list[j].name+'</a></td>'
          for(k=0;k<(data_list[j].diff).length;k++){
             tbl_sel+='<td>'+data_list[j].diff[k].rmse+'</td><td>'+data_list[j].diff[k].perc_rmse+'%</td>';
          }
          tbl_sel+='</tr>';
        }
        if(data_list[j].data_type==dt_arr[i] && data_list[j].checked=='0'){ k2++;
          tbl_unsel+='<tr class="d'+k2%2+'"><td width="20%"><input type=checkbox>'+data_list[j].name+'</td>'
          for(k=0;k<(data_list[j].diff).length;k++){
             tbl_unsel+='<td width=80>'+data_list[j].diff[k].rmse+'</td><td width=80>'+data_list[j].diff[k].perc_rmse+'%</td>';
          }
          tbl_unsel+='</tr>';             
        }
       }
       tbl_sel+='</table>';      
       tbl_sel+='<img id="img" src="js/open.JPG" onClick=hideDiv("all'+i+'")> Open all stations<br>';
       tbl_unsel+='</table></div>';           
       $("#"+dt_arr[i]+"_p").append(tbl_sel);
       $("#"+dt_arr[i]+"_p").append(tbl_unsel);
    };
    $('#system-water-balance-table tr').click(function(){
        var_name = $($(this).find('td')[0]).text();
        var svg_element = $('div').filter(function(){ return $(this).text().indexOf(var_name)>=0;})
        if (svg_element && svg_element.length > 0){
            anchor_name = $($(svg_element[0]).parent()).attr("href");
            window.location.href=window.location.href.split('#')[0]+anchor_name;
        }
    });
</script> 
<script type="text/javascript"> 
    $('#time-window-select').change(function(){
        var changed_val = $('#time-window-select option:selected').val().split("-");
        $('#SDate').val(to_date_str(changed_val[0]));
        $('#EDate').val(to_date_str(changed_val[1]));
        clear_and_draw(extract_date(changed_val[0]),extract_date(changed_val[1]));
    });
    $('#threshold').change(function(){
        set_diff_threshold($('#threshold').val());
    });
    $('input[name=diff_plot]').change(function(){
        sdate = extract_date(to_date_comma($("#SDate").val()));
        edate = extract_date(to_date_comma($("#EDate").val()));
        clear_and_draw(sdate,edate);
    });
    $('#calendar').click(function(){
        clear_and_draw(extract_date(to_date_comma($('#SDate').val())),extract_date(to_date_comma($('#EDate').val())));
    });
    function set_diff_threshold(threshold){
        $('td').each(function(){ 
            if ($(this).text().search('%')>=0){
                if(Math.abs(parseFloat($(this).text().split('%')[0])) >= threshold){
                    $(this).addClass('large-diff');
                }else{
                    $(this).removeClass('large-diff');
                } 
            }
        });
    }
    set_diff_threshold($('#threshold').val());
</script> 
</body> 
</html>"""    

def fireup(html):
    if os.path.exists("C:/Program Files/Google/Chrome/Application/chrome.exe"):
        os.system('cmd /c start chrome "'+html+'"')        
    elif os.path.exists("C:/Program Files/Mozilla Firefox/firefox.exe"):
        os.system('cmd /c start firefox "'+html+'"')
    else:
        print "Please install Chrome and Firefox for best performance."
        os.system('cmd /c start "'+html+'"')

if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG)
    #template_file = 'dsm2_compare_template.inp'
    if len(sys.argv) != 2:
        #print 'Usage: dss_compare_template.inp'
        print "Please specify the report inp file!"
        exit(1)
    template_file = sys.argv[1]
    logging.debug('Parsing input template file %s'%template_file)
    from time import strftime
    print "Starting at: ",strftime("%a, %d %b %Y %H:%M:%S")
    #parse template file
    scalars, output_values, tw_values = parse_template_file(template_file)
    # do processing
    do_processing(scalars, output_values, tw_values)
    logging.debug('Done processing. The end!')
    print "End at: ",strftime("%a, %d %b %Y %H:%M:%S")
    sys.exit(0) 