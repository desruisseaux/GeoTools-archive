/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.ct;

// Geotools dependencies
import java.util.Locale;

import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.ParameterListDescriptorImpl;
import javax.media.jai.ParameterListImpl;
import javax.media.jai.util.Range;

import org.geotools.cs.Projection;
import org.geotools.measure.Latitude;
import org.geotools.measure.Longitude;
import org.geotools.resources.Utilities;
import org.geotools.resources.XArray;
import org.geotools.resources.cts.Resources;
import org.opengis.referencing.FactoryException;



/**
 * Base class for {@link MathTransform} providers. Instance of this class
 * allow the creation of transform objects from a classification name.
 * <br><br>
 * <strong>Note: this class is not part of OpenGIS specification and
 * may change in a future version. Do not rely strongly on it.</strong>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Replaced by {@link org.geotools.referencing.operation.MathTransformProvider}
 *             in the <code>org.geotools.referencing.operation</code> package.
 */
public abstract class MathTransformProvider {
    /**
     * The value zero as a {@link Double}.
     */
    static final Double ZERO = new Double(0);
    
    /**
     * The value one as a {@link Double}.
     */
    static final Double ONE = new Double(1);
    
    /**
     * Range of positives values. Range goes
     * from 0 exclusive to positive infinity.
     */
    protected static final Range POSITIVE_RANGE = new Range(Double.class, ZERO, false, null, false);
    
    /**
     * Range of longitude values. Range goes
     * from -180� to +180� inclusives.
     */
    protected static final Range LONGITUDE_RANGE = new Range(Double.class, new Double(Longitude.MIN_VALUE), true, new Double(Longitude.MAX_VALUE), true);
    
    /**
     * Range of latitude values. Range goes
     * from -90� to +90� inclusives.
     */
    protected static final Range LATITUDE_RANGE = new Range(Double.class, new Double(Latitude.MIN_VALUE), true, new Double(Latitude.MAX_VALUE), true);
    
    /**
     * Number of colunms in table {@link #properties} below.
     */
    private static final int RECORD_LENGTH = 4;
    
    /**
     * A default parameter list descriptor for
     * map projections. This descriptor declare
     * <code>"semi_major"</code>,
     * <code>"semi_minor"</code>,
     * <code>"central_meridian"</code>,
     * <code>"latitude_of_origin"</code>,
     * <code>"scale_factor"</code>,
     * <code>"false_easting"</code> and
     * <code>"false_northing"</code> parameters.
     */
    public static final ParameterListDescriptor DEFAULT_PROJECTION_DESCRIPTOR = getDescriptor(new Object[] {
        "semi_major",          Double.class, ParameterListDescriptor.NO_PARAMETER_DEFAULT, POSITIVE_RANGE,
        "semi_minor",          Double.class, ParameterListDescriptor.NO_PARAMETER_DEFAULT, POSITIVE_RANGE,
        "central_meridian",    Double.class, ZERO,                                         LONGITUDE_RANGE,
        "latitude_of_origin",  Double.class, ZERO,                                         LATITUDE_RANGE,
        "scale_factor",        Double.class, ONE,                                          POSITIVE_RANGE,
        "false_easting",       Double.class, ZERO,                                         null,
        "false_northing",      Double.class, ZERO,                                         null
    });
    
    /**
     * The set parameters to use for {@link ParameterListDescriptor} construction,
     * or <code>null</code> if the descriptor is already constructed.
     */
    private Object[] properties;
    
    /**
     * The parameter list descriptor. This object will
     * be constructed only the first time it is needed.
     */
    private ParameterListDescriptor descriptor;
    
    /**
     * The classification name. This name do
     * not contains leading or trailing blanks.
     */
    private final String classification;
    
    /**
     * Resources key for a human readable name. This
     * is used for {@link #getName} implementation.
     */
    private final int nameKey;
    
    /**
     * Construct a new provider.
     *
     * @param classification The classification name.
     * @param inherit The parameter list descriptor to inherit from, or <code>null</code>
     *        if there is none. All parameter descriptions from <code>inherit</code> will
     *        be copied into this newly created <code>MathTransformProvider</code>.   For
     *        map projections, this argument may be {@link #DEFAULT_PROJECTION_DESCRIPTOR}.
     *        Subclasses may add or change parameters in their constructor by invoking
     *        {@link #put}.
     */
    protected MathTransformProvider(final String classification,
                                    final ParameterListDescriptor inherit)
    {
        this(classification, -1, inherit);
    }
    
