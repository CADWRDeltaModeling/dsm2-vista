import arcpy, os, time
from arcpy.mapping import *
from arcpy.analysis import *
from arcpy.management import *
from arcpy.conversion import *
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
SMayr_lyr = StationListsGDB + "Stations_SMayr_Proj"
CDEC_lyr = StationListsGDB + "CDEC_Delta_Proj"
USBR_lyr = StationListsGDB + "USBR_Others_Proj"
SW_lyr = StationListsGDB + "SurfaceWater_fr_GSmith_Proj"
WQD1641_lyr = StationListsGDB + "waterquality_Stations_D1641_Proj"
NCRO_Oct2011_lyr = StationListsGDB + "NCRO_FlowStation_Oct2011_Proj"
NCRO_Flow_lyr = StationListsNCRO + "FlowStations"
NCRO_SW_lyr = StationListsNCRO + "SurfaceWater"
NCRO_WQ_lyr = StationListsNCRO + "WaterQuality_Proj"
# for each station list, which field is the primary station name field
NameFields = {GPS_lyr: "station", SMayr_lyr: 'STA_NO', CDEC_lyr: 'CDEC_ID', USBR_lyr: 'StationDescription', \
              SW_lyr: 'Site_ID', WQD1641_lyr: 'StationID', NCRO_Oct2011_lyr: "Internal_c", \
              NCRO_Flow_lyr: "Name", NCRO_SW_lyr: "Station_No", NCRO_WQ_lyr: "Station_Na"}
# 8-char names for each station list (to import into MS Access)
ShortNames = {GPS_lyr: "GPSMEAS", SMayr_lyr: 'SMAYR', CDEC_lyr: 'CDEC', USBR_lyr: 'USBR', \
              SW_lyr: 'SWGSMITH', WQD1641_lyr: 'WQD1641', NCRO_Oct2011_lyr: "NCRO2011", \
              NCRO_Flow_lyr: "NCROFLOW", NCRO_SW_lyr: "NCROSW", NCRO_WQ_lyr: "NCROWQ"}
# A generic primary station name field
genericStaID = 'BaseStaID'
# which station lists to check
StationLyrs = [NCRO_Oct2011_lyr, NCRO_Flow_lyr, NCRO_SW_lyr, NCRO_WQ_lyr, \
               SMayr_lyr, CDEC_lyr, USBR_lyr, SW_lyr, WQD1641_lyr, GPS_lyr]
# where to put output tables
env.workspace = StationListsGDB
# find other stations with searchRadius of each Base list location
searchRadius = '100 Meters'
location = 'NO_LOCATION'
angle = 'NO_ANGLE'
closest = 'ALL'
closestCount = 5
tempTable = GPSMeasGDB + "temp"
outFile = open(workspaceDir2+'StaMatches.txt','w')
for baseLyr in StationLyrs:
    temp = os.path.basename(baseLyr).replace('_lyr','')
    temp = temp.replace('_Proj','')
    outTable = 'Nearest_' + temp
    # get OID field of base table
    BaseOIDFld_lst = ListFields(baseLyr, '', 'OID')
    BaseOIDFld_nm = BaseOIDFld_lst[0].name
    print "Base List:", os.path.basename(baseLyr) #, "OID:", BaseOIDFld_nm
    # total number of stations in base layer, and nearest stations found
    nStasBase = int(GetCount(baseLyr).getOutput(0)) 
    nStasFound = 0
    # create nearest table...
    lyrCount = 0
    for lyr in StationLyrs:
        shortLyr = os.path.basename(lyr)
        if lyr == baseLyr:
            continue
        #print 'List: ',shortLyr
        try: Delete(tempTable)
        except: pass
        desc = Describe(lyr)
        if not desc.hasOID:
            print 'No ObjectID field, skipping layer', shortLyr
            continue     
        # put the nearest table list into the temporary table...
        # we will add fields to it for the permanent table.
        GenerateNearTable(baseLyr, lyr, tempTable, searchRadius,location, angle, closest, closestCount)
        # now join the Base table to the nearest table...
        # get correct field delimiters
        delmField = arcpy.AddFieldDelimiters(tempTable, 'NEAR_FC')
        # ...join to get location names
        JoinField(tempTable, 'in_fid', baseLyr, BaseOIDFld_nm, NameFields[baseLyr])
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
    # Done checking the 'base' list against all station lists 
    # Add a station id field common to all station lists
    AddField(GPSMeasGDB + outTable, genericStaID, 'TEXT', '','', '50', '', 'NULLABLE', 'NON_REQUIRED', '')
    # Make a python list of rows to fill in as placeholders 
    #  for stations in the base layer that had no near matches
    baseStaNumPrev = 0
    missingStas = []
    rows = UpdateCursor(GPSMeasGDB + outTable, '', '', '', 'IN_FID A')
    for row in rows:
        row.setValue(genericStaID,row.getValue(NameFields[baseLyr]))
        rows.updateRow(row)
        baseStaNum = long(row.IN_FID)
        if row.NEAR_DIST < 0.5:
            staEquiv = 'Exact'
        else:
            staEquiv = 'Equiv'
        outFile.write(staEquiv+','+os.path.basename(baseLyr)+','+row.BaseStaID+ \
            ','+row.StationList+','+row.StaListName+'\n')
#        print staEquiv, 'stations:',os.path.basename(baseLyr),row.BaseStaID, \
#            row.StationList,row.StaListName
        if baseStaNum == baseStaNumPrev:
            continue
        while baseStaNum-baseStaNumPrev > 1:
            missingStas += [baseStaNumPrev+1]
            baseStaNumPrev += 1
        baseStaNumPrev = baseStaNum
    # delete the base list station id field
    DeleteField(GPSMeasGDB + outTable, NameFields[baseLyr])
    del row, rows
    # Fill in blank placeholder rows for stations in the base layer that had no near matches
    rows = InsertCursor(GPSMeasGDB + outTable)
    nStasFound = nStasBase - len(missingStas)
    for sta in missingStas:
        row = rows.newRow()
        rowsBase = SearchCursor(baseLyr, BaseOIDFld_nm+" = "+str(sta), "", NameFields[baseLyr], "")
        for rowBase in rowsBase:    # should be only 1 row
            baseStaID = rowBase.getValue(NameFields[baseLyr])
            row.setValue(genericStaID,baseStaID)
        row.IN_FID = sta
        row.NEAR_FID = 0L
        row.NEAR_DIST = 0.0
        row.StationList = 'None'
        row.StaListName = 'None'
        rows.insertRow(row)
        #print "No near neighbor in", os.path.basename(baseLyr), "for station", baseStaID
    try:
        Delete(workspaceDir2 + ShortNames[baseLyr]+'.dbf')
        Delete(workspaceDir2 + ShortNames[baseLyr]+'.dbf.xml')
    except: pass
    TableToTable(GPSMeasGDB + outTable, workspaceDir2, ShortNames[baseLyr]+'.dbf')
    print 'Total stations', nStasBase, 'Stations Nearest', nStasFound
    print
outFile.close()
print "Finished"
