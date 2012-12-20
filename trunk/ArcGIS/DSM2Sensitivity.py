# Import arcpy module
import arcpy, os, time
from arcpy.mapping import *

#workspace = "D:/ArcGIS"
#mapDir = "D:/Temp"
workspace = "D:/delta/GIS/DSM2"
mapDir = "Z:/Temp"
#scratchWorkspace = "C:/Temp"
overwriteOutput = True

# Local variables:
outputParams = ['ec', 'flow']
#outputParams = ['ec']
perturbedParamsChans = ['MANN', 'DISP', 'XELEV', 'XTOPW']
perturbedParamsNodes = ['DICU-QDIV', 'DICU-QRET', 'DICU-ECRET']
DSM2OutLocsLyr = "DSM2OutLocsAll"
DSM2NodesLyr = "DSM2 Nodes"
DSM2ChansLyrQ = "DSM2 Channels"
DSM2ChansLyrB = "DSM2 Channels-background"
mxd = arcpy.mapping.MapDocument(workspace+"/DSM2_Network.mxd")

dataframe = ListDataFrames(mxd)[0]
texts = ListLayoutElements(mxd,"TEXT_ELEMENT")
lyrDSM2ChansQ = ListLayers(mxd,DSM2ChansLyrQ)[0]
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

# loop over output parameters
for outParam in outputParams:
    txtOutParam.text = 'Output: ' + outParam.upper()
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
        txtLoc.text = 'Location: ' + Loc.upper()
        queryExpr = "\"OUTPARAM\" = " + "'" + outParam + "'" + "AND \"NAME\" = " + "'" + Loc + "'"
        # query to display target output location
        lyrDSM2OutLocs.definitionQuery = queryExpr
        lyrDSM2ChansQ.visible = True
        lyrDSM2Nodes.visible = False
        # loop over perturbed parameters
        for perturbed in perturbedParamsChans:
            txtPerturb.text = '+20% Perturbed: ' + perturbed
            # query to get channel sensitivity results for target loc
            queryExpr = "Perturbed = " + "'" + perturbed + "'" + " AND " + \
                        "Parameter = " + "'" + outParam + "'" + " AND " + \
                        "Output_Location = " + "'" + Loc + "'"
            lyrDSM2ChansQ.definitionQuery = queryExpr
            mapName = mapDir + '/' + 'MAP_' + Region + \
                          '_' + '{number:0{digits}d}'.format(number=locCtr, digits=2) + \
                          '_' + Loc + '_' + outParam + '_' + perturbed
            mapNamePDF = mapName + '.pdf'
        ##  ExportToPDF(mxd, mapNamePDF, image_quality="FASTEST")
            mapNameJPEG = mapName + '.jpg'
            print mapNameJPEG
            ExportToJPEG(mxd, mapNameJPEG, resolution=72, jpeg_quality=50)
            locCtr += 1

        lyrDSM2ChansQ.visible = False
        lyrDSM2Nodes.visible = True
        for perturbed in perturbedParamsNodes:
            txtPerturb.text = '+20% Perturbed: ' + perturbed.upper()
            # query to get channel sensitivity results for target loc
            queryExpr = "Perturbed = " + "'" + perturbed + "'" + " AND " + \
                        "Parameter = " + "'" + outParam + "'" + " AND " + \
                        "Output_Location = " + "'" + Loc + "'"
            lyrDSM2Nodes.definitionQuery = queryExpr
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