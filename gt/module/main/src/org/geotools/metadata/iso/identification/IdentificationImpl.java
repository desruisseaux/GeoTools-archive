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
package org.geotools.metadata.iso.identification;

// J2SE direct dependencies
import java.util.Collection;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.constraint.Constraints;
import org.opengis.metadata.distribution.Format;
import org.opengis.metadata.identification.Identification;
import org.opengis.metadata.identification.BrowseGraphic;
import org.opengis.metadata.identification.Keywords;
import org.opengis.metadata.identification.Progress;
import org.opengis.metadata.identification.Usage;
import org.opengis.metadata.maintenance.MaintenanceInformation;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Basic information required to uniquely identify a resource or resources.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class IdentificationImpl extends MetadataEntity implements Identification {
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
    private Collection credits;

    /**
     * Status of the resource(s).
     */
    private Collection status;

    /**
     * Identification of, and means of communication with, person(s) and organizations(s)
     * associated with the resource(s).
     */
    private Collection pointOfContacts;

    /**
     * Provides information about the frequency of resource updates, and the scope of those updates.
     */
    private Collection resourceMaintenance;

    /**
     * Provides a graphic that illustrates the resource(s) (should include a legend for the graphic).
     */
    private Collection graphicOverviews;

    /**
     * Provides a description of the format of the resource(s).
     */
    private Collection resourceFormat;

    /**
     * Provides category keywords, their type, and reference source.
     */
    private Collection descriptiveKeywords;

    /**
     * Provides basic information about specific application(s) for which the resource(s)
     * has/have been or is being used by different users.
     */
    private Collection resourceSpecificUsages;

    /**
     * Provides information about constraints which apply to the resource(s).
     */
    private Collection resourceConstraints;
    
    /**
     * Constructs an initially empty identification.
     */
    public IdentificationImpl() {
    }

    /**
     * Creates an identification initialized to the specified values.
     */
    public IdentificationImpl(final Citation citation, final InternationalString abstracts) {
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
    public synchronized Collection getCredits() {
        return credits = nonNullCollection(credits, String.class);
    }

    /**
     * Set a recognition of those who contributed to the resource(s).
     */
    public synchronized void setCredits(final Collection newValues) {
        credits = copyCollection(newValues, credits, String.class);
    }

    /**
     * Status of the resource(s).
     */
    public synchronized Collection getStatus() {
        return status = nonNullCollection(status, Progress.class);
    }

    /**
     * Set the status of the resource(s).
     */
    public synchronized void setStatus(final Collection newValues) {
        status = copyCollection(newValues, status, Progress.class);
    }

    /**
     * Identification of, and means of communication with, person(s) and organizations(s)
     * associated with the resource(s).
     */
    public synchronized Collection getPointOfContacts() {
        return pointOfContacts = nonNullCollection(pointOfContacts, ResponsibleParty.class);
    }

    /**
     * Set the point of contacts.
     */
    public synchronized void setPointOfContacts(final Collection newValues) {
        pointOfContacts = copyCollection(newValues, pointOfContacts, ResponsibleParty.class);
    }

    /**
     * Provides information about the frequency of resource updates, and the scope of those updates.
     */
    public synchronized Collection getResourceMaintenance() {
        return resourceMaintenance = nonNullCollection(resourceMaintenance,
                                                       MaintenanceInformation.class);
    }

    /**
     * Set information about the frequency of resource updates, and the scope of those updates.
     */
    public synchronized void setResourceMaintenance(final Collection newValues) {
        resourceMaintenance = copyCollection(newValues, resourceMaintenance,
                                             MaintenanceInformation.class);
    }

    /**
     * Provides a graphic that illustrates the resource(s) (should include a legend for the graphic).
     */
    public synchronized Collection getGraphicOverviews() {
        return graphicOverviews = nonNullCollection(graphicOverviews, BrowseGraphic.class);
    }

    /**
     * Set a graphic that illustrates the resource(s).
     */
    public synchronized void setGraphicOverviews(final Collection newValues) {
        graphicOverviews = copyCollection(newValues, graphicOverviews, BrowseGraphic.class);
    }

    /**
     * Provides a description of the format of the resource(s).
     */
    public synchronized Collection getResourceFormat() {
        return resourceFormat = nonNullCollection(resourceFormat, Format.class);
    }

    /**
     * Set a description of the format of the resource(s).
     */
    public synchronized void setResourceFormat(final Collection newValues) {
        resourceFormat = copyCollection(newValues, resourceFormat, Format.class);
    }

    /**
     * Provides category keywords, their type, and reference source.
     */
    public synchronized Collection getDescriptiveKeywords() {
        return descriptiveKeywords = nonNullCollection(descriptiveKeywords, Keywords.class);
    }

    /**
     * Set category keywords, their type, and reference source.
     */
    public synchronized void setDescriptiveKeywords(final Collection newValues) {
        descriptiveKeywords = copyCollection(newValues, descriptiveKeywords, Keywords.class);
    }

    /**
     * Provides basic information about specific application(s) for which the resource(s)
     * has/have been or is being used by different users.
     */
    public synchronized Collection getResourceSpecificUsages() {
        return resourceSpecificUsages = nonNullCollection(resourceSpecificUsages, Usage.class);
    }

    /**
     * Set basic information about specific application(s).
     */
    public synchronized void setResourceSpecificUsages(final Collection newValues) {
        resourceSpecificUsages = copyCollection(newValues, resourceSpecificUsages, Usage.class);
    }

    /**
     * Provides information about constraints which apply to the resource(s).
     */
    public synchronized Collection getResourceConstraints() {
        return resourceConstraints = nonNullCollection(resourceConstraints, Constraints.class);
    }

    /**
     * Set information about constraints which apply to the resource(s).
     */
    public synchronized void setResourceConstraints(final Collection newValues) {
        resourceConstraints = copyCollection(newValues, resourceConstraints, Constraints.class);
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        citation               = (Citation)            unmodifiable(citation);
        abstracts              = (InternationalString) unmodifiable(abstracts);
        purpose                = (InternationalString) unmodifiable(purpose);
        credits                = (Collection)          unmodifiable(credits);
        status                 = (Collection)          unmodifiable(status);
        pointOfContacts        = (Collection)          unmodifiable(pointOfContacts);
        resourceMaintenance    = (Collection)          unmodifiable(resourceMaintenance);
        resourceFormat         = (Collection)          unmodifiable(resourceFormat);
        descriptiveKeywords    = (Collection)          unmodifiable(descriptiveKeywords);
        resourceSpecificUsages = (Collection)          unmodifiable(resourceSpecificUsages);
        resourceConstraints    = (Collection)          unmodifiable(resourceConstraints);
    }

    /**
     * Compare this identification with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final IdentificationImpl that = (IdentificationImpl) object;
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
