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
package org.geotools.metadata.identification;

// J2SE direct dependencies
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.geotools.metadata.MetadataEntity;
import org.geotools.resources.Utilities;
import org.geotools.util.CheckedArrayList;
import org.geotools.util.CheckedHashSet;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.constraint.Constraints;
import org.opengis.metadata.distribution.Format;
import org.opengis.metadata.identification.BrowseGraphic;
import org.opengis.metadata.identification.Keywords;
import org.opengis.metadata.identification.Progress;
import org.opengis.metadata.identification.Usage;
import org.opengis.metadata.maintenance.MaintenanceInformation;
import org.opengis.util.InternationalString;


/**
 * Basic information required to uniquely identify a resource or resources.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class Identification extends MetadataEntity
        implements org.opengis.metadata.identification.Identification
{
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = -3715084806249419137L;

    /**
     * Citation data for the resource(s).
     */
    private Citation citation;

    /**
     * Brief narrative summary of the content of the resource(s).
     */
    private InternationalString abstracts;

    /**
     * Summary of the intentions with which the resource(s) was developed.
     */
    private InternationalString purpose;

    /**
     * Recognition of those who contributed to the resource(s).
     */
    private List credits;

    /**
     * Status of the resource(s).
     */
    private Set status;

    /**
     * Identification of, and means of communication with, person(s) and organizations(s)
     * associated with the resource(s).
     */
    private Set pointOfContacts;

    /**
     * Provides information about the frequency of resource updates, and the scope of those updates.
     */
    private Set resourceMaintenance;

    /**
     * Provides a graphic that illustrates the resource(s) (should include a legend for the graphic).
     */
    private Set graphicOverviews;

    /**
     * Provides a description of the format of the resource(s).
     */
    private Set resourceFormat;

    /**
     * Provides category keywords, their type, and reference source.
     */
    private Set descriptiveKeywords;

    /**
     * Provides basic information about specific application(s) for which the resource(s)
     * has/have been or is being used by different users.
     */
    private Set resourceSpecificUsages;

    /**
     * Provides information about constraints which apply to the resource(s).
     */
    private Set resourceConstraints;
    
    /**
     * Construct an initially empty identification.
     */
    public Identification() {
    }

    /**
     * Creates an identification initialized to the specified values.
     */
    public Identification(final Citation citation, final InternationalString abstracts) {
        setCitation(citation );
        setAbstract(abstracts);    
    }

    /**
     * Citation data for the resource(s).
     */
    public Citation getCitation() {
        return citation;
    }

    /**
     * Set the citation data for the resource(s).
     */
    public synchronized void setCitation(final Citation newValue) {
        checkWritePermission();
        citation = newValue;
    }

    /**
     * Brief narrative summary of the content of the resource(s).
     */
    public InternationalString getAbstract() {
        return abstracts;
    }

    /**
     * Set a brief narrative summary of the content of the resource(s).
     */
    public synchronized void setAbstract(final InternationalString newValue) {
        checkWritePermission();
        abstracts = newValue;
    }

    /**
     * Summary of the intentions with which the resource(s) was developed.
     */
    public InternationalString getPurpose() {
        return purpose;
    }

    /**
     * Set a summary of the intentions with which the resource(s) was developed.
     */
    public synchronized void setPurpose(final InternationalString newValue) {
        checkWritePermission();
        purpose = newValue;
    }

    /**
     * Recognition of those who contributed to the resource(s).
     */
    public List getCredits() {
        final List credits = this.credits; // Avoid synchronization
        return (credits!=null) ? credits : Collections.EMPTY_LIST;
    }

    /**
     * Set a recognition of those who contributed to the resource(s).
     */
    public synchronized void setCredits(final List newValues) {
        checkWritePermission();
        if (credits == null) {
            credits = new CheckedArrayList(String.class);
        } else {
            credits.clear();
        }
        credits.addAll(newValues);
    }

    /**
     * Status of the resource(s).
     */
    public Set getStatus() {
        final Set status = this.status; // Avoid synchronization
        return (status!=null) ? status : Collections.EMPTY_SET;
    }

    /**
     * Set the status of the resource(s).
     */
    public synchronized void setStatus(final Set newValues) {
        checkWritePermission();
        if (status == null) {
            status = new CheckedHashSet(Progress.class);
        } else {
            status.clear();
        }
        status.addAll(newValues);
    }

    /**
     * Identification of, and means of communication with, person(s) and organizations(s)
     * associated with the resource(s).
     */
    public Set getPointOfContacts() {
        final Set pointOfContacts = this.pointOfContacts; // Avoid synchronization
        return (pointOfContacts!=null) ? pointOfContacts : Collections.EMPTY_SET;
    }

    /**
     * Set the point of contacts.
     */
    public synchronized void setPointOfContacts(final Set newValues) {
        checkWritePermission();
        if (pointOfContacts == null) {
            pointOfContacts = new CheckedHashSet(ResponsibleParty.class);
        } else {
            pointOfContacts.clear();
        }
        pointOfContacts.addAll(newValues);
    }

    /**
     * Provides information about the frequency of resource updates, and the scope of those updates.
     */
    public Set getResourceMaintenance() {
        final Set resourceMaintenance = this.resourceMaintenance; // Avoid synchronization
        return (resourceMaintenance!=null) ? resourceMaintenance : Collections.EMPTY_SET;
    }

    /**
     * Set information about the frequency of resource updates, and the scope of those updates.
     */
    public synchronized void setResourceMaintenance(final Set newValues) {
        checkWritePermission();
        if (resourceMaintenance == null) {
            resourceMaintenance = new CheckedHashSet(MaintenanceInformation.class);
        } else {
            resourceMaintenance.clear();
        }
        resourceMaintenance.addAll(newValues);
    }

    /**
     * Provides a graphic that illustrates the resource(s) (should include a legend for the graphic).
     */
    public Set getGraphicOverviews() {
        final Set graphicOverviews = this.graphicOverviews; // Avoid synchronization
        return (graphicOverviews!=null) ? graphicOverviews : Collections.EMPTY_SET;
    }

    /**
     * Set a graphic that illustrates the resource(s).
     */
    public synchronized void setGraphicOverviews(final Set newValues) {
        checkWritePermission();
        if (graphicOverviews == null) {
            graphicOverviews = new CheckedHashSet(BrowseGraphic.class);
        } else {
            graphicOverviews.clear();
        }
        graphicOverviews.addAll(newValues);
    }

    /**
     * Provides a description of the format of the resource(s).
     */
    public Set getResourceFormat() {
        final Set resourceFormat = this.resourceFormat; // Avoid synchronization
        return (resourceFormat!=null) ? resourceFormat : Collections.EMPTY_SET;
    }

    /**
     * Set a description of the format of the resource(s).
     */
    public synchronized void setResourceFormat(final Set newValues) {
        checkWritePermission();
        if (resourceFormat == null) {
            resourceFormat = new CheckedHashSet(Format.class);
        } else {
            resourceFormat.clear();
        }
        resourceFormat.addAll(newValues);
    }

    /**
     * Provides category keywords, their type, and reference source.
     */
    public Set getDescriptiveKeywords() {
        final Set descriptiveKeywords = this.descriptiveKeywords; // Avoid synchronization
        return (descriptiveKeywords!=null) ? descriptiveKeywords : Collections.EMPTY_SET;
    }

    /**
     * Set category keywords, their type, and reference source.
     */
    public synchronized void setDescriptiveKeywords(final Set newValues) {
        checkWritePermission();
        if (descriptiveKeywords == null) {
            descriptiveKeywords = new CheckedHashSet(Keywords.class);
        } else {
            descriptiveKeywords.clear();
        }
        descriptiveKeywords.addAll(newValues);
    }

    /**
     * Provides basic information about specific application(s) for which the resource(s)
     * has/have been or is being used by different users.
     */
    public Set getResourceSpecificUsages() {
        final Set resourceSpecificUsages = this.resourceSpecificUsages; // Avoid synchronization
        return (resourceSpecificUsages!=null) ? resourceSpecificUsages : Collections.EMPTY_SET;
    }

    /**
     * Set basic information about specific application(s).
     */
    public synchronized void setResourceSpecificUsages(final Set newValues) {
        checkWritePermission();
        if (resourceSpecificUsages == null) {
            resourceSpecificUsages = new CheckedHashSet(Usage.class);
        } else {
            resourceSpecificUsages.clear();
        }
        resourceSpecificUsages.addAll(newValues);
    }

    /**
     * Provides information about constraints which apply to the resource(s).
     */
    public Set getResourceConstraints() {
        final Set resourceConstraints = this.resourceConstraints; // Avoid synchronization
        return (resourceConstraints!=null) ? resourceConstraints : Collections.EMPTY_SET;
    }

    /**
     * Set information about constraints which apply to the resource(s).
     */
    public synchronized void setResourceConstraints(final Set newValues) {
        checkWritePermission();
        if (resourceConstraints == null) {
            resourceConstraints = new CheckedHashSet(Constraints.class);
        } else {
            resourceConstraints.clear();
        }
        resourceConstraints.addAll(newValues);
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        citation               = (Citation)            unmodifiable(citation);
        abstracts              = (InternationalString) unmodifiable(abstracts);
        purpose                = (InternationalString) unmodifiable(purpose);
        credits                = (List)                unmodifiable(credits);
        status                 = (Set)                 unmodifiable(status);
        pointOfContacts        = (Set)                 unmodifiable(pointOfContacts);
        resourceMaintenance    = (Set)                 unmodifiable(resourceMaintenance);
        resourceFormat         = (Set)                 unmodifiable(resourceFormat);
        descriptiveKeywords    = (Set)                 unmodifiable(descriptiveKeywords);
        resourceSpecificUsages = (Set)                 unmodifiable(resourceSpecificUsages);
        resourceConstraints    = (Set)                 unmodifiable(resourceConstraints);
    }

    /**
     * Compare this identification with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final Identification that = (Identification) object;
            return Utilities.equals(this.citation,               that.citation               ) &&
                   Utilities.equals(this.abstracts,              that.abstracts              ) &&
                   Utilities.equals(this.purpose,                that.purpose                ) &&
                   Utilities.equals(this.credits,                that.credits                ) &&
                   Utilities.equals(this.status,                 that.status                 ) &&
                   Utilities.equals(this.pointOfContacts,        that.pointOfContacts        ) &&
                   Utilities.equals(this.resourceMaintenance,    that.resourceMaintenance    ) &&
                   Utilities.equals(this.resourceFormat,         that.resourceFormat         ) &&
                   Utilities.equals(this.descriptiveKeywords,    that.descriptiveKeywords    ) &&
                   Utilities.equals(this.resourceSpecificUsages, that.resourceSpecificUsages ) &&
                   Utilities.equals(this.resourceConstraints,    that.resourceConstraints    )  ;
        }
        return false;
    }

    /**
     * Returns a hash code value for this identification.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (citation                != null) code ^= citation              .hashCode();
        if (abstracts               != null) code ^= abstracts             .hashCode();
        if (purpose                 != null) code ^= purpose               .hashCode();
        if (credits                 != null) code ^= credits               .hashCode();
        if (status                  != null) code ^= status                .hashCode();
        if (pointOfContacts         != null) code ^= pointOfContacts       .hashCode();
        if (resourceMaintenance     != null) code ^= resourceMaintenance   .hashCode();
        if (resourceFormat          != null) code ^= resourceFormat        .hashCode();
        if (descriptiveKeywords     != null) code ^= descriptiveKeywords   .hashCode();
        if (resourceSpecificUsages  != null) code ^= resourceSpecificUsages.hashCode();
        if (resourceConstraints     != null) code ^= resourceConstraints   .hashCode();
        return code;
    }
 
    /**
     * Returns a string representation of this identification.
     */
    public String toString() {
        return String.valueOf(resourceMaintenance);
    }
}
