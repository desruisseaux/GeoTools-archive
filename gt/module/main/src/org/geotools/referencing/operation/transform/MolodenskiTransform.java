/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, 2004 Geotools Project Managment Committee (PMC)
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
 * Two- or three-dimensional datum shift using the Molodensky transformation.
 * The Molodensky transformation (EPSG code 9604) transforms two or three dimensional
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
 * @author Rueben Schulz
 *
 * @see AbridgedMolodenskiTransform
 */
public class MolodenskiTransform extends AbstractMathTransform implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5965481951620975152L;

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
     * Difference in the semi-major (<code>da = target a - source a</code>) and semi-minor
     * (<code>db=target b - source b</code>) axes of the target and source ellipsoids.
     */
    private final double da, db;
    
    /**
     * Difference between the flattenings (<code>df = target f - source f</code>)
     * of the target and source ellipsoids.
     */
    private final double df;
    
    /**
     * Ratio of the Semi-major (<var>a</var>) semi-minor (<var>b/<var>) axis 
     * values (<code>a_b = a/b</code> and <code>b_a = b/a</code>).
     */
    private final double b_a, a_b;
    
    /**
     * Some more constants (<code>daa = da*a</code> and <code>da_a = da/a</code>).
     */
    private final double daa, da_a;
    
    /**
     * The square of excentricity of the ellipsoid: e² = (a²-b²)/a² where
     * <var>a</var> is the semi-major axis length and
     * <var>b</var> is the semi-minor axis length.
     */
    private final double e2;
    
    /**
     * Construct a Molodenski transform from the specified parameters.
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
    public MolodenskiTransform(final double  a, final double  b, final boolean source3D,
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

        da    =  ta - a;
        db    =  tb - b;
        a_b   =  a/b;
        b_a   =  b/a;
        daa   =  da*a;
        da_a  =  da/a;
        df    =  (ta-tb)/ta - (a-b)/a;
        e2    =  1 - (b*b)/(a*a);
    }

    /**
     * Returns the parameter descriptors for this math transform.
     */
    public ParameterDescriptorGroup getParameterDescriptors() {
        return Provider.PARAMETERS;
    }
    
    /**
     * Returns the parameters for this math transform.
     *
     * @return The parameters for this math transform.
     */
    public ParameterValueGroup getParameterValues() {
        final ParameterValue dim = new org.geotools.parameter.Parameter(Provider.DIM);
        dim.setValue(getDimSource());
        return new org.geotools.parameter.ParameterGroup(getParameterDescriptors(),
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
    public int getSourceDimensions() {
        return source3D ? 3 : 2;
    }
    
    /**
     * Gets the dimension of output points.
     */
    public final int getTargetDimensions() {
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
            final double Rn = a / Math.sqrt(1 - e2*sin2Y);
            final double Rm = Rn * (1 - e2) / (1 - e2*sin2Y);
            
            // Note: Computation of 'x' and 'y' ommit the division by sin(1"), because
            //       1/sin(1") / (60*60*180/PI) = 1.0000000000039174050898603898692...
            //       (60*60 is for converting the final result from seconds to degrees,
            //       and 180/PI is for converting degrees to radians). This is an error
            //       of about 8E-7 arc seconds, probably close to rounding errors anyway.
            y += (dz*cosY - sinY*(dy*sinX + dx*cosX) + da_a*(Rn*e2*sinY*cosY) + 
                  df*(Rm*(a_b) + Rn*(b_a))*sinY*cosY) / (Rm + z);
            x += (dy*cosX - dx*sinX) / ((Rn + z)*cosY);
            
            //stay within latitude +-90 deg. and longitude +-180 deg.
            if (Math.abs(y) > Math.PI/2.0) {
                dstPts[dstOff++] = 0.0;
                dstPts[dstOff++] = (y > 0.0) ? 90.0 : -90.0;
            } else {
                dstPts[dstOff++] = Math.toDegrees(rollLongitude(x));
                dstPts[dstOff++] = Math.toDegrees(y);
            }
            if (target3D) {
                z += dx*cosY*cosX + dy*cosY*sinX + dz*sinY + df*(b_a)*Rn*sin2Y - daa/Rn;
                dstPts[dstOff++] = z;
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
            final MolodenskiTransform that = (MolodenskiTransform) object;
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
     * The provider for {@link MolodenskiTransform}. This provider will construct transforms
     * from {@linkplain org.geotools.referencing.crs.GeographicCRS geographic} to
     * {@linkplain org.geotools.referencing.crs.GeographicCRS geographic} coordinate reference
     * systems.
     *
     * The EPSG does not use src_semi_major, etc. parameters and instead uses 
     * "Semi-major axis length difference" and "Flattening difference".
     *
     * @version $Id$
     * @author Rueben Schulz
     */
    public static class Provider extends MathTransformProvider {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = -5332126871499059030L;

        /**
         * The number of geographic dimension (2 or 3). The default value is 2.
         * This parameter is specific to Geotools.
         */
        public static final ParameterDescriptor DIM =
                AbridgedMolodenskiTransform.Provider.DIM;

        /**
         * The operation parameter descriptor for the "dx" parameter value.
         * Valid values range from -infinity to infinity.
         */
        public static final ParameterDescriptor DX =
                AbridgedMolodenskiTransform.Provider.DX;
        
        /**
         * The operation parameter descriptor for the "dy" parameter value.
         * Valid values range from -infinity to infinity.
         */
        public static final ParameterDescriptor DY =
                AbridgedMolodenskiTransform.Provider.DY;
        
        /**
         * The operation parameter descriptor for the "dx" parameter value.
         * Valid values range from -infinity to infinity, default is 0.0.
         */
        public static final ParameterDescriptor DZ =
                AbridgedMolodenskiTransform.Provider.DZ;
        
        /**
         * The operation parameter descriptor for the "src_semi_major" parameter value.
         * Valid values range from 0 to infinity.
         */
        public static final ParameterDescriptor SRC_SEMI_MAJOR =
                AbridgedMolodenskiTransform.Provider.SRC_SEMI_MAJOR;

        /**
         * The operation parameter descriptor for the "src_semi_minor" parameter value.
         * Valid values range from 0 to infinity.
         */
        public static final ParameterDescriptor SRC_SEMI_MINOR =
                AbridgedMolodenskiTransform.Provider.SRC_SEMI_MINOR;
        
        /**
         * The operation parameter descriptor for the "tgt_semi_major" parameter value.
         * Valid values range from 0 to infinity.
         */
        public static final ParameterDescriptor TGT_SEMI_MAJOR =
                AbridgedMolodenskiTransform.Provider.TGT_SEMI_MAJOR;
        
        /**
         * The operation parameter descriptor for the "tgt_semi_minor" parameter value.
         * Valid values range from 0 to infinity.
         */
        public static final ParameterDescriptor TGT_SEMI_MINOR =
                AbridgedMolodenskiTransform.Provider.TGT_SEMI_MINOR;

        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new Identifier[] {
                new Identifier(Citation.OPEN_GIS, "Molodenski"),
                new Identifier(Citation.EPSG,     "Molodenski"),
                new Identifier(Citation.EPSG,     "9604"),
                new Identifier(Citation.GEOTOOLS, Resources.formatInternational(
                                                  ResourceKeys.MOLODENSKI_TRANSFORM))
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
            final int dim = intValue(DIM, values);
            switch (dim) {
                case 2:  hasHeight=false; break;
                case 3:  hasHeight=true;  break;
                default: throw new IllegalArgumentException(Resources.format(
                               ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, "dim", new Integer(dim)));
            }
            return new MolodenskiTransform(doubleValue(SRC_SEMI_MAJOR, values),
                                           doubleValue(SRC_SEMI_MINOR, values), hasHeight,
                                           doubleValue(TGT_SEMI_MAJOR, values),
                                           doubleValue(TGT_SEMI_MINOR, values), hasHeight,
                                           doubleValue(DX,             values),
                                           doubleValue(DY,             values),
                                           doubleValue(DZ,             values));
        }
    }
}
