/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Management Committee (PMC)
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
package org.geotools.coverage.operation;

// J2SE dependencies
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

// JAI dependencies
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.WritablePropertySource;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterDescriptor;

// Geotools dependencies
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.Operation2D;
import org.geotools.coverage.processing.OperationJAI;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.resources.GCSUtilities;
import org.geotools.resources.image.ColorUtilities;


/**
 * A grid coverage containing a subset of an other grid coverage's sample dimensions,
 * and/or a different {@link ColorModel}. A common reason for changing the color model
 * is to select a different visible band. Consequently, the {@code "SelectSampleDimension"}
 * operation name still appropriate in this context.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.2
 */
public final class BandSelect2D extends GridCoverage2D {
    /**
     * The mapping to bands in the source grid coverage.
     * May be {@code null} if all bands were keept.
     */
    private final int[] bandIndices;

    /**
     * Constructs a new {@code BandSelect2D} grid coverage. This grid coverage will use
     * the same coordinate reference system and the same geometry than the source grid
     * coverage.
     *
     * @param source      The source coverage.
     * @param image       The image to use.
     * @param bands       The sample dimensions to use.
     * @param bandIndices The mapping to bands in {@code source}. Not used
     *                    by this constructor, but keept for futur reference.
     *
     * @todo It would be nice if we could use always the "BandSelect" operation
     *       without the "Null" one. But as of JAI-1.1.1, "BandSelect" do not
     *       detect by itself the case were no copy is required.
     */
    private BandSelect2D(final GridCoverage2D       source,
                         final RenderedImage         image,
                         final GridSampleDimension[] bands,
                         final int[]           bandIndices)
    {
        super(source.getName(),                      // The grid source name
              image,                                 // The underlying data
              source.getCoordinateReferenceSystem(), // The coordinate system.
              source.getGridGeometry().getGridToCoordinateSystem(),
              bands,                                 // The sample dimensions
              new GridCoverage2D[]{source},          // The source grid coverages.
              null);                                 // Properties

        this.bandIndices = bandIndices;
        assert bandIndices==null || bandIndices.length==bands.length;
    }

    /**
     * Applies the band select operation to a grid coverage.
     *
     * @param  parameters List of name value pairs for the parameters.
     * @param  A set of rendering hints, or {@code null} if none.
     * @return The result as a grid coverage.
     */
    static GridCoverage2D create(final ParameterValueGroup parameters, RenderingHints hints) {
        /*
         * Fetch all parameters, clone them if needed. The "VisibleSampleDimension" parameter is
         * Geotools-specific and optional. We get it has an Integer both for catching null value,
         * and also because it is going to be stored as an image's property anyway.
         */
        GridCoverage2D source = (GridCoverage2D) parameters.parameter("Source").getValue();
        int[] bandIndices = parameters.parameter("SampleDimensions").intValueList();
        if (bandIndices != null) {
            bandIndices = (int[]) bandIndices.clone();
        }
        Integer visibleBand = (Integer) parameters.parameter("VisibleSampleDimension").getValue();
        /*
         * Prepares the informations needed for JAI's "BandSelect" operation. The loop below
         * should be executed only once, except if the source grid coverage is itself an instance
         * of an other BandSelect2D object, in which case the sources will be extracted
         * recursively until a non-BandSelect2D object is found.
         */
        int visibleSourceBand;
        int visibleTargetBand;
        GridSampleDimension[] sourceBands;
        GridSampleDimension[] targetBands;
        RenderedImage image;
        while (true) {
            sourceBands = source.getSampleDimensions();
            targetBands = sourceBands;
            /*
             * Constructs an array of target bands.  If the 'bandIndices' parameter contains
             * only "identity" indices (0, 1, 2...), then we will work as if no band indices
             * were provided. It will allow us to use the "Null" operation rather than
             * "BandSelect", which make it possible to avoid to copy raster data.
             */
            if (bandIndices != null) {
                if (bandIndices.length!=sourceBands.length || !isIdentity(bandIndices)) {
                    targetBands = new GridSampleDimension[bandIndices.length];
                    for (int i=0; i<bandIndices.length; i++) {
                        targetBands[i] = sourceBands[bandIndices[i]];
                    }
                } else {
                    bandIndices = null;
                }
            }
            image             = source.getRenderedImage();
            visibleSourceBand = GCSUtilities.getVisibleBand(image);
            visibleTargetBand = (visibleBand!=null) ? visibleBand.intValue() :
                                (bandIndices!=null) ? bandIndices[visibleSourceBand] :
                                                                  visibleSourceBand;
            if (bandIndices==null && visibleSourceBand==visibleTargetBand) {
                return source;
            }
            if (!(source instanceof BandSelect2D)) {
                break;
            }
            /*
             * If the source coverage was the result of an other "BandSelect" operation, go up
             * the chain and checks if an existing GridCoverage could fit. We do that in order
             * to avoid to create new GridCoverage everytime the user is switching the visible
             * band. For example we could change the visible band from 0 to 1, and then come
             * back to 0 later.
             */
            final int[] parentIndices = ((BandSelect2D)source).bandIndices;
            if (parentIndices != null) {
                if (bandIndices != null) {
                    for (int i=0; i<bandIndices.length; i++) {
                        bandIndices[i] = parentIndices[bandIndices[i]];
                    }
                } else {
                    bandIndices = (int[])parentIndices.clone();
                }
            }
            assert source.getSources().size() == 1 : source;
            source = (GridCoverage2D) source.getSources().get(0);
        }
        /*
         * All required information are now know. Creates the GridCoverage resulting from the
         * operation. A color model will be defined only if the user didn't specify an explicit
         * one.
         */
        String operation = "Null";
        ImageLayout layout = null;
        if (hints != null) {
            layout = (ImageLayout) hints.get(JAI.KEY_IMAGE_LAYOUT);
        }
        if (layout == null) {
            layout = new ImageLayout();
        }
        if (visibleBand!=null || !layout.isValid(ImageLayout.COLOR_MODEL_MASK)) {
            ColorModel colors = image.getColorModel();
            if (colors instanceof IndexColorModel &&
                sourceBands[visibleSourceBand].equals(targetBands[visibleTargetBand]))
            {
                /*
                 * If the source color model was an instance of  IndexColorModel,  reuse
                 * its color mapping. It may not matches the category colors if the user
                 * provided its own color model. We are better to use what the user said.
                 */
                final IndexColorModel indexed = (IndexColorModel) colors;
                final int[] ARGB = new int[indexed.getMapSize()];
                indexed.getRGBs(ARGB);
                colors = ColorUtilities.getIndexColorModel(ARGB, targetBands.length,
                                                                 visibleTargetBand);
            } else {
                colors = targetBands[visibleTargetBand]
                      .getColorModel(visibleTargetBand, targetBands.length);
            }
            layout.setColorModel(colors);
            if (hints != null) {
                hints = (RenderingHints) hints.clone();
                hints.put(JAI.KEY_IMAGE_LAYOUT, layout);
            } else {
                hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
            }
        }
        if (visibleBand == null) {
            visibleBand = new Integer(visibleTargetBand);
        }
        ParameterBlock params = new ParameterBlock().addSource(image);
        if (targetBands != sourceBands) {
            operation = "BandSelect";
            params = params.add(bandIndices);
        }
        image = OperationJAI.getJAI(hints).createNS(operation, params, hints);
        ((WritablePropertySource) image).setProperty("GC_VisibleBand", visibleBand);
        return new BandSelect2D(source, image, targetBands, bandIndices);
    }

