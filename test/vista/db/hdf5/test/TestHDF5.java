package vista.db.hdf5.test;
import junit.framework.TestCase;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.object.h5.H5File;


public class TestHDF5 extends TestCase {
	public void testRead() throws Exception{
		H5File file = new H5File("hdf5_test.h5", HDF5Constants.H5F_ACC_RDONLY);
		int open = file.open();
		int childCount = file.getRootNode().getChildCount();
	}
}
