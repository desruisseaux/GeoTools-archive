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
package org.geotools.metadata.iso.citation;

// J2SE direct dependencies
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.citation.CitationDate;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.citation.OnLineFunction;
import org.opengis.metadata.citation.PresentationForm;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.citation.Series;
import org.opengis.util.InternationalString;
import org.opengis.referencing.crs.CRSAuthorityFactory;       // For javadoc
import org.opengis.referencing.crs.CoordinateReferenceSystem; // For javadoc

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;
import org.geotools.resources.Utilities;
import org.geotools.util.SimpleInternationalString;


/**
 * Standardized resource reference.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Jody Garnett
 */
public class CitationImpl extends MetadataEntity implements Citation {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -4415559967618358778L;

    /**
     * Name by which the cited resource is known.
     */
    private InternationalString title;

    /**
     * Short name or other language name by which the cited information is known.
     * Example: "DCW" as an alternative title for "Digital Chart of the World.
     */
    private Collection alternateTitles;

    /**
     * Reference date for the cited resource.
     */
    private Collection dates;

    /**
     * Version of the cited resource.
     */
    private InternationalString edition;

    /**
     * Date of the edition in millisecondes ellapsed sine January 1st, 1970,
     * or {@link Long#MIN_VALUE} if none.
     */
    private long editionDate = Long.MIN_VALUE;

    /**
     * Unique identifier for the resource. Example: Universal Product Code (UPC),
     * National Stock Number (NSN).
     */
    private Collection identifiers;

    /**
     * Reference form of the unique identifier (ID). Example: Universal Product Code (UPC),
     * National Stock Number (NSN).
     */
    private Collection identifierTypes;

    /**
     * Name and position information for an individual or organization that is responsible
     * for the resource. Returns an empty string if there is none.
     */
    private Collection citedResponsibleParties;

    /**
     * Mode in which the resource is represented, or an empty string if none.
     */
    private Collection presentationForm;

    /**
     * Information about the series, or aggregate dataset, of which the dataset is a part.
     * May be {@code null} if none.
     */
    private Series series;

    /**
     * Other information required to complete the citation that is not recorded elsewhere.
     * May be {@code null} if none.
     */
    private InternationalString otherCitationDetails;

    /**
     * Common title with holdings note. Note: title identifies elements of a series
     * collectively, combined with information about what volumes are available at the
     * source cited. May be {@code null} if there is no title.
     */
    private InternationalString collectiveTitle;

    /**
     * International Standard Book Number, or {@code null} if none.
     */
    private String ISBN;

    /**
     * International Standard Serial Number, or {@code null} if none.
     */
    private String ISSN;

    /**
     * Constructs an initially empty citation.
     */
    public CitationImpl() {
    }

    /**
     * Constructs a new citation initialized to the values specified by the given object.
     * This constructor performs a shallow copy (i.e. each source attributes are reused
     * without copying them).
     */
    public CitationImpl(final Citation source) {
        if (source != null) {
            setTitle                  (source.getTitle());
            setAlternateTitles        (source.getAlternateTitles());
            setDates                  (source.getDates());
            setEdition                (source.getEdition());
            setEditionDate            (source.getEditionDate());
            setIdentifiers            (source.getIdentifiers());
            setIdentifierTypes        (source.getIdentifierTypes());
            setCitedResponsibleParties(source.getCitedResponsibleParties());
            setPresentationForm       (source.getPresentationForm());
            setSeries                 (source.getSeries());
            setOtherCitationDetails   (source.getOtherCitationDetails());
            setCollectiveTitle        (source.getCollectiveTitle());
            setISBN                   (source.getISBN());
            setISSN                   (source.getISSN());
        }
    }

    /**
     * Constructs a citation with the specified title.
     *
     * @param title The title, as a {@link String} or an {@link InternationalString} object.
     */
    public CitationImpl(final CharSequence title) {
        final InternationalString t;
        if (title instanceof InternationalString) {
            t = (InternationalString) title;
        } else {
            t = new SimpleInternationalString(title.toString());
        }
        setTitle(t);
    }

    /**
     * Constructs a citation with the specified responsible party. This convenience constructor
     * initialize the citation title to the first non-null of the following properties:
     * {@linkplain ResponsibleParty#getOrganisationName organisation name},
     * {@linkplain ResponsibleParty#getPositionName position name} or
     * {@linkplain ResponsibleParty#getIndividualName individual name}.
     *
     * @since 2.2
     */
    public CitationImpl(final ResponsibleParty party) {
        InternationalString title = party.getOrganisationName();
        if (title == null) {
            title = party.getPositionName();
            if (title == null) {
                String name = party.getIndividualName();
                if (name != null) {
                    title = new SimpleInternationalString(name);
                }
            }
        }
        setTitle(title);
        getCitedResponsibleParties().add(party);
    }

