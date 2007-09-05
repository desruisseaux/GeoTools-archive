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
package org.geotools.gml3;

import junit.framework.TestCase;
import org.apache.xerces.parsers.SAXParser;
import org.eclipse.xsd.XSDSchema;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import org.geotools.feature.FeatureCollection;
import org.geotools.gml3.bindings.TEST;
import org.geotools.gml3.bindings.TestConfiguration;
import org.geotools.xml.Encoder;
import org.geotools.xml.Parser;
import org.geotools.xml.SchemaLocator;


public class GML3EncodingTest extends TestCase {
    public void testWithConfiguration() throws Exception {
        TestConfiguration configuration = new TestConfiguration();

        //first parse in test data
        Parser parser = new Parser(configuration);
        FeatureCollection fc = (FeatureCollection) parser.parse(TestConfiguration.class
                .getResourceAsStream("test.xml"));
        assertNotNull(fc);

        XSDSchema schema = TEST.getInstance().getSchema();
        assertNotNull(schema);

        Encoder encoder = new Encoder(configuration, schema);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        encoder.write(fc, TEST.TestFeatureCollection, output);

        SAXParser saxParser = new SAXParser();
        saxParser.setFeature("http://xml.org/sax/features/validation", true);
        saxParser.setFeature("http://apache.org/xml/features/validation/schema", true);
        saxParser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);

        saxParser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
            TEST.NAMESPACE);

        saxParser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation",
            TEST.NAMESPACE + " " + configuration.getSchemaFileURL());

        final ArrayList errors = new ArrayList();
        DefaultHandler handler = new DefaultHandler() {
                public void error(SAXParseException e)
                    throws SAXException {
                    System.out.println(e.getMessage());
                    errors.add(e);
                }

                public void fatalError(SAXParseException e)
                    throws SAXException {
                    System.out.println(e.getMessage());
                    errors.add(e);
                }
            };

        saxParser.setErrorHandler(handler);
        saxParser.parse(new InputSource(new ByteArrayInputStream(output.toByteArray())));

        assertTrue(errors.isEmpty());
    }

    public void testWithApplicationSchemaConfiguration()
        throws Exception {
        String schemaLocation = new File(TestConfiguration.class.getResource("test.xsd").getFile())
            .getAbsolutePath();

        ApplicationSchemaConfiguration configuration = new ApplicationSchemaConfiguration(TEST.NAMESPACE,
                schemaLocation);

        //first parse in test data
        Parser parser = new Parser(configuration);
        FeatureCollection fc = (FeatureCollection) parser.parse(TestConfiguration.class
                .getResourceAsStream("test.xml"));
        assertNotNull(fc);

        XSDSchema schema = TEST.getInstance().getSchema();
        assertNotNull(schema);

        Encoder encoder = new Encoder(configuration, schema);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        encoder.write(fc, TEST.TestFeatureCollection, output);

        SAXParser saxParser = new SAXParser();
        saxParser.setFeature("http://xml.org/sax/features/validation", true);
        saxParser.setFeature("http://apache.org/xml/features/validation/schema", true);
        saxParser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);

        saxParser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
            TEST.NAMESPACE);

        saxParser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation",
            TEST.NAMESPACE + " " + configuration.getSchemaFileURL());

        final ArrayList errors = new ArrayList();
        DefaultHandler handler = new DefaultHandler() {
                public void error(SAXParseException e)
                    throws SAXException {
                    System.out.println(e.getMessage());
                    errors.add(e);
                }

                public void fatalError(SAXParseException e)
                    throws SAXException {
                    System.out.println(e.getMessage());
                    errors.add(e);
                }
            };

        saxParser.setErrorHandler(handler);
        saxParser.parse(new InputSource(new ByteArrayInputStream(output.toByteArray())));

        assertTrue(errors.isEmpty());
    }
}
