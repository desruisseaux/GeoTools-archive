package org.geotools.metadata;

import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.citation.ResponsibleParty;

/**
 * Identification is one of the required MetadataElements in
 * the Metadata interface
 * 
 * See ISO 19115 specification
 * 
 * @author jeichar
 *
 */
public interface Identification extends MetadataEntity {
    Citation getCitation();
    String getAbstract();
    String getPurpose();
    String[] getCredit();
    MetadataElement[] getStatus();
    ResponsibleParty[] getResponsible();
    
    MetadataEntity[] getDescriptiveKeywords();
    MetadataEntity[] getResourceMaintenance();
    MetadataEntity[] getResourceFormat();
    MetadataEntity[] getGraphicOverview();
    MetadataEntity[] getResourceSpecificUsage();
    MetadataEntity[] getrourceConstraints();
}
