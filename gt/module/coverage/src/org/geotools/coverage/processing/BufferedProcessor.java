/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le Développement
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
 */
package org.geotools.coverage.processing;

// J2SE dependencies
import java.util.Map;
import java.util.Collection;
import java.awt.RenderingHints;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.processing.Operation;
import org.opengis.coverage.processing.OperationNotFoundException;
import org.opengis.parameter.ParameterValueGroup;

// Geotools dependencies
import org.geotools.util.WeakValueHashMap;


/**
 * A coverage processor caching the result of any operations. Since a
 * {@linkplain GridCoverage grid coverage} may be expensive to compute and consumes a lot of
 * memory, we can save a lot of resources by returning cached instances every time the same
 * {@linkplain Operation operation} with the same {@linkplain ParameterValueGroup parameters}
 * is applied on the same coverage. Coverages are cached using
 * {@linkplain java.lang.ref.WeakReference weak references}.
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class BufferedProcessor extends AbstractProcessor {
    /**
     * The underlying processor.
     */
    protected final AbstractProcessor processor;

    /**
     * A set of {@link GridCoverage}s resulting from previous invocations to {@link #doOperation}.
     * Will be created only when first needed.
     */
    private transient Map cache;

    /**
     * Creates a buffered processor backed by a {@linkplain DefaultProcessor default processor}
     * using the specified hints. Null or empty hints are legal, but consider using the
     * {@linkplain #getInstance default instance} in such case.
     */
    public BufferedProcessor(final RenderingHints hints) {
        final DefaultProcessor processor = new DefaultProcessor(hints);
        processor.setProcessor(this);
        this.processor = processor;
    }

    /**
     * Creates a new buffered processor backed by the specified processor. If the specified
     * processor is an instance of {@link DefaultProcessor}, consider using the
     * {@linkplain #BufferedProcessor(RenderingHints) constructor expecting hints} instead,
     * for efficienty.
     */
    public BufferedProcessor(final AbstractProcessor processor) {
        ensureNonNull("processor", processor);
        this.processor = processor;
    }

    /**
     * Notifies this processor that it is going to be used as the application-wide default
     * processor.
     */
    void setAsDefault() {
        processor.setAsDefault();
    }

    /**
     * Retrieves grid processing operations information. The default implementation forward
     * the call directly to the {@linkplain #processor underlying processor}.
     */
    public Collection/*<Operation>*/ getOperations() {
        return processor.getOperations();
    }

    /**
     * Returns the operation for the specified name. The default implementation forward
     * the call directly to the {@linkplain #processor underlying processor}.
     *
     * @param  name Name of the operation.
     * @return The operation for the given name.
     * @throws OperationNotFoundException if there is no operation for the specified name.
     */
    public Operation getOperation(final String name) throws OperationNotFoundException {
        return processor.getOperation(name);
    }

    /**
     * Applies an operation. The default implementation first checks if a coverage has already
     * been created from the same parameters. If such a coverage is found, it is returned.
     * Otherwise, this method forward the call to the {@linkplain #processor underlying processor}
     * and caches the result.
     *
     * @param  parameters Parameters required for the operation.
     * @return The result as a coverage.
     * @throws OperationNotFoundException if there is no operation for the parameter group name.
     */
    public Coverage doOperation(final ParameterValueGroup parameters)
            throws OperationNotFoundException
    {
        synchronized (processor) {
            final String operationName = getOperationName(parameters);
            final Operation  operation = processor.getOperation(operationName);
            final CachedOperation cacheKey = new CachedOperation(operation, parameters);
            if (cache != null) {
                final Coverage coverage = (Coverage) cache.get(cacheKey);
                if (coverage != null) {
                    log(getPrimarySource(parameters), coverage, operationName, true);
                    return coverage;
                }
            } else {
                cache = new WeakValueHashMap();
            }
            final Coverage coverage = processor.doOperation(parameters);
            cache.put(cacheKey, coverage);
            return coverage;
        }
    }
}
