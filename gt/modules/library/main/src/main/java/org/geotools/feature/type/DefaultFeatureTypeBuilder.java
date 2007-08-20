package org.geotools.feature.type;

import java.util.List;
import java.util.Set;

import org.geotools.feature.AttributeType;
import org.geotools.feature.DefaultAttributeType;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.simple.SimpleTypeBuilder;
import org.geotools.feature.simple.SimpleTypeFactoryImpl;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

import com.vividsolutions.jts.geom.Geometry;

public class DefaultFeatureTypeBuilder extends SimpleTypeBuilder {

	public DefaultFeatureTypeBuilder() {
		super( new DefaultFeatureTypeFactory());
		attributeBuilder = new DefaultAttributeTypeBuilder();
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
	/**
	 * Extension of {@link SimpleTypeFactoryImpl} which creates intances of 
	 * {@link DefaultFeatureType}. 
	 * <p>
	 * This class is provided for backwards compatability with old feature model 
	 * while the transition to geoapi takes place. This class should not be used
	 * directly outside of the library.
	 * </p>
	 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
	 *
	 */
	private static class DefaultFeatureTypeFactory extends SimpleTypeFactoryImpl {

		public SimpleFeatureType createSimpleFeatureType(Name name, List schema, AttributeDescriptor defaultGeometry, CoordinateReferenceSystem crs, Set restrictions, InternationalString description) {
			return new DefaultFeatureType(name,schema,defaultGeometry,crs,restrictions,description);
		}
	}
}
