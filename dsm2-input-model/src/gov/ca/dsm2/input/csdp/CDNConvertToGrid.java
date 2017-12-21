package gov.ca.dsm2.input.csdp;

import gov.ca.dsm2.input.model.DSM2Model;
import gov.ca.dsm2.input.parser.InputTable;
import gov.ca.dsm2.input.parser.Parser;
import gov.ca.dsm2.input.parser.Tables;

import java.io.FileInputStream;
import java.io.PrintWriter;

public class CDNConvertToGrid {
	/**
	 * Parse a echo.inp file and associated .cdn file that describes the cross
	 * sections in the CSDP format.
	 * 
	 * Usage: <this program> echo.inp xsections.cdn
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.err
					.println("Usage: <this program> echo.inp xsections.cdn [gis.inp]");
			return;
		}
		// read dsm2 grid information from echo file
		Parser p = new Parser();
		Tables tables = p.parseModel(args[0]);
		if (args.length == 3) {
			p.parseAndAddToModel(tables, new FileInputStream(args[2]));
		}
		DSM2Model dsm2Model = tables.toDSM2Model();
		/*
		 * lets lose the node gis information
		 */
		/*
		 * for (Node n : dsm2Model.getNodes().getNodes()) { n.setLatitude(0);
		 * n.setLongitude(0); }
		 */
		// read in centerline and xsection information and update this models
		// grid information
		CDNReader reader = new CDNReader(args[1]);
		reader.readAndUpdateModel(dsm2Model);
		//
		tables.fromDSM2Model(dsm2Model);
		PrintWriter wr = new PrintWriter(args[0] + "_gis.inp");
		for (InputTable table : tables.getTables()) {
			if (table.getName().indexOf("_GIS") > 0) {
				wr.println(table.toStringRepresentation());
			}
		}
		wr.close();

	}
}
