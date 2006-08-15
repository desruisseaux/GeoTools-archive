package org.geotools.filter.v1_1;


import java.util.HashSet;

import org.geotools.xml.*;

import org.opengis.filter.FeatureId;
import org.opengis.filter.FilterFactory;		

import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/ogc:FeatureIdType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:complexType name="FeatureIdType"&gt;
 *      &lt;xsd:complexContent&gt;
 *          &lt;xsd:extension base="ogc:AbstractIdType"&gt;
 *              &lt;xsd:attribute name="fid" type="xsd:ID" use="required"/&gt;
 *          &lt;/xsd:extension&gt;
 *      &lt;/xsd:complexContent&gt;
 *  &lt;/xsd:complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class FeatureIdTypeBinding extends AbstractComplexBinding {

	FilterFactory filterfactory;		
	public FeatureIdTypeBinding( FilterFactory filterfactory ) {
		this.filterfactory = filterfactory;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.FEATUREIDTYPE;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return FeatureId.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		HashSet fids = new HashSet();
		
		if ( node.getAttribute( "fid") != null ) {
			fids.add( node.getAttributeValue( "fid") );
		}
		
		return filterfactory.featureId( fids );
	}

}