/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.metadata.iso.quality;

// J2SE dependencies and extension
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.metadata.quality.QuantitativeResult;
import org.opengis.util.InternationalString;
import org.opengis.util.RecordType;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * Information about the value (or set of values) obtained from applying a data quality measure.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
public class QuantitativeResultImpl extends ResultImpl implements QuantitativeResult {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = -3430567391539903195L;
    
    /**
     * Quantitative value or values, content determined by the evaluation procedure used.
     */    
    private List/*<Double>*/ values;

    /**
     * Value type for reporting a data quality result, or {@code null} if none.
     */
    private RecordType valueType;

    /**
     * Value unit for reporting a data quality result, or {@code null} if none.
     */
    private Unit valueUnit;

    /**
     * Statistical method used to determine the value, or {@code null} if none.
     */
    private InternationalString errorStatistic;

    /**
     * Constructs an initially empty quantitative result.
     */
    public QuantitativeResultImpl() {
    }

    /**
     * Constructs a quantitative result initialized to the specified value.
     */
    public QuantitativeResultImpl(final double[] values) {
        setValues(values);
    }
    
    /**
     * Quantitative value or values, content determined by the evaluation procedure used.
     */
    public synchronized Collection getValues() {
        if (isModifiable()) {
            return values;
        } else {
            return Collections.unmodifiableList( values );            
        }
    }

    /**
     * Set the quantitative value or values, content determined by the evaluation procedure used.
     */
    public synchronized void setValues(final double[] newValues) {
        checkWritePermission();
        values = new ArrayList();
        for( int i=0; i<newValues.length;i++){
            values.add( new Double( newValues[i]));            
        }
    }

    public synchronized void setValues( List newValues ){
        values = newValues;
    }
    
    /**
     * Value type for reporting a data quality result, or {@code null} if none.
     */
    public RecordType getValueType()  {
        return valueType;
    }

    /**
     * Set the value type for reporting a data quality result, or {@code null} if none.
     *
     * @todo Verify if the value is of the requested type.
     */
    public synchronized void setValueType(final RecordType newValue) {
        checkWritePermission();
        valueType = newValue;
    }
    
    /**
     * Value unit for reporting a data quality result, or {@code null} if none.
     */
    public Unit getValueUnit()  {
        return valueUnit;
    }

    /**
     * Set the value unit for reporting a data quality result, or {@code null} if none.
     */
    public synchronized void setValueUnit(final Unit newValue) {
        checkWritePermission();
        valueUnit = newValue;
    }

    /**
     * Statistical method used to determine the value, or {@code null} if none.
     */
    public InternationalString getErrorStatistic()  {
        return errorStatistic;
    }

    /**
     * Set the statistical method used to determine the value, or {@code null} if none.
     */
    public synchronized void setErrorStatistic(final InternationalString newValue) {
        checkWritePermission();
        errorStatistic = newValue;
    } 

    /**
     * Declares this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        values         = (List)            unmodifiable(values);
        valueType      = (RecordType)          unmodifiable(valueType);
        valueUnit      = (Unit)                unmodifiable(valueUnit);
        errorStatistic = (InternationalString) unmodifiable(errorStatistic);
    }

    /**
     * Compare this quantitative result with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final QuantitativeResultImpl that = (QuantitativeResultImpl) object; 
            return Utilities.equals(this.values,         that.values         ) &&
                   Utilities.equals(this.valueType,      that.valueType      ) &&
                   Utilities.equals(this.valueUnit,      that.valueUnit      ) &&
                   Utilities.equals(this.errorStatistic, that.errorStatistic ) ;
        }
        return false;
    }

    /**
     * Returns a hash code value for this address. For performance reason, this method do
     * not uses all attributes for computing the hash code. Instead, it uses the attributes
     * that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
//TODO: Uses Arrays.hashCode when we will be allowed to uses J2SE 1.5.
//      if (values    != null) code ^= values   .hashCode();
        if (valueType != null) code ^= valueType.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this result.
     *
     * @todo Provides a more elaborated implementation.
     */
    public String toString() {
        return String.valueOf(valueType);
    }        
}
