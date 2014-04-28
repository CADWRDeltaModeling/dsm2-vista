"""
Calculates the xsection properties from the xsection profile (x,z values along a cross section line) 

The file is the format exported by ArcGIS when drawing a line profile and choosing text format for export
The first line is a header followed by lines containing x and z in the units of the raster dem
Please ensure that the units of x and z are in meters before using this script or change the conversion part of this script

The layers are calculated at elevations of minimum elevation, -10, -5, 0, 5 and 10 by this script. 

It assumes the name of the file to be channel id and last two digits before .txt to be the normalized distance times 10 from the upstream end
For example 44101.txt implies 441 is the name of the channel and 01/10 = 0.1 is the normalized distance (distance/channel length) from upstream end.
"""
from gov.ca.dsm2.input.model import XSectionProfile
from java.util import ArrayList
import sys, glob, os
import jarray
def load_profile(filename):
    fh=open(filename,'r')
    lines=fh.readlines()
    profile_points = ArrayList()
    for line in lines[1:]:
        fields=line.split("\t")
        if len(fields) != 2: continue
        x,z=map(lambda x: x.replace(",",""),fields)
        profile_points.add(jarray.array([float(x)/0.3048,float(z)/0.3048], 'd'))
    fh.close()
    return profile_points
def print_layer(outf,chan,dist,layer):
    print >>outf, "%d\t%.1f\t%.2f\t%.2f\t%.2f\t%.2f"%(chan,dist,layer.elevation, layer.area, layer.topWidth, layer.wettedPerimeter)
if __name__=='__main__':
    if len(sys.argv) < 2:
        print "Usage: xsection_calculator.py directory_containing_txt_files_of_profiles"
    dirname=sys.argv[1]
    filenames = glob.glob(dirname+"/*.txt")
    print "Writing out to dsm2_xsections.inp in current directory"
    outf = open("dsm2_xsections.inp","w")
    outf.write("""XSECT_LAYER
CHAN_NO  DIST    ELEV      AREA   WIDTH     WET_PERIM
    """)
    for filename in filenames:
        xs = XSectionProfile()
        xs.setProfilePoints(load_profile(filename))
        dist=float(filename.split(".txt")[0][-2:])/10.
        chan=int(os.path.basename(filename).split(".txt")[0][:-2])
        minElevation=xs.getMinimumElevation()
        print "!!!!ASSUMING UNITS OF METERS FOR CROSS-SECTION PROFILE!!!!"
        print "Minimum Elevation: %.2f"%minElevation
        bottom_layer = xs.calculateLayer(minElevation)
        print_layer(outf,chan,dist,bottom_layer)
        print_layer(outf,chan,dist,xs.calculateLayer(-10))
        print_layer(outf,chan,dist,xs.calculateLayer(-5))
        print_layer(outf,chan,dist,xs.calculateLayer(0))
        print_layer(outf,chan,dist,xs.calculateLayer(5))
        print_layer(outf,chan,dist,xs.calculateLayer(10))
    outf.write("END")
    outf.close()