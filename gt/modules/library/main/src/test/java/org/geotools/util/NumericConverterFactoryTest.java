package org.geotools.util;

import java.math.BigDecimal;
import java.math.BigInteger;

import junit.framework.TestCase;

public class NumericConverterFactoryTest extends TestCase {

	NumericConverterFactory factory;
	
	protected void setUp() throws Exception {
		factory = new NumericConverterFactory();
	}
	
	public void testIntegral() throws Exception {
		
		//to byte
		assertEquals( new Byte( (byte)127 ), convert( new Byte( (byte)127 ), Byte.class ) );
		assertEquals( new Byte( (byte)127 ), convert( new Short( (short)127 ), Byte.class ) );
		assertEquals( new Byte( (byte)127 ), convert( new Integer( 127 ), Byte.class ) );
		assertEquals( new Byte( (byte)127 ), convert( new Long( 127 ), Byte.class ) );
		assertEquals( new Byte( (byte)127 ), convert( BigInteger.valueOf( 127 ), Byte.class ) );
		
		//to short
		assertEquals( new Short( (short)127 ), convert( new Byte( (byte)127 ), Short.class ) );
		assertEquals( new Short( (short)127 ), convert( new Short( (short)127 ), Short.class ) );
		assertEquals( new Short( (short)127 ), convert( new Integer( 127 ), Short.class ) );
		assertEquals( new Short( (short)127 ), convert( new Long( 127 ), Short.class ) );
		assertEquals( new Short( (short)127 ), convert( BigInteger.valueOf( 127 ), Short.class ) );
		
		//to integer
		assertEquals( new Integer( 127 ), convert( new Byte( (byte)127 ), Integer.class ) );
		assertEquals( new Integer( 127 ), convert( new Short( (short)127 ), Integer.class ) );
		assertEquals( new Integer( 127 ), convert( new Integer( 127 ), Integer.class ) );
		assertEquals( new Integer( 127 ), convert( new Long( 127 ), Integer.class ) );
		assertEquals( new Integer( 127 ), convert( BigInteger.valueOf( 127 ), Integer.class ) );
		
		//to long
		assertEquals( new Long( 127 ), convert( new Byte( (byte)127 ), Long.class ) );
		assertEquals( new Long( 127 ), convert( new Short( (short)127 ), Long.class ) );
		assertEquals( new Long( 127 ), convert( new Integer( 127 ), Long.class ) );
		assertEquals( new Long( 127 ), convert( new Long( 127 ), Long.class ) );
		assertEquals( new Long( 127 ), convert( BigInteger.valueOf( 127 ), Long.class ) );
		
		//to big integer
		assertEquals( BigInteger.valueOf( 127 ), convert( new Byte( (byte)127 ), BigInteger.class ) );
		assertEquals( BigInteger.valueOf( 127 ), convert( new Short( (short)127 ), BigInteger.class ) );
		assertEquals( BigInteger.valueOf( 127 ), convert( new Integer( 127 ), BigInteger.class ) );
		assertEquals( BigInteger.valueOf( 127 ), convert( new Long( 127 ), BigInteger.class ) );
		assertEquals( BigInteger.valueOf( 127 ), convert( BigInteger.valueOf( 127 ), BigInteger.class ) );
	}
	
	public void testFloat() throws Exception {
		//to float
		assertEquals( new Float( 127.127 ), convert( new Float( 127.127 ), Float.class ) );
		assertEquals( new Float( 127.127 ), convert( new Double( 127.127 ), Float.class ) );
		assertEquals( new Float( 127.127 ), convert( new BigDecimal( 127.127 ), Float.class ) );
		
		//to double
		assertEquals( 
			new Double( 127.127 ).doubleValue(), 
			((Double)convert( new Float( 127.127 ), Double.class )).doubleValue(), 0.001 
		);
		assertEquals( new Double( 127.127 ), convert( new Double( 127.127 ), Double.class ) );
		assertEquals( new Double( 127.127 ), convert( new BigDecimal( 127.127 ), Double.class ) );
		
		//to big decimal
		assertEquals( 
			new BigDecimal( 127.127 ).doubleValue(), 
			((BigDecimal) convert( new Float( 127.127 ), BigDecimal.class )).doubleValue(), 0.001 
		);
		assertEquals( new BigDecimal( 127.127 ), convert( new Double( 127.127 ), BigDecimal.class ) );
		assertEquals( new BigDecimal( 127.127 ), convert( new BigDecimal( 127.127 ), BigDecimal.class ) );
	}
	
