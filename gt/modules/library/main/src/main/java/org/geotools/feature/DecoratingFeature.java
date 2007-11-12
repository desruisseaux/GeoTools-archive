package org.geotools.feature;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;

import com.vividsolutions.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;

/**
 * Base class for feature decorators.
 * <p>
 * Subclasses should override those methods which are relevant to the decorator.
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project
 * @since 2.5
 * 
 */
public class DecoratingFeature implements SimpleFeature {

    protected SimpleFeature delegate;

    public DecoratingFeature(SimpleFeature delegate) {
        this.delegate = delegate;
    }

    public Object getAttribute(int index) {
        return delegate.getAttribute(index);
    }

    public Object getAttribute(Name arg0) {
        return delegate.getAttribute(arg0);
    }

    public Object getAttribute(String path) {
        return delegate.getAttribute(path);
    }

    public int getAttributeCount() {
        return delegate.getAttributeCount();
    }

    public List<Object> getAttributes() {
        return delegate.getAttributes();
    }

    public BoundingBox getBounds() {
        return delegate.getBounds();
    }

    public Object getDefaultGeometry() {
        return delegate.getDefaultGeometry();
    }

    public GeometryAttribute getDefaultGeometryProperty() {
        return delegate.getDefaultGeometryProperty();
    }

    public AttributeDescriptor getDescriptor() {
        return delegate.getDescriptor();
    }

    public SimpleFeatureType getFeatureType() {
        return delegate.getFeatureType();
    }

    public String getID() {
        return delegate.getID();
    }

    public Name getName() {
        return delegate.getName();
    }

    public Collection<Property> getProperties() {
        return delegate.getProperties();
    }

    public Collection<Property> getProperties(Name arg0) {
        return delegate.getProperties(arg0);
    }

    public Collection<Property> getProperties(String arg0) {
        return delegate.getProperties(arg0);
    }

    public Property getProperty(Name arg0) {
        return delegate.getProperty(arg0);
    }

    public Property getProperty(String arg0) {
        return delegate.getProperty(arg0);
    }

    public SimpleFeatureType getType() {
        return delegate.getType();
    }

    public Map<Object, Object> getUserData() {
        return delegate.getUserData();
    }

    public Collection<? extends Property> getValue() {
        return delegate.getValue();
    }

    public boolean isNillable() {
        return delegate.isNillable();
    }

    public void setAttribute(int position, Object val) {
        delegate.setAttribute(position, val);
    }

    public void setAttribute(Name arg0, Object arg1) {
        delegate.setAttribute(arg0, arg1);
    }

    public void setAttribute(String path, Object attribute) {
        delegate.setAttribute(path, attribute);
    }

    public void setAttributes(List<Object> arg0) {
        delegate.setAttributes(arg0);
    }

    public void setAttributes(Object[] arg0) {
        delegate.setAttributes(arg0);
    }

    public void setDefaultGeometry(Object arg0) {
        delegate.setDefaultGeometry(arg0);
    }

    public void setDefaultGeometryProperty(GeometryAttribute arg0) {
        delegate.setDefaultGeometryProperty(arg0);
    }

    public void setDefaultGeometry(Geometry geometry)
            throws IllegalAttributeException {
        delegate.setDefaultGeometry(geometry);
    }

    public void setValue(Collection<Property> arg0) {
        delegate.setValue(arg0);
    }

    public void setValue(Object arg0) {
        delegate.setValue(arg0);
    }
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }
    public int hashCode() {
        return delegate.hashCode();
    }
    public String toString() {
        return "<"+getClass().getCanonicalName()+">"+delegate.toString();
    }
}
