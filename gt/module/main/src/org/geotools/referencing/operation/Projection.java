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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.operation;

// J2SE dependencies
import java.util.Map;

// OpenGIS dependencies
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationMethod;


/**
 * A {@linkplain org.geotools.referencing.operation.Conversion conversion} transforming
 * (<var>longitude</var>,<var>latitude</var>) coordinates to cartesian coordinates
 * (<var>x</var>,<var>y</var>).
 *
 * <P>An unofficial list of projections and their parameters can
 * be found <A HREF="http://www.remotesensing.org/geotiff/proj_list/">there</A>.
 * Most projections expect the following parameters:
 *  <code>"semi_major"</code> (mandatory),
 *  <code>"semi_minor"</code> (mandatory),
 *  <code>"central_meridian"</code> (default to 0),
 *  <code>"latitude_of_origin"</code> (default to 0),
 *  <code>"scale_factor"</code> (default to 1),
 *  <code>"false_easting"</code> (default to 0) and
 *  <code>"false_northing"</code> (default to 0).</P>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see org.geotools.referencing.crs.ProjectedCRS
 * @see <A HREF="http://mathworld.wolfram.com/MapProjection.html">Map projections on MathWorld</A>
 */
public class Projection extends Conversion implements org.opengis.referencing.operation.Projection {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -7176751851369816864L;

    /**
     * Construct a projection from a set of properties. The properties given in argument
     * follow the same rules than for the {@link CoordinateOperation} constructor.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param sourceCRS The source CRS, or <code>null</code> if not available.
     * @param targetCRS The target CRS, or <code>null</code> if not available.
     * @param transform Transform from positions in the {@linkplain #getSourceCRS source coordinate
     *                  reference system} to positions in the {@linkplain #getTargetCRS target
     *                  coordinate reference system}.
     * @param method    The operation method.
     */
    public Projection(final Map                       properties,
                      final CoordinateReferenceSystem sourceCRS,
                      final CoordinateReferenceSystem targetCRS,
                      final MathTransform             transform,
                      final OperationMethod           method)
    {
        super(properties, sourceCRS, targetCRS, transform, method);
    }
}
