package org.geotools.wps;

import junit.framework.TestCase;

import net.opengis.wps.WPSCapabilitiesType;

import org.geotools.xml.Parser;

public class GetCapabilitiesTest extends TestCase {

    public void testParse() throws Exception {
        WPSConfiguration wps = new WPSConfiguration();
        Parser parser = new Parser( wps );
        
        Object o = parser.parse( getClass().getResourceAsStream( "20_wpsGetCapabilities_response.xml"));
        assertTrue( o instanceof WPSCapabilitiesType);
    }
}
