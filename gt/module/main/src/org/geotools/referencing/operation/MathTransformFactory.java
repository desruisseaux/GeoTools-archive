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
package org.geotools.referencing.operation;

// J2SE dependencies
import java.util.Locale;
import java.text.ParseException;
import java.awt.geom.AffineTransform;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.parameter.GeneralParameterValue;

// Geotools dependencies
import org.geotools.referencing.wkt.AbstractParser;
import org.geotools.referencing.operation.GeneralMatrix;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.referencing.operation.transform.PassThroughTransform;
import org.geotools.referencing.operation.transform.ConcatenatedTransform;

// Resources
import org.geotools.util.WeakHashSet;
import org.geotools.resources.XArray;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Low level factory for creating {@linkplain MathTransform math transforms}.
 * Many high level GIS applications will never need to use this factory directly;
 * they can use a {@linkplain CoordinateOperationFactory coordinate operation factory}
 * instead. However, the <code>MathTransformFactory</code> interface can be used directly
 * by applications that wish to transform other types of coordinates (e.g. color coordinates,
 * or image pixel coordinates).
 * <br><br>
 * A {@linkplain MathTransform math transform} is an object that actually does
 * the work of applying formulae to coordinate values. The math transform does
 * not know or care how the coordinates relate to positions in the real world.
 * This lack of semantics makes implementing <code>MathTransformFactory</code>
 * significantly easier than it would be otherwise.
 *
 * For example the affine transform applies a matrix to the coordinates
 * without knowing how what it is doing relates to the real world. So if
 * the matrix scales <var>Z</var> values by a factor of 1000, then it could
 * be converting meters into millimeters, or it could be converting kilometers
 * into meters.
 * <br><br>
 * Because {@linkplain MathTransform math transforms} have low semantic value
 * (but high mathematical value), programmers who do not have much knowledge
 * of how GIS applications use coordinate systems, or how those coordinate
 * systems relate to the real world can implement <code>MathTransformFactory</code>.
 * The low semantic content of {@linkplain MathTransform math transforms} also
 * means that they will be useful in applications that have nothing to do with
 * GIS coordinates. For example, a math transform could be used to map color
 * coordinates between different color spaces, such as converting (red, green, blue)
 * colors into (hue, light, saturation) colors.
 * <br><br>
 * Since a {@linkplain MathTransform math transform} does not know what its source
 * and target coordinate systems mean, it is not necessary or desirable for a math
 * transform object to keep information on its source and target coordinate systems.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class MathTransformFactory implements org.opengis.referencing.operation.MathTransformFactory {
    /**
     * The object to use for parsing <cite>Well-Known Text</cite> (WKT) strings.
     * Will be created only when first needed.
     */
    private transient AbstractParser parser;
    
    /**
     * A pool of math transform. This pool is used in order to
     * returns instance of existing math transforms when possible.
     */
    private final WeakHashSet pool = new WeakHashSet();
    
    /**
     * List of registered math transforms.
     */
    private final MathTransformProvider[] providers;
    
    /**
     * Construct a factory using the specified providers.
     */
    public MathTransformFactory(final MathTransformProvider[] providers) {
        this.providers = (MathTransformProvider[]) providers.clone();
    }

    /**
     * Returns the vendor responsible for creating this factory implementation. Many implementations
     * may be available for the same factory interface. The default implementation returns
     * {@linkplain org.geotools.metadata.citation.Citation.GEOTOOLS Geotools}.
     *
     * @return The vendor for this factory implementation.
     */
    public Citation getVendor() {
        return org.geotools.metadata.citation.Citation.GEOTOOLS;
    }
    
    /**
     * Creates an affine transform from a matrix.
     *
     * @param matrix The matrix used to define the affine transform.
     * @return The affine transform.
     */
    public MathTransform2D createAffineTransform(final AffineTransform matrix) {
        return (MathTransform2D) pool.canonicalize(ProjectiveTransform.create(matrix));
    }
    
    /**
     * Creates an affine transform from a matrix.
     *
     * @param  matrix The matrix used to define the affine transform.
     * @return The affine transform.
     */
    public MathTransform createAffineTransform(final Matrix matrix) {
        return (MathTransform2D) pool.canonicalize(ProjectiveTransform.create(matrix));
    }
    
    /**
     * Creates a transform by concatenating two existing transforms.
     * A concatenated transform acts in the same way as applying two
     * transforms, one after the other. The dimension of the output
     * space of the first transform must match the dimension of the
     * input space in the second transform. If you wish to concatenate
     * more than two transforms, then you can repeatedly use this method.
     *
     * @param  tr1 The first transform to apply to points.
     * @param  tr2 The second transform to apply to points.
     * @return The concatenated transform.
     */
    public MathTransform createConcatenatedTransform(MathTransform tr1, MathTransform tr2) {
        return (MathTransform) pool.canonicalize(ConcatenatedTransform.create(tr1, tr2));
    }

    /**
     * Creates a transform which passes through a subset of ordinates to another transform.
     * This allows transforms to operate on a subset of ordinates. For example, if you have
     * (<var>latitidue</var>,<var>longitude</var>,<var>height</var>) coordinates, then you
     * may wish to convert the height values from feet to meters without affecting the
     * latitude and longitude values.
     *
     * @param  firstAffectedOrdinate Index of the first affected ordinate.
     * @param  subTransform The sub transform.
     * @param  numTrailingOrdinates Number of trailing ordinates to pass through.
     *         Affected ordinates will range from <code>firstAffectedOrdinate</code>
     *         inclusive to <code>dimTarget-numTrailingOrdinates</code> exclusive.
     * @return A pass through transform with the following dimensions:<br>
     *         <pre>
     * Source: firstAffectedOrdinate + subTransform.getDimSource() + numTrailingOrdinates
     * Target: firstAffectedOrdinate + subTransform.getDimTarget() + numTrailingOrdinates</pre>
     *
     * @see #createSubTransform
     */
    public MathTransform createPassThroughTransform(final int firstAffectedOrdinate,
                                                    final MathTransform subTransform,
                                                    final int numTrailingOrdinates)
    {
        return (MathTransform) pool.canonicalize(PassThroughTransform.create(firstAffectedOrdinate,
                                                                             subTransform,
                                                                             numTrailingOrdinates));
    }

    /**
     * Creates a transform from a classification name and parameters.
     * The client must ensure that all the linear parameters are expressed
     * in meters, and all the angular parameters are expressed in degrees.
     * Also, they must supply "semi_major" and "semi_minor" parameters
     * for cartographic projection transforms.
     *
     * @param  classification The classification name of the transform
     *         (e.g. "Transverse_Mercator"). Leading and trailing spaces
     *         are ignored, and comparaison is case-insensitive.
     * @param  parameters The parameter values in standard units.
     * @return The parameterized transform.
     * @throws NoSuchClassificationException if there is no transform for the specified
     *         classification.
     * @throws MissingParameterException if a parameter was required but not found.
     * @throws FactoryException if the math transform creation failed from some other reason.
     *
     * @see org.opengis.ct.CT_MathTransformFactory#createParameterizedTransform
     * @see #getAvailableTransforms
     */
//    public MathTransform createParameterizedTransform(String classification,
//                                                      final ParameterList parameters)
//            throws MissingParameterException, FactoryException
//    {
//        final MathTransform transform;
//        classification = classification.trim();
//        if (classification.equalsIgnoreCase("Affine")) {
//            return createAffineTransform(MatrixParameters.getMatrix(parameters));
//        }
//        transform = getMathTransformProvider(classification).create(parameters);
//        return (MathTransform) pool.canonicalize(transform);
//    }
    
    /**
     * Creates a transform from a projection. The client must ensure that all the linear
     * parameters are expressed in meters, and all the angular parameters are expressed
     * in degrees. Also, they must supply "semi_major" and "semi_minor" parameters for
     * cartographic projection transforms.
     *
     * @param  projection The projection.
     * @return The parameterized transform.
     * @throws NoSuchClassificationException if there is no transform for the specified projection.
     * @throws MissingParameterException if a parameter was required but not found.
     * @throws FactoryException if the math transform creation failed from some other reason.
     */
//    public MathTransform createParameterizedTransform(final Projection projection)
//            throws MissingParameterException, FactoryException
//    {
//        final MathTransform transform;
//        transform = getMathTransformProvider(projection.getClassName()).create(projection);
//        return (MathTransform) pool.canonicalize(transform);
//    }
    
    /**
     * Returns the provider for the specified classification. This provider
     * may be used to query parameter list for a classification name (e.g.
     * <code>getMathTransformProvider("Transverse_Mercator").getParameterList()</code>),
     * or the transform name in a given locale (e.g.
     * <code>getMathTransformProvider("Transverse_Mercator").getName({@link Locale#FRENCH})</code>)
     *
     * @param  classification The classification name of the transform
     *         (e.g. "Transverse_Mercator"). It should be one of the name
     *         returned by {@link #getAvailableTransforms}. Leading and
     *         trailing spaces are ignored. Comparisons are case-insensitive.
     * @return The provider for a math transform.
     * @throws NoSuchClassificationException if there is no provider registered
     *         with the specified classification name.
     */
//    public MathTransformProvider getMathTransformProvider(String classification)
//            throws NoSuchClassificationException
//    {
//        classification = classification.trim();
//        for (int i=0; i<providers.length; i++) {
//            if (classification.equalsIgnoreCase(providers[i].getClassName().trim())) {
//                return providers[i];
//            }
//        }
//        throw new NoSuchClassificationException(null, classification);
//    }

    /**
     * Creates a math transform object from a <cite>Well-Known Text</cite> (WKT) string.
     * WKT are part of <cite>Coordinate Transformation Services Specification</cite>.
     *
     * @param  text The <cite>Well-Known Text</cite>.
     * @return The math transform (never <code>null</code>).
     * @throws FactoryException if the Well-Known Text can't be parsed,
     *         or if the math transform creation failed from some other reason.
     */
    public MathTransform createFromWKT(final String text) throws FactoryException {
        if (parser == null) {
            // Not a big deal if we are not synchronized. If this method is invoked in
            // same time by two different threads, we may have two WKTParser objects
            // for a short time. It doesn't hurt...
//TODO            parser = new WKTParser(Locale.US, this);
        }
        return null; // TODO
//        try {
//            return parser.parseMathTransform(text);
//        } catch (ParseException exception) {
//            final Throwable cause = exception.getCause();
//            if (cause instanceof FactoryException) {
//                throw (FactoryException) cause;
//            }
//            throw new FactoryException(exception.getLocalizedMessage(), exception);
//        }
    }
    
    public MathTransform createFromXML(String xml) throws FactoryException {
        return null; // TODO
    }    
    
    public MathTransform createParameterizedTransform(String identifier,
                                                      GeneralParameterValue[] parameters)
            throws FactoryException
    {
        return null; // TODO
    }    
    
    public GeneralParameterValue[] getDefaultParameters(String identifier) throws NoSuchIdentifierException {
        return null; // TODO
    }
}
