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
package org.geotools.metadata;

// J2SE direct dependencies
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import org.geotools.metadata.content.ContentInformation;
import org.geotools.resources.Utilities;
import org.geotools.util.CheckedHashSet;
import org.opengis.metadata.citation.Citation;
import org.opengis.util.GenericName;
 

/**
 * Location of the responsible individual or organization.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class FeatureCatalogueDescription extends ContentInformation
       implements org.opengis.metadata.content.FeatureCatalogueDescription
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2566307080447896276L;
    
    /**
     * Indication of whether or not the cited feature catalogue complies with ISO 19110.
     */
    private boolean compliant;

    /**
     * Language(s) used within the catalogue
     */
    private Set language;

    /**
     * Indication of whether or not the feature catalogue is included with the dataset.
     */
    private boolean includeWithDataset;

    /**
     * Subset of feature types from cited feature catalogue occurring in dataset.
     */
    private Set featureTypes;

    /**
     * Complete bibliographic reference to one or more external feature catalogues.
     */
    private Set featureCatalogueCitations;

    /**
     * Construct an initially empty feature catalogue description.
     */
    public FeatureCatalogueDescription() {
    }
    
    /**
     * Returns whether or not the cited feature catalogue complies with ISO 19110.
     */
    public boolean isCompliant() {
        return compliant;
    }
    /**
     * Set whether or not the cited feature catalogue complies with ISO 19110.
     */
    public synchronized void setCompliant(final boolean newValue) {
        checkWritePermission();
        compliant = newValue;
    }
    
    /**
     * Returns the language(s) used within the catalogue
     */
    public Set getLanguages() {
        final Set language = this.language; // Avoid synchronization
        return (language!=null) ? language : Collections.EMPTY_SET;
    }
    
    /**
     * Returns the language(s) used within the catalogue
     */
    public synchronized void setLanguages(final Set newValues) {
        checkWritePermission();
        if (language == null) {
            language = new CheckedHashSet(Locale.class);
        } else {
            language.clear();
        }
        language.addAll(newValues);
    }
    
    /**
     * Returns whether or not the feature catalogue is included with the dataset.
     */
    public boolean isIncludedWithDataset() {
        return includeWithDataset;
    }
    
    /**
     * Set whether or not the feature catalogue is included with the dataset.
     */
    public synchronized void setIncludedWithDataset(final boolean newValue) {
        checkWritePermission();
        includeWithDataset = newValue;
    }

    /**
     * Returns the Complete bibliographic reference to one or more external feature catalogues.
     */
    public Set getFeatureTypes() {
        final Set featureTypes = this.featureTypes; // Avoid synchronization
        return (featureTypes!=null) ? featureTypes : Collections.EMPTY_SET;
    }
    
    /**
     * Returns the Complete bibliographic reference to one or more external feature catalogues.
     *
     * @todo Use CheckedHashSet(GenericName.class)
     */
    public synchronized void setFeatureTypes(final Set newValues) {
        checkWritePermission();
        if (featureTypes == null) {
            featureTypes = new CheckedHashSet(GenericName.class);
        } else {
            featureTypes.clear();
        }
        featureTypes.addAll(newValues);
    }

    /**
     * Returns the Complete bibliographic reference to one or more external feature catalogues.
     */
    public Set getFeatureCatalogueCitations() {
        final Set featureCatalogueCitations = this.featureCatalogueCitations; // Avoid synchronization
        return (featureCatalogueCitations!=null) ? featureCatalogueCitations : Collections.EMPTY_SET;
    }
    
    /**
     * Returns the Complete bibliographic reference to one or more external feature catalogues.
     */
    public synchronized void setFeatureCatalogueCitations(final Set newValues) {
        checkWritePermission();
        if (featureCatalogueCitations == null) {
            featureCatalogueCitations = new CheckedHashSet(Citation.class);
        } else {
            featureCatalogueCitations.clear();
        }
        featureCatalogueCitations.addAll(newValues);
    }
    
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        language                  = (Set) unmodifiable(language);
        featureTypes              = (Set) unmodifiable(featureTypes);
        featureCatalogueCitations = (Set) unmodifiable(featureCatalogueCitations);
    }

    /**
     * Compare this feature catalogue description with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(this)) {
            final FeatureCatalogueDescription that = (FeatureCatalogueDescription) object;
            return                 (compliant               == that.compliant                ) &&
                                   (includeWithDataset      == that.includeWithDataset       ) &&
                   Utilities.equals(featureTypes,              that.featureTypes             ) &&
                   Utilities.equals(featureCatalogueCitations, that.featureCatalogueCitations) &&
                   Utilities.equals(language,                  that.language                 );
        }
        return false;
    }

    /**
     * Returns a hash code value for this catalogue. For performance reason, this method do
     * not uses all attributes for computing the hash code. Instead, it uses the attributes
     * that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (featureCatalogueCitations != null) code ^= featureCatalogueCitations.hashCode();
        if (language                  != null) code ^= language                 .hashCode();
        return code;
    }
}
