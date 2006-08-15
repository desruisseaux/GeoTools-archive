package org.geotools.xs.bindings;

import javax.xml.bind.ValidationException;
import javax.xml.namespace.QName;

import org.geotools.xs.TestSchema;
import org.geotools.xs.bindings.XS;

public class XSBooleanStrategyTest extends TestSchema {

    /**
     * An instance of a datatype that is defined as ??boolean?? can have the 
     * following legal literals {true, false, 1, 0}.
     * @throws Exception 
     *
     */
    
    /*
     * Test method for 'org.geotools.xs.strategies.XSBooleanStrategy.parse(Element, Node[], Object)'
     */
    public void testTruth() throws Exception {
        validateValues("true", Boolean.TRUE);
        validateValues("false", Boolean.FALSE);
        validateValues("1", Boolean.TRUE);
        validateValues("0", Boolean.FALSE);        
    }
    public void testUntruth() throws Exception {
    	try {
            validateValues("TRUE", Boolean.FALSE);        
            fail("TRUTH is not absolute");
    	}
    	catch( ValidationException expected ){
    		// yeah!
    	}
    }

    protected QName getQName() {
        return XS.BOOLEAN;
    }

}
