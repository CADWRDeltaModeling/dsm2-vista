package dsm2.server;

import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hec.heclib.dss.CondensedReference;
import hec.heclib.dss.DSSPathname;
import hec.heclib.dss.HecDss;
import hec.heclib.util.HecTime;
import hec.heclib.util.Heclib;
import hec.io.DataContainer;
import hec.io.TimeSeriesContainer;
import hecdssvue.ca.dwr.dsm2.tidefile.DSM2Tidefile;
import hecdssvue.ca.dwr.dsm2.tidefile.HDF5DataReference;

/**
 * Servlet implementation class DSSServlet
 */
public class H5DataServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public H5DataServlet() {
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
		try {
			DSM2Tidefile dss = new DSM2Tidefile(file);
			List<HDF5DataReference> refs = dss.getRefs();
			HDF5DataReference ref = searchRegex(replaceAndEscape(path),refs);
			if (path == null){
				return;
			}
			DataContainer dataContainer = ref.getData();
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
				ht.set(tsc.startTime);
				for (int i = 0; i < tsc.numberValues; i++) {
					ht.increment(1, tsc.interval);
					response.getWriter().print("[" + ht.getTimeInMillis() + "," + (tsc.values[i] == Heclib.UNDEFINED_DOUBLE ? null : tsc.values[i]) + "]");
					if (i == tsc.numberValues - 1)
						break;
					response.getWriter().println(",");
				}
			}
			response.getWriter().println("]}");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public HDF5DataReference searchRegex(String regpath, List<HDF5DataReference> refs) {
		Pattern p = Pattern.compile(regpath, Pattern.CASE_INSENSITIVE);
		for (HDF5DataReference r : refs) {
			String pathname = r.getPathname().pathname();
			if (p.matcher(pathname).matches()) {
				return r;
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
