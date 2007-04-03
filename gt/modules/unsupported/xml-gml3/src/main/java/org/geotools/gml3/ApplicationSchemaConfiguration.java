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
import java.io.File;
import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xs.XSConfiguration;


/**
 * An xml configuration for application schemas.
 * <p>
 * This Configuration expects the namespace and schema location URI of the main
 * xsd file for a given application schema and is able to resolve the schema
 * location for the includes and imports as well as they're defined as relative
 * paths and the provided <code>schemaLocation</code> is a file URI.
 * </p>
 *
 * @author Justin Deoliveira, The Open Planning Project
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @since 2.4
 */
public class ApplicationSchemaConfiguration extends Configuration {
    /** application schema namespace */
    private String namespace;

    /** location of the application schema itself */
    private String schemaLocation;

    public ApplicationSchemaConfiguration(String namespace, String schemaLocation) {
        this.namespace = namespace;
        this.schemaLocation = schemaLocation;
        addDependency(new XSConfiguration());
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
                /**
                 * Uses the <code>schema.getSchemaLocation()</code>'s parent
                 * folder as the base folder to resolve <code>location</code> as a
                 * relative URI of.
                 * <p>
                 * This way, application schemas splitted over multiple files can be
                 * resolved based on the relative location of a given import or
                 * include.
                 * </p>
                 *
                 * @param schema
                 *            the schema being resolved
                 * @param uri
                 *            not used as it might be an empty string when location
                 *            refers to an include
                 * @param location
                 *            the xsd location, either of <code>schema</code>, an
                 *            import or an include, for which to try resolving it as
                 *            a relative path of the <code>schema</code> location.
                 * @return a file: style uri with the resolved schema location for
                 *         the given one, or <code>null</code> if
                 *         <code>location</code> can't be resolved as a relative
                 *         path of the <code>schema</code> location.
                 */
                public String resolveSchemaLocation(XSDSchema schema, String uri, String location) {
                    String schemaLocation;

                    if (schema == null) {
                        schemaLocation = ApplicationSchemaConfiguration.this.schemaLocation;
                    } else {
                        schemaLocation = schema.getSchemaLocation();
                    }

                    String locationUri = null;

                    if ((null != schemaLocation) && !("".equals(schemaLocation))) {
                        String schemaLocationFolder = schemaLocation;
                        int lastSlash = schemaLocation.lastIndexOf('/');

                        if (lastSlash > 0) {
                            schemaLocationFolder = schemaLocation.substring(0, lastSlash);
                        }

                        if (schemaLocationFolder.startsWith("file:")) {
                            schemaLocationFolder = schemaLocationFolder.substring(5);
                        }

                        File locationFile = new File(schemaLocationFolder, location);

                        if (locationFile.exists()) {
                            locationUri = locationFile.toURI().toString();
                        }
                    }

                    if ((locationUri == null) && (location != null) && location.startsWith("http:")) {
                        locationUri = location;
                    }

                    return locationUri;
                }
            };
    }
}
