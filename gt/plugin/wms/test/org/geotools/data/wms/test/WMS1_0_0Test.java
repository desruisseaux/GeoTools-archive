/*
 * Created on Aug 25, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.test;

import junit.framework.TestCase;

import org.geotools.data.ows.BoundingBox;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WMS1_0_0;
import org.geotools.data.wms.WMSBuilder;
import org.geotools.data.wms.WMSParser;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.GetCapabilitiesRequest;

import org.geotools.resources.TestData;

import org.jdom.Document;

import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.util.Properties;
import java.util.StringTokenizer;


/**
 * @author Richard Gould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMS1_0_0Test extends TestCase {
    private URL server;

    public WMS1_0_0Test() throws Exception {
        this.server = new URL(
                "http://www2.demis.nl/mapserver/Request.asp?wmtver=1.0.0&request=getcapabilities");
    }

    public void testGetName() {
        WMS1_0_0 spec = new WMS1_0_0();
        assertEquals(spec.getName(), "WMT_MS_Capabilities");
    }

    public void testGetVersion() {
        WMS1_0_0 spec = new WMS1_0_0();
        assertEquals(spec.getVersion(), "1.0.0");
    }

    public void testCreateGetCapabilitiesRequest() throws Exception {
        WMS1_0_0 spec = new WMS1_0_0();
        GetCapabilitiesRequest request = spec.createGetCapabilitiesRequest(server);
        System.out.println(request.getFinalURL());

        Properties properties = new Properties();

        StringTokenizer tokenizer = new StringTokenizer(request.getFinalURL()
                                                               .getQuery(), "&");

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            String[] param = token.split("=");
            properties.setProperty(param[0].toUpperCase(), param[1]);
        }

        assertEquals(properties.getProperty("REQUEST"), "capabilities");
        assertEquals(properties.getProperty("WMTVER"), "1.0.0");

        WebMapServer wms = new WebMapServer(server, true);
        WMSCapabilities capabilities = wms.getCapabilities();

        if (!(wms.getProblem() instanceof IOException)) {
            assertNotNull(capabilities);
        }
    }

    public void testCreateParser() throws Exception {
        WMS1_0_0 spec = new WMS1_0_0();

        File getCaps = TestData.file(this, "1.0.0Capabilities.xml");
        URL getCapsURL = getCaps.toURL();

        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(getCapsURL);

        WMSParser parser = spec.createParser(document);
        assertEquals(parser.getClass(), WMS1_0_0.Parser.class);

        WMSCapabilities capabilities = parser.constructCapabilities(document,
                new WMSBuilder());
        assertEquals(capabilities.getVersion(), "1.0.0");
        assertEquals(capabilities.getService().getName(), "GetMap");
        assertEquals(capabilities.getService().getTitle(), "World Map");

        for (int i = 0; i < capabilities.getService().getKeywordList().length;
                i++) {
            assertEquals(capabilities.getService().getKeywordList()[i],
                "OpenGIS WMS Web Map Server".split(" ")[i]);
        }

        assertEquals(capabilities.getService().getOnlineResource(),
            new URL("http://www2.demis.nl"));
        assertEquals(capabilities.getRequest().getGetCapabilities()
                                 .getFormatStrings()[0],
            "application/vnd.ogc.wms_xml");
        assertEquals(capabilities.getRequest().getGetFeatureInfo().getGet(),
            new URL("http://www2.demis.nl/wms/wms.asp?wms=WorldMap&"));
        assertEquals(capabilities.getRequest().getGetMap().getFormatStrings().length,
            4);

        assertEquals(capabilities.getLayers().length, 21);

        Layer[] layers = capabilities.getLayers();
        assertEquals(layers[0].getTitle(), "World Map");
        assertEquals(layers[0].getParent(), null);
        assertTrue(layers[0].getSrs().contains("EPSG:4326"));
        assertTrue(layers[0].getSrs().contains("EPSG:4327"));
        assertEquals(layers[1].getTitle(), "Bathymetry");
        assertEquals(layers[1].getName(), "Bathymetry");
        assertEquals(layers[20].getTitle(), "Ocean features");
        assertEquals(layers[20].getName(), "Ocean features");
        assertEquals(layers[0].getBoundingBoxes().size(), 1);

        BoundingBox bbox = (BoundingBox) layers[1].getBoundingBoxes().get("EPSG:4326");
        assertNotNull(bbox);
    }
}
