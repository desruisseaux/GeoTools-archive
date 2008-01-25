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

import java.io.InputStream;
import java.net.URL;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.geotools.test.TestData;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author Gabriel Roldan
 * @version $Id: StreamingParserFeatureReaderTest.java 28884 2008-01-22
 *          15:21:03Z groldan $
 * @since 2.5.x
 * @URL $URL:
 *      http://svn.geotools.org/geotools/trunk/gt/modules/plugin/wfs/src/test/java/org/geotools/wfs/v_1_1_0/data/StreamingParserFeatureReaderTest.java $
 */
public class StreamingParserFeatureReaderTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verifies correctness on parsing a normal geoserver WFS 1.1.0 GetFeature
     * response.
     * 
     * Test method for {@link StreamingParserFeatureReader#parse()}.
     * 
     * @throws Exception
     */
    public void testParseGeoServerSimpleFeatures() throws Exception {
        final String nsUri = "http://www.openplans.org/spearfish";
        final QName featureName = new QName(nsUri, "archsites");
        final int expectedCount = 3;
        final String fileName = "geoserver_archsites_features.xml";
        final String schemaName = "schemas/geoserver/geoserver_archsites_describeFeatureType.xsd";

        final FeatureVisitor assertor = new FeatureVisitor() {
            public void visit(Feature f) {
                assertNotNull(f);
                assertNotNull(f.getID());
                assertNotNull(f.getProperty("cat"));
                assertNotNull(f.getProperty("str1"));
                assertNotNull(f.getProperty("the_geom"));
                assertTrue(f.getProperty("the_geom").getValue() instanceof Point);
            }
        };

        testParseGetFeatures(featureName, fileName, schemaName, assertor, expectedCount);
    }

    /**
     * Verifies correctness on parsing a sample CubeWerx WFS 1.1.0 GetFeature
     * response.
     * 
     * Test method for {@link StreamingParserFeatureReader#parse()}.
     * 
     * @throws Exception
     */
    public void testParseCubeWerxComplexFeatures_GovernmentalUnitCE() throws Exception {
        final String nsUri = "http://www.fgdc.gov/framework/073004/gubs";
        final QName featureName = new QName(nsUri, "GovernmentalUnitCE");
        final int expectedCount = 3;
        final String fileName = "CubeWerx_nsdi_GovernmentalUnitCE.xml";
        final String schemaName = "schemas/CubeWerx_nsdi/CubeWerx_nsdi_GovernmentalUnitCE_DescribeFeatureType.xsd";

        final FeatureVisitor assertor = new FeatureVisitor() {
            public void visit(Feature f) {
                assertNotNull(f);
                assertNotNull(f.getID());
                assertTrue(f.getProperty("geometry").getValue() instanceof Polygon);
                 assertNotNull(f.getProperty("instanceName").getValue());
                 assertNotNull(f.getProperty("instanceCode").getValue());
                 assertNotNull(f.getProperty("effectiveDate").getValue());
                
                 assertNotNull(f.getProperty("governmentalUnitType"));

                // for(Property p : f.getProperties()){
                // Name localPart = p.getName();
                // Object value = p.getValue();
                // System.out.println(localPart + " = " + value);
                // }
                // System.out.println("--");
            }
        };

        testParseGetFeatures(featureName, fileName, schemaName, assertor, expectedCount);
    }

    /**
     * Uses a {@link StreamingParserFeatureReader} to parse the features while
     * traversing the feature collection in a test {@code wfs:FeatureCollection}
     * document; {@code assertor} is a visitor provided by the actual unit test
     * calling this method, every feature fetched is passed to the visitor who
     * contains the specific assertions.
     * 
     * @param featureName
     *            the name of the features (not the feature type) expected
     * @param getFeatureResultTestFile
     *            the name of the test file name to load in order to simulate
     *            the response of a GetFeature request
     * @param assertor
     *            a FeatureVisitor to assert the contents or structure of the
     *            features
     * @param expectedFeatureCount
     *            the number of features there should be on the feature
     *            collection, an assertion is made at the end of the method.
     * @param schemaName
     * @throws Exception
     */
    private void testParseGetFeatures(final QName featureName,
            final String getFeatureResultTestFile, final String schemaName,
            final FeatureVisitor assertor, final int expectedFeatureCount) throws Exception {

        String namespace = featureName.getNamespaceURI();
        URL schemaLocation = TestData.getResource(this, schemaName);
        String schemaUri = schemaLocation.toExternalForm();

        TestWFSConfiguration configuration = new TestWFSConfiguration(namespace, schemaUri);
        final InputStream inputStream = TestData.openStream(this, getFeatureResultTestFile);
        final StreamingParserFeatureReader featureReader;
        featureReader = new StreamingParserFeatureReader(configuration, inputStream, featureName);

        int featureCount = 0;
        SimpleFeature feature;

        try {
            for (int i = 0; i < expectedFeatureCount; i++) {
                feature = featureReader.parse();
                assertNotNull(feature);
                featureCount++;
                assertor.visit(feature);
            }
            feature = featureReader.parse();
            assertNull(feature);
        } finally {
            inputStream.close();
        }

        assertEquals(expectedFeatureCount, featureCount);
    }
}
