/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data.complex;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.data.DataAccessFinder;
import org.geotools.data.complex.config.ComplexDataStoreConfigurator;
import org.geotools.data.complex.config.ComplexDataStoreDTO;
import org.geotools.data.complex.config.EmfAppSchemaReader;
import org.geotools.data.complex.config.XMLConfigDigester;
import org.geotools.data.feature.FeatureAccess;
import org.geotools.data.feature.FeatureSource2;
import org.geotools.feature.iso.Types;
import org.opengis.feature.Feature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.TypeName;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

/**
 * DOCUMENT ME!
 * 
 * @author Rob Atkinson
 * @version $Id$
 */
public class TimeSeriesTest extends TestCase {
    private static final Logger LOGGER = Logger
            .getLogger(TimeSeriesTest.class.getPackage().getName());

    private static final String AWNS = "http://brs.gov.au/awdip/0.2";

    private static final String CVNS = "http://www.opengis.net/cv/0.0";
    
    private static final String SANS = "http://www.seegrid.csiro.au/xml/sampling";

    private static final String OMNS = "http://www.opengis.net/om";

    private static final String SWENS = "http://www.opengis.net/swe";

    private static final String GMLNS = "http://www.opengis.net/gml";

    private static final String GEONS = "http://www.seegrid.csiro.au/xml/geometry";

    final String schemaBase = "/test-data/";

    EmfAppSchemaReader reader;

