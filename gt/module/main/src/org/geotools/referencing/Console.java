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
 */
package org.geotools.referencing;

// J2SE dependencies
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.StringTokenizer;

import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.io.TableWriter;
import org.geotools.measure.Measure;
import org.geotools.referencing.wkt.AbstractConsole;
import org.geotools.referencing.wkt.Parser;
import org.geotools.referencing.wkt.Preprocessor;
import org.geotools.resources.Arguments;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;


/**
 * A console for executing CRS operations from the command line.
 * Instructions are read from the {@linkplain System#in standard input stream}
 * and results are sent to the  {@linkplain System#out standard output stream}.
 * Instructions include:
 *
 * <table>
 *   <tr><td nowrap valign="top"><P><code>SET</code> <var>name</var> <code>=</code> <var>wkt</var></P></td><td>
 *   <P align="justify">Set the specified <var>name</var> as a shortcut for the specified Well Know
 *   Text (<var>wkt</var>). This WKT can contains other shortcuts defined previously.</P></td></tr>
 *
 *   <tr><td nowrap valign="top"><P><code>transform = </code> <var>wkt</var></P></td><td>
 *   <P align="justify">Set explicitly a {@linkplain MathTransform math transform} to use for
 *   coordinate transformations. This instruction is a more direct alternative to the usage of
 *   <code>source crs</code> and <code>target crs</code> instruction.</P></td></tr>
 *
 *   <tr><td nowrap valign="top"><P><code>source crs = </code> <var>wkt</var></P></td><td>
 *   <P align="justify">Set the source {@linkplain CoordinateReferenceSystem coordinate reference
 *   system} to the specified object. This object can be specified as a Well Know Text
 *   (<var>wkt</var>) or as a shortcut previously set.</P></td></tr>
 *
 *   <tr><td nowrap valign="top"><P><code>target crs = </code> <var>wkt</var></P></td><td>
 *   <P align="justify">Set the target {@linkplain CoordinateReferenceSystem coordinate reference
 *   system} to the specified object. This object can be specified as a Well Know Text
 *   (<var>wkt</var>) or as a shortcut previously set. Once both source and target
 *   CRS are specified a {@linkplain MathTransform math transform} from source to
 *   target CRS is automatically infered.</P></td></tr>
 *
 *   <tr><td nowrap valign="top"><P><code>source pt = </code> <var>coord</var></P></td><td>
 *   <P align="justify">Transforms the specified coordinates from source CRS to target CRS
 *   and prints the result.</P>
 *
 *   <tr><td nowrap valign="top"><P><code>target pt = </code> <var>coord</var></P></td><td>
 *   <P align="justify">Inverse transforms the specified coordinates from target CRS to source CRS
 *   and prints the result.</P>
 *
 *   <tr><td nowrap valign="top"><P><code>test tolerance = </code> <var>vector</var></P></td><td>
 *   <P align="justify">Set the maximum difference between the transformed source point and the
 *   target point. Once this value is set, every occurence of the <code>target pt</code> instruction
 *   will trig this comparaison. If a greater difference is found, an exception is thrown or a
 *   message is printed to the error stream.</P>
 *
 *   <tr><td nowrap valign="top"><P><code>print set</code></P></td><td>
 *   <P align="justify">Prints the set of shortcuts defined in previous calls to <code>SET</code>
 *   instruction.</P></td></tr>
 *
 *   <tr><td nowrap valign="top"><P><code>print crs</code></P></td><td>
 *   <P align="justify">Prints the source and target
 *   {@linkplain CoordinateReferenceSystem coordinate reference system},
 *   {@linkplain MathTransform math transform} and its inverse
 *   as Well Know Text (wkt).</P></td></tr>
 *
 *   <tr><td nowrap valign="top"><P><code>print pts</code></P></td><td>
 *   <P align="justify">Prints the source and target points, their transformed points, and
 *   the distance between them.</P></td></tr>
 *
 *   <tr><td nowrap valign="top"><P><code>exit</code></P></td><td>
 *   <P align="justify">Quit the console.</P></td></tr>
 * </table>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Console extends AbstractConsole {
    /**
     * The number format to use for reading coordinate points.
     */
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

    /**
     * The number separator in vectors. Usually <code>,</code>, but could
     * also be <code>;</code> if the coma is already used as the decimal
     * separator.
     */
    private final String numberSeparator;

    /**
     * The coordinate operation factory to use.
     */
    private final CoordinateOperationFactory factory =
                  FactoryFinder.getCoordinateOperationFactory();

    /**
     * The source and target CRS, or <code>null</code> if not yet determined.
     */
    private CoordinateReferenceSystem sourceCRS, targetCRS;
    
    /**
     * Source and target coordinate points, or <code>null</code> if not yet determined.
     */
    private DirectPosition sourcePosition, targetPosition;

    /**
     * The math transform, or <code>null</code> if not yet determined.
     */
    private MathTransform transform;

    /**
     * The tolerance value. If non-null, the difference between the computed and the specified
     * target point will be compared against this tolerance threshold. If it is greater, a message
     * will be printed.
     */
    private double[] tolerance;

    /**
     * The last error thats occured while processing an instruction.
     * Used in order to print the stack trace on request.
     */
    private transient Exception lastError;

    /**
     * Creates a new console instance using {@linkplain System#in standard input stream},
     * {@linkplain System#out standard output stream}, {@linkplain System#err error output stream}
     * and the system default line separator.
     */
    public Console() {
        super(new Preprocessor(new Parser()));
        numberSeparator = getNumberSeparator(numberFormat);
    }
    
    /**
     * Creates a new console instance using the specified input stream.
     *
     * @param in The input stream.
     */
    public Console(final LineNumberReader in) {
        super(new Preprocessor(new Parser()), in);
        numberSeparator = getNumberSeparator(numberFormat);
    }

    /**
     * Returns the character to use as a number separator.
     * As a side effect, this method also adjust the minimum and maximum digits.
     */
    private static String getNumberSeparator(final NumberFormat numberFormat) {
        numberFormat.setGroupingUsed(false);
        numberFormat.setMinimumFractionDigits(6);
        numberFormat.setMaximumFractionDigits(6);
        if (numberFormat instanceof DecimalFormat) {
            final char decimalSeparator = ((DecimalFormat) numberFormat)
                        .getDecimalFormatSymbols().getDecimalSeparator();
            if (decimalSeparator == ',') {
                return ";";
            }
        }
        return ",";
    }

    /**
     * Run the console from the command line. Before to process all instructions
     * from the {@linkplain System#in standard input stream}, this method first
     * process the following optional command-line arguments:
     *
     * <TABLE CELLPADDING='0' CELLSPACING='0'>
     *   <TR><TD NOWRAP><CODE>-load</CODE> <VAR>&lt;filename&gt;</VAR></TD>
     *       <TD>&nbsp;Load a definition file before to run instructions from
     *           the standard input stream.</TD></TR>
     *   <TR><TD NOWRAP><CODE>-encoding</CODE> <VAR>&lt;code&gt;</VAR></TD>
     *       <TD>&nbsp;Set the character encoding.</TD></TR>
     *   <TR><TD NOWRAP><CODE>-locale</CODE> <VAR>&lt;language&gt;</VAR></TD>
     *       <TD>&nbsp;Set the language for the output (e.g. "fr" for French).</TD></TR>
     * </TABLE>
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final Arguments arguments = new Arguments(args);
        final String   load = arguments.getOptionalString("-load" );
        final String   file = arguments.getOptionalString("-file" );
        args = arguments.getRemainingArguments(0);
        Locale.setDefault(arguments.locale);
        final LineNumberReader input;
        final Console console;
        /*
         * The usual way to execute instructions from a file is to redirect the standard input
         * stream using the standard DOS/Unix syntax (e.g. "< thefile.txt").  However, we also
         * accept a "-file" argument for the same purpose. It is easier to debug. On DOS system,
         * it also use the system default encoding instead of the command-line one.
         */
        if (file == null) {
            input   = null;
            console = new Console();
        } else try {
            input   = new LineNumberReader(new FileReader(file));
            console = new Console(input);
            console.setPrompt(null);
        } catch (IOException exception) {
            System.err.println(exception.getLocalizedMessage());
            return;
        }
        /*
         * Load predefined shorcuts. The file must be in the form "name = WKT". An example
         * of such file is the property file used by the property-based authority factory.
         */
        if (load != null) try {
            final LineNumberReader in = new LineNumberReader(new FileReader(load));
            try {
                console.loadDefinitions(in);
            } catch (ParseException exception) {
                console.reportError(exception);
                in.close();
                return;
            }
            in.close();
        } catch (IOException exception) {
            console.reportError(exception);
            return;
        }
        /*
         * Run all instructions and close the stream if it was a file one.
         */
        console.run();
        if (input != null) try {
            input.close();
        } catch (IOException exception) {
            console.reportError(exception);
        }
    }

    /**
     * Execute the specified instruction.
     *
     * @param  instruction The instruction to execute.
     * @throws IOException if an I/O operation failed while writting to the
     *         {@linkplain #out output stream}.
     * @throws ParseException if a line can't be parsed.
     * @throws FactoryException If a transform can't be created.
     * @throws TransformException if a transform failed.
     */
    protected void execute(String instruction)
            throws IOException, ParseException, FactoryException, TransformException
    {
        String value = null;
        int i = instruction.indexOf('=');
        if (i >= 0) {
            value       = instruction.substring(i+1).trim();
            instruction = instruction.substring(0,i).trim();
        }
        final StringTokenizer keywords = new StringTokenizer(instruction);
        if (keywords.hasMoreTokens()) {
            final String key0 = keywords.nextToken();
            if (!keywords.hasMoreTokens()) {
                // -------------------------------
                //   exit
                // -------------------------------
                if (key0.equalsIgnoreCase("exit")) {
                    if (value != null) {
                        throw unexpectedArgument("exit");
                    }
                    stop();
                    return;
                }
                // -------------------------------
                //   stacktrace
                // -------------------------------
                if (key0.equalsIgnoreCase("stacktrace")) {
                    if (value != null) {
                        throw unexpectedArgument("stacktrace");
                    }
                    if (lastError != null) {
                        lastError.printStackTrace(err);
                    }
                    return;
                }
                // -------------------------------
                //   transform = <the transform>
                // -------------------------------
                if (key0.equalsIgnoreCase("transform")) {
                    transform = (MathTransform) parseObject(value, MathTransform.class);
                    sourceCRS = null;
                    targetCRS = null;
                    return;
                }
            } else {
                final String key1 = keywords.nextToken();
                if (!keywords.hasMoreTokens()) {
                    // -------------------------------
                    //   print definition|crs|points
                    // -------------------------------
                    if (key0.equalsIgnoreCase("print")) {
                        if (value != null) {
                            throw unexpectedArgument("print");
                        }
                        if (key1.equalsIgnoreCase("set")) {
                            printDefinitions();
                            return;
                        }
                        if (key1.equalsIgnoreCase("crs")) {
                            printCRS();
                            return;
                        }
                        if (key1.equalsIgnoreCase("pts")) {
                            printPts();
                            return;
                        }
                    }
                    // -------------------------------
                    //   set <name> = <wkt>
                    // -------------------------------
                    if (key0.equalsIgnoreCase("set")) {
                        addDefinition(key1, value);
                        return;
                    }
                    // -------------------------------
                    //   test tolerance = <vector>
                    // -------------------------------
                    if (key0.equalsIgnoreCase("test")) {
                        if (key1.equalsIgnoreCase("tolerance")) {
                            tolerance = parseVector(value);
                            return;
                        }
                    }
                    // -------------------------------
                    //   source|target crs = <wkt>
                    // -------------------------------
                    if (key1.equalsIgnoreCase("crs")) {
                        if (key0.equalsIgnoreCase("source")) {
                            sourceCRS = (CoordinateReferenceSystem)
                                        parseObject(value, CoordinateReferenceSystem.class);
                            transform = null;
                            return;
                        }
                        if (key0.equalsIgnoreCase("target")) {
                            targetCRS = (CoordinateReferenceSystem)
                                        parseObject(value, CoordinateReferenceSystem.class);
                            transform = null;
                            return;
                        }
                    }
                    // -------------------------------
                    //   source|target pt = <coords>
                    // -------------------------------
                    if (key1.equalsIgnoreCase("pt")) {
                        if (key0.equalsIgnoreCase("source")) {
                            sourcePosition = new GeneralDirectPosition(parseVector(value));
                            return;
                        }
                        if (key0.equalsIgnoreCase("target")) {
                            targetPosition = new GeneralDirectPosition(parseVector(value));
                            if (tolerance!=null && sourcePosition!=null) {
                                update();
                                if (transform != null) {
                                    test();
                                }
                            }
                            return;
                        }
                    }
                }
            }
        }
        // TODO: localize
        throw new ParseException("Illegal instruction \""+instruction+"\".", 0);
    }

    /**
     * Executes the "<code>print crs</code>" instruction.
     * @todo Localize
     */
    private void printCRS() throws FactoryException, IOException {
        final TableWriter table = new TableWriter(out, " \u2502 ");
        table.setMultiLinesCells(true);
        char separator = '\u2500';
        if (sourceCRS!=null || targetCRS!=null) {
            table.writeHorizontalSeparator();
            table.write("Source CRS");
            table.nextColumn();
            table.write("Target CRS");
            table.nextLine();
            table.writeHorizontalSeparator();
            if (sourceCRS != null) {
                table.write(parser.format(sourceCRS));
            }
            table.nextColumn();
            if (targetCRS != null) {
                table.write(parser.format(targetCRS));
            }
            table.nextLine();
            separator = '\u2550';
        }
        /*
         * Format the math transform and its inverse, if any.
         */
        update();
        if (transform != null) {
            table.nextLine(separator);
            table.write("Math transform");
            table.nextColumn();
            table.write("Inverse transform");
            table.nextLine();
            table.writeHorizontalSeparator();
            table.write(parser.format(transform));
            table.nextColumn();
            try {
                table.write(parser.format(transform.inverse()));
            } catch (NoninvertibleTransformException exception) {
                table.write(exception.getLocalizedMessage());
            }
            table.nextLine();
        }
        table.writeHorizontalSeparator();
        table.flush();
    }

    /**
     * Print the source and target point, and their transforms.
     *
     * @throws FactoryException if the transform can't be computed.
     * @throws TransformException if a transform failed.
     * @throws IOException if an error occured while writing to the output stream.
     * @todo Localize line headers.
     */
    private void printPts() throws FactoryException, TransformException, IOException {
        update();
        DirectPosition transformedSource = null;
        DirectPosition transformedTarget = null;
        if (transform != null) {
            if (sourcePosition != null) {
                transformedSource = transform.transform(sourcePosition, null);
            }
            if (targetPosition != null) {
                transformedTarget = transform.inverse().transform(targetPosition, null);
            }
        }
        final TableWriter table = new TableWriter(out, 0);
        table.setMultiLinesCells(true);
        table.writeHorizontalSeparator();
        table.setAlignment(TableWriter.ALIGN_RIGHT);
        if (sourcePosition != null) {
            table.write("Source point:");
            print(sourcePosition,    table);
            print(transformedSource, table);
            table.nextLine();
        }
        if (targetPosition != null) {
            table.write("Target point:");
            print(transformedTarget, table);
            print(targetPosition,    table);
            table.nextLine();
        }
        if (sourceCRS!=null && targetCRS!=null) {
            table.write("Distance:");
            printDistance(sourceCRS, sourcePosition, transformedTarget, table);
            printDistance(targetCRS, targetPosition, transformedSource, table);
            table.nextLine();
        }
        table.writeHorizontalSeparator();
        table.flush();
    }

    /**
     * Print the specified point to the specified table.
     * This helper method is for use by {@link #printPts}.
     *
     * @param  point The point to print, or <code>null</code> if none.
     * @throws IOException if an error occured while writting to the output stream.
     */
    private void print(final DirectPosition point, final TableWriter table) throws IOException {
        if (point != null) {
            table.nextColumn();
            table.write("  (");
            final double[] coords = point.getCoordinates();
            for (int i=0; i<coords.length; i++) {
                if (i != 0) {
                    table.write(", ");
                }
                table.nextColumn();
                table.write(numberFormat.format(coords[i]));
            }
            table.write(')');
        }
    }

    /**
     * Print the distance between two points using the specified CRS.
     */
    private void printDistance(final CoordinateReferenceSystem crs,
                               final DirectPosition      position1,
                               final DirectPosition      position2,
                               final TableWriter         table)
            throws IOException
    {
        if (position1 == null) {
            // Note: 'position2' is checked below, *after* blank columns insertion.
            return;
        }
        for (int i=crs.getCoordinateSystem().getDimension(); --i>=0;) {
            table.nextColumn();
        }
        if (position2 != null) {
            if (crs instanceof org.geotools.referencing.crs.CoordinateReferenceSystem) try {
                final Measure distance;
                distance = ((org.geotools.referencing.crs.CoordinateReferenceSystem)crs)
                           .distance(position1.getCoordinates(), position2.getCoordinates());
                table.setAlignment(TableWriter.ALIGN_RIGHT);
                table.write(numberFormat.format(distance.doubleValue()));
                table.write("  ");
                table.nextColumn();
                table.write(String.valueOf(distance.getUnit()));
                table.setAlignment(TableWriter.ALIGN_LEFT);
                return;
            } catch (UnsupportedOperationException ignore) {
                /*
                 * Underlying CS do not supports distance computation.
                 * Left the column blank.
                 */
            }
        }
        table.nextColumn();
    }




    ///////////////////////////////////////////////////////////
    ////////                                           ////////
    ////////        H E L P E R   M E T H O D S        ////////
    ////////                                           ////////
    ///////////////////////////////////////////////////////////

    /**
     * Invoked automatically when the <code>target pt</code> instruction were executed and a
     * <code>test tolerance</code> were previously set. The default implementation compares
     * the transformed source point with the expected target point. If a mismatch greater than
     * the tolerance error is found, an exception is thrown. Subclasses may overrides this
     * method in order to performs more tests.
     *
     * @throws TransformException if the source point can't be transformed, or a mistmatch is found.
     * @throws MismatchedDimensionException if the transformed source point doesn't have the
     *         expected dimension.
     */
    protected void test() throws TransformException, MismatchedDimensionException {
        final DirectPosition transformedSource = transform.transform(sourcePosition, null);
        final int sourceDim = transformedSource.getDimension();
        final int targetDim =    targetPosition.getDimension();
        if (sourceDim != targetDim) {
            throw new MismatchedDimensionException(Resources.format(
                        ResourceKeys.ERROR_MISMATCHED_DIMENSION_$2,
                        new Integer(sourceDim), new Integer(targetDim)));
        }
        for (int i=0; i<sourceDim; i++) {
            // Use '!' for catching NaN.
            if (!(Math.abs(transformedSource.getOrdinate(i) -
                              targetPosition.getOrdinate(i))
                  <= tolerance[Math.min(i, tolerance.length-1)]))
            {
                // TODO: Localize
                throw new TransformException("Transformation doesn't produce the expected value.");
            }
        }
    }

    /**
     * Check if the specified string start and end with the specified delimitors,
     * and returns the string without the delimitors.
     *
     * @param text  The string to check.
     * @param start The delimitor required at the string begining.
     * @param end   The delimitor required at the string end.
     */
    private static String removeDelimitors(String text, final char start, final char end) {
        text = text.trim();
        final int endPos = text.length()-1;
        if (endPos >= 1) {
            if (text.charAt(0)==start && text.charAt(endPos)==end) {
                text = text.substring(1, endPos).trim();
            }
        }
        return text;
    }

    /**
     * Parse a vector of values. Vectors are used for coordinate points.
     * Example:
     * <pre>
     * (46.69439222, 13.91405611, 41.21)
     * </pre>
     *
     * @param  text The vector to parse.
     * @return The vector as floating point numbers.
     * @throws ParseException if a number can't be parsed.
     */
    private double[] parseVector(String text) throws ParseException {
        text = removeDelimitors(text, '(', ')');
        final StringTokenizer st = new StringTokenizer(text, numberSeparator);
        final double[]    values = new double[st.countTokens()];
        for (int i=0; i<values.length; i++) {
            values[i] = numberFormat.parse(st.nextToken().trim()).doubleValue();
        }
        return values;
    }

    /**
     * Update the internal state after a change, before to apply transformation.
     * The most important change is to update the math transform, if needed.
     */
    private void update() throws FactoryException {
        if (transform==null && sourceCRS!=null && targetCRS!=null) {
            transform = factory.createOperation(sourceCRS, targetCRS).getMathTransform();
        }
    }

    /**
     * Constructs an exception saying that an argument was unexpected.
     *
     * @param  instruction The instruction name.
     * @return The exception to throws.
     * @todo Localize.
     */
    private static ParseException unexpectedArgument(final String instruction) {
        return new ParseException("Unexpected argument for instruction \"" +
                                  instruction + "\".", 0);
    }

    /**
     * {@inheritDoc}
     *
     * @param exception The exception to report.
     */
    protected void reportError(final Exception exception) {
        super.reportError(exception);
        lastError = exception;
    }
}
