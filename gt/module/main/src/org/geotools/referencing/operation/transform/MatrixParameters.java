/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
package org.geotools.referencing.operation.transform;

// J2SE dependencies
import java.awt.Point;
import java.util.Map;
import java.util.Arrays;
import java.util.Locale;

// OpenGIS dependencies
import org.opengis.referencing.operation.Matrix;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.GeneralOperationParameter;
import org.opengis.parameter.ParameterNotFoundException;

// Geotools dependencies
import org.geotools.referencing.Info;
import org.geotools.resources.XArray;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.parameter.ParameterValue;
import org.geotools.parameter.OperationParameter;
import org.geotools.parameter.OperationParameterGroup;
import org.geotools.referencing.operation.GeneralMatrix;


/**
 * A parameter group backed by a {@linkplain Matrix matrix}. Changes to the {@linkplain Matrix
 * matrix} are reflected in the {@link OperationParameterGroup} and {@link ParameterValueGroup},
 * and vis-versa. This custom implementation is different than the default implementation in that
 * it is "extensible", i.e. the number of parameters depends of the number of row and column in
 * the matrix.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class MatrixParameters extends OperationParameterGroup implements ParameterValueGroup {
    /**
     * Serial number for interoperability with different versions.
     */
//    private static final long serialVersionUID = -5150306783193080487L;
// TODO

    /**
     * The default matrix size.
     */
    private static final int DEFAULT_SIZE = 3;

    /**
     * The height and weight of the matrix of {@link #parameters} to cache. Descriptors
     * for row or column indices greater than or equals to this value will not be cached.
     */
    private static final int CACHE_SIZE = 8;

    /**
     * The cached descriptors for each elements in a matrix.
     */
    private static final OperationParameter[] parameters = new OperationParameter[CACHE_SIZE*CACHE_SIZE];

    /**
     * The parameter for <code>"num_row"</code>.
     */
    private static final OperationParameter NUM_ROW =
            new OperationParameter("num_row", DEFAULT_SIZE, 0, Integer.MAX_VALUE);

    /**
     * The parameter for <code>"num_col"</code>.
     */
    private static final OperationParameter NUM_COL =
            new OperationParameter("num_col", DEFAULT_SIZE, 0, Integer.MAX_VALUE);

    /**
     * The parameter values. Will be constructed only when first requested.
     */
    private ParameterValue[][] values;

    /**
     * The value for <code>"num_row"</code>.
     */
    private final ParameterValue numRow = new ParameterValue(NUM_ROW);

    /**
     * The value for <code>"num_col"</code>.
     */
    private final ParameterValue numCol = new ParameterValue(NUM_COL);

    /**
     * The underlying matrix.
     */
    private final Matrix matrix = new GeneralMatrix(DEFAULT_SIZE);

    /**
     * Construct a parameter group. The properties map is given unchanged
     * to the {@linkplain Info#Info(Map) super-class constructor}.
     */
    public MatrixParameters(final Map properties) {
        super(properties, new OperationParameter[] {NUM_ROW, NUM_COL});
    }

    /**
     * Returns a description of this parameter value group. This is the same class, since the
     * description depends on the value of <code>"num_row"</code> and <code>"num_col"</code>
     * parameters.
     */
    public GeneralOperationParameter getDescriptor() {
        return this;
    }

    /**
     * Returns the parameter in this group for the specified name.
     * The name can be a matrix element if it uses the following syntax:
     * <code>"elt_<var>row</var>_<var>col</var>"</code>. For example
     * <code>"elt_2_1"</code> is the element name for the parameter at line
     * 2 and row 1. The row and column index are 0 based.
     *
     * @param  name The case insensitive name of the parameter to search for.
     * @return The parameter for the given name.
     * @throws ParameterNotFoundException if there is no parameter for the given name.
     */
    public GeneralOperationParameter getParameter(final String name) throws ParameterNotFoundException {
        return getValue(name).getDescriptor();
    }

    /**
     * Returns the value in this group for the specified parameter name.
     * The name can be a matrix element if it uses the following syntax:
     * <code>"elt_<var>row</var>_<var>col</var>"</code>. For example
     * <code>"elt_2_1"</code> is the element name for the value at line
     * 2 and row 1. The row and column index are 0 based.
     *
     * @param  name The case insensitive name of the parameter to search for.
     * @return The parameter value for the given name.
     * @throws ParameterNotFoundException if there is no parameter for the given name.
     */
    public GeneralParameterValue getValue(String name) throws ParameterNotFoundException {
        ensureNonNull("name", name);
        name = name.trim();
        if (name.equalsIgnoreCase("num_row")) {
            return numRow;
        }
        if (name.equalsIgnoreCase("num_col")) {
            return numCol;
        }
        NumberFormatException cause = null;
        try {
            if (name.regionMatches(true, 0, "elt_", 0, 4)) {
                final int separator = name.lastIndexOf('_');
                final int row = Short.parseShort(name.substring(4, separator));
                final int col = Short.parseShort(name.substring(separator+1));
                return getValue(row, col);
            }
        } catch (NumberFormatException exception) {
            cause = exception;
        }
        ParameterNotFoundException e = new ParameterNotFoundException(Resources.format(
                                       ResourceKeys.ERROR_MISSING_PARAMETER_$1, name), name);
        e.initCause(cause);
        throw e;
    }

    /**
     * Ensure that the specified value is a non-null positive integer.
     */
    private static void ensurePositive(final String name, final int value) {
        if (value <=0) {
            throw new IllegalArgumentException(Resources.format(
                ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, name, new Integer(value)));
        }
    }

    /**
     * Returns the parameter in this group for a matrix element at the specified index.
     * Row and column index are 0 based.
     */
    public OperationParameter getParameter(final int row, final int column)
            throws ParameterNotFoundException
    {
        int index = -1;
        OperationParameter param;
        if (row>=0 && row<CACHE_SIZE && column>=0 && column<CACHE_SIZE) {
            index = row*CACHE_SIZE + column;
            param = parameters[index];
            if (param != null) {
                return param;
            }
        }
        /*
         * Parameter not found in the cache. Create a new one and cache it for future reuse.
         * Note that this cache is shared by all MatrixParameters instance. There is no need
         * to synchronize since it is not a big deal if the same parameter is constructed twice.
         */
        param = new OperationParameter("elt_"+row+'_'+column,
                                       (row==column) ? 1.0 : 0.0,
                                       Double.NEGATIVE_INFINITY,
                                       Double.POSITIVE_INFINITY, null);
        if (index >= 0) {
            parameters[index] = param;
        }
        return param;
    }

    /**
     * Returns the value in this group for a matrix element at the specified index.
     * Row and column index are 0 based.
     */
    public ParameterValue getValue(final int row, final int column)
            throws ParameterNotFoundException
    {
        final double m = matrix.getElement(row, column);
        return null; // TODO
    }

    public GeneralParameterValue[] getValues() {
        return null; // TODO
    }

    /**
     * Returns the total number of parameters. This is equals to the
     * number of matrix elements plus 2 (the <code>"num_row"</code>
     * and <code>"num_col"</code> parameters).
     */
