# Import arcpy module
import arcpy, os, time
from arcpy.mapping import *
from arcpy.analysis import *
from arcpy.management import *
from arcpy import *
## For GPS-measured stations, find nearby stations in other lists

#workspaceDir2 = "D:/ArcGIS"
#outDir = "D:/Temp"
workspaceDir1 = "D:/delta/GIS/Observed/Monitoring Stations/"
workspaceDir2 = workspaceDir1 + "Jane 20110802/"
commonDir = "C:/Users/rfinch/Documents/ArcGIS/Packages/"
outDir = "Z:/Temp"
scratchWorkspace = "z:/Temp"
overwriteOutput = True

# the GPS-measured stations DB and layer
GPSMeasGDB = workspaceDir2 + "DeltaStationsGPS.gdb/"
GPS_lyr = GPSMeasGDB + "GarminWaypoints"
# the provided lists of station locations to check
StationListsNCRO = workspaceDir1 + "Stations_NCRO/BranchStations.mdb/"
StationListsGDB = workspaceDir2 + "DeltaStationLists.gdb/"
#
SMayr_lyr = StationListsGDB + "Stations_SMayr"
CDEC_lyr = StationListsGDB + "CDEC_Delta"
USBR_lyr = StationListsGDB + "USBR_Others"
SW_lyr = StationListsGDB + "SurfaceWater_fr_GSmith"
WQD1641_lyr = StationListsGDB + "waterquality_Stations_D1641"
NCRO_Oct2011_lyr = StationListsGDB + "NCRO_FlowStation_Oct2011"
NCRO_Flow_lyr = StationListsNCRO + "FlowStations"
NCRO_SW_lyr = StationListsNCRO + "SurfaceWater"
NCRO_WQ_lyr = StationListsNCRO + "WaterQuality"
# for each station list, which field is the primary station name field
NameFields = {GPS_lyr: "station", SMayr_lyr: 'STA_NO', CDEC_lyr: 'CDEC_ID', USBR_lyr: 'StationDescription', \
              SW_lyr: 'Site_ID', WQD1641_lyr: 'StationID', NCRO_Oct2011_lyr: "Internal_c", \
              NCRO_Flow_lyr: "Name", NCRO_SW_lyr: "Station_No", NCRO_WQ_lyr: "Station_Na"}
# Define the base station to check others against
#baseLyr = GPS_lyr
baseLyr = SMayr_lyr
# which station lists to check
StationLyrs = [NCRO_Oct2011_lyr, NCRO_Flow_lyr, NCRO_SW_lyr, NCRO_WQ_lyr, \
               SMayr_lyr, CDEC_lyr, USBR_lyr, SW_lyr, WQD1641_lyr]
# where to put output tables
env.workspace = StationListsGDB
# find other stations with searchRadius of each Base list location
tempTable = GPSMeasGDB + "temp"
outTable = "NearestTable"
searchRadius = "50 meters"
# create nearest table...
lyrCount = 0
for lyr in StationLyrs:
    shortLyr = os.path.basename(lyr)
    if lyr == baseLyr:
        continue
    print 'List: ',shortLyr
    try: Delete(tempTable)
    except: pass
    desc = Describe(lyr)
    if not desc.hasOID:
        print 'No ObjectID field, skipping layer', shortLyr
        continue     
    # put the nearest table list into the temporary table...
    # we will add fields to it for the permanent table.
    GenerateNearTable(baseLyr, lyr, tempTable, searchRadius, None, None, 'ALL', 15)
    # now join the Base table to the nearest table...
    # get correct field delimiters
    delmField = arcpy.AddFieldDelimiters(tempTable, 'NEAR_FC')
    # ...join to get location names
    JoinField(tempTable, 'in_fid', baseLyr, 'OBJECTID', NameFields[baseLyr])
    if lyrCount == 0:
        # Create permanent table
        try: Delete(GPSMeasGDB + outTable)
        except: pass
        CreateTable(GPSMeasGDB, outTable, tempTable, '')
        AddField(GPSMeasGDB + outTable, 'StationList', 'text', '', '', 50, '', '', '', '')
        AddField(GPSMeasGDB + outTable, 'StaListName', 'text', '', '', 254, '', '', '', '')
    # ...join the station name from the target station list
    JoinField(tempTable, 'near_fid', lyr, desc.OIDFieldName, NameFields[lyr])
    AddField(tempTable, 'StationList', 'text', '', '', 50, '', '', '', '')
    rows = UpdateCursor(tempTable, '', '', '', '')
    for row in rows:
        row.StationList = shortLyr[0:49]
        rows.updateRow(row)
    # Create FieldMappings object for append output fields
    fieldMappings = FieldMappings()
    # Add all fields from tempTable
    fieldMappings.addTable(tempTable)
    fldMap_staListName = fieldMappings.getFieldMap(fieldMappings.findFieldMapIndex(NameFields[lyr]))
    # Set name of permanent output field StaListName
    fld_staListName = fldMap_staListName.outputField
    fld_staListName.name = "StaListName"
    fldMap_staListName.outputField = fld_staListName
    fieldMappings.addFieldMap(fldMap_staListName)
    fieldMappings.removeFieldMap(fieldMappings.findFieldMapIndex(NameFields[lyr]))
    Append(tempTable, GPSMeasGDB + outTable, 'NO_TEST', fieldMappings, '')
    lyrCount += 1
try: arcpy.management.Delete(tempTable)
except: pass
# Check the output table for stations in the base layer that had no near matches
staNumListPrev = 0
missingStas = []
rows = SearchCursor(GPSMeasGDB + outTable, '', '', '', 'IN_FID A')
for row in rows:
    staNumList = long(row.IN_FID)
    if staNumList == staNumListPrev:
        continue
    while staNumList-staNumListPrev > 1:
        print "No near neighbor for IN_FID", staNumListPrev+1
        missingStas += [staNumListPrev+1]
        staNumListPrev += 1
    staNumListPrev = staNumList
rows = InsertCursor(GPSMeasGDB + outTable)
for sta in missingStas:
    row = rows.newRow()
    rowsBase = SearchCursor(baseLyr, "OBJECTID = " + str(sta), "", "STA_NO", "")
    for rowBase in rowsBase:
        row.STA_NO = rowBase.STA_NO
    row.IN_FID = sta
    row.NEAR_FID = 0L
    row.NEAR_DIST = 0.0
    row.StationList = 'None'
    row.StaListName = 'None'
    rows.insertRow(row)
del row, rows
print "Finished"
