/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
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
import java.util.Collections;
import javax.units.NonSI;
import javax.units.SI;

// OpenGIS dependencies
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Transformation;

// Geotools dependencies
import org.geotools.measure.Units;
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.FloatParameter;
import org.geotools.parameter.ParameterGroup;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.referencing.datum.BursaWolfParameters;
import org.geotools.referencing.operation.MathTransformProvider;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;


/**
 * An affine transform applied on {@linkplain org.opengis.referencing.crs.GeocentricCRS geocentric}
 * coordinates. While "geocentric translation" is a little bit more restrictive name, it describes
 * the part which is common to all instances of this class. A rotation may also be performed in
 * addition of the translation, but the rotation sign is operation-dependent (EPSG 9606 and 9607
 * have opposite sign). This transform is used for the following operations:
 * <p>
 * <table border="1">
 *   <tr><th>EPSG name</th>                               <th>EPSG code</th></tr>
 *   <tr><td>Geocentric translations</td>                 <td>9603</td></tr>
 *   <tr><td>Position Vector 7-param. transformation</td> <td>9606</td></tr>
 *   <tr><td>Coordinate Frame rotation</td>               <td>9607</td></tr>
 * </table>
 * <p>
 * The conversion between geographic and geocentric coordinates is usually not <strong>not</strong>
 * part of this transform. However, the Geotools implementation of the
 * {@linkplain GeocentricTranslation.Provider provider} accepts the following extensions:
 * <p>
 * <ul>
 *   <li>If {@code "src_semi_major"} and {@code "src_semi_minor"} parameters are provided, then
 *       a {@code "Ellipsoid_To_Geocentric"} transform is concatenated before this transform.</li>
 *   <li>If {@code "tgt_semi_major"} and {@code "tgt_semi_minor"} parameters are provided, then
 *       a {@code "Geocentric_To_Ellipsoid"} transform is concatenated after this transform.</li>
 * </ul>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.2
 */
public class GeocentricTranslation extends ProjectiveTransform {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -168669443433018655L;

    /**
     * The parameter descriptor group.
     */
    private final ParameterDescriptorGroup descriptor;

    /**
     * Creates a new geocentric affine transform. If the parameters don't contain rotation terms,
     * then this transform will be of kind "<cite>Geocentric translations</cite>". Otherwise, it
     * will be of kind "<cite>Position Vector 7-param. transformation</cite>".
     */
    public GeocentricTranslation(final BursaWolfParameters parameters) {
        this(parameters, parameters.isTranslation() ?
                         Provider.PARAMETERS : SevenParamProvider.PARAMETERS);
    }

    /**
     * Creates a new geocentric affine transform using the specified parameter descriptor.
     */
    GeocentricTranslation(final BursaWolfParameters      parameters,
                          final ParameterDescriptorGroup descriptor)
    {
        super(parameters.getAffineTransform());
        this.descriptor = descriptor;
    }

    /**
     * Creates a new geocentric affine transform using the specified parameter descriptor.
     */
    private GeocentricTranslation(final Matrix matrix,
                                  final ParameterDescriptorGroup descriptor)
    {
        super(matrix);
        this.descriptor = descriptor;
    }

    /**
     * Returns the parameter descriptors for this math transform.
     */
    public ParameterDescriptorGroup getParameterDescriptors() {
        return descriptor;
    }

    /**
     * Returns the parameters for this math transform.
     */
    public ParameterValueGroup getParameterValues() {
        final BursaWolfParameters parameters = new BursaWolfParameters(null);
        parameters.setAffineTransform(getMatrix());
        if (FrameRotationProvider.PARAMETERS.equals(descriptor)) {
            parameters.ex = -parameters.ex;
            parameters.ey = -parameters.ey;
            parameters.ez = -parameters.ez;
        }
        final boolean isTranslation = Provider.PARAMETERS.equals(descriptor);
        final FloatParameter[] param = new FloatParameter[isTranslation ? 3 : 7];
        param[0] = new FloatParameter(Provider.DX,  parameters.dx);
        param[1] = new FloatParameter(Provider.DY,  parameters.dy);
        param[2] = new FloatParameter(Provider.DZ,  parameters.dz);
        if (!isTranslation) {
            param[3] = new FloatParameter(SevenParamProvider.EX,  parameters.ex);
            param[4] = new FloatParameter(SevenParamProvider.EY,  parameters.ey);
            param[5] = new FloatParameter(SevenParamProvider.EZ,  parameters.ez);
            param[6] = new FloatParameter(SevenParamProvider.PPM, parameters.ppm);
        }
        return new ParameterGroup(getParameterDescriptors(), param);
    }

    /**
     * Creates an inverse transform using the specified matrix.
     */
    MathTransform createInverse(final Matrix matrix) {
        return new GeocentricTranslation(matrix, descriptor);
    }

    /**
     * Returns a hash value for this transform.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        return super.hashCode() ^ descriptor.hashCode();
    }
    
    /**
     * Compares the specified object with this math transform for equality.
     */
    public boolean equals(final Object object) {
        if (super.equals(object)) {
            final GeocentricTranslation that = (GeocentricTranslation) object;
            return Utilities.equals(this.descriptor, that.descriptor);
        }
        return false;
    }

    /**
     * Base class for {@linkplain GeocentricTranslation geocentric affine transform} providers.
     * This base class is the provider for the "<cite>Geocentric translations</cite>" operation
     * (EPSG code 9603). The translation terms are the same for the 2 derived operations,
     * "<cite>Position Vector 7-param. transformation</cite>" and
     * "<cite>Coordinate Frame rotation</cite>".
     *
     * @version $Id$
     * @author Martin Desruisseaux
     *
     * @since 2.2
     */
    public static class Provider extends MathTransformProvider {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = -7160250630666911608L;

        /**
         * The default value for geographic source and target dimensions, which is 2.
         * NOTE: If this default value is modified, then the handling of the 3D cases
         * in {@link MolodenskiTransform} must be adjusted.
         */
        static final int DEFAULT_DIM = 2;

        /**
         * The number of source geographic dimension (2 or 3).
         * This is a Geotools-specific argument.
         */
        static final ParameterDescriptor SRC_DIM = new DefaultParameterDescriptor(
                    Collections.singletonMap(NAME_KEY,
                        new NamedIdentifier(CitationImpl.GEOTOOLS, "src_dim")),
                    DEFAULT_DIM, 2, 3, false);

        /**
         * The number of target geographic dimension (2 or 3).
         * This is a Geotools-specific argument.
         */
        static final ParameterDescriptor TGT_DIM = new DefaultParameterDescriptor(
                    Collections.singletonMap(NAME_KEY,
                        new NamedIdentifier(CitationImpl.GEOTOOLS, "tgt_dim")),
                    DEFAULT_DIM, 2, 3, false);

