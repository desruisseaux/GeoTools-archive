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
package org.geotools.metadata.citation;

// J2SE direct dependencies
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Collections;
import java.io.Serializable;

// OpenGIS dependencies
import org.opengis.metadata.citation.Series;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.PresentationForm;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.MetadataEntity;
import org.geotools.util.CheckedHashSet;
import org.geotools.util.CheckedHashMap;
import org.geotools.util.CheckedArrayList;
import org.geotools.resources.Utilities;


/**
 * Standardized resource reference.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Jody Garnett
 */
public class Citation extends MetadataEntity implements org.opengis.metadata.citation.Citation {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -4415559967618358778L;
    
    /**
     * The <A HREF="http://www.opengis.org">OpenGIS consortium</A> authority.
     */
    public static final Citation OPEN_GIS = new Citation("OpenGIS consortium");
    static {
        OPEN_GIS.freeze();
    }

    /**
     * The <A HREF="http://www.epsg.org">European Petroleum Survey Group</A> authority.
     */
    public static final Citation EPSG = new Citation("European Petroleum Survey Group");
    static {
        EPSG.freeze();
    }

    /**
     * The <A HREF="http://www.geotools.org">Geotools</A> project.
     */
    public static final Citation GEOTOOLS = new Citation("Geotools");
    static {
        GEOTOOLS.freeze();
    }

    /**
     * Name by which the cited resource is known.
     */
    private InternationalString title;

    /**
     * Short name or other language name by which the cited information is known.
     * Example: "DCW" as an alternative title for "Digital Chart of the World.
     */
    private List alternateTitles;

    /**
     * Reference date for the cited resource.
     */
    private Map dates;

    /**
     * Version of the cited resource.
     */
    private InternationalString edition;

    /**
     * Date of the edition, or <code>null</code> if none.
     */
    private Date editionDate;

    /**
     * Unique identifier for the resource. Example: Universal Product Code (UPC),
     * National Stock Number (NSN).
     */
    private Set identifiers;

    /**
     * Reference form of the unique identifier (ID). Example: Universal Product Code (UPC),
     * National Stock Number (NSN).
     */
    private Set identifierTypes;

    /**
     * Name and position information for an individual or organization that is responsible
     * for the resource. Returns an empty string if there is none.
     */
    private Set citedResponsibleParties;

    /**
     * Mode in which the resource is represented, or an empty string if none.
     */
    private Set presentationForm;

    /**
     * Information about the series, or aggregate dataset, of which the dataset is a part.
     * Returns <code>null</code> if none.
     */
    private Series series;

    /**
     * Other information required to complete the citation that is not recorded elsewhere.
     * Returns <code>null</code> if none.
     */
    private InternationalString otherCitationDetails;

    /**
     * Common title with holdings note. Note: title identifies elements of a series
     * collectively, combined with information about what volumes are available at the
     * source cited. Returns <code>null</code> if there is no title.
     */
    private InternationalString collectiveTitle;

    /**
     * International Standard Book Number, or <code>null</code> if none.
     */
    private String ISBN;

    /**
     * International Standard Serial Number, or <code>null</code> if none.
     */
    private String ISSN;

