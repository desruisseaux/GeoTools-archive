package org.geotools.metadata.iso19115;

import java.util.Date;
import java.util.Set;
import java.util.Collection;
import java.nio.charset.Charset;

import org.geotools.util.CheckedHashSet;
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

/**
 * @deprecated Replaced by {@link org.geotools.metadata.MetaData}.
 */
public class MetaData extends AbstractMetaData implements
		org.opengis.metadata.MetaData {
    /** Set of ScopeCode */
	private Set hierarchyLevels;
	
	private String parentIdentifier;
	private Charset characterSet;
	
	/** Set of String */
	private Set hierarchyLevelNames;
	
	private ResponsibleParty contact;
	private Date dateStamp;
	
	/** Set of SpatialRepresentation */
	private Set spatialRepresentationInfo;
	
	/** Set of ReferenceSystem */
	private Set referenceSystemInfo;
	
	/** Set of Identification */
	private Set identificationInfo;
	/** Set of MetadataExtensionInformation */
	private Set metadataExtensionInfo;
	/** Set of ContentInformation */ 
	private Set contentInfo;
	private Distribution distributionInfo;
	/** Set of DataQuality */
	private Set dataQualityInfo;
	/** Set of PortrayalCatalogueReference */ 
	private Set portrayalCatalogueInfo;
	/** Set of Constraints */
	private Set metadataConstraints;
	private MaintenanceInformation metadataMaintenance;
	/** Set of ApplicationSchemaInformation */
	private Set applicationSchemaInfo;	

	public Charset getCharacterSet() {
		return characterSet;
	}	
	public String getParentIdentifier() {
		return parentIdentifier;
	}	
	public void setHierarchyLevels(Set hierarchyLevels) {
		if( this.hierarchyLevels == null ){
	        this.hierarchyLevels = new CheckedHashSet( ScopeCode.class );	        
	    }
	    else {	        
	        this.hierarchyLevels.clear();
	    }
	    this.hierarchyLevels.addAll( hierarchyLevels );		
	}	
	public synchronized Collection getHierarchyLevels() {
	    if( hierarchyLevels == null ){
	        hierarchyLevels = new CheckedHashSet( ScopeCode.class );
	    }
	    return hierarchyLevels;	    
	}
	public void setHierarchyLevelNames(Set hierarchyLevelNames) {
		if( this.hierarchyLevelNames == null ){
	        this.hierarchyLevelNames = new CheckedHashSet( String.class );	        
	    }
	    else {	        
	        this.hierarchyLevelNames.clear();
	    }
	    this.hierarchyLevelNames.addAll( hierarchyLevelNames );
	}
	public Collection getHierarchyLevelNames() {
		if( hierarchyLevelNames == null ){
		    hierarchyLevelNames = new CheckedHashSet( String.class );
	    }
		return hierarchyLevelNames;
	}
	public ResponsibleParty getContact() {
		return contact;
	}
	public Date getDateStamp() {
		return dateStamp;
	}
	public Collection getSpatialRepresentationInfo() {
		if( spatialRepresentationInfo == null ){
		    spatialRepresentationInfo = new CheckedHashSet( SpatialRepresentation.class );
	    }
	    return spatialRepresentationInfo;	    	
	}
	public Collection getReferenceSystemInfo() {
		if( referenceSystemInfo == null ){
		    referenceSystemInfo = new CheckedHashSet( ReferenceSystem.class );
	    }
	    return referenceSystemInfo;
	}
	public Collection getMetadataExtensionInfo() {
	    if( metadataExtensionInfo == null ){
	        metadataExtensionInfo = new CheckedHashSet( MetadataExtensionInformation.class );
	    }
	    return metadataExtensionInfo;
	}
	public Collection getIdentificationInfo() {
	    if( identificationInfo == null ){
	        identificationInfo = new CheckedHashSet( Identification.class );
	    }
	    return identificationInfo;
	}
	public Collection getContentInfo() {
		if( contentInfo == null ){
		    contentInfo = new CheckedHashSet( ContentInformation.class );
	    }
	    return contentInfo;
	}
	public Distribution getDistributionInfo() {
		return distributionInfo;
	}
	public Collection getDataQualityInfo() {
		if( dataQualityInfo == null ){
		    dataQualityInfo = new CheckedHashSet( DataQuality.class );
	    }
	    return dataQualityInfo;
	}
	public Collection getPortrayalCatalogueInfo() {
		if( portrayalCatalogueInfo == null ){
		    portrayalCatalogueInfo = new CheckedHashSet( PortrayalCatalogueReference.class );
	    }
	    return portrayalCatalogueInfo;
	}
	public Collection getMetadataConstraints() {
		if( metadataConstraints == null ){
		    metadataConstraints = new CheckedHashSet( Constraints.class );
	    }
	    return metadataConstraints;
	}
	public Collection getApplicationSchemaInfo() {
		if( applicationSchemaInfo == null ){
		    applicationSchemaInfo = new CheckedHashSet( ApplicationSchemaInformation.class );
	    }
	    return applicationSchemaInfo;
	}
	public MaintenanceInformation getMetadataMaintenance() {
		return metadataMaintenance;
	}

	public void setApplicationSchemaInfo( Set applicationSchemaInfo) {
	    if( this.applicationSchemaInfo == null ){
	        this.applicationSchemaInfo = new CheckedHashSet( ContentInformation.class );	        
	    }
	    else {	        
	        this.applicationSchemaInfo.clear();
	    }
	    this.applicationSchemaInfo.addAll( applicationSchemaInfo );		
	}
	public void setCharacterSet(Charset characterSet) {
		this.characterSet = characterSet;
	}
	public void setContact(ResponsibleParty contact) {
		this.contact = contact;
	}
	
	/** Lazy construction of a CheckedHashSet( ContentInformation.class ) for contentInfo */
	public void setContentInfo(Set contentInfo) {
	    if( this.contentInfo == null ){
	        this.contentInfo = new CheckedHashSet( ContentInformation.class );	        
	    }
	    else {	        
	        this.contentInfo.clear();
	    }
	    this.contentInfo.addAll( contentInfo );
	}
	/** Lazy construction of a CheckedHashSet( DataQuality ) for dataQualityInfo */
	public void setDataQualityInfo(Set dataQualityInfo) {		
		if( this.dataQualityInfo == null ){
	        this.dataQualityInfo = new CheckedHashSet( DataQuality.class );	        
	    }
	    else {	        
	        this.dataQualityInfo.clear();
	    }
	    this.dataQualityInfo.addAll( dataQualityInfo );		
	}
	public void setDateStamp(Date dateStamp) {
		this.dateStamp = dateStamp;
	}
	public void setDistributionInfo(Distribution distributionInfo) {
		this.distributionInfo = distributionInfo;
	}
	public void setIdentificationInfo(Set identificationInfo) {		
		if( this.identificationInfo == null ){
	        this.identificationInfo = new CheckedHashSet( Identification.class );	        
	    }
	    else {	        
	        this.identificationInfo.clear();
	    }
	    this.identificationInfo.addAll( identificationInfo );	
	}
	public void setMetadataConstraints(Set metadataConstraints) {
		if( this.metadataConstraints == null ){
	        this.metadataConstraints = new CheckedHashSet( Constraints.class );	        
	    }
	    else {	        
	        this.metadataConstraints.clear();
	    }
	    this.metadataConstraints.addAll( metadataConstraints );
	}
	public void setMetadataExtensionInfo( Set metadataExtensionInfo) {
		if( this.metadataExtensionInfo == null ){
	        this.metadataExtensionInfo = new CheckedHashSet( MetadataExtensionInformation.class );	        
	    }
	    else {	        
	        this.metadataExtensionInfo.clear();
	    }
	    this.metadataExtensionInfo.addAll( metadataExtensionInfo );		
	}
	public void setMetadataMaintenance(
			MaintenanceInformation metadataMaintenance) {
		this.metadataMaintenance = metadataMaintenance;
	}
	public void setParentIdentifier(String parentIdentifier) {
		this.parentIdentifier = parentIdentifier;
	}
	public void setPortrayalCatalogueInfo( Set portrayalCatalogueInfo) {
	    if( this.portrayalCatalogueInfo == null ){
	        this.portrayalCatalogueInfo = new CheckedHashSet( PortrayalCatalogueReference.class );	        
	    }
	    else {	        
	        this.portrayalCatalogueInfo.clear();
	    }
	    this.portrayalCatalogueInfo.addAll( portrayalCatalogueInfo );
	}
	public void setReferenceSystemInfo( Set referenceSystemInfo) {
	    if( this.referenceSystemInfo == null ){
	        this.referenceSystemInfo = new CheckedHashSet( ReferenceSystem.class );	        
	    }
	    else {	        
	        this.referenceSystemInfo.clear();
	    }
	    this.referenceSystemInfo.addAll( referenceSystemInfo );
	}
	public void setSpatialRepresentationInfo( Set spatialRepresentationInfo) {
	    if( this.spatialRepresentationInfo == null ){
	        this.spatialRepresentationInfo = new CheckedHashSet( SpatialRepresentation.class );	        
	    }
	    else {	        
	        this.spatialRepresentationInfo.clear();
	    }
	    this.spatialRepresentationInfo.addAll( spatialRepresentationInfo );
	}
}