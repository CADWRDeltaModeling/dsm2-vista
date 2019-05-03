package dsm2.server;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import hec.heclib.dss.HecTimePattern;
import hec.heclib.util.HecTime;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.CompoundDS;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5Group;
import ncsa.hdf.object.h5.H5ScalarDS;
import vista.time.Time;
import vista.time.TimeFactory;
import vista.time.TimeInterval;

/**
 * H5File Info Sends back a data structure in JSON with all the meta information
 * on the file.
 */
public class H5FileInfoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public H5FileInfoServlet() {
		super();
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
		try {
			H5FileInfo info = extractFileInfo(file);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			response.setContentType("applicaton/json");
			gson.toJson(info, response.getWriter());
			response.getWriter().flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Assumes that the path points to H5ScalerDS of table with single column
	 * @param pathToTable
	 * @return
	 * @throws Exception 
	 * @throws OutOfMemoryError 
	 */
	public String[] getTableAsString(H5File h5file, String pathToTable) throws OutOfMemoryError, Exception {
		H5ScalarDS ds = (H5ScalarDS) h5file.get(pathToTable);
		Object obj = ds.getData();
		return (String[]) obj;
	}

	public H5FileInfo extractFileInfo(String file) throws Exception, HDF5Exception, OutOfMemoryError {
		H5File h5file = new H5File(file);
		h5file.open();
		H5FileInfo fileInfo = new H5FileInfo();
		HObject hydroObject = h5file.get("/hydro");
		String path = "/input";
		String model = "qual";
		if (hydroObject != null){
			model="hydro";
			path="/hydro/input";
			fileInfo.dataTypeNames = new String[]{"stage","depth","flow","velocity"};
		}else if (h5file.get("/output/constituent_names") != null) {
			model="qual";
			H5ScalarDS ds = (H5ScalarDS) h5file.get("/output/constituent_names");
			fileInfo.dataTypeNames = (String[]) ds.getData();
		}else if (h5file.get("/output/bed_solids") != null) {
			model="sediment";
			H5ScalarDS ds = (H5ScalarDS) h5file.get("/output/bed_solid_names");
			extractDataTypeNamesByZoneLayer(h5file, fileInfo, ds);
		}else if (h5file.get("/output/bed_hg") != null) {
			model="hg";
			H5ScalarDS ds = (H5ScalarDS) h5file.get("/output/bed_hg_names");
			extractDataTypeNamesByZoneLayer(h5file, fileInfo, ds);
		}
		
		fileInfo.model = model;
		
		H5Group inputTables = (H5Group) h5file.get(path); 
		if (inputTables != null) {
		H5InputTable[] tables = new H5InputTable[inputTables.getNumberOfMembersInFile()];
		List memberList = inputTables.getMemberList();
		for (int i = 0; i < inputTables.getNumberOfMembersInFile(); i++) {
			H5InputTable table = parseInputTable((HObject) memberList.get(i));
			tables[i] = table;
			if (table.name.equals("scalar")) {
				String[] times = findModelStartEndTimes(table);
				fileInfo.startDate = times[0];
				fileInfo.startTime = times[1];
				fileInfo.endDate = times[2];
				fileInfo.endTime = times[3];
			}
			if (model.equals("qual") && table.name.equals("output_channel_source_track")){
				// constituent names either in constituent_names table or derived from looping over unique variable.source_group columns from output_channel_source_track
				if (table.count!=0){
					System.out.println("Original data type names: "+fileInfo.dataTypeNames.length);
					fileInfo.dataTypeNames = getDataTypeNames(table);
					System.out.println("New data type names: "+fileInfo.dataTypeNames.length);
				}
			}
		}
		fileInfo.inputTables = tables;
		} else { // try to get this information from the sediment or flux tables
			H5ScalarDS ds = null;
			if (model.equals("sediment")) {
				ds = (H5ScalarDS) h5file.get("/output/bed_solids");
			} else if (model.equals("hg")) {
				ds = (H5ScalarDS) h5file.get("/output/bed_hg");
			} else {
				throw new IllegalArgumentException("Unknown type of h5 file");
			}
			
			if (ds != null) {
				String[] infoStr = extractInfoFromOutputTable(ds);
				fileInfo.startDate = infoStr[0];
				fileInfo.startTime = infoStr[1];
				fileInfo.endDate = infoStr[2];
				fileInfo.endTime = infoStr[3];
				fileInfo.model = infoStr[4];
			}
		}
		h5file.close();
		return fileInfo;
	}

	private void extractDataTypeNamesByZoneLayer(H5File h5file, H5FileInfo fileInfo, H5ScalarDS ds)
			throws Exception, OutOfMemoryError {
		fileInfo.dataTypeNames = (String[]) ds.getData();
		// now add on zone and layerInfo info
		String[] layers = getTableAsString(h5file,"/output/layer");
		String[] zones = getTableAsString(h5file,"/output/zone");
		String [] dataTypeNames = new String[fileInfo.dataTypeNames.length*layers.length*zones.length];
		for(int i=0; i < fileInfo.dataTypeNames.length; i++) {
			String typeName = fileInfo.dataTypeNames[i];
			for (int l=0; l < layers.length; l++) {
				String layerName = "l"+(l+1);
				for(int z=0; z < zones.length; z++) {
					String zoneName = "z"+(z+1);
					dataTypeNames[(i*layers.length+l)*zones.length+z] = typeName+"x"+layerName+"x"+zoneName;
				}
			}
		}
		fileInfo.dataTypeNames=dataTypeNames;
	}

	public String[] extractInfoFromOutputTable(H5ScalarDS ds) throws HDF5Exception {
		List metadata = ds.getMetadata();
		int numberOfIntervals = (int) ds.getDims()[0];
		//
		Time startTime = null;
		Time endTime = null;
		String timeInterval = null;
		TimeInterval ti = null;
		int timeIntervalInMins = 0;
		String modelRun = "";
		for (Object meta : metadata) {
			Attribute attr = (Attribute) meta;
			if (attr.getName().equals("start_time")) {
				String timeStr = ((String[]) attr.getValue())[0];
				// "yyyy-MM-dd HH:mm:ss");
				startTime = TimeFactory.getInstance().createTime(timeStr,"yyyy-MM-dd HH:mm:ss");
			}
			if (attr.getName().equals("interval")) {
				String tistr = ((String[]) attr.getValue())[0];
				// FIXME: workaround for bug in qual tidefile, hydro ends with n
				// as in min
				if (tistr.toLowerCase().endsWith("m")) {
					tistr += "in";
				}
				int[] status = new int[] { 0 };
				String intervalAsString = ((String[]) attr.getValue())[0];
				timeInterval = intervalAsString.toUpperCase();
				if (timeInterval.equals("60MIN")) {
					timeInterval = "1HOUR"; 
				}
				ti = TimeFactory.getInstance().createTimeInterval(intervalAsString);
				timeIntervalInMins = (int) ti.getIntervalInMinutes(null);
			}
			if (attr.getName().equals("model")) {
				modelRun = ((String[]) attr.getValue())[0];
			}
		}
		startTime = startTime.create(startTime);
		endTime = startTime.create(startTime);
		endTime.incrementBy(ti,numberOfIntervals);
		String[] startTimeStr = startTime.toString().split("\\s+");
		String[] endTimeStr = endTime.toString().split("\\s+");
		String[] infoStr = new String[5];
		System.arraycopy(startTimeStr, 0, infoStr, 0, startTimeStr.length);
		System.arraycopy(endTimeStr, 0, infoStr, startTimeStr.length, endTimeStr.length);
		infoStr[4]=modelRun;
		return infoStr;		
	}
	
	private String[] getDataTypeNames(H5InputTable table) {
		LinkedHashSet<String> set = new LinkedHashSet<String>();
		String[] headers = table.headers;
		int variableIndex=-1, sourceGroupIndex=-1;
		for(int i=0; i < headers.length; i++){
			if (headers[i].equals("variable")){
				variableIndex=i;
			} else if (headers[i].equals("source_group")){
				sourceGroupIndex=i;
			}
		}
		Object[] values = table.values;
		String[] variables = (String[]) values[variableIndex];
		String[] sourceGroups = (String[]) values[sourceGroupIndex];
		for(int i=0; i < variables.length; i++){
			set.add(variables[i]+"."+sourceGroups[i]);
		}
		String[] uniqueConstituents = new String[set.size()];
		return set.toArray(uniqueConstituents);
	}

	public String[] findModelStartEndTimes(H5InputTable table) {
		String startDate = "";
		String startTime = "";
		String endDate = "";
		String endTime = "";
		if (table.name.equals("scalar")) {
			Object[] values = table.values;
			String[] col0 = (String[]) values[0];
			String[] col1 = (String[]) values[1];
			for (int i = 0; i < col0.length; i++) {
				if (col0[i].equals("run_start_date")) {
					startDate = (String) col1[i];
				} else if (col0[i].equals("run_start_time")) {
					startTime = (String) col1[i];
				} else if (col0[i].equals("run_end_date")) {
					endDate = (String) col1[i];
				} else if (col0[i].equals("run_end_time")) {
					endTime = (String) col1[i];
				}
			}
		}
		return new String[] { startDate, startTime, endDate, endTime };
	}

	public H5InputTable parseInputTable(HObject object) throws OutOfMemoryError, Exception {
		CompoundDS ds = (CompoundDS) object;
		List data = (List) ds.getData();
		H5InputTable table = new H5InputTable();
		table.name = ds.getName();
		table.headers = ds.getMemberNames();
		int nheaders = ds.getMemberCount();
		Object[] values = new Object[nheaders];
		for (int j = 0; j < nheaders; j++) {
			values[j] = data.get(j);
		}
		table.count = (int) ds.getDims()[0];
		table.values = values;
		return table;
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

	public class H5FileInfo {
		public String model; // hydro or qual
		public String startDate;
		public String startTime; // model start time in 01JAN2004 1600 format
		public String endDate;
		public String endTime; // model end time
		public H5InputTable[] inputTables; // all input path variables
		public String[] dataTypeNames; // names of constituents or flow,stage,area available
	}

	public class H5InputTable {
		public String name;
		public String[] headers;
		public Object[] values;
		public int count;
	}

}
