package org.geotools.ml.bindings;


import org.geotools.xml.BindingConfiguration;
import org.picocontainer.MutablePicoContainer;

/**
 * Binding configuration for the http://mails/refractions/net schema.
 *
 * @generated
 */
public final class MLBindingConfiguration
	implements BindingConfiguration {


	/**
	 * @generated modifiable
	 */
	public void configure(MutablePicoContainer container) {
		container.registerComponentImplementation(ML.ATTACHMENTTYPE,MLAttachmentTypeBinding.class);
		container.registerComponentImplementation(ML.BODYTYPE,MLBodyTypeBinding.class);
		container.registerComponentImplementation(ML.ENVELOPETYPE,MLEnvelopeTypeBinding.class);
		container.registerComponentImplementation(ML.MAILSTYPE,MLMailsTypeBinding.class);
		container.registerComponentImplementation(ML.MAILTYPE,MLMailTypeBinding.class);
		container.registerComponentImplementation(ML.MIMETOPLEVELTYPE,MLMimeTopLevelTypeBinding.class);
	}

}