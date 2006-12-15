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
package org.geotools.geometry.iso.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.coordinate.DirectPositionImpl;
import org.geotools.geometry.iso.primitive.CurveImpl;
import org.opengis.spatialschema.geometry.DirectPosition;

/**
 * Merges curves at end points
 * 
 * @author Sanjay Dominik Jena
 *
 */
public class Merger {
	
	FeatGeomFactoryImpl mFactory = null;
	
	public Merger(FeatGeomFactoryImpl factory) {
		this.mFactory = factory;
	}
	
	/**
	 * Merges a list of continuous curves into a new single curve.
	 * In order two neighboured curves are merged, their end and startpoint must be equal.
	 * 
	 * @param curves
	 * @return
	 */
	public CurveImpl merge(List<CurveImpl> curves) {

		for (int i=0; i<curves.size()-1; i++) {
			if (!curves.get(i).getEndPoint().equals(curves.get(i+1).getStartPoint())) {
				throw new IllegalArgumentException("Curves are not continuous");
			}
		}
		

		return null;
	}
	
	/**
	 * Merges a set of curves into a new single curve.
	 * This method trys all combinations of curve´s start and end points.
	 * 
	 * @param curves
	 * @return
	 */
	public CurveImpl merge(Set<CurveImpl> curves) {
		return null;
	}
	
	/**
	 * Constructs a new Curve by merging this Curve with another Curve
	 * The two input curves will not be modified.
	 * There will be no more references to positions or lists of the input curves, all values are copied.
	 * 
	 * @param curve1
	 * @param curve2
	 * @return new curve
	 */
	public CurveImpl merge(CurveImpl curve1, CurveImpl curve2) {
		CurveImpl firstCurve = null;
		CurveImpl secondCurve = null;
		
		if (curve1.getStartPoint().equals(curve2.getEndPoint())) {
			firstCurve = curve2;
			secondCurve = curve1;
		} else
		if (curve1.getEndPoint().equals(curve2.getStartPoint())) {
				firstCurve = curve1;
				secondCurve = curve2;
		} else
			throw new IllegalArgumentException("Curves do not share a start and end point ");
		
		List<CurveImpl> curves = new ArrayList<CurveImpl>();
		curves.add(firstCurve);
		curves.add(secondCurve);
		
		return this.mergeContinuousCurves(curves);		
	}
	
	/**
	 * Merges a list of continuous curves into a new single curve.
	 * In order two neighboured curves are merged, their end and startpoint must be equal.
	 * 
	 * @param curves
	 * @return
	 */
	private CurveImpl mergeContinuousCurves(List<CurveImpl> curves) {

		List<DirectPosition> newDPList = new ArrayList<DirectPosition>();
		
		int i=0;
		int j=0;
		for (i=0; i<curves.size(); i++) {
			List<DirectPositionImpl> dPList = curves.get(i).asDirectPositions();
			for (j=0; j<dPList.size()-1; j++) {
				newDPList.add(dPList.get(j).clone());
			}
		}
		
		newDPList.add(curves.get(curves.size()-1).getEndPoint());
		
		return (CurveImpl) this.mFactory.getPrimitiveFactory().createCurveByDirectPositions(newDPList);
	}
	
	

}
