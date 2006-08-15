package org.geotools.xs.bindings;

import java.math.BigInteger;

import javax.xml.namespace.QName;

import org.geotools.xs.TestSchema;
import org.geotools.xs.bindings.XS;

public class XSIntegerStrategyTest extends TestSchema {

	/**
	 * integer has a lexical representation consisting of a finite-length 
	 * sequence of decimal digits (#x30-#x39) with an optional leading sign. 
	 * If the sign is omitted, "+" is assumed. 
	 * 
	 * For example: -1, 0, 12678967543233, +100000.
	 */
	
	public void testParse() throws Exception {
		validateValues("-1", new BigInteger("-1"));
		validateValues("0", new BigInteger("0"));
		validateValues("12678967543233", new BigInteger("12678967543233"));
		validateValues("+100000", new BigInteger("100000"));
	}
	protected QName getQName() {
		return XS.INTEGER;
	}

}
