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
package org.geotools.metadata.spatial;

// J2SE direct dependencies
import java.util.Collections;
import java.util.Set;

import org.geotools.resources.Utilities;
import org.geotools.util.CheckedHashSet;
import org.opengis.metadata.spatial.CellGeometry;


/**
 * Basic information required to uniquely identify a resource or resources.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 */
public class GridSpatialRepresentation extends SpatialRepresentation
        implements org.opengis.metadata.spatial.GridSpatialRepresentation
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8400572307442433979L;

    /**
     * Number of independent spatial-temporal axes.
     */
    private int numberOfDimensions;

    /**
     * Information about spatial-temporal axis properties.
     */
    private Set axisDimensionsProperties;

    /**
     * Identification of grid data as point or cell.
     */
    private CellGeometry cellGeometry;

    /**
     * Indication of whether or not parameters for transformation exists.
     */
    private boolean transformationParameterAvailable;

    /**
     * Construct an initially empty grid spatial representation.
     */
    public GridSpatialRepresentation() {
    }

    /**
     * Creates a grid spatial representation initialized to the given values.
     */
    public GridSpatialRepresentation(final int numberOfDimensions,
                                     final Set axisDimensionsProperties,
                                     final CellGeometry cellGeometry,
                                     final boolean transformationParameterAvailable)
    {
        setNumberOfDimensions               (numberOfDimensions);
        setAxisDimensionsProperties         (axisDimensionsProperties);
        setCellGeometry                     (cellGeometry);
        setTransformationParameterAvailable (transformationParameterAvailable);
    }

    /**
     * Number of independent spatial-temporal axes.
     */
    public int getNumberOfDimensions() {
        return numberOfDimensions;
    }

    /**
     * Set the number of independent spatial-temporal axes.
     */
    public synchronized void setNumberOfDimensions(final int newValue) {
        checkWritePermission();
        numberOfDimensions = newValue;
    }
    
    /**
     * Information about spatial-temporal axis properties.
     */
    public Set getAxisDimensionsProperties() {
        final Set axisDimensionsProperties = this.axisDimensionsProperties; // Avoid synchronization
        return (axisDimensionsProperties!=null) ? axisDimensionsProperties : Collections.EMPTY_SET;
    }

    /**
     * Set information about spatial-temporal axis properties.
     */
    public synchronized void setAxisDimensionsProperties(final Set newValues) {
        checkWritePermission();
        if (axisDimensionsProperties == null) {
            axisDimensionsProperties = new CheckedHashSet(Dimension.class);
        } else {
            axisDimensionsProperties.clear();
        }
        axisDimensionsProperties.addAll(newValues);
    }

    /**
     * Identification of grid data as point or cell.
     */
    public CellGeometry getCellGeometry() {
        return cellGeometry;        
    }

    /**
     * Set identification of grid data as point or cell.
     */
    public synchronized void setCellGeometry(final CellGeometry newValue) {
        checkWritePermission();
        cellGeometry = newValue;
    }

    /**
     * Indication of whether or not parameters for transformation exists.
     */
    public boolean isTransformationParameterAvailable() {
        return transformationParameterAvailable;
    }

    /**
     * Set indication of whether or not parameters for transformation exists.
     */
    public synchronized void setTransformationParameterAvailable(final boolean newValue) {
        checkWritePermission();
        transformationParameterAvailable = newValue;
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        axisDimensionsProperties = (Set) unmodifiable(axisDimensionsProperties);
        cellGeometry = (CellGeometry) unmodifiable(cellGeometry); 
    }

    /**
     * Compare this grid spatial representation with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final GridSpatialRepresentation that = (GridSpatialRepresentation) object; 
            return  Utilities.equals(this.axisDimensionsProperties, that.axisDimensionsProperties  ) &&
                    Utilities.equals(this.cellGeometry,             that.cellGeometry  ) &&
                    (this.numberOfDimensions                     == that.numberOfDimensions ) &&
                    (this.transformationParameterAvailable       == that.transformationParameterAvailable ) ;
        }
        return false;
    }

    /**
     * Returns a hash code value for this representation. For performance reason, this method do
     * not uses all attributes for computing the hash code. Instead, it uses the attributes
     * that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (axisDimensionsProperties != null)  code ^= axisDimensionsProperties.hashCode();
        if (cellGeometry != null)              code ^= cellGeometry.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this representation.
     */
    public String toString() {
        return String.valueOf(axisDimensionsProperties);
    }            
}
