package vista.db.dss;

import java.io.File;

import junit.framework.TestCase;
import vista.set.DataSet;
import vista.set.IrregularTimeSeries;
import vista.set.RegularTimeSeries;

public class TestDSSWrite extends TestCase {

	private String filename;

	protected void setUp(){
		 filename = "test/testwrite.dss";
		 tearDown();
	}
	
	protected void tearDown(){
		File file = new File(filename);
		if (file.exists()){
			file.delete();
		}
	}
	
	public void testWriteRTS() {
		RegularTimeSeries rts = new RegularTimeSeries("/A/B/C//15MIN/F/",
				"01JAN1990 0100", "15MIN", new double[] { 1, 2, 3 });
		String pathname = "/A/B/C/01JAN1990/15MIN/F/";
		DSSUtil.writeData(filename, pathname, rts);
		DataSet dataSet = DSSUtil.readData(filename, pathname, false);
		assertNotNull(dataSet);
	}

	public void testWriteITS() {
		RegularTimeSeries rts = new RegularTimeSeries("/A/B/C//15MIN/F/",
				"01JAN1990 0100", "15MIN", new double[] { 1, 2, 3 });
		double stime = rts.getStartTime().getTimeInMinutes();
		IrregularTimeSeries its = new IrregularTimeSeries(
				"/A-ITS/B/C/01JAN1990/IR-HOUR/F-ITS/", new double[] { stime,
						stime + 5, stime + 11 }, new double[] { 0, 1, 7 });
		String pathname = "/A-ITS/B/C/01JAN1990/IR-MON/F-ITS/";
		DSSUtil.writeData(filename, pathname, its);
		DataSet dataSet = DSSUtil.readData(filename, pathname, false);
		assertNotNull(dataSet);
	}
}
