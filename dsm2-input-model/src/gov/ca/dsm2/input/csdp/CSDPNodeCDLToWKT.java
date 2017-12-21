package gov.ca.dsm2.input.csdp;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;
import java.io.PrintWriter;

/**
 * Convert CSDP node.cdl file into WKL points
 * Tested with QGIS import delimited text layer 
 * (https://en.wikipedia.org/wiki/Well-known_text) format
 * 
 * @author psandhu
 *
 */
public class CSDPNodeCDLToWKT {
	public static void main(String[] args) throws Exception {
		String filename = "resources/node.cdl";
		LineNumberReader reader = new LineNumberReader(new FileReader(filename));
		String line = reader.readLine();
		int numberOfNodes = Integer.parseInt(line.trim());
		PrintWriter wr = new PrintWriter(new FileWriter("resources/node.wkt"));
		wr.println("id;wkt");
		for (int i = 0; i < numberOfNodes; i++) {
			line = reader.readLine();
			String[] fields = line.trim().split("\\s+");
			wr.println(fields[2]+";"+"POINT("+fields[0]+" "+fields[1]+")");
		}
		wr.close();
		reader.close();
	}
}
