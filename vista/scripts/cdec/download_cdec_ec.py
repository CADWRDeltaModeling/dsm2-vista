import cdec
import vdisplay
from vutils import *

if __name__=='__main__':
    cdec.download_data_in_yearly_chunks('EMM', 542, 2008, 2014, 'd:/data/cdec/cdec_ec_raw.dss')
