/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2002, Institut de Recherche pour le Développement
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

// Formatting
import java.util.Locale;
import java.text.Format;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.ParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

// OpenGIS dependencies
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.GeneralParameterValue;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Base class for <cite>Well Know Text</cite> (WKT) parser.
 *
 * @version $Id$
 * @author Remi Eve
 * @author Martin Desruisseaux
 *
 * @see <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html">Well Know Text specification</A>
 * @see <A HREF="http://gdal.velocet.ca/~warmerda/wktproblems.html">OGC WKT Coordinate System Issues</A>
 */
public abstract class AbstractParser extends Format {
    /**
     * A formatter using the same symbols than this parser.
     * Will be created by the {@link #format} method only when first needed.
     */
    private transient Formatter formatter;

    /**
     * The symbols to use for parsing WKT.
     */
    final Symbols symbols;

    /**
     * The object to use for formatting numbers.
     */
    final NumberFormat numberFormat;
    
    /**
     * Construct a parser using the specified set of symbols.
     */
    public AbstractParser(final Symbols symbols) {
        this.symbols      = symbols;
        this.numberFormat = (NumberFormat) symbols.numberFormat.clone();
    }

    /**
     * Returns a tree of {@link Element} for the specified text.
     *
     * @param  text       The text to parse.
     * @param  position   In input, the position where to start parsing from.
     *                    In output, the first character after the separator.
     */
    protected final Element getTree(final String text, final ParsePosition position)
            throws ParseException
    {
        return new Element(new Element(this, text, position));
    }


    /**
     * Parses the next element in the specified <cite>Well Know Text</cite> (WKT) tree.
     *
     * @param  element The element to be parsed.
     * @return The object.
     * @throws ParseException if the element can't be parsed.
     */
    protected abstract Object parse(final Element element) throws ParseException;

    /**
     * Parses a <cite>Well Know Text</cite> (WKT).
     *
     * @param  text The text to be parsed.
     * @return The object.
     * @throws ParseException if the string can't be parsed.
     */
    public final Object parseObject(final String text) throws ParseException {
        final Element element = getTree(text, new ParsePosition(0));
        final Object object = parse(element);
        element.close();
        return object;
    }
    
    /**
     * Parses a <cite>Well Know Text</cite> (WKT).
     *
     * @param  text The text to be parsed.
     * @param  position The position to start parsing from.
     * @return The object.
     */
    public final Object parseObject(final String text, final ParsePosition position) {
        final int origin = position.getIndex();
        try {
            return parse(getTree(text, position));
        } catch (ParseException exception) {
            position.setIndex(origin);
            if (position.getErrorIndex() < origin) {
                position.setErrorIndex(exception.getErrorOffset());
            }
            return null;
        }
    }

    /**
     * Format the specified object as a Well Know Text.
     * Formatting will uses the same set of symbols than the one used for parsing.
     */
    public StringBuffer format(final Object        object,
                               final StringBuffer  toAppendTo,
                               final FieldPosition pos)
    {
        if (formatter == null) {
            formatter = new Formatter(symbols, numberFormat);
        }
        try {
            formatter.buffer = toAppendTo;
            formatter.bufferBase = toAppendTo.length();
            if (object instanceof MathTransform) {
                formatter.append((MathTransform) object);
            } else if (object instanceof GeneralParameterValue) {
                // Special processing for parameter values, which is formatted
                // directly in 'Formatter'. Note that in GeoAPI, this interface
                // doesn't share the same parent interface than other interfaces.
                formatter.append((GeneralParameterValue) object);
            } else {
                formatter.append((IdentifiedObject) object);
            }
            return toAppendTo;
        } finally {
            formatter.buffer = null;
            formatter.clear();
        }
    }     

    /**
     * Read WKT strings from an input stream and reformat them to the specified
     * output stream. WKT strings are read until the the end-of-stream, or until
     * an unparsable WKT has been hit. In this later case, an error message is
     * formatted to the specified error stream.
     *
     * @param  in  The input stream.
     * @param  out The output stream.
     * @param  err The error stream.
     * @throws IOException if an error occured while reading from the input stream
     *         or writting to the output stream.
     */
    public void reformat(final BufferedReader in, final Writer out, final PrintWriter err)
            throws IOException
    {
        final String lineSeparator = System.getProperty("line.separator", "\n");
        String line = null;
        try {
            while ((line=in.readLine()) != null) {
                if ((line=line.trim()).length() != 0) {
                    out.write(lineSeparator);
                    out.write(format(parseObject(line)));
                    out.write(lineSeparator);
                    out.write(lineSeparator);
                    out.flush();
                }
            }
        } catch (ParseException exception) {
            err.println(exception.getLocalizedMessage());
            if (line != null) {
                line = line.replace('\r', ' ').replace('\n', ' ');
                final int WINDOW_WIDTH    = 80; // Arbitrary value.
                int           stop        = line.length();
                int           errorOffset = exception.getErrorOffset();
                int           base        = errorOffset-WINDOW_WIDTH/2;
                final int     baseMax     = stop-WINDOW_WIDTH;
                final boolean hasTrailing = (Math.max(base,0) < baseMax);
                if (!hasTrailing) {
                    base = baseMax;
                }
                if (base < 0) {
                    base = 0;
                }
                stop = Math.min(stop, base+WINDOW_WIDTH);
                if (hasTrailing) {
                    stop -= 3;
                }
                if (base != 0) {
                    err.print("...");
                    errorOffset += 3;
                    base += 3;
                }
                err.print(line.substring(base, stop));
                if (hasTrailing) {
                    err.println("...");
                } else {
                    err.println();
                }
                err.print(Utilities.spaces(errorOffset-base));
                err.println('^');
            }
        } catch (InvalidParameterValueException exception) {
            err.print(Resources.format(ResourceKeys.ERROR_IN_$1, exception.getParameterName()));
            err.print(' ');
            err.println(exception.getLocalizedMessage());
        }
    }
}
