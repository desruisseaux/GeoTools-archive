package org.geotools.feature.iso.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.geotools.feature.iso.FeatureCollections;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.FeatureList;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.ProgressListener;

/**
 * Used as a reasonable default implementation for subCollection.
 * <p>
 * Note: to implementors, this is not optimal, please do your own thing - your
 * users will thank you.
 * </p>
 * 
 * @author Jody Garnett, Refractions Research, Inc.
 * @author Justin Deoliveira, The Open Planning Project
 */
public class SubFeatureCollection extends AbstractResourceCollection implements
        FeatureCollection {
    /**
     * The unfiltered feature collection.
     */
    protected FeatureCollection collection;

    /**
     * Filters the unfiltered feature collection.
     */
    protected Filter filter;

    /**
     * Filter used to contstruct new filters.
     */
    protected FilterFactory factory;

    public SubFeatureCollection(FeatureCollection collection, Filter filter,
            FilterFactory factory) {
        if (filter.equals(org.geotools.filter.Filter.ALL)) {
            throw new IllegalArgumentException(
                    "A subcollection with Filter.ALL is a null operation");
        }
        if (filter.equals(org.geotools.filter.Filter.NONE)) {
            throw new IllegalArgumentException(
                    "A subcollection with Filter.NONE should be a FeatureCollectionEmpty");
        }
        if (collection instanceof SubFeatureCollection) {
            SubFeatureCollection sub = (SubFeatureCollection) collection;
            collection = sub.collection;
            this.filter = factory.and(sub.filter, filter);
        } else {
            this.collection = collection;
            this.filter = filter;
        }
    }

    public int size() {
        int count = 0;
        for (Iterator itr = this.collection.iterator(); itr.hasNext();) {
            Feature f = (Feature) itr.next();
            if (this.filter.evaluate(f)) {
                count++;
            }
        }
        return count;
    }

    protected Iterator openIterator() {
        Iterator/* <Feature> */filtered = new FilteredIterator(collection,
                filter);
        return filtered;
    }

    protected void closeIterator(Iterator close) {
        if (close instanceof FilteredIterator) {
            ((FilteredIterator) close).close();
        } else {
            collection.close(close);
        }
    }

    public FeatureCollection subCollection(org.opengis.filter.Filter filter) {
        return new SubFeatureCollection(this, filter, factory);
    }

    public FeatureList sort(org.opengis.filter.sort.SortBy order) {
        // return new SubFeatureList(this,filter,factory,order);
        throw new UnsupportedOperationException();
    }

    public boolean nillable() {
        return collection.nillable();
    }

    public Collection memberTypes() {
        return collection.memberTypes();
    }

    public void putUserData(Object key, Object value) {
        collection.putUserData(key, value);

    }

    public Object getUserData(Object key) {
        return collection.getUserData(key);
    }

    public String getID() {
        return collection.getID();
    }

    public CoordinateReferenceSystem getCRS() {
        return collection.getCRS();
    }

    public void setCRS(CoordinateReferenceSystem crs) {
        collection.setCRS(crs);
    }

    public BoundingBox getBounds() {
        return FeatureCollections.getBounds(iterator());
    }

    public GeometryAttribute getDefaultGeometry() {
        return collection.getDefaultGeometry();
    }

    public void setDefaultGeometry(GeometryAttribute geom) {
        collection.setDefaultGeometry(geom);

    }

    public void set(List newValue) throws IllegalArgumentException {
        collection.set(newValue);

    }

    public List get(Name name) {
        return collection.get(name);
    }

    public AttributeType getType() {
        return collection.getType();
    }

    public AttributeDescriptor getDescriptor() {
        return collection.getDescriptor();
    }

    public PropertyDescriptor descriptor() {
        return collection.descriptor();
    }

    public Name name() {
        return collection.name();
    }

    public Object get() {
        return collection.get();
    }

    public Collection attributes() {
        return collection.attributes();
    }

    public Collection associations() {
        return collection.associations();
    }

    public void set(Object newValue) throws IllegalArgumentException {
        collection.set(newValue);
    }

    /**
     * Accepts a visitor, which then visits each feature in the collection.
     */
    public void accepts(FeatureVisitor visitor, ProgressListener progress) {
        Iterator iterator = null;
        // if( progress == null ) progress = new NullProgressListener();
        try {
            float size = size();
            float position = 0;
            progress.started();
            visitor.init((FeatureCollectionType) collection.getType());
            for (iterator = iterator(); !progress.isCanceled()
                    && iterator.hasNext(); progress.progress(position++ / size)) {
                try {
                    Feature feature = (Feature) iterator.next();
                    visitor.visit(feature);
                } catch (Exception erp) {
                    progress.exceptionOccurred(erp);
                }
            }
        } finally {
            progress.complete();
            close(iterator);
        }
    }

    public Object operation(Name arg0, List arg1) {
        throw new UnsupportedOperationException("operation not supported yet");
    }
}
