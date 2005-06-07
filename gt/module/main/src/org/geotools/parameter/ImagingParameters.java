/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
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
package org.geotools.parameter;

// J2SE dependencies
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

// JAI dependencies
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListImpl;
import javax.media.jai.OperationDescriptor;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterNotFoundException;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Wraps a JAI's {@link ParameterList}. Any change to a {@linkplain #parameter parameter value}
 * in this group is reflected into the {@linkplain #parameters underlying parameter list}, and
 * conversely. This adaptor is provided for interoperability with
 * <A HREF="http://java.sun.com/products/java-media/jai/">Java Advanced Imaging</A>.
 * A typical usage is to wrap a JAI {@linkplain OperationDescriptor operation descriptor} into an
 * {@linkplain ImagingParameterDescriptors imaging parameter descriptor} and create instances of
 * {@code ImagingParameters} through the {@link ImagingParameterDescriptors#createValue createValue}
 * method.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.2
 */
public class ImagingParameters extends AbstractParameter implements ParameterValueGroup {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1378692626023992530L;

    /**
     * The JAI's parameter list. This is also the backing store for this
     * {@linkplain ParameterValueGroup parameter value group}: all parameters
     * are actually stored in this list.
     */
    protected final ParameterList parameters;

    /**
     * The wrappers around each elements in {@link #parameters}. Keys are parameter name
     * in lower case. Will be created by {@link #createElements} only when first needed.
     */
    private transient Map elements;

    /**
     * The {@link #elements} array as a list. Will be created only when first needed.
     */
    private transient List asList;

    /**
     * Constructs a parameter group for the specified descriptor.
     */
    public ImagingParameters(final ImagingParameterDescriptors descriptor) {
        super(descriptor);
        parameters = new ParameterListImpl(descriptor.descriptor);
    }

    /**
     * Constructs a parameter group wrapping the specified JAI parameters.
     * A default {@link ImagingParameterDescriptors} is created.
     * 
     * @param name The parameter group name (usually the operation name).
     * @param parameters The JAI's parameters.
     */
    public ImagingParameters(final String name, final ParameterList parameters) {
        super(new ImagingParameterDescriptors(name, parameters.getParameterListDescriptor()));
        this.parameters = parameters;
        ensureNonNull("parameters", parameters);
    }

    /**
     * Creates and fill the {@link #elements} map.
     */
    private void createElements() {
        final ImagingParameterDescriptors descriptor = (ImagingParameterDescriptors) this.descriptor;
        final List   descriptors = descriptor.descriptors();
        final Set parameterNames = descriptor.getParameterNames();
        elements = new LinkedHashMap((int)(descriptors.size()/0.75f) + 1);
        for (final Iterator it=descriptors.iterator(); it.hasNext();) {
            final ParameterDescriptor d = (ParameterDescriptor) it.next();
            final String name = d.getName().getCode().trim().toLowerCase();
            final ParameterValue value;
            if (parameterNames.contains(name)) {
                value = new ImagingParameter(d, parameters);
            } else {
                value = new Parameter(d);
            }
            elements.put(name, value);
        }
    }

    /**
     * Returns all values in this group as an unmodifiable list. The returned list contains all
     * parameters found in the {@linkplain #parameters underlying parameter list}. In addition, it
     * may contains sources found in the JAI's {@linkplain OperationDescriptor operation descriptor}.
     */
    public synchronized List values() {
        if (asList == null) {
            if (elements == null) {
                createElements();
            }
            asList = Collections.unmodifiableList(new ArrayList(elements.values()));
        }
        return asList;
    }

    /**
     * Returns the value in this group for the specified identifier code. Getter and setter methods
     * will use directly the JAI's {@linkplain #parameters parameter list} as the underlying backing
     * store, when applicable.
     *
     * @param  name The case insensitive identifier code of the parameter to search for. 
     * @return The parameter value for the given identifier code.
     * @throws ParameterNotFoundException if there is no parameter value for the given identifier code.
     */
    public ParameterValue parameter(final String name) throws ParameterNotFoundException {
        if (elements == null) {
            createElements();
        }
        final ParameterValue value = (ParameterValue) elements.get(name.trim().toLowerCase());
        if (value != null) {
            return value;
        }
        throw new ParameterNotFoundException(Resources.format(
                  ResourceKeys.ERROR_MISSING_PARAMETER_$1, name), name);
    }

    /**
     * Always throws an exception, since JAI's {@linkplain ParameterList parameter list}
     * don't have subgroups.
     */
    public List groups(final String name) throws ParameterNotFoundException {
        throw new ParameterNotFoundException(Resources.format(
                  ResourceKeys.ERROR_MISSING_PARAMETER_$1, name), name);
    }

    /**
     * Always throws an exception, since JAI's {@linkplain ParameterList parameter list}
     * don't have subgroups.
     */
    public ParameterValueGroup addGroup(final String name)
            throws ParameterNotFoundException, IllegalStateException
    {
        throw new ParameterNotFoundException(Resources.format(
                  ResourceKeys.ERROR_MISSING_PARAMETER_$1, name), name);
    }

    /**
     * Compares the specified object with this parameter group for equality.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            // Slight optimization
            return true;
        }
        if (super.equals(object)) {
            final ImagingParameters that = (ImagingParameters) object;
            return Utilities.equals(this.parameters, that.parameters);
        }
        return false;
    }

    /**
     * Returns a hash value for this parameter group. This value doesn't need
     * to be the same in past or future versions of this class.
     */
    public int hashCode() {
        return super.hashCode()*37 + parameters.hashCode();
    }
}
