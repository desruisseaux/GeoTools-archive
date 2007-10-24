/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.wfs;

import junit.framework.TestCase;
import net.opengis.ows.KeywordsType;
import net.opengis.ows.OperationType;
import net.opengis.ows.OperationsMetadataType;
import net.opengis.ows.ServiceIdentificationType;
import net.opengis.wfs.FeatureCollectionType;
import net.opengis.wfs.FeatureTypeListType;
import net.opengis.wfs.FeatureTypeType;
import net.opengis.wfs.WFSCapabilitiesType;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDSchema;
import org.w3c.dom.Document;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import com.vividsolutions.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.capability.FilterCapabilities;
import org.opengis.filter.capability.SpatialOperators;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.xml.Parser;
import org.geotools.xml.Schemas;
import org.geotools.xml.StreamingParser;


public class WFSParsingTest extends TestCase {
    WFSConfiguration configuration;

    protected void setUp() throws Exception {
        super.setUp();

        configuration = new WFSConfiguration();
    }

    public void testParseGetCapabilities() throws Exception {
        Parser parser = new Parser(configuration);
        WFSCapabilitiesType caps = (WFSCapabilitiesType) parser.parse(getClass()
                                                                          .getResourceAsStream("geoserver-GetCapabilities.xml"));

        assertNotNull(caps);

        assertServiceIdentification(caps);
        assertOperationsMetadata(caps);
        assertFeatureTypeList(caps);
        assertFilterCapabilities(caps);
    }

    void assertServiceIdentification(WFSCapabilitiesType caps) {
        ServiceIdentificationType sa = caps.getServiceIdentification();
        assertNotNull(sa);

        assertEquals(1, sa.getKeywords().size());

        KeywordsType keywords = (KeywordsType) sa.getKeywords().get(0);
        assertTrue(keywords.getKeyword().contains("WFS"));
        assertTrue(keywords.getKeyword().contains("NY"));
        assertTrue(keywords.getKeyword().contains("New York"));

        assertEquals("WFS", sa.getServiceType().getValue());
        assertEquals("1.1.0", sa.getServiceTypeVersion());
    }

    void assertOperationsMetadata(WFSCapabilitiesType caps) {
        OperationsMetadataType om = caps.getOperationsMetadata();
        assertNotNull(om);

        assertEquals(6, om.getOperation().size());
        assertEquals("GetCapabilities", ((OperationType) om.getOperation().get(0)).getName());
        assertEquals("DescribeFeatureType", ((OperationType) om.getOperation().get(1)).getName());
        assertEquals("GetFeature", ((OperationType) om.getOperation().get(2)).getName());
        assertEquals("LockFeature", ((OperationType) om.getOperation().get(3)).getName());
        assertEquals("GetFeatureWithLock", ((OperationType) om.getOperation().get(4)).getName());
        assertEquals("Transaction", ((OperationType) om.getOperation().get(5)).getName());
    }

    void assertFeatureTypeList(WFSCapabilitiesType caps) {
        FeatureTypeListType ftl = caps.getFeatureTypeList();
        assertNotNull(ftl);

        assertEquals(3, ftl.getFeatureType().size());
        assertEquals("AggregateGeoFeature",
            ((FeatureTypeType) ftl.getFeatureType().get(0)).getName().getLocalPart());
        assertEquals("sf", ((FeatureTypeType) ftl.getFeatureType().get(0)).getName().getPrefix());
        assertEquals("http://cite.opengeospatial.org/gmlsf",
            ((FeatureTypeType) ftl.getFeatureType().get(0)).getName().getNamespaceURI());

        assertEquals("Entit\u00E9G\u00E9n\u00E9rique",
            ((FeatureTypeType) ftl.getFeatureType().get(1)).getName().getLocalPart());
        assertEquals("PrimitiveGeoFeature",
            ((FeatureTypeType) ftl.getFeatureType().get(2)).getName().getLocalPart());
    }

    void assertFilterCapabilities(WFSCapabilitiesType caps) {
        FilterCapabilities fc = (FilterCapabilities) caps.getFilterCapabilities();
        assertTrue(fc.getIdCapabilities().hasEID());
        assertTrue(fc.getIdCapabilities().hasFID());

        assertEquals(4, fc.getSpatialCapabilities().getGeometryOperands().length);

        SpatialOperators spatial = (SpatialOperators) fc.getSpatialCapabilities()
                                                        .getSpatialOperators();
        assertEquals(10, spatial.getOperators().length);
        assertNotNull(spatial.getOperator("BBOX"));
        assertNotNull(spatial.getOperator("Intersects"));
    }

