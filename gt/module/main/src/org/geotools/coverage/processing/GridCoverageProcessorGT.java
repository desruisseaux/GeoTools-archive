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

// Collections
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

// JAI dependencies
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.TileCache;
import javax.media.jai.util.CaselessStringKey;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;

// Geotools dependencies
import org.geotools.coverage.grid.Hints;
import org.geotools.coverage.grid.Interpolator2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.jai.HysteresisDescriptor;
import org.geotools.coverage.processing.jai.NodataFilterDescriptor;
import org.geotools.resources.Arguments;
import org.geotools.resources.Utilities;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.gcs.Resources;
import org.geotools.util.WeakValueHashMap;


/**
 * Allows for different ways of accessing the grid coverage values.
 * Using one of these operations to change the way the grid is being
 * accessed will not affect the state of the grid coverage controlled
 * by another operations. For example, changing the interpolation method
 * should not affect the number of sample dimensions currently being
 * accessed or value sequence.
 *
 * @version $Id$
 * @author <a href="www.opengis.org">OpenGIS</a>
 * @author Martin Desruisseaux
 */
public class GridCoverageProcessorGT {
    /**
     * Augment the amout of memory allocated for the tile cache.
     */
    static {
        final long targetCapacity = 0x4000000; // 64 Mo.
        final long maxMemory = Runtime.getRuntime().maxMemory();
        final TileCache cache = JAI.getDefaultInstance().getTileCache();
        if (maxMemory > 2*targetCapacity) {
            if (cache.getMemoryCapacity() < targetCapacity) {
                cache.setMemoryCapacity(targetCapacity);
            }
        }
        final Logger logger = Logger.getLogger("org.geotools.gp");
        logger.config("Java Advanced Imaging: "+JAI.getBuildVersion()+
                    ", TileCache capacity="+(float)(cache.getMemoryCapacity()/(1024*1024))+" Mb");
        /*
         * Verify that the tile cache has some reasonable value.  A lot of users seems to
         * misunderstand the memory setting in Java and set wrong values. If the user set
         * a tile cache greater than the maximum heap size, tell him that he is looking
         * for serious trouble.
         */
        if (cache.getMemoryCapacity() + (4*1024*1024) >= maxMemory) {
            logger.severe(Resources.format(ResourceKeys.WARNING_EXCESSIVE_TILE_CACHE_$1,
                                           new Double(maxMemory/(1024*1024.0))));
        }
    }
    
    /**
     * The default grid coverage processor. Will
     * be constructed only when first requested.
     */
    private static GridCoverageProcessorGT DEFAULT;
    
    /**
     * The set of operation for this grid coverage processor.
     * Keys are operation's name. Values are operations and
     * should not contains duplicated values.
     * <br><br>
     * Generic-Type: <CaselessStringKey,Operation>
     */
    private final Map operations = new HashMap();

    /**
     * The rendering hints for JAI operations (never {@code null}).
     * This field is usually given as argument to {@link OperationJAI} methods.
     */
    private final RenderingHints hints;

    /**
     * A set of {@link GridCoverage}s resulting from previous invocations to
     * {@link #doOperation(Operation,ParameterList)}. Will be used in order
     * to returns pre-computed images as much as possible.
     */
    private final Map cache = new WeakValueHashMap();
    
    /**
     * Construct a grid coverage processor with no operation and using the
     * default {@link JAI} instance. Operations can be added by invoking
     * the {@link #addOperation} method at construction time.
     *
     * Rendering hints will be initialized with the following hints:
     * <ul>
     *   <li>{@link JAI#KEY_REPLACE_INDEX_COLOR_MODEL} set to {@link Boolean#FALSE}.</li>
     *   <li>{@link JAI#KEY_TRANSFORM_ON_COLORMAP} set to {@link Boolean#FALSE}.</li>
     * </ul>
     */
    protected GridCoverageProcessorGT() {
        hints = new RenderingHints(Hints.GRID_COVERAGE_PROCESSOR, this);
        hints.put(JAI.KEY_REPLACE_INDEX_COLOR_MODEL, Boolean.FALSE);
        hints.put(JAI.KEY_TRANSFORM_ON_COLORMAP,     Boolean.FALSE);
    }

