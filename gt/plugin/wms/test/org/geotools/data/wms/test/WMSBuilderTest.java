/*
 * Created on Aug 21, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.test;

import junit.framework.TestCase;

import org.geotools.data.ows.BoundingBox;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WMSBuilder;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/**
 * @author Kefka
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMSBuilderTest extends TestCase {
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testFinish() throws Exception {
        WMSBuilder builder = new WMSBuilder();
        builder.buildCapabilities("1.1.1");
        builder.buildService("FakeService", "Test",
            new URL("http://online.com"), "nothin", null);

        List formats = new ArrayList();
        formats.add("image/jpeg");

        builder.buildGetCapabilitiesOperation(formats,
            new URL("http://get.com"), new URL("http://post.com"));
        builder.buildGetMapOperation(formats, new URL("http://get.com"),
            new URL("http://post.com"));

        Set srss = new TreeSet();
        srss.add("EPSG:blah");
        srss.add("EPSG:2");

        List styles = new ArrayList(2);
        styles.add("Style1");
        styles.add("Style2");

        builder.buildLayer("Layer1", "layer1", true, null, srss, styles);
        builder.buildBoundingBox("bork", 1.0, 1.0, 2.0, 2.0);

        srss = new TreeSet();
        srss.add("EPSG:3");

        styles = new ArrayList(1);
        styles.add("Style3");

        builder.buildLayer("Layer2", "layer2", false, null, srss, styles);

        WMSCapabilities capabilities = builder.finish();
        assertEquals(capabilities.getVersion(), "1.1.1");
        assertEquals(capabilities.getService().getName(), "FakeService");
        assertEquals(capabilities.getService().getTitle(), "Test");
        assertEquals(capabilities.getRequest().getGetCapabilities().getGet(),
            new URL("http://get.com"));
        assertEquals(capabilities.getRequest().getGetMap().getFormatStrings()[0],
            "image/jpeg");
        assertEquals(capabilities.getLayers()[0].getName(), "layer1");
        assertEquals(capabilities.getLayers()[1].getTitle(), "Layer2");
        assertTrue(capabilities.getLayers()[1].getSrs().contains("EPSG:3"));

        BoundingBox bbox = (BoundingBox) capabilities.getLayers()[0].getBoundingBoxes()
                                                                    .get("bork");
        assertEquals(Double.toString(bbox.getMinX()), Double.toString(1.0));
    }
}
