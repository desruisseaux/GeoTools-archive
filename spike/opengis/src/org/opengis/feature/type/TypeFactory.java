package org.opengis.feature.type;

import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geotools.filter.Filter;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.simple.SimpleDescriptor;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Factory interface for the typing system.
 * 
 * @author Gabriel Roldan, Axios Engineering
 * TODO: add FeatureCollection factory methods
 */
public interface TypeFactory {

	/**
	 * 
	 * @param name
	 * @param binding
	 * @return
	 */
	AttributeType createType(
			String name,
			Class binding);

	/**
	 * 
	 * @param name
	 * @param binding
	 * @return
	 */
	AttributeType createType(
			QName name, 
			Class binding);

	/**
	 * 
	 * @param name
	 * @param binding
	 * @param identified
	 * @param nillable
	 * @param restrictions
	 * @return
	 */
	AttributeType createType(
			QName name,
			Class binding, 
			boolean identified, 
			boolean nillable,
			Set<Filter> restrictions);

	/**
	 * 
	 * @param name
	 * @param binding
	 * @param identified
	 * @param nillable
	 * @param restrictions
	 * @param superType
	 * @return
	 */
	AttributeType createType(
			QName name,
			Class binding, 
			boolean identified, 
			boolean nillable,
			Set<Filter> restrictions, 
			AttributeType superType);

	/**
	 * 
	 * @param name
	 * @param schema
	 * @return
	 */
	ComplexType createType(
			String name,
			Descriptor schema);

	/**
	 * 
	 * @param name
	 * @param schema
	 * @return
	 */
	ComplexType createType(
			QName name,
			Descriptor schema);

	/**
	 * 
	 * @param name
	 * @param schema
	 * @param identified
	 * @param binding
	 * @param nillable
	 * @param restrictions
	 * @return
	 */
	ComplexType createType(
			QName name,
			Descriptor schema, 
			boolean identified, 
			Class binding,
			boolean nillable, 
			Set<Filter> restrictions);

	/**
	 * 
	 * @param name
	 * @param schema
	 * @param identified
	 * @param binding
	 * @param nillable
	 * @param restrictions
	 * @param superType
	 * @param isAbstract
	 * @return
	 */
	ComplexType createType(
			QName name,
			Descriptor schema, 
			boolean identified, 
			Class binding,
			boolean nillable, 
			Set<Filter> restrictions, 
			ComplexType superType,
			boolean isAbstract);

	/**
	 * 
	 * @param name
	 * @param schema
	 * @param defaultGeometry
	 * @return
	 */
	FeatureType createFeatureType(
			String name,
			Descriptor schema, 
			GeometryType defaultGeometry);

	/**
	 * 
	 * @param name
	 * @param schema
	 * @param defaultGeometry
	 * @return
	 */
	FeatureType createFeatureType(
			QName name,
			Descriptor schema, 
			GeometryType defaultGeometry);

	/**
	 * 
	 * @param name
	 * @param schema
	 * @param defaultGeometry
	 * @param restrictions
	 * @param superType
	 * @param isAbstract
	 * @return
	 */
	FeatureType createFeatureType(
			QName name,
			Descriptor schema, 
			GeometryType defaultGeometry,
			Set<Filter> restrictions, 
			FeatureType superType, 
			boolean isAbstract);

	/**
	 * 
	 * @param name
	 * @param types
	 * @param defaultGeometry
	 * @return
	 */
	SimpleFeatureType createFeatureType(
			QName name,
			List<AttributeType> types, 
			GeometryType defaultGeometry);
	
	/**
	 * 
	 * @param name
	 * @param schema
	 * @param defeaultGeometry
	 * @param isAbstract
	 * @return
	 */
	SimpleFeatureType createFeatureType(
			QName name,
			SimpleDescriptor schema, 
			GeometryType defeaultGeometry,
			boolean isAbstract);
}