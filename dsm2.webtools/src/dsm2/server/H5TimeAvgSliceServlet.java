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
import hec.heclib.dss.HecTimeSeriesBase;
import hec.heclib.util.HecTime;
import hec.heclib.util.Heclib;
import hec.io.DataContainer;
import hec.io.TimeSeriesContainer;
import hecdssvue.ca.dwr.dsm2.tidefile.DSM2Tidefile;
import hecdssvue.ca.dwr.dsm2.tidefile.HDF5DataReference;
import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.CompoundDS;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5ScalarDS;

/**
 * Servlet implementation class DSSServlet
 */
public class H5TimeAvgSliceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public H5TimeAvgSliceServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	static String getEnvar(String varName, H5File h5file) throws Exception {
		CompoundDS envarTable = (CompoundDS) h5file.get("/input/envvar");
		Vector columns = (Vector) envarTable.getData();
		String[] names = (String[]) columns.get(0);
		String[] values = (String[]) columns.get(1);
		for (int i = 0; i < names.length; i++) {
			if (varName.equals(names[i])) {
				return values[i];
			}
		}
		return "N.A.";
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
		String startTimeReq = request.getParameter("time"); // starting time

		int sliceSize = Integer.parseInt(request.getParameter("slice"));// number
																		// of
																		// time
																		// steps
		// to get at a
		// single shot
		try {
			H5File h5file = new H5File(file);
			h5file.open();

			// run meta data
			HObject avgConcOutput = h5file.get("/output/channel avg concentration");
			if (avgConcOutput == null) {
				return;
			}
			H5ScalarDS ds = (H5ScalarDS) avgConcOutput;
			List metadata = avgConcOutput.getMetadata();
			int numberOfIntervals = (int) ds.getDims()[0];
			//
			HecTime startTime = null;
			String timeInterval = null;
			int timeIntervalInMins = 0;
			String modelRun = "";
			for (Object meta : metadata) {
				Attribute attr = (Attribute) meta;
				if (attr.getName().equals("start_time")) {
					String timeStr = ((String[]) attr.getValue())[0];
					// "yyyy-MM-dd HH:mm:ss");
					startTime = new HecTime(timeStr);
				}
				if (attr.getName().equals("interval")) {
					String tistr = ((String[]) attr.getValue())[0];
					// FIXME: workaround for bug in qual tidefile
					if (tistr.toLowerCase().endsWith("m")) {
						tistr += "in";
					}
					int[] status = new int[] { 0 };
					String intervalAsString = ((String[]) attr.getValue())[0];// Heclib.getEPartFromInterval(((int[])
																				// attr.getValue())[0],
																				// status);
					timeInterval = intervalAsString.toUpperCase();
					if (timeInterval.equals("60MIN")) {
						timeInterval = "1HOUR"; // FIXME: Hec does not accept
												// non standard intervals, e.g.
												// 20 min or 21 min etc.
					}
					timeIntervalInMins = HecTimeSeriesBase.getIntervalFromEPart(timeInterval);
				}
				if (attr.getName().equals("model")) {
					modelRun = ((String[]) attr.getValue())[0];
				}
			}
			//
			modelRun = getEnvar("DSM2MODIFIER", h5file);
			//
			if (startTime == null || timeInterval == null || numberOfIntervals == 0) {
				throw new RuntimeException("start time, time interval or number of intervals is not defined!");
			}
			HecTime endTime = new HecTime(startTime);
			endTime.increment(numberOfIntervals - 1, timeIntervalInMins);
			String timeWindow = startTime.dateAndTime(104) + " - " + endTime.dateAndTime(104);
			//
			HecTime startTimeOffset = new HecTime(startTimeReq);
			int timeOffset = startTime.computeNumberIntervals(startTimeOffset, timeIntervalInMins);

			HecTime endTimeOffset = new HecTime(startTimeOffset);
			endTimeOffset.increment(sliceSize, timeIntervalInMins);
			//
			long[] startDims = ds.getStartDims();
			long[] stride = ds.getStride();
			long[] selectedDims = ds.getSelectedDims();
			long[] dims = ds.getDims();
			startDims[0] = timeOffset;
			selectedDims[0] = sliceSize;
			//
			Object rawData = ds.read();
			if (!(rawData != null && rawData instanceof float[])) {
				throw new IllegalArgumentException(
						"Path: " + " in HDF5 file: " + file + " is either null or not a floating point array");
			}
			// FIXME: data sets should be able to hold floats?
			float[] fData = (float[]) rawData;
			HObject hObject = h5file.get("/output/channel_number");
			int[] channelArray = null;
			if (hObject instanceof H5ScalarDS) {
				H5ScalarDS channelds = (H5ScalarDS) hObject;
				Object data = channelds.getData();
				channelArray = (int[]) data;
			}

			response.setContentType("application/json");
			response.getWriter().println("{");
			response.getWriter().println("\"startTimeOffset\": \"" + startTimeOffset.dateAndTime(104) + "\",");
			response.getWriter().println("\"endTimeOffset\": \"" + endTimeOffset.dateAndTime(104) + "\",");
			response.getWriter().println("\"intervalInMins\": " + timeIntervalInMins + ",");
			response.getWriter().println("\"channelIds\":[");
			for (int i = 0; i < channelArray.length; i++) {
				response.getWriter().print(channelArray[i]);
				if (i == channelArray.length - 1)
					break;
				response.getWriter().print(",");
			}
			response.getWriter().println("],");
			response.getWriter().println("\"data\": [");
			for (int j = 0; j < sliceSize; j++) {
				response.getWriter().print("[");
				for (int i = 0; i < channelArray.length; i++) {
					response.getWriter().print(fData[j * channelArray.length + i]);
					if (i == channelArray.length - 1)
						break;
					response.getWriter().print(",");
				}
				response.getWriter().print("]");
				if (j == sliceSize - 1)
					break;
				response.getWriter().println(",");
			}
			response.getWriter().println("]");
			response.getWriter().println("}");
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
