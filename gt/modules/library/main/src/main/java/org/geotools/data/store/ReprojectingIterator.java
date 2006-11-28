package org.geotools.data.store;

import java.io.IOException;
import java.util.Iterator;

import org.geotools.factory.FactoryRegistryException;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.geometry.jts.GeometryCoordinateSequenceTransformer;
import org.geotools.referencing.FactoryFinder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;

public class ReprojectingIterator implements Iterator {

    /**
     * decorated iterator
     */
    Iterator delegate;

    /**
     * The target coordinate reference system
     */
    CoordinateReferenceSystem target;

    /**
     * schema of reprojected features
     */
    FeatureType schema;

    /**
     * Transformer
     */
    GeometryCoordinateSequenceTransformer tx;

    public ReprojectingIterator(
		Iterator delegate, MathTransform transform, FeatureType schema, 
		GeometryCoordinateSequenceTransformer transformer
    ) throws OperationNotFoundException, FactoryRegistryException, FactoryException {
        this.delegate = delegate;
        
        this.schema = schema;

        tx = transformer;
        tx.setMathTransform((MathTransform2D) transform);
    }

    public ReprojectingIterator(
		Iterator delegate, CoordinateReferenceSystem source, CoordinateReferenceSystem target,
        FeatureType schema, GeometryCoordinateSequenceTransformer transformer
    ) throws OperationNotFoundException, FactoryRegistryException, FactoryException {
        this.delegate = delegate;
        this.target = target;
        this.schema = schema;
        tx = transformer;

        MathTransform transform = FactoryFinder.getCoordinateOperationFactory(
                null).createOperation(source, target).getMathTransform();
        tx.setMathTransform((MathTransform2D) transform);
    }

    public Iterator getDelegate() {
        return delegate;
    }

    public void remove() {
        delegate.remove();
    }

    public boolean hasNext() {
        return delegate.hasNext();
    }

    public Object next() {
        Feature feature = (Feature) delegate.next();
        try {
            return reproject(feature);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Feature reproject(Feature feature) throws IOException {

        Object[] attributes = feature.getAttributes(null);

        for (int i = 0; i < attributes.length; i++) {
            Object object = attributes[i];
            if (object instanceof Geometry) {
                // do the transformation
                Geometry geometry = (Geometry) object;
                try {
                    attributes[i] = tx.transform(geometry);
                } catch (TransformException e) {
                    String msg = "Error occured transforming "
                            + geometry.toString();
                    throw (IOException) new IOException(msg).initCause(e);
                }
            }
        }

        try {
            return schema.create(attributes, feature.getID());
        } catch (IllegalAttributeException e) {
            String msg = "Error creating reprojeced feature";
            throw (IOException) new IOException(msg).initCause(e);
        }
    }

}
