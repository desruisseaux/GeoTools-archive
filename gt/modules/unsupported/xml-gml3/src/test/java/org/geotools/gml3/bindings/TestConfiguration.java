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
import java.net.MalformedURLException;
import java.net.URL;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;


public class TestConfiguration extends Configuration {
    public TestConfiguration() {
        super(TEST.getInstance());
        addDependency(new GMLConfiguration());
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
                        return getSchemaFileURL().toString();
                    }

                    return null;
                }
            };
    }
}
