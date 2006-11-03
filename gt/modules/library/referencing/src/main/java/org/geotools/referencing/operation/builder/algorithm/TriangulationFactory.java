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

import org.opengis.spatialschema.geometry.DirectPosition;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * The class for generating TIN with respect to delaunay criterion. It
 * means that there are no alien vertices in  the circumcicle of each
 * triangle. The algorithm that is used is also known as incremental insetion.
 *
 * @author Jan Jezek
 */
public class TriangulationFactory {
    /** The list of TINTrianlgles of TIN. */
    private List triangles;

/**
 * Constructs the TriangulationFactory.
 * 
 *    @param quad of location to be triangulated.
 *    @param pt Array of points fot triangulation.
 *    @throws TriangulationException when the vertices are outside of the specified quad.  
 */
    protected TriangulationFactory(Quadrilateral quad, DirectPosition[] pt)
        throws TriangulationException {
        List vertices = new ArrayList();

        for (int i = 0; i < pt.length; i++) {
            vertices.add(pt[i]);
        }

        if (quad.containsAll(vertices) == false) {
            throw new TriangulationException("Point is outside triangles");
        }

        this.triangles = quad.getTriangles();

        for (Iterator i = vertices.iterator(); i.hasNext();) {
            DirectPosition vertex = (DirectPosition) i.next();
            insertPoint(vertex);
        }
    }

    /**
     * Set a List of points for triangulation
     *
     * @return TIN as list of triangles.
     */
    public List getTriangulation() {
        return triangles;
    }

    /**
     * This is the test loop, that starts to tests of triangles until
     * the result of insertation of triangle evokes changes in TIN.
     *
     * @param ChangedTriangles List of changed triangles
     *
     * @throws TriangulationException TriangulationException
     */
    protected void recursiveDelaunayTest(List ChangedTriangles)
        throws TriangulationException {
        int i = ChangedTriangles.size();

        while (i != 0) {
            triangles.removeAll(ChangedTriangles);
            ChangedTriangles = insertTriangles(ChangedTriangles);
            i = ChangedTriangles.size();
        }
    }

    /**
     * Decides whether to insert triangle directly or go throught
     * delaunay test.
     *
     * @param trian if triangles to be insterted
     *
     * @return List of changed triangles
     *
     * @throws TriangulationException TriangulationException
     */
    protected List insertTriangles(List trian) throws TriangulationException {
        List ChangedTriangles = new ArrayList();

        for (Iterator i = trian.iterator(); i.hasNext();) {
            TINTriangle trig = (TINTriangle) i.next();

            if (adjacentTriangles(trig, triangles).size() == 0) {
                // that is, a boundary triangle
                triangles.add(trig);
            } else {
                ChangedTriangles.addAll(delaunayCircleTest(trig));
            }
        }

        return ChangedTriangles;
    }

    /**
     * Tests wheteher there is a alian vertex in the circimcicle of
     * triangle. When there is, the diagonal of quad made by these triangles
     * changes.
     *
     * @param triangle to be tested
     *
     * @return List of changed triangles
     *
     * @throws TriangulationException DOCUMENT ME!
     */
    private List delaunayCircleTest(TINTriangle triangle)
        throws TriangulationException {
        List changedTriangles = new ArrayList();

        Iterator j = adjacentTriangles(triangle, triangles).iterator();
        int ct = changedTriangles.size();

        while (j.hasNext() && (changedTriangles.size() == ct)) {
            TINTriangle adjacent = (TINTriangle) j.next();

            List NewTriangles = new ArrayList();

            // The delaunay test
            if (triangle.getCircumCicle().contains(adjacent.p1)
                    || triangle.getCircumCicle().contains(adjacent.p0)
                    || triangle.getCircumCicle().contains(adjacent.p2)) {
                triangles.remove(triangle);
                triangles.remove(adjacent);

                NewTriangles.addAll(alternativeTriangles(triangle, adjacent));

                triangles.addAll(NewTriangles);
                changedTriangles = NewTriangles;
            } else if (!triangles.contains(triangle)) {
                triangles.add(triangle);
            }
        }

        return changedTriangles;
    }

