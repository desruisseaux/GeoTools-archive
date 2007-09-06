package org.geotools.kml.bindings;

import java.util.Collection;

import org.geotools.feature.Feature;
import org.geotools.kml.KML;
import org.geotools.kml.KMLTestSupport;
import org.geotools.xml.Binding;

public class FolderTypeBindingTest extends KMLTestSupport {

    public void testType() throws Exception {
        assertEquals(Feature.class, binding(KML.FolderType).getType());
    }

    public void testExecutionMode() throws Exception {
        assertEquals(Binding.AFTER, binding(KML.FolderType).getExecutionMode());
    }

    public void testParse() throws Exception {
        String xml = "<Folder>" + "<name>folder</name>" + "<Placemark>" + "<Point>"
            + "<coordinates>0,0</coordinates>" + "</Point>" + "</Placemark>" + "</Folder>";
        buildDocument(xml);

        Feature document = (Feature) parse();
        assertEquals("folder", document.getAttribute("name"));

        Collection features = (Collection) document.getAttribute("Feature");
        assertEquals(1, features.size());
    }
}
