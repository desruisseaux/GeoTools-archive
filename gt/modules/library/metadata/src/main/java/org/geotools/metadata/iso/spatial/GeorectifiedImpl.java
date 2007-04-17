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
import java.util.Collection;
import java.util.List;

// OpenGIS dependencies
import org.opengis.metadata.spatial.CellGeometry;
import org.opengis.metadata.spatial.Georectified;
import org.opengis.metadata.spatial.PixelOrientation;
import org.opengis.geometry.primitive.Point;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.util.CheckedArrayList;


/**
 * Grid whose cells are regularly spaced in a geographic (i.e., lat / long) or map
 * coordinate system defined in the Spatial Referencing System (SRS) so that any cell
 * in the grid can be geolocated given its grid coordinate and the grid origin, cell spacing,
 * and orientation indication of whether or not geographic.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 *
 * @since 2.1
 */
public class GeorectifiedImpl extends GridSpatialRepresentationImpl implements Georectified {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5875851898471237138L;

    /**
     * Indication of whether or not geographic position points are available to test the
     * accuracy of the georeferenced grid data.
     */
    private boolean checkPointAvailable;

    /**
     * Description of geographic position points used to test the accuracy of the
     * georeferenced grid data.
     */
    private InternationalString checkPointDescription;

    /**
     * Earth location in the coordinate system defined by the Spatial Reference System
     * and the grid coordinate of the cells at opposite ends of grid coverage along two
     * diagonals in the grid spatial dimensions. There are four corner points in a
     * georectified grid; at least two corner points along one diagonal are required.
     */
    private List cornerPoints;

    /**
     * Earth location in the coordinate system defined by the Spatial Reference System
     * and the grid coordinate of the cell halfway between opposite ends of the grid in the
     * spatial dimensions.
     */
    private Point centerPoint;

    /**
     * Point in a pixel corresponding to the Earth location of the pixel.
     */
    private PixelOrientation pointInPixel;

    /**
     * Description of the information about which grid dimensions are the spatial dimensions.
     */
    private InternationalString transformationDimensionDescription;

    /**
     * Information about which grid dimensions are the spatial dimensions.
     */
    private Collection transformationDimensionMapping;

    /**
     * Constructs an initially empty georectified object.
     */
    public GeorectifiedImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public GeorectifiedImpl(final Georectified source) {
        super(source);
    }

    /**
     * Creates a georectified object initialized to the specified values.
     */
    public GeorectifiedImpl(final int              numberOfDimensions,
                            final List             axisDimensionsProperties,
                            final CellGeometry     cellGeometry,
                            final boolean          transformationParameterAvailable,
                            final boolean          checkPointAvailable,
                            final List             cornerPoints,
                            final PixelOrientation pointInPixel)
    {
        super(numberOfDimensions,
              axisDimensionsProperties,
              cellGeometry,
              transformationParameterAvailable);
        setCheckPointAvailable(checkPointAvailable);
        setCornerPoints       (cornerPoints       );
        setPointInPixel       (pointInPixel       );
    }

    /**
     * Indication of whether or not geographic position points are available to test the
     * accuracy of the georeferenced grid data.
     */
    public boolean isCheckPointAvailable() {
        return checkPointAvailable;
    }

    /**
     * Set indication of whether or not geographic position points are available to test the
     * accuracy of the georeferenced grid data.
     */
    public synchronized void setCheckPointAvailable(final boolean newValue) {
        checkWritePermission();
        checkPointAvailable = newValue;
    }

    /**
     * Description of geographic position points used to test the accuracy of the
     * georeferenced grid data.
     */
    public InternationalString getCheckPointDescription() {
        return checkPointDescription;
    }

    /**
     * Set the description of geographic position points used to test the accuracy of the
     * georeferenced grid data.
     */
    public synchronized void setCheckPointDescription(final InternationalString newValue) {
        checkWritePermission();
        checkPointDescription = newValue;
    }

    /**
     * Earth location in the coordinate system defined by the Spatial Reference System
     * and the grid coordinate of the cells at opposite ends of grid coverage along two
     * diagonals in the grid spatial dimensions. There are four corner points in a
     * georectified grid; at least two corner points along one diagonal are required.
     */
    public synchronized List getCornerPoints() {
        return cornerPoints = nonNullList(cornerPoints, Point.class);
    }

    /**
     * Set the corner points.
     */
    public synchronized void setCornerPoints(final List newValues) {
        checkWritePermission();
        if (cornerPoints == null) {
            cornerPoints = new CheckedArrayList(Point.class);
        } else {
            cornerPoints.clear();
        }
        cornerPoints.addAll(newValues);
    }

    /**
     * Earth location in the coordinate system defined by the Spatial Reference System
     * and the grid coordinate of the cell halfway between opposite ends of the grid in the
     * spatial dimensions.
     */
    public Point getCenterPoint() {
        return centerPoint;
    }

    /**
     * Set the center point.
     */
    public synchronized void setCenterPoint(final Point newValue) {
        checkWritePermission();
        centerPoint = newValue;
    }

    /**
     * Point in a pixel corresponding to the Earth location of the pixel.
     */
    public PixelOrientation getPointInPixel() {
        return pointInPixel;
    }

    /**
     * Set the point in a pixel corresponding to the Earth location of the pixel.
     */
    public synchronized void setPointInPixel(final PixelOrientation newValue) {
        checkWritePermission();
        pointInPixel = newValue;
    }

    /**
     * Description of the information about which grid dimensions are the spatial dimensions.
     */
    public InternationalString getTransformationDimensionDescription() {
        return transformationDimensionDescription;
    }

    /**
     * Set the description of the information about which grid dimensions are the spatial dimensions.
     */
    public synchronized void setTransformationDimensionDescription(final InternationalString newValue) {
        checkWritePermission();
        transformationDimensionDescription = newValue;
    }

    /**
     * Information about which grid dimensions are the spatial dimensions.
     */
    public synchronized Collection getTransformationDimensionMapping() {
        return transformationDimensionMapping = nonNullCollection(transformationDimensionMapping,
                                                                  InternationalString.class);
    }

    /**
     * Set information about which grid dimensions are the spatial dimensions.
     */
    public synchronized void setTransformationDimensionMapping(final Collection newValues) {
        transformationDimensionMapping = copyCollection(newValues, transformationDimensionMapping,
                                                        InternationalString.class);
    }
}
