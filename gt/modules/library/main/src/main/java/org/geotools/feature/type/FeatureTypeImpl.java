package org.geotools.feature.type;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.geotools.resources.Utilities;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

/**
 * 
 * Base implementation of FeatureType.
 * 
 * @author gabriel
 *
 */
public class FeatureTypeImpl extends ComplexTypeImpl implements FeatureType {
	
	protected AttributeDescriptor DEFAULT;

	protected CoordinateReferenceSystem CRS;
	
	public FeatureTypeImpl(
		Name name, Collection schema, AttributeDescriptor defaultGeom, 
		CoordinateReferenceSystem crs, boolean isAbstract, 
		Set/*<Filter>*/ restrictions, AttributeType superType, InternationalString description
	) {
		super(name, schema, true, isAbstract, restrictions, superType, description);
		DEFAULT = defaultGeom;
        CRS = crs;
	}

	public CoordinateReferenceSystem getCRS() {
		return CRS;
	}
	
	public AttributeDescriptor getDefaultGeometry() {
		if (DEFAULT == null) {
			for (Iterator itr = attributes().iterator(); itr.hasNext();) {
				AttributeDescriptor desc = (AttributeDescriptor) itr.next();
				if (desc.getType() instanceof GeometryType) {
					DEFAULT = desc; 
					break;
				}
			}
		}
		return DEFAULT;
	}
	
	public boolean equals(Object o) {
		if(!(o instanceof FeatureType)){
    		return false;
    	}
    	if(!super.equals(o)){
    		return false;
    	}
    	
    	FeatureType other = (FeatureType) o;
    	if (!Utilities.equals( DEFAULT, other.getDefaultGeometry())) {
    		return false;
    	}
    	
    	if (!Utilities.equals( CRS, other.getCRS()) ) {
    		return false;
    	}
    	
    	return true;
	}
	
	public int hashCode() {
		int hashCode = super.hashCode();
		
		if ( DEFAULT != null ) {
			hashCode = hashCode ^ DEFAULT.hashCode();
		}
		
		if ( CRS != null ) {
			hashCode = hashCode ^ CRS.hashCode();
		}
		
		return hashCode;
	}
	
//	public GeometryType getDefaultGeometry() {
//		
//		if (DEFAULT == null) {
//			for (Iterator itr = getAttributes().iterator(); itr.hasNext();) {
//				AttributeDescriptor desc = (AttributeDescriptor) itr.next();
//				if (desc.getType() instanceof GeometryType) {
//					DEFAULT = (GeometryType) desc.getType(); 
//					break;
//				}
//			}
//		}
//		return DEFAULT;
//	}
}
