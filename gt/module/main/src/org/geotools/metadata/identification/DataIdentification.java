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
import java.util.Set;
import java.util.Collections;
import java.util.Locale;

// OpenGIS direct dependencies
// OpenGIS direct dependencies
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.extent.GeographicDescription;
import org.opengis.metadata.spatial.SpatialRepresentationType;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.metadata.identification.Resolution;
import org.opengis.util.InternationalString;
import org.opengis.metadata.citation.Citation;

// Geotools dependencies
import org.geotools.metadata.MetadataEntity;
import org.geotools.resources.Utilities;
import org.geotools.util.CheckedHashSet;


/**
 * Information required to identify a dataset.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class DataIdentification extends Identification
        implements org.opengis.metadata.identification.DataIdentification
{
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = -4418520352804939785L;

    /**
     * Method used to spatially represent geographic information.
     */
    private Set spatialRepresentationTypes;

    /**
     * Factor which provides a general understanding of the density of spatial data
     * in the dataset.
     */
    private Set spatialResolutions;

    /**
     * Language(s) used within the dataset.
     */
    private Set language;

    /**
     * Full name of the character coding standard used for the dataset.
     */
    private String characterSet;

    /**
     * Main theme(s) of the datset.
     */
    private Set topicCategories;

    /**
     * Minimum bounding rectangle within which data is available.
     * Only one of <code>getGeographicBox()</code> and {@link #getGeographicDescription()}
     * should be provided.
     */
    private Set geographicBox;

    /**
     * Description of the geographic area within which data is available.
     * Only one of {@link #getGeographicBox()} and <code>getGeographicDescription()</code>
     * should be provided.
     */
    private Set geographicDescription;

    /**
     * Description of the dataset in the producer’s processing environment, including items
     * such as the software, the computer operating system, file name, and the dataset size
     */
    private InternationalString environmentDescription;

    /**
     * Additional extent information including the bounding polygon, vertical, and temporal
     * extent of the dataset.
     */
    private Set extent;

    /**
     * Any other descriptive information about the dataset.
     */
    private InternationalString supplementalInformation;

    /**
     * Construct an initially empty data identification.
     */
    public DataIdentification() {
    }

    /**
     * Creates a data identification initialized to the specified values.
     */
    public DataIdentification(final Citation citation, 
                              final InternationalString abstracts, 
                              final Set language, 
                              final Set topicCategories)
    {
        super(citation, abstracts);
        setLanguage       (language       );
        setTopicCategories(topicCategories);
    }
    
    /**
     * Method used to spatially represent geographic information.
     */
    public Set getSpatialRepresentationTypes() {
        final Set spatialRepresentationTypes = this.spatialRepresentationTypes; // Avoid synchronization
        return (spatialRepresentationTypes!=null) ? spatialRepresentationTypes : Collections.EMPTY_SET;
    }

    /**
     * Set the method used to spatially represent geographic information.
     */
    public synchronized void setSpatialRepresentationTypes(final Set newValues) {
        checkWritePermission();
        if (spatialRepresentationTypes == null) {
            spatialRepresentationTypes = new CheckedHashSet(SpatialRepresentationType.class);
        } else {
            spatialRepresentationTypes.clear();
        }
        spatialRepresentationTypes.addAll(newValues);
    }

    /**
     * Factor which provides a general understanding of the density of spatial data
     * in the dataset.
     */
    public Set getSpatialResolutions() {
        final Set spatialResolutions = this.spatialResolutions; // Avoid synchronization
        return (spatialResolutions!=null) ? spatialResolutions : Collections.EMPTY_SET;
    }

    /**
     * Set the factor which provides a general understanding of the density of spatial data
     * in the dataset.
     */
    public synchronized void setSpatialResolutions(final Set newValues)  {
        checkWritePermission();
        if (spatialResolutions == null) {
            spatialResolutions = new CheckedHashSet(Resolution.class);
        } else {
            spatialResolutions.clear();
        }
        spatialResolutions.addAll(newValues);
    }

    /**
     * Language(s) used within the dataset.
     */
    public Set getLanguage() {
        final Set language = this.language; // Avoid synchronization
        return (language!=null) ? language : Collections.EMPTY_SET;
    }

    /**
     * Set the language(s) used within the dataset.
     */
    public synchronized void setLanguage(final Set newValues)  {
        checkWritePermission();
        if (language == null) {
            language = new CheckedHashSet(Locale.class);
        } else {
            language.clear();
        }
        language.addAll(newValues);
    }

    /**
     * Full name of the character coding standard used for the dataset.
     */
    public String getCharacterSet() {
        return characterSet;
    }

    /**
     * Set the full name of the character coding standard used for the dataset.
     */
    public synchronized void setCharacterSet(final String newValue)  {
        checkWritePermission();
        characterSet = newValue;
    }
    
    /**
     * Main theme(s) of the datset.
     */
    public Set getTopicCategories()  {
        final Set topicCategories = this.topicCategories; // Avoid synchronization
        return (topicCategories!=null) ? topicCategories : Collections.EMPTY_SET;
    }

    /**
     * Set the main theme(s) of the datset.
     */
    public synchronized void setTopicCategories(final Set newValues)  {
        checkWritePermission();
        if (topicCategories == null) {
            topicCategories = new CheckedHashSet(TopicCategory.class);
        } else {
            topicCategories.clear();
        }
        topicCategories.addAll(newValues);
    }
    
    /**
     * Minimum bounding rectangle within which data is available.
     * Only one of <code>getGeographicBox()</code> and {@link #getGeographicDescription()}
     * should be provided.
     */
    public Set getGeographicBox() {
        final Set geographicBox = this.geographicBox; // Avoid synchronization
        return (geographicBox!=null) ? geographicBox : Collections.EMPTY_SET;
    }

    /**
     * Set the minimum bounding rectangle within which data is available.
     */
    public synchronized void setGeographicBox(final Set newValues)  {
        checkWritePermission();
        if (geographicBox == null) {
            geographicBox = new CheckedHashSet(GeographicBoundingBox.class);
        } else {
            geographicBox.clear();
        }
        geographicBox.addAll(newValues);
    }

    /**
     * Description of the geographic area within which data is available.
     * Only one of {@link #getGeographicBox()} and <code>getGeographicDescription()</code>
     * should be provided.
     */
    public Set getGeographicDescription() {
        final Set geographicDescription = this.geographicDescription; // Avoid synchronization
        return (geographicDescription!=null) ? geographicDescription : Collections.EMPTY_SET;
    }

    /**
     * Set the description of the geographic area within which data is available.
     */
    public synchronized void setGeographicDescription(final Set newValues)  {
        checkWritePermission();
        if (geographicDescription == null) {
            geographicDescription = new CheckedHashSet(InternationalString.class);
        } else {
            geographicDescription.clear();
        }
        geographicDescription.addAll(newValues);
    }

    /**
     * Description of the dataset in the producer’s processing environment, including items
     * such as the software, the computer operating system, file name, and the dataset size.
     */
    public InternationalString getEnvironmentDescription() {
        return environmentDescription;
    }

    /**
     * Set the description of the dataset in the producer’s processing environment.
     */
    public synchronized void setEnvironmentDescription(final InternationalString newValue)  {
        checkWritePermission();
        environmentDescription = newValue;
    }

    /**
     * Additional extent information including the bounding polygon, vertical, and temporal
     * extent of the dataset.
     */
    public Set getExtent() {
        final Set extent = this.extent; // Avoid synchronization
        return (extent!=null) ? extent : Collections.EMPTY_SET;
    }

    /**
     * Set additional extent information.
     */
    public synchronized void setExtent(final Set newValues)  {
        checkWritePermission();
        if (extent == null) {
            extent = new CheckedHashSet(Extent.class);
        } else {
            extent.clear();
        }
        extent.addAll(newValues);
    }

    /**
     * Any other descriptive information about the dataset.
     */
    public InternationalString getSupplementalInformation() {
        return supplementalInformation;
    }

    /**
     * Set any other descriptive information about the dataset.
     */
   public synchronized void setSupplementalInformation(final InternationalString newValue)  {
        checkWritePermission();
        supplementalInformation = newValue;
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        spatialRepresentationTypes = (Set)                 unmodifiable(spatialRepresentationTypes);
        spatialResolutions         = (Set)                 unmodifiable(spatialResolutions);
        language                   = (Set)                 unmodifiable(language);
        characterSet               = (String)              unmodifiable(characterSet);
        topicCategories            = (Set)                 unmodifiable(topicCategories);
        geographicBox              = (Set)                 unmodifiable(geographicBox);
        geographicDescription      = (Set)                 unmodifiable(geographicDescription);
        environmentDescription     = (InternationalString) unmodifiable(environmentDescription);
        extent                     = (Set)                 unmodifiable(extent);
        supplementalInformation    = (InternationalString) unmodifiable(supplementalInformation);
    }

    /**
     * Compare this data identification with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final DataIdentification that = (DataIdentification) object;
            return Utilities.equals(this.spatialRepresentationTypes, that.spatialRepresentationTypes   ) &&
                   Utilities.equals(this.spatialResolutions,         that.spatialResolutions           ) &&
                   Utilities.equals(this.language,                   that.language                     ) &&
                   Utilities.equals(this.characterSet,               that.characterSet                 ) &&
                   Utilities.equals(this.topicCategories,            that.topicCategories              ) &&
                   Utilities.equals(this.geographicBox,              that.geographicBox                ) &&
                   Utilities.equals(this.geographicDescription,      that.geographicDescription        ) &&
                   Utilities.equals(this.environmentDescription,     that.environmentDescription       ) &&
                   Utilities.equals(this.extent,                     that.extent                       ) &&
                   Utilities.equals(this.supplementalInformation,    that.supplementalInformation      )  ;
        }
        return false;
    }

    /**
     * Returns a hash code value for this identification.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (spatialRepresentationTypes != null) code ^= spatialRepresentationTypes.hashCode();
        if (spatialResolutions         != null) code ^= spatialResolutions        .hashCode();
        if (language                   != null) code ^= language                  .hashCode();
        if (characterSet               != null) code ^= characterSet              .hashCode();
        if (topicCategories            != null) code ^= topicCategories           .hashCode();
        if (geographicBox              != null) code ^= geographicBox             .hashCode();
        if (geographicDescription      != null) code ^= geographicDescription     .hashCode();
        if (environmentDescription     != null) code ^= environmentDescription    .hashCode();
        if (extent                     != null) code ^= extent                    .hashCode();
        if (supplementalInformation    != null) code ^= supplementalInformation   .hashCode();
        return code;
    }

    /**
     * Returns a string representation of this identification.
     */
    public String toString() {
        return String.valueOf(spatialRepresentationTypes);
    }    
}
