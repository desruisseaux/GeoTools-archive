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
import net.opengis.wfs.FeatureTypeListType;
import net.opengis.wfs.FeatureTypeType;
import net.opengis.wfs.WFSCapabilitiesType;
import org.opengis.filter.capability.FilterCapabilities;
import org.opengis.filter.capability.SpatialOperators;
import org.geotools.xml.Parser;


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
}
