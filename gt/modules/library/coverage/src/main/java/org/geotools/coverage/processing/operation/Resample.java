/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2002, Institut de Recherche pour le Développement
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.coverage.processing.operation;

// JAI dependencies
import javax.media.jai.Interpolation;
import javax.media.jai.operator.WarpDescriptor;    // For javadoc
import javax.media.jai.operator.AffineDescriptor;  // For javadoc

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

// Geotools dependencies
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.Operation2D;
import org.geotools.coverage.processing.CannotReprojectException;
import org.geotools.factory.Hints;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.image.ImageUtilities;


/**
 * Resample a grid coverage using a different grid geometry. This operation provides the following
 * functionality:
 * <p>
 * <UL>
 *   <LI><strong>Resampling</strong><br>
 *       The grid coverage can be resampled at a different cell resolution. Some implementations
 *       may be able to do resampling efficiently at any resolution. Also a non-rectilinear grid
 *       coverage can be accessed as rectilinear grid coverage with this operation.</LI>
 *   <LI><strong>Reprojecting</strong><br>
 *       The new grid geometry can have a different coordinate reference system than the underlying
 *       grid geometry. For example, a grid coverage can be reprojected from a geodetic coordinate
 *       reference system to Universal Transverse Mercator CRS.</LI>
 *   <LI><strong>Subsetting</strong><br>
 *       A subset of a grid can be viewed as a separate coverage by using this operation with a
 *       grid geometry which as the same geoferencing and a region. Grid range in the grid geometry
 *       defines the region to subset in the grid coverage.</LI>
 * </UL>
 * <p>
 * <strong>Geotools extension:</strong><br>
 * The {@code "Resample"} operation use the default
 * {@link org.opengis.referencing.operation.CoordinateOperationFactory} for creating a
 * transformation from the source to the destination coordinate reference systems.
 * If a custom factory is desired, it may be supplied as a rendering hint with the
 * {@link org.geotools.factory.Hints#COORDINATE_OPERATION_FACTORY} key. Rendering
 * hints can be supplied to {@link org.geotools.coverage.processing.DefaultProcessor}
 * at construction time.
 *
 * <P><STRONG>Name:</STRONG>&nbsp;<CODE>"Resample"</CODE><BR>
 *    <STRONG>JAI operator:</STRONG>&nbsp;<CODE>"{@linkplain AffineDescriptor Affine}"</CODE>
 *            or <CODE>"{@linkplain WarpDescriptor Warp}"</CODE><BR>
 *    <STRONG>Parameters:</STRONG></P>
 * <table border='3' cellpadding='6' bgcolor='F4F8FF'>
 *   <tr bgcolor='#B9DCFF'>
 *     <th>Name</th>
 *     <th>Class</th>
 *     <th>Default value</th>
 *     <th>Minimum value</th>
 *     <th>Maximum value</th>
 *   </tr>
 *   <tr>
 *     <td>{@code "Source"}</td>
 *     <td>{@link org.geotools.coverage.grid.GridCoverage2D}</td>
 *     <td align="center">N/A</td>
 *     <td align="center">N/A</td>
 *     <td align="center">N/A</td>
 *   </tr>
 *   <tr>
 *     <td>{@code "InterpolationType"}</td>
 *     <td>{@link java.lang.CharSequence}</td>
 *     <td>"NearestNieghbor"</td>
 *     <td align="center">N/A</td>
 *     <td align="center">N/A</td>
 *   </tr>
 *   <tr>
 *     <td>{@code "CoordinateReferenceSystem"}</td>
 *     <td>{@link org.opengis.referencing.crs.CoordinateReferenceSystem}</td>
 *     <td>Same as source grid coverage</td>
 *     <td align="center">N/A</td>
 *     <td align="center">N/A</td>
 *   </tr>
 *   <tr>
 *     <td>{@code "GridGeometry"}</td>
 *     <td>{@link org.geotools.coverage.grid.GridGeometry2D}</td>
 *     <td>(automatic)</td>
 *     <td align="center">N/A</td>
 *     <td align="center">N/A</td>
 *   </tr>
 * </table>
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see org.geotools.coverage.processing.Operations#resample
 * @see WarpDescriptor
 */
public class Resample extends Operation2D {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2022393087647420577L;

    /**
     * The parameter descriptor for the interpolation type.
     */
    public static final ParameterDescriptor INTERPOLATION_TYPE =
            new DefaultParameterDescriptor(Citations.OGC, "InterpolationType",
                Object.class,                       // Value class (mandatory)
                null,                               // Array of valid values
                "NearestNeighbor",                  // Default value
                null,                               // Minimal value
                null,                               // Maximal value
                null,                               // Unit of measure
                false);                             // Parameter is optional

    /**
     * The parameter descriptor for the coordinate reference system.
     */
    public static final ParameterDescriptor COORDINATE_REFERENCE_SYSTEM =
            new DefaultParameterDescriptor(Citations.OGC, "CoordinateReferenceSystem",
                CoordinateReferenceSystem.class,    // Value class (mandatory)
                null,                               // Array of valid values
                null,                               // Default value
                null,                               // Minimal value
                null,                               // Maximal value
                null,                               // Unit of measure
                false);                             // Parameter is optional

    /**
     * The parameter descriptor for the grid geometry.
     */
    public static final ParameterDescriptor GRID_GEOMETRY =
            new DefaultParameterDescriptor(Citations.OGC, "GridGeometry",
                GridGeometry2D.class,               // Value class (mandatory)
                null,                               // Array of valid values
                null,                               // Default value
                null,                               // Minimal value
                null,                               // Maximal value
                null,                               // Unit of measure
                false);                             // Parameter is optional

    /**
     * Constructs a {@code "Resample"} operation.
     */
    public Resample() {
        super(new DefaultParameterDescriptorGroup(Citations.OGC, "Resample",
              new ParameterDescriptor[] {
                    SOURCE_0,
                    INTERPOLATION_TYPE,
                    COORDINATE_REFERENCE_SYSTEM,
                    GRID_GEOMETRY
        }));
    }

    /**
     * Resample a grid coverage. This method is invoked by
     * {@link org.geotools.coverage.processing.DefaultProcessor}
     * for the {@code "Resample"} operation.
     */
    public Coverage doOperation(final ParameterValueGroup parameters, final Hints hints) {
        GridCoverage2D         source = (GridCoverage2D)               parameters.parameter("Source")                   .getValue();
        Interpolation          interp = ImageUtilities.toInterpolation(parameters.parameter("InterpolationType")        .getValue());
        CoordinateReferenceSystem crs = (CoordinateReferenceSystem)    parameters.parameter("CoordinateReferenceSystem").getValue();
        GridGeometry2D       gridGeom = (GridGeometry2D)               parameters.parameter("GridGeometry")             .getValue();
        GridCoverage2D       coverage; // The result to be computed below.
        if (crs == null) {
            crs = source.getCoordinateReferenceSystem();
        }
        try {
            coverage = Resampler2D.reproject(source, crs, gridGeom, interp,
                (hints instanceof Hints) ? (Hints) hints : new Hints(hints));
        } catch (FactoryException exception) {
            throw new CannotReprojectException(Errors.format(
                    ErrorKeys.CANT_REPROJECT_$1, source.getName()), exception);
        } catch (TransformException exception) {
            throw new CannotReprojectException(Errors.format(
                    ErrorKeys.CANT_REPROJECT_$1, source.getName()), exception);
        }
        return coverage;
    }
}
