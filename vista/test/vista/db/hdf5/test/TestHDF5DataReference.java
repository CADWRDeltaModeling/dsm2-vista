package vista.db.hdf5.test;

import junit.framework.TestCase;
import vista.db.hdf5.HDF5DataReference;
import vista.set.DataReferenceMath;
import vista.set.DataReferenceVectorMathProxy;
import vista.set.DataSet;
import vista.set.Pathname;
import vista.set.RegularTimeSeries;
import vista.set.TimeSeriesMath;

public class TestHDF5DataReference extends TestCase {
	public void testRetrieve(){
		Pathname path1 = Pathname.createPathname("/hydro/channel 409/flow///test/");
		HDF5DataReference dataReference1 = new HDF5DataReference("test/sample_hydro.h5", "/hydro/data/channel flow/", new int[] {409, 0}, null, null, path1);
		Pathname path2 = Pathname.createPathname("/hydro/channel 409/area///test/");
		HDF5DataReference dataReference2 = new HDF5DataReference("test/sample_hydro.h5", "/hydro/data/channel area/", new int[] {409, 0}, null, null, path2);
		DataSet data1 = dataReference1.getData();
		dataReference2.getData();
		assertNotNull(data1);
		assertTrue(data1 instanceof RegularTimeSeries);
		DataReferenceVectorMathProxy mathProxy = new DataReferenceVectorMathProxy(dataReference1, dataReference2, DataReferenceMath.DIV);
		DataSet mathData = mathProxy.getData();
		assertNotNull(mathData);
	}
	
	public void testStage() {
		Pathname path1 = Pathname.createPathname("/hydro/channel 441/stage///test/");
		HDF5DataReference ref1 = new HDF5DataReference("test/sample_hydro.h5", "/hydro/data/channel stage/", new int[] {420, 0}, null, null, path1);
		DataSet data = ref1.getData();
		assertTrue(data instanceof RegularTimeSeries);
		RegularTimeSeries rts = (RegularTimeSeries) data;
		double val = TimeSeriesMath.sum(rts)/rts.size();
		assertTrue(val < 10); // check that its a reasonable value for stage in the delta.
	}
}
