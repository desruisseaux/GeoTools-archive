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
import java.util.Locale;
import java.io.Serializable;

// OpenGIS dependencies
import org.opengis.metadata.citation.Series;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.PresentationForm;


/**
 * Standardized resource reference.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Citation implements org.opengis.metadata.citation.Citation, Serializable {
    /**
     * The <A HREF="http://www.opengis.org">OpenGIS consortium</A> authority.
     */
    public static final Citation OPEN_GIS = new Citation("OpenGIS consortium");

    /**
     * The <A HREF="http://www.epsg.org">European Petroleum Survey Group</A> authority.
     */
    public static final Citation EPSG = new Citation("European Petroleum Survey Group");

    /**
     * The <A HREF="http://www.geotools.org">Geotools</A> project.
     */
    public static final Citation GEOTOOLS = new Citation("Geotools");

    /**
     * An immutable empty array of strings.
     */
    private static final String[] EMPTY = new String[0];

    /**
     * An immutable empty array of responsible party.
     */
    private static final ResponsibleParty[] EMPTY_RESPONSIBLE = new ResponsibleParty[0];

    /**
     * An immutable empty array of presentation form.
     */
    private static final PresentationForm[] EMPTY_PRESENTATION = new PresentationForm[0];

    /**
     * The title.
     */
    private final String title;

    /**
     * Construct a citation with the specified title.
     */
    public Citation(final String title) {
        this.title = title;
    }

    /**
     * Name by which the cited resource is known.
     *
     * @param  locale The desired locale for the title to be returned, or <code>null</code>
     *         for a title in some default locale (may or may not be the
     *         {@linkplain Locale#getDefault() system default}).
     * @return The citation title in the given locale.
     *         If no name is available in the given locale, then some default locale is used.
     */
    public String getTitle(final Locale locale) {
        return title;
    }

    /**
     * Short name or other language name by which the cited information is known.
     * Example: "DCW" as an alternative title for "Digital Chart of the World.
     *
     * @param  locale The desired locale for the title to be returned, or <code>null</code>
     *         for a title in some default locale (may or may not be the
     *         {@linkplain Locale#getDefault() system default}).
     * @return The citation title in the given locale.
     *         If no name is available in the given locale, then some default locale is used.
     */
    public String[] getAlternateTitles(final Locale locale) {
        return EMPTY;
    }
    
    /**
     * Reference date for the cited resource.
     *
     * @todo This information is mandatory. We should not returns <code>null</code>.
     */
    public Date[] getDates() {
        return null;
    }
    
    /**
     * Version of the cited resource.
     *
     * @param  locale The desired locale for the edition to be returned, or <code>null</code>
     *         for an edition in some default locale (may or may not be the
     *         {@linkplain Locale#getDefault() system default}).
     * @return The edition in the given locale.
     *         If no edition is available in the given locale, then some default locale is used.
     */
    public String getEdition(Locale locale) {
        return null;
    }
    
    /**
     * Date of the edition, or <code>null</code> if none.
     */
    public Date getEditionDate() {
        return null;
    }
    
    /**
     * Unique identifier for the resource. Example: Universal Product Code (UPC),
     * National Stock Number (NSN).
     */
    public String[] getIdentifiers() {
        return EMPTY;
    }
    
    /**
     * Reference form of the unique identifier (ID). Example: Universal Product Code (UPC),
     * National Stock Number (NSN).
     */
    public String[] getIdentifierTypes() {
        return EMPTY;
    }

    /**
     * Name and position information for an individual or organization that is responsible
     * for the resource. Returns an empty string if there is none.
     */
    public ResponsibleParty[] getCitedResponsibleParties() {
        return EMPTY_RESPONSIBLE;
    }
    
    /**
     * Mode in which the resource is represented, or an empty string if none.
     */
    public PresentationForm[] getPresentationForm() {
        return EMPTY_PRESENTATION;
    }
    
    /**
     * Information about the series, or aggregate dataset, of which the dataset is a part.
     * Returns <code>null</code> if none.
     *
     * @param  locale The desired locale for the series to be returned, or <code>null</code>
     *         for a series in some default locale (may or may not be the
     *         {@linkplain Locale#getDefault() system default}).
     * @return The series in the given locale.
     *         If no series is available in the given locale, then some default locale is used.
     */
    public Series getSeries(Locale locale) {
        return null;
    }
    
    /**
     * Other information required to complete the citation that is not recorded elsewhere.
     * Returns <code>null</code> if none.
     *
     * @param  locale The desired locale for the details to be returned, or <code>null</code>
     *         for details in some default locale (may or may not be the
     *         {@linkplain Locale#getDefault() system default}).
     * @return The details in the given locale.
     *         If no details is available in the given locale, then some default locale is used.
     */
    public String getOtherCitationDetails(Locale locale) {
        return null;
    }
    
    /**
     * Common title with holdings note. Note: title identifies elements of a series
     * collectively, combined with information about what volumes are available at the
     * source cited. Returns <code>null</code> if there is no title.
     *
     * @param  locale The desired locale for the title to be returned, or <code>null</code>
     *         for a title in some default locale (may or may not be the
     *         {@linkplain Locale#getDefault() system default}).
     * @return The title in the given locale.
     *         If no title is available in the given locale, then some default locale is used.
     */
    public String getCollectiveTitle(Locale locale) {
        return null;
    }
    
    /**
     * International Standard Book Number, or <code>null</code> if none.
     */
    public String getISBN() {
        return null;
    }
    
    /**
     * International Standard Serial Number, or <code>null</code> if none.
     */
    public String getISSN() {
        return null;
    }

    /**
     * Compare this citation with the specified object for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final Citation that = (Citation) object;
            return Utilities.equals(this.title, that.title);
        }
        return false;
    }

    /**
     * Returns a hash code value for this citation.
     */
    public int hashCode() {
        return title.hashCode();
    }

    /**
     * Returns a string representation of this citation.
     */
    public String toString() {
        return title;
    }
}
