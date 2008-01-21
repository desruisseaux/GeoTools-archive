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

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.test.TestData;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import junit.framework.TestCase;

/**
 * 
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.x
 * @URL $URL$
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
     */
    public void testParse() {
        final String namespace = "http://www.openplans.org/topp";
        final String featureName = "states";
        final QName featureTypeName = new QName(namespace, featureName);
        final URL schemaLocation = TestData.getResource(this, "DescribeFeatureType_States.xml");
        final CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        SimpleFeatureType featureType = EmfAppSchemaParser.parse(featureTypeName, schemaLocation, crs);
        assertNotNull(featureType);
        assertSame(crs,featureType.getCRS());

        List<AttributeDescriptor> attributes = featureType.getAttributes();
        assertEquals(28, attributes.size());
    }
}
