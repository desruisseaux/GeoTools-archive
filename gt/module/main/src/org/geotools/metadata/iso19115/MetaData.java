package org.geotools.metadata.iso19115;

import java.util.Date;

import org.opengis.metadata.ApplicationSchemaInformation;
import org.opengis.metadata.MetadataExtensionInformation;
import org.opengis.metadata.PortrayalCatalogueReference;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.constraint.Constraints;
import org.opengis.metadata.content.ContentInformation;
import org.opengis.metadata.distribution.Distribution;
import org.opengis.metadata.identification.Identification;
import org.opengis.metadata.maintenance.MaintenanceInformation;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.metadata.quality.DataQuality;
import org.opengis.metadata.spatial.SpatialRepresentation;
import org.opengis.referencing.ReferenceSystem;

public class MetaData extends AbstractMetaData implements
		org.opengis.metadata.MetaData {

	private ScopeCode[] hierarchyLevels;
	private String parentIdentifier;
	private String characterSet;
	private String[] hierarchyLevelNames;
	private ResponsibleParty contact;
	private Date dateStamp;
	private SpatialRepresentation[] spatialRepresentationInfo;
	private ReferenceSystem[] referenceSystemInfo;
	private Identification[] identificationInfo;
	private MetadataExtensionInformation[] metadataExtensionInfo;
	private ContentInformation[] contentInfo;
	private Distribution distributionInfo;
	private DataQuality[] dataQualityInfo;
	private PortrayalCatalogueReference[] portrayalCatalogueInfo;
	private Constraints[] metadataConstraints;
	private MaintenanceInformation metadataMaintenance;
	private ApplicationSchemaInformation[] applicationSchemaInfo;	

	public String getCharacterSet() {
		return characterSet;
	}	
	public String getParentIdentifier() {
		return parentIdentifier;
	}
	public ScopeCode[] getHierarchyLevels() {
		return hierarchyLevels;
	}
	public String[] getHierarchyLevelNames() {
		return hierarchyLevelNames;
	}
	public ResponsibleParty getContact() {
		return contact;
	}
	public Date getDateStamp() {
		return dateStamp;
	}
	public SpatialRepresentation[] getSpatialRepresentationInfo() {
		return spatialRepresentationInfo;
	}
	public ReferenceSystem[] getReferenceSystemInfo() {
		return referenceSystemInfo;
	}
	public MetadataExtensionInformation[] getMetadataExtensionInfo() {
		return metadataExtensionInfo;
	}
	public Identification[] getIdentificationInfo() {
		return identificationInfo;
	}
	public ContentInformation[] getContentInfo() {
		return contentInfo;
	}
	public Distribution getDistributionInfo() {
		return distributionInfo;
	}
	public DataQuality[] getDataQualityInfo() {
		return dataQualityInfo;
	}
	public PortrayalCatalogueReference[] getPortrayalCatalogueInfo() {
		return portrayalCatalogueInfo;
	}
	public Constraints[] getMetadataConstraints() {
		return metadataConstraints;
	}
	public ApplicationSchemaInformation[] getApplicationSchemaInfo() {
		return applicationSchemaInfo;
	}
	public MaintenanceInformation getMetadataMaintenance() {
		return metadataMaintenance;
	}

	public void setApplicationSchemaInfo(
			ApplicationSchemaInformation[] applicationSchemaInfo) {
		this.applicationSchemaInfo = applicationSchemaInfo;
	}
	public void setCharacterSet(String characterSet) {
		this.characterSet = characterSet;
	}
	public void setContact(ResponsibleParty contact) {
		this.contact = contact;
	}
	public void setContentInfo(ContentInformation[] contentInfo) {
		this.contentInfo = contentInfo;
	}
	public void setDataQualityInfo(DataQuality[] dataQualityInfo) {
		this.dataQualityInfo = dataQualityInfo;
	}
	public void setDateStamp(Date dateStamp) {
		this.dateStamp = dateStamp;
	}
	public void setDistributionInfo(Distribution distributionInfo) {
		this.distributionInfo = distributionInfo;
	}
	public void setHierarchyLevelNames(String[] hierarchyLevelNames) {
		this.hierarchyLevelNames = hierarchyLevelNames;
	}
	public void setHierarchyLevels(ScopeCode[] hierarchyLevels) {
		this.hierarchyLevels = hierarchyLevels;
	}
	public void setIdentificationInfo(Identification[] identificationInfo) {
		this.identificationInfo = identificationInfo;
	}
	public void setMetadataConstraints(Constraints[] metadataConstraints) {
		this.metadataConstraints = metadataConstraints;
	}
	public void setMetadataExtensionInfo(
			MetadataExtensionInformation[] metadataExtensionInfo) {
		this.metadataExtensionInfo = metadataExtensionInfo;
	}
	public void setMetadataMaintenance(
			MaintenanceInformation metadataMaintenance) {
		this.metadataMaintenance = metadataMaintenance;
	}
	public void setParentIdentifier(String parentIdentifier) {
		this.parentIdentifier = parentIdentifier;
	}
	public void setPortrayalCatalogueInfo(
			PortrayalCatalogueReference[] portrayalCatalogueInfo) {
		this.portrayalCatalogueInfo = portrayalCatalogueInfo;
	}
	public void setReferenceSystemInfo(ReferenceSystem[] referenceSystemInfo) {
		this.referenceSystemInfo = referenceSystemInfo;
	}
	public void setSpatialRepresentationInfo(
			SpatialRepresentation[] spatialRepresentationInfo) {
		this.spatialRepresentationInfo = spatialRepresentationInfo;
	}
}
