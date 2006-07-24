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
package org.geotools.filter;

import org.xml.sax.ContentHandler;


/**
 * Interface to recieve filters from the filter sax parsing classes.  Should be
 * implemented by classes that want the sax parsing classes to pass on their
 * filter information.
 *
 * @author Rob Hranac, Vision for New York
 * @source $URL$
 * @version $Id$
 */
public interface FilterHandler extends ContentHandler {
    /**
     * Method to recieve the filters from the sax processing.
     *
     * @param filter The filter constructed by the factories.
     */
    void filter(Filter filter);
}
