package org.geotools.styling;

import org.geotools.event.GTComponent;

/**
 * LayerFeatureConstraints define what features and feature types are referenced 
 * in a layer.
 * 
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:element name="LayerFeatureConstraints"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;         LayerFeatureConstraints define what
 *              features &amp; feature types are         referenced in a
 *              layer.       &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:complexType&gt;
 *          &lt;xsd:sequence&gt;
 *              &lt;xsd:element ref="sld:FeatureTypeConstraint" maxOccurs="unbounded"/&gt;
 *          &lt;/xsd:sequence&gt;
 *      &lt;/xsd:complexType&gt;
 *  &lt;/xsd:element&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 * @source $URL$
 */
public interface LayerFeatureConstraints extends GTComponent {

	/**
	 * @return The feature type constraints.
	 */
	FeatureTypeConstraint[] getFeatureTypeConstraints();
	
	/**
	 * @param constraints The new feature type constraints.
	 */
	void setFeatureTypeConstraints(FeatureTypeConstraint[] constraints);
}
