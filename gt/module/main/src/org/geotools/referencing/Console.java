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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing;

// J2SE dependencies
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;
import java.text.ParseException;
import java.text.NumberFormat;
import java.io.Writer;
import java.io.IOException;
import java.io.LineNumberReader;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.spatialschema.geometry.DirectPosition;

// Geotools dependencies
import org.geotools.io.TableWriter;
import org.geotools.resources.Arguments;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.referencing.wkt.Parser;
import org.geotools.geometry.GeneralDirectPosition;


/**
 * A console for executing CRS operations from the command line.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Console implements Runnable {
    /**
     * The standard input stream.
     */
    private final LineNumberReader in;

    /**
     * The standard output stream.
     */
    private final Writer out;

    /**
     * The error output stream.
     */
    private final Writer err;

    /**
     * The line separator.
     */
    private final String lineSeparator = System.getProperty("line.separator", "\n");


    /**
     * The number format to use for reading coordinate points.
     */
    private final NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);

    /**
     * The WKT parser using default factories.
     */
    private final Parser parser = new Parser();

    /**
     * The coordinate operation factory to use.
     */
    private final CoordinateOperationFactory factory =
                  FactoryFinder.getCoordinateOperationFactory();
    
    /**
     * The set of object defined during the execution of this console. Keys are
     * {@link String} objects, while values are {@link CoordinateReferenceSystem}
     * or {@link MathTransform} objects.
     */
    private final Map definitions = new HashMap();

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
     * Creates a new instance of console.
     *
     * @param in  The standard input stream.
     * @param out The standard output stream.
     * @param err The error output stream.
     */
    public Console(final LineNumberReader in, final Writer out, final Writer err) {
        this.in  = in;
        this.out = out;
        this.err = err;
    }

    /**
     * Returns a coordinate reference system for the specified name. The object
     * must has been previously defined with a call to {@link #setDefinition}.
     *
     * @param  name The identifier name.
     * @throws NoSuchIdentifierException if no CRS is registered under that name.
     */
    private CoordinateReferenceSystem getCoordinateReferenceSystem(final String name)
            throws NoSuchIdentifierException
    {
        final Object crs = definitions.get(name);
        if (crs instanceof CoordinateReferenceSystem) {
            return (CoordinateReferenceSystem) crs;
        }
        throw new NoSuchIdentifierException(
                  Resources.format(ResourceKeys.ERROR_NO_SUCH_AUTHORITY_CODE_$2,
                  Utilities.getShortName(CoordinateReferenceSystem.class), name), name);
    }

    /**
     * Returns a math transform for the specified name. The object
     * must has been previously defined with a call to {@link #setDefinition}.
     *
     * @param  name The identifier name.
     * @throws NoSuchIdentifierException if no math transform is registered under that name.
     */
    private MathTransform getMathTransform(final String name)
            throws NoSuchIdentifierException
    {
        final Object crs = definitions.get(name);
        if (crs instanceof CoordinateReferenceSystem) {
            return (MathTransform) crs;
        }
        throw new NoSuchIdentifierException(
                  Resources.format(ResourceKeys.ERROR_NO_SUCH_AUTHORITY_CODE_$2,
                  Utilities.getShortName(MathTransform.class), name), name);
    }

    /**
     * If the specified string start with <code>"set"</code>, then add its
     * value to the {@link #definitions} map and returns <code>true</code>.
     * Otherwise, returns <code>false</code>.
     *
     * @param  text The string to parse.
     * @return <code>true</code> if it was a definition string,
     *         or <code>false</code> otherwise.
     * @throws ParseException if the string can't be parsed.
     */
    private boolean setDefinition(String text) throws ParseException, IOException {
        /*
         * If the string is in the form "set name = value",
         * then separate the name and the value parts.
         */
        final String SET = "set";
        if (!text.regionMatches(true, 0, SET, 0, SET.length())) {
            return false;
        }
        text = text.substring(SET.length());
        int lower,upper,index;
        int length = text.length();
        index=0;     while (index<length && Character.isSpaceChar         (text.charAt(index))) index++;
        lower=index; while (index<length && Character.isJavaIdentifierPart(text.charAt(index))) index++;
        upper=index; while (index<length && Character.isSpaceChar         (text.charAt(index))) index++;
        if (lower==upper || index==length || text.charAt(index)!='=') {
            // TODO: localize
            throw new ParseException("SET must be in the form \"name = value\".", 0);
        }
        String name  = text.substring(lower,upper);
        String value = text.substring(index+1).trim();
        final Object object = parser.parseObject(value);
        final boolean updated = (definitions.put(name, object) != null);
        // TODO: localize
        out.write((updated ? "Updated" : "Added")+" definition for \""+name+"\".");
        out.write(lineSeparator);
        out.flush();
        return true;
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
    private static double[] parseVector(String text) throws ParseException {
        text = removeDelimitors(text, '(', ')');
        final StringTokenizer st = new StringTokenizer(text, ",");
        final double[]    values = new double[st.countTokens()];
        for (int i=0; i<values.length; i++) {
            values[i] = Double.parseDouble(st.nextToken());
        }
        return values;
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
     * Update the internal state after a change, before to apply transformation.
     */
    private final void update() throws FactoryException {
        if (transform==null && sourceCRS!=null && targetCRS!=null) {
            transform = factory.createOperation(sourceCRS, targetCRS).getMathTransform();
        }
    }

    /**
     * Print the current console state.
     */
    private final void printStatus() throws FactoryException, IOException {
        boolean hasOutput = false;
        final TableWriter table = new TableWriter(out, " \u2502 ");
        char separator = '\u2500';
        if (sourceCRS!=null || targetCRS!=null) {
            table.setMultiLinesCells(true);
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
            hasOutput = true;
        }
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
            hasOutput = true;
        }
        table.writeHorizontalSeparator();
        table.flush();
        if (!hasOutput) {
            out.write("No CRS or transform specified.");
            out.write(lineSeparator);
            out.flush();
        }
    }
    
    /**
     * Run an instruction. Instruction may be any of the following lines
     * (values listed here are just examples):
     * <pre>
     *   source crs     = _Wgs84NE_
     *   target crs     = _Wgs84SW_
     *   test_tolerance = 1e-6
     *   source pt      = (1, 2)
     *   target pt      = (-1, -2)
     * </pre>
     *
     * or
     * 
     * <pre>
     *   transform      = _mt_merc1_
     *   test_tolerance = 1e-6
     *   source pt      = (1, 2)
     *   target pt      = (-1, -2)
     * </pre>
     *
     * The "<code>pt_target</code>" instruction triggers the computation.
     *
     * @param  text The instruction to parse.
     * @throws ParseException if the instruction can't be parsed.
     * @throws NoSuchIdentifierException if the instruction uses an undefined variable.
     * @throws TransformException if the transformation can't be run.
     */
    private void runInstruction(final String text)
            throws ParseException, FactoryException, TransformException, IOException
    {
        final StringTokenizer st = new StringTokenizer(text, "=");
        switch (st.countTokens()) {
            case 1: {
                final String name  = st.nextToken().trim();
                if (name.equalsIgnoreCase("status")) {
                    printStatus();
                    return;
                }
                break;
            }
            case 2: {
                final String name  = st.nextToken().trim();
                final String value = st.nextToken().trim();
                if (name.equalsIgnoreCase("source crs")) {
                    sourceCRS = getCoordinateReferenceSystem(value);
                    transform = null;
                    return;
                }
                if (name.equalsIgnoreCase("target crs")) {
                    targetCRS = getCoordinateReferenceSystem(value);
                    transform = null;
                    return;
                }
                if (name.equalsIgnoreCase("transform")) {
                    transform = getMathTransform(value);
                    sourceCRS = null;
                    targetCRS = null;
                    return;
                }
                if (name.equalsIgnoreCase("source pt")) {
                    sourcePosition = new GeneralDirectPosition(parseVector(value));
                    out.write("source pt = ");
                    out.write(String.valueOf(sourcePosition));
                    out.write(lineSeparator);
                    update();
                    if (transform != null) {
                        targetPosition = transform.transform(sourcePosition, null);
                        out.write("target pt = ");
                        out.write(String.valueOf(targetPosition));
                        out.write(lineSeparator);
                    }
                    out.flush();
                    return;
                }
                break;
            }
        }
        throw new ParseException("Illegal instruction: "+text, 0);
    }

    /**
     * Process instructions from the input stream specified at construction time.
     * All lines are read until the end of file. This method may be invoked instead
     * of {@link #run} if the processing must stop at the first error.
     *
     * @throws IOException if an I/O operation failed while reading from the input
     *         stream or writting to the output stream.
     * @throws ParseException if a line can't be parsed.
     * @throws If a transform can't be created.
     * @throws TransformException if a transform failed.
     */
    public void process() throws IOException, ParseException,
                                 FactoryException, TransformException
    {
        String line;
        while ((line=in.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) {
                // Ignore empty lines.
                continue;
            }
            if (line.startsWith("//")) {
                // Ignore comment lines.
                continue;
            }
            if (setDefinition(line)) {
                // Definition line are processed by 'setDefinition'.
                continue;
            }
            runInstruction(line);
            out.flush();
        }
    }

    /**
     * Process instructions from the input stream specified at construction time.
     * All lines are read until the end of file. Errors are catched and printed
     * to the error output stream.
     */
    public void run() {
        while (true) {
            try {
                process();
                break;
            } catch (Exception exception) {
                try {
                    out.flush();
                    err.write(Utilities.getShortClassName(exception));
                    final String message = exception.getLocalizedMessage();
                    if (message != null) {
                        err.write(": ");
                        err.write(message);
                    }
                    err.write(lineSeparator);
                    err.flush();
                } catch (IOException ignore) {
                    System.err.println(ignore.getLocalizedMessage());
                }
                continue;
            }
        }
    }

    /**
     * Run the console.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final Arguments arguments = new Arguments(args);
        args = arguments.getRemainingArguments(0);
        final Console console = new Console(
              new LineNumberReader(Arguments.getReader(System.in)),
                                   Arguments.getWriter(System.out),
                                   Arguments.getWriter(System.err));
        console.run();
    }
}
