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
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.PassThroughOperation;
import org.opengis.referencing.operation.Operation;

// Geotools dependencies
import org.geotools.referencing.operation.transform.PassThroughTransform;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.util.UnsupportedImplementationException;


/**
 * A pass-through operation specifies that a subset of a coordinate tuple is subject to a specific
 * coordinate operation.
 *  
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DefaultPassThroughOperation extends DefaultSingleOperation implements PassThroughOperation {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 4308173919747248695L;

    /**
     * The operation to apply on the subset of a coordinate tuple.
     */
    protected final Operation operation;

    /**
     * Constructs a single operation from a set of properties. The properties given in argument
     * follow the same rules than for the {@link AbstractCoordinateOperation} constructor.
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
    public DefaultPassThroughOperation(final Map                      properties,
                                       final CoordinateReferenceSystem sourceCRS,
                                       final CoordinateReferenceSystem targetCRS,
                                       final Operation                 operation,
                                       final int           firstAffectedOrdinate,
                                       final int            numTrailingOrdinates)
    {
//      TODO: Uncomment if Sun fix RFE #4093999
//      ensureNonNull("operation", operation);
        this(properties, sourceCRS, targetCRS, operation,
             PassThroughTransform.create(firstAffectedOrdinate,
                                         operation.getMathTransform(),
                                         numTrailingOrdinates));
    }

    /**
     * Constructs a single operation from a set of properties and the given transform.
     * The properties given in argument follow the same rules than for the
     * {@link AbstractCoordinateOperation} constructor.
     *
     * @param  properties Set of properties. Should contains at least <code>"name"</code>.
     * @param  sourceCRS The source CRS.
     * @param  targetCRS The target CRS.
     * @param  operation The operation to apply on the subset of a coordinate tuple.
     * @param  transform The {@linkplain MathTransformFactory#createPassThroughTransform
     *                   pass through transform}.
     */
    public DefaultPassThroughOperation(final Map                      properties,
                                       final CoordinateReferenceSystem sourceCRS,
                                       final CoordinateReferenceSystem targetCRS,
                                       final Operation                 operation,
                                       final MathTransform             transform)
    {
        super(properties, sourceCRS, targetCRS, transform);
        this.operation = operation;
        ensureNonNull("operation", operation);
        ensureValidDimension(operation.getSourceCRS(), transform.getSourceDimensions());
        ensureValidDimension(operation.getTargetCRS(), transform.getTargetDimensions());
    }

    /**
     * Ensure that the dimension of the specified CRS is not greater than the specified value.
     */
    private static void ensureValidDimension(final CoordinateReferenceSystem crs, final int dim) {
        if (crs.getCoordinateSystem().getDimension() > dim) {
            throw new IllegalArgumentException(); // TODO: provides a localized message.
        }
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
     *
     * @todo Current version work only with Geotools implementation.
     */
    public int[] getModifiedCoordinates() {
        if (!(transform instanceof PassThroughTransform)) {
            throw new UnsupportedImplementationException(transform.getClass());
        }
        return ((PassThroughTransform) transform).getModifiedCoordinates();
    }
    
    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name.
     */
    protected String formatWKT(final Formatter formatter) {
        final String name = super.formatWKT(formatter);
        try {
            final int[] ordinates = getModifiedCoordinates();
            for (int i=0; i<ordinates.length; i++) {
                formatter.append(ordinates[i]);
            }
        } catch (UnsupportedOperationException exception) {
            // Ignore: no indices will be formatted.
            formatter.setInvalidWKT();
        }
        formatter.append(operation);
        return name;
    }
}
