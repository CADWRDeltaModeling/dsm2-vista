package vista.set;

import junit.framework.TestCase;
import vista.time.Time;
import vista.time.TimeFactory;

public class TestIrregularToRegular extends TestCase{
	public void testIrregularToRegular(){
		String[] tsa = new String[]{"01JAN2010 0100", "01JAN2010 0315", "01JAN2010 0330", "01JAN2010 0349"};
		Time[] times = new Time[tsa.length];
		double [] values = new double[tsa.length];
		for(int i=0; i < tsa.length; i++){
			times[i] = TimeFactory.getInstance().createTime(tsa[i]);
			values[i] = Math.sin(i*Math.PI/tsa.length);
		}
		IrregularTimeSeries its = new IrregularTimeSeries("/ITS/LOC/TYPE//IR-DAY/TEST/",times, values);
		RegularTimeSeries snapNearest = TimeSeriesMath.snap(its, TimeFactory.getInstance().createTimeInterval("15MIN"), TimeSeriesMath.SNAP_NEAREST);
		assertNotNull(snapNearest);
		//
		
	}
}
