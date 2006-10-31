/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2003  Refractions Research Inc.
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    Refractions Research Inc. Can be found on the web at:
 *    http://www.refractions.net/
 *
 *    Created on Oct 31, 2003
 */
package org.geotools.data.oracle.sdo;

/**
 * @author bowens
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 * @source $URL$
 */
public interface TT 
{
	/** <code>TT</code> code representing unknown geometies (like splines) */
	public static final int UNKNOWN       = 00;

	/** <code>TT</code> code representing Point */
	public static final int POINT         = 01;

	/** <code>TT</code> code representing Line (or Curve) */
	public static final int LINE          = 02;

	/** <code>TT</code> code representing Curve (or Line) */
	public static final int CURVE         = 02;    
    
	/** <code>TT</code> code representing Polygon */
	public static final int POLYGON       = 03;

	/** <code>TT</code> code representing Collection */
	public static final int COLLECTION    = 04;   

	/** <code>TT</code> code representing Multpoint */
	public static final int MULTIPOINT    = 05;       

	/** <code>TT</code> code representing Multiline (or Multicurve) */
	public static final int MULTILINE     = 06;

	/** <code>TT</code> code representing Multicurve (or Multiline) */    
	public static final int MULTICURVE    = 06;

	/** <code>TT</code> code representing MULTIPOLYGON */
	public static final int MULTIPOLYGON  = 07;

}
