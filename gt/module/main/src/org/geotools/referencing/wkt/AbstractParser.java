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

// OpenGIS dependencies
import org.opengis.referencing.IdentifiedObject;
import org.opengis.parameter.GeneralParameterValue;


/**
 * The base class for <cite>Well Know Text</cite> (WKT) parser and formatter.
 *
 * @version $Id$
 * @author Remi Eve
 * @author Martin Desruisseaux
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
            if (object instanceof GeneralParameterValue) {
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
        }
    }     
}
