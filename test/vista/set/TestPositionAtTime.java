package vista.set;

import vista.time.TimeFactory;
import junit.framework.TestCase;

public class TestPositionAtTime extends TestCase {
	IrregularTimeSeries its;
	RegularTimeSeries ts;
	RegularTimeSeries tsmon;
	protected void setUp() {
		TimeFactory f= TimeFactory.getInstance();
		its = new IrregularTimeSeries("its1",
				new double[] { 0, 10, 20, 30, 40, 50 }, new double[] { 10.0, 11.0,
						12.0, 13.0, 14.0, 15.0 });
		ts = new RegularTimeSeries("rts1", f.createTime(0), f.createTimeInterval(
				"10MIN"), new double[] { 22, 23, 24, 25, 26, 27 }, null, null);
		tsmon = new RegularTimeSeries("rts monthly", f.createTime("30SEP1926 2400"),
				f.createTimeInterval("1MON"), new double[]{ 300, 400, 450, 500, 550, 600}, null, null);
	}

	public void testPositionAtRegular() {
		checkThis(ts);

	}

	public void testPositionAtIrregular() {
		checkThis(its);
	}

	private void checkThis(TimeSeries ts) {
		DataSetIterator iterator = its.getIterator();
		assertNotNull(iterator);
		assertTrue(iterator instanceof TimeSeriesIterator);
		TimeSeriesIterator tsi = (TimeSeriesIterator) iterator;
		assertTrue(tsi.atStart());
		assertEquals(0, tsi.getUnderlyingIndex());
		// Retrieving elements on time series should not affect this iterator
		DataSetElement el3 = its.getElementAt(3);
		assertTrue(tsi.atStart());
		// positioning by index should work
		tsi.positionAtIndex(2);
		assertEquals(2, tsi.getUnderlyingIndex());
		assertTrue(!tsi.atStart());
		// position by time between (moving upwards)
		tsi.positionAtTime(TimeFactory.getInstance().createTime(4));
		assertEquals(0, tsi.getUnderlyingIndex());
		tsi.positionAtTime(TimeFactory.getInstance().createTime(13));
		assertEquals(1, tsi.getUnderlyingIndex());
		tsi.positionAtTime(TimeFactory.getInstance().createTime(23));
		assertEquals(2, tsi.getUnderlyingIndex());
		tsi.positionAtTime(TimeFactory.getInstance().createTime(39));
		assertEquals(3, tsi.getUnderlyingIndex());
		// position by time exact (moving upwards)
		tsi.positionAtTime(TimeFactory.getInstance().createTime(40));
		assertEquals(4, tsi.getUnderlyingIndex());
		// position by time approx (moving downwards)
		tsi.positionAtTime(TimeFactory.getInstance().createTime(32));
		assertEquals(3, tsi.getUnderlyingIndex());
		//
		tsi.positionAtTime(TimeFactory.getInstance().createTime(28));
		assertEquals(2, tsi.getUnderlyingIndex());
		//
		tsi.positionAtTime(TimeFactory.getInstance().createTime(10));
		assertEquals(1, tsi.getUnderlyingIndex());

	}
	
	public void testMonthlyPosition(){
		String tmstr = "30SEP1926 2400";
		TimeElement sepValue = tsmon.findElementAt(tmstr);
		assertEquals(tmstr, sepValue.getXString());
		tmstr="31OCT1926 2400";
		sepValue = tsmon.findElementAt(tmstr);
		assertEquals(tmstr, sepValue.getXString());
	}
}
