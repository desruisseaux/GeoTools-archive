package org.geotools.xs.bindings;

import java.math.BigInteger;

import javax.xml.bind.ValidationException;
import javax.xml.namespace.QName;

import org.geotools.xs.TestSchema;
import org.geotools.xs.bindings.XS;

public class XSNonNegativeIntegerStrategyTest extends TestSchema {

	public void validateValues(String text, Number expected) throws Exception {
		Object value = new BigInteger( text.trim() );
		
		Object result = strategy.parse( element(text, qname), value );
		if( !(result instanceof BigInteger) && result instanceof Number ){
			result = BigInteger.valueOf( ((Number)result).longValue() );
		}
		assertEquals( integer(expected), integer(result) );
	}
	
	public BigInteger integer( Object value ){
		return value instanceof BigInteger
				? ((BigInteger) value )
						: BigInteger.valueOf( ((Number)value).longValue() );
		
	}
	public Number number( String number ){
		return BigInteger.valueOf( Integer.valueOf(number).longValue() );
	}
	/*
	 * Test method for 'org.geotools.xml.strategies.xs.XSNonPositiveIntegerStrategy.parse(Element, Node[], Object)'
	 */
	public void testNegativeOne() throws Exception {
		try {
         validateValues("-1", number("-1") );
      } catch (ValidationException e) {
         // yeah!
      }
	}
   
	public void testZero() throws Exception {
	   validateValues("0", number("0"));
	}
   
	public void testLargePositiveNumber() throws Exception{
		validateValues("12678967543233", new BigInteger("12678967543233"));
	}
   
	public void testPositiveNumber() throws Exception{
		validateValues("1000", new Integer("1000"));
	}

	protected QName getQName() {
		return XS.NONNEGATIVEINTEGER;
	}

}
