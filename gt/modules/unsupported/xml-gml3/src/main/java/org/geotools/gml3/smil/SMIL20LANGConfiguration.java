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
package org.geotools.gml3.smil;

import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import java.net.MalformedURLException;
import java.net.URL;
import org.geotools.gml3.bindings.smil.SMIL20LANG;
import org.geotools.gml3.bindings.smil.SMIL20LANGBindingConfiguration;
import org.geotools.gml3.bindings.smil.SMIL20SchemaLocationResolver;
import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;


public class SMIL20LANGConfiguration extends Configuration {
    /**
     * @return {@link SMIL20LANG#NAMESPACE}
     */
    public String getNamespaceURI() {
        return SMIL20LANG.NAMESPACE;
    }

    /**
     * @return URL to smil20-language.xsd file of schema.
     */
    public String getSchemaFileURL() {
        return getSchemaLocationResolver()
                   .resolveSchemaLocation(null, getNamespaceURI(), "smil20-language.xsd");
    }

    /**
     * @return A new instance of {@link SMIL20LANGBindingConfiguration}
     */
    public BindingConfiguration getBindingConfiguration() {
        return new SMIL20LANGBindingConfiguration();
    }

    /**
     * @return A new instance of {@link SMIL20SchemaLocationResolver}
     */
    public XSDSchemaLocationResolver getSchemaLocationResolver() {
        return new SMIL20SchemaLocationResolver();
    }
}
