/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.coverage.processing;

// J2SE dependencies
import java.awt.RenderingHints;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// OpenGIS dependencies
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.processing.Operation;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.factory.Hints;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.util.NumberRange;
import org.geotools.resources.Utilities;



/**
 * Provides descriptive information for a grid coverage processing operation. The descriptive
 * information includes such information as the name of the operation, operation description,
 * and number of source grid coverages required for the operation.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.1
 */
public abstract class Operation2D implements Operation, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 574096338873406394L;

    /**
     * Convenience constant for the first {@linkplain GridCoverage2D grid coverage} source.
     */
    public static final ParameterDescriptor SOURCE_0;
    static {
        final Map properties = new HashMap();
        properties.put(AbstractIdentifiedObject.NAME_PROPERTY, "Source");
        properties.put(AbstractIdentifiedObject.ALIAS_PROPERTY, "source0");
        SOURCE_0 = new DefaultParameterDescriptor(properties, GridCoverage2D.class,
                        null, null, null, null, null, true);
    }

    /** Convenient constant */ static final Integer     ZERO    = new Integer(0);
    /** Convenient constant */ static final Integer     ONE     = new Integer(1);
    /** Convenient constant */ static final Integer     TWO     = new Integer(2);
    /** Convenient constant */ static final Integer     THREE   = new Integer(3);
    /** Convenient constant */ static final NumberRange RANGE_0 = new NumberRange(Integer.class, ZERO, null);
    /** Convenient constant */ static final NumberRange RANGE_1 = new NumberRange(Integer.class, ONE,  null);

    /**
     * The parameters descriptor.
     */
    protected final ParameterDescriptorGroup descriptor;

    /**
     * Constructs an operation. The operation name will be the same than the
     * parameter descriptor name.
     *
     * @param descriptor The parameters descriptor.
     */
    public Operation2D(final ParameterDescriptorGroup descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * Returns the name of the processing operation. The default implementation
     * returns the {@linkplain #descriptor} code name.
     */
    public String getName() {
        return descriptor.getName().getCode();
    }

    /**
     * Returns the description of the processing operation. If there is no description,
     * returns {@code null}. The default implementation returns the {@linkplain #descriptor}
     * remarks.
     */
    public String getDescription() {
        final InternationalString remarks = descriptor.getRemarks();
        return (remarks!=null) ? remarks.toString() : null;
    }

    /**
     * Returns the URL for documentation on the processing operation. If no online documentation
     * is available the string will be null. The default implementation returns {@code null}.
     */
    public String getDocURL() {
        return null;
    }

    /**
     * Returns the version number of the implementation.
     */
    public String getVersion() {
        return descriptor.getName().getVersion();
    }

    /**
     * Returns the vendor name of the processing operation implementation.
     * The default implementation returns "Geotools 2".
     */
    public String getVendor() {
        return "Geotools 2";
    }

    /**
     * Returns the number of source grid coverages required for the operation.
     */
    public int getNumSources() {
        return getNumSources(descriptor);
    }
    
    /**
     * Returns the number of source grid coverages in the specified parameter group.
     */
    private static int getNumSources(final ParameterDescriptorGroup descriptor) {
        int count = 0;
        for (final Iterator it=descriptor.descriptors().iterator(); it.hasNext();) {
            final GeneralParameterDescriptor candidate = (GeneralParameterDescriptor) it.next();
            if (candidate instanceof ParameterDescriptorGroup) {
                count += getNumSources((ParameterDescriptorGroup) candidate);
                continue;
            }
            if (candidate instanceof ParameterDescriptor) {
                final Class type = ((ParameterDescriptor) candidate).getValueClass();
                if (GridCoverage.class.isAssignableFrom(type)) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * Returns a default parameters information.
     */
    public ParameterValueGroup getParameters() {
        return (ParameterValueGroup) descriptor.createValue(); // TODO: remove cast with J2SE 1.5.
    }
    
    /**
     * Apply a process operation to a grid coverage. This method is invoked by
     * {@link GridCoverageProcessor2D}.
     *
     * @param  parameters List of name value pairs for the parameters
     *         required for the operation.
     * @param  hints A set of rendering hints, or {@code null} if none.
     *         The {@code GridCoverageProcessor2D} may provides hints
     *         for the following keys: {@link Hints#COORDINATE_OPERATION_FACTORY}
     *         and {@link Hints#JAI_INSTANCE}.
     * @return The result as a grid coverage.
     */
    protected abstract GridCoverage2D doOperation(final ParameterValueGroup parameters,
                                                  final RenderingHints hints);

    /**
     * Returns the {@link GridCoverageProcessor2D} instance used for an operation.
     * The instance is fetch from the rendering hints given to the {@link #doOperation} method.
     *
     * @param  hints The rendering hints, or {@code null} if none.
     * @return The {@code GridCoverageProcessor2D} instance in use (never {@code null}).
     */
    protected static GridCoverageProcessor2D getGridCoverageProcessor(final RenderingHints hints) {
        if (hints != null) {
            final Object value = hints.get(Hints.GRID_COVERAGE_PROCESSOR);
            if (value instanceof GridCoverageProcessor2D) {
                return (GridCoverageProcessor2D) value;
            }
        }
        return GridCoverageProcessor2D.getDefault();
    }

    /**
     * Returns a hash value for this operation. This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        // Since we should have only one operation registered for each name,
        // the descriptors hash code should be enough.
        return descriptor.hashCode();
    }

    /**
     * Compares the specified object with this operation for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final Operation2D that = (Operation2D) object;
            return Utilities.equals(this.descriptor, that.descriptor);
        }
        return false;
    }

    /**
     * Returns a string représentation of this operation.
     * The returned string is implementation dependent. It
     * is usually provided for debugging purposes only.
     */
    public String toString() {
        return Utilities.getShortClassName(this) + '[' + getName() + ']';
    }
}
