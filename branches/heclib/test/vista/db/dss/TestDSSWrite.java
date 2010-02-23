package vista.db.dss;

import vista.set.DataSet;
import vista.set.RegularTimeSeries;
import junit.framework.TestCase;

public class TestDSSWrite extends TestCase{

	public void testWriteRTS(){
		RegularTimeSeries rts = new RegularTimeSeries("/A/B/C//15MIN/F/", "01JAN1990 0100", "15MIN", new double[]{1,2,3});
		String filename = "test/testrtswrite.dss";
		String pathname = "/A/B/C/01JAN1990/15MIN/F/";
		DSSUtil.writeData(filename, pathname, rts);
		DataSet dataSet = DSSUtil.readData(filename, pathname, false);
		assertNotNull(dataSet);
	}
}