    private FeatureSource2 source;

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception
     *             DOCUMENT ME!
     */
    protected void setUp() throws Exception {
        super.setUp();
        reader = EmfAppSchemaReader.newInstance();
        //Logging.GEOTOOLS.forceMonolineConsoleOutput(Level.FINEST);
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception
     *             DOCUMENT ME!
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * 
     * @param location
     *            schema location path discoverable through
     *            getClass().getResource()
     */
    private void loadSchema(String location) throws IOException {
        // load needed GML types directly from the gml schemas
        URL schemaLocation = getClass().getResource(location);
        assertNotNull(location, schemaLocation);
        reader.parse(schemaLocation);
    }

    /**
     * Tests if the schema-to-FM parsing code developed for complex datastore
     * configuration loading can parse the GeoSciML types
     * 
     * @throws Exception
     */
    public void testParseSchema() throws Exception {
        /*
         * not found types and elements:
         */

        // load geosciml schema

        try {
            loadSchema(schemaBase + "SampleSite.xsd");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        Map typeRegistry = reader.getTypeRegistry();

        TypeName typeName = Types.typeName(AWNS, "SiteSinglePhenomTimeSeriesType");
        ComplexType testType = (ComplexType) typeRegistry.get(typeName);
        assertNotNull(testType);
        assertTrue(testType instanceof FeatureType);

        AttributeType superType = testType.getSuper();
        assertNotNull(superType);
        TypeName superTypeName = Types.typeName(AWNS, "SamplingSiteType");
        assertEquals(superTypeName, superType.getName());
        assertTrue(superType instanceof FeatureType);
/*
        // ensure all needed types were parsed and aren't just empty proxies
        Collection properties = borehole.getProperties();
        assertEquals(16, properties.size());
        Map expectedNamesAndTypes = new HashMap();
        //from gml:AbstractFeatureType
        expectedNamesAndTypes.put(name(GMLNS, "metaDataProperty"), typeName(GMLNS, "MetaDataPropertyType"));
        expectedNamesAndTypes.put(name(GMLNS, "description"), typeName(GMLNS, "StringOrRefType"));
        expectedNamesAndTypes.put(name(GMLNS, "name"), typeName(GMLNS, "CodeType"));
        expectedNamesAndTypes.put(name(GMLNS, "boundedBy"), typeName(GMLNS, "BoundingShapeType"));
        expectedNamesAndTypes.put(name(GMLNS, "location"), typeName(GMLNS, "LocationPropertyType"));
        //from sa:ProfileType
        expectedNamesAndTypes.put(name(SANS, "begin"), typeName(GMLNS, "PointPropertyType"));
        expectedNamesAndTypes.put(name(SANS, "end"), typeName(GMLNS, "PointPropertyType"));
        expectedNamesAndTypes.put(name(SANS, "length"), typeName(SWENS, "RelativeMeasureType"));
        expectedNamesAndTypes.put(name(SANS, "shape"), typeName(GEONS, "Shape1DPropertyType"));
        //sa:SamplingFeatureType
        expectedNamesAndTypes.put(name(SANS, "member"), typeName(SANS, "SamplingFeaturePropertyType"));
        expectedNamesAndTypes.put(name(SANS, "surveyDetails"), typeName(SANS, "SurveyProcedurePropertyType"));
        expectedNamesAndTypes.put(name(SANS, "associatedSpecimen"), typeName(SANS, "SpecimenPropertyType"));
        expectedNamesAndTypes.put(name(SANS, "relatedObservation"), typeName(OMNS, "AbstractObservationPropertyType"));
        //from xmml:BoreholeType
        expectedNamesAndTypes.put(name(XMMLNS, "drillMethod"), typeName(XMMLNS, "drillCode"));
        expectedNamesAndTypes.put(name(XMMLNS, "collarDiameter"), typeName(GMLNS, "MeasureType"));
        expectedNamesAndTypes.put(name(XMMLNS, "log"), typeName(XMMLNS, "LogPropertyType"));

        for (Iterator it = expectedNamesAndTypes.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Entry) it.next();
            Name dName = (Name) entry.getKey();
            TypeName tName = (TypeName) entry.getValue();
            
            AttributeDescriptor d = (AttributeDescriptor) Types.descriptor(borehole, dName);
            assertNotNull("Descriptor not found: " + dName, d);
            AttributeType type;
            try {
                type = d.getType();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "type not parsed for "
                        + ((AttributeDescriptor) d).getName(), e);
                throw e;
            }
            assertNotNull(type);
            assertNotNull(type.getName());
            assertNotNull(type.getBinding());
            if (tName != null) {
                assertEquals(tName, type.getName());
            }
        }

        TypeName tcl = Types.typeName(SWENS, "TypedCategoryListType");
        AttributeType typedCategoryListType = (AttributeType) typeRegistry.get(tcl);
        assertNotNull(typedCategoryListType);
        assertFalse(typedCategoryListType instanceof ComplexType);
  */
  
    }

    private TypeName typeName(String ns, String localName) {
        return Types.typeName(ns, localName);
    }

    private Object name(String ns, String localName) {
        return new org.geotools.feature.Name(ns, localName);
    }

/*    
    public void testLoadMappingsConfig() throws Exception {
        XMLConfigDigester reader = new XMLConfigDigester();
        URL url = getClass().getResource(schemaBase + "BoreholeTest_properties.xml");

        ComplexDataStoreDTO config = reader.parse(url);

        Set mappings = ComplexDataStoreConfigurator.buildMappings(config);

        assertNotNull(mappings);
        assertEquals(1, mappings.size());

        FeatureTypeMapping mapping = (FeatureTypeMapping) mappings.iterator().next();

        AttributeDescriptor targetFeature = mapping.getTargetFeature();
        assertNotNull(targetFeature);
        assertNotNull(targetFeature.getType());
        assertEquals(XMMLNS, targetFeature.getName().getNamespaceURI());
        assertEquals("Borehole", targetFeature.getName().getLocalPart());

        source = mapping.getSource();
        assertNotNull(source);
        org.geotools.feature.FeatureType schema = source.getSchema();
        String typeName = schema.getTypeName();
        assertEquals("boreholes_denormalized", typeName);

        List groupingAttributes = mapping.getGroupByAttNames();

        assertEquals(4, groupingAttributes.size());

        assertTrue(groupingAttributes.contains("QS"));
        assertTrue(groupingAttributes.contains("NUMB"));
        assertTrue(groupingAttributes.contains("BSUFF"));
        assertTrue(groupingAttributes.contains("RT"));
     
         * assertTrue(groupingAttributes.contains("BGS_ID"));
         * assertTrue(groupingAttributes.contains("NAME"));
         * assertTrue(groupingAttributes.contains("ORIGINAL_N"));
         * assertTrue(groupingAttributes.contains("CONFIDENTI"));
         * assertTrue(groupingAttributes.contains("LENGTHC"));
         * assertTrue(groupingAttributes.contains("SHAPE"));
        

        List attributeMappings = mapping.getAttributeMappings();
        assertEquals(24, attributeMappings.size());

        AttributeMapping attMapping = (AttributeMapping) attributeMappings.get(0);
        assertNotNull(attMapping);
        assertEquals("Borehole", attMapping.getTargetXPath());

        Expression idExpression = attMapping.getIdentifierExpression();
        assertNotNull(idExpression);
        assertTrue(idExpression instanceof Function);
        Function idFunction = (Function) idExpression;
        assertEquals("strConcat", idFunction.getName());
        assertTrue(idFunction.getParameters().get(0) instanceof Literal);
        assertTrue(idFunction.getParameters().get(1) instanceof PropertyName);

        assertEquals(Expression.NIL, attMapping.getSourceExpression());
    }

    public void testDataStore() throws Exception {
        final Map dsParams = new HashMap();
        final URL url = getClass().getResource(schemaBase + "BoreholeTest_properties.xml");
        dsParams.put("dbtype", "complex");
        dsParams.put("url", url.toExternalForm());

        Map propsParams = new HashMap();

        final Name typeName = new org.geotools.feature.Name(XMMLNS, "Borehole");

        FeatureAccess mappingDataStore = (FeatureAccess) DataAccessFinder.createAccess(dsParams);
        assertNotNull(mappingDataStore);
        AttributeDescriptor borehole = (AttributeDescriptor) mappingDataStore.describe(typeName);
        assertNotNull(borehole);
        assertTrue(borehole.getType() instanceof FeatureType);
        FeatureType boreholeType = (FeatureType) borehole.getType();

        FeatureSource2 fSource = (FeatureSource2) mappingDataStore.access(typeName);

        // make a getFeatures request with a nested properties filter.
        // note that the expected result count is set to 65 since that's the
        // number
        // of results I get from a sql select on min_time_d = 'carnian'
        final int EXPECTED_RESULT_COUNT = 10;

        Collection features = fSource.content();

        int resultCount = features.size();
        String msg = "be sure difference in result count is not due to different dataset."
                + " Query used should be min_time_d = 'carnian'";
        assertEquals(msg, EXPECTED_RESULT_COUNT, resultCount);

        Feature feature;
        int count = 0;
        for (Iterator it = features.iterator(); it.hasNext();) {
            feature = (Feature) it.next();
            count++;
        }
        assertEquals(EXPECTED_RESULT_COUNT, count);
  }
*/

}
