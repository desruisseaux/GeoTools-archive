package org.geotools.xs.bindings;

import java.util.Enumeration;
import java.util.Iterator;

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.geotools.xml.InstanceComponent;
import org.geotools.xml.SimpleBinding;
import org.xml.sax.helpers.NamespaceSupport;

import com.sun.xml.bind.DatatypeConverterImpl;


/**
 * Binding object for the type http://www.w3.org/2001/XMLSchema:QName.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xs:simpleType name="QName" id="QName"&gt;
 *      &lt;xs:annotation&gt;
 *          &lt;xs:appinfo&gt;
 *              &lt;hfp:hasFacet name="length"/&gt;
 *              &lt;hfp:hasFacet name="minLength"/&gt;
 *              &lt;hfp:hasFacet name="maxLength"/&gt;
 *              &lt;hfp:hasFacet name="pattern"/&gt;
 *              &lt;hfp:hasFacet name="enumeration"/&gt;
 *              &lt;hfp:hasFacet name="whiteSpace"/&gt;
 *              &lt;hfp:hasProperty name="ordered" value="false"/&gt;
 *              &lt;hfp:hasProperty name="bounded" value="false"/&gt;
 *              &lt;hfp:hasProperty name="cardinality" value="countably infinite"/&gt;
 *              &lt;hfp:hasProperty name="numeric" value="false"/&gt;
 *          &lt;/xs:appinfo&gt;
 *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#QName"/&gt;
 *      &lt;/xs:annotation&gt;
 *      &lt;xs:restriction base="xs:anySimpleType"&gt;
 *          &lt;xs:whiteSpace value="collapse" fixed="true" id="QName.whiteSpace"/&gt;
 *      &lt;/xs:restriction&gt;
 *  &lt;/xs:simpleType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class XSQNameBinding implements SimpleBinding  {

	NamespaceSupport namespaceSupport;
	NamespaceContext namespaceContext;
	
	public XSQNameBinding( NamespaceSupport namespaceSupport ) {
		this.namespaceSupport = namespaceSupport;
		namespaceContext = new NamespaceSupportWrappper(); 
	}
	
	/**
	 * @generated
	 */	
	public QName getTarget() {
		return XS.QNAME;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public int getExecutionMode() {
		return AFTER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * This binding returns objects of type {@link QName}.
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return QName.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * This binding returns objects of type {@link QName}.
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(InstanceComponent instance, Object value) 
		throws Exception {
		DatatypeConverter.setDatatypeConverter(DatatypeConverterImpl.theInstance);
		QName qName = DatatypeConverter.parseQName((String) value, namespaceContext );
		if ( qName != null ) {
			return qName;
		}
		
		if ( value == null ) {
			return new QName( null );
		}
		
		String s = (String) value;
		int i = s.indexOf( ':' );
		if ( i != -1 ) {
			String prefix = s.substring( 0, i );
			String local = s.substring( i+1 );
			
			return new QName( null, local, prefix );
		}
		
		return new QName( null, s );
	}

	/**
	 * NamespaceContext wrapper around namespace support.
	 */
	class NamespaceSupportWrappper implements NamespaceContext {

		
		public String getNamespaceURI( String prefix ) {
			return XSQNameBinding.this.namespaceSupport.getURI( prefix );
		}

		public String getPrefix( String namespaceURI ) {
			return XSQNameBinding.this.namespaceSupport.getPrefix( namespaceURI );
		}

		public Iterator getPrefixes( String namespaceURI ) {
			final Enumeration e = 
				XSQNameBinding.this.namespaceSupport.getPrefixes( namespaceURI );
			
			return new Iterator() {

				public void remove() {
					throw new UnsupportedOperationException();
				}

				public boolean hasNext() {
					return e.hasMoreElements();
				}

				public Object next() {
					return e.nextElement();
				}
			};
		}
		
	}
	
}