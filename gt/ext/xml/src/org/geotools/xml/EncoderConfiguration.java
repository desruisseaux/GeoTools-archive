package org.geotools.xml;

import org.picocontainer.MutablePicoContainer;

/**
 * Used to configure an instance of {@link org.geotools.xml.Writer}.
 * 
 * <p>
 * Implementations supply a series of {@link org.geotools.xml.Encoder} 
 * implementations (typically one for each type of object in your model that 
 * must be encoded.
 * </p>
 * 
 * <p>
 * Encoder implementations are supplied by registering them with the supplied 
 * container.
 * 
 * <pre>
 * 	<code>
 *	class MyEncoderConfiguration implements EncoderConfiguration {
 *		void configure(MutablePicoContainer container) {
 * 			container.registerComponentImplementation(FooEncoder.class);
 *  		container.registerComponentImplementation(BarEncoder.class);
 *  		...
 *		}
 * 	}
 * 	</code>
 * </pre>
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public interface EncoderConfiguration {

	/**
	 * Populates the container with implementations of {@link Encoder}.
	 *
	 * @param container The container used to store encoder implementations.
	 */
	void confgiure(MutablePicoContainer container);
}
