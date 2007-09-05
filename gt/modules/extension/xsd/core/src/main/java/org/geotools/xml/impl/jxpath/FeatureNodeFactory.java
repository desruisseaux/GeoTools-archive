package org.geotools.xml.impl.jxpath;

import java.util.Locale;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.NodePointerFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;

/**
 * A node factory which creates special node pointers featurs.
 * <p>
 * The following types are supported:
 * <ul>
 * 	<li>{@link org.geotools.feature.Feature}
 * 	<li>{@link org.geotools.feature.FeatureType}
 * </ul>
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class FeatureNodeFactory implements NodePointerFactory {

	public int getOrder() {
		return 0;
	}

	public NodePointer createNodePointer(QName name, Object object,
			Locale locale) {
	
		if ( object instanceof Feature ) {
			return new FeaturePointer( null, (Feature)object, name );	
		}
		
		if ( object instanceof FeatureType ) {
			return new FeatureTypePointer( null, (FeatureType) object, name );
		}
		
		return null;
	}

	public NodePointer createNodePointer(NodePointer parent, QName name,
			Object object) {
		
		if ( object instanceof Feature ) {
			return new FeaturePointer( parent, (Feature)object, name );	
		}
		
		if ( object instanceof FeatureType ) {
			return new FeatureTypePointer( null, (FeatureType) object, name );
		}
		
		
		return null;
	}

}
