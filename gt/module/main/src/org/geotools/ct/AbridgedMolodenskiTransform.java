/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.ct;

// J2SE and JAI dependencies
import java.io.Serializable;
import javax.media.jai.util.Range;
import javax.media.jai.ParameterList;

// OpenGIS dependencies
import org.opengis.referencing.operation.TransformException;

// Geotools dependencies
import org.geotools.cs.Ellipsoid;
import org.geotools.cs.HorizontalDatum;
import org.geotools.cs.WGS84ConversionInfo;

// Resources
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * The Abridged Molodensky transformation (EPSG code 9605) is a simplified version of the
 * {@link MolodenskiTransform} method. This transforms three dimensional 
 * geographic points from one geographic coordinate reference system to another
 * (a datum shift), using three shift parameters (delta X, delta Y, delta Z) and
 * the difference between the semi-major axis and flattenings of the two ellipsoids.
 * <br><br>
 *
 * Unlike the Bursa-Wolf 3 parameter method (which acts on geocentric coordinates),
 * this transformation can be performed directly on geographic coordinates.
 * <br><br>
 *
 * <strong>References:</strong><ul>
 *   <li> Defense Mapping Agency (DMA), Datums, Ellipsoids, Grids and Grid Reference Systems,
 *        Technical Manual 8358.1. 
 *        Available from <a href="http://earth-info.nga.mil/GandG/pubs.html">http://earth-info.nga.mil/GandG/pubs.html</a></li>
 *   <li> Defense Mapping Agency (DMA), The Universal Grids: Universal Transverse 
 *        Mercator (UTM) and Universal Polar Stereographic (UPS), Fairfax VA, Technical Manual 8358.2. 
 *        Available from <a href="http://earth-info.nga.mil/GandG/pubs.html">http://earth-info.nga.mil/GandG/pubs.html</a></li>
 *   <li> National Imagry and Mapping Agency (NIMA), Department of Defense World 
 *        Geodetic System 1984, Technical Report 8350.2. 
 *        Available from <a href="http://earth-info.nga.mil/GandG/pubs.html">http://earth-info.nga.mil/GandG/pubs.html</a></li>
 *   <li> "Coordinate Conversions and Transformations including Formulas",
 *        EPSG Guidence Note Number 7, Version 19.</li>
 * </ul>
 *
 * @version $Id$
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 *
 * @deprecated Replaced by {@link org.geotools.referencing.operation.transform.AbridgedMolodenskiTransform}.
 */
