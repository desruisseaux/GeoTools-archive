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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.Locale;
import java.net.URI;
import java.net.URISyntaxException;

// JAI dependencies
import javax.media.jai.util.Range;
import javax.media.jai.EnumeratedParameter;
import javax.media.jai.OperationDescriptor;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.RegistryElementDescriptor;
import javax.media.jai.registry.RenderedRegistryMode;

// OpenGIS dependencies
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Role;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.metadata.iso.citation.ContactImpl;
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.metadata.iso.citation.OnLineResourceImpl;
import org.geotools.metadata.iso.citation.ResponsiblePartyImpl;


/**
 * Wraps a JAI's {@link ParameterListDescriptor}. This adaptor is provided for interoperability
 * with <A HREF="http://java.sun.com/products/java-media/jai/">Java Advanced Imaging</A>. A JAI
 * parameter list descriptor is part of an {@linkplain OperationDescriptor operation descriptor}.
 * This adaptor make it easier to access parameters for a JAI operation through the general GeoAPI
 * parameters framework.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.2
 */
public class ImagingParameterDescriptors extends DefaultParameterDescriptorGroup {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 2127050865911951239L;

    /**
     * The registry mode. Usually {@value RenderedRegistryMode#MODE_NAME}.
     * This field is {@code null} if {@link #operation} is null.
     */
    final String registryMode;

    /**
     * The JAI's operation descriptor, or {@code null} if none. This is usually an
     * instance of {@link OperationDescriptor}, but this is not strictly required.
     */
    protected final RegistryElementDescriptor operation;

    /**
     * The Java Advanced Imaging parameter descriptor. If {@link #operation} is non-null, then
     * this attribute is defined by {@link RegistryElementDescriptor#getParameterListDescriptor}.
     */
    protected final ParameterListDescriptor descriptor;

    /**
     * The set of parameter names in lower cases, <strong>not</strong> including the sources.
     * This is used by {@link ImagingParameters} in order to differentiate the sources from
     * ordinary parameters. This set will be created when first needed.
     */
    private transient Set parameterNames;

    /**
     * Constructs a parameter descriptor wrapping the specified JAI operation, including sources.
     *
     * @param operation The JAI's operation descriptor, usually as an instance of
     *        {@link OperationDescriptor}.
     * @param sourceTypeMap Mapping from JAI source type to this group source type. Typically a
     *        singleton with the (<code>{@linkplain java.awt.image.RenderedImage}.class</code>,
     *        <code>{@linkplain org.opengis.coverage.grid.GridCoverage}.class</code>) key-value
     *        pair.
     * @param registryMode The JAI's registry mode (usually {@value RenderedRegistryMode#MODE_NAME}).
     */
    public ImagingParameterDescriptors(final RegistryElementDescriptor operation,
                                       final Map/*<Class,Class>*/ sourceTypeMap,
                                       final String registryMode)
    {
        this(getName(operation),
             operation.getParameterListDescriptor(registryMode),
             operation, sourceTypeMap, registryMode);
    }

    /**
     * Returns a name from the specified operation descriptor. If the name begins
     * with the {@code "org.geotools"} prefix, then the prefix will be ignored.
     *
     * @todo Should be inlined in the constructor if only Sun was to fix RFE #4093999
     *       ("Relax constraint on placement of this()/super() call in constructors").
     *
     * @todo We could do a more general work and ommit the vendor prefix instead of the
     *       hard-coded "org.geotools." prefix.
     */
    private static String getName(final RegistryElementDescriptor operation) {
        final String prefix = "org.geotools.";
        String name = operation.getName();
        if (name.startsWith(prefix)) {
            name = name.substring(prefix.length());
        }
        return name;
    }

    /**
     * Constructs a parameter descriptor wrapping the specified JAI parameter list descriptor.
     *
     * @param name The parameter descriptor name.
     * @param descriptor The JAI descriptor.
     */
    public ImagingParameterDescriptors(final String name, final ParameterListDescriptor descriptor) {
        this(name, descriptor, null, null, null);
    }

