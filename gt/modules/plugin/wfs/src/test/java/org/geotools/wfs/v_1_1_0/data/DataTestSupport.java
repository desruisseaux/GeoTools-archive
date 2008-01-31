package org.geotools.wfs.v_1_1_0.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.geotools.test.TestData;

public abstract class DataTestSupport extends TestCase {

    /**
     * Location of a test data capabilities from geoserver
     */
    public static final String GEOS_CAPABILITIES = "geoserver/geoserver_capabilities_1_1_0.xml";

    /**
     * Location of a test DescribeFeatureType response from a geoserver
     * {@code topp:states} FeatureType
     */
    protected static final String GEOS_STATES_SCHEMA = "geoserver/DescribeFeatureType_States.xsd";

    /**
     * Type name for the sample geoserver states featuretype
     */
    public static final QName GEOS_STATES_TYPENAME = new QName("http://www.openplans.org/topp", "states");

    /**
     * Prefixed type name for the sample geoserver states featuretype as used in
     * the capabilities document (and thus as presented by the
     * WFSDataStore.getTypeNames() method)
     */
    public static final String GEOS_STATES_FEATURETYPENAME = "topp:states";

    /**
     * Type name for the sample geoserver archsites featuretype
     */
    public static final QName GEOS_ARCHSITES_TYPENAME = new QName(
            "http://www.openplans.org/spearfish", "archsites");

    /**
     * Prefixed type name for the sample geoserver archsites featuretype as used
     * in the capabilities document (and thus as presented by the
     * WFSDataStore.getTypeNames() method)
     */
    public static final String GEOS_ARCHSITES_FEATURETYPENAME = "sf:archsites";

    /**
     * Location of the file containing a sample GeoServer GetFeature response
     * for the {@code archsites} feature type
     */
    public static final String GEOS_ARCHSITES_DATA = "geoserver/geoserver_archsites_features.xml";

    /**
     * Location of a test DescribeFeatureType response from a geoserver
     * {@code sf:archsites} FeatureType
     */
    public static final String GEOS_ARCHSITES_SCHEMA = "geoserver/geoserver_archsites_describeFeatureType.xsd";

    /**
     * Type name for the sample CubeWerx GovernmentalUnitCE featuretype
     */
    public static final QName CUBEWERX_GOVUNITCE_TYPENAME = new QName(
            "http://www.fgdc.gov/framework/073004/gubs", "GovernmentalUnitCE");

    /**
     * Prefixed type name for the sample CubeWerx GovernmentalUnitCE featuretype
     * as used in capabilities
     */
    public static final String CUBEWERX_GOVUNITCE_FEATURETYPENAME = "gubs:GovernmentalUnitCE";

    /**
     * Location of the sample GetFeature response from a CubeWerx server for the
     * GovernmentalUnitCE feature type
     */
    public static final String CUBEWERX_GOVUNITCE_DATA = "CubeWerx_nsdi/CubeWerx_nsdi_GovernmentalUnitCE.xml";

    /**
     * Location of a test DescribeFeatureType response from a CubeWerx
     * {@code GovernmentalUnitCE} FeatureType
     */
    public static final String CUBEWERX_GOVUNITCE_SCHEMA = "CubeWerx_nsdi/CubeWerx_nsdi_GovernmentalUnitCE_DescribeFeatureType.xsd";

    /**
     * Location of a test data capabilities from CubeWerx
     */
    public static final String CUBEWERX_CAPABILITIES = "CubeWerx_nsdi/CubeWerx_nsdi_GetCapabilities.xml";

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
        protocolHandler = new WFS110ProtocolHandler(stream, tryGzip, auth, "UTF-8", Integer.valueOf(0)) {
            @Override
            public URL getDescribeFeatureTypeURLGet(final String typeName)
                    throws MalformedURLException {
                if ("topp:states".equals(typeName)) {
                    String schemaLocation = DataTestSupport.GEOS_STATES_SCHEMA;
                    URL url = TestData.getResource(this, schemaLocation);
                    assertNotNull(url);
                    return url;
                }
                throw new IllegalArgumentException("unknown typename: " + typeName);
            }
        };
    }
}
