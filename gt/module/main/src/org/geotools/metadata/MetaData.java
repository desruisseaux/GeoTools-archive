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
import java.util.Set;
import java.util.Date;
import java.util.Locale;
import java.util.Collections;

// OpenGIS direct dependencies
import org.opengis.metadata.quality.DataQuality;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.metadata.constraint.Constraints;
import org.opengis.metadata.distribution.Distribution;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.content.ContentInformation;
import org.opengis.metadata.spatial.SpatialRepresentation;
import org.opengis.metadata.identification.Identification;
import org.opengis.metadata.maintenance.MaintenanceInformation;
import org.opengis.metadata.MetadataExtensionInformation;
import org.opengis.metadata.ApplicationSchemaInformation;
import org.opengis.metadata.PortrayalCatalogueReference;
import org.opengis.referencing.ReferenceSystem;

// Geotools dependencies
import org.geotools.metadata.MetadataEntity;
import org.geotools.util.CheckedHashSet;
import org.geotools.resources.Utilities;


/**
 * Root entity which defines metadata about a resource or resources.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class MetaData extends MetadataEntity implements org.opengis.metadata.MetaData {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 4280620497868178687L;
    
    /**
     * Unique identifier for this metadata file, or <code>null</code> if none.
     */
    private String fileIdentifier;

    /**
     * Language used for documenting metadata.
     */
    private Locale language;

    /**
     * Full name of the character coding standard used for the metadata set.
     */
    private String characterSet;

    /**
     * File identifier of the metadata to which this metadata is a subset (child).
     */
    private String parentIdentifier;

    /**
     * Scope to which the metadata applies.
     */
    private Set hierarchyLevels;

    /**
     * Name of the hierarchy levels for which the metadata is provided.
     */
    private Set hierarchyLevelNames;

    /**
     * Party responsible for the metadata information.
     */
    private ResponsibleParty contact;

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
    private Set spatialRepresentationInfo;

    /**
     * Description of the spatial and temporal reference systems used in the dataset.
     */
    private Set referenceSystemInfo;

    /**
     * Information describing metadata extensions.
     */
    private Set metadataExtensionInfo;

    /**
     * Basic information about the resource(s) to which the metadata applies.
     */
    private Set identificationInfo;

    /**
     * Provides information about the feature catalogue and describes the coverage and
     * image data characteristics.
     */
    private Set contentInfo;

    /**
     * Provides information about the distributor of and options for obtaining the resource(s).
     */
    private Distribution distributionInfo;

    /**
     * Provides overall assessment of quality of a resource(s).
     */
    private Set dataQualityInfo;

    /**
     * Provides information about the catalogue of rules defined for the portrayal of a resource(s).
     */
    private Set portrayalCatalogueInfo;

    /**
     * Provides restrictions on the access and use of data.
     */
    private Set metadataConstraints;

    /**
     * Provides information about the conceptual schema of a dataset.
     */
    private Set applicationSchemaInfo;
     
    /**
     * Provides information about the frequency of metadata updates, and the scope of those updates.
     */
    private MaintenanceInformation metadataMaintenance;
     
    /**
     * Creates an initially empty metadata.
     */
    public MetaData() {
    }
     
    /**
     * Creates a meta data initialised to the specified values.
     *
     * @param contact   Party responsible for the metadata information.
     * @param dateStamp Date that the metadata was created.
     * @param identificationInfo Basic information about the resource
     *        to which the metadata applies.
     */
    public MetaData(final ResponsibleParty contact,
                    final Date             dateStamp,
                    final Identification   identificationInfo)
    {
        setContact           (contact);
        setDateStamp         (dateStamp);
        setIdentificationInfo(Collections.singleton(identificationInfo));
    }
     
    /**
     * Returns the unique identifier for this metadata file, or <code>null</code> if none.
     */
    public String getFileIdentifier() {
        return fileIdentifier;
    }
     
    /**
     * Set the unique identifier for this metadata file, or <code>null</code> if none.
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
    public String getCharacterSet()  {
        return characterSet;
    }

    /**
     * Set the full name of the character coding standard used for the metadata set.
     */
    public synchronized void setCharacterSet(final String newValue) {
        checkWritePermission();
        fileIdentifier = newValue;
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
    public Set getHierarchyLevels() {
        final Set hierarchyLevels = this.hierarchyLevels; // Avoid synchronization
        return (hierarchyLevels!=null) ? hierarchyLevels : Collections.EMPTY_SET;
    }

    /**
     * Set the scope to which the metadata applies.
     */
    public synchronized void setHierarchyLevels(final Set newValues) {
        checkWritePermission();
        if (hierarchyLevels == null) {
            hierarchyLevels = new CheckedHashSet(ScopeCode.class);
        } else {
            hierarchyLevels.clear();
        }
        hierarchyLevels.addAll(newValues);
    }
    
    /**
     * Returns the name of the hierarchy levels for which the metadata is provided.
     */
    public Set getHierarchyLevelNames() {
        final Set hierarchyLevelNames = this.hierarchyLevelNames; // Avoid synchronization
        return (hierarchyLevelNames!=null) ? hierarchyLevelNames : Collections.EMPTY_SET;
    }

    /**
     * Set the name of the hierarchy levels for which the metadata is provided.
     */
    public synchronized void setHierarchyLevelNames(final Set newValues) {
        checkWritePermission();
        if (hierarchyLevelNames == null) {
            hierarchyLevelNames = new CheckedHashSet(String.class);
        } else {
            hierarchyLevelNames.clear();
        }
        hierarchyLevelNames.addAll(newValues);
    }

    /**
     * Returns the party responsible for the metadata information.
     */
    public ResponsibleParty getContact() {
        return contact;
    }

    /**
     * Set the party responsible for the metadata information.
     */
    public synchronized void setContact(final ResponsibleParty newValue) {
        checkWritePermission();
        contact = newValue;
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
    public Set getSpatialRepresentationInfo() {
        final Set spatialRepresentationInfo = this.spatialRepresentationInfo; // Avoid synchronization
        return (spatialRepresentationInfo!=null) ? spatialRepresentationInfo : Collections.EMPTY_SET;
    }

    /**
     * Set the digital representation of spatial information in the dataset.
     */
    public synchronized void setSpatialRepresentationInfo(final Set newValues) {
        checkWritePermission();
        if (spatialRepresentationInfo == null) {
            spatialRepresentationInfo = new CheckedHashSet(SpatialRepresentation.class);
        } else {
            spatialRepresentationInfo.clear();
        }
        spatialRepresentationInfo.addAll(newValues);
    }

    /**
     * Returns the description of the spatial and temporal reference systems used in the dataset.
     */
     public Set getReferenceSystemInfo() {
        final Set referenceSystemInfo = this.referenceSystemInfo; // Avoid synchronization
        return (referenceSystemInfo!=null) ? referenceSystemInfo : Collections.EMPTY_SET;
    }
    
    /**
     * Set the description of the spatial and temporal reference systems used in the dataset.
     */
    public synchronized void setReferenceSystemInfo(final Set newValues) {
        checkWritePermission();
        if (referenceSystemInfo == null) {
            referenceSystemInfo = new CheckedHashSet(ReferenceSystem.class);
        } else {
            referenceSystemInfo.clear();
        }
        referenceSystemInfo.addAll(newValues);
    }

    /**
     * Returns information describing metadata extensions.
     */
    public Set getMetadataExtensionInfo() {
        final Set metadataExtensionInfo = this.metadataExtensionInfo; // Avoid synchronization
        return (metadataExtensionInfo!=null) ? metadataExtensionInfo : Collections.EMPTY_SET;
    }

    /**
     * Set information describing metadata extensions.
     */
    public synchronized void setMetadataExtensionInfo(final Set newValues) {
        checkWritePermission();
        if (metadataExtensionInfo == null) {
            metadataExtensionInfo = new CheckedHashSet(MetadataExtensionInformation.class);
        } else {
            metadataExtensionInfo.clear();
        }
        metadataExtensionInfo.addAll(newValues);
    }

    /**
     * Returns basic information about the resource(s) to which the metadata applies.
     */
    public Set getIdentificationInfo() {
        final Set identificationInfo = this.identificationInfo; // Avoid synchronization
        return (identificationInfo!=null) ? identificationInfo : Collections.EMPTY_SET;
    }
     
    /**
     * Set basic information about the resource(s) to which the metadata applies.
     */
    public synchronized void setIdentificationInfo(final Set newValues) {
        checkWritePermission();
        if (identificationInfo == null) {
            identificationInfo = new CheckedHashSet(Identification.class);
        } else {
            identificationInfo.clear();
        }
        identificationInfo.addAll(newValues);
    }

    /**
     * Provides information about the feature catalogue and describes the coverage and
     * image data characteristics.
     */
    public Set getContentInfo() {
        final Set contentInfo = this.contentInfo; // Avoid synchronization
        return (contentInfo!=null) ? contentInfo : Collections.EMPTY_SET;
    }
     
    /**
     * Set information about the feature catalogue and describes the coverage and
     * image data characteristics.
     */
    public synchronized void setContentInfo(final Set newValues) {
        checkWritePermission();
        if (contentInfo == null) {
            contentInfo = new CheckedHashSet(Identification.class);
        } else {
            contentInfo.clear();
        }
        contentInfo.addAll(newValues);
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
    public Set getDataQualityInfo() {
        final Set dataQualityInfo = this.dataQualityInfo; // Avoid synchronization
        return (dataQualityInfo!=null) ? dataQualityInfo : Collections.EMPTY_SET;
    }
     
    /**
     * Set overall assessment of quality of a resource(s).
     */
    public synchronized void setDataQualityInfo(final Set newValues) {
        checkWritePermission();
        if (dataQualityInfo == null) {
            dataQualityInfo = new CheckedHashSet(DataQuality.class);
        } else {
            dataQualityInfo.clear();
        }
        dataQualityInfo.addAll(newValues);
    }

     /**
      * Provides information about the catalogue of rules defined for the portrayal of a
      * resource(s).
      */
     public Set getPortrayalCatalogueInfo() {
        final Set portrayalCatalogueInfo = this.portrayalCatalogueInfo; // Avoid synchronization
        return (portrayalCatalogueInfo!=null) ? portrayalCatalogueInfo : Collections.EMPTY_SET;
    }
     
    /**
     * Set information about the catalogue of rules defined for the portrayal of a resource(s).
     */
    public synchronized void setPortrayalCatalogueInfo(final Set newValues) {
        checkWritePermission();
        if (portrayalCatalogueInfo == null) {
            portrayalCatalogueInfo = new CheckedHashSet(PortrayalCatalogueReference.class);
        } else {
            portrayalCatalogueInfo.clear();
        }
        portrayalCatalogueInfo.addAll(newValues);
    }

    /**
     * Provides restrictions on the access and use of data.
     */
    public Set getMetadataConstraints() {
        final Set metadataConstraints = this.metadataConstraints; // Avoid synchronization
        return (metadataConstraints!=null) ? metadataConstraints : Collections.EMPTY_SET;
    }
     
    /**
     * Set restrictions on the access and use of data.
     */
    public synchronized void setMetadataConstraints(final Set newValues) {
        checkWritePermission();
        if (metadataConstraints == null) {
            metadataConstraints = new CheckedHashSet(Constraints.class);
        } else {
            metadataConstraints.clear();
        }
        metadataConstraints.addAll(newValues);
    }

    /**
     * Provides information about the conceptual schema of a dataset.
     */
    public Set getApplicationSchemaInfo() {
        final Set applicationSchemaInfo = this.applicationSchemaInfo; // Avoid synchronization
        return (applicationSchemaInfo!=null) ? applicationSchemaInfo : Collections.EMPTY_SET;
    }

    /**
     * Provides information about the conceptual schema of a dataset.
     */
    public synchronized void setApplicationSchemaInfo(final Set newValues) {
        checkWritePermission();
        if (applicationSchemaInfo == null) {
            applicationSchemaInfo = new CheckedHashSet(ApplicationSchemaInformation.class);
        } else {
            applicationSchemaInfo.clear();
        }
        applicationSchemaInfo.addAll(newValues);
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
    public synchronized void setMetadataMaintenance(MaintenanceInformation maintenanceInformation) {
        checkWritePermission();
        this.fileIdentifier = fileIdentifier;
    }
     
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        fileIdentifier              = (String)                 unmodifiable(fileIdentifier);
        language                    = (Locale)                 unmodifiable(language);
        characterSet                = (String)                 unmodifiable(characterSet);
        parentIdentifier            = (String)                 unmodifiable(parentIdentifier);
        hierarchyLevels             = (Set)                    unmodifiable(hierarchyLevels);
        hierarchyLevelNames         = (Set)                    unmodifiable(hierarchyLevelNames);
        contact                     = (ResponsibleParty)       unmodifiable(contact);
        metadataStandardName        = (String)                 unmodifiable(metadataStandardName);
        metadataStandardVersion     = (String)                 unmodifiable(metadataStandardVersion);
        spatialRepresentationInfo   = (Set)                    unmodifiable(spatialRepresentationInfo);
        referenceSystemInfo         = (Set)                    unmodifiable(referenceSystemInfo);
        metadataExtensionInfo       = (Set)                    unmodifiable(metadataExtensionInfo);
        identificationInfo          = (Set)                    unmodifiable(identificationInfo);
        contentInfo                 = (Set)                    unmodifiable(contentInfo);
        distributionInfo            = (Distribution)           unmodifiable(distributionInfo);
        dataQualityInfo             = (Set)                    unmodifiable(dataQualityInfo);
        portrayalCatalogueInfo      = (Set)                    unmodifiable(portrayalCatalogueInfo);
        metadataConstraints         = (Set)                    unmodifiable(metadataConstraints);
        applicationSchemaInfo       = (Set)                    unmodifiable(applicationSchemaInfo);
        metadataMaintenance         = (MaintenanceInformation) unmodifiable(metadataMaintenance);     
    }

    /**
     * Compare this MetaData with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final MetaData that = (MetaData) object;
            return               this.dateStamp             ==   that.dateStamp                  &&
                Utilities.equals(this.fileIdentifier,            that.fileIdentifier           ) &&
                Utilities.equals(this.language,                  that.language                 ) &&
                Utilities.equals(this.characterSet,              that.characterSet             ) &&
                Utilities.equals(this.parentIdentifier,          that.parentIdentifier         ) &&
                Utilities.equals(this.hierarchyLevels,           that.hierarchyLevels          ) &&
                Utilities.equals(this.hierarchyLevelNames,       that.hierarchyLevelNames      ) &&
                Utilities.equals(this.contact,                   that.contact                  ) &&
                Utilities.equals(this.metadataStandardName,      that.metadataStandardName     ) &&
                Utilities.equals(this.metadataStandardVersion,   that.metadataStandardVersion  ) &&
                Utilities.equals(this.spatialRepresentationInfo, that.spatialRepresentationInfo) &&
                Utilities.equals(this.referenceSystemInfo,       that.referenceSystemInfo      ) &&
                Utilities.equals(this.metadataExtensionInfo,     that.metadataExtensionInfo    ) &&
                Utilities.equals(this.identificationInfo,        that.identificationInfo       ) &&
                Utilities.equals(this.contentInfo,               that.contentInfo              ) &&
                Utilities.equals(this.distributionInfo,          that.distributionInfo         ) &&
                Utilities.equals(this.dataQualityInfo,           that.dataQualityInfo          ) &&
                Utilities.equals(this.portrayalCatalogueInfo,    that.portrayalCatalogueInfo   ) &&
                Utilities.equals(this.metadataConstraints,       that.metadataConstraints      ) &&
                Utilities.equals(this.applicationSchemaInfo,     that.applicationSchemaInfo    ) &&
                Utilities.equals(this.metadataMaintenance,       that.metadataMaintenance      );
        }
        return false;
    }

    /**
     * Returns a hash code value for this address. For performance reason, this method do
     * not uses all attributes for computing the hash code. Instead, it uses the attributes
     * that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (contact            != null) code ^= contact           .hashCode();
        if (identificationInfo != null) code ^= identificationInfo.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this metadata. The content of this string is
     * implementation dependent and used mostly for debugging purpose.
     */
    public String toString() {
        return String.valueOf(contact);
    }        
}
