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

// J2SE direct dependencies and extensions
import java.util.List;
import javax.units.Unit;
import java.util.Collections;

// OpenGIS direct dependencies
import org.opengis.util.InternationalString;
import org.opengis.metadata.distribution.MediumFormat;
import org.opengis.metadata.distribution.MediumName;

// Geotools dependencies
import org.geotools.metadata.MetadataEntity;
import org.geotools.util.CheckedArrayList;
import org.geotools.resources.Utilities;


/**
 * Information about the media on which the resource can be distributed.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class Medium extends MetadataEntity implements org.opengis.metadata.distribution.Medium {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2838122926367921673L;
    
    /**
     * Name of the medium on which the resource can be received.
     */
    private MediumName name;

    /**
     * Density at which the data is recorded.
     * Returns <code>null</code> if unknown.
     * If non-null, then the number should be greater than zero.
     */
    private List densities;

    /**
     * Units of measure for the recording density.
     */
    private Unit densityUnits;

    /**
     * Number of items in the media identified.
     * Returns <code>null</code> if unknown.
     */
    private Integer volumes;

    /**
     * Method used to write to the medium.
     */
    private MediumFormat mediumFormat;

    /**
     * Description of other limitations or requirements for using the medium.
     */
    private InternationalString mediumNote;
    
    /**
     * Construct an initially empty medium.
     */
    public Medium() {
    }

    /**
     * Returns the name of the medium on which the resource can be received.
     */
    public MediumName getName() {
        return name;
    }

    /**
     * Set the name of the medium on which the resource can be received.
     */
    public synchronized void setName(final MediumName newValue) {
        checkWritePermission();
        name = newValue;
    }

    /**
     * Returns the units of measure for the recording density.
     */
    public Unit getDensityUnits() {
        return densityUnits;
    }

    /**
     * Set the units of measure for the recording density.
     */
    public synchronized void setDensityUnits(final Unit newValue) {
        checkWritePermission();
        densityUnits = newValue;
    }

    /**
     * Returns the number of items in the media identified.
     * Returns <code>null</code> if unknown.
     */
    public Integer getVolumes() {
        return volumes;
    }

    /**
     * Set the number of items in the media identified.
     * Returns <code>null</code> if unknown.
     */
    public synchronized void setVolumes(final Integer newValue) {
        checkWritePermission();
        volumes = newValue;
    }

    /**
     * Returns the method used to write to the medium.
     */
    public MediumFormat getMediumFormat() {
        return mediumFormat;
    }

    /**
     * Set the method used to write to the medium.
     */
    public synchronized void setMediumFormat(final MediumFormat newValue) {
        checkWritePermission();
        mediumFormat = newValue;
    }

    /**
     * Returns a description of other limitations or requirements for using the medium.
     */
    public InternationalString getMediumNote() {
        return mediumNote;
    }
    
    /**
     * Set a description of other limitations or requirements for using the medium.
     */
    public synchronized void setMediumNote(final InternationalString newValue) {
        checkWritePermission();
        mediumNote = newValue;
    }
    
    /**
     * Returns the density at which the data is recorded.
     * The numbers should be greater than zero.
     */
    public List getDensities() {
        final List densities = this.densities; // Avoid synchronization
        return (densities!=null) ? densities : Collections.EMPTY_LIST;
    }

    /**
     * Set density at which the data is recorded.
     * The numbers should be greater than zero.
     */
    public synchronized void setDensities(final List newValues) {
        checkWritePermission();
        if (densities == null) {
            densities = new CheckedArrayList(Number.class);
        } else {
            densities.clear();
        }
        densities.addAll(newValues);
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        name         = (MediumName)          unmodifiable(name);
        densities    = (List)                unmodifiable(densities);
        densityUnits = (Unit)                unmodifiable(densityUnits);
        volumes      = (Integer)             unmodifiable(volumes);
        mediumFormat = (MediumFormat)        unmodifiable(mediumFormat);
        mediumNote   = (InternationalString) unmodifiable(mediumNote);
    }

    /**
     * Compare this Medium with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final Medium that = (Medium) object;
            return Utilities.equals(this.name,          that.name         ) &&
                   Utilities.equals(this.densities,     that.densities    ) &&
                   Utilities.equals(this.densityUnits,  that.densityUnits ) &&
                   Utilities.equals(this.volumes,       that.volumes      ) &&
                   Utilities.equals(this.mediumFormat,  that.mediumFormat ) &&
                   Utilities.equals(this.mediumNote,    that.mediumNote   );
        }
        return false;
    }

    /**
     * Returns a hash code value for this series.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (name           != null) code ^= name        .hashCode();
        if (densities      != null) code ^= densities   .hashCode();
        if (densityUnits   != null) code ^= densityUnits.hashCode();
        if (volumes        != null) code ^= volumes     .hashCode();
        if (mediumFormat   != null) code ^= mediumFormat.hashCode();
        if (mediumNote     != null) code ^= mediumNote  .hashCode();
        return code;
    }

    /**
     * Returns a string representation of this series.
     *
     * @todo Provides a more elaborated implementation.
     */
    public String toString() {
        return String.valueOf(name);
    }
}
