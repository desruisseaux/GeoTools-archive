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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.geotools.feature.FeatureType;

/**
 * Another Quick hack of a DataRepository as a bridge to the Opperations api.
 * 
 * This time we are storing by FeatureType, not DataStores will be harned in
 * the configuration of this class.
 * 
 * @author Jody Garnett
 */
public class FeatureSourceRepository implements Repository {	    
	
	/** Map of FeatuerSource by dataStoreId:typeName */
    protected SortedMap featuresources = new TreeMap();
    
    /**
     * All FeatureTypes by dataStoreId:typeName 
     */
	public SortedMap getTypes() {
		return Collections.unmodifiableSortedMap( featuresources );
	}
    /**
     * Retrieve prefix set.
     * 
     * @see org.geotools.data.Catalog#getPrefixes()
     * 
     * @return Set of namespace prefixes
     * @throws IOException
     */
    public Set getPrefixes() throws IOException {
    	Set prefix = new HashSet();
    	for( Iterator i=featuresources.values().iterator(); i.hasNext();){
    		FeatureSource fs = (FeatureSource) i.next();
    		FeatureType schema = fs.getSchema();
    		prefix.add( schema.getNamespace() );
    	}
        return prefix;
    }
    private SortedSet typeNames() throws IOException {
    	SortedSet typeNames = new TreeSet();
    	for( Iterator i=featuresources.values().iterator(); i.hasNext();){
    		FeatureSource fs = (FeatureSource) i.next();
    		FeatureType schema = fs.getSchema();
    		typeNames.add( schema.getTypeName() );
    	}
        return typeNames;    	    	
    }
    /** Map of dataStores by dataStoreId */
    private Map dataStores() {
    	SortedMap dataStores = new TreeMap();    	
    	for( Iterator i=featuresources.entrySet().iterator(); i.hasNext();){
    		Map.Entry entry = (Map.Entry) i.next();
    		String key = (String) entry.getKey();
    		String dataStoreId = key.split(":")[0];
    		FeatureSource fs = (FeatureSource) entry.getValue();
    		dataStores.put( dataStoreId, fs.getDataStore() );    		
    	}        
        return dataStores;    	    	
    }
    private SortedMap types( DataStore ds ) throws IOException {
    	SortedMap map = new TreeMap();
    	String typeNames[] = ds.getTypeNames();
    	for( int i=0; i<typeNames.length; i++){
    		try {
    			map.put( typeNames[i], ds.getSchema( typeNames[i]));
    		}
    		catch (IOException ignore ){
    			// ignore broken featureType
    		}
    	}
    	return map;
    }
    
    /**
     * All FeatureTypes by dataStoreId:typeName 
     */
    public SortedMap types() {
    	return new TreeMap( featuresources );    	
    }

    
    /**
     * Implement lockExists.
     * 
     * @see org.geotools.data.Catalog#lockExists(java.lang.String)
     * 
     * @param lockID
     */
    public boolean lockExists(String lockID) {
        if( lockID == null ) return false;
        DataStore store;
        LockingManager lockManager;
                
        for( Iterator i=dataStores().values().iterator(); i.hasNext(); ){
             store = (DataStore) i.next();
             lockManager = store.getLockingManager();
             if( lockManager == null ) continue; // did not support locking
             if( lockManager.exists( lockID ) ){
                 return true;
             }
        }
        return false;
    }
    /**
     * Implement lockRefresh.
     * <p>
     * Currently it is an error if the lockID is not found. Because if
     * we can't find it we cannot refresh it.
     * </p>
     * <p>
     * Since locks are time sensitive it is impossible to check
     * if a lockExists and then be sure it will still exist when you try to
     * refresh it. Nothing we do can protect client code from this fact, they
     * will need to do with the IOException when (not if) this situation
     * occurs.
     * </p>
     * @see org.geotools.data.Catalog#lockRefresh(java.lang.String, org.geotools.data.Transaction)
     * 
     * @param lockID Authorizataion of lock to refresh
     * @param transaction Transaction used to authorize refresh
     * @throws IOException If opperation encounters problems, or lock not found
     * @throws IllegalArgumentException if lockID is <code>null</code>
     */
    public boolean lockRefresh(String lockID, Transaction transaction) throws IOException{
        if( lockID == null ){
            throw new IllegalArgumentException("lockID required");
        }
        if( transaction == null || transaction == Transaction.AUTO_COMMIT ){
            throw new IllegalArgumentException("Tansaction required (with authorization for "+lockID+")");        
        }
        
        DataStore store;
        LockingManager lockManager;
        
        boolean refresh = false;
        for( Iterator i=dataStores().values().iterator(); i.hasNext(); ){
             store = (DataStore) i.next();
             lockManager = store.getLockingManager();
             if( lockManager == null ) continue; // did not support locking
                          
             if( lockManager.release( lockID, transaction )){
                 refresh = true;    
             }                           
        }
        return refresh;        
    }

