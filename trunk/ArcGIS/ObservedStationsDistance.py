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
# the lat/lon fields of each list
LatFields = {GPS_lyr: "Y", SMayr_lyr: 'lat_dd', CDEC_lyr: 'Lat', USBR_lyr: 'Latdec', \
              SW_lyr: 'Latitude', WQD1641_lyr: 'Latitude', NCRO_Oct2011_lyr: "Lat__DecDe", \
              NCRO_Flow_lyr: "Latitude", NCRO_SW_lyr: "Lat_DD", NCRO_WQ_lyr: "Latitude"}
LonFields = {GPS_lyr: "X", SMayr_lyr: 'long__dd', CDEC_lyr: 'Long_', USBR_lyr: 'Londec', \
              SW_lyr: 'Longitude', WQD1641_lyr: 'Longitude', NCRO_Oct2011_lyr: "Long__DecD", \
              NCRO_Flow_lyr: "Longitude", NCRO_SW_lyr: "Long_DD", NCRO_WQ_lyr: "Longitude"}
# the descriptive field (if any) for each list
DescFields = {GPS_lyr: '', SMayr_lyr: 'STR_NAME', CDEC_lyr: 'Location', USBR_lyr: '', \
              SW_lyr: 'Site_Name', WQD1641_lyr: 'Name', NCRO_Oct2011_lyr: "Station", \
              NCRO_Flow_lyr: "", NCRO_SW_lyr: "Station_Name", NCRO_WQ_lyr: "Station_Na"}
# 8-char names for each station list (to import into MS Access)
ShortNames = {GPS_lyr: "GPSMEAS", SMayr_lyr: 'SMAYR', CDEC_lyr: 'CDEC', USBR_lyr: 'USBR', \
              SW_lyr: 'SWGSMITH', WQD1641_lyr: 'WQD1641', NCRO_Oct2011_lyr: "NCRO2011", \
              NCRO_Flow_lyr: "NCROFLOW", NCRO_SW_lyr: "NCROSW", NCRO_WQ_lyr: "NCROWQ"}
