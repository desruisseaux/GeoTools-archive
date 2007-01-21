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
package org.geotools.filter.v1_1;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.xml.Encoder;
import org.geotools.xml.Parser;


public class OGCFilterTest extends TestCase {
    public void testEncode() throws Exception {
        FilterFactory f = CommonFactoryFinder.getFilterFactory(null);
        Filter filter = f.equal(f.property("testString"), f.literal(2), false);

        File file = File.createTempFile("filter", "xml");
        file.deleteOnExit();

        OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
        Encoder encoder = new Encoder(new OGCConfiguration());

        encoder.encode(filter, OGC.PropertyIsEqualTo, output);
        output.flush();
        output.close();

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);

        Document doc = docFactory.newDocumentBuilder().parse(file);

        assertEquals("ogc:PropertyIsEqualTo", doc.getDocumentElement().getNodeName());
        assertEquals(1, doc.getElementsByTagName("ogc:PropertyName").getLength());
        assertEquals(1, doc.getElementsByTagName("ogc:Literal").getLength());

        Element propertyName = (Element) doc.getElementsByTagName("ogc:PropertyName").item(0);
        Element literal = (Element) doc.getElementsByTagName("ogc:Literal").item(0);

        assertEquals("testString", propertyName.getFirstChild().getNodeValue());
        assertEquals("2", literal.getFirstChild().getNodeValue());
    }

    public void testParse() throws Exception {
        Parser parser = new Parser(new OGCConfiguration());
        InputStream in = getClass().getResourceAsStream("test1.xml");

        if (in == null) {
            throw new FileNotFoundException(getClass().getResource("test1.xml").toExternalForm());
        }

        Object thing = parser.parse(in);
        assertEquals(0, parser.getValidationErrors().size());

        assertNotNull(thing);
        assertTrue(thing instanceof PropertyIsEqualTo);

        PropertyIsEqualTo equal = (PropertyIsEqualTo) thing;
        assertTrue(equal.getExpression1() instanceof PropertyName);
        assertTrue(equal.getExpression2() instanceof Literal);

        PropertyName name = (PropertyName) equal.getExpression1();
        assertEquals("testString", name.getPropertyName());

        Literal literal = (Literal) equal.getExpression2();
        assertEquals("2", literal.toString());
    }
}
