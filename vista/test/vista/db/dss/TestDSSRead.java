package vista.db.dss;

import junit.framework.TestCase;
import vista.set.DataSet;
import vista.set.Group;
import vista.time.TimeFactory;

public class TestDSSRead extends TestCase {

	private String dssFileName;
	private String pathname;
	private long startJulmin;
	private long endJulmin;
	private boolean retrieveFlags;

	public TestDSSRead() {

	}

	protected void setUp() throws Exception {
		super.setUp();
		dssFileName = "scripts/testdata/file1.dss";
		pathname = "/VISTA-EX1/COS/FLOW/01JAN1982/5MIN/COS-WAVE/";
		startJulmin = TimeFactory.getInstance().createTime("01JAN1982 0000")
				.getTimeInMinutes();
		endJulmin = startJulmin + 1440;
		retrieveFlags = false;
		tearDown();
	}

	public void testRecordType() {
		DSSDataReader reader = new DSSDataReader();
		int recordType = reader.recordType(dssFileName, pathname);
		assertEquals(DSSUtil.REGULAR_TIME_SERIES, recordType);
	}

	public void testReadRegularTimeSeries() {
		DSSDataReader reader = new DSSDataReader();
		DSSData data = reader.getData(dssFileName, pathname, startJulmin,
				endJulmin, retrieveFlags);
		assertNotNull(data);
	}

	public void testDSSUtilReadData() {
		DataSet dataSet = DSSUtil.readData(dssFileName, pathname, true);
		assertNotNull(dataSet);
	}

	public void testReadIrregularTimeSeries() {
		DSSDataReader reader = new DSSDataReader();
		String irtsFilename = "scripts/testdata/gates_db.dss";
		String irtsPathname = "/HIST+GATE/CHWST000/POS/01JAN1971/IR-YEAR/DWR-OM-JOC-DSM2/";
		DSSData data = reader
				.getData(irtsFilename, irtsPathname, startJulmin, endJulmin,
						retrieveFlags);
		assertNotNull(data);
	}
	
	public void testMissingDSSFile(){
		Group missingGroup = DSSUtil.createGroup("local", "d:/missing.dss");
		missingGroup.getAllDataReferences();
	}
}