    /**
     * Implement lockRelease.
     * <p>
     * Currently it is <b>not</b> and error if the lockID is not found, it may
     * have expired. Since locks are time sensitive it is impossible to check
     * if a lockExists and then be sure it will still exist when you try to
     * release it.
     * </p>
     * @see org.geotools.data.Catalog#lockRefresh(java.lang.String, org.geotools.data.Transaction)
     * 
     * @param lockID Authorizataion of lock to refresh
     * @param transaction Transaction used to authorize refresh
     * @throws IOException If opperation encounters problems
     * @throws IllegalArgumentException if lockID is <code>null</code>
     */
    public boolean lockRelease(String lockID, Transaction transaction) throws IOException{
        if( lockID == null ){
            throw new IllegalArgumentException("lockID required");
        }
        if( transaction == null || transaction == Transaction.AUTO_COMMIT ){
            throw new IllegalArgumentException("Tansaction required (with authorization for "+lockID+")");        
        }
    
        DataStore store;
        LockingManager lockManager;
                
        boolean release = false;                
        for( Iterator i=dataStores().values().iterator(); i.hasNext(); ){
             store = (DataStore) i.next();
             lockManager = store.getLockingManager();
             if( lockManager == null ) continue; // did not support locking
         
             if( lockManager.release( lockID, transaction )){
                 release = true;    
             }             
        }
        return release;        
    }

    /**
     * Implement registerDataStore.
     * <p>
     * Description ...
     * </p>
     * @see org.geotools.data.Catalog#registerDataStore(org.geotools.data.DataStore)
     * 
     * @param dataStore
     * @throws IOException
     */
    public void register(String id, FeatureSource featureSource) throws IOException {
    	featuresources.put( id+":"+featureSource.getSchema().getTypeName(), featureSource );        
    }

    /**
     * Implement getDataStores.
     * <p>
     * Description ...
     * </p>
     * @see org.geotools.data.Catalog#getDataStores(java.lang.String)
     * 
     * @param namespace
     * @return
     */
    public DataStore datastore(String id ) {    	
    	Set prefix = new HashSet();
    	for( Iterator i=featuresources.entrySet().iterator(); i.hasNext();){
    		Map.Entry entry = (Map.Entry) i.next();
    		String key = (String) entry.getKey();
    		String dataStoreId = key.split(":")[0];
    		if( id.equals( dataStoreId )){
    			FeatureSource fs = (FeatureSource) entry.getValue();
    			return fs.getDataStore();
    		}
    	}
        return null;        
    }
    
    /**
     * Access to the set of registered DataStores.
     * <p>
     * The provided Set may not be modified :-)
     * </p>
     * @see org.geotools.data.Catalog#getDataStores(java.lang.String)
     * 
     * @param namespace
     * @return
     */
    public Map getDataStores() {
    	return Collections.unmodifiableMap( dataStores() );
    }
    public FeatureSource source( String dataStoreId, String typeName ) throws IOException{
    	String typeRef = dataStoreId+":"+typeName;
    	return (FeatureSource) featuresources.get( typeRef );
    }
}