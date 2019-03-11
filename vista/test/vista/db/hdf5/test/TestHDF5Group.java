package vista.db.hdf5.test;

import junit.framework.TestCase;
import vista.db.hdf5.HDF5Group;
import vista.set.DataReference;
import vista.set.DataSet;

public class TestHDF5Group extends TestCase{
	public void testRetrieveHydro(){
		HDF5Group group = new HDF5Group("test/sample_hydro.h5");
		assertNotNull(group);
		DataReference[] refs = group.getAllDataReferences();
		assertNotNull(refs);
		assertTrue(refs.length > 0);
		DataSet data = refs[0].getData();
		assertNotNull(data);
	}

	public void testRetrieveQual(){
		HDF5Group group = new HDF5Group("test/sample_qual.h5");
		assertNotNull(group);
		DataReference[] refs = group.getAllDataReferences();
		assertNotNull(refs);
		assertTrue(refs.length > 0);
		DataSet data = refs[0].getData();
		assertNotNull(data);
	}

	public void testRetrieveGTM(){
		HDF5Group group = new HDF5Group("test/sample_gtm.h5");
		assertNotNull(group);
		DataReference[] refs = group.getAllDataReferences();
		assertNotNull(refs);
		assertTrue(refs.length > 0);
		DataSet data = refs[0].getData();
		assertNotNull(data);
	}
	
	public void testRetrieveSedimentBed() {
		HDF5Group group = new HDF5Group("test/sample_gtm_sed.h5");
		assertNotNull(group);
		DataReference[] refs = group.getAllDataReferences();
		assertNotNull(refs);
		assertTrue(refs.length > 0);
		DataSet data = refs[0].getData();
		assertNotNull(data);		
	}
}
