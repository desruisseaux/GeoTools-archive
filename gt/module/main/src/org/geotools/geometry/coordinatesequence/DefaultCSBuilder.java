/*
 * Created on 31-dic-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.geometry.coordinatesequence;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.DefaultCoordinateSequenceFactory;

/**
 * CSBuilder that generates DefaultCoordinateSequence objects, that is, 
 * coordinate sequences backed by Coordinate[]
 * @author wolf
 */
public class DefaultCSBuilder implements CSBuilder {

	private Coordinate[] coordinateArray;
	private CoordinateSequenceFactory factory = new DefaultCoordinateSequenceFactory();

	/**
	 * @see org.geotools.geometry.coordinatesequence.CSBuilder#start(int, int)
	 */
	public void start(int size, int dimensions) {
		coordinateArray = new Coordinate[size];	
		for(int i = 0; i < size; i++)
			coordinateArray[i] = new Coordinate();
	}

	/**
	 * @see org.geotools.geometry.coordinatesequence.CSBuilder#getCoordinateSequence()
	 */
	public CoordinateSequence end() {
		CoordinateSequence cs = factory.create(coordinateArray);
		coordinateArray = null;
		return cs;
	}

	/**
	 * @see org.geotools.geometry.coordinatesequence.CSBuilder#setOrdinate(double, int, int)
	 */
	public void setOrdinate(double value, int ordinateIndex, int coordinateIndex) {
		Coordinate c = coordinateArray[coordinateIndex];
		switch(ordinateIndex) {
			case 0: c.x = value;
			case 1: c.y = value;
			case 2: c.z = value;
		}
	}

	/**
	 * @see org.geotools.geometry.coordinatesequence.CSBuilder#getOrdinate(int, int)
	 */
	public double getOrdinate(int ordinateIndex, int coordinateIndex) {
		Coordinate c = coordinateArray[coordinateIndex];
		switch(ordinateIndex) {
			case 0: return c.x;
			case 1: return c.y;
			case 2: return c.z;
			default: return 0.0;
		}
	}

	/**
	 * @see org.geotools.geometry.coordinatesequence.CSBuilder#getSize()
	 */
	public int getSize() {
		if(coordinateArray != null) {
			return coordinateArray.length;
		} else {
			return -1;
		}
	}

	/**
	 * @see org.geotools.geometry.coordinatesequence.CSBuilder#getDimension()
	 */
	public int getDimension() {
		if(coordinateArray != null) {
			return 3;
		} else {
			return -1;
		}
	}

	/**
	 * @see org.geotools.geometry.coordinatesequence.CSBuilder#setOrdinate(com.vividsolutions.jts.geom.CoordinateSequence, double, int, int)
	 */
	public void setOrdinate(CoordinateSequence sequence, double value, int ordinateIndex, int coordinateIndex) {
		Coordinate c = sequence.getCoordinate(coordinateIndex);
		switch(ordinateIndex) {
			case 0: c.x = value;
			case 1: c.y = value;
			case 2: c.z = value;
		}
		
	}

}
