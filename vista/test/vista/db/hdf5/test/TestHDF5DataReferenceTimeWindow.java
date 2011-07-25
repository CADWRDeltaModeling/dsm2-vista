package vista.db.hdf5.test;

import vista.db.hdf5.HDF5Group;
import vista.set.DataReference;
import vista.set.DataSet;
import vista.time.TimeFactory;
import vista.time.TimeWindow;
import junit.framework.TestCase;

public class TestHDF5DataReferenceTimeWindow extends TestCase{
	public void testTimeWindow() throws Exception{
		String file = "D:\\models\\DSM2v8\\Studies\\Historical_MiniCalibration_811\\text\\output_test\\hist_mini_calib_v811.h5";
		HDF5Group group = new HDF5Group(file);
		DataReference dataReference = group.getDataReference(0);
		TimeWindow tw = TimeFactory.getInstance().createTimeWindow("01JAN2000 0100 - 01JAN2000 2400");
		DataReference dataReference2 = dataReference.create(dataReference, tw);
		DataSet data = dataReference2.getData();
		assertNotNull(data);
	}
}
