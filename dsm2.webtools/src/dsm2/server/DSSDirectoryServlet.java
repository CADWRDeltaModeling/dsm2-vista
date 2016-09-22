package dsm2.server;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Shows all the files in the servers upload directory
 */
public class DSSDirectoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String defaultDir = "";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DSSDirectoryServlet() {
		super();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		defaultDir = config.getInitParameter("default_dir");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		File dir = new File(defaultDir);
		if (dir.isDirectory()) {
			response.getWriter().println("<html>");
			response.getWriter().println("<table>");
			for (File f : dir.listFiles()) {
				if (f.isFile() && f.getName().toLowerCase().endsWith(".dss")) {
					response.getWriter().println("<tr>");
					response.getWriter().append("<td><a href=\"/dsm2.webtools/catalog?dssfile="
							+ URLEncoder.encode(f.getAbsolutePath(), "UTF-8") + "\">"+f.getName()+"</a></td>");
					response.getWriter().println("</tr>");
				}
			}
			response.getWriter().println("</table>");
			response.getWriter().println("</html>");

		} else {
			return;
		}
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
