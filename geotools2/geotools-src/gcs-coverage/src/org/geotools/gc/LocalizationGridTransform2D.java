/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2002, Institut de Recherche pour le D�veloppement
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
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assist�e par Satellite
 *             Institut de Recherche pour le D�veloppement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.gc;

// J2SE dependencies
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

// Geotools dependencies
import org.geotools.pt.Matrix;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.TransformException;
import org.geotools.ct.AbstractMathTransform;


/**
 * Transform a set of coordinate points using a grid of localization.
 * Input coordinates are index in this two-dimensional array.
 * Those input coordinates (or index) should be in the range
 *
 * <code>x</sub>input</sub>&nbsp;=&nbsp;[0..width-1]</code> and
 * <code>y</sub>input</sub>&nbsp;=&nbsp;[0..height-1]</code> inclusive,
 *
 * where <code>width</code> and <code>height</code> are the number of columns and
 * rows in the grid of localization. Output coordinates are the values stored in
 * the grid of localization at the specified index. If input coordinates (index)
 * are non-integer values, then output coordinates are interpolated using a bilinear
 * interpolation. If input coordinates are outside the grid range, then output
 * coordinates are extrapolated.
 *
 * @version $Id: LocalizationGridTransform2D.java,v 1.3 2002/08/06 14:19:41 desruisseaux Exp $
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
final class LocalizationGridTransform2D extends AbstractMathTransform implements MathTransform2D {
    /**
     * <var>x</var> (usually longitude) offset relative to an entry.
     * Points are stored in {@link #grid} as <code>(x,y)</code> pairs.
     */
    static final int X_OFFSET = 0;

    /**
     * <var>y</var> (usually latitude) offset relative to an entry.
     * Points are stored in {@link #grid} as <code>(x,y)</code> pairs.
     */
    static final int Y_OFFSET = 1;

    /**
     * Length of an entry in the {@link #grid} array. This lenght
     * is equals to the dimension of output coordinate points.
     */
    static final int CP_LENGTH = 2;

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
     * A global affine transform for the whole grid.
     */
    private final AffineTransform global;
    
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
    protected LocalizationGridTransform2D(final int width, final int height, final double[] grid,
                                          final AffineTransform global) {
        this.width  = width;
        this.height = height;
        this.grid   = grid;
        this.global = global;
    }
    
    /**
     * Calcule l'indice d'un enregistrement dans la grille.
     *
     * @param  col  Coordonnee <var>x</var> du point.
     * @param  row  Coordonnee <var>y</var> du point.
     * @return l'indice de l'enregistrement ou du point dans la matrice.
     */
    private int computeOffset(final int col, final int row) {
        return (col + row * width) * CP_LENGTH;
    }

    /**
     * Returns the dimension of input points.
     */    
    public int getDimSource() {
        return 2;
    }
    
    /**
     * Returns the dimension of output points.
     */    
    public int getDimTarget() {
        return 2;
    }
    
    /**
     * Tests if this transform is the identity transform.
     */    
    public boolean isIdentity() {
        return false;
    }

    /**
     * Gets the derivative of this transform at a point.
     */
    public Matrix derivative(final Point2D point) {
        final AffineTransform tr = new AffineTransform();
        getAffineTransform((int)point.getX(), (int)point.getY(), tr);
        final Matrix matrix = new Matrix(2,2);
        matrix.setElement(0,0, tr.getScaleX());
        matrix.setElement(1,1, tr.getScaleY());
        matrix.setElement(0,1, tr.getShearX());
        matrix.setElement(1,0, tr.getShearY());
        return matrix;
    }
       
    /** 
     * Transforme des coordonn�es sources (g�n�ralement des index de pixels) en coordonn�es
     * destinations (g�n�ralement des degr�s de longitude et latitude). Les transformations
     * feront intervenir des interpolations lin�aires si les coordonn�es sources ne sont pas
     * enti�res.
     *
     * @param  srcPts  Points d'entr�e.
     * @param  srcOff  Index du premier point d'entr�e � transformer.
     * @param  dstPts  Points de sortie.
     * @param  dstOff  Index du premier point de sortie.
     * @param  numPts  Nombre de points � transformer.
     */    
    public void transform(final double[] srcPts, int srcOff,
                          final double[] dstPts, int dstOff, int numPts)
    {
        final int minCol = 0;
        final int minRow = 0;
        final int maxCol = width  - 2;
        final int maxRow = height - 2;
        int postIncrement = 0;
        if (srcPts == dstPts && srcOff < dstOff) {
            srcOff += (numPts-1)*2;
            dstOff += (numPts-1)*2;
            postIncrement = -4;
        }
        while (--numPts >= 0) {
            final double xi = srcPts[srcOff++];
            final double yi = srcPts[srcOff++];
            final int  col = Math.max(Math.min((int)xi, maxCol), minCol);
            final int  row = Math.max(Math.min((int)yi, maxRow), minRow);
            final int offset00 = computeOffset(col, row);
            final int offset01 = offset00 + CP_LENGTH*width; // Une ligne plus bas
            final int offset10 = offset00 + CP_LENGTH;  // Une colonne � droite
            final int offset11 = offset01 + CP_LENGTH;  // Une colonne � droite, une ligne plus bas
            /*
             * Interpole les coordonn�es de destination        [00]--.(x0,y0)----[10]
             * sur la ligne courante (x0,y0)  ainsi que         |                  |
             * sur la ligne suivante (x1,y1).   Exemple         |    .(x,y)        |
             * ci-contre:  les coordonn�es sources sont         |                  |
             * entre crochets, et les coordonn�es de la        [01]--.(x1,y1)----[11]
             * sortie (� calculer) sont entre parenth�ses.
             */
            final double x0 = linearInterpolation(col+0, grid[offset00 + X_OFFSET],
                                                  col+1, grid[offset10 + X_OFFSET], xi);
            final double y0 = linearInterpolation(col+0, grid[offset00 + Y_OFFSET],
                                                  col+1, grid[offset10 + Y_OFFSET], xi);
            final double x1 = linearInterpolation(col+0, grid[offset01 + X_OFFSET],
                                                  col+1, grid[offset11 + X_OFFSET], xi);
            final double y1 = linearInterpolation(col+0, grid[offset01 + Y_OFFSET],
                                                  col+1, grid[offset11 + Y_OFFSET], xi);
            /*
             * Interpole maintenant les coordonn�es (x,y) entre les deux lignes.
             */
            dstPts[dstOff++] = linearInterpolation(row, x0, row+1, x1, yi);
            dstPts[dstOff++] = linearInterpolation(row, y0, row+1, y1, yi);
            srcOff += postIncrement;
            dstOff += postIncrement;
            if (false) {
                final java.io.PrintStream out = System.out;
                out.print("TD  ==> xi : ");    out.print  (xi);
                out.print(     " / yi : ");    out.print  (yi);
                out.print("  --->  xo : "); out.print  (dstPts[dstOff-2]);
                out.print(     " / yo : "); out.println(dstPts[dstOff-1]);
            }
        }
    }

    /**
     * Interpole/extrapole entre deux points.
     *
     * @param   x1  Coordonnee <var>x</var> du premier point.
     * @param   y1  Coordonnee <var>y</var> du premier point.
     * @param   x2  Coordonnee <var>x</var> du second point.
     * @param   y2  Coordonnee <var>y</var> du second point.
     * @param   x   Position <var>x</var> � laquelle calculer la valeur de <var>y</var>.
     * @return      La valeur <var>y</var> interpol�e entre les deux points.
     */
    private static double linearInterpolation(final double x1, final double y1,
                                              final double x2, final double y2, final double x)
    {
        return y1 + (y2-y1)/(x2-x1) * (x-x1);
    }

    /**
     * Retourne une approximation de la transformation affine � la position indiqu�e.
     *
     * @param  col  Coordonnee <var>x</var> du point.
     * @param  row  Coordonnee <var>y</var> du point.
     * @param dest  Matrice dans laquelle �crire la transformation affine.
     */
    private void getAffineTransform(int col, int row, final AffineTransform dest) {
        if (col > width -2) col = width -2;
        if (row > height-2) row = height-2;
        if (col < 0)        col = 0;
        if (row < 0)        row = 0;
        /*
         * Le calcul de la transformation affine comprend 6          P--------Pcol
         * inconnues. Sa solution recquiert donc 6 �quations.        |        |
         * Nous les obtenons en utilisant 3 points,  chaque          |        |
         * points ayant 2 coordonn�es. Illustration � droite:        Prow-----(ignored)
         */
        final int offset00 = computeOffset(col, row);
        final int offset01 = offset00 + CP_LENGTH*width;  // Une ligne plus bas
        final int offset10 = offset00 + CP_LENGTH;         // Une colonne � droite
        final double x     = grid[offset00 + X_OFFSET];
        final double y     = grid[offset00 + Y_OFFSET];
        final double dxCol = grid[offset10 + X_OFFSET] - x;
        final double dyCol = grid[offset10 + Y_OFFSET] - y;
        final double dxRow = grid[offset01 + X_OFFSET] - x;
        final double dyRow = grid[offset01 + Y_OFFSET] - y;
        dest.setTransform(dxCol, dyCol, dxRow, dyRow,
                          x - dxCol*col - dxRow*row,
                          y - dyCol*col - dyRow*row);
        /*
         * Si l'on transforme les 3 points qui ont servit � d�terminer la transformation
         * affine, on devrait obtenir un r�sultat identique (aux erreurs d'arrondissement
         * pr�s) peu importe que l'on utilise la transformation affine ou la grille de
         * localisation.
         */
        assert distance(new Point(col,   row  ), dest) < 1E-5;
        assert distance(new Point(col+1, row  ), dest) < 1E-5;
        assert distance(new Point(col,   row+1), dest) < 1E-5;
    }

    /**
     * Transform a point using the localization grid, transform it back using the inverse
     * of the specified affine transform, and returns the distance between the source and
     * the resulting point. This is used for assertions only.
     *
     * @param  index The source point to test.
     * @param  tr The affine transform to test.
     * @return The distance in grid coordinate. Should be close to 0.
     */
    private double distance(final Point2D index, final AffineTransform tr) {
        try {
            Point2D geoCoord = transform(index, null);
            geoCoord = tr.inverseTransform(geoCoord, geoCoord);
            return geoCoord.distance(index);
        } catch (TransformException exception) {
            // Should not happen
            throw new AssertionError(exception);
        } catch (NoninvertibleTransformException exception) {
            // Not impossible. What should we do? Open question...
            throw new AssertionError(exception);
        }
    }

    /** 
     * Retourne la transformation inverse.
     */
//    public MathTransform inverse() {
//        return new Inverse();
//    }
}
