package org.geotools.feature;

import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.PropertyDescriptor;

import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.feature.type.Types;
import org.geotools.geometry.jts.ReferencedEnvelope;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * An implementation of SimpleFeature that has a more relaxed attitude about being valid.
 * 
 * @author Jesse Eichar
 */
public class LenientFeature extends SimpleFeatureImpl {
    static Logger LOGGER = Logger.getLogger("org.geotools.data.wfs");
    
    boolean constructing;
    /**
     * Creates a new instance of flat feature, which must take a flat feature
     * type schema and all attributes as arguments.
     *
     * @param schema Feature type schema for this flat feature.
     * @param attributes Initial attributes for this feature.
     * @param featureID The unique ID for this feature.
     *
     * @throws IllegalAttributeException Attribtues do not conform to feature
     *         type schema.
     * @throws NullPointerException if schema is null.
     */
    protected LenientFeature(SimpleFeatureType schema, Object[] attributes,String featureID)
        throws IllegalAttributeException, NullPointerException {
        super(Arrays.asList(attributes),
              checkSchema( schema),
              checkId( featureID ));
        // superclass just punts the values in ... we are going to validate if needed
        constructing=true;
        setAttributes(attributes);
        constructing=false;
    }

    private static SimpleFeatureType checkSchema(SimpleFeatureType schema) {
        if (schema == null) {
            throw new NullPointerException("schema");
        }
        return schema;
    }

    /**
     * Creates a new instance of flat feature, which must take a flat feature
     * type schema and all attributes as arguments.
     *
     * @param schema Feature type schema for this flat feature.
     * @param attributes Initial attributes for this feature.
     *
     * @throws IllegalAttributeException Attribtues do not conform to feature
     *         type schema.
     *
     * @task REVISIT: should we allow this?  Force users to explicitly set
     *       featureID to null?
     */
    protected LenientFeature(SimpleFeatureType schema, Object[] attributes)
        throws IllegalAttributeException {
        this(schema, attributes, null);
    }

    /**
     * Creates an ID from a hashcode.
     *
     * @return an id for the feature.
     */
    static String checkId(String featureID) {
        if( featureID == null ){
            return "fid-" + new UID().toString().replace(':', '_');
        }
        else {
            return featureID;
        }
    }

    /**
     * Sets the attribute at position to val.
     *
     * @param position the index of the attribute to set.
     * @param val the new value to give the attribute at position.
     *
     * @throws IllegalAttributeException if the passed in val does not validate
     *         against the AttributeType at that position.
     */
    public void setAttribute(int position, Object val)
        throws IllegalAttributeException {
        AttributeDescriptor type = getFeatureType().getAttribute(position);
        try {
            
            if ((val == null) && !type.isNillable()) {
                value = type.getDefaultValue();
            }
            Object parsed = parse(val);
            try {
                Types.validate( type, parsed );
            }catch (Throwable e) {
                if( constructing ){
                    LOGGER.logp(Level.WARNING, "LenientFeature", "setAttribute", "Illegal Argument but ignored since we are being lenient",
                            e);
                } else {
                    throw new IllegalAttributeException(type, val, e);
                }
            }
            super.setAttribute(position, val);            
        } catch (IllegalArgumentException iae) {
            throw new IllegalAttributeException(type, val, iae);
        }
    }

    /**
     * Sets all attributes for this feature, passed in as a list.
     * @param attributes All feature attributes.
     * @throws IllegalAttributeException Passed attributes do not match feature
     *         type.
     */
    
    public void setAttributes(List<Object> attributes) {
        if( constructing ){
            // we are going to make this work no matter what
            // so try and figure out some mapping
            if ( attributes == null ){
                attributes = Arrays.asList(new Object[getFeatureType().getAttributeCount()]);
            }
            if ( attributes.size() != getFeatureType().getAttributeCount() ) {
                String msg = "Expected " + getFeatureType().getAttributeCount() + " attributes but " 
                    + attributes.size() + " were specified";
                    throw new IllegalArgumentException( msg );                    
            }
            List<Object> fixed;
            fixed = assumeCorrectOrder( attributes );
            if( fixed == null ){
                //fixed = greedyMatch(attributes);
            }
            super.setAttributes( fixed );
        }
        else {
            super.setAttributes( attributes );
        }
    }

    private List<Object> assumeCorrectOrder( List<Object> newAtts ) {
        SimpleFeatureType schema = getFeatureType();
        List<Object> tmp = Arrays.asList(new Object[schema.getAttributeCount()]);
        for( int i = 0; i < newAtts.size() && i<schema.getAttributeCount(); i++ ) {
            Object object = newAtts.get(i);
            AttributeDescriptor att = schema.getAttribute(i);
            if( object==null ){
                continue;
            }
            Object value = Types.parse( att, object );
            tmp.set(i, value);
        }
        return tmp;
    }

    private List<Object> greedyMatch(List<Object> newAtts ) {
        SimpleFeatureType schema = getFeatureType();
        List<Object> relaxedAttrs=Arrays.asList(new Object[schema.getAttributeCount()]);
        boolean inValid = false;
        for( int i = 0; i < newAtts.size(); i++ ) {
            Object object = newAtts.get(i);
            boolean found = false;
            if( object==null )
                continue;
            Class realClass = object.getClass();
            for( int j = 0; j < schema.getAttributeCount(); j++ ) {
                AttributeDescriptor att = schema.getAttribute(j);
                Class requiredClass = att.getType().getBinding();
                if( relaxedAttrs.get(j)==null && requiredClass.isAssignableFrom(realClass) ){
                    relaxedAttrs.set(j,object);
                    found=true;
                    break;
                }
            }
            if( !found ) {
                inValid=true;
            }
        }
        newAtts=relaxedAttrs;
        if( inValid ){
            StringBuffer buf=new StringBuffer();
            buf.append("WFSFeatureType#setAttributes(Object[]):");
            buf.append("\nAttributes were not correct for the feature Type:");
            buf.append(schema.getTypeName());
            buf.append(".  Made best guess:\n Recieved: ");
            for( int i = 0; i < newAtts.size(); i++ ) {
                Object object = newAtts.get(i);
                buf.append(object==null?"null":object.toString());
                buf.append(",");
            }
            buf.append("\nBest Guess: \n");
            for( int i = 0; i < relaxedAttrs.size(); i++ ) {
                Object object = relaxedAttrs.get(i);
                buf.append(object==null?"null":object.toString());
                buf.append(",");
            }

            LOGGER.warning(buf.toString());
        }
        return relaxedAttrs;
    }


    /**
     * Creates an exact copy of this feature.
     *
     * @return A default feature.
     *
     * @throws RuntimeException If some contents are not cloneable.
     */
    public Object clone() {
        try {
            DefaultFeature clone = (DefaultFeature) super.clone();

            for (int i = 0; i < getAttributeCount(); i++) {
                clone.setAttribute(i, getAttribute(i));                
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("The impossible has happened", e);
        }
    }

}