class AbridgedMolodenskiTransform extends AbstractMathTransform implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1759367353860977791L;

    /**
     * <code>true</code> for a 3D transformation, or
     * <code>false</code> for a 2D transformation.
     */
    private final boolean source3D, target3D;
    
    /**
     * X,Y,Z shift in meters.
     */
    private final double dx, dy, dz;
    
    /**
     * Semi-major (<var>a</var>) semi-minor (<var>b/<var>) radius in meters.
     */
    private final double a, b;
    
    /**
     * Difference in the semi-major (<code>da=target a - source a</code>) and semi-minor
     * (<code>db=target b - source b</code>) axes of the target and source ellipsoids.
     */
    private final double da, db;
    
    /**
     * The square of excentricity of the ellipsoid: e² = (a²-b²)/a² where
     * <var>a</var> is the semi-major axis length and
     * <var>b</var> is the semi-minor axis length.
     */
    private final double e2;
    
    /**
     * Defined as <code>(a*df) + (f*da)</code>.
     */
    private final double adf;
    
    /**
     * Construct an AbridgedMolodenskiTransform from the specified datums.
     *
     * @param source source horizontal datum you are transforming from.
     * @param target target horizontal datum you are transforming to.
     * @param source3D <code>true</code> if the source geographic CRS has a Z-axis (3 dimentional)
     * @param target3D <code>true</code> if the target geographic CRS has a Z-axis (3 dimentional)
     */
    protected AbridgedMolodenskiTransform(final HorizontalDatum source,
                                          final HorizontalDatum target,
                                          final boolean source3D, final boolean target3D)
    {
        double f, df;
        final WGS84ConversionInfo srcInfo = source.getWGS84Parameters();
        final WGS84ConversionInfo tgtInfo = target.getWGS84Parameters();
        final Ellipsoid      srcEllipsoid = source.getEllipsoid();
        final Ellipsoid      tgtEllipsoid = target.getEllipsoid();
        dx =     srcInfo.dx - tgtInfo.dx;
        dy =     srcInfo.dy - tgtInfo.dy;
        dz =     srcInfo.dz - tgtInfo.dz;
        a  =     srcEllipsoid.getSemiMajorAxis();
        b  =     srcEllipsoid.getSemiMinorAxis();
        da = tgtEllipsoid.getSemiMajorAxis() - a;
        db = tgtEllipsoid.getSemiMinorAxis() - b;
        f  = 1 / srcEllipsoid.getInverseFlattening();
        df = 1/tgtEllipsoid.getInverseFlattening() - f;
        e2  = 1 - (b*b)/(a*a);
        adf = (a*df) + (f*da);
        this.source3D = source3D;
        this.target3D = target3D;
    }
    
    /**
     * Construct an AbridgedMolodenskiTransform from the specified parameters.
     * 
     * @param  parameters The parameter values in standard units.
     */
    protected AbridgedMolodenskiTransform(final ParameterList parameters) {
        final int dim = parameters.getIntParameter("dim");
        switch (dim) {
            case 2:  source3D=target3D=false; break;
            case 3:  source3D=target3D=true;  break;
            default: throw new IllegalArgumentException(Resources.format(
                                ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, "dim", new Integer(dim)));
        }
        final double ta, tb, f, df;
        dx = parameters.getDoubleParameter("dx");
        dy = parameters.getDoubleParameter("dy");
        dz = parameters.getDoubleParameter("dz");
        a  = parameters.getDoubleParameter("src_semi_major");
        b  = parameters.getDoubleParameter("src_semi_minor");
        ta = parameters.getDoubleParameter("tgt_semi_major");
        tb = parameters.getDoubleParameter("tgt_semi_minor");
        da = ta - a;
        db = tb - b;
        f  = (a-b)/a;
        df = (ta-tb)/ta - f;
        e2  = 1 - (b*b)/(a*a);
        adf = (a*df) + (f*da);
    }
    
    /**
     * Transforms a list of coordinate point ordinal values.
     * This method is provided for efficiently transforming many points.
     * The supplied array of ordinal values will contain packed ordinal
     * values.  For example, if the source dimension is 3, then the ordinals
     * will be packed in this order:
     *
     * (<var>x<sub>0</sub></var>,<var>y<sub>0</sub></var>,<var>z<sub>0</sub></var>,
     *  <var>x<sub>1</sub></var>,<var>y<sub>1</sub></var>,<var>z<sub>1</sub></var> ...).
     *
     * @param srcPts the array containing the source point coordinates.
     * @param srcOff the offset to the first point to be transformed
     *               in the source array.
     * @param dstPts the array into which the transformed point
     *               coordinates are returned. May be the same
     *               than <code>srcPts</code>.
     * @param dstOff the offset to the location of the first
     *               transformed point that is stored in the
     *               destination array.
     * @param numPts the number of point objects to be transformed.
     */
    public void transform(final double[] srcPts, int srcOff,
                          final double[] dstPts, int dstOff, int numPts)
    {
        int step = 0;
        if (srcPts==dstPts && srcOff<dstOff && srcOff+numPts*getDimSource()>dstOff) {
            if (source3D != target3D) {
                // TODO: we need to figure out a general way to handle this case
                //       (overwritting the source array  while source and target
                //       dimensions are not the same).   This case occurs enough
                //       in the CTS implementation...
                throw new UnsupportedOperationException();
            }
            step = -getDimSource();
            srcOff -= (numPts-1)*step;
            dstOff -= (numPts-1)*step;
        }
        while (--numPts >= 0) {
            double x = Math.toRadians(srcPts[srcOff++]);
            double y = Math.toRadians(srcPts[srcOff++]);
            double z = (source3D) ? srcPts[srcOff++] : 0;
            final double sinX = Math.sin(x);
            final double cosX = Math.cos(x);
            final double sinY = Math.sin(y);
            final double cosY = Math.cos(y);
            final double sin2Y = sinY*sinY;
            final double nu = a / Math.sqrt(1 - e2*sin2Y);
            final double rho = nu * (1 - e2) / (1 - e2*sin2Y);
            
            // Note: Computation of 'x' and 'y' ommit the division by sin(1"), because
            //       1/sin(1") / (60*60*180/PI) = 1.0000000000039174050898603898692...
            //       (60*60 is for converting the final result from seconds to degrees,
            //       and 180/PI is for converting degrees to radians). This is an error
            //       of about 8E-7 arc seconds, probably close to rounding errors anyway.
            y += (dz*cosY - sinY*(dy*sinX + dx*cosX) + adf*Math.sin(2*y)) / rho;
            x += (dy*cosX - dx*sinX) / (nu*cosY);
            
            //stay within latitude +-90 deg. and longitude +-180 deg.
            if (Math.abs(y) > Math.PI/2.0) {
                dstPts[dstOff++] = 0.0;
                dstPts[dstOff++] = (y > 0.0) ? 90.0 : -90.0;
            } else {
                dstPts[dstOff++] = Math.toDegrees(ensureInRange(x));
                dstPts[dstOff++] = Math.toDegrees(y);
            }
            if (target3D) {
                z += dx*cosY*cosX + dy*cosY*sinX + dz*sinY + adf*sin2Y - da;
                dstPts[dstOff++] = z;
            }
            srcOff += step;
            dstOff += step;
        }
    }
    
    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final float[] srcPts, int srcOff,
                          final float[] dstPts, int dstOff, int numPts)
    {
        int step = 0;
        if (srcPts==dstPts && srcOff<dstOff && srcOff+numPts*getDimSource()>dstOff) {
            if (source3D != target3D) {
                // see TODO above
                throw new UnsupportedOperationException();
            }
            step = -getDimSource();
            srcOff -= (numPts-1)*step;
            dstOff -= (numPts-1)*step;
        }
        while (--numPts >= 0) {
            double x = Math.toRadians(srcPts[srcOff++]);
            double y = Math.toRadians(srcPts[srcOff++]);
            double z = (source3D) ? srcPts[srcOff++] : 0;
            final double sinX = Math.sin(x);
            final double cosX = Math.cos(x);
            final double sinY = Math.sin(y);
            final double cosY = Math.cos(y);
            final double sin2Y = sinY*sinY;
            final double nu = a / Math.sqrt(1 - e2*sin2Y);
            final double rho = nu * (1 - e2) / (1 - e2*sin2Y);
            
            // See sin(1") note above
            y += (dz*cosY - sinY*(dy*sinX + dx*cosX) + adf*Math.sin(2*y)) / rho;
            x += (dy*cosX - dx*sinX) / (nu*cosY);
            
            //stay within latitude +-90 deg. and longitude +-180 deg.
            if (Math.abs(y) > Math.PI/2.0) {
                dstPts[dstOff++] = 0.0F;
                dstPts[dstOff++] = (y > 0.0) ? 90.0F : -90.0F;
            } else {
                dstPts[dstOff++] = (float) Math.toDegrees(ensureInRange(x));
                dstPts[dstOff++] = (float) Math.toDegrees(y);
            }
            if (target3D) {
                z += dx*cosY*cosX + dy*cosY*sinX + dz*sinY + adf*sin2Y - da;
                dstPts[dstOff++] = (float) z;
            }
            srcOff += step;
            dstOff += step;
        }
    }
    
    /**
     * Gets the dimension of input points.
     */
    public int getDimSource() {
        return source3D ? 3 : 2;
    }
    
    /**
     * Gets the dimension of output points.
     */
    public final int getDimTarget() {
        return target3D ? 3 : 2;
    }
    
    /**
     * Makes sure that the specified longitude stay within ±180 degrees. This methpod should be
     * invoked after coordinates are transformed. This
     * method may add or substract an amount of 360° to <var>x</var>.
     *
     * @param  x The longitude.
     * @return The longitude in the range +/- 180°.
     *
     * @task REVISIT: could be moved to AbstractMathTransform
     */
    final double ensureInRange(double x) {
        if (x > Math.PI) {
            x -= 2*Math.PI;
        } else if (x < -Math.PI) {
            x += 2*Math.PI;
        }
        return x;
    }
    
    /**
     * Returns a hash value for this transform.
     */
    public final int hashCode() {
        final long code = Double.doubleToLongBits(dx) +
                          37*(Double.doubleToLongBits(dy) +
                          37*(Double.doubleToLongBits(dz) +
                          37*(Double.doubleToLongBits(a ) +
                          37*(Double.doubleToLongBits(b ) +
                          37*(Double.doubleToLongBits(da) +
                          37*(Double.doubleToLongBits(db)))))));
        return (int) code ^ (int) (code >>> 32);
    }
    
    /**
     * Compares the specified object with
     * this math transform for equality.
     */
    public final boolean equals(final Object object) {
        if (object == this) {
            // Slight optimization
            return true;
        }
        if (super.equals(object)) {
            final AbridgedMolodenskiTransform that = (AbridgedMolodenskiTransform) object;
            return Double.doubleToLongBits(this.dx) == Double.doubleToLongBits(that.dx) &&
                   Double.doubleToLongBits(this.dy) == Double.doubleToLongBits(that.dy) &&
                   Double.doubleToLongBits(this.dz) == Double.doubleToLongBits(that.dz) &&
                   Double.doubleToLongBits(this.a ) == Double.doubleToLongBits(that.a ) &&
                   Double.doubleToLongBits(this.b ) == Double.doubleToLongBits(that.b ) &&
                   Double.doubleToLongBits(this.da) == Double.doubleToLongBits(that.da) &&
                   Double.doubleToLongBits(this.db) == Double.doubleToLongBits(that.db) &&
                   this.source3D == that.source3D &&
                   this.target3D == that.target3D;
        }
        return false;
    }
    
    /**
     * Returns the WKT for this math transform.
     */
    public final String toString() {
        final StringBuffer buffer = paramMT("Abridged_Molodenski");
        addParameter(buffer, "dim", getDimSource());
        addParameter(buffer, "dx",              dx);
        addParameter(buffer, "dy",              dy);
        addParameter(buffer, "dz",              dz);
        addParameter(buffer, "src_semi_major",   a);
        addParameter(buffer, "src_semi_minor",   b);
        addParameter(buffer, "tgt_semi_major",   a+da);
        addParameter(buffer, "tgt_semi_minor",   b+db);
        buffer.append(']');
        return buffer.toString();
    }
    
    /**
     * The provider for {@link AbridgedMolodenskiTransform}.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    static final class Provider extends MathTransformProvider {
        /**
         * The range of values for the dimension.
         */
        static final Range DIM_RANGE = new Range(Integer.class, new Integer(2), new Integer(3));
        
        /**
         * Create a provider.
         */
        public Provider() {
            super("Abridged_Molodenski", ResourceKeys.ABRIDGED_MOLODENSKI_TRANSFORM, null);
            putInt("dim",         3, DIM_RANGE);
            put("dx",             Double.NaN, null);
            put("dy",             Double.NaN, null);
            put("dz",             0,          null);
            put("src_semi_major", Double.NaN, POSITIVE_RANGE);
            put("src_semi_minor", Double.NaN, POSITIVE_RANGE);
            put("tgt_semi_major", Double.NaN, POSITIVE_RANGE);
            put("tgt_semi_minor", Double.NaN, POSITIVE_RANGE);
        }
        
        /**
         * Returns a transform for the specified parameters.
         *
         * @param  parameters The parameter values in standard units.
         * @return A {@link MathTransform} object of this classification.
         */
        public MathTransform create(final ParameterList parameters) {
            return new AbridgedMolodenskiTransform(parameters);
        }
    }
}
