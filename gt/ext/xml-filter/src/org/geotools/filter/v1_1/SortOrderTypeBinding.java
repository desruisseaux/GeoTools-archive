package org.geotools.filter.v1_1;


import org.geotools.xml.*;

import org.opengis.filter.FilterFactory;		
import org.opengis.filter.sort.SortOrder;

import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/ogc:SortOrderType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:simpleType name="SortOrderType"&gt;
 *      &lt;xsd:restriction base="xsd:string"&gt;
 *          &lt;xsd:enumeration value="DESC"/&gt;
 *          &lt;xsd:enumeration value="ASC"/&gt;
 *      &lt;/xsd:restriction&gt;
 *  &lt;/xsd:simpleType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class SortOrderTypeBinding extends AbstractSimpleBinding {

	FilterFactory filterfactory;		
	public SortOrderTypeBinding( FilterFactory filterfactory ) {
		this.filterfactory = filterfactory;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.SORTORDERTYPE;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return SortOrder.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(InstanceComponent instance, Object value) 
		throws Exception {
		
		if ( "ASC".equals( value ) ) {
			return SortOrder.ASCENDING;
		}
		
		if  ( "DESC".equals( value ) ) {
			return SortOrder.DESCENDING;
		}
		
		return null;
	}

}