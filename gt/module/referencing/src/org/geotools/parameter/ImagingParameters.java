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
import java.lang.reflect.Field;
import java.lang.reflect.Method;

// JAI dependencies
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListImpl;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.OperationDescriptor;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.InvalidParameterNameException;
import org.opengis.util.GenericName;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.referencing.AbstractIdentifiedObject;


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
 * @since 2.2
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ImagingParameters extends AbstractParameter implements ParameterValueGroup {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1378692626023992530L;

    /**
     * The JAI's parameter list. This is also the backing store for this
     * {@linkplain ParameterValueGroup parameter value group}: all "ordinary" parameters
     * (i.e. <strong>not</strong> including {@linkplain ParameterBlockJAI#getSources sources})
     * are actually stored in this list.
     * <P>
     * If the {@linkplain ImagingParameterDescriptors#descriptor JAI descriptor} is an instance
     * of {@link OperationDescriptor}, then this parameter list is also an instance of
     * {@link ParameterBlockJAI}. The {@linkplain ParameterBlockJAI#getSources sources}
     * must be handled separatly, because the source type for a JAI operator (typically
     * {@link java.awt.image.RenderedImage}) is not the same than the source type for a
     * coverage operation (typically {@link org.opengis.coverage.GridCoverage}).
     */
    public final ParameterList parameters;

    /**
     * The wrappers around each elements in {@link #parameters}. Will be created by
     * {@link #createElements} only when first needed.
     */
    private transient List/*<ParameterValue>*/ values;

    /**
     * A view of {@link #values} as an immutable list. Will be constructed only when first
     * needed. Note that while this list may be immutable, <strong>elements</strong> in this
     * list stay modifiable. The goal is to allows the following idiom:
     *
     * <blockquote><pre>
     * values().get(i).setValue(myValue);
     * </pre></blockquote>
     */
    private transient List/*<ParameterValue>*/ asList;

    /**
     * Constructs a parameter group for the specified descriptor.
     */
    public ImagingParameters(final ImagingParameterDescriptors descriptor) {
        super(descriptor);
        if (descriptor.operation instanceof OperationDescriptor) {
            // Parameters with sources
            parameters = new ParameterBlockJAI((OperationDescriptor) descriptor.operation,
                                                                     descriptor.registryMode);
        } else {
            // Parameters without sources
            parameters = new ParameterListImpl(descriptor.descriptor);
        }
    }

    /**
     * Constructs a parameter group wrapping the specified JAI parameters.
     * A default {@link ImagingParameterDescriptors} is created.
     * 
     * @param properties Set of properties. Should contains at least {@code "name"}.
     * @param parameters The JAI's parameters.
     */
    public ImagingParameters(final Map properties, final ParameterList parameters) {
        super(new ImagingParameterDescriptors(properties, parameters.getParameterListDescriptor()));
        this.parameters = parameters;
        ensureNonNull("parameters", parameters);
    }

    /**
     * Creates and fill the {@link #values} list. Note: this method must creates elements
     * inconditionnally and most not requires synchronization for proper working of the
     * {@link #clone} method.
     */
    private void createElements() {
        final ImagingParameterDescriptors descriptor = (ImagingParameterDescriptors) this.descriptor;
        final List   descriptors = descriptor.descriptors();
        final Set parameterNames = descriptor.getParameterNames();
        values = new ArrayList(descriptors.size());
        for (final Iterator it=descriptors.iterator(); it.hasNext();) {
            final ParameterDescriptor d = (ParameterDescriptor) it.next();
            String name = d.getName().getCode().trim().toLowerCase();
            final ParameterValue value;
            if (parameterNames.contains(name)) {
                /*
                 * Uses 'parameters' as the backing store.
                 */
                value = new ImagingParameter(d, parameters);
            } else {
                /*
                 * In theory, we should uses ParameterBlock sources. However, we can't because
                 * the type is not the same: JAI operations typically expect a RenderedImage
                 * source, while coverage operations typically expect a GridCoverage source.
                 * The value will be stored separatly, and the coverage framework will need
                 * to handle it itself.
                 */
                value = new Parameter(d);
            }
            values.add(value);
        }
        /*
         * Checks for name clashes.
         */
        final int size = values.size();
        for (int j=0; j<size; j++) {
            final String name;
            name = ((ParameterValue) values.get(j)).getDescriptor().getName().getCode().trim();
            for (int i=0; i<size; i++) {
                if (i != j) {
                    final ParameterDescriptor d = (ParameterDescriptor)
                                                ((ParameterValue) values.get(i)).getDescriptor();
                    if (AbstractIdentifiedObject.nameMatches(d, name)) {
                        throw new InvalidParameterNameException(Errors.format(
                                ErrorKeys.PARAMETER_NAME_CLASH_$4,
                                d.getName().getCode(), new Integer(j),   // The duplicated name
                                name,                  new Integer(i)),  // The existing name
                                name);
                    }
                }
            }
        }
        asList = Collections.unmodifiableList(values);
    }

    /**
     * Returns all values in this group as an unmodifiable list. The returned list contains all
     * parameters found in the {@linkplain #parameters underlying parameter list}. In addition, it
     * may contains sources found in the JAI's {@linkplain OperationDescriptor operation descriptor}.
     */
    public synchronized List values() {
        if (asList == null) {
            createElements();
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
    public synchronized ParameterValue parameter(String name)
            throws ParameterNotFoundException
    {
        ensureNonNull("name", name);
        name = name.trim();
        if (values == null) {
            createElements();
        }
        final int size = values.size();
        for (int i=0; i<size; i++) {
            final ParameterValue value = (ParameterValue) values.get(i);
            if (AbstractIdentifiedObject.nameMatches(value.getDescriptor(), name)) {
                return value;
            }
        }
        throw new ParameterNotFoundException(Errors.format(
                  ErrorKeys.MISSING_PARAMETER_$1, name), name);
    }

    /**
     * Always throws an exception, since JAI's {@linkplain ParameterList parameter list}
     * don't have subgroups.
     */
    public List groups(final String name) throws ParameterNotFoundException {
        throw new ParameterNotFoundException(Errors.format(
                  ErrorKeys.MISSING_PARAMETER_$1, name), name);
    }

    /**
     * Always throws an exception, since JAI's {@linkplain ParameterList parameter list}
     * don't have subgroups.
     */
    public ParameterValueGroup addGroup(final String name)
            throws ParameterNotFoundException, IllegalStateException
    {
        throw new ParameterNotFoundException(Errors.format(
                  ErrorKeys.MISSING_PARAMETER_$1, name), name);
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

    /**
     * Returns a deep copy of this group of parameter values.
     */
    public Object clone() {
        final ImagingParameters copy = (ImagingParameters) super.clone();
        try {
            final Method cloneMethod = parameters.getClass().getMethod("clone", (Class[])null);
            final Field  paramField  = ImagingParameters.class.getField("parameters");
            paramField.setAccessible(true); // Will work only with J2SE 1.5 or above.
            paramField.set(copy, cloneMethod.invoke(parameters, (Object[]) null));
        } catch (Exception exception) {
            // TODO: localize.
            // TODO: Use constructor with Throwable when we will be allowed to compile for J2SE 1.5.
            UnsupportedOperationException e = new UnsupportedOperationException("Clone not supported.");
            e.initCause(exception);
            throw e;
        }
        if (copy.values != null) {
            copy.createElements();
            for (int i=values.size(); --i>=0;) {
                final ParameterValue value = (ParameterValue) values.get(i);
                if (value instanceof Parameter) {
                    copy.values.set(i, (ParameterValue) value.clone());
                }
            }
        }
        return copy;
    }
}
