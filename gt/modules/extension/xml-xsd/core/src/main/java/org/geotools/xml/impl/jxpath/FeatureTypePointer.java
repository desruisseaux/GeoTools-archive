package org.geotools.xml.impl.jxpath;

import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.geotools.feature.FeatureType;

/**
 * Special node pointer for {@link org.geotools.feature.FeatureTy}.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class FeatureTypePointer extends NodePointer {

	/**
	 * The name of hte node.
	 */
	QName name;
	
	/**
	 * The underlying feature type
	 */
	FeatureType featureType;
	
	protected FeatureTypePointer(NodePointer parent, FeatureType featureType, QName name ) {
		super(parent);
		this.name = name;
		this.featureType = featureType;
	}

	public boolean isLeaf() {
		return false;
	}

	public boolean isCollection() {
		return true;
	}

	public int getLength() {
		return featureType.getAttributeCount();
	}

	public QName getName() {
		return name;
	}

	public Object getBaseValue() {
		return null;
	}

	public Object getImmediateNode() {
		return featureType;
	}

	public void setValue(Object value) {
		featureType = (FeatureType)value;
	}

	public int compareChildNodePointers(NodePointer pointer1,
			NodePointer pointer2) {
	
		return 0;
	}

	public NodeIterator childIterator(NodeTest test, boolean reverse, NodePointer startWith) {
		if ( test instanceof NodeNameTest ) {
			NodeNameTest nodeNameTest = (NodeNameTest) test;
			
			if ( !nodeNameTest.isWildcard() ) {
				int index = featureType.find( nodeNameTest.getNodeName().getName() );	
				if ( index > -1 ) {
					return new SingleFeatureTypeAttributeIterator( this, index );	
				}
				
			}
			else {
				return new FeatureTypeAttributeIterator( this );	
			}
		}
		
		if ( test instanceof NodeTypeTest ) {
			NodeTypeTest nodeTypeTest = (NodeTypeTest) test;
			if ( nodeTypeTest.getNodeType() == Compiler.NODE_TYPE_NODE ) {
				return new FeatureTypeAttributeIterator( this );
			}
		}
		
		return super.childIterator( test, reverse, startWith );
	}
	
}
