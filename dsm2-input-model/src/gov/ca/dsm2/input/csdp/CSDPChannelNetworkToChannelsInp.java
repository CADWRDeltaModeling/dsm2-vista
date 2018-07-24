package gov.ca.dsm2.input.csdp;

import java.io.FileReader;
import java.io.FileWriter;
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
public class CSDPChannelNetworkToChannelsInp {
	public static void main(String[] args) throws Exception {
		String filename = "resources/dsm2-ext-grid.cdn"; //+proj=utm +zone=10 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs
		//String filename = "resources/05jul2000.cdn"; //+proj=utm +zone=10 +datum=NAD27 +units=m +no_defs
		LineNumberReader reader = new LineNumberReader(new FileReader(filename));
		String line = reader.readLine();
		while (line.startsWith(";")){
			line=reader.readLine();
		}
		//PrintWriter wr = new PrintWriter(new FileWriter("resources/delta_2008Calib.wkt"));
		PrintWriter wr = new PrintWriter(new FileWriter("resources/dsm2-ext-grid.inp"));
		wr.println("channel;length");
		while(line!=null){
			while(line.trim().equals("")){
				line=reader.readLine();
				if (line == null) break;
			}
			if (line==null) break;
			String[] fields = line.trim().split("\\s+");
			String id = fields[0].replaceAll("\"","");
			int npts = Integer.parseInt(fields[1]);
			double channelLength=0;
			double[] p1 = new double[2];
			double[] p2 = new double[2];
			for(int i=0; i < npts; i++){
				line = reader.readLine().trim();
				fields = line.split(",");
				p1[0]=Double.parseDouble(fields[0]);
				p1[1]=Double.parseDouble(fields[1]);
				if (i > 0){
					channelLength+=Math.sqrt( Math.pow(p2[1]-p1[1],2) + Math.pow(p2[0]-p1[0],2) );
				}
				p2[0]=p1[0];
				p2[1]=p1[1];
			}
			wr.println(id+";"+channelLength);
			while(line.length() > 2 && !line.substring(2,3).equals("\"")){
				line = reader.readLine();
			}
			line = reader.readLine();
		}
		wr.close();
		reader.close();
	}
}
