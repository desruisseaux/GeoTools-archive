package org.geotools.data;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.catalog.CatalogEntry;
import org.geotools.cs.CoordinateSystem;
import org.geotools.data.AbstractFeatureLocking;
import org.geotools.data.AbstractFeatureSource;
import org.geotools.data.AbstractFeatureStore;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DiffFeatureReader;
import org.geotools.data.EmptyFeatureReader;
import org.geotools.data.EmptyFeatureWriter;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureListenerManager;
import org.geotools.data.FeatureLocking;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FilteringFeatureReader;
import org.geotools.data.InProcessLockingManager;
import org.geotools.data.MaxFeatureReader;
import org.geotools.data.Query;
import org.geotools.data.ReTypeFeatureReader;
import org.geotools.data.Transaction;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;
import org.geotools.filter.Filter;
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
public interface TypeEntry{
    
    public InternationalString getDisplayName();
    
    public InternationalString getDescription();
    
    public FeatureType getFeatureType();
    
    public URL getURL();
    
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
     */
    public FeatureSource getFeatureSource();
    
    public void fireAdded( Feature newFeature, Transaction transaction );
    public void fireRemoved( Feature removedFeature, Transaction transaction );
    public void fireChanged( Feature before, Feature after, Transaction transaction ); 
    
}