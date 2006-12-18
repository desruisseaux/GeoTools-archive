package org.geotools.util;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.geotools.factory.Hints;

import junit.framework.TestCase;

public class TemporalConverterFactoryTest extends TestCase {

	TemporalConverterFactory factory;
	
	protected void setUp() throws Exception {
		factory = new TemporalConverterFactory();
	}
	
	public void testCalendarToDate() throws Exception {
		Calendar calendar = Calendar.getInstance();
		assertNotNull( factory.createConverter( Calendar.class, Date.class, null ) );
		
		Date date = (Date) factory.createConverter( Calendar.class, Date.class, null )
			.convert( calendar, Date.class );
		assertNotNull( date );
		assertEquals( calendar.getTime(), date );
	}
	
	public void testCalendarToTime() throws Exception {
		Calendar calendar = Calendar.getInstance();
		assertNotNull( factory.createConverter( Calendar.class, Time.class, null ) );
		
		Time time = (Time) factory.createConverter( Calendar.class, Time.class, null )
			.convert( calendar, Time.class );
		assertNotNull( time );
		assertEquals( new Time( calendar.getTime().getTime() ), time );
	}
	
	public void testCalendarToTimestamp() throws Exception {
		Calendar calendar = Calendar.getInstance();
		assertNotNull( factory.createConverter( Calendar.class, Timestamp.class, null ) );
		
		Timestamp timeStamp = (Timestamp) factory.createConverter( Calendar.class, Timestamp.class, null )
			.convert( calendar, Timestamp.class );
		assertNotNull( timeStamp );
		assertEquals( new Timestamp( calendar.getTime().getTime() ), timeStamp );
	}
	
	public void testDateToCalendar() throws Exception {
		Date date = new Date();
		assertNotNull( factory.createConverter(  Date.class, Calendar.class, null ) );
		
		Calendar calendar = (Calendar) factory.createConverter( Date.class, Calendar.class, null )
			.convert( date, Calendar.class );
		assertNotNull( calendar );
		assertEquals( date, calendar.getTime() );
	}
	
	public void testDateToTime() throws Exception {
		Date date = new Date();
		assertNotNull( factory.createConverter(  Date.class, Time.class, null ) );
		
		Time time = (Time) factory.createConverter( Date.class, Time.class, null )
			.convert( date, Time.class );
		assertNotNull( time );
		assertEquals( new Time( date.getTime() ), time );
	}
	
	public void testDateToTimestamp() throws Exception {
		Date date = new Date();
		assertNotNull( factory.createConverter(  Date.class, Timestamp.class, null ) );
		
		Timestamp timeStamp = (Timestamp) factory.createConverter( Date.class, Timestamp.class, null )
			.convert( date, Timestamp.class );
		assertNotNull( timeStamp );
		assertEquals( new Timestamp( date.getTime() ), timeStamp );
	}
	
	public void testTimeToCalendar() throws Exception {
		Time time = new Time(new Date().getTime());
		assertNotNull( factory.createConverter(  Time.class, Calendar.class, null ) );
		
		Calendar calendar = (Calendar) factory.createConverter( Time.class, Calendar.class, null )
			.convert( time, Calendar.class );
		assertNotNull( calendar );
		assertEquals( time, new Time( calendar.getTime().getTime() ) );
	}
	
	public void testTimestampToCalendar() throws Exception {
		Timestamp timeStamp = new Timestamp( new Date().getTime() );
		assertNotNull( factory.createConverter(  Timestamp.class, Calendar.class, null ) );
		
		Calendar calendar = (Calendar) factory.createConverter( Timestamp.class, Calendar.class, null )
			.convert( timeStamp, Calendar.class );
		assertNotNull( calendar );
		assertEquals( timeStamp, new Timestamp( calendar.getTime().getTime() ) );
	}
	
	
	
}
