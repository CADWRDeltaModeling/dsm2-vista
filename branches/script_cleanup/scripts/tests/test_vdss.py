import unittest
import vdss
import os, os.path

class Test_vdss(unittest.TestCase):
    def setUp(self):
        self.input_dss_file = '../testdata/file1.dss'
        self.output_dss_file='../testdata/file1_temp.dss'
        if (os.path.exists(self.output_dss_file)):
            self.tearDown()
    def tearDown(self):
        if (os.path.exists(self.output_dss_file)):
            os.remove(self.output_dss_file)
    def test_opendss(self):
        dss_group = vdss.opendss(self.input_dss_file)
        self.assert_(dss_group)
        self.assertEqual(4, len(dss_group))
    def test_writedss(self):
        dss_group = vdss.opendss(self.input_dss_file)
        self.assert_(dss_group)
        self.assertEquals(4, len(dss_group))
        for ref in dss_group:
            vdss.writedss(self.output_dss_file, str(ref.pathname), ref.data)
        dss_group2 = vdss.opendss(self.output_dss_file)
        self.assertEquals(4, len(dss_group2))
    def test_gen_ref(self):
        dss_group = vdss.opendss(self.input_dss_file)
        ref1 = dss_group[0]
        ref_generated = vdss.gen_ref(ref1.data)
        self.assertEquals('local', ref_generated.servername)
        self.assertEquals('data.dss', ref_generated.filename)
        from vista.set import Pathname
        for i in range(Pathname.MAX_PARTS):
            if i == Pathname.D_PART: continue
            self.assertEquals(ref1.pathname.getPart(i),ref_generated.pathname.getPart(i))
        self.assertEquals(ref_generated.timeWindow, ref1.data.timeWindow)
#end of class
if __name__ == '__main__':
    unittest.main()
