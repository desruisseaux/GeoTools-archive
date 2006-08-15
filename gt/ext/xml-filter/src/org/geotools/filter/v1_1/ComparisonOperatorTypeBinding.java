package org.geotools.filter.v1_1;


import org.geotools.xml.*;

import org.opengis.filter.FilterFactory;		

import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/ogc:ComparisonOperatorType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:simpleType name="ComparisonOperatorType"&gt;
 *      &lt;xsd:restriction base="xsd:string"&gt;
 *          &lt;xsd:enumeration value="LessThan"/&gt;
 *          &lt;xsd:enumeration value="GreaterThan"/&gt;
 *          &lt;xsd:enumeration value="LessThanEqualTo"/&gt;
 *          &lt;xsd:enumeration value="GreaterThanEqualTo"/&gt;
 *          &lt;xsd:enumeration value="EqualTo"/&gt;
 *          &lt;xsd:enumeration value="NotEqualTo"/&gt;
 *          &lt;xsd:enumeration value="Like"/&gt;
 *          &lt;xsd:enumeration value="Between"/&gt;
 *          &lt;xsd:enumeration value="NullCheck"/&gt;
 *      &lt;/xsd:restriction&gt;
 *  &lt;/xsd:simpleType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class ComparisonOperatorTypeBinding extends AbstractSimpleBinding {

	FilterFactory filterfactory;		
	public ComparisonOperatorTypeBinding( FilterFactory filterfactory ) {
		this.filterfactory = filterfactory;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.COMPARISONOPERATORTYPE;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return null;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(InstanceComponent instance, Object value) 
		throws Exception {
		
		//TODO: implement
		return null;
	}

}