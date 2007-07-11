/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.referencing.operation.builder;

import java.awt.geom.Point2D;
import java.util.List;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.geotools.referencing.operation.transform.IdentityTransform;


public class TPSGridBuilder extends WarpGridBuilder {
    private GeneralMatrix Cx = null;
    private GeneralMatrix Cy = null;
    private GeneralMatrix K;
    private GeneralMatrix L;
    private GeneralMatrix O;
    private GeneralMatrix V;
    private GeneralMatrix P;
    private final int number = super.getMappedPositions().size();
    
    public TPSGridBuilder(List vectors, double dx, double dy, Envelope env) throws TransformException {
    	 
        this(vectors, dx, dy, env, IdentityTransform.create(2));
    }

    public TPSGridBuilder(List vectors, double dx, double dy, Envelope envelope,
        MathTransform realToGrid) throws TransformException {
    	
        super(vectors, dx, dy, envelope, realToGrid);

      
        L = new GeneralMatrix(number + 3, number + 3);
        
        fillKsubMatrix();
        fillPsubMatrix();
        fillOsubMatrix();
                
        
    }

    public float[] computeWarpGrid(ParameterValueGroup WarpParams) {
    	L.invert();
        GeneralMatrix V =  fillVMatrix(0);
        GeneralMatrix resultx = new GeneralMatrix(number+3,1);
        resultx.mul(L,V);
        
        V =  fillVMatrix(1);
        GeneralMatrix resulty = new GeneralMatrix(number+3,1);
        resulty.mul(L,V);
        
        float[] warpPositions = (float[]) WarpParams.parameter("warpPositions").getValue();
        
        for (int i = 0; i <= WarpParams.parameter("yNumCells").intValue(); i++) {
            for (int j = 0; j <= WarpParams.parameter("xNumCells").intValue(); j++) {
                 DirectPosition2D dp = new DirectPosition2D(WarpParams.parameter(
                 "xStart").intValue()
                 + (j * WarpParams.parameter("xStep").intValue()),
                 WarpParams.parameter("yStart").intValue()
                 + (i * WarpParams.parameter("yStep").intValue())); 
            	
            	  double x = -calculateTPSFunction(resultx, dp) + (j * WarpParams.parameter("xStep").intValue())
                  + WarpParams.parameter("xStart").intValue();
                  double y = -calculateTPSFunction(resulty, dp) + (i * WarpParams.parameter("yStep").intValue())
                  + WarpParams.parameter("yStart").intValue();

               
                warpPositions[(i * ((1 + WarpParams.parameter("xNumCells").intValue()) * 2))
                + (2 * j)] = (float) x;

                warpPositions[(i * ((1 + WarpParams.parameter("xNumCells").intValue()) * 2))
                + (2 * j) + 1] = (float) y;
                
              
            }
        }

        return warpPositions;
    }

    private double calculateTPSFunction(GeneralMatrix v, Point2D p){
    	double a1 = v.getElement(v.getNumRow()-3, 0);
    	double a2 = v.getElement(v.getNumRow()-2, 0);
    	double a3 = v.getElement(v.getNumRow()-1, 0);
    	
    	double result;
    	double sum = 0;
    	
    	for (int i = 0; i <  (v.getNumRow()-3); i++){
    		double dist = p.distance((Point2D)((MappedPosition)getMappedPositions().get(i)).getSource());
    		
    		sum = sum + v.getElement(i, 0) * functionU(dist);    		
    	}
    	    	
    	result = a1 + a2 * p.getX() + a3 * p.getY() + sum;
    	return result;
    }
    
    private double functionU(double r) {
        if (r == 0) {
            return 0;
        }

        return r * r * Math.log(r);
    }

    /**
     *
     * @param p_i
     * @param p_j
     * @return
     */
    private double calculateFunctionU(MappedPosition p_i, MappedPosition p_j) {
        double distance = ((Point2D) p_i.getSource()).distance((Point2D) p_j.getSource());

        return functionU(distance);
    }

    /**
     *
     * @param K
     * @return
     */
    private void fillKsubMatrix() {
    	double alfa = 0;
        for (int i = 0; i < number; i++) {
            for (int j = i + 1; j < number; j++) {
                double u = calculateFunctionU((MappedPosition) super.getMappedPositions().get(i),
                        (MappedPosition) super.getMappedPositions().get(j));
                L.setElement(i, j, u);
                L.setElement(j, i, u);
                alfa = alfa + u*2; // same for upper and lower part
            }
        }
        alfa = alfa / (number*number);
    }

    /**
     *
     * @param L
     * @return
     */
    private void fillPsubMatrix() {
        for (int i = 0; i < number; i++) {
        	
            L.setElement(i, i, 0);
        	
            DirectPosition source = ((MappedPosition) getMappedPositions().get(i)).getSource();

            L.setElement(i, number + 0, 1);
            L.setElement(i, number + 1, source.getCoordinates()[0]);
            L.setElement(i, number + 2, source.getCoordinates()[1]);

            L.setElement(number + 0, i, 1);
            L.setElement(number + 1, i, source.getCoordinates()[0]);
            L.setElement(number + 2, i, source.getCoordinates()[1]);
        }
    }

    private void fillOsubMatrix() {
        for (int i = number; i < (number + 3); i++) {
            for (int j = number; j < (number + 3); j++) {
                L.setElement(i, j, 0);
            }
        }
    }

    /**
     *
     * @param dim 0 for dx, 1 for dy.
     * @return
     */
    private GeneralMatrix fillVMatrix(int dim) {
    	
    	V = new GeneralMatrix(number + 3, 1);
    	
        for (int i = 0; i < number; i++) {
            MappedPosition mp = ((MappedPosition) getMappedPositions().get(i));
          //  V.setElement(i, 0, mp.getDelta(dim));
            V.setElement(i, 0, mp.getSource().getOrdinate(dim)-mp.getTarget().getOrdinate(dim));
        
        }
        V.setElement(V.getNumRow() - 3, 0, 0);
        V.setElement(V.getNumRow() - 2, 0, 0);
        V.setElement(V.getNumRow() - 1, 0, 0);

        return V;
    }

    protected void createCMatrix() {
        List mp = getMappedPositions();

        for (int i = 0; i < mp.size(); i++) {
            MappedPosition position = (MappedPosition) mp.get(i);
            Cx.setRow(i,
                new double[] {
                    position.getSource().getOrdinate(0), position.getSource().getOrdinate(1),
                   // position.getDelta(0)
                    position.getSource().getOrdinate(0)-position.getTarget().getOrdinate(0)
                });
            Cy.setRow(i,
                new double[] {
                    position.getSource().getOrdinate(0), position.getSource().getOrdinate(1),
                   // position.getDelta(1)
                    position.getSource().getOrdinate(1)-position.getTarget().getOrdinate(1)
                    
                });
        }
    }
}
