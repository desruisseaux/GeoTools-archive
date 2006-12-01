package org.geotools.xml.impl.jxpath;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.geotools.feature.Feature;

/**
 * Pointer to a single property of a feature.
 * <p>
 * THe property of the feature is specified via index. Setting the index to 
 * -1 will cause the pointer to point at the feature id.
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class FeaturePropertyPointer extends NodePointer {

	/**
	 * the feature
	 */
	Feature feature;
	/**
	 * The parent pointer
	 */
	FeaturePointer parent;
	/**
	 * the indedx of hte property being pointed at
	 */
	int index;
	
	/**
	 * Creates the pointer.
	 * 
	 * @param parent The parent pointer, pointer at the feature.
	 * @param index The index of hte property to point to, or -1 to point 
	 * 	to the feature id.
	 */
	public FeaturePropertyPointer( FeaturePointer parent, int index ) {
		super( parent );
		this.index = index;
		this.feature = (Feature) parent.getImmediateNode();
	}
	
	/**
	 * Return <code>true</code>.
	 */
	public boolean isLeaf() {
		return true;
	}

	/**
	 * Return <code>false</code>. 
	 */
	public boolean isCollection() {
		return false;
	}

	/**
	 * Return <code>1</code>
	 */
	public int getLength() {
		return 1;
	}

	/**
	 * Returns the qname with prefix as <code>null</code>, and local part the name of the 
	 * feature attribute.
	 */
	public QName getName() {
		return index != -1 ? 
			new QName( null, feature.getFeatureType().getAttributeType( index ).getName() ) :
			new QName( null, "fid" );
	}

	public Object getBaseValue() {
		return feature;
	}

	public Object getImmediateNode() {
		return index != -1 ? feature.getAttribute( index ) : feature.getID();
	}

	public void setValue(Object value) {
		if ( index == -1 )
			return; //fids arent settable
		
		try {
			feature.setAttribute( index, value );
		} 
		catch( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	/**
	 * Always return <code>0</code>, can never have child pointers.
	 */
	public int compareChildNodePointers(NodePointer pointer1,
			NodePointer pointer2) {
		return 0;
	}

}
