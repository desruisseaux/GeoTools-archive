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
import org.opengis.metadata.distribution.DigitalTransferOptions;
import org.opengis.metadata.distribution.Distribution;
import org.opengis.metadata.distribution.Distributor;
import org.opengis.metadata.distribution.Format;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;
import org.geotools.resources.Utilities;


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
public class DistributionImpl extends MetadataEntity implements Distribution {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5899590027802365131L;

    /**
     * Provides a description of the format of the data to be distributed.
     */
    private Collection distributionFormats;

    /**
     * Provides information about the distributor.
     */
    private Collection distributors;

    /**
     * Provides information about technical means and media by which a resource is obtained
     * from the distributor.
     */
    private Collection transferOptions;
    
    /**
     * Constructs an initially empty distribution.
     */
    public DistributionImpl() {
    }

    /**
     * Provides a description of the format of the data to be distributed.
     */
    public synchronized Collection getDistributionFormats() {
        return distributionFormats = nonNullCollection(distributionFormats, Format.class);
    }

    /**
     * Set a description of the format of the data to be distributed.
     */
    public synchronized void setDistributionFormats(final Collection newValues) {
        distributionFormats = copyCollection(newValues, distributionFormats, Format.class);
    }

    /**
     * Provides information about the distributor.
     */
    public synchronized Collection getDistributors() {
        return distributors = nonNullCollection(distributors, Distributor.class);
    }

    /**
     * Set information about the distributor.
     */
    public synchronized void setDistributors(final Collection newValues) {
        distributors = copyCollection(newValues, distributors, Distributor.class);
    }

    /**
     * Provides information about technical means and media by which a resource is obtained
     * from the distributor.
     */
    public synchronized Collection getTransferOptions() {
        return transferOptions = nonNullCollection(transferOptions, DigitalTransferOptions.class);
    }

    /**
     * Set information about technical means and media by which a resource is obtained
     * from the distributor.
     */
    public synchronized void setTransferOptions(final Collection newValues) {
        transferOptions = copyCollection(newValues, transferOptions, DigitalTransferOptions.class);
    }
    
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        distributionFormats = (Collection) unmodifiable(distributionFormats);
        distributors        = (Collection) unmodifiable(distributors);
        transferOptions     = (Collection) unmodifiable(transferOptions);
    }

    /**
     * Compare this Distribution with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final DistributionImpl that = (DistributionImpl) object;
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
     *
     * @todo Provides a more elaborated implementation.
     */
    public String toString() {
        return String.valueOf(distributionFormats);
    }
}
