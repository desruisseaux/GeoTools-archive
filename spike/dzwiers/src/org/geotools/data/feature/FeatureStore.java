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
package org.geotools.data.feature;

import java.io.IOException;

import org.geotools.data.Transaction;
import org.geotools.feature.Feature;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;

public interface FeatureStore extends FeatureSource {
    
    void setTransaction(Transaction transaction);

    boolean del(String fid) throws IOException;
    
    boolean del(Filter filter) throws IOException;
    
    boolean add(Feature feature) throws IOException;
    
    boolean set(String fid, Feature feature) throws IOException;
    
    boolean set(Filter filter, Expression expr) throws IOException;
}
