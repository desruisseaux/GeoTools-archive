/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;

import org.geotools.data.crs.ForceCoordinateSystemFeatureResults;
import org.geotools.data.crs.ReprojectFeatureResults;
import org.geotools.data.store.EmptyFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;


/**
 * This is a starting point for providing your own FeatureSource implementation.
 *
 * <p>
 * Subclasses must implement:
 * </p>
 *
 * <ul>
 * <li>
 * getDataStore()
 * </li>
 * <li>
 * getSchema()
 * </li>
 * <li>
 * addFeatureListener()
 * </li>
 * <li>
 * removeFeatureListener()
 * </li>
 * </ul>
 *
 * <p>
 * You may find a FeatureSource implementations that is more specific to your needs - such as
 * JDBCFeatureSource.
 * </p>
 *
 * <p>
 * For an example of this class customized for use please see MemoryDataStore.
 * </p>
 *
 * @author Jody Garnett, Refractions Research Inc
 * @source $URL$
 */
public abstract class AbstractFeatureSource implements FeatureSource {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data");
    
    protected Set hints = Collections.EMPTY_SET;
    
    public AbstractFeatureSource() {
        // just to keep the default constructor around
    }
    
    /**
     * This constructors allows to set the supported hints 
     * @param hints
     */
    public AbstractFeatureSource(Set hints) {
        this.hints = Collections.unmodifiableSet(new HashSet(hints));
    }
    
    /**
     * Retrieve the Transaction this FeatureSource is operating against.
     *
     * <p>
     * For a plain FeatureSource that cannot modify this will always be Transaction.AUTO_COMMIT.
     * </p>
     *
     * @return Transacstion FeatureSource is operating against
     */
    public Transaction getTransaction() {
        return Transaction.AUTO_COMMIT;
    }
    
    /**
     * Provides an interface to for the Results of a Query.
     *
     * <p>
     * Various queries can be made against the results, the most basic being to retrieve Features.
     * </p>
     *
     * @param query
     *
     *
     * @see org.geotools.data.FeatureSource#getFeatures(org.geotools.data.Query)
     */
    public FeatureCollection getFeatures(Query query) throws IOException {
    	SimpleFeatureType schema = getSchema();        
        String typeName = schema.getTypeName();
        
        if( query.getTypeName() == null ){ // typeName unspecified we will "any" use a default
            DefaultQuery defaultQuery = new DefaultQuery(query);
            defaultQuery.setTypeName( typeName );
        }
        else if ( !typeName.equals( query.getTypeName() ) ){
            return new EmptyFeatureCollection( schema );
        }
        
        FeatureCollection collection = new DefaultFeatureResults(this, query);
        if( collection.getDefaultGeometry() == null ){
            return collection; // no geometry no reprojection needed
        }
        
        if( false ){ // we need to have our CRS forced
            if ( query.getCoordinateSystem() != null ){
                try {
                    collection = new ForceCoordinateSystemFeatureResults(collection, query.getCoordinateSystem() );
                } catch (SchemaException e) {
                    throw new IOException( "Could not force CRS "+query.getCoordinateSystem() ); 
                }
            }
        }
        if( false ){ // we need our data reprojected
            if ( query.getCoordinateSystemReproject() != null){
                try {
                    collection = new ReprojectFeatureResults(collection, query.getCoordinateSystemReproject() );
                } catch (Exception e) {
                    throw new IOException( "Could not reproject to "+query.getCoordinateSystemReproject() );
                }
            }            
        }
        return collection;
    }
    
    /**
     * Retrieve all Feature matching the Filter.
     *
     * @param filter Indicates features to retrieve
     *
     * @return FeatureResults indicating features matching filter
     *
     * @throws IOException If results could not be obtained
     */
    public FeatureCollection getFeatures(Filter filter) throws IOException {
        return getFeatures(new DefaultQuery(getSchema().getTypeName(), filter));
    }
    
    /**
     * Retrieve all Features.
     *
     * @return FeatureResults of all Features in FeatureSource
     *
     * @throws IOException If features could not be obtained
     */
    public FeatureCollection getFeatures() throws IOException {
        return getFeatures(Filter.INCLUDE);
    }
    
