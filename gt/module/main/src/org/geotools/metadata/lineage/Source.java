/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le D�veloppement
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
package org.geotools.metadata.lineage;

// J2SE direct dependencies
import java.util.Set;
import java.util.Collections;

// OpenGIS direct dependencies
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.lineage.ProcessStep;
import org.opengis.referencing.ReferenceSystem;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.MetadataEntity;
import org.geotools.util.CheckedHashSet;
import org.geotools.resources.Utilities;


/**
 * Information about the source data used in creating the data specified by the scope.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 */
public class Source extends MetadataEntity
       implements org.opengis.metadata.lineage.Source
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1754233428736991423L;
    
    /**
     * Detailed description of the level of the source data.
     */
    private InternationalString description;

    /**
     * Denominator of the representative fraction on a source map.
     */
    private long scaleDenominator;

    /**
     * Spatial reference system used by the source data.
     */
    private ReferenceSystem sourceReferenceSystem;

    /**
     * Recommended reference to be used for the source data.
     */
    private Citation sourceCitation;

    /**
     * Information about the spatial, vertical and temporal extent of the source data.
     */
    private Set sourceExtents;

    /**
     * Information about an event in the creation process for the source data.
     */
    private Set sourceSteps;
    
    /**
     * Creates an initially empty source.
     */
    public Source() {
    }
    
    /**
     * Creates a source initialized with the given description.
     */
    public Source(final InternationalString description) {
        setDescription(description);
    }

    /**
     * Returns a detailed description of the level of the source data.
     */
    public InternationalString getDescription() {
        return description;
    }

    /**
     * Set a detailed description of the level of the source data.
     */
    public synchronized void setDescription(final InternationalString newValue) {
        checkWritePermission();
        description = newValue;
    }

    /**
     * Returns the denominator of the representative fraction on a source map.
     */
    public synchronized long getScaleDenominator()  {
        return scaleDenominator;
    }

    /**
     * Set the denominator of the representative fraction on a source map.
     */
    public synchronized void setScaleDenominator(final long newValue)  {
        checkWritePermission();
        scaleDenominator = newValue;
    }

    /**
     * Returns the spatial reference system used by the source data.
     */
    public ReferenceSystem getSourceReferenceSystem()  {
        return sourceReferenceSystem;
    }

    /**
     * Set the spatial reference system used by the source data.
     */
    public synchronized void setSourceReferenceSystem(final ReferenceSystem newValue) {
        checkWritePermission();
        sourceReferenceSystem = newValue;
    }

    /**
     * Returns the recommended reference to be used for the source data.
     */
    public Citation getSourceCitation() {
        return sourceCitation;
    }

    /**
     * Set the recommended reference to be used for the source data.
     */
    public synchronized void setSourceCitation(final Citation newValue) {
        checkWritePermission();
        sourceCitation = newValue;
    }

    /**
     * Returns tiInformation about the spatial, vertical and temporal extent
     * of the source data.
     */
    public Set getSourceExtents()  {
        final Set sourceExtents = this.sourceExtents; // Avoid synchronization
        return (sourceExtents!=null) ? sourceExtents : Collections.EMPTY_SET;
    }

    /**
     * Information about the spatial, vertical and temporal extent of the source data.
     */
    public synchronized void setSourceExtents(final Set newValues) {
        checkWritePermission();
        if (sourceExtents == null) {
            sourceExtents = new CheckedHashSet(Extent.class);
        } else {
            sourceExtents.clear();
        }
        sourceExtents.addAll(newValues);
    }

    /**
     * Returns information about an event in the creation process for the source data.
     */
    public Set getSourceSteps() {
        final Set sourceSteps = this.sourceSteps; // Avoid synchronization
        return (sourceSteps!=null) ? sourceSteps : Collections.EMPTY_SET;
    }

    /**
     * Set information about an event in the creation process for the source data.
     */
    public synchronized void setSourceSteps(final Set newValues) {
        checkWritePermission();
        if (sourceSteps == null) {
            sourceSteps = new CheckedHashSet(ProcessStep.class);
        } else {
            sourceSteps.clear();
        }
        sourceSteps.addAll(newValues);
    }
    
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        description           = (InternationalString) unmodifiable(description);
        sourceReferenceSystem = (ReferenceSystem)     unmodifiable(sourceReferenceSystem);
        sourceCitation        = (Citation)            unmodifiable(sourceCitation);
        sourceExtents         = (Set)                 unmodifiable(sourceExtents);
        sourceSteps           = (Set)                 unmodifiable(sourceSteps);
    }

    /**
     * Compare this source with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final Source that = (Source) object;
            return Utilities.equals(this.description,           that.description           ) &&
                   Utilities.equals(this.sourceReferenceSystem, that.sourceReferenceSystem ) &&
                   Utilities.equals(this.sourceCitation,        that.sourceCitation        ) &&
                   Utilities.equals(this.sourceExtents,         that.sourceExtents         ) &&
                   Utilities.equals(this.sourceSteps,           that.sourceSteps           ) &&
                                   (this.scaleDenominator    == that.scaleDenominator      );
        }
        return false;
    }

    /**
     * Returns a hash code value for this source.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (description           != null) code ^= description          .hashCode();
        if (sourceReferenceSystem != null) code ^= sourceReferenceSystem.hashCode();
        if (sourceCitation        != null) code ^= sourceCitation       .hashCode();
        if (sourceExtents         != null) code ^= sourceExtents        .hashCode();
        if (sourceSteps           != null) code ^= sourceSteps          .hashCode();
        return code;
    }

    /**
     * Returns a string representation of this source.
     *
     * @todo Provides a more elaborated implementation.
     */
    public String toString() {
        return String.valueOf(description);
    }        
}
