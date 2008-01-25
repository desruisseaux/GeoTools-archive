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

package org.geotools.wfs.v_1_1_0.data;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.test.TestData;
import org.geotools.wfs.WFSConfiguration;
import org.geotools.xml.Configuration;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * 
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.x
 * @URL $URL:
 *      http://svn.geotools.org/geotools/trunk/gt/modules/plugin/wfs/src/test/java/org/geotools/wfs/v_1_1_0/data/EmfAppSchemaParserTest.java $
 */
public class EmfAppSchemaParserTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test method for
     * {@link EmfAppSchemaParser#parse(javax.xml.namespace.QName, java.net.URL)}.
     * 
     * @throws IOException
     */
    public void testParseGeoServerSimpleFeatureType() throws IOException {
        final String namespace = "http://www.openplans.org/topp";
        final String featureName = "states";
        final QName featureTypeName = new QName(namespace, featureName);
        final String schemaFileName = "schemas/geoserver/DescribeFeatureType_States.xsd";
        final URL schemaLocation = TestData.getResource(this, schemaFileName);
        final int expectedAttributeCount = 28;

        SimpleFeatureType ftype = testParseDescribeFeatureType(featureTypeName, schemaLocation, expectedAttributeCount);
    }

    public void testParseCubeWerx_GML_Level1_FeatureType() throws IOException {
        final String namespace = "http://www.fgdc.gov/framework/073004/gubs";
        final String featureName = "GovernmentalUnitCE";
        final QName featureTypeName = new QName(namespace, featureName);
        final String schemaFileName = "schemas/CubeWerx_nsdi/CubeWerx_nsdi_GovernmentalUnitCE_DescribeFeatureType.xsd";
        final URL schemaLocation = TestData.getResource(this, schemaFileName);
        final int expectedAttributeCount = 18;

        SimpleFeatureType ftype = testParseDescribeFeatureType(featureTypeName, schemaLocation, expectedAttributeCount);
        for (AttributeDescriptor descriptor : ftype.getAttributes()){
            System.out.print(descriptor.getName().getNamespaceURI());
            System.out.print("#");
            System.out.print(descriptor.getName().getLocalPart());
            System.out.print("[" + descriptor.getMinOccurs() + ":" + descriptor.getMaxOccurs() + "]");
            System.out.print(" (" + descriptor.getType().getName() + ": " + descriptor.getType().getBinding() + ")");
            System.out.println("");
        }
    }

    private SimpleFeatureType testParseDescribeFeatureType(final QName featureTypeName,
            final URL schemaLocation, int expectedAttributeCount) throws IOException {
        assertNotNull(schemaLocation);
        final CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        
        Configuration configuration = new WFSConfiguration();
        
        SimpleFeatureType featureType;
        featureType = EmfAppSchemaParser.parse(configuration, featureTypeName,
                schemaLocation, crs);

        assertNotNull(featureType);
        assertSame(crs, featureType.getCRS());

        List<AttributeDescriptor> attributes = featureType.getAttributes();
        assertEquals(expectedAttributeCount, attributes.size());
        return featureType;
    }
}
