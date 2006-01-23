package org.geotools.data.pickle;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.geotools.data.AbstractDataStore;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.EmptyFeatureReader;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;

/**
 * Pickel is really not very good, this is a simple wrap to let it
 * live on as a zombie.
 * <p>
 * Any chance of people picking this up, adding index support to it and
 * making it sing and dance? Please...
 * </p>
 * @author jgarnett
 * @source $URL$
 */
public class PickleDataStore extends AbstractDataStore {
    PickleDataSource zombie;
    /**
     * Construct MapInfoDataStore and attach it to the specified file.
     * <p>
     * TODO: Hook this up with the directory datastore.
     * </p>
     * 
     * @param url location of the mif file to read
     * @throws MalformedURLException
     * @throws MalformedURLException invalid URL was used
     */    
    public PickleDataStore( File parent, String name ) throws IOException {
        super( true );
        zombie = new PickleDataSource( parent, name );
    }

    FeatureType schema = null;
    public void createSchema(FeatureType featureType) throws IOException {
        if( schema == null ){
            schema = featureType;
        }
        else {
            throw new IOException( "DataStore already in use for "+schema.getTypeName() );
        }
    }
    /**
     * Return featureType from pickle or null if nothing is there.
     * <p>
     * To bootstrap the process for an empty pickel use createSchema( featureType )
     * - like normal?
     * </p>
     */
    private final FeatureType getSchema(){
        if( schema == null ){
            try {
                final FeatureCollection features = FeatureCollections.newCollection();
                zombie.getFeatures( features, new DefaultQuery(null, null, Filter.NONE, 1, Query.NO_NAMES, "getSchema") );
                FeatureIterator it = features.features();
                Feature feature = it.next();        
                schema = feature.getFeatureType();
            }
            catch (Throwable bad ){
                return null;
            }
        }
        return schema;
    }
    
    public String[] getTypeNames() {        
        return new String[]{ getSchema().getTypeName(), };        
    }
    
    public FeatureType getSchema(String typeName) throws IOException {
        FeatureType type = getSchema();
        if( typeName != null && typeName.equals( type.getTypeName() )){
            return type;
        }
        throw new IOException( "Requested typeName '"+typeName+"' not found");
    }
    
    protected FeatureReader getFeatureReader(String typeName, Query query)
            throws IOException {
        FeatureCollection features = FeatureCollections.newCollection();
        zombie.getFeatures( features, query );
        if( features.isEmpty() ){
            return new EmptyFeatureReader( getSchema() );            
        }
        return DataUtilities.reader( features );
        
    }
    protected FeatureReader getFeatureReader(String typeName)
            throws IOException {
        return getFeatureReader( typeName, Query.ALL );
    }
    protected FeatureWriter getFeatureWriter(String typeName)
            throws IOException {
        FeatureCollection oldFeatures = FeatureCollections.newCollection();
        zombie.getFeatures( oldFeatures, Query.ALL );
        System.out.println( "write over:"+ oldFeatures.size() );
        final FeatureIterator iterator = oldFeatures.features();                
        return new FeatureWriter(){
            FeatureCollection features = FeatureCollections.newCollection();
            FeatureType schema = getSchema();
            Feature live = null;
            Feature next = null;
            
            public FeatureType getFeatureType() {
                return schema;
            }

            public Feature next() throws IOException {
                if( hasNext() ){
                    live = next;
                    next = null;
                    return live;
                }
                try {
                    live =  DataUtilities.template( schema );
                    return live;
                } catch (IllegalAttributeException e) {
                    throw new DataSourceException( "Could not use schema "+schema );
                }
            }
            public void remove() throws IOException {
                live = null; // ie it won't get writen out                
            }

            public void write() throws IOException {
                if( live != null ){
                    features.add( live );
                    live = null;
                }                
            }
            public boolean hasNext() throws IOException {
                live = null;
                if( next != null ){
                    return true;
                }
                if( iterator.hasNext() ){
                    next = iterator.next();
                    return next != null;
                }
                return false;
            }
            public void close() throws IOException {
                live = null;
                next=null;
                zombie.setFeatures( features );                
            }            
        };        
    }
}
