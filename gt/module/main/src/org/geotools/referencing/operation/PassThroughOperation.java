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

import org.geotools.referencing.operation.transform.PassThroughTransform;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Operation;


/**
 * A pass-through operation specifies that a subset of a coordinate tuple is subject to a specific
 * coordinate operation.
 *  
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class PassThroughOperation extends SingleOperation
                               implements org.opengis.referencing.operation.PassThroughOperation
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 4308173919747248695L;

    /**
     * The operation to apply on the subset of a coordinate tuple.
     */
    protected final Operation operation;

    /**
     * Construct a single operation from a set of properties. The properties given in argument
     * follow the same rules than for the {@link CoordinateOperation} constructor.
     * Affected ordinates will range from <code>firstAffectedOrdinate</code>
     * inclusive to <code>dimTarget-numTrailingOrdinates</code> exclusive.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param sourceCRS The source CRS.
     * @param targetCRS The target CRS.
     * @param operation The operation to apply on the subset of a coordinate tuple.
     * @param firstAffectedOrdinate Index of the first affected ordinate.
     * @param numTrailingOrdinates Number of trailing ordinates to pass through.
     */
    public PassThroughOperation(final Map                      properties,
                                final CoordinateReferenceSystem sourceCRS,
                                final CoordinateReferenceSystem targetCRS,
                                final Operation                 operation,
                                final int           firstAffectedOrdinate,
                                final int            numTrailingOrdinates)
    {
        super(properties, sourceCRS, targetCRS,
              createTransform(operation, firstAffectedOrdinate, numTrailingOrdinates));
        this.operation = operation;
    }

    /**
     * Create the math transform to use for this operation.
     * This is a work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static MathTransform createTransform(final Operation       operation,
                                                 final int firstAffectedOrdinate,
                                                 final int  numTrailingOrdinates)
    {
        ensureNonNull("operation", operation);
        return PassThroughTransform.create(firstAffectedOrdinate,
                                           operation.getMathTransform(),
                                           numTrailingOrdinates);
    }

    /**
     * Returns the operation to apply on the subset of a coordinate tuple.
     *
     * @return The operation.
     */
    public Operation getOperation() {
        return operation;
    }

    /**
     * Ordered sequence of positive integers defining the positions in a coordinate
     * tuple of the coordinates affected by this pass-through operation. The returned
     * index are for source coordinates.
     *
     * @return The modified coordinates.
     */
    public int[] getModifiedCoordinates() {
        return ((PassThroughTransform) transform).getModifiedCoordinates();
    }
}
