/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.util;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

import org.geotools.factory.Hints;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

import junit.framework.TestCase;

public class TemporalConverterFactoryTest extends TestCase {

	TemporalConverterFactory factory;
	
	protected void setUp() throws Exception {
		factory = new TemporalConverterFactory();
	}

	/**
	 * When converting from Calendar to Date from ArcSDE we run into a problem
	 * where the Dates are out by a very small number. Basically we need to 
	 * look at the Calendar and see if it represents an *entire* day.
	 * 
	 * @throws Exception
	 */
    public void testStitchInTime() throws Exception {
        Converter converter = factory.createConverter( Calendar.class, Date.class, null );
        
        Calendar calendar = Calendar.getInstance();
        
        // Year, month, date, hour, minute, second.
        calendar.set(2004, 06, 1);
        for( int i=1; i<=12;i++){
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            Date date = converter.convert( calendar, Date.class );
            pause();
            assertNotNull( date );
            assertEquals( calendar.getTime(), date );            
        }        
        calendar.set(2004, 06, 1, 12, 30 );
        Date date = converter.convert( calendar, Date.class );
        pause();
        assertNotNull( date );
        assertEquals( calendar.getTime(), date );
    }
    
    /** Pause for one tick of the clock ... */
    public static void pause(){
        long pause = System.currentTimeMillis() + 15; // 15 is about the resolution of a system clock
        while( System.currentTimeMillis() < pause ){
            Thread.yield();
        }
    }
    
	public void testCalendarToDate() throws Exception {
		Calendar calendar = Calendar.getInstance();
		assertNotNull( factory.createConverter( Calendar.class, Date.class, null ) );
		
		Date date = (Date) factory.createConverter( Calendar.class, Date.class, null )
			.convert( calendar, Date.class );
		assertNotNull( date );
		assertEquals( calendar.getTime(), date );
	}
	// java.util.GregorianCalendar[time=?,areFieldsSet=false,areAllFieldsSet=true,lenient=true,zone=sun.util.calendar.ZoneInfo[id="America/Los_Angeles",offset=-28800000,dstSavings=3600000,useDaylight=true,transitions=185,lastRule=java.util.SimpleTimeZone[id=America/Los_Angeles,offset=-28800000,dstSavings=3600000,useDaylight=true,startYear=0,startMode=3,startMonth=2,startDay=8,startDayOfWeek=1,startTime=7200000,startTimeMode=0,endMode=3,endMonth=10,endDay=1,endDayOfWeek=1,endTime=7200000,endTimeMode=0]],firstDayOfWeek=1,minimalDaysInFirstWeek=1,ERA=1,YEAR=2004,MONTH=6,WEEK_OF_YEAR=17,WEEK_OF_MONTH=4,DAY_OF_MONTH=6,DAY_OF_YEAR=116,DAY_OF_WEEK=6,DAY_OF_WEEK_IN_MONTH=4,AM_PM=1,HOUR=3,HOUR_OF_DAY=0,MINUTE=0,SECOND=0,MILLISECOND=468,ZONE_OFFSET=-28800000,DST_OFFSET=3600000]
	// cachedFixedDate 733157
	// 
	
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
	
	public void testXMLGregorianCalendarToCalendar() throws Exception {
	    XMLGregorianCalendar gc = XMLGregorianCalendarImpl.parse( "1981-06-20T12:00:00");
	    assertNotNull( factory.createConverter( XMLGregorianCalendar.class, Calendar.class, null));
	    
	    Calendar calendar = (Calendar) factory.createConverter( XMLGregorianCalendar.class, Calendar.class, null)
	        .convert( gc, Calendar.class );
	    assertNotNull(calendar);
	    
	    assertEquals( 1981, calendar.get( Calendar.YEAR ) );
	    assertEquals( 5, calendar.get( Calendar.MONTH ) );
	    assertEquals( 20, calendar.get( Calendar.DATE ) );
	    assertEquals( 12, calendar.get( Calendar.HOUR_OF_DAY ) );
	    assertEquals( 0, calendar.get( Calendar.MINUTE ) );
	    assertEquals( 0, calendar.get( Calendar.SECOND ) );
	}
	
}
