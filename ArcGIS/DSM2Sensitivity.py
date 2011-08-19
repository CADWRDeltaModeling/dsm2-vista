# Import arcpy module
import arcpy, os, time
from arcpy.mapping import *

#workspace = "D:/ArcGIS"
#mapDir = "D:/Temp"
workspace = "D:/delta/GIS/DSM2"
mapDir = "Z:/Temp"
scratchWorkspace = "D:/Temp"
overwriteOutput = True

# Local variables:
outputParams = ['ec', 'flow']
outputParams = ['ec']
perturbedParams = ['MANN', 'DISP', 'XELEV']
DSM2OutLocsLyr = "DSM2OutLocsAll"
DSM2NodesLyr = "DSM2 Nodes"
DSM2ChansLyr = "DSM2 Channels"
mxd = arcpy.mapping.MapDocument("D:/delta/GIS/DSM2/DSM2_Network.mxd")

dataframe = ListDataFrames(mxd)[0]
texts = ListLayoutElements(mxd,"TEXT_ELEMENT")
lyrDSM2Chans = ListLayers(mxd,DSM2ChansLyr)[0]
# set map extent to DSM2 Nodes
lyrDSM2Nodes = ListLayers(mxd,DSM2NodesLyr)[0]
dataframe.extent = lyrDSM2Nodes.getExtent()
legend = ListLayoutElements(mxd, "LEGEND_ELEMENT", "Legend")[0]
# get the DSM2 Output Station layer
lyrDSM2OutLocs = ListLayers(mxd, DSM2OutLocsLyr)[0]
#
prevReg = ""
# find several text elements
for txt in texts:
    if txt.name == 'Output':
        txtOutParam = txt
    if txt.name == 'Perturbed':
        txtPerturb = txt
    if txt.name == 'Location':
        txtLoc = txt

# loop over perturbed parameters
for perturbed in perturbedParams:
    txtPerturb.text = 'Perturbed: ' + perturbed
    # loop over output parameters
    for outParam in outputParams:
        txtOutParam.text = 'Output: ' + outParam
        # loop over DSM2 output locations
        queryExpr = "\"OUTPARAM\" = " + "'" + outParam + "'"
        rows = arcpy.SearchCursor("DSM2.gdb/" + DSM2OutLocsLyr, queryExpr, "", "", \
                                  "Outparam A; Area A; Lng A")
        for row in rows:
            Loc = row.NAME
            Region = row.AREA
            if prevReg != Region:
                prevReg = Region
                locCtr = 0
            txtLoc.text = 'Location: ' + Loc
            queryExpr = "\"OUTPARAM\" = " + "'" + outParam + "'" + "AND \"NAME\" = " + "'" + Loc + "'"
            # query to display target output location
            lyrDSM2OutLocs.definitionQuery = queryExpr
            # query to get channel sensitivity results for target loc
            queryExpr = "Perturbed = " + "'" + perturbed + "'" + " AND " + \
                        "Parameter = " + "'" + outParam + "'" + " AND " + \
                        "Output_Location = " + "'" + Loc + "'"
            lyrDSM2Chans.definitionQuery = queryExpr
            mapName = mapDir + '/' + 'MAP_' + Region + \
                          '_' + '{number:0{digits}d}'.format(number=locCtr, digits=2) + \
                          '_' + Loc + '_' + outParam + '_' + perturbed
            mapNamePDF = mapName + '.pdf'
        ##  ExportToPDF(mxd, mapNamePDF, image_quality="FASTEST")
            mapNameJPEG = mapName + '.jpg'
            print mapNameJPEG
            ExportToJPEG(mxd, mapNameJPEG, resolution=72, jpeg_quality=50)
            locCtr += 1
#