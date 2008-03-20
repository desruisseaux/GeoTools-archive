/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.metadata.iso;

import java.util.Collection;

import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.opengis.metadata.citation.OnLineResource;
import org.opengis.metadata.ExtendedElementInformation;
import org.opengis.metadata.MetadataExtensionInformation;


/**
 * Information describing metadata extensions.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
@XmlType(propOrder={
    "extensionOnLineResource", "extendedElementInformation"
})
@XmlRootElement(name = "MD_MetadataExtensionInformation")
public class MetadataExtensionInformationImpl extends MetadataEntity
        implements MetadataExtensionInformation
{
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = 573866936088674519L;

    /**
     * Information about on-line sources containing the community profile name and
     * the extended metadata elements. Information for all new metadata elements.
     */
    private OnLineResource extensionOnLineResource;

    /**
     * Provides information about a new metadata element, not found in ISO 19115, which is
     * required to describe geographic data.
     */
    private Collection<ExtendedElementInformation> extendedElementInformation;

    /**
     * Construct an initially empty metadata extension information.
     */
    public MetadataExtensionInformationImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public MetadataExtensionInformationImpl(final MetadataExtensionInformation source) {
        super(source);
    }

    /**
     * Information about on-line sources containing the community profile name and
     * the extended metadata elements. Information for all new metadata elements.
     */
    @XmlElement(name = "extensionOnLineResource", required = false)
    public OnLineResource getExtensionOnLineResource() {
        return extensionOnLineResource;
    }

   /**
     * Set information about on-line sources.
     */
    public synchronized void setExtensionOnLineResource(final OnLineResource newValue) {
        checkWritePermission();
        this.extensionOnLineResource = newValue;
    }

    /**
     * Provides information about a new metadata element, not found in ISO 19115, which is
     * required to describe geographic data.
     */
    @XmlElement(name = "extendedElementInformation", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<ExtendedElementInformation> getExtendedElementInformation() {
        return xmlOptional(extendedElementInformation = nonNullCollection(extendedElementInformation,
                                                              ExtendedElementInformation.class));
    }

    /**
     * Set information about a new metadata element.
     */
    public synchronized void setExtendedElementInformation(
            final Collection<? extends ExtendedElementInformation> newValues) {
        extendedElementInformation = copyCollection(newValues, extendedElementInformation,
                                                    ExtendedElementInformation.class);
    }
    
        /**
     * Sets the {@code isMarshalling} flag to {@code true}, since the marshalling
     * process is going to be done.
     * This method is automatically called by JAXB, when the marshalling begins.
     * 
     * @param marshaller Not used in this implementation.
     */
    private void beforeMarshal(Marshaller marshaller) {
        isMarshalling(true);
    }

    /**
     * Sets the {@code isMarshalling} flag to {@code false}, since the marshalling
     * process is finished.
     * This method is automatically called by JAXB, when the marshalling ends.
     * 
     * @param marshaller Not used in this implementation
     */
    private void afterMarshal(Marshaller marshaller) {
        isMarshalling(false);
    }
}
