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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

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
public class DataRepository {	    
	
	/** Map of DataStore by dataStoreId */
    protected SortedMap datastores = new TreeMap();
    
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
    	for( Iterator i=datastores.values().iterator(); i.hasNext();){
    		DataStore ds = (DataStore) i.next();
    		for( Iterator t = types( ds ).values().iterator(); t.hasNext();){
    			FeatureType schema = (FeatureType) t.next();
    			prefix.add( schema.getNamespace() );
    		}
    	}
        return prefix;
    }
    private SortedSet typeNames( DataStore ds ) throws IOException {
    	return new TreeSet( Arrays.asList( ds.getTypeNames() ));    	
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
    
    /** All FeatureTypes by dataStoreId:typeName 
     * @throws IOException*/
    public SortedMap types() throws IOException {
    	SortedMap map = new TreeMap();
    	for( Iterator i=datastores.entrySet().iterator(); i.hasNext();){
    		Map.Entry entry = (Map.Entry) i.next();
    		String id = (String) entry.getKey();
    		DataStore ds = (DataStore) entry.getValue();
    		for( Iterator t = types( ds ).values().iterator(); t.hasNext();){
    			FeatureType schema = (FeatureType) t.next();
    			map.put( id+":"+schema.getTypeName(), schema );
    		}
    	}
        return map;
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
                
        for( Iterator i=datastores.values().iterator(); i.hasNext(); ){
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
        for( Iterator i=datastores.values().iterator(); i.hasNext(); ){
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
        for( Iterator i=datastores.values().iterator(); i.hasNext(); ){
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
    public void register(String id, DataStore dataStore) throws IOException {
        if( datastores.containsKey( id ) ){        	
            throw new IOException("ID already registered");
        }
        if( datastores.containsValue( dataStore ) ){        	
            throw new IOException("dataStore already registered");
        }
        datastores.put( id, dataStore );
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
        return (DataStore) datastores.get( id );
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
    public Set getDataStores() {
    	return Collections.unmodifiableSet( new HashSet( datastores.values()) );
    }
    
    /** FeatureView wrapper on FeatureSource. 
     * @throws IOException*/
    public FeatureView view( String dataStoreId, String typeName ) throws IOException{
    	DataStore ds = datastore( dataStoreId );
    	final FeatureSource fs = ds.getFeatureSource( typeName );
    	return new FeatureView(){
			public FeatureType getSchema() {
				return fs.getSchema();
			}

			public FeatureReader reader() throws IOException {
				return fs.getFeatures().reader();
			}

			public Envelope bounds() {
				Envelope bounds = null;
				try {
					bounds = fs.getBounds();
				}
				catch( IOException ignore ){}
				if( bounds != null ) return bounds;
				try {
					return fs.getFeatures().getBounds();
				} catch (IOException e) {
					return null;
				}
			}

			public int count() {
				int count = -1;
				try {
					count = fs.getCount( Query.ALL );
				}
				catch( IOException ignore ){}
				if( count != -1 ) return count;
				try {
					return fs.getFeatures().getCount();
				} catch (IOException e) {
					return -1;
				}
			}

			public FeatureView name(String typeName) {
				return null;
			}

			public FeatureView prefix(String prefix) {
				// TODO Auto-generated method stub
				return null;
			}

			public FeatureView cs(CoordinateReferenceSystem crs) {
				// TODO Auto-generated method stub
				return null;
			}

			public FeatureView reproject(CoordinateReferenceSystem crs) {
				// TODO Auto-generated method stub
				return null;
			}

			public FeatureView as(String[] attributes) {
				// TODO Auto-generated method stub
				return null;
			}

			public FeatureView as(String attribute, Expression expr) {
				// TODO Auto-generated method stub
				return null;
			}

			public FeatureView as(String attribute, String xpath) {
				// TODO Auto-generated method stub
				return null;
			}

			public FeatureView as(As[] as) {
				// TODO Auto-generated method stub
				return null;
			}

			public FeatureView as(List asList) {
				// TODO Auto-generated method stub
				return null;
			}

			public FeatureView filter(Filter filter) {
				// TODO Auto-generated method stub
				return null;
			}

			public FeatureView join(FeatureView view) {
				// TODO Auto-generated method stub
				return null;
			}

			public FeatureView join(FeatureView view, Filter expression) {
				// TODO Auto-generated method stub
				return null;
			}
    	};
    }
}