from java.lang import Float
def write_begin_data_array(file_handle):
    print >> file_handle, "data_list=["
def write_end_data_array(file_handle):
    print >> file_handle, "]"
def write_file(file_handle, name, data_type, checked, diff_arr):
    print >> file_handle, """ {
    "name": "%s",
    "data_type": "%s",
    "checked": "%s",
    "diff": [""" %(name, data_type, checked)
    for item in diff_arr:
        print >> file_handle, """ { "avg1": "%0.3f", "avg2": "%0.3f", "diff": "%0.3f", "perc": "%0.2f" }, 
        """ %(item[0],item[1],item[2],item[3])       
    print >> file_handle, "] }"
def format_for_nan(value):
    if Float.isNaN(value):
        return "NaN"
    else:
        return "%f"%value