import unittest
import vdiff
from vdiff import *

class TestVdiff(unittest.TestCase):
    ''' class TestVdiff(unittest.TestCase)
        This class is used to perform unittest for functions in vdiff.py file. 
    '''
    def setUp(self):
        dss_file1 = 'D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\End.dss'
        dss_file2 = 'D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\IntCLFT.dss'
        timewindow = '01SEP1974 0000 - 01SEP1991 0000'
        outdss = 'D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\IntCLFT-End.dss'
        self.ms = dss_ts_diff_metric(dss_file1,dss_file2,rmse,timewindow,'FLOW','y',outdss)
        
    def test_metrics(self):
        self.assertNotEqual(len(self.ms),0,' The returning metric is empty! ')
        
    def test_getxyz(self):
        hydro_echo = 'D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\hydro_echo.inp'
        gis_inp = 'D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\gis.inp'
        out_txt = 'D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\metric_xy.txt'
        chkarr = get_metric_xy(self.ms,hydro_echo,gis_inp,out_txt)
        self.assertNotEqual(len(chkarr),0,' The returning xy array is empty! ')
        
        
if __name__ == '__main__':
    unittest.main()