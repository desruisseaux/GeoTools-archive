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
import java.util.Map;
import java.util.Arrays;
import java.util.Locale;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString; // For javadoc
import org.opengis.metadata.Identifier;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
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

// Geotools dependencies
import org.geotools.resources.XArray;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


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
     * @param identifiers  The operation identifier. Most contains at least one entry.
     * @param parameters   The set of parameters, or <code>null</code> or an empty array if none.
     */
    protected static ParameterDescriptorGroup group(final Identifier[] identifiers,
                                                    final GeneralParameterDescriptor[] parameters)
    {
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
        return new org.geotools.parameter.ParameterDescriptorGroup(properties, parameters);
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
        if (parameters.equals(values.getDescriptor())) {
            /*
             * Since the "official" parameter descriptor was used, the descriptor should
             * have already enforced argument validity. Concequently, there is no need to
             * performs the check and we will avoid it as a performance enhancement,
             * unless assertions are enabled.
             */
            IllegalArgumentException e;
            assert (e=assertValidValues(values.values(), parameters))==null : e;
            return values;
        }
        if (values instanceof FallbackParameterValueGroup) {
            if (parameters.equals(((FallbackParameterValueGroup) values).fallback)) {
                return values;
            }
        }
        final Collection asList = values.values();
        ensureValidValues(asList, parameters);
        return new FallbackParameterValueGroup(parameters, (GeneralParameterValue[])
                   asList.toArray(new GeneralParameterValue[asList.size()]));
    }

    /**
     * Implementation of <code>ensureValidValues</code>, to be invoked recursively
     * if the specified values contains sub-groups of values.
     *
     * @param  values The parameters values to check.
     * @param  parameters The <strong>expected</strong> parameter descriptor
     *         (not the supplied values descriptor).
     * @throws InvalidParameterNameException if a parameter name is unknow.
     * @throws InvalidParameterValueException if a parameter has an invalid value.
     */
    private static void ensureValidValues(final Collection/*<GeneralParameterValue>*/ values,
                                          final ParameterDescriptorGroup parameters)
            throws InvalidParameterNameException, InvalidParameterValueException
    {
        for (final Iterator it=values.iterator(); it.hasNext();) {
            final GeneralParameterValue value = (GeneralParameterValue) it.next();
            final String name = value.getDescriptor().getName().getCode();
            final GeneralParameterDescriptor descriptor;
            try {
                descriptor = parameters.descriptor(name);
            } catch (ParameterNotFoundException cause) {
                final InvalidParameterNameException exception =
                      new InvalidParameterNameException(Resources.format(
                          ResourceKeys.ERROR_UNEXPECTED_PARAMETER_$1, name), name);
                exception.initCause(cause);
                throw exception;
            }
            if (value instanceof ParameterValueGroup) {
                if (descriptor instanceof ParameterDescriptorGroup) {
                    ensureValidValues(((ParameterValueGroup) value).values(),
                                       (ParameterDescriptorGroup) descriptor);
                    continue;
                }
                throw new InvalidParameterNameException(Resources.format(
                          ResourceKeys.ERROR_UNEXPECTED_PARAMETER_$1, name), name);
            }
            if (value instanceof ParameterValue) {
                org.geotools.parameter.Parameter.ensureValidValue(
                        (ParameterDescriptor) descriptor, 
                        ((ParameterValue) value).getValue());
            }
        }
    }

    /**
     * If arguments are invalid, returns the exception instead of throwing it.
     * This method is for internal use by assertions only; they will wrap the
     * exception into an {@link AssertionError}.
     */
    private static IllegalArgumentException assertValidValues(
            final Collection/*<GeneralParameterValue>*/ values,
            final ParameterDescriptorGroup parameters)
    {
        try {
            ensureValidValues(values, parameters);
        } catch (IllegalArgumentException exception) {
            return exception;
        }
        return null;
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
        return group.parameter(param.getName().getCode());
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
    protected static String stringValue(final ParameterDescriptor param,
                                        final ParameterValueGroup group)
            throws ParameterNotFoundException
    {
        return getValue(param, group).stringValue();
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
    protected static int intValue(final ParameterDescriptor param,
                                  final ParameterValueGroup group)
            throws ParameterNotFoundException
    {
        return getValue(param, group).intValue();
    }

    /**
     * Returns the parameter value for the specified operation parameter.
     * Values are automatically converted into the standard units specified
     * by the supplied <code>param</code> argument.
     * This convenience method is used by subclasses for initializing
     * {@linkplain MathTransform math transform} from a set of parameters.
     *
     * @param  param The parameter to look for.
     * @param  group The parameter value group to search into.
     * @return The requested parameter value.
     * @throws ParameterNotFoundException if the parameter is not found.
     */
    protected static double doubleValue(final ParameterDescriptor param,
                                        final ParameterValueGroup group)
            throws ParameterNotFoundException
    {
        final Unit unit = param.getUnit();
        final ParameterValue value = getValue(param, group);
        return (unit!=null) ? value.doubleValue(unit) : value.doubleValue();
    }
}