    /**
     * Constructs a parameter descriptor wrapping the specified JAI descriptor.
     * This constructor is a work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private ImagingParameterDescriptors(final String name,
                                        final ParameterListDescriptor descriptor,
                                        final RegistryElementDescriptor operation,
                                        final Map/*<Class,Class>*/ sourceTypeMap,
                                        final String registryMode)
    {
        super(properties(name, operation), 1, 1,
              asDescriptors(descriptor, operation, sourceTypeMap, registryMode));
        this.descriptor   = descriptor;
        this.operation    = operation;
        this.registryMode = registryMode;
    }

    /**
     * Returns the properties for the parameter descriptor group.
     * This method try to extract a maximum of informations from
     * the supplied operation.
     * Note: this method is a work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static Map properties(final String name, final RegistryElementDescriptor operation) {
        if (operation instanceof OperationDescriptor) {
            final OperationDescriptor op = (OperationDescriptor) operation;
            final ResourceBundle  bundle = op.getResourceBundle(Locale.getDefault());
            final Map properties = new HashMap();
            properties.put(NAME_KEY, name);
            properties.put(ALIAS_KEY,   new ImagingParameterDescription(op, "LocalName"));
            properties.put(REMARKS_KEY, new ImagingParameterDescription(op, "Description"));
            properties.put(Identifier.VERSION_KEY, bundle.getString("Version"));
            try {
                final URI                     uri = new URI(bundle.getString("DocURL"));
                final OnLineResourceImpl resource = new OnLineResourceImpl(uri);
                final ContactImpl         contact = new ContactImpl(resource);
                final InternationalString  vendor = new ImagingParameterDescription(op, "Vendor");
                final ResponsiblePartyImpl  party = new ResponsiblePartyImpl(Role.RESOURCE_PROVIDER);
                final CitationImpl       citation = new CitationImpl(vendor);
                party.setOrganisationName(vendor);
                party.setContactInfo(contact);
                citation.setCitedResponsibleParties(Collections.singleton(party));
                properties.put(Identifier.AUTHORITY_KEY, citation.unmodifiable());
            } catch (URISyntaxException exception) {
                // Invalid URI syntax. Ignore, since this property
                // was really just for information purpose.
            }
            return properties;
        } else {
            return Collections.singletonMap(NAME_KEY, name);
        }
    }

    /**
     * Returns the JAI's parameters as {@link ParameterDescriptor} objects.
     * This method is a work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static ParameterDescriptor[] asDescriptors(final ParameterListDescriptor  descriptor,
                                                       final RegistryElementDescriptor operation,
                                                       final Map/*<Class,Class>*/ sourceTypeMap,
                                                       final String registryMode)
    {
        ensureNonNull("descriptor", descriptor);
        final Map properties = new HashMap();
        /*
         * JAI considers sources as a special kind of parameters, while GridCoverageProcessor makes
         * no distinction. If the registry element "operation" is really a JAI's OperationDescriptor
         * (which should occurs most of the time), prepend the JAI's sources before all ordinary
         * parameters. In addition, transform the source type if needed.
         */
        final int numSources;
        final int numParameters = descriptor.getNumParameters();
        final ParameterDescriptor[] desc;
        if (operation instanceof OperationDescriptor) {
            final OperationDescriptor op = (OperationDescriptor) operation;
            final String[] names = op.getSourceNames();
            final Class[]  types = op.getSourceClasses(registryMode);
            numSources = op.getNumSources();
            desc = new ParameterDescriptor[numParameters + numSources];
            for (int i=0; i<numSources; i++) {
                Class type = (Class) sourceTypeMap.get(types[i]);
                if (type == null) {
                    type = types[i];
                }
                String name = names[i];
                properties.clear();
                if (numSources == 1) {
                    /*
                     * If there is only one source argument, rename for example "Source0"
                     * as "Source" for better compliance with OpenGIS usage. However, we
                     * will keep the original name as an alias.
                     */
                    final int length = name.length();
                    if (length != 0) {
                        final char c = name.charAt(length-1);
                        if (c=='0' || c=='1') {
                            properties.put(ALIAS_KEY, name);
                            name = name.substring(0, length-1);
                        }
                    }
                }
                properties.put(NAME_KEY, name);
                desc[i] = new DefaultParameterDescriptor(properties, type,
                                                         null,   // validValues
                                                         null,   // defaultValue
                                                         null,   // minimum
                                                         null,   // maximum
                                                         null,   // unit
                                                         true);  // required
            }
        } else {
            numSources = 0;
            desc = new ParameterDescriptor[numParameters];
        }
        /*
         * Source parameters completed. Now get the ordinary parameters.
         */
        final String[]    names = descriptor.getParamNames();
        final Class[]   classes = descriptor.getParamClasses();
        final Object[] defaults = descriptor.getParamDefaults();
        for (int i=0; i<numParameters; i++) {
            final String name = names[i];
            final Class  type = classes[i];
            final Range range = descriptor.getParamValueRange(name);
            final Comparable min, max;
            if (range != null) {
                min = range.getMinValue();
                max = range.getMaxValue();
            } else {
                min = null;
                max = null;
            }
            EnumeratedParameter[] validValues;
            if (EnumeratedParameter.class.isAssignableFrom(type)) try {
                validValues = descriptor.getEnumeratedParameterValues(name);
            } catch (UnsupportedOperationException exception) {
                validValues = null;
            } else {
                validValues = null;
            }
            Object defaultValue = defaults[i];
            if (defaultValue == ParameterListDescriptor.NO_PARAMETER_DEFAULT) {
                defaultValue = null;
            }
            properties.clear();
            properties.put(NAME_KEY, name);
            if (operation instanceof OperationDescriptor) {
                final ImagingParameterDescription remark =
                        new ImagingParameterDescription((OperationDescriptor) operation, i);
                if (remark.exists()) {
                    properties.put(REMARKS_KEY, remark);
                }
            }
            desc[i + numSources] = new DefaultParameterDescriptor(properties,
                                    type, validValues, defaultValue, min, max, null, true);
        }
        return desc;
    }

    /**
     * Returns the set of parameter names in lower cases, <strong>not</strong> including the
     * sources. This is used by {@link ImagingParameters} in order to differentiate the sources
     * from ordinary parameters.
     */
    final Set getParameterNames() {
        if (parameterNames == null) {
            final String[] names = descriptor.getParamNames();
            parameterNames = new HashSet((int)(names.length/0.75f) + 1);
            for (int i=0; i<names.length; i++) {
                parameterNames.add(names[i].trim().toLowerCase());
            }
            parameterNames = Collections.unmodifiableSet(parameterNames);
        }
        return parameterNames;
    }

    /**
     * Creates a new instance of parameter value group. A JAI {@link javax.media.jai.ParameterList}
     * is created for holding parameter values, and wrapped into an {@link ImagingParameters}
     * instance.
     */
    public GeneralParameterValue createValue() {
        return new ImagingParameters(this);
    }
    
    /**
     * Compares the specified object with this parameter group for equality.
     *
     * @param  object The object to compare to {@code this}.
     * @param  compareMetadata {@code true} for performing a strict comparaison, or
     *         {@code false} for comparing only properties relevant to transformations.
     * @return {@code true} if both objects are equal.
     */
    public boolean equals(final AbstractIdentifiedObject object, final boolean compareMetadata) {
        if (object == this) {
            // Slight optimization
            return true;
        }
        if (super.equals(object, compareMetadata)) {
            final ImagingParameterDescriptors that = (ImagingParameterDescriptors) object;
            return Utilities.equals(this.operation,  that.operation) &&
                   Utilities.equals(this.descriptor, that.descriptor);
        }
        return false;
    }
    
    /**
     * Returns a hash value for this parameter. This value doesn't need
     * to be the same in past or future versions of this class.
     */
    public int hashCode() {
        return super.hashCode() ^ descriptor.hashCode();
    }
}
