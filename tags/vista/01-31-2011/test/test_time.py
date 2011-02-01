from vutils import *
TF=TimeFactory.getInstance()
dssfile = opendss(r'D:\temp\vista.data\180_currentDir.dss')
dssfile.filterBy('15MIN')
ts = dssfile.allDataReferences[0].data
dsi = ts.getIterator()
e=dsi.getElement()
x=e.getX()
print x
t=TF.createTime(long(x))
print t
ti_end = ts.getStartTime()+TF.createTimeInterval('1DAY')
print ti_end
print t<ti_end
dsi.advance()
e=dsi.getElement()
x=e.getX()
t=TF.createTime(long(x))
print t<ti_end
