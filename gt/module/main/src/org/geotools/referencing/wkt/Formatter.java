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

// J2SE dependencies
import java.util.Locale;
import java.text.FieldPosition;
import javax.units.NonSI;
import javax.units.SI;
import javax.units.Unit;
import javax.units.UnitFormat;

// OpenGIS dependencies
import org.opengis.util.CodeList;
import org.opengis.referencing.Info;
import org.opengis.referencing.Identifier;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.OperationParameter;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.operation.MathTransform;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Format {@link Formattable} objects as
 * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
 * Known Text</cite> (WKT)</A>.
 *
 * A formatter is constructed with a specified locale, which will be used for querying
 * {@linkplain org.geotools.referencing.Info#getName names} and
 * {@linkplain org.geotools.metadata.citation.Citation#getTitle authority titles}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html">Well Know Text specification</A>
 * @see <A HREF="http://gdal.velocet.ca/~warmerda/wktproblems.html">OGC WKT Coordinate System Issues</A>
 *
 * @todo Symbols are hard-coded as static constants for now. They should be declared in a
 *       <code>Symbols</code> class, which should be shared with {@link AbstractParser}.
 */
public class Formatter {
    /**
     * The character used for opening brace.
     * Usually <code>'['</code>, but <code>'('</code> is legal as well.
     */
    private static final char OPEN = '[';

    /**
     * The character used for closing brace.
     * Usually <code>']'</code>, but <code>')'</code> is legal as well.
     */
    private static final char CLOSE = ']';

    /**
     * The character used for quote.
     * Usually <code>'"'</code>.
     */
    private static final char QUOTE = '"';

    /**
     * The character used as a separator. Usually <code>','</code>, but would need
     * to be changed if a non-English locale is used for formatting numbers.
     */
    private static final char SEPARATOR = ',';

    /**
     * The character used for space.
     * Usually <code>'&nbsp;'</code>, but could be a no-break space too if unicode is allowed.
     */
    private static final char SPACE = ' ';

    /**
     * The locale for querying localizable information.
     */
    private final Locale locale;

    /**
     * The unit for formatting measures, or <code>null</code> for the "natural" unit of each WKT
     * element. This value is set for example by "GEOGCS", which force its enclosing "PRIMEM" to
     * take the same units than itself.
     */
    private Unit contextualUnit;

    /**
     * The format to use for formatting units.
     */
    private final UnitFormat unitFormat = UnitFormat.getAsciiInstance();

    /**
     * Dummy field position.
     */
    private final FieldPosition dummy = new FieldPosition(0);

    /**
     * The buffer in which to format.
     */
    private final StringBuffer buffer = new StringBuffer();

    /**
     * The amount of space to use in indentation, or 0 if indentation is disabled.
     */
    private final int indentation;

    /**
     * The amount of space to write on the left side of each line. This amount is increased
     * by <code>indentation</code> every time a {@link Formattable} object is appenned in a
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
     * Creates a new instance of the formatter. The whole WKT will be formatted
     * on a single line.
     *
     * @param locale The locale, or <code>null</code>.
     */
    public Formatter(final Locale locale) {
        this(locale, 0);
    }

    /**
     * Creates a new instance of the formatter with the specified indentation width.
     * The WKT will be formatted on many lines, and the indentation width will have
     * the value specified to this constructor. If the specified indentation is 0,
     * then the whole WKT will be formatted on a single line.
     *
     * @param locale The locale, or <code>null</code>.
     * @param indentation The amount of spaces to use in indentation. Typical values are 2 or 4.
     */
    public Formatter(final Locale locale, final int indentation) {
        this.locale = locale;
        this.indentation = indentation;
        if (indentation < 0) {
            throw new IllegalArgumentException(Resources.format(
                                               ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
                                               "indentation", new Integer(indentation)));
        }
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
            if (c == OPEN) {
                return;
            }
        } while (Character.isSpaceChar(c));
        buffer.append(SEPARATOR);
        buffer.append(SPACE);
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
        buffer.append(OPEN);
        final Info info = (formattable instanceof Info) ? (Info) formattable : null;
        if (info != null) {
            buffer.append(QUOTE);
            buffer.append(info.getName(locale));
            buffer.append(QUOTE);
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
        if (info != null) {
            final Identifier[] identifiers = info.getIdentifiers();
            for (int i=0; i<identifiers.length; i++) {
                final Identifier identifier = identifiers[i];
                final Citation authority = identifier.getAuthority();
                if (authority != null) {
                    final String title = authority.getTitle(locale);
                    if (title != null) {
                        buffer.append(SEPARATOR);
                        buffer.append(SPACE);
                        buffer.append("AUTHORITY");
                        buffer.append(OPEN);
                        buffer.append(QUOTE);
                        buffer.append(title);
                        final String code = identifier.getCode();
                        if (code != null) {
                            buffer.append(QUOTE);
                            buffer.append(SEPARATOR);
                            buffer.append(QUOTE);
                            buffer.append(code);
                        }
                        buffer.append(QUOTE);
                        buffer.append(CLOSE);
                        break;
                    }
                }
            }
        }
        buffer.append(CLOSE);
        if (margin >= 0) {
            margin -= indentation;
        }
    }

    /**
     * Append the specified OpenGIS's <code>Info</code> object.
     *
     * @param info The info object to append to the WKT.
     */
    public void append(final Info info) {
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
     * Append a {@linkplain ParameterValue parameter} in WKT form.
     *
     * @see #appendParameter(String, int)
     * @see #appendParameter(String, double, Unit)
     */
    public void append(final GeneralParameterValue parameter) {
        if (parameter instanceof ParameterValueGroup) {
            final GeneralParameterValue[] parameters = ((ParameterValueGroup)parameter).getValues();
            for (int i=0; i<parameters.length; i++) {
                append(parameters[i]);
            }
        }
        if (parameter instanceof ParameterValue) {
            final ParameterValue param = (ParameterValue) parameter;
            // Covariance: Remove cast if covariance is allowed.
            final OperationParameter descriptor = (OperationParameter) param.getDescriptor();
            Unit unit = descriptor.getUnit();
            if (unit!=null && contextualUnit!=null && unit.isCompatible(contextualUnit)) {
                unit = contextualUnit;
            }
            final String name = descriptor.getName(locale);
            final String value;
            if (unit != null) {
                value = String.valueOf(param.doubleValue(unit));
            } else {
                value = String.valueOf(param.getValue());
            }
            appendParameter(name, value);
        }
    }

    /**
     * Append the specified <code>name</code>, <code>value</code> pair as a parameter value.
     *
     * @param name The parameter name.
     * @param value The parameter value.
     *
     * @see #append(GeneralParameterValue)
     * @see #appendParameter(String, int)
     * @see #appendParameter(String, double, Unit)
     */
    private void appendParameter(final String name, final String value) {
        appendSeparator(false);
        buffer.append("PARAMETER");
        buffer.append(OPEN);
        buffer.append(QUOTE);
        buffer.append(name);
        buffer.append(QUOTE);
        buffer.append(SEPARATOR);
        buffer.append(SPACE);
        buffer.append(value);
        buffer.append(CLOSE);
    }

    /**
     * Append the specified <code>name</code>, <code>value</code> pair as a parameter value.
     *
     * @param name The parameter name.
     * @param value The parameter value.
     *
     * @see #append(GeneralParameterValue)
     * @see #appendParameter(String, double, Unit)
     */
    public void appendParameter(final String name, final int value) {
        appendParameter(name, String.valueOf(value));
    }

    /**
     * Append the specified <code>name</code>, <code>value</code> pair as a parameter value.
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     * @param unit  The units for the parameter value, or <code>null</code>.
     *
     * @see #append(GeneralParameterValue)
     * @see #appendParameter(String, int)
     */
    public void appendParameter(final String name, final double value, final Unit unit) {
        appendParameter(name, String.valueOf(value));
    }

    /**
     * Append an integer number. A comma (or any other element
     * separator) will be written before the number if needed.
     */
    public void append(final int number) {
        appendSeparator(false);
        buffer.append(number);
    }

    /**
     * Append a floating point number. A comma (or any other element
     * separator) will be written before the number if needed.
     */
    public void append(final double number) {
        appendSeparator(false);
        buffer.append(number);
    }

    /**
     * Appends a unit in WKT form. For example, <code>append(SI.KILOMETER)</code>
     * can append "<code>UNIT["km", 1000]</code>" to the WKT.
     */
    public void append(final Unit unit) {
        if (unit != null) {
            appendSeparator(false);
            buffer.append(usesClassname ? "Unit" : "UNIT");
            buffer.append(OPEN);
            buffer.append(QUOTE);
            if (NonSI.DEGREE_ANGLE.equals(unit)) {
                buffer.append("degree");
            } else {
                unitFormat.format(unit, buffer, dummy);
            }
            buffer.append(QUOTE);
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
            buffer.append(CLOSE);
        }
    }

    /**
     * Append a character string. The string will be written between quotes.
     * A comma (or any other element separator) will be written before the string if needed.
     */
    public void append(final String text) {
        appendSeparator(false);
        buffer.append(QUOTE);
        buffer.append(text);
        buffer.append(QUOTE);
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
}
