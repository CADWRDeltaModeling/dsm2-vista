package vista.db.dss;

import java.io.File;

import junit.framework.TestCase;

public class TestDSSCatalog extends TestCase {

	private String dssFileName;

	protected void setUp() throws Exception {
		super.setUp();
		dssFileName = "scripts/testdata/file1.dss";
		tearDown();
	}

	protected void tearDown() throws Exception {
		if (new File(dssFileName).exists()){
			File fullCatalogFile = new File(dssFileName.replace(".dss",".dsc"));
			File condensedCatalogFile = new File(dssFileName.replace(".dss",".dsd"));
			if (fullCatalogFile.exists()){
				fullCatalogFile.delete();
			}
			if (condensedCatalogFile.exists()){
				condensedCatalogFile.delete();
			}
		}
	}
	
	public void testCatalogDSS(){
		DSSDataReader reader = new DSSDataReader();
		reader.generateCatalog(dssFileName);
		assertExistsFile(dssFileName.replace(".dss",".dsc"));
		assertExistsFile(dssFileName.replace(".dss",".dsd"));
	}
	
	public void assertExistsFile(String filename){
		File file = new File(filename);
		assertTrue(file.exists());
	}
}
