/*
 * Geotools 2 - OpenSource mapping toolkit
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
 *
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.coverage.processing;

// Collections
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

// JAI dependencies
import javax.media.jai.PropertySource;
import javax.media.jai.PropertySourceImpl;
import javax.media.jai.util.CaselessStringKey;  // For javadoc

// OpenGIS dependencies
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.processing.GridAnalysis;
import org.opengis.coverage.processing.GridCoverageProcessor;
import org.opengis.coverage.processing.Operation;
import org.opengis.coverage.processing.OperationNotFoundException;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.InvalidParameterNameException;

// Geotools dependencies
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.gcs.Resources;



/**
 * Base class for {@linkplain GridCoverageProcessor grid coverage processor} implementations.
 * Processors allow for different ways of accessing the grid coverage values. Using one of these
 * operations to change the way the grid is being accessed will not affect the state of the grid
 * coverage controlled by another operations. For example, changing the interpolation method
 * should not affect the number of sample dimensions currently being accessed or value sequence.
 * <br><br>
 * This base class provides a default implementation for all methods except
 * {@link #doOperation(Operation, ParameterValueGroup)}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class AbstractGridCoverageProcessor extends PropertySourceImpl
                                                 implements GridCoverageProcessor
{
    /**
     * The sequence of string to returns when there is no metadata.
     */
    private static final String[] NO_PROPERTIES = new String[0];

    /**
     * The logger for grid coverage processing operations.
     */
    public static final Logger LOGGER = Logger.getLogger("org.geotools.coverage.processing");

    /**
     * The logging level for reporting grid coverage operations.
     * This level is equals or slightly lower than {@link Level#INFO}.
     */
    public static final Level OPERATION = new LogLevel("OPERATION", 780);

    /**
     * The grid coverage logging level type.
     */
    private static final class LogLevel extends Level {
        protected LogLevel(final String name, final int level) {
            super(name, level);
        }
    }

    /**
     * The comparator for ordering of operation names.
     */
    private static final Comparator COMPARATOR = new Comparator() {
        public int compare(final Object name1, final Object name2) {
            return ((String) name1).toLowerCase().compareTo(
                   ((String) name2).toLowerCase());
        }
    };

    /**
     * The set of operation for this grid coverage processor. Keys are operation's name.
     * Values are operations and should not contains duplicated values.
     */
    final Map/*<String,Operation>*/ operations = new TreeMap(COMPARATOR);

    /**
     * Operations as array. Will be constructed only when first needed.
     */
    private transient Operation2D[] asArray;
    
    /**
     * Constructs a grid coverage processor.
     *
     * @param source The source for this processor, or {@code null} if none.
     * @param properties The set of properties for this processor, or {@code null} if there is none.
     *        "Properties" in <cite>Java Advanced Imaging</cite> is what OpenGIS calls "Metadata".
     *        Keys are {@link String} objects ({@link CaselessStringKey} are accepted as well),
     *        while values may be any {@link Object}.
     */
    public AbstractGridCoverageProcessor(final PropertySource source, final Map properties) {
        super(properties, source);
    }

    /**
     * Add the specified operation to this processor. This method is usually invoked
     * at construction time before this processor is made accessible.
     *
     * @param  operation The operation to add.
     * @throws IllegalStateException if an operation already exists
     *         with the same name than {@code operation}.
     */
    protected synchronized void addOperation(final Operation operation) throws IllegalStateException {
        final String name = operation.getName();
        if (!operations.containsKey(name)) {
            assert !operations.containsValue(operation);
            operations.put(name, operation);
            asArray = null;
        } else {
            throw new IllegalStateException(Resources.format(
                      ResourceKeys.ERROR_OPERATION_ALREADY_BOUND_$1, operation.getName()));
        }
    }

    /**
     * Returns the number of operations supported by this grid coverage processor.
     */
    public synchronized int getNumOperations() {
        return operations.size();
    }

    /**
     * Retrieve a grid processing operation information. The operation information
     * will contain the name of the operation as well as a list of its parameters.
     *
     * @param  index Index of the operation.
     * @return The operation for the given index.
     * @throws IndexOutOfBoundsException if the specified index is out of bounds.
     */
    public synchronized Operation getOperation(final int index) throws IndexOutOfBoundsException {
        if (asArray == null) {
            asArray =(Operation2D[])operations.values().toArray(new Operation2D[operations.size()]);
        }
        return asArray[index];
    }

    /**
     * Returns the operation for the specified name.
     *
     * @param  name Name of the operation.
     * @return The operation for the given name.
     * @throws OperationNotFoundException if there is no operation for the specified name.
     */
    public synchronized Operation getOperation(final String name) throws OperationNotFoundException {
        final Operation2D operation = (Operation2D) operations.get(name);
        if (operation != null) {
            return operation;
        }
        throw new OperationNotFoundException(Resources.format(
                ResourceKeys.ERROR_OPERATION_NOT_FOUND_$1, name));
    }

    /**
     * Convenience method applying a process operation with default parameters.
     *
     * @param  operationName Name of the operation to be applied to the grid coverage..
     * @param  source The source grid coverage.
     * @return The result as a grid coverage.
     * @throws OperationNotFoundException if there is no operation named <code>operationName</code>.
     *
     * @see #doOperation(Operation,ParameterValueGroup)
     */
    public GridCoverage doOperation(final String operationName, final GridCoverage source)
            throws OperationNotFoundException
    {
        final Operation operation = getOperation(operationName);
        final ParameterValueGroup parameters = operation.getParameters();
        parameters.parameter("Source").setValue(source);
        return doOperation(operation, parameters);
    }
    
    /**
     * Convenience method applying a process operation with one parameter.
     *
     * @param  operationName  Name of the operation to be applied to the grid coverage..
     * @param  source         The source grid coverage.
     * @param  argumentName1  The name of the first parameter to set.
     * @param  argumentValue1 The value for the first parameter.
     * @return The result as a grid coverage.
     * @throws OperationNotFoundException if there is no operation named <code>operationName</code>.
     * @throws InvalidParameterNameException if there is no parameter with the specified name.
     *
     * @see #doOperation(Operation,ParameterValueGroup)
     */
    public GridCoverage doOperation(final String operationName, final GridCoverage source,
                                    final String argumentName1, final Object argumentValue1)
            throws OperationNotFoundException, InvalidParameterNameException
    {
        final Operation operation = getOperation(operationName);
        final ParameterValueGroup parameters = operation.getParameters();
        parameters.parameter("Source").setValue(source);
        try {
            parameters.parameter(argumentName1).setValue(argumentValue1);
        } catch (ParameterNotFoundException cause) {
            throw invalidParameterName(cause);
        }
        return doOperation(operation, parameters);
    }
    
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
     * @throws InvalidParameterNameException if there is no parameter with the specified name.
     *
     * @see #doOperation(Operation,ParameterValueGroup)
     */
    public GridCoverage doOperation(final String operationName, final GridCoverage source,
                                    final String argumentName1, final Object argumentValue1,
                                    final String argumentName2, final Object argumentValue2)
            throws OperationNotFoundException, InvalidParameterNameException
    {
        final Operation operation = getOperation(operationName);
        final ParameterValueGroup parameters = operation.getParameters();
        parameters.parameter("Source").setValue(source);
        try {
            parameters.parameter(argumentName1).setValue(argumentValue1);
            parameters.parameter(argumentName2).setValue(argumentValue2);
        } catch (ParameterNotFoundException cause) {
            throw invalidParameterName(cause);
        }
        return doOperation(operation, parameters);
    }

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
     * @throws InvalidParameterNameException if there is no parameter with the specified name.
     *
     * @see #doOperation(Operation,ParameterValueGroup)
     */
    public GridCoverage doOperation(final String operationName, final GridCoverage source,
                                    final String argumentName1, final Object argumentValue1,
                                    final String argumentName2, final Object argumentValue2,
                                    final String argumentName3, final Object argumentValue3)
            throws OperationNotFoundException, InvalidParameterNameException
    {
        final Operation operation = getOperation(operationName);
        final ParameterValueGroup parameters = operation.getParameters();
        parameters.parameter("Source").setValue(source);
        try {
            parameters.parameter(argumentName1).setValue(argumentValue1);
            parameters.parameter(argumentName2).setValue(argumentValue2);
            parameters.parameter(argumentName3).setValue(argumentValue3);
        } catch (ParameterNotFoundException cause) {
            throw invalidParameterName(cause);
        }
        return doOperation(operation, parameters);
    }

    /**
     * Applies a process operation to a grid coverage.
     *
     * @deprecated Use {@link #doOperation(Operation, ParameterValueGroup)} instead.
     */
    public GridCoverage doOperation(final String operationName,
                                    final GeneralParameterValue[] parameters)
    {
        final Operation operation = getOperation(operationName);
        final ParameterValueGroup group = operation.getParameters();
        for (int i=0; i<parameters.length; i++) {
            final ParameterValue value = (ParameterValue) parameters[i];
            group.parameter(value.getDescriptor().getName().getCode()).setValue(value.getValue());
        }
        return doOperation(operation, group);
    }

    /**
     * Applies an operation.
     *
     * @param  operation The operation to be applied.
     * @param  parameters Parameters required for the operation. The easiest way to construct them
     *         is to invoke <code>operation.{@link Operation#getParameters getParameters}()</code>
     *         and to modify the returned group.
     * @return The result as a grid coverage.
     */
    public abstract GridCoverage doOperation(final Operation operation,
                                             final ParameterValueGroup parameters);

    /**
     * Converts a "parameter not found" exception into an "invalid parameter name".
     */
    private static InvalidParameterNameException invalidParameterName(final ParameterNotFoundException cause) {
        final String name = cause.getParameterName();
        final InvalidParameterNameException exception = new InvalidParameterNameException(
                Resources.format(ResourceKeys.ERROR_UNKNOW_PARAMETER_NAME_$1, name), name);
        exception.initCause(cause);
        return exception;
    }

    /**
     * @deprecated This method is not yet implemented, and may not be there in a future
     *             release of GeoAPI interfaces.
     */
    public GridAnalysis analyze(final GridCoverage gridCoverage) {
        throw new UnsupportedOperationException();
    }

    /**
     * List of metadata keywords for a coverage. If no metadata is available, the sequence
     * will be empty. The default implementation gets the list of metadata names from the
     * {@link #getPropertyNames()} method.
     *
     * @return the list of metadata keywords for a coverage.
     */
    public String[] getMetadataNames() {
        final String[] list = getPropertyNames();
        return (list != null) ? list : NO_PROPERTIES;
    }

    /**
     * Retrieve the metadata value for a given metadata name. The default implementation query
     * the {@link #getProperty(String)} method.
     *
     * @param name Metadata keyword for which to retrieve data.
     * @return the metadata value for a given metadata name.
     * @throws MetadataNameNotFoundException if there is no value for the specified metadata name.
     */
    public String getMetadataValue(final String name) throws MetadataNameNotFoundException {
        final Object value = getProperty(name);
        if (value == java.awt.Image.UndefinedProperty) {
            throw new MetadataNameNotFoundException(Resources.format(
                    ResourceKeys.ERROR_UNDEFINED_PROPERTY_$1, name));
        }
        return (value!=null) ? value.toString() : null;
    }    
}
