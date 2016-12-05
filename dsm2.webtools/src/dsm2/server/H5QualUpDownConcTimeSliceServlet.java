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
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.CompoundDS;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5ScalarDS;

/**
 * Servlet implementation class DSSServlet
 */
public class H5QualUpDownConcTimeSliceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public H5QualUpDownConcTimeSliceServlet() {
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
		int sliceSize = Integer.parseInt(request.getParameter("slice"));// number of time steps
		String baseFile = request.getParameter("basefile"); // file to difference from => values = file-basefile
		// System.out.println("Serving slice from "+file+" @ "+startTimeReq + " of size: "+ sliceSize);
		try {
			H5Slice slice = extractSliceFromFile(file, startTimeReq, sliceSize);
			if (baseFile != null){
				H5Slice baseSlice = extractSliceFromFile(baseFile, startTimeReq, sliceSize);
				slice = diff(slice,baseSlice);
			}
			if (slice==null){
				return;
			}
			writeSliceAsJson(response, slice);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public H5Slice diff(H5Slice slice, H5Slice baseSlice){
		if (slice.timeIntervalInMins != baseSlice.timeIntervalInMins){
			System.err.println("The time interval in file and base file don't match!");
			return null;
		}
		for (int j = 0; j < slice.sliceSize; j++) {
			// upnodes
			for (int i = 0; i < slice.channelArray.length; i++) {
				int index = j*slice.channelArray.length + i;
				slice.fData1[index]=slice.fData1[index]-baseSlice.fData1[index];
				slice.fData2[index]=slice.fData2[index]-baseSlice.fData2[index];
			}
			// end of slice array
		}
		return slice;
	}

	public H5Slice extractSliceFromFile(String file, String startTimeReq, int sliceSize)
			throws Exception, HDF5Exception, OutOfMemoryError {
		H5File h5file = new H5File(file);
		h5file.open();

		// run meta data
		HObject avgConcOutput = h5file.get("/output/channel concentration");
		if (avgConcOutput == null) {
			return null;
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
		if (startTimeOffset.lessThan(startTime)){ // if requesting time is less than time in tidefile, set it to start of time and serve that slice.
			startTimeOffset = new HecTime(startTime);
		}
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
		startDims[3] = 0;
		selectedDims[3] = 1;
		// read upstream slice
		Object rawData = ds.read();
		if (!(rawData != null && rawData instanceof float[])) {
			throw new IllegalArgumentException(
					"Path: " + " in HDF5 file: " + file + " is either null or not a floating point array");
		}
		// FIXME: data sets should be able to hold floats?
		float[] fData1 = (float[]) rawData;
		// read downstream slice
		startDims[3] = 1;
		rawData = ds.read();
		if (!(rawData != null && rawData instanceof float[])) {
			throw new IllegalArgumentException(
					"Path: " + " in HDF5 file: " + file + " is either null or not a floating point array");
		}
		float[] fData2 = (float[]) rawData;
		HObject hObject = h5file.get("/output/channel_number");
		int[] channelArray = null;
		if (hObject instanceof H5ScalarDS) {
			H5ScalarDS channelds = (H5ScalarDS) hObject;
			Object data = channelds.getData();
			channelArray = (int[]) data;
		}
		H5Slice slice = new H5Slice();
		slice.sliceSize = sliceSize;
		slice.startTime=startTime;
		slice.timeIntervalInMins = timeIntervalInMins;
		slice.endTime = endTime;
		slice.startTimeOffset = startTimeOffset;
		slice.endTimeOffset = endTimeOffset;
		slice.fData1 = fData1;
		slice.fData2 = fData2;
		slice.channelArray = channelArray;
		return slice;
	}

	public void writeSliceAsJson(HttpServletResponse response,H5Slice slice) throws IOException {
		response.setContentType("application/json");
		response.getWriter().println("{");
		response.getWriter().println("\"startTimeOffset\": \"" + slice.startTimeOffset.dateAndTime(104) + "\",");
		response.getWriter().println("\"endTimeOffset\": \"" + slice.endTimeOffset.dateAndTime(104) + "\",");
		response.getWriter().println("\"startTime\": \"" + slice.startTime.dateAndTime(104) + "\",");
		response.getWriter().println("\"endTime\": \"" + slice.endTime.dateAndTime(104) + "\",");
		response.getWriter().println("\"intervalInMins\": " + slice.timeIntervalInMins + ",");
		response.getWriter().println("\"channelIds\":[");
		for (int i = 0; i < slice.channelArray.length; i++) {
			response.getWriter().print(slice.channelArray[i]);
			if (i == slice.channelArray.length - 1)
				break;
			response.getWriter().print(",");
		}
		response.getWriter().println("],");
		response.getWriter().println("\"data\": [");
		for (int j = 0; j < slice.sliceSize; j++) {
			// start of slice array
			response.getWriter().println("[");
			// upnodes
			response.getWriter().print("[");
			for (int i = 0; i < slice.channelArray.length; i++) {
				response.getWriter().print(slice.fData1[j * slice.channelArray.length + i]);
				if (i == slice.channelArray.length - 1)
					break;
				response.getWriter().print(",");
			}
			response.getWriter().print("],");
			// downnodes
			response.getWriter().print("[");
			for (int i = 0; i < slice.channelArray.length; i++) {
				response.getWriter().print(slice.fData2[j * slice.channelArray.length + i]);
				if (i == slice.channelArray.length - 1)
					break;
				response.getWriter().print(",");
			}
			response.getWriter().print("]");
			// end of slice array
			response.getWriter().println("]");
			// if has more slices
			if (j == slice.sliceSize - 1)
				break;
			response.getWriter().println(",");
		}
		// end data array
		response.getWriter().println("]");
		response.getWriter().println("}");
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
	
	public class H5Slice{
		public int sliceSize;
		public HecTime startTime;
		public int timeIntervalInMins;
		public HecTime endTime;
		public HecTime startTimeOffset;
		public HecTime endTimeOffset;
		public float[] fData1,  fData2;
		public int[] channelArray;
	}

}
