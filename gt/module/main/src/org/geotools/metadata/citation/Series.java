/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
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

// OpenGIS dependencies
import org.geotools.metadata.MetadataEntity;
import org.geotools.resources.Utilities;
import org.geotools.util.SimpleInternationalString;
import org.opengis.util.InternationalString;


/**
 * Information about the series, or aggregate dataset, to which a dataset belongs.
 *
 * @author Jody Garnett
 * @author Martin Desruisseaux
 */
public class Series extends MetadataEntity
       implements org.opengis.metadata.citation.Series
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 2784101441023323052L;

    /**
     * Name of the series, or aggregate dataset, of which the dataset is a part.
     */
    private InternationalString name;

    /**
     * Information identifying the issue of the series.
     */
    private String issueIdentification;

    /**
     * Details on which pages of the publication the article was published.
     */
    private String page;

    /**
     * Constructs a default series.
     */
    public Series() {
    }

    /**
     * Constructs a series with the specified name.
     */
    public Series(final CharSequence name) {
        if (name instanceof InternationalString) {
            this.name = (InternationalString) name;
        } else {
            this.name = new SimpleInternationalString(name.toString());
        }
    }

    /**
     * Returne the name of the series, or aggregate dataset, of which the dataset is a part.
     */
    public InternationalString getName() {
        return name;
    }

    /**
     * Set the name of the series, or aggregate dataset, of which the dataset is a part.
     */
    public synchronized void setName(final InternationalString newValue) {
        checkWritePermission();
        name = newValue;
    }

    /**
     * Returns information identifying the issue of the series.
     */
    public String getIssueIdentification() {
        return issueIdentification;
    }

    /**
     * Set information identifying the issue of the series.
     */
    public synchronized void setIssueIdentification(final String newValue) {
        checkWritePermission();
        issueIdentification = newValue;
    }

    /**
     * Returns details on which pages of the publication the article was published.
     */
    public String getPage() {
        return page;
    }

    /**
     * Set details on which pages of the publication the article was published.
     */
    public synchronized void setPage(final String newValue) {
        checkWritePermission();
        page = newValue;
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        name                = (InternationalString) unmodifiable(name);
        issueIdentification = (String)              unmodifiable(issueIdentification);
        page                = (String)              unmodifiable(page);
    }

    /**
     * Compare this series with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final Series that = (Series) object;
            return Utilities.equals(this.name,                that.name               ) &&
                   Utilities.equals(this.issueIdentification, that.issueIdentification) &&
                   Utilities.equals(this.page,                that.page               );
        }
        return false;
    }

    /**
     * Returns a hash code value for this series.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (name != null) code ^= name.hashCode();
        if (page != null) code ^= page.hashCode();
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
