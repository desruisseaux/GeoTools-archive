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
package org.geotools.referencing.operation;

// J2SE dependencies and extensions
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.InvalidParameterCardinalityException;
import org.opengis.parameter.InvalidParameterNameException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Operation;
import org.opengis.referencing.operation.Projection;
import org.opengis.util.GenericName;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.XArray;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;
import org.geotools.referencing.wkt.Formatter;


/**
 * An {@linkplain OperationMethod operation method} capable to creates a {@linkplain MathTransform
 * math transform} from set of {@linkplain GeneralParameterValue parameter values}.
 * Implementations of this class should be listed in the following file:
 *
 * <blockquote>
 * <P><code>META-INF/services/org.geotools.referencing.operation.OperationProvider</code></P>
 * </blockquote>
 *
 * <P>The {@linkplain MathTransformFactory math transform factory} will parse this file in order
 * to gets all available providers on a system. If this file is bundle in many JAR files, the
 * {@link MathTransformFactory math transform factory} will read all of them.</P>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class MathTransformProvider extends OperationMethod {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 7530475536803158473L;

    /**
     * Constructs a math transform provider from a set of parameters. The provider
     * {@linkplain #getIdentifiers identifiers} will be the same than the parameter
     * ones.
     *
     * @param sourceDimensions Number of dimensions in the source CRS of this operation method.
     * @param targetDimensions Number of dimensions in the target CRS of this operation method.
     * @param parameters The set of parameters (never <code>null</code>).
     */
    public MathTransformProvider(final int sourceDimensions,
                                 final int targetDimensions,
                                 final ParameterDescriptorGroup parameters)
    {
        this(toMap(parameters), sourceDimensions, targetDimensions, parameters);
    }

    /**
     * Constructs a math transform provider from a set of properties.
     * The properties map is given unchanged to the
     * {@linkplain OperationMethod#OperationMethod(Map,int,int,ParameterDescriptorGroup)
     * super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param sourceDimensions Number of dimensions in the source CRS of this operation method.
     * @param targetDimensions Number of dimensions in the target CRS of this operation method.
     * @param parameters The set of parameters (never <code>null</code>).
     */
    public MathTransformProvider(final Map properties,
                                 final int sourceDimensions,
                                 final int targetDimensions,
                                 final ParameterDescriptorGroup parameters)
    {
        super(properties, sourceDimensions, targetDimensions, parameters);        
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static Map toMap(final IdentifiedObject parameters) {
        ensureNonNull("parameters", parameters);
        final Map properties = new HashMap(4);
        properties.put(NAME_PROPERTY,        parameters.getName());
        properties.put(IDENTIFIERS_PROPERTY, parameters.getIdentifiers());
        properties.put(ALIAS_PROPERTY,       parameters.getAlias());
        return properties;
    }

    /**
     * Returns the operation type. It may be
     * <code>{@linkplain org.opengis.referencing.operation.Operation}.class</code>,
     * <code>{@linkplain org.opengis.referencing.operation.Conversion}.class</code>,
     * <code>{@linkplain org.opengis.referencing.operation.Projection}.class</code>,
     * <cite>etc</cite>.
     *
     * The default implementation returns <code>Operation.class</code>.
     * Subclass should overrides this methods and returns the appropriate
     * OpenGIS interface type (<strong>not</strong> the implementation type).
     */
    protected Class getOperationType() {
        return Operation.class;
    }

    /**
     * Constructs a parameter descriptor from a set of alias. The parameter is
     * identified by codes provided by one or more authorities. Common authorities are
     * {@link org.geotools.metadata.citation.Citation#OPEN_GIS} and
     * {@link org.geotools.metadata.citation.Citation#EPSG} for example.
     *
     * <P>The first entry in the <code>identifiers</code> array is both the
     * {@linkplain ParameterDescriptor#getName main name} and the
     * {@linkplain ParameterDescriptor#getIdentifiers identifiers}.
     * All others are {@linkplain ParameterDescriptor#getAlias aliases}.</P>
     *
     * @param identifiers  The parameter identifiers. Most contains at least one entry.
     * @param defaultValue The default value for the parameter, or {@link Double#NaN} if none.
     * @param minimum The minimum parameter value, or {@link Double#NEGATIVE_INFINITY} if none.
     * @param maximum The maximum parameter value, or {@link Double#POSITIVE_INFINITY} if none.
     * @param unit    The unit for default, minimum and maximum values.
     */
    protected static ParameterDescriptor createDescriptor(final Identifier[] identifiers,
                                                          final double       defaultValue,
                                                          final double       minimum,
                                                          final double       maximum,
                                                          final Unit         unit)
    {
        return new org.geotools.parameter.ParameterDescriptor(
                toMap(identifiers), defaultValue, minimum, maximum, unit, true);
    }

    /**
     * Constructs an optional parameter descriptor from a set of alias.
     * The parameter is identified as with {@link #createDescriptor}.
     *
     * @param identifiers The parameter identifiers. Most contains at least one entry.
     * @param minimum The minimum parameter value, or {@link Double#NEGATIVE_INFINITY} if none.
     * @param maximum The maximum parameter value, or {@link Double#POSITIVE_INFINITY} if none.
     * @param unit    The unit for default, minimum and maximum values.
     */
    protected static ParameterDescriptor createOptionalDescriptor(final Identifier[] identifiers,
                                                                  final double       minimum,
                                                                  final double       maximum,
                                                                  final Unit         unit)
    {
        return new org.geotools.parameter.ParameterDescriptor(
                toMap(identifiers), Double.NaN, minimum, maximum, unit, false);
    }

    /**
     * Constructs a parameter group from a set of alias. The parameter group is
     * identified by codes provided by one or more authorities. Common authorities are
     * {@link org.geotools.metadata.citation.Citation#OPEN_GIS} and
     * {@link org.geotools.metadata.citation.Citation#EPSG} for example.
     *
     * <P>The first entry in the <code>identifiers</code> array is both the
     * {@linkplain ParameterDescriptorGroup#getName main name} and the
     * {@linkplain ParameterDescriptorGroup#getIdentifiers identifiers}.
     * All others are {@linkplain ParameterDescriptorGroup#getAlias aliases}.</P>
     *
     * @param identifiers  The operation identifiers. Most contains at least one entry.
     * @param parameters   The set of parameters, or <code>null</code> or an empty array if none.
     */
    protected static ParameterDescriptorGroup createDescriptorGroup(
                final Identifier[] identifiers, final GeneralParameterDescriptor[] parameters)
    {
        return new org.geotools.parameter.ParameterDescriptorGroup(toMap(identifiers), parameters);
    }

    /**
     * Put the identifiers into a properties map suitable for {@link IdentifiedObject}
     * constructor.
     */
    private static Map toMap(final Identifier[] identifiers) {
        ensureNonNull("identifiers", identifiers);
        if (identifiers.length == 0) {
            throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_EMPTY_ARRAY));
        }
        int count = 0;
        GenericName[] alias = new GenericName[identifiers.length];
        for (int i=0; i<identifiers.length; i++) {
            if (identifiers[i] instanceof GenericName) {
                alias[count++] = (GenericName) identifiers[i];
            }
        }
        alias = (GenericName[]) XArray.resize(alias, count);
        final Map properties = new HashMap(4, 0.8f);
        properties.put(NAME_PROPERTY,        identifiers[0]);
        properties.put(IDENTIFIERS_PROPERTY, identifiers[0]);
        properties.put(ALIAS_PROPERTY,       alias);
        return properties;
    }

    /**
     * Ensures that the given set of parameters contains only valid values.
     * This method compares all parameter names against the names declared in the
     * {@linkplain #getParameters operation method parameter descriptor}. If an unknow
     * parameter name is found, then an {@link InvalidParameterNameException} is thrown.
     * This method also ensures that all values are assignable to the
     * {@linkplain ParameterDescriptor#getValueClass expected class}, are between the
     * {@linkplain ParameterDescriptor#getMinimumValue minimum} and
     * {@linkplain ParameterDescriptor#getMaximumValue maximum} values and are one of the
     * {@linkplain ParameterDescriptor#getValidValues set of valid values}.
     * If the value fails any of those tests, then an
     * {@link InvalidParameterValueException} is thrown.
     *
     * @param  values The parameters values to check.
     * @return The parameter values to use for {@linkplain MathTransform math transform}
     *         construction. May be different than the supplied <code>values</code>
     *         argument if some missing values needed to be filled with default values.
     * @throws InvalidParameterNameException if a parameter name is unknow.
     * @throws InvalidParameterValueException if a parameter has an invalid value.
     */
    protected ParameterValueGroup ensureValidValues(final ParameterValueGroup values)
            throws InvalidParameterNameException, InvalidParameterValueException
    {
        final ParameterDescriptorGroup parameters = getParameters();
        final GeneralParameterDescriptor descriptor = values.getDescriptor();
        if (parameters.equals(descriptor)) {
            /*
             * Since the "official" parameter descriptor was used, the descriptor should
             * have already enforced argument validity. Concequently, there is no need to
             * performs the check and we will avoid it as a performance enhancement.
             */
            return values;
        }
        /*
         * Copy the all values from the user-supplied group to the provider-supplied group.
         * The provider group should performs all needed checks. Furthermore, it is suppliers
         * responsability to know about alias (e.g. OGC, EPSG, ESRI), while the user will
         * probably use the name from only one authority. With a copy, we gives a chances to
         * the provider-supplied parameters to uses its alias for understanding the user
         * parameter names.
         */
        final ParameterValueGroup copy = (ParameterValueGroup) parameters.createValue();
        copy(values, copy);
        return copy;
    }

    /**
     * Implementation of <code>ensureValidValues</code>, to be invoked recursively
     * if the specified values contains sub-groups of values. This method copy all
     * values from the user-supplied parameter values into the provider-supplied
     * one. The provider one should understand alias, and performs name conversion
     * as well as argument checking on the fly.
     *
     * @param  values The parameters values to copy.
     * @param  copy   The parameters values where to put the copy.
     * @throws InvalidParameterNameException if a parameter name is unknow.
     * @throws InvalidParameterValueException if a parameter has an invalid value.
     */
    private static void copy(final ParameterValueGroup values,
                             final ParameterValueGroup copy)
            throws InvalidParameterNameException, InvalidParameterValueException
    {
        for (final Iterator it=values.values().iterator(); it.hasNext();) {
            final GeneralParameterValue value = (GeneralParameterValue) it.next();
            final String name = value.getDescriptor().getName().getCode();
            if (value instanceof ParameterValueGroup) {
                /*
                 * Contains sub-group - invokes 'copy' recursively.
                 */
                final GeneralParameterDescriptor descriptor;
                descriptor = ((ParameterDescriptorGroup) copy.getDescriptor()).descriptor(name);
                if (descriptor instanceof ParameterDescriptorGroup) {
                    final ParameterValueGroup groups = (ParameterValueGroup) descriptor.createValue();
                    copy((ParameterValueGroup) value, groups);
                    values.groups(name).add(groups);
                    continue;
                } else {
                    throw new InvalidParameterNameException(Resources.format(
                              ResourceKeys.ERROR_UNEXPECTED_PARAMETER_$1, name), name);
                }
            }
            /*
             * Single parameter - copy the value, with special care for value with units.
             */
            final ParameterValue source = (ParameterValue) value;
            final ParameterValue target;
            try {
                target = copy.parameter(name);
            } catch (ParameterNotFoundException cause) {
                final InvalidParameterNameException exception =
                      new InvalidParameterNameException(Resources.format(
                          ResourceKeys.ERROR_UNEXPECTED_PARAMETER_$1, name), name);
                exception.initCause(cause);
                throw exception;
            }
            final Object v  = source.getValue();
            final Unit unit = source.getUnit();
            if (unit == null) {
                target.setValue(v);
            } else if (v instanceof Number) {
                target.setValue(((Number) v).doubleValue(), unit);
            } else if (v instanceof double[]) {
                target.setValue((double[]) v, unit);
            } else {
                throw new InvalidParameterValueException(Resources.format(
                          ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, name, v), name, v);
            }
        }
    }

    /**
     * Returns the parameter value for the specified operation parameter.
     * This convenience method is used by subclasses for initializing
     * {@linkplain MathTransform math transform} from a set of parameters.
     *
     * @param  param The parameter to look for.
     * @param  group The parameter value group to search into.
     * @return The requested parameter value.
     * @throws ParameterNotFoundException if the parameter is not found.
     */
    private static ParameterValue getValue(final ParameterDescriptor param,
                                           final ParameterValueGroup group)
            throws ParameterNotFoundException
    {
        /*
         * Search for an identifier matching the group's authority, if any.
         * This is needed if the parameter values group was created from an
         * EPSG database for example: we need to use the EPSG names instead
         * of the OGC ones.
         */
        String name = param.getName().getCode();
        final Citation authority = group.getDescriptor().getName().getAuthority();
        if (authority != null) {
            final GenericName[] alias = param.getAlias();
            for (int i=0; i<alias.length; i++) {
                final GenericName scope = alias[i].getScope();
                if (scope != null) {
                    if (org.geotools.metadata.citation.Citation.titleMatches(authority, scope.toString())) {
                        name = alias[i].asLocalName().toString();
                        break;
                    }
                }
            }
        }
        if (param.getMinimumOccurs() != 0) {
            return group.parameter(name);
        }
        /*
         * The parameter is optional. We don't want to invokes 'parameter(name)', because we don't
         * want to create a new parameter is the user didn't supplied one. Search the parameter
         * ourself (so we don't create any), and returns null if we don't find any.
         *
         * TODO: A simplier solution would be to add a 'isDefined' method in GeoAPI,
         *       or something similar.
         */
        final GeneralParameterDescriptor search;
        search = ((ParameterDescriptorGroup) group.getDescriptor()).descriptor(name);
        if (search instanceof ParameterDescriptor) {
            for (final Iterator it=group.values().iterator(); it.hasNext();) {
                final GeneralParameterValue candidate = (GeneralParameterValue) it.next();
                if (search.equals(candidate.getDescriptor())) {
                    return (ParameterValue) candidate;
                }
            }        
        }
        return null;
    }
    
    /**
     * Returns the parameter value for the specified operation parameter.
     * This convenience method is used by subclasses for initializing
     * {@linkplain MathTransform math transform} from a set of parameters.
     *
     * @param  param The parameter to look for.
     * @param  group The parameter value group to search into.
     * @return The requested parameter value, or {@code null} if {@code param} is
     *         {@linkplain #createOptionalDescriptor optional} and the user didn't
     *         provided any value.
     * @throws ParameterNotFoundException if the parameter is not found.
     */
    protected static String stringValue(final ParameterDescriptor param,
                                        final ParameterValueGroup group)
            throws ParameterNotFoundException
    {
        final ParameterValue value = getValue(param, group);
        return (value!=null) ? value.stringValue() : null;
    }

    /**
     * Returns the parameter value for the specified operation parameter.
     * This convenience method is used by subclasses for initializing
     * {@linkplain MathTransform math transform} from a set of parameters.
     *
     * @param  param The parameter to look for.
     * @param  group The parameter value group to search into.
     * @return The requested parameter value, or {@code 0} if {@code param} is
     *         {@linkplain #createOptionalDescriptor optional} and the user didn't
     *         provided any value.
     * @throws ParameterNotFoundException if the parameter is not found.
     */
    protected static int intValue(final ParameterDescriptor param,
                                  final ParameterValueGroup group)
            throws ParameterNotFoundException
    {
        final ParameterValue value = getValue(param, group);
        return (value!=null) ? value.intValue() : 0;
    }

    /**
     * Returns the parameter value for the specified operation parameter.
     * Values are automatically converted into the standard units specified
     * by the supplied {@code param} argument.
     * This convenience method is used by subclasses for initializing
     * {@linkplain MathTransform math transform} from a set of parameters.
     *
     * @param  param The parameter to look for.
     * @param  group The parameter value group to search into.
     * @return The requested parameter value, or {@code NaN} if {@code param} is
     *         {@linkplain #createOptionalDescriptor optional} and the user didn't
     *         provided any value.
     * @throws ParameterNotFoundException if the parameter is not found.
     */
    protected static double doubleValue(final ParameterDescriptor param,
                                        final ParameterValueGroup group)
            throws ParameterNotFoundException
    {
        final Unit unit = param.getUnit();
        final ParameterValue value = getValue(param, group);
        return (value==null) ? Double.NaN :
                (unit!=null) ? value.doubleValue(unit) : value.doubleValue();
    }

    /**
     * Creates a math transform from the specified group of parameter values.
     * Subclasses should implements this method as in the example below:
     *
     * <blockquote><pre>
     * double semiMajor = values.parameter("semi_major").doubleValue(SI.METER);
     * double semiMinor = values.parameter("semi_minor").doubleValue(SI.METER);
     * // etc...
     * </pre></blockquote>
     *
     * @param  values The group of parameter values.
     * @return The created math transform.
     * @throws InvalidParameterNameException if the values contains an unknow parameter.
     * @throws ParameterNotFoundException if a required parameter was not found.
     * @throws InvalidParameterValueException if a parameter has an invalid value.
     * @throws FactoryException if the math transform can't be created for some other reason
     *         (for example a required file was not found).
     */
    protected abstract MathTransform createMathTransform(ParameterValueGroup values)
            throws InvalidParameterNameException,
                   ParameterNotFoundException,
                   InvalidParameterValueException,
                   FactoryException;

    /**
     * Returns the operation method for the specified math transform. This method is invoked
     * automatically after <code>createMathTransform</code>. The default implementation returns
     * <code>this</code>, which is appropriate for the vast majority of cases. An exception is
     * affine transform, which provides different methods for different matrix sizes.
     */
    protected org.opengis.referencing.operation.OperationMethod getMethod(final MathTransform mt) {
        return this;
    }
    
    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name.
     */
    protected String formatWKT(final Formatter formatter) {
        final Class type = getOperationType();
        if (Projection.class.isAssignableFrom(type)) {
            return super.formatWKT(formatter);
        }
        formatter.setInvalidWKT();
        return "OperationMethod";
    }
}
