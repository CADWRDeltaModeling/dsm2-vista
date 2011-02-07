""" 
   This is the test for DSS Comparison Tool.
   Before the run, make sure you have dsm2_output.inp under all those folders. 
    'D:/delta/dsm2_v8/report/case1', 'D:/delta/dsm2_v8/report/case2', 'D:/delta/dsm2_v8/report/case3'
    'D:/delta/dsm2_v8/report/case4', 'D:/delta/dsm2_v8/report/case5'
   Once it is successully completed, go to each folder and open *.html to perform QA/QC.
"""
import sys, os, shutil
import dsm2_dss_compare
import logging
from time import strftime

def test_single_case():
    logging.basicConfig(level=logging.DEBUG)
    template_file = 'D:/delta/dsm2_v8/report/compare_haoPrepros/dsm2_output.inp'
    try:
        outdir, outfile =dsm2_dss_compare.get_output_html(template_file)
        dsm2_dss_compare.copy_basic_files(outdir)
    except:
        logging.critical("Error in copying js files and creating data folder to designated folder")
    logging.debug('Parsing input template file %s'%template_file)
    logging.debug("Starting at: "+strftime("%a, %d %b %Y %H:%M:%S"))
    globals, scalars, var_values, output_values, tw_values = dsm2_dss_compare.parse_template_file(template_file)
    dsm2_dss_compare.do_processing(globals, scalars, var_values, output_values, tw_values)
    logging.debug("End at: "+strftime("%a, %d %b %Y %H:%M:%S"))
    logging.debug("Complete successfully")
    logging.debug("--------------------------------------------")
    logging.debug("Finished all the test cases.")
    sys.exit(0)

def test_all_cases():
    logging.basicConfig(level=logging.DEBUG)
    test_cases = ['case1','case2','case3','case4','case5']
    for i in test_cases:
        logging.debug('Start to work on %s' %i.upper())
        template_file = 'D:/delta/dsm2_v8/report/'+i+'/dsm2_output.inp'
        try:
            outdir, outfile =dsm2_dss_compare.get_output_html(template_file)
            dsm2_dss_compare.copy_basic_files(outdir)
        except:
            logging.critical("Error in copying js files and creating data folder to designated folder")
        logging.debug('Parsing input template file %s'%template_file)
        logging.debug("Starting at: "+strftime("%a, %d %b %Y %H:%M:%S"))
        globals, scalars, var_values, output_values, tw_values = dsm2_dss_compare.parse_template_file(template_file)
        dsm2_dss_compare.do_processing(globals, scalars, var_values, output_values, tw_values)
        logging.debug("End at: "+strftime("%a, %d %b %Y %H:%M:%S"))
        logging.debug("Complete successfully with %s" %i.upper())
        logging.debug("--------------------------------------------")
    logging.debug("Finished all the test cases.")
    sys.exit(0)

if __name__ == '__main__':
    test_all_cases()
    #test_single_case()
    