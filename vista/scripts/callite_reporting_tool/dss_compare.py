import sys
import vdss, vutils, vdisplay
import js_data
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
    #load scalars into a map
    scalar_table = tables.getTableNamed("SCALAR")
    scalar_values = scalar_table.getValues()
    nscalars = scalar_values.size()
    scalars = {}
    for i in range(nscalars):
        name = scalar_table.getValue(i, "NAME")
        value = scalar_table.getValue(i, "VALUE")
        scalars[name] = value
    # load pathname mapping into a map
    pathname_mapping_table = tables.getTableNamed("PATHNAME_MAPPING")
    pmap_values = pathname_mapping_table.getValues()
    nvalues = pmap_values.size()
    pathname_maps=[]
    for i in range(nvalues):
        var_name = pathname_mapping_table.getValue(i, "VARIABLE")
        path_map = PathnameMap(var_name)
        path_map.report_type = pathname_mapping_table.getValue(i, "REPORT_TYPE")
        path_map.path1 = pathname_mapping_table.getValue(i, "PATH1")
        path_map.path2 = pathname_mapping_table.getValue(i, "PATH2")
        path_map.var_category = pathname_mapping_table.getValue(i, "VAR_CATEGORY")
        if path_map.path2 == None:
            path_map.path2 = path_map.path1
        pathname_maps.append(path_map)
    return scalars, pathname_maps
def do_processing(scalars, pathname_maps):
    # open files 1 and file 2 and loop over to plot
    from java.util import Date
    dss_group1 = vutils.opendss(scalars['FILE1'])
    dss_group2 = vutils.opendss(scalars['FILE2'])
    output_file=scalars['OUTFILE']
    data_output_file = output_file.split(".")[0]+".js"
    fh=open(data_output_file,'w')
    print >> fh, """/*
    Comparison Output File
    Generated on : %s
    */"""%(str(Date()))
    js_data.write_begin_data_array(fh);
    if dss_group1 == None or dss_group2 == None:
        sys.exit(2);
    dataIndex=0
    for path_map in pathname_maps:
        dataIndex=dataIndex+1
        logging.debug('Working on index: %d'%dataIndex)
        if dataIndex>1:
            fh.write(",")
        #path_map = pathname_mapping[var_name]
        var_name = path_map.var_name
        calculate_dts=0
        if path_map.report_type == 'Exceedance_Post':
            calculate_dts=1
        ref1 = get_ref(dss_group1, path_map.path1,calculate_dts)
        ref2 = get_ref(dss_group2, path_map.path2,calculate_dts)
        if (ref1==None or ref2==None): 
            continue
        series_name = [scalars['NAME1'],scalars['NAME2']]
        data_units=get_units(ref1,ref2)
        data_type=get_type(ref1,ref2)
        if path_map.report_type == 'Average':
            write_plot_data(fh, build_data_array(ref1,ref2), dataIndex, "Average %s"%path_map.var_name.replace('"',''), series_name, "%s(%s)"%(data_type,data_units), "Time", PlotType.TIME_SERIES)
        elif path_map.report_type == 'Exceedance':
            write_plot_data(fh, build_exceedance_array(ref1,ref2), dataIndex, "Exceedance %s" %path_map.var_name.replace('"',''), series_name, "%s(%s)"%(data_type,data_units), "Percent at or above", PlotType.EXCEEDANCE)
        elif path_map.report_type == 'Avg_Excd':
            write_plot_data(fh, build_data_array(ref1,ref2), dataIndex, "Average %s"%path_map.var_name.replace('"',''), series_name, "%s(%s)"%(data_type,data_units), "Time", PlotType.TIME_SERIES)
            write_plot_data(fh, build_exceedance_array(ref1,ref2), dataIndex, "Exceedance %s"%path_map.var_name.replace('"',''), series_name, "%s(%s)"%(data_type,data_units), "Percent at or above", PlotType.EXCEEDANCE)
        elif path_map.report_type == 'Timeseries':
            write_plot_data(fh, build_data_array(ref1,ref2), dataIndex, "Average %s"%path_map.var_name.replace('"',''), series_name, "%s(%s)"%(data_type,data_units), "Time", PlotType.TIME_SERIES)
        elif path_map.report_type == 'Exceedance_Post':
            write_plot_data(fh, build_exceedance_array(ref1,ref2), dataIndex, "Exceedance %s"%path_map.var_name.replace('"',''), series_name, "%s(%s)"%(data_type,data_units), "Percent at or above", PlotType.EXCEEDANCE)
    js_data.write_end_data_array(fh);
    logging.debug('Writing end of data array')
    fh.close()
    # Generate the main html file
    fh=open(scalars['OUTFILE'],'w')
    print >> fh, """ 
<html>
<head>
<title>Time Series Chart</title>
<script type="text/javascript" src="%s"></script>
<script type="text/javascript" src="protovis-d3.3.js"></script>
<script type="text/javascript" src="plots.js"></script>
</head>
"""%data_output_file
    write_water_balance_table(fh,dss_group1, dss_group2, scalars, pathname_maps)
    for index in range(len(pathname_maps)):
        print >> fh, """<div id="fig%d"></div>"""%index
    print >> fh, """<script type="text/javascript">
    n=data.length
    for(i=0; i < n; i++){
        if (data[i]==null) continue;
        if (data[i].plot_type=="timeseries"){
            time_series_plot("fig"+(i+1),data[i]);
        }else if (data[i].plot_type=="exceedance"){
            exceedance_plot("fig"+(i+1),data[i]);
        }
    }
</script>

</body>

</html>"""
    fh.close()    
    logging.debug('Closed out data file')
