package org.geotools.util;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

public class TemporalConverterFactoryTest extends TestCase {

	TemporalConverterFactory factory;
	
	protected void setUp() throws Exception {
		factory = new TemporalConverterFactory();
	}
	
	public void testToDate() throws Exception {
		Calendar calendar = Calendar.getInstance();
		assertNotNull( factory.createConverter( Calendar.class, Date.class, null ) );
		
		Date date = (Date) factory.createConverter( Calendar.class, Date.class, null )
			.convert( calendar, Date.class );
		assertNotNull( date );
		assertEquals( calendar.getTime(), date );
	}
	
	public void testToCalendar() throws Exception {
		Date date = new Date();
		assertNotNull( factory.createConverter(  Date.class, Calendar.class, null ) );
		
		Calendar calendar = (Calendar) factory.createConverter( Date.class, Calendar.class, null )
			.convert( date, Calendar.class );
		assertNotNull( calendar );
		assertEquals( date, calendar.getTime() );
	}
}
