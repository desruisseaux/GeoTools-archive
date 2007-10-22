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
package org.geotools.metadata.iso;

// J2SE direct dependencies
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.nio.charset.Charset;

// OpenGIS dependencies
import org.opengis.metadata.MetaData;
import org.opengis.metadata.ApplicationSchemaInformation;
import org.opengis.metadata.MetadataExtensionInformation;
import org.opengis.metadata.PortrayalCatalogueReference;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.constraint.Constraints;
import org.opengis.metadata.distribution.Distribution;
import org.opengis.metadata.identification.CharacterSet;
import org.opengis.metadata.identification.Identification;
import org.opengis.metadata.maintenance.MaintenanceInformation;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.metadata.quality.DataQuality;
import org.opengis.metadata.spatial.SpatialRepresentation;
import org.opengis.referencing.ReferenceSystem;


/**
 * Root entity which defines metadata about a resource or resources.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
public class MetaDataImpl extends MetadataEntity implements MetaData {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5600409558876701144L;

    /**
     * Unique identifier for this metadata file, or {@code null} if none.
     */
    private String fileIdentifier;

    /**
     * Language used for documenting metadata.
     */
    private Locale language;

    /**
     * Information about an alternatively used localized character
     * strings for linguistic extensions.
     */
    private Collection locales;

    /**
     * Full name of the character coding standard used for the metadata set.
     */
    private CharacterSet characterSet;

    /**
     * File identifier of the metadata to which this metadata is a subset (child).
     */
    private String parentIdentifier;

    /**
     * Scope to which the metadata applies.
     */
    private Collection hierarchyLevels;

    /**
     * Name of the hierarchy levels for which the metadata is provided.
     */
    private Collection hierarchyLevelNames;

    /**
     * Parties responsible for the metadata information.
     */
    private Collection contacts;

    /**
     * Uniformed Resource Identifier (URI) of the dataset to which the metadata applies.
     */
    private String dataSetUri;

    /**
     * Date that the metadata was created, in milliseconds ellapsed since January 1st, 1970.
     * If not defined, then then value is {@link Long#MIN_VALUE}.
     */
    private long dateStamp = Long.MIN_VALUE;

    /**
     * Name of the metadata standard (including profile name) used.
     */
    private String metadataStandardName;

    /**
     * Version (profile) of the metadata standard used.
     */
    private String metadataStandardVersion;

    /**
     * Digital representation of spatial information in the dataset.
     */
    private Collection spatialRepresentationInfo;

    /**
     * Description of the spatial and temporal reference systems used in the dataset.
     */
    private Collection referenceSystemInfo;

    /**
     * Information describing metadata extensions.
     */
    private Collection metadataExtensionInfo;

    /**
     * Basic information about the resource(s) to which the metadata applies.
     */
    private Collection identificationInfo;

    /**
     * Provides information about the feature catalogue and describes the coverage and
     * image data characteristics.
     */
    private Collection contentInfo;

    /**
     * Provides information about the distributor of and options for obtaining the resource(s).
     */
    private Distribution distributionInfo;

    /**
     * Provides overall assessment of quality of a resource(s).
     */
    private Collection dataQualityInfo;

    /**
     * Provides information about the catalogue of rules defined for the portrayal of a resource(s).
     */
    private Collection portrayalCatalogueInfo;

    /**
     * Provides restrictions on the access and use of data.
     */
    private Collection metadataConstraints;

    /**
     * Provides information about the conceptual schema of a dataset.
     */
    private Collection applicationSchemaInfo;

    /**
     * Provides information about the frequency of metadata updates, and the scope of those updates.
     */
    private MaintenanceInformation metadataMaintenance;

    /**
     * Creates an initially empty metadata.
     */
    public MetaDataImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public MetaDataImpl(final MetaData source) {
        super(source);
    }

    /**
     * Creates a meta data initialised to the specified values.
     *
     * @param contact   Party responsible for the metadata information.
     * @param dateStamp Date that the metadata was created.
     * @param identificationInfo Basic information about the resource
     *        to which the metadata applies.
     */
    public MetaDataImpl(final ResponsibleParty contact,
                        final Date             dateStamp,
                        final Identification   identificationInfo)
    {
        setContacts          (Collections.singleton(contact));
        setDateStamp         (dateStamp);
        setIdentificationInfo(Collections.singleton(identificationInfo));
    }

    /**
     * Returns the unique identifier for this metadata file, or {@code null} if none.
     */
    public String getFileIdentifier() {
        return fileIdentifier;
    }

    /**
     * Set the unique identifier for this metadata file, or {@code null} if none.
     */
    public synchronized void setFileIdentifier(final String newValue) {
        checkWritePermission();
        fileIdentifier = newValue;
    }

    /**
     * Returns the language used for documenting metadata.
     */
    public Locale getLanguage() {
        return language;
    }

    /**
     * Set the language used for documenting metadata.
     */
    public synchronized void setLanguage(final Locale newValue) {
        checkWritePermission();
        language = newValue;
    }

    /**
     * Returns the full name of the character coding standard used for the metadata set.
     */
    public CharacterSet getCharacterSet()  {
        return characterSet;
    }

    /**
     * Set the full name of the character coding standard used for the metadata set.
     */
    public synchronized void setCharacterSet(final CharacterSet newValue) {
        checkWritePermission();
        characterSet = newValue;
    }

    /**
     * Returns the file identifier of the metadata to which this metadata is a subset (child).
     */
    public String getParentIdentifier() {
        return parentIdentifier;
    }

    /**
     * Set the file identifier of the metadata to which this metadata is a subset (child).
     */
    public synchronized void setParentIdentifier(final String newValue) {
        checkWritePermission();
        fileIdentifier = newValue;
    }

    /**
     * Returns the scope to which the metadata applies.
     */
    public synchronized Collection getHierarchyLevels() {
        return hierarchyLevels = nonNullCollection(hierarchyLevels, ScopeCode.class);
    }

    /**
     * Set the scope to which the metadata applies.
     */
    public synchronized void setHierarchyLevels(final Collection newValues) {
        hierarchyLevels = copyCollection(newValues, hierarchyLevels, ScopeCode.class);
    }

    /**
     * Returns the name of the hierarchy levels for which the metadata is provided.
     */
    public synchronized Collection getHierarchyLevelNames() {
        return hierarchyLevelNames = nonNullCollection(hierarchyLevelNames, String.class);
    }

    /**
     * Set the name of the hierarchy levels for which the metadata is provided.
     */
    public synchronized void setHierarchyLevelNames(final Collection newValues) {
        hierarchyLevelNames = copyCollection(newValues, hierarchyLevelNames, String.class);
    }

    /**
     * Returns the party responsible for the metadata information.
     *
     * @deprecated use {@link #getContacts}.
     */
    public ResponsibleParty getContact() {
        final Collection contacts = getContacts();
        return contacts.isEmpty() ? null : (ResponsibleParty) contacts.iterator().next();
    }

    /**
     * Returns the parties responsible for the metadata information.
     */
    public synchronized Collection getContacts() {
        return contacts = nonNullCollection(contacts, ResponsibleParty.class);
    }

    /**
     * Set the party responsible for the metadata information.
     *
     * @deprecated use {@link #setContacts}.
     */
    public synchronized void setContact(final ResponsibleParty newValue) {
        setContacts(Collections.singleton(newValue));
    }

    /**
     * Set the parties responsible for the metadata information.
     */
    public synchronized void setContacts(final Collection newValues) {
        checkWritePermission();
        contacts = copyCollection(newValues, contacts, ResponsibleParty.class);
    }

    /**
     * Returns the date that the metadata was created.
     */
    public synchronized Date getDateStamp() {
        return (dateStamp!=Long.MIN_VALUE) ? new Date(dateStamp) : (Date)null;
    }

    /**
     * Set the date that the metadata was created.
     */
    public synchronized void setDateStamp(final Date newValue) {
        checkWritePermission();
        dateStamp = (newValue!=null) ? newValue.getTime() : Long.MIN_VALUE;
    }

    /**
     * Returns the name of the metadata standard (including profile name) used.
     */
    public String getMetadataStandardName() {
        return metadataStandardName;
    }

    /**
     * Name of the metadata standard (including profile name) used.
     */
    public synchronized void setMetadataStandardName(final String newValue) {
        checkWritePermission();
        metadataStandardName = newValue;
    }

    /**
     * Returns the version (profile) of the metadata standard used.
     */
    public String getMetadataStandardVersion() {
        return metadataStandardVersion;
    }

    /**
     * Set the version (profile) of the metadata standard used.
     */
    public synchronized void setMetadataStandardVersion(final String newValue) {
        checkWritePermission();
        metadataStandardVersion = newValue;
    }

    /**
     * Returns the digital representation of spatial information in the dataset.
     */
    public synchronized Collection getSpatialRepresentationInfo() {
        return spatialRepresentationInfo = nonNullCollection(spatialRepresentationInfo,
                                                             SpatialRepresentation.class);
    }

    /**
     * Set the digital representation of spatial information in the dataset.
     */
    public synchronized void setSpatialRepresentationInfo(final Collection newValues) {
        spatialRepresentationInfo = copyCollection(newValues, spatialRepresentationInfo,
                                                   SpatialRepresentation.class);
    }

    /**
     * Returns the description of the spatial and temporal reference systems used in the dataset.
     */
     public synchronized Collection getReferenceSystemInfo() {
        return referenceSystemInfo = nonNullCollection(referenceSystemInfo, ReferenceSystem.class);
    }

    /**
     * Set the description of the spatial and temporal reference systems used in the dataset.
     */
    public synchronized void setReferenceSystemInfo(final Collection newValues) {
        referenceSystemInfo = copyCollection(newValues, referenceSystemInfo, ReferenceSystem.class);
    }

    /**
     * Returns information describing metadata extensions.
     */
    public synchronized Collection getMetadataExtensionInfo() {
        return metadataExtensionInfo = nonNullCollection(metadataExtensionInfo,
                                                         MetadataExtensionInformation.class);
    }

    /**
     * Set information describing metadata extensions.
     */
    public synchronized void setMetadataExtensionInfo(final Collection newValues) {
        metadataExtensionInfo = copyCollection(newValues, metadataExtensionInfo,
                                               MetadataExtensionInformation.class);
    }

    /**
     * Returns basic information about the resource(s) to which the metadata applies.
     */
    public synchronized Collection getIdentificationInfo() {
        return identificationInfo = nonNullCollection(identificationInfo, Identification.class);
    }

    /**
     * Set basic information about the resource(s) to which the metadata applies.
     */
    public synchronized void setIdentificationInfo(final Collection newValues) {
        identificationInfo = copyCollection(newValues, identificationInfo, Identification.class);
    }

    /**
     * Provides information about the feature catalogue and describes the coverage and
     * image data characteristics.
     */
    public synchronized Collection getContentInfo() {
        return contentInfo = nonNullCollection(contentInfo, Identification.class);
    }

    /**
     * Set information about the feature catalogue and describes the coverage and
     * image data characteristics.
     */
    public synchronized void setContentInfo(final Collection newValues) {
        contentInfo = copyCollection(newValues, contentInfo, Identification.class);
    }

    /**
     * Provides information about the distributor of and options for obtaining the resource(s).
     */
    public Distribution getDistributionInfo() {
        return distributionInfo;
    }

    /**
     * Provides information about the distributor of and options for obtaining the resource(s).
     */
    public synchronized void setDistributionInfo(final Distribution newValue) {
        checkWritePermission();
        distributionInfo = newValue;
    }

    /**
     * Provides overall assessment of quality of a resource(s).
     */
    public synchronized Collection getDataQualityInfo() {
        return dataQualityInfo = nonNullCollection(dataQualityInfo, DataQuality.class);
    }

    /**
     * Set overall assessment of quality of a resource(s).
     */
    public synchronized void setDataQualityInfo(final Collection newValues) {
        dataQualityInfo = copyCollection(newValues, dataQualityInfo, DataQuality.class);
    }

     /**
      * Provides information about the catalogue of rules defined for the portrayal of a
      * resource(s).
      */
     public synchronized Collection getPortrayalCatalogueInfo() {
        return portrayalCatalogueInfo = nonNullCollection(portrayalCatalogueInfo,
                                                          PortrayalCatalogueReference.class);
    }

    /**
     * Set information about the catalogue of rules defined for the portrayal of a resource(s).
     */
    public synchronized void setPortrayalCatalogueInfo(final Collection newValues) {
        portrayalCatalogueInfo = copyCollection(newValues, portrayalCatalogueInfo,
                                                PortrayalCatalogueReference.class);
    }

    /**
     * Provides restrictions on the access and use of data.
     */
    public synchronized Collection getMetadataConstraints() {
        return metadataConstraints = nonNullCollection(metadataConstraints, Constraints.class);
    }

    /**
     * Set restrictions on the access and use of data.
     */
    public synchronized void setMetadataConstraints(final Collection newValues) {
        metadataConstraints = copyCollection(newValues, metadataConstraints, Constraints.class);
    }

    /**
     * Provides information about the conceptual schema of a dataset.
     */
    public synchronized Collection getApplicationSchemaInfo() {
        return applicationSchemaInfo = nonNullCollection(applicationSchemaInfo,
                                                         ApplicationSchemaInformation.class);
    }

    /**
     * Provides information about the conceptual schema of a dataset.
     */
    public synchronized void setApplicationSchemaInfo(final Collection newValues) {
        applicationSchemaInfo = copyCollection(newValues, applicationSchemaInfo,
                                               ApplicationSchemaInformation.class);
    }

    /**
     * Provides information about the frequency of metadata updates, and the scope of those updates.
     */
    public MaintenanceInformation getMetadataMaintenance() {
        return metadataMaintenance;
    }

    /**
     * Set information about the frequency of metadata updates, and the scope of those updates.
     */
    public synchronized void setMetadataMaintenance(final MaintenanceInformation newValue) {
        checkWritePermission();
        metadataMaintenance = newValue;
    }

    /**
     * Provides information about an alternatively used localized character
     * string for a linguistic extension.
     *
     * @since 2.4
     */
    public synchronized Collection getLocales() {
        return locales = nonNullCollection(locales, Locale.class);
    }

    /**
     * Set information about an alternatively used localized character
     * string for a linguistic extension.
     *
     * @since 2.4
     */
    public synchronized void setLocales(final Collection newValues) {
        locales = copyCollection(newValues, locales, Locale.class);
    }

    /**
     * Provides the URI of the dataset to which the metadata applies.
     *
     * @since 2.4
     */
    public String getDataSetUri() {
        return dataSetUri;
    }

    /**
     * Sets the URI of the dataset to which the metadata applies.
     *
     * @since 2.4
     */
    public void setDataSetUri(final String newValue) {
        checkWritePermission();
        dataSetUri = newValue;
    }
}
