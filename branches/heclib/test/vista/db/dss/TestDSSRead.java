package vista.db.dss;

import vista.db.dss.hec.DSSData;
import vista.db.dss.hec.DSSDataReader;
import vista.db.dss.hec.DSSUtil;
import vista.set.DataSet;
import vista.time.TimeFactory;
import junit.framework.TestCase;

public class TestDSSRead extends TestCase{

	private String dssFileName;
	private String pathname;
	private long startJulmin;
	private long endJulmin;
	private boolean retrieveFlags;

	public TestDSSRead(){
		
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		dssFileName = "scripts/testdata/file1.dss";
		pathname = "/VISTA-EX1/COS/FLOW/01JAN1982 0005 - 01JAN1982 2400/5MIN/COS-WAVE/";
		startJulmin = TimeFactory.getInstance().createTime("01JAN1982 0000").getTimeInMinutes();
		endJulmin = startJulmin+1440;
		retrieveFlags = false;
		tearDown();
	}
	
	public void testReadRegularTimeSeries(){
		DSSDataReader reader = new DSSDataReader();
		DSSData data = reader.getData(dssFileName, pathname, startJulmin, endJulmin, retrieveFlags);
		assertNotNull(data);
	}
	
	public void testDSSUtilReadData(){
		DataSet dataSet = DSSUtil.readData(dssFileName, pathname, true);
		assertNotNull(dataSet);
	}
}
