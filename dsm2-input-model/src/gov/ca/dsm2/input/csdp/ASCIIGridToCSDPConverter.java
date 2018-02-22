package gov.ca.dsm2.input.csdp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Convers an ASCII grid DEM file to CSDP prn file :-
 * The ASCII grid should be in NAD83 with units of meter.
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

	private String inFilename;
	private String outFilename;

	public ASCIIGridToCSDPConverter(String inFileName, String outFileName) {
		this.inFilename = inFileName;
		this.outFilename = outFileName;
	}

	public void convert() throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(this.inFilename));
		File tempFile = File.createTempFile("csdp", "prn");
		PrintWriter writer = new PrintWriter(new FileWriter(tempFile));
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
		int realCount=0;
		for (int i = 0; i < nrows; i++) {
			String[] fields = line.split("\\s");
			if ((i % pct) == 0) {
				System.out.println("Processed " + i + " of " + nrows
						+ " rows from file " + inFilename);
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
					depth = rawDepth / 0.3048;
					realCount++;
					writer.println(String.format("%12.5f,%12.5f,%4.2f,%s,%s", x, y,
							depth, "2014", "RFW-DEM"));
				}
			}
			line = reader.readLine();
		}
		writer.close();
		reader.close();
		//
		System.out.println("Actual number of points in data: "+realCount);
		// rewrite with realcount
		reader = new BufferedReader(new FileReader(tempFile));
		writer = new PrintWriter(new FileWriter(this.outFilename));
		 line = reader.readLine();
		while (line!=null){
			if (line.startsWith(";NumElements:")){
				line=";NumElements: "+realCount;
			}
			writer.println(line);
			line=reader.readLine();
		}
		reader.close();
		writer.close();
		tempFile.delete();
	}

}
