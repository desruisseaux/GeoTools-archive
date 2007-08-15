package org.geotools.feature;

import java.util.Collection;
import java.util.List;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

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
public class DecoratingFeature implements Feature {

    protected Feature delegate;

    public DecoratingFeature(Feature delegate) {
        this.delegate = delegate;
    }

    public Collection associations() {
        return delegate.associations();
    }

    public Collection attributes() {
        return delegate.attributes();
    }

    public PropertyDescriptor descriptor() {
        return delegate.descriptor();
    }

    public List get(Name name) {
        return delegate.get(name);
    }

    public Object getAttribute(int index) {
        return delegate.getAttribute(index);
    }

    public Object getAttribute(String path) {
        return delegate.getAttribute(path);
    }

    public List getAttributes() {
        return delegate.getAttributes();
    }

    public Object[] getAttributes(Object[] attributes) {
        return delegate.getAttributes(attributes);
    }

    public ReferencedEnvelope getBounds() {
        return delegate.getBounds();
    }

    public CoordinateReferenceSystem getCRS() {
        return delegate.getCRS();
    }

    public GeometryAttribute getDefaultGeometry() {
        return delegate.getDefaultGeometry();
    }

    public Object getDefaultGeometryValue() {
        return delegate.getDefaultGeometryValue();
    }

    public AttributeDescriptor getDescriptor() {
        return delegate.getDescriptor();
    }

    public FeatureType getFeatureType() {
        return delegate.getFeatureType();
    }

    public String getID() {
        return delegate.getID();
    }

    public int getNumberOfAttributes() {
        return delegate.getNumberOfAttributes();
    }

    public Geometry getPrimaryGeometry() {
        return delegate.getPrimaryGeometry();
    }

    public AttributeType getType() {
        return delegate.getType();
    }

    public List getTypes() {
        return delegate.getTypes();
    }

    public Object getUserData(Object key) {
        return delegate.getUserData(key);
    }

    public Object getValue() {
        return delegate.getValue();
    }

    public Object getValue(int index) {
        return delegate.getValue(index);
    }

    public Object getValue(String name) {
        return delegate.getValue(name);
    }

    public List getValues() {
        return delegate.getValues();
    }

    public Name name() {
        return delegate.name();
    }

    public boolean nillable() {
        return delegate.nillable();
    }

    public Object operation(Name name, List parameters) {
        return delegate.operation(name, parameters);
    }

    public void putUserData(Object key, Object value) {
        delegate.putUserData(key, value);
    }

    public void setAttribute(int position, Object val)
            throws IllegalAttributeException, ArrayIndexOutOfBoundsException {
        delegate.setAttribute(position, val);
    }

    public void setAttribute(String path, Object attribute)
            throws IllegalAttributeException {
        delegate.setAttribute(path, attribute);
    }

    public void setCRS(CoordinateReferenceSystem crs) {
        delegate.setCRS(crs);
    }

    public void setDefaultGeometry(GeometryAttribute geometryAttribute) {
        delegate.setDefaultGeometry(geometryAttribute);
    }

    public void setDefaultGeometryValue(Object geometry) {
        delegate.setDefaultGeometryValue(geometry);
    }

    public void setPrimaryGeometry(Geometry geometry)
            throws IllegalAttributeException {
        delegate.setPrimaryGeometry(geometry);
    }

    public void setValue(int index, Object value) {
        delegate.setValue(index, value);
    }

    public void setValue(List arg0) {
        delegate.setValue(arg0);
    }

    public void setValue(Object arg0) {
        delegate.setValue(arg0);
    }

    public void setValue(String name, Object value) {
        delegate.setValue(name, value);
    }

    public void setValues(List values) {
        delegate.setValues(values);
    }

    public void setValues(Object[] values) {
        delegate.setValues(values);
    }

    
}
