from vutils import *
x=range(5)
y=[33,32,45,39,53]
its=IrregularTimeSeries('its1',x,y)
iterator = its.getIterator()
iterator.positionAtTime(time('30DEC1899 0002'))