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
import java.util.Collection;
import java.util.Locale;
import java.nio.charset.Charset;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.identification.Resolution;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.metadata.spatial.SpatialRepresentationType;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.resources.Utilities;


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
    private Collection spatialRepresentationTypes;

    /**
     * Factor which provides a general understanding of the density of spatial data
     * in the dataset.
     */
    private Collection spatialResolutions;

    /**
     * Language(s) used within the dataset.
     */
    private Collection language;

    /**
     * Full name of the character coding standard used for the dataset.
     */
    private Charset characterSet;

    /**
     * Main theme(s) of the datset.
     */
    private Collection topicCategories;

    /**
     * Minimum bounding rectangle within which data is available.
     * Only one of <code>getGeographicBox()</code> and {@link #getGeographicDescription()}
     * should be provided.
     */
    private Collection geographicBox;

    /**
     * Description of the geographic area within which data is available.
     * Only one of {@link #getGeographicBox()} and <code>getGeographicDescription()</code>
     * should be provided.
     */
    private Collection geographicDescription;

    /**
     * Description of the dataset in the producer’s processing environment, including items
     * such as the software, the computer operating system, file name, and the dataset size
     */
    private InternationalString environmentDescription;

    /**
     * Additional extent information including the bounding polygon, vertical, and temporal
     * extent of the dataset.
     */
    private Collection extent;

    /**
     * Any other descriptive information about the dataset.
     */
    private InternationalString supplementalInformation;

    /**
     * Constructs an initially empty data identification.
     */
    public DataIdentification() {
    }

    /**
     * Creates a data identification initialized to the specified values.
     */
    public DataIdentification(final Citation citation, 
                              final InternationalString abstracts, 
                              final Collection language, 
                              final Collection topicCategories)
    {
        super(citation, abstracts);
        setLanguage       (language       );
        setTopicCategories(topicCategories);
    }
    
    /**
     * Method used to spatially represent geographic information.
     */
    public synchronized Collection getSpatialRepresentationTypes() {
        return spatialRepresentationTypes = nonNullCollection(spatialRepresentationTypes,
                                                              SpatialRepresentationType.class);
    }

    /**
     * Set the method used to spatially represent geographic information.
     */
    public synchronized void setSpatialRepresentationTypes(final Collection newValues) {
        spatialRepresentationTypes = copyCollection(newValues, spatialRepresentationTypes,
                                                    SpatialRepresentationType.class);
    }

    /**
     * Factor which provides a general understanding of the density of spatial data
     * in the dataset.
     */
    public synchronized Collection getSpatialResolutions() {
        return spatialResolutions = nonNullCollection(spatialResolutions, Resolution.class);
    }

    /**
     * Set the factor which provides a general understanding of the density of spatial data
     * in the dataset.
     */
    public synchronized void setSpatialResolutions(final Collection newValues)  {
        spatialResolutions = copyCollection(newValues, spatialResolutions, Resolution.class);
    }

    /**
     * Language(s) used within the dataset.
     */
    public synchronized Collection getLanguage() {
        return language = nonNullCollection(language, Locale.class);
    }

    /**
     * Set the language(s) used within the dataset.
     */
    public synchronized void setLanguage(final Collection newValues)  {
        language = copyCollection(newValues, language, Locale.class);
    }

    /**
     * Full name of the character coding standard used for the dataset.
     */
    public Charset getCharacterSet() {
        return characterSet;
    }

    /**
     * Set the full name of the character coding standard used for the dataset.
     */
    public synchronized void setCharacterSet(final Charset newValue)  {
        checkWritePermission();
        characterSet = newValue;
    }
    
    /**
     * Main theme(s) of the datset.
     */
    public synchronized Collection getTopicCategories()  {
        return topicCategories = nonNullCollection(topicCategories, TopicCategory.class);
    }

    /**
     * Set the main theme(s) of the datset.
     */
    public synchronized void setTopicCategories(final Collection newValues)  {
        topicCategories = copyCollection(newValues, topicCategories, TopicCategory.class);
    }
    
    /**
     * Minimum bounding rectangle within which data is available.
     * Only one of <code>getGeographicBox()</code> and {@link #getGeographicDescription()}
     * should be provided.
     */
    public synchronized Collection getGeographicBox() {
        return geographicBox = nonNullCollection(geographicBox, GeographicBoundingBox.class);
    }

    /**
     * Set the minimum bounding rectangle within which data is available.
     */
    public synchronized void setGeographicBox(final Collection newValues)  {
        geographicBox = copyCollection(newValues, geographicBox, GeographicBoundingBox.class);
    }

    /**
     * Description of the geographic area within which data is available.
     * Only one of {@link #getGeographicBox()} and <code>getGeographicDescription()</code>
     * should be provided.
     */
    public synchronized Collection getGeographicDescription() {
        return geographicDescription = nonNullCollection(geographicDescription,
                                                         InternationalString.class);
    }

    /**
     * Set the description of the geographic area within which data is available.
     */
    public synchronized void setGeographicDescription(final Collection newValues)  {
        geographicDescription = copyCollection(newValues, geographicDescription,
                                               InternationalString.class);
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
    public synchronized Collection getExtent() {
        return extent = nonNullCollection(extent, Extent.class);
    }

    /**
     * Set additional extent information.
     */
    public synchronized void setExtent(final Collection newValues)  {
        extent = copyCollection(newValues, extent, Extent.class);
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
        spatialRepresentationTypes = (Collection)          unmodifiable(spatialRepresentationTypes);
        spatialResolutions         = (Collection)          unmodifiable(spatialResolutions);
        language                   = (Collection)          unmodifiable(language);
        characterSet               = (Charset)             unmodifiable(characterSet);
        topicCategories            = (Collection)          unmodifiable(topicCategories);
        geographicBox              = (Collection)          unmodifiable(geographicBox);
        geographicDescription      = (Collection)          unmodifiable(geographicDescription);
        environmentDescription     = (InternationalString) unmodifiable(environmentDescription);
        extent                     = (Collection)          unmodifiable(extent);
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
