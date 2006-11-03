/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.referencing.operation.builder.algorithm;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.spatialschema.geometry.DirectPosition;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


public class TriangulationFactoryTest extends TestCase {
    
    /**
     * Run the suit from the command line.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    /**
     * Returns the test suite.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(TriangulationFactoryTest.class);

        return suite;
    }
    /**
     * Test (@link TringulationFactory).
     */
    public void testTringulationFactory() {
        DirectPosition sp1 = new DirectPosition2D(10, 10);
        DirectPosition tp1 = new DirectPosition2D(10, 10);

        DirectPosition sp2 = new DirectPosition2D(20, 10);
        DirectPosition tp2 = new DirectPosition2D(20, 10);

        DirectPosition sp3 = new DirectPosition2D(20, 20);
        DirectPosition tp3 = new DirectPosition2D(20, 20);

        DirectPosition sp4 = new DirectPosition2D(10, 20);
        DirectPosition tp4 = new DirectPosition2D(10, 20);

        DirectPosition sp5 = new DirectPosition2D(14, 16);
        DirectPosition tp5 = new DirectPosition2D(14, 16);

        MappedPosition mtp1 = new MappedPosition(sp1, tp1);
        MappedPosition mtp2 = new MappedPosition(sp2, tp2);
        MappedPosition mtp3 = new MappedPosition(sp3, tp3);
        MappedPosition mtp4 = new MappedPosition(sp4, tp4);
        MappedPosition mtp5 = new MappedPosition(sp5, tp5);
        DirectPosition[] vertices = new DirectPosition[1];
        vertices[0] = mtp5;

        Quadrilateral quad = new Quadrilateral(mtp1, mtp2, mtp3, mtp4);

        try {
            new TriangulationFactory(quad,
                    vertices);
        } catch (TriangulationException e) {
            System.out.println(e.getMessage());
        }
    }
    /**
     * Test (@link TringulationFactory). Triangles are tested with delaunay test.
     */
    public void testDelaunay() throws TriangulationException {
        // coordinates of quadrilateral for triangulation 
        DirectPosition2D leftDown = new DirectPosition2D(100, 100);

        DirectPosition2D rightDown = new DirectPosition2D(200, 100);

        DirectPosition2D rightTop = new DirectPosition2D(200, 250);

        DirectPosition2D leftTop = new DirectPosition2D(100, 250);

        // ArrayList vertices = new ArrayList();

        // generator for points within the quadrilateral:        
        Random randomCoord = new Random(872066443);

        // number of points
        int number = 5;
        DirectPosition[] vertices = new DirectPosition[number];

        for (int i = 0; i < number; i++) {
            double x = leftDown.x
                + (randomCoord.nextDouble() * (rightDown.x - leftDown.x));
            double y = leftDown.y
                + (randomCoord.nextDouble() * (leftTop.y - leftDown.y));
            vertices[i] = new DirectPosition2D(x, y);
        }

        Quadrilateral quad = new Quadrilateral(leftDown, rightDown, rightTop,
                leftTop);

        List triangles = new ArrayList();

        try {
            TriangulationFactory trigfac = new TriangulationFactory(quad,
                    vertices);
            triangles = trigfac.getTriangulation();
        } catch (TriangulationException e) {
            System.out.println(e.getMessage());
        }

        int j = 1;

        for (Iterator i = triangles.iterator(); i.hasNext();) {
            TINTriangle triangle = (TINTriangle) i.next();            

            for (j = 0; j < vertices.length; j++) {
                // Delunay Test - there are no vetrices in the CircumCicle
                assertFalse(triangle.getCircumCicle().contains(vertices[j]));
            }
        }
    }
}
