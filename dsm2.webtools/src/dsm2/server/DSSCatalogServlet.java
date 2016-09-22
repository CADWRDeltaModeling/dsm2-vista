package dsm2.server;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hec.heclib.dss.CondensedReference;
import hec.heclib.dss.HecDss;

/**
 * Servlet implementation class DSSServlet
 */
public class DSSCatalogServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DSSCatalogServlet() {
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
		dsspath = replaceAndEscape(dsspath);
		try {
			HecDss dss = HecDss.open(dssfile);
			Vector<CondensedReference> condensedCatalog = dss.getCondensedCatalog();
			response.getWriter().println("<html>");
			response.getWriter().println("<table>");
			Pattern p = Pattern.compile(dsspath, Pattern.CASE_INSENSITIVE);
			for (CondensedReference r : condensedCatalog) {
				String pathname = r.getNominalPathname();
				if (!p.matcher(pathname).matches())
					continue; // skip non-matches
				response.getWriter().println("<tr>");
				response.getWriter()
						.append("<td><a href=\"/dsm2.webtools/timeseries_chart.jsp?dssfile="
								+ URLEncoder.encode(dssfile, "UTF-8") + "&dsspath="
								+ URLEncoder.encode(pathname, "UTF-8") + "\">" + pathname + "</a></td>");
				response.getWriter().println("</tr>");
			}
			response.getWriter().println("</table>");
			response.getWriter().println("</html>");
			dss.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String searchRegex(String dsspath, HecDss dss) {
		Vector<CondensedReference> condensedCatalog = dss.getCondensedCatalog();
		String regpath = replaceAndEscape(dsspath);
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
