/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
 * (C) 1998, Pêches et Océans Canada
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
package org.geotools.measure;

// J2SE dependencies
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.text.Format;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import javax.units.Unit;
import javax.units.SI;
import javax.units.NonSI;
import javax.units.Converter;

// OpenGIS dependencies
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.TemporalDatum;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.referencing.crs.TemporalCRS;


/**
 * Format a {@link org.geotools.geometry.DirectPosition} in an arbitrary
 * {@link org.geotools.referencing.crs.CoordinateReferenceSystem}. The
 * format for each ordinate is infered from the coordinate system units
 * using the following rules:
 * <ul>
 *   <li>Ordinate values in {@linkplain NonSI#DEGREE_ANGLE degrees} are formated as angles
 *       using {@link AngleFormat}.</li>
 *   <li>Ordinate values in any unit compatible with {@linkplain SI#SECOND seconds}
 *       are formated as dates using {@link DateFormat}.</li>
 *   <li>All other values are formatted as numbers using {@link NumberFormat}.</li>
 * </ul>
 *
 * <strong>Note:</strong> parsing is not yet implemented in this version.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class CoordinateFormat extends Format {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1334894996513164253L;
    
    /**
     * The output coordinate reference system.
     */
    private CoordinateReferenceSystem crs;

    /**
     * The formats to use for formatting. This array's length must be equals
     * to the {@linkplain #getCoordinateReferenceSystem coordinate system}'s
     * dimension. This array is never <code>null</code>.
     */
    private Format[] formats;

    /**
     * The type for each value in the <code>formats</code> array.
     * Types are: 0=number, 1=longitude, 2=latitude, 3=other angle,
     * 4=date, 5=ellapsed time. This array is never <code>null</code>.
     */
    private byte[] types;

    /**
     * Constants for the <code>types</code> array.
     */
    private static final byte LONGITUDE=1, LATITUDE=2, ANGLE=3, DATE=4, TIME=5;

    /**
     * The time epochs. Non-null only if at least one ordinate is a date.
     */
    private long[] epochs;

    /**
     * Conversions from temporal axis units to milliseconds.
     * Non-null only if at least one ordinate is a date.
     */
    private Converter[] toMillis;

    /**
     * Dummy field position.
     */
    private final FieldPosition dummy = new FieldPosition(0);

    /**
     * The locale for formatting coordinates and numbers.
     */
    private final Locale locale;

    /**
     * Construct a new coordinate format with default locale and a two-dimensional
     * {@linkplain org.geotools.referencing.crs.GeographicCRS#WGS84 geographic (WGS 1984)}
     * coordinate reference system.
     */
    public CoordinateFormat() {
        this(Locale.getDefault());
    }

    /**
     * Construct a new coordinate format for the specified locale and a two-dimensional
     * {@linkplain org.geotools.referencing.crs.GeographicCRS#WGS84 geographic (WGS 1984)}
     * coordinate reference system.
     *
     * @param locale The locale for formatting coordinates and numbers.
     */
    public CoordinateFormat(final Locale locale) {
        this(locale, org.geotools.referencing.crs.GeographicCRS.WGS84);
    }

    /**
     * Construct a new coordinate format for the specified locale and coordinate system.
     *
     * @param locale The locale for formatting coordinates and numbers.
     * @param crs    The output coordinate reference system.
     */
    public CoordinateFormat(final Locale locale, final CoordinateReferenceSystem crs) {
        this.locale = locale;
        setCoordinateReferenceSystem(crs);
    }

    /**
     * Returns the coordinate reference system for points to be formatted.
     *
     * @return The output coordinate reference system.
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    /**
     * Set the coordinate reference system for points to be formatted. The number
     * of dimensions must matched the dimension of points to be formatted.
     *
     * @param crs The new coordinate system.
     */
    public void setCoordinateReferenceSystem(final CoordinateReferenceSystem crs) {
        if (!CRSUtilities.equalsIgnoreMetadata(this.crs, crs)) {
            final CoordinateSystem cs = crs.getCoordinateSystem();
            Format numberFormat = null;
            Format  angleFormat = null;
            Format   dateFormat = null;
            /*
             * Reuse existing formats. It is necessary in order to avoid
             * overwritting any setting done with 'setNumberPattern(...)'
             * or 'setAnglePattern(...)'
             */
            if (formats != null) {
                for (int i=formats.length; --i>=0;) {
                    final Format format = formats[i];
                    if (format instanceof NumberFormat) {
                        numberFormat = format;
                    } else if (format instanceof AngleFormat) {
                        angleFormat = format;
                    } else if (format instanceof DateFormat) {
                        dateFormat = format;
                    }
                }
            }
            /*
             * Create a new array of 'Format' objects, one for each dimension.
             * The format subclasses are infered from coordinate system axis.
             */
            epochs   = null;
            toMillis = null;
            formats  = new Format[cs.getDimension()];
            types    = new byte[formats.length];
            for (int i=0; i<formats.length; i++) {
                final Unit unit = cs.getAxis(i).getUnit();
                /////////////////
                ////  Angle  ////
                /////////////////
                if (NonSI.DEGREE_ANGLE.equals(unit)) {
                    if (angleFormat == null) {
                        angleFormat = new AngleFormat("DD°MM.m'", locale);
                    }
                    formats[i] = angleFormat;
                    final AxisDirection axis = cs.getAxis(i).getDirection().absolute();
                    if (AxisDirection.EAST.equals(axis)) {
                        types[i] = LONGITUDE;
                    } else if (AxisDirection.NORTH.equals(axis)) {
                        types[i] = LATITUDE;
                    } else {
                        types[i] = ANGLE;
                    }
                    continue;
                }
                ////////////////
                ////  Date  ////
                ////////////////
                if (SI.SECOND.isCompatible(unit)) {
                    final Datum datum = CRSUtilities.getSubCRS(crs, i, i+1).getDatum();
                    if (datum instanceof TemporalDatum) {
                        if (toMillis == null) {
                            toMillis = new Converter[formats.length];
                            epochs   = new long     [formats.length];
                        }
                        toMillis[i] = unit.getConverterTo(TemporalCRS.MILLISECOND);
                        epochs  [i] = ((TemporalDatum) datum).getOrigin().getTime();
                        if (dateFormat == null) {
                            dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
                        }
                        formats[i] = dateFormat;
                        types  [i] = DATE;
                        continue;
                    }
                    types[i] = TIME;
                    // Fallthrough: formatted as number for now.
                    // TODO: Provide ellapsed time formatting later.
                }
                //////////////////
                ////  Number  ////
                //////////////////
                if (numberFormat == null) {
                    numberFormat = NumberFormat.getNumberInstance(locale);
                }
                formats[i] = numberFormat;
                // types[i] default to 0.
            }
        }
        this.crs = crs;
    }

    /**
     * Set the pattern for numbers fields.  If some ordinates are formatted as plain number
     * (for example in {@linkplain org.geotools.referencing.cs.CartesianCS cartesian coordinate
     * system}), then those numbers will be formatted using this pattern.
     *
     * @param pattern The number pattern as specified in {@link DecimalFormat}.
     */
    public void setNumberPattern(final String pattern) {
        Format lastFormat = null;
        for (int i=0; i<formats.length; i++) {
            final Format format = formats[i];
            if (format!=lastFormat && (format instanceof DecimalFormat)) {
                ((DecimalFormat) format).applyPattern(pattern);
                lastFormat = format;
            }
        }
    }

    /**
     * Set the pattern for angles fields. If some ordinates are formatted as angle
     * (for example in {@linkplain org.geotools.referencing.cs.EllipsoidalCS ellipsoidal
     * coordinate system}), then those angles will be formatted using this pattern.
     *
     * @param pattern The angle pattern as specified in {@link AngleFormat}.
     */
    public void setAnglePattern(final String pattern) {
        Format lastFormat = null;
        for (int i=0; i<formats.length; i++) {
            final Format format = formats[i];
            if (format!=lastFormat && (format instanceof AngleFormat)) {
                ((AngleFormat) format).applyPattern(pattern);
                lastFormat = format;
            }
        }
    }

    /**
     * Set the pattern for dates fields. If some ordinates are formatted as date (for example in
     * {@linkplain org.geotools.referencing.cs.TemporalCS temporal coordinate system}), then
     * those dates will be formatted using this pattern.
     *
     * @param pattern The date pattern as specified in {@link SimpleDateFormat}.
     */
    public void setDatePattern(final String pattern) {
        Format lastFormat = null;
        for (int i=0; i<formats.length; i++) {
            final Format format = formats[i];
            if (format!=lastFormat && (format instanceof SimpleDateFormat)) {
                ((SimpleDateFormat) format).applyPattern(pattern);
                lastFormat = format;
            }
        }
    }

    /**
     * Set the time zone for dates fields. If some ordinates are formatted as date (for example in
     * {@linkplain org.geotools.referencing.cs.TemporalCS temporal coordinate system}), then
     * those dates will be formatted using the specified time zone.
     *
     * @param timezone The time zone for dates.
     */
    public void setTimeZone(final TimeZone timezone) {
        Format lastFormat = null;
        for (int i=0; i<formats.length; i++) {
            final Format format = formats[i];
            if (format!=lastFormat && (format instanceof DateFormat)) {
                ((DateFormat) format).setTimeZone(timezone);
                lastFormat = format;
            }
        }
    }

    /**
     * Returns the format to use for formatting an ordinate at the given dimension.
     * The dimension parameter range from 0 inclusive to the
     * {@linkplain #getCoordinateReferenceSystem coordinate reference system}'s dimension,
     * exclusive. This method returns a direct reference to the internal format; any change
     * to the returned {@link Format} object will change the formatting for this
     * <code>CoordinateFormat</code> object.
     *
     * @param  dimension The dimension for the ordinate to format.
     * @return The format for the given dimension.
     * @throws IndexOutOfBoundsException if <code>dimension</code> is out of range.
     */
    public Format getFormat(final int dimension) throws IndexOutOfBoundsException {
        return formats[dimension];
    }

    /**
     * Formats a direct position. The position's dimension must matches the
     * {@linkplain #getCoordinateReferenceSystem coordinate reference system} dimension.
     *
     * @param  point The position to format.
     * @return The formatted position.
     * @throws IllegalArgumentException if this <code>CoordinateFormat</code>
     *         cannot format the given object.
     */
    public String format(final DirectPosition point) {
        return format(point, new StringBuffer(), null).toString();
    }
    
    /**
     * Formats a direct position and appends the resulting text to a given string buffer.
     * The position's dimension must matches the {@linkplain #getCoordinateReferenceSystem
     * coordinate reference system} dimension.
     *
     * @param point      The position to format.
     * @param toAppendTo Where the text is to be appended.
     * @param position   A <code>FieldPosition</code> identifying a field in the formatted text,
     *                   or <code>null</code> if none.
     * @return The string buffer passed in as <code>toAppendTo</code>, with formatted text appended.
     * @throws IllegalArgumentException if this <code>CoordinateFormat</code>
     *         cannot format the given object.
     */
    public StringBuffer format(final DirectPosition  point,
                               final StringBuffer    toAppendTo,
                               final FieldPosition   position)
            throws IllegalArgumentException
    {
        final int dimension = point.getDimension();
        if (dimension != formats.length) {
            throw new MismatchedDimensionException(Resources.format(
                        ResourceKeys.ERROR_MISMATCHED_DIMENSION_$3, "point",
                        new Integer(dimension), new Integer(formats.length)));
        }
        for (int i=0; i<formats.length; i++) {
            final double value = point.getOrdinate(i);
            final Object object;
            switch (types[i]) {
                default:        object=new Double   (value); break;
                case LONGITUDE: object=new Longitude(value); break;
                case LATITUDE:  object=new Latitude (value); break;
                case ANGLE:     object=new Angle    (value); break;
                case DATE: {
                    final CoordinateSystemAxis axis = crs.getCoordinateSystem().getAxis(i);
                    long offset = Math.round(toMillis[i].convert(value));
                    if (AxisDirection.PAST.equals(axis.getDirection())) {
                        offset = -offset;
                    }
                    object = new Date(epochs[i] + offset);
                    break;
                }
            }
            if (i != 0) {
                toAppendTo.append(' ');
            }
            formats[i].format(object, toAppendTo, dummy);
        }
        return toAppendTo;
    }

    /**
     * Formats a direct position and appends the resulting text to a given string buffer.
     * The position's dimension must matches the {@linkplain #getCoordinateReferenceSystem
     * coordinate reference system} dimension.
     *
     * @param object     The {@link DirectPosition} to format.
     * @param toAppendTo Where the text is to be appended.
     * @param position   A <code>FieldPosition</code> identifying a field in the formatted text,
     *                   or <code>null</code> if none.
     * @return The string buffer passed in as <code>toAppendTo</code>, with formatted text appended.
     * @throws NullPointerException if <code>toAppendTo</code> is null.
     * @throws IllegalArgumentException if this <code>CoordinateFormat</code>
     *         cannot format the given object.
     */
    public StringBuffer format(final Object        object,
                               final StringBuffer  toAppendTo,
                               final FieldPosition position)
            throws IllegalArgumentException
    {
        if (object instanceof DirectPosition) {
            return format((DirectPosition) object, toAppendTo, position);
        } else {
            throw new IllegalArgumentException(String.valueOf(object));
        }
    }
    
    /**
     * Not yet implemented.
     */
    public Object parseObject(final String source, final ParsePosition position) {
        throw new UnsupportedOperationException("DirectPosition parsing not yet implemented.");
    }
}
