package gov.ca.dsm2.input.csdp;

import gov.ca.dsm2.input.gis.CoordinateConversion;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;
import java.io.PrintWriter;

public class CSDPNodeCDLToGeoJsonConverter {
	public static void main(String[] args) throws Exception {
		String filename = "resources/node.cdl";
		LineNumberReader reader = new LineNumberReader(new FileReader(filename));
		String line = reader.readLine();
		int numberOfNodes = Integer.parseInt(line.trim());
		PrintWriter wr = new PrintWriter(new FileWriter("resources/dsm2_node.geojson"));
		wr.println("{ \"type\": \"FeatureCollection\", \"features\": [");
		boolean first = true;
		for(int i=0; i < numberOfNodes; i++){
			line = reader.readLine();
			String[] fields = line.trim().split("\\s+");
			if (!first){
				wr.println(",");
			} else{
				first=false;
			}
			CoordinateConversion converter = new CoordinateConversion();
			double[] latlng = converter.utm2LatLon("10 N "+ fields[0] + " " + fields[1]);
			wr.println("{\"type\": \"Feature\",");
			wr.println("\"properties\": { \"name\": \""+ fields[2].trim() +"\" },");
			wr.println("\"geometry\": { \"type\": \"Point\", \"coordinates\": [ "+latlng[1]+", "+latlng[0]+"]}");
			wr.println("}");
		}
		wr.println("]");
		wr.println("}");
		wr.close();
		reader.close();
	}
}
