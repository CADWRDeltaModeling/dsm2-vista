package vista.db.hdf5;

import hec.heclib.util.Heclib;

import java.lang.ref.WeakReference;
import java.util.List;

import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5ScalarDS;
import vista.set.DataReference;
import vista.set.DataSet;
import vista.set.DataSetAttr;
import vista.set.DataType;
import vista.set.Pathname;
import vista.set.RegularTimeSeries;
import vista.time.Time;
import vista.time.TimeFactory;
import vista.time.TimeInterval;
import vista.time.TimeWindow;

/**
 * A data reference implementation to the storage of regular time series only in
 * hdf5 file format.
 * <p>
 * The data reference points to a hdf5 file and requires a path (hdf5 path) in
 * that file to the table containing the data.
 * <p>
 * In additon to that it needs information on how to slice the table to extract
 * the time series
 * 
 * Table is assumed to be dimension of [time index, type of data, number of
 * channels, upstream/downstream end (size=2)]
 * 
 * @author Nicky Sandhu
 * 
 */
@SuppressWarnings("serial")
public class HDF5DataReference extends DataReference {
	private String path;
	private transient WeakReference<DataSet> dataset;
	/**
	 * Time is always assumed as 0 dimension and is sliced for entire length or as
	 * dictated by time window These are the slices through the other dimensions,
	 * ie. the index of the columns in the other dimension so the slice results in a
	 * 1-D time series.
	 */
	private int[] otherDimensionSelections;

	/**
	 * When dimension of table referenced by file and path is equal to 5. Assumes
	 * [time x data type x location x zone x layer ]
	 * 
	 * @param file           is the operating system file location
	 * @param path           is the path to the data. This is of the form
	 *                       /parent/child/dataset
	 * @param otherDimension index into other dimension than 0
	 * @param tw             length to select this dimension
	 * @param ti             only needed to give meaning the time dimension
	 * @param pathname
	 */
	public HDF5DataReference(String file, String path, int[] otherDimensionSelections, TimeWindow tw, TimeInterval ti,
			Pathname pathname) {
		setFilename(file);
		setServername("local");
		this.path = path;
		this.otherDimensionSelections = otherDimensionSelections;
		setTimeWindow(tw);
		setTimeInterval(ti);
		setPathname(pathname);
		setName(file + "::" + pathname);
	}

	@Override
	protected DataReference createClone() {
		return new HDF5DataReference(getFilename(), this.path, this.otherDimensionSelections, getTimeWindow(),
				getTimeInterval(), getPathname());
	}

