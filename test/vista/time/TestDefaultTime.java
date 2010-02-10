package vista.time;

import junit.framework.TestCase;

public class TestDefaultTime extends TestCase {
	public void testGetNumberOfIntervalsTo(){
		TimeFactory tf = TimeFactory.getInstance();
		Time startTime = tf.createTime("01JAN2000 0000");
		Time endTime = tf.createTime("01JAN2000 2400");
		TimeInterval ti = tf.createTimeInterval("15MIN");
		long numberOfIntervalsTo = startTime.getNumberOfIntervalsTo(endTime, ti);
		assertEquals(96, numberOfIntervalsTo);
		long numberOfMinutesTo = startTime.getNumberOfMinutesTo(endTime);
		assertEquals(1440, numberOfMinutesTo);
	}
}
