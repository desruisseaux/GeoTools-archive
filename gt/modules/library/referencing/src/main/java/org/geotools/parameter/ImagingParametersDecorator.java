/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2007, Geotools Project Managment Committee (PMC)
 *    (C) 2007, GeoSolutions S.A.S.
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
 */
package org.geotools.parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.media.jai.ParameterList;

import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 * This class allows to decorate instances of
 * {@link ImagingParameterDescriptors} in order to change the behaviour of some
 * parameters for the JAI operations we describe, in order to make them
 * geospatial aware.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @since 2.4.x
 *
 * @deprecated Users should avoid this class, since it is likely to be merged with
 *             {@link ImagingParameters} later.
 */
public class ImagingParametersDecorator extends ImagingParameters {
    /**
     * 
     */
    private static final long serialVersionUID = 7739016790970750018L;

    /**
     * This {@link Map} contains the {@link ImagingParameters} for which we want to to change
     * the behaviour.
     */
    protected Map replacedParameters;

    /**
     * @param descriptor
     */
    public ImagingParametersDecorator(final ImagingParameterDescriptors descriptor,
                                      final Map /* <Parameter> */replacedParameters) {
        super(descriptor);
        // replaced parameters
        ensureNonNull("replacedParameters", replacedParameters);
        this.replacedParameters = Collections.unmodifiableMap(new HashMap(replacedParameters));
    }

    /**
     * @param properties
     * @param parameters
     */
    public ImagingParametersDecorator(Map properties, ParameterList parameters,
                                      final Map /* <Parameter> */replacedParameters)
    {
        super(properties, parameters);
        // replaced parameters
        ensureNonNull("replacedParameters", replacedParameters);
        this.replacedParameters = Collections.unmodifiableMap(new HashMap(replacedParameters));
    }

    /**
     * XXX Not sure how to behave here....
     */
    public ParameterValueGroup addGroup(String name)
            throws ParameterNotFoundException, IllegalStateException
    {
        return super.addGroup(name);
    }

    /**
     * Returns a deep copy of this group of parameter values.
     */
    public Object clone() {
        final ImagingParametersDecorator clone = (ImagingParametersDecorator) super.clone();
        clone.replacedParameters=Collections.unmodifiableMap(new HashMap(this.replacedParameters));
        return clone;
    }

    /**
     * Returns the value in this group for the specified identifier code. Getter
     * and setter methods will use directly the JAI's
     * {@linkplain #parameters parameter list} as the underlying backing store,
     * when applicable.
     * 
     * @param name
     *            The case insensitive identifier code of the parameter to
     *            search for.
     * @return The parameter value for the given identifier code.
     * @throws ParameterNotFoundException
     *             if there is no parameter value for the given identifier code.
     */
    public ParameterValue parameter(String name) throws ParameterNotFoundException {
        ensureNonNull("name", name);
        name = name.trim();
        //check inside the replaced list first
        if(this.replacedParameters.containsKey(name))
                return (ParameterValue) replacedParameters.get(name);

        //then check in super implementation explotiting its 
        //exception throwing mechanism
        return super.parameter(name);
    }


    /**
     * Creates and fill the {@link #values} list. Note: this method must creates elements
     * inconditionnally and most not requires synchronization for proper working of the
     * {@link #clone} method.
     */
    void createElements() {
        super.createElements();
        //get the values from the super class
        final List values = new ArrayList(asList.size());

        //replaced the paramters we need to replace
        final Iterator it = asList.iterator();
        while (it.hasNext()) {
            //if the created list contains parameters that must be replaced let's 
            //just replaced the here
            final ParameterValue pv=(ParameterValue) it.next();
            final String name = pv.getDescriptor().getName().getCode().trim().toLowerCase();
            if (replacedParameters.containsKey(name)) {
                values.add(replacedParameters.get(name));
            } else {
                values.add(pv);
            }
        }
        asList= Collections.unmodifiableList(values);
    }
}
