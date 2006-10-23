/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2004, Refractions Research Inc.
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
 */
 package org.geotools.geometry;

import java.awt.geom.AffineTransform;

import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import junit.framework.TestCase;

/**
 *  summary sentence.
 * <p>
 * Paragraph ...
 * </p><p>
 * Responsibilities:
 * <ul>
 * <li>
 * <li>
 * </ul>
 * </p><p>
 * Example:<pre><code>
 * JTSTest x = new JTSTest( ... );
 * TODO code example
 * </code></pre>
 * </p>
 * @author jeichar
 * @since 0.6.0
 *
 * @deprecated This class moved to {@link org.geotools.geometry.jts} package.
 * @source $URL$
 */
public class JTSTest extends TestCase {

    private static final double DIFF = 0.000001;

    /*
     * Class under test for Envelope transform(Envelope, MathTransform)
     */
    public void testTransformEnvelopeMathTransform() throws Exception{
        Envelope env=new Envelope( 0,10,0,10);
        AffineTransform at=AffineTransform.getScaleInstance(.5,1);
        MathTransform2D t=(MathTransform2D) FactoryFinder.getMathTransformFactory(null)
                                            .createAffineTransform(new GeneralMatrix(at));
        env=JTS.transform(env, t);
        assertEquals( new Envelope(0, 5, 0, 10), env);
        env=JTS.transform(env, t.inverse());
        assertEquals( new Envelope(0, 10, 0, 10), env);
        
        CoordinateReferenceSystem crs=FactoryFinder.getCRSFactory(null).createFromWKT(
                "PROJCS[\"NAD_1983_UTM_Zone_10N\",GEOGCS[\"GCS_North_American_1983\",DATUM[\"D_North_American_1983\",TOWGS84[0,0,0,0,0,0,0],SPHEROID[\"GRS_1980\",6378137,298.257222101]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"False_Easting\",500000],PARAMETER[\"False_Northing\",0],PARAMETER[\"Central_Meridian\",-123],PARAMETER[\"Scale_Factor\",0.9996],PARAMETER[\"Latitude_Of_Origin\",0],UNIT[\"Meter\",1]]");
        t=(MathTransform2D) FactoryFinder.getCoordinateOperationFactory(null).createOperation(
                DefaultGeographicCRS.WGS84,crs).getMathTransform();
        env=new Envelope(-123,-133, 55, 60);
        env=JTS.transform(env, t);
        env=JTS.transform(env, t.inverse());
        assertTrue(Math.abs(-133-env.getMinX())<DIFF);
        assertTrue(Math.abs(-123-env.getMaxX())<DIFF);
        assertTrue(Math.abs(55-env.getMinY())<DIFF);
        assertTrue(Math.abs(60-env.getMaxY())<DIFF);
    }
    
    public void testTransformCoordinate() throws Exception{
        Coordinate coord=new Coordinate( 10,10);
        AffineTransform at=AffineTransform.getScaleInstance(.5,1);
        MathTransform2D t=(MathTransform2D) FactoryFinder.getMathTransformFactory(null).createAffineTransform(new GeneralMatrix(at));
        coord=JTS.transform(coord, coord, t);
        assertEquals( new Coordinate( 5, 10), coord);
        coord=JTS.transform(coord, coord, t.inverse());
        assertEquals( new Coordinate( 10, 10 ), coord);
        
        CoordinateReferenceSystem crs=FactoryFinder.getCRSFactory(null).createFromWKT(
                "PROJCS[\"NAD_1983_UTM_Zone_10N\",GEOGCS[\"GCS_North_American_1983\",DATUM[\"D_North_American_1983\",TOWGS84[0,0,0,0,0,0,0],SPHEROID[\"GRS_1980\",6378137,298.257222101]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"False_Easting\",500000],PARAMETER[\"False_Northing\",0],PARAMETER[\"Central_Meridian\",-123],PARAMETER[\"Scale_Factor\",0.9996],PARAMETER[\"Latitude_Of_Origin\",0],UNIT[\"Meter\",1]]");
        t=(MathTransform2D) FactoryFinder.getCoordinateOperationFactory(null).createOperation(
                DefaultGeographicCRS.WGS84,crs).getMathTransform();
        coord=new Coordinate(-123,55);
        coord=JTS.transform(coord,coord,  t);
        coord=JTS.transform(coord,coord,  t.inverse());
        assertTrue(Math.abs(-123-coord.x)<DIFF);
        assertTrue(Math.abs(55-coord.y)<DIFF);
    }

}
