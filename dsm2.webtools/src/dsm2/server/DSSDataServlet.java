package dsm2.server;

import java.io.IOException;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hec.heclib.dss.CondensedReference;
import hec.heclib.dss.HecDss;
import hec.heclib.util.HecTime;
import hec.heclib.util.Heclib;
import hec.io.DataContainer;
import hec.io.TimeSeriesContainer;

/**
 * Servlet implementation class DSSServlet
 */
public class DSSDataServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DSSDataServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String dssfile = request.getParameter("dssfile");
		if (dssfile == null) {
			response.getWriter().println("No dss file specified");
			return;
		}
		String dsspath = request.getParameter("dsspath");
		try {
			HecDss dss = HecDss.open(dssfile);
			dsspath = searchRegex(replaceAndEscape(dsspath),dss);
			if (dsspath == null){
				return;
			}
			DataContainer dataContainer = dss.get(dsspath, true);
			response.setContentType("application/json");
			response.getWriter().println("{");

			if (dataContainer instanceof TimeSeriesContainer) {
				TimeSeriesContainer tsc = (TimeSeriesContainer) dataContainer;
				response.getWriter().println("\"location\": \""+tsc.location +"\",");
				response.getWriter().println("\"parameter\": \""+tsc.parameter+"\",");
				response.getWriter().println("\"subParameter\": \""+tsc.subParameter+"\",");
				response.getWriter().println("\"subLocation\": \""+tsc.subLocation+"\",");
				response.getWriter().println("\"units\": \""+tsc.units+"\",");
				response.getWriter().println("\"type\": \""+tsc.type+"\",");
				response.getWriter().println("\"valueArray\":[");
				HecTime ht = new HecTime();
				for (int i = 0; i < tsc.numberValues; i++) {
					ht.set(tsc.times[i]);
					response.getWriter().print("[" + ht.getTimeInMillis() + "," + (tsc.values[i] == Heclib.UNDEFINED_DOUBLE ? null : tsc.values[i]) + "]");
					if (i == tsc.numberValues - 1)
						break;
					response.getWriter().println(",");
				}
			}
			response.getWriter().println("]}");
			dss.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String searchRegex(String regpath, HecDss dss) {
		Vector<CondensedReference> condensedCatalog = dss.getCondensedCatalog();
		Pattern p = Pattern.compile(regpath, Pattern.CASE_INSENSITIVE);
		for (CondensedReference condensedReference : condensedCatalog) {
			String pathname = condensedReference.getNominalPathname();
			if (p.matcher(pathname).matches()) {
				return pathname;
			}
		}
		return null;
	}

	public static String replaceAndEscape(String dsspath) {
		if (dsspath == null)
			return ".*";
		String[] fields = dsspath.split("/");
		StringBuilder sb = new StringBuilder();
		int slashCount = 0;
		for (String f : fields) {
			slashCount++;
			if (slashCount == 1) {
				sb.append("/");
				continue;
			}
			if (f.length() == 0) {
				sb.append(".*");
			} else {
				sb.append(Pattern.quote(f));
			}
			sb.append("/");
		}
		while (slashCount < 7) {
			sb.append(".*/");
			slashCount++;
		}
		return sb.toString();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