    /**
     * Adds the specified identifier as a CRS authority factory. This is used as a convenience
     * method for the creation of constants, and for making sure that all of them use the same
     * identifier type.
     */
    final void addAuthority(final String identifier, final boolean asTitle) {
        if (asTitle) {
            getAlternateTitles().add(new SimpleInternationalString(identifier));
        }
        getIdentifierTypes().add("Authority name");
        getIdentifiers().add(identifier);
    }

    /**
     * Returns the name by which the cited resource is known.
     */
    public InternationalString getTitle() {
        return title;
    }

    /**
     * Set the name by which the cited resource is known.
     */
    public synchronized void setTitle(final InternationalString newValue) {
        checkWritePermission();
        title = newValue;
    }

    /**
     * Returns the short name or other language name by which the cited information is known.
     * Example: "DCW" as an alternative title for "Digital Chart of the World".
     */
    public synchronized Collection getAlternateTitles() {
        return alternateTitles = nonNullCollection(alternateTitles, InternationalString.class);
    }

    /**
     * Set the short name or other language name by which the cited information is known.
     */
    public synchronized void setAlternateTitles(final Collection newValues) {
        alternateTitles = copyCollection(newValues, alternateTitles, InternationalString.class);
    }

    /**
     * Returns the reference date for the cited resource.
     */
    public synchronized Collection getDates() {
        return dates = nonNullCollection(dates, CitationDate.class);
    }

    /**
     * Set the reference date for the cited resource.
     */
    public synchronized void setDates(final Collection newValues) {
        dates = copyCollection(newValues, dates, CitationDate.class);
    }

    /**
     * Returns the version of the cited resource.
     */
    public InternationalString getEdition() {
        return edition;
    }

    /**
     * Set the version of the cited resource.
     */
    public synchronized void setEdition(final InternationalString newValue) {
        checkWritePermission();
        edition = newValue;
    }

    /**
     * Returns the date of the edition, or {@code null} if none.
     */
    public synchronized Date getEditionDate() {
        return (editionDate!=Long.MIN_VALUE) ? new Date(editionDate) : null;
    }

    /**
     * Set the date of the edition, or {@code null} if none.
     *
     * @todo Use an unmodifiable {@link Date} here.
     */
    public synchronized void setEditionDate(final Date newValue) {
        checkWritePermission();
        editionDate = (newValue!=null) ? newValue.getTime() : Long.MIN_VALUE;
    }

    /**
     * Returns the unique identifier for the resource. Example: Universal Product Code (UPC),
     * National Stock Number (NSN).
     */
    public synchronized Collection getIdentifiers() {
        return identifiers = nonNullCollection(identifiers, String.class);
    }

    /**
     * Set the unique identifier for the resource. Example: Universal Product Code (UPC),
     * National Stock Number (NSN).
     */
    public synchronized void setIdentifiers(final Collection newValues) {
        identifiers = copyCollection(newValues, identifiers, String.class);
    }

    /**
     * Returns the reference form of the unique identifier (ID).
     * Example: Universal Product Code (UPC), National Stock Number (NSN).
     */
    public synchronized Collection getIdentifierTypes() {
        return identifierTypes = nonNullCollection(identifierTypes, String.class);
    }

    /**
     * Set the reference form of the unique identifier (ID).
     * Example: Universal Product Code (UPC), National Stock Number (NSN).
     */
    public synchronized void setIdentifierTypes(final Collection newValues) {
        identifierTypes = copyCollection(newValues, identifierTypes, String.class);
    }

    /**
     * Returns the name and position information for an individual or organization that is
     * responsible for the resource. Returns an empty string if there is none.
     */
    public synchronized Collection getCitedResponsibleParties() {
        return citedResponsibleParties = nonNullCollection(citedResponsibleParties,
                                                           ResponsibleParty.class);
    }

    /**
     * Set the name and position information for an individual or organization that is responsible
     * for the resource. Returns an empty string if there is none.
     */
    public synchronized void setCitedResponsibleParties(final Collection newValues) {
        citedResponsibleParties = copyCollection(newValues, citedResponsibleParties,
                                                 ResponsibleParty.class);
    }

    /**
     * Returns the mode in which the resource is represented, or an empty string if none.
     */
    public synchronized Collection getPresentationForm() {
        return presentationForm = nonNullCollection(presentationForm, PresentationForm.class);
    }

    /**
     * Set the mode in which the resource is represented, or an empty string if none.
     */
    public synchronized void setPresentationForm(final Collection newValues) {
        presentationForm = copyCollection(newValues, presentationForm, PresentationForm.class);
    }

