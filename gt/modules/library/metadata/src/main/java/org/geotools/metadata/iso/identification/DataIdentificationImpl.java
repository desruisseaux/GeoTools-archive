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
package org.geotools.metadata.iso.identification;

// J2SE direct dependencies
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.nio.charset.Charset;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.identification.DataIdentification;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.identification.CharacterSet;
import org.opengis.metadata.identification.Resolution;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.metadata.spatial.SpatialRepresentationType;
import org.opengis.util.InternationalString;


/**
 * Information required to identify a dataset.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
public class DataIdentificationImpl extends IdentificationImpl implements DataIdentification {
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
    private Collection/*<CharacterSet>*/ characterSets;

    /**
     * Main theme(s) of the datset.
     */
    private Collection topicCategories;

    /**
     * Minimum bounding rectangle within which data is available.
     * Only one of {@code getGeographicBox()} and {@link #getGeographicDescription()}
     * should be provided.
     *
     * @deprecated not in ISO 19115:2003
     */
    private Collection geographicBox;

    /**
     * Description of the geographic area within which data is available.
     * Only one of {@link #getGeographicBox()} and {@code getGeographicDescription()}
     * should be provided.
     *
     * @deprecated not in ISO 19115:2003
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
    public DataIdentificationImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public DataIdentificationImpl(final DataIdentification source) {
        super(source);
    }

    /**
     * Creates a data identification initialized to the specified values.
     */
    public DataIdentificationImpl(final Citation citation, 
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
     *
     * @deprecated Use {@link #getCharacterSets} instead.
     */
    public Charset getCharacterSet() {
        final Collection characterSet = getCharacterSets();
        return characterSet.isEmpty() ? null : ((CharacterSet) characterSet.iterator().next()).toCharset();
    }

    /**
     * Full name of the character coding standard used for the dataset.
     */
    public synchronized Collection getCharacterSets() {
        return characterSets = nonNullCollection(characterSets, CharacterSet.class);
    }

    /**
     * Set the full name of the character coding standard used for the dataset.
     *
     * @deprecated Use {@link #setCharacterSets} instead.
     */
    public synchronized void setCharacterSet(final Charset newValue) {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the full name of the character coding standard used for the dataset.
     */
    public synchronized void setCharacterSets(final Collection/*<CharacterSet>*/ newValues) {
        characterSets = copyCollection(newValues, characterSets, CharacterSet.class);
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
     * Only one of {@code getGeographicBox()} and {@link #getGeographicDescription()}
     * should be provided.
     *
     * @deprecated not in ISO 19115:2003
     */
    public synchronized Collection getGeographicBox() {
        return geographicBox = nonNullCollection(geographicBox, GeographicBoundingBox.class);
    }

    /**
     * Set the minimum bounding rectangle within which data is available.
     *
     * @deprecated not in ISO 19115:2003
     */
    public synchronized void setGeographicBox(final Collection newValues)  {
        geographicBox = copyCollection(newValues, geographicBox, GeographicBoundingBox.class);
    }

    /**
     * Description of the geographic area within which data is available.
     * Only one of {@link #getGeographicBox()} and {@code getGeographicDescription()}
     * should be provided.
     *
     * @deprecated not in ISO 19115:2003
     */
    public synchronized Collection getGeographicDescription() {
        return geographicDescription = nonNullCollection(geographicDescription,
                                                         InternationalString.class);
    }

    /**
     * Set the description of the geographic area within which data is available.
     *
     * @deprecated not in ISO 19115:2003
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
}
