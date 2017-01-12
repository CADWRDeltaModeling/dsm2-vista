package gov.ca.dsm2.input.parser.test;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.ca.dsm2.input.model.DSM2Model;
import gov.ca.dsm2.input.parser.InputTable;
import gov.ca.dsm2.input.parser.Parser;
import gov.ca.dsm2.input.parser.Tables;

public class DSM2JsonTest {
	public static void main(String[] args) throws Exception{
		Parser p = new Parser();
		Tables tables = p.parseModel("test/hydro_echo.inp");
		ArrayList<InputTable> tableArray = tables.getTables();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();//new Gson();
		for(InputTable t: tableArray){
			System.out.println(gson.toJson(t));
		}
		
		DSM2Model model = tables.toDSM2Model();
		System.out.println(gson.toJson(model));
	}
}
