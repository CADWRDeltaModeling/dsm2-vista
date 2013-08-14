from vdss import *
from vdisplay import *
from vista.set import Pathname
from vtimeseries import *
#calculations on the dicu pathnames
def do_sum(cpart,dicufile):
    g=opendss(dicufile)
    g=findparts(g,c=cpart)
    ts=None
    for ref in g:
        if ts==None:
            ts=ref.data
        else:
            ts+=ref.data
    path=Pathname.createPathname(ts.name)
    path=set_part(path,'ALL',Pathname.B_PART)
    ts.name=str(path)
    return ts
#
def do_scale(cpart,scale,outfile, twstr=None):    
    g=opendss(dicufile)
    g=findparts(g,c=cpart)
    if twstr:
        tw=timewindow(twstr)
    for ref in g:
        if tw:
            ref=DataReference.create(ref,tw)
        ds=ref.data*scale
        writedss(outfile,ds.name,ds)
#
if __name__=='__main__':
    dicufile=r'Z:/DSM2_v81_Beta_Release/timeseries/dicu_201203.dss'
    outfile=r'Z:/DSM2_v81_Beta_Release/timeseries/dicu_201203_lower_20pct.dss'
    #outfile=r'D:\models\DSM2v8.1.x\Historical_MiniCalibration_811_MTZ_ts_corrected\timeseries\dicu_201004_minus20.dss'
    cparts=['DIV-FLOW','DRAIN-FLOW','SEEP-FLOW']
    DO_ADJUSTMENT=False
    if DO_ADJUSTMENT: 
        for cpart in cparts:
            if cpart=='DIV-FLOW':
                do_scale(cpart,1.2,outfile,"01JAN1999 0000 - 01JAN2003 0000")
            elif cpart=='DRAIN-FLOW':
                do_scale(cpart,0.8,outfile,"01JAN1999 0000 - 01JAN2003 0000")
            elif cpart=='SEEP-FLOW':
                do_scale(cpart,1.2,outfile,"01JAN1999 0000 - 01JAN2003 0000")
            print 'Done with C Part: %s'%cpart
    else:
        for cpart in cparts:
            ts=do_sum(cpart, dicufile)
            plot(ts)