    /**
     * Construct a grid coverage processor initialized with the same set of operations than the
     * specified processor. The rendering hints are initialized to the union of the rendering
     * hints of the specified processor, and the specified rendering hints. More operations can
     * be added by invoking the {@link #addOperation} method at construction time.
     *
     * @param processor The processor to inherit from, or {@code null} if none.
     * @param hints A set of supplemental rendering hints, or {@code null} if none.
     */
    public GridCoverageProcessorGT(final GridCoverageProcessorGT processor,
                                   final RenderingHints          hints)
    {
        this();
        if (processor != null) {
            operations.putAll(processor.operations);
            this.hints.add(processor.hints);
        }
        if (hints != null) {
            this.hints.add(hints);
        }
        this.hints.put(Hints.GRID_COVERAGE_PROCESSOR, this);
    }

    /**
     * Returns the default grid coverage processor.
     */
    public static synchronized GridCoverageProcessorGT getDefault() {
//        if (DEFAULT==null) {
//            DEFAULT = new GridCoverageProcessorGT();
//            //
//            // OpenGIS operations
//            //
//            DEFAULT.addOperation(new Resampler.Operation());
//            DEFAULT.addOperation(new Interpolator2D.Operation());
//            DEFAULT.addOperation(new SelectSampleDimension.Operation());
//            DEFAULT.addOperation(new MaskFilterOperation("MinFilter"));
//            DEFAULT.addOperation(new MaskFilterOperation("MaxFilter"));
//            DEFAULT.addOperation(new MaskFilterOperation("MedianFilter"));
//            DEFAULT.addOperation(new ConvolveOperation("LaplaceType1Filter", ConvolveOperation.LAPLACE_TYPE_1));
//            DEFAULT.addOperation(new ConvolveOperation("LaplaceType2Filter", ConvolveOperation.LAPLACE_TYPE_2));
//            DEFAULT.addOperation(new BilevelOperation("Threshold", "Binarize"));
//            //
//            // JAI operations
//            //
//            DEFAULT.addOperation(new ConvolveOperation());
//            DEFAULT.addOperation(new OperationJAI("Absolute"));
//            DEFAULT.addOperation(new OperationJAI("Add"));
//            DEFAULT.addOperation(new OperationJAI("AddConst"));
//            DEFAULT.addOperation(new OperationJAI("Divide"));
//            DEFAULT.addOperation(new OperationJAI("DivideByConst"));
//            DEFAULT.addOperation(new OperationJAI("Exp"));
//            DEFAULT.addOperation(new OperationJAI("Invert"));
//            DEFAULT.addOperation(new OperationJAI("Multiply"));
//            DEFAULT.addOperation(new OperationJAI("MultiplyConst"));
//            DEFAULT.addOperation(new OperationJAI("Subtract"));
//            DEFAULT.addOperation(new OperationJAI("SubtractConst"));
//            DEFAULT.addOperation(new OperationJAI("Rescale"));
//            //
//            // Custom (Geotools) operations
//            //
//            DEFAULT.addOperation(new RecolorOperation());
//            DEFAULT.addOperation(new GradualColormapOperation());
//            DEFAULT.addOperation(new GradientMagnitudeOperation()); // Backed by JAI
//            try {
//                DEFAULT.addOperation(new FilterOperation(HysteresisDescriptor.OPERATION_NAME));
//                DEFAULT.addOperation(new FilterOperation(NodataFilterDescriptor.OPERATION_NAME));
//                DEFAULT.addOperation(new CombineOperation());
//            } catch (OperationNotFoundException exception) {
//                /*
//                 * "Hysteresis", "NodataFilter" and "Combine" operations should have been declared
//                 * into META-INF/registryFile.jai.   If we reach this point, it means that JAI has
//                 * not found this file or failed to initialize the factory classes.   This failure
//                 * will not prevent GridCoverage to work in most case,  since those operations are
//                 * used only for some computation purpose  and  will never be required if the user
//                 * doesn't ask explicitly for them.
//                 */
//                Logger.getLogger("org.geotools.gp").warning(exception.getLocalizedMessage());
//            }
//            /*
//             * Remove the GRID_COVERAGE_PROCESSOR hints It will avoid its serialization and a strong
//             * reference in RenderedImage's properties for the common case where we are using the
//             * default instance. The method Operation.getGridCoverageProcessor will automatically
//             * maps the null value to the default instance anyway.
//             */
//            DEFAULT.hints.remove(Hints.GRID_COVERAGE_PROCESSOR);
//        }
        return DEFAULT;
    }

