/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2003, Institut de Recherche pour le D�veloppement
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.gp;

// J2SE dependencies
import java.awt.RenderingHints;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.util.Range;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.ParameterListDescriptorImpl;
import javax.media.jai.EnumeratedParameter;
import javax.media.jai.OperationDescriptor;
import javax.media.jai.operator.MinFilterShape;
import javax.media.jai.operator.MaxFilterShape;
import javax.media.jai.operator.MedianFilterShape;
import javax.media.jai.operator.MinFilterDescriptor;
import javax.media.jai.operator.MaxFilterDescriptor;
import javax.media.jai.operator.MedianFilterDescriptor;

// Geotools dependencies
import org.geotools.gc.GridCoverage;
import org.geotools.cv.SampleDimension;
import org.geotools.cs.CoordinateSystem;


/**
 * Common super-class for filter operation. The following is adapted from OpenGIS specification:
 *
 * <blockquote>
 * Filtering is an enhancement operation that alters the grid values on the basis of the
 * neighborhood grid values. For this reason, filtering is considered to be a spatial or
 * area opeartion. There are many different filters that can be applied to a grid coverage
 * but the general concept of filtering is the same. A filter window or kernel is defined,
 * its dimension being an odd number in the x and y dimensions. Each cell in this window
 * contains a co-efficient or weighting factor representative of some mathmetical relationship.
 * A filtered grid coverage is generated by multipling each coefficient in the window by the
 * grid value in the original grid coverage corresponding to the window�s current location
 * and assigning the result to the central pixel location of the window in the filtered grid
 * coverage. The window is moved thoughout the grid coverage on pixel at a time. This window
 * multiplication process is known as convolution. A grid coverage contains both low and high
 * spatial information. High frequencies describe rapid change from one grid cell to another
 * such as roads or other boundary conditions. Low frequencies describe gradual change over a
 * large number of cells such as water bodies. High pass filters allow only high frequency
 * information to be generated in the new grid coverage Grid coverages generated with high
 * pass filters will show edge conditions. Low pass filters allow low frequency information
 * to be generated in the new grid coverage. The grid coverage produced from a filtering
 * operation will have the same dimension as the source grid coverage. To produce filtered
 * values around the edges of the source grid coverage, edge rows and columns will be
 * duplicated to fill a complete kernel.
 * </blockquote>
 *
 * @version $Id: FilterOperation.java,v 1.3 2003/05/13 10:59:52 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class FilterOperation extends OperationJAI {
    /**
     * Returns the default value for mask shape.
     */
    private static EnumeratedParameter getDefaultMaskShape(final String name) {
        if (name.equalsIgnoreCase(   "MinFilter")) return    MinFilterDescriptor.   MIN_MASK_SQUARE;
        if (name.equalsIgnoreCase(   "MaxFilter")) return    MaxFilterDescriptor.   MAX_MASK_SQUARE;
        if (name.equalsIgnoreCase("MedianFilter")) return MedianFilterDescriptor.MEDIAN_MASK_SQUARE;
        throw new IllegalArgumentException(name);
    }

    /**
     * Construct a new filter operation.
     *
     * @param name The operation name. Should be "MinFilter", "MaxFilter" or "MedianFilter".
     */
    public FilterOperation(final String name) {
        this(getOperationDescriptor(name), getDefaultMaskShape(name));
    }

    /**
     * Construct a new filter operation.
     *
     * @param descriptor The operation descriptor.
     * @param defaultShape The default mask shape. Should be an enumeration of kind
     *        {@link MinFilterShape}, {@link MaxFilterShape} or {@link MedianFilterShape}.
     *
     * @task TODO: The "SampleDimension" argument is not yet supported.
     */
    private FilterOperation(final OperationDescriptor descriptor,
                            final EnumeratedParameter defaultShape)
    {
        super(descriptor.getName(), descriptor, new ParameterListDescriptorImpl(
          descriptor,    // the object to be reflected upon for enumerated values.
          new String[] { // the names of each parameter.
              "Source",
           // "SampleDimension",
              "Xsize",
              "Ysize",
              "maskShape" // Not an OpenGIS parameter.
          },
          new Class[]   // the class of each parameter.
          {
              GridCoverage.class,
           // Integer.class,
              Integer.class,
              Integer.class,
              defaultShape.getClass()
          },
          new Object[] // The default values for each parameter.
          {
              ParameterListDescriptor.NO_PARAMETER_DEFAULT,
           // ZERO,
              THREE,
              THREE,
              defaultShape
          },
          new Object[] // Defines the valid values for each parameter.
          {
              null,
           // RANGE_0,
              RANGE_1,
              RANGE_1,
              null
          }));
    }

    /**
     * Set a parameter. This method override the {@link OperationJAI} method
     * in order to apply some conversions from OpenGIS to JAI parameter names.
     *
     * @param block The parameter block in which to set a parameter.
     * @param name  The parameter OpenGIS name.
     * @param value The parameter OpenGIS value.
     */
    void setParameter(final ParameterBlockJAI block, String name, final Object value) {
        if (name.equalsIgnoreCase("Xsize") ||
            name.equalsIgnoreCase("Ysize"))
        {
            name = "maskSize";
        }
        block.setParameter(name, value);
    }

    /**
     * Returns <code>false</code> since "min", "median" and "mode" filters can be applied
     * directly on sample values.
     *
     * @task HACK: This value would be incorrect for a "mean" filter. Okay for now,
     *             since the "mean" filter is not yet implemented.
     */
    boolean computeOnGeophysicsValues() {
        return false;
    }

    /**
     * Apply the operation.
     */
    protected GridCoverage doOperation(final ParameterList  parameters, RenderingHints hints) {
        final int xSize = parameters.getIntParameter("Xsize");
        final int ySize = parameters.getIntParameter("Ysize");
        if (xSize != ySize) {
            throw new UnsupportedOperationException("Xsize and Ysize must have the same value.");
        }
        return super.doOperation(parameters, hints);
    }

    /**
     * Returns the target sample dimensions, which are the same than source sample dimension.
     */
    protected SampleDimension[] deriveSampleDimension(final SampleDimension[][] bandLists,
                                                      final CoordinateSystem cs,
                                                      final ParameterList parameters)
    {
        return bandLists[0];
    }
}
