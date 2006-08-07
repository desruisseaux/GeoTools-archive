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
package org.geotools.referencing.operation;

// J2SE dependencies
import java.util.Map;

// OpenGIS dependencies
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.PlanarProjection;


/**
 * Base class for for azimuthal (or planar) map projections.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.1
 *
 * @see org.geotools.referencing.crs.DefaultProjectedCRS
 * @see <A HREF="http://mathworld.wolfram.com/AzimuthalProjection.html">Azimuthal projection on MathWorld</A>
 */
public class DefaultPlanarProjection extends DefaultProjection implements PlanarProjection {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8171256287775067736L;

    /**
     * Constructs a new projection with the same values than the specified one, together with the
     * specified source and target CRS. While the source conversion can be an arbitrary one, it is
     * typically a {@linkplain DefiningConversion defining conversion}.
     *
     * @param conversion The defining conversion.
     * @param sourceCRS The source CRS.
     * @param targetCRS The target CRS.
     * @param transform Transform from positions in the {@linkplain #getSourceCRS source CRS}
     *                  to positions in the {@linkplain #getTargetCRS target CRS}.
     */
    public DefaultPlanarProjection(final Conversion                conversion,
                                   final CoordinateReferenceSystem sourceCRS,
                                   final CoordinateReferenceSystem targetCRS,
                                   final MathTransform             transform)
    {
        super(conversion, sourceCRS, targetCRS, transform);
    }

    /**
     * Constructs a projection from a set of properties. The properties given in argument
     * follow the same rules than for the {@link AbstractCoordinateOperation} constructor.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param sourceCRS The source CRS, or {@code null} if not available.
     * @param targetCRS The target CRS, or {@code null} if not available.
     * @param transform Transform from positions in the {@linkplain #getSourceCRS source coordinate
     *                  reference system} to positions in the {@linkplain #getTargetCRS target
     *                  coordinate reference system}.
     * @param method    The operation method.
     */
    public DefaultPlanarProjection(final Map                       properties,
                                   final CoordinateReferenceSystem sourceCRS,
                                   final CoordinateReferenceSystem targetCRS,
                                   final MathTransform             transform,
                                   final OperationMethod           method)
    {
        super(properties, sourceCRS, targetCRS, transform, method);
    }
}
