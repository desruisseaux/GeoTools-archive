/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2003, Institut de Recherche pour le Développement
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
package org.geotools.coverage.processing;

// J2SE and JAI dependencies
import java.util.Arrays;
import javax.media.jai.operator.BinarizeDescriptor;

// OpenGIS dependencies
import org.opengis.coverage.processing.OperationNotFoundException;

// Geotools dependencies
import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;


/**
 * Wraps any JAI operation producing a bilevel image. An example of such operation is
 * {@link BinarizeDescriptor Binarize}.
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class BilevelOperation extends OperationJAI {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8975871552152978976L;

    /**
     * The sample dimension for the resulting image.
     */
    private static final GridSampleDimension SAMPLE_DIMENSION = new GridSampleDimension(new Category[] {
        Category.FALSE,
        Category.TRUE
    }, null);

    /**
     * Constructs a bilevel operation with an OGC's name identical to the JAI name.
     *
     * @param name The JAI operation name.
     * @throws OperationNotFoundException if no JAI descriptor was found for the given name.
     */
    public BilevelOperation(final String name) throws OperationNotFoundException {
        super(name);
    }

    /**
     * Derives the {@link GridSampleDimension}s for the destination image.
     *
     * @param  bandLists Sample dimensions for each band in each source coverages.
     * @param  parameters The user-supplied parameters.
     * @return The sample dimensions for each band in the destination image.
     */
    protected GridSampleDimension[] deriveSampleDimension(final GridSampleDimension[][] bandLists,
                                                          final Parameters parameters)
    {
        final GridSampleDimension[] bands = new GridSampleDimension[bandLists[0].length];
        Arrays.fill(bands, SAMPLE_DIMENSION);
        return bands;
    }
}
