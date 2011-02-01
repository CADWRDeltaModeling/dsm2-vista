package vista.set;

import junit.framework.TestCase;
import vista.time.TimeFactory;
import vista.time.TimeWindow;

public class TestPadTimeSeries extends TestCase{
	public void testPadRTSNonOverlappingTimeWindow(){
		RegularTimeSeries rts = new RegularTimeSeries("test", "01JAN1990 0100", "1HOUR", new double[]{1,2,3,4,5,6,7,8,9});
		TimeWindow tw = TimeFactory.getInstance().createTimeWindow("01JAN1990 1300 - 02JAN1990 0500");
		TimeSeries pad = TimeSeriesMergeUtils.pad(rts, tw);
		assertTrue(pad instanceof RegularTimeSeries);
		TimeWindow ptw = pad.getTimeWindow();
		assertEquals(tw, ptw);
	}
	
	public void testPadRTSOverlappingTimeWindow(){
		RegularTimeSeries rts = new RegularTimeSeries("test", "01JAN1990 0100", "1HOUR", new double[]{1,2,3,4,5,6,7,8,9});
		TimeWindow tw = TimeFactory.getInstance().createTimeWindow("01JAN1990 0500 - 02JAN1990 0500");
		TimeSeries pad = TimeSeriesMergeUtils.pad(rts, tw);
		assertTrue(pad instanceof RegularTimeSeries);
		TimeWindow ptw = pad.getTimeWindow();
		assertEquals(tw, ptw);
	}
	
	public void testPadITSNonOverlappingTimeWindow(){
		
	}
	
	public void testPadITSOverlappingTimeWindow(){
		
	}
}