def cfs2taf(data):
    from vista.report import TSMath
    data_taf = TSMath.createCopy(data)
    TSMath.cfs2taf(data_taf)
    return data_taf
def sum(data, tw):
    import vmath
    return vmath.total(data.createSlice(tw))
def format_timewindow(tw):
    from vista.time import SubTimeFormat
    year_format = SubTimeFormat('yyyy')
    return tw.startTime.format(year_format) + "-" + tw.endTime.format(year_format)
#
def write_water_balance_table(fh, dss_group1,dss_group2,scalars,pathname_maps):
    import vtimeseries
    print >> fh, "<h1>System Water Balance Comparison: %s vs %s</h1>"%(scalars['NAME1'], scalars['NAME2'])
    print >> fh, "Note: %s"%(scalars['NOTE'])
    print >> fh, "Assumptions: %s"%scalars['ASSUMPTIONS']
    time_windows = ["01OCT1922 0100 - 30SEP2003 2400", "01OCT1929 0100 - 30SEP1934 2400", "01OCT1987 0100 - 30SEP1992 2400"]
    print >> fh, '<table>'
    print >> fh, '<tr><td colspan="4"></td>'
    tws = map(lambda x: vtimeseries.timewindow(x), time_windows)
    for tw in tws:
        print >> fh, '<td colspan="4">%s</td>'%format_timewindow(tw)
    print >> fh, "</tr>"
    for path_map in pathname_maps:
        var_name = path_map.var_name
        calculate_dts=0
        if path_map.report_type == 'Exceedance_Post':
            calculate_dts=1
        ref1 = get_ref(dss_group1, path_map.path1,calculate_dts)
        ref2 = get_ref(dss_group2, path_map.path2,calculate_dts)
        if (ref1==None or ref2==None):
            logging.debug("No data for %s"%path_map.var_name)
            print "No data found for %s"%(path_map.var_name) 
            continue
        data1=cfs2taf(ref1.data)
        data2=cfs2taf(ref2.data)
        print >> fh, "<tr>"
        if path_map.var_category in ("RF", "DI", "DO", "DE", "SWPSOD", "CVPSOD"):
            print >> fh, '<td colspan="4">%s</td>'%(path_map.var_name.replace('"',''))
            for tw in tws:
                s1=sum(data1, tw)
                s2=sum(data2, tw)
                diff=s2-s1
                pct_diff=diff/s1*100.0
                print >> fh, "<td>%0.1f</td><td>%0.1f</td><td>%0.1f</td><td>%0.1f %%</td>"%(s1,s2,diff,pct_diff)
        print >> fh, "</tr>"
    print >> fh, '</table>'
#
def show_gui():
    """
    Shows a GUI to select dss files to compare and select an input file
    """
    from javax.swing import JPanel, JFrame, JButton, SpringLayout, JTextBox
    from javax.swing.border import LineBorder
    textBox1 = new JTextBox();
    textBox2 = new JTextBox();
    contentPane = new JPanel(new SpringLayout())
    contentPane.setBorder(new LineBorder(Color.blue))
    contentPane.add(new JLabel("Alternative DSS File"))
    contentPane.add(textBox1)
    contentPane.add(new JLabel("Base DSS File"))
    contentPane.add(textBox2)
    SpringUtilities.makeCompactGrid(contentPane, 3, 3, 3, 3, 3, 3);
    //
    fr = JFrame("Calsim Report Generator")
    fr.contentPane().add(contentPane)
    fr.pack();fr.show();
#
def write_plot_data(fh, data, dataIndex,  title, series_name, yaxis, xaxis, plot_type):
    js_data.write_file(fh, data, dataIndex, title, series_name, yaxis, xaxis, plot_type);
#
if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG)
    if len(sys.argv) != 2:
        print 'Usage: dss_compare template.inp'
        exit(1)
    template_file = sys.argv[1]
    logging.debug('Parsing input template file %s'%template_file)
    #parse template file
    scalars, pathname_maps = parse_template_file(template_file)
    # do processing
    do_processing(scalars, pathname_maps)
    logging.debug('Done processing. The end!')
    sys.exit(0)