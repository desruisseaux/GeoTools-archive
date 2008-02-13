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
import java.net.URL;

import javax.xml.namespace.QName;

import org.geotools.test.TestData;
import org.geotools.wfs.WFSConfiguration;
import org.geotools.xml.Configuration;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * @author Gabriel Roldan
 * @version $Id: StreamingParserFeatureReaderTest.java 28884 2008-01-22
 *          15:21:03Z groldan $
 * @since 2.5.x
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/wfs/src/test/java/org/geotools/wfs/v_1_1_0/data/StreamingParserFeatureReaderTest.java $
 */
public class StreamingParserFeatureReaderTest extends AbstractGetFeatureParserTest {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected GetFeatureParser getParser(QName featureName, String schemaLocation,
            SimpleFeatureType featureType, String dataFile) throws IOException {
        URL schemaLocationUrl = TestData.getResource(this, schemaLocation);

        Configuration configuration = new WFSConfiguration();
        final InputStream inputStream = TestData.openStream(this, dataFile);
        final StreamingParserFeatureReader featureReader;
        featureReader = new StreamingParserFeatureReader(configuration, inputStream, featureName,
                schemaLocationUrl);
        return featureReader;
    }
}