    public void testParseDescribeFeatureType() throws Exception {
        String loc = getClass().getResource("geoserver-DescribeFeatureType.xml").getFile();
        XSDSchema schema = Schemas.parse(loc);

        assertNotNull(schema);
        assertEquals(1, schema.getTypeDefinitions().size());

        XSDComplexTypeDefinition type = (XSDComplexTypeDefinition) schema.getTypeDefinitions().get(0);
        assertEquals("PrimitiveGeoFeatureType", type.getName());
        assertEquals("http://cite.opengeospatial.org/gmlsf", type.getTargetNamespace());
    }

    public void testParseGetFeature() throws Exception {
        File tmp = File.createTempFile("geoserver-DescribeFeatureType", "xml");
        tmp.deleteOnExit();

        InputStream in = getClass().getResourceAsStream("geoserver-DescribeFeatureType.xml");
        copy(in, tmp);

        in = getClass().getResourceAsStream("geoserver-GetFeature.xml");

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(in);

        //http://cite.opengeospatial.org/gmlsf http://localhost:8080/geoserver/wfs?service=WFS&amp;version=1.1.0&amp;request=DescribeFeatureType&amp;typeName=sf:PrimitiveGeoFeature
        String schemaLocation = doc.getDocumentElement()
                                   .getAttributeNS("http://www.w3.org/2001/XMLSchema-instance",
                "schemaLocation");
        schemaLocation = schemaLocation.replaceAll("http://cite.opengeospatial.org/gmlsf .*",
                "http://cite.opengeospatial.org/gmlsf " + tmp.getAbsolutePath());
        doc.getDocumentElement()
           .setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
            schemaLocation);

        tmp = File.createTempFile("geoserver-GetFeature", "xml");
        tmp.deleteOnExit();

        Transformer tx = TransformerFactory.newInstance().newTransformer();
        tx.transform(new DOMSource(doc), new StreamResult(tmp));

        in = new FileInputStream(tmp);

        Parser parser = new Parser(configuration);
        FeatureCollectionType fc = (FeatureCollectionType) parser.parse(in);
        assertNotNull(fc);

        List featureCollections = fc.getFeature();
        assertEquals(1, featureCollections.size());

        FeatureCollection featureCollection = (FeatureCollection) featureCollections.get(0);
        assertEquals(5, featureCollection.size());

        FeatureIterator features = featureCollection.features();

        try {
            assertTrue(features.hasNext());

            SimpleFeature f = features.next();

            assertEquals("PrimitiveGeoFeature.f001", f.getID());
            assertNotNull(f.getDefaultGeometry());

            Point p = (Point) f.getDefaultGeometry();

            assertEquals(39.73245, p.getX(), 0.1);
            assertEquals(2.00342, p.getY(), 0.1);

            assertEquals(BigInteger.valueOf(155), f.getAttribute("intProperty"));
            assertEquals(new URI("http://www.opengeospatial.org/"), f.getAttribute("uriProperty"));
            assertEquals(new Float(12765.0), f.getAttribute("measurand"));
            assertTrue(f.getAttribute("dateProperty") instanceof Date);
            assertEquals(BigDecimal.valueOf(5.03), f.getAttribute("decimalProperty"));
        } finally {
            featureCollection.close(features);
        }
    }

    void copy(InputStream in, File to) throws Exception {
        Writer writer = new BufferedWriter(new FileWriter(to));
        InputStreamReader reader = new InputStreamReader(in);

        int b = -1;

        while ((b = reader.read()) != -1) {
            writer.write(b);
        }

        writer.flush();
        writer.close();
    }

    public void testParseGetFeatureStreaming() throws Exception {
        InputStream in = getClass().getResourceAsStream("geoserver-GetFeature.xml");
        StreamingParser parser = new StreamingParser(configuration, in, SimpleFeature.class);

        int n = 0;

        while (parser.parse() != null)
            n++;

        assertEquals(5, n);
    }
}
