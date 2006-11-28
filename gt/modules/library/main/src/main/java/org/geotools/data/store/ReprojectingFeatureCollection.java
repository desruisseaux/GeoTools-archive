package org.geotools.data.store;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.geotools.data.FeatureReader;
import org.geotools.data.collection.DelegateFeatureReader;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureList;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.collection.DelegateFeatureIterator;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.geometry.jts.GeometryCoordinateSequenceTransformer;
import org.geotools.referencing.CRS;
import org.geotools.util.ProgressListener;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * FeatureCollection decorator that reprojects the default geometry.
 * 
 * @author Justin
 */
public class ReprojectingFeatureCollection implements FeatureCollection {

    /**
     * The decorated collection
     */
    FeatureCollection delegate;

    /**
     * The transform to the target coordinate reference system
     */
    MathTransform transform;

    /**
     * The schema of reprojected features
     */
    FeatureType schema;

    /**
     * The feature type of the feature collection
     */
    FeatureType featureType;

    /**
     * The target coordinate reference system
     */
    CoordinateReferenceSystem target;
    
    /**
     * Transformer used to transform geometries;
     */
    GeometryCoordinateSequenceTransformer transformer;
    
    public ReprojectingFeatureCollection(FeatureCollection delegate,
            CoordinateReferenceSystem target) {
        this( delegate, delegate.getSchema().getDefaultGeometry().getCoordinateSystem(), target );
    }
    
    public ReprojectingFeatureCollection(
		FeatureCollection delegate, CoordinateReferenceSystem source, CoordinateReferenceSystem target
	) {
    	this.delegate = delegate;
        this.target = target;
        FeatureType schema = delegate.getSchema();
        this.schema = reType(schema, target);
       
        FeatureType featureType = delegate.getFeatureType();
        this.featureType = reType(featureType, target);

        if (source == null) {
            throw new NullPointerException("source crs");
        }
        if ( target == null ) {
        	throw new NullPointerException("destination crs");
        }
        
        this.transform = transform(source, target);
        transformer = new GeometryCoordinateSequenceTransformer();
    }

    public void setTransformer(GeometryCoordinateSequenceTransformer transformer) {
		this.transformer = transformer;
	}  

    private MathTransform transform(CoordinateReferenceSystem source,
            CoordinateReferenceSystem target) {
        try {
            return CRS.findMathTransform(source, target);
        } catch (FactoryException e) {
            throw new IllegalArgumentException(
                    "Could not create math transform");
        }
    }

    private FeatureType reType(FeatureType type,
            CoordinateReferenceSystem target) {
        try {
            return FeatureTypes.transform(type, target);
        } catch (SchemaException e) {
            throw new IllegalArgumentException(
                    "Could not transform source schema");
        }
    }

    public FeatureReader reader() throws IOException {
        return new DelegateFeatureReader(getSchema(), features());
    }

    public FeatureIterator features() {
        return new DelegateFeatureIterator(this, iterator());
    }

    public void close(FeatureIterator close) {
        close.close();
    }

