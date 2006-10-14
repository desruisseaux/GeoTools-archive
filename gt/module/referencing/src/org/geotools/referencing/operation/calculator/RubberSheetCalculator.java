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
package org.geotools.referencing.operation.calculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.operation.calculator.algorithm.MapTriangulationFactory;
import org.geotools.referencing.operation.calculator.algorithm.MappedPosition;
import org.geotools.referencing.operation.calculator.algorithm.Quadrilateral;
import org.geotools.referencing.operation.calculator.algorithm.RubberSheetTransform;
import org.geotools.referencing.operation.calculator.algorithm.TINTriangle;
import org.geotools.referencing.operation.calculator.algorithm.TriangulationException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;


/**
 * Tha class for calculating the RubberSheet transformation. The
 * explanation of RubberSheet transformation can be seen <a href =
 * "http://planner.t.u-tokyo.ac.jp/member/fuse/rubber_sheeting.pdf">here</a>.
 *
 * @author Jan Jezek
 */
public class RubberSheetCalculator extends AbstractParamCalculator {
    /** trianglesMap Map of the original and destination triangles. */
    private HashMap trianglesMap;

    /**
     * trianglesToKeysMap Map of a original triangles and associated
     * AffineTransformation Objects.
     */
    private HashMap trianglesToKeysMap;
    
    /**
     * Creates the transformation from specified pairs of points and
     * quadrilateral that deffines the area of transformation.
     * 
     * @param ptSrc
     *            Set of source points
     * @param ptDst
     *            Set of destination points
     * @throws CalculationException 
     * @throws TriangulationException
     */
    public RubberSheetCalculator(DirectPosition[] ptSrc,
        DirectPosition[] ptDst, Quadrilateral quad)
        throws CalculationException, TriangulationException, CRSException {
        this.ptDst = ptDst;
        this.ptSrc = ptSrc;

        super.checkPoints(0, 2);
        checkQuad(quad);

        MappedPosition[] vectors = new MappedPosition[ptSrc.length];
        List vectorlist = new ArrayList();

        for (int i = 0; i < ptSrc.length; i++) {
            vectors[i] = new MappedPosition(ptSrc[i], ptDst[i]);
            vectorlist.add(vectors[i]);
        }

        Quadrilateral mQuad = mappedQuad(quad, vectorlist);

        MapTriangulationFactory trianglemap = new MapTriangulationFactory(mQuad,
                vectors);
        this.trianglesMap = (HashMap) trianglemap.getTriangleMap();
        this.trianglesToKeysMap = mapTrianglesToKey();
    }

    /**
     * Checks the Coordinate Reference System of the quad.
     *
     * @param quad to be tested
     *
     * @throws CRSException
     */
    private void checkQuad(Quadrilateral quad) throws CRSException {
        CoordinateReferenceSystem crs = ptSrc[0].getCoordinateReferenceSystem();

        if ((quad.p0.getCoordinateReferenceSystem() != crs)
                || (quad.p1.getCoordinateReferenceSystem() != crs)
                || (quad.p2.getCoordinateReferenceSystem() != crs)
                || (quad.p3.getCoordinateReferenceSystem() != crs)) {
            throw new CRSException(
                "Wrong Coordinate Reference System of the quad.");
        }
    }

    /**
     * Returns the map of source and destination triangles.
     *
     * @return The Map of source and destination triangles.
     */
    public HashMap getMapTriangulation() {
        return trianglesMap;
    }

