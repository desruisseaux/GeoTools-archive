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

import java.net.URI;

import org.geotools.data.Transaction;
import org.geotools.feature.FeatureType;
import org.opengis.catalog.CatalogEntry;

/**
 * @author dzwiers
 */
public interface FeatureTypeEntry extends CatalogEntry {
    public static final String FeatureTypeKey = "org.geotools.data.feature.FeatureTypeEntry.FeatureTypeKey";
    
    // same as (FeatureType)getMetaData(FeatureTypeEntry.FeatureTypeKey)
    FeatureType getFeatureType();
    
    // same as ((FeatureType)getMetaData(FeatureTypeEntry.FeatureTypeKey)).getTypeName();
    String getTypeName();
    
    // same as ((FeatureType)getMetaData(FeatureTypeEntry.FeatureTypeKey)).getNamespace();
    URI getNamespace();
    
    // same as FeatureSource fs = ((FeatureSource)getResource());
    //         fs.setTransaction(t);
    FeatureSource getFeatureSource(Transaction transaction);
}
