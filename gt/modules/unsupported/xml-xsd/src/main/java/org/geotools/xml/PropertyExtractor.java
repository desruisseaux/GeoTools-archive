package org.geotools.xml;

import java.util.List;
import java.util.Map;

import org.eclipse.xsd.XSDElementDeclaration;

/**
 * Factory used by the encoder to obtain child values from objects being encoded.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public interface PropertyExtractor {

	/**
	 * Determines if this extractor can handle objects of the given type.
	 * 
	 * @param object The object being encoded.
	 *  
	 * @return <code>true</code> if the extractor can handle the object, 
	 * 	otherwise <code>false<code>.
	 */
	boolean canHandle( Object object );
	
	/**
	 * Exracts the properties from the object being encoded.
	 * <p>
	 * This method should return a set of tuples made up of 
	 * ({@link org.eclipse.xsd.XSDParticle},Object).
	 * </p>
	 * 
	 * @param object The object being encoded.
	 * @param element The element declaration corresponding to the object being encoded. 
	 * 
	 * @return A set of element, object tuples.
	 */
	List/*Object[2]*/ properties( Object object, XSDElementDeclaration element );
	
}