    public Iterator iterator() {
        try {
            return new ReprojectingIterator(delegate.iterator(), transform, schema, transformer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void close(Iterator close) {
        Iterator iterator = ((ReprojectingIterator) close).getDelegate();
        delegate.close(iterator);
    }

    public void addListener(CollectionListener listener)
            throws NullPointerException {
        delegate.addListener(listener);
    }

    public void removeListener(CollectionListener listener)
            throws NullPointerException {
        delegate.removeListener(listener);
    }

    public FeatureType getFeatureType() {
        return this.featureType;
    }

    public FeatureType getSchema() {
        return this.schema;
    }

    public void accepts(FeatureVisitor visitor, ProgressListener progress)
            throws IOException {
        delegate.accepts(visitor, progress);
    }

    public FeatureCollection subCollection(Filter filter) {
        Filter unFilter = unFilter(filter);
        return new ReprojectingFeatureCollection(delegate
                .subCollection(unFilter), target);
        // TODO: return new delegate.subCollection( filter ).reproject( target
        // );
    }

    /**
     * Takes any literal geometry in the provided filter and backprojects it
     * 
     * @param FilterFactory
     * @param MathTransform
     */
    private Filter unFilter(Filter filter) {
        // need: filterFactory
        // need: inverse of our transform
        // FilterVisitor fv = new ReprojectingFilterVisitor(ff, transform);
        // filter.accept(fv, null);
        // TODO: create FilterVisitor that backproject literal geometry
        return filter;
    }

    public FeatureList sort(SortBy order) {
        // return new ReprojectingFeatureList( delegate.sort( order ), target );
        throw new UnsupportedOperationException("Not yet");
    }

    public void purge() {
        delegate.purge();
    }

    public int size() {
        return delegate.size();
    }

    public void clear() {
        delegate.clear();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public Object[] toArray() {
        return toArray(new Object[size()]);
    }

    public Object[] toArray(Object[] a) {
        List list = new ArrayList();
        Iterator i = iterator();
        try {
            while (i.hasNext()) {
                list.add(i.next());
            }

            return list.toArray(a);
        } finally {
            close(i);
        }
    }

    public boolean add(Object o) {
        // must back project any geometry attributes
        throw new UnsupportedOperationException("Not yet");
        // return delegate.add( o );
    }

    public boolean contains(Object o) {
        // must back project any geometry attributes
        return delegate.add(o);
    }

    public boolean remove(Object o) {
        // must back project any geometry attributes
        return delegate.remove(o);
    }

    public boolean addAll(Collection c) {
        // must back project any geometry attributes
        return delegate.addAll(c);
    }

    public boolean containsAll(Collection c) {
        // must back project any geometry attributes
        return delegate.containsAll(c);
    }

    public boolean removeAll(Collection c) {
        // must back project any geometry attributes
        return delegate.removeAll(c);
    }

    public boolean retainAll(Collection c) {
        // must back project any geometry attributes
        return delegate.retainAll(c);
    }

    public String getID() {
        return delegate.getID();
    }

    public Object[] getAttributes(Object[] attributes) {
        // must reproject any geometry attributes
        return delegate.getAttributes(attributes);
    }

    public Object getAttribute(String xPath) {
        // must project any geometry attributes
        return delegate.getAttribute(xPath);
    }

    public Object getAttribute(int index) {
        return delegate.getAttribute(index);
    }

    public void setAttribute(int position, Object val)
            throws IllegalAttributeException, ArrayIndexOutOfBoundsException {
        delegate.setAttribute(position, val);
    }

    public int getNumberOfAttributes() {
        return delegate.getNumberOfAttributes();
    }

    public void setAttribute(String xPath, Object attribute)
            throws IllegalAttributeException {
        delegate.setAttribute(xPath, attribute);
    }

    public Geometry getDefaultGeometry() {
        return delegate.getDefaultGeometry();
    }

    public void setDefaultGeometry(Geometry geometry)
            throws IllegalAttributeException {
        delegate.setDefaultGeometry(geometry);
    }

    /**
     * This method computes reprojected bounds the hard way, but computing them
     * feature by feature. This method could be faster if computed the
     * reprojected bounds by reprojecting the original feature bounds a Shape
     * object, thus getting the true shape of the reprojected envelope, and then
     * computing the minimum and maximum coordinates of that new shape. The
     * result would not a true representation of the new bounds.
     * 
     * @see org.geotools.data.FeatureResults#getBounds()
     */
    public Envelope getBounds() {
        FeatureIterator r = features();
        try {
            Envelope newBBox = new Envelope();
            Envelope internal;
            Feature feature;

            while (r.hasNext()) {
                feature = r.next();
                internal = feature.getDefaultGeometry().getEnvelopeInternal();
                newBBox.expandToInclude(internal);
            }
            return newBBox;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Exception occurred while computing reprojected bounds", e);
        } finally {
            r.close();
        }
    }

}