    /**
     * Accomodate new vertex into the existing triangles.
     *
     * @param newVertex new vertex
     *
     * @throws TriangulationException DOCUMENT ME!
     */
    public void insertPoint(DirectPosition newVertex)
        throws TriangulationException {
        TINTriangle triangleContainingNewVertex = triangleContains(newVertex);

        if (triangleContainingNewVertex == null) {
            throw new TriangulationException("Point is outside triangles");
        }

        triangles.remove(triangleContainingNewVertex);
        recursiveDelaunayTest(triangleContainingNewVertex.subTriangles(
                newVertex));
    }

    /**
     * Returns the List of adjacent TINTriangles.
     *
     * @param triangle to be tested
     * @param triangles adjacent triangles of triangle
     *
     * @return the triangles adjacent to the given triangle
     *
     * @throws TriangulationException DOCUMENT ME!
     */
    private List adjacentTriangles(TINTriangle triangle, List triangles)
        throws TriangulationException {
        ArrayList adjacentTriangles = new ArrayList();

        for (Iterator i = triangles.iterator(); i.hasNext();) {
            TINTriangle candidate = (TINTriangle) i.next();
            int identicalVertices = 0;

            if (candidate.hasVertex(triangle.p0)) {
                identicalVertices++;
            }

            if (candidate.hasVertex(triangle.p1)) {
                identicalVertices++;
            }

            if (candidate.hasVertex(triangle.p2)) {
                identicalVertices++;
            }

            if (identicalVertices == 3) {
                throw new TriangulationException("Triangle already exists");
            }

            if (identicalVertices == 2) {
                adjacentTriangles.add(candidate);
            }
        }

        return adjacentTriangles;
    }

    /**
     * Method that changes the diagonal of the quad generated from two
     * adjacent triangles.
     *
     * @param ABC triangle sharing an edge with BCD
     * @param BCD
     *
     * @return triangles ABD and ADC, or null if ABCD is not convex
     *
     * @throws TriangulationException DOCUMENT ME!
     */
    private List alternativeTriangles(TINTriangle ABC, TINTriangle BCD)
        throws TriangulationException {
        Quadrilateral quad = quadFromTriangles(ABC, BCD);

        if (!quad.isConvex()) {
            return null;
        }

        return quad.getTriangles();
    }

    /**
     * Makes a quad from two triangles.
     *
     * @param ABC a triangle that shares an edge with BCD. The order of the
     *        Coordinates does not matter.
     * @param BCD
     *
     * @return a quadrilateral (four Coordinates) formed from the two triangles
     *
     * @throws TriangulationException TriangulationException
     */
    private Quadrilateral quadFromTriangles(TINTriangle ABC, TINTriangle BCD)
        throws TriangulationException {
        ArrayList ABCvertices = new ArrayList();
        ArrayList BCDvertices = new ArrayList();
        ABCvertices.add(ABC.p0);
        ABCvertices.add(ABC.p1);
        ABCvertices.add(ABC.p2);
        BCDvertices.add(BCD.p0);
        BCDvertices.add(BCD.p1);
        BCDvertices.add(BCD.p2);

        ArrayList sharedVertices = new ArrayList();
        ArrayList unsharedVertices = new ArrayList();

        for (Iterator i = ABCvertices.iterator(); i.hasNext();) {
            DirectPosition vertex = (DirectPosition) i.next();

            if (!BCDvertices.contains(vertex)) {
                unsharedVertices.add(vertex);
            } else if (BCDvertices.contains(vertex)) {
                sharedVertices.add(vertex);
                BCDvertices.remove(vertex);
            } else {
                throw new TriangulationException("should never reach here");
            }
        }

        unsharedVertices.addAll(BCDvertices);

        return new Quadrilateral((DirectPosition) unsharedVertices.get(0),
            (DirectPosition) sharedVertices.get(0),
            (DirectPosition) unsharedVertices.get(1),
            (DirectPosition) sharedVertices.get(1));
    }

    /**
     * Returns the TINTriangle that contains the p Coordinate.
     *
     * @param p The Coordinate to be tested
     *
     * @return the triangle containing p, or null if there is no triangle that
     *         contains p
     */
    private TINTriangle triangleContains(DirectPosition p) {
        for (Iterator i = triangles.iterator(); i.hasNext();) {
            TINTriangle triangle = (TINTriangle) i.next();

            if (triangle.containsOrIsVertex(p)) {
                return triangle;
            }
        }

        return null;
    }
}