    /**
     * Returns a rendering hint.
     *
     * @param  key The hint key (e.g. {@link Hints#JAI_INSTANCE}).
     * @return The hint value for the specified key, or null if
     *         there is no hint for the specified key.
     */
    public final Object getRenderingHint(final RenderingHints.Key key) {
        return (hints!=null) ? hints.get(key) : null;
    }
    
    /**
     * Add the specified operation to this processor. This method is usually invoked
     * at construction time <strong>before</strong> this processor is made accessible.
     * Once accessible, all <code>GridCoverageProcessorGT</code> instances should be
     * immutable.
     *
     * @param  operation The operation to add.
     * @throws IllegalStateException if an operation already exists
     *         with the same name than <code>operation</code>.
     */
//    protected synchronized void addOperation(final Operation operation) throws IllegalStateException {
//        final CaselessStringKey name = new CaselessStringKey(operation.getName());
//        if (!operations.containsKey(name)) {
//            assert !operations.containsValue(operation);
//            operations.put(name, operation);
//        }
//        else throw new IllegalStateException(Resources.format(ResourceKeys.ERROR_OPERATION_ALREADY_BOUND_$1, operation.getName()));
//    }
    
    /**
     * Retrieve grid processing operation informations. The operation information
     * will contain the name of the operation as well as a list of its parameters.
     */
//    public synchronized Operation[] getOperations() {
//        return (Operation[]) operations.values().toArray(new Operation[operations.size()]);
//    }
    
    /**
     * Returns the operation for the specified name.
     *
     * @param  name Name of the operation.
     * @return The operation for the given name.
     * @throws OperationNotFoundException if there is no operation for the specified name.
     */
//    public Operation getOperation(final String name) throws OperationNotFoundException {
//        final Operation operation = (Operation) operations.get(new CaselessStringKey(name));
//        if (operation!=null) {
//            return operation;
//        }
//        throw new OperationNotFoundException(Resources.format(
//                ResourceKeys.ERROR_OPERATION_NOT_FOUND_$1, name));
//    }
    
    /**
     * Convenience method applying a process operation with default parameters.
     *
     * @param  operationName Name of the operation to be applied to the grid coverage..
     * @param  source The source grid coverage.
     * @return The result as a grid coverage.
     * @throws OperationNotFoundException if there is no operation named <code>operationName</code>.
     *
     * @see #doOperation(Operation,ParameterList)
     */
//    public GridCoverage doOperation(final String operationName, final GridCoverage source)
//        throws OperationNotFoundException
//    {
//        final Operation operation = getOperation(operationName);
//        return doOperation(operation, operation.getParameterList()
//                .setParameter("Source", source));
//    }
    
    /**
     * Convenience method applying a process operation with one parameter.
     *
     * @param  operationName  Name of the operation to be applied to the grid coverage..
     * @param  source         The source grid coverage.
     * @param  argumentName1  The name of the first parameter to set.
     * @param  argumentValue1 The value for the first parameter.
     * @return The result as a grid coverage.
     * @throws OperationNotFoundException if there is no operation named <code>operationName</code>.
     * @throws IllegalArgumentException if there is no parameter with the specified name.
     *
     * @see #doOperation(Operation,ParameterList)
     */
//    public GridCoverage doOperation(final String operationName, final GridCoverage source,
//                                    final String argumentName1, final Object argumentValue1)
//        throws OperationNotFoundException, IllegalArgumentException
//    {
//        final Operation operation = getOperation(operationName);
//        return doOperation(operation, operation.getParameterList()
//                .setParameter("Source", source)
//                .setParameter(argumentName1, argumentValue1));
//    }
    
