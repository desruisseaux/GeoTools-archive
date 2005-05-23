/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.metadata.iso.quality;

// J2SE dependencies and extension
import java.util.Arrays;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.metadata.quality.QuantitativeResult;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * Information about the value (or set of values) obtained from applying a data quality measure.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class QuantitativeResultImpl extends ResultImpl implements QuantitativeResult {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = -3430567391539903195L;
    
    /**
     * Quantitative value or values, content determined by the evaluation procedure used.
     */    
    private double[] values;

    /**
     * Value type for reporting a data quality result, or <code>null</code> if none.
     */
    private Class valueType;

    /**
     * Value unit for reporting a data quality result, or <code>null</code> if none.
     */
    private Unit valueUnit;

    /**
     * Statistical method used to determine the value, or <code>null</code> if none.
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
    public synchronized double[] getValues() {
        if (isModifiable()) {
            return values;
        } else {
            return (double[]) values.clone();
        }
    }

    /**
     * Set the quantitative value or values, content determined by the evaluation procedure used.
     */
    public synchronized void setValues(final double[] newValues) {
        checkWritePermission();
        values = (double[]) newValues.clone();
    }

    /**
     * Value type for reporting a data quality result, or <code>null</code> if none.
     */
    public Class getValueType()  {
        return valueType;
    }

    /**
     * Set the value type for reporting a data quality result, or <code>null</code> if none.
     *
     * @todo Verify if the value is of the requested type.
     */
    public synchronized void setValueType(final Class newValue) {
        checkWritePermission();
        valueType = newValue;
    }
    
    /**
     * Value unit for reporting a data quality result, or <code>null</code> if none.
     */
    public Unit getValueUnit()  {
        return valueUnit;
    }

    /**
     * Set the value unit for reporting a data quality result, or <code>null</code> if none.
     */
    public synchronized void setValueUnit(final Unit newValue) {
        checkWritePermission();
        valueUnit = newValue;
    }

    /**
     * Statistical method used to determine the value, or <code>null</code> if none.
     */
    public InternationalString getErrorStatistic()  {
        return errorStatistic;
    }

    /**
     * Set the statistical method used to determine the value, or <code>null</code> if none.
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
        values         = (double[])            unmodifiable(values);
        valueType      = (Class)               unmodifiable(valueType);
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
            return    Arrays.equals(this.values,         that.values         ) &&
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
