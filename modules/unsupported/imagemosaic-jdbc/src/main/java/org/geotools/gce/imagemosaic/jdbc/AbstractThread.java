package org.geotools.gce.imagemosaic.jdbc;

import org.geotools.coverage.grid.GridCoverageFactory;

import org.geotools.geometry.GeneralEnvelope;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * this class is the base class for concrete thread classes
 *
 * @author mcr
 *
 */
abstract class AbstractThread extends Thread {
    final static GridCoverageFactory coverageFactory = new GridCoverageFactory();
    LinkedBlockingQueue<Object> tileQueue;
    Config config;
    GeneralEnvelope requestEnvelope;
    Rectangle pixelDimension;
    ImageLevelInfo levelInfo;
    double rescaleX;
    double rescaleY;
    double resX;
    double resY;

    /**
     * Constructor
     *
     * @param pixelDimenison
     *            the requested pixel dimension
     * @param requestEnvelope
     *            the requested world rectangle
     * @param levelInfo
     *            levelinfo of selected pyramid
     * @param tileQueue
     *            queue for thread synchronization
     * @param config
     *            the configuraton of the plugin
     */
    AbstractThread(Rectangle pixelDimenison, GeneralEnvelope requestEnvelope,
        ImageLevelInfo levelInfo, LinkedBlockingQueue<Object> tileQueue,
        Config config) {
        super();
        this.config = config;
        this.tileQueue = tileQueue;
        this.requestEnvelope = requestEnvelope;
        this.levelInfo = levelInfo;
        this.pixelDimension = pixelDimenison;

        resX = requestEnvelope.getLength(0) / pixelDimenison.getWidth();
        resY = requestEnvelope.getLength(1) / pixelDimenison.getHeight();
        rescaleX = levelInfo.getResX() / resX;
        rescaleY = levelInfo.getResY() / resY;
    }

    /**
     * @return a Map containing a rendereing hint for the interpolation as
     *         specified in the config
     */
    Map<RenderingHints.Key, Object> getRenderingHints() {
        final Map<RenderingHints.Key, Object> hints = new HashMap<RenderingHints.Key, Object>();
        Object interpolation = null;

        if (config.getInterpolation().intValue() == 1) {
            interpolation = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        }

        if (config.getInterpolation().intValue() == 2) {
            interpolation = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
        }

        if (config.getInterpolation().intValue() == 3) {
            interpolation = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
        }

        hints.put(RenderingHints.KEY_INTERPOLATION, interpolation);

        return hints;
    }

    /**
     * @param image
     *            to scale
     * @return rescaled image fitting into the requested pixel dimension
     */
    protected BufferedImage rescaleImage(BufferedImage image) {
        BufferedImage scaledImage = new BufferedImage((int) Math.floor(
                    image.getWidth() * rescaleX),
                (int) Math.floor(image.getHeight() * rescaleY),
                BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2D = (Graphics2D) scaledImage.getGraphics();

        g2D.addRenderingHints(getRenderingHints());
        g2D.drawImage(image,
            AffineTransform.getScaleInstance(rescaleX, rescaleY), null);

        return scaledImage;
    }
}