    /**
     * Convenience method applying a process operation with two parameters.
     *
     * @param  operationName  Name of the operation to be applied to the grid coverage..
     * @param  source         The source grid coverage.
     * @param  argumentName1  The name of the first parameter to set.
     * @param  argumentValue1 The value for the first parameter.
     * @param  argumentName2  The name of the second parameter to set.
     * @param  argumentValue2 The value for the second parameter.
     * @return The result as a grid coverage.
     * @throws OperationNotFoundException if there is no operation named <code>operationName</code>.
     * @throws IllegalArgumentException if there is no parameter with the specified name.
     *
     * @see #doOperation(Operation,ParameterList)
     */
//    public GridCoverage doOperation(final String operationName, final GridCoverage source,
//                                    final String argumentName1, final Object argumentValue1,
//                                    final String argumentName2, final Object argumentValue2)
//    throws OperationNotFoundException, IllegalArgumentException
//    {
//        final Operation operation = getOperation(operationName);
//        return doOperation(operation, operation.getParameterList()
//                .setParameter("Source", source)
//                .setParameter(argumentName1, argumentValue1)
//                .setParameter(argumentName2, argumentValue2));
//    }
    
    /**
     * Convenience method applying a process operation with three parameters.
     *
     * @param  operationName  Name of the operation to be applied to the grid coverage..
     * @param  source         The source grid coverage.
     * @param  argumentName1  The name of the first parameter to set.
     * @param  argumentValue1 The value for the first parameter.
     * @param  argumentName2  The name of the second parameter to set.
     * @param  argumentValue2 The value for the second parameter.
     * @param  argumentName3  The name of the third parameter to set.
     * @param  argumentValue3 The value for the third parameter.
     * @return The result as a grid coverage.
     * @throws OperationNotFoundException if there is no operation named <code>operationName</code>.
     * @throws IllegalArgumentException if there is no parameter with the specified name.
     *
     * @see #doOperation(Operation,ParameterList)
     */
//    public GridCoverage doOperation(final String operationName, final GridCoverage source,
//                                    final String argumentName1, final Object argumentValue1,
//                                    final String argumentName2, final Object argumentValue2,
//                                    final String argumentName3, final Object argumentValue3)
//    throws OperationNotFoundException, IllegalArgumentException
//    {
//        final Operation operation = getOperation(operationName);
//        return doOperation(operation, operation.getParameterList()
//                .setParameter("Source", source)
//                .setParameter(argumentName1, argumentValue1)
//                .setParameter(argumentName2, argumentValue2)
//                .setParameter(argumentName3, argumentValue3));
//    }
    
    /**
     * Apply a process operation to a grid coverage.
     *
     * @param  operationName Name of the operation to be applied to the grid coverage.
     * @param  parameters List of name value pairs for the parameters required for the operation.
     *         The easiest way to construct this list is to invoke <code>{@link #getOperation
     *         getOperation}(name).{@link Operation#getParameterList getParameterList}()</code>
     *         and to modify the returned list.
     * @return The result as a grid coverage.
     * @throws OperationNotFoundException if there is no operation named <code>operationName</code>.
     */
//    public synchronized GridCoverage doOperation(final String     operationName,
//                                                 final ParameterList parameters)
//            throws OperationNotFoundException
//    {
//        return doOperation(getOperation(operationName), parameters);
//    }
    
