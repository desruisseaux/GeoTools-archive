/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.xml.handlers.xsi;

import org.geotools.xml.XSIElementHandler;
import org.geotools.xml.schema.ElementGrouping;
import org.xml.sax.SAXException;


/**
 * <p>
 * Allows the developer to avoid instanceof operators when wishing to compress.
 * </p>
 *
 * @author dzwiers www.refractions.net
 * @source $URL$
 */
public abstract class ElementGroupingHandler extends XSIElementHandler {
    /**
     * <p>
     * This will compress the given element based on it's type and the  parent
     * schema, allowing for references to be resolved.
     * </p>
     *
     * @param parent
     *
     *
     * @throws SAXException
     */
    protected abstract ElementGrouping compress(SchemaHandler parent)
        throws SAXException;
}
