package org.geotools.data;

import java.io.IOException;

import org.geotools.catalog.CatalogEntry;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.opengis.util.InternationalString;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Starting place for holding information about a FeatureType.
 * <p>
 * Like say for instance the FeatureType, its metadata and so on.
 * </p>
 * <p>
 * The default implemenation should contain enough information to wean
 * us off of AbstractDataStore. That is it should provide its own locking
 * and event notification.
 * </p>
 * <p>
 * There is a naming convention:
 * <ul>
 * <li> data access follows bean conventions: getTypeName(), getSchema()
 * <li> resource access methods follow Collections conventions reader(), 
 * writer(), etc...
 * <li> overrrides are all protected and follow factory conventions:
 *  createWriter(), createAppend(), createFeatureSource(),
 *  createFeatureStore(), etc...
 * </ul>
 * <li>
 * </p>
 * <p>
 * Feedback:
 * <ul>
 * <li>even notification yes
 * <li>locking not - locking needs to be rejuggled
 * <li>naming convention really helps when subclassing
 * </ul>
 * </p>
 * 
 * @author jgarnett
 */
public interface TypeEntry extends CatalogEntry {
    
    public InternationalString getDisplayName();
    
    public InternationalString getDescription();
    
    public FeatureType getFeatureType() throws IOException;
        
    /**
     * Bounding box for associated Feature Collection, will be calcualted as needed.
     * <p>
     * Note bounding box is returned in lat/long - the coordinate system of the default geometry
     * is used to provide this reprojection.
     * </p>
     */
    public Envelope getBounds();
    
    /** Number of features in associated Feature Collection, will be calcualted as needed */
    public int getCount();
    
    /**
     * Create a new FeatueSource allowing interaction with content.
     * <p>
     * Subclass may optionally implement:
     * <ul>
     * <li>FeatureStore - to allow read/write access
     * <li>FeatureLocking - for locking support
     * </ul>
     * This choice may even be made a runtime (allowing the api
     * to represent a readonly file).
     * </p>
     * <p>
     * Several default implemenations are provided
     * 
     * @return FeatureLocking allowing access to content.
     * @throws IOException
     */
    public FeatureSource getFeatureSource() throws IOException;
    
    public void fireAdded( Feature newFeature, Transaction transaction );
    public void fireRemoved( Feature removedFeature, Transaction transaction );
    public void fireChanged( Feature before, Feature after, Transaction transaction ); 
    
    /**
     * Equals based only on resource definition information (not connection information).
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     * @param obj
     * @return true if TypeEntry represents the same resource
     */
    public boolean equals( Object obj );

    /**
     * This hashcode is *VERY* important!
     * <p>
     * The hascode must be dependent only on the parameters that "define"
     * the resource, not those that control opperation.
     * <ul>
     * <li>when representing a URL the hashcode must be: url.hashCode()
     * <li>when representing a File the hashcode must be: file.toURL().hashcode()
     * <li>when representing a database connection: hascode of jdbc url w/ out username, password
     * </ul> 
     * </p>
     * <p>
     * Implemetnation tip - URL.hashCode is a blocking operation, so you calculate and cache when the URL changes,
     * rather than block this method.
     * </p>
     * @see java.lang.Object#hashCode()
     * @return hashCode based on resource definition
     */
    public int hashCode();

}