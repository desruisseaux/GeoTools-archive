/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, 2004 Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
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
package org.geotools.referencing.operation.transform;

// J2SE dependencies
import java.io.Serializable;
import javax.units.SI;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.referencing.operation.MathTransform;

// Geotools dependencies
import org.geotools.metadata.citation.Citation;
import org.geotools.referencing.Identifier;
import org.geotools.referencing.operation.MathTransformProvider;
import org.geotools.parameter.ParameterReal;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Two- or three-dimensional datum shift using the abridged Molodensky transformation.
 * The abridged Molodensky transformation (EPSG code 9605) is a simplified version of the
 * {@link MolodenskiTransform} method. This transforms two or three dimensional 
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
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 *
 * @see MolodenskiTransform
 */
public class AbridgedMolodenskiTransform extends AbstractMathTransform implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 4697887228069004313L;

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
     * The square of excentricity of the ellipsoid: e� = (a�-b�)/a� where
     * <var>a</var> is the semi-major axis length and
     * <var>b</var> is the semi-minor axis length.
     */
    private final double e2;
    
    /**
     * Defined as <code>(a*df) + (f*da)</code>.
     */
    private final double adf;
    
    /**
     * Construct an abridged Molodenski transform from the specified parameters.
     * 
     * @param a        The source semi-major axis length in meters.
     * @param b        The source semi-minor axis length in meters.
     * @param source3D <code>true</code> if the source has a height.
     * @param ta       The target semi-major axis length in meters.
     * @param tb       The target semi-minor axis length in meters.
     * @param target3D <code>true</code> if the target has a height.
     * @param dx       The <var>x</var> translation in meters.
     * @param dy       The <var>y</var> translation in meters.
     * @param dz       The <var>z</var> translation in meters.
     */
    public AbridgedMolodenskiTransform(final double  a, final double  b, final boolean source3D,
                                       final double ta, final double tb, final boolean target3D,
                                       final double dx, final double dy, final double  dz)
    {
        this.source3D = source3D;
        this.target3D = target3D;
        this.dx       = dx;
        this.dy       = dy;
        this.dz       = dz;
        this.a        = a;
        this.b        = b;

        final double f, df;
        da   =  ta - a;
        db   =  tb - b;
        f    =  (a-b)/a;
        df   =  (ta-tb)/ta - f;
        e2   =  1 - (b*b)/(a*a);
        adf  =  (a*df) + (f*da);
    }

    /**
     * Returns the parameter values for this math transform.
     *
     * @return A copy of the parameter values for this math transform.
     */
    public ParameterValueGroup getParameterValues() {
        final ParameterValue dim = new org.geotools.parameter.Parameter(Provider.DIM);
        dim.setValue(getDimSource());
        return new org.geotools.parameter.ParameterGroup(Provider.PARAMETERS,
               new ParameterValue[] {
                   dim,
                   new ParameterReal(Provider.DX,             dx),
                   new ParameterReal(Provider.DY,             dy),
                   new ParameterReal(Provider.DZ,             dz),
                   new ParameterReal(Provider.SRC_SEMI_MAJOR, a),
                   new ParameterReal(Provider.SRC_SEMI_MINOR, b),
                   new ParameterReal(Provider.TGT_SEMI_MAJOR, a+da),
                   new ParameterReal(Provider.TGT_SEMI_MINOR, b+db)
               });
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
                throw new UnsupportedOperationException("Not yet implemented.");
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
                dstPts[dstOff++] = Math.toDegrees(rollLongitude(x));
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
                dstPts[dstOff++] = (float) Math.toDegrees(rollLongitude(x));
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
     * The provider for {@link AbridgedMolodenskiTransform}. This provider will construct transforms
     * from {@linkplain org.geotools.referencing.crs.GeographicCRS geographic} to
     * {@linkplain org.geotools.referencing.crs.GeographicCRS geographic} coordinate reference
     * systems.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     * @author Rueben Schulz
     */
    public static class Provider extends MathTransformProvider {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = 6831719006135449291L;

        /**
         * The number of geographic dimension (2 or 3). The default value is 2.
         */
        public static final ParameterDescriptor DIM = new org.geotools.parameter.ParameterDescriptor(
                "dim", 2, 2, 3);

        /**
         * The operation parameter descriptor for the "dx" parameter value.
         * Valid values range from -infinity to infinity.
         */
        public static final ParameterDescriptor DX = new org.geotools.parameter.ParameterDescriptor(
                "dx", Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, SI.METER);
        
        /**
         * The operation parameter descriptor for the "dy" parameter value.
         * Valid values range from -infinity to infinity.
         */
        public static final ParameterDescriptor DY = new org.geotools.parameter.ParameterDescriptor(
                "dy", Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, SI.METER);
        
        /**
         * The operation parameter descriptor for the "dx" parameter value.
         * Valid values range from -infinity to infinity, default is 0.0.
         */
        public static final ParameterDescriptor DZ = new org.geotools.parameter.ParameterDescriptor(
                "dz", 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, SI.METER);
        
        /**
         * The operation parameter descriptor for the "src_semi_major" parameter value.
         * Valid values range from 0 to infinity.
         */
        public static final ParameterDescriptor SRC_SEMI_MAJOR = new org.geotools.parameter.ParameterDescriptor(
                "src_semi_major", Double.NaN, 0.0, Double.POSITIVE_INFINITY, SI.METER);

        /**
         * The operation parameter descriptor for the "src_semi_minor" parameter value.
         * Valid values range from 0 to infinity.
         */
        public static final ParameterDescriptor SRC_SEMI_MINOR = new org.geotools.parameter.ParameterDescriptor(
                "src_semi_minor", Double.NaN, 0.0, Double.POSITIVE_INFINITY, SI.METER);
        
        /**
         * The operation parameter descriptor for the "tgt_semi_major" parameter value.
         * Valid values range from 0 to infinity.
         */
        public static final ParameterDescriptor TGT_SEMI_MAJOR = new org.geotools.parameter.ParameterDescriptor(
                "tgt_semi_major", Double.NaN, 0.0, Double.POSITIVE_INFINITY, SI.METER);
        
        /**
         * The operation parameter descriptor for the "tgt_semi_minor" parameter value.
         * Valid values range from 0 to infinity.
         */
        public static final ParameterDescriptor TGT_SEMI_MINOR = new org.geotools.parameter.ParameterDescriptor(
                "tgt_semi_minor", Double.NaN, 0.0, Double.POSITIVE_INFINITY, SI.METER);

        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = group(
                     new Identifier[] {
                        new Identifier(Citation.OPEN_GIS, null,  "Abridged_Molodenski"),
                        new Identifier(Citation.EPSG,    "EPSG", "9605")
                     }, new ParameterDescriptor[] {
                        DIM, DX, DY, DZ,
                        SRC_SEMI_MAJOR, SRC_SEMI_MINOR,
                        TGT_SEMI_MAJOR, TGT_SEMI_MINOR
                     });

        /**
         * Constructs a provider.
         */
        public Provider() {
            super(3, 3, PARAMETERS);
        }
        
        /**
         * Returns the resources key for {@linkplain #getName localized name}.
         * This method is for internal purpose by Geotools implementation only.
         */
        protected int getLocalizationKey() {
            return ResourceKeys.ABRIDGED_MOLODENSKI_TRANSFORM;
        }
        
        /**
         * Creates a math transform from the specified group of parameter values.
         *
         * @param  values The group of parameter values.
         * @return The created math transform.
         * @throws ParameterNotFoundException if a required parameter was not found.
         */
        protected MathTransform createMathTransform(final ParameterValueGroup values) 
                throws ParameterNotFoundException 
        {
            final boolean hasHeight;
            final int dim = intValue(values, Provider.DIM);
            switch (dim) {
                case 2:  hasHeight=false; break;
                case 3:  hasHeight=true;  break;
                default: throw new IllegalArgumentException(Resources.format(
                               ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, "dim", new Integer(dim)));
            }
            return new AbridgedMolodenskiTransform(doubleValue(values, SRC_SEMI_MAJOR),
                                                   doubleValue(values, SRC_SEMI_MINOR), hasHeight,
                                                   doubleValue(values, TGT_SEMI_MAJOR),
                                                   doubleValue(values, TGT_SEMI_MINOR), hasHeight,
                                                   doubleValue(values, DX),
                                                   doubleValue(values, DY),
                                                   doubleValue(values, DZ));
        }
    }
}
