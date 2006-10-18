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
package org.geotools.gml3.bindings;

import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Element;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import org.geotools.feature.Feature;
import org.geotools.gml3.GML3TestSupport;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;


public class AbstractFeatureTypeBindingTest extends GML3TestSupport {
    protected Configuration createConfiguration() {
        return new TestConfiguration();
    }

    protected void registerNamespaces(Element root) {
        super.registerNamespaces(root);
        root.setAttribute("xmlns:test", TEST.NAMESPACE);
    }

    public void testWithoutGmlProperties() throws Exception {
        if (true) {
            return;
        }

        Element testFeature = GML3MockData.element(new QName(TEST.NAMESPACE, "TestFeature"),
                document, document);
        Element geom = GML3MockData.element(new QName(TEST.NAMESPACE, "geom"), document, testFeature);
        GML3MockData.point(document, geom);

        Element count = GML3MockData.element(new QName(TEST.NAMESPACE, "count"), document,
                testFeature);
        count.appendChild(document.createTextNode("1"));

        Feature feature = (Feature) parse();
        assertNotNull(feature);
    }

    static class TEST {
        public static String NAMESPACE = "http://www.geotools.org/test";
    }

    static class TestConfiguration extends Configuration {
        public TestConfiguration() {
            addDependency(new GMLConfiguration());
        }

        public String getNamespaceURI() {
            return TEST.NAMESPACE;
        }

        public URL getSchemaFileURL() throws MalformedURLException {
            return getClass().getResource("AbstractFeatureTypeBindingTest.xsd");
        }

        public BindingConfiguration getBindingConfiguration() {
            return new BindingConfiguration() {
                    public void configure(MutablePicoContainer container) {
                        //no bindings
                    }
                };
        }

        public XSDSchemaLocationResolver getSchemaLocationResolver() {
            return new XSDSchemaLocationResolver() {
                    public String resolveSchemaLocation(XSDSchema schema, String namespaceURI,
                        String schemaLocationURI) {
                        if (getNamespaceURI().equals(namespaceURI)) {
                            try {
                                return getSchemaFileURL().toString();
                            } catch (MalformedURLException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        return null;
                    }
                };
        }
    }
}
