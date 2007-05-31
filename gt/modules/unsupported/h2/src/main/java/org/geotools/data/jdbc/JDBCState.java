/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.jdbc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geotools.data.FeatureListener;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentState;


/**
 * Functions as the State for a JDBCFeatureSource (all fields should be here).
 * 
 * @author Jody Garnett, Refractions Research Inc.
 * @author Justin Deoliveira, The Open Planning Project
 */
public final class JDBCState extends ContentState {
    private Connection connection;
    
    /**
     * Duplicates provided JDBCState .. for everything except connection & listeners.
     */
    public JDBCState( JDBCState state ){
    	super( state );    	
	}
    public JDBCState( ContentEntry entry ){
    	super( entry );
	}
    public Connection getConnection(){
    	return connection;    	
    }
    public void setConnection( Connection connection ){
    	this.connection = connection;
    }
    public void flush() {
    	connection = null;
    	super.flush();
    }
}
