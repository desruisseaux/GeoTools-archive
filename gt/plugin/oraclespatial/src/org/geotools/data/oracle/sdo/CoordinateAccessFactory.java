/*
 * Created on Oct 29, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.geotools.data.oracle.sdo;

import com.vividsolutions.jts.geom.CoordinateSequenceFactory;

/**
 * Extends CoordianteSequenceFactory with meta data information.
 * <p>
 * This allows us to determine the dimensions of a Geometry.
 * </p>
 * @author jgarnett
 * @source $URL$
 */
public interface CoordinateAccessFactory extends CoordinateSequenceFactory {
    /**
     * Create method that allows additional content.
     * <p>
     * Example: (x,y,z,t) getDimension()==2, getNumAttributes()==2
     * </p> 
     * <pre><code>
     * <b>xyz</b>:[ [ x1, x2,...,xN], [ y1, y2,...,yN] ]
     * <b>attributes</b>:[ [ z1, z2,...,zN], [ t1, t2,..., tN] ]
     * </code></pre>
     * @param ordinates column major ordinate arrays where xyz.length == getDimension()
     * @param can be null. Column major measure arrays where attributes.length == getNumAttributes()
     */    
    public CoordinateAccess create(double[] xyz[], Object[] attributes );

    /** Number of spatial ordinates() */
    public int getDimension();
    
    /** Number of non spatial ordinates() */
    public int getNumAttributes();
}
