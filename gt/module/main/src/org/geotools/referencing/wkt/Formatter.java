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
import java.lang.reflect.Array;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Locale;
import javax.units.NonSI;
import javax.units.SI;
import javax.units.Unit;
import javax.units.UnitFormat;

// OpenGIS dependencies
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.util.CodeList;
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.resources.Arguments;
import org.geotools.resources.Utilities;
import org.geotools.resources.XMath;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;


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
    Citation authority = org.geotools.metadata.citation.Citation.OPEN_GIS;

    /**
     * The unit for formatting measures, or <code>null</code> for the "natural" unit of each WKT
     * element. This value is set for example by "GEOGCS", which force its enclosing "PRIMEM" to
     * take the same units than itself.
     */
    private Unit linearUnit, angularUnit;

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
     * The starting point in the buffer. Always 0, except when used by
     * {@link AbstractParser#format}.
     */
    int bufferBase;

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
     * <code>true</code> if a new line were requested during the execution
     * of {@link #append(Formattable)}. This is used to determine if
     * <code>UNIT</code> and <code>AUTHORITY</code> elements should appears
     * on a new line too.
     */
    private boolean lineChanged;

    /**
     * <code>true</code> if the WKT is invalid.
     */
    private boolean invalidWKT;

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
        if (symbols == null) {
            throw new IllegalArgumentException(Resources.format(
                      ResourceKeys.ERROR_NULL_ARGUMENT_$1, "symbols"));
        }
        if (indentation < 0) {
            throw new IllegalArgumentException(Resources.format(
                                               ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
                                               "indentation", new Integer(indentation)));
        }
        numberFormat = (NumberFormat) symbols.numberFormat.clone();
        buffer = new StringBuffer();
    }

    /**
     * Constructor for private use by {@link AbstractParser#format} only.
     * This constructor help to share some objects with {@link AbstractFormat}.
     */
    Formatter(final Symbols symbols, final NumberFormat numberFormat) {
        this.symbols = symbols;
        indentation  = Formattable.getIndentation();
        this.numberFormat = numberFormat; // No clone needed.
        // Do not set the buffer. It will be set by AbstractParser.format.
    }

    /**
     * Add a separator to the buffer, if needed.
     *
     * @param newLine if <code>true</code>, add a line separator too.
     */
    private void appendSeparator(final boolean newLine) {
        int length = buffer.length();
        char c;
        do {
            if (length == bufferBase) {
                return;
            }
            c = buffer.charAt(--length);
            if (c==symbols.open || c==symbols.openArray) {
                return;
            }
        } while (Character.isWhitespace(c) || c==symbols.space);
        buffer.append(symbols.separator);
        buffer.append(symbols.space);
        if (newLine && indentation != 0) {
            buffer.append(System.getProperty("line.separator", "\n"));
            buffer.append(Utilities.spaces(margin));
            lineChanged = true;
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
        /*
         * Formats the opening bracket and the object name (e.g. "NAD27").
         * The WKT entity name (e.g. "PROJCS") will be formatted later.
         * The result of this code portion looks like the following:
         *
         *         <previous text>,
         *           ["NAD27 / Idaho Central"
         */
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
        /*
         * Formats the part after the object name, then insert the WKT element name
         * in front of them. The result of this code portion looks like the following:
         *
         *         <previous text>,
         *           PROJCS["NAD27 / Idaho Central",
         *             GEOGCS[...etc...],
         *             ...etc...
         */
        indent(+1);
        lineChanged = false;
        String keyword = formattable.formatWKT(this);
        buffer.insert(base, keyword);
        /*
         * Formats the AUTHORITY[<name>,<code>] entity, if there is one. The entity
         * will be on the same line than the enclosing one if no line separator were
         * added (e.g. SPHEROID["Clarke 1866", ..., AUTHORITY["EPSG","7008"]]), or on
         * a new line otherwise. After this block, the result looks like the following:
         *
         *         <previous text>,
         *           PROJCS["NAD27 / Idaho Central",
         *             GEOGCS[...etc...],
         *             ...etc...
         *             AUTHORITY["EPSG","26769"]]
         */
        final Identifier identifier = getIdentifier(info);
        if (identifier != null) {
            final Citation authority = identifier.getAuthority();
            if (authority != null) {
                /*
                 * Since WKT often use abbreviations, search for the shortest
                 * title or alternate title. If one is found, it will be used
                 * as the authority name (e.g. "EPSG").
                 */
                InternationalString inter = authority.getTitle();
                String title = (inter!=null) ? inter.toString(symbols.locale) : null;
                for (final Iterator it=authority.getAlternateTitles().iterator(); it.hasNext();) {
                    inter = (InternationalString) it.next();
                    if (inter != null) {
                        final String candidate = inter.toString(symbols.locale);
                        if (candidate != null) {
                            if (title==null || candidate.length()<title.length()) {
                                title = candidate;
                            }
                        }
                    }
                }
                if (title != null) {
                    appendSeparator(lineChanged);
                    buffer.append("AUTHORITY");
                    buffer.append(symbols.open);
                    buffer.append(symbols.quote);
                    buffer.append(title);
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
        lineChanged = true;
        indent(-1);
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
            final Unit valueUnit = descriptor.getUnit();
            Unit unit = valueUnit;
            if (unit!=null && !Unit.ONE.equals(unit)) {
                if (linearUnit!=null && unit.isCompatible(linearUnit)) {
                    unit = linearUnit;
                } else if (angularUnit!=null && unit.isCompatible(angularUnit)) {
                    unit = angularUnit;
                }
            }
            appendSeparator(true);
            buffer.append("PARAMETER");
            buffer.append(symbols.open);
            buffer.append(symbols.quote);
            buffer.append(getName(descriptor));
            buffer.append(symbols.quote);
            buffer.append(symbols.separator);
            buffer.append(symbols.space);
            if (unit != null) {
                double value = param.doubleValue(unit);
                if (!unit.equals(valueUnit)) {
                    value = XMath.fixRoundingError(value, 9);
                }
                format(value);
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
        if (value instanceof Number) {
            format((Number) value);
        } else {
            buffer.append(symbols.quote);
            buffer.append(value);
            buffer.append(symbols.quote);
        }
    }

    /**
     * Append an integer number. A comma (or any other element
     * separator) will be written before the number if needed.
     */
    public void append(final int number) {
        appendSeparator(false);
        format(number);
    }

    /**
     * Append a floating point number. A comma (or any other element
     * separator) will be written before the number if needed.
     */
    public void append(final double number) {
        appendSeparator(false);
        format(number);
    }

    /**
     * Appends a unit in WKT form. For example, <code>append(SI.KILOMETER)</code>
     * can append "<code>UNIT["km", 1000]</code>" to the WKT.
     */
    public void append(final Unit unit) {
        if (unit != null) {
            appendSeparator(lineChanged);
            buffer.append("UNIT");
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
            } else if (SI.SECOND.isCompatible(unit)) {
                base = SI.SECOND;
            } else if (SI.RADIAN.isCompatible(unit)) {
                if (!Unit.ONE.equals(unit)) {
                    base = SI.RADIAN;
                }
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
     * Format an arbitrary number.
     */
    private void format(final Number number) {
        if (number instanceof Byte  ||
            number instanceof Short ||
            number instanceof Integer)
        {
            format(number.intValue());
        } else {
            format(number.doubleValue());
        }
    }

    /**
     * Format an integer number.
     */
    private void format(final int number) {
        final int fraction = numberFormat.getMinimumFractionDigits();
        numberFormat.setMinimumFractionDigits(0);
        numberFormat.format(number, buffer, dummy);
        numberFormat.setMinimumFractionDigits(fraction);
    }

    /**
     * Format a floating point number.
     */
    private void format(final double number) {
        numberFormat.format(number, buffer, dummy);
    }

    /**
     * Increase or reduce the indentation. A value of <code>+1</code> increase
     * the indentation by the amount of spaces specified at construction time,
     * and a value of <code>+1</code> reduce it.
     */
    private void indent(final int amount) {
        margin = Math.max(0, margin + indentation*amount);
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
               authority.getTitle().toString(Locale.US).equalsIgnoreCase(
                citation.getTitle().toString(Locale.US));
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
                /*
                 * The main name doesn't matches. Search in alias. We will first
                 * check if alias implements Identifier (this is the case of
                 * Geotools implementation). Otherwise, we will look at the
                 * scope in generic name.
                 */
                for (int i=0; i<aliases.length; i++) {
                    final GenericName alias = aliases[i];
                    if (alias instanceof Identifier) {
                        final Identifier candidate = (Identifier) alias;
                        if (authorityMatches(candidate.getAuthority())) {
                            return candidate.getCode();
                        }
                    }
                }
                final String title = authority.getTitle().toString(Locale.US);
                for (int i=0; i<aliases.length; i++) {
                    final GenericName alias = aliases[i];
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
     * The linear unit for formatting measures, or <code>null</code> for the "natural" unit of each
     * WKT element.
     *
     * @return The unit for measure. Default value is <code>null</code>.
     */
    public Unit getLinearUnit() {
        return linearUnit;
    }

    /**
     * Set the unit for formatting linear measures.
     *
     * @param unit The new unit, or <code>null</code>.
     */
    public void setLinearUnit(final Unit unit) {
        if (unit!=null && !SI.METER.isCompatible(unit)) {
            throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_NON_LINEAR_UNIT_$1, unit));
        }
        linearUnit = unit;
    }

    /**
     * The angular unit for formatting measures, or <code>null</code> for the "natural" unit of
     * each WKT element. This value is set for example by "GEOGCS", which force its enclosing
     * "PRIMEM" to take the same units than itself.
     *
     * @return The unit for measure. Default value is <code>null</code>.
     */
    public Unit getAngularUnit() {
        return angularUnit;
    }

    /**
     * Set the angular unit for formatting measures.
     *
     * @param unit The new unit, or <code>null</code>.
     */
    public void setAngularUnit(final Unit unit) {
        if (unit!=null && (!SI.RADIAN.isCompatible(unit) || Unit.ONE.equals(unit))) {
            throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_NON_ANGULAR_UNIT_$1, unit));
        }
        angularUnit = unit;
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
     * Clear this formatter. All properties (including {@linkplain #getLinearUnit unit}
     * and {@linkplain #isInvalidWKT WKT validity flag} are reset to their default value.
     * After this method call, this <code>Formatter</code> object is ready for formatting
     * a new object.
     */
    public void clear() {
        if (buffer != null) {
            buffer.setLength(0);
        }
        linearUnit  = null;
        angularUnit = null;
        lineChanged = false;
        invalidWKT  = false;
        margin      = 0;
    }

    /**
     * Set the preferred indentation from the command line. This indentation is used by
     * {@link Formattable#toWKT()} when no indentation were explicitly requested. This
     * method can be invoked from the command line using the following syntax:
     *
     * <blockquote>
     * <code>java org.geotools.referencing.wkt.Formatter -indentation=</code><var>&lt;preferred
     * indentation&gt;</var>
     * </blockquote>
     */
    public static void main(String[] args) {
        final Arguments arguments = new Arguments(args);
        final int indentation = arguments.getRequiredInteger(Formattable.INDENTATION);
        args = arguments.getRemainingArguments(0);
        Formattable.setIndentation(indentation);
    }
}
