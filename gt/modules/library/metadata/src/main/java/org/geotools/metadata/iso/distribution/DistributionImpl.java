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
package org.geotools.metadata.iso.distribution;

import java.util.Collection;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.opengis.metadata.distribution.DigitalTransferOptions;
import org.opengis.metadata.distribution.Distribution;
import org.opengis.metadata.distribution.Distributor;
import org.opengis.metadata.distribution.Format;
import org.geotools.metadata.iso.MetadataEntity;


/**
 * Information about the distributor of and options for obtaining the resource.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
@XmlRootElement(name = "MD_Distribution")
public class DistributionImpl extends MetadataEntity implements Distribution {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5899590027802365131L;

    /**
     * Provides a description of the format of the data to be distributed.
     */
    private Collection<Format> distributionFormats;

    /**
     * Provides information about the distributor.
     */
    private Collection<Distributor> distributors;

    /**
     * Provides information about technical means and media by which a resource is obtained
     * from the distributor.
     */
    private Collection<DigitalTransferOptions> transferOptions;

    /**
     * Constructs an initially empty distribution.
     */
    public DistributionImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public DistributionImpl(final Distribution source) {
        super(source);
    }

    /**
     * Provides a description of the format of the data to be distributed.
     */
    @XmlElement(name = "distributionFormat", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<Format> getDistributionFormats() {
        return xmlOptional(distributionFormats = nonNullCollection(distributionFormats, Format.class));
    }

    /**
     * Set a description of the format of the data to be distributed.
     */
    public synchronized void setDistributionFormats(final Collection<? extends Format> newValues) {
        distributionFormats = copyCollection(newValues, distributionFormats, Format.class);
    }

    /**
     * Provides information about the distributor.
     */
    @XmlElement(name = "distributor", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<Distributor> getDistributors() {
        return xmlOptional(distributors = nonNullCollection(distributors, Distributor.class));
    }

    /**
     * Set information about the distributor.
     */
    public synchronized void setDistributors(final Collection<? extends Distributor> newValues) {
        distributors = copyCollection(newValues, distributors, Distributor.class);
    }

    /**
     * Provides information about technical means and media by which a resource is obtained
     * from the distributor.
     */
    @XmlElement(name = "transferOptions", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<DigitalTransferOptions> getTransferOptions() {
        return xmlOptional(transferOptions = nonNullCollection(transferOptions, DigitalTransferOptions.class));
    }

    /**
     * Set information about technical means and media by which a resource is obtained
     * from the distributor.
     */
    public synchronized void setTransferOptions(
            final Collection<? extends DigitalTransferOptions> newValues)
    {
        transferOptions = copyCollection(newValues, transferOptions, DigitalTransferOptions.class);
    }

    /**
     * Sets the {@code isMarshalling} flag to {@code true}, since the marshalling
     * process is going to be done.
     * This method is automatically called by JAXB, when the marshalling begins.
     * 
     * @param marshaller Not used in this implementation.
     */
///    private void beforeMarshal(Marshaller marshaller) {
///        isMarshalling(true);
///    }

    /**
     * Sets the {@code isMarshalling} flag to {@code false}, since the marshalling
     * process is finished.
     * This method is automatically called by JAXB, when the marshalling ends.
     * 
     * @param marshaller Not used in this implementation
     */
///    private void afterMarshal(Marshaller marshaller) {
///        isMarshalling(false);
///    }
}