    /**
     * Apply a process operation to a grid coverage. Default implementation
     * checks if source coverages use an interpolation,    and then invokes
     * {@link Operation#doOperation}. If all source coverages used the same
     * interpolation, the same interpolation is applied to the resulting
     * coverage (except if the resulting coverage has already an interpolation).
     *
     * @param  operation The operation to be applied to the grid coverage.
     * @param  parameters List of name value pairs for the parameters required for
     *         the operation.  The easiest way to construct this list is to invoke
     *         <code>operation.{@link Operation#getParameterList getParameterList}()</code>
     *         and to modify the returned list.
     * @return The result as a grid coverage.
     */
//    public synchronized GridCoverage doOperation(final Operation     operation,
//                                                 final ParameterList parameters)
//    {
//        GridCoverage source;
//        try {
//            source = (GridCoverage) parameters.getObjectParameter("Source");
//        } catch (RuntimeException exception) {
//            // "Source" parameter may not exists. Conservatively
//            // assume that the operation will do some usefull work.
//            source = null;
//        }
//        /*
//         * Check if the result for this operation is already available in the cache.
//         */
//        final String operationName = operation.getName();
//        final CacheKey cacheKey = new CacheKey(operation, parameters);
//        GridCoverage coverage = (GridCoverage) cache.get(cacheKey);
//        if (coverage != null) {
//            log(source, coverage, operationName, true);
//            return coverage;
//        }
//        /*
//         * Detects the interpolation type for the source grid coverage.
//         * The same interpolation will be applied on the result.
//         */
//        Interpolation[] interpolations = null;
//        if (!operationName.equalsIgnoreCase("Interpolate")) {
//            final String[] paramNames = parameters.getParameterListDescriptor().getParamNames();
//            for (int i=0; i<paramNames.length; i++) {
//                final Object param = parameters.getObjectParameter(paramNames[i]);
//                if (param instanceof Interpolator2D) {
//                    // If all sources use the same interpolation,  preserve the
//                    // interpolation for the resulting coverage. Otherwise, use
//                    // the default interpolation (nearest neighbor).
//                    final Interpolation[] interp = ((Interpolator2D) param).getInterpolations();
//                    if (interpolations == null) {
//                        interpolations = interp;
//                    } else if (!Arrays.equals(interpolations, interp)) {
//                        // Set to no interpolation.
//                        interpolations = null;
//                        break;
//                    }
//                }
//            }
//        }
//        /*
//         * Apply the operation, apply the same interpolation and log a message.
//         */
//        coverage = operation.doOperation(parameters, hints);
//        if (interpolations!=null && coverage!=null && !(coverage instanceof Interpolator2D)) {
//            coverage = Interpolator2D.create(coverage, interpolations);
//        }
//        if (coverage != source) {
//            log(source, coverage, operationName, false);
//            cache.put(cacheKey, coverage);
//        }
//        return coverage;
//    }

    /**
     * Log a message for an operation. The message will be logged only if the source grid
     * coverage is different from the result (i.e. if the operation did some work).
     *
     * @param source The source grid coverage.
     * @param result The resulting grid coverage.
     * @param operationName the operation name.
     * @param fromCache <code>true</code> if the result has been fetch from the cache.
     */
    private static void log(final GridCoverage2D source,
                            final GridCoverage2D result,
                            final String  operationName,
                            final boolean     fromCache)
    {
        if (source != result) {
            String interp = "Nearest";
            if (result instanceof Interpolator2D) {
                interp = ((Interpolator2D)result).getInterpolationName();
            }
            final Locale locale = null; // Set locale here (if any).
            final LogRecord record = Resources.getResources(locale).getLogRecord(
                                     Level.FINE, ResourceKeys.APPLIED_OPERATION_$4,
                                     ((source!=null) ? source : result).getName().toString(locale),
                                     operationName, interp, new Integer(fromCache ? 1:0));
            record.setSourceClassName("GridCoverageProcessorGT");
            record.setSourceMethodName("doOperation");
            Logger.getLogger("org.geotools.coverage").log(record);
        }
    }