//    public int getNumParameters() {
//        return getNumRow() * getNumCol() + 2;
//    }

    /**
     * Construct a matrix from a parameter block. This method is used by
     * {@link MathTransformFactory#createParameterizedTransform}.
     */
//    public static Matrix getMatrix(final GeneralParameterValue[] parameters) {
//        if (parameters instanceof Matrix) {
//            return (Matrix) parameters;
//        }
//        final int numRow = parameters.getIntParameter("num_row");
//        final int numCol = parameters.getIntParameter("num_col");
//        final Matrix  matrix = new Matrix(numRow, numCol);
//        final String[] names = parameters.getParameterListDescriptor().getParamNames();
//        if (names!=null) {
//            for (int i=0; i<names.length; i++) {
//                final String name = names[i];
//                if (name.regionMatches(true, 0, "elt_", 0, 4)) {
//                    final int separator = name.lastIndexOf('_');
//                    final int row = Integer.parseInt(name.substring(4, separator));
//                    final int col = Integer.parseInt(name.substring(separator+1));
//                    matrix.setElement(row, col, parameters.getDoubleParameter(name));
//                }
//            }
//        }
//        return matrix;
//    }

    /**
     * Returns a clone of this parameter group.
     */
    public Object clone() {
        return null; // TODO (and also equals and hashCode)
    }
}
