package vista.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
	
	public void testGetNumberOfIntervalsTo2(){
		TimeFactory tf = TimeFactory.getInstance();
		Time startTime = tf.createTime("28OCT1990 0015");
		TimeInterval ti = tf.createTimeInterval("15MIN");
		TimeInterval tiday = tf.createTimeInterval("1DAY");
		Time ceiling = startTime.ceiling(tiday);
		long intervalsTo = startTime.getExactNumberOfIntervalsTo(ceiling, ti);
		assertEquals(intervalsTo,95);
	}
	
	public void testGetDate(){
		TimeFactory tf = TimeFactory.getInstance();
		Time t1 = tf.createTime("01JAN1980 0100");
		Date d1 = t1.getDate();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d1);
		assertEquals(1980,calendar.get(Calendar.YEAR));
		assertEquals(1,calendar.get(Calendar.DATE));
		assertEquals(Calendar.JANUARY, calendar.get(Calendar.MONTH));
		assertEquals(1,calendar.get(Calendar.HOUR_OF_DAY));
		//
		Time t2 = tf.createTime("29FEB1984 2355");
		Date d2 = t2.getDate();
		calendar.setTime(d2);
		assertEquals(1984,calendar.get(Calendar.YEAR));
		assertEquals(29,calendar.get(Calendar.DATE));
		assertEquals(Calendar.FEBRUARY, calendar.get(Calendar.MONTH));
		assertEquals(23,calendar.get(Calendar.HOUR_OF_DAY));
		assertEquals(55, calendar.get(Calendar.MINUTE));
	}
	
	public void testSetDate() throws ParseException{
		TimeFactory tf = TimeFactory.getInstance();
		String timeAsString = "01JAN2010 0100";
		Time t1 = tf.createTime(timeAsString);
		//
		Date d1 = t1.getDate();
		System.out.println("Time is "+t1);
		t1.setDate(d1);
		System.out.println("Time is "+t1);
		Date d2 = new SimpleDateFormat("ddMMMyyyy HHmm").parse(timeAsString);
		System.out.println("Date is "+d2);
		t1.setDate(d2);
		System.out.println("Time is "+t1);
	}
}
