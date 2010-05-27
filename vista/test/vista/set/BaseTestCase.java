package vista.set;

import vista.db.dss.DSSUtil;
import vista.time.Time;
import vista.time.TimeFactory;
import vista.time.TimeInterval;
import junit.framework.TestCase;

public class BaseTestCase extends TestCase {

	protected RegularTimeSeries rts;
	protected DataReference ref;
	protected RegularTimeSeries rts_missing_values;
	protected DataReference ref_missing_values;
	protected DataReference ref_reject_flags;
	protected RegularTimeSeries rts_non_overlapping;
	protected DataReference ref_non_overlapping;

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
		// non - overlapping values are an order different
		double[] yvals_non_overlap = new double[yvals.length];
		for (int i = 0; i < yvals.length; i++) {
			yvals_non_overlap[i] = yvals[i] * 10;
		}
		rts_non_overlapping = new RegularTimeSeries("rts_non_overlapping",
				timeFactory.createTime("01JUL2010 0000"), timeInterval,
				yvals_non_overlap, flags, null);
		ref_non_overlapping = DSSUtil.createDataReference("local",
				"d:/temp.dss", "/TEST/RTS_NON_OVERLAPPING/FLOW/"
						+ rts_non_overlapping.getTimeWindow().toString() + "/"
						+ rts_non_overlapping.getTimeInterval()
						+ "/NON_OVERLAPPING", rts_non_overlapping);
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

	public static double TOLERANCE = 0.00000001;

	public static void assertEqualsApprox(double expected, double actual) {
		assertTrue(Math.abs(expected - actual) <= TOLERANCE);
	}

}
