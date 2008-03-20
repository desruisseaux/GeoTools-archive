/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.metadata.iso;

import java.io.Serializable;

import java.util.Collection;
import org.geotools.metadata.MetadataStandard;
import org.geotools.metadata.ModifiableMetadata;
import org.geotools.metadata.InvalidMetadataException;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * A superclass for implementing ISO 19115 metadata interfaces. Subclasses
 * must implement at least one of the ISO MetaData interface provided by
 * <A HREF="http://geoapi.sourceforge.net">GeoAPI</A>.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Jody Garnett
 * @author Martin Desruisseaux
 */
public class MetadataEntity extends ModifiableMetadata implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5730550742604669102L;

    /**
     * A flag that indicates if a marshalling process from JAXB has been launched.
     */
    private transient ThreadLocal<Boolean> isMarshalling;
    
    /**
     * Constructs an initially empty metadata entity.
     */
    protected MetadataEntity() {
        super();
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     * The {@code source} metadata must implements the same metadata interface than this class.
     *
     * @param  source The metadata to copy values from.
     * @throws ClassCastException if the specified metadata don't implements the expected
     *         metadata interface.
     *
     * @since 2.4
     */
    protected MetadataEntity(final Object source) throws ClassCastException {
        super(source);
    }

    /**
     * Returns the metadata standard implemented by subclasses,
     * which is {@linkplain MetadataStandard#ISO_19115 ISO 19115}.
     *
     * @since 2.4
     */
    public MetadataStandard getStandard() {
        return MetadataStandard.ISO_19115;
    }

    /**
     * Makes sure that an argument is non-null. This is used for checking if
     * a mandatory attribute is presents.
     *
     * @param  name   Argument name.
     * @param  object User argument.
     * @throws InvalidMetadataException if {@code object} is null.
     *
     * @since 2.4
     */
    protected static void ensureNonNull(final String name, final Object object)
            throws InvalidMetadataException
    {
        if (object == null) {
            throw new InvalidMetadataException(Errors.format(ErrorKeys.NULL_ATTRIBUTE_$1, name));
        }
    }

    /**
     * Specify the value for the flag {@code isMarshalling}. If this flag is not instanciated,
     * then a new boolean is created for this thread. Else the current value for this flag is 
     * just set with the wished one.
     * 
     * @param marshalling The value the flag has to take.
     */
    protected final synchronized void isMarshalling(final boolean marshalling) {
        if (isMarshalling == null) {
            if (!marshalling) {
                return;
            }
            isMarshalling = new ThreadLocal<Boolean>();
        }
        isMarshalling.set(marshalling);
    }

    /**
     * Returns the collection specified if the flag {@code isMarshalling} is set to {@code false}.
     * In this case the marshalling is not in process, because it has been called from another way,
     * so we always return the collection.
     * If we are marshalling the collection, then we do not want to return an empty collection.
     * 
     * @param elements The collection to return.
     * @return The collection specified, or {@code null} if we are marshalling.
     */
    protected final <E> Collection<E> xmlOptional(final Collection<E> elements) {
        if (elements != null && elements.isEmpty()) {
            // Copy the reference in order to avoid the need for synchronization.
            final ThreadLocal<Boolean> isMarshalling = this.isMarshalling;
            if (isMarshalling != null && Boolean.TRUE.equals(isMarshalling.get())) {
                return null;
            }
        }
        return elements;
    }
}
