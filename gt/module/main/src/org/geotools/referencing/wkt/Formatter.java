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
package org.geotools.referencing.wkt;

// J2SE dependencies and extensions
import java.util.Locale;
import java.util.Iterator;
import java.lang.reflect.Array;
import java.text.FieldPosition;
import java.text.NumberFormat;
import javax.units.NonSI;
import javax.units.SI;
import javax.units.Unit;
import javax.units.UnitFormat;

// OpenGIS dependencies
import org.opengis.util.CodeList;
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.operation.MathTransform;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.Arguments;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Format {@link Formattable} objects as
 * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
 * Known Text</cite> (WKT)</A>.
 *
 * A formatter is constructed with a specified set of symbols.
 * The {@linkplain Locale locale} associated with the symbols is used for querying
 * {@linkplain org.opengis.metadata.citation.Citation#getTitle authority titles}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html">Well Know Text specification</A>
 * @see <A HREF="http://gdal.velocet.ca/~warmerda/wktproblems.html">OGC WKT Coordinate System Issues</A>
 */
public class Formatter {
    /**
     * The symbols to use for this formatter.
     */
    private final Symbols symbols;

    /**
     * The preferred authority for object or parameter names.
     */
    private final Citation authority = org.geotools.metadata.citation.Citation.OPEN_GIS;

    /**
     * The unit for formatting measures, or <code>null</code> for the "natural" unit of each WKT
     * element. This value is set for example by "GEOGCS", which force its enclosing "PRIMEM" to
     * take the same units than itself.
     */
    private Unit contextualUnit;

    /**
     * The object to use for formatting numbers.
     */
    private final NumberFormat numberFormat;

    /**
     * The object to use for formatting units.
     */
    private final UnitFormat unitFormat = UnitFormat.getAsciiInstance();

    /**
     * Dummy field position.
     */
    private final FieldPosition dummy = new FieldPosition(0);

    /**
     * The buffer in which to format. Consider this field as private final; the only
     * method to set the buffer to a new value is {@link AbstractParser#format}.
     */
    StringBuffer buffer;

    /**
     * The amount of space to use in indentation, or 0 if indentation is disabled.
     */
    final int indentation;

    /**
     * The amount of space to write on the left side of each line. This amount is increased
     * by <code>indentation</code> every time a {@link Formattable} object is appended in a
     * new indentation level.
     */
    private int margin;

    /**
     * <code>true</code> if the WKT is invalid.
     */
    private boolean invalidWKT;

    /**
     * True if the formatter should use class name instead of "GEOCS", "DATUM", etc. keywords.
     */
    boolean usesClassname;

    /**
     * Creates a new instance of the formatter with the default symbols.
     */
    public Formatter() {
        this(Symbols.DEFAULT, 0);
    }

    /**
     * Creates a new instance of the formatter. The whole WKT will be formatted
     * on a single line.
     *
     * @param symbols The symbols.
     */
    public Formatter(final Symbols symbols) {
        this(symbols, 0);
    }

    /**
     * Creates a new instance of the formatter with the specified indentation width.
     * The WKT will be formatted on many lines, and the indentation width will have
     * the value specified to this constructor. If the specified indentation is 0,
     * then the whole WKT will be formatted on a single line.
     *
     * @param symbols The symbols.
     * @param indentation The amount of spaces to use in indentation. Typical values are 2 or 4.
     */
    public Formatter(final Symbols symbols, final int indentation) {
        this.symbols     = symbols;
        this.indentation = indentation;
        if (indentation < 0) {
            throw new IllegalArgumentException(Resources.format(
                                               ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
                                               "indentation", new Integer(indentation)));
        }
        if (symbols.numberFormat != Symbols.DEFAULT.numberFormat) {
            numberFormat = (NumberFormat) symbols.numberFormat.clone();
        } else {
            numberFormat = null;
        }
        buffer = new StringBuffer();
    }

    /**
     * Constructor for private use by {@link AbstractParser#format} only.
     * This constructor help to share some objects with {@link AbstractFormat}.
     */
    Formatter(final Symbols symbols, final NumberFormat numberFormat) {
        this.symbols = symbols;
        indentation  = Formattable.getIndentation();
        if (symbols.numberFormat != Symbols.DEFAULT.numberFormat) {
            this.numberFormat = numberFormat;
        } else {
            this.numberFormat = null;
        }
        // Do not set the buffer. It will be set by AbstractParser.format.
    }

    /**
     * Add a separator to the buffer, if needed.
     *
     * @param indent if <code>true</code>, add indentation too.
     */
    private void appendSeparator(final boolean indent) {
        int length = buffer.length();
        char c;
        do {
            if (length == 0) {
                return;
            }
            c = buffer.charAt(--length);
            if (c == symbols.open) {
                return;
            }
        } while (Character.isSpaceChar(c));
        buffer.append(symbols.separator);
        buffer.append(symbols.space);
        if (indent && indentation != 0) {
            buffer.append('\n');
            buffer.append(Utilities.spaces(margin += indentation));
        }
    }

    /**
     * Append the specified <code>Formattable</code> object. This method will automatically append
     * the keyword (e.g. <code>"GEOCS"</code>), the name and the authority code, and will invokes
     * <code>formattable.{@linkplain Formattable#formatWKT formatWKT}(this)</code> for completing
     * the inner part of the WKT.
     *
     * @param formattable The formattable object to append to the WKT.
     */
    public void append(final Formattable formattable) {
        appendSeparator(true);
        final int base = buffer.length();
        buffer.append(symbols.open);
        final IdentifiedObject info = (formattable instanceof IdentifiedObject)
                                    ? (IdentifiedObject) formattable : null;
        if (info != null) {
            buffer.append(symbols.quote);
            buffer.append(getName(info));
            buffer.append(symbols.quote);
        }
        String keyword = formattable.formatWKT(this);
        if (usesClassname) {
            keyword = Utilities.getShortClassName(formattable);
            final int inner = keyword.indexOf('.');
            if (inner >= 0) {
                keyword = keyword.substring(0, inner);
            }
        }
        buffer.insert(base, keyword);
        final Identifier identifier = getIdentifier(info);
        if (identifier != null) {
            final Citation authority = identifier.getAuthority();
            if (authority != null) {
                final InternationalString title = authority.getTitle();
                if (title != null) {
                    buffer.append(symbols.separator);
                    buffer.append(symbols.space);
                    buffer.append("AUTHORITY");
                    buffer.append(symbols.open);
                    buffer.append(symbols.quote);
                    buffer.append(title.toString(symbols.locale));
                    final String code = identifier.getCode();
                    if (code != null) {
                        buffer.append(symbols.quote);
                        buffer.append(symbols.separator);
                        buffer.append(symbols.quote);
                        buffer.append(code);
                    }
                    buffer.append(symbols.quote);
                    buffer.append(symbols.close);
                }
            }
        }
        buffer.append(symbols.close);
        if (margin >= 0) {
            margin -= indentation;
        }
    }

    /**
     * Append the specified OpenGIS's <code>IdentifiedObject</code> object.
     *
     * @param info The info object to append to the WKT.
     */
    public void append(final IdentifiedObject info) {
        if (info instanceof Formattable) {
            append((Formattable) info);
        } else {
            append(new Adapter(info));
        }
    }

    /**
     * Append the specified math transform.
     *
     * @param transform The transform object to append to the WKT.
     */
    public void append(final MathTransform transform) {
        if (transform instanceof Formattable) {
            append((Formattable) transform);
        } else {
            append(new Adapter(transform));
        }
    }

    /**
     * Append a code list to the WKT.
     */
    public void append(final CodeList code) {
        if (code != null) {
            appendSeparator(false);
            buffer.append(code.name());
        }
    }

    /**
     * Append a {@linkplain ParameterValue parameter} in WKT form. If the supplied parameter
     * is actually a {@linkplain ParameterValueGroup parameter group}, all parameters will be
     * inlined.
     */
    public void append(final GeneralParameterValue parameter) {
        if (parameter instanceof ParameterValueGroup) {
            for (final Iterator it=((ParameterValueGroup)parameter).values().iterator(); it.hasNext();) {
                append((GeneralParameterValue) it.next());
            }
        }
        if (parameter instanceof ParameterValue) {
            final ParameterValue param = (ParameterValue) parameter;
            // Covariance: Remove cast if covariance is allowed.
            final ParameterDescriptor descriptor = (ParameterDescriptor) param.getDescriptor();
            Unit unit = descriptor.getUnit();
            if (unit!=null && contextualUnit!=null && unit.isCompatible(contextualUnit)) {
                unit = contextualUnit;
            }
            appendSeparator(false);
            buffer.append("PARAMETER");
            buffer.append(symbols.open);
            buffer.append(symbols.quote);
            buffer.append(getName(descriptor));
            buffer.append(symbols.quote);
            buffer.append(symbols.separator);
            buffer.append(symbols.space);
            if (unit != null) {
                buffer.append(param.doubleValue(unit));
            } else {
                appendObject(param.getValue());
            }
            buffer.append(symbols.close);
        }
    }

    /**
     * Append the specified value to a string buffer. If the value is an array, then the
     * array elements are appended recursively (i.e. the array may contains sub-array).
     */
    private void appendObject(final Object value) {
        if (value == null) {
            buffer.append("null");
            return;
        }
        if (value.getClass().isArray()) {
            buffer.append(symbols.openArray);
            final int length = Array.getLength(value);
            for (int i=0; i<length; i++) {
                if (i != 0) {
                    buffer.append(symbols.separator);
                    buffer.append(symbols.space);
                }
                appendObject(Array.get(value, i));
            }
            buffer.append(symbols.closeArray);
            return;
        }
        final boolean isNumeric = (value instanceof Number);
        if (!isNumeric) {
            buffer.append(symbols.quote);
        }
        buffer.append(value);
        if (!isNumeric) {
            buffer.append(symbols.quote);
        }
    }

    /**
     * Append an integer number. A comma (or any other element
     * separator) will be written before the number if needed.
     */
    public void append(final int number) {
        appendSeparator(false);
        if (numberFormat != null) {
            final int fraction = numberFormat.getMinimumFractionDigits();
            numberFormat.setMinimumFractionDigits(0);
            numberFormat.format(number, buffer, dummy);
            numberFormat.setMinimumFractionDigits(fraction);
        } else {
            buffer.append(number);
        }
    }

    /**
     * Append a floating point number. A comma (or any other element
     * separator) will be written before the number if needed.
     */
    public void append(final double number) {
        appendSeparator(false);
        if (numberFormat != null) {
            numberFormat.format(number, buffer, dummy);
        } else {
            buffer.append(number);
        }
    }

    /**
     * Appends a unit in WKT form. For example, <code>append(SI.KILOMETER)</code>
     * can append "<code>UNIT["km", 1000]</code>" to the WKT.
     */
    public void append(final Unit unit) {
        if (unit != null) {
            appendSeparator(false);
            buffer.append(usesClassname ? "Unit" : "UNIT");
            buffer.append(symbols.open);
            buffer.append(symbols.quote);
            if (NonSI.DEGREE_ANGLE.equals(unit)) {
                buffer.append("degree");
            } else {
                unitFormat.format(unit, buffer, dummy);
            }
            buffer.append(symbols.quote);
            Unit base = null;
            if (SI.METER.isCompatible(unit)) {
                base = SI.METER;
            } else if (SI.RADIAN.isCompatible(unit)) {
                base = SI.RADIAN;
            } else if (SI.SECOND.isCompatible(unit)) {
                base = SI.SECOND;
            }
            if (base != null) {
                append(unit.getConverterTo(base).convert(1));
            }
            buffer.append(symbols.close);
        }
    }

    /**
     * Append a character string. The string will be written between quotes.
     * A comma (or any other element separator) will be written before the string if needed.
     */
    public void append(final String text) {
        appendSeparator(false);
        buffer.append(symbols.quote);
        buffer.append(text);
        buffer.append(symbols.quote);
    }

    /**
     * Returns the preferred identifier for the specified object. If the specified
     * object contains an identifier from the preferred authority (usually
     * {@linkplain org.geotools.metadata.citation.Citation#OPEN_GIS OpenGIS}), then
     * this identifier is returned. Otherwise, the first identifier is returned. If
     * the specified object contains no identifier, then this method returns <code>null</code>.
     *
     * @param  info The object to looks for a preferred identifier.
     * @return The preferred identifier, or <code>null</code> if none.
     */
    private Identifier getIdentifier(final IdentifiedObject info) {
        Identifier first = null;
        if (info != null) {
            final Identifier[] identifiers = info.getIdentifiers();
            if (identifiers != null) {
                for (int i=0; i<identifiers.length; i++) {
                    final Identifier id = identifiers[i];
                    if (authorityMatches(id.getAuthority())) {
                        return id;
                    }
                    if (first == null) {
                        first = id;
                    }
                }
            }
        }
        return first;
    }

    /**
     * Checks if the specified authority can be recognized as the expected authority.
     * This implementation do not requires an exact matches. A matching title is enough.
     */
    private boolean authorityMatches(final Citation citation) {
        if (authority == citation) {
            return true;
        }
        return (citation != null) && 
               authority.getTitle().toString(null).equalsIgnoreCase(
                citation.getTitle().toString(null));
    }

    /**
     * Returns the preferred name for the specified object. If the specified
     * object contains a name from the preferred authority (usually
     * {@linkplain org.geotools.metadata.citation.Citation#OPEN_GIS OpenGIS}),
     * then this name is returned. Otherwise, the first name found is returned.
     *
     * @param  info The object to looks for a preferred name.
     * @return The preferred name.
     */
    public String getName(final IdentifiedObject info) {
        final Identifier name = info.getName();
        if (!authorityMatches(name.getAuthority())) {
            final GenericName[] aliases = info.getAlias();
            if (aliases != null) {
                final String title = authority.getTitle().toString(null);
                for (int i=0; i<aliases.length; i++) {
                    GenericName alias = aliases[i];
                    final GenericName scope = alias.getScope();
                    if (scope != null) {
                        if (title.equalsIgnoreCase(scope.toString())) {
                            return alias.asLocalName().toString();
                        }
                    }
                }
            }    
        }
        return name.getCode();
    }

    /**
     * The unit for formatting measures, or <code>null</code> for the "natural" unit of each WKT
     * element. This value is set for example by "GEOGCS", which force its enclosing "PRIMEM" to
     * take the same units than itself.
     *
     * @return The unit for measure. Default value is <code>null</code>.
     */
    public Unit getContextualUnit() {
        return contextualUnit;
    }

    /**
     * Set the unit for formatting measures.
     *
     * @param unit The new unit, or <code>null</code>.
     */
    public void setContextualUnit(final Unit unit) {
        contextualUnit = unit;
    }

    /**
     * Returns <code>true</code> if the WKT in this formatter is not strictly compliant to the
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html">WKT
     * specification</A>. This method returns <code>true</code> if {@link #setInvalidWKT} has
     * been invoked at least once. The action to take regarding invalid WKT is caller-dependant.
     * For example {@link Formattable#toString} will accepts loose WKT formatting and ignore this
     * flag, while {@link Formattable#toWKT} requires strict WKT formatting and will thrown an
     * exception if this flag is set.
     */
    public boolean isInvalidWKT() {
        return invalidWKT || buffer.length()==0;
    }

    /**
     * Set a flag marking the current WKT as not strictly compliant to the
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html">WKT
     * specification</A>. This method is invoked by {@link Formattable#formatWKT} methods when the
     * object to format is more complex than what the WKT specification allows. For example this
     * method is invoked when an {@linkplain org.geotools.referencing.crs.EngineeringCRS engineering CRS}
     * uses different unit for each axis, An application can tests {@link #isInvalidWKT} later for
     * checking WKT validity.
     */
    public void setInvalidWKT() {
        invalidWKT = true;
    }

    /**
     * Returns the WKT in its current state.
     */
    public String toString() {
        return buffer.toString();
    }

    /**
     * Clear this formatter. All properties (including {@linkplain #getContextualUnit contextual
     * unit} and {@linkplain #isInvalidWKT WKT validity flag} are reset to their default value.
     * After this method call, this <code>Formatter</code> object is ready for formatting a new
     * object.
     */
    public void clear() {
        buffer.setLength(0);
        contextualUnit = null;
        invalidWKT     = false;
        margin         = 0;
    }

    /**
     * Set the preferred indentation from the command line. This indentation is used by
     * {@link Formattable#toWKT()} when no indentation were explicitly requested. This
     * method can be invoked from the command line using the following syntax:
     *
     * <blockquote>
     * <code>java org.geotools.referencing.wkt.Formatter -identation=</code><var>&lt;preferred
     * indentation&gt;</var>
     * </blockquote>
     */
    public static void main(final String[] args) {
        final Arguments arguments = new Arguments(args);
        final int indentation = arguments.getRequiredInteger(Formattable.INDENTATION);
        arguments.getRemainingArguments(0);
        Formattable.setIndentation(indentation);
    }
}
