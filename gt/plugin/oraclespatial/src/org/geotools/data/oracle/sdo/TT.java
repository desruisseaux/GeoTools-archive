/*
 * Created on Oct 31, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.geotools.data.oracle.sdo;

/**
 * @author bowens
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
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
