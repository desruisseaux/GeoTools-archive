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
package org.geotools.metadata.identification;

// J2SE direct dependencies
import java.util.List;

import org.geotools.metadata.MetadataEntity;
import org.geotools.resources.Utilities;
import org.geotools.util.CheckedArrayList;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.identification.KeywordType;
import org.opengis.util.InternationalString;


/**
 * Keywords, their type and reference source.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 */
public class Keywords extends MetadataEntity
        implements org.opengis.metadata.identification.Keywords
{
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = 48691634443678266L;

    /**
     * Commonly used word(s) or formalised word(s) or phrase(s) used to describe the subject.
     */
    private List keywords;

    /**
     * Subject matter used to group similar keywords.
     */
    private KeywordType type;

    /**
     * Name of the formally registered thesaurus or a similar authoritative source of keywords.
     */
    private Citation thesaurusName;

    /**
     * Construct an initially empty keywords.
     */
    public Keywords() {
        super();
    }

    /* 
     * Creates keywords initialized to the given list.
     */    
    public Keywords(final List keywords) {
        setKeywords(keywords);
    }

    /**
     * Commonly used word(s) or formalised word(s) or phrase(s) used to describe the subject.
     */
    public List getKeywords() {
        final List keywords = this.keywords; // Avoid synchronization
        return (keywords!=null) ? keywords : java.util.Collections.EMPTY_LIST;
    }

    /**
     * Set commonly used word(s) or formalised word(s) or phrase(s) used to describe the subject.
     */
    public synchronized void setKeywords(final List newValues) {
        checkWritePermission();
        if (keywords == null) {
            keywords = new CheckedArrayList(InternationalString.class);
        } else {
            keywords.clear();
        }
        keywords.addAll(newValues);
    }

    /**
     * Subject matter used to group similar keywords.
     */
    public KeywordType getType() {
        return type;
    }

    /**
     * Set the subject matter used to group similar keywords.
     */
    public synchronized void setType(final KeywordType newValue) {
        checkWritePermission();
        type = newValue;
    }

    /**
     * Name of the formally registered thesaurus or a similar authoritative source of keywords.
     */
    public Citation getThesaurusName() {
        return thesaurusName;
    }
    
    /**
     * Set the name of the formally registered thesaurus or a similar authoritative source
     * of keywords.
     */
    public synchronized void setThesaurusName(final Citation newValue) {
        checkWritePermission();
        thesaurusName = newValue;
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        keywords       = (List)        unmodifiable(keywords);
        type           = (KeywordType) unmodifiable(type);
        thesaurusName  = (Citation)    unmodifiable(thesaurusName);
    }

    /**
     * Compare this keywords with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final Keywords that = (Keywords) object;
            return Utilities.equals(this.keywords,      that.keywords      ) &&
                   Utilities.equals(this.type,          that.type          ) &&
                   Utilities.equals(this.thesaurusName, that.thesaurusName )  ;
        }
        return false;
    }

    /**
     * Returns a hash code value for this object.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (keywords      != null) code ^= keywords     .hashCode();
        if (type          != null) code ^= type         .hashCode();
        if (thesaurusName != null) code ^= thesaurusName.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this object.
     */
    public String toString() {
        return String.valueOf(keywords);
    }    
}
