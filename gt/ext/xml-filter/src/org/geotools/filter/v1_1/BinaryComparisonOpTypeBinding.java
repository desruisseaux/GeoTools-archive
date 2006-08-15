package org.geotools.filter.v1_1;


import org.geotools.xml.*;

import org.opengis.filter.FilterFactory;		
import org.opengis.filter.expression.Expression;

import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/ogc:BinaryComparisonOpType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:complexType name="BinaryComparisonOpType"&gt;
 *      &lt;xsd:complexContent&gt;
 *          &lt;xsd:extension base="ogc:ComparisonOpsType"&gt;
 *              &lt;xsd:sequence&gt;
 *                  &lt;xsd:element maxOccurs="2" minOccurs="2" ref="ogc:expression"/&gt;
 *              &lt;/xsd:sequence&gt;
 *              &lt;xsd:attribute default="true" name="matchCase"
 *                  type="xsd:boolean" use="optional"/&gt;
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
public class BinaryComparisonOpTypeBinding extends AbstractComplexBinding {

	FilterFactory filterfactory;		
	public BinaryComparisonOpTypeBinding( FilterFactory filterfactory ) {
		this.filterfactory = filterfactory;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.BINARYCOMPARISONOPTYPE;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return Expression[].class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		Expression e1 = (Expression) node.getChildValue( 0 );
		Expression e2 = (Expression) node.getChildValue( 1 );
		
		return new Expression[]{ e1, e2 };
	}

}