/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
import java.util.Locale;
import javax.units.Unit;
import java.io.Serializable;

// OpenGIS dependencies
import org.opengis.parameter.OperationParameter;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform1D;

// Geotools dependencies
import org.geotools.parameter.ParameterValue;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.referencing.operation.LinearTransform;
import org.geotools.referencing.operation.MathTransformProvider;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;


/**
 * A one dimensional, logarithmic transform.
 * Input values <var>x</var> are converted into
 * output values <var>y</var> using the following equation:
 *
 * <p align="center"><var>y</var> &nbsp;=&nbsp;
 * {@linkplain #offset} + log<sub>{@linkplain #base}</sub>(<var>x</var>)
 * &nbsp;&nbsp;=&nbsp;&nbsp;
 * {@linkplain #offset} + ln(<var>x</var>)/ln({@linkplain #base})</p>
 *
 * This transform is the inverse of {@link ExponentialTransform1D}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see ExponentialTransform1D
 * @see LinearTransform1D
 */
public class LogarithmicTransform1D extends AbstractMathTransform
                                 implements MathTransform1D, Serializable
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1535101265352133948L;

    /**
     * The base of the logarithm.
     */
    public final double base;

    /**
     * Natural logarithm of {@link #base}.
     */
    final double lnBase;

    /**
     * The offset to add to the logarithm.
     */
    public final double offset;

    /**
     * The inverse of this transform. Created only when first needed.
     * Serialized in order to avoid rounding error if this transform
     * is actually the one which was created from the inverse.
     */
    private MathTransform inverse;

    /**
     * Construct a new logarithmic transform which is the
     * inverse of the supplied exponentional transform.
     */
    LogarithmicTransform1D(final ExponentialTransform1D inverse) {
        this.base    = inverse.base;
        this.lnBase  = inverse.lnBase;
        this.offset  = -Math.log(inverse.scale)/lnBase;
        this.inverse = inverse;
    }

    /**
     * Construct a new logarithmic transform. This constructor is provided for subclasses only.
     * Instances should be created using the {@linkplain #create factory method}, which
     * may returns optimized implementations for some particular argument values.
     *
     * @param base    The base of the logarithm.
     * @param offset  The offset to add to the logarithm.
     */
    protected LogarithmicTransform1D(final double base, final double offset) {
        this.base    = base;
        this.offset  = offset;
        this.lnBase  = Math.log(base);
    }

    /**
     * Construct a new logarithmic transform.
     *
     * @param base    The base of the logarithm.
     * @param offset  The offset to add to the logarithm.
     */
    public static MathTransform1D create(final double base, final double offset) {
        if (base==Double.POSITIVE_INFINITY || base==0) {
            return LinearTransform1D.create(0, offset);
        }
        return new LogarithmicTransform1D(base, offset);
    }
    
    /**
     * Gets the dimension of input points, which is 1.
     */
    public int getDimSource() {
        return 1;
    }
    
    /**
     * Gets the dimension of output points, which is 1.
     */
    public int getDimTarget() {
        return 1;
    }
    
    /**
     * Creates the inverse transform of this object.
     */
    public MathTransform inverse() {
        if (inverse == null) {
            inverse = new ExponentialTransform1D(this);
        }
        return inverse;
    }
    
    /**
     * Gets the derivative of this function at a value.
     */
    public double derivative(final double value) {
        return 1 / (lnBase * value);
    }
    
    /**
     * Transforms the specified value.
     */
    public double transform(final double value) {
        return Math.log(value)/lnBase + offset;
    }

    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final float[] srcPts, int srcOff,
                          final float[] dstPts, int dstOff, int numPts)
    {
        if (srcPts!=dstPts || srcOff>=dstOff) {
            while (--numPts >= 0) {
                dstPts[dstOff++] = (float) (Math.log(srcPts[srcOff++])/lnBase + offset);
            }
        } else {
            srcOff += numPts;
            dstOff += numPts;
            while (--numPts >= 0) {
                dstPts[--dstOff] = (float) (Math.log(srcPts[srcOff++])/lnBase + offset);
            }
        }
    }

    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final double[] srcPts, int srcOff,
                          final double[] dstPts, int dstOff, int numPts)
    {
        if (srcPts!=dstPts || srcOff>=dstOff) {
            while (--numPts >= 0) {
                dstPts[dstOff++] = Math.log(srcPts[srcOff++])/lnBase + offset;
            }
        } else {
            srcOff += numPts;
            dstOff += numPts;
            while (--numPts >= 0) {
                dstPts[--dstOff] = Math.log(srcPts[srcOff++])/lnBase + offset;
            }
        }
    }

    /**
     * Concatenates in an optimized way a {@link MathTransform} <code>other</code> to this
     * <code>MathTransform</code>. This implementation can optimize some concatenation with
     * {@link LinearTransform1D} and {@link ExponentialTransform1D}.
     *
     * @param  other The math transform to apply.
     * @param  applyOtherFirst <code>true</code> if the transformation order is <code>other</code>
     *         followed by <code>this</code>, or <code>false</code> if the transformation order is
     *         <code>this</code> followed by <code>other</code>.
     * @return The combined math transform, or <code>null</code> if no optimized combined
     *         transform is available.
     */
    MathTransform concatenate(final MathTransform other, final boolean applyOtherFirst) {
        if (other instanceof LinearTransform) {
            final LinearTransform1D linear = (LinearTransform1D) other;
            if (applyOtherFirst) {
                if (linear.offset==0 && linear.scale>0) {
                    return create(base, Math.log(linear.scale)/lnBase+offset);
                }
            } else {
                final double newBase = Math.pow(base, 1/linear.scale);
                if (!Double.isNaN(newBase)) {
                    return create(newBase, linear.scale*offset + linear.offset);
                }
            }
        } else if (other instanceof ExponentialTransform1D) {
            return ((ExponentialTransform1D) other).concatenateLog(this, !applyOtherFirst);
        }
        return super.concatenate(other, applyOtherFirst);
    }
    
    /**
     * Returns a hash value for this transform.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        long code;
        code = serialVersionUID + Double.doubleToLongBits(base);
        code =          code*37 + Double.doubleToLongBits(offset);
        return (int)(code >>> 32) ^ (int)code;
    }
    
    /**
     * Compares the specified object with this math transform for equality.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            // Slight optimization
            return true;
        }
        if (super.equals(object)) {
            final LogarithmicTransform1D that = (LogarithmicTransform1D) object;
            return Double.doubleToLongBits(this.base)   == Double.doubleToLongBits(that.base) &&
                   Double.doubleToLongBits(this.offset) == Double.doubleToLongBits(that.offset);
        }
        return false;
    }
    
    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name.
     */
    protected String formatWKT(final Formatter formatter) {
        formatter.append("Logarithmic");
        ParameterValue value = new ParameterValue(Provider.BASE);
        value.setValue(base);
        formatter.append(value);
        if (offset != 0) {
            value = new ParameterValue(Provider.OFFSET);
            value.setValue(offset);
            formatter.append(value);
        }
        return "PARAM_MT";
    }
    
    /**
     * The provider for the {@link LogarithmicTransform1D}.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static class Provider extends MathTransformProvider {
        /**
         * The operation parameter descriptor for the {@link #base} parameter value.
         * Valid values range from 0 to infinity. The default value is 10.
         */
        public static final OperationParameter BASE = new org.geotools.parameter.OperationParameter(
                "base", 10, 0, Double.POSITIVE_INFINITY, Unit.ONE);

        /**
         * The operation parameter descriptor for the {@link #offset} parameter value.
         * Valid values range is unrestricted. The default value is 0.
         */
        public static final OperationParameter OFFSET = new org.geotools.parameter.OperationParameter(
                "offset", 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Unit.ONE);

        /**
         * Create a provider for logarithmic transforms.
         */
        public Provider() {
            super("Logarithmic", 1, 1, new OperationParameter[] {BASE, OFFSET});
        }

        /**
         * Returns the name by which this object is identified. If <code>locale</code> is
         * <code>null</code>, then this method returns <code>"Logarithmic"</code>. Otherwise,
         * it try to returns a localized string.
         *
         * @param  locale The desired locale for the name to be returned,
         *         or <code>null</code> for a non-localized string.
         * @return The name, or <code>null</code> if not available.
         */
        public String getName(final Locale locale) {
            if (locale == null) {
                return super.getName(locale);
            }
            return Resources.getResources(locale).getString(ResourceKeys.LOGARITHM);
        }
        
        /**
         * Returns a transform for the specified parameters.
         *
         * @param  parameters The parameter values.
         * @return A {@link MathTransform} object of this classification.
         */
        public MathTransform createMathTransform(final ParameterValueGroup parameters) {
            final double base = 0;//TODO parameters.getValue("base").doubleValue();
            return create(base, 0);
        }
    }
}