# A generic primary station name field
genericStaID = 'BaseStaID'
# River Kilometer Index field name
RKIName = 'RKIName'
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
outFile.write('Type,BaseList,BaseSta,List,ListSta,RKIName,Lat,Lon\n')
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
        # Generate the nearest table list into the temporary table...
        # we will add fields to it for the permanent table.
        GenerateNearTable(baseLyr, lyr, tempTable, searchRadius,location, angle, closest, closestCount)
        # join some fields from the Base table to the nearest table...
        # ...join to get location names, lat & lon of base layer
        JoinField(tempTable, 'in_fid', baseLyr, BaseOIDFld_nm, [NameFields[baseLyr], \
                LatFields[baseLyr], LonFields[baseLyr]])
        if lyrCount == 0:
            # Create permanent table
            try: Delete(GPSMeasGDB + outTable)
            except: pass
            CreateTable(GPSMeasGDB, outTable, tempTable, '')
            AddField(GPSMeasGDB + outTable, 'Latitude', 'double', '', '', '', '', '', '', '')
            AddField(GPSMeasGDB + outTable, 'Longitude', 'double', '', '', '', '', '', '', '')
            AddField(GPSMeasGDB + outTable, 'StationList', 'text', '', '', 50, '', '', '', '')
            AddField(GPSMeasGDB + outTable, 'StaListName', 'text', '', '', 254, '', '', '', '')
            AddField(GPSMeasGDB + outTable, RKIName, 'text', '', '', 10, '', '', '', '')
            AddField(GPSMeasGDB + outTable, 'Description', 'text', '', '', 50, '', '', '', '')
        # ...join the station name from the target station list to the temp table
        if lyr == WQD1641_lyr:
            JoinField(tempTable, 'near_fid', lyr, desc.OIDFieldName, [NameFields[lyr],'RKI'])
        else:
            JoinField(tempTable, 'near_fid', lyr, desc.OIDFieldName, NameFields[lyr])
        # add the station list field to the temp table
        AddField(tempTable, 'StationList', 'text', '', '', 50, '', '', '', '')
        # fill the StationList field with the station list name
        rows = UpdateCursor(tempTable, '', '', '', '')
        for row in rows:
            row.StationList = shortLyr[0:49]
            rows.updateRow(row)
        # Make field mappings from the temp to the permanent table
        # Create FieldMappings object for append output fields
        fieldMappings = FieldMappings()
        # Add all fields from tempTable
        fieldMappings.addTable(tempTable)
        # create mapping for the station names field, "NameFields[]" for each station list,
        # "StaListName" for the permanent output table, and RKI name if available in this lyr
        fldMap_staListName = fieldMappings.getFieldMap(fieldMappings.findFieldMapIndex(NameFields[lyr]))
        # Set name of permanent output field StaListName
        fld_staListName = fldMap_staListName.outputField
        fld_staListName.name = "StaListName"
        fldMap_staListName.outputField = fld_staListName
        fieldMappings.addFieldMap(fldMap_staListName)
        # create mapping for the base layer lat/lon fields
        fldMap_lat = fieldMappings.getFieldMap(fieldMappings.findFieldMapIndex(LatFields[baseLyr]))
        fld_lat = fldMap_lat.outputField
        fld_lat.name = "Latitude"
        fldMap_lat.outputField = fld_lat
        fieldMappings.addFieldMap(fldMap_lat)
        #
        fldMap_lon = fieldMappings.getFieldMap(fieldMappings.findFieldMapIndex(LonFields[baseLyr]))
        fld_lon = fldMap_lon.outputField
        fld_lon.name = "Longitude"
        fldMap_lon.outputField = fld_lon
        fieldMappings.addFieldMap(fldMap_lon)
        #
        if lyr == WQD1641_lyr:
            fldMap_rki = fieldMappings.getFieldMap(fieldMappings.findFieldMapIndex('RKI'))
            fld_rki = fldMap_rki.outputField
            fld_rki.name = RKIName
            fldMap_rki.outputField = fld_rki
            fieldMappings.addFieldMap(fldMap_rki)
        # if the base list has the RKI names, add those to the temp table
        if baseLyr == WQD1641_lyr:
            fieldMappings = FieldMappings()
            # Add all fields from tempTable
            JoinField(tempTable, 'in_fid', baseLyr, BaseOIDFld_nm, 'RKI')
            fieldMappings.addTable(tempTable)
            fldMap_rki = fieldMappings.getFieldMap(fieldMappings.findFieldMapIndex('RKI'))
            fld_rki = fldMap_rki.outputField
            fld_rki.name = RKIName
            fldMap_rki.outputField = fld_rki
            fieldMappings.addFieldMap(fldMap_rki)
        #
        fieldMappings.removeFieldMap(fieldMappings.findFieldMapIndex(NameFields[lyr]))
