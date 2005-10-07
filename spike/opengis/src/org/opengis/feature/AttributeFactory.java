package org.opengis.feature;

import java.util.List;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Plays the role of making actual instances of types in this puzzle.
 * <p>
 * </p>
 * 
 * @author Gabriel Roldan, Axios Engineering
 *
 */
public interface AttributeFactory {


	/**
	 * Creates an attribute to hold values of the prescribed <code>type</code>
	 * @param type
	 * @return
	 */
	public Attribute create(AttributeType type);

	/**
	 * 
	 * @param type
	 * @param value
	 * @return
	 */
	public Attribute create(AttributeType type, Object value);
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public GeometryAttribute create(GeometryType type);
	
	/**
	 * 
	 * @param type
	 * @param crs
	 * @return
	 */
	public GeometryAttribute create(GeometryType type, CoordinateReferenceSystem crs);
	
	/**
	 * 
	 * @param type
	 * @param value
	 * @return
	 */
	public GeometryAttribute create(GeometryType type, Geometry value);
	
	/**
	 * 
	 * @param type
	 * @param crs
	 * @param value
	 * @return
	 */
	public GeometryAttribute create(GeometryType type, CoordinateReferenceSystem crs, Geometry value);
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public SimpleFeature create(SimpleFeatureType type);
	
	/**
	 * 
	 * @param type
	 * @param values
	 * @return
	 */
	public SimpleFeature create(SimpleFeatureType type, List<? extends Object>values);

	/**
	 * 
	 * @param type
	 * @param values
	 * @return
	 */
	public SimpleFeature create(SimpleFeatureType type, Object []values);

	/**
	 * 
	 * @param type
	 * @return
	 */
	public ComplexAttribute create(ComplexType type);

	/**
	 * 
	 * @param type
	 * @return
	 */
	public Feature create(FeatureType type);
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public FeatureCollection create(FeatureCollectionType type);
}
