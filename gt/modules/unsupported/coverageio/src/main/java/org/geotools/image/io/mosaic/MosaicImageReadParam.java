/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2008, GeoTools Project Managment Committee (PMC)
 *    (C) 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.image.io.mosaic;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.Collections;
import javax.imageio.ImageReader;
import javax.imageio.ImageReadParam;
import javax.imageio.IIOParamController;


/**
 * The parameters for {@link MosaicImageReader}.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class MosaicImageReadParam extends ImageReadParam {
    /**
     * The image type policy, or {@code null} for the default one.
     *
     * @see MosaicImageReader#getDefaultImageTypePolicy
     */
    private ImageTypePolicy imageTypePolicy;

    /**
     * The tile readers obtained from the {@link MosaicImageReader} given at construction time,
     * or an empty map if none. Values are the parameters to be given to those readers, created
     * only when first needed.
     *
     * @see MosaicImageReader#readerInputs
     */
    final Map<ImageReader,ImageReadParam> readers;

    /**
     * The value to be returned by {@link #hasController}, or {@code null} if not yet computed.
     */
    private Boolean hasController;

    /**
     * Constructs default parameters for any mosaic image reader. Parameters created by this
     * constructor will not {@linkplain #hasController have controller}. Consider invoking
     * {@link MosaicImageReader#getDefaultReadParam} for parameters better suited to a
     * particular reader instance.
     */
    public MosaicImageReadParam() {
        this(null);
        hasController = Boolean.FALSE;
    }

    /**
     * Constructs default parameters for the given image reader. This constructor is provided
     * for subclassing only. Uses {@link MosaicImageReader#getDefaultReadParam} instead.
     *
     * @param reader The image reader, or {@code null} if none.
     */
    protected MosaicImageReadParam(final MosaicImageReader reader) {
        readers = new WeakHashMap<ImageReader,ImageReadParam>();
        if (reader != null) {
            for (final ImageReader tileReader : reader.getTileReaders()) {
                readers.put(tileReader, null);
            }
        }
        controller = defaultController = MosaicController.DEFAULT;
    }

    /**
     * Returns the policy for {@link MosaicImageReader#getImageTypes computing image types}.
     * If no policy has been specified, then this method returns {@code null}. In the later
     * case, the reader will use its {@linkplain MosaicImageReader#getDefaultImageTypePolicy
     * default policy}.
     */
    public ImageTypePolicy getImageTypePolicy() {
        return imageTypePolicy;
    }

    /**
     * Sets the policy for {@link MosaicImageReader#getImageTypes computing image types}.
     *
     * @param policy
     *          The new policy, or {@code null} for reader
     *          {@linkplain MosaicImageReader#getDefaultImageTypePolicy default policy}.
     */
    public void getImageTypePolicy(final ImageTypePolicy policy) {
        imageTypePolicy = policy;
    }

    /**
     * Returns the parameters for the given image reader, looking in the cache first. This is
     * needed because {@link MosaicController} may have configured those parameters.
     */
    final ImageReadParam getCachedTileParameters(final ImageReader reader) {
        ImageReadParam parameters = readers.get(reader);
        if (parameters == null) {
            parameters = getTileParameters(reader);
            readers.put(reader, parameters);
        }
        return parameters;
    }

    /**
     * Returns the parameters to use for reading an image from the given image reader.
     * This method is invoked automatically by {@link MosaicImageReader}. The default
     * implementation invokes {@link ImageReader#getDefaultReadParam()} and copies the
     * {@linkplain #setSourceBands source bands},
     * {@linkplain #setDestinationBands destination bands} and
     * {@linkplain #setSourceProgressivePasses progressive passes} settings.
     * Other settings like {@linkplain #setSourceRegion source region} don't need to be
     * copied since they will be computed by the mosaic image reader.
     * <p>
     * Subclasses can override this method if they want to configure the parameters for
     * tile readers in an other way. Note however that {@link MosaicController} provides
     * an other way to configure parameters.
     */
    protected ImageReadParam getTileParameters(final ImageReader reader) {
        final ImageReadParam parameters = reader.getDefaultReadParam();
        parameters.setSourceBands(sourceBands);
        parameters.setDestinationBands(destinationBands);
        parameters.setSourceProgressivePasses(minProgressivePass, numProgressivePasses);
        return parameters;
    }

    /**
     * Returns {@code true} if there is a controller installed. The implementation in this
     * class is more conservative than the default implementation in that if the current
     * {@linkplain #controller controller} is the {@linkplain #defaultController default
     * controller} instance, then this method returns {@code true} only if the controller
     * for at least one {@linkplain #getTileParameters tile parameters} returned {@code true}.
     */
    @Override
    public boolean hasController() {
        if (controller != defaultController) {
            return super.hasController();
        }
        if (hasController == null) {
            hasController = Boolean.FALSE;
            for (final Map.Entry<ImageReader,ImageReadParam> entry : readers.entrySet()) {
                ImageReadParam parameters = entry.getValue();
                if (parameters == null) {
                    parameters = getTileParameters(entry.getKey());
                    entry.setValue(parameters);
                }
                if (parameters.hasController()) {
                    hasController = Boolean.TRUE;
                    break;
                }
            }
        }
        return hasController;
    }

    /**
     * Returns the default controller. This is typically an instance of
     * {@link MosaicController}, but this is not mandatory.
     */
    @Override
    public IIOParamController getDefaultController() {
        // Overriden only for documentation purpose.
        return super.getDefaultController();
    }
}
