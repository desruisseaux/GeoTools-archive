/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data.vpf.io;

/**
 * Class Coordinate2DDouble.java is responsible for
 * 
 * <p>
 * Created: Thu Jan 30 08:31:38 2003
 * </p>
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version 1.0.0
 * @deprecated replaced by JTS classes
 */
public class Coordinate2DDouble extends CoordinateDouble {
    
    static final int dimensionality = 2;
    
    /**
     * Creates a new Coordinate2DDouble object.
     *
     * @param coords DOCUMENT ME!
     */
    public Coordinate2DDouble(double[][] coords) {
        super(coords);
    }
    
    public int getDimensionality() {
        return dimensionality;
    }
    
}

// Coordinate2DDouble
