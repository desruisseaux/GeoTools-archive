/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le D�veloppement
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
package org.geotools.metadata.iso.identification;

// OpenGIS dependencies
import org.opengis.metadata.identification.RepresentativeFraction;
import org.opengis.metadata.identification.Resolution;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;


/**
 * Level of detail expressed as a scale factor or a ground distance.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 *
 * @since 2.1
 */
public class ResolutionImpl extends MetadataEntity implements Resolution {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = 4418980634370167689L;

    /**
     * Level of detail expressed as the scale of a comparable hardcopy map or chart.
     * This value should be between 0 and 1.
     * Only one of {@linkplain #getEquivalentScale equivalent scale} and
     * {@linkplain #getDistance ground sample distance} may be provided.
     */
    private RepresentativeFraction equivalentScale;

    /**
     * Ground sample distance.
     * Only one of {@linkplain #getEquivalentScale equivalent scale} and
     * {@linkplain #getDistance ground sample distance} may be provided.
     */
    private Double distance;

    /**
     * Constructs an initially empty Resolution.
     */
    public ResolutionImpl() {
    }

    /**
     * Level of detail expressed as the scale of a comparable hardcopy map or chart.
     * This value should be between 0 and 1.
     * Only one of {@linkplain #getEquivalentScale equivalent scale} and
     * {@linkplain #getDistance ground sample distance} may be provided.
     */
    public RepresentativeFraction getEquivalentScale()  {
        return equivalentScale;
    }

    /**
     * Set the level of detail expressed as the scale of a comparable hardcopy map or chart.
     * 
     * Values greater than 1 will be stored as (1 / newValue), values less than one will be stored as is.
     */
    public synchronized void setEquivalentScale(final double newValue) {
        checkWritePermission();
        equivalentScale = RepresentativeFractionImpl.fromDouble(newValue);
    }

    /**
     * Set the level of detail expressed as the scale of a comparable hardcopy map or chart.
     */
    public synchronized void setEquivalentScale(final RepresentativeFraction newValue) {
        checkWritePermission();
        equivalentScale = newValue;
    }

    /**
     * Ground sample distance.
     * Only one of {@linkplain #getEquivalentScale equivalent scale} and
     * {@linkplain #getDistance ground sample distance} may be provided.
     */
    public Double getDistance() {
        return distance;
    }    

    /**
     * Set the ground sample distance.
     */
    public synchronized void setDistance(final Double newValue) {
        checkWritePermission();
        distance = newValue;
    }

    /**
     * Declares this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
    }

    /**
     * Compares this Resolution with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final ResolutionImpl that = (ResolutionImpl) object;
            return (this.equivalentScale == that.equivalentScale) &&
                   (this.distance        == that.distance       );
        }
        return false;
    }

    /**
     * Returns a hash code value for this resolution.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (equivalentScale != null) {
            code ^= (int) equivalentScale.hashCode();
        }
        if (distance != null) {
            code ^= distance.intValue();
        }
        return code;
    }

    /**
     * Returns a string representation of this resolution.
     */
    public String toString() {
        return String.valueOf(distance);
    }
}
