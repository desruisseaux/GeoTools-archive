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
package org.geotools.metadata.distribution;

// J2SE direct dependencies
import java.util.Set;
import java.util.Collections;

// OpenGIS direct dependencies
import org.opengis.util.InternationalString;
import org.opengis.metadata.distribution.Format;
import org.opengis.metadata.distribution.Distributor;
import org.opengis.metadata.distribution.DigitalTransferOptions;

// Geotools dependencies
import org.geotools.metadata.MetadataEntity;
import org.geotools.util.CheckedHashSet;
import org.geotools.resources.Utilities;


/**
 * Information about the distributor of and options for obtaining the resource.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class Distribution extends MetadataEntity
       implements org.opengis.metadata.distribution.Distribution
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5899590027802365131L;

    /**
     * Provides a description of the format of the data to be distributed.
     */
    private Set distributionFormats;

    /**
     * Provides information about the distributor.
     */
    private Set distributors;

    /**
     * Provides information about technical means and media by which a resource is obtained
     * from the distributor.
     */
    private Set transferOptions;
    
    /**
     * Construct an initially empty distribution.
     */
    public Distribution() {
    }

    /**
     * Provides a description of the format of the data to be distributed.
     */
    public Set getDistributionFormats() {
        final Set distributionFormats = this.distributionFormats; // Avoid synchronization
        return (distributionFormats!=null) ? distributionFormats : Collections.EMPTY_SET;
    }

    /**
     * Set a description of the format of the data to be distributed.
     */
    public synchronized void setDistributionFormats(final Set newValues) {
        checkWritePermission();
        if (distributionFormats == null) {
            distributionFormats = new CheckedHashSet(Format.class);
        } else {
            distributionFormats.clear();
        }
        distributionFormats.addAll(newValues);
    }

    /**
     * Provides information about the distributor.
     */
    public Set getDistributors() {
        final Set distributors = this.distributors; // Avoid synchronization
        return (distributors!=null) ? distributors : Collections.EMPTY_SET;
    }

    /**
     * Set information about the distributor.
     */
    public synchronized void setDistributors(final Set newValues) {
        checkWritePermission();
        if (distributors == null) {
            distributors = new CheckedHashSet(Distributor.class);
        } else {
            distributors.clear();
        }
        distributors.addAll(newValues);
    }

    /**
     * Provides information about technical means and media by which a resource is obtained
     * from the distributor.
     */
    public Set getTransferOptions() {
        final Set transferOptions = this.transferOptions; // Avoid synchronization
        return (transferOptions!=null) ? transferOptions : Collections.EMPTY_SET;
    }

    /**
     * Set information about technical means and media by which a resource is obtained
     * from the distributor.
     */
    public synchronized void setTransferOptions(final Set newValues) {
        checkWritePermission();
        if (transferOptions == null) {
            transferOptions = new CheckedHashSet(DigitalTransferOptions.class);
        } else {
            transferOptions.clear();
        }
        transferOptions.addAll(newValues);
    }
    
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        distributionFormats = (Set) unmodifiable(distributionFormats);
        distributors        = (Set) unmodifiable(distributors);
        transferOptions     = (Set) unmodifiable(transferOptions);
    }

    /**
     * Compare this Distribution with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final Distribution that = (Distribution) object;
            return Utilities.equals(this.distributionFormats,  that.distributionFormats) &&
                   Utilities.equals(this.distributors,         that.distributors       ) &&
                   Utilities.equals(this.transferOptions,      that.transferOptions    ) ;
        }
        return false;
    }

    /**
     * Returns a hash code value for this series.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (distributionFormats != null) code ^= distributionFormats.hashCode();
        if (distributors        != null) code ^= distributors.hashCode();
        if (transferOptions     != null) code ^= transferOptions.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this series.
     */
    public String toString() {
        return String.valueOf(distributionFormats);
    }
}
