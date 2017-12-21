package gov.ca.dsm2.input.csdp;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;
import java.io.PrintWriter;

/**
 * Convert CSDP mjtstrm_vec.cdo file into WKL
 * (https://en.wikipedia.org/wiki/Well-known_text) format
 * 
 * These were the outlines of the levees. The file had issues with NAD83 datum but worked well with NAD27 UTM 10N import as projection.
 * 
 * @author psandhu
 *
 */
public class CSDPChannelOutlineToWKT {
	public static void main(String[] args) throws Exception {
		String filename = "resources/mjtstrm_vec.cdo";
		LineNumberReader reader = new LineNumberReader(new FileReader(filename));
		String line = reader.readLine();
		while (line.startsWith(";")){
			line=reader.readLine();
		}
		PrintWriter wr = new PrintWriter(new FileWriter("resources/mjtstrm_vec.wkt"));
		wr.println("id;wkt");
		while(line!=null){
			String id = line.trim();
			String wkt = "LINESTRING(";
			line=reader.readLine();
			while (!line.startsWith("END")){
				String[] fields = line.trim().split("\\s+");
				wkt+=fields[0]+" "+fields[1];
				line=reader.readLine();
				if (!line.startsWith("END")){
					wkt+=",";
				}
			}
			wkt+=")";
			wr.println(id+";"+wkt);
			line = reader.readLine();
			if (line.startsWith("END")){
				break;
			}
		}
		wr.close();
		reader.close();
	}
}
