/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
package org.geotools.referencing.operation;

// J2SE dependencies
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import javax.imageio.spi.ServiceRegistry;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.OperationMethod;

// Geotools dependencies
import org.geotools.parameter.ParameterWriter;
import org.geotools.referencing.IdentifiedObject;
import org.geotools.referencing.Identifier;
import org.geotools.referencing.operation.transform.AbstractMathTransform;
import org.geotools.referencing.operation.transform.ConcatenatedTransform;
import org.geotools.referencing.operation.transform.PassThroughTransform;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.referencing.wkt.MathTransformParser;
import org.geotools.referencing.wkt.Symbols;
import org.geotools.resources.Arguments;
import org.geotools.resources.LazySet;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;
import org.geotools.util.WeakHashSet;


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
    private transient MathTransformParser parser;

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
        // TODO: remove the cast when we will be allowed to compile for J2SE 1.5.
        registry = new ServiceRegistry((Iterator) Arrays.asList(categories).iterator());
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
     * Returns a set of all available {@linkplain MathTransform math transform} methods. For each
     * element in this set, the {@linkplain OperationMethod#getName operation method name} will be
     * a classification name known to the {@link #getDefaultParameters} method in this factory.
     *
     * @return All {@linkplain MathTransform math transform} methods available in this factory.
     *
     * @see #getDefaultParameters
     * @see #createParameterizedTransform
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
     * This provider can be used in order to query parameter for a classification
     * code (e.g. <code>getProvider("Transverse_Mercator").getParameters()</code>),
     * or any of the alias in a given locale.
     *
     * @param  classification The case insensitive {@linkplain Identifier#getCode identifier code}
     *         of the operation method to search for (e.g. <code>"Transverse_Mercator"</code>).
     * @return The math transform provider.
     * @throws NoSuchIdentifierException if there is no provider registered for the specified
     *         classification.
     */
    private MathTransformProvider getProvider(final String classification)
            throws NoSuchIdentifierException
    {
        final Iterator providers = getProviders(MathTransformProvider.class);        
        while (providers.hasNext()) {
            final MathTransformProvider provider = (MathTransformProvider) providers.next();            
            if (provider.nameMatches(classification)) {
                return provider;
            }
        }
        throw new NoSuchIdentifierException(Resources.format(
                  ResourceKeys.ERROR_NO_TRANSFORM_FOR_CLASSIFICATION_$1, classification),
                  classification);
    }
    
    /**
     * Returns the default parameter values for a math transform of the given classification.
     * The classification may be the name of any operation method returned by the
     * {@link #getAvailableTransforms} method. A typical example is
     * <code>"<A HREF="http://www.remotesensing.org/geotiff/proj_list/transverse_mercator.html">Transverse_Mercator</A>"</code>).
     *
     * <P>The {@link #createParameterizedTransform createParameterizedTransform} method
     * in this factory shall be able to infer the classification from the parameter group
     * returned by this method. For this purpose, the current implementation set the
     * {@linkplain ParameterDescriptorGroup#getName parameter group name} to the classification
     * name.</P>
     *
     * <P>This method creates new parameter instances at every call. It is intented to be modified
     * by the user before to be passed to <code>{@linkplain #createParameterizedTransform
     * createParameterizedTransform}(parameters)</code>.</P>
     *
     * @param  classification The case insensitive classification to search for.
     * @return The default parameter values.
     * @throws NoSuchIdentifierException if there is no transform registered for the specified
     *         classification.
     *
     * @see #getAvailableTransforms
     * @see #createParameterizedTransform
     * @see org.geotools.referencing.operation.transform.AbstractMathTransform#getParameterValues
     */
    public ParameterValueGroup getDefaultParameters(final String classification)
            throws NoSuchIdentifierException
    {
        // Remove the cast when we will be allowed to compile for J2SE 1.5.
        return (ParameterValueGroup) getProvider(classification).getParameters().createValue();
    }

    /**
     * Creates a transform from a group of parameters. The classification name is inferred from
     * the {@linkplain ParameterDescriptorGroup#getName parameter group name}. Example:
     *
     * <blockquote><pre>
     * ParameterValueGroup p = factory.getDefaultParameters("Transverse_Mercator");
     * p.parameter("semi_major").setValue(6378137.000);
     * p.parameter("semi_minor").setValue(6356752.314);
     * MathTransform mt = factory.createParameterizedTransform(p);
     * </pre></blockquote>
     *
     * @param  parameters The parameter values.
     * @return The parameterized transform.
     * @throws NoSuchIdentifierException if there is no transform registered for the classification.
     * @throws FactoryException if the object creation failed. This exception is thrown
     *         if some required parameter has not been supplied, or has illegal value.
     *
     * @see #getDefaultParameters
     * @see #getAvailableTransforms
     */
    public MathTransform createParameterizedTransform(ParameterValueGroup parameters)
            throws FactoryException
    {
        final String classification = parameters.getDescriptor().getName().getCode();
        final MathTransformProvider provider = getProvider(classification);
        MathTransform tr;
        try {
            parameters = provider.ensureValidValues(parameters);
            tr = provider.createMathTransform(parameters);
        } catch (IllegalArgumentException exception) {
            /*
             * Catch only exceptions which may be the result of improper parameter
             * usage (e.g. a value out of range). Do not catch exception caused by
             * programming errors (e.g. null pointer).
             */
            throw new FactoryException(exception);
        }
        if (tr instanceof AbstractMathTransform) {
            ((AbstractMathTransform) tr).method = provider;
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
     * Source: firstAffectedOrdinate + subTransform.getSourceDimensions() + numTrailingOrdinates
     * Target: firstAffectedOrdinate + subTransform.getTargetDimensions() + numTrailingOrdinates</pre>
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
    public synchronized MathTransform createFromWKT(final String text) throws FactoryException {
        // Note: while this factory is thread safe, the WKT parser is not.
        //       Since we share a single instance of this parser, we must
        //       synchronize.
        if (parser == null) {
            parser = new MathTransformParser(Symbols.DEFAULT, this);
        }
        try {
            return parser.parseMathTransform(text);
        } catch (ParseException exception) {
            final Throwable cause = exception.getCause();
            if (cause instanceof FactoryException) {
                throw (FactoryException) cause;
            }
            throw new FactoryException(exception);
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

    /**
     * Dump to the standard output stream a list of available math transforms.
     * This method can be invoked from the command line. It provides a mean to
     * verify which transforms were found in the classpath. The syntax is:
     * <BR>
     * <BLOCKQUOTE><CODE>
     * java org.geotools.referencing.operation.MathTransformFactory
     * <VAR>&lt;options&gt;</VAR> <VAR>&lt;classification&gt;</VAR>
     * </CODE></BLOCKQUOTE>
     *
     * <P>where options are:</P>
     *
     * <TABLE CELLPADDING='0' CELLSPACING='0'>
     *   <TR><TD NOWRAP><CODE>-all</CODE></TD>
     *       <TD NOWRAP>&nbsp;List the parameters for all transforms</TD></TR>
     *   <TR><TD NOWRAP><CODE>-encoding</CODE> <VAR>&lt;code&gt;</VAR></TD>
     *       <TD NOWRAP>&nbsp;Set the character encoding</TD></TR>
     *   <TR><TD NOWRAP><CODE>-locale</CODE> <VAR>&lt;language&gt;</VAR></TD>
     *       <TD NOWRAP>&nbsp;Set the language for the output (e.g. "fr" for French)</TD></TR>
     * </TABLE>
     *
     * <P>and <VAR>&lt;classification&gt;</VAR> is the optional name of a math
     * transform (e.g. <CODE>"Affine"</CODE>, <CODE>"EPSG:9624"</CODE> or just
     * <CODE>"9624"</CODE> for the affine transform).</P>
     *
     * <P><strong>Note for Windows users:</strong> If the output contains strange
     * symbols, try to supply an "<code>-encoding</code>" argument. Example:</P>
     *
     * <blockquote><code>
     * java org.geotools.referencing.operation.MathTransformFactory -encoding Cp850
     * </code></blockquote>
     *
     * <P>The codepage number (850 in the previous example) can be obtained from the DOS
     * commande line using the "<code>chcp</code>" command with no arguments.
     * This <code>-encoding</code> argument need to be supplied only once.</P>
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        /*
         * Parse the command-line arguments and print the summary.
         */
        final Arguments arguments = new Arguments(args);
        final boolean printAll = arguments.getFlag("-all");
        args = arguments.getRemainingArguments(1);
        try {
            final MathTransformFactory factory = new MathTransformFactory();
            final ParameterWriter writer = new ParameterWriter(arguments.out);
            writer.setLocale(arguments.locale);
            Set transforms = Collections.EMPTY_SET;
            if (printAll || args.length==0) {
                transforms = new TreeSet(IdentifiedObject.NAME_COMPARATOR);
                transforms.addAll(factory.getAvailableTransforms());
                writer.summary(transforms);
            }
            if (!printAll) {
                if (args.length == 0) {
                    transforms = Collections.EMPTY_SET;
                } else {
                    transforms = Collections.singleton(factory.getProvider(args[0]));
                }
            }
            /*
             * Iterates through all math transform to print. It may be a singleton
             * if the user ask for a specific math transform.
             */
            final Iterator it = transforms.iterator();
            final String lineSeparator = System.getProperty("line.separator", "\n");
            while (it.hasNext()) {
                arguments.out.write(lineSeparator);
                writer.format((OperationMethod) it.next());
            }
        } catch (NoSuchIdentifierException exception) {
            arguments.err.println(exception.getLocalizedMessage());
            return;
        } catch (Exception exception) {
            exception.printStackTrace(arguments.err);
            return;
        }
        arguments.out.flush();
    }
}
