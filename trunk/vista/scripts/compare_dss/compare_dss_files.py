import sys
import vutils
import vtimeseries
from vdisplay import plot
from vista.set import Pathname
from vdss import writedss, set_part
import os
def list_pathnames(dssgroup):
    for ref in dssgroup:
        p = ref.pathname
def matches(path1, path2):
    if path1.getPart(Pathname.B_PART) == path2.getPart(Pathname.B_PART) and path1.getPart(Pathname.C_PART) == path2.getPart(Pathname.C_PART) and path1.getPart(Pathname.E_PART) == path2.getPart(Pathname.E_PART):
        return True
    else: 
        return False
def compare_dss_files(file1, file2, showPlot=False, outputFile=None):
    """
    Simply compares the files and outputs differences if any of those that differ and lists mismatching pathnames in either
    """
    g1 = vutils.opendss(file1)
    g2 = vutils.opendss(file2)
    print 'Comparing %s to %s'%(file1, file2)
    print '%12s\t%32s'%('DIFFERENCE','PATHNAME')
    for ref1 in g1:
        p1 = ref1.pathname
        found = False
        for ref2 in g2:
            p2 = ref2.pathname
            if matches(p1, p2):
                found = True
                diff = ref2.data - ref1.data
                diff_total = vtimeseries.total(diff)
                if (diff_total > 1e-06) :
                    if showPlot: plot(ref1.data, ref2.data)
                    print '%10.2f\t%32s' % (diff_total, p1)
                    if outputFile:
                        diffp = set_part(p1,'DIFF-%s-%s'%(os.path.basename(file1),os.path.basename(file2)), Pathname.A_PART)
                        writedss(outputFile, str(diffp), diff)
                break
        if (not found):
            print 'No matching path: %s in file %s found in file %s' % (p1, file1, file2)
    for ref2 in g2:
        p2 = ref2.pathname
        for ref1 in g1:
            found = False
            p1 = ref1.pathname
            if matches(p1,p2):
                found = True
                break
        if (not found):
            print 'No matching path: %s in file %s found in file %s' % (p2, file2, file1)
#
def usage():
    print 'Usage: compare_dss_files [-s|--show] [-o|--output=<outputdssfile>] file1.dss file2.dss'
if __name__=='__main__':
    import getopt
    try:
        opts, args = getopt.getopt(sys.argv[1:], "hso:", ["help", "show","output="])
    except err:
        # print help information and exit:
        print str(err) # will print something like "option -a not recognized"
        usage()
        sys.exit(2)    
    if len(args) != 2:
        usage()
        sys.exit(3)
    showPlot=False
    outputFile=None
    for o, a in opts:
        if o in ("-h","help"):
            usage()
            exit(0)
        elif o in ("-s","--show"):
            showPlot=True
        elif o in ("-o","--output"):
            outputFile=a
            print 'Writing paths that differ to %s'%outputFile
        else:
            assert False, "unhandled option"
    file1 = args[0]
    file2 = args[1]
    compare_dss_files(file1, file2, showPlot,outputFile)
    if not showPlot:
        sys.exit(0)
#    