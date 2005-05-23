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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// OpenGIS dependencies
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.citation.OnLineFunction;
import org.opengis.metadata.citation.PresentationForm;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.citation.Series;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.MetadataEntity;
import org.geotools.resources.Utilities;
import org.geotools.util.SimpleInternationalString;


/**
 * Standardized resource reference.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Jody Garnett
 * @deprecated Renamed as {@code CitationImpl} in {@code org.geotools.metadata.iso} subpackage.
 */
public class Citation extends MetadataEntity
       implements org.opengis.metadata.citation.Citation
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -4415559967618358778L;

    /**
     * Create a a responsible party metadata entry for OGC involvement.
     * The organisation name is automatically set to "Open Geospatial Consortium".
     *
     * @param role           The OGC role (point of contact, owner, etc.) for a resource.
     * @param function       The OGC function (information, download, etc.) for a resource.
     * @param onlineResource The URI on the resource.
     * @return ResponsibleParty describing OGC involvement
     */ 
    private static ResponsibleParty OGC(Role role, OnLineFunction function, String onlineResource) {
        try {
            return org.geotools.metadata.citation.ResponsibleParty.OGC(role, function,
                                                                       new URI(onlineResource));
        }
        catch (URISyntaxException badContact) {
            Utilities.unexpectedException("org.geotools.metadata", "Citation", "OGC", badContact);
            return org.geotools.metadata.citation.ResponsibleParty.OGC;
        }
    }

    /**
     * The <A HREF="http://www.opengis.org/docs/01-068r3.pdf">WMS 1.1.1</A>
     * (01-068r3) "Automatic Projections" Authority.
     * <p>
     * Here is the assumptions used by the CRSAuthorityFactory to locate an
     * authority on AUTO data:
     * <ul>
     * <li>getTitle() returns something human readable
     * <li>getIdentifiers().contains( "AUTO" )
     * </ul>
     * </p>
     * <p>
     * Warning: different from AUTO2 used for WMS 1.3.0.
     * </p>
     * @see <A HREF="http://www.opengeospatial.org/">Open Geospatial Consortium</A>
     * @see <A HREF="http://www.opengis.org/docs/01-068r3.pdf">WMS 1.1.1 specification</A>
     * @see org.geotools.metadata.citation.ResponsibleParty#OGC
     *
     * @todo May need to move to a class more closely related to CRS factories.
     */
    public static final Citation AUTO = new Citation("Automatic Projections");
    static { // Sanity check ensure that all @see tags are actually available in the metadata
        AUTO.setPresentationForm(Collections.singleton(PresentationForm.MODEL_DIGITAL));

        List titles = new ArrayList(3);
        titles.add( new SimpleInternationalString("AUTO"));        
        titles.add( new SimpleInternationalString("WMS 1.1.1"));
        titles.add( new SimpleInternationalString("OGC 01-068r2"));                
        AUTO.setAlternateTitles( titles );

        Set parties = new HashSet(4);
        parties.add( OGC( Role.POINT_OF_CONTACT, OnLineFunction.INFORMATION, "http://www.opengeospatial.org/" ));
        parties.add( OGC( Role.OWNER, OnLineFunction.INFORMATION, "http://www.opengis.org/docs/01-068r3.pdf" ));
        AUTO.setCitedResponsibleParties(parties);

        AUTO.setIdentifiers(Collections.singleton("AUTO"));
        AUTO.freeze();
    }
    
    /**
     * The <A HREF="http://portal.opengis.org/files/?artifact_id=5316">WMS 1.3.0</A>
     * (01-068r3) "Automatic Projections" Authority.
     * <p>
     * Here is the assumptions used by the CRSAuthorityFactory to locate an
     * authority on AUTO2 data:
     * <ul>
     * <li>getTitle() returns something human readable
     * <li>getIdentifiers().contains( "AUTO2" )
     * </ul>
     * </p>
     * <p>
     * Warning: different from AUTO used for WMS 1.1.1. and earlier.
     * </p>
     * @see <A HREF="http://portal.opengis.org/files/?artifact_id=5316">WMS 1.3.0 specification</A>
     * @see org.geotools.metadata.citation.ResponsibleParty#OGC
     *
     * @todo May need to move to a class more closely related to CRS factories.
     */
    public static final Citation AUTO2 = new Citation("Automatic Projections");
    static {
        AUTO2.setPresentationForm(Collections.singleton(PresentationForm.MODEL_DIGITAL));

        List titles = new ArrayList(3);
        titles.add( new SimpleInternationalString("AUTO2"));        
        titles.add( new SimpleInternationalString("WMS 1.3.0"));
        titles.add( new SimpleInternationalString("OGC 04-024"));                
        AUTO2.setAlternateTitles( titles );

        Set parties = new HashSet(4);
        parties.add( OGC( Role.POINT_OF_CONTACT, OnLineFunction.INFORMATION, "http://www.opengeospatial.org/" ));
        parties.add( OGC( Role.OWNER, OnLineFunction.INFORMATION, "http://portal.opengis.org/files/?artifact_id=5316" ));
        AUTO2.setCitedResponsibleParties( parties );

        AUTO2.setIdentifiers(Collections.singleton("AUTO2"));
        AUTO2.freeze();
    }
    
    /**
     * The <A HREF="http://www.opengis.org">OpenGIS consortium</A> authority.
     *
     * @see org.geotools.metadata.citation.ResponsibleParty#OPEN_GIS
     *
     * @todo This citation is using wrong constants: OGC / OPEN_GIS are mixed up.
     */
    public static final Citation OPEN_GIS = new Citation("OpenGIS consortium");
    static {
        OPEN_GIS.setPresentationForm(Collections.singleton(PresentationForm.DOCUMENT_DIGITAL));
        OPEN_GIS.setAlternateTitles(Collections.singletonList(new SimpleInternationalString("OGC")));
        OPEN_GIS.setCitedResponsibleParties(Collections.singleton(
                 org.geotools.metadata.citation.ResponsibleParty.OGC));
        OPEN_GIS.freeze();
    }
    
    /**
     * The <A HREF="http://www.epsg.org">European Petroleum Survey Group</A> authority.
     * <p>
     * Here is the assumptions used by the CRSAuthorityFactory to locate an
     * authority on EPSG data:
     * <ul>
     * <li>getTitle() returns something human readable
     * <li>getIdentifiers().contains( "EPSG" )
     * </ul>
     * </p>
     * @see org.geotools.metadata.citation.ResponsibleParty#OGC
     */    
    public static final Citation EPSG = new Citation("European Petroleum Survey Group");
    static {
        EPSG.setPresentationForm(Collections.singleton(PresentationForm.TABLE_DIGITAL));
        EPSG.setAlternateTitles(Collections.singletonList(new SimpleInternationalString("EPSG")));
        EPSG.setCitedResponsibleParties(Collections.singleton(
             org.geotools.metadata.citation.ResponsibleParty.EPSG));
        EPSG.setIdentifiers( Collections.singleton("EPSG") );        
        EPSG.freeze();
    }

    /**
     * The <A HREF="http://www.remotesensing.org/geotiff/geotiff.html">GeoTIFF</A> authority.
     *
     * @see org.geotools.metadata.citation.ResponsibleParty#GEOTIFF
     */
    public static final Citation GEOTIFF = new Citation("GeoTIFF");
    static {
        GEOTIFF.setPresentationForm(Collections.singleton(PresentationForm.DOCUMENT_DIGITAL));
        GEOTIFF.setCitedResponsibleParties(Collections.singleton(
             org.geotools.metadata.citation.ResponsibleParty.GEOTIFF));
        GEOTIFF.freeze();
    }

    /**
     * The <A HREF="http://www.esri.com">ESRI</A> authority.
     *
     * @see org.geotools.metadata.citation.ResponsibleParty#ESRI
     */
    public static final Citation ESRI = new Citation("ESRI");
    static {
        ESRI.setCitedResponsibleParties(Collections.singleton(
             org.geotools.metadata.citation.ResponsibleParty.ESRI));
        ESRI.freeze();
    }
    
    /**
     * The <A HREF="http://www.oracle.com">Oracle</A> authority.
     *
     * @see org.geotools.metadata.citation.ResponsibleParty#ORACLE
     */
    public static final Citation ORACLE = new Citation("Oracle");
    static {
        ORACLE.setCitedResponsibleParties(Collections.singleton(
                 org.geotools.metadata.citation.ResponsibleParty.ORACLE));
        ORACLE.freeze();
    }

    /**
     * The <A HREF="http://www.geotools.org">Geotools</A> project.
     *
     * @see org.geotools.metadata.citation.ResponsibleParty#GEOTOOLS
     */
    public static final Citation GEOTOOLS = new Citation("Geotools");
    static {
        GEOTOOLS.setCitedResponsibleParties(Collections.singleton(
                 org.geotools.metadata.citation.ResponsibleParty.GEOTOOLS));
        GEOTOOLS.freeze();
    }

    /**
     * List of authorities declared in this class.
     */
    private static final Citation[] AUTHORITIES = {
        OPEN_GIS, EPSG, GEOTIFF, ESRI, GEOTOOLS
    };

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
     * Constructs an initially empty citation.
     */
    public Citation() {
    }

    /**
     * Constructs a citation with the specified title.
     *
     * @param title The title, as a {@link String} or an {@link InternationalString} object.
     */
    public Citation(final CharSequence title) {
        final InternationalString t;
        if (title instanceof InternationalString) {
            t = (InternationalString) title;
        } else {
            t = new SimpleInternationalString(title.toString());
        }
        setTitle(t);
    }

    /**
     * Returns a citation of the given name. If the given name matches a
     * {@linkplain #getTitle title} or an {@linkplain #getAlternateTitles
     * alternate titles} of one of the pre-defined constants
     * (e.g. {@link #EPSG}, {@link #GEOTIFF}, <cite>etc.</cite>),
     * then this constant is returned. Otherwise, a new citation is created
     * with the specified name as the title.
     *
     * @param name The citation name (or title).
     */
    public static Citation createCitation(final String name) {
        for (int i=0; i<AUTHORITIES.length; i++) {
            final Citation citation = AUTHORITIES[i];
            if (titleMatches(citation, name)) {
                return citation;
            }
        }
        return new Citation(name);
    }

    /**
     * Returns <code>true</code> if the {@linkplain #getTitle title} or any
     * {@linkplain #getAlternateTitles alternate title} in the given citation
     * matches the given string.
     *
     * @param  citation The citation to check for.
     * @param  title The title or alternate title to compare.
     * @return <code>true</code> in the title or alternate title matche the given string.
     */
    public static boolean titleMatches(final org.opengis.metadata.citation.Citation citation,
                                       String title)
    {
        title = title.trim();
        InternationalString candidate = citation.getTitle();
        Iterator iterator = null;
        do {
            if (candidate.toString(Locale.US).trim().equalsIgnoreCase(title)) {
                return true;
            }
            if (candidate.toString().trim().equalsIgnoreCase(title)) {
                return true;
            }
            if (iterator == null) {
                final Collection titles = citation.getAlternateTitles();
                if (titles == null) {
                    break;
                }
                iterator = titles.iterator();
            }
            if (!iterator.hasNext()) {
                break;
            }
            candidate = (InternationalString) iterator.next();
        } while (true);
        return false;
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
     * Returns the date of the edition, or <code>null</code> if none.
     */
    public synchronized Date getEditionDate() {
        return (editionDate!=Long.MIN_VALUE) ? new Date(editionDate) : null;
    }

    /**
     * Set the date of the edition, or <code>null</code> if none.
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
     * a part. Returns <code>null</code> if none.
     */
    public Series getSeries() {
        return series;
    }

    /**
     * Set the information about the series, or aggregate dataset, of which the dataset is
     * a part. Set to <code>null</code> if none.
     */
    public synchronized void setSeries(final Series newValue) {
        checkWritePermission();
        series = newValue;
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
    public synchronized void setOtherCitationDetails(final InternationalString newValue) {
        checkWritePermission();
        otherCitationDetails = newValue;
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
    public synchronized void setCollectiveTitle(final InternationalString newValue) {
        checkWritePermission();
        collectiveTitle = newValue;
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
    public synchronized void setISBN(final String newValue) {
        checkWritePermission();
        ISBN = newValue;
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
            final Citation that = (Citation) object;
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
