package org.geotools.data.store;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Envelope;

public abstract class AbstractFeatureSource2 implements FeatureSource {

	/** The logger for the data module. */
    protected static final Logger LOGGER = Logger.getLogger("org.geotools.data");
    
    /**
     * the type entry
     */
	protected ActiveTypeEntry entry;
	
	public AbstractFeatureSource2( ActiveTypeEntry entry ) {
		this.entry = entry;
	}
	
	public DataStore getDataStore() {
		return entry.parent;
	}

	public void addFeatureListener(FeatureListener listener) {
		entry.listenerManager.addFeatureListener( this, listener );
	}

	public void removeFeatureListener(FeatureListener listener) {
		entry.listenerManager.removeFeatureListener( this, listener );
	}

	public FeatureCollection getFeatures(Query query) throws IOException {
		 FeatureType featureType = entry.getFeatureType();
		 
		 Filter filter = query.getFilter();
		 if (filter == null) {
            throw new NullPointerException("getFeatureReader requires Filter: "
                + "did you mean Filter.INCLUDE?");
         }
         String propertyNames[] = query.getPropertyNames();
         
         if ( filter == Filter.EXCLUDE || filter.equals( Filter.EXCLUDE )) {
             //return new EmptyFeatureReader(featureType);
             return new EmptyFeatureCollection( featureType );
         }
         //GR: allow subclases to implement as much filtering as they can,
         //by returning just it's unsupperted filter
//         filter = getUnsupportedFilter( filter);
//         if(filter == null){
//             throw new NullPointerException("getUnsupportedFilter shouldn't return null. Do you mean Filter.INCLUDE?");
//         }
         
         //filter
         FeatureCollection features = getFeatures( filter );
         
         //retyping
         if( propertyNames != null || query.getCoordinateSystem() != null ){
             try {
                 FeatureType target = 
                	 DataUtilities.createSubType( featureType, propertyNames, query.getCoordinateSystem() );
                 if ( !featureType.equals( target ) ) {
                	 LOGGER.fine("Recasting feature type to subtype by using a ReTypeFeatureReader");
                	 features = new ReTypingFeatureCollection(features, target); 
                 }
             } catch (SchemaException e) {
                 LOGGER.log( Level.FINEST, e.getMessage(), e);
                 throw new DataSourceException( "Could not create Feature Type for query", e );

             }
         }
       
         //reprojection 
         if ( query.getCoordinateSystemReproject() != null ) {
        	 if ( query.getCoordinateSystem() != null ) {
        		 features = new ReprojectingFeatureCollection( 
    				 features, query.getCoordinateSystem(), query.getCoordinateSystemReproject() 
				 );
        	 }
        	 else {
        		 features = new ReprojectingFeatureCollection( 
        			features, query.getCoordinateSystemReproject()	 
        		 );
        	}
         }
         
         //max feature cap
         if (query.getMaxFeatures() != Query.DEFAULT_MAX) {
             features = new SizeCappedFeatureCollection( features, query.getMaxFeatures() );
         }
         
         return features;
	}

	public FeatureCollection getFeatures(Filter filter) throws IOException {
        //filter
        if ( filter != null && !filter.equals( Filter.INCLUDE ) ) {
            return new FilteringFeatureCollection( getFeatures() , filter );
        }
        
        return getFeatures();
	}

	public FeatureType getSchema() {
		return entry.getFeatureType();
	}

	public Envelope getBounds() throws IOException {
		return getFeatures().getBounds();
	}

	public Envelope getBounds(Query query) throws IOException {
		return getFeatures( query ).getBounds();
	}

	public int getCount(Query query) throws IOException {
		return getFeatures( query ).size();
	}

}
