package org.geotools.data.store;

import java.util.HashMap;
import java.util.Map;

import org.geotools.data.Transaction;
import org.opengis.feature.type.Name;
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
    
    final TypeName getName() {
        return typeName;
    }
    
    final String getTypeName(){
        return typeName.getLocalPart();
    }
    
    public String toString() {
        return getTypeName() + " ContentEntry";
    }

    /** Grab the per transaction state */
    ContentState getState( Transaction transaction ){
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