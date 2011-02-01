package vista.db.hdf5.test;

import java.util.List;

import junit.framework.TestCase;
import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5ScalarDS;
import vista.set.RegularTimeSeries;
import vista.set.TimeSeries;
import vista.time.Time;
import vista.time.TimeFactory;
import vista.time.TimeInterval;

public class TestDataSetRetreive extends TestCase {
	public void testDataSetRetrieve() throws Exception {
		H5File h5File = new H5File(
				"test/hist.h5");
		h5File = new H5File("d:/temp/test_big.h5");
		int fileId = h5File.open();
		assertTrue(fileId > 0);
		HObject hObject = h5File.get("/hydro/data/channel flow");
		assertNotNull(hObject);
		assertTrue(hObject instanceof H5ScalarDS);
		H5ScalarDS ds = (H5ScalarDS) hObject;
		//
		List<Attribute> metadata = ds.getMetadata();

		long[] startDims = ds.getStartDims();
		long[] stride = ds.getStride();
		long[] selectedDims = ds.getSelectedDims();
		long[] dims = ds.getDims();
		//
		startDims[0] = 0; // beginining of time
		startDims[1] = 0; // up node/ down node
		startDims[2] = 0; // channel no
		//
		stride[0] = 1;
		stride[1] = 1;
		stride[2] = 1;
		//
		selectedDims[0] = dims[0]; // end of time
		selectedDims[1] = 1; // just that node
		selectedDims[2] = 1; // just that channel
		//
		int i = 1;
		startDims[1] = 1;
		startDims[2] = i;
		//
		Object data = ds.read();
		assertTrue(data instanceof float[]);
		float[] dataArray = (float[]) data;
		assertNotNull(dataArray);
		assertTrue(dataArray.length >= 400);
		assertNotNull(data);
		double[] xdataArray = new double[dataArray.length];
		for (int j = 0; j < xdataArray.length; j++) {
			xdataArray[j] = dataArray[j];
		}
		Time startTime = null;
		TimeInterval timeInterval = null;
		for (Attribute attribute : metadata) {
			String name = attribute.getName();
			Object value = attribute.getValue();
			if (name.equals("CLASS")) {
			} else if (name.equals("start_time")) {
				String tmstr = ((String[]) value)[0];
				startTime = TimeFactory.getInstance().createTime(tmstr,
						"yyyy-MM-dd HH:mm:ss");
			} else if (name.equals("interval")) {
				String intervalAsString = ((String[]) value)[0];
				timeInterval = TimeFactory.getInstance().createTimeInterval(
						intervalAsString);
			}
		}
		TimeSeries ts = new RegularTimeSeries("/hydro/channel " + i
				+ " upnode/channel flow//1hour/version8/", startTime,
				timeInterval, xdataArray, null, null);
		System.out.println(ts);
	}
}
