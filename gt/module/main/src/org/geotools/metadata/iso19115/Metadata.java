package org.geotools.metadata.iso19115;

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
public interface Metadata extends org.geotools.metadata.Metadata {
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
    
    Metadata getMetadataConstraints();
    Metadata getApplicationSchemaInfo();
    Metadata getPortrayalCatalogueInfo();
    Metadata getContentInfo();
    Metadata getDistributionInfo();
    Metadata getDataQualityInfo();
    Metadata getSpatialRepresentationInfo();
    Metadata getReferenceSystemInfo();
    Metadata getMetadataExtensionInfo();
    Metadata getMetadataMaintenance();
}
