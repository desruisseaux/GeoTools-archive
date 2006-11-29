package org.geotools.referencing.crs;

import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.metadata.iso.citation.ResponsiblePartyImpl;
import org.geotools.referencing.factory.AuthorityFactoryAdapter;
import org.geotools.util.SimpleInternationalString;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.citation.PresentationForm;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


public class URNEPSGCRSAuthorityFactoryAdapter extends AuthorityFactoryAdapter
	implements CRSAuthorityFactory {

	private static final Citation EPSG;
	static {
		final CitationImpl c = new CitationImpl(ResponsiblePartyImpl.EPSG);
		c.setTitle( new SimpleInternationalString( "URN" ) );
		c.getPresentationForm().add(PresentationForm.TABLE_DIGITAL);
		EPSG = c;
	}
	
	public URNEPSGCRSAuthorityFactoryAdapter() {		
		super( new EPSGCRSAuthorityFactory(),null,null,null);
	}
	
	public Citation getAuthority() {
		return EPSG;
	}
	
	public CoordinateReferenceSystem createCoordinateReferenceSystem(String code) throws FactoryException {
		if ( code.startsWith( "URN:X-OGC:DEF:CRS:EPSG:6.11.2:" ) ) {
			code = "EPSG:" + code.substring( "URN:X-OGC:DEF:CRS:EPSG:6.11.2:".length() );
		}
		
		return crsFactory.createCoordinateReferenceSystem( code );
	}
	
	
}
