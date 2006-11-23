/*
 * This implementation of the OGC Feature Geometry Abstract Specification
 * (ISO 19107) is a project of the University of Applied Sciences Cologne
 * (Fachhochschule Köln) in collaboration with GeoTools and GeoAPI.
 *
 * Copyright (C) 2006 University of Applied Sciences Köln
 *                    (Fachhochschule Köln) and GeoTools
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * For more information, contact:
 *
 *     Prof. Dr. Jackson Roehrig
 *     Institut für Technologie in den Tropen
 *     Fachhochschule Köln
 *     Betzdorfer Strasse 2
 *     D-50679 Köln
 *     Jackson.Roehrig@fh-koeln.de
 *
 *     Sanjay Dominik Jena
 *     san.jena@gmail.com
 *
 */
package org.geotools.geometry.iso;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geotools.geometry.iso.aggregate.AggregateFactoryImpl;
import org.geotools.geometry.iso.complex.ComplexFactoryImpl;
import org.geotools.geometry.iso.coordinate.CoordinateFactoryImpl;
import org.geotools.geometry.iso.primitive.PrimitiveFactoryImpl;
import org.geotools.geometry.iso.root.GeometryImpl;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.primitive.OrientableCurve;
import org.opengis.spatialschema.geometry.primitive.OrientableSurface;
import org.opengis.spatialschema.geometry.primitive.Point;
import org.opengis.spatialschema.geometry.primitive.Primitive;

/**
 * The Feature Geometry Factory is the most upper factory and is responsible for
 * the management of:
 * 
 *  - The used Coordinate Factory implementation (GeoAPI GeometryFactory). Each
 *    Feature Geometry Factory can only hold one Coordinate Factory implementation.
 *    
 *  - The used Primitive Factory implementation (GeoAPI PrimitiveFactory). Each
 *    Feature Geometry Factory can only hold one Primitive Factory implementation.
 *    
 *  - The used Complex Factory implementation (GeoAPI ComplexFactory). Each
 *    Feature Geometry Factory can only hold one Complex Factory implementation.
 *    
 *  - The Coordinate Reference System.
 *  
 *  - The dimension model. Each Feature Geometry Factory can be of one dimension
 *    model, e.g. can only handle geometry objects of the same coordinate
 *    dimension.
 *    
 *  - The precision model.
 * 
 */
public class FeatGeomFactoryImpl {

	// Instances of the dimension types
	private static FeatGeomFactoryImpl singleton2D = null;

	private static FeatGeomFactoryImpl singleton2o5D = null;

	private static FeatGeomFactoryImpl singleton3D = null;

	/**
	 * Returns the coordinate factory according to the desired dimension model
	 * 
	 * @param dimension
	 * @return CoordinateFactory
	 */
	public static CoordinateFactoryImpl getDefaultCoordinateFactory(
			DimensionModel dimension) {
		if (dimension.is2D()) {
			return singleton2D.getCoordinateFactory();
		} else if (dimension.is2o5D()) {
			return singleton2o5D.getCoordinateFactory();
		} else if (dimension.is3D()) {
			return singleton3D.getCoordinateFactory();
		} else {
			return null;
		}
	}

	/**
	 * TODO Diese Lösung wird auf Dauer nicht funktionieren, wenn wir zwischen
	 * 2.D, 2.5D und 3D unterscheiden möchten. Ich schlage den oben stehenden
	 * getter vor.
	 * 
	 * @param dimension
	 * @return
	 */
//	public static CoordinateFactoryImpl getDefaultCoordinateFactory(
//			int dimension) {
//		if (dimension == 2) {
//			return singleton2D.getCoordinateFactory();
//		} else if (dimension == 3) {
//			return singleton3D.getCoordinateFactory();
//		} else {
//			return null;
//		}
//	}

	// The coordinate reference system
	private CoordinateReferenceSystem coordinateReferenceSystem = null;

	// The used precision model
	private PrecisionModel precisionModel = null;

	private CoordinateFactoryImpl coordinateFactory;

	private PrimitiveFactoryImpl primitiveFactory;

	private ComplexFactoryImpl complexFactory;

	private AggregateFactoryImpl aggregateFactory;

	private ListFactory listFactory;

	// The dimension model, which handles one of the three dimension types: 2D,
	// 2.5D and 3D
	private DimensionModel dimensionModel = null;

	/**
	 * Private Constructor
	 * 
	 * @param crs
	 * @param dimensionModel
	 */
	private FeatGeomFactoryImpl(CoordinateReferenceSystem crs,
			int dimensionModel) {
		this.coordinateReferenceSystem = crs;
		this.coordinateFactory = new CoordinateFactoryImpl(this);
		this.primitiveFactory = new PrimitiveFactoryImpl(this);
		this.complexFactory = new ComplexFactoryImpl(this);
		this.aggregateFactory = new AggregateFactoryImpl(this);
		this.listFactory = new ListFactoryImpl();

		// Create the dimension model
		this.dimensionModel = new DimensionModel(dimensionModel);

		// Set Precision Model (Default: float)
		this.precisionModel = new PrecisionModel();
	}

	/**
	 * Returns the Feature Geometry Factory for 2 dimensional space
	 * 
	 * @return GeometryFactoryImpl
	 */
	public static FeatGeomFactoryImpl getDefault2D() {
		if (singleton2D == null)
			singleton2D = new FeatGeomFactoryImpl(null,
					DimensionModel.TWO_DIMENSIONIAL);
		return singleton2D;
	}

