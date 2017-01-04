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

import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.object.CompoundDS;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5Group;
import ncsa.hdf.object.h5.H5ScalarDS;

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
			fileInfo.dataTypeNames = new String[]{"stage","flow","velocity"};
		}
		fileInfo.model = model;
		if (model.equals("qual")){
			H5ScalarDS ds = (H5ScalarDS) h5file.get("/output/constituent_names");
			fileInfo.dataTypeNames = (String[]) ds.getData();
		}
		
		H5Group inputTables = (H5Group) h5file.get(path); 
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
		h5file.close();
		return fileInfo;
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
