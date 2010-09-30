from java.lang import Float
def write_begin_data_array(file_handle):
    print >> file_handle, "data_list=["
def write_end_data_array(file_handle):
    print >> file_handle, "]"
def write_file(file_handle, name, data_type, checked, diff_arr,latlng):
    print >> file_handle, """ {
    "name": "%s",
    "data_type": "%s",
    "checked": "%s",
    "latitude": "%s",
    "longitude": "%s",
    "diff": [""" %(name, data_type, checked,latlng[2],latlng[3])
    for item in diff_arr:
        print >> file_handle, """ {"rmse": "%0.3f", "perc_rmse": "%0.2f" }, 
        """ %(item[0],item[1])       
    print >> file_handle, "] }"
def format_for_nan(value):
    if Float.isNaN(value):
        return "NaN"
    else:
        return "%f"%value