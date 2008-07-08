/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gce.imagemosaic.jdbc;

import org.geotools.coverage.grid.GridCoverage2D;

import org.geotools.geometry.GeneralEnvelope;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;


/**
 * This class reads decoded tiles from the queue and performs the mosaicing and
 * scaling
 *
 * @author mcr
 *
 */
class ImageComposerThread extends AbstractThread {
    /** Logger. */
    protected final static Logger LOGGER = Logger.getLogger(ImageComposerThread.class.getPackage()
                                                                                     .getName());
    private GridCoverage2D gridCoverage2D;
    private Color outputTransparentColor;

    ImageComposerThread(Color outputTransparentColor, Rectangle pixelDimension,
        GeneralEnvelope requestEnvelope, ImageLevelInfo levelInfo,
        LinkedBlockingQueue<Object> tileQueue, Config config) {
        super(pixelDimension, requestEnvelope, levelInfo, tileQueue, config);
        this.outputTransparentColor = outputTransparentColor;
    }

    private Dimension getStartDimension() {
        double width;
        double height;

        width = pixelDimension.getWidth() / rescaleX;
        height = pixelDimension.getHeight() / rescaleY;

        return new Dimension((int) Math.round(width), (int) Math.round(height));
    }

    private BufferedImage getStartImage() {
        Dimension dim = getStartDimension();
        BufferedImage image = new BufferedImage((int) dim.getWidth(),
                (int) dim.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2D = (Graphics2D) image.getGraphics();
        Color save = g2D.getColor();
        g2D.setColor(outputTransparentColor);
        g2D.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2D.setColor(save);

        return image;
    }

    @Override
    public void run() {
        BufferedImage image = getStartImage();

        Graphics2D g2D = (Graphics2D) image.getGraphics();
        Object queueObject = null;

        try {
            while ((queueObject = tileQueue.take()) != ImageMosaicJDBCReader.QUEUE_END) {
                GridCoverage2D tileCoverage = (GridCoverage2D) queueObject;
                int posx = (int) ((tileCoverage.getEnvelope().getMinimum(0) -
                    requestEnvelope.getMinimum(0)) / levelInfo.getResX());
                int posy = (int) ((requestEnvelope.getMaximum(1) -
                    tileCoverage.getEnvelope().getMaximum(1)) / levelInfo.getResY());
                g2D.drawRenderedImage(tileCoverage.getRenderedImage(),
                    AffineTransform.getTranslateInstance(posx, posy));
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        gridCoverage2D = coverageFactory.create(config.getCoverageName(),
                rescaleImage(image), requestEnvelope);
    }

    GridCoverage2D getGridCoverage2D() {
        return gridCoverage2D;
    }
}
