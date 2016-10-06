package dsm2.server;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hec.heclib.dss.CondensedReference;
import hec.heclib.dss.HecDss;
import hecdssvue.ca.dwr.dsm2.tidefile.DSM2Tidefile;
import hecdssvue.ca.dwr.dsm2.tidefile.HDF5DataReference;

/**
 * Servlet implementation class DSSServlet
 */
public class H5CatalogServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public H5CatalogServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String file = request.getParameter("file");
		if (file == null) {
			response.getWriter().println("No h5 file specified");
			return;
		}
		String path = request.getParameter("path");
		path = replaceAndEscape(path);
		try {
			DSM2Tidefile dss = new DSM2Tidefile(file);
			List<HDF5DataReference> refs = dss.getRefs();
			response.getWriter().println("<html>");
			response.getWriter().println("<table>");
			Pattern p = Pattern.compile(path, Pattern.CASE_INSENSITIVE);
			for (HDF5DataReference r : refs) {
				String pathname = r.getPathname().pathname();
				if (!p.matcher(pathname).matches())
					continue; // skip non-matches
				response.getWriter().println("<tr>");
				response.getWriter()
						.append("<td><a href=\"h5timeseries_chart.jsp?file="
								+ URLEncoder.encode(file, "UTF-8") + "&path="
								+ URLEncoder.encode(pathname, "UTF-8") + "\">" + pathname + "</a></td>");
				response.getWriter().println("</tr>");
			}
			response.getWriter().println("</table>");
			response.getWriter().println("</html>");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static String replaceAndEscape(String path) {
		if (path == null)
			return ".*";
		String[] fields = path.split("/");
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
