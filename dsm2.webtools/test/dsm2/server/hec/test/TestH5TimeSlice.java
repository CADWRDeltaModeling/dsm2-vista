package dsm2.server.hec.test;

import java.util.List;
import java.util.Vector;

import hec.heclib.dss.HecTimeSeriesBase;
import hec.heclib.util.HecTime;
import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.CompoundDS;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5ScalarDS;

public class TestH5TimeSlice {
	public static void main(String[] args) throws Exception {
		String file = "D:/delta/dsm2_v812_2309_fresh/studies/historical/output/historical_v81_ec.h5";
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
				String intervalAsString = ((String[]) attr.getValue())[0];//Heclib.getEPartFromInterval(((int[]) attr.getValue())[0], status);
				timeInterval = intervalAsString.toUpperCase();
				if (timeInterval.equals("60MIN")){
					timeInterval = "1HOUR"; //FIXME: Hec does not accept non standard intervals, e.g. 20 min or 21 min etc.
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
		long[] startDims = ds.getStartDims();
		long[] stride = ds.getStride();
		long[] selectedDims = ds.getSelectedDims();
		long[] dims = ds.getDims();
		startDims[0]=0;
		selectedDims[0]=16;
		//
		Object rawData = ds.read();
		if (!(rawData != null && rawData instanceof float[])) {
			throw new IllegalArgumentException("Path: " 
					+ " in HDF5 file: " + file
					+ " is either null or not a floating point array");
		}
		// FIXME: data sets should be able to hold floats?
		float[] fData = (float[]) rawData;
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

	static int findIndexOfMemberName(String string, CompoundDS channelds) {
		String[] memberNames = channelds.getMemberNames();
		for (int i = 0; i < memberNames.length; i++) {
			if (string.equals(memberNames[i]))
				return i;
		}
		return -1;
	}
}
