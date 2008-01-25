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
import java.io.InputStream;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.geotools.test.TestData;
import org.geotools.wfs.WFSConfiguration;
import org.geotools.xml.Configuration;
import org.opengis.feature.simple.SimpleFeature;

/**
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.x
 * @URL $URL$
 */
public class StreamingParserFeatureReaderTest extends TestCase {

    private static Configuration wfsConfiguration;

    private static QName archSitesName = new QName("http://www.openplans.org/spearfish",
            "archsites");

    private StreamingParserFeatureReader featureReader;

    private InputStream archSitesInputStream;

    protected void setUp() throws Exception {
        super.setUp();
        synchronized (StreamingParserFeatureReaderTest.class) {
            if (wfsConfiguration == null) {
                wfsConfiguration = new WFSConfiguration();
            }
        }
        archSitesInputStream = TestData.openStream(this, "geoserver_archsites_features.xml");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        try {
            archSitesInputStream.close();
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * Test method for {@link StreamingParserFeatureReader#parse()}.
     * 
     * @throws IOException
     */
    public void testParse() throws IOException {
        featureReader = new StreamingParserFeatureReader(wfsConfiguration, archSitesInputStream,
                archSitesName);
        final int featureCount = 3;
        SimpleFeature feature;

        for (int i = 0; i < featureCount; i++) {
            feature = featureReader.parse();
            assertNotNull(feature);
        }

        feature = featureReader.parse();
        assertNull(feature);
    }

}