    /**
     * Returns the information about the series, or aggregate dataset, of which the dataset is
     * a part. Returns {@code null} if none.
     */
    public Series getSeries() {
        return series;
    }

    /**
     * Set the information about the series, or aggregate dataset, of which the dataset is
     * a part. Set to {@code null} if none.
     */
    public synchronized void setSeries(final Series newValue) {
        checkWritePermission();
        series = newValue;
    }

    /**
     * Returns other information required to complete the citation that is not recorded elsewhere.
     * Returns {@code null} if none.
     */
    public InternationalString getOtherCitationDetails() {
        return otherCitationDetails;
    }

    /**
     * Set other information required to complete the citation that is not recorded elsewhere.
     * Set to {@code null} if none.
     */
    public synchronized void setOtherCitationDetails(final InternationalString newValue) {
        checkWritePermission();
        otherCitationDetails = newValue;
    }

    /**
     * Returns the common title with holdings note. Note: title identifies elements of a series
     * collectively, combined with information about what volumes are available at the
     * source cited. Returns {@code null} if there is no title.
     */
    public InternationalString getCollectiveTitle() {
        return collectiveTitle;
    }

    /**
     * Set the common title with holdings note. Note: title identifies elements of a series
     * collectively, combined with information about what volumes are available at the
     * source cited. Set to {@code null} if there is no title.
     */
    public synchronized void setCollectiveTitle(final InternationalString newValue) {
        checkWritePermission();
        collectiveTitle = newValue;
    }

    /**
     * Returns the International Standard Book Number, or {@code null} if none.
     */
    public String getISBN() {
        return ISBN;
    }

    /**
     * Set the International Standard Book Number, or {@code null} if none.
     */
    public synchronized void setISBN(final String newValue) {
        checkWritePermission();
        ISBN = newValue;
    }

    /**
     * Returns the International Standard Serial Number, or {@code null} if none.
     */
    public String getISSN() {
        return ISSN;
    }

    /**
     * Set the International Standard Serial Number, or {@code null} if none.
     */
    public synchronized void setISSN(final String newValue) {
        checkWritePermission();
        ISSN = newValue;
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        title                   = (InternationalString) unmodifiable(title);
        alternateTitles         = (Collection)          unmodifiable(alternateTitles);
        dates                   = (Collection)          unmodifiable(dates);
        edition                 = (InternationalString) unmodifiable(edition);
        identifiers             = (Collection)          unmodifiable(identifiers);
        identifierTypes         = (Collection)          unmodifiable(identifierTypes);
        citedResponsibleParties = (Collection)          unmodifiable(citedResponsibleParties);
        presentationForm        = (Collection)          unmodifiable(presentationForm);
        otherCitationDetails    = (InternationalString) unmodifiable(otherCitationDetails);
        collectiveTitle         = (InternationalString) unmodifiable(collectiveTitle);
    }

    /**
     * Compare this citation with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final CitationImpl that = (CitationImpl) object;
            return Utilities.equals(this.title,                   that.title                  ) &&
                   Utilities.equals(this.alternateTitles,         that.alternateTitles        ) &&
                   Utilities.equals(this.dates,                   that.dates                  ) &&
                   Utilities.equals(this.edition,                 that.edition                ) &&
                                   (this.editionDate         ==   that.editionDate            ) &&
                   Utilities.equals(this.identifiers,             that.identifiers            ) &&
                   Utilities.equals(this.identifierTypes,         that.identifierTypes        ) &&
                   Utilities.equals(this.citedResponsibleParties, that.citedResponsibleParties) &&
                   Utilities.equals(this.presentationForm,        that.presentationForm       ) &&
                   Utilities.equals(this.otherCitationDetails,    that.otherCitationDetails   ) &&
                   Utilities.equals(this.collectiveTitle,         that.collectiveTitle        ) &&
                   Utilities.equals(this.ISBN,                    that.ISBN                   ) &&
                   Utilities.equals(this.ISSN,                    that.ISSN                   );
        }
        return false;
    }

    /**
     * Returns a hash code value for this citation. For performance reason, this method do
     * not uses all attributes for computing the hash code. Instead, it uses the attributes
     * that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (title       != null) code ^= title      .hashCode();
        if (identifiers != null) code ^= identifiers.hashCode();
        if (ISBN        != null) code ^= ISBN       .hashCode();
        if (ISSN        != null) code ^= ISSN       .hashCode();
        return code;
    }

    /**
     * Returns a string representation of this citation.
     *
     * @todo Provides a more elaborated implementation.
     */
    public String toString() {
        return String.valueOf(title);
    }
}