	@SuppressWarnings("unchecked")
	@Override
	public DataSet getData() {
		if (dataset == null || dataset.get() == null) {
			String file = getFilename();
			H5File h5file = new H5File(file);
			try {
				h5file.open();
				HObject hObject = h5file.get(path);
				if (!(hObject instanceof H5ScalarDS)) {
					throw new IllegalArgumentException(
							"Path: " + path + " in HDF5 file: " + file + " is not a scalar dataset");
				}
				//
				H5ScalarDS ds = (H5ScalarDS) hObject;
				// initialize the dim arrays
				List<Attribute> attributes = ds.getMetadata();
				Time startTimeString = null;
				TimeInterval timeInterval = null;
				String modelName = "";
				String modelVersion = "";
				for (Attribute attribute : attributes) {
					String name = attribute.getName();
					Object value = attribute.getValue();
					if (name.equals("CLASS")) {
						// TODO: add assert for match to TIMESERIES
					} else if (name.equals("start_time")) {
						String tmstr = ((String[]) value)[0];
						startTimeString = TimeFactory.getInstance().createTime(tmstr, "yyyy-MM-dd HH:mm:ss");
					} else if (name.equals("interval")) {
						// FIXME: copied over from HDF5QualGroup.java (consolidate this)
						String tistr = ((String[]) value)[0];
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
					} else if (name.equals("model")) {
						modelName = ((String[]) value)[0];
					} else if (name.equals("") || name.equals("model_version")) {
						modelVersion = ((String[]) value)[0];
					}
				}
				Time dataStartTime = TimeFactory.getInstance().createTime(startTimeString);
				if (getTimeInterval() == null) {
					setTimeInterval(timeInterval);
				}
				//
				long[] startDims = ds.getStartDims();
				long[] stride = ds.getStride();
				long[] selectedDims = ds.getSelectedDims();
				long[] dims = ds.getDims();
				// FIXME: startDims[0]=startTimeIndex
				// dimension 0 is assumed to be time.
				if (getTimeWindow() != null) {
					Time startTime = getTimeWindow().getStartTime();
					startDims[0] = Math.max(0, dataStartTime.getExactNumberOfIntervalsTo(startTime, getTimeInterval()));
				} else {
					startDims[0] = 0;
				}
				// dims 4 => dataType x channel x node (up/down)
				// dims 3 => x channel (for qual only )
				// dims 2 => channel x node (up/node)
				//
				assert(startDims.length == this.otherDimensionSelections.length+1);
				for(int i=1; i < startDims.length; i++) {
					startDims[i]=this.otherDimensionSelections[i-1];
				}
				for (int i = 0; i < stride.length; i++) {
					stride[i] = 1;
				}
				// only slice the time window from the time dimension : assumption dimension 0
				if (getTimeWindow() != null) {
					Time startTime = getTimeWindow().getStartTime();
					Time endTime = getTimeWindow().getEndTime();
					selectedDims[0] = Math.min(startTime.getExactNumberOfIntervalsTo(endTime, getTimeInterval()) + 1,
							dims[0]);
				} else { // if no time window, slice the entire length.
					selectedDims[0] = dims[0];
				}
				// for all other dimensions slice through them
				for (int i = 1; i < selectedDims.length; i++) {
					selectedDims[i] = 1;
				}
				//
				Object rawData = ds.read();
				if (!(rawData != null && rawData instanceof float[])) {
					throw new IllegalArgumentException("Path: " + path + " in HDF5 file: " + file
							+ " is either null or not a floating point array");
				}
				// FIXME: data sets should be able to hold floats?
				float[] fData = (float[]) rawData;
				double[] dData = new double[fData.length];
				
				//FIXME: Once DSM2-164 fixes hydro this could be changed to be stage
				if (path.contains("hydro/data/channel stage")) {
					double elevation = getBottomElevation(h5file, otherDimensionSelections);
					for (int i = 0; i < fData.length; i++) {
						dData[i] = fData[i] + elevation;
					}
				} else {
					for (int i = 0; i < fData.length; i++) {
						dData[i] = fData[i];
					}
				}

				if (getTimeWindow() == null) {
					Time endTime = dataStartTime.create(dataStartTime);
					endTime.incrementBy(getTimeInterval(), dData.length - 1);
					setTimeWindow(TimeFactory.getInstance().createTimeWindow(dataStartTime, endTime));
				}

				String yUnits = getUnits(getPathname().getPart(Pathname.C_PART));
				DataSetAttr attr = new DataSetAttr(DataType.REGULAR_TIME_SERIES, "TIME", yUnits, "", "INST-VAL");
				RegularTimeSeries rts = new RegularTimeSeries(getPathname().toString(), getTimeWindow().getStartTime(),
						timeInterval, dData, null, attr);
				dataset = new WeakReference<DataSet>(rts);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					h5file.close();
				} catch (HDF5Exception e) {
				}
			}
		}
		return dataset.get();
	}

	private String getUnits(String part) {
		if (part.equalsIgnoreCase("FLOW")) {
			return "CFS";
		} else if (part.equalsIgnoreCase("STAGE")) {
			return "FT";
		} else if (part.equalsIgnoreCase("AREA") || part.equalsIgnoreCase("AVG_AREA")) {
			return "FT^2";
		} else if (part.equalsIgnoreCase("VOLUME")) {
			return "FT^3";
		} else {
			return "";
		}
	}

	@Override
	public void reloadData() {
		if (dataset != null) {
			dataset.clear();
		}
		dataset = null;
	}

	public String toString() {
		return "HDF5::" + getFilename() + "::" + path + "::" + otherDimensionSelections + "::" + getTimeWindow() + "::"
				+ getTimeInterval() + "::" + getPathname();
	}

	public double getBottomElevation(H5File h5file, int [] dimensions) throws Exception {
		HObject hObject = h5file.get("/hydro/geometry/channel_bottom");
		if (!(hObject instanceof H5ScalarDS)) {
			throw new IllegalArgumentException(
					"Path: " + path + " in HDF5 file: " + h5file.getPath() + " is not a scalar dataset");
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
		startDims[0] = dimensions[1]; // dimension are backwards?
		startDims[1] = dimensions[0];
		//
		stride[0] = 1;
		stride[1] = 1;
		//
		selectedDims[0] = 1;
		selectedDims[1] = 1;
		//
		Object rawData = ds.read();
		if (!(rawData != null && rawData instanceof float[])) {
			throw new IllegalArgumentException("Path: " + path + " in HDF5 file: " + h5file.getPath()
					+ " is either null or not a floating point array");
		}
		// FIXME: data sets should be able to hold floats?
		float[] fData = (float[]) rawData;
		return fData[0];
	}

}
