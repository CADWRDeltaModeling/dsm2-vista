import sys
import vdss, vutils, vdisplay, vmath, vtimeseries
import js_data, js_data_list
import logging
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
def sum(data, tw):
    import vmath
    try:
        return vmath.total(data.createSlice(tw))
    except:
        return float('nan')
def avg(data, tw):
    import vmath
    try:
        ds = data.createSlice(tw)
        return vmath.total(ds)/len(ds)
    except:
        return float('nan')    
    
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
    output_file = scalars['OUTFILE']
    out_name = column(output_values,0)
    out_type = column(output_values,1)
    type_arr = ['spec','flow','stage','ec','others']
    series_name = [scalars['NAME1'],scalars['NAME2']]
    if dss_group1 == None or dss_group2 == None:
        sys.exit(2);
    time_windows = map(lambda val: val[1].replace('"',''), tw_values)
    tws = map(lambda x: vtimeseries.timewindow(x), time_windows)
    dIndex = 0
    dataIndex = {}
    data_output = {}
    fm = {}
    fl = open("data_list.js",'w')
    for i in type_arr:
        data_output[i] = output_file.split(".")[0]+"_"+i+".js"
        fm[i] = open(data_output[i],'w')
        print >> fm[i], """/* Comparison Output File Generated on : %s */"""%(str(Date()))
        dataIndex[i] = 0
        js_data.write_begin_data_array(fm[i]);
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
            avg1 = avg(ref1.getData(),tws[i])
            avg2 = avg(ref2.getData(),tws[i])
            if avg1!=0:
                diff_arr.append([avg1, avg2, avg2-avg1, (avg2-avg1)/avg1*100])
            else:
                diff_arr.append([avg1, avg2, avg2-avg1, float('nan')])
        ref1_godin = vmath.godin(ref1)
        ref1_daily = vmath.per_avg(ref1_godin,'1day')
        ref2_godin = vmath.godin(ref2)
        ref2_daily = vmath.per_avg(ref2_godin,'1day')       
        logging.debug('Working on index: %d'%dIndex)         
        var_name = get_name(ref1,ref2)
        data_units=get_units(ref1,ref2)
        data_type=get_type(ref1,ref2)
        dIndex = dIndex + 1
        if dIndex>1: 
            fl.write(",")           
        try:
            a1 = out_name.index(p.getPart(p.B_PART))
            b1 = out_type.index(p.getPart(p.C_PART))
            dataIndex['spec']=dataIndex['spec']+1            
            if dataIndex['spec']>1:
                fm['spec'].write(",")              
            write_list_data(fl,p.getPart(p.B_PART), p.getPart(p.C_PART), 1, diff_arr)
            write_plot_data(fm['spec'], build_data_array(ref1_daily,ref2_daily), dataIndex['spec'], "%s"%var_name, series_name, "%s(%s)"%(data_type,data_units), "Time", PlotType.TIME_SERIES, p.getPart(p.C_PART))           
        except:           
            write_list_data(fl,p.getPart(p.B_PART),p.getPart(p.C_PART), 0, diff_arr)
    logging.debug('Writing end of data array')
    for i in type_arr:
        js_data.write_end_data_array(fm[i]);
        fm[i].close()
    js_data_list.write_end_data_array(fl)
    fl.close()
    # Generate the main html file
    fh = open(scalars['OUTFILE'],'w')
    print >> fh, """ 
<html>
<head>
<title>DSM2 Report: %s vs %s</title>
<script type="text/javascript" src="%s"></script>
<script type="text/javascript" src="data_list.js"></script>
<script type="text/javascript" src="protovis-d3.3.js"></script>
<script type="text/javascript" src="plots.js"></script>
<script type="text/javascript" src="jquery-1.4.2.min.js"></script>
<script type="text/javascript" src="calendar.js"></script>
<link rel="stylesheet" type="text/css" media="print" href="print.css" /> 
<link rel="stylesheet" type="text/css" media="screen" href="screen.css" />
<link rel="stylesheet" type="text/css"  href="calendar.css" />
<script type="text/javascript" src="tabber.js"></script>
<link rel="stylesheet" href="example.css" TYPE="text/css" MEDIA="screen">
<link rel="stylesheet" href="example-print.css" TYPE="text/css" MEDIA="print">
<script type="text/javascript">
document.write('<style type="text/css">.tabber{display:none;}<\/style>');
</script>
</head>
"""%(scalars['NAME1'],scalars['NAME2'],data_output['spec'])
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
    logging.debug('Closed out data file')
