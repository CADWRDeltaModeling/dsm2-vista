package dsm2.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hec.heclib.dss.HecTimeSeriesBase;
import hec.heclib.util.HecTime;
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
public class H5TimeSliceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public H5TimeSliceServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	static String getEnvar(String varName, H5File h5file, boolean qualTidefile) throws Exception {
		String envarPath = qualTidefile ? "/input/envvar" : "/hydro/input/envvar";
		CompoundDS envarTable = (CompoundDS) h5file.get(envarPath);
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
		// starting time
		String startTimeReq = request.getParameter("time"); 
		// type-> one of ec, stage, flow, area.
		String dataType = request.getParameter("type");
		// number of time steps
		int sliceSize = Integer.parseInt(request.getParameter("slice"));
		//file to difference values ~ file - base file
		String baseFile = request.getParameter("basefile"); 
		try {
			H5Slice slice = extractSliceFromFile(file, startTimeReq, sliceSize, dataType);
			if (baseFile != null) {
				H5Slice baseSlice = extractSliceFromFile(baseFile, startTimeReq, sliceSize, dataType);
				slice = diff(slice, baseSlice);
			}
			if (slice == null) {
				return;
			}
			writeSliceAsJson(response, slice);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Find the common array and indices of their positions in either array.
	 * Note: Assumes c1 and c2 are sorted in ascending order.
	 * @return an array of 3 integer arrays (common array , index in array 1, index in array 2)
	 */
	public int[][] findCommonArray(int[] c1, int[] c2){
		int i1=0,i2=0;
		int i=0;
		int[] c = new int[Math.min(c1.length, c2.length)]; // max size of common
		int[] ic1 = new int[c.length]; // index of i1 for i
		int[] ic2 = new int[c.length]; // index of i2 for i
		while (i1 < c1.length && i2 < c2.length){
			if (c1[i1] == c2[i2]){
				c[i] = c1[i1];
				ic1[i] = i1;
				ic2[i] = i2;
				i1++; i2++; i++;
			} else if (c1[i1] < c2[i2]){
				i1++;
			} else if (c1[i1] > c2[i2]){
				i2++;
			}
		}
		c = resizeArray(c, i);
		ic1 = resizeArray(ic1, i);
		ic2 = resizeArray(ic2, i);
		return new int[][]{c, ic1, ic2};
	}

	/**
	 * Finds common array between the two arrays and returns an array of [String[] (common array), int[] (index in first of common value), int[] (index in second of common value)] 
	 * */
	public Object[] findCommonArray(String[] c1, String[] c2) {
		ArrayList<String> ac = new ArrayList<String>();
		int ml = Math.max(c1.length, c2.length);
		int[] ic1 = new int[ml]; // index of i1 for i
		int[] ic2 = new int[ml]; // index of i2 for i
		// efficient enough for small arrays expected here. For larger arrays sorting will be necessary for performance
		int i=0; 
		for(int i1=0; i1 < c1.length; i1++){
			for(int i2=0; i2 < c2.length; i2++){
				if (c1[i1].equalsIgnoreCase((c2[i2]))){
					ac.add(c1[i1]);
					ic1[i] = i1;
					ic2[i] = i2;
					i++;
					break;
				}
			}
		}
		String[] c = new String[ac.size()];
		ac.toArray(c);
		ic1 = resizeArray(ic1, c.length);
		ic2 = resizeArray(ic2, c.length);
		return new Object[]{c, ic1, ic2};
	}

	public int[] resizeArray(int[] array, int size) {
		if (size < array.length){ // resize
			int[] nc = new int[size];
			System.arraycopy(array, 0, nc, 0, size);
			array=nc;
		}
		return array;
	}

	public H5Slice diff(H5Slice slice, H5Slice baseSlice) {
		if (slice.timeIntervalInMins != baseSlice.timeIntervalInMins) {
			System.err.println("The time interval in file and base file don't match!");
			return null;
		}
		// choose the smaller sized slice (channels missing in other)
		H5Slice diffSlice = new H5Slice();
		diffSlice.dataType = slice.dataType + "-" + baseSlice.dataType; 
		diffSlice.sliceSize = slice.sliceSize;
		diffSlice.startTime = slice.startTime;
		diffSlice.timeIntervalInMins = slice.timeIntervalInMins;
		diffSlice.endTime = slice.endTime; //FIXME: may not be same for both tidefiles. Check!
		diffSlice.startTimeOffset = slice.startTimeOffset; //FIXME: may not be same for both tidefiles. Check!
		diffSlice.endTimeOffset = slice.endTimeOffset;
		int[][] common = findCommonArray(slice.channelArray, baseSlice.channelArray);
		Object[] rcommon = findCommonArray(slice.reservoirNames, baseSlice.reservoirNames);
		int[] indexReservoirSlice = (int[]) rcommon[1];
		int[] indexReservoirBaseSlice = (int[]) rcommon[2];
		diffSlice.reservoirNames = (String[]) rcommon[0];
		diffSlice.reservoirValues = new float[diffSlice.reservoirNames.length*diffSlice.sliceSize];
		diffSlice.channelArray = common[0];
		diffSlice.fData1 = new float[diffSlice.channelArray.length*diffSlice.sliceSize];
		diffSlice.fData2 = new float[diffSlice.channelArray.length*diffSlice.sliceSize];
		int[] indexSlice = common[1];
		int[] indexBase = common[2];
		for (int k = 0; k < slice.sliceSize; k++) {
			for (int i = 0; i < diffSlice.channelArray.length; i++) {
				int index = k * diffSlice.channelArray.length + i;
				int si = k*slice.channelArray.length+indexSlice[i];
				int bsi = k* baseSlice.channelArray.length+indexBase[i];
				diffSlice.fData1[index] = slice.fData1[si] - baseSlice.fData1[bsi];
				diffSlice.fData2[index] = slice.fData2[si] - baseSlice.fData2[bsi];
			}
			for(int i=0; i < diffSlice.reservoirNames.length; i++){
				int index = k * diffSlice.reservoirNames.length + i;
				int si = k*slice.reservoirNames.length+indexReservoirSlice[i];
				int bsi = k*baseSlice.reservoirNames.length+indexReservoirBaseSlice[i];
				diffSlice.reservoirValues[index] = slice.reservoirValues[si] - baseSlice.reservoirValues[bsi];
			}
		}
		return diffSlice;
	}

	public H5Slice extractSliceFromFile(String file, String startTimeReq, int sliceSize, String dataType)
			throws Exception, HDF5Exception, OutOfMemoryError {

		H5File h5file = new H5File(file);
		h5file.open();
		boolean qualTidefile = true;
		if (h5file.get("/hydro") != null) {
			qualTidefile = false;
		}

		String pathToData = "";
		if (qualTidefile) {
			pathToData = "/output/channel concentration";
		} else {
			if (dataType.equals("0")) {
				pathToData = "/hydro/data/channel stage";
			} else if (dataType.equals("1")) {
				pathToData = "/hydro/data/channel flow";
			} else if (dataType.equals("2")) { 
				pathToData = "/hydro/data/channel area";
			} else {
				System.err.println("Request for unknown data type: " + dataType);
			}
		}

		HObject dataObject = h5file.get(pathToData);
		if (dataObject == null) {
			return null;
		}
		H5ScalarDS ds = (H5ScalarDS) dataObject;
		List metadata = dataObject.getMetadata();
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
				// FIXME: workaround for bug in qual tidefile, hydro ends with n
				// as in min
				if (tistr.toLowerCase().endsWith("m")) {
					tistr += "in";
				}
				int[] status = new int[] { 0 };
				String intervalAsString = ((String[]) attr.getValue())[0];// Heclib.getEPartFromInterval(((int[])
																			// attr.getValue())[0],
																			// status);
				timeInterval = intervalAsString.toUpperCase();
				if (timeInterval.equals("60MIN")) {
					timeInterval = "1HOUR"; // FIXME: Hec does not accept, get
											// rid of this hec dependence
											// here!!!
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
		modelRun = getEnvar("DSM2MODIFIER", h5file, qualTidefile);
		//
		if (startTime == null || timeInterval == null || numberOfIntervals == 0) {
			throw new RuntimeException("start time, time interval or number of intervals is not defined!");
		}
		HecTime endTime = new HecTime(startTime);
		endTime.increment(numberOfIntervals - 1, timeIntervalInMins);
		String timeWindow = startTime.dateAndTime(104) + " - " + endTime.dateAndTime(104);
		//
		HecTime startTimeOffset = new HecTime(startTimeReq);
		if (startTimeOffset.lessThan(startTime)) { // if requesting time is less
													// than time in tidefile,
													// set it to start of time
													// and serve that slice.
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
		startDims[0] = timeOffset; // common to both hydro and qual, first
									// dimension is time
		selectedDims[0] = sliceSize;
		if (startDims.length == 4) { // qual has 4 dimensions for concentration,
										// 2nd dimension for constituent type as
										// defined in /output/constituent_names
			startDims[1] = Integer.parseInt(dataType);
			startDims[3] = 0; 
			selectedDims[3] = 1;
		} else { // hydro stage,flow and area have 3 dimensions, constituent
					// names can be substitued to be stage, flow and area for
					// same structure as qual
			startDims[2] = 0;
			selectedDims[2] = 1;
		}
		// read upstream slice
		Object rawData = ds.read();
		if (!(rawData != null && rawData instanceof float[])) {
			throw new IllegalArgumentException(
					"Path: " + " in HDF5 file: " + file + " is either null or not a floating point array");
		}
		// FIXME: data sets should be able to hold floats?
		float[] fData1 = (float[]) rawData;
		// read downstream slice
		if (startDims.length == 4) {
			startDims[3] = 1;
		} else {
			startDims[2] = 1;
		}
		rawData = ds.read();
		if (!(rawData != null && rawData instanceof float[])) {
			throw new IllegalArgumentException(
					"Path: " + " in HDF5 file: " + file + " is either null or not a floating point array");
		}

		float[] fData2 = (float[]) rawData;

		int[] channelArray = readChannelIds(h5file, qualTidefile);
		
		String[] reservoirNames = readReservoirNames(h5file, qualTidefile);
		
		float[] reservoirValues = readReservoirValues(h5file, qualTidefile, dataType, timeOffset, sliceSize);
		
		if (reservoirValues == null){
			reservoirValues = new float[reservoirNames.length*sliceSize];
		}
		// if stage then adjust for channel bottoms
		if (dataType.equals("stage")) {
			float[] channelBottoms = getBottomElevations(h5file);
			for (int k = 0; k < sliceSize; k++) {
				int soffset = k * channelArray.length;
				for (int i = 0; i < channelArray.length; i++) {
					fData1[soffset + i] = fData1[soffset + i] + channelBottoms[i];
					fData2[soffset + i] = fData2[soffset + i] + channelBottoms[i + channelArray.length];
				}
			}
		}
		H5Slice slice = new H5Slice();
		slice.dataType = dataType; // stage or flow, or area or
									// constituent_names[index] ?
		slice.sliceSize = sliceSize;
		slice.startTime = startTime;
		slice.timeIntervalInMins = timeIntervalInMins;
		slice.endTime = endTime;
		slice.startTimeOffset = startTimeOffset;
		slice.endTimeOffset = endTimeOffset;
		slice.fData1 = fData1;
		slice.fData2 = fData2;
		slice.channelArray = channelArray;
		slice.reservoirNames = reservoirNames;
		slice.reservoirValues = reservoirValues;
		// area is not meaningful so return velocity instead.
		if (dataType.equals("2") && !qualTidefile){
			H5Slice flowSlice = extractSliceFromFile(file, startTimeReq, sliceSize, "1");
			for(int i=0; i < slice.fData1.length; i++){
				slice.fData1[i] = flowSlice.fData1[i]/slice.fData1[i]; 
			}
			for(int i=0; i < slice.fData2.length; i++){
				slice.fData2[i] = flowSlice.fData2[i]/slice.fData2[i]; 
			}
		}
		return slice;
	}
	
	public float[] readReservoirValues(H5File h5file, boolean qualTidefile, String dataType, long timeOffset, int sliceSize) throws Exception, OutOfMemoryError{
		String pathToData = "/output/reservoir concentration";
		if (!qualTidefile) {
			if (dataType.equals("0")) {
				pathToData = "/hydro/data/reservoir height";
			} else if (dataType.equals("1")) {
				pathToData = "/hydro/data/reservoir flow";
			} else if (dataType.equals("2")) { //FIXME: no equivalent of velocity, just send back null
				pathToData = "";
				return null;
			} else {
				System.err.println("Request for unknown data type: " + dataType);
			}
		}

		HObject dataObject = h5file.get(pathToData);
		if (dataObject == null) {
			return null;
		}
		H5ScalarDS ds = (H5ScalarDS) dataObject;
		List metadata = dataObject.getMetadata(); // DO NOT REMOVE! Needed to initialize the data structures
		int numberOfIntervals = (int) ds.getDims()[0];
		//
		long[] startDims = ds.getStartDims();
		long[] stride = ds.getStride();
		long[] selectedDims = ds.getSelectedDims();
		long[] dims = ds.getDims();
		//first dimension is time
		startDims[0] = timeOffset; 
		selectedDims[0] = sliceSize;
		if (qualTidefile){
			// second dimension is constituent index
			startDims[1] = Integer.parseInt(dataType);
			selectedDims[1] = 1;
		} else { 
			return null; //FIXME: not implemented yet
		}
		// read upstream slice
		Object rawData = ds.read();
		if (!(rawData != null && rawData instanceof float[])) {
			throw new IllegalArgumentException(
					"Path: " + pathToData + " in HDF5 file: is either null or not a floating point array");
		}
		return (float[]) rawData;
	}

	public String[] readReservoirNames(H5File h5file, boolean qualTidefile) throws Exception, OutOfMemoryError {
		String pathToReservoirNames = qualTidefile ? "/output/reservoir_names": "/hydro/geometry/reservoir_names";
		HObject hObject = h5file.get(pathToReservoirNames);
		String[] reservoirNames = null;
		if (hObject instanceof H5ScalarDS){
			H5ScalarDS reservoirds = (H5ScalarDS) hObject;
			Object data = reservoirds.getData();
			reservoirNames = (String[]) data;
		}
		return reservoirNames;
	}

	public int[] readChannelIds(H5File h5file, boolean qualTidefile) throws Exception, OutOfMemoryError {
		String pathToChannelNumber = qualTidefile ? "/output/channel_number" : "/hydro/geometry/channel_number"; 
		HObject hObject = h5file.get(pathToChannelNumber);
		int[] channelArray = null;
		if (hObject instanceof H5ScalarDS) {
			H5ScalarDS channelds = (H5ScalarDS) hObject;
			Object data = channelds.getData();
			channelArray = (int[]) data;
		}
		return channelArray;
	}

	private float[] getBottomElevations(H5File h5file) throws Exception {
		HObject hObject = h5file.get("/hydro/geometry/channel_bottom");
		if (!(hObject instanceof H5ScalarDS)) {
			throw new IllegalArgumentException(
					"No channel bottom info in HDF5 file: " + h5file + " is not a scalar dataset");
		}
		//
		H5ScalarDS ds = (H5ScalarDS) hObject;
		// initialize the dim arrays
		List<Attribute> attributes = ds.getMetadata();
		//
		long[] startDims = ds.getStartDims();
		long[] stride = ds.getStride();
		long[] selectedDims = ds.getSelectedDims();
		long[] dims = ds.getDims();
		//
		startDims[0] = 0;
		startDims[1] = 0;
		//
		stride[0] = 1;
		stride[1] = 1;
		//
		// selectedDims[0] = 1;
		// selectedDims[1] = 1;
		//
		Object rawData = ds.read();
		float[] fData = (float[]) rawData;
		return fData;
	}

	public void writeSliceAsJson(HttpServletResponse response, H5Slice slice) throws IOException {
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
		if (slice.reservoirNames != null){
			response.getWriter().println("\"reservoirNames\":[");
			for (int i = 0; i < slice.reservoirNames.length; i++) {
				response.getWriter().print("\""+slice.reservoirNames[i]+"\"");
				if (i == slice.reservoirNames.length - 1)
					break;
				response.getWriter().print(",");
			}
			response.getWriter().println("],");
		}
		if (slice.reservoirValues != null){
			response.getWriter().println("\"reservoirValues\":[");
			for (int j = 0; j < slice.sliceSize; j++) {
				// start of slice array
				response.getWriter().println("[");
				for (int i = 0; i < slice.reservoirNames.length; i++) {
					response.getWriter().print(slice.reservoirValues[j*slice.reservoirNames.length+i]);
					if (i == slice.reservoirNames.length - 1)
						break;
					response.getWriter().print(",");
				}
				response.getWriter().println("]");
				if (j==slice.sliceSize-1){
					break;
				}
				response.getWriter().println(",");
			}
			// end data array
			response.getWriter().println("], ");
		}
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

	public class H5Slice {
		public String dataType;
		public int sliceSize;
		public HecTime startTime;
		public int timeIntervalInMins;
		public HecTime endTime;
		public HecTime startTimeOffset;
		public HecTime endTimeOffset;
		public float[] fData1, fData2;
		public int[] channelArray;
		public float[] reservoirValues;
		public String[] reservoirNames;
	}

}
