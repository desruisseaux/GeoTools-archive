package org.geotools.metadata;

import java.util.Date;

import org.opengis.metadata.citation.ResponsibleParty;


/**
 * Metadata is the minimal Metadata required for data.
 * See ISO 19115 Metadata class
 * 
 * dateStamp, contact and identificationInfo are required
 * MetadataElements the rest are optional
 *  
 * @author jeichar
 *
 */
public interface Metadata extends MetadataEntity {
    Date getDateStamp();
    ResponsibleParty getContact();
    Identification getIdentificationInfo();
    
    MetadataElement getFileIdentifier();
    MetadataElement getLanguage();
    MetadataElement getCharacterSet();
    MetadataElement getParentIdentifier();
    MetadataElement getHierarchyLevel();
    MetadataElement getHierarchyLevelName();
    MetadataElement getMetadataStandardName();
    MetadataElement getMetadataStandardVersion();
    
    MetadataEntity getMetadataConstraints();
    MetadataEntity getApplicationSchemaInfo();
    MetadataEntity getPortrayalCatalogueInfo();
    MetadataEntity getContentInfo();
    MetadataEntity getDistributionInfo();
    MetadataEntity getDataQualityInfo();
    MetadataEntity getSpatialRepresentationInfo();
    MetadataEntity getReferenceSystemInfo();
    MetadataEntity getMetadataExtensionInfo();
    MetadataEntity getMetadataMaintenance();
}
