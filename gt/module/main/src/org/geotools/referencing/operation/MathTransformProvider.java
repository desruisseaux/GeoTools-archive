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
import java.util.HashMap;
import java.util.Collections;
import java.util.Locale;

// OpenGIS dependencies
import org.opengis.referencing.Info;
import org.opengis.referencing.Identifier;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.OperationParameter;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.OperationParameterGroup;
import org.opengis.parameter.GeneralOperationParameter;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.InvalidParameterNameException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * An {@linkplain OperationMethod operation method} capable to creates a {@linkplain MathTransform
 * math transform} from set of {@linkplain GeneralParameterValue parameter values}.
 * Implementations of this class should be listed in the following file:
 * <br><br>
 * <blockquote>
 * <code>META-INF/services/org.geotools.referencing.operation.OperationProvider</code>
 * </blockquote>
 * <br><br>
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
     * The {@linkplain #getParameters parameters} represented as a group of descriptors. This
     * convenience field make it easier to {@linkplain OperationParameterGroup#getParameter(String)
     * search for named parameters}.
     */
    protected final OperationParameterGroup parameters;

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
                                 final OperationParameterGroup parameters)
    {
        this(toMap(parameters, null), sourceDimensions, targetDimensions, parameters);
    }

    /**
     * Constructs a math transform provider from a set of properties.
     * The properties map is given unchanged to the
     * {@linkplain OperationMethod#OperationMethod(Map,int,int,GeneralOperationParameter[])
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
                                 final OperationParameterGroup parameters)
    {
        super(properties, sourceDimensions, targetDimensions, parameters.getParameters());
        this.parameters = parameters;
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static Map toMap(final Info parameters, Identifier[] identifiers) {
        if (identifiers == null) {
            ensureNonNull("parameters", parameters);
            identifiers = parameters.getIdentifiers();
        }
        ensureNonNull("identifiers", identifiers);
        if (identifiers.length == 0) {
            // TODO: provides a localized message.
            throw new IllegalArgumentException();
        }
        final Map properties = new HashMap(4);
        properties.put("name", (parameters!=null) ? parameters.getName(null) : identifiers[0].getCode());
        properties.put("identifiers", identifiers);
        return properties;
    }

    /**
     * Constructs a parameter group from a set of identifiers. The parameter group is
     * identified by codes provided by one or more authorities. Common authorities are
     * {@link org.geotools.metadata.citation.Citation#OPEN_GIS} and
     * {@link org.geotools.metadata.citation.Citation#EPSG}.
     *
     * @param identifiers The operation identifiers. Should contains at least one identifier.
     * @param parameters The set of parameters, or <code>null</code> or an empty array if none.
     */
    protected static OperationParameterGroup group(final Identifier[] identifiers,
                                                   final GeneralOperationParameter[] parameters)
    {
        return new org.geotools.parameter.OperationParameterGroup(toMap(null, identifiers), parameters);
    }

    /**
     * Returns the name by which this provider is identified.
     *
     * @param  locale The desired locale for the name to be returned,
     *         or <code>null</code> for a non-localized string.
     * @return The name, or <code>null</code> if not available.
     */
    public String getName(final Locale locale) {
        int key;
        if (locale==null || (key=getLocalizationKey())<0) {
            return super.getName(locale);
        }
        return Resources.getResources(locale).getString(key);
    }

    /**
     * Ensure that the given set of parameters contains only valid values. This method
     * compares all parameter names against the name declared in {@link #parameters} and
     * thrown an exception if an unknow parameter is found. It also ensure that all values
     * are assignable to the
     * {@linkplain org.geotools.parameter.OperationParameter#getValueClass expected class}, are between the
     * {@linkplain org.geotools.parameter.OperationParameter#getMinimumValue minimum} and
     * {@linkplain org.geotools.parameter.OperationParameter#getMaximumValue maximum} values and are one of the
     * {@linkplain org.geotools.parameter.OperationParameter#getValidValues set of valid values}.
     * If the value fails any of those tests, then an exception is thrown.
     *
     * @param  values The parameters values to check.
     * @throws InvalidParameterNameException if a parameter name is unknow.
     * @throws InvalidParameterValueException if a parameter has an invalid value.
     */
    private void ensureValidValues(final GeneralParameterValue[] values)
            throws InvalidParameterNameException, InvalidParameterValueException
    {
        for (int i=0; i<values.length; i++) {
            final GeneralParameterValue value = values[i];
            if (value instanceof ParameterValueGroup) {
                ensureValidValues(((ParameterValueGroup) value).getValues());
                continue;
            }
            final String name;
            final GeneralOperationParameter userDescriptor = value.getDescriptor();
            final Identifier[] identifiers = userDescriptor.getIdentifiers();
            if (identifiers!=null && identifiers.length!=0) {
                name = identifiers[0].getCode();
            } else {
                name = userDescriptor.getName(null);
            }
            final OperationParameter descriptor;
            try {
                descriptor = parameters.getParameter(name);
            } catch (ParameterNotFoundException cause) {
                final InvalidParameterNameException exception =
                      new InvalidParameterNameException(Resources.format(
                          ResourceKeys.ERROR_UNEXPECTED_PARAMETER_$1, name), name);
                exception.initCause(cause);
                throw exception;
            }
            if (value instanceof ParameterValue) {
                org.geotools.parameter.ParameterValue.ensureValidValue(descriptor, 
                                                      ((ParameterValue) value).getValue());
            }
        }
    }

    /**
     * Creates a math transform from the specified set of parameter values. The default
     * implementation ensures that the specified set of values do not contains any parameter
     * unknow to {@link #parameters}. It also ensures that all values are assignable to the
     * {@linkplain org.geotools.parameter.OperationParameter#getValueClass expected class}, are between the
     * {@linkplain org.geotools.parameter.OperationParameter#getMinimumValue minimum} and
     * {@linkplain org.geotools.parameter.OperationParameter#getMaximumValue maximum} values and are one of the
     * {@linkplain org.geotools.parameter.OperationParameter#getValidValues set of valid values}.
     * Then it wraps wraps the values in a group and invokes
     * {@link #createMathTransform(ParameterValueGroup)}.
     *
     * @param  values The parameter values.
     * @return The created math transform.
     * @throws InvalidParameterNameException if a parameter name is unknow.
     * @throws InvalidParameterValueException if a parameter has an invalid value.
     * @throws ParameterNotFoundException if a required parameter was not found.
     */
    public MathTransform createMathTransform(final GeneralParameterValue[] values)
            throws InvalidParameterNameException, InvalidParameterValueException, ParameterNotFoundException
    {
        ensureValidValues(values);
        if (values.length == 1) {
            final GeneralParameterValue value = values[0];
            if (parameters.equals(value.getDescriptor())) {
                if (value instanceof ParameterValueGroup) {
                    return createMathTransform((ParameterValueGroup) value);
                }
            }
        }
        return createMathTransform(new FallbackParameterValueGroup(parameters, values));
    }

    /**
     * Creates a math transform from the specified group of parameter values.
     *
     * @param  values The group of parameter values.
     * @return The created math transform.
     * @throws ParameterNotFoundException if a required parameter was not found.
     */
    protected abstract MathTransform createMathTransform(ParameterValueGroup values)
            throws ParameterNotFoundException;

    /**
     * Returns the resources key for {@linkplain #getName localized name}.
     * This method is for internal purpose by Geotools implementation only.
     */
    protected int getLocalizationKey() {
        return -1;
    }
}
