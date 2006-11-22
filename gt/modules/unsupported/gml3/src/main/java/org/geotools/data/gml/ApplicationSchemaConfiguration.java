package org.geotools.data.gml;

import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.SchemaLocationResolver;

public class ApplicationSchemaConfiguration extends Configuration {

	private String namespace;
	private String schemaLocation;
	
	public ApplicationSchemaConfiguration( String namespace, String schemaLocation ) {
		this.namespace = namespace;
		this.schemaLocation = schemaLocation;
		
		addDependency( new GMLConfiguration() );
	}
	
	public String getNamespaceURI() {
		return namespace;
	}

	public String getSchemaFileURL() {
		return schemaLocation;
	}

	public BindingConfiguration getBindingConfiguration() {
		return null;
	}

	public XSDSchemaLocationResolver getSchemaLocationResolver() {
		return new SchemaLocationResolver( namespace, schemaLocation );
	}

}