        /**
         * The operation parameter descriptor for the "src_semi_major" parameter value.
         * This is a Geotools-specific argument.
         * Valid values range from 0 to infinity.
         */
        static final ParameterDescriptor SRC_SEMI_MAJOR = createOptionalDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(CitationImpl.OGC, "src_semi_major")
                },
                0.0, Double.POSITIVE_INFINITY, SI.METER);

        /**
         * The operation parameter descriptor for the "src_semi_minor" parameter value.
         * This is a Geotools-specific argument.
         * Valid values range from 0 to infinity.
         */
         static final ParameterDescriptor SRC_SEMI_MINOR = createOptionalDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(CitationImpl.OGC, "src_semi_minor"),
                },
                0.0, Double.POSITIVE_INFINITY, SI.METER);

        /**
         * The operation parameter descriptor for the "tgt_semi_major" parameter value.
         * This is a Geotools-specific argument.
         * Valid values range from 0 to infinity.
         */
        static final ParameterDescriptor TGT_SEMI_MAJOR = createOptionalDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(CitationImpl.OGC, "tgt_semi_major")
                },
                0.0, Double.POSITIVE_INFINITY, SI.METER);

        /**
         * The operation parameter descriptor for the "tgt_semi_minor" parameter value.
         * This is a Geotools-specific argument.
         * Valid values range from 0 to infinity.
         */
        static final ParameterDescriptor TGT_SEMI_MINOR = createOptionalDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(CitationImpl.OGC, "tgt_semi_minor")
                },
                0.0, Double.POSITIVE_INFINITY, SI.METER);

        /**
         * The operation parameter descriptor for the <cite>X-axis translation</cite> ("dx")
         * parameter value. Valid values range from -infinity to infinity. Units are meters.
         */
        public static final ParameterDescriptor DX = createDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(CitationImpl.OGC,  "dx"),
                    new NamedIdentifier(CitationImpl.EPSG, "X-axis translation")
                },
                0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, SI.METER);

        /**
         * The operation parameter descriptor for the <cite>Y-axis translation</cite> ("dy")
         * parameter value. Valid values range from -infinity to infinity. Units are meters.
         */
        public static final ParameterDescriptor DY = createDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(CitationImpl.OGC,  "dy"),
                    new NamedIdentifier(CitationImpl.EPSG, "Y-axis translation")
                },
                0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, SI.METER);

        /**
         * The operation parameter descriptor for the <cite>Z-axis translation</cite> ("dz")
         * parameter value. Valid values range from -infinity to infinity. Units are meters.
         */
        public static final ParameterDescriptor DZ = createDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(CitationImpl.OGC,  "dz"),
                    new NamedIdentifier(CitationImpl.EPSG, "Z-axis translation")
                },
                0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, SI.METER);

        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new NamedIdentifier[] {
                new NamedIdentifier(CitationImpl.EPSG, "Geocentric translations"),
                new NamedIdentifier(CitationImpl.EPSG, "9603")
            }, new ParameterDescriptor[] {
                DX, DY, DZ,
                SRC_SEMI_MAJOR, SRC_SEMI_MINOR,
                TGT_SEMI_MAJOR, TGT_SEMI_MINOR,
                SRC_DIM, TGT_DIM
            });

        /**
         * Constructs a default provider.
         */
        public Provider() {
            this(PARAMETERS);
        }

        /**
         * Constructs a provider with the specified parameters.
         */
        Provider(final ParameterDescriptorGroup parameters) {
            super(3, 3, parameters);
        }

        /**
         * Returns the operation type.
         */
        protected Class getOperationType() {
            return Transformation.class;
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
            final BursaWolfParameters parameters = new BursaWolfParameters(null);
            fill(parameters, values);
            return concatenate(concatenate(new GeocentricTranslation(parameters, getParameters()),
                               values, SRC_SEMI_MAJOR, SRC_SEMI_MINOR, SRC_DIM),
                               values, TGT_SEMI_MAJOR, TGT_SEMI_MINOR, TGT_DIM);
        }

        /**
         * Fill the specified Bursa-Wolf parameters according the specified values.
         * This method is invoked automatically by {@link #createMathTransform}.
         */
        protected void fill(final BursaWolfParameters parameters, final ParameterValueGroup values) {
            parameters.dx  = doubleValue(DX,  values);
            parameters.dy  = doubleValue(DY,  values);
            parameters.dz  = doubleValue(DZ,  values);
        }

        /**
         * Concatenates the supplied transform with an "ellipsoid to geocentric" or a
         * "geocentric to ellipsod" step, if needed.
         */
        private static MathTransform concatenate(final MathTransform    transform,
                                                 final ParameterValueGroup values,
                                                 final ParameterDescriptor major,
                                                 final ParameterDescriptor minor,
                                                 final ParameterDescriptor dim)
        {
            double semiMajor = doubleValue(major, values);
            double semiMinor = doubleValue(minor, values);
            int    dimension =    intValue(dim,   values);
            switch (dimension) {
                case 0: if (Double.isNaN(semiMajor) && Double.isNaN(semiMinor)) return transform;
                case 2:        // Fall through for 0 and 2 cases.
                case 3: break; // The dimension is a valid value.
                default: throw new IllegalArgumentException(Resources.format(
                               ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, dim.getName().getCode(),
                               new Integer(dimension)));
            }
            ensureValid(major, semiMajor);
            ensureValid(minor, semiMinor);
            final GeocentricTransform step;
            step = new GeocentricTransform(semiMajor, semiMinor, SI.METER, dimension==3);
            // Note: dimension may be 0 if not user-provided, which is treated as 2.
            if (dim == SRC_DIM) {
                return ConcatenatedTransform.create(step, transform);
            } else {
                return ConcatenatedTransform.create(transform, step.inverse());
            }
        }

        /**
         * Ensures the the specified parameter is valid.
         */
        private static void ensureValid(final ParameterDescriptor param, double value) {
            if (!(value > 0)) {
                throw new IllegalStateException(Resources.format(
                          ResourceKeys.ERROR_MISSING_PARAMETER_$1, param.getName().getCode()));
            }
        }
    }

    /**
     * Base class for {@linkplain GeocentricTranslation geocentric affine transform} providers
     * with rotation terms. This base class is the provider for the "<cite>Position Vector 7-param.
     * transformation</cite>".
     *
     * @version $Id$
     * @author Martin Desruisseaux
     *
     * @since 2.2
     */
    public static class SevenParamProvider extends Provider {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = -6398226638364450229L;

        /**
         * The maximal value for a rotation, in arc-second.
         */
        private static final double MAX_ROTATION = 180*60*60;

        /**
         * The operation parameter descriptor for the <cite>X-axis rotation</cite> ("ex")
         * parameter value. Units are arc-seconds.
         */
        public static final ParameterDescriptor EX = createDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(CitationImpl.OGC,  "ex"),
                    new NamedIdentifier(CitationImpl.EPSG, "X-axis rotation")
                },
                0.0, -MAX_ROTATION, MAX_ROTATION, NonSI.SECOND_ANGLE);

        /**
         * The operation parameter descriptor for the <cite>Y-axis rotation</cite> ("ey")
         * parameter value. Units are arc-seconds.
         */
        public static final ParameterDescriptor EY = createDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(CitationImpl.OGC,  "ey"),
                    new NamedIdentifier(CitationImpl.EPSG, "Y-axis rotation")
                },
                0.0, -MAX_ROTATION, MAX_ROTATION, NonSI.SECOND_ANGLE);

        /**
         * The operation parameter descriptor for the <cite>Z-axis rotation</cite> ("ez")
         * parameter value. Units are arc-seconds.
         */
        public static final ParameterDescriptor EZ = createDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(CitationImpl.OGC,  "ez"),
                    new NamedIdentifier(CitationImpl.EPSG, "Z-axis rotation")
                },
                0.0, -MAX_ROTATION, MAX_ROTATION, NonSI.SECOND_ANGLE);

        /**
         * The operation parameter descriptor for the <cite>Scale difference</cite> ("ppm")
         * parameter value. Valid values range from -infinity to infinity. Units are parts
         * per million.
         */
        public static final ParameterDescriptor PPM = createDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(CitationImpl.OGC,  "ppm"),
                    new NamedIdentifier(CitationImpl.EPSG, "Scale difference")
                },
                0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Units.PPM);

        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS =
                        createDescriptorGroup("Position Vector 7-param. transformation", "9606");

        /**
         * Creates a parameters group.
         */
        static ParameterDescriptorGroup createDescriptorGroup(final String name, final String code) {
            return createDescriptorGroup(new NamedIdentifier[] {
                new NamedIdentifier(CitationImpl.EPSG, name),
                new NamedIdentifier(CitationImpl.EPSG, code)
            }, new ParameterDescriptor[] {
                DX, DY, DZ, EX, EY, EZ, PPM,
                SRC_SEMI_MAJOR, SRC_SEMI_MINOR,
                TGT_SEMI_MAJOR, TGT_SEMI_MINOR,
                SRC_DIM, TGT_DIM
            });
        }

        /**
         * Constructs a default provider.
         */
        public SevenParamProvider() {
            super(PARAMETERS);
        }

        /**
         * Constructs a provider with the specified parameters.
         */
        SevenParamProvider(final ParameterDescriptorGroup parameters) {
            super(parameters);
        }

        /**
         * Fill the specified Bursa-Wolf parameters according the specified values.
         */
        protected void fill(final BursaWolfParameters parameters, final ParameterValueGroup values) {
            super.fill(parameters, values);
            parameters.ppm = doubleValue(PPM, values);
            parameters.ex  = doubleValue(EX, values);
            parameters.ey  = doubleValue(EY, values);
            parameters.ez  = doubleValue(EZ, values);
        }
    }

    /**
     * {@linkplain GeocentricTranslation Geocentric affine transform} provider for
     * "<cite>Coordinate Frame rotation</cite>".
     *
     * @version $Id$
     * @author Martin Desruisseaux
     *
     * @since 2.2
     */
    public static class FrameRotationProvider extends SevenParamProvider {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID =  5513675854809530038L;

        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS =
                        createDescriptorGroup("Coordinate Frame rotation", "9607");

        /**
         * Constructs a default provider.
         */
        public FrameRotationProvider() {
            super(PARAMETERS);
        }

        /**
         * Fill the specified Bursa-Wolf parameters according the specified values.
         */
        protected void fill(final BursaWolfParameters parameters, final ParameterValueGroup values) {
            super.fill(parameters, values);
            parameters.ex = -parameters.ex;
            parameters.ey = -parameters.ey;
            parameters.ez = -parameters.ez;
        }
    }
}