    /**
     * Retrieve Bounds of all Features.
     *
     * <p>
     * Currently returns null, consider getFeatures().getBounds() instead.
     * </p>
     *
     * <p>
     * Subclasses may override this method to perform the appropriate optimization for this result.
     * </p>
     *
     * @return null representing the lack of an optimization
     *
     * @throws IOException DOCUMENT ME!
     */
    public Envelope getBounds() throws IOException {
//        return getBounds(Query.ALL); // DZ should this not return just the bounds for this type?
        return getBounds(getSchema()==null?Query.ALL:new DefaultQuery(getSchema().getTypeName()));
    }
    
    /**
     * Retrieve Bounds of Query results.
     *
     * <p>
     * Currently returns null, consider getFeatures( query ).getBounds() instead.
     * </p>
     *
     * <p>
     * Subclasses may override this method to perform the appropriate optimization for this result.
     * </p>
     *
     * @param query Query we are requesting the bounds of
     *
     * @return null representing the lack of an optimization
     *
     * @throws IOException DOCUMENT ME!
     */
    public Envelope getBounds(Query query) throws IOException {
        if (query.getFilter() == Filter.EXCLUDE) {
            return new Envelope();
        }
        
        DataStore dataStore = getDataStore();
        
        if ((dataStore == null) || !(dataStore instanceof AbstractDataStore)) {
            // too expensive
            return null;
        } else {
            // ask the abstract data store
            return ((AbstractDataStore) dataStore).getBounds( namedQuery( query ) );
        }
    }
    /**
     * Ensure query modified with typeName.
     * <p>
     * This method will make copy of the provided query, using
     * DefaultQuery, if query.getTypeName is not equal to
     * getSchema().getTypeName().
     * </p>
     * @param query Original query
     * @return Query with getTypeName() equal to getSchema().getTypeName()
     */
    protected Query namedQuery( Query query ){
        String typeName = getSchema().getTypeName();
        if( query.getTypeName() == null ||
                !query.getTypeName().equals( typeName )){
            
            return new DefaultQuery(
                    typeName,
                    query.getFilter(),
                    query.getMaxFeatures(),
                    query.getPropertyNames(),
                    query.getHandle()
                    );
        }
        return query;
    }
    
    /**
     * Retrieve total number of Query results.
     *
     * <p>
     * Currently returns -1, consider getFeatures( query ).getCount() instead.
     * </p>
     *
     * <p>
     * Subclasses may override this method to perform the appropriate optimization for this result.
     * </p>
     *
     * @param query Query we are requesting the count of
     *
     * @return -1 representing the lack of an optimization
     */
    public int getCount(Query query) throws IOException {
        if (query.getFilter() == Filter.EXCLUDE) {
            return 0;
        }
        
        DataStore dataStore = getDataStore();
        if ((dataStore == null) || !(dataStore instanceof AbstractDataStore)) {
            // too expensive
            return -1;
        } else {
            // ask the abstract data store
            Transaction t = getTransaction();
            //State state = t.getState(dataStore);
            int delta = 0;
            if(t != Transaction.AUTO_COMMIT){
                if (!(t.getState(dataStore) instanceof TransactionStateDiff)) {
                	//we cannot proceed; abort!
                	return -1;
                }
            	Diff diff = ((AbstractDataStore)dataStore).state(t).diff(namedQuery(query).getTypeName());
            	synchronized (diff) {
            		Iterator it = diff.added.values().iterator();
            		while(it.hasNext()){
            			Object feature = it.next();
            			if( query.getFilter().evaluate(feature) )
            				delta++;
            		}
            		
            		it = diff.modified2.values().iterator();
            		while(it.hasNext()){
            			Object feature = it.next();
            			
            			if(feature==TransactionStateDiff.NULL){
            				delta--;
            			}
            		}
            	}
            }
            return ((AbstractDataStore) dataStore).getCount( namedQuery(query))+delta;
        }
    }
    
    /**
     * By default, no Hints are supported
     */
    public Set getSupportedHints() {
        return hints;
    }
}
