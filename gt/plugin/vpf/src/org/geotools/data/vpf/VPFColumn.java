/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
/*
 * Created on Jul 30, 2004
 *
 */
package org.geotools.data.vpf;

import org.geotools.data.vpf.ifc.*;
import org.geotools.data.vpf.io.TripletId;
import org.geotools.data.vpf.util.DataUtils;
import org.geotools.feature.*;
import org.geotools.feature.IllegalAttributeException;

import com.vividsolutions.jts.geom.Geometry;


/**
 * A column in a VPF File.
 *
 * @author <a href="mailto:jeff@ionicenterprise.com">Jeff Yutzler</a>
 */
public class VPFColumn implements AttributeType, DataTypesDefinition {
    /**
     * The contained attribute type. 
     * AttributeType operations are delegated to this object.
     */
    private final AttributeType attribute;

    /** Describe variable <code>elementsNumber</code> here. */
    private int elementsNumber = 0;

    /** Describe variable <code>narrTable</code> here. */
    private String narrTable = null;

    /** Describe variable <code>keyType</code> here. */
    private char keyType = CHAR_NULL_VALUE;

    /** Describe variable <code>colDesc</code> here. */
    private String colDesc = null;

    /** Describe variable <code>thematicIdx</code> here. */
    private String thematicIdx = null;

    /** Describe variable <code>type</code> here. */
    private char typeChar = CHAR_NULL_VALUE;

    /** Describe variable <code>valDescTableName</code> here. */
    private String valDescTableName = null;
    /**
     * Constructor with all of the elements of a VPF column
     * @param name
     * @param type
     * @param elementsNumber
     * @param keyType
     * @param colDesc
     * @param valDescTableName
     * @param thematicIdx
     * @param narrTable
     */
    public VPFColumn(String name, char type, int elementsNumber, char keyType,
        String colDesc, String valDescTableName, String thematicIdx,
        String narrTable) {
        this.typeChar = type;
        this.elementsNumber = elementsNumber;
        this.keyType = keyType;
        this.colDesc = colDesc;
        this.valDescTableName = valDescTableName;
        this.thematicIdx = thematicIdx;
        this.narrTable = narrTable;
        attribute = AttributeTypeFactory.newAttributeType(name,
                getColumnClass(), true, getColumnSize());
    }
    /**
     * Retrieves the class for the column,
     * based on a char value.
     * @return the class
     */
    public Class getColumnClass() {
        Class columnClass;

        switch (typeChar) {
        case DATA_LONG_INTEGER:
            columnClass = Integer.class;

            break;

        case DATA_SHORT_FLOAT:
            columnClass = Float.class;

            break;

        case DATA_LONG_FLOAT:
            columnClass = Double.class;

            break;

        case DATA_2_COORD_F:
        case DATA_2_COORD_R:
        case DATA_3_COORD_F:
        case DATA_3_COORD_R:
            columnClass = Geometry.class;

            break;

        case DATA_TRIPLET_ID:
            columnClass = TripletId.class;

            break;

        case DATA_TEXT:
        case DATA_NULL_FIELD:
        case DATA_LEVEL1_TEXT:
        case DATA_LEVEL2_TEXT:
        case DATA_LEVEL3_TEXT:

        // Short integers are usually coded values
        case DATA_SHORT_INTEGER:
        default:
            columnClass = String.class;

            break;
        }

        return columnClass;
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.AttributeType#createDefaultValue()
     */
    public Object createDefaultValue() {
        return attribute.createDefaultValue();
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.AttributeType#duplicate(java.lang.Object)
     */
    public Object duplicate(Object src) throws IllegalAttributeException {
        return attribute.duplicate(src);
    }

    /**
     * Gets the size of the column in bytes
     * @return the size
     */
    private int getColumnSize() {
        return DataUtils.getDataTypeSize(typeChar) * elementsNumber;
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.AttributeType#getFieldLength()
     */
    public int getFieldLength() {
        return attribute.getFieldLength();
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.AttributeType#getName()
     */
    public String getName() {
        return attribute.getName();
    }

    /**
     * Gets the value of narrTable
     *
     * @return the value of narrTable
     */
    public String getNarrTable() {
        return this.narrTable;
    }

    /**
     * Gets the value of thematicIdx
     *
     * @return the value of thematicIdx
     */
    public String getThematicIdx() {
        return this.thematicIdx;
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.AttributeType#getType()
     */
    public Class getType() {
        return attribute.getType();
    }

    /**
     * Gets the value of valDescTableName
     *
     * @return the value of valDescTableName
     */
    public String getValDescTableName() {
        return valDescTableName;
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.AttributeType#isGeometry()
     */
    public boolean isGeometry() {
        return attribute.isGeometry();
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.AttributeType#isNested()
     */
    public boolean isNested() {
        return attribute.isNested();
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.AttributeType#isNillable()
     */
    public boolean isNillable() {
        return attribute.isNillable();
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.AttributeType#parse(java.lang.Object)
     */
    public Object parse(Object value) throws IllegalArgumentException {
        return attribute.parse(value);
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.AttributeType#validate(java.lang.Object)
     */
    public void validate(Object obj) throws IllegalArgumentException {
        attribute.validate(obj);
    }

    /**
     * Returns the typeChar field
     *
     * @return Returns the typeChar.
     */
    public char getTypeChar() {
        return typeChar;
    }

    /**
     * Returns the elementsNumber field
     *
     * @return Returns the elementsNumber.
     */
    public int getElementsNumber() {
        return elementsNumber;
    }
    /**
     * Identifies and returns the GeometryAttributeType,
     * or null if none exists.
     * @return The <code>GeometryAttributeType</code> value
     */
    public GeometryAttributeType getGeometryAttributeType() {
        GeometryAttributeType result = null;

        if (isGeometry()) {
            result = (GeometryAttributeType) attribute;
        }

        return result;
    }
}
