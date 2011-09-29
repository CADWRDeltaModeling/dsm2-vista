package vista.set;

import vista.time.TimeFactory;
import junit.framework.TestCase;

public class TestPeriodMinMaxIrregularTS extends TestCase{
	public void testRegularTSMin(){
		RegularTimeSeries rts = new RegularTimeSeries("testMin", "01JAN1990 0100", "15MIN", new double[]{1.1,3.3,2.4,3.1,3.2,4.5,6.7,1.1});
		IrregularTimeSeries itshour = TimeSeriesMath.getPeriodMinMax(rts, TimeFactory.getInstance().createTimeInterval("1HOUR"), TimeSeriesMath.PERIOD_MIN);
		IrregularTimeSeries itsday = TimeSeriesMath.getPeriodMinMax(rts, TimeFactory.getInstance().createTimeInterval("1DAY"), TimeSeriesMath.PERIOD_MIN);
		System.out.println("itsday:"+itsday);
	}
	
	public void testRegularTSMax(){
		RegularTimeSeries rts = new RegularTimeSeries("testMin", "01JAN1990 0100", "15MIN", new double[]{1.1,3.3,2.4,3.1,3.2,4.5,6.7,1.1});
		IrregularTimeSeries itshour = TimeSeriesMath.getPeriodMinMax(rts, TimeFactory.getInstance().createTimeInterval("1HOUR"), TimeSeriesMath.PERIOD_MAX);
		IrregularTimeSeries itsday = TimeSeriesMath.getPeriodMinMax(rts, TimeFactory.getInstance().createTimeInterval("1DAY"), TimeSeriesMath.PERIOD_MAX);
		System.out.println("itsday:"+itsday);
	}
}
