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
import org.picocontainer.MutablePicoContainer;
import java.io.File;
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
    public ApplicationSchemaConfiguration(String namespace, String schemaLocation) {
        super(new ApplicationSchemaXSD(namespace, schemaLocation));
        addDependency(new XSConfiguration());
        addDependency(new GMLConfiguration());
    }

    protected void registerBindings(MutablePicoContainer container) {
        //no bindings
    }
}
