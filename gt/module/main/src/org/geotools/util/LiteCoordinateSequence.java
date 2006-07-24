/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence;

/**
 * @TODO class description
 * 
 * @author jeichar
 * @since 2.1.x
 * @source $URL: http://svn.geotools.org/geotools/branches/2.2.x/module/render/src/org/geotools/renderer/lite/LiteCoordinateSequence.java $
 */
public class LiteCoordinateSequence extends PackedCoordinateSequence{

    /**
     * The packed coordinate array
     */
    private double[] coords;

    /**
     * Builds a new packed coordinate sequence
     *
     * @param coords
     * @param dimensions
     */
    public LiteCoordinateSequence(double[] coords) {
    	this.dimension=2;
      if (coords.length % dimension != 0) {
        throw new IllegalArgumentException("Packed array does not contain "
            + "an integral number of coordinates");
      }
      this.coords = coords;
    }

    /**
     * Builds a new packed coordinate sequence out of a float coordinate array
     *
     * @param coordinates
     */
    public LiteCoordinateSequence(float[] coordinates) {
      this.coords = new double[coordinates.length];
  	this.dimension=2;
      for (int i = 0; i < coordinates.length; i++) {
        this.coords[i] = coordinates[i];
      }
    }

    /**
     * Builds a new packed coordinate sequence out of a coordinate array
     *
     * @param coordinates
     */
    public LiteCoordinateSequence(Coordinate[] coordinates) {
      if (coordinates == null)
        coordinates = new Coordinate[0];
  	this.dimension=2;

      coords = new double[coordinates.length * this.dimension];
      for (int i = 0; i < coordinates.length; i++) {
        coords[i * this.dimension] = coordinates[i].x;
        if (this.dimension >= 2)
          coords[i * this.dimension + 1] = coordinates[i].y;
      }
    }

    /**
     * Builds a new empty packed coordinate sequence of a given size and dimension
     * @param dimension
     *
     * @param coordinates
     */
    public LiteCoordinateSequence(int size, int dimension) {
    	if( dimension!=2 )
    		throw new IllegalArgumentException("This type of sequence is always 2 dimensional");
    	this.dimension=2;
    	coords = new double[size * this.dimension];
    }

    /**
     * @see com.vividsolutions.jts.geom.CoordinateSequence#getCoordinate(int)
     */
    public Coordinate getCoordinateInternal(int i) {
      double x = coords[i * dimension];
      double y = coords[i * dimension + 1];
      double z = dimension == 2 ? 0.0 : coords[i * dimension + 2];
      return new Coordinate(x, y, z);
    }

    /**
     * @see com.vividsolutions.jts.geom.CoordinateSequence#size()
     */
    public int size() {
   		return coords.length/dimension;
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
      double[] clone = new double[coords.length];
      System.arraycopy(coords, 0, clone, 0, coords.length);
      return new LiteCoordinateSequence(clone);
    }

    /**
     * @see com.vividsolutions.jts.geom.CoordinateSequence#getOrdinate(int, int)
     *      Beware, for performace reasons the ordinate index is not checked, if
     *      it's over dimensions you may not get an exception but a meaningless
     *      value.
     */
    public double getOrdinate(int index, int ordinate) {
      return coords[index * dimension + ordinate];
    }

    /**
     * @see com.vividsolutions.jts.geom.PackedCoordinateSequence#setOrdinate(int,
     *      int, double)
     */
    public void setOrdinate(int index, int ordinate, double value) {
      coordRef = null;
      coords[index * dimension + ordinate] = value;
    }

    public Envelope expandEnvelope(Envelope env)
    {
      for (int i = 0; i < coords.length; i += dimension ) {
        env.expandToInclude(coords[i], coords[i + 1]);
      }
      return env;
    }

	/**
	 * @return
	 */
	public double[] getArray() {
		return coords;
	}

	/**
	 * @param coords2
	 */
	public void setArray(double[] coords2) {
		coords=coords2;
	}
	
	/**
	 *  if this is a dimension=2 seq, then this is the same as getArray().
	 *  If its >2 dims this will make a new array with dim=2
	 * @return
	 */
	public double[] getXYArray()
	{
		if (dimension==2)  //this is always true
			return coords;
		// this should never run, but its here for the future...
		int n = size();
		double[] result = new double[n*2];
		for (int t=0;t<n;t++)
		{
			result[t*2] = getOrdinate(t,0);
			result[t*2+1] = getOrdinate(t,1);
		}
		return result;
	}
	
}
