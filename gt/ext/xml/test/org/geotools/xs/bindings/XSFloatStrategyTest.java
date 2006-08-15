package org.geotools.xs.bindings;

import javax.xml.namespace.QName;

import org.geotools.xs.TestSchema;
import org.geotools.xs.bindings.XS;

public class XSFloatStrategyTest extends TestSchema {
    
	public void validateValues(String given, Float expected) throws Exception {
		Float actual = (Float) strategy.parse( element(given, qname), given );
		assertEquals( given, expected, actual);
	}
    /**
     * For example, -1E4, 1267.43233E12, 12.78e-2, 12 , -0, 0 and INF are all 
     * legal literals for float.
     * @throws Exception 
     */

	public void testWhiteSpace() throws Exception{
		validateValues(" \n12", new Float(12));
	}
	public void testParse() throws Exception {
        validateValues("-1E4", new Float(-1E4));
        validateValues("1267.43233E12", new Float(1267.43233E12));
        validateValues("12.78e-2", new Float(12.78e-2));
        validateValues("12", new Float(12));
        validateValues("-0", new Float("-0"));
        validateValues("0", new Float(0));
        validateValues("INF", new Float(Float.POSITIVE_INFINITY));
    }
    
    protected QName getQName() {
        return XS.FLOAT;
    }

}
