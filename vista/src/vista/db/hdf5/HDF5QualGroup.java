package vista.db.hdf5;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import hec.heclib.util.Heclib;
import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.CompoundDS;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5ScalarDS;
import vista.set.DataReference;
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
public class HDF5QualGroup extends GroupProxy {

	private String file;
	private boolean isGTM = false;
	private boolean isSedimentBed;

	public HDF5QualGroup(String file) {
		this.file = file;
		setName(file + "::");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Group getInitializedGroup() {
		H5File h5file = new H5File(file);
		ArrayList<DataReference> references = new ArrayList<DataReference>();
		try {
			h5file.open();
			readInput(h5file);

			// run meta data
			HObject concentrationObject = h5file.get("/output/channel avg concentration");
			if (concentrationObject == null) { // indicates possible GTM file
				// try GTM with output/channel concentration path
				concentrationObject = h5file.get("/output/channel concentration");
				if (concentrationObject == null) {
					concentrationObject = h5file.get("/output/bed_solid_fluxes");
					if (concentrationObject != null) {
						isSedimentBed = true;
					} else {
						throw new IllegalArgumentException("Unknown kind of file: " + file);
					}
				} else {
					isGTM = true;
				}
			}
			// concentration object should be non null at this point
			H5ScalarDS scalar = (H5ScalarDS) concentrationObject;
			List metadata = concentrationObject.getMetadata();
			int numberOfIntervals = (int) scalar.getDims()[0];
			Time startTime = null;
			TimeInterval timeInterval = null;
			String modelRun = "model?";
			String modelVersion = "version?";
			for (Object meta : metadata) {
				Attribute attr = (Attribute) meta;
				if (attr.getName().equals("start_time")) {
					String timeStr = ((String[]) attr.getValue())[0];
					startTime = TimeFactory.getInstance().createTime(timeStr, "yyyy-MM-dd HH:mm:ss");
				}
				if (attr.getName().equals("interval")) {
					String tistr = ((String[]) attr.getValue())[0];
					// FIXME: workaround for bug in qual tidefile
					if (tistr.toLowerCase().endsWith("m")) {
						tistr += "in";
					}
					timeInterval = TimeFactory.getInstance().createTimeInterval(tistr);
					int[] status = new int[] { 0 };
					int mins = (int) timeInterval.getIntervalInMinutes(null);
					String intervalAsString = Heclib.getEPartFromInterval(mins, status);
					if (status[0] == 0) {
						timeInterval = TimeFactory.getInstance().createTimeInterval(intervalAsString);
					} else {
						// heclib was unsuccessful at conversion so lets keep using the parsed raw
						// value.
					}
				}
				if (attr.getName().equals("model")) {
					modelRun = ((String[]) attr.getValue())[0];
				}
				if (attr.getName().equals("model_version")) {
					modelVersion = ((String[]) attr.getValue())[0];
				}
			}
			//
			if (startTime == null || timeInterval == null || numberOfIntervals == 0) {
				throw new RuntimeException("start time, time interval or number of intervals is not defined!");
			}
			Time endTime = startTime.create(startTime);
			endTime.incrementBy(timeInterval, numberOfIntervals - 1);
			TimeWindow timeWindow = TimeFactory.getInstance().createTimeWindow(startTime, endTime);
			// references for channel flow
			int[] channelArray = getChannelNumbers(h5file);
			if (!isSedimentBed) { // sediment bed does not have these data sets
				//
				String[] constituentNames = getConstituentNames((H5ScalarDS) h5file.get("/output/constituent_names"));
				for (int i = 0; i < channelArray.length; i++) {
					Pathname pathname;
					if (!isGTM) {
						pathname = Pathname.createPathname(new String[] { "qual", channelArray[i] + "", "avg conc",
								timeWindow.toString(), timeInterval.toString(), modelRun + "-" + modelVersion });
						DataReference avgConc = new HDF5DataReference(file, "/output/channel avg concentration",
								new int[] { 0, i }, timeWindow.create(), timeInterval.create(timeInterval), pathname);
						references.add(avgConc);
					}
					for (int k = 0; k < constituentNames.length; k++) {
						pathname = Pathname.createPathname(new String[] { isGTM ? "gtm" : "qual",
								channelArray[i] + "_upstream", constituentNames[k], timeWindow.toString(),
								timeInterval.toString(), modelRun + "-" + modelVersion });
						references
								.add(new HDF5DataReference(file, "output/channel concentration", new int[] { k, i, 0 },
										timeWindow.create(), timeInterval.create(timeInterval), pathname));
						pathname = Pathname.createPathname(new String[] { isGTM ? "gtm" : "qual",
								channelArray[i] + "_downstream", constituentNames[k], timeWindow.toString(),
								timeInterval.toString(), modelRun + "-" + modelVersion });
						references
								.add(new HDF5DataReference(file, "/output/channel concentration", new int[] { k, i, 1 },
										timeWindow.create(), timeInterval.create(timeInterval), pathname));
					}
				}
				HObject reservoirObject = h5file.get("/output/reservoir_names");
				if (reservoirObject instanceof H5ScalarDS) {
					H5ScalarDS reservoirIds = (H5ScalarDS) reservoirObject;
					Object data = reservoirIds.getData();// for initializing
					// now add references for stage and flow
					String[] reservoirNames = (String[]) data;
					for (int i = 0; i < reservoirNames.length; i++) {
						// references for reservoir stage
						Pathname pathname = Pathname.createPathname(
								new String[] { isGTM ? "gtm" : "qual", reservoirNames[i], "conc", timeWindow.toString(),
										timeInterval.toString(), modelRun + "-" + modelVersion });
						references.add(new HDF5DataReference(file, "/output/reservoir concentration",
								new int[] { i, 0 }, timeWindow, timeInterval, pathname));
					}
				}
			}
			// For sediment bed model
			if (isSedimentBed) {
				// bed solid fluxes = time x flux x channels x zone x layer
				HObject bedSolidFluxes = h5file.get("/output/bed_solid_fluxes");
				if (bedSolidFluxes != null && bedSolidFluxes instanceof H5ScalarDS) {
					String[] bedSolidFluxNames = (String[]) ((H5ScalarDS) h5file.get("/output/bed_solid_flux_names"))
							.getData();
					String[] layerNames = (String[]) ((H5ScalarDS) h5file.get("/output/layer")).getData();
					String[] zoneNames = (String[]) ((H5ScalarDS) h5file.get("/output/zone")).getData();
					for (int f = 0; f < bedSolidFluxNames.length; f++) {
						for (int l = 0; l < layerNames.length; l++) {
							for (int z = 0; z < zoneNames.length; z++) {
								for (int c = 0; c < channelArray.length; c++) {
									Pathname pathname = Pathname
											.createPathname(
													new String[] { isGTM ? "gtm" : "qual", "CHANNEL " + channelArray[c],
															stripUnits(bedSolidFluxNames[f]) + ":" + layerNames[l] + ":"
																	+ zoneNames[z],
															timeWindow.toString(), timeInterval.toString(),
															modelRun + "-" + modelVersion });
									references.add(new HDF5DataReference(file, "output/bed_solid_fluxes",
											new int[] { f, c, z, l }, timeWindow.create(),
											timeInterval.create(timeInterval), pathname));
								}
							}
						}
					}
				}
				// bed solids = time x solids x channels x zone x layer
				HObject bedSolids = h5file.get("/output/bed_solids");
				if (bedSolids != null && bedSolids instanceof H5ScalarDS) {
					String[] bedSolidNames = (String[]) ((H5ScalarDS) h5file.get("/output/bed_solid_names")).getData();
					String[] layerNames = (String[]) ((H5ScalarDS) h5file.get("/output/layer")).getData();
					String[] zoneNames = (String[]) ((H5ScalarDS) h5file.get("/output/zone")).getData();
					for (int s = 0; s < bedSolidNames.length; s++) {
						for (int l = 0; l < layerNames.length; l++) {
							for (int z = 0; z < zoneNames.length; z++) {
								for (int c = 0; c < channelArray.length; c++) {
									Pathname pathname = Pathname.createPathname(
											new String[] { isGTM ? "gtm" : "qual", "CHANNEL " + channelArray[c],
													stripUnits(bedSolidNames[s]) + ":" + layerNames[l] + ":" + zoneNames[z],
													timeWindow.toString(), timeInterval.toString(),
													modelRun + "-" + modelVersion });
									references.add(new HDF5DataReference(file, "output/bed_solids",
											new int[] { s, c, z, l }, timeWindow.create(),
											timeInterval.create(timeInterval), pathname));
								}
							}
						}
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

	/**
	 * Assumes string contains "NAME (UNITS)" and extracts NAME field from string
	 * @param string
	 * @return
	 */
	public String stripUnits(String string) {
		return string.split("/s+")[0];
	}

	/**
	 * returns channel number array from the file from the path
	 * /output/channel_number
	 * 
	 * @param h5file
	 * @return int[] array of channel number as integers
	 */
	public int[] getChannelNumbers(H5File h5file) throws Exception {
		return (int[]) ((H5ScalarDS) h5file.get("/output/channel_number")).getData();
	}

	private void readInput(H5File h5file) {
		// TODO Auto-generated method stub

	}

	public String[] getConstituentNames(H5ScalarDS constituentNames) throws Exception {
		String[] names = (String[]) constituentNames.getData();
		return names;
	}

	private String getEnvar(String varName, H5File h5file) throws Exception {
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

	private int findIndexOfMemberName(String string, CompoundDS channelds) {
		String[] memberNames = channelds.getMemberNames();
		for (int i = 0; i < memberNames.length; i++) {
			if (string.equals(memberNames[i]))
				return i;
		}
		return -1;
	}

}
