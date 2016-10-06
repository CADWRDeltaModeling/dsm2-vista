from hec.script import *
from hec.heclib.dss import *
from hec.hecmath import TimeSeriesMath, HecMath
from hec.gfx2d import G2dDialog, G2dLine, Symbol
from hec.io import PairedDataContainer
from hec.gfx2d import G2dPanelProp
from java.util import Vector

from org.w3c.dom import Document
import java
import math
from java.awt import Color
from javax.swing import *
import string

if __name__ == '__main__':
    import sys
    dssfile = sys.argv[1] #    dssfile=r"D:\testin.DSS"
    outdssfile = sys.argv[2] #    outdssfile=r"D:\testout.dss"
    dss=HecDss.open(dssfile)
    outdss = HecDss.open(outdssfile, False)
    matches=dss.getCatalogedPathnames("C=FLOW")
    for m in matches:
        print "Match: %s"%m
        data = dss.get(m, "01JAN1972 2400", "31DEC1991 2400")
        path=DSSPathname(data.fullName)
        path.setFPart(path.fPart()+"-COPY")
        data.fullName=path.getPathname()
        data.fileName=outdssfile
        outdss.put(data)
    dss.done()
    outdss.done()
