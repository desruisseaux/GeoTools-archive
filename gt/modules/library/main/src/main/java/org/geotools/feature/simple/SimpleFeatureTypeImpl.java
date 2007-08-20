package org.geotools.feature.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.geotools.feature.type.Descriptors;
import org.geotools.feature.type.FeatureTypeImpl;
import org.geotools.feature.type.Types;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

/**
 * Implementation fo SimpleFeatureType, subtypes must be atomic and are stored
 * in a list.
 * 
 * @author Justin
 */
public class SimpleFeatureTypeImpl extends FeatureTypeImpl implements
		SimpleFeatureType {

	// list of types
	List types = null;

	public SimpleFeatureTypeImpl(Name name, List/*<AttributeDescriptor>*/ schema,
			AttributeDescriptor defaultGeometry, CoordinateReferenceSystem crs,
			Set/* <Filter> */restrictions, InternationalString description) {
		super(name, schema, defaultGeometry, crs, false, restrictions, null,
				description);
	}
	
	public SimpleFeatureTypeImpl(Name name, List/*<AttributeType>*/ typeList, AttributeType geometryType, CoordinateReferenceSystem crs, Set restrictions, InternationalString description) {
		this( name, Descriptors.wrapAttributeTypes( typeList ),Descriptors.wrapAttributeType(geometryType) , crs, restrictions, description );
		types = typeList;
	}
	
	public List getAttributes() {
		return (List) getProperties();
	}
	
	public AttributeType getType(Name qname) {
		return Descriptors.type(PROPERTIES, qname);
	}
	public AttributeDescriptor getAttribute(Name name) {
		return Descriptors.node(PROPERTIES, name);
	}

	public AttributeType getType(String name) {
		return getType(Types.attributeName(name));
	}
	public AttributeDescriptor getAttribute(String name) {
		return getAttribute(Types.attributeName(name));
	}

	public AttributeType getType(int index) {
		return (AttributeType) getTypes().get(index);
	}
	public AttributeDescriptor getAttribute(int index) {
		return (AttributeDescriptor) ((List)PROPERTIES).get(index);
	}
	public int indexOf(String name) {
		int index = 0;
		for (Iterator itr = PROPERTIES.iterator(); itr.hasNext(); index++) {
			AttributeDescriptor descriptor = (AttributeDescriptor) itr.next();
			if (name.equals(descriptor.getName().getLocalPart())) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * Number of available attributes
	 */
	public int getAttributeCount() {
		return PROPERTIES.size();
	}

	/**
	 * Types are returned in the perscribed index order.
	 * 
	 * @return Types in prescribed order
	 */
	public List /* List<AttributeType> */getTypes() {
		if (types == null) {
			synchronized (this) {
				if (types == null) {
					types = new ArrayList();
					for (Iterator itr = PROPERTIES.iterator(); itr.hasNext();) {
						AttributeDescriptor ad = (AttributeDescriptor) itr
								.next();
						types.add(ad.getType());
					}
				}
			}
		}

		return types;
	}

	public GeometryType getDefaultGeometryType() {
		AttributeDescriptor desc = getDefaultGeometry();
		if (desc != null)
			return (GeometryType) desc.getType();

		return null;
	}

}
