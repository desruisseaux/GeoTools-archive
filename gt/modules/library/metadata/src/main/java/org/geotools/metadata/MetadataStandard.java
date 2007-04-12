/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
 *   
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.metadata;

// J2SE dependencies
import java.util.HashMap;
import java.util.Map;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * A set of methods operating on implementations of some specific metadata standard.
 * The standards is defined by interfaces in some package and sub-packages.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux (Geomatys)
 */
public final class MetadataStandard {
    /**
     * An instance working on ISO 19115 standard as defined by
     * <A HREF="http://geoapi.sourceforge.net">GeoAPI</A> in the
     * {@link org.opengis.metadata} package.
     */
    public static final MetadataStandard ISO_19115 = new MetadataStandard("org.opengis.metadata.");

    /**
     * The root packages for metadata interfaces. Must ends with {@code "."}.
     */
    private final String interfacePackage;

    /**
     * Accessors for the specified implementations.
     */
    private final Map/*<Class,PropertyAccessor>*/ accessors = new HashMap();
    
    /**
     * Creates a new instance working on implementation of interfaces defined
     * in the specified package. For the ISO 19115 standard reflected by GeoAPI
     * interfaces, it should be the {@code org.opengis.metadata} package.
     *
     * @param interfacePackage The root package for metadata interfaces.
     */
    private MetadataStandard(String interfacePackage) {
        if (!interfacePackage.endsWith(".")) {
            interfacePackage += '.';
        }
        this.interfacePackage = interfacePackage;
    }

    /**
     * Returns the accessor for the specified implementation.
     *
     * @throws ClassCastException if the specified implementation class do
     *         not implements a metadata interface of the expected package.
     */
    private PropertyAccessor getAccessor(final Class implementation) throws ClassCastException {
        synchronized (accessors) {
            PropertyAccessor accessor = (PropertyAccessor) accessors.get(implementation);
            if (accessor == null) {
                accessor = new PropertyAccessor(implementation, interfacePackage);
                accessors.put(implementation, accessor);
            }
            return accessor;
        }
    }

    /**
     * Compares the two specified metadata objects. The comparaison is <cite>shallow</cite>,
     * i.e. all metadata attributes are compared using the {@link Object#equals} method without
     * recursive call to this {@code shallowEquals} method for other metadata.
     * <p>
     * This method can optionaly excludes null values from the comparaison. In metadata,
     * null value often means "don't know", so in some occasion we want to consider two
     * metadata as different only if an attribute value is know for sure to be different.
     * <p>
     * The two arguments must be implementations of a metadata interface, otherwise an
     * exception will be thrown. They do not need to be the same implementation however.
     *
     * @param metadata1 The first metadata object to compare.
     * @param metadata2 The second metadata object to compare.
     * @param skipNulls If {@code true}, only non-null values will be compared.
     * @throws ClassCastException if at least one metadata object don't
     *         implements a metadata interface of the expected package.
     */
    public boolean shallowEquals(final Object metadata1, final Object metadata2, final boolean skipNulls)
            throws ClassCastException
    {
        if (metadata1 == metadata2) {
            return true;
        }
        if (metadata1 == null || metadata2 == null) {
            return false;
        }
        final PropertyAccessor accessor = getAccessor(metadata1.getClass());
        if (!accessor.sameInterface(metadata2.getClass(), interfacePackage)) {
            return false;
        }
        return accessor.shallowEquals(metadata1, metadata2, skipNulls);
    }

    /**
     * Copies all metadata from source to target. The source must implements the same
     * metadata interface than the target.
     *
     * @param  source The metadata to copy.
     * @param  target The target metadata.
     * @param  skipNulls If {@code true}, only non-null values will be copied.
     * @throws ClassCastException if the source or target object don't
     *         implements a metadata interface of the expected package.
     * @throws UnmodifiableMetadataException if the target metadata is unmodifiable,
     *         or if at least one setter method was required but not found.
     */
    public void shallowCopy(final Object source, final Object target, final boolean skipNulls)
            throws ClassCastException, UnmodifiableMetadataException
    {
        final PropertyAccessor accessor = getAccessor(target.getClass());
        accessor.ensureValidType(source);
        if (!accessor.shallowCopy(source, target, skipNulls)) {
            throw new UnmodifiableMetadataException(Errors.format(ErrorKeys.UNMODIFIABLE_METADATA));
        }
    }

    /**
     * Returns a view of the specified metadata object as a {@linkplain Map map}.
     * The map is backed by the metadata object using Java reflection, so changes
     * in the underlying metadata object are immediately reflected in this map.
     * The keys are the property names as determined by the list of {@code get}
     * methods declared in the metadata interface.
     * <p>
     * The map supports the {@link Map#put put} operations if the underlying
     * metadata object contains {@link #set} methods.
     *
     * @param  metadata The metadata object to view as a map.
     * @return A map view over the metadata object.
     * @throws ClassCastException if at the metadata object don't
     *         implements a metadata interface of the expected package.
     */
    public Map asMap(final Object metadata) throws ClassCastException {
        return new PropertyMap(metadata, getAccessor(metadata.getClass()));
    }
}
