package org.geotools.xml.impl.jxpath;

import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.geotools.feature.Feature;

public class FeaturePointer extends NodePointer {

	QName name;
	
	Feature feature;
	
	protected FeaturePointer(NodePointer parent, Feature feature, QName name ) {
		super(parent);
		this.name = name;
		this.feature = feature;
	}

	public boolean isLeaf() {
		return false;
	}

	public boolean isCollection() {
		return true;
	}

	public int getLength() {
		return feature.getNumberOfAttributes();
	}

	public QName getName() {
		return null;
	}

	public Object getBaseValue() {
		return null;
	}

	public Object getImmediateNode() {
		return feature;
	}

	public void setValue(Object value) {
		feature = (Feature)value;
	}

	public int compareChildNodePointers(NodePointer pointer1,
			NodePointer pointer2) {
	
		return 0;
	}

	public NodeIterator childIterator(NodeTest test, boolean reverse, NodePointer startWith) {
		if ( test instanceof NodeNameTest ) {
			NodeNameTest nodeNameTest = (NodeNameTest) test;
			
			if ( !nodeNameTest.isWildcard() ) {
				int index = feature.getFeatureType().find( nodeNameTest.getNodeName().getName() );	
				return new SingleFeaturePropertyIterator( this, index );
			}
			
			return new FeaturePropertyIterator( this ); 
		}
		
		if ( test instanceof NodeTypeTest ) {
			NodeTypeTest nodeTypeTest = (NodeTypeTest) test;
			if ( nodeTypeTest.getNodeType() == Compiler.NODE_TYPE_NODE ) {
				return new FeaturePropertyIterator( this );
			}
		}
		
		return super.childIterator( test, reverse, startWith );
	}
	
	public NodeIterator attributeIterator(QName qname) {
		if ( qname.getName().equals( "id") || qname.getName().equals( "fid") ) {
			return new SingleFeaturePropertyIterator( this, -1 );		
		}
		
		return super.attributeIterator( qname );
	}
	
	
}
