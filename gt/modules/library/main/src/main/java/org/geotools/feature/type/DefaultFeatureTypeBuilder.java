package org.geotools.feature.type;

import java.util.List;
import java.util.Set;

import org.geotools.feature.AttributeType;
import org.geotools.feature.DefaultAttributeType;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.DefaultTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.simple.SimpleTypeFactoryImpl;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Feature Type Builder which creates instances of the old model.
 * <p>
 * This class should not be used outside of geotools itself by client code. 
 * Client code should be using {@link SimpleFeatureTypeBuilder}.
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class DefaultFeatureTypeBuilder extends SimpleFeatureTypeBuilder {
    
	public DefaultFeatureTypeBuilder() {
		super( new DefaultTypeFactory());
		attributeBuilder = new DefaultAttributeTypeBuilder();
		
		//sets the default namespace to gml
		setNamespaceURI((String)null);
	}
	
	public void setNamespaceURI(String namespaceURI) {
	    if ( namespaceURI != null ) {
	        super.setNamespaceURI(namespaceURI);
	    }
	    else {
	        super.setNamespaceURI("http://www.opengis.net/gml");
	    }
	    
	}
	
	public void add(String name, Class binding) {
		if ( Geometry.class.isAssignableFrom(binding)) {
			//TODO: check user data
			add( name, binding, (CoordinateReferenceSystem) null );
		}
		else {
			super.add( name, binding );
		}
	}
	
	/**
	 * Additional api for adding an AttributeType directly.
	 * 
	 */
	public void add(AttributeType type) {
	    attributes().add(type);
	}
	public void add(AttributeType[] types) {
	    if( types == null ) 
	        return;
	    
	    for ( int i = 0; i < types.length; i++ ) {
	        add(types[i]);
	    }
	}
	
	protected boolean isGeometry(AttributeDescriptor descriptor) {
		return descriptor instanceof GeometryAttributeType;
	}
	
	/**
	 * Override to type narror to DefaultFeautreType.
	 */
	public DefaultFeatureType buildFeatureType() {
	    return (DefaultFeatureType) super.buildFeatureType();
	}
}