    /**
     * Construct a new provider.
     *
     * @param classification The classification name.
     * @param nameKey Resources key for a human readable name.
     *        This is used for {@link #getName} implementation.
     * @param inherit The parameter list descriptor to inherit from, or <code>null</code>
     *        if there is none. All parameter descriptions from <code>inherit</code> will
     *        be copied into this newly created <code>MathTransformProvider</code>.   For
     *        map projections, this argument may be {@link #DEFAULT_PROJECTION_DESCRIPTOR}.
     *        Subclasses may add or change parameters in their constructor by invoking
     *        {@link #put}.
     */
    MathTransformProvider(final String classification, final int nameKey,
                          final ParameterListDescriptor inherit)
    {
        this.classification = classification.trim();
        this.nameKey        = nameKey;
        if (inherit!=null) {
            final String[]    names = inherit.getParamNames();
            final Class []  classes = inherit.getParamClasses();
            final Object[] defaults = inherit.getParamDefaults();
            properties = new Object[names.length*RECORD_LENGTH];
            for (int i=0; i<names.length; i++) {
                final int j=i*RECORD_LENGTH;
                properties[j+0] = names   [i];
                properties[j+1] = classes [i];
                properties[j+2] = defaults[i];
                properties[j+3] = inherit.getParamValueRange(names[i]);
            }
        } else {
            properties = new Object[0];
        }
    }
    
    /**
     * Adds or changes a parameter to this math transform provider. If this
     * <code>MathTransformProvider</code> has been constructed with {@link
     * #DEFAULT_PROJECTION_DESCRIPTOR} as argument, then default values are
     * already provided for "semi_major", "semi_minor", "central_meridian",
     * "latitude_of_origin", "scale_factor", "false_easting" and "false_northing".
     * Subclasses may call this method in their constructor for adding or
     * changing parameters.
     *
     * @param parameter    The parameter name.
     * @param defaultValue The default value for this parameter, or
     *                     {@link Double#NaN} if there is none.
     * @param range        The range of legal values. May be one of the predefined
     *                     constants ({@link #POSITIVE_RANGE}, {@link #LONGITUDE_RANGE},
     *                     {@link #LATITUDE_RANGE}) or any other {@link Range} object.
     *                     May be <code>null</code> if all values are valid for this
     *                     parameter.
     * @throws IllegalStateException If {@link #getParameterList} has already
     *         been invoked prior to this call.
     */
    protected final void put(final String parameter, final double defaultValue, final Range range)
            throws IllegalStateException
    {
        put(parameter, Double.class, wrap(defaultValue), range);
    }
    
    /**
     * Adds or changes an integer parameter to this math transform provider.
     * Support of integer values help to make the API clearer, but the true
     * OpenGIS's parameter class support only <code>double</code> values.
     * This is why this method is not yet public. Current Geotools version use
     * integer parameters only for matrix dimension and for a custom parameter
     * in geocentric transform. We hope the user will barely notice it...
     *
     * @param parameter    The parameter name.
     * @param defaultValue The default value for this parameter.
     * @param range        The range of legal values. This is up to the caller to
     *                     build is own range with integer values (predefined ranges
     *                     like {@link #POSITIVE_RANGE} will not work).
     *
     * @throws IllegalStateException If {@link #getParameterList}
     *         has already been invoked prior to this call.
     */
    final void putInt(final String parameter, final int defaultValue, final Range range)
            throws IllegalStateException
    {
        put(parameter, Integer.class, new Integer(defaultValue), range);
    }

    /**
     * Add of changes an object parameter to this math transform provider.  The parameter will
     * have no default value. This method is used for the construction of some math transforms
     * that are not part of OpenGIS specification (e.g. "Power" and "Logarithm").  The OpenGIS
     * specification allows only <code>double</code> parameter values, which is why this method
     * is not yet public.
     *
     * @param parameter The parameter name.
     * @param type      The parameter type.
     */
    final void putObject(final String parameter, final Class type) throws IllegalStateException {
        put(parameter, type, ParameterListDescriptor.NO_PARAMETER_DEFAULT, null);
    }
    
    /**
     * Adds or changes a parameter to this math transform provider.
     *
     * @param parameter    The parameter name.
     * @param type         The parameter type.
     * @param defaultValue The default value for this parameter, or
     *                     {@link  ParameterListDescriptor#NO_PARAMETER_DEFAULT} if none.
     * @param range        The range of legal values, or <code>null</code> if none.
     *
     * @throws IllegalStateException If {@link #getParameterList}
     *         has already been invoked prior to this call.
     */
    private void put(String parameter, final Class type, Object defaultValue, final Range range)
            throws IllegalStateException
    {
        if (properties==null) {
            // Construction is finished.
            throw new IllegalStateException();
        }
        if (defaultValue!=null && range!=null) {
            // Slight optimization for reducing the amount of objects in the heap.
            Object check;
            if (defaultValue.equals(check=range.getMinValue())) defaultValue=check;
            if (defaultValue.equals(check=range.getMaxValue())) defaultValue=check;
        }
        parameter = parameter.trim();
        final int end = properties.length;
        for (int i=0; i<end; i+=RECORD_LENGTH) {
            if (parameter.equalsIgnoreCase(properties[i].toString())) {
                properties[i+0] = parameter;
                properties[i+1] = type;
                properties[i+2] = defaultValue;
                properties[i+3] = range;
                return;
            }
        }
        properties = XArray.resize(properties, end+RECORD_LENGTH);
        properties[end+0] = parameter;
        properties[end+1] = type;
        properties[end+2] = defaultValue;
        properties[end+3] = range;
    }
    
