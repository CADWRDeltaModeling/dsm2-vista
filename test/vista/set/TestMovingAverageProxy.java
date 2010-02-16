package vista.set;

import vista.db.dss.DSSUtil;
import vista.time.Time;
import vista.time.TimeFactory;
import vista.time.TimeInterval;
import junit.framework.TestCase;

public class TestMovingAverageProxy extends TestCase {

	private RegularTimeSeries rts;
	private DataReference ref;
	private RegularTimeSeries rts_missing_values;
	private DataReference ref_missing_values;
	private DataReference ref_reject_flags;

	protected void setUp() {
		TimeFactory timeFactory = TimeFactory.getInstance();
		Time startTime = timeFactory.createTime("01JUL2009 0000");
		TimeInterval timeInterval = timeFactory.createTimeInterval("15MIN");
		double[] yvals = new double[] { 1.0, 1.5, 2.0, 2.5, 3.0, 3.5 };
		int[] flags = new int[yvals.length];
		rts = new RegularTimeSeries("rts1", startTime, timeInterval, yvals,
				flags, null);
		ref = DSSUtil.createDataReference("local", "d:/temp.dss",
				"/TEST/RTS1/FLOW/" + rts.getTimeWindow().toString() + "/"
						+ rts.getTimeInterval() + "/MOVINGAV", rts);
		double[] yvals_missing = new double[] { 1.0, Constants.MISSING_VALUE,
				2.0, 2.5, Constants.MISSING_VALUE, 3.5 };
		int[] flags_missing = new int[yvals_missing.length];
		rts_missing_values = new RegularTimeSeries("rts_missing_values",
				startTime, timeInterval, yvals_missing, flags_missing, null);
		ref_missing_values = DSSUtil.createDataReference("local",
				"d:/temp.dss", "/TEST/RTS_MISSING_VALUES/FLOW/"
						+ rts_missing_values.getTimeWindow().toString() + "/"
						+ rts_missing_values.getTimeInterval() + "/MOVINGAV",
				rts_missing_values);
		int flag = 0;
		flag = FlagUtils.setBit(flag, FlagUtils.SCREENED_BIT);
		flag = FlagUtils.setBit(flag, FlagUtils.REJECT_BIT);
		flag = FlagUtils.setUserId(flag, 2);
		int[] flags_with_reject = new int[] { 0, flag, 0, 0, flag, 0 };
		RegularTimeSeries rts_reject_flags = new RegularTimeSeries(
				"rts_reject_flags", startTime, timeInterval, yvals,
				flags_with_reject, null);
		ref_reject_flags = DSSUtil.createDataReference("local", "d:/temp.dss",
				"/TEST/RTS_REJECT_FLAGS/FLOW/"
						+ rts_reject_flags.getTimeWindow().toString() + "/"
						+ rts_reject_flags.getTimeInterval() + "/MOVINGAV",
				rts_reject_flags);
	}

	public void testMovingAverage() {
		MovingAverageProxy mavg00 = new MovingAverageProxy(ref, 0, 0);
		RegularTimeSeries ds00 = (RegularTimeSeries) mavg00.getData();
		for (DataSetIterator iterator = ds00.getIterator(), iterator2 = rts
				.getIterator(); !(iterator.atEnd() && iterator2.atEnd()); iterator
				.advance(), iterator2.advance()) {
			DataSetElement dse = iterator.getElement();
			DataSetElement dse2 = iterator2.getElement();
			assertEqualsApprox(dse.getY(), dse2.getY());
		}
		MovingAverageProxy mavg01 = new MovingAverageProxy(ref, 0, 1);
		RegularTimeSeries ds01 = (RegularTimeSeries) mavg01.getData();
		for (int i = 0; i < rts.size() - 1; i++) {
			double y = ds01.getElementAt(i).getY();
			double ycalc = rts.getElementAt(i).getY()
					+ rts.getElementAt(i + 1).getY();
			ycalc = ycalc / 2;
			assertEqualsApprox(ycalc, y);
		}
	}

	public void testMovingAverageWithMissing() {
		MovingAverageProxy mavg01 = new MovingAverageProxy(ref_missing_values,
				0, 1);
		RegularTimeSeries ds01 = (RegularTimeSeries) mavg01.getData();
		for (int i = 0; i < rts.size() - 1; i++) {
			double y = ds01.getElementAt(i).getY();
			if (i == 2) {
				double ycalc = rts.getElementAt(i).getY()
						+ rts.getElementAt(i + 1).getY();
				ycalc = ycalc / 2;
				assertEqualsApprox(ycalc, y);
			} else {
				assertEquals(Constants.MISSING_VALUE, y);
			}
		}
	}

	public void testMovingAverageWithRejectFlags() {
		MovingAverageProxy mavg01 = new MovingAverageProxy(ref_reject_flags, 0,
				1);
		RegularTimeSeries ds01 = (RegularTimeSeries) mavg01.getData();
		for (int i = 0; i < rts.size() - 1; i++) {
			double y = ds01.getElementAt(i).getY();
			if (i == 2) {
				double ycalc = rts.getElementAt(i).getY()
						+ rts.getElementAt(i + 1).getY();
				ycalc = ycalc / 2;
				assertEqualsApprox(ycalc, y);
			} else {
				/*
				 * FIXME: keep those flags when doing operations??
				int flag = ds01.getElementAt(i).getFlag();
				assertTrue(FlagUtils.isBitSet(flag, FlagUtils.REJECT_BIT));
				System.out.println(ds01.getElementAt(i));
								 */
				assertEquals(Constants.MISSING_VALUE, y);
			}
		}
	}

	// FIXME: move elsewhere
	public static double TOLERANCE = 0.00000001;

	public static void assertEqualsApprox(double expected, double actual) {
		assertTrue(Math.abs(expected - actual) <= TOLERANCE);
	}

}
