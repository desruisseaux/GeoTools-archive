package org.geotools.filter.v1_1;


import org.geotools.xml.*;

import org.opengis.filter.FilterFactory;		
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import org.opengis.webservice.SortProperty;

import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/ogc:SortPropertyType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:complexType name="SortPropertyType"&gt;
 *      &lt;xsd:sequence&gt;
 *          &lt;xsd:element ref="ogc:PropertyName"/&gt;
 *          &lt;xsd:element minOccurs="0" name="SortOrder" type="ogc:SortOrderType"/&gt;
 *      &lt;/xsd:sequence&gt;
 *  &lt;/xsd:complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class SortPropertyTypeBinding extends AbstractComplexBinding {

	FilterFactory filterfactory;		
	public SortPropertyTypeBinding( FilterFactory filterfactory ) {
		this.filterfactory = filterfactory;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.SORTPROPERTYTYPE;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return SortBy.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		PropertyName name = (PropertyName) node.getChildValue( PropertyName.class );
		SortOrder order = (SortOrder) node.getChildValue( SortOrder.class );
		if ( order == null ) {
			order = SortOrder.ASCENDING;
		}
		
		return filterfactory.sort( name.getPropertyName(), order );
	}
	

}