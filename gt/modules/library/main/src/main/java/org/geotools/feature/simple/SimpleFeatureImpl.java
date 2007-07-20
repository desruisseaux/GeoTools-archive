package org.geotools.feature.simple;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


import org.geotools.feature.FeatureImpl;
import org.opengis.feature.Attribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;

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

    /**
     * Retrive value by attribute name.
     * 
     * @param name
     * @return Attribute Value associated with name
     */
    public Object getValue(String name) {
        for (Iterator itr = super.properties.iterator(); itr.hasNext();) {
            Attribute att = (Attribute) itr.next();
            AttributeType type = att.getType();
            String attName = type.getName().getLocalPart();
            if (attName.equals(name)) {
                return att.get();
            }
        }
        return null;
    }

    public Object getValue(AttributeType type) {
        if (!super.types().contains(type)) {
            throw new IllegalArgumentException(
                    "this feature content model has no type " + type);
        }
        for (Iterator itr = super.properties.iterator(); itr.hasNext();) {
            Attribute att = (Attribute) itr.next();
            if (att.getType().equals(type)) {
                return att.get();
            }
        }
        throw new Error();
    }

    /**
     * Access attribute by "index" indicated by SimpleFeatureType.
     * 
     * @param index
     * @return
     */
    public Object getValue(int index) {
        Attribute att = (Attribute) super.properties.get(index);
        return att == null ? null : att.get();
        // return values().get(index);
    }

    /**
     * Modify attribute with "name" indicated by SimpleFeatureType.
     * 
     * @param name
     * @param value
     */
    public void setValue(String name, Object value) {
        AttributeType type = ((SimpleFeatureType) getType()).getType(name);
        List/* <AttributeType> */types = types();
        int idx = types.indexOf(type);
        if (idx == -1) {
            throw new IllegalArgumentException(name
                    + " is not a feature attribute");
        }
        setValue(idx, value);
    }

    /**
     * Modify attribute at the "index" indicated by SimpleFeatureType.
     * 
     * @param index
     * @param value
     */
    public void setValue(int index, Object value) {
        List/* <Attribute> */contents = (List) get();
        Attribute attribute = (Attribute) contents.get(index);
        attribute.set(value);
        this.set(contents);
    }

    public void setValues(List values) {
    	for ( int i = 0; i < values.size(); i++ ) {
    		setValue( i , values.get( i ) );
    	}
    }
    
    public void setValues(Object[] values) {
    	setValues( Arrays.asList(values));
    }
    
    public List getAttributes() {
        return (List) get();
    }

    public int getNumberOfAttributes() {
        return types().size();
    }

    public List getTypes() {
        return super.types();
    }
    
    public List getValues() {
    	return super.values();
    }
    
    public Object getDefaultGeometryValue() {
        return getDefaultGeometry() != null ? getDefaultGeometry().get() : null;
    }

    public void setDefaultGeometryValue(Object geometry) {
        if (getDefaultGeometry() != null) {
            getDefaultGeometry().set(geometry);
        }
    }

    public Object operation(String arg0, Object arg1) {
        throw new UnsupportedOperationException("operation not supported yet");
    }
}
