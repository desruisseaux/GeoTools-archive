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

import java.io.IOException;

/**
 * Quick hack of a DataRepository allows me to bridge the existing DataStore
 * API with these experiments for a Opperations api.
 * 
 * I have used the old DefaultCatalaog as a starting point.
 * 
 * This also serves as a reminder that we need CrossDataStore functionality
 * - at least for Locks. And possibly for "Query". 
 * 
 * @author Jody Garnett
 */
public class DataRepository2 extends DataRepository {	    
	
    /** FeatureView wrapper on FeatureSource. 
     * @throws IOException*/
    public FeatureView view( String dataStoreId, String typeName ) throws IOException{    	
    	final FeatureSource fs = null;
    	return new DefaultFeatureView(fs);
    }
}