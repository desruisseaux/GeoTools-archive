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

import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;


/**
 * An xml configuration for application schemas.
 * <p>
 *
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 * 
 * TODO: do we need multiple schema locations? what about application schemas that are 
 * split over multiple files?
 *
 */
public class ApplicationSchemaConfiguration extends Configuration {
    /** application schema namespace */
    private String namespace;

    /** location of the application schema itself */
    private String schemaLocation;

    public ApplicationSchemaConfiguration(String namespace, String schemaLocation) {
        this.namespace = namespace;
        this.schemaLocation = schemaLocation;

        addDependency(new GMLConfiguration());
    }

    public String getNamespaceURI() {
        return namespace;
    }

    public String getSchemaFileURL() {
        return schemaLocation;
    }

    public BindingConfiguration getBindingConfiguration() {
        return null;
    }

    public XSDSchemaLocationResolver getSchemaLocationResolver() {
        return new XSDSchemaLocationResolver() {

			public String resolveSchemaLocation(XSDSchema schema, String uri, String location) {
				if ( namespace.equals( uri ) ) {
					return location;
				}
				
				return null;
			}
        	
        };
    }
}
