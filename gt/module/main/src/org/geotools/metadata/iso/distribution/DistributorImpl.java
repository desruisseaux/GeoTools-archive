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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.metadata.iso.distribution;

// J2SE direct dependencies
import java.util.Collection;

// OpenGIS dependencies
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.distribution.DigitalTransferOptions;
import org.opengis.metadata.distribution.Distributor;
import org.opengis.metadata.distribution.Format;
import org.opengis.metadata.distribution.StandardOrderProcess;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Information about the distributor.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class DistributorImpl extends MetadataEntity implements Distributor {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 7142984376823483766L;

    /**
     * Party from whom the resource may be obtained. This list need not be exhaustive.
     */
    private ResponsibleParty distributorContact;

    /**
     * Provides information about how the resource may be obtained, and related
     * instructions and fee information.
     */
    private Collection distributionOrderProcesses;

    /**
     * Provides information about the format used by the distributor.
     */
    private Collection distributorFormats;

    /**
     * Provides information about the technical means and media used by the distributor.
     */
    private Collection distributorTransferOptions;
    
    /**
     * Constructs an initially empty distributor.
     */
    public DistributorImpl() {
    }

    /**
     * Creates a distributor with the specified contact.
     */
    public DistributorImpl(final ResponsibleParty distributorContact) {
        setDistributorContact(distributorContact);
    }

    /**
     * Party from whom the resource may be obtained. This list need not be exhaustive.
     */
    public ResponsibleParty getDistributorContact() {
        return distributorContact;
    }

    /**
     * Set the party from whom the resource may be obtained. This list need not be exhaustive.
     */
    public synchronized void setDistributorContact(final ResponsibleParty newValue) {
        checkWritePermission();
        distributorContact = newValue;
    }

    /**
     * Provides information about how the resource may be obtained, and related
     * instructions and fee information.
     */
    public synchronized Collection getDistributionOrderProcesses() {
        return distributionOrderProcesses = nonNullCollection(distributionOrderProcesses,
                                                              StandardOrderProcess.class);
    }

    /**
     * Set information about how the resource may be obtained, and related
     * instructions and fee information.
     */
    public synchronized void setDistributionOrderProcesses(final Collection newValues) {
        distributionOrderProcesses = copyCollection(newValues, distributionOrderProcesses,
                                                    StandardOrderProcess.class);
    }

    /**
     * Provides information about the format used by the distributor.
     */
    public synchronized Collection getDistributorFormats() {
        return distributorFormats = nonNullCollection(distributorFormats, Format.class);
    }

    /**
     * Set information about the format used by the distributor.
     */
    public synchronized void setDistributorFormats(final Collection newValues) {
        distributorFormats = copyCollection(newValues, distributorFormats, Format.class);
    }

    /**
     * Provides information about the technical means and media used by the distributor.
     */
    public synchronized Collection getDistributorTransferOptions() {
        return distributorTransferOptions = nonNullCollection(distributorTransferOptions,
                                                              DigitalTransferOptions.class);
    }
    
    /**
     * Provides information about the technical means and media used by the distributor.
     */
    public synchronized void setDistributorTransferOptions(final Collection newValues) {
        distributorTransferOptions = copyCollection(newValues, distributorTransferOptions,
                                                    DigitalTransferOptions.class);
    }
    
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        distributorContact         = (ResponsibleParty) unmodifiable(distributorContact);
        distributionOrderProcesses = (Collection)       unmodifiable(distributionOrderProcesses);
        distributorFormats         = (Collection)       unmodifiable(distributorFormats);
        distributorTransferOptions = (Collection)       unmodifiable(distributorTransferOptions);
    }

    /**
     * Compare this Distributor with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final DistributorImpl that = (DistributorImpl) object;
            return Utilities.equals(this.distributorContact,         that.distributorContact        ) &&
                   Utilities.equals(this.distributionOrderProcesses, that.distributionOrderProcesses) &&
                   Utilities.equals(this.distributorFormats,         that.distributorFormats        ) &&
                   Utilities.equals(this.distributorTransferOptions, that.distributorTransferOptions);
        }
        return false;
    }

    /**
     * Returns a hash code value for this series.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (distributorContact != null)         code ^= distributorContact        .hashCode();
        if (distributionOrderProcesses != null) code ^= distributionOrderProcesses.hashCode();
        if (distributorFormats != null)         code ^= distributorFormats        .hashCode();
        if (distributorTransferOptions != null) code ^= distributorTransferOptions.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this series.
     *
     * @todo Provides a more elaborated implementation.
     */
    public String toString() {
        return String.valueOf(distributorContact);
    }
}
