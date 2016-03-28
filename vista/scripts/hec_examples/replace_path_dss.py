import jarray
from hec.script import *
from hec.heclib.dss import *
from hec.hecmath import TimeSeriesMath, HecMath
from hec.gfx2d import G2dDialog, G2dLine, Symbol
from hec.io import PairedDataContainer
from hec.gfx2d import G2dPanelProp
from java.util import Vector
def open_dss(dssfile):
    """
    opens a dss files only if it exists, else fails
    """
    dss=HecDss.open(dssfile,True)
    return dss
def close_dss(dss):
    """
    Takes a handle and closes it
    """
    dss.close()
def set_working_timewindow(dss, stime,etime):
    """
    Takes a dss handle (previously opened by call to open_dss and 
    a time window defined by strings in ddMMMyyyy HHmm format (start time and end time)  
    """
    dss.setTimeWindow(stime,etime)
def get_matching(dss,pattern):
    """
    Takes a dss handle and
    a pattern in format of "([part letter (A|B|C|D|E|F)]=<string to match> [space])*"
    e.g.
    get_matching(obs,'A=OBS C=MTZ E=15MIN')
    
    Fails by return None and printing message of failure.
    """
    matches=dss.getCatalogedPathnames(pattern)
    if (len(matches) >= 1):
        return dss.get(matches[0])
    else:
        print 'No match for: %s, %s'%(pattern,matches)
        return None
def plot(data, title):
    """
    Takes an array of data and a title and displays a plot (HECDssVue style)
    """
    plotd = newPlot(title)
    for d in data:
        plotd.addData(d)
    plotd.showPlot()
def newPlot(title):
    """
    Creates a blank plot window with title
    """
    plotProp = G2dPanelProp()
    plotProp.hasToolbar=False
    return G2dDialog(None,title,False,Vector(),plotProp)

######### MAIN ##########

if __name__=='__main__':
    dssfile="test.dss"
    outdssfile="testout.dss"
    dss=open_dss(dssfile)
    outdss = open_dss(outdssfile)
    set_working_timewindow(dss, "01JAN1972", "31DEC1991")
    matches=dss.getCatalogedPathnames("C=FLOW")
    for m in matches:
        data = dss.get(m)
        outdss.put(data)
    close_dss(dss)
    close_dss(outdss)
#