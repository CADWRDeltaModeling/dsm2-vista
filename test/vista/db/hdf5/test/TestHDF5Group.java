package vista.db.hdf5.test;

import junit.framework.TestCase;
import vista.db.hdf5.HDF5Group;
import vista.set.DataReference;

public class TestHDF5Group extends TestCase{
	public void testRetrieve(){
		HDF5Group group = new HDF5Group("d:/temp/vista.data/180_currentDir.h5");
		assertNotNull(group);
		DataReference[] refs = group.getAllDataReferences();
		assertNotNull(refs);
		assertTrue(refs.length > 0);
	}
	/*
from vutils import *
from vista.db.hdf5 import *
g=HDF5Group('d:/temp/vista.data/180_currentDir.h5')
GroupFrame(g)
	 */
}
