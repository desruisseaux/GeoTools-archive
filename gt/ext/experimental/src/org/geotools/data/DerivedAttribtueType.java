package org.geotools.data;

import org.geotools.feature.AttributeType;
import org.geotools.feature.IllegalAttributeException;

import com.vividsolutions.jts.geom.Geometry;

/**
 * This is really just a normal attribute type.
 * <p>
 * DefaultAttributeType is locked away behind a builder interface and cannot be
 * accessed directly. This class is used as a data transfer object to hold all the correct
 * values so you can pass them into FeatureTypeFactory (*cough* builder).
 * </p> 
 * <p>
 * @author Jody Garnett
 */
public class DerivedAttribtueType implements AttributeType {    
    final private String name;
    final private boolean nested;
    final private Class type;
    final private boolean nillable;
    private int length;
    /**
     * Construct <code>DerivedAttribtueType</code>.
     *
     * @param name
     * @param type
     * @param nillable
     * @param nested
     */
    public DerivedAttribtueType( String name, Class type, boolean nillable, boolean nested ){
        this.name = name;
        this.type = type;
        this.nillable = nillable;
        this.nested = nested;
        this.length = -1;
    }
    
    /*
     * @see org.geotools.feature.AttributeType#isNested()
     */
    public boolean isNested() {
        return nested;
    }

    /*
     * @see org.geotools.feature.AttributeType#getName()
     */
    public String getName() {
        return name;
    }

    /*
     * @see org.geotools.feature.AttributeType#getType()
     */
    public Class getType() {
        return type;
    }

    /*
     * @see org.geotools.feature.AttributeType#isNillable()
     */
    public boolean isNillable() {
        return nillable;
    }

    /*
     * @see org.geotools.feature.AttributeType#isGeometry()
     */
    public boolean isGeometry() {
        return false;
    }

    /*
     * @see org.geotools.feature.AttributeType#parse(java.lang.Object)
     */
    public Object parse( Object value ) throws IllegalArgumentException {
        throw new IllegalArgumentException("DerivedAttributeType is only used as a DTO, use for storing data is not supported");
    }

    /*
     * @see org.geotools.feature.AttributeType#validate(java.lang.Object)
     */
    public void validate( Object obj ) throws IllegalArgumentException {
        throw new IllegalArgumentException("DerivedAttributeType is only used as a DTO, use for storing data is not supported");
    }

    /*
     * @see org.geotools.feature.AttributeType#duplicate(java.lang.Object)
     */
    public Object duplicate( Object src ) throws IllegalAttributeException {
        throw new IllegalArgumentException("DerivedAttributeType is only used as a DTO, use for storing data is not supported");
    }

    /*
     * @see org.geotools.feature.AttributeType#createDefaultValue()
     */
    public Object createDefaultValue() {
        throw new UnsupportedOperationException("DerivedAttributeType is only used as a DTO, use for storing data is not supported");
    }

    /*
     * @see org.geotools.feature.AttributeType#getFieldLength()
     */
    public int getFieldLength() {
        return length;
    }

}
