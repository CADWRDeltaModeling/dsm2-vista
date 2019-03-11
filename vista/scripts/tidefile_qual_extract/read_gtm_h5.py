from vtidefile import opentidefile
from vdss import writedss
from vutils import *
import vdisplay
from vdisplay import plot
import sys
import string

def get_ts(tidefile, data_name, data_type, twstr):
    tf=opentidefile(tidefile)
    if twstr != None:
        print 'Timewindow: %s'%twstr
        tw=timewindow(twstr)
    else:
        tw=None
    refs=tf.find(['','^%s$'%data_name,data_type])
    if refs and len(refs)==1:
        print "Getting data %s"%(str(chan))
        if tw!=None:
            ref=DataReference.create(refs[0],tw)
        else:
            ref=refs[0]
        return ref.data
    else:
        raise "No data found for %s in file %s"%(chan, tidefile)
if __name__ == '__main__':
    tidefile="test/sample_gtm.h5"
    twstr="30SEP2010 2400 - 31OCT2010 2000"
    chans=[291,290,436,435,434,433]
    chan_concs=[]
    for chan in chans:
        chan_concs.append(get_ts(tidefile, str(chan)+"_upstream", "ssc", twstr))
    for conc in chan_concs:
        plot(conc)
#
