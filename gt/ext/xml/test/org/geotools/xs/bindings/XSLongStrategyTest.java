package org.geotools.xs.bindings;

import javax.xml.namespace.QName;

import org.geotools.xs.TestSchema;
import org.geotools.xs.bindings.XS;

public class XSLongStrategyTest extends TestSchema {

	/**
	 * long has a lexical representation consisting of an 
	 * optional sign followed by a finite-length sequence 
	 * of decimal digits (#x30-#x39). If the sign is omitted, 
	 * "+" is assumed. 
	 * 
	 * For example: -1, 0, 12678967543233, +100000.
	 * @throws Exception 
	 *
	 */
	
	/*
	 * Test method for 'org.geotools.xml.strategies.xs.XSLongStrategy.parse(Element, Node[], Object)'
	 */
	public void testParse() throws Exception {
		validateValues("-1", new Long(-1));
		validateValues("0", new Long(0));
		validateValues("12678967543233", new Long(12678967543233L));
		validateValues("+100000", new Long(100000));
	}

	protected QName getQName() {
		return XS.LONG;
	}

}
