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
import java.util.Set;
import java.util.Locale;
import java.util.Arrays;
import java.util.Iterator;
import java.text.ParseException;
import javax.imageio.spi.ServiceRegistry;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

// Geotools dependencies
import org.geotools.parameter.Parameters;
import org.geotools.referencing.Identifier;         // For javadoc
import org.geotools.referencing.wkt.AbstractParser;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.referencing.operation.transform.PassThroughTransform;
import org.geotools.referencing.operation.transform.ConcatenatedTransform;
import org.geotools.referencing.wkt.MathTransformParser;

// Resources
import org.geotools.util.WeakHashSet;
import org.geotools.resources.LazySet;
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
     * The service registry for finding {@link MathTransformProvider} implementations.
     */
    private final ServiceRegistry registry;
    
    /**
     * Construct a default {@link MathTransform math transform} factory.
     */
    public MathTransformFactory() {
        this(new Class[] {MathTransformProvider.class});
    }

    /**
     * Construct a default {@link MathTransform math transform} factory using the
     * specified {@linkplain MathTransformProvider transform providers} categories.
     *
     * @param categories The providers categories, as implementations
     *                   of {@link MathTransformProvider}.
     */
    private MathTransformFactory(final Class[] categories) {
        registry = new ServiceRegistry(Arrays.asList(categories).iterator());
    }

    /**
     * Returns the vendor responsible for creating this factory implementation. Many implementations
     * may be available for the same factory interface. The default implementation returns
     * {@linkplain org.geotools.metadata.citation.Citation#GEOTOOLS Geotools}.
     *
     * @return The vendor for this factory implementation.
     */
    public Citation getVendor() {
        return org.geotools.metadata.citation.Citation.GEOTOOLS;
    }

    /**
     * Returns a set of all available {@linkplain MathTransform math transform} methods. Elements in
     * this set are {@linkplain org.geotools.referencing.operation.OperationMethod operation method}
     * objects. Their {@link Identifier#getCode identifier codes} are used as argument for
     * {@linkplain #createParameterizedTransform parameterized transforms creation}.
     *
     * @return All {@linkplain MathTransform math transform} methods available in this factory,
     *         as a set of {@link org.geotools.referencing.operation.OperationMethod} objects.
     */
    public Set getAvailableTransforms() {
        return new LazySet(getProviders(MathTransformProvider.class));
    }

    /**
     * Returns the providers for the specified category. This method will scan for plugin the
     * first time it will be invoked.
     */
    private synchronized Iterator getProviders(final Class category) {
        Iterator iterator = registry.getServiceProviders(category, false);
        if (!iterator.hasNext()) {
            /*
             * No plugin. This method is probably invoked the first time for the specified
             * category, otherwise we should have found at least the Geotools implementation.
             */
            scanForPlugins();
            iterator = registry.getServiceProviders(category, false);
        }
        return iterator;
    }

    /**
     * Returns the math transform provider for the specified operation method.
     * This provider can be used in order to query parameter for an identifier
     * code (e.g. <code>getProvider("Transverse_Mercator").getParameters()</code>),
     * or the transform name in a given locale (e.g.
     * <code>getProvider("Transverse_Mercator").getName({@link Locale#FRENCH})</code>).
     *
     * @param  identifier The case insensitive {@linkplain Identifier#getCode identifier code} of the
     *         operation method to search for (e.g. "Transverse_Mercator"). If this string contains
     *         the <code>':'</code> character, then the part before <code>':'</code> is the
     *         {@linkplain Identifier#getCodeSpace code space}.
     * @return The math transform provider.
     * @throws NoSuchIdentifierException if there is no provider registered for the specified
     *         operation method identifier.
     *
     * @todo Rename resource keys as "NO_TRANSFORM_FOR_IDENTIFIER_$1", and rename the
     *       message content too.
     */
    private MathTransformProvider getProvider(final String identifier)
            throws NoSuchIdentifierException
    {
        final Iterator providers = getProviders(MathTransformProvider.class);
        while (providers.hasNext()) {
            final MathTransformProvider provider = (MathTransformProvider) providers.next();
            if (provider.identifierMatches(identifier)) {
                return provider;
            }
        }
        throw new NoSuchIdentifierException(Resources.format(
                  ResourceKeys.ERROR_NO_TRANSFORM_FOR_CLASSIFICATION_$1, identifier), identifier);
    }
    
    /**
     * Returns the default parameter values for the specified operation method.
     * This method always returns clones. It is safe to modify the returned
     * parameter values and give them to
     * <code>{@linkplain #createParameterizedTransform createParameterizedTransform}(identifier, parameters)</code>.
     *
     * @param  identifier The case insensitive {@linkplain Identifier#getCode identifier code} of the
     *         operation method to search for (e.g. "Transverse_Mercator"). If this string contains
     *         the <code>':'</code> character, then the part before <code>':'</code> is the
     *         {@linkplain Identifier#getCodeSpace code space}.
     * @return The default parameter values.
     * @throws NoSuchIdentifierException if there is no transform registered for the specified
     *         operation method identifier.
     *
     * @see #getAvailableTransforms
     */
    public GeneralParameterValue[] getDefaultParameters(final String identifier)
            throws NoSuchIdentifierException
    {
        ParameterDescriptorGroup type = getProvider(identifier).getParameters();
        ParameterValueGroup group = (ParameterValueGroup) type.createValue();
        
        return Parameters.array( group );         
    }

    /**
     * Creates a transform from an {@linkplain org.geotools.referencing.operation.OperationMethod
     * operation method} identifier and parameters. The client must supply <code>"semi_major"</code>
     * and <code>"semi_minor"</code> parameters for cartographic projection transforms.
     *
     * @param  identifier The case insensitive {@linkplain Identifier#getCode identifier code} of the
     *         operation method to search for (e.g. "Transverse_Mercator"). If this string contains
     *         the <code>':'</code> character, then the part before <code>':'</code> is the
     *         {@linkplain Identifier#getCodeSpace code space}.
     * @param  parameters The parameter values. A default set can be obtained with
     *         <code>{@linkplain #getDefaultParameters getDefaultParameters}(identifier)}</code>
     *         and modified before to be given to this method.
     * @return The parameterized transform.
     * @throws NoSuchIdentifierException if there is no transform registered for the specified
     *         operation method identifier.
     * @throws FactoryException if the object creation failed. This exception is thrown
     *         if some required parameter has not been supplied, or has illegal value.
     *
     * @see #getDefaultParameters
     * @see #getAvailableTransforms
     */
    public MathTransform createParameterizedTransform(final String identifier,
                                                      final GeneralParameterValue[] parameters)
            throws FactoryException
    {
        final MathTransformProvider provider = getProvider(identifier);
        MathTransform tr;
        try {
            tr = provider.createMathTransform(parameters);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        tr = (MathTransform) pool.canonicalize(tr);
        return tr;
    }    
    
    /**
     * Creates an affine transform from a matrix.
     * If the transform's input dimension is <code>M</code>, and output dimension
     * is <code>N</code>, then the matrix will have size <code>[N+1][M+1]</code>.
     * The +1 in the matrix dimensions allows the matrix to do a shift, as well as
     * a rotation. The <code>[M][j]</code> element of the matrix will be the j'th
     * ordinate of the moved origin. The <code>[i][N]</code> element of the matrix
     * will be 0 for <var>i</var> less than <code>M</code>, and 1 for <var>i</var>
     * equals <code>M</code>.
     *
     * @param matrix The matrix used to define the affine transform.
     * @return The affine transform.
     * @throws FactoryException if the object creation failed.
     */
    public MathTransform createAffineTransform(final Matrix matrix)
            throws FactoryException
    {
        return (MathTransform) pool.canonicalize(ProjectiveTransform.create(matrix));
    }
    
    /**
     * Creates a transform by concatenating two existing transforms.
     * A concatenated transform acts in the same way as applying two
     * transforms, one after the other.
     *
     * The dimension of the output space of the first transform must match
     * the dimension of the input space in the second transform.
     * If you wish to concatenate more than two transforms, then you can
     * repeatedly use this method.
     *
     * @param  transform1 The first transform to apply to points.
     * @param  transform2 The second transform to apply to points.
     * @return The concatenated transform.
     * @throws FactoryException if the object creation failed.
     */
    public MathTransform createConcatenatedTransform(final MathTransform transform1,
                                                     final MathTransform transform2)
            throws FactoryException
    {
        MathTransform tr;
        try {
            tr = ConcatenatedTransform.create(transform1, transform2);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        tr = (MathTransform) pool.canonicalize(tr);
        return tr;
    }

    /**
     * Creates a transform which passes through a subset of ordinates to another transform.
     * This allows transforms to operate on a subset of ordinates. For example, if you have
     * (<var>Lat</var>,<var>Lon</var>,<var>Height</var>) coordinates, then you may wish to
     * convert the height values from meters to feet without affecting the
     * (<var>Lat</var>,<var>Lon</var>) values.
     *
     * @param  firstAffectedOrdinate The lowest index of the affected ordinates.
     * @param  subTransform Transform to use for affected ordinates.
     * @param  numTrailingOrdinates Number of trailing ordinates to pass through.
     *         Affected ordinates will range from <code>firstAffectedOrdinate</code>
     *         inclusive to <code>dimTarget-numTrailingOrdinates</code> exclusive.
     * @return A pass through transform with the following dimensions:<br>
     *         <pre>
     * Source: firstAffectedOrdinate + subTransform.getDimSource() + numTrailingOrdinates
     * Target: firstAffectedOrdinate + subTransform.getDimTarget() + numTrailingOrdinates</pre>
     * @throws FactoryException if the object creation failed.
     */
    public MathTransform createPassThroughTransform(final int firstAffectedOrdinate,
                                                    final MathTransform subTransform,
                                                    final int numTrailingOrdinates)
            throws FactoryException
    {
        MathTransform tr;
        try {
            tr = PassThroughTransform.create(firstAffectedOrdinate,
                                             subTransform,
                                             numTrailingOrdinates);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        tr = (MathTransform) pool.canonicalize(tr);
        return tr;
    }

    /**
     * Creates a math transform object from a XML string. The default implementation
     * always throws an exception, since this method is not yet implemented.
     *
     * @param  xml Math transform encoded in XML format.
     * @throws FactoryException if the object creation failed.
     */
    public MathTransform createFromXML(String xml) throws FactoryException {
        throw new FactoryException("Not yet implemented.");
    }    

    /**
     * Creates a math transform object from a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A>.
     *
     * @param  text Math transform encoded in Well-Known Text format.
     * @return The math transform (never <code>null</code>).
     * @throws FactoryException if the Well-Known Text can't be parsed,
     *         or if the math transform creation failed from some other reason.
     */
    public MathTransform createFromWKT(final String text) throws FactoryException {
        if (parser == null) {
            // Not a big deal if we are not synchronized. If this method is invoked in
            // same time by two different threads, we may have two WKTParser objects
            // for a short time. It doesn't hurt...
            parser = new MathTransformParser(Locale.US);
        }
        try {
            return (MathTransform) parser.parseObject(text);
        } catch (ParseException exception) {
            final Throwable cause = exception.getCause();
            if (cause instanceof FactoryException) {
                throw (FactoryException) cause;
            }
            throw new FactoryException(exception.getLocalizedMessage(), exception);
        }
    }

    /**
     * Scans for provider plug-ins on the application class path. This method is needed because the
     * application class path can theoretically change, or additional plug-ins may become available.
     * Rather than re-scanning the classpath on every invocation of the API, the class path is
     * scanned automatically only on the first invocation. Clients can call this method to prompt
     * a re-scan. Thus this method need only be invoked by sophisticated applications which
     * dynamically make new plug-ins available at runtime.
     *
     * @todo Provides the same logging mechanism than in {@link org.geotools.referencing.FactoryFinder}
     *       once it will be implemented in the later.
     */
    public synchronized void scanForPlugins() {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        for (final Iterator categories=registry.getCategories(); categories.hasNext();) {
            final Class category = (Class) categories.next();
            for (final Iterator providers=ServiceRegistry.lookupProviders(category, loader); providers.hasNext();) {
                registry.registerServiceProvider(providers.next(), category);
            }
        }
    }
}
