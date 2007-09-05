package org.geotools.ml;

import org.geotools.ml.bindings.ML;
import org.geotools.ml.bindings.MLAttachmentTypeBinding;
import org.geotools.ml.bindings.MLBodyTypeBinding;
import org.geotools.ml.bindings.MLEnvelopeTypeBinding;
import org.geotools.ml.bindings.MLMailTypeBinding;
import org.geotools.ml.bindings.MLMailsTypeBinding;
import org.geotools.ml.bindings.MLMimeTopLevelTypeBinding;
import org.geotools.xml.Configuration;
import org.picocontainer.MutablePicoContainer;

public class MLConfiguration extends Configuration {

	public MLConfiguration() {
        super(ML.getInstance());
        
    }

	protected final void registerBindings(MutablePicoContainer container) {
	    container.registerComponentImplementation(ML.ATTACHMENTTYPE,MLAttachmentTypeBinding.class);
        container.registerComponentImplementation(ML.BODYTYPE,MLBodyTypeBinding.class);
        container.registerComponentImplementation(ML.ENVELOPETYPE,MLEnvelopeTypeBinding.class);
        container.registerComponentImplementation(ML.MAILSTYPE,MLMailsTypeBinding.class);
        container.registerComponentImplementation(ML.MAILTYPE,MLMailTypeBinding.class);
        container.registerComponentImplementation(ML.MIMETOPLEVELTYPE,MLMimeTopLevelTypeBinding.class);
	}
	
}
