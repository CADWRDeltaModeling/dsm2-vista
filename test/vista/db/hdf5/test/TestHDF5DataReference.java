package vista.db.hdf5.test;

import junit.framework.TestCase;
import vista.db.hdf5.HDF5DataReference;
import vista.set.DataReferenceMath;
import vista.set.DataReferenceVectorMathProxy;
import vista.set.DataSet;
import vista.set.RegularTimeSeries;
import vista.set.VectorMath;

public class TestHDF5DataReference extends TestCase {
	public void testRetrieve(){
		HDF5DataReference dataReference = new HDF5DataReference("d:/temp/vista.data/180_currentDir.h5", "/hydro/data/channel flow/", 409, 0);
		HDF5DataReference dataReference2 = new HDF5DataReference("d:/temp/vista.data/180_currentDir.h5", "/hydro/data/channel area/", 409, 0);
		DataSet data = dataReference.getData();
		dataReference2.getData();
		assertNotNull(data);
		assertTrue(data instanceof RegularTimeSeries);
		DataReferenceVectorMathProxy mathProxy = new DataReferenceVectorMathProxy(dataReference, dataReference2, DataReferenceMath.DIV);
		DataSet mathData = mathProxy.getData();
		assertNotNull(mathData);
	}
}