	public void testIntegralToFloat() throws Exception {
		assertEquals( new Float( 127.0 ), convert( new Byte( (byte)127 ), Float.class ) );
		assertEquals( new Float( 127.0 ), convert( new Short( (short)127 ), Float.class ) );
		assertEquals( new Float( 127.0 ), convert( new Integer( 127 ), Float.class ) );
		assertEquals( new Float( 127.0 ), convert( new Long( 127 ), Float.class ) );
		assertEquals( new Float( 127.0 ), convert( BigInteger.valueOf( 127 ), Float.class ) );
		
		assertEquals( new Double( 127.0 ), convert( new Byte( (byte)127 ), Double.class ) );
		assertEquals( new Double( 127.0 ), convert( new Short( (short)127 ), Double.class ) );
		assertEquals( new Double( 127.0 ), convert( new Integer( 127 ), Double.class ) );
		assertEquals( new Double( 127.0 ), convert( new Long( 127 ), Double.class ) );
		assertEquals( new Double( 127.0 ), convert( BigInteger.valueOf( 127 ), Double.class ) );
		
		assertEquals( new BigDecimal( 127.0 ), convert( new Byte( (byte)127 ), BigDecimal.class ) );
		assertEquals( new BigDecimal( 127.0 ), convert( new Short( (short)127 ), BigDecimal.class ) );
		assertEquals( new BigDecimal( 127.0 ), convert( new Integer( 127 ), BigDecimal.class ) );
		assertEquals( new BigDecimal( 127.0 ), convert( new Long( 127 ), BigDecimal.class ) );
		assertEquals( new BigDecimal( 127.0 ), convert( BigInteger.valueOf( 127 ), BigDecimal.class ) );
	}
	
	public void testFloatToIntegral() throws Exception {
		//to byte
		assertEquals( new Byte( (byte)127 ), convert( new Float( 127.127 ), Byte.class ) );
		assertEquals( new Byte( (byte)127 ), convert( new Double( 127.127 ), Byte.class ) );
		assertEquals( new Byte( (byte)127 ), convert( new BigDecimal( 127.127 ), Byte.class ) );
		
		//to short
		assertEquals( new Short( (short)127 ), convert( new Float( 127.127 ), Short.class ) );
		assertEquals( new Short( (short)127 ), convert( new Double( 127.127 ), Short.class ) );
		assertEquals( new Short( (short)127 ), convert( new BigDecimal( 127.127 ), Short.class ) );
		
		//to integer
		assertEquals( new Integer( 127 ), convert( new Float( 127.127 ), Integer.class ) );
		assertEquals( new Integer( 127 ), convert( new Double( 127.127 ), Integer.class ) );
		assertEquals( new Integer( 127 ), convert( new BigDecimal( 127.127 ), Integer.class ) );
		
		//to long
		assertEquals( new Long( 127 ), convert( new Float( 127.127 ), Long.class ) );
		assertEquals( new Long( 127 ), convert( new Double( 127.127 ), Long.class ) );
		assertEquals( new Long( 127 ), convert( new BigDecimal( 127.127 ), Long.class ) );
		
		//to big integer
		assertEquals( BigInteger.valueOf( 127 ), convert( new Float( 127.127 ), BigInteger.class ) );
		assertEquals( BigInteger.valueOf( 127 ), convert( new Double( 127.127 ), BigInteger.class ) );
		assertEquals( BigInteger.valueOf( 127 ), convert( new BigDecimal( 127.127 ), BigInteger.class ) );
		
	}
	
	Object convert( Object source, Class target ) throws Exception {
		return factory.createConverter( source.getClass(), target, null ).convert( source, target );
	}
}