    /**
     * Remove a parameter from this math transform provider.
     *
     * @param  parameter The parameter name to remove.
     * @throws IllegalStateException If {@link #getParameterList}
     *         has already been invoked prior to this call.
     */
    protected final void remove(final String parameter) throws IllegalStateException {
        if (properties==null) {
            // Construction is finished.
            throw new IllegalStateException();
        }
        for (int i=properties.length; (i-=RECORD_LENGTH)!=0;) {
            if (parameter.equalsIgnoreCase(properties[i].toString())) {
                properties = XArray.remove(properties, i, RECORD_LENGTH);
            }
        }
    }
    
    /**
     * Wrap the specified double value in an object.
     */
    private static Object wrap(final double value) {
        if (Double.isNaN(value)) {
            return ParameterListDescriptor.NO_PARAMETER_DEFAULT;
        }
        if (value ==  Latitude.MIN_VALUE) return  LATITUDE_RANGE.getMinValue();
        if (value ==  Latitude.MAX_VALUE) return  LATITUDE_RANGE.getMaxValue();
        if (value == Longitude.MIN_VALUE) return LONGITUDE_RANGE.getMinValue();
        if (value == Longitude.MAX_VALUE) return LONGITUDE_RANGE.getMaxValue();
        if (value == 0)                   return ZERO;
        if (value == 1)                   return ONE;
        return new Double(value);
    }
    
    /**
     * Returns the parameter list descriptor for the specified properties list.
     */
    private static ParameterListDescriptor getDescriptor(final Object[] properties) {
        final String[]    names = new String[properties.length/RECORD_LENGTH];
        final Class []  classes = new Class [names.length];
        final Object[] defaults = new Object[names.length];
        final Range []   ranges = new Range [names.length];
        for (int i=0; i<names.length; i++) {
            final int j = i*RECORD_LENGTH;
            names   [i] = (String)properties[j+0];
            classes [i] =  (Class)properties[j+1];
            defaults[i] = (Object)properties[j+2];
            ranges  [i] =  (Range)properties[j+3];
        }
        return new ParameterListDescriptorImpl(null, names, classes, defaults, ranges);
    }
    
    /**
     * Returns the classification name.
     */
    public String getClassName() {
        return classification;
    }
    
    /**
     * Returns a human readable name localized for the specified locale.
     * If no name is available for the specified locale, this method may
     * returns a name in an arbitrary locale.
     */
    public String getName(final Locale locale) {
        return (nameKey>=0) ? Resources.getResources(locale).getString(nameKey) : getClassName();
    }
    
    /**
     * Returns the parameter list descriptor. The default implementation build the
     * descriptor from the parameters specified by the constructor with {@link #put}.
     */
    public synchronized ParameterListDescriptor getParameterListDescriptor() {
        if (descriptor==null) {
            descriptor = getDescriptor(properties);
            properties = null; // No longer needed.
        }
        return descriptor;
    }
    
    /**
     * Returns a newly created parameter list. The set of parameter
     * depend of the transform this provider is for. Parameters may
     * have default values and a range of validity.
     */
    public ParameterList getParameterList() {
        return new ParameterListImpl(getParameterListDescriptor());
    }
    
    /**
     * Returns a transform for the specified parameters.
     *
     * @param  parameters The parameter values in standard units.
     * @return A {@link MathTransform} object of this classification.
     * @throws MissingParameterException if a mandatory parameter is missing.
     * @throws FactoryException if the transform can't be created.
     */
    public abstract MathTransform create(final ParameterList parameters)
            throws MissingParameterException, FactoryException;
    
    /**
     * Returns a transform for the specified projection. The default implementation
     * invokes {@link #create(ParameterList)} with the projection's parameters.
     *
     * @param  projection The projection.
     * @return A {@link MathTransform} object of this classification.
     * @throws MissingParameterException if a mandatory parameter is missing.
     * @throws FactoryException if the transform can't be created for some other reason.
     */
    protected MathTransform create(final Projection projection)
            throws MissingParameterException, FactoryException
    {
        return create(projection.getParameters());
    }
    
    /**
     * Returns a string representation for this provider.
     */
    public String toString() {
        return Utilities.getShortClassName(this)+'['+getName(null)+']';
    }
}
