/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data;

import java.net.URL;
import java.util.Iterator;

import org.opengis.catalog.Catalog;
import org.opengis.catalog.QueryResult;

/**
 * @author dzwiers
 */
public interface DataFactory extends Catalog{
  // used to build both raster and feature data sources
    
    // same as query(QueryDefinition) but specific for the url
    QueryResult query(URL url);
    
    // same as query(url).iterator(); 
    Iterator iterator(URL url);
}
