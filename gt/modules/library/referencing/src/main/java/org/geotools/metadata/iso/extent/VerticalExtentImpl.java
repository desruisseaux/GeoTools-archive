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
package org.geotools.metadata.iso.extent;

// J2SE direct dependencies
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.metadata.extent.VerticalExtent;
import org.opengis.referencing.datum.VerticalDatum;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Vertical domain of dataset.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
public class VerticalExtentImpl extends MetadataEntity implements VerticalExtent {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -6288728580430359681L;

    /**
     * The lowest vertical extent contained in the dataset.
     */
    private double minimumValue;

    /**
     * The highest vertical extent contained in the dataset.
     */
    private double maximumValue;

    /**
     * The vertical units used for vertical extent information.
     * Examples: metres, feet, millimetres, hectopascals.
     */
    private Unit unit;

    /**
     * Provides information about the origin from which the
     * maximum and minimum elevation values are measured.
     */
    private VerticalDatum verticalDatum;
    
    /**
     * Constructs an initially empty vertical extent.
     */
    public VerticalExtentImpl() {
    }

    /**
     * Creates a vertical extent initialized to the specified values.
     */
    public VerticalExtentImpl(final double minimumValue,
                          final double maximumValue,
                          final Unit   unit,
                          final VerticalDatum verticalDatum)
    {
        setMinimumValue (minimumValue );
        setMaximumValue (maximumValue );
        setUnit         (unit         );
        setVerticalDatum(verticalDatum);
    }

    /**
     * Returns the lowest vertical extent contained in the dataset.
     */
    public double getMinimumValue() {
        return minimumValue;
    }
    
    /**
     * Set the lowest vertical extent contained in the dataset.
     */
    public synchronized void setMinimumValue(final double newValue) {
        checkWritePermission();
        minimumValue = newValue;
    }

    /**
     * Returns the highest vertical extent contained in the dataset.
     */
    public double getMaximumValue() {
        return maximumValue;
    }

    /**
     * Set the highest vertical extent contained in the dataset.
     */
    public synchronized void setMaximumValue(final double newValue) {
        checkWritePermission();
        maximumValue = newValue;
    }

    /**
     * Returns the vertical units used for vertical extent information.
     * Examples: metres, feet, millimetres, hectopascals.
     */
    public Unit getUnit()  {
        return unit;
    }

    /**
     * Set the vertical units used for vertical extent information.
     * Examples: metres, feet, millimetres, hectopascals.
     */
    public synchronized void setUnit(final Unit newValue) {
        checkWritePermission();
        unit = newValue;
    }

    /**
     * Provides information about the origin from which the
     * maximum and minimum elevation values are measured.
     */
    public VerticalDatum getVerticalDatum()  {
        return verticalDatum;
    }
    
    /**
     * Set information about the origin from which the
     * maximum and minimum elevation values are measured.
     */
    public synchronized void setVerticalDatum(final VerticalDatum newValue ) {
        checkWritePermission();
        verticalDatum = newValue;
    }
    
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        unit          = (Unit)          unmodifiable(unit);
        verticalDatum = (VerticalDatum) unmodifiable(verticalDatum);
    }

    /**
     * Compare this vertical extent with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final VerticalExtentImpl that = (VerticalExtentImpl) object;
            return Utilities.equals(this.unit,           that.unit          ) &&
                   Utilities.equals(this.verticalDatum,  that.verticalDatum ) &&
                   Double.doubleToLongBits(this.minimumValue) ==
                   Double.doubleToLongBits(that.minimumValue) &&
                   Double.doubleToLongBits(this.maximumValue) ==
                   Double.doubleToLongBits(that.maximumValue);
        }
        return false;
    }

    /**
     * Returns a hash code value for this extent.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (unit          != null) code ^= unit         .hashCode();
        if (verticalDatum != null) code ^= verticalDatum.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this extent.
     */
    public String toString() {
        return String.valueOf(verticalDatum);
    }
}
