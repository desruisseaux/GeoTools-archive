package org.geotools.data.feature.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.jxpath.JXPathIntrospector;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.iso.AttributeImpl;
import org.geotools.feature.iso.xpath.AttributePropertyHandler;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.BoundingBox;

import com.vividsolutions.jts.geom.Geometry;

public class ISOFeatureAdapter implements Feature, SimpleFeature {

    static{
        JXPathIntrospector.registerDynamicClass(ISOFeatureAdapter.class,
                AttributePropertyHandler.class);
    }
    
    private org.geotools.feature.Feature adaptee;

    private SimpleFeatureType featureType;

    private SimpleFeatureFactory attributeFactory;

    public ISOFeatureAdapter(org.geotools.feature.Feature feature, SimpleFeatureType ftype,
            SimpleFeatureFactory attributeFactory) {
        if (adaptee instanceof GTFeatureAdapter) {
            throw new IllegalArgumentException(
                    "No need to adapt GTFeaureAdapter, use getAdaptee() instead");
        }
        this.attributeFactory = attributeFactory;
        this.adaptee = feature;
        this.featureType = ftype;
    }

    public org.geotools.feature.Feature getAdaptee() {
        return adaptee;
    }

    public BoundingBox getBounds() {
        ReferencedEnvelope env = new ReferencedEnvelope((CoordinateReferenceSystem) null);
        env.init(adaptee.getBounds());
        return env;
    }

    public CoordinateReferenceSystem getCRS() {
        GeometryAttributeType defaultGeometry = adaptee.getFeatureType().getDefaultGeometry();
        return defaultGeometry == null ? null : defaultGeometry.getCoordinateSystem();
    }

    public GeometryAttribute getDefaultGeometry() {
        AttributeDescriptor defaultGeometry = featureType.getDefaultGeometry();
        Geometry geom = adaptee.getDefaultGeometry();
        Attribute attribute;
        attribute = attributeFactory.createAttribute(geom, defaultGeometry, null);
        return (GeometryAttribute) attribute;
    }

    public String getID() {
        return adaptee.getID();
    }

    public Object getUserData(Object key) {
        return null;
    }

    public void putUserData(Object key, Object value) {
        // do nothing?
    }

    public void setCRS(CoordinateReferenceSystem crs) {
        Geometry defaultGeometry = adaptee.getDefaultGeometry();
        defaultGeometry.setUserData(crs);
    }

    public void setDefaultGeometry(GeometryAttribute geometryAttribute) {
        Object value = geometryAttribute.get();
        try {
            adaptee.setDefaultGeometry((Geometry) value);
        } catch (IllegalAttributeException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        }
    }

    public Collection associations() {
        return Collections.EMPTY_SET;
    }

    public Collection attributes() {
        Object[] attributes = this.adaptee.getAttributes(null);
        Collection descriptors = this.featureType.attributes();

        assert attributes.length == descriptors.size();

        List atts = new ArrayList(attributes.length);
        int current = 0;
        for (Iterator it = descriptors.iterator(); it.hasNext(); current++) {
            Object content = attributes[current];
            AttributeDescriptor descriptor = (AttributeDescriptor) it.next();
            String id = null;
            atts.add(new AttributeImpl(content, descriptor, id));
        }

        return atts;
    }

    public Object get() {
        Collection properties = featureType.getProperties();
        Object[] values = this.adaptee.getAttributes((Object[]) null);
        List attributes = new ArrayList(properties.size());
        AttributeDescriptor descriptor;
        int i = 0;
        for (Iterator it = properties.iterator(); it.hasNext(); i++) {
            descriptor = (AttributeDescriptor) it.next();
            Object value = values[i];
            Attribute att = attributeFactory.createAttribute(value, descriptor, null);
            attributes.add(att);
        }
        return attributes;
    }

    public List get(Name name) {
        Object object = get(name.getLocalPart());
        return Collections.singletonList(object);
    }

    public AttributeDescriptor getDescriptor() {
        return null;
    }

    public void set(Object atts) throws IllegalArgumentException {
        Collection attributes = (Collection) atts;
        int i = 0;
        Attribute att;
        for (Iterator it = attributes.iterator(); it.hasNext(); i++) {
            att = (Attribute) it.next();
            set(i, att.get());
        }
    }

    public AttributeType getType() {
        return featureType;
    }

    public boolean nillable() {
        return true;
    }

    public Object operation(Name name, List parameters) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public PropertyDescriptor descriptor() {
        return null;
    }

    public Name name() {
        return this.featureType.getName();
    }

    public Object defaultGeometry() {
        return adaptee.getDefaultGeometry();
    }

    public void defaultGeometry(Object geometry) {
        // TODO Auto-generated method stub
    }

    public Object get(String name) {
        return adaptee.getAttribute(name);
    }

    public Object get(int index) {
        return adaptee.getAttribute(index);
    }

    public int getNumberOfAttributes() {
        return adaptee.getNumberOfAttributes();
    }

    public Object operation(String name, Object parameters) {
        return null;
    }

    public void set(String name, Object value) {
        try {
            adaptee.setAttribute(name, value);
        } catch (IllegalAttributeException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        }
    }

    public void set(int index, Object value) {
        try {
            adaptee.setAttribute(index, value);
        } catch (IllegalAttributeException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        }
    }

    /**
     * TODO implement as a data view
     */
    public List types() {
        org.geotools.feature.AttributeType[] types = adaptee.getFeatureType().getAttributeTypes();
        return Arrays.asList(types);
    }

    /**
     * TODO implement as a data view as per {@link SimpleFeature#values()}
     */
    public List values() {
        Object[] attributes = adaptee.getAttributes(null);
        return Arrays.asList(attributes);
    }

}
