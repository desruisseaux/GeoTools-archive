package org.geotools.wfs.v_1_1_0.data;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;

import org.geotools.test.TestData;
import org.opengis.feature.simple.SimpleFeatureType;

public class XmlSimpleFeatureParserTest extends AbstractGetFeatureParserTest {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected GetFeatureParser getParser(final QName featureName, final String schemaLocation,
            final SimpleFeatureType featureType, final String dataFile) throws IOException {

        InputStream inputStream = TestData.openStream(this, dataFile);
        GetFeatureParser parser = new XmlSimpleFeatureParser(inputStream, featureName, featureType);
        return parser;
    }

}
