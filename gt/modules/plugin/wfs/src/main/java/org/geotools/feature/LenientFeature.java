package org.geotools.feature;

import java.rmi.server.UID;
import java.util.Arrays;
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
     * Sets all attributes for this feature, passed as an array.  All
     * attributes are checked for validity before adding.
     *
     * @param attributes All feature attributes.
     *
     * @throws IllegalAttributeException Passed attributes do not match feature
     *         type.
     */
    public void setAttributes(Object[] attributes)
        throws IllegalAttributeException {
        // the passed in attributes were null, lets make that a null array
        Object[] newAtts = attributes;

        if (attributes == null) {
            newAtts = new Object[this.attributes.length];
        }

        if( constructing ){

            // We're trying to make this work no matter what 
            // so try to figure out some mapping
            Object[] tmp = assumeCorrectOrder( newAtts );
            if( tmp==null )
                newAtts = greedyMatch(newAtts);
            else
                newAtts=tmp;
        }else{
            if (newAtts.length != this.attributes.length) {
                throw new IllegalAttributeException(
                    "Wrong number of attributes expected "
                    + schema.getAttributeCount() + " got " + newAtts.length);
            }
        }
        
        for (int i = 0, ii = newAtts.length; i < ii; i++) {
            setAttribute(i, newAtts[i]);
        }
    }

    private Object[] assumeCorrectOrder( Object[] newAtts ) {
        Object[] tmp=new Object[schema.getAttributeCount()];
        for( int i = 0; i < newAtts.length && i<schema.getAttributeCount(); i++ ) {
            Object object = newAtts[i];
            AttributeType att = schema.getAttributeType(i);
            if( object==null )
                continue;
            Class requiredClass = att.getBinding();
            Class realClass = object.getClass();
            if( !requiredClass.isAssignableFrom(realClass) )
                return null;
            else
                tmp[i]=object;
            
        }
        return tmp;
    }

    private Object[] greedyMatch( Object[] newAtts ) {
        Object[] relaxedAttrs=new Object[this.attributes.length];
        boolean inValid = false;
        for( int i = 0; i < newAtts.length; i++ ) {
            Object object = newAtts[i];
            boolean found = false;
            if( object==null )
                continue;
            Class realClass = object.getClass();
            for( int j = 0; j < schema.getAttributeCount(); j++ ) {
                AttributeType att = schema.getAttributeType(j);
                Class requiredClass = att.getBinding();
                if( relaxedAttrs[j]==null && requiredClass.isAssignableFrom(realClass) ){
                    relaxedAttrs[j]=object;
                    found=true;
                    break;
                }
            }
            if( !found ) inValid=true;
        }
        newAtts=relaxedAttrs;
        if( inValid ){
            StringBuffer buf=new StringBuffer();
            buf.append("WFSFeatureType#setAttributes(Object[]):");
            buf.append("\nAttributes were not correct for the feature Type:");
            buf.append(schema.getTypeName());
            buf.append(".  Made best guess:\n Recieved: ");
            for( int i = 0; i < newAtts.length; i++ ) {
                Object object = newAtts[i];
                buf.append(object==null?"null":object.toString());
                buf.append(",");
            }
            buf.append("\nBest Guess: \n");
            for( int i = 0; i < relaxedAttrs.length; i++ ) {
                Object object = relaxedAttrs[i];
                buf.append(object==null?"null":object.toString());
                buf.append(",");
            }

            LOGGER.warning(buf.toString());
        }
        return newAtts;
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