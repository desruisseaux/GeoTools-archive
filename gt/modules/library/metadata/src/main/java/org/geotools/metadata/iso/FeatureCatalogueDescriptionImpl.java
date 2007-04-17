/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le D�veloppement
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
package org.geotools.metadata.iso;

// J2SE direct dependencies
import java.util.Collection;
import java.util.Locale;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.content.FeatureCatalogueDescription;
import org.opengis.util.GenericName;

// Geotools dependencies
import org.geotools.metadata.iso.content.ContentInformationImpl;


/**
 * Location of the responsible individual or organization.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 *
 * @since 2.1
 */
public class FeatureCatalogueDescriptionImpl extends ContentInformationImpl
       implements FeatureCatalogueDescription
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5361236546997056467L;

    /**
     * Indication of whether or not the cited feature catalogue complies with ISO 19110.
     */
    private Boolean compliant;

    /**
     * Language(s) used within the catalogue
     */
    private Collection language;

    /**
     * Indication of whether or not the feature catalogue is included with the dataset.
     */
    private Boolean includeWithDataset;

    /**
     * Subset of feature types from cited feature catalogue occurring in dataset.
     */
    private Collection featureTypes;

    /**
     * Complete bibliographic reference to one or more external feature catalogues.
     */
    private Collection featureCatalogueCitations;

    /**
     * Construct an initially empty feature catalogue description.
     */
    public FeatureCatalogueDescriptionImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public FeatureCatalogueDescriptionImpl(final FeatureCatalogueDescription source) {
        super(source);
    }

    /**
     * Returns whether or not the cited feature catalogue complies with ISO 19110.
     */
    public Boolean isCompliant() {
        return compliant;
    }
    /**
     * Set whether or not the cited feature catalogue complies with ISO 19110.
     */
    public synchronized void setCompliant(final Boolean newValue) {
        checkWritePermission();
        compliant = newValue;
    }

    /**
     * Returns the language(s) used within the catalogue
     */
    public synchronized Collection getLanguages() {
        return language = nonNullCollection(language, Locale.class);
    }

    /**
     * Returns the language(s) used within the catalogue
     */
    public synchronized void setLanguages(final Collection newValues) {
        language = copyCollection(newValues, language, Locale.class);
    }

    /**
     * Returns whether or not the feature catalogue is included with the dataset.
     *
     * @todo Return type should be {@link Boolean}.
     */
    public boolean isIncludedWithDataset() {
        return includeWithDataset.booleanValue();
    }

    /**
     * Set whether or not the feature catalogue is included with the dataset.
     */
    public synchronized void setIncludedWithDataset(final Boolean newValue) {
        checkWritePermission();
        includeWithDataset = newValue;
    }

    /**
     * Returns the Complete bibliographic reference to one or more external feature catalogues.
     */
    public synchronized Collection getFeatureTypes() {
        return featureTypes = nonNullCollection(featureTypes, GenericName.class);
    }

    /**
     * Returns the Complete bibliographic reference to one or more external feature catalogues.
     */
    public synchronized void setFeatureTypes(final Collection newValues) {
        featureTypes = copyCollection(newValues, featureTypes, GenericName.class);
    }

    /**
     * Returns the Complete bibliographic reference to one or more external feature catalogues.
     */
    public synchronized Collection getFeatureCatalogueCitations() {
        return featureCatalogueCitations = nonNullCollection(featureCatalogueCitations, Citation.class);
    }

    /**
     * Returns the Complete bibliographic reference to one or more external feature catalogues.
     */
    public synchronized void setFeatureCatalogueCitations(final Collection newValues) {
        featureCatalogueCitations = copyCollection(newValues, featureCatalogueCitations, Citation.class);
    }
}
