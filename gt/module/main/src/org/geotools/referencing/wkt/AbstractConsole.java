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
package org.geotools.referencing.wkt;

// J2SE dependencies
import java.util.Set;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Collections;
import java.text.ParseException;
import java.text.Format;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.LineNumberReader;

// OpenGIS dependencies
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.operation.MathTransform;       // For javadoc
import org.opengis.referencing.crs.CoordinateReferenceSystem; // For javadoc

// Geotools dependencies
import org.geotools.io.TableWriter;
import org.geotools.resources.XArray;
import org.geotools.resources.Utilities;
import org.geotools.resources.Arguments;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Base class for application performing operations on WKT objects from the command line.
 * Instructions are usually read from the {@linkplain System#in standard input stream} and
 * results sent to the {@linkplain System#out standard output stream}, but those streams can
 * be redirected. The set of allowed instructions depends on the subclass used. This base class
 * provides common services related to WKT, especially managing a set of shortcuts for some
 * predefined WKT (the <cite>definitions</cite>).
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class AbstractConsole implements Runnable {
    /**
     * The input stream, usually the {@linkplain System#in standard one}.
     */
    protected final LineNumberReader in;

    /**
     * The output stream, usually the {@linkplain System#out standard one}.
     */
    protected final Writer out;

    /**
     * The error stream, usually the {@linkplain System#err standard one}.
     */
    protected final PrintWriter err;

    /**
     * The line separator, usually the system default.
     */
    protected final String lineSeparator;

    /**
     * The WKT parser, usually a {@link Parser} object.
     */
    protected final Format parser;

    /**
     * The command-line prompt.
     */
    private String prompt = "crs>";
    
    /**
     * The set of objects defined during the execution of this console. Keys are
     * {@link String} objects on the left side of <code>=</code>, while value are
     * {@link Definition} objects built from the right side of <code>=</code>.
     */
    private final Map definitions = new TreeMap();

    /**
     * The unmodifiable set of keys in the {@link #definitions} map. Will be constructed
     * only when first needed.
     */
    private transient Set names;

    /**
     * The last line read, or <code>null</code> if none.
     */
    private transient String line;

    /**
     * A linked list of informations about the index changes induced by a replacements in the
     * {@link #line} string. Each {@link Replacement} object contains information about a single
     * replacement performed by {@link #substitute}. Those informations are used by
     * {@link #parseObject} in order to adjust {@linkplain ParseException#getErrorOffset
     * error offset} in case of failure.
     */
    private transient Replacement replacements;

    /**
     * Set to <code>true</code> if {@link #stop()} was invoked.
     */
    private transient volatile boolean stop;
    
    /**
     * Creates a new console instance using {@linkplain System#in standard input stream},
     * {@linkplain System#out standard output stream}, {@linkplain System#err error output stream}
     * and the system default line separator.
     *
     * @param parser The WKT parser, usually a {@link Parser} object.
     */
    public AbstractConsole(final Format parser) {
        this(parser, new LineNumberReader(Arguments.getReader(System.in)));
    }
    
    /**
     * Creates a new console instance using the specified input stream.
     *
     * @param parser The WKT parser, usually a {@link Parser} object.
     * @param in  The input stream.
     */
    public AbstractConsole(final Format        parser,
                           final LineNumberReader  in)
    {
        this(parser, in, Arguments.getWriter(System.out),
         new PrintWriter(Arguments.getWriter(System.err), true),
                    System.getProperty("line.separator", "\n"));
    }
    
    /**
     * Creates a new console instance using the specified streams and line separator.
     *
     * @param parser The WKT parser, usually a {@link Parser} object.
     * @param in  The input stream.
     * @param out The output stream.
     * @param err The error stream.
     * @param lineSeparator The line separator.
     */
    public AbstractConsole(final Format        parser,
                           final LineNumberReader  in,
                           final Writer           out,
                           final PrintWriter      err,
                           final String lineSeparator)
    {
        this.parser        = parser;
        this.in            = in;
        this.out           = out;
        this.err           = err;
        this.lineSeparator = lineSeparator;
    }

    /**
     * Returns <code>true</code> if the specified text is a valid identifier.
     * Such identifier can be used of the left side of <code>=</code> in
     * <code>SET</code> instructions.
     */
    private static boolean isIdentifier(final String text) {
        for (int i=text.length(); --i>=0;) {
            if (!Character.isJavaIdentifierPart(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns an unmodifiable set which contains all definition's names given to the
     * <code>{@linkplain #addDefinition addDefinition}(name, ...)</code> method. The
     * elements in this set are sorted in alphabetical order.
     */
    public Set getDefinitionNames() {
        if (names == null) {
            names = Collections.unmodifiableSet(definitions.keySet());
        }
        return names;
    }

    /**
     * Format to the {@linkplain #out output stream} a table of all definitions.
     * The content of this table is inferred from the values given to the
     * {@link #addDefinition} method.
     *
     * @throws IOException if an error occured while writting to the output stream.
     * @todo Localize table header.
     */
    public void printDefinitions() throws IOException {
        final TableWriter table = new TableWriter(out, " \u2502 ");
        table.setMultiLinesCells(true);
        table.writeHorizontalSeparator();
        table.write("Name");
        table.nextColumn();
        table.write("Type");
        table.nextColumn();
        table.write("Description");
        table.nextLine();
        table.writeHorizontalSeparator();
        for (final Iterator it=definitions.entrySet().iterator(); it.hasNext();) {
            final Map.Entry entry = (Map.Entry) it.next();
            final Object   object = ((Definition) entry.getValue()).asObject;
            table.write(String.valueOf(entry.getKey()));
            table.nextColumn();
            table.write(Utilities.getShortClassName(object));
            table.nextColumn();
            if (object instanceof IdentifiedObject) {
                table.write(((IdentifiedObject) object).getName().getCode());
            }
            table.nextLine();
        }
        table.writeHorizontalSeparator();
        table.flush();
    }

    /**
     * Returns the object associated with the specified definition.
     * The definition can be any of the following:
     * <BR>
     * <UL>
     *   <LI>A shortcut's name declared in some previous call to
     *       <code>{@linkplain #addDefinition addDefinition}(name, ...)</code>.</LI>
     *   <LI>A Well Know Text, which may contains itself shortcuts declared in
     *       previous call to <code>addDefinition</code>.</LI>
     *   <LI>Any services provided by subclasses. For example a subclass way recognize
     *       some authority code like <code>EPSG:6326</code>.</LI>
     * </UL>
     *
     * @param  definition The definition, as a name, a WKT to parse, or an authority code.
     * @param  type The expected type for the object to be parsed (usually a
     *         <code>{@linkplain CoordinateReferenceSystem}.class</code> or
     *         <code>{@linkplain MathTransform}.class</code>).
     * @return The object.
     * @throws ParseException if parsing the specified WKT failed.
     * @throws FactoryException if the object is not of the expected type.
     */
    public Object fromDefinition(String definition, final Class type)
            throws ParseException, FactoryException
    {
        Object value;
        final Definition def = (Definition) definitions.get(definition);
        if (def != null) {
            value = def.asObject;
            if (type.isAssignableFrom(value.getClass())) {
                return value;
            }
        } else if (!isIdentifier(definition)) {
            /*
             * The specified string was not found in the definitions map. Try to parse it as a
             * WKT, but only if it contains more than a single word. This later condition exists
             * only in order to produces a more accurate error message (WKT parsing of a single
             * word is garantee to fail). In any case, the definitions map is not updated since
             * this method is not invoked from the SET instruction.
             */
            definition = substitute (definition);
            value      = parseObject(definition);
            final Class actualType = value.getClass();
            if (type.isAssignableFrom(actualType)) {
                return value;
            }
            throw new FactoryException(
                      Resources.format(ResourceKeys.ERROR_ILLEGAL_CLASS_$2,
                      Utilities.getShortName(actualType), Utilities.getShortName(type)));
        }
        throw new NoSuchIdentifierException(
                  Resources.format(ResourceKeys.ERROR_NO_SUCH_AUTHORITY_CODE_$2,
                  Utilities.getShortName(type), definition), definition);
    }

    /**
     * Add a shortcut for a Well Know Text (WKT). The specified WKT can contains
     * itself other shortcuts defined in some previous calls to <code>addDefinition</code>.
     *
     * @param  name The name for the shortcut.
     * @param  value The Well Know Text (WKT) represented by the name.
     * @throws ParseException if the name is invalid, or the WKT can't be parsed.
     * @throws IOException if an error occured while writting a report to
     *         the {@linkplain #out output stream}.
     *
     * @todo Localize error messages.
     */
    public void addDefinition(final String name, String value)
            throws ParseException, IOException
    {
        if (value==null || value.trim().length()==0) {
            throw new ParseException("Missing WKT definition.", line.length());
        }
        if (!isIdentifier(name)) {
            throw new ParseException("\""+name+"\" is not a valid identifier.",
                      (line!=null) ? Math.max(line.indexOf(name), 0) : 0);
        }
        value = substitute(value);
        final Definition newDef = new Definition(value, parseObject(value));
        final Definition oldDef = (Definition) definitions.put(name, newDef);
    }

    /**
     * Load all definitions from the specified stream. Definitions are key-value pairs
     * in the form <code>name = wkt</code> (without the <code>SET</code> keyword). The
     * result is the same than invoking the <code>SET</code> instruction for each line
     * in the specified stream. This method is used for loading predefined objects like
     * the database used by {@link org.geotools.referencing.PropertyAuthorityFactory}.
     *
     * @param  in The input stream.
     * @throws IOException if an input operation failed.
     * @throws ParseException if a well know text (WKT) can't be parsed.
     */
    public void loadDefinitions(final LineNumberReader in) throws IOException, ParseException {
        while ((line=readLine(in)) != null) {
            String name=line, value=null;
            final int i = line.indexOf('=');
            if (i >= 0) {
                name  = line.substring(0,i).trim();
                value = line.substring(i+1).trim();
            }
            addDefinition(name, value);
        }
    }

    /**
     * Read the next line from the specified input stream. Empty lines
     * and comment lines are skipped. If there is no more line to read,
     * then this method returns <code>null</code>.
     *
     * @param  in The input stream to read from.
     * @return The next non-empty and non-commented line, or <code>null</code> if none.
     * @throws IOException if the reading failed.
     */
    private static String readLine(final LineNumberReader in) throws IOException {
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
            break;
        }
        return line;
    }

    /**
     * Process instructions from the {@linkplain #in input stream} specified at construction
     * time. All lines are read until the end of stream (<code>[Ctrl-Z]</code> for input from
     * the keyboard), or until {@link #stop()} is invoked. Non-empty and non-comment lines are
     * given to the {@link #execute} method. Errors are catched and printed to the
     * {@linkplain #err error stream}.
     */
    public void run() {
        try {
            while (!stop) {
                if (prompt != null) {
                    out.write(prompt);
                }
                out.flush();
                line = readLine(in);
                if (line == null) {
                    break;
                }
                try {
                    execute(line);
                } catch (Exception exception) {
                    reportError(exception);
                }
            }
            out.flush();
            stop = false;
        } catch (IOException exception) {
            reportError(exception);
        }
    }

    /**
     * Execute the specified instruction.
     *
     * @param  instruction The instruction to execute.
     * @throws Exception if the instruction failed.
     */
    protected abstract void execute(String instruction) throws Exception;

    /**
     * Stops the {@link #run} method. This method can been invoked from any thread.
     * If a line is in process, it will be finished before the {@link #run} method
     * stops.
     */
    public void stop() {
        this.stop = true;
    }

    /**
     * For every definition key found in the given string, substitute
     * the key by its value. The replacement will not be performed if
     * the key was found between two quotation marks.
     *
     * @param  text The string to process.
     * @return The string with all keys replaced by their values.
     */
    private String substitute(final String text) {
        Replacement last;
        replacements = last = new Replacement(0, 0, -Math.max(0, line.lastIndexOf(text)));
        StringBuffer buffer = null;
        for (final Iterator it=definitions.entrySet().iterator(); it.hasNext();) {
            final Map.Entry entry = (Map.Entry)  it.next();
            final String     name = (String)     entry.getKey();
            final Definition def  = (Definition) entry.getValue();
            int index = (buffer!=null) ? buffer.indexOf(name) : text.indexOf(name);
            while (index >= 0) {
                /*
                 * An occurence of the text to substitute was found. First, make sure
                 * that the occurence found is a full word  (e.g. if the occurence to
                 * search is "WGS84", do not accept "TOWGS84").
                 */
                final int upper = index + name.length();
                final CharSequence cs = (buffer!=null) ? (CharSequence)buffer : (CharSequence)text;
                if ((index==0           || !Character.isJavaIdentifierPart(cs.charAt(index-1))) &&
                    (upper==cs.length() || !Character.isJavaIdentifierPart(cs.charAt(upper))))
                {
                    /*
                     * Count the number of quotes before the text to substitute. If this
                     * number is odd, then the text is between quotes and should not be
                     * substituted.
                     */
                    int count = 0;
                    for (int scan=index; --scan>=0;) {
                        scan = (buffer!=null) ? buffer.lastIndexOf("\"", scan)
                                              :   text.lastIndexOf( '"', scan);
                        if (scan < 0) {
                            break;
                        }
                        count++;
                    }
                    if ((count & 1) == 0) {
                        /*
                         * An even number of quotes was found before the text to substitute.
                         * Performs the substitution and keep trace of this replacement in a
                         * chained list of 'Replacement' objects.
                         */
                        if (buffer == null) {
                            buffer = new StringBuffer(text);
                            assert buffer.indexOf(name, index) == index;
                        }
                        final String value = def.asString;
                        buffer.replace(index, upper, value);
                        final int change = value.length() - name.length();
                        last = last.next = new Replacement(index, index+value.length(), change);
                        index = buffer.indexOf(name, index + change);
                        // Note: it is okay to skip the text we just replaced, since the
                        //       'definitions' map do not contains nested definitions.
                        continue;
                    }
                }
                /*
                 * The substitution was not performed because the text found was not a word,
                 * or was between quotes. Search the next occurence.
                 */
                index += name.length();
                index = (buffer!=null) ? buffer.indexOf(name, index)
                                       : text  .indexOf(name, index);
            }
        }
        return (buffer!=null) ? buffer.toString() : text;
    }

    /**
     * Parses a WKT. This method delegates the work to the {@link #parser}, but
     * catch the exception in case of failure. The exception is rethrown with the
     * {@linkplain ParseException#getErrorIndex error index} adjusted in order to
     * point to the character in the {@linkplain #line original line}.
     *
     * @param  value The WKT to parse.
     * @return The object.
     * @throws ParseException if the parsing failed.
     */
    private Object parseObject(final String value) throws ParseException {
        try {
            return parser.parseObject(value);
        } catch (ParseException exception) {
            int shift = 0;
            int errorOffset = exception.getErrorOffset();
            for (Replacement r=replacements; r!=null; r=r.next) {
                if (errorOffset < r.lower) {
                    break;
                }
                if (errorOffset < r.upper) {
                    errorOffset = r.lower;
                    break;
                }
                shift += r.shift;
            }
            final ParseException adjusted = new ParseException(exception.getLocalizedMessage(),
                                                               errorOffset - shift);
            adjusted.setStackTrace(exception.getStackTrace());
            adjusted.initCause(exception.getCause());
            throw adjusted;
        }
    }

    /**
     * Returns the command-line prompt, or <code>null</code> if there is none.
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * Set the command-line prompt, or <code>null</code> for none.
     */
    public void setPrompt(final String prompt) {
        this.prompt = prompt;
    }

    /**
     * Print an exception message to the {@linkplain System#err standard error stream}.
     * The error message includes the line number, and the column where the failure
     * occured in the exception is an instance of {@link ParseException}.
     *
     * @param exception The exception to report.
     * @todo Localize
     */
    protected void reportError(final Exception exception) {
        try {
            out.flush();
        } catch (IOException ignore) {
            Utilities.unexpectedException("org.geotools.referencing.wkt",
                                          "AbstractConsole", "reportError", ignore);
        }
        err.print(Utilities.getShortClassName(exception));
        err.print(" at line ");
        err.print(in.getLineNumber());
        final String message = exception.getLocalizedMessage();
        if (message != null) {
            err.print(": ");
            err.print(message);
        }
        err.println();
        if (line!=null && exception instanceof ParseException) {
            AbstractParser.reportError(err, line, ((ParseException)exception).getErrorOffset());
        }
    }

    /**
     * An entry for the {@link Console#definitions} map. This entry contains a definition
     * as a well know text (WKT), and the parsed value for this WKT (usually a
     * {@linkplain CoordinateReferenceSystem} or a {@linkplain MathTransform} object).
     */
    private static final class Definition {
        /**
         * The definition as a string. This string should not contains anymore
         * shortcut to substitute by an other WKT (i.e. compound definitions
         * must be resolved before to construct a <code>Definition</code> object).
         */
        public final String asString;

        /**
         * The definition as an object (usually a {@linkplain CoordinateReferenceSystem}
         * or a {@linkplain MathTransform} object).
         */
        public final Object asObject;

        /**
         * Constructs a new definition.
         */
        public Definition(final String asString, final Object asObject) {
            this.asString = asString;
            this.asObject = asObject;
        }
    }

    /**
     * Contains informations about the index changes induced by a replacement in a string.
     * All index refer to the string <strong>after</strong> the replacement. The substring
     * at index between {@link #lower} inclusive and {@link #upper} exclusive is the replacement
     * string. The {@link #shift} is the difference between the replacement substring length and
     * the replaced substring length.
     */
    private static final class Replacement {
        /** The lower index in the target string, inclusive. */ public final int  lower;
        /** The upper index in the target string, exclusive. */ public final int  upper;
        /** The shift from source string to target string.   */ public final int  shift;
        /** The next element in the linked list.             */ public Replacement next;

        /** Constructs a new index shift initialized with the given values. */
        public Replacement(final int lower, final int upper, final int shift) {
            this.lower = lower;
            this.upper = upper;
            this.shift = shift;
        }

        /**
         * Returns a string representation for debugging purpose.
         */
        public String toString() {
            final StringBuffer buffer = new StringBuffer();
            for (Replacement r=this; r!=null; r=r.next) {
                if (r != this) {
                    buffer.append(", ");
                }
                buffer.append('[')
                      .append(r.lower)
                      .append("..")
                      .append(r.upper)
                      .append("] \u2192 ")
                      .append(r.shift);
            }
            return buffer.toString();
        }
    }
}