def cfs2taf(data):
    from vista.report import TSMath
    data_taf = TSMath.createCopy(data)
    TSMath.cfs2taf(data_taf)
    return data_taf
def sum(data, tw):
    import vmath
    try:
        return vmath.total(data.createSlice(tw))
    except:
        return float('nan')
def format_timewindow(tw):
    from vista.time import SubTimeFormat
    year_format = SubTimeFormat('yyyy')
    return tw.startTime.format(year_format) + "-" + tw.endTime.format(year_format)
def format_time_as_year_month_day(t):
    from java.util import Calendar, TimeZone
    gmtCal = Calendar.getInstance(TimeZone.getTimeZone('GMT'))
    gmtCal.setTime(t.date)
    return "%d,%d,%d"%(gmtCal.get(Calendar.YEAR),gmtCal.get(Calendar.MONTH),gmtCal.get(Calendar.DATE))
def timewindow_option_value(tw):
    return format_time_as_year_month_day(tw.startTime)+"-"+format_time_as_year_month_day(tw.endTime)
#
def write_summary_table(fh, dss_group1,dss_group2,scalars,tw_values):
    import vtimeseries
    print >> fh, "<h1>DSM2 Output Comparison: %s vs %s</h1>"%(scalars['NAME1'], scalars['NAME2'])
    print >> fh, '<div id="note">Note: %s</div>'%(scalars['NOTE'].replace('"',''))
    print >> fh, '<div id="assumptions">Assumptions: %s</div>'%(scalars['ASSUMPTIONS'].replace('"',''))    
    print >> fh, """<div id="control-panel"> 
<form>
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
    twstr = timewindow_option_value(vtimeseries.timewindow(tw_values[i][1].replace('"',''))).split("-")
    a1=twstr[0].split(",")
    a2=twstr[1].split(",")
    print >> fh, """Start Date: <input name="SDate" id="SDate" value="%s" onclick="displayDatePicker('SDate');" size=10>"""%(a1[1]+"/"+a1[2]+"/"+a1[0])
    print >> fh, """End Date: <input name="EDate" id="EDate" value="%s" onclick="displayDatePicker('EDate');" size=10>"""%(a2[1]+"/"+a2[2]+"/"+a2[0])
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
</form> 
</div> 
    """
    time_windows = map(lambda val: val[1].replace('"',''), tw_values)
    tws = map(lambda x: vtimeseries.timewindow(x), time_windows)
    print >> fh, '<div class="tabber">'
    part_c = get_cpart_list(dss_group1)
    for type_item in part_c:
        part_b = get_bpart_list(dss_group1,type_item)
        print >> fh, '<div class="tabbertab" id="%s"><h2>%s</h2><p>'%(type_item,type_item)
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
def write_plot_data(fh, data, dataIndex,  title, series_name, yaxis, xaxis, plot_type, data_type):
    js_data.write_file(fh, data, dataIndex, title, series_name, yaxis, xaxis, plot_type, data_type);
def write_list_data(fh, name, data_type, checked, diff):
    js_data_list.write_file(fh, name, data_type, checked, diff);
#


