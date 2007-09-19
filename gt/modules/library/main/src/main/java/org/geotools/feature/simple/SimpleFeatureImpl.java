package org.geotools.feature.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.geotools.feature.FeatureImpl;
import org.opengis.feature.Attribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;

/**
 * An implementation of the SimpleFeature convience methods ontop of
 * FeatureImpl.
 * 
 * @author Justin
 */
public class SimpleFeatureImpl extends FeatureImpl implements SimpleFeature {

    public SimpleFeatureImpl(List properties, AttributeDescriptor desc, String id) {
        super(properties, desc, id);
    }

    public SimpleFeatureImpl(List properties, SimpleFeatureType type, String id) {
        super(properties, type, id);
    }

    public SimpleFeatureType getType() {
        return (SimpleFeatureType) super.getType();
    }
    
    public SimpleFeatureType getFeatureType() {
        return getType();
    }
    
    public List<Attribute> getValue() {
        return (List<Attribute>) super.getValue();
    }
    
    public Object getAttribute(Name name) {
        return getAttribute(name.getLocalPart());
    }
    
    public void setAttribute(Name name, Object value) {
        setAttribute( name.getLocalPart(), value );
    }
    
    public Object getAttribute(String name) {
        for (Iterator<Attribute> itr = getValue().iterator(); itr.hasNext();) {
            Attribute att = (Attribute) itr.next();
            if ( att.getName().getLocalPart().equals( name )) {
                return att.getValue();
            }
        }
        return null;
    }
    
    public void setAttribute(String name, Object value) {
        Attribute attribute = (Attribute) getProperty(name);
        if ( attribute == null ) {
            throw new IllegalArgumentException("No such attribute: " + name);
        }
        
        attribute.setValue( value );
    }
    
    public Object getAttribute(int index) throws IndexOutOfBoundsException {
        return getValue().get(index).getValue();
    }
    
    public void setAttribute(int index, Object value)
            throws IndexOutOfBoundsException {
        Attribute attribute = getValue().get( index );
        attribute.setValue( value );
    }
    
    public List<Object> getAttributes() {
        List attributes = new ArrayList();
        for ( Iterator<Attribute> a = getValue().iterator(); a.hasNext(); ) {
            Attribute attribute = a.next();
            attributes.add( attribute.getValue() );
        }
        
        return attributes;
    }
    
    public void setAttributes(List<Object> values) {
        if ( values.size() != getValue().size() ) {
            String msg = "Expected " + getValue().size() + " attributes but " 
                + values.size() + " were specified";
            throw new IllegalArgumentException( msg );
        }
        
        for ( int i = 0; i < values.size(); i++ ) {
            getValue().get(i).setValue( values.get(i) );
        }
    }
    
    public void setAttributes(Object[] values) {
        setAttributes( Arrays.asList(values) );
    }
    
    public int getAttributeCount() {
        return getValue().size();
    }
    
    public int getNumberOfAttributes() {
        return getAttributeCount();
    }
    
    public Object getDefaultGeometry() {
        return getDefaultGeometryProperty() != null ? 
            getDefaultGeometryProperty().getValue() : null;
    }
    
    public void setDefaultGeometry(Object geometry) {
        if ( getDefaultGeometryProperty() != null ) {
            getDefaultGeometryProperty().setValue(geometry);
        }
        else {
            throw new IllegalStateException("Feature has no defaultGeometry property");    
        }
        
    }
    
}
