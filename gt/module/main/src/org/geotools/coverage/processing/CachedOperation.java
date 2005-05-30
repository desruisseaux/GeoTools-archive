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
 */
package org.geotools.coverage.processing;

// J2SE dependencies
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.opengis.coverage.processing.Operation;
import org.opengis.parameter.ParameterValueGroup;

// Geotools dependencies
import org.geotools.coverage.CoverageCache;
import org.geotools.resources.Utilities;
import org.geotools.parameter.Parameters;


/**
 * An {@link Operation}-{@link ParameterValueGroup} pair, used by
 * {@link GridCoverageProcessor2D#doOperation(Operation,ParameterValueGroup)}
 * for caching the result of operations. Reusing previous computation outputs
 * should be okay since grid coverage (both the sources and the result) are
 * immutable by default.
 *
 * @todo There is a tricky issue for grid coverage backed by a writable rendered
 *       image. The OpenGIS specification allows to change sample values. What
 *       should be the semantic for operation using those images as sources?
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.1
 */
final class CachedOperation {
    /**
     * The operation to apply on grid coverages.
     */
    private final Operation operation;

    /**
     * The parameters names in alphabetical order, including source grid coverages.
     */
    private final String[] names;

    /**
     * The parameters values. {@link Coverage} objects will use weak references.
     */
    private final Object[] values;

    /**
     * The hash code value for this key.
     */
    private final int hashCode;

    /**
     * Constructs a new key for the specified operation and parameters.
     *
     * @param operation  The operation to apply on grid coverages.
     * @param parameters The parameters, including source grid coverages.
     */
    public CachedOperation(final Operation operation, final ParameterValueGroup parameters) {
        this.operation  = operation;
        int   hashCode  = operation.hashCode();
        final Map param = Parameters.toNameValueMap(parameters, new TreeMap());
        this.names      = new String[param.size()];
        this.values     = new Object[names.length];
        int  index      = 0;
        for (final Iterator it=param.entrySet().iterator(); it.hasNext(); index++) {
            final Map.Entry entry = (Map.Entry) it.next();
            names[index] = ((String) entry.getKey()).toLowerCase();
            Object value = entry.getValue();
            if (value instanceof Coverage) {
                value = CoverageCache.DEFAULT.reference((Coverage) value);
            }
            if (value != null) {
                hashCode = 37*hashCode + value.hashCode();
            }
            values[index] = value;
        }
        this.hashCode = hashCode;
    }

    /**
     * Compares the specified object with this key for equality.
     */
    public boolean equals(final Object object) {
        if (object instanceof CachedOperation) {
            final CachedOperation that = (CachedOperation) object;
            return Utilities.equals(this.operation,  that.operation) &&
                      Arrays.equals(this.names,      that.names)     &&
                      Arrays.equals(this.values,     that.values);
            /*
             * NOTE: values array may contains WeakReference, in which case the 'equals'
             *       method applies to reference instead of referent (i.e. Coverage). It
             *       should work anyway, since CoverageCache should returns the same
             *       WeakReference instance for the same Coverage.
             */
        }
        return false;
    }

    /**
     * Returns a hash code value for this key.
     */
    public int hashCode() {
        return hashCode;
    }
}
