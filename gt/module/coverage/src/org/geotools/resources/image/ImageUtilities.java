/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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
 */
package org.geotools.resources.image;

// J2SE dependencies
import java.awt.Dimension;
import java.awt.Transparency;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;

// Image I/O and JAI dependencies
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageReaderWriterSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.media.jai.*;
import javax.media.jai.operator.*;
import com.sun.media.jai.util.ImageUtil;
import com.sun.media.jai.operator.ImageReadDescriptor;

// Geotools dependencies
import org.geotools.image.ImageWorker;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * A set of static methods working on images. Some of those methods are useful, but not
 * really rigorous. This is why they do not appear in any "official" package, but instead
 * in this private one.
 *
 *                      <strong>Do not rely on this API!</strong>
 *
 * It may change in incompatible way in any future version.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Simone Giannecchini
 */
public final class ImageUtilities {
    /**
     * The default tile size. This default tile size can be
     * overriden with a call to {@link JAI#setDefaultTileSize}.
     */
    private static final Dimension DEFAULT_TILE_SIZE = new Dimension(512,512);

    /**
     * The minimum tile size.
     */
    private static final int MIN_TILE_SIZE = 128;

    /**
     * Maximum tile width or height before to consider a tile as a stripe. It tile width or height
     * are smaller or equals than this size, then the image will be retiled. That is done because
     * there are many formats that use stripes as an alternative to tiles, an example is tiff. A
     * stripe can be a performance black hole, users can have stripes as large as 20000 columns x 8
     * rows. If we just want to see a chunk of 512x512, this is a lot of uneeded data to load.
     */
    private static final int STRIPE_SIZE = 64;

    /**
     * List of valid names. Note: the "Optimal" type is not
     * implemented because currently not provided by JAI.
     */
    private static final String[] INTERPOLATION_NAMES = {
        "Nearest",          // JAI name
        "NearestNeighbor",  // OpenGIS name
        "Bilinear",
        "Bicubic",
        "Bicubic2"          // Not in OpenGIS specification.
    };

    /**
     * Interpolation types (provided by Java Advanced Imaging) for {@link #INTERPOLATION_NAMES}.
     */
    private static final int[] INTERPOLATION_TYPES= {
        Interpolation.INTERP_NEAREST,
        Interpolation.INTERP_NEAREST,
        Interpolation.INTERP_BILINEAR,
        Interpolation.INTERP_BICUBIC,
        Interpolation.INTERP_BICUBIC_2
    };

    /**
     * Do not allow creation of instances of this class.
     */
    private ImageUtilities() {
    }

    /**
     * Suggests an {@link ImageLayout} for the specified image. All parameters are initially set
     * equal to those of the given {@link RenderedImage}, and then the {@linkplain #toTileSize
     * tile size is updated according the image size}. This method never returns {@code null}.
     */
    public static ImageLayout getImageLayout(final RenderedImage image) {
        return getImageLayout(image, true);
    }

    /**
     * Returns an {@link ImageLayout} for the specified image. If {@code initToImage} is
     * {@code true}, then all parameters are initially set equal to those of the given
     * {@link RenderedImage} and the returned layout is never {@code null} (except if
     * the image is null).
     */
    private static ImageLayout getImageLayout(final RenderedImage image, final boolean initToImage) {
        if (image == null) {
            return null;
        }
        ImageLayout layout = initToImage ? new ImageLayout(image) : null;
        if ((image.getNumXTiles()==1 || image.getTileWidth () <= STRIPE_SIZE) &&
            (image.getNumYTiles()==1 || image.getTileHeight() <= STRIPE_SIZE))
        {
            // If the image was already tiled, reuse the same tile size.
            // Otherwise, compute default tile size.  If a default tile
            // size can't be computed, it will be left unset.
            if (layout != null) {
                layout = layout.unsetTileLayout();
            }
            Dimension defaultSize = JAI.getDefaultTileSize();
            if (defaultSize == null) {
                defaultSize = DEFAULT_TILE_SIZE;
            }
            int s;
            if ((s=toTileSize(image.getWidth(), defaultSize.width)) != 0) {
                if (layout == null) {
                    layout = new ImageLayout();
                }
                layout = layout.setTileWidth(s);
                layout.setTileGridXOffset(image.getMinX());
            }
            if ((s=toTileSize(image.getHeight(), defaultSize.height)) != 0) {
                if (layout == null) {
                    layout = new ImageLayout();
                }
                layout = layout.setTileHeight(s);
                layout.setTileGridYOffset(image.getMinY());
            }
        }
        return layout;
    }