    /**
     * Construct a citation with the specified title.
     *
     * @param title The title, as a {@link String} or an {@link InternationalString} object.
     */
    public Citation(final CharSequence title) {
        if (title instanceof InternationalString) {
            this.title = (InternationalString) title;
        } else {
            this.title = new org.geotools.util.InternationalString(title.toString());
        }
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
    public synchronized void setTitle(final InternationalString title) {
        checkWritePermission();
        this.title = title;
    }

    /**
     * Returns the short name or other language name by which the cited information is known.
     * Example: "DCW" as an alternative title for "Digital Chart of the World.
     */
    public List getAlternateTitles() {
        final List alternateTitles = this.alternateTitles; // Avoid synchronization
        return (alternateTitles!=null) ? alternateTitles : Collections.EMPTY_LIST;
    }

    /**
     * Set the short name or other language name by which the cited information is known.
     */
    public synchronized void setAlternateTitles(final List alternateTitles) {
        checkWritePermission();
        if (this.alternateTitles == null) {
            this.alternateTitles = new CheckedArrayList(InternationalString.class);
        } else {
            this.alternateTitles.clear();
        }
        this.alternateTitles.addAll(alternateTitles);
    }

    /**
     * Returns the reference date for the cited resource.
     */
    public Map getDates() {
        final Map dates = this.dates; // Avoid synchronization
        return (dates!=null) ? dates : Collections.EMPTY_MAP;
    }

    /**
     * Set the reference date for the cited resource.
     *
     * @todo Defines a {@link HashMap} subclass which transform all {@link Date} object
     *       into unmidifiable dates.
     */
    public synchronized void setDates(final Map dates) {
        checkWritePermission();
        if (this.dates == null) {
            this.dates = new CheckedHashMap(DateType.class, Date.class);
        } else {
            this.dates.clear();
        }
        this.dates.putAll(dates);
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
    public synchronized void setEdition(final InternationalString edition) {
        checkWritePermission();
        this.edition = edition;
    }

    /**
     * Returns the date of the edition, or <code>null</code> if none.
     */
    public Date getEditionDate() {
        return editionDate;
    }

    /**
     * Set the date of the edition, or <code>null</code> if none.
     *
     * @todo Use an unmodifiable {@link Date} here.
     */
    public synchronized void setEditionDate(Date editionDate) {
        checkWritePermission();
        if (editionDate != null) {
            editionDate = new Date(editionDate.getTime());
        }
        this.editionDate = editionDate;
    }

    /**
     * Returns the unique identifier for the resource. Example: Universal Product Code (UPC),
     * National Stock Number (NSN).
     */
    public Set getIdentifiers() {
        final Set identifiers = this.identifiers; // Avoid synchronization
        return (identifiers!=null) ? identifiers : Collections.EMPTY_SET;
    }

    /**
     * Set the unique identifier for the resource. Example: Universal Product Code (UPC),
     * National Stock Number (NSN).
     */
    public synchronized void setIdentifiers(final Set identifiers) {
        checkWritePermission();
        if (this.identifiers == null) {
            this.identifiers = new CheckedHashSet(String.class);
        } else {
            this.identifiers.clear();
        }
        this.identifiers.addAll(identifiers);
    }

    /**
     * Returns the reference form of the unique identifier (ID).
     * Example: Universal Product Code (UPC), National Stock Number (NSN).
     */
    public Set getIdentifierTypes() {
        final Set identifierTypes = this.identifierTypes; // Avoid synchronization
        return (identifierTypes!=null) ? identifierTypes : Collections.EMPTY_SET;
    }

    /**
     * Set the reference form of the unique identifier (ID).
     * Example: Universal Product Code (UPC), National Stock Number (NSN).
     */
    public synchronized void setIdentifierTypes(final Set identifierTypes) {
        checkWritePermission();
        if (this.identifierTypes == null) {
            this.identifierTypes = new CheckedHashSet(String.class);
        } else {
            this.identifierTypes.clear();
        }
        this.identifierTypes.addAll(identifierTypes);
    }

    /**
     * Returns the name and position information for an individual or organization that is
     * responsible for the resource. Returns an empty string if there is none.
     */
    public Set getCitedResponsibleParties() {
        final Set citedResponsibleParties = this.citedResponsibleParties; // Avoid synchronization
        return (citedResponsibleParties!=null) ? citedResponsibleParties : Collections.EMPTY_SET;
    }

    /**
     * Set the name and position information for an individual or organization that is responsible
     * for the resource. Returns an empty string if there is none.
     */
    public synchronized void setCitedResponsibleParties(final Set citedResponsibleParties) {
        checkWritePermission();
        if (this.citedResponsibleParties == null) {
            this.citedResponsibleParties = new CheckedHashSet(ResponsibleParty.class);
        } else {
            this.citedResponsibleParties.clear();
        }
        this.citedResponsibleParties = citedResponsibleParties;
    }

    /**
     * Returns the mode in which the resource is represented, or an empty string if none.
     */
    public Set getPresentationForm() {
        final Set presentationForm = this.presentationForm; // Avoid synchronization
        return (presentationForm!=null) ? presentationForm : Collections.EMPTY_SET;
    }

    /**
     * Set the mode in which the resource is represented, or an empty string if none.
     */
    public synchronized void setPresentationForm(final Set presentationForm) {
        checkWritePermission();
        if (this.presentationForm == null) {
            this.presentationForm = new CheckedHashSet(PresentationForm.class);
        } else {
            this.presentationForm.clear();
        }
        this.presentationForm.addAll( presentationForm );
    }

    /**
     * Returns the information about the series, or aggregate dataset, of which the dataset is
     * a part. Returns <code>null</code> if none.
     */
    public Series getSeries() {
        return series;
    }

    /**
     * Set the information about the series, or aggregate dataset, of which the dataset is
     * a part. Set to <code>null</code> if none.
     */
    public synchronized void setSeries(final Series series) {
        checkWritePermission();
        this.series = series;
    }

    /**
     * Returns other information required to complete the citation that is not recorded elsewhere.
     * Returns <code>null</code> if none.
     */
    public InternationalString getOtherCitationDetails() {
        return otherCitationDetails;
    }

    /**
     * Set other information required to complete the citation that is not recorded elsewhere.
     * Set to <code>null</code> if none.
     */
    public synchronized void setOtherCitationDetails(final InternationalString otherCitationDetails) {
        checkWritePermission();
        this.otherCitationDetails = otherCitationDetails;
    }

    /**
     * Returns the common title with holdings note. Note: title identifies elements of a series
     * collectively, combined with information about what volumes are available at the
     * source cited. Returns <code>null</code> if there is no title.
     */
    public InternationalString getCollectiveTitle() {
        return collectiveTitle;
    }

    /**
     * Set the common title with holdings note. Note: title identifies elements of a series
     * collectively, combined with information about what volumes are available at the
     * source cited. Set to <code>null</code> if there is no title.
     */
    public synchronized void setCollectiveTitle(final InternationalString collectiveTitle) {
        checkWritePermission();
        this.collectiveTitle = collectiveTitle;
    }

    /**
     * Returns the International Standard Book Number, or <code>null</code> if none.
     */
    public String getISBN() {
        return ISBN;
    }

    /**
     * Set the International Standard Book Number, or <code>null</code> if none.
     */
    public synchronized void setISBN(final String ISBN) {
        checkWritePermission();
        this.ISBN = ISBN;
    }

    /**
     * Returns the International Standard Serial Number, or <code>null</code> if none.
     */
    public String getISSN() {
        return ISSN;
    }

    /**
     * Set the International Standard Serial Number, or <code>null</code> if none.
     */
    public synchronized void setISSN(final String ISSN) {
        checkWritePermission();
        this.ISSN = ISSN;
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        title                   = (InternationalString) unmodifiable(title);
        alternateTitles         = (List)                unmodifiable(alternateTitles);
        dates                   = (Map)                 unmodifiable(dates);
        edition                 = (InternationalString) unmodifiable(edition);
        editionDate             = (Date)                unmodifiable(editionDate);
        identifiers             = (Set)                 unmodifiable(identifiers);
        identifierTypes         = (Set)                 unmodifiable(identifierTypes);
        citedResponsibleParties = (Set)                 unmodifiable(citedResponsibleParties);
        presentationForm        = (Set)                 unmodifiable(presentationForm);
        otherCitationDetails    = (InternationalString) unmodifiable(otherCitationDetails);
        collectiveTitle         = (InternationalString) unmodifiable(collectiveTitle);
        ISBN                    = (String)              unmodifiable(ISBN);
        ISSN                    = (String)              unmodifiable(ISSN);
    }

    /**
     * Compare this citation with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final Citation that = (Citation) object;
            return Utilities.equals(this.title,                   that.title                  ) &&
                   Utilities.equals(this.alternateTitles,         that.alternateTitles        ) &&
                   Utilities.equals(this.dates,                   that.dates                  ) &&
                   Utilities.equals(this.edition,                 that.edition                ) &&
                   Utilities.equals(this.editionDate,             that.editionDate            ) &&
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
     * Returns a string representation of this citation. The default implementation
     * returns the title in the default locale.
     */
    public String toString() {
        return String.valueOf(title);
    }
}