    /**
     * A {@link Operation}-{@link ParameterList} pair, used by
     * {@link #doOperation(Operation,ParameterList} for caching the result of operations.
     * Reusing previous computation outputs should be okay since grid coverage (both the
     * sources and the result) are immutable by default.
     *
     * @task REVISIT: There is a trick issue for grid coverage backed by a writable rendered
     *                image. The OpenGIS specification allows to change sample values.  What
     *                should be the semantic for operation using those images as sources?
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
//    private static final class CacheKey {
//        /** The operation to apply on grid coverages. */
//        private final Operation operation;
//
//        /** The parameters names, including source grid coverages. */
//        private final String[] names;
//
//        /** The parameters values. {@link Coverage} objects will use weak references. */
//        private final Object[] values;
//
//        /** The hash code value for this key. */
//        private final int hashCode;
//
//        /**
//         * Construct a new key for the specified operation and parameters.
//         *
//         * @param operation  The operation to apply on grid coverages.
//         * @param parameters The parameters, including source grid coverages.
//         */
//        public CacheKey(final Operation operation, final ParameterList parameters) {
//            this.operation = operation;
//            int hashCode = operation.hashCode();
//            names = parameters.getParameterListDescriptor().getParamNames();
//            if (names != null) {
//                values = new Object[names.length];
//                for (int i=0; i<names.length; i++) {
//                    Object value;
//                    try {
//                        value = parameters.getObjectParameter(names[i]);
//                        if (value instanceof Coverage) {
//                            value = new Ref(value);
//                        }
//                        if (value != null) {
//                            hashCode = 37*hashCode + value.hashCode();
//                        }
//                    } catch (IllegalStateException exception) {
//                        // Parameter not set. This is not really an error.
//                        value = ParameterListDescriptor.NO_PARAMETER_DEFAULT;
//                    }
//                    values[i] = value;
//                }
//            } else {
//                values = null;
//            }
//            this.hashCode = hashCode;
//        }
//
//        /**
//         * Returns a hash code value for this key.
//         */
//        public int hashCode() {
//            return hashCode;
//        }
//
//        /**
//         * A weak reference for coverages referenced in {@link CacheKey} objects. This
//         * reference overrides the {@link #equals} method  in order to make it possible for
//         * {@link Arrays#equals(Object[],Object[])} to compare referenced values instead of
//         * the reference itself. The cache will not work without this feature...  Note that
//         * the {@link #equals} method is inconsistent with {@link #hashCode}. It should not
//         * be a problem since this reference is used in an array only.   It should never be
//         * visible outside {@link CacheKey}.
//         */
//        private static final class Ref extends WeakReference {
//            /**
//             * Constructs a new reference for the specified object.
//             */
//            public Ref(final Object coverage) {
//                super(coverage);
//            }
//
//            /**
//             * Compares the specified object with this reference for equality.
//             */
//            public boolean equals(final Object object) {
//                if (object instanceof Ref) {
//                    return Utilities.equals(get(), ((Ref)object).get());
//                }
//                return false;
//            }
//        }
//    
//        /**
//         * Compares the specified object with this key for equality.
//         */
//        public boolean equals(final Object object) {
//            if (object instanceof CacheKey) {
//                final CacheKey that = (CacheKey) object;
//                return Utilities.equals(this.operation,  that.operation) &&
//                          Arrays.equals(this.names,      that.names)     &&
//                          Arrays.equals(this.values,     that.values);
//            }
//            return false;
//        }
//    }
    
    /**
     * Prints a description of all operations to the specified stream.
     * The description include operation names and lists of parameters.
     *
     * @param  out The destination stream.
     * @throws IOException if an error occured will writing to the stream.
     */
//    public void print(final Writer out) throws IOException {
//        final String  lineSeparator = System.getProperty("line.separator", "\n");
//        final Operation[] operations = getOperations();
//        Arrays.sort(operations, new Comparator() {
//            public int compare(final Object obj1, final Object obj2) {
//                return ((Operation)obj1).getName().compareTo(((Operation)obj2).getName());
//            }
//        });
//        for (int i=0; i<operations.length; i++) {
//            out.write(lineSeparator);
//            operations[i].print(out, null);
//        }
//    }

    /**
     * Dumps to the standard output stream a list of operations for the default
     * {@link GridCoverageProcessorGT}. This method can been invoked from the
     * command line. For example:
     *
     * <blockquote><pre>
     * java org.geotools.gc.GridCoverageProcessorGT
     * </pre></blockquote>
     *
     * <strong>Note for Windows users:</strong> If the output contains strange
     * symbols, try to supply an "<code>-encoding</code>" argument. Example:
     *
     * <blockquote><pre>
     * java org.geotools.gc.GridCoverageProcessorGT -encoding Cp850
     * </pre></blockquote>
     *
     * The codepage number (850 in the previous example) can be obtained from the DOS
     * commande line by entering the "<code>chcp</code>" command with no arguments.
     */
//    public static void main(final String[] args) {
//        final Arguments arguments = new Arguments(args);
//        arguments.getRemainingArguments(0);
//        try {
//            getDefault().print(arguments.out);
//        } catch (IOException exception) {
//            // Should not occurs
//            exception.printStackTrace(arguments.out);
//        }
//        arguments.out.close();
//    }
}