def write_js_block(fh):
    print >> fh, """<script type="text/javascript"> 
    function clear_and_draw(sdate, edate){
        $('.plot').empty();
        n=data.length
        plot_diff = $('input[name=diff_plot]').is(':checked') ? 1 : 0 ;
        for(i=0; i < n; i++){
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
    };
    function hideDiv(div_name) {
     if ($("#img").attr("src")=="images/open.JPG") {
       $("#"+div_name).show("slow");
       $("#img").attr("src","images/close.JPG");
     }else{ 
       $("#"+div_name).hide("slow"); 
       $("#img").attr("src","images/open.JPG");
     }
    }
    function location_list(){
       ns=data_list.length;
       for(i=0;i<dt_arr.length;i++){
        tbl_sel='<table class="alt-highlight" id="tbl_sel'+i+'"><tr><th colspan=13>DSM2 Output Comparison</th></tr>'; k1=0;
        tbl_sel+='<tr><td></td>';
        for(k=0;k<(data_list[0].diff).length;k++) tbl_sel+='<td colspan=4 class="timewindow">'+period_name[k]+'</td>';
        tbl_sel+='</tr>';
        tbl_sel+='<tr><td></td>';
        for(k=0;k<(data_list[0].diff).length;k++) tbl_sel+='<td colspan=4 class="timewindow">'+period_period_range[k]+'</td>';
        tbl_sel+='</tr>';
        tbl_unsel='<div id="all'+i+'" style="display:none"><table class="alt-highlight" id="tbl_unsel'+i+'"><tr><td colspan=13></td></tr>';k2=0;
        for(j=0;j<ns;j++){
         if(data_list[j].data_type==dt_arr[i] && data_list[j].checked=='1'){ k1++;
           tbl_sel+='<tr class="d'+k1%2+'"><td width="20%"><input type=checkbox checked><a href="#fig_'+ data_list[j].data_type+'_'+data_list[j].name+'">'+data_list[j].name+'</a></td>'
           for(k=0;k<(data_list[j].diff).length;k++){
              tbl_sel+='<td>'+data_list[j].diff[k].avg1+'</td><td>'+data_list[j].diff[k].avg2+'</td><td>'+data_list[j].diff[k].diff+'</td><td>'+data_list[j].diff[k].perc+'%</td>';
           }
           tbl_sel+='</tr>';
         }
         if(data_list[j].data_type==dt_arr[i] && data_list[j].checked=='0'){ k2++;
           tbl_unsel+='<tr class="d'+k2%2+'"><td width="20%"><input type=checkbox>'+data_list[j].name+'</td>'
           for(k=0;k<(data_list[j].diff).length;k++){
              tbl_unsel+='<td>'+data_list[j].diff[k].avg1+'</td><td>'+data_list[j].diff[k].avg2+'</td><td>'+data_list[j].diff[k].diff+'</td><td>'+data_list[j].diff[k].perc+'%</td>';
           }
           tbl_unsel+='</tr>';             
         }
        }
        tbl_sel+='</table>';      
        tbl_sel+='<img id="img" src="images/open.JPG" onClick=hideDiv("all'+i+'")> Open all stations<br>';
        tbl_unsel+='</table></div>';           
        $("#"+dt_arr[i]).append(tbl_sel);
        $("#"+dt_arr[i]).append(tbl_unsel);
       }
    };
    $(document).ready(location_list());
    $(document).ready(clear_and_draw(null,null));
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
    function extract_date(date_str){
        date_fields=date_str.split(",");
        return new Date(date_fields[0],date_fields[1],date_fields[2]);
    }
    function to_date_comma(calendar_date){
        fi = calendar_date.split("/");
        return fi[2]+","+fi[0]+","+fi[1];
    }
    function to_date_str(comma_date){
        fi = comma_date.split(",");
        return fi[1]+"/"+fi[2]+"/"+fi[0];    
    }
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
        var changed_val = $('#time-window-select option:selected').val().split("-");
        clear_and_draw(extract_date(changed_val[0]),extract_date(changed_val[1]));
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

if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG)
    #if len(sys.argv) != 2:
    #    print 'Usage: dss_compare_template.inp'
    #    exit(1)
    #template_file = sys.argv[1]
    template_file = 'dsm2_compare_template.inp'
    logging.debug('Parsing input template file %s'%template_file)
    #parse template file
    scalars, output_values, tw_values = parse_template_file(template_file)
    # do processing
    do_processing(scalars, output_values, tw_values)
    logging.debug('Done processing. The end!')
    sys.exit(0)   