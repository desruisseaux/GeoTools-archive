package org.geotools.data.complex;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.geotools.data.complex.config.XMLConfigReaderTest;
import org.geotools.data.complex.filter.UnmappingFilterVisitorTest;
import org.geotools.data.feature.memory.MemoryDataAccessTest;
import org.geotools.filter.IDFunctionExpressionTest;
import org.geotools.gml.producer.GML3FeatureTransformerTest;
import org.geotools.gml.schema.SchemaHandlerTest;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.geotools.data.complex");
		//$JUnit-BEGIN$
		suite.addTestSuite(UnmappingFilterVisitorTest.class);
		suite.addTestSuite(FeatureTypeMappingTest.class);
		suite.addTestSuite(ComplexDataStoreFactoryTest.class);
		suite.addTestSuite(ComplexDataStoreTest.class);
		suite.addTestSuite(MemoryDataAccessTest.class);
		suite.addTestSuite(IDFunctionExpressionTest.class);
		suite.addTestSuite(SchemaHandlerTest.class);
		suite.addTestSuite(XMLConfigReaderTest.class);
		suite.addTestSuite(GML3FeatureTransformerTest.class);
		
		//$JUnit-END$
		return suite;
	}

}
