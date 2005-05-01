/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2002, Institut de Recherche pour le Développement
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.referencing.operation.transform;

// J2SE dependencies
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;

import javax.media.jai.WarpPolynomial;

import org.geotools.resources.Utilities;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;


/**
 * Non-Affine Transform of a set of coordinate points using a grid of localization
 * by a warp polynomial.
 * Input coordinates are index in this two-dimensional array.
 * Those input coordinates (or index) should be in the range
 *
 * <code>x</sub>input</sub>&nbsp;=&nbsp;[0..width-1]</code> and
 * <code>y</sub>input</sub>&nbsp;=&nbsp;[0..height-1]</code> inclusive,
 *
 * where <code>width</code> and <code>height</code> are the number of columns and
 * rows in the grid of localization. Output coordinates are the values stored in
 * the grid of localization at the specified index.
 *
 * @version $Id$
 * @author Alessio Fabiani
 */
final class LocalizationGridPolynomialTransform2D extends AbstractMathTransform
                                     implements MathTransform2D, Serializable
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1067560328828441289L;

    /**
     * <var>x</var> (usually longitude) offset relative to an entry.
     * Points are stored in {@link #grid} as <code>(x,y)</code> pairs.
     */
    private static final int X_OFFSET = LocalizationGridTransform2D.X_OFFSET;

    /**
     * <var>y</var> (usually latitude) offset relative to an entry.
     * Points are stored in {@link #grid} as <code>(x,y)</code> pairs.
     */
    private static final int Y_OFFSET = LocalizationGridTransform2D.Y_OFFSET;

    /**
     * Length of an entry in the {@link #grid} array. This lenght
     * is equals to the dimension of output coordinate points.
     */
    private static final int CP_LENGTH = LocalizationGridTransform2D.CP_LENGTH;

    /**
     * Number of grid's columns.
     */
    private final int width;
    
    /**
     * Number of grid's rows.
     */
    private final int height;
               
    /**
     * Grid of coordinate points.
     * Points are stored as <code>(x,y)</code> pairs.
     */
    private final double[] grid;

    /**
     * The warp polynomial.
     */
    private final WarpPolynomial pol;

    /**
     * The direct math transform.
     */
    private transient MathTransform transform;

    /**
     * The inverse math transform. Will be constructed only when first requested.
     */
    private transient MathTransform inverse;
    
    /**
     * Calcule l'indice d'un enregistrement dans la grille.
     *
     * @param  row  Coordonnee x du point.
     * @param  col  Coordonnee y du point.
     * @return l'indice de l'enregistrement ou du point dans la matrice.
     */
    private int computeOffset(final int col, final int row) {
        if (col<0 || col>=width) {
            throw new IndexOutOfBoundsException(String.valueOf(col));
        }
        if (row<0 || row>=height) {
            throw new IndexOutOfBoundsException(String.valueOf(row));
        }
        return (col + row * width) * CP_LENGTH;
    }
    
    /**
     * Construct a localization grid using the specified data.
     *
     * @param width  Number of grid's columns.
     * @param height Number of grid's rows.
     * @param grid   The localization grid as an array of <code>(x,y)</code> coordinates.
     *               This array is not cloned; this is the caller's responsability to ensure
     *               that it will not be modified as long as this transformation is strongly
     *               reachable.
     * @param global A global affine transform for the whole grid.
     */
    protected LocalizationGridPolynomialTransform2D(final int width, final int height, final double[] grid,
                                          final WarpPolynomial pol)
    {
        this.width  = width;
        this.height = height;
        this.grid   = grid;
        this.pol = pol;
    }

    /**
     * Returns the dimension of input points.
     */    
    public int getSourceDimensions() {
        return 2;
    }
    
    /**
     * Returns the dimension of output points.
     */    
    public int getTargetDimensions() {
        return 2;
    }
    
    /**
     * Tests if this transform is the identity transform.
     */    
    public boolean isIdentity() {
        return false;
    }

    /** 
     * Transforme des coordonnées sources (généralement des index de pixels) en coordonnées
     * destinations (généralement des degrés de longitude et latitude). Les transformations
     * feront intervenir des interpolations linéaires si les coordonnées sources ne sont pas
     * entières.
     *
     * @param  srcPts  Points d'entrée.
     * @param  srcOff  Index du premier point d'entrée à transformer.
     * @param  dstPts  Points de sortie.
     * @param  dstOff  Index du premier point de sortie.
     * @param  numPts  Nombre de points à transformer.
     */    
    public void transform(final float[] srcPts, int srcOff,
                          final float[] dstPts, int dstOff, int numPts)
    {
        transform(srcPts, null, srcOff, dstPts, null, dstOff, numPts);
    }

    /** 
     * Transforme des coordonnées sources (généralement des index de pixels) en coordonnées
     * destinations (généralement des degrés de longitude et latitude). Les transformations
     * feront intervenir des interpolations linéaires si les coordonnées sources ne sont pas
     * entières.
     *
     * @param  srcPts  Points d'entrée.
     * @param  srcOff  Index du premier point d'entrée à transformer.
     * @param  dstPts  Points de sortie.
     * @param  dstOff  Index du premier point de sortie.
     * @param  numPts  Nombre de points à transformer.
     */    
    public void transform(final double[] srcPts, int srcOff,
                          final double[] dstPts, int dstOff, int numPts)
    {
        transform(null, srcPts, srcOff, null, dstPts, dstOff, numPts);
    }

    /**
     * Implementation of direct transformation.
     */
    private void transform(final float[] srcPts1, final double[] srcPts2, int srcOff,
                           final float[] dstPts1, final double[] dstPts2, int dstOff, int numPts)
    {
        final int minCol = 0;
        final int minRow = 0;
        final int maxCol = width  - 2;
        final int maxRow = height - 2;
        int postIncrement = 0;
        if (srcOff < dstOff) {
            if ((srcPts2!=null) ? srcPts2==dstPts2 : srcPts1==dstPts1) {
                srcOff += (numPts-1)*2;
                dstOff += (numPts-1)*2;
                postIncrement = -4;
            }
        }
        
        int ptCnt = numPts;
        while (--ptCnt >= 0) {
            final double xi, yi;
            if (srcPts2 != null) {
            	xi = srcPts2[srcOff++];
            	yi = srcPts2[srcOff++];
            	
            	float[] dstCoords = new float[2];
            	this.pol.warpPoint((int) Math.round(Math.floor(xi)), (int) Math.round(Math.floor(yi)), dstCoords);
            	
            	dstPts2[dstOff++] = dstCoords[0];
            	dstPts2[dstOff++] = dstCoords[1];
            } else {
            	xi = srcPts1[srcOff++];
            	yi = srcPts1[srcOff++];
            	
            	float[] dstCoords = new float[2];
            	this.pol.warpPoint((int) Math.round(Math.floor(xi)), (int) Math.round(Math.floor(yi)), dstCoords);
            	
            	dstPts1[dstOff++] = dstCoords[0];
            	dstPts1[dstOff++] = dstCoords[1];
            }
        }
    }

    /** 
     * Returns the inverse transform.
     */
    public MathTransform inverse() {
        if (inverse == null) {
            inverse = new Inverse();
        }
        return inverse;
    }

    /**
     * The inverse transform. This inner class is
     * the inverse of the enclosing math transform.
     *
     * @version $Id$
     * @author Alessio Fabiani
     */
    private final class Inverse extends AbstractMathTransform.Inverse implements MathTransform2D,
                                                                                 Serializable
    {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = 4876426825123740967L;

        /**
         * The warp polynomial.
         */
        private final WarpPolynomial invPol;

        /**
         * Default constructor.
         */
        public Inverse() {
            LocalizationGridPolynomialTransform2D.this.super();
            this.invPol = this.fitPolynomial2DPlane();
        }

        /**
         * Transform a "real world" coordinate into a grid coordinate.
         */
        public void transform(final float[] srcPts, int srcOff,
        		final float[] dstPts, int dstOff, int numPts)
        {
        	transform(srcPts, null, srcOff, dstPts, null, dstOff, numPts);
        }
        
        public void transform(final double[] srcPts, int srcOff,
        		final double[] dstPts, int dstOff, int numPts)
        {
        	transform(null, srcPts, srcOff, null, dstPts, dstOff, numPts);
        }
        
        /**
         * Implementation of inverse transformation.
         */
        private void transform(final float[] srcPts1, final double[] srcPts2, int srcOff,
        		final float[] dstPts1, final double[] dstPts2, int dstOff, int numPts)
        {
        	
        	final int minCol = 0;
        	final int minRow = 0;
        	final int maxCol = width  - 2;
        	final int maxRow = height - 2;
        	int postIncrement = 0;
        	if (srcOff < dstOff) {
        		if ((srcPts2!=null) ? srcPts2==dstPts2 : srcPts1==dstPts1) {
        			srcOff += (numPts-1)*2;
        			dstOff += (numPts-1)*2;
        			postIncrement = -4;
        		}
        	}
        	
            int ptCnt = numPts;
            while (--ptCnt >= 0) {
                final double xi, yi;
                if (srcPts2 != null) {
                	xi = srcPts2[srcOff++];
                	yi = srcPts2[srcOff++];
                	
                	float[] dstCoords = new float[2];
                	this.invPol.warpPoint((int) Math.round(Math.floor(xi)), (int) Math.round(Math.floor(yi)), dstCoords);
                	
                	dstPts2[dstOff++] = dstCoords[0];
                	dstPts2[dstOff++] = dstCoords[1];
                } else {
                	xi = srcPts1[srcOff++];
                	yi = srcPts1[srcOff++];
                	
                	float[] dstCoords = new float[2];
                	this.invPol.warpPoint((int) Math.round(Math.floor(xi)), (int) Math.round(Math.floor(yi)), dstCoords);
                	
                	dstPts1[dstOff++] = dstCoords[0];
                	dstPts1[dstOff++] = dstCoords[1];
                }
            }
        }

        private WarpPolynomial fitPolynomial2DPlane() {
        	
        	final int polyDegree = 2;
        	final int numNeededPoints = (polyDegree + 1)*(polyDegree + 2)/2;
        	final int imageWidth = LocalizationGridPolynomialTransform2D.this.width;
        	final int imageHeight = LocalizationGridPolynomialTransform2D.this.height;
        	float[] destCoords = new float[2 * imageWidth * imageHeight];
        	float[] srcCoords = new float[2 * imageWidth * imageHeight];

        	double periodX = 0.0;
        	double periodY = 0.0;
        	double lonMax = - Double.MAX_VALUE;
        	double latMax = - Double.MAX_VALUE;
        	double lonMin = Double.MAX_VALUE;
        	double latMin = Double.MAX_VALUE;

        	for( int i = 0; i < LocalizationGridPolynomialTransform2D.this.grid.length; i+=2 ) {
        		lonMin = (LocalizationGridPolynomialTransform2D.this.grid[i] <= lonMin ? LocalizationGridPolynomialTransform2D.this.grid[i] : lonMin);
        		lonMax = (LocalizationGridPolynomialTransform2D.this.grid[i] >= lonMax ? LocalizationGridPolynomialTransform2D.this.grid[i] : lonMax);
        		latMin = (LocalizationGridPolynomialTransform2D.this.grid[i + 1] <= latMin ? LocalizationGridPolynomialTransform2D.this.grid[i + 1] : latMin);
        		latMax = (LocalizationGridPolynomialTransform2D.this.grid[i + 1] >= latMax ? LocalizationGridPolynomialTransform2D.this.grid[i + 1] : latMax);
        	}

        	periodX = (lonMax - lonMin) / imageWidth;
        	periodY = (latMax - latMin) / imageHeight;
        	
        	int counter = 0;
        	for (int yi=0; yi < imageHeight; yi++) {
        		for (int xi=0; xi < imageWidth; xi++) {
        			destCoords[counter] = xi;
        			srcCoords[counter] = (float) ((grid[LocalizationGridPolynomialTransform2D.this.computeOffset(xi,yi) + LocalizationGridPolynomialTransform2D.this.X_OFFSET] - lonMin) / periodX);
        			destCoords[counter+1] = yi;
        			srcCoords[counter+1] = (float) ((grid[LocalizationGridPolynomialTransform2D.this.computeOffset(xi,yi) + LocalizationGridPolynomialTransform2D.this.Y_OFFSET] - latMin) / periodY);
        			counter+=2;
        		}
        	}

            /*
             * Source Coordinates
             * Source Offset
             * Dest Coordinates
             * Dest Offset
             * Num Coords
             * PreScale x
             * PreScale y
             * PostScale x
             * PostScale y
             * Pol. Degree
             */
        	WarpPolynomial pol = 
        		WarpPolynomial.createWarp(
        				srcCoords, 0, 
						destCoords, 0, 
						srcCoords.length / 2, 
						1.0f / imageWidth, 1.0f / imageHeight, 
						(float) imageWidth, (float) imageHeight, 
						polyDegree
				);
        	
        	return pol;
        }
        
        /**
         * Restore reference to this object after deserialization.
         */
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            LocalizationGridPolynomialTransform2D.this.inverse = this;
        }
    }

    
    /**
     * Returns a hash value for this transform.
     */
    public int hashCode() {
        return super.hashCode() ^ this.pol.hashCode();
    }

    /**
     * Compare this transform with the specified object for equality.
     */
    public boolean equals(final Object object) {
        if (super.equals(object)) {
            final LocalizationGridPolynomialTransform2D that = (LocalizationGridPolynomialTransform2D) object;
            return this.width  == that.width   &&
                   this.height == that.height  &&
                   Utilities.equals(this.pol, that.pol) &&
                   Arrays   .equals(this.grid,   that.grid);
        }
        return false;
    }
}