#        fieldMappings.removeFieldMap(fieldMappings.findFieldMapIndex(LatFields[baseLyr]))
#        fieldMappings.removeFieldMap(fieldMappings.findFieldMapIndex(LonFields[baseLyr]))
        if fieldMappings.findFieldMapIndex('RKI') >= 0:
            fieldMappings.removeFieldMap(fieldMappings.findFieldMapIndex('RKI'))
        Append(tempTable, GPSMeasGDB + outTable, 'NO_TEST', fieldMappings, '')
        lyrCount += 1
    try: arcpy.management.Delete(tempTable)
    except: pass
    # Done checking the 'base' list against all station lists 
    # Add a station id field common to all station lists
    AddField(GPSMeasGDB + outTable, genericStaID, 'TEXT', '','', '50', '', 'NULLABLE', 'NON_REQUIRED', '')
    # Make a python list of rows to fill in as placeholders 
    #  for stations in the base layer that have no near matches
    # If there are gaps in the base layer numbering, this list
    #  will have them too...we'll resolve that later
    iRow = 0
    baseStaNumPrev = 0L
    missingStas = []
    rows = UpdateCursor(GPSMeasGDB + outTable, '', '', '', 'IN_FID A')
    for row in rows:
        iRow += 1
        row.setValue(genericStaID,row.getValue(NameFields[baseLyr]))
        rows.updateRow(row)
        baseStaNum = long(row.IN_FID)
        if row.NEAR_DIST < 0.5:
            staEquiv = 'Exact'
        else:
            staEquiv = 'Equiv'
        if row.RKIName == None:
            RKI_str = ''
        else:
            RKI_str = str(row.RKIName)
        if row.StaListName == None:
            SLN_str = ''
        else:
            SLN_str = row.StaListName
        outFile.write(staEquiv+','+os.path.basename(baseLyr).replace('_Proj','')+ \
            ','+row.BaseStaID+','+row.StationList.replace('_Proj','')+','+SLN_str+ \
            ','+RKI_str+','+str(row.Latitude)+','+str(row.Longitude)+'\n')
#        print staEquiv, 'stations:',os.path.basename(baseLyr),row.BaseStaID, \
#            row.StationList,row.StaListName
        if baseStaNum == baseStaNumPrev:
            continue
        while baseStaNum-baseStaNumPrev > 1L:
            missingStas += [baseStaNumPrev+1L]
            baseStaNumPrev += 1L
        baseStaNumPrev = baseStaNum
    outFile.flush()
    # delete the base list station id, lat/lon fields
    DeleteField(GPSMeasGDB + outTable, NameFields[baseLyr])
    if LatFields[baseLyr] <> 'Latitude':
        DeleteField(GPSMeasGDB + outTable, LatFields[baseLyr])
    if LonFields[baseLyr] <> 'Longitude':
        DeleteField(GPSMeasGDB + outTable, LonFields[baseLyr])
    del row, rows
    # Fill in blank placeholder rows for stations in the base layer that had no near matches
    # The missingStas list may contain false misses (gaps in the station numbering);
    # we account for that here
    rows = InsertCursor(GPSMeasGDB + outTable)
    nStasFound = nStasBase - len(missingStas)
    for sta in missingStas:
        row = rows.newRow()
        rowsBase = SearchCursor(baseLyr, BaseOIDFld_nm+" = "+str(sta), "", "", "")
        baseStaID = ''; baseLat = 0.0; baseLon = 0.0
        for rowBase in rowsBase:    # should be 0 or 1 row
            baseStaID = rowBase.getValue(NameFields[baseLyr])
            baseLat = rowBase.getValue(LatFields[baseLyr])
            baseLon = rowBase.getValue(LonFields[baseLyr])
        # if no station found, it's just a gap in the numbering
        if baseStaID == '':
            continue
        row.setValue(genericStaID, baseStaID)
        row.setValue('Latitude', baseLat)
        row.setValue('Longitude', baseLon)
        row.IN_FID = sta
        row.NEAR_FID = 0L
        row.NEAR_DIST = 0.0
        row.StationList = 'None'
        row.StaListName = 'None'
        rows.insertRow(row)
        outFile.write('NoMatch,'+os.path.basename(baseLyr).replace('_Proj','')+',' \
            +baseStaID+',,,'+str(baseLat)+','+str(baseLon)+'\n')
        #print "No near neighbor in", os.path.basename(baseLyr), "for station", baseStaID
    outFile.flush()
    try:
        Delete(workspaceDir2 + ShortNames[baseLyr]+'.dbf')
        Delete(workspaceDir2 + ShortNames[baseLyr]+'.dbf.xml')
    except: pass
    TableToTable(GPSMeasGDB + outTable, workspaceDir2, ShortNames[baseLyr]+'.dbf')
    print 'Total stations', nStasBase, 'Near Stations', nStasFound
    print
outFile.close()
print "Finished"
