package vista.db.hdf5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.CompoundDS;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5File;
import vista.set.DataReference;
import vista.set.DataReferenceMath;
import vista.set.DataReferenceVectorMathProxy;
import vista.set.Group;
import vista.set.GroupProxy;
import vista.set.Pathname;
import vista.time.Time;
import vista.time.TimeFactory;
import vista.time.TimeInterval;
import vista.time.TimeWindow;

/**
 * Reads assuming a hydro tidefile structure.
 * 
 * @author psandhu
 * 
 */
@SuppressWarnings("serial")
public class HDF5Group extends GroupProxy {

	private String file;
	private String path;

	public HDF5Group(String file) {
		this.file = file;
		this.path = "/hydro/data";
		setName(file + "::" + path);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Group getInitializedGroup() {
		H5File h5file = new H5File(file);
		ArrayList<DataReference> references = new ArrayList<DataReference>();
		try {
			h5file.open();
			// run meta data
			HObject hydroObject = h5file.get("/hydro");
			List metadata = hydroObject.getMetadata();
			Time startTime = null;
			TimeInterval timeInterval = null;
			int numberOfIntervals = 0;
			String modelRun = "";
			for (Object meta : metadata) {
				Attribute attr = (Attribute) meta;
				if (attr.getName().equals("Start time")) {
					startTime = TimeFactory.getInstance().createTime(
							((int[]) attr.getValue())[0]);
				}
				if (attr.getName().equals("Time interval")) {
					timeInterval = TimeFactory.getInstance()
							.createTimeInterval(((int[]) attr.getValue())[0],
									TimeInterval.MIN_INTERVAL);
				}
				if (attr.getName().equals("Number of intervals")) {
					numberOfIntervals = ((int[]) attr.getValue())[0];
				}
				if (attr.getName().equals("Created Date")) {
					modelRun = ((String[]) attr.getValue())[0];
				}
			}
			//
			modelRun = getEnvar("DSM2MODIFIER", h5file);
			//
			if (startTime == null || timeInterval == null
					|| numberOfIntervals == 0) {
				throw new RuntimeException(
						"start time, time interval or number of intervals is not defined!");
			}
			Time endTime = startTime.create(startTime);
			endTime.incrementBy(timeInterval, numberOfIntervals);
			TimeWindow timeWindow = TimeFactory.getInstance().createTimeWindow(
					startTime, endTime);
			// references for channel flow
			HObject hObject = h5file.get("/hydro/input/channel");
			if (hObject instanceof CompoundDS) {
				CompoundDS channelds = (CompoundDS) hObject;
				channelds.getData();// for initializing
				int memberNameIndex = findIndexOfMemberName("chan_no",
						channelds);
				if (memberNameIndex == -1) {
					return null;
				}
				Object data = channelds.getData();
				if (data instanceof List) {
					List list = (List) data;
					Object object = list.get(memberNameIndex);
					if (object instanceof int[]) {
						int[] channelArray = (int[]) object;
						for (int i = 0; i < channelArray.length; i++) {
							Pathname pathname = Pathname
									.createPathname(new String[] { "hydro",
											channelArray[i] + "_upstream",
											"flow", timeWindow.toString(),
											timeInterval.toString(), modelRun });
							DataReference flowUpstream = new HDF5DataReference(file,
									"/hydro/data/channel flow", i, 0,
									timeWindow.create(), timeInterval.create(timeInterval), pathname);
							references.add(flowUpstream);
							pathname = Pathname.createPathname(new String[] {
									"hydro", channelArray[i] + "_downstream",
									"flow", timeWindow.toString(),
									timeInterval.toString(), modelRun });
							DataReference flowDownstream = new HDF5DataReference(file,
									"/hydro/data/channel flow", i, 1,
									timeWindow.create(), timeInterval.create(timeInterval), pathname);
							references.add(flowDownstream);
							// references for channel stage
							pathname = Pathname.createPathname(new String[] {
									"hydro", channelArray[i] + "_upstream",
									"stage", timeWindow.toString(),
									timeInterval.toString(), modelRun });
							references.add(new HDF5DataReference(file,
									"/hydro/data/channel stage", i, 0,
									timeWindow.create(), timeInterval.create(timeInterval), pathname));
							pathname = Pathname.createPathname(new String[] {
									"hydro", channelArray[i] + "_downstream",
									"stage", timeWindow.toString(),
									timeInterval.toString(), modelRun });
							references.add(new HDF5DataReference(file,
									"/hydro/data/channel stage", i, 1,
									timeWindow.create(), timeInterval.create(timeInterval), pathname));
							// references for channel area
							pathname = Pathname.createPathname(new String[] {
									"hydro", channelArray[i] + "_upstream",
									"area", timeWindow.toString(),
									timeInterval.toString(), modelRun });
							DataReference areaUpstream = new HDF5DataReference(file,
									"/hydro/data/channel area", i, 0,
									timeWindow.create(), timeInterval.create(timeInterval), pathname);
							references.add(areaUpstream);
							pathname = Pathname.createPathname(new String[] {
									"hydro", channelArray[i] + "_downstream",
									"area", timeWindow.toString(),
									timeInterval.toString(), modelRun });
							DataReference areaDownstream = new HDF5DataReference(file,
									"/hydro/data/channel area", i, 1,
									timeWindow.create(), timeInterval.create(timeInterval), pathname);
							references.add(areaDownstream);
							// references for channel velocity
							pathname = Pathname.createPathname(new String[] {
									"hydro", channelArray[i] + "_upstream",
									"velocity", timeWindow.toString(),
									timeInterval.toString(), modelRun });
							DataReference ref = new DataReferenceVectorMathProxy(flowUpstream, areaUpstream, DataReferenceMath.DIV);
							ref.setPathname(pathname);
							references.add(ref);
							pathname = Pathname.createPathname(new String[] {
									"hydro", channelArray[i] + "_downstream",
									"velocity", timeWindow.toString(),
									timeInterval.toString(), modelRun });
							ref = new DataReferenceVectorMathProxy(flowDownstream, areaDownstream, DataReferenceMath.DIV);
							ref.setPathname(pathname);
							references.add(ref);
						}
					} else {
						return null;
					}
				}
			}
			HObject reservoirObject = h5file.get("/hydro/input/reservoir");
			HObject reservoirConnectionObject = h5file
					.get("/hydro/input/reservoir_connection");
			HObject gateObject = h5file.get("/hydro/input/gate");
			HashMap<String, Reservoir> reservoirMap = new HashMap<String, Reservoir>();
			if (reservoirObject instanceof CompoundDS) {
				CompoundDS reservoirIds = (CompoundDS) reservoirObject;
				reservoirIds.getData();// for initializing
				int memberNameIndex = findIndexOfMemberName("name",
						reservoirIds);
				if (memberNameIndex == -1) {
					return null;
				}
				CompoundDS reservoirConnectionDS = (CompoundDS) reservoirConnectionObject;
				reservoirConnectionDS.getData();
				int nodeNumberIndex = findIndexOfMemberName("node",
						reservoirConnectionDS);
				//
				CompoundDS gateDS = (CompoundDS) gateObject;
				gateDS.getData();

				Object reservoirData = reservoirIds.getData();
				Object reservoirConnectionData = reservoirConnectionDS
						.getData();

				if (reservoirData instanceof List) {
					List list = (List) reservoirData;
					Object object = list.get(memberNameIndex);
					if (object instanceof String[]) {
						String[] reservoirNames = (String[]) object;
						for (int j = 0; j < reservoirNames.length; j++) {
							reservoirMap.put(reservoirNames[j], new Reservoir(
									reservoirNames[j]));
						}
						// fill with node connections
						List connectionList = (List) reservoirConnectionData;
						String[] names = (String[]) connectionList.get(0);
						int[] nodes = (int[]) connectionList.get(1);
						for (int k = 0; k < names.length; k++) {
							reservoirMap.get(names[k]).addNode(nodes[k]);
						}
						// fill with gate connections
						List gateList = (List) gateDS.getData();
						String[] gateNames = (String[]) gateList.get(0);
						String[] fromObj = (String[]) gateList.get(1);
						int[] to_node = (int[]) gateList.get(3);
						for (int k = 0; k < fromObj.length; k++) {
							if (fromObj[k].equalsIgnoreCase("reservoir")) {
								reservoirMap.get(gateNames[k]).addGateNode(
										to_node[k]);
							}
						}
						// now add references for stage and flow
						for (int i = 0; i < reservoirNames.length; i++) {
							// references for reservoir stage
							Pathname pathname = Pathname
									.createPathname(new String[] { "hydro",
											reservoirNames[i], "stage",
											timeWindow.toString(),
											timeInterval.toString(), modelRun });
							// FIXME: reservoir height needs a different slicing
							// then the other references
							references.add(new HDF5DataReference(file,
									"/hydro/data/reservoir height", i, 0,
									timeWindow, timeInterval, pathname));
							// references for reservoir flow
							Reservoir reservoir = reservoirMap
									.get(reservoirNames[i]);

							Integer[] nodeArray = reservoir.getNodes();
							for (int j = 0; j < nodeArray.length; j++) {
								pathname = Pathname
										.createPathname(new String[] {
												"hydro",
												reservoirNames[i] + "@"
														+ nodeArray[j], "flow",
												timeWindow.toString(),
												timeInterval.toString(),
												modelRun });
								references.add(new HDF5DataReference(file,
										"/hydro/data/reservoir flow", i, 0,
										timeWindow, timeInterval, pathname));
							}
						}
					} else {
						// FIXME: all these returns need to be consolidated
						return null;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		DataReference[] refs = new DataReference[references.size()];
		refs = references.toArray(refs);
		return Group.createGroup(this.file, refs);
	}

	private String getEnvar(String varName, H5File h5file) throws Exception{
		CompoundDS envarTable = (CompoundDS) h5file.get("/hydro/input/envvar");
		Vector columns = (Vector) envarTable.getData();
		String[] names = (String[]) columns.get(0);
		String[] values = (String[]) columns.get(1);
		for(int i=0; i < names.length; i++){
			if (varName.equals(names[i])){
				return values[i];
			}
		}
		return "N.A.";
	}

	private int findIndexOfMemberName(String string, CompoundDS channelds) {
		String[] memberNames = channelds.getMemberNames();
		for (int i = 0; i < memberNames.length; i++) {
			if (string.equals(memberNames[i]))
				return i;
		}
		return -1;
	}

}
