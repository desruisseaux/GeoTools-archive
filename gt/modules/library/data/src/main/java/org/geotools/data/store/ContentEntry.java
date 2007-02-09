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
package org.geotools.data.store;

import java.util.HashMap;
import java.util.Map;

import org.geotools.data.Transaction;
import org.opengis.feature.type.TypeName;

/**
 * Captures all content information that does not change.
 *
 * @author Jody Garnett, Refractions Research Inc.
 */
public abstract class ContentEntry {
    /**
     * TypeName (storngly typed) for this content.
     */
    private final TypeName typeName;
    
    /**
     * Map<Transaction,ContentState> state according to Transaction.
     */
    private final Map state;
    
    /** Backpointer to parent */
    protected ContentDataStore dataStore;
    
    /**
     * Subclass must provide typeName.
     * 
     * @param typeName
     */
    protected ContentEntry( ContentDataStore dataStore, TypeName typeName ){
        this.typeName = typeName;
        this.state = new HashMap();
        
        this.dataStore = dataStore;
        ContentState autoState = dataStore.content.state( this );
        this.state.put( Transaction.AUTO_COMMIT, autoState );
    }
    
    public final TypeName getName() {
        return typeName;
    }
    
    public final String getTypeName(){
        return typeName.getLocalPart();
    }
    
    /**
     * @return Back pointer to the datastore.
     */
    public ContentDataStore getDataStore() {
		return dataStore;
	}
    
    public String toString() {
        return getTypeName() + " ContentEntry";
    }

    /** Grab the per transaction state */
    public final ContentState getState( Transaction transaction ){
        if( state.containsKey( transaction )){
            return (ContentState) state.get( transaction );
        }
        else {
            ContentState auto = (ContentState) state.get( Transaction.AUTO_COMMIT );            
            ContentState copy = (ContentState) auto.copy();
            state.put( transaction, copy );
            
            return copy;
        }
    }    
    
    
}