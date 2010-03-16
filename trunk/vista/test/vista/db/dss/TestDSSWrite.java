package vista.db.dss;

import java.io.File;

import junit.framework.TestCase;
import vista.set.DataSet;
import vista.set.DataSetAttr;
import vista.set.DataType;
import vista.set.FlagUtils;
import vista.set.IrregularTimeSeries;
import vista.set.RegularTimeSeries;
import vista.set.TimeElement;

public class TestDSSWrite extends TestCase {

	private String filename;

	protected void setUp() {
		filename = "test/testwrite.dss";
		tearDown();
	}

	protected void tearDown() {
		File file = new File(filename);
		if (file.exists()) {
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
		assertTrue(dataSet instanceof IrregularTimeSeries);
		IrregularTimeSeries its2 = (IrregularTimeSeries) dataSet;
		TimeElement elementAt = its.getElementAt("01JAN1990 0100");
		TimeElement elementAt2 = its2.getElementAt("01JAN1990 0100");
		assertTrue(elementAt.getFlag() == 0);
		assertEquals(elementAt.getX(), elementAt2.getX());
		assertEquals(elementAt.getY(), elementAt2.getY());
		assertEquals(elementAt.getFlag(), elementAt2.getFlag());
	}

	public void testWriteITSWithFlags() {
		int flagReject = FlagUtils.makeFlagValue("REJECT|nsandhu");
		int flagQuestionable = FlagUtils.makeFlagValue("QUESTIONABLE|nsandhu");
		int flagMissing = FlagUtils.makeFlagValue("MISSING|nsandhu");
		RegularTimeSeries rts = new RegularTimeSeries("/A/B/C//15MIN/F/",
				"01JAN1990 0100", "15MIN", new double[] { 1, 2, 3 });
		double stime = rts.getStartTime().getTimeInMinutes();
		DataSetAttr attr = new DataSetAttr(DataType.IRREGULAR_TIME_SERIES,
				"TIME", "CFS", "", "INST-VAL");
		IrregularTimeSeries its = new IrregularTimeSeries(
				"/A-ITS/B/C/01JAN1990/IR-HOUR/F-ITS/", new double[] { stime,
						stime + 5, stime + 11 }, new double[] { 0, 1, 7 },
				new int[] { flagReject, flagQuestionable, flagMissing }, attr);
		String pathname = "/A-ITS/B/C/01JAN1990/IR-MON/F-ITS/";
		DSSUtil.writeData(filename, pathname, its);
		DataSet dataSet = DSSUtil.readData(filename, pathname, true);
		assertNotNull(dataSet);
		assertTrue(dataSet instanceof IrregularTimeSeries);
		IrregularTimeSeries its2 = (IrregularTimeSeries) dataSet;
		TimeElement elementAt = its.getElementAt("01JAN1990 0100");
		TimeElement elementAt2 = its2.getElementAt("01JAN1990 0100");
		assertTrue(elementAt.getFlag() != 0);
		assertEquals(elementAt.getX(), elementAt2.getX());
		assertEquals(elementAt.getY(), elementAt2.getY());
		assertEquals(elementAt.getFlag(), elementAt2.getFlag());
	}
}
