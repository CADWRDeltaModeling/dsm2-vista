package gov.ca.dsm2.input.csdp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Convers an ASCII grid DEM file to CSDP prn file :-
 * 
 * <pre>
 *   ;HorizontalDatum: UTMNAD83
 *   ;HorizontalZone: 10
 *   ;HorizontalUnits: Meters 
 *   ;VerticalDatum: NAVD88
 *   ;VerticalUnits: USSurveyFeet 
 *   ;Filetype: bathmetry 
 *   ;NumElements: 3392903
 *   599225.70000,4209893.31000,-14.63,1950,NOAA
 *   ...
 * </pre>
 * 
 * @author psandhu
 * 
 */
public class ASCIIGridToCSDPConverter {

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out
					.println("Usage: ASCIIGridDEMSplitter <DEM file> <CSDP PRN File>");
			System.exit(2);
		}
		ASCIIGridToCSDPConverter converter = new ASCIIGridToCSDPConverter(
				args[0], args[1]);
		converter.convert();
	}

	private File inFile;
	private File outFile;

	public ASCIIGridToCSDPConverter(String inFileName, String outFileName) {
		inFile = new File(inFileName);
		outFile = new File(outFileName);
	}

	public void convert() throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(inFile));
		PrintWriter writer = new PrintWriter(new FileWriter(outFile));
		String[] headers = new String[] { ";HorizontalDatum:  UTMNAD83",
				";HorizontalZone:   10", ";HorizontalUnits:  Meters",
				";VerticalDatum:    NAVD88", ";VerticalUnits:    USSurveyFeet",
				";Filetype: bathmetry" };
		for (String h : headers) {
			writer.println(h);
		}
		String line = reader.readLine();
		int count = 0;
		int ncols = 0;
		int nrows = 0;
		double xllcorner = 0;
		double yllcorner = 0;
		double cellsize = 0;
		int nodataValue = 0;
		while (count < 6) {
			String[] fields = line.split("\\s+");
			if (fields.length != 2) {
				continue;
			}
			if (fields[0].equalsIgnoreCase("ncols")) {
				ncols = Integer.parseInt(fields[1]);
			} else if (fields[0].equalsIgnoreCase("nrows")) {
				nrows = Integer.parseInt(fields[1]);
			} else if (fields[0].equalsIgnoreCase("xllcorner")) {
				xllcorner = Double.parseDouble(fields[1]);
			} else if (fields[0].equalsIgnoreCase("yllcorner")) {
				yllcorner = Double.parseDouble(fields[1]);
			} else if (fields[0].equalsIgnoreCase("cellsize")) {
				cellsize = Double.parseDouble(fields[1]);
			} else if (fields[0].equalsIgnoreCase("nodata_value")) {
				nodataValue = Integer.parseInt(fields[1]);
			}
			line = reader.readLine();
			count++;
		}
		int numberOfValues = nrows * ncols;
		writer.println(";NumElements: " + numberOfValues);
		int pct = nrows / 100;
		for (int i = 0; i < nrows; i++) {
			String[] fields = line.split("\\s");
			if ((i % pct) == 0) {
				System.out.println("Processed " + i + " of " + nrows
						+ " rows from file " + inFile);
			}
			for (int j = 0; j < fields.length; j++) {
				double x = xllcorner + (j * cellsize);
				double y = yllcorner + ((nrows - i) * cellsize);
				double depth = 0;
				// int rawDepth = Integer.parseInt(fields[j]);
				// ASCII Grid values in meters in float format while output is
				// 10ths of feet in integer format
				float rawDepth = Float.parseFloat(fields[j]);
				if (Math.abs(rawDepth - nodataValue) <= 1e-5) {
					depth = -9999;
				} else {
					depth = rawDepth / 0.3048 / 1000;
				}
				writer.println(String.format("%12.5f,%12.5f,%4.2f,%s,%s", x, y,
						depth, "2014", "RFW-DEM"));
			}
			line = reader.readLine();
		}
		writer.close();
		reader.close();
	}

}