    /**
     * Calculates affine transformation prameters from the pair of
     * triagles.
     *
     * @return The HashMap where the keys are the original triangles and values
     *         are AffineTransformation Objects.
     */
    private HashMap mapTrianglesToKey() {
        AffineParamCalculator calculator;

        // CoordinateList ptlSrc = new CoordinateList();
        // CoordinateList ptlDst = new CoordinateList();
        HashMap trianglesToKeysMap = (HashMap) trianglesMap.clone();

        Iterator it = trianglesToKeysMap.entrySet().iterator();

        while (it.hasNext()) {
            /*
             * ptlSrc.clear(); ptlDst.clear();
             */
            Map.Entry a = (Map.Entry) it.next();

            try {
                calculator = new AffineParamCalculator(((TINTriangle) a.getKey())
                        .getPoints(), ((TINTriangle) a.getValue()).getPoints());
                a.setValue(calculator.getMathTransform());
            } catch (Exception e) {
                // the numeber of vertices of two triangles is the same. So we
                // can ignore...
            }
        }

        return trianglesToKeysMap;
    }

    /**
     * Generates mapped quad from destination quad and source quad. The
     * new vertices of quad are calculated from source quad and difference of
     * nearest pair of identical points.
     *
     * @param sourceQuad the quad that defines the area for triangulating.
     * @param vectors of identical points (MappedCoordinates).
     *
     * @return destination quad
     */
    private Quadrilateral mappedQuad(Quadrilateral sourceQuad, List vectors) {
        if (vectors.isEmpty()) {
            return (Quadrilateral) sourceQuad.clone();
        }

        MappedPosition[] mappedVertices = new MappedPosition[4];

        for (int i = 0; i < mappedVertices.length; i++) {
            mappedVertices[i] = generateCoordFromNearestOne(sourceQuad.getPoints()[i],
                    vectors);
        }

        return new Quadrilateral(mappedVertices[0], mappedVertices[1],
            mappedVertices[2], mappedVertices[3]);
    }

    /**
     * Returns the new Coordinate from the nearest one Mapped
     * Coordinate.
     *
     * @param x the original coordinate.
     * @param vertices List of the MappedPosition.
     *
     * @return MappedPosition from the original and new coordinate, so the
     *         differnce between them is the same as for the nearest one
     *         MappedPosition.
     */
    protected MappedPosition generateCoordFromNearestOne(DirectPosition x,
        List vertices) {
        MappedPosition nearestOne = nearestMappedCoordinate(x, vertices);

        double dstX = x.getCoordinates()[0]
            + (nearestOne.getMappedposition().getCoordinates()[0]
            - nearestOne.getCoordinates()[0]);
        double dstY = x.getCoordinates()[1]
            + (nearestOne.getMappedposition().getCoordinates()[1]
            - nearestOne.getCoordinates()[1]);
        DirectPosition dst = new DirectPosition2D(nearestOne
                .getCoordinateReferenceSystem(), dstX, dstY);

        return new MappedPosition(x, dst);
    }

    /**
     * Returns the nearest MappedPosition to specified point P.
     *
     * @param dp P point.
     * @param vertices the List of MappedCoordinates.
     *
     * @return the MappedPosition to the x Coordinate.
     */
    protected MappedPosition nearestMappedCoordinate(DirectPosition dp,
        List vertices) {
        DirectPosition2D x = new DirectPosition2D(dp);

        // Assert.isTrue(vectors.size() > 0);
        MappedPosition nearestOne = (MappedPosition) vertices.get(0);

        for (Iterator i = vertices.iterator(); i.hasNext();) {
            MappedPosition candidate = (MappedPosition) i.next();

            if (candidate.toPoint2D().distance(x.toPoint2D()) < nearestOne.toPoint2D()
                                                                              .distance(x
                        .toPoint2D())) {
                nearestOne = candidate;
            }
        }

        return nearestOne;
    }

    /**
     * Returns MathTransform transformation setup as RubberSheet, that
     * transforms the {@link #ptSrc} into the {@link #ptDst} with zero deltas
     * on these points
     *
     * @return calculated MathTransform
     *
     * @throws TransformException when the size of source and destination point
     *         is not the same.
     */
    public MathTransform getMathTransform() throws TransformException {
        return new RubberSheetTransform(trianglesToKeysMap);
    }
}
