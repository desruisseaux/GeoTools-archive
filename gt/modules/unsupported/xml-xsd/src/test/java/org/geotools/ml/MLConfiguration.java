package org.geotools.ml;

import org.geotools.ml.bindings.ML;
import org.geotools.ml.bindings.MLBindingConfiguration;
import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;

public class MLConfiguration extends Configuration {

	public MLConfiguration() {
        super(ML.getInstance());
        
    }

    public BindingConfiguration getBindingConfiguration() {
		return new MLBindingConfiguration();
	}
}
