package org.geotools.xs.bindings;

import javax.xml.namespace.QName;

import org.geotools.xml.ElementInstance;
import org.geotools.xs.TestSchema;
import org.geotools.xs.bindings.XS;

public class XSDoubleStrategyTest extends TestSchema {

	/**
	 * For example, -1E4, 1267.43233E12, 12.78e-2, 12 , -0, 0 and INF are all legal literals for double.
	 * 
	 */
	
	/*
	 * Test method for 'org.geotools.xml.strategies.xs.XSDoubleStrategy.parse(Element, Node[], Object)'
	 */
	public void testParse() throws Exception {
		validateValues("-1E4", new Double(-1E4));
		validateValues("1267.43233E12", new Double(1267.43233E12));
		validateValues("12.78e-2", new Double(12.78e-2));
		validateValues("12", new Double(12));
		validateValues("-0", new Double("-0"));
		validateValues("0", new Double(0));
		ElementInstance element = element( "INF", XS.DOUBLE );
		assertEquals( new Double(Double.POSITIVE_INFINITY), strategy.parse( element, "INF" ) );

	}
	
	public void testIntegerParse() throws Exception {
		ElementInstance element = element( "12345", XS.INTEGER );
		assertEquals( new Double(12345.0), strategy.parse( element, "12345" ) );
	}

	
	protected QName getQName() {
		return XS.DOUBLE;
	}

}
