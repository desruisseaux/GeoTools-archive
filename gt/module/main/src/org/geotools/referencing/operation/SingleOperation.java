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
import org.opengis.referencing.operation.ConicProjection;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.CylindricalProjection;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.PlanarProjection;
import org.opengis.referencing.operation.Projection;
import org.opengis.referencing.operation.Transformation;


/**
 * A single (not {@linkplain ConcatenatedOperation concatenated}) coordinate operation.
 *  
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class SingleOperation extends CoordinateOperation
                          implements org.opengis.referencing.operation.SingleOperation
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 672935231344137185L;

    /**
     * Construct a single operation from a set of properties. The properties given in argument
     * follow the same rules than for the {@link CoordinateOperation} constructor.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param sourceCRS The source CRS, or <code>null</code> if not available.
     * @param targetCRS The target CRS, or <code>null</code> if not available.
     * @param transform Transform from positions in the {@linkplain #getSourceCRS source coordinate
     *                  reference system} to positions in the {@linkplain #getTargetCRS target
     *                  coordinate reference system}.
     */
    public SingleOperation(final Map                      properties,
                           final CoordinateReferenceSystem sourceCRS,
                           final CoordinateReferenceSystem targetCRS,
                           final MathTransform             transform)
    {
        super(properties, sourceCRS, targetCRS, transform);
    }

    /**
     * Returns a single operation of the specified class. This method may constructs instance of
     * {@link Conversion} or {@link Transformation} among others.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param sourceCRS The source CRS, or <code>null</code> if not available.
     * @param targetCRS The target CRS, or <code>null</code> if not available.
     * @param transform Transform from positions in the {@linkplain #getSourceCRS source coordinate
     *                  reference system} to positions in the {@linkplain #getTargetCRS target
     *                  coordinate reference system}.
     * @param method    The operation method, or <code>null</code>.
     * @param type      The minimal type as <code>{@linkplain Conversion}.class</code>,
     *                  <code>{@linkplain Projection}.class</code>, etc. This method may
     *                  create an instance of a subclass of <code>type</code>.
     */
    public static SingleOperation create(final Map                      properties,
                                         final CoordinateReferenceSystem sourceCRS,
                                         final CoordinateReferenceSystem targetCRS,
                                         final MathTransform             transform,
                                         final OperationMethod           method,
                                               Class                     type)
    {
        if (method != null) {
            if (method instanceof MathTransformProvider) {
                final Class candidate = ((MathTransformProvider) method).getOperationType();
                if (candidate != null) {
                    if (type==null || type.isAssignableFrom(candidate)) {
                        type = candidate;
                    }
                }
            }
            if (type != null) {
                if (Transformation.class.isAssignableFrom(type)) {
                    return new org.geotools.referencing.operation.Transformation(
                               properties, sourceCRS, targetCRS, transform, method);
                }
                if (ConicProjection.class.isAssignableFrom(type)) {
                    return new org.geotools.referencing.operation.ConicProjection(
                               properties, sourceCRS, targetCRS, transform, method);
                }
                if (CylindricalProjection.class.isAssignableFrom(type)) {
                    return new org.geotools.referencing.operation.CylindricalProjection(
                               properties, sourceCRS, targetCRS, transform, method);
                }
                if (PlanarProjection.class.isAssignableFrom(type)) {
                    return new org.geotools.referencing.operation.PlanarProjection(
                               properties, sourceCRS, targetCRS, transform, method);
                }
                if (Projection.class.isAssignableFrom(type)) {
                    return new org.geotools.referencing.operation.Projection(
                               properties, sourceCRS, targetCRS, transform, method);
                }
                if (Conversion.class.isAssignableFrom(type)) {
                    return new org.geotools.referencing.operation.Conversion(
                               properties, sourceCRS, targetCRS, transform, method);
                }
            }
            return new org.geotools.referencing.operation.Operation(
                       properties, sourceCRS, targetCRS, transform, method);
        }
        return new org.geotools.referencing.operation.SingleOperation(
                   properties, sourceCRS, targetCRS, transform);
    }
}
