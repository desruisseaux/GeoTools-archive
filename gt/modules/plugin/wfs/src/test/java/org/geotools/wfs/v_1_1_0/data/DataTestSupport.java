package org.geotools.wfs.v_1_1_0.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

import org.geotools.test.TestData;

public abstract class DataTestSupport extends TestCase {
    protected WFS110ProtocolHandler protocolHandler;

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        protocolHandler = null;
    }

    /**
     * Tests methods call this one to set up a protocolHandler to test
     * 
     * @param capabilitiesFileName
     * @param tryGzip
     * @param auth
     * @throws IOException
     */
    protected void createProtocolHandler(String capabilitiesFileName, boolean tryGzip,
            Authenticator auth) throws IOException {
        InputStream stream = TestData.openStream(this, capabilitiesFileName);
        protocolHandler = new WFS110ProtocolHandler(stream, tryGzip, auth, "UTF-8") {
            @Override
            public URL getDescribeFeatureTypeURLGet(final String typeName)
                    throws MalformedURLException {
                if ("topp:states".equals(typeName)) {
                    String schemaLocation = "schemas/geoserver/DescribeFeatureType_States.xsd";
                    URL url = TestData.getResource(this, schemaLocation);
                    assertNotNull(url);
                    return url;
                }
                throw new IllegalArgumentException("unknown typename: " + typeName);
            }
        };
    }
}
