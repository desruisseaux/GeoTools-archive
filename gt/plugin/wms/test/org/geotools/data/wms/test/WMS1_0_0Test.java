/*
 * Created on Aug 25, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.test;

import java.net.URL;

import org.geotools.data.wms.WMS1_0_0;
import org.geotools.data.wms.WMS1_1_1;
import org.geotools.data.wms.request.GetCapabilitiesRequest;

import junit.framework.TestCase;

/**
 * @author Richard Gould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMS1_0_0Test extends TestCase {

    private URL server;

    public WMS1_0_0Test () throws Exception {
        this.server = new URL("http://www2.demis.nl/mapserver/Request.asp?wmtver=1.0.0&request=getcapabilities");
    }
    
    public void testGetName() {
        WMS1_0_0 spec = new WMS1_0_0();
        assertEquals(spec.getName(), "WMT_MS_Capabilities");
    }

    public void testGetVersion() {
        WMS1_0_0 spec = new WMS1_0_0();
        assertEquals(spec.getVersion(), "1.0.0");
    }

    public void testCreateRequest() throws Exception {
        WMS1_0_0 spec = new WMS1_0_0();
        GetCapabilitiesRequest request = spec.createGetCapabilitiesRequest(server);
        URL expectedRequestURL = new URL("http://www2.demis.nl/mapserver/Request.asp?WMTVER=1.0.0&REQUEST=capabilities");
        System.out.println(request.getFinalURL());
        assertEquals(request.getFinalURL(), expectedRequestURL);
    }

    public void testCreateParser() {
    }

    public void testWMS1_0_0() {
    }

    public void testToMIME() {
    }

    public void testToFormat() {
    }

}
