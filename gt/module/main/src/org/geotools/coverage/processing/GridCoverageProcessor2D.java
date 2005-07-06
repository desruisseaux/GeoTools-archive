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
 * @since 2.1
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Replaced by {@link DefaultProcessor}, which is more general. There is no GeoAPI
 *  interface right now for {@code DefaultProcessor}, but the {@code GridCoverageProcessor}
 *  interface is not ready anyway. GeoAPI's Coverage interfaces are work in progress and not
 *  yet aligned on ISO 19123.
 */
public class GridCoverageProcessor2D extends AbstractGridCoverageProcessor {
    /**
     * The default grid coverage processor. Will be constructed only when first requested.
     */
    private static GridCoverageProcessor2D DEFAULT;
    
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
    }

    /**
     * Constructs a grid coverage processor initialized with the same set of operations than the
     * specified processor. The rendering hints are initialized to the union of the rendering
     * hints of the specified processor, and the specified rendering hints. More operations can
     * be added by invoking the {@link #addOperation} method at construction time.
     *
     * @param processor The processor to inherit from, or {@code null} if none.
     * @param hints A set of supplemental rendering hints, or {@code null} if none.
     *
     * @deprecated This constructor ignores the arguments.
     */
    public GridCoverageProcessor2D(final GridCoverageProcessor2D processor,
                                   final RenderingHints          hints)
    {
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
        }
        return DEFAULT;
    }

    /**
     * Prints a description of all operations to the specified stream.
     * The description include operation names and lists of parameters.
     *
     * @param  out The destination stream.
     * @throws IOException if an error occured will writing to the stream.
     */
    public void print(final Writer out) throws IOException {
        printOperations(out, null);
    }
}
