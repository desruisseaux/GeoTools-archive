package org.geotools.metadata.iso19115;

import org.geotools.metadata.Metadata;
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
public interface Identification extends Metadata {
    Citation getCitation();
    String getAbstract();
    String getPurpose();
    String[] getCredit();
    MetadataElement[] getStatus();
    ResponsibleParty[] getResponsible();
    
    Metadata[] getDescriptiveKeywords();
    Metadata[] getResourceMaintenance();
    Metadata[] getResourceFormat();
    Metadata[] getGraphicOverview();
    Metadata[] getResourceSpecificUsage();
    Metadata[] getrourceConstraints();
}
