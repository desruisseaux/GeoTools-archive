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
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.logging.LogRecord;

// JAI dependencies
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.TileCache;

// OpenGIS dependencies
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.processing.Operation;
import org.opengis.coverage.processing.OperationNotFoundException;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterNotFoundException;

// Geotools dependencies
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.operation.Interpolator2D;
import org.geotools.factory.FactoryRegistry;
import org.geotools.factory.Hints;
import org.geotools.resources.Arguments;
import org.geotools.resources.image.ImageUtilities;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.gcs.Resources;
import org.geotools.util.WeakValueHashMap;


/**
 * Processor for {@link GridCoverage2D} objects.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.1
 */
public class GridCoverageProcessor2D extends AbstractGridCoverageProcessor {
    /**
     * Augments the amout of memory allocated for the tile cache.
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
        LOGGER.config("Java Advanced Imaging: "+JAI.getBuildVersion()+
                    ", TileCache capacity="+(float)(cache.getMemoryCapacity()/(1024*1024))+" Mb");
        /*
         * Verify that the tile cache has some reasonable value. A lot of users seem to
         * misunderstand the memory setting in Java and set wrong values. If the user set
         * a tile cache greater than the maximum heap size, tell him that he is looking
         * for serious trouble.
         */
        if (cache.getMemoryCapacity() + (4*1024*1024) >= maxMemory) {
            LOGGER.severe(Resources.format(ResourceKeys.WARNING_EXCESSIVE_TILE_CACHE_$1,
                                           new Double(maxMemory/(1024*1024.0))));
        }
    }
    
    /**
     * The default grid coverage processor. Will be constructed only when first requested.
     */
    private static GridCoverageProcessor2D DEFAULT;
    
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
    private final transient Map cache = new WeakValueHashMap();

    /**
     * The service registry for finding {@link Operation2D} implementations.
     */
    private final FactoryRegistry registry;
    
    /**
     * Constructs a grid coverage processor with no operation and using the
     * default {@link JAI} instance. Operations can be added by invoking
     * the {@link #addOperation} method at construction time.
     *
     * Rendering hints will be initialized with the following hints:
     * <ul>
     *   <li>{@link JAI#KEY_REPLACE_INDEX_COLOR_MODEL} set to {@link Boolean#FALSE}.</li>
     *   <li>{@link JAI#KEY_TRANSFORM_ON_COLORMAP} set to {@link Boolean#FALSE}.</li>
     * </ul>
     */
    protected GridCoverageProcessor2D() {
        super(null, null);
        registry = new FactoryRegistry(Collections.singleton(Operation2D.class));
        hints = new RenderingHints(Hints.GRID_COVERAGE_PROCESSOR, this);
        hints.put(JAI.KEY_REPLACE_INDEX_COLOR_MODEL, Boolean.FALSE);
        hints.put(JAI.KEY_TRANSFORM_ON_COLORMAP,     Boolean.FALSE);
        scanForPlugins(); // TODO: Should not be invoked in constructor.
    }

    /**
     * Constructs a grid coverage processor initialized with the same set of operations than the
     * specified processor. The rendering hints are initialized to the union of the rendering
     * hints of the specified processor, and the specified rendering hints. More operations can
     * be added by invoking the {@link #addOperation} method at construction time.
     *
     * @param processor The processor to inherit from, or {@code null} if none.
     * @param hints A set of supplemental rendering hints, or {@code null} if none.
     */
    public GridCoverageProcessor2D(final GridCoverageProcessor2D processor,
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
    public static synchronized GridCoverageProcessor2D getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new GridCoverageProcessor2D();
            //
            // OpenGIS operations
            //
//            DEFAULT.addOperation(new SelectSampleDimension.Operation());
//            DEFAULT.addOperation(new MaskFilterOperation("MinFilter"));
//            DEFAULT.addOperation(new MaskFilterOperation("MaxFilter"));
//            DEFAULT.addOperation(new MaskFilterOperation("MedianFilter"));
//            DEFAULT.addOperation(new ConvolveOperation("LaplaceType1Filter", ConvolveOperation.LAPLACE_TYPE_1));
//            DEFAULT.addOperation(new ConvolveOperation("LaplaceType2Filter", ConvolveOperation.LAPLACE_TYPE_2));
//            DEFAULT.addOperation(new BilevelOperation("Threshold", "Binarize"));
            //
            // JAI operations
            //
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
//                LOGGER.getLogger("org.geotools.coverage.grid").warning(exception.getLocalizedMessage());
//            }
            /*
             * Remove the GRID_COVERAGE_PROCESSOR hint. It will avoid its serialization and a strong
             * reference in RenderedImage's properties for the common case where we are using the
             * default instance. The method Operation.getGridCoverageProcessor will automatically
             * maps the null value to the default instance anyway.
             */
            DEFAULT.hints.remove(Hints.GRID_COVERAGE_PROCESSOR);
        }
        return DEFAULT;
    }

    /**
     * Returns a rendering hint.
     *
     * @param  key The hint key (e.g. {@link Hints#JAI_INSTANCE}).
     * @return The hint value for the specified key, or {@code null} if there is no hint for the
     *         specified key.
     */
    public final Object getRenderingHint(final RenderingHints.Key key) {
        return (hints!=null) ? hints.get(key) : null;
    }
    
    /**
     * Apply a process operation to a grid coverage. The default implementation checks if source
     * coverages use an interpolation, and then invokes {@link Operation2D#doOperation}. If all
     * source coverages used the same interpolation, then this interpolation is applied to the
     * resulting coverage (except if the resulting coverage has already an interpolation).
     *
     * @param  operation The operation to be applied to the grid coverage.
     * @param  parameters Parameters required for the operation. The easiest way to construct them
     *         is to invoke <code>operation.{@link Operation#getParameters getParameters}()</code>
     *         and to modify the returned group.
     * @return The result as a grid coverage.
     */
    public synchronized GridCoverage doOperation(final Operation           operation,
                                                 final ParameterValueGroup parameters)
    {
        GridCoverage2D source;
        try {
            source = (GridCoverage2D) parameters.parameter("Source").getValue();
        } catch (ParameterNotFoundException exception) {
            // "Source" parameter may not exists. Conservatively
            // assume that the operation will do some usefull work.
            source = null;
        }
        /*
         * Checks if the result for this operation is already available in the cache.
         */
        final String operationName = operation.getName();
        final CachedOperation cacheKey = new CachedOperation(operation, parameters);
        GridCoverage2D coverage = (GridCoverage2D) cache.get(cacheKey);
        if (coverage != null) {
            log(source, coverage, operationName, true);
            return coverage;
        }
        /*
         * Detects the interpolation type for the source grid coverage.
         * The same interpolation will be applied on the result.
         */
        Interpolation[] interpolations = null;
        if (!operationName.equalsIgnoreCase("Interpolate")) {
            for (final Iterator it=parameters.values().iterator(); it.hasNext();) {
                final GeneralParameterValue param = (GeneralParameterValue) it.next();
                if (param instanceof ParameterValue) {
                    final Object value = ((ParameterValue) param).getValue();
                    if (value instanceof Interpolator2D) {
                        // If all sources use the same interpolation,  preserve the
                        // interpolation for the resulting coverage. Otherwise, use
                        // the default interpolation (nearest neighbor).
                        final Interpolation[] interp = ((Interpolator2D) value).getInterpolations();
                        if (interpolations == null) {
                            interpolations = interp;
                        } else if (!Arrays.equals(interpolations, interp)) {
                            // Set to no interpolation.
                            interpolations = null;
                            break;
                        }
                    }
                }
            }
        }
        /*
         * Apply the operation, apply the same interpolation and log a message.
         */
        if (operation instanceof Operation2D) {
            coverage = ((Operation2D) operation).doOperation(parameters, hints);
            if (interpolations!=null && coverage!=null && !(coverage instanceof Interpolator2D)) {
                coverage = Interpolator2D.create(coverage, interpolations);
            }
            if (coverage != source) {
                log(source, coverage, operationName, false);
                cache.put(cacheKey, coverage);
            }
            return coverage;
        }
        throw new OperationNotFoundException(Resources.format(
                  ResourceKeys.ERROR_OPERATION_NOT_FOUND_$1, operationName));
    }

    /**
     * Log a message for an operation. The message will be logged only if the source grid
     * coverage is different from the result (i.e. if the operation did some work).
     *
     * @param source The source grid coverage.
     * @param result The resulting grid coverage.
     * @param operationName the operation name.
     * @param fromCache {@code true} if the result has been fetch from the cache.
     */
    private static void log(final GridCoverage2D source,
                            final GridCoverage2D result,
                            final String  operationName,
                            final boolean     fromCache)
    {
        if (source != result) {
            String interp = "Nearest";
            if (result instanceof Interpolator2D) {
                interp = ImageUtilities.getInterpolationName(
                            ((Interpolator2D)result).getInterpolation());
            }
            final Locale locale = null; // Set locale here (if any).
            final LogRecord record = Resources.getResources(locale).getLogRecord(
                                     OPERATION, ResourceKeys.APPLIED_OPERATION_$4,
                                     ((source!=null) ? source : result).getName().toString(locale),
                                     operationName, interp, new Integer(fromCache ? 1:0));
            record.setSourceClassName("GridCoverageProcessor2D");
            record.setSourceMethodName("doOperation");
            LOGGER.log(record);
        }
    }

    /**
     * Scans for factory plug-ins on the application class path. This method is
     * needed because the application class path can theoretically change, or
     * additional plug-ins may become available. Rather than re-scanning the
     * classpath on every invocation of the API, the class path is scanned
     * automatically only on the first invocation. Clients can call this
     * method to prompt a re-scan. Thus this method need only be invoked by
     * sophisticated applications which dynamically make new plug-ins
     * available at runtime.
     *
     * @todo This method should be public, but can be executed only once in current
     *       implementation. We suffer from GeoAPI limitation here; the GridCoverage
     *       API really need a redesign.
     */
    private void scanForPlugins() {
        for (final Iterator it=registry.getServiceProviders(Operation2D.class); it.hasNext();) {
            addOperation((Operation2D) it.next());
        }
    }

    /**
     * Prints a description of all operations to the specified stream.
     * The description include operation names and lists of parameters.
     *
     * @param  out The destination stream.
     * @throws IOException if an error occured will writing to the stream.
     */
    public synchronized void print(final Writer out) throws IOException {
        final CoverageParameterWriter writer = new CoverageParameterWriter(out);
        final String lineSeparator = System.getProperty("line.separator", "\n");
        for (final Iterator it=operations.values().iterator(); it.hasNext();) {
            out.write(lineSeparator);
            writer.format(((Operation2D) it.next()).descriptor);
        }
    }

    /**
     * Dumps to the standard output stream a list of operations for the default
     * {@link GridCoverageProcessor2D}. This method can been invoked from the
     * command line. For example:
     *
     * <blockquote><pre>
     * java org.geotools.coverage.processing.GridCoverageProcessor2D
     * </pre></blockquote>
     *
     * <strong>Note for Windows users:</strong> If the output contains strange
     * symbols, try to supply an "{@code -encoding}" argument. Example:
     *
     * <blockquote><pre>
     * java org.geotools.coverage.processing.GridCoverageProcessor2D -encoding Cp850
     * </pre></blockquote>
     *
     * The codepage number (850 in the previous example) can be obtained from the DOS
     * commande line by entering the "{@code chcp}" command with no arguments.
     */
    public static void main(final String[] args) {
        final Arguments arguments = new Arguments(args);
        arguments.getRemainingArguments(0);
        try {
            getDefault().print(arguments.out);
        } catch (IOException exception) {
            // Should not occurs
            exception.printStackTrace(arguments.out);
        }
        arguments.out.close();
    }
}
