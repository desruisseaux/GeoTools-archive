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

// J2SE dependencies
import java.util.Map;
import java.util.Arrays;
import java.util.Locale;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;

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
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * An {@linkplain OperationMethod operation method} capable to creates a {@linkplain MathTransform
 * math transform} from set of {@linkplain GeneralParameterValue parameter values}.
 * Implementations of this class should be listed in the following file:
 * <br>
 * <blockquote>
 * <code>META-INF/services/org.geotools.referencing.operation.OperationProvider</code>
 * </blockquote>
 * <br>
 * The {@linkplain MathTransformFactory math transform factory} will parse this file in order
 * to gets all available providers on a system. If this file is bundle in many JAR files, the
 * {@link MathTransformFactory math transform factory} will read all of them.
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
     * {@linkplain OperationMethod#OperationMethod(Map,int,int,GeneralParameterDescriptor[])
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
     * {@link org.geotools.metadata.citation.Citation#EPSG}.
     *
     * @param identifiers The operation identifiers. Should contains at least one identifier.
     * @param parameters The set of parameters, or <code>null</code> or an empty array if none.
     */
    protected static ParameterDescriptorGroup group(final Identifier[] identifiers,
                                                    final GeneralParameterDescriptor[] parameters)
    {
        ensureNonNull("identifiers", identifiers);
        if (identifiers.length == 0) {
            // TODO: localize the message.
            throw new IllegalArgumentException("Empty array");
        }
        final GenericName[] alias = new GenericName[identifiers.length];
        for (int i=0; i<alias.length; i++) {
            // TODO: create alias here.
        }
        final Map properties = new HashMap(4);
        properties.put(NAME_PROPERTY,        identifiers[0]);
        properties.put(IDENTIFIERS_PROPERTY, identifiers);
//        properties.put(ALIAS_PROPERTY,       alias);
        return new org.geotools.parameter.ParameterDescriptorGroup(properties, parameters);
    }

    /**
     * Ensure that the given set of parameters contains only valid values. This method
     * compares all parameter names against the name declared in {@link #parameters} and
     * thrown an exception if an unknow parameter is found. It also ensure that all values
     * are assignable to the
     * {@linkplain ParameterDescriptor#getValueClass expected class}, are between the
     * {@linkplain ParameterDescriptor#getMinimumValue minimum} and
     * {@linkplain ParameterDescriptor#getMaximumValue maximum} values and are one of the
     * {@linkplain ParameterDescriptor#getValidValues set of valid values}.
     * If the value fails any of those tests, then an exception is thrown.
     *
     * @param  values The parameters values to check.
     * @throws InvalidParameterNameException if a parameter name is unknow.
     * @throws InvalidParameterValueException if a parameter has an invalid value.
     */
    private void ensureValidValues(final Collection/*<GeneralParameterValue>*/ values)
            throws InvalidParameterNameException, InvalidParameterValueException
    {
        for (final Iterator it=values.iterator(); it.hasNext();) {
            final GeneralParameterValue value = (GeneralParameterValue) it.next();
            if (value instanceof ParameterValueGroup) {
                ensureValidValues(((ParameterValueGroup) value).values());
                continue;
            }
            final String name;
            final GeneralParameterDescriptor userDescriptor = value.getDescriptor();
            final Identifier[] identifiers = userDescriptor.getIdentifiers();
            if (identifiers!=null && identifiers.length!=0) {
                name = identifiers[0].getCode();
            } else {
                name = userDescriptor.getName().getCode();
            }
            final GeneralParameterDescriptor descriptor;
            try {
                descriptor = getParameters().descriptor(name);
            } catch (ParameterNotFoundException cause) {
                final InvalidParameterNameException exception =
                      new InvalidParameterNameException(Resources.format(
                          ResourceKeys.ERROR_UNEXPECTED_PARAMETER_$1, name), name);
                exception.initCause(cause);
                throw exception;
            }
            if (value instanceof ParameterValue) {
                org.geotools.parameter.Parameter.ensureValidValue(
                        (ParameterDescriptor) descriptor, 
                        ((ParameterValue) value).getValue());
            }
        }
    }

    /**
     * Creates a math transform from the specified set of parameter values. The default
     * implementation ensures that the specified set of values do not contains any parameter
     * unknow to {@link #parameters}. It also ensures that all values are assignable to the
     * {@linkplain ParameterDescriptor#getValueClass expected class}, are between the
     * {@linkplain ParameterDescriptor#getMinimumValue minimum} and
     * {@linkplain ParameterDescriptor#getMaximumValue maximum} values and are one of the
     * {@linkplain ParameterDescriptor#getValidValues set of valid values}.
     * Then it wraps wraps the values in a group and invokes
     * {@link #createMathTransform(ParameterValueGroup)}.
     *
     * @param  values The parameter values.
     * @return The created math transform.
     * @throws InvalidParameterNameException if a parameter name is unknow.
     * @throws InvalidParameterValueException if a parameter has an invalid value.
     * @throws ParameterNotFoundException if a required parameter was not found.
     * @throws FactoryException if the math transform can't be created for some other reason
     *         (for example a required file was not found).
     */
    public MathTransform createMathTransform(final GeneralParameterValue[] values)
            throws InvalidParameterNameException, InvalidParameterValueException,
                   ParameterNotFoundException, FactoryException
    {
        ensureValidValues(Arrays.asList(values));
        final ParameterDescriptorGroup descriptor = getParameters();
        if (values.length == 1) {
            final GeneralParameterValue value = values[0];
            if (value instanceof ParameterValueGroup) {
                if (descriptor.equals(value.getDescriptor())) {
                    return createMathTransform((ParameterValueGroup) value);
                }
            }
        }
        return createMathTransform(new FallbackParameterValueGroup(descriptor, values));
    }

    /**
     * Creates a math transform from the specified group of parameter values.
     *
     * @param  values The group of parameter values.
     * @return The created math transform.
     * @throws ParameterNotFoundException if a required parameter was not found.
     * @throws FactoryException if the math transform can't be created for some other reason
     *         (for example a required file was not found). Checked exceptions like
     *         {@link java.io.IOException} should be wrapped in this exception.
     *
     * @todo Make this method public, and specify in the documentation that the implementors
     *       must invokes <code>ensureValidValues(values)</code> first.
     */
    protected abstract MathTransform createMathTransform(ParameterValueGroup values)
            throws ParameterNotFoundException, FactoryException;

    /**
     * Returns the parameter value for the specified operation parameter.
     * This convenience method is used by subclasses for initializing
     * {@linkplain MathTransform math transform} from a set of parameters.
     *
     * @param  group The parameter value group to search into.
     * @param  param The parameter to look for.
     * @return The requested parameter value.
     * @throws ParameterNotFoundException if the parameter is not found.
     *
     * @deprecated This is a bad example of what to do.
     */
    private static ParameterValue getValue(final ParameterValueGroup group,
                                           final ParameterDescriptor param)
            throws ParameterNotFoundException
    {
        return group.parameter(param.getName().getCode());
    }

    /**
     * Returns the parameter value for the specified operation parameter.
     * This convenience method is used by subclasses for initializing
     * {@linkplain MathTransform math transform} from a set of parameters.
     *
     * @param  group The parameter value group to search into.
     * @param  param The parameter to look for.
     * @return The requested parameter value.
     * @throws ParameterNotFoundException if the parameter is not found.
     *
     * @deprecated This is a bad example of what to do.
     */
    protected static int intValue(final ParameterValueGroup group,
                                  final ParameterDescriptor param)
            throws ParameterNotFoundException
    {
        return getValue(group, param).intValue();
    }

    /**
     * Returns the parameter value for the specified operation parameter.
     * This convenience method is used by subclasses for initializing
     * {@linkplain MathTransform math transform} from a set of parameters.
     *
     * @param  group The parameter value group to search into.
     * @param  param The parameter to look for.
     * @return The requested parameter value.
     * @throws ParameterNotFoundException if the parameter is not found.
     *
     * @todo What to do with unit? (if some action is taken, don't forget to check
     *       for null units).
     *
     * @deprecated This is a bad example of what to do.
     */
    protected static double doubleValue(final ParameterValueGroup group,
                                        final ParameterDescriptor param)
            throws ParameterNotFoundException
    {
        return getValue(group, param).doubleValue();
    }
    
    /**
     * Returns the parameter value for the specified operation parameter.
     * This convenience method is used by subclasses for initializing
     * {@linkplain MathTransform math transform} from a set of parameters.
     *
     * @param  group The parameter value group to search into.
     * @param  param The parameter to look for.
     * @return The requested parameter value.
     * @throws ParameterNotFoundException if the parameter is not found.
     *
     * @deprecated This is a bad example of what to do.
     */
    protected static String stringValue(final ParameterValueGroup group,
                                       final ParameterDescriptor  param)
            throws ParameterNotFoundException
    {
        return getValue(group, param).stringValue();
    }

    /**
     * Returns the resources key for {@linkplain #getName localized name}.
     * This method is for internal purpose by Geotools implementation only.
     *
     * @deprecated No longer used by this method. We need to construct
     *             an {@link InternationalString} instead.
     */
    protected int getLocalizationKey() {
        return -1;
    }
}
