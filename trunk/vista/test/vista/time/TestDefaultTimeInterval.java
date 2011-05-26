package vista.time;

import junit.framework.TestCase;

public class TestDefaultTimeInterval extends TestCase {
	public void test60minInterval(){
		Time startTime = TimeFactory.getInstance().createTime("01JAN2000 0000");
		TimeInterval ti60min = TimeFactory.getInstance().createTimeInterval("60MIN");
		long intervalInMinutes = ti60min.getIntervalInMinutes(startTime);
		assertEquals(60, intervalInMinutes);
		long exactNumberOfIntervalsTo = startTime.getExactNumberOfIntervalsTo(startTime, ti60min);
		assertEquals(0, exactNumberOfIntervalsTo);
	}
	
	public void testTimeIntervalsIn(){
		TimeFactory factory = TimeFactory.getInstance();
		TimeInterval nti = factory.createTimeInterval("1hour");
		TimeInterval oti = factory.createTimeInterval("60min");
		Time atime = factory.createTime(0);
		Time etime = factory.createTime(0);
		etime.incrementBy(nti, 1);
		long exactNumberOfIntervalsTo = atime.getExactNumberOfIntervalsTo(etime, oti);
		assertEquals(1, exactNumberOfIntervalsTo);
	}
	
	public void testIncrementMonth(){
		Time startTime = TimeFactory.getInstance().createTime("07JAN2000 2200");
		TimeInterval ti60min = TimeFactory.getInstance().createTimeInterval("5MON");
		long intervalInMinutes = ti60min.getIntervalInMinutes(startTime);
		assertEquals(60, intervalInMinutes);
		long exactNumberOfIntervalsTo = startTime.getExactNumberOfIntervalsTo(startTime, ti60min);
		assertEquals(0, exactNumberOfIntervalsTo);
	}
}
