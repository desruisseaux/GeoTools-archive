package org.geotools.data.complex.config;

import java.net.URL;
import java.util.Set;
import java.util.logging.Level;

import junit.framework.TestCase;

import org.geotools.data.complex.FeatureTypeMapping;
import org.geotools.util.Logging;

public class XMLConfigReaderTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'org.geotools.data.complex.config.XMLConfigReader.parse(URL)'
	 */
	public void testParseURL() throws Exception{
        Logging.GEOTOOLS.forceMonolineConsoleOutput(Level.INFO);
		XMLConfigDigester reader = new XMLConfigDigester();
		URL url = XMLConfigDigester.class.getResource("../test-data/roadsegments.xml");
		ComplexDataStoreDTO config = reader.parse(url);
		
		Set mappings = ComplexDataStoreConfigurator.buildMappings(config);
		
		assertNotNull(mappings);
		assertEquals(1, mappings.size());
		FeatureTypeMapping mapping = (FeatureTypeMapping)mappings.iterator().next();
		
		assertEquals(0, mapping.getGroupByAttNames().size());
		assertEquals(6, mapping.getAttributeMappings().size());
		assertNotNull(mapping.getTargetFeature());
		assertNotNull(mapping.getSource());
		
		//Map/*<String, Expression>*/idMappings = mapping.getIdMappings();
		//assertEquals(idMappings.get("RoadSegment"), ExpressionBuilder.parse("getId()"));
	}

}
