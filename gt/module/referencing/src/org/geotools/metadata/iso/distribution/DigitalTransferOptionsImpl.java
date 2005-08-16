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
import org.opengis.metadata.citation.OnLineResource;
import org.opengis.metadata.distribution.DigitalTransferOptions;
import org.opengis.metadata.distribution.Medium;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Technical means and media by which a resource is obtained from the distributor.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
public class DigitalTransferOptionsImpl extends MetadataEntity implements DigitalTransferOptions {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1533064478468754337L;

    /**
     * Tiles, layers, geographic areas, etc., in which data is available.
     */
    private InternationalString unitsOfDistribution;

    /**
     * Estimated size of a unit in the specified transfer format, expressed in megabytes.
     * The transfer size is &gt; 0.0.
     * Returns {@code null} if the transfer size is unknown.
     */
    private Number transferSize;

    /**
     * Information about online sources from which the resource can be obtained.
     */
    private Collection onLines;

    /**
     * Information about offline media on which the resource can be obtained.
     */
    private Medium offLines;
    
    /**
     * Constructs an initially empty digital transfer options.
     */
    public DigitalTransferOptionsImpl() {
    }

    /**
     * Returne tiles, layers, geographic areas, etc., in which data is available.
     */
    public InternationalString getUnitsOfDistribution() {
        return unitsOfDistribution;
    }

    /**
     * Set tiles, layers, geographic areas, etc., in which data is available.
     */
    public synchronized void setUnitsOfDistribution(final InternationalString newValue) {
        checkWritePermission();
        unitsOfDistribution = newValue;
    }

    /**
     * Returns an estimated size of a unit in the specified transfer format, expressed in megabytes.
     * The transfer size is &gt; 0.0.
     * Returns {@code null} if the transfer size is unknown.
     */
    public Number getTransferSize() {
        return transferSize;
    }

    /**
     * Set an estimated size of a unit in the specified transfer format, expressed in megabytes.
     * The transfer size is &gt; 0.0.
     */
    public synchronized void setTransferSize(final Number newValue) {
        checkWritePermission();
        transferSize = newValue;
    }

    /**
     * Returns information about online sources from which the resource can be obtained.
     */
    public synchronized Collection getOnLines() {
        return onLines = nonNullCollection(onLines, OnLineResource.class);
    }

    /**
     * Set information about online sources from which the resource can be obtained.
     */
    public synchronized void setOnLines(final Collection newValues) {
        onLines = copyCollection(newValues, onLines, OnLineResource.class);
    }

    /**
     * Returns information about offline media on which the resource can be obtained.
     */
    public Medium getOffLine() {
        return offLines;
    }
    
    /**
     * Set information about offline media on which the resource can be obtained.
     */
    public synchronized void setOffLine(final Medium newValue) {
        checkWritePermission();
        offLines = newValue;
    }
    
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        unitsOfDistribution = (InternationalString) unmodifiable(unitsOfDistribution);
        transferSize        = (Number)              unmodifiable(transferSize);
        onLines             = (Collection)          unmodifiable(onLines);
        offLines            = (Medium)              unmodifiable(offLines);
    }

    /**
     * Compare this digital transfer options with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final DigitalTransferOptionsImpl that = (DigitalTransferOptionsImpl) object;
            return Utilities.equals(this.unitsOfDistribution,  that.unitsOfDistribution) &&
                   Utilities.equals(this.transferSize,         that.transferSize       ) &&
                   Utilities.equals(this.onLines,              that.onLines            ) &&
                   Utilities.equals(this.offLines,             that.offLines           );
        }
        return false;
    }

    /**
     * Returns a hash code value for this digital transfer options.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (unitsOfDistribution != null) code ^= unitsOfDistribution.hashCode();
        if (transferSize        != null) code ^= transferSize.hashCode();
        if (onLines             != null) code ^= onLines.hashCode();
        if (offLines            != null) code ^= offLines.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this digital transfer options.
     *
     * @todo Provides a more elaborated implementation.
     */
    public String toString() {
        return String.valueOf(unitsOfDistribution);
    }
}
