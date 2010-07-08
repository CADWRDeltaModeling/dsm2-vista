import sys
import vdss, vutils, vdisplay
import js_data
class PlotType:
    TIME_SERIES="timeseries"
    EXCEEDANCE="exceedance"
class PathnameMap:
    def __init__(self, name):
        self.var_name = name;
        self.report_type = "Average"
        self.path1 = None
        self.path2 = None
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
    pathname_mapping = {}
    for i in range(nvalues):
        var_name = pathname_mapping_table.getValue(i, "VARIABLE")
        path_map = PathnameMap(var_name)
        path_map.report_type = pathname_mapping_table.getValue(i, "REPORT_TYPE")
        path_map.path1 = pathname_mapping_table.getValue(i, "PATH1")
        path_map.path2 = pathname_mapping_table.getValue(i, "PATH2")
        if path_map.path2 == None:
            path_map.path2 = path_map.path1
        pathname_mapping[var_name] = path_map
    return scalars, pathname_mapping
def do_processing(scalars, pathname_mapping):
    # open files 1 and file 2 and loop over to plot
    from java.util import Date
    dss_group1 = vutils.opendss(scalars['FILE1'])
    dss_group2 = vutils.opendss(scalars['FILE2'])
    output_file=scalars['OUTFILE']
    fh=open(output_file,'w')
    print >> fh, """/*
    Comparison Output File
    Generated on : %s
    */"""%(str(Date()))
    js_data.write_begin_data_array(fh);
    if dss_group1 == None or dss_group2 == None:
        sys.exit(2);
    dataIndex=0
    for key in pathname_mapping.keys():
        dataIndex=dataIndex+1
        if dataIndex>1:
            fh.write(",")
        var_name = key
        path_map = pathname_mapping[var_name]
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
            write_plot_data(fh, build_data_array(ref1,ref2), dataIndex, "Average %s"%extract_name_from_ref(ref1), series_name, "%s(%s)"%(data_type,data_units), "Time", PlotType.TIME_SERIES)
        elif path_map.report_type == 'Exceedance':
            write_plot_data(fh, build_exceedance_array(ref1,ref2), dataIndex, "Exceedance %s" %extract_name_from_ref(ref1), series_name, "%s(%s)"%(data_type,data_units), "Percent at or above", PlotType.EXCEEDANCE)
        elif path_map.report_type == 'Avg_Excd':
            write_plot_data(fh, build_data_array(ref1,ref2), dataIndex, "Average %s"%extract_name_from_ref(ref1), series_name, "%s(%s)"%(data_type,data_units), "Time", PlotType.TIME_SERIES)
            write_plot_data(fh, build_exceedance_array(ref1,ref2), dataIndex, "Exceedance %s"%extract_name_from_ref(ref1), series_name, "%s(%s)"%(data_type,data_units), "Percent at or above", PlotType.EXCEEDANCE)
        elif path_map.report_type == 'Timeseries':
            write_plot_data(fh, build_data_array(ref1,ref2), dataIndex, "Average %s"%extract_name_from_ref(ref1), series_name, "%s(%s)"%(data_type,data_units), "Time", PlotType.TIME_SERIES)
        elif path_map.report_type == 'Exceedance_Post':
            write_plot_data(fh, build_exceedance_array(ref1,ref2), dataIndex, "Exceedance %s"%extract_name_from_ref(ref1), series_name, "%s(%s)"%(data_type,data_units), "Percent at or above", PlotType.EXCEEDANCE)
    js_data.write_end_data_array(fh);
    fh.close()
#
def write_plot_data(fh, data, dataIndex,  title, series_name, yaxis, xaxis, plot_type):
    js_data.write_file(fh, data, dataIndex, title, series_name, yaxis, xaxis, plot_type);
#
if __name__ == '__main__':
    if len(sys.argv) != 2:
        print 'Usage: dss_compare template.inp'
        exit(1)
    template_file = sys.argv[1]
    #parse template file
    scalars, pathname_mapping = parse_template_file(template_file)
    # do processing
    do_processing(scalars, pathname_mapping)
