import unittest
import vdss

class TestVDSS(unittest.TestCase):
    
    def setUp(self):
        self.basedir = '../scripts/testdata/'
        self.file1 = self.basedir+'file1.dss'
    def tearDown(self):
        pass
    def test_opendss(self):
        g1=vdss.opendss(self.file1)
        self.assertTrue(g1 != None)
    def test_get_ds(self):
        g1=vdss.opendss(self.file1)
        self.assertTrue(g1 != None)
        ref0=g1[0]
        self.assertTrue(ref0 != None)
        path0=ref0.pathname
        self.assertEquals('/VISTA-EX1/COS/FLOW/01JAN1982/5MIN/COS-WAVE/', str(path0))
        tw0=ref0.timeWindow
        self.assertTrue(tw0 != None)
        print tw0
        self.assertTrue('01JAN1982 0500' == tw0.startTime.toString())
        self.assertTrue('01JAN1982 2400' == str(tw0.endTime))
        self.assertTrue(ref0.filename != None)
        ds0=ref0.data
        self.assertTrue(ds0 != None)
