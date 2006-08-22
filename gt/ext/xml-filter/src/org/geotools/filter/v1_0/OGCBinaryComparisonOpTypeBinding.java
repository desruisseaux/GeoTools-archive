package org.geotools.filter.v1_0;

import org.geotools.xml.*;

import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
public class OGCBinaryComparisonOpTypeBinding implements ComplexBinding {
	private FilterFactory factory;
	public OGCBinaryComparisonOpTypeBinding( FilterFactory factory ){
		this.factory = factory;
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
	public int getExecutionMode() {
		return OVERRIDE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return BinaryComparisonOperator.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public void initialize(ElementInstance instance, Node node, MutablePicoContainer context) {
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		//TODO: replace with element bindings
		Expression e1 = (Expression) node.getChildValue( 0 );
		Expression e2 = (Expression) node.getChildValue( 1 );
		
		String name = instance.getName();
		short op;
//		<xsd:element name="PropertyIsEqualTo" substitutionGroup="ogc:comparisonOps" type="ogc:BinaryComparisonOpType"/>
		if( "PropertyIsEqualTo".equals( name )){
			return factory.equals( e1, e2 );
		}
//		<xsd:element name="PropertyIsNotEqualTo" substitutionGroup="ogc:comparisonOps" type="ogc:BinaryComparisonOpType"/>
		else if( "PropertyIsNotEqualTo".equals( name )){
			//TODO: add geoapi interface
			return factory.not( factory.equals( e1, e2 ) );
		}
//		<xsd:element name="PropertyIsLessThan" substitutionGroup="ogc:comparisonOps" type="ogc:BinaryComparisonOpType"/>
		else if( "PropertyIsLessThan".equals( name )){
			return factory.less( e1, e2 );
		}
//		<xsd:element name="PropertyIsGreaterThan" substitutionGroup="ogc:comparisonOps" type="ogc:BinaryComparisonOpType"/>
		else if( "PropertyIsGreaterThan".equals( name )){
			return factory.greater( e1, e2 );
		}
//		<xsd:element name="PropertyIsLessThanOrEqualTo" substitutionGroup="ogc:comparisonOps" type="ogc:BinaryComparisonOpType"/>
		else if( "PropertyIsLessThanOrEqualTo".equals( name )){
			return factory.lessOrEqual( e1, e2 );
		}
//		<xsd:element name="PropertyIsGreaterThanOrEqualTo" substitutionGroup="ogc:comparisonOps" type="ogc:BinaryComparisonOpType"/>		
		else if( "PropertyIsGreaterThanOrEqualTo".equals( name )){
			return factory.greaterOrEqual( e1, e2 );
		}
		else {
			throw new IllegalStateException( name );
		}		
		
	}
}