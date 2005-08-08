/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Management Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
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

// J2SE and JAI dependencies
import java.awt.RenderingHints;
import javax.media.jai.KernelJAI;
import javax.media.jai.Interpolation;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.coverage.processing.Operation;
import org.opengis.coverage.processing.OperationNotFoundException;
import org.opengis.parameter.InvalidParameterNameException;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * Convenience, type-safe, methods for applying some common operations on
 * {@linkplain Coverage coverage} objects. All methods wrap their arguments in a
 * {@linkplain ParameterValueGroup parameter value group} and delegate the work to the processor's
 * {@link AbstractProcessor#doOperation doOperation} method.
 * This convenience class do not brings any new functionalities, but brings type-safety when the
 * operation is know at compile time. For operation unknown at compile time (e.g. for an operation
 * selected by users in some widget), use the {@linkplain AbstractProcessor processor} directly.
 *
 * @since 2.2
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see org.geotools.coverage.processing.operation
 */
public class Operations {
    /**
     * The default instance.
     */
    public static final Operations DEFAULT = new Operations(null);

    /**
     * The processor to use for applying operations. If null, will be created only when first
     * needed.
     *
     * @todo Uses the GeoAPI's interface instead once it will be ready.
     */
    private AbstractProcessor processor;

    /**
     * Creates a new instance using the specified hints.
     *
     * @param hints The hints, or {@code null} if none.
     */
    public Operations(final RenderingHints hints) {
        if (hints != null && !hints.isEmpty()) {
            processor = new BufferedProcessor(hints);
        }
        // Otherwise, will creates the processor only when first needed.
    }

    /**
     * Adds constants (one for each band) to every sample values of the source coverage.
     *
     * @param source The source coverage.
     * @param constants The constants to add to each band.
     *
     * @see org.geotools.coverage.processing.operation.AddConst
     */
    public Coverage add(final Coverage source, final double[] constants) {
        return doOperation("AddConst", source, "constants", constants);
    }

    /**
     * Subtracts constants (one for each band) from every sample values of the source coverage.
     *
     * @param source The source coverage.
     * @param constants The constants to subtract to each band.
     *
     * @see org.geotools.coverage.processing.operation.SubtractConst
     */
    public Coverage subtract(final Coverage source, final double[] constants) {
        return doOperation("SubtractConst", source, "constants", constants);
    }

    /**
     * Subtracts every sample values of the source coverage from constants (one for each band).
     *
     * @param source The source coverage.
     * @param constants The constants to subtract from.
     *
     * @see org.geotools.coverage.processing.operation.SubtractFromConst
     */
    public Coverage subtractFrom(final Coverage source, final double[] constants) {
        return doOperation("SubtractFromConst", source, "constants", constants);
    }

    /**
     * Multiplies every sample values of the source coverage by constants (one for each band).
     *
     * @param source The source coverage.
     * @param constants The constants to multiply to each band.
     *
     * @see org.geotools.coverage.processing.operation.MultiplyConst
     */
    public Coverage multiply(final Coverage source, final double[] constants) {
        return doOperation("MultiplyConst", source, "constants", constants);
    }

    /**
     * Divides every sample values of the source coverage by constants (one for each band).
     *
     * @param source The source coverage.
     * @param constants The constants to divides by.
     *
     * @see org.geotools.coverage.processing.operation.DivideByConst
     */
    public Coverage divideBy(final Coverage source, final double[] constants) {
        return doOperation("DivideByConst", source, "constants", constants);
    }

    /**
     * Maps the sample values of a coverage from one range to another range.
     *
     * @param source The source coverage.
     * @param constants The constants to multiply to each band.
     * @param offsets The constants to add to each band.
     *
     * @see org.geotools.coverage.processing.operation.Rescale
     */
    public Coverage rescale(final Coverage source, final double[] constants, final double[] offsets) {
        return doOperation("Rescale", source, "constants", constants, "offsets", offsets);
    }

    /**
     * Inverts the sample values of a coverage.
     *
     * @param source The source coverage.
     *
     * @see org.geotools.coverage.processing.operation.Invert
     */
    public Coverage invert(final Coverage source) {
        return doOperation("Invert", source);
    }

    /**
     * Computes the mathematical absolute value of each sample value.
     *
     * @param source The source coverage.
     *
     * @see org.geotools.coverage.processing.operation.Absolute
     */
    public Coverage absolute(final Coverage source) {
        return doOperation("Absolute", source);
    }

    /**
     * Takes the natural logarithm of the sample values of a coverage.
     *
     * @param source The source coverage.
     *
     * @see org.geotools.coverage.processing.operation.Log
     */
    public Coverage log(final Coverage source) {
        return doOperation("Log", source);
    }

    /**
     * Takes the exponential of the sample values of a coverage.
     *
     * @param source The source coverage.
     *
     * @see org.geotools.coverage.processing.operation.Exp
     */
    public Coverage exp(final Coverage source) {
        return doOperation("Exp", source);
    }

    /**
     * Specifies the interpolation type to be used to interpolate values for points which fall
     * between grid cells. The default value is nearest neighbor. The new interpolation type
     * operates on all sample dimensions.
     *
     * @param source The source coverage.
     * @param type The interpolation type. Possible values are {@code "NearestNeighbor"},
     *        {@code "Bilinear"} and {@code "Bicubic"}.
     *
     * @see org.geotools.coverage.processing.operation.Interpolate
     */
    public GridCoverage interpolate(final GridCoverage source, final String type) {
        return (GridCoverage) doOperation("Interpolate", source, "Type", type);
    }

    /**
     * Specifies the interpolation type to be used to interpolate values for points which fall
     * between grid cells. The default value is nearest neighbor. The new interpolation type
     * operates on all sample dimensions.
     *
     * @param source The source coverage.
     * @param type The interpolation type as a JAI interpolation object.
     *
     * @see org.geotools.coverage.processing.operation.Interpolate
     */
    public GridCoverage interpolate(final GridCoverage source, final Interpolation type) {
        return (GridCoverage) doOperation("Interpolate", source, "Type", type);
    }

    /**
     * Specifies the interpolation types to be used to interpolate values for points which fall
     * between grid cells. The first element in the array is the primary interpolation. All other
     * elements are fallback to be used if the primary interpolation returns a {@code NaN} value.
     * See {@link org.geotools.coverage.processing.operation.Interpolate} operation for details.
     *
     * @param source The source coverage.
     * @param types The interpolation types and their fallback.
     *
     * @see org.geotools.coverage.processing.operation.Interpolate
     */
    public GridCoverage interpolate(final GridCoverage source, final Interpolation[] types) {
        return (GridCoverage) doOperation("Interpolate", source, "Type", types);
    }

    /**
     * Resamples a coverage to the specified coordinate reference system.
     *
     * @param source The source coverage.
     * @param crs The target coordinate reference system.
     *
     * @see org.geotools.coverage.processing.operation.Resample
     */
    public Coverage resample(final Coverage source, final CoordinateReferenceSystem crs) {
        return doOperation("Resample", source, "CoordinateReferenceSystem", crs);
    }

    /**
     * Resamples a grid coverage to the specified coordinate reference system and grid geometry.
     *
     * @param source The source coverage.
     * @param crs The target coordinate reference system, or {@code null} for keeping it unchanged.
     * @param gridGeometry      The grid geometry, or {@code null} for a default one.
     * @param interpolationType The interpolation type, or {@code null} for the default one.
     *
     * @see org.geotools.coverage.processing.operation.Resample
     */
    public Coverage resample(final GridCoverage  source,
                             final CoordinateReferenceSystem crs,
                             final GridGeometry  gridGeometry,
                             final Interpolation interpolationType)
    {
        return doOperation("Resample", source, "CoordinateReferenceSystem", crs,
                           "GridGeometry", gridGeometry, "InterpolationType", interpolationType);
    }

    /**
     * Chooses <var>N</var> {@linkplain org.geotools.coverage.GridSampleDimension sample dimensions}
     * from a coverage and copies their sample data to the destination grid coverage in the order
     * specified.
     *
     * @param source The source coverage.
     * @param sampleDimensions The sample dimensions to select.
     *
     * @see org.geotools.coverage.processing.operation.SelectSampleDimension
     */
    public Coverage selectSampleDimension(final Coverage source, final int[] sampleDimensions) {
        return doOperation("SelectSampleDimension", source, "SampleDimensions", sampleDimensions);
    }

    /**
     * Replaces {@link Float#NaN NaN} values by the weighted average of neighbors values.
     * This method uses the default padding and validity threshold.
     *
     * @param source The source coverage.
     *
     * @see org.geotools.coverage.processing.operation.NodataFilter
     */
    public GridCoverage nodataFilter(final GridCoverage source) {
        return (GridCoverage) doOperation("NodataFilter", source);
    }

    /**
     * Replaces {@link Float#NaN NaN} values by the weighted average of neighbors values.
     *
     * @param source The source coverage.
     *
     * @see org.geotools.coverage.processing.operation.NodataFilter
     */
    public GridCoverage nodataFilter(final GridCoverage source, final int padding,
                                     final int validityThreshold)
    {
        return (GridCoverage) doOperation("NodataFilter",      source,
                                          "padding",           new Integer(padding),
                                          "validityThreshold", new Integer(validityThreshold));
    }

    /**
     * Edge detector which computes the magnitude of the image gradient vector in two orthogonal
     * directions. The default masks are the Sobel ones.
     *
     * @param source The source coverage.
     *
     * @see org.geotools.coverage.processing.operation.GradientMagnitude
     */
    public Coverage gradientMagnitude(final Coverage source) {
        return doOperation("GradientMagnitude", source);
    }

    /**
     * Edge detector which computes the magnitude of the image gradient vector in two orthogonal
     * directions.
     *
     * @param source The source coverage.
     * @param mask1  The first mask.
     * @param mask2  The second mask, orthogonal to the first one.
     *
     * @see org.geotools.coverage.processing.operation.GradientMagnitude
     */
    public Coverage gradientMagnitude(final Coverage source,
                                      final KernelJAI mask1, final KernelJAI mask2)
    {
        return doOperation("GradientMagnitude", source, "mask1", mask1, "mask2", mask2);
    }

    /**
     * Returns the processor, creating one if needed.
     */
    private AbstractProcessor getProcessor() {
        // No need to synchronize.
        if (processor == null) {
            processor = AbstractProcessor.getInstance();
        }
        return processor;
    }

    /**
     * Applies a process operation with default parameters.
     * This is a helper method for implementation of various convenience methods in this class.
     *
     * @param  operationName Name of the operation to be applied to the coverage.
     * @param  source The source coverage.
     * @return The result as a coverage.
     * @throws OperationNotFoundException if there is no operation named {@code operationName}.
     */
    protected final Coverage doOperation(final String operationName, final Coverage source)
            throws OperationNotFoundException
    {
        final AbstractProcessor processor = getProcessor();
        final Operation operation = processor.getOperation(operationName);
        final ParameterValueGroup parameters = operation.getParameters();
        parameters.parameter("Source").setValue(source);
        return processor.doOperation(parameters);
    }
    
    /**
     * Applies a process operation with one parameter.
     * This is a helper method for implementation of various convenience methods in this class.
     *
     * @param  operationName  Name of the operation to be applied to the coverage.
     * @param  source         The source coverage.
     * @param  argumentName1  The name of the first parameter to set.
     * @param  argumentValue1 The value for the first parameter.
     * @return The result as a coverage.
     * @throws OperationNotFoundException if there is no operation named {@code operationName}.
     * @throws InvalidParameterNameException if there is no parameter with the specified name.
     */
    protected final Coverage doOperation(final String operationName, final Coverage source,
                                         final String argumentName1, final Object   argumentValue1)
            throws OperationNotFoundException, InvalidParameterNameException
    {
        final AbstractProcessor processor = getProcessor();
        final Operation operation = processor.getOperation(operationName);
        final ParameterValueGroup parameters = operation.getParameters();
        parameters.parameter("Source").setValue(source);
        try {
            if (argumentValue1!=null) parameters.parameter(argumentName1).setValue(argumentValue1);
        } catch (ParameterNotFoundException cause) {
            throw invalidParameterName(cause);
        }
        return processor.doOperation(parameters);
    }
    
    /**
     * Applies process operation with two parameters.
     * This is a helper method for implementation of various convenience methods in this class.
     *
     * @param  operationName  Name of the operation to be applied to the coverage.
     * @param  source         The source coverage.
     * @param  argumentName1  The name of the first parameter to set.
     * @param  argumentValue1 The value for the first parameter.
     * @param  argumentName2  The name of the second parameter to set.
     * @param  argumentValue2 The value for the second parameter.
     * @return The result as a coverage.
     * @throws OperationNotFoundException if there is no operation named {@code operationName}.
     * @throws InvalidParameterNameException if there is no parameter with the specified name.
     */
    protected final Coverage doOperation(final String operationName, final Coverage source,
                                         final String argumentName1, final Object   argumentValue1,
                                         final String argumentName2, final Object   argumentValue2)
            throws OperationNotFoundException, InvalidParameterNameException
    {
        final AbstractProcessor processor = getProcessor();
        final Operation operation = processor.getOperation(operationName);
        final ParameterValueGroup parameters = operation.getParameters();
        parameters.parameter("Source").setValue(source);
        try {
            if (argumentValue1!=null) parameters.parameter(argumentName1).setValue(argumentValue1);
            if (argumentValue2!=null) parameters.parameter(argumentName2).setValue(argumentValue2);
        } catch (ParameterNotFoundException cause) {
            throw invalidParameterName(cause);
        }
        return processor.doOperation(parameters);
    }

    /**
     * Applies a process operation with three parameters.
     * This is a helper method for implementation of various convenience methods in this class.
     *
     * @param  operationName  Name of the operation to be applied to the coverage.
     * @param  source         The source coverage.
     * @param  argumentName1  The name of the first parameter to set.
     * @param  argumentValue1 The value for the first parameter.
     * @param  argumentName2  The name of the second parameter to set.
     * @param  argumentValue2 The value for the second parameter.
     * @param  argumentName3  The name of the third parameter to set.
     * @param  argumentValue3 The value for the third parameter.
     * @return The result as a coverage.
     * @throws OperationNotFoundException if there is no operation named {@code operationName}.
     * @throws InvalidParameterNameException if there is no parameter with the specified name.
     */
    protected final Coverage doOperation(final String operationName, final Coverage source,
                                         final String argumentName1, final Object   argumentValue1,
                                         final String argumentName2, final Object   argumentValue2,
                                         final String argumentName3, final Object   argumentValue3)
            throws OperationNotFoundException, InvalidParameterNameException
    {
        final AbstractProcessor processor = getProcessor();
        final Operation operation = processor.getOperation(operationName);
        final ParameterValueGroup parameters = operation.getParameters();
        parameters.parameter("Source").setValue(source);
        try {
            if (argumentValue1!=null) parameters.parameter(argumentName1).setValue(argumentValue1);
            if (argumentValue2!=null) parameters.parameter(argumentName2).setValue(argumentValue2);
            if (argumentValue3!=null) parameters.parameter(argumentName3).setValue(argumentValue3);
        } catch (ParameterNotFoundException cause) {
            throw invalidParameterName(cause);
        }
        return processor.doOperation(parameters);
    }

    /**
     * Converts a "parameter not found" exception into an "invalid parameter name".
     */
    private static InvalidParameterNameException invalidParameterName(final ParameterNotFoundException cause) {
        final String name = cause.getParameterName();
        final InvalidParameterNameException exception = new InvalidParameterNameException(
                Errors.format(ErrorKeys.UNKNOW_PARAMETER_NAME_$1, name), name);
        exception.initCause(cause);
        return exception;
    }
}