    /**
     * Returns {@code true} if the specified array contains increasing values 0, 1, 2...
     */
    private static boolean isIdentity(final int[] bands) {
        for (int i=0; i<bands.length; i++) {
            if (bands[i] != i) {
                return false;
            }
        }
        return true;
    }

    /**
     * The {@code "SelectSampleDimension"} operation.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static final class Operation extends Operation2D {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = 6889502343896409135L;

        /**
         * The parameter descriptor for the sample dimension indices.
         */
        public static final ParameterDescriptor SAMPLE_DIMENSIONS =
                new DefaultParameterDescriptor(CitationImpl.OGC, "SampleDimensions",
                    int[].class,                        // Value class (mandatory)
                    null,                               // Array of valid values
                    null,                               // Default value
                    null,                               // Minimal value
                    null,                               // Maximal value
                    null,                               // Unit of measure
                    false);                             // Parameter is optional

        /**
         * The parameter descriptor for the visible dimension indice.
         * This is a Geotools-specific parameter.
         */
        public static final ParameterDescriptor VISIBLE_SAMPLE_DIMENSION =
                new DefaultParameterDescriptor(CitationImpl.GEOTOOLS, "VisibleSampleDimension",
                    Integer.class,                      // Value class (mandatory)
                    null,                               // Array of valid values
                    null,                               // Default value
                    new Integer(0),                     // Minimal value
                    null,                               // Maximal value
                    null,                               // Unit of measure
                    false);                             // Parameter is optional

        /**
         * Constructs a default "SelectSampleDimension" operation.
         */
        public Operation() {
            super(new DefaultParameterDescriptorGroup("SelectSampleDimension",
                  new ParameterDescriptor[] {
                        SOURCE_0,
                        SAMPLE_DIMENSIONS,
                        VISIBLE_SAMPLE_DIMENSION
            }));
        }

        /**
         * Applies the band select operation to a grid coverage.
         *
         * @param  parameters List of name value pairs for the parameters.
         * @param  hints A set of rendering hints, or {@code null} if none.
         * @return The result as a grid coverage.
         */
        protected GridCoverage2D doOperation(final ParameterValueGroup parameters,
                                             final RenderingHints hints)
        {
            return BandSelect2D.create(parameters, hints);
        }
    }
}
