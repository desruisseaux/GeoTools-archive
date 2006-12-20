package org.geotools.ml;

import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.geotools.ml.bindings.ML;
import org.geotools.ml.bindings.MLBindingConfiguration;
import org.geotools.ml.bindings.MLSchemaLocationResolver;
import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;

public class MLConfiguration extends Configuration {

	public String getNamespaceURI() {
		return ML.NAMESPACE;
	}

	public String getSchemaFileURL() {
		return getSchemaLocationResolver().resolveSchemaLocation( 
			null, ML.NAMESPACE, "mails.xsd"	
		);
	}

	public BindingConfiguration getBindingConfiguration() {
		return new MLBindingConfiguration();
	}

	public XSDSchemaLocationResolver getSchemaLocationResolver() {
		return new MLSchemaLocationResolver();
	}

}
