package dsm2.server;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import gov.ca.dsm2.input.parser.InputTable;
import gov.ca.dsm2.input.parser.Parser;
import gov.ca.dsm2.input.parser.Tables;

/**
 * A servlet that serves up the echo file in json format.
 * Simply serves up the tables as arrays
 * @author psandhu
 *
 */
public class DSM2EchoFileServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String file = req.getParameter("file");
		if (file==null) return;
		Parser p = new Parser();
		Tables tables = p.parseModel(file);
		Gson gson = new Gson();
		resp.setContentType("applicaton/json");
		resp.getWriter().println("{ \"tables\": [ ");
		ArrayList<InputTable> itables = tables.getTables();
		int ntables = itables.size();
		for(int i=0; i < ntables; i++){
			InputTable t = itables.get(i);
			resp.getWriter().println(gson.toJson(t));
			if (i < ntables-1) resp.getWriter().println(",");
		}
		resp.getWriter().println("]}");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req,resp);
	}
	
}
