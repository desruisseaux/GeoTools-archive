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
package org.geotools.metadata.iso.spatial;

// OpenGIS dependencies
import org.opengis.metadata.spatial.Dimension;
import org.opengis.metadata.spatial.DimensionNameType;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Axis properties.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class DimensionImpl extends MetadataEntity implements Dimension {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1945030788532182129L;
    
    /**
     * Name of the axis.
     */
    private DimensionNameType dimensionName;

    /**
     * Number of elements along the axis.
     */
    private int dimensionSize;

    /**
     * Degree of detail in the grid dataset.
     */
    private double resolution;

    /**
     * Constructs an initially empty dimension.
     */
    public DimensionImpl() {
    }

    /*
     * Creates a dimension initialized to the given type.
     */
    public DimensionImpl(final DimensionNameType dimensionName, final int dimensionSize) {
        setDimensionName(dimensionName);
        setDimensionSize(dimensionSize);
    }

    /**
     * Name of the axis.
     */
    public DimensionNameType getDimensionName() {
        return dimensionName;
    }

    /**
     * Set the name of the axis.
     */
    public synchronized void setDimensionName(final DimensionNameType newValue) {
        checkWritePermission();
        dimensionName = newValue;
    }

    /**
     * Number of elements along the axis.
     */
    public int getDimensionSize() {
        return dimensionSize;
    }

    /**
     * Set the number of elements along the axis.
     */
    public synchronized void setDimensionSize(final int newValue) {
        checkWritePermission();
        dimensionSize = newValue;
    }

    /**
     * Degree of detail in the grid dataset.
     */
    public double getResolution() {
        return resolution;
    }

    /**
     * Set the degree of detail in the grid dataset.
     */
    public synchronized void setResolution(final double newValue) {
        checkWritePermission();
        resolution = newValue;
    }
    
    /**
     * Declares this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
    }

    /**
     * Compares this dimension with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final DimensionImpl that = (DimensionImpl) object; 
            return Utilities.equals(this.dimensionName,   that.dimensionName) &&
                                   (this.dimensionSize == that.dimensionSize) &&
                                   (this.resolution    == that.resolution   );
        }
        return false;
    }

    /**
     * Returns a hash code value for this dimension. For performance reason, this method do
     * not uses all attributes for computing the hash code. Instead, it uses the attributes
     * that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (dimensionName != null) code ^= dimensionName.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this dimension.
     */
    public String toString() {
        return String.valueOf(dimensionName);
    }            
}
