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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.operation;

// J2SE dependencies
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// OpenGIS dependencies
import org.opengis.metadata.quality.PositionalAccuracy;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.ConcatenatedOperation;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.SingleOperation;

// Geotools dependencies
import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.referencing.operation.transform.ConcatenatedTransform;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;


/**
 * An ordered sequence of two or more single coordinate operations. The sequence of operations is
 * constrained by the requirement that the source coordinate reference system of step
 * (<var>n</var>+1) must be the same as the target coordinate reference system of step
 * (<var>n</var>). The source coordinate reference system of the first step and the target
 * coordinate reference system of the last step are the source and target coordinate reference
 * system associated with the concatenated operation.
 *  
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.1
 */
public class DefaultConcatenatedOperation extends AbstractCoordinateOperation
                                       implements ConcatenatedOperation
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 4199619838029045700L;

    /**
     * The sequence of operations.
     */
    private final List/*<SingleOperation>*/ operations;

    /**
     * Constructs a concatenated operation from the specified name.
     *
     * @param name The operation name.
     * @param operations The sequence of operations.
     */
    public DefaultConcatenatedOperation(final String name,
                                        final CoordinateOperation[] operations)
    {
        this(Collections.singletonMap(NAME_PROPERTY, name), operations);
    }

    /**
     * Constructs a concatenated operation from a set of properties.
     * The properties given in argument follow the same rules than for the
     * {@link AbstractCoordinateOperation} constructor.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param operations The sequence of operations.
     */
    public DefaultConcatenatedOperation(final Map properties,
                                        final CoordinateOperation[] operations)
    {
        this(properties, new ArrayList(operations!=null ? operations.length : 4), operations);
    }

    /**
     * Constructs a concatenated operation from a set of properties and a
     * {@linkplain MathTransformFactory math transform factory}.
     * The properties given in argument follow the same rules than for the
     * {@link AbstractCoordinateOperation} constructor.
     *
     * @param  properties Set of properties. Should contains at least <code>"name"</code>.
     * @param  operations The sequence of operations.
     * @param  factory    The math transform factory to use for math transforms concatenation.
     * @throws FactoryException if the factory can't concatenate the math transforms.
     */
    public DefaultConcatenatedOperation(final Map properties,
                                        final CoordinateOperation[] operations,
                                        final MathTransformFactory factory)
            throws FactoryException
    {
        this(properties, new ArrayList(operations!=null ? operations.length : 4),
             operations, factory);
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private DefaultConcatenatedOperation(final Map  properties,
                                         final ArrayList list,
                                         final CoordinateOperation[] operations)
    {
        this(properties, expand(operations, list), list);
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private DefaultConcatenatedOperation(final Map  properties,
                                         final ArrayList list,
                                         final CoordinateOperation[] operations,
                                         final MathTransformFactory factory)
            throws FactoryException
    {
        this(properties, expand(operations, list, factory, true), list);
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private DefaultConcatenatedOperation(final Map  properties,
                                         final MathTransform transform,
                                         final ArrayList/*<SingleOperation>*/ operations)
    {
        super(mergeAccuracy(properties, operations),
              ((SingleOperation) operations.get(0)).getSourceCRS(),
              ((SingleOperation) operations.get(operations.size()-1)).getTargetCRS(),
              transform);
        operations.trimToSize();
        this.operations = Collections.unmodifiableList(operations); // No need to clone.
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static MathTransform expand(final CoordinateOperation[] operations,
                                        final List list)
    {
        try {
            return expand(operations, list, null, true);
        } catch (FactoryException exception) {
            // Should not happen, since we didn't used any MathTransformFactory.
            throw new AssertionError(exception);
        }
    }

    /**
     * Transforms the list of operations into a list of single operations. This method
     * also check against null value and make sure that all CRS dimension matches.
     *
     * @param  operations The array of operations to expand.
     * @param  list The list in which to add {@code SingleOperation}.
     * @param  factory The math transform factory to use, or {@code null}
     * @param  wantTransform {@code true} if the concatenated math transform should be computed.
     * @return The concatenated math transform.
     * @throws FactoryException if the factory can't concatenate the math transforms.
     */
    private static MathTransform expand(final CoordinateOperation[] operations,
                                        final List list,
                                        final MathTransformFactory factory,
                                        final boolean wantTransform)
            throws FactoryException
    {
        MathTransform transform = null;
        ensureNonNull("operations", operations);
        for (int i=0; i<operations.length; i++) {
            ensureNonNull("operations", operations, i);
            final CoordinateOperation op = operations[i];
            if (op instanceof SingleOperation) {
                list.add(op);
            } else if (op instanceof ConcatenatedOperation) {
                final ConcatenatedOperation cop = (ConcatenatedOperation) op;
                final List cops = cop.getOperations();
                expand((CoordinateOperation[]) cops.toArray(new CoordinateOperation[cops.size()]),
                       list, factory, false);
            } else {
                throw new IllegalArgumentException(Resources.format(
                                                   ResourceKeys.ERROR_ILLEGAL_CLASS_$2,
                                                   Utilities.getShortClassName(op),
                                                   Utilities.getShortName(SingleOperation.class)));
            }
            /*
             * Check the CRS dimensions.
             */
            if (i != 0) {
                final CoordinateReferenceSystem previous = operations[i-1].getTargetCRS();
                final CoordinateReferenceSystem next     = op             .getSourceCRS();
                if (previous!=null && next!=null) {
                    final int dim1 = previous.getCoordinateSystem().getDimension();
                    final int dim2 =     next.getCoordinateSystem().getDimension();
                    if (dim1 != dim2) {
                        throw new IllegalArgumentException(Resources.format(
                                  ResourceKeys.ERROR_MISMATCHED_DIMENSION_$2,
                                  new Integer(dim1), new Integer(dim2)));
                    }
                }
            }
            /*
             * Concatenates the math transform.
             */
            if (wantTransform) {
                final MathTransform step = op.getMathTransform();
                if (transform == null) {
                    transform = step;
                } else if (factory != null) {
                    transform = factory.createConcatenatedTransform(transform, step);
                } else {
                    transform = ConcatenatedTransform.create(transform, step);
                }
            }
        }
        if (wantTransform) {
            final int size = list.size();
            if (size <= 1) {
                throw new IllegalArgumentException(Resources.format(
                            ResourceKeys.ERROR_MISSING_PARAMETER_$1, "operations["+size+']'));
            }
        }
        return transform;
    }

    /**
     * If no accuracy were specified in the given properties map, add all accuracy founds in
     * the operation to concatenate. This method is a work around for RFE #4093999 in Sun's bug
     * database ("Relax constraint on placement of this()/super() call in constructors").
     *
     * @todo We should use a Map and merge only one accuracy for each specification.
     */
    private static Map mergeAccuracy(final Map properties,
                                     final List/*<CoordinateOperation>*/ operations)
    {
        if (!properties.containsKey(POSITIONAL_ACCURACY_PROPERTY)) {
            Set accuracy = null;
            for (final Iterator it=operations.iterator(); it.hasNext();) {
                final Collection/*<PositionalAccuracy>*/ candidates =
                        ((CoordinateOperation)it.next()).getPositionalAccuracy();
                if (candidates != null) {
                    if (accuracy == null) {
                        accuracy = new LinkedHashSet();
                    }
                    accuracy.addAll(candidates);
                }
            }
            if (accuracy != null) {
                final Map merged = new HashMap(properties);
                merged.put(POSITIONAL_ACCURACY_PROPERTY,
                           accuracy.toArray(new PositionalAccuracy[accuracy.size()]));
                return merged;
            }
        }
        return properties;
    }

    /**
     * Returns the sequence of operations.
     */
    public List/*<SingleOperation>*/ getOperations() {
        return operations;
    }

    /**
     * Compare this concatenated operation with the specified object for equality.
     * If {@code compareMetadata} is {@code true}, then all available properties are
     * compared including {@linkplain #getValidArea valid area} and {@linkplain #getScope scope}.
     *
     * @param  object The object to compare to {@code this}.
     * @param  compareMetadata {@code true} for performing a strict comparaison, or
     *         {@code false} for comparing only properties relevant to transformations.
     * @return {@code true} if both objects are equal.
     */
    public boolean equals(final AbstractIdentifiedObject object, final boolean compareMetadata) {
        if (object == this) {
            return true; // Slight optimization.
        }
        if (super.equals(object, compareMetadata)) {
            final DefaultConcatenatedOperation that = (DefaultConcatenatedOperation) object;
            return equals(this.operations, that.operations, compareMetadata);
        }
        return false;
    }

    /**
     * Returns a hash code value for this concatenated operation.
     */
    public int hashCode() {
        return operations.hashCode() ^ (int)serialVersionUID;
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
        for (final Iterator it=operations.iterator(); it.hasNext();) {
            formatter.append((SingleOperation) it.next());
        }
        formatter.setInvalidWKT();
        return Utilities.getShortClassName(this);
    }
}
