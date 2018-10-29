package gov.ca.dsm2.input.csdp;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;

/**
 * Convert CSDP *.cdn file into WKL
 * (https://en.wikipedia.org/wiki/Well-known_text) format
 * Even though the file has both channel and xsection, we import only channel outlines at this point
 *  
 * FIXME: Import XSections into GIS space as well.
 * 
 * 
 * @author psandhu
 *
 */
public class CSDPChannelNetworkToWKT {
	public static void main(String[] args) throws Exception {
		//String filename = "resources/delta_2009Calib.cdn"; //+proj=utm +zone=10 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs
		if (args.length < 2) {
			System.out
					.println("Usage: CSDPChannelNetworkToWKT <.cdn Filename> <.wkt Filename>");
			System.exit(2);
		}
		String filename = args[0];//+proj=utm +zone=10 +datum=NAD27 +units=m +no_defs
		String outfilename = args[1];
		convertToChannelFlowlinesWKT(filename, outfilename);
	}

	public static void convertToChannelFlowlinesWKT(String filename, String outfilename)
			throws FileNotFoundException, IOException {
		LineNumberReader reader = new LineNumberReader(new FileReader(filename));
		String line = reader.readLine();
		while (line.startsWith(";")){
			line=reader.readLine();
		}
		PrintWriter wr = new PrintWriter(new FileWriter(outfilename));
		wr.println("id;wkt");
		while(line!=null){
			while(line.trim().equals("")){
				line=reader.readLine();
				if (line == null) break;
			}
			if (line==null) break;
			String[] fields = line.trim().split("\\s+");
			String id = fields[0].replaceAll("\"","");
			int npts = Integer.parseInt(fields[1]);
			String wkt = "LINESTRING(";
			for(int i=0; i < npts; i++){
				line = reader.readLine().trim();
				fields = line.split(",");
				wkt+=(Double.parseDouble(fields[0])*0.3048)+" "+(Double.parseDouble(fields[1])*0.3048);
				if (i < npts-1){
					wkt+=",";
				}
			}
			wkt+=")";
			wr.println(id+";"+wkt);
			while(line.length() > 2 && !line.substring(2,3).equals("\"")){
				line = reader.readLine();
			}
			line = reader.readLine();
		}
		wr.close();
		reader.close();
	}
}
