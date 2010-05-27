package vista.set;


public class TestMovingAverageProxy extends BaseTestCase {

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

}
