package org.geotools.feature.type;

import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geotools.filter.Filter;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;

public class FeatureTypeImpl extends ComplexTypeImpl implements FeatureType {
	protected GeometryType DEFAULT;

	public FeatureTypeImpl(String name, Descriptor schema, GeometryType defaultGeom ) {
		this( new QName( name ), schema, defaultGeom );
	}	
	public FeatureTypeImpl(QName name, Descriptor schema, GeometryType defaultGeom ) {
		super(name, schema, true, List.class, false, null);
		setDefault(defaultGeom);
	}
	public FeatureTypeImpl(QName name, Descriptor schema, GeometryType defaultGeom, Set<Filter> restrictions, FeatureType superType, boolean isAbstract){
		super(name, schema, true, List.class, false, restrictions, superType, isAbstract );
		setDefault(defaultGeom);
	}
	
	private void setDefault(GeometryType defaultGeom){
		if(defaultGeom == null){
			for(AttributeType type : super.types()){
				if(type instanceof GeometryType){
					defaultGeom = (GeometryType)type;
					break;
				}
			}
		}else{
			if(!super.types().contains(defaultGeom)){
				throw new IllegalArgumentException("Default geometry type " + 
						defaultGeom + " is not found on schema");
			}
		}
		DEFAULT = defaultGeom;
	}
	
	public GeometryType getDefaultGeometry() {
		return DEFAULT;
	}
}
