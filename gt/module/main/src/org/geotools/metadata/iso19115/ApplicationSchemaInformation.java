package org.geotools.metadata.iso19115;

import java.net.URL;

import org.opengis.metadata.SpatialAttributeSupplement;
import org.opengis.metadata.citation.Citation;

/**
 * Sample ISO19115 MetaData implementation for ApplicationSchemaInformation.
 */
public class ApplicationSchemaInformation extends MetaData implements
		org.opengis.metadata.ApplicationSchemaInformation {
	
	private Citation name;
	private URL graphicsFile;
	private URL schemaAscii;
	private String constraintLanguage;
	private String schemaLanguage;
	private URL softwareDevelopmentFile;
	private SpatialAttributeSupplement featureCatalogueSupplement;
	private String softwareDevelopmentFileFormat;
	

	public Citation getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.opengis.metadata.ApplicationSchemaInformation#getSchemaLanguage()
	 */
	public String getSchemaLanguage() {
		return schemaLanguage;
	}

	/* (non-Javadoc)
	 * @see org.opengis.metadata.ApplicationSchemaInformation#getConstraintLanguage()
	 */
	public String getConstraintLanguage() {
		return constraintLanguage;
	}

	/* (non-Javadoc)
	 * @see org.opengis.metadata.ApplicationSchemaInformation#getSchemaAscii()
	 */
	public URL getSchemaAscii() {
		return schemaAscii;
	}

	/* (non-Javadoc)
	 * @see org.opengis.metadata.ApplicationSchemaInformation#getGraphicsFile()
	 */
	public URL getGraphicsFile() {
		return graphicsFile;
	}

	/* (non-Javadoc)
	 * @see org.opengis.metadata.ApplicationSchemaInformation#getSoftwareDevelopmentFile()
	 */
	public URL getSoftwareDevelopmentFile() {
		return softwareDevelopmentFile;
	}

	/* (non-Javadoc)
	 * @see org.opengis.metadata.ApplicationSchemaInformation#getSoftwareDevelopmentFileFormat()
	 */
	public String getSoftwareDevelopmentFileFormat() {
		return softwareDevelopmentFileFormat;
	}

	/* (non-Javadoc)
	 * @see org.opengis.metadata.ApplicationSchemaInformation#getFeatureCatalogueSupplement()
	 */
	public SpatialAttributeSupplement getFeatureCatalogueSupplement() {
		return featureCatalogueSupplement;
	}

}
