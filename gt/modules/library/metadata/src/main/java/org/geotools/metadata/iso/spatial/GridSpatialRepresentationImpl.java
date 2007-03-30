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
package org.geotools.metadata.iso.spatial;

// J2SE direct dependencies
import java.util.List;

// OpenGIS dependencies
import org.opengis.metadata.spatial.Dimension;
import org.opengis.metadata.spatial.CellGeometry;
import org.opengis.metadata.spatial.GridSpatialRepresentation;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.util.CheckedArrayList;


/**
 * Basic information required to uniquely identify a resource or resources.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 *
 * @since 2.1
 */
public class GridSpatialRepresentationImpl extends SpatialRepresentationImpl
        implements GridSpatialRepresentation
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8400572307442433979L;

    /**
     * Number of independent spatial-temporal axes.
     */
    private Integer numberOfDimensions;

    /**
     * Information about spatial-temporal axis properties.
     */
    private List axisDimensionsProperties;

    /**
     * Identification of grid data as point or cell.
     */
    private CellGeometry cellGeometry;

    /**
     * Indication of whether or not parameters for transformation exists.
     */
    private boolean transformationParameterAvailable;

    /**
     * Constructs an initially empty grid spatial representation.
     */
    public GridSpatialRepresentationImpl() {
    }

    /**
     * Creates a grid spatial representation initialized to the given values.
     */
    public GridSpatialRepresentationImpl(final int numberOfDimensions,
                                     final List axisDimensionsProperties,
                                     final CellGeometry cellGeometry,
                                     final boolean transformationParameterAvailable)
    {
        this(new Integer(numberOfDimensions),
             axisDimensionsProperties,
             cellGeometry,
             transformationParameterAvailable);
    }

    /**
     * Creates a grid spatial representation initialized to the given values.
     */
    public GridSpatialRepresentationImpl(final Integer numberOfDimensions,
                                     final List axisDimensionsProperties,
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
    public Integer getNumberOfDimensions() {
        return numberOfDimensions;
    }

    /**
     * Set the number of independent spatial-temporal axes.
     */
    public synchronized void setNumberOfDimensions(final Integer newValue) {
        checkWritePermission();
        numberOfDimensions = newValue;
    }
    
    /**
     * Information about spatial-temporal axis properties.
     */
    public synchronized List getAxisDimensionsProperties() {
        if (axisDimensionsProperties == null) {
            axisDimensionsProperties = new CheckedArrayList(Dimension.class);
        }
        return axisDimensionsProperties;
    }

    /**
     * Set information about spatial-temporal axis properties.
     */
    public synchronized void setAxisDimensionsProperties(final List newValues) {
        checkWritePermission();
        if (axisDimensionsProperties == null) {
            axisDimensionsProperties = new CheckedArrayList(Dimension.class);
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
        axisDimensionsProperties = (List) unmodifiable(axisDimensionsProperties);
    }

    /**
     * Compare this grid spatial representation with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final GridSpatialRepresentationImpl that = (GridSpatialRepresentationImpl) object; 
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
