/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
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
import java.util.Locale;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.text.ParseException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.Format;
import java.io.Writer;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.LineNumberReader;
import java.io.IOException;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.spatialschema.geometry.DirectPosition;

// Geotools dependencies
import org.geotools.io.TableWriter;
import org.geotools.resources.Arguments;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.referencing.wkt.Parser;
import org.geotools.referencing.wkt.AbstractConsole;
import org.geotools.geometry.GeneralDirectPosition;


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
 *   <tr><td nowrap valign="top"><P><code>status</code></P></td><td>
 *   <P align="justify">Print the current status (source and target
 *   {@linkplain CoordinateReferenceSystem coordinate reference system},
 *   {@linkplain MathTransform math transform} and its inverse).</P></td></tr>
 * </table>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Console extends AbstractConsole {
    /**
     * The number format to use for reading coordinate points.
     */
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance();

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
     * Creates a new console instance using {@linkplain System#in standard input stream},
     * {@linkplain System#out standard output stream}, {@linkplain System#err error output stream}
     * and the system default line separator.
     */
    public Console() {
        super(new Parser());
        numberSeparator = getNumberSeparator(numberFormat);
    }
    
    /**
     * Creates a new console instance using the specified input stream.
     *
     * @param in The input stream.
     */
    public Console(final LineNumberReader in) {
        super(new Parser(), in);
        numberSeparator = getNumberSeparator(numberFormat);
    }

    /**
     * Returns the character to use as a number separator.
     * As a side effect, this method also adjust the minimum and maximum digits.
     */
    private static String getNumberSeparator(final NumberFormat numberFormat) {
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
     *   <TR><TD NOWRAP><CODE>-quiet</CODE></TD>
     *       <TD>&nbsp;Do not print a confirmation for each command executed.</TD></TR>
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
        final boolean quiet = arguments.getFlag          ("-quiet");
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
        console.setQuiet(quiet);
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
            /*
             * 1-keyword instructions
             * Example: status, transform
             */
            final String key0 = keywords.nextToken();
            if (!keywords.hasMoreTokens()) {
                if (key0.equalsIgnoreCase("status")) {
                    if (value != null) {
                        throw unexpectedArgument("status");
                    }
                    executeStatus();
                    return;
                }
                if (key0.equalsIgnoreCase("transform")) {
                    final MathTransform old = transform;
                    transform = (MathTransform) fromDefinition(value, MathTransform.class);
                    sourceCRS = null;
                    targetCRS = null;
                    firePropertyChange("transform", old, transform);
                    return;
                }
            } else {
                /*
                 * 2-keywords instructions
                 * Example: source crs, target crs, set <name>
                 */
                final String key1 = keywords.nextToken();
                if (!keywords.hasMoreTokens()) {
                    if (key0.equalsIgnoreCase("set")) {
                        addDefinition(key1, value);
                        return;
                    }
                    if (key1.equalsIgnoreCase("crs")) {
                        if (key0.equalsIgnoreCase("source")) {
                            final CoordinateReferenceSystem old = sourceCRS;
                            sourceCRS = (CoordinateReferenceSystem)
                                        fromDefinition(value, CoordinateReferenceSystem.class);
                            transform = null;
                            firePropertyChange("source CRS", old, sourceCRS);
                            return;
                        }
                        if (key0.equalsIgnoreCase("target")) {
                            final CoordinateReferenceSystem old = targetCRS;
                            targetCRS = (CoordinateReferenceSystem)
                                        fromDefinition(value, CoordinateReferenceSystem.class);
                            transform = null;
                            firePropertyChange("target CRS", old, targetCRS);
                            return;
                        }
                    }
                    if (key1.equalsIgnoreCase("pt")) {
                        if (key0.equalsIgnoreCase("source")) {
                            executeSourcePt(value);
                            return;
                        }
                        if (key0.equalsIgnoreCase("target")) {
                            executeTargetPt(value);
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
     * Execute the "<code>status</code>" instruction.
     * This instruction print the current console state.
     *
     * @todo Localize
     */
    private final void executeStatus() throws FactoryException, IOException {
        /*
         * Format the list of pre-defined objects first.
         */
        if (true) {
            String separator = "Predefined objects: ";
            for (final Iterator it=getDefinitionNames().iterator(); it.hasNext();) {
                out.write(separator);
                out.write(String.valueOf(it.next()));
                separator = ", ";
            }
            out.write(lineSeparator);
        }
        /*
         * Format source and target CRS, if any.
         */
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
     * Execute the "source pt" instruction.
     *
     * @param  value The value on the right side of <code>=</code>.
     * @throws ParseException if the point can't be parsed.
     * @throws TransformException if the transformation can't be performed.
     */
    private void executeSourcePt(final String value)
            throws ParseException, FactoryException, TransformException, IOException
    {
        final double[] vector = parseVector(value);
        final DirectPosition old = sourcePosition;
        sourcePosition = new GeneralDirectPosition(vector);
        update();
        if (transform != null) {
            targetPosition = transform.transform(sourcePosition, null);
            printVector(vector);
            out.write(" --> ");
            printVector(targetPosition.getCoordinates());
            out.write(lineSeparator);
        } else {
            firePropertyChange("source pt", old, sourcePosition);
        }
    }
    
    /**
     * Execute the "target pt" instruction.
     *
     * @param  value The value on the right side of <code>=</code>.
     * @throws ParseException if the point can't be parsed.
     * @throws TransformException if the transformation can't be performed.
     */
    private void executeTargetPt(final String value)
            throws ParseException, FactoryException, TransformException, IOException
    {
        final double[] vector = parseVector(value);
        final DirectPosition old = targetPosition;
        targetPosition = new GeneralDirectPosition(vector);
        update();
        if (transform != null) {
            sourcePosition = transform.inverse().transform(targetPosition, null);
            printVector(sourcePosition.getCoordinates());
            out.write(" <-- ");
            printVector(vector);
            out.write(lineSeparator);
        } else {
            firePropertyChange("target pt", old, targetPosition);
        }
    }




    ///////////////////////////////////////////////////////////
    ////////                                           ////////
    ////////        H E L P E R   M E T H O D S        ////////
    ////////                                           ////////
    ///////////////////////////////////////////////////////////

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
     * Print the specified vector to the output stream.
     *
     * @param  values The vector to print.
     * @throws IOException if an error occured while writting to the output stream.
     */
    private void printVector(final double[] values) throws IOException {
        out.write('(');
        for (int i=0; i<values.length; i++) {
            if (i != 0) {
                out.write(numberSeparator);
                out.write(' ');
            }
            out.write(numberFormat.format(values[i]));
        }
        out.write(')');
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
}
