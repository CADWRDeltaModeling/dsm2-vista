# Import arcpy module
import arcpy, os, time
from arcpy.mapping import *
from arcpy.analysis import *
from arcpy.management import *
from arcpy import *
## For GPS-measured stations, find nearby stations in other lists

#workspaceDir = "D:/ArcGIS"
#outDir = "D:/Temp"
workspaceDir = "D:/delta/GIS/Observed/Monitoring Stations/Jane 20110802/"
commonDir = "C:/Users/rfinch/Documents/ArcGIS/Packages/"
outDir = "Z:/Temp"
scratchWorkspace = "z:/Temp"
overwriteOutput = True

# the GPS-measured stations DB and layer
GPSMeasGDB = workspaceDir + "DeltaStationsGPS.gdb/"
GPSlyr = "GarminWaypoints"
# the provided lists of station locations to check
StationListsGDB = workspaceDir + "DeltaStationLists.gdb/"
env.workspace = StationListsGDB
SMayrlyr = "Stations_SMayr"
CDEClyr = "CDEC_Delta"
USBRlyr = "USBR_Others"
SWlyr = "SurfaceWater_fr_GSmith"
WQD1641lyr = "waterquality_Stations_D1641"
StationLyrs = [SMayrlyr, CDEClyr, USBRlyr, SWlyr, WQD1641lyr]
NameFields = {SMayrlyr: 'STA_NO', CDEClyr: 'CDEC_ID', USBRlyr: 'StationDescription', SWlyr: 'Site_ID', WQD1641lyr: 'StationID'}

# find other stations with searchRadius of each GPS location
tempTable = GPSMeasGDB + "temp"
outTable = "NearestTable"
searchRadius = "150 meters"
# create nearest table...
try: arcpy.management.Delete(tempTable)
except: pass
lyrCount = 0
for lyr in StationLyrs:
    print '===>',lyr
    try: Delete(tempTable)
    except: pass
    desc = Describe(lyr)
    if not desc.hasOID:
        print 'No ObjectID field, skipping layer', lyr
        continue     
    # put the nearest table list into the temporary table...
    # we will add fields to it for the permanent table.
    GenerateNearTable(GPSMeasGDB + GPSlyr, lyr, tempTable, searchRadius, None, None, 'ALL', 10)
    # now join the GPS-measured table to the nearest table...
    # get correct field delimiters
    delmField = arcpy.AddFieldDelimiters(tempTable, 'NEAR_FC')
    # ...join to get location names
    JoinField(tempTable, 'in_fid', GPSMeasGDB + GPSlyr, 'ObjectID', 'station')
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
        row.StationList = lyr[0:49]
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
#    junk = fieldMappings.exportToString().split(';')
#    for j in junk:
#        print j.replace('D:/delta/GIS/Observed/Monitoring Stations/Jane 20110802/','')
    Append(tempTable, GPSMeasGDB + outTable, 'NO_TEST', fieldMappings, '')
    lyrCount += 1
print "Finished"
