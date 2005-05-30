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
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderedImageFactory;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

// Image I/O and JAI dependencies
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageReaderWriterSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.OpImage;
import javax.media.jai.OperationRegistry;
import javax.media.jai.registry.RIFRegistry;
import javax.media.jai.registry.RenderedRegistryMode;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.gcs.Resources;


/**
 * A set of static methods working on images. Some of those methods are useful, but not
 * really rigorous. This is why they do not appear in any "official" package, but instead
 * in this private one.
 *
 *                      <strong>Do not rely on this API!</strong>
 *
 * It may change in incompatible way in any future version.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.0
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
     * Interpolation types (provided by Java Advanced
     * Imaging) for {@link #INTERPOLATION_NAMES}.
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
     * Suggest an {@link ImageLayout} for the specified image.
     * All parameters are initially set equal to those of the
     * given {@link RenderedImage}, and then the tile size is
     * updated according the image's size.  This method never
     * returns {@code null}.
     */
    public static ImageLayout getImageLayout(final RenderedImage image) {
        return getImageLayout(image, true);
    }

    /**
     * Returns an {@link ImageLayout} for the specified image.
     * If {@code initToImage} is {@code true}, then
     * All parameters are initially set equal to those of the
     * given {@link RenderedImage} and the returned layout is
     * never {@code null}.
     */
    private static ImageLayout getImageLayout(final RenderedImage image, final boolean initToImage) {
        if (image == null) {
            return null;
        }
        ImageLayout layout = initToImage ? new ImageLayout(image) : null;
        if (image.getNumXTiles()==1 && image.getNumYTiles()==1) {
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
            }
            if ((s=toTileSize(image.getHeight(), defaultSize.height)) != 0) {
                if (layout == null) {
                    layout = new ImageLayout();
                }
                layout = layout.setTileHeight(s);
            }
        }
        return layout;
    }

    /**
     * Suggest a set of {@link RenderingHints} for the specified image.
     * The rendering hints may include the following parameters:
     *
     * <ul>
     *   <li>{@link JAI#KEY_IMAGE_LAYOUT} with a proposed tile size.</li>
     * </ul>
     *
     * This method may returns {@code null}
     * if no rendering hints is proposed.
     */
    public static RenderingHints getRenderingHints(final RenderedImage image) {
        final ImageLayout layout = getImageLayout(image, false);
        return (layout!=null) ? new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout) : null;
    }

    /**
     * Suggest a tile size for the specified image size. On input,
     * {@code size} is the image's size. On output, it is the
     * tile size. This method returns {@code size} for convenience.
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
     * Suggest a tile size close to {@code tileSize} for the specified
     * {@code imageSize}. If this method can't suggest a size, then it
     * returns 0.
     */
    private static int toTileSize(final int imageSize, final int tileSize) {
        int sopt=0, rmax=0;
        final int MAX_TILE_SIZE = Math.min(tileSize*2, imageSize);
        final int stop = Math.max(tileSize-MIN_TILE_SIZE, MAX_TILE_SIZE-tileSize);
        for (int i=0; i<=stop; i++) {
            int s,r;
            if ((s=tileSize-i) >= MIN_TILE_SIZE) {
                r = imageSize % s;
                if (r==0) {
                    return s;
                }
                if (r > rmax) {
                    rmax = r;
                    sopt = s;
                }
            }
            if ((s=tileSize+i) <= MAX_TILE_SIZE) {
                r = imageSize % s;
                if (r==0) {
                    return s;
                }
                if (r > rmax) {
                    rmax = r;
                    sopt = s;
                }
            }
        }
        return (tileSize-rmax <= tileSize/4) ? sopt : 0;
    }

    /**
     * Compute a new {@link ImageLayout} which is the intersection of the specified
     * {@code ImageLayout} and all {@code RenderedImage}s in the supplied
     * list. If the {@link ImageLayout#getMinX minX}, {@link ImageLayout#getMinY minY},
     * {@link ImageLayout#getWidth width} and {@link ImageLayout#getHeight height}
     * properties are not defined in the {@code layout}, then they will be inherited
     * from the <strong>first</strong> source for consistency with {@link OpImage} constructor.
     *
     * @param  layout The original layout. This object will not be modified.
     * @param  sources The list of sources {@link RenderedImage}.
     * @return A new {@code ImageLayout}, or the original {@code layout} if no
     *         change was needed.
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
     * Cast the specified object to an {@link Interpolation object}.
     *
     * @param  type The interpolation type as an {@link Interpolation}
     *         or a {@link CharSequence} object.
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
        throw new IllegalArgumentException(Resources.format(
                ResourceKeys.ERROR_UNKNOW_INTERPOLATION_$1, type));
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
     * Allow or disallow native acceleration for the specified JAI operation. By default, JAI uses
     * hardware accelerated method when available. For example, it make use of MMX instructions on
     * Intel processors. Unfortunatly, some native method crash the Java Virtual Machine under some
     * circonstances. For example on JAI 1.1.2, the "Affine" operation on an image with float data
     * type, bilinear interpolation and an {@link ImageLayout} rendering hint cause an exception in
     * medialib native code. Disabling the native acceleration (i.e using the pure Java version) is
     * a convenient workaround until Sun fix the bug.
     * <br><br>
     * <strong>Implementation note:</strong> the current implementation assume that factories
     * for native implementations are declared in the {@code com.sun.media.jai.mlib}
     * package, while factories for pure java implementations are declared in the
     * {@code com.sun.media.jai.opimage} package. It work for Sun's 1.1.2 implementation,
     * but may change in future versions. If this method doesn't recognize the package, it does
     * nothing.
     *
     * @param operation The operation name (e.g. "Affine").
     * @param allowed {@code false} to disallow native acceleration.
     */
    public synchronized static void allowNativeAcceleration(final String operation,
                                                            final boolean  allowed)
    {
        final String             product = "com.sun.media.jai";
        final OperationRegistry registry = JAI.getDefaultInstance().getOperationRegistry();
        final List             factories = registry.getOrderedFactoryList(
                                           RenderedRegistryMode.MODE_NAME, operation, product);
        if (factories != null) {
            RenderedImageFactory   javaFactory = null;
            RenderedImageFactory nativeFactory = null;
            Boolean               currentState = null;
            for (final Iterator it=factories.iterator(); it.hasNext();) {
                final RenderedImageFactory factory = (RenderedImageFactory) it.next();
                final String pack = factory.getClass().getPackage().getName();
                if (pack.equals("com.sun.media.jai.mlib")) {
                    nativeFactory = factory;
                    if (javaFactory != null) {
                        currentState = Boolean.FALSE;
                    }
                }
                if (pack.equals("com.sun.media.jai.opimage")) {
                    javaFactory = factory;
                    if (nativeFactory != null) {
                        currentState = Boolean.TRUE;
                    }
                }
            }
            if (currentState!=null && currentState.booleanValue()!=allowed) {
                RIFRegistry.unsetPreference(registry, operation, product,
                                            allowed ? javaFactory : nativeFactory,
                                            allowed ? nativeFactory : javaFactory);
                RIFRegistry.setPreference(registry, operation, product,
                                          allowed ? nativeFactory : javaFactory,
                                          allowed ? javaFactory : nativeFactory);
                final LogRecord record = Resources.getResources(null).getLogRecord(Level.CONFIG,
                                                   ResourceKeys.NATIVE_ACCELERATION_STATE_$2,
                                                   operation, new Integer(allowed ? 1 : 0));
                record.setSourceClassName("ImageUtilities");
                record.setSourceMethodName("allowNativeAcceleration");
                Logger.getLogger("org.geotools.coverage").log(record);
                // We used the "org.geotools.coverage" logger since this method is usually
                // invoked from the GridCoverageProcessor or one of its operations.
            }
        }
    }

    /**
     * Allow or disallow native acceleration for the specified image format. By default, the
     * image I/O extension for JAI provides native acceleration for PNG and JPEG. Unfortunatly,
     * those native codec has bug in their 1.0 version. Invoking this method will force the use
     * of standard codec provided in J2SE 1.4.
     * <br><br>
     * <strong>Implementation note:</strong> the current implementation assume that JAI codec
     * class name start with "CLib". It work for Sun's 1.0 implementation, but may change in
     * future versions. If this method doesn't recognize the class name, it does nothing.
     *
     * @param format The format name (e.g. "png").
     * @param writer {@code false} to set the reader, or {@code true} to set the writer.
     * @param allowed {@code false} to disallow native acceleration.
     */
    public synchronized static void allowNativeCodec(final String format,
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
}
