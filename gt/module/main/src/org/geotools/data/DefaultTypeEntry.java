/*
 * Created on Sep 1, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.geotools.data;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.util.SimpleInternationalString;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Default impelmentation of TypeEntry.
 * <p>
 * This implemenation is entirely dependent on the DataStore api, it
 * does cache values and listen to events.
 * </p><p>
 * For an alternative take see data.store.*, AbstractDataStore2 is entirly
 * dependendent on TypeEntry_GT.
 * 
 * @author jgarnett
 * @since 0.3
 */
public class DefaultTypeEntry implements TypeEntry {
    private final DataStore store;
    private final URI namespace;
    private final String typeName;
    private Map metadata;
    
    /** Host or Server providing the information */
    private String host;
    
    /** Name suitable for display to end user's */
    private InternationalString displayName;
    
    /** Describe the nature of the datasource constructed by this factory */
    private InternationalString description;
    
    /** FeatureSource used for event notificaiton */
    FeatureSource source;
    
    /** Cached count */
    int count;
    
    /** Cached bounds */
    Envelope bounds;    
    
    public DefaultTypeEntry( DataStore store, URI namespace, String typeName ){
        this.store = store;
        this.namespace = namespace;
        this.typeName = typeName;
        this.displayName = new SimpleInternationalString( typeName.substring(0,1).toUpperCase() + typeName.substring(1).toLowerCase() );
        this.description = new SimpleInternationalString( "Feature type "+typeName );
        try {
            source = store.getFeatureSource( typeName );
            source.addFeatureListener( new FeatureListener(){
                public void changed(FeatureEvent featureEvent) {
                    reset();
                }                
            });            
        } catch (IOException e) {
            
        }
    } 

    public Object resource() {
        try {
            return getFeatureSource(); 
        } catch (IOException e) {
            return null;
        }
    }
    /**
     * Aquire featuresource 
     * 
     * @see org.geotools.data.TypeEntry#getFeatureSource()
     * @return
     * @throws IOException
     */
    public FeatureSource getFeatureSource() throws IOException  {
        return this.store.getFeatureSource( typeName );
    }
    /**
     * Aquire featureType.
     * 
     * @see org.geotools.data.TypeEntry#getFeatureType()
     * @return
     * @throws IOException
     */
    public FeatureType getFeatureType() throws IOException {
        return store.getSchema( typeName );
    }
    /**
     * Access to map of metadata by name.
     * 
     * @see org.geotools.catalog.CatalogEntry#metadata()
     * 
     * @return Map of metadata by name
     */
    public synchronized Map metadata() {
        if( metadata == null ) {
            metadata = createMetadata();
        }
        return Collections.unmodifiableMap( metadata );
    }
    /**
     * Lazy creatation of metdata, subclass overrride recomended.
     * <p>
     * By default returns an empty Map
     * </p>
     * @return Map of fresh metadata
     */
    protected Map createMetadata() {
        return Collections.EMPTY_MAP;
    }
    
    /**
     * Quick access to metadata.keySet.
     * 
     * @see org.geotools.catalog.CatalogEntry#getMetadataNames()
     * @return Metadata names
     */
    public String[] getMetadataNames() {
        return (String[]) metadata.keySet().toArray( new String[ metadata().size() ] );
    }
    
    public boolean equals(Object obj){
    	if( !(obj instanceof DefaultTypeEntry) ){
    		return false;
    	}
    	DefaultTypeEntry entry=(DefaultTypeEntry) obj;
    	return typeName.equals(entry.typeName) &&
               namespace.equals( entry.namespace ) &&
               store==entry.store;
    }
    /**
     * Hashcode based on typename, uri and store.
     * 
     * @return hashcode based on typename, entry and uri.
     */
    public int hashCode() {
        return typeName.hashCode() ^
               namespace.hashCode() << 3 ^
               store.hashCode() << 6;
    }

    /** Called to reset the count and bounds, they will be recalculated as needed */     
    public synchronized void reset(){
        count = -1;
        bounds = null;
    }
    public InternationalString getDisplayName() {
        return displayName;
    }
    /** namespace:typename */
    public String getDataName() {
        return namespace + ":" + typeName;        
    }
    public InternationalString getDescription() {
        return description;
    }
    public URI getNamespace(){
        return namespace;
    }
    public String getTypeName() {
        return typeName;
    }
    /**
     * Bounding box for associated Feature Collection, will be calcualted as needed.
     * <p>
     * Note bounding box is returned in lat/long - the coordinate system of the default geometry
     * is used to provide this reprojection.
     * </p>
     */
    public synchronized Envelope getBounds() {        
        if( bounds == null ) {
            bounds = createBounds();            
        }
        return bounds;        
    }
    /**
     * Override to provide your own optimized calculation of bbox.
     * <p>
     * Default impelmenation uses the a feature source.
     * 
     * @return BBox in lat long
     */
    protected Envelope createBounds() {
        Envelope bbox;
        try {
            bbox = source.getBounds();
            if( bbox == null ){
                bbox = source.getFeatures().getBounds();
            }
            try {
                CoordinateReferenceSystem cs = source.getSchema().getDefaultGeometry().getCoordinateSystem();
                bbox = CRSService.toGeographic( bbox, cs );
            }
            catch (Throwable badRepoject ) {
                badRepoject.printStackTrace();
            }
        } catch (Exception e) {
            bbox = new Envelope();
        }        
        return bbox;        
    }
    
    /** Number of features in associated Feature Collection, will be calcualted as needed */
    public synchronized int getCount() {
        if( count != -1 ) return count;
        try {
            count = source.getCount( Query.ALL );
            if( count == -1 ){
                count = source.getFeatures().getCount();
            }
        } catch (IOException e) {
            bounds = new Envelope();
        }
        return count;
    }
    
    /**
     * TODO summary sentence for fireAdded ...
     * 
     * @see org.geotools.data.TypeEntry#fireAdded(org.geotools.feature.Feature, org.geotools.data.Transaction)
     * @param newFeature
     * @param transaction
     */
    public void fireAdded( Feature newFeature, Transaction transaction ) {
        // need api change to impelemnt
    }
    /**
     * TODO summary sentence for fireChanged ...
     * 
     * @see org.geotools.data.TypeEntry#fireChanged(org.geotools.feature.Feature, org.geotools.feature.Feature, org.geotools.data.Transaction)
     * @param before
     * @param after
     * @param transaction
     */
    public void fireChanged( Feature before, Feature after, Transaction transaction ) {
        // need an api change to implement
    }
    /**
     * TODO summary sentence for fireRemoved ...
     * 
     * @see org.geotools.data.TypeEntry#fireRemoved(org.geotools.feature.Feature, org.geotools.data.Transaction)
     * @param removedFeature
     * @param transaction
     */
    public void fireRemoved( Feature removedFeature, Transaction transaction ) {
        // need an api change to implement
    }
}
