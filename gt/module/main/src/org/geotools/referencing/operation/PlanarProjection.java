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
 * Base class for for azimuthal (or planar) map projections.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see org.geotools.referencing.crs.ProjectedCRS
 * @see <A HREF="http://mathworld.wolfram.com/AzimuthalProjection.html">Azimuthal projection on MathWorld</A>
 */
public class PlanarProjection extends Projection
                           implements org.opengis.referencing.operation.PlanarProjection
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8171256287775067736L;

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
    public PlanarProjection(final Map                       properties,
                            final CoordinateReferenceSystem sourceCRS,
                            final CoordinateReferenceSystem targetCRS,
                            final MathTransform             transform,
                            final OperationMethod           method)
    {
        super(properties, sourceCRS, targetCRS, transform, method);
    }
}
