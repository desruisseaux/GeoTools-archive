/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.gp;

// Java Advanced Imaging
import javax.media.jai.JAI;
import javax.media.jai.util.Range;
import javax.media.jai.ImageLayout;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.OperationDescriptor;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.ParameterListDescriptorImpl;
import javax.media.jai.registry.RenderedRegistryMode;

// Image (Java2D) and collections
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.util.Arrays;

// Geotools dependencies
import org.geotools.pt.Envelope;
import org.geotools.cv.Category;
import org.geotools.gc.GridCoverage;
import org.geotools.cv.SampleDimension;
import org.geotools.cs.CoordinateSystem;

// Resources
import org.geotools.units.Unit;
import org.geotools.resources.Utilities;
import org.geotools.resources.GCSUtilities;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * Wrap an {@link OperationDescriptor} for interoperability with
 * <A HREF="http://java.sun.com/products/java-media/jai/">Java Advanced
 * Imaging</A>. This class help to leverage the rich set of JAI operators
 * in an OpenGIS framework. <code>OperationJAI</code> inherits operation
 * name and argument types from {@link OperationDescriptor}, except source
 * argument type which is set to <code>{@link GridCoverage}.class</code>.
 * If there is only one source argument, il will be renamed "Source" for
 * better compliance to OpenGIS usage.
 * <br><br>
 * The entry point for applying operation is the usual <code>doOperation</code>
 * method. The default implementation forward the call to other methods for
 * different bits of tasks, resulting in the following chain of calls:
 *
 * <ol>
 *   <li>{@link #doOperation(ParameterList, RenderingHints)}</li>
 *   <li>{@link #doOperation(GridCoverage[], ParameterBlockJAI, RenderingHints)}</li>
 *   <li>{@link #deriveSampleDimension}</li>
 *   <li>{@link #deriveCategory}</li>
 *   <li>{@link #deriveUnit}</li>
 * </ol>
 *
 * Subclasses should override the two last <code>derive</code> methods. The
 * default implementation for other methods should be sufficient in most cases.
 *
 * @version $Id: OperationJAI.java,v 1.19 2003/05/19 13:09:48 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class OperationJAI extends Operation {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5974520239347639965L;

    /**
     * The rendered mode for JAI operation.
     */
    private static final String RENDERED_MODE = RenderedRegistryMode.MODE_NAME;
    
    /**
     * Index of the source {@link GridCoverage} to use as a model. The
     * destination grid coverage will reuse the same coordinate system,
     * envelope and qualitative categories than this "master" source.
     * <br><br>
     * For operations expecting only one source, there is no ambiguity.
     * But for operations expecting more than one source, the choice of
     * a "master" source is somewhat arbitrary.   This constant is used
     * merely as a flag for spotting those places in the code.
     */
    private static final int MASTER_SOURCE_INDEX = 0;
    
    /**
     * The operation descriptor.
     */
    protected final OperationDescriptor descriptor;
    
    /**
     * Construct an OpenGIS operation from a JAI operation name. This convenience constructor
     * fetch the {@link OperationDescriptor} from the specified operation name using the
     * default {@link JAI} instance.
     *
     * @param operationName JAI operation name (e.g. "GradientMagnitude").
     */
    public OperationJAI(final String operationName) {
        this(getOperationDescriptor(operationName));
    }
    
    /**
     * Construct an OpenGIS operation from a JAI operation descriptor.
     * The OpenGIS operation will have the same name than the JAI operation.
     *
     * @param descriptor The operation descriptor. This descriptor
     *        must supports the "rendered" mode (which is the case
     *        for most JAI operations).
     */
    public OperationJAI(final OperationDescriptor descriptor) {
        this(null, descriptor, null);
    }

    /**
     * Construct an OpenGIS operation backed by a JAI operation.
     *
     * @param name The operation name for {@link GridCoverageProcessor} registration.
     *        May or may not be the same than the JAI operation name. If <code>null</code>,
     *        then the JAI operation name is used.
     * @param operationDescriptor The operation descriptor. This descriptor must supports
     *        supports the "rendered" mode (which is the case for most JAI operations).
     * @param paramDescriptor The parameters descriptor. If <code>null</code>,
     *        then it will be infered from the JAI's parameter descriptor.
     *
     * @throws NullPointerException if <code>operationDescriptor</code> is null.
     */
    protected OperationJAI(final String name,
                           final OperationDescriptor operationDescriptor,
                           final ParameterListDescriptor paramDescriptor)
    {
        super((name!=null) ? name : operationDescriptor.getName(),
              (paramDescriptor!=null) ? paramDescriptor
                                      : getParameterListDescriptor(operationDescriptor));
        this.descriptor = operationDescriptor;
    }
    
    /**
     * V�rifie que la classe sp�cifi�e impl�mente l'interface {@link RenderedImage}.
     * Cette m�thode est utilis�e pour v�rifier les classes des images sources et
     * destinations.
     */
    private static final void ensureValid(final Class classe) throws IllegalArgumentException {
        if (!RenderedImage.class.isAssignableFrom(classe)) {
            throw new IllegalArgumentException(classe.getName());
        }
    }

    /**
     * Returns the operation descriptor for the specified JAI operation name.
     * This method uses the default {@link JAI} instance and looks for the
     * <code>&quot;rendered&quot;</code> mode.
     */
    protected static OperationDescriptor getOperationDescriptor(final String name) {
        return (OperationDescriptor) JAI.getDefaultInstance().
                getOperationRegistry().getDescriptor(RENDERED_MODE, name);
    }
    
    /**
     * Gets the parameter list descriptor for an operation descriptor.
     * {@link OperationDescriptor} parameter list do not include sources.
     * This method will add them in front of the parameter list.
     */
    private static ParameterListDescriptor getParameterListDescriptor
                                (final OperationDescriptor descriptor)
    {
        ensureValid(descriptor.getDestClass(RENDERED_MODE));
        final Class[] sourceClasses = descriptor.getSourceClasses(RENDERED_MODE);
        for (int i=0; i<sourceClasses.length; i++) {
            ensureValid(sourceClasses[i]);
        }
        
        final ParameterListDescriptor parent = descriptor.getParameterListDescriptor(RENDERED_MODE);
        final String[] sourceNames    = getSourceNames(descriptor);
        final String[] parentNames    = parent.getParamNames();
        final Class [] parentClasses  = parent.getParamClasses();
        final Object[] parentDefaults = parent.getParamDefaults();
        
        final int    numSources = descriptor.getNumSources();
        final String[]    names = new String[length(parentNames   ) + numSources];
        final Class []  classes = new Class [length(parentClasses ) + numSources];
        final Object[] defaults = new Object[length(parentDefaults) + numSources];
        final Range[]    ranges = new Range [defaults.length];
        for (int i=0; i<ranges.length; i++) {
            if (i<numSources) {
                names   [i] = sourceNames[i];
                classes [i] = GridCoverage.class;
                defaults[i] = ParameterListDescriptor.NO_PARAMETER_DEFAULT;
            } else {
                names   [i] = parentNames   [i-numSources];
                classes [i] = parentClasses [i-numSources];
                defaults[i] = parentDefaults[i-numSources];
                ranges  [i] = parent.getParamValueRange(names[i]);
            }
        }
        return new ParameterListDescriptorImpl(descriptor, names, classes, defaults, ranges);
    }

    /**
     * Returns the array length, of 0 if the array is <code>null</code>.
     */
    private static int length(final Object[] array) {
        return (array!=null) ? array.length : 0;
    }
    
    /**
     * Returns source name for the specified descriptor. If the descriptor has
     * only one source,  it will be renamed "Source" for better conformance to
     * to OpenGIS usage.
     */
    private static String[] getSourceNames(final OperationDescriptor descriptor) {
        if (descriptor.getNumSources()==1) {
            return new String[] {"Source"};
        } else {
            return descriptor.getSourceNames();
        }
    }
    
    /**
     * Check if array <code>names</code> contains the element <code>name</code>.
     * Search is done in case-insensitive manner. This method is efficient enough
     * if <code>names</code> is very short (less than 10 entries).
     */
    private static boolean contains(final String[] names, final String name) {
        for (int i=0; i<names.length; i++) {
            if (name.equalsIgnoreCase(names[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns <code>true</code> if grid coverage should be transformed from sample values
     * to geophysics value before to apply an operation.
     */
    boolean computeOnGeophysicsValues() {
        return true;
    }

    /**
     * Set a parameter. This method can be overriden in order to apply some conversions
     * from OpenGIS to JAI parameters.
     *
     * @param block The parameter block in which to set a parameter.
     * @param name  The parameter OpenGIS name.
     * @param value The parameter OpenGIS value.
     */
    void setParameter(final ParameterBlockJAI block, final String name, final Object value) {
        block.setParameter(name, value);
    }

    /**
     * Apply a process operation to a grid coverage.
     * The default implementation performs the following steps:
     *
     * <ul>
     *   <li>Separate the source {@link GridCoverage}s from other parameters.</li>
     *   <li>Convert source grid coverages to their <cite>geophysics</cite> model, using
     *       <code>{@link GridCoverage#geophysics GridCoverage.geophysics}(true)</code>.
     *       This allow to performs all computation on geophysics values instead of encoded
     *       samples.</li>
     *   <li>Invoke {@link #doOperation(GridCoverage[], ParameterBlockJAI, RenderingHints)}.
     *       The sources in the <code>ParameterBlock</code> are {@link RenderedImage} objects
     *       obtained with {@link GridCoverage#getRenderedImage()}.</li>
     *   <li>If the source <code>GridCoverage</code>s was not a geophysics model, convert the
     *       result back to the same type with <code>GridCoverage.geophysics}(false)</code>.</li>
     * </ul>
     *
     * @param  parameters List of name value pairs for the
     *         parameters required for the operation.
     * @param  A set of rendering hints, or <code>null</code> if none.
     * @return The result as a grid coverage.
     *
     * @see #doOperation(GridCoverage[], ParameterBlockJAI, RenderingHints)
     */
    protected GridCoverage doOperation(final ParameterList  parameters,
                                       final RenderingHints hints)
    {
        Boolean requireGeophysicsType = null;
        final ParameterBlockJAI block = new ParameterBlockJAI(descriptor, RENDERED_MODE);
        final String[]     paramNames = parameters.getParameterListDescriptor().getParamNames();
        final String[]    sourceNames = getSourceNames(descriptor);
        final GridCoverage[]  sources = new GridCoverage[descriptor.getNumSources()];
        for (int srcCount=0,i=0; i<paramNames.length; i++) {
            final String name  = paramNames[i];
            final Object param = parameters.getObjectParameter(name);
            if (contains(sourceNames, name)) {
                GridCoverage source = ((GridCoverage) param);
                if (computeOnGeophysicsValues()) {
                    final GridCoverage old = source;
                    source = source.geophysics(true);
                    if (srcCount == MASTER_SOURCE_INDEX) {
                        requireGeophysicsType = (old==source) ? Boolean.TRUE : Boolean.FALSE;
                    }
                }
                block.addSource(source.getRenderedImage());
                sources[srcCount++] = source;
            } else {
                setParameter(block, name, param);
            }
        }
        GridCoverage result = doOperation(sources, block, hints);
        if (requireGeophysicsType != null) {
            result = result.geophysics(requireGeophysicsType.booleanValue());
        }
        return result;
    }
    
    /**
     * Apply a JAI operation to a grid coverage.
     * The default implementation performs the following steps:
     *
     * <ul>
     *   <li>Ensure that every source <code>GridCoverage</code>s use the same coordinate
     *       system and have the same envelope.</li>
     *   <li>Get the {@link SampleDimension}s for the target images by invoking the
     *       {@link #deriveSampleDimension deriveSampleDimension(...)} method.</li>
     *   <li>Apply the operation using the following pseudo-code:
     * <blockquote><pre>
     * {@link JAI#createNS JAI.createNS}({@link #descriptor}.getName(),&nbsp;parameters,&nbsp;hints)
     * </pre></blockquote></li>
     * </ul>
     *
     * @param  sources The source coverages.
     * @param  parameters List of name value pairs for the
     *         parameters required for the operation.
     * @param  A set of rendering hints, or <code>null</code> if none.  The JAI
     *         instance to use for the <code>createNS</code> call will be fetch
     *         from the {@link Hints#JAI_INSTANCE} key.
     * @return The result as a grid coverage.
     *
     * @see #doOperation(ParameterList, RenderingHints)
     * @see #deriveSampleDimension
     * @see JAI#createNS
     */
    protected GridCoverage doOperation(final GridCoverage[]    sources,
                                       final ParameterBlockJAI parameters,
                                       final RenderingHints    hints)
    {
              GridCoverage source = sources[MASTER_SOURCE_INDEX];
        final CoordinateSystem cs = source.getCoordinateSystem();
        final Envelope   envelope = source.getEnvelope();
        /*
         * Ensure that all coverages use the same
         * coordinate system and has the same envelope.
         */
        for (int i=1; i<sources.length; i++) {
            if (!cs.equals(sources[i].getCoordinateSystem(), false)) {
                throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_INCOMPATIBLE_COORDINATE_SYSTEM));
            }
            if (!envelope.equals(sources[i].getEnvelope())) {
                throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_ENVELOPE_MISMATCH));
            }
        }
        /*
         * Get the target SampleDimensions. If they are identical to the SampleDimensions of
         * one of the source GridCoverage, then this GridCoverage will be use at the source.
         * It will affect the target GridCoverage's name and the visible band.  Then, a new
         * color model will be constructed from the new SampleDimensions, taking in account
         * the visible band.
         */
        final SampleDimension[][] list = new SampleDimension[sources.length][];
        for (int i=0; i<list.length; i++) {
            list[i] = sources[i].getSampleDimensions();
        }
        final SampleDimension[] sampleDims = deriveSampleDimension(list, cs, parameters);
        for (int i=0; i<list.length; i++) {
            if (Arrays.equals(sampleDims, list[i])) {
                source = sources[i];
                break;
            }
        }
        ImageLayout layout = new ImageLayout();
        if (sampleDims!=null && sampleDims.length!=0) {
            int visibleBand = GCSUtilities.getVisibleBand(source.getRenderedImage());
            if (visibleBand >= sampleDims.length) {
                visibleBand = 0;
            }
            final ColorModel colors;
            colors = sampleDims[visibleBand].getColorModel(visibleBand, sampleDims.length);
            layout = layout.setColorModel(colors);
        }
        /*
         * Performs the operation using JAI and construct the new grid coverage.
         */
        final RenderingHints exHints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
        final JAI processor = getJAI(hints);
        if (hints != null) {
            exHints.add(hints); // May overwrite the image layout we just set.
        }
        final RenderedImage data = processor.createNS(descriptor.getName(), parameters, exHints);
        return new GridCoverage(deriveName(source),   // The grid coverage name
                                data,                 // The underlying data
                                cs,                   // The coordinate system.
                                envelope,             // The coverage envelope.
                                sampleDims,           // The sample dimensions
                                sources,              // The source grid coverages.
                                null);                // Properties
    }
    
    /**
     * Returns the index of the quantitative category, providing that there
     * is one and only one quantitative category. If <code>categories</code>
     * contains 0, 2 or more quantative category, then this method returns
     * <code>-1</code>.
     */
    private static int getQuantitative(final Category[] categories) {
        int index = -1;
        for (int i=0; i<categories.length; i++) {
            if (categories[i].isQuantitative()) {
                if (index >= 0) {
                    return -1;
                }
                index = i;
            }
        }
        return index;
    }

    /**
     * Returns a name for the target {@linkplain GridCoverage grid coverage} based on the given
     * source. The default implementation returns the operation name followed by the source name
     * in parenthesis, as in a method call.
     *
     * @param  source The source grid coverage.
     * @return A name for the target grid coverage.
     */
    protected String deriveName(final GridCoverage source) {
        return getName()+'('+source.getName(null)+')';
    }
    
    /**
     * Derive the {@link SampleDimension}s for the destination image. The
     * default implementation iterate among all bands  and invokes the
     * {@link #deriveCategory deriveCategory} and {@link #deriveUnit deriveUnit}
     * methods for each individual band.
     *
     * @param  bandLists {@link SampleDimension}s for each band in each source
     *         <code>GridCoverage</code>s. For a band (or "sample dimension")
     *         <code>band</code> in a source coverage <code>source</code>, the
     *         corresponding <code>SampleDimension</code> is
     *
     *                 <code>bandLists[source][band]</code>.
     *
     * @param  cs The coordinate system of the destination grid coverage.
     * @param  parameters The user-supplied parameters.
     * @return The category lists for each band in the destination image. The
     *         length of this array must matches the number of bands in the
     *         destination image. If the <code>SampleDimension</code>s are unknow,
     *         then this method may returns <code>null</code>.
     *
     * @see #deriveCategory
     * @see #deriveUnit
     */
    protected SampleDimension[] deriveSampleDimension(final SampleDimension[][] bandLists,
                                                      final CoordinateSystem cs,
                                                      final ParameterList parameters)
    {
        /*
         * Compute the number of bands. Sources with only 1 band are treated as
         * a special case:  their unique band is applied to every band in other
         * sources.   If sources don't have the same number of bands, then this
         * method returns  <code>null</code>  since we don't know how to handle
         * those cases.
         */
        int numBands = 1;
        for (int i=0; i<bandLists.length; i++) {
            final int nb = bandLists[i].length;
            if (nb != 1) {
                if (numBands!=1 && nb!=numBands) {
                    return null;
                }
                numBands = nb;
            }
        }
        /*
         * Iterate among all bands. The 'result' array will contains
         * SampleDimensions constructed during the iteration for each
         * individual band. The 'XS' suffix designate temporary arrays
         * of categories and units accross all sources for one particular
         * band.
         */
        final SampleDimension[] result = new SampleDimension[numBands];
        final Category[]    categoryXS = new Category[bandLists.length];
        final Unit[]            unitXS = new Unit[bandLists.length];
        while (--numBands >= 0) {
            SampleDimension sampleDim = null;
            Category[]  categoryArray = null;
            int   indexOfQuantitative = 0;
            assert MASTER_SOURCE_INDEX == 0; // See comment below.
            for (int i=bandLists.length; --i>=0;) {
                /*
                 * Iterate among all sources (i) for the current band. We iterate
                 * sources in reverse order because the master source MUST be the
                 * last one iterated, in order to have proper value for variables
                 * 'sampleDim', 'categoryArray'  and  'indexOfQuantitative' after
                 * the loop.
                 */
                final SampleDimension[] allBands = bandLists[i];
                sampleDim           = allBands[allBands.length==1 ? 0 : numBands];
                categoryArray       = (Category[]) sampleDim.getCategories().toArray();
                indexOfQuantitative = getQuantitative(categoryArray);
                if (indexOfQuantitative < 0) {
                    return null;
                }
                unitXS    [i] = sampleDim.getUnits();
                categoryXS[i] = categoryArray[indexOfQuantitative];
            }
            final Category oldCategory = categoryArray[indexOfQuantitative];
            final Unit     oldUnit     = sampleDim.getUnits();
            final Category newCategory = deriveCategory(categoryXS, cs, parameters);
            final Unit     newUnit     = deriveUnit(unitXS, cs, parameters);
            if (newCategory == null) {
                return null;
            }
            if (!oldCategory.equals(newCategory) || !Utilities.equals(oldUnit, newUnit)) {
                categoryArray[indexOfQuantitative] = newCategory;
                result[numBands] = new SampleDimension(categoryArray, newUnit);
            } else {
                // Reuse the category list from the master source.
                result[numBands] = sampleDim;
            }
        }
        return result;
    }
    
    /**
     * Derive the quantative category for a band in the destination image.
     * This method is invoked automatically by the {@link #deriveSampleDimension
     * deriveSampleDimension} method for each band in the destination image. The
     * default implementation always returns <code>null</code>. Subclasses
     * should override this method in order to compute the destination {@link
     * Category} from the source categories. For example, the "<code>add</code>"
     * operation may implement this method as below:
     *
     * <blockquote><pre>
     * Range r0 = categories[0].getRange();
     * Range r1 = categories[0].getRange();
     * double min = ((Number)r0.getMinValue()).doubleValue() + ((Number)r1.getMinValue()).doubleValue();
     * double min = ((Number)r0.getMaxValue()).doubleValue() + ((Number)r1.getMaxValue()).doubleValue();
     * final Range newRange = new Range(Double.class, new Double(min), new Double(max));
     * return new Category("My category", null, r0, newRange);
     * </pre></blockquote>
     *
     * @param  categories The quantitative categories from every sources.
     *         For unary operations like "GradientMagnitude", this array
     *         has a length of 1. For binary operations like "add" and
     *         "multiply", this array has a length of 2.
     * @param  cs The coordinate system of the destination grid coverage.
     * @param  parameters The user-supplied parameters.
     * @return The quantative category to use in the destination image.
     *         or <code>null</code> if unknow. This category should always
     *         be derived from <code>categories[0]</code>.
     */
    protected Category deriveCategory(final Category[] categories,
                                      final CoordinateSystem cs,
                                      final ParameterList parameters)
    {
        return null;
    }
    
    /**
     * Derive the unit of data for a band in the destination image. This method is
     * invoked automatically by the {@link #deriveSampleDimension deriveSampleDimension}
     * method for each band in the destination image.   The default implementation
     * always returns <code>null</code>. Subclasses should override this method in
     * order to compute the destination units from the source units.  For example,
     * the "<code>multiply</code>" operation may implement this method as below:
     *
     * <blockquote><pre>
     * if (units[0]!=null && units[1]!=null) {
     *     return units[0].{@link Unit#multiply(Unit) multiply}(units[1]);
     * } else {
     *     return super.deriveUnit(units, cs, parameters);
     * }
     * </pre></blockquote>
     *
     * @param  units The units from every sources. For unary operations like
     *         "GradientMagnitude", this array has a length of 1. For binary
     *         operations like "add" and "multiply", this array has a length
     *         of 2.
     * @param  cs The coordinate system of the destination grid coverage.
     * @param  parameters The user-supplied parameters.
     * @return The unit of data in the destination image,
     *         or <code>null</code> if unknow.
     */
    protected Unit deriveUnit(final Unit[] units,
                              final CoordinateSystem cs,
                              final ParameterList parameters)
    {
        return null;
    }
    
    /**
     * Compares the specified object with this operation for equality.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            // Slight optimisation
            return true;
        }
        if (super.equals(object)) {
            final OperationJAI that = (OperationJAI) object;
            return Utilities.equals(this.descriptor, that.descriptor);
        }
        return false;
    }
}
