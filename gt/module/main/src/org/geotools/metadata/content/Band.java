/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le D�veloppement
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
package org.geotools.metadata.content;

// J2SE extensions
import javax.units.Unit;

import org.geotools.resources.Utilities;


/**
 * Range of wavelengths in the electromagnetic spectrum.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 */
public class Band extends RangeDimension
       implements org.opengis.metadata.content.Band
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2302918545469034653L;

    /**
     * Longest wavelength that the sensor is capable of collecting within a designated band.
     */
    private Number maxValue;

    /**
     * Shortest wavelength that the sensor is capable of collecting within a designated band.
     */
    private Number minValue;

    /**
     * Units in which sensor wavelengths are expressed. Should be non-null if
     * {@linkplain #getMinValue min value} or {@linkplain #getMaxValue max value}
     * are provided.
     */
    private Unit units;

    /**
     * Wavelength at which the response is the highest.
     * <code>null</code> if unspecified.
     */
    private Number peakResponse;

    /**
     * Maximum number of significant bits in the uncompressed representation for the value
     * in each band of each pixel.
     * <code>null</code> if unspecified.
     */
    private Integer bitsPerValue;

    /**
     * Number of discrete numerical values in the grid data.
     * <code>null</code> if unspecified.
     */
    private Integer toneGradation;

    /**
     * Scale factor which has been applied to the cell value.
     * <code>null</code> if unspecified.
     */
    private Number scaleFactor;

    /**
     * The physical value corresponding to a cell value of zero.
     * <code>null</code> if unspecified.
     */
    private Number offset;

    /**
     * Constructs an initially empty band.
     */
    public Band() {
    }

    /**
     * Returns the longest wavelength that the sensor is capable of collecting within
     * a designated band. Returns <code>null</code> if unspecified.
     */
    public Number getMaxValue() {
        return maxValue;
    }

    /**
     * Set the longest wavelength that the sensor is capable of collecting within a
     * designated band. Returns <code>null</code> if unspecified.
     */
    public synchronized void setMaxValue(final Number newValue) {
        checkWritePermission();
        maxValue = newValue;
    }

    /**
     * Returns the shortest wavelength that the sensor is capable of collecting
     * within a designated band.
     */
    public Number getMinValue() {
        return minValue;
    }

    /**
     * Set the shortest wavelength that the sensor is capable of collecting within
     * a designated band.
     */
    public synchronized void setMinValue(final Number newValue) {
        checkWritePermission();
        minValue = newValue;
    }

    /**
     * Returns the units in which sensor wavelengths are expressed. Should be non-null
     * if {@linkplain #getMinValue min value} or {@linkplain #getMaxValue max value}
     * are provided.
     */
    public Unit getUnits() {
        return units;
    }
    
    /**
     * Set the units in which sensor wavelengths are expressed. Should be non-null if
     * {@linkplain #getMinValue min value} or {@linkplain #getMaxValue max value}
     * are provided.
     */
    public synchronized void setUnits(final Unit newValue) {
        checkWritePermission();
        units = newValue;
    }

    /**
     * Returns the wavelength at which the response is the highest.
     * Returns <code>null</code> if unspecified.
     */
    public Number getPeakResponse() {
        return peakResponse;
    }

    /**
     * Set the wavelength at which the response is the highest.
     */
    public synchronized void setPeakResponse(final Number newValue) {
        checkWritePermission();
        peakResponse = newValue;
    }

    /**
     * Returns the maximum number of significant bits in the uncompressed
     * representation for the value in each band of each pixel.
     * Returns <code>null</code> if unspecified.
     */
    public Integer getBitsPerValue() {
        return bitsPerValue;
    }

    /**
     * Set the maximum number of significant bits in the uncompressed representation
     * for the value in each band of each pixel.
     */
    public synchronized void setBitsPerValue(final Integer newValue) {
        checkWritePermission();
        bitsPerValue = newValue;
    }

    /**
     * Returns the number of discrete numerical values in the grid data.
     * Returns <code>null</code> if unspecified.
     */
    public Integer getToneGradation() {
        return toneGradation;
    }

    /**
     * Set the number of discrete numerical values in the grid data.
     */
    public synchronized void setToneGradation(final Integer newValue) {
        checkWritePermission();
        toneGradation = newValue;
    }

    /**
     * Returns the scale factor which has been applied to the cell value.
     * Returns <code>null</code> if unspecified.
     */
    public Number getScaleFactor() {
        return scaleFactor;
    }

    /**
     * Set the scale factor which has been applied to the cell value.
     */
    public synchronized void setScaleFactor(final Number newValue) {
        checkWritePermission();
        scaleFactor = newValue;
    }

    /**
     * Returns the physical value corresponding to a cell value of zero.
     * Returns <code>null</code> if unspecified.
     */
    public Number getOffset() {
        return offset;
    }

    /**
     * Set the physical value corresponding to a cell value of zero.
:     */
    public synchronized void setOffset(final Number newValue) {
        checkWritePermission();
        offset = newValue;
    }
    
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        maxValue       = (Number)  unmodifiable(maxValue);
        minValue       = (Number)  unmodifiable(minValue);
        units          = (Unit)    unmodifiable(units);
        peakResponse   = (Number)  unmodifiable(peakResponse);
        bitsPerValue   = (Integer) unmodifiable(bitsPerValue);
        toneGradation  = (Integer) unmodifiable(toneGradation);
        scaleFactor    = (Number)  unmodifiable(scaleFactor);
        offset         = (Number)  unmodifiable(offset);
    }

    /**
     * Compare this Band with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final Band that = (Band) object;
            return Utilities.equals(this.maxValue,      that.maxValue      ) &&
                   Utilities.equals(this.minValue,      that.minValue      ) &&
                   Utilities.equals(this.units,         that.units         ) &&
                   Utilities.equals(this.peakResponse,  that.peakResponse  ) &&
                   Utilities.equals(this.bitsPerValue,  that.bitsPerValue  ) &&
                   Utilities.equals(this.toneGradation, that.toneGradation ) &&
                   Utilities.equals(this.scaleFactor,   that.scaleFactor   ) &&
                   Utilities.equals(this.offset,        that.offset        )  ;
        }
        return false;
    }

    /**
     * Returns a hash code value for this band. For performance reason, this method do
     * not uses all attributes for computing the hash code. Instead, it uses the attributes
     * that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (maxValue != null)  code ^= maxValue.hashCode();
        if (minValue != null)  code ^= minValue.hashCode();
        return code;
    }
}
