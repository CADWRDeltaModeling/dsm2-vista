package vista.set;

import junit.framework.TestCase;
import vista.app.MultiDataTableFrame;

public class TestTimeSeriesMergeUtils extends TestCase {

	public void testMergeRTS() {
		double[] values1 = new double[100];
		for (int i = 0; i < values1.length; i++) {
			values1[i] = i;
		}
		int[] flags1 = new int[100];
		for (int i = 0; i < flags1.length; i++) {
			flags1[i] = 0;
		}
		DataSetAttr attr1 = new DataSetAttr(DataType.REGULAR_TIME_SERIES,
				"TIME", "XX", "", "INST-VAL");
		RegularTimeSeries rts1 = new RegularTimeSeries("original",
				"01JAN1990 0100", "15MIN", values1, flags1, attr1);
		double[] values2 = new double[100];
		for (int i = 0; i < values2.length; i++) {
			values2[i] = 1000+i;
		}
		int[] flags2 = new int[100];
		for (int i = 0; i < flags2.length; i++) {
			if (i < 20) {
				flags2[i] = FlagUtils.setFlagTypeAndUserId(flags2[i],
						FlagUtils.OK_FLAG, 0);
			} else if (i < 40) {
				flags2[i] = FlagUtils.setFlagTypeAndUserId(flags2[i],
						FlagUtils.QUESTIONABLE_FLAG, 0);
			} else if (i < 60) {
				flags2[i] = FlagUtils.setFlagTypeAndUserId(flags2[i],
						FlagUtils.REJECT_FLAG, 0);
			} else if (i < 80) {
				flags2[i] = 0;
			}
		}
		DataSetAttr attr2 = new DataSetAttr(DataType.REGULAR_TIME_SERIES,
				"TIME", "XX", "", "INST-VAL");
		RegularTimeSeries rts2 = new RegularTimeSeries("replacer",
				"01JAN1990 0500", "15MIN", values2, flags2, attr2);
		TimeSeries merge = TimeSeriesMergeUtils.merge(new TimeSeries[]{rts1,rts2}, rts1.getTimeWindow().union(rts2.getTimeWindow()));
		MultiDataTableFrame table = new MultiDataTableFrame(new DataReference[]{new DefaultReference(rts1), new DefaultReference(rts2), new DefaultReference(merge)});
		table.show();
	}

	public void testReplaceRTS() {
		double[] values = new double[100];
		for (int i = 0; i < values.length; i++) {
			values[i] = i;
		}
		RegularTimeSeries rts1 = new RegularTimeSeries("original",
				"01JAN1990 0100", "15MIN", values);
		RegularTimeSeries rts2 = new RegularTimeSeries("replacer",
				"01JAN1990 0500", "15MIN", new double[] { 101, 102, 103 });
		TimeSeries replaced = TimeSeriesMergeUtils.replace(rts1, rts2);
		assertEquals(rts1.getTimeWindow(), replaced.getTimeWindow());
		assertApproxEquals(101, replaced.getElementAt("01JAN1990 0500").getY());
		assertApproxEquals(102, replaced.getElementAt("01JAN1990 0515").getY());
		assertApproxEquals(103, replaced.getElementAt("01JAN1990 0530").getY());
		RegularTimeSeries rts3 = new RegularTimeSeries("replacer outside tw",
				"01JAN2000 0100", "15MIN", new double[] { 101, 102, 103 });
		replaced = TimeSeriesMergeUtils.replace(rts1, rts3);
		assertEquals(rts1.getTimeWindow().union(rts3.getTimeWindow()), replaced
				.getTimeWindow());
	}
	
	public void testReplaceInPlace(){
		RegularTimeSeries series1 = new RegularTimeSeries("series1","01JAN1990 0100", "1HOUR", new double[]{5, 10, 15, Constants.MISSING_VALUE});
		RegularTimeSeries series2 = new RegularTimeSeries("series2","01JAN1990 0200", "1HOUR", new double[]{-5, -10, -15});
		assertApproxEquals(5, series1.getElementAt(0).getY());
		assertApproxEquals(10, series1.getElementAt(1).getY());
		assertApproxEquals(Constants.MISSING_VALUE, series1.getElementAt(3).getY());
		TimeSeriesMergeUtils.replaceInPlace(series1, series2);
		assertApproxEquals(5, series1.getElementAt(0).getY());
		assertApproxEquals(-5, series1.getElementAt(1).getY());
		assertApproxEquals(-15, series1.getElementAt(3).getY());
	}

	public static void assertApproxEquals(double expected, double actual) {
		assertTrue(Math.abs(expected - actual) < 1e-6);
	}
}
