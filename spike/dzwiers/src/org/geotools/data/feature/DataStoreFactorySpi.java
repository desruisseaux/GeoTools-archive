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

import org.geotools.data.ParamValues;
import org.geotools.factory.Factory;
import org.opengis.catalog.CatalogEntry;

public interface DataStoreFactorySpi extends Factory, CatalogEntry {

    public static final String DESCRIPTION_KEY = "org.geotools.data.DataStoreFactorySpi.DESCRIPTION_KEY";
    public static final String NAME_KEY = "org.geotools.data.DataStoreFactorySpi.NAME_KEY";
    public static final String ISAVAILABLE_KEY = "org.geotools.data.DataStoreFactorySpi.ISAVAILABLE_KEY";
    
    DataStore createDataStore(ParamValues params) throws IOException;
   
    DataStore createNewDataStore(ParamValues params) throws IOException;

    boolean canProcess(ParamValues params);
    
    // same as (String)getMetaData(DataStoreFactorySpi.NAME_KEY)
    String getName();
    
    // same as (String)getMetaData(DataStoreFactorySpi.DESCRIPTION_KEY)
    String getDescription();
    
    // same as ((Boolean)getMetaData(DataStoreFactorySpi.ISAVAILABLE_KEY)).booleanValue()
    boolean isAvailable();
}
