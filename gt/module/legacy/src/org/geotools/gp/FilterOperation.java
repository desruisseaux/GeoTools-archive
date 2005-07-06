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
 */
package org.geotools.gp;

// JAI dependencies
import javax.media.jai.OperationDescriptor;
import javax.media.jai.ParameterListDescriptor;

import org.geotools.cv.SampleDimension;


/**
 * Common super-class for filter operation. The following is adapted from OpenGIS specification:
 *
 * <blockquote>
 * Filtering is an enhancement operation that alters the grid values on the basis of the
 * neighborhood grid values. For this reason, filtering is considered to be a spatial or
 * area opeartion. There are many different filters that can be applied to a grid coverage
 * but the general concept of filtering is the same. A filter window or kernel is defined,
 * its dimension being an odd number in the <var>x</var> and <var>y</var> dimensions. Each
 * cell in this window contains a co-efficient or weighting factor representative of some
 * mathmetical relationship. A filtered grid coverage is generated by multipling each
 * coefficient in the window by the grid value in the original grid coverage corresponding
 * to the window�s current location and assigning the result to the central pixel location
 * of the window in the filtered grid coverage. The window is moved thoughout the grid coverage
 * on pixel at a time. This window multiplication process is known as convolution. A grid coverage
 * contains both low and high spatial information. High frequencies describe rapid change from one
 * grid cell to another such as roads or other boundary conditions. Low frequencies describe gradual
 * change over a large number of cells such as water bodies. High pass filters allow only high
 * frequency information to be generated in the new grid coverage Grid coverages generated with high
 * pass filters will show edge conditions. Low pass filters allow low frequency information
 * to be generated in the new grid coverage. The grid coverage produced from a filtering
 * operation will have the same dimension as the source grid coverage. To produce filtered
 * values around the edges of the source grid coverage, edge rows and columns will be
 * duplicated to fill a complete kernel.
 * </blockquote>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
class FilterOperation extends OperationJAI {
    /**
     * Construct a new filter operation.
     *
     * @param  name The operation name.
     * @throws OperationNotFoundException if no JAI descriptor was found for the given name.
     */
    public FilterOperation(final String name) throws OperationNotFoundException {
        super(name);
    }

    /**
     * Construct a new filter operation backed by a JAI operation.
     * The arguments are passed unchanged to super-class constructor.
     *
     * @param name The operation name for {@link GridCoverageProcessor} registration.
     * @param operationDescriptor The operation descriptor.
     * @param paramDescriptor The parameters descriptor, or <code>null</code>.
     *
     * @throws NullPointerException if <code>operationDescriptor</code> is null.
     */
    protected FilterOperation(final String name,
                              final OperationDescriptor operationDescriptor,
                              final ParameterListDescriptor paramDescriptor)
    {
        super(name, operationDescriptor, paramDescriptor);
    }

    /**
     * Returns the target sample dimensions. Since filter operation do not change the range of
     * values, this method returns the same sample dimension than the first source.
     */
    protected SampleDimension[] deriveSampleDimension(final SampleDimension[][] bandLists,
                                                      final Parameters parameters)
    {
        return bandLists[0];
    }
}