	/**
	 * Returns the Feature Geometry Factory for 2.5 dimensional space
	 * 
	 * @return GeometryFactoryImpl
	 */
	public static FeatGeomFactoryImpl getDefault2o5D() {
		if (singleton2o5D == null)
			singleton2o5D = new FeatGeomFactoryImpl(null,
					DimensionModel.TWOoFIVE_DIMENSIONIAL);
		return singleton2o5D;
	}

	/**
	 * Returns the Feature Geometry Factory for 3 dimensional space
	 * 
	 * @return GeometryFactoryImpl
	 */
	public static FeatGeomFactoryImpl getDefault3D() {
		if (singleton3D == null)
			singleton3D = new FeatGeomFactoryImpl(null,
					DimensionModel.THREE_DIMENSIONIAL);
		return singleton3D;
	}

	/**
	 * @return CoordinateReferenceSystem
	 */
	public CoordinateReferenceSystem getCoordinateReferenceSystem() {
		return this.coordinateReferenceSystem;
	}

	/**
	 * @return coordinate dimension (int)
	 * @throws Exception
	 */
	public int getCoordinateDimension() {
		// Auskommentiert und ersetzt durch Sanjay, da Endlos-Schleife
		// Stattdessen wird Membervariable mDimension eingefuegt.
		// return this.getCoordinateDimension();
		return this.dimensionModel.getCoordinateDimension();
	}

	/**
	 * Returns the used Precision Model
	 * 
	 * @return the used Precision Model
	 */
	public PrecisionModel getPrecisionModel() {
		return this.precisionModel;
	}

	/**
	 * Returns the used Dimension Model
	 * 
	 * @return the used Dimension Model
	 */
	public DimensionModel getDimensionModel() {
		return this.dimensionModel;
	}

	/**
	 * @return Returns the aggregateFactory.
	 */
	public AggregateFactoryImpl getAggregateFactory() {
		return aggregateFactory;
	}

	/**
	 * @return Returns the complexFactory.
	 */
	public ComplexFactoryImpl getComplexFactory() {
		return complexFactory;
	}

	/**
	 * @return Returns the coordinateFactory.
	 */
	public CoordinateFactoryImpl getCoordinateFactory() {
		return coordinateFactory;
	}

	/**
	 * @return Returns the primitiveFactory.
	 */
	public PrimitiveFactoryImpl getPrimitiveFactory() {
		return primitiveFactory;
	}

	/**
	 * @return Returns the listFactory.
	 */
	public ListFactory getListFactory() {
		return listFactory;
	}
	
	
	/**
	 * Creates a new Geometry object appropriate to the input Primitives. The
	 * method will return a Primitive object, if one list contains only one
	 * element and the rest is empty. In all other cases, that is that exist
	 * more than one Primitive in the lists, the method will return a Complex
	 * object.
	 * 
	 * @param aSurfaces
	 *            List of Surfaces
	 * @param aCurves
	 *            List of Curves
	 * @param aPoints
	 *            List of Points
	 * @return a Geometry instance.
	 * That is a Point/Curve/Surface if the parameters only contain one point or one curve or one surface.
	 * It is a MultiPoint/MultiCurve/MultiSurface if the parameters contain one list with more than two entries and the other two lists are empty.
	 * Or it is a MultiPrimitive if the parameters contain a mixture of points, curves and surfaces.
	 */
	public GeometryImpl createGeometry(List<OrientableSurface> aSurfaces,
			List<OrientableCurve> aCurves, List<Point> aPoints) {

		int nS = aSurfaces.size();
		int nC = aCurves.size();
		int nP = aPoints.size();
		
		if (nS + nC + nP == 0)
			// Return null if the sets are empty
			return null;
			//throw new IllegalArgumentException("All Sets are empty");

		
		if (nS == 0) {
			
			if (nC == 0) {
				
				// Surfaces empty, Curves empty, Points not empty
				if (nP == 1) {
					
					// POINT
					return (GeometryImpl) aPoints.get(0);
					
				} else {
					
					// MULTIPOINT
					return (GeometryImpl) this.aggregateFactory.createMultiPoint(new HashSet(aPoints));
					
				}
			} else if (nP == 0) {
				
				// Surfaces empty, Curves not empty, Points empty
				if (nC == 1) {
					
					// CURVE
					
					return (GeometryImpl) aCurves.get(0);
				} else {
					
					// MULTICURVE
					return (GeometryImpl) this.aggregateFactory.createMultiCurve(new HashSet(aCurves));
				}
			}

		} else {
			
			if (nC == 0 && nP == 0) {
				
				if (nS == 1) {
					
					// SURFACE
					return (GeometryImpl) aSurfaces.get(0);
					
				} else {
					
					// MULTISURFACE
					return (GeometryImpl) this.aggregateFactory.createMultiSurface(new HashSet(aSurfaces));
					
				}
				
			}

		}
		
		// All other cases: MULTIPRIMITIVE
		Set<Primitive> tPrimitives = new HashSet<Primitive>();
		tPrimitives.addAll(aSurfaces);
		tPrimitives.addAll(aCurves);
		tPrimitives.addAll(aPoints);
		
		return (GeometryImpl) this.aggregateFactory.createMultiPrimitive(tPrimitives);

	}

	

}