    /**
     * Suggests a set of {@link RenderingHints} for the specified image.
     * The rendering hints may include the following parameters:
     *
     * <ul>
     *   <li>{@link JAI#KEY_IMAGE_LAYOUT} with a proposed tile size.</li>
     * </ul>
     *
     * This method may returns {@code null} if no rendering hints is proposed.
     */
    public static RenderingHints getRenderingHints(final RenderedImage image) {
        final ImageLayout layout = getImageLayout(image, false);
        return (layout!=null) ? new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout) : null;
    }

    /**
     * Suggests a tile size for the specified image size. On input, {@code size} is the image's
     * size. On output, it is the tile size. This method write the result directly in the supplied
     * object and returns {@code size} for convenience.
     * <p>
     * This method it aimed to computing a tile size such that the tile grid would have overlapped
     * the image bound in order to avoid having tiles crossing the image bounds and being therefore
     * partially empty. This method will never returns a tile size smaller than
     * {@value #MIN_TILE_SIZE}. If this method can't suggest a size, then it left the corresponding
     * {@code size} field ({@link Dimension#width width} or {@link Dimension#height height})
     * unchanged.
     * <p>
     * The {@link Dimension#width width} and {@link Dimension#height height} fields are processed
     * independently in the same way. The following discussion use the {@code width} field as an
     * example.
     * <p>
     * This method inspects different tile sizes close to the {@linkplain JAI#getDefaultTileSize()
     * default tile size}. Lets {@code width} be the default tile width. Values are tried in the
     * following order: {@code width}, {@code width+1}, {@code width-1}, {@code width+2},
     * {@code width-2}, {@code width+3}, {@code width-3}, <cite>etc.</cite> until one of the
     * following happen:
     * <p>
     * <ul>
     *   <li>A suitable tile size is found. More specifically, a size is found which is a dividor
     *       of the specified image size, and is the closest one of the default tile size. The
     *       {@link Dimension} field ({@code width} or {@code height}) is set to this value.</li>
     *
     *   <li>An arbitrary limit (both a minimum and a maximum tile size) is reached. In this case,
     *       this method <strong>may</strong> set the {@link Dimension} field to a value that
     *       maximize the remainder of <var>image size</var> / <var>tile size</var> (in other
     *       words, the size that left as few empty pixels as possible).</li>
     * </ul>
     */
    public static Dimension toTileSize(final Dimension size) {
        Dimension defaultSize = JAI.getDefaultTileSize();
        if (defaultSize == null) {
            defaultSize = DEFAULT_TILE_SIZE;
        }
        int s;
        if ((s=toTileSize(size.width,  defaultSize.width )) != 0) size.width  = s;
        if ((s=toTileSize(size.height, defaultSize.height)) != 0) size.height = s;
        return size;
    }

    /**
     * Suggests a tile size close to {@code tileSize} for the specified {@code imageSize}.
     * This method it aimed to computing a tile size such that the tile grid would have
     * overlapped the image bound in order to avoid having tiles crossing the image bounds
     * and being therefore partially empty. This method will never returns a tile size smaller
     * than {@value #MIN_TILE_SIZE}. If this method can't suggest a size, then it returns 0.
     *
     * @param imageSize The image size.
     * @param tileSize  The preferred tile size, which is often {@value #DEFAULT_TILE_SIZE}.
     */
    private static int toTileSize(final int imageSize, final int tileSize) {
        final int MAX_TILE_SIZE = Math.min(tileSize*2, imageSize);
        final int stop = Math.max(tileSize-MIN_TILE_SIZE, MAX_TILE_SIZE-tileSize);
        int sopt = 0;  // An "optimal" tile size, to be used if no exact dividor is found.
        int rmax = 0;  // The remainder of 'imageSize / sopt'. We will try to maximize this value.
        /*
         * Inspects all tile sizes in the range [MIN_TILE_SIZE .. MAX_TIME_SIZE]. We will begin
         * with a tile size equals to the specified 'tileSize'. Next we will try tile sizes of
         * 'tileSize+1', 'tileSize-1', 'tileSize+2', 'tileSize-2', 'tileSize+3', 'tileSize-3',
         * etc. until a tile size if found suitable.
         *
         * More generally, the loop below tests the 'tileSize+i' and 'tileSize-i' values. The
         * 'stop' constant was computed assuming that MIN_TIME_SIZE < tileSize < MAX_TILE_SIZE.
         * If a tile size is found which is a dividor of the image size, than that tile size (the
         * closest one to 'tileSize') is returned. Otherwise, the loop continue until all values
         * in the range [MIN_TILE_SIZE .. MAX_TIME_SIZE] were tested. In this process, we remind
         * the tile size that gave the greatest reminder (rmax). In other words, this is the tile
         * size with the smallest amount of empty pixels.
         */
        for (int i=0; i<=stop; i++) {
            int s;
            if ((s=tileSize+i) <= MAX_TILE_SIZE) {
                final int r = imageSize % s;
                if (r == 0) {
                    // Found a size >= to 'tileSize' which is a dividor of image size.
                    return s;
                }
                if (r > rmax) {
                    rmax = r;
                    sopt = s;
                }
            }
            if ((s=tileSize-i) >= MIN_TILE_SIZE) {
                final int r = imageSize % s;
                if (r == 0) {
                    // Found a size <= to 'tileSize' which is a dividor of image size.
                    return s;
                }
                if (r > rmax) {
                    rmax = r;
                    sopt = s;
                }
            }
        }
        /*
         * No dividor were found in the range [MIN_TILE_SIZE .. MAX_TIME_SIZE]. At this point
         * 'sopt' is an "optimal" tile size (the one that left as few empty pixel as possible),
         * and 'rmax' is the amount of non-empty pixels using this tile size. We will use this
         * "optimal" tile size only if it fill at least 75% of the tile. Otherwise, we arbitrarily
         * consider that it doesn't worth to use a "non-standard" tile size. The purpose of this
         * arbitrary test is again to avoid too many small tiles (assuming that 
         */
        return (rmax >= tileSize - tileSize/4) ? sopt : 0;
    }

    /**
     * Computes a new {@link ImageLayout} which is the intersection of the specified
     * {@code ImageLayout} and all {@code RenderedImage}s in the supplied list. If the
     * {@link ImageLayout#getMinX minX}, {@link ImageLayout#getMinY minY},
     * {@link ImageLayout#getWidth width} and {@link ImageLayout#getHeight height}
     * properties are not defined in the {@code layout}, then they will be inherited
     * from the <strong>first</strong> source for consistency with {@link OpImage} constructor.
     *
     * @param  layout The original layout. This object will not be modified.
     * @param  sources The list of sources {@link RenderedImage}.
     * @return A new {@code ImageLayout}, or the original {@code layout} if no change was needed.
     */
    public static ImageLayout createIntersection(final ImageLayout layout, final List sources) {
        ImageLayout result = layout;
        if (result == null) {
            result = new ImageLayout();
        }
        final int n = sources.size();
        if (n != 0) {
            // If layout is not set, OpImage uses the layout of the *first*
            // source image according OpImage constructor javadoc.
            RenderedImage source = (RenderedImage) sources.get(0);
            int minXL = result.getMinX  (source);
            int minYL = result.getMinY  (source);
            int maxXL = result.getWidth (source) + minXL;
            int maxYL = result.getHeight(source) + minYL;
            for (int i=0; i<n; i++) {
                source = (RenderedImage) sources.get(i);
                final int minX = source.getMinX  ();
                final int minY = source.getMinY  ();
                final int maxX = source.getWidth () + minX;
                final int maxY = source.getHeight() + minY;
                int mask = 0;
                if (minXL < minX) mask |= (1|4); // set minX and width
                if (minYL < minY) mask |= (2|8); // set minY and height
                if (maxXL > maxX) mask |= (4);   // Set width
                if (maxYL > maxY) mask |= (8);   // Set height
                if (mask != 0) {
                    if (layout == result) {
                        result = (ImageLayout) layout.clone();
                    }
                    if ((mask & 1) != 0) result.setMinX   (minXL=minX);
                    if ((mask & 2) != 0) result.setMinY   (minYL=minY);
                    if ((mask & 4) != 0) result.setWidth ((maxXL=maxX) - minXL);
                    if ((mask & 8) != 0) result.setHeight((maxYL=maxY) - minYL);
                }
            }
            // If the bounds changed, adjust the tile size.
            if (result != layout) {
                source = (RenderedImage) sources.get(0);
                if (result.isValid(ImageLayout.TILE_WIDTH_MASK)) {
                    final int oldSize = result.getTileWidth(source);
                    final int newSize = toTileSize(result.getWidth(source), oldSize);
                    if (oldSize != newSize) {
                        result.setTileWidth(newSize);
                    }
                }
                if (result.isValid(ImageLayout.TILE_HEIGHT_MASK)) {
                    final int oldSize = result.getTileHeight(source);
                    final int newSize = toTileSize(result.getHeight(source), oldSize);
                    if (oldSize != newSize) {
                        result.setTileHeight(newSize);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Casts the specified object to an {@link Interpolation object}.
     *
     * @param  type The interpolation type as an {@link Interpolation} or a {@link CharSequence}
     *         object.
     * @return The interpolation object for the specified type.
     * @throws IllegalArgumentException if the specified interpolation type is not a know one.
     */
    public static Interpolation toInterpolation(final Object type) throws IllegalArgumentException {
        if (type instanceof Interpolation) {
            return (Interpolation) type;
        } else if (type instanceof CharSequence) {
            final String name = type.toString();
            for (int i=0; i<INTERPOLATION_NAMES.length; i++) {
                if (INTERPOLATION_NAMES[i].equalsIgnoreCase(name)) {
                    return Interpolation.getInstance(INTERPOLATION_TYPES[i]);
                }
            }
        }
        throw new IllegalArgumentException(Errors.format(ErrorKeys.UNKNOW_INTERPOLATION_$1, type));
    }

    /**
     * Returns the interpolation name for the specified interpolation object.
     * This method tries to infer the name from the object's class name.
     *
     * @param Interpolation The interpolation object.
     */
    public static String getInterpolationName(final Interpolation interp) {
        final String prefix = "Interpolation";
        for (Class classe = interp.getClass(); classe!=null; classe=classe.getSuperclass()) {
            String name = Utilities.getShortName(classe);
            int index = name.lastIndexOf(prefix);
            if (index >= 0) {
                return name.substring(index + prefix.length());
            }
        }
        return Utilities.getShortClassName(interp);
    }

    /**
     * Allows or disallows native acceleration for the specified image format. By default, the
     * image I/O extension for JAI provides native acceleration for PNG and JPEG. Unfortunatly,
     * those native codec has bug in their 1.0 version. Invoking this method will force the use
     * of standard codec provided in J2SE 1.4.
     * <p>
     * <strong>Implementation note:</strong> the current implementation assume that JAI codec
     * class name start with "CLib". It work for Sun's 1.0 implementation, but may change in
     * future versions. If this method doesn't recognize the class name, it does nothing.
     *
     * @param format The format name (e.g. "png").
     * @param writer {@code false} to set the reader, or {@code true} to set the writer.
     * @param allowed {@code false} to disallow native acceleration.
     */
    public static synchronized void allowNativeCodec(final String  format,
                                                     final boolean writer,
                                                     final boolean allowed)
    {
        ImageReaderWriterSpi standard = null;
        ImageReaderWriterSpi codeclib = null;
        final IIORegistry registry = IIORegistry.getDefaultInstance();
        final Class category = writer ? ImageWriterSpi.class : ImageReaderSpi.class;
        for (final Iterator it=registry.getServiceProviders(category, false); it.hasNext();) {
            final ImageReaderWriterSpi provider = (ImageReaderWriterSpi) it.next();
            final String[] formats = provider.getFormatNames();
            for (int i=0; i<formats.length; i++) {
                if (formats[i].equalsIgnoreCase(format)) {
                    if (Utilities.getShortClassName(provider).startsWith("CLib")) {
                        codeclib = provider;
                    } else {
                        standard = provider;
                    }
                    break;
                }
            }
        }
        if (standard!=null && codeclib!=null) {
            if (allowed) {
                registry.setOrdering(category, codeclib, standard);
            } else {
                registry.setOrdering(category, standard, codeclib);
            }
        }
    }





    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////                                                                                        ////
    ////  The following methods were refactored in a new class: org.geotools.image.ImageWorker. ////
    ////  The new class is a proposal - not yet accepted - and is significantly different. The  ////
    ////  proposed replacements appear in the code below as "if (PROPOSED_REPLACEMENT)" block.  ////
    ////                                                                                        ////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Set to {@code true} for enabling the proposed replacements, or {@code false} for running
     * the original code.
     */
    private static final boolean PROPOSED_REPLACEMENT = false;

    /**
     * Tiles the specified image.
     *
     * @throws IOException If an I/O operation were required (in order to check if the image
     *         were tiled on disk) and failed.
     *
     * @since 2.3
     *
     * @deprecated Usually, the tiling doesn't need to be performed as a separated operation. The
     *       {@link ImageLayout} hint with tile information can be provided to most JAI operators.
     *       The {@link #getRenderingHints} method provides such tiling information only if the
     *       image was not already tiled, so it should not be a cause of tile size mismatch in an
     *       operation chain. The mean usage for a separated "tile" operation is to tile an image
     *       before to save it on disk in some format supporting tiling. The proposed replacement
     *       for this operation is {@link ImageWorker#write}.
     */
    public static RenderedOp tileImage(RenderedOp image) throws IOException {
        // /////////////////////////////////////////////////////////////////////
        //
        // initialization
        //
        // /////////////////////////////////////////////////////////////////////
        final int width = image.getWidth();

        final int height = image.getHeight();
        final int tileHeight = image.getTileHeight();
        final int tileWidth = image.getTileWidth();

        boolean needToTile = false;

        // /////////////////////////////////////////////////////////////////////
        //
        // checking if the image comes directly from an image read operation
        //
        // /////////////////////////////////////////////////////////////////////
        // getting the reader
        final Object o = image
                .getProperty(ImageReadDescriptor.PROPERTY_NAME_IMAGE_READER);
        if ((o instanceof ImageReader)) {
            final ImageReader reader = (ImageReader) o;
            if (!reader.isImageTiled(0))
                needToTile = true;
        }
        // /////////////////////////////////////////////////////////////////////
        //
        // If the original image has tileW==W &&tileH==H it is untiled.
        //
        // /////////////////////////////////////////////////////////////////////
        if (tileWidth == width && tileHeight <= height)
            needToTile = true;

        // /////////////////////////////////////////////////////////////////////
        //
        // tiling central.
        //
        // /////////////////////////////////////////////////////////////////////
        if (needToTile) {

            // tiling the original image
            final ImageLayout layout = getImageLayout(image);
            layout.unsetValid(ImageLayout.COLOR_MODEL_MASK|ImageLayout.SAMPLE_MODEL_MASK);

            // changing parameters related to the tiling
            final RenderingHints hints = new RenderingHints(
                    JAI.KEY_IMAGE_LAYOUT, layout);

            // reading the image
            final ParameterBlockJAI pbjFormat = new ParameterBlockJAI("Format");
            pbjFormat.addSource(image);
            pbjFormat.setParameter("dataType", image.getSampleModel()
                    .getDataType());

            return  JAI
                    .create("Format", pbjFormat, hints);

        }
        return image;
    }

    /**
     * Rescale the image such that it uses 8 bit.
     * 
     * @param surrogateImage The image to rescale.
     * 
     * @return The rescaled image.
     * 
     * @todo
     * <ol>
     * <li>caching should be more intelligent</li>
     * <li>use <code>ParameterBlockJAI</code> </li>
     * </ol>
     *
     * @since 2.3
     */
    public static PlanarImage rescale2Byte(PlanarImage surrogateImage) {
        if (PROPOSED_REPLACEMENT) {
            ImageWorker w = new ImageWorker(surrogateImage);
            w.rescaleToBytes();
            return w.getPlanarImage();
        }

        // rescale the initial image in order
        // to expand the dynamic

        // /////////////////////////////////////////////////////////////////////
        //
        // EXTREMA
        //
        // /////////////////////////////////////////////////////////////////////
        // Set up the parameter block for the source image and
        // the constants
        final ParameterBlock pb = new ParameterBlock();
        pb.addSource(surrogateImage); // The source image
        pb.add(null); // The region of the image to scan
        pb.add(1); // The horizontal sampling rate
        pb.add(1); // The vertical sampling rate

        // Perform the extrema operation on the source image
        // Retrieve both the maximum and minimum pixel value
        final double[][] extrema = (double[][]) JAI.create("extrema", pb)
                .getProperty("extrema");

        // /////////////////////////////////////////////////////////////////////
        //
        // RESCALE
        //
        // /////////////////////////////////////////////////////////////////////
        pb.removeSources();
        pb.removeParameters();

        // set the levels for the dynamic
        pb.addSource(surrogateImage);

        // rescaling each band to 8 bits
        final double[] scale = new double[extrema[0].length];
        final double[] offset = new double[extrema[0].length];
        final int length = extrema[0].length;
        for (int i = 0; i < length; i++) {
            scale[i] = 255 / (extrema[1][i] - extrema[0][i]);
            offset[i] = -((255 * extrema[0][i]) / (extrema[1][i] - extrema[0][i]));
        }
        pb.add(scale);
        pb.add(offset);

        final RenderedOp image2return = JAI.create("rescale", pb);

        // setting up the right layout for this image
        ImageLayout layout = new ImageLayout(image2return);
        final Dimension tileSize = ImageUtilities.toTileSize(new Dimension(
                image2return.getWidth(), image2return.getHeight()));
        layout.setTileGridXOffset(0);
        layout.setTileGridYOffset(0);
        layout.setTileHeight((int) tileSize.getWidth());
        layout.setTileWidth((int) tileSize.getHeight());
        pb.removeParameters();
        pb.removeSources();
        pb.addSource(image2return);
        pb.add(DataBuffer.TYPE_BYTE);

        final RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT,
                layout);
        return JAI.create("format", pb, hints);
    }

    /**
     * Reducing the color model to index color model. This should world only for
     * RGB since it performs a ditering on the original color model.
     * 
     * @param sourceImage The image to reduces.
     * 
     * @return The image with index color model.
     *
     * @since 2.3
     */
    public static PlanarImage RGBIndexColorModel(PlanarImage sourceImage) {
        if (PROPOSED_REPLACEMENT) {
            ImageWorker w = new ImageWorker(sourceImage);
            w.forceIndexColorModel();
            return w.getPlanarImage();
        }

        if (sourceImage.getColorModel() instanceof IndexColorModel)
            return sourceImage;
        // error dither
        final KernelJAI[] ditherMask = KernelJAI.DITHER_MASK_443;// KernelJAI.ERROR_FILTER_STUCKI;
        // //
        final ColorCube colorMap = ColorCube.BYTE_496;

        // PARAMETER BLOCK
        final ParameterBlock pb = new ParameterBlock();
        // color map
        pb.addSource(sourceImage);
        pb.add(colorMap);
        pb.add(ditherMask);

        // final layout
        final ImageLayout layout = ImageUtilities.getImageLayout(sourceImage);
        layout.unsetValid(ImageLayout.COLOR_MODEL_MASK);
        layout.unsetValid(ImageLayout.SAMPLE_MODEL_MASK);

        return JAI.create("OrderedDither", pb, new RenderingHints(
                JAI.KEY_IMAGE_LAYOUT, layout));
    }

    /**
     * This method allows me to go from DirectColorModel to ComponentColorModel
     * which seems to be well accepted from PNGEncoder and TIFFEncoder.
     * 
     * This comes from the sun javadocs of the DirectColorModel class.
     * 
     * The DirectColorModel class is a ColorModel class that works with pixel
     * values that represent RGB color and alpha information as separate samples
     * and that pack all samples for a single pixel into a single int, short, or
     * byte quantity. This class can be used only with ColorSpaces of type
     * ColorSpace.TYPE_RGB. In addition, for each component of the ColorSpace,
     * the minimum normalized component value obtained via the getMinValue()
     * method of ColorSpace must be 0.0, and the maximum value obtained via the
     * getMaxValue() method must be 1.0 (these min/max values are typical for
     * RGB spaces). There must be three color samples in the pixel values and
     * there can be a single alpha sample. For those methods that use a
     * primitive array pixel representation of type transferType, the array
     * length is always one. The transfer types supported are
     * DataBuffer.TYPE_BYTE, DataBuffer.TYPE_USHORT, and DataBuffer.TYPE_INT.
     * Color and alpha samples are stored in the single element of the array in
     * bits indicated by bit masks. Each bit mask must be contiguous and masks
     * must not overlap. The same masks apply to the single int pixel
     * representation used by other methods. The correspondence of masks and
     * color/alpha samples is as follows:
     * 
     * Masks are identified by indices running from 0 through 2 if no alpha is
     * present, or 3 if an alpha is present. The first three indices refer to
     * color samples; index 0 corresponds to red, index 1 to green, and index 2
     * to blue. Index 3 corresponds to the alpha sample, if present.
     * 
     * @param surrogateImage The image with a direct color model.
     * @return The image with a component color model.
     * @todo what if the numBits is bigger than 8?
     *
     * @since 2.3
     *
     * @deprecated This method is similar to {@link #reformatColorModel2ComponentColorModel},
     *             the later being slightly more general.
     */
    public static final PlanarImage direct2ComponentColorModel(PlanarImage sourceImage) {
        if (PROPOSED_REPLACEMENT) {
            ImageWorker w = new ImageWorker(sourceImage);
            w.forceComponentColorModel();
            return w.getPlanarImage();
        }

        final ColorModel cm = sourceImage.getColorModel();
        if (!(cm instanceof DirectColorModel))
            return null;
        final ParameterBlockJAI pb = new ParameterBlockJAI("Format");
        pb.addSource(sourceImage);

        // final int numBits = cm.getComponentSize(0);
        // final int transferType= DataBuffer.TYPE_BYTE;
        // if(numBits<=8)
        // transferType= DataBuffer.TYPE_BYTE;
        // if (numBits == 32) {
        // transferType=DataBuffer.TYPE_BYTE ;
        // } else if ((DataBuffer.TYPE_USHORT == transferType)
        // || (DataBuffer.TYPE_SHORT == transferType)) {
        // numBits = 16;
        // } else if (DataBuffer.TYPE_FLOAT == transferType) {
        // numBits = 32;
        // } else if (DataBuffer.TYPE_DOUBLE == transferType) {
        // numBits = 64;
        // }

        // /////////////////////////////////////////////////////////////////////
        //
        // Creating a component color model.
        //
        // /////////////////////////////////////////////////////////////////////
        final ComponentColorModel colorModel = new ComponentColorModel(
                ColorSpace.getInstance(ColorSpace.CS_sRGB), cm.hasAlpha(), cm
                        .isAlphaPremultiplied(), cm.getTransparency(),
                DataBuffer.TYPE_BYTE);
        pb.setParameter("dataType", DataBuffer.TYPE_BYTE);

        // /////////////////////////////////////////////////////////////////////
        //
        // Creating a right layout with color model and sample model.
        //
        // /////////////////////////////////////////////////////////////////////
        final ImageLayout layout = ImageUtilities.getImageLayout(sourceImage);
        layout.setColorModel(colorModel);
        layout.setSampleModel(colorModel.createCompatibleSampleModel(
                sourceImage.getWidth(), sourceImage.getHeight()));
        final RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT,
                layout);

        // /////////////////////////////////////////////////////////////////////
        //
        // Creating the operation.
        //
        // /////////////////////////////////////////////////////////////////////
        return JAI.create("Format", pb, hints);
    }

    /**
     * Bnarize an image with caching control.
     * 
     * @param source    The image to binarize.
     * @param threshold The threshold value for the "binarize" operation.
     * @param cacheMe   {@code false} if the image should not be cached.
     * @return          The binarized image.
     *
     * @since 2.3
     */
    public static final RenderedOp binarizeImageExt(RenderedImage source,
            final double threshold, final boolean cacheMe) {

        if (PROPOSED_REPLACEMENT) {
            ImageWorker w = new ImageWorker(source);
            if (!cacheMe) {
                w.setRenderingHint(JAI.KEY_TILE_CACHE, null);
            }
            w.binarize(threshold);
            return w.getRenderedOperation();
        }

        final SampleModel sm = source.getSampleModel();

        if (sm.getNumBands() != 1) {
            final ParameterBlockJAI pbjBandSelect = new ParameterBlockJAI(
                    "BandSelect");
            pbjBandSelect.addSource(source);
            pbjBandSelect.setParameter("bandIndices", new int[] { 0 });
            source = JAI.create("BandSelect", pbjBandSelect,
                    new RenderingHints(JAI.KEY_TILE_CACHE, null));

        }

        // If the image is already binary and the threshold is >1
        // then there is no work to do.
        if ((threshold >= 1) && ImageUtil.isBinary(sm)) {
            return NullDescriptor.create(source, cacheMe ? null
                    : new RenderingHints(JAI.KEY_TILE_CACHE, null));

            // Otherwise binarize the image for efficiency.
        } else {

            final ParameterBlockJAI pbj = new ParameterBlockJAI("binarize");
            pbj.addSource(source);
            pbj.setParameter("threshold", threshold);

            return JAI.create("binarize", pbj, cacheMe ? null
                    : new RenderingHints(JAI.KEY_TILE_CACHE, null));
        }
    }

    /**
     * Extended version of the JAI ROI operation which allows you to have much
     * more control than the original operation. It also works with muti bands
     * images through a conversion to IHS.
     * 
     * @param image     The image to binarize.
     * @param threshold The threshold value for the "binarize" operation.
     * @param cache     {@code false} if the image should not be cached.
     * @return          The binarized image.
     *
     * @since 2.3
     */
    public static final ROI roiExt(RenderedOp image, final double threshold, final boolean cache) {
        if (PROPOSED_REPLACEMENT) {
            ImageWorker w = new ImageWorker(image);
            if (!cache) {
                w.setRenderingHint(JAI.KEY_TILE_CACHE, null);
            }
            w.binarize(threshold);
            return w.getImageAsROI();
        }

        // going to IHS and geting Intensity band
        final RenderedOp ihs = bandCombineSimple(image, cache);
        final RenderedOp i = selectBand(ihs, cache);
        final RenderedOp bin = binarizeImageExt(i, threshold, cache);
        // building up ROI using image with no intensity
        return new ROI(bin, (int) threshold);
    }

    /**
     * This method is responsible for doing a simple bandcombine on the provided
     * image in order to come up with a simple estimation of the intensity of
     * the image based on the average value of the color compnents. It is
     * worthwhile to note that the alpha band is stripped from the provided
     * image.
     * 
     * 
     * Citing from <code>ComponentColorModel</code>:
     * 
     * "For those methods that use a primitive array pixel representation of
     * type transferType, the array length is the same as the number of color
     * and alpha samples. Color samples are stored first in the array followed
     * by the alpha sample, if present. The order of the color samples is
     * specified by the ColorSpace. Typically, this order reflects the name of
     * the color space type. For example, for TYPE_RGB, index 0 corresponds to
     * red, index 1 to green, and index 2 to blue."
     * 
     * Therefore for component color model alpha is always the last component.
     * 
     * 
     * Citing from <code>PackedColorModel</code>: " Masks are identified by
     * indices running from 0 through getNumComponents - 1. The first
     * getNumColorComponents indices refer to color samples. If an alpha sample
     * is present, it corresponds the last index. The order of the color indices
     * is specified by the ColorSpace. Typically, this reflects the name of the
     * color space type (for example, TYPE_RGB), index 0 corresponds to red,
     * index 1 to green, and index 2 to blue. "
     * 
     * @param image The image to combine.
     * @param cache {@code false} if the image should not be cached.
     * @return The combined image.
     *
     * @since 2.3
     */
    public final static RenderedOp bandCombineSimple(RenderedOp image, boolean cache) {
        if (PROPOSED_REPLACEMENT) {
            ImageWorker w = new ImageWorker(image);
            if (!cache) {
                w.setRenderingHint(JAI.KEY_TILE_CACHE, null);
            }
            w.intensity();
            return w.getRenderedOperation();
        }

        // /////////////////////////////////////////////////////////////////////
        //
        // I need a component color model to be sure to understand what I am
        // doing.
        //
        // /////////////////////////////////////////////////////////////////////
        ColorModel cm = image.getColorModel();
        if (cm instanceof IndexColorModel) {
            image = reformatColorModel2ComponentColorModel(image);
            cm = image.getColorModel();
        }

        // number of color componenents
        final int numBands = cm.getNumComponents();
        final int numColorBands = cm.getNumColorComponents();
        final boolean hasAlpha = cm.hasAlpha()
                && (numColorBands - numBands > 0);

        // one band, nothing to combine
        if (numBands == 1)
            return image;

        // one band plus alpha, let's remove alpha
        if (numColorBands == 1 && hasAlpha)
            return BandSelectDescriptor
                    .create(image, new int[] { 0 }, cache ? null
                            : new RenderingHints(JAI.KEY_TILE_CACHE, null));
        // I have more than one band

        // remove alpha band
        if (hasAlpha) {
            final int bands[] = new int[numColorBands];
            for (int i = 0; i < numColorBands; i++)
                bands[i] = i;
            image = BandSelectDescriptor.create(image, bands, null);
        }

        // compute the coefficients
        final double[][] coeff = new double[1][numColorBands + 1];
        for (int i = 0; i < numColorBands; i++)
            coeff[0][i] = 1.0 / numColorBands;
        return BandCombineDescriptor.create(image, coeff, cache ? null
                : new RenderingHints(JAI.KEY_TILE_CACHE, null));
    }

    /**
     * Performing the bandselect on the input image in case it has more than one
     * band.
     * 
     * @param image The image where to select a band.
     * @param cache {@code false} if the image should not be cached.
     * @return The image with the selected band.
     *
     * @since 2.3
     *
     * @deprecated This is a duplicated (actually a special case) of {@link #getBandsFromImage},
     *             except for the {@code cache} argument and a special processing done for the
     *             {@link IHSColorSpace}.
     */
    public final static RenderedOp selectBand(RenderedImage image, boolean cache) {
        if (PROPOSED_REPLACEMENT) {
            ImageWorker w = new ImageWorker(image);
            if (!cache) {
                w.setRenderingHint(JAI.KEY_TILE_CACHE, null);
            }
            w.retainFirstBand();
            return w.getRenderedOperation();
        }

        final ColorModel cm = image.getColorModel();
        if ((cm.getNumComponents() <= 1)
                || !(cm.getColorSpace() instanceof IHSColorSpace))
            return image instanceof RenderedOp ? (RenderedOp) image
                    : NullDescriptor.create(image, null);

        if (cache)
            return BandSelectDescriptor.create(image, new int[] { 0 }, null);
        return BandSelectDescriptor.create(image, new int[] { 0 },
                new RenderingHints(JAI.KEY_TILE_CACHE, null));
    }

    /**
     * Converting the input image to an IHS color model in order to be able to
     * base our decisions only on the intensity band. If the input color model
     * is based on a grayscale colorspace we do not convert anything and we use
     * that single band. Rationale of doing this conversion is to avoid carrying
     * around useless data since the intensity band keeps all the information we
     * need to proceed.
     * 
     * @param image
     * @param cache
     * @return
     *
     * @since 2.3
     *
     * @deprecated This method is similar to {@link #bandCombineSimple}. The computation performed
     *             by the later matches the definition of the {@code I} in a {@code IHS} color
     *             space.
     */
    public final static RenderedOp convertIHS(RenderedImage image, boolean cache) {
        if (PROPOSED_REPLACEMENT) {
            ImageWorker w = new ImageWorker(image);
            if (!cache) {
                w.setRenderingHint(JAI.KEY_TILE_CACHE, null);
            }
            w.intensity();
            return w.getRenderedOperation();
        }

        ColorModel cm = image.getColorModel();
        if (cm instanceof IndexColorModel) {
            image = reformatColorModel2ComponentColorModel(PlanarImage
                    .wrapRenderedImage(image));
            cm = image.getColorModel();
        }
        final ColorSpace cs = cm.getColorSpace();
        final int colorSpaceType = cs.getType();
        if (colorSpaceType == ColorSpace.CS_GRAY
                || colorSpaceType == ColorSpace.TYPE_GRAY
                || cs instanceof IHSColorSpace)
            return image instanceof RenderedOp ? (RenderedOp) image
                    : NullDescriptor.create(image, null);
        final ColorModel newCm = new ComponentColorModel(IHSColorSpace
                .getInstance(), false, false, cm.getTransparency(), cm
                .getTransferType());
        final ParameterBlockJAI pbjCC = new ParameterBlockJAI("colorconvert");
        pbjCC.addSource(image);
        pbjCC.setParameter("colorModel", newCm);
        if (cache)
            return JAI.create("colorconvert", pbjCC);
        return JAI.create("colorconvert", pbjCC, new RenderingHints(
                JAI.KEY_TILE_CACHE, null));
    }

    /**
     * This method is used to add transparency to a preexisting image whose
     * color model is indexcolormodel. There are quite a few step to perform
     * here. 1>Creating a new IndexColorModel which supports transparency, using
     * the given image's colormodel 2>creating a suitable sample model 3>copying
     * the old sample model to the new sample model. 4>looping through the
     * alphaChannel and setting the corresponding pixels in the new sample model
     * to the index for transparency 5>creating a bufferedimage 6>creating a
     * planar image to be returned
     * 
     * NOTE For optimizing writing GIF we need to create this image UNTILED!!.
     * 
     * @todo Extensive testing should is required.
     * 
     * @param surrogateImage
     * @param alphaChannel
     * @param pb
     * 
     * @return
     *
     * @since 2.3
     */
    public static PlanarImage addTransparency2IndexColorModel(
            final PlanarImage surrogateImage, final RenderedImage alphaChannel,
            final boolean optimizeForWritingGIF) {

        if (PROPOSED_REPLACEMENT) {
            ImageWorker w = new ImageWorker(surrogateImage);
            if (optimizeForWritingGIF) {
                w.setRenderingHint(ImageWorker.TILING_ALLOWED, Boolean.FALSE);
            }
            w.forceBitmaskIndexColorModel(255);
            w.addTransparencyToIndexColorModel(alphaChannel);
            return w.getPlanarImage();
        }

        final IndexColorModel cm = (IndexColorModel) surrogateImage
                .getColorModel();

        final byte[][] rgba = new byte[3][256]; // WE MIGHT USE LESS THAN 256
        // COLORS
        // get the r g b a components
        cm.getReds(rgba[0]);
        cm.getGreens(rgba[1]);
        cm.getBlues(rgba[2]);

        /*
         * Now all the color are opaque except one and the color map has been
         * rebuilt loosing all the tranpsarent colors except the first one. The
         * raster has been rebuilt as well, in order to make it point to the
         * right color in the color map. We have to create the new image to be
         * returned.
         */
        final IndexColorModel cm1 = new IndexColorModel(cm.getPixelSize(), 256,
                rgba[0], rgba[1], rgba[2], 255);

        /*
         * 
         * Threshold on the alpha channel to go to 0 -255 values
         * 
         */
        final ParameterBlockJAI pbTheshold = new ParameterBlockJAI("Threshold");
        pbTheshold.addSource(alphaChannel);
        pbTheshold.setParameter("low", new double[] { 1 });
        pbTheshold.setParameter("high", new double[] { 254 });
        pbTheshold.setParameter("constants", new double[] { 0 });
        final RenderedOp newAlphaChannel = JAI.create("threshold", pbTheshold,
                new RenderingHints(JAI.KEY_TILE_CACHE, null));

        /*
         * colorspacetype Threshold on the alpha channel to go to 0 -255 values
         * 
         */
        final ParameterBlockJAI pbInvert = new ParameterBlockJAI("Invert");
        pbInvert.addSource(newAlphaChannel);
        final RenderedOp newInvertedAlphaChannel = JAI.create("Invert",
                pbTheshold, new RenderingHints(JAI.KEY_TILE_CACHE, null));

        /*
         * preparing hints and layout to reuse all over the methid. It worth to
         * remark on that to optimie gif writing we need to untile the gif
         * image.
         */
        final ImageLayout layout = new ImageLayout(surrogateImage);
        layout.setColorModel(cm1);
        if (optimizeForWritingGIF) {
            layout.setTileGridXOffset(surrogateImage.getMinX());
            layout.setTileGridYOffset(surrogateImage.getMinY());
            layout.setTileWidth(surrogateImage.getWidth());
            layout.setTileHeight(surrogateImage.getHeight());
        }

        final RenderingHints hints = new RenderingHints(
                JAI.KEY_REPLACE_INDEX_COLOR_MODEL, Boolean.FALSE);
        hints.add(new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout));
        hints.add(new RenderingHints(JAI.KEY_TILE_CACHE, null));

        /*
         * Adding to the other image
         * 
         */
        final ParameterBlockJAI pbjAdd = new ParameterBlockJAI("add");
        pbjAdd.addSource(surrogateImage);
        pbjAdd.addSource(newInvertedAlphaChannel);
        return JAI.create("add", pbjAdd, hints);
    }

    /**
     * Convert the image to a GIF-compliant image. This method has been created
     * in order to convert the input image to a form that is compatible with the
     * GIF model. It first remove the information about transparency since the
     * error diffusion and the error dither operations are unable to process
     * images with more than 3 bands. Sfterwards the image is processed with an
     * error diffusion operator in order to reduce the number of bands from 3 to
     * 1 and the number of color to 216. A suitable layout is used for the final
     * image via the RenderingHints in order to take into account the different
     * layout model for the final image.
     * 
     * @param surrogateImage
     *            image to convert
     * 
     * @return PlanarImage image converted
     *
     * @since 2.3
     */
    public static final PlanarImage componentColorModel2IndexColorModel4GIF(
            PlanarImage sourceImage) {

        if (PROPOSED_REPLACEMENT) {
            ImageWorker w = new ImageWorker(sourceImage);
            w.forceIndexColorModelForGIF();
            return w.getPlanarImage();
        }

        // /////////////////////////////////////////////////////////////////
        //
        // checking the color model to see if we need to convert it back to
        // color model.
        //
        // /////////////////////////////////////////////////////////////////
        if (sourceImage.getColorModel() instanceof DirectColorModel)
            sourceImage = ImageUtilities
                    .direct2ComponentColorModel(sourceImage);

        ParameterBlock pb = new ParameterBlock();
        RenderedImage alphaChannel = null;

        // /////////////////////////////////////////////////////////////////
        // 
        // AMPLITUDE RESCALING
        // I might also need to reformat the image in order to get it to 8
        // bits samples
        //
        // /////////////////////////////////////////////////////////////////
        if (sourceImage.getSampleModel().getTransferType() != DataBuffer.TYPE_BYTE) {
            sourceImage = ImageUtilities.rescale2Byte(sourceImage);
        }

        // /////////////////////////////////////////////////////////////////
        // 
        // ALPHA CHANNEL getting the alpha channel and separating from the
        // others bands.
        //
        // /////////////////////////////////////////////////////////////////
        if (sourceImage.getColorModel().hasAlpha()) {
            int numBands = sourceImage.getSampleModel().getNumBands();

            // getting alpha channel
            alphaChannel = JAI.create("bandSelect", sourceImage,
                    new int[] { numBands - 1 });

            // getting needed bands
            sourceImage = getBandsFromImage(sourceImage, numBands);
        }

        // /////////////////////////////////////////////////////////////////
        // 
        // BAND MERGE If we do not have 3 bands we have no way to go to
        // index color model in a simple way using jai. Therefore we add the
        // bands we need in order to get there. This trick works fine with
        // gray scale images. ATTENTION, if the initial image had no alpha
        // channel we proceed without doing anything since it seems that GIF
        // encoder in such a case works fine.
        // /////////////////////////////////////////////////////////////////
        if ((sourceImage.getSampleModel().getNumBands() == 1)
                && (alphaChannel != null)) {
            int numBands = sourceImage.getSampleModel().getNumBands();

            // getting first band
            final RenderedImage firstBand = JAI.create("bandSelect",
                    sourceImage, new int[] { 0 });

            // adding to the image
            for (int i = 0; i < (3 - numBands); i++) {
                pb.removeParameters();
                pb.removeSources();

                pb.addSource(sourceImage);
                pb.addSource(firstBand);
                sourceImage = JAI.create("bandmerge", pb);

                pb.removeParameters();
                pb.removeSources();
            }
        }

        // /////////////////////////////////////////////////////////////////
        // 
        // ERROR DIFFUSION we create a single banded image with index color
        // model.
        // /////////////////////////////////////////////////////////////////
        if (sourceImage.getSampleModel().getNumBands() == 3) {
            sourceImage = ImageUtilities.RGBIndexColorModel(sourceImage);
        }
        // /////////////////////////////////////////////////////////////////
        // 
        // TRANSPARENCY Adding transparency if needed, which means using the
        // alpha channel to build a new color model
        // /////////////////////////////////////////////////////////////////
        if (alphaChannel != null) {
            sourceImage = ImageUtilities.addTransparency2IndexColorModel(
                    sourceImage, alphaChannel, true);
        }

        return sourceImage;
    }

    /**
     * Remove the alpha band and keeps the others.
     * 
     * @param surrogateImage
     * @param numBands
     * 
     * @return
     *
     * @since 2.3
     */
    public static PlanarImage getBandsFromImage(PlanarImage surrogateImage, int numBands) {
        if (PROPOSED_REPLACEMENT) {
            ImageWorker w = new ImageWorker(surrogateImage);
            w.retainBands(numBands - 1);
            return w.getPlanarImage();
        }

        switch (numBands - 1) {
        case 1:
            surrogateImage = JAI.create("bandSelect", surrogateImage,
                    new int[] { 0 });

            break;

        case 3:
            surrogateImage = JAI.create("bandSelect", surrogateImage,
                    new int[] { 0, 1, 2 });

            break;
        }

        return surrogateImage;
    }

    /**
     * GIF does not support full alpha channel we need to reduce it in order to
     * provide a simple transparency index to a unique fully transparent color.
     * 
     * @todo Extensive testing is required.
     * @param surrogateImage
     * 
     * @return
     *
     * @since 2.3
     */
    public static final PlanarImage convertIndexColorModelAlpha4GIF(PlanarImage surrogateImage) {
        if (PROPOSED_REPLACEMENT) {
            ImageWorker w = new ImageWorker(surrogateImage);
            w.forceBitmaskIndexColorModel();
            return w.getPlanarImage();
        }

        // doing nothing if the input color model is correct
        final IndexColorModel cm = (IndexColorModel) surrogateImage
                .getColorModel();

        if (cm.getTransparency() != Transparency.TRANSLUCENT) {
            return surrogateImage;
        }

        final byte[][] rgba = new byte[4][256]; // WE MIGHT USE LESS THAN 256
        // COLORS

        // getting all the colors
        cm.getReds(rgba[0]);
        cm.getGreens(rgba[1]);
        cm.getBlues(rgba[2]);

        /*
         * Now we are going for the first transparent color in the color map.
         * From now on we will reuse this color as the default trasnparent
         * color.
         */
        int transparencyIndex = -1;
        int index = -1;
        final int length = cm.getMapSize();

        final byte lookupTable[] = new byte[256];
        for (int i = 0; i < length; i++) {

            // check for transparency
            if ((cm.getAlpha(i) & 0xff) == 0) {
                // FULLY TRANSPARENT PIXEL

                // setting transparent color to this one
                // the other tranpsarent bits will point to this one
                if (transparencyIndex == -1)
                    transparencyIndex = cm.getAlpha(index);

                lookupTable[i] = (byte) (transparencyIndex & 0xff);

            } else
                // non transparent pixel
                lookupTable[i] = (byte) (i & 0xff);

        }

        /*
         * 
         * Now we need to perform the look up transformation. First of all we
         * create the new color model with a bitmask transparency using the
         * transparency index we just found. Then we perform the lookup
         * operation in order to prepare for the gif image.
         * 
         */
        // color model
        final IndexColorModel cm1 = new IndexColorModel(cm.getComponentSize(0),
                256, rgba[0], rgba[1], rgba[2], transparencyIndex);

        // look up table
        final LookupTableJAI lookUpTableJAI = new LookupTableJAI(lookupTable);
        final ParameterBlockJAI pbjLookUp = new ParameterBlockJAI("LookUp");
        pbjLookUp.setParameter("table", lookUpTableJAI);
        pbjLookUp.addSource(surrogateImage);
        final ImageLayout layout = new ImageLayout(surrogateImage);
        layout.setColorModel(cm1);
        layout.setTileGridXOffset(0);
        layout.setTileGridYOffset(0);
        final Dimension tileSize = ImageUtilities.toTileSize(new Dimension(
                surrogateImage.getWidth(), surrogateImage.getHeight()));
        layout.setTileHeight((int) tileSize.getHeight());
        layout.setTileWidth((int) tileSize.getWidth());
        final RenderingHints hints = new RenderingHints(
                JAI.KEY_REPLACE_INDEX_COLOR_MODEL, Boolean.FALSE);
        hints.add(new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout));
        hints.add(new RenderingHints(JAI.KEY_TILE_CACHE, null));
        return JAI.create("LookUp", pbjLookUp, hints);

    }

    /**
     * Reformat the color model to a component color model preserving
     * transparency. Code from jai-interests archive with some improvements.
     * 
     * @param surrogateImage
     * 
     * @return
     * 
     * @throws IllegalArgumentException
     *
     * @since 2.3
     */
    public static RenderedOp reformatColorModel2ComponentColorModel(
            PlanarImage sourceImage) throws IllegalArgumentException {

        if (PROPOSED_REPLACEMENT) {
            ImageWorker w = new ImageWorker(sourceImage);
            w.forceComponentColorModel();
            return w.getRenderedOperation();
        }

        final ColorModel cm = sourceImage.getColorModel();
        final SampleModel sm = sourceImage.getSampleModel();
        if (cm instanceof ComponentColorModel)
            return NullDescriptor.create(sourceImage, new RenderingHints(
                    JAI.KEY_TILE_CACHE, null));

        // /////////////////////////////////////////////////////////////////////
        //
        // Format the image to ComponentColorModel
        //
        // /////////////////////////////////////////////////////////////////////
        final ParameterBlockJAI pbjFormat = new ParameterBlockJAI("Format");
        pbjFormat.addSource(sourceImage);
        pbjFormat.setParameter("dataType", sm.getTransferType());

        // /////////////////////////////////////////////////////////////////////
        //
        // creating the final image layout which should allow me to change color
        // model
        //
        // /////////////////////////////////////////////////////////////////////
        ColorModel cm1;
        final int numBits;
        final int transferType = (cm instanceof DirectColorModel) ? DataBuffer.TYPE_BYTE
                : sm.getTransferType();
        switch (transferType) {
        case DataBuffer.TYPE_BYTE:
            numBits = 8;

            break;

        case DataBuffer.TYPE_USHORT:
            numBits = 16;

            break;

        case DataBuffer.TYPE_SHORT:
            numBits = 16;

            break;

        case DataBuffer.TYPE_INT:
            numBits = 32;

            break;

        case DataBuffer.TYPE_FLOAT:
            numBits = 32;

            break;

        case DataBuffer.TYPE_DOUBLE:
            numBits = 64;

            break;

        default:
            throw new IllegalArgumentException(
                    "Unsupported data type for a color model!");
        }

        // do we need alpha?
        final int transparency = cm.getTransparency();
        if (transparency != Transparency.OPAQUE) {
            cm1 = new ComponentColorModel(ColorSpace
                    .getInstance(ColorSpace.CS_sRGB), new int[] { numBits,
                    numBits, numBits, numBits }, true, false, transparency,
                    transferType);
        } else {
            cm1 = new ComponentColorModel(ColorSpace
                    .getInstance(ColorSpace.CS_sRGB), new int[] { numBits,
                    numBits, numBits }, false, false, transparency,
                    transferType);
        }
        // /////////////////////////////////////////////////////////////////////
        //
        // creating the final image layout which should allow me to change color
        // model
        //
        // /////////////////////////////////////////////////////////////////////
        // setting tile dimensions and color model for the format operation
        final ImageLayout layout = ImageUtilities.getImageLayout(sourceImage);
        layout.setColorModel(cm1);
        layout.setSampleModel(cm1.createCompatibleSampleModel(sourceImage
                .getWidth(), sourceImage.getHeight()));
        final RenderingHints hint = new RenderingHints(JAI.KEY_IMAGE_LAYOUT,
                layout);
        return JAI.create("format", pbjFormat, hint);
    }
}
