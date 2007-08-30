/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, Geotools Project Managment Committee (PMC)
 *    (C) 2007, Geomatys
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
 */
package org.geotools.image;

import java.awt.Dimension;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import org.geotools.resources.Utilities;


/**
 * An image dimension, including the number of bands.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ImageDimension extends Dimension {
    /**
     * For compatibility between different version of this class.
     */
    private static final long serialVersionUID = -4349573462196081362L;

    /**
     * The number of bands in the image or raster.
     */
    public int numBands;

    /**
     * The image data type.
     */
    private final int dataType;

    /**
     * Creates a new dimension initialized to the dimension of the given image.
     */
    public ImageDimension(final RenderedImage image) {
        super(image.getWidth(), image.getHeight());
        final SampleModel model = image.getSampleModel();
        numBands = model.getNumBands();
        dataType = model.getDataType();
    }

    /**
     * Creates a new dimension initialized to the dimension of the given raster.
     */
    public ImageDimension(final Raster raster) {
        super(raster.getWidth(), raster.getHeight());
        numBands = raster.getNumBands();
        dataType = raster.getDataBuffer().getDataType();
    }

    /**
     * Returns the number of sample values. This is the product of
     * {@link #width width}, {@link #height height} and {@link #numBands}.
     */
    public long getNumSampleValues() {
        return (long) width * (long) height * (long) numBands;
    }

    /**
     * Returns the number of bytes required in order to memorize {@linkplain #getNumSampleValues
     * all sample values}. The sample values size is determined by the image or raster given at
     * construction time.
     */
    public long getMemoryUsage() {
        return getNumSampleValues() * (DataBuffer.getDataTypeSize(dataType) / 8);
        // TODO: replace 8 by Byte.SIZE when we will be allowed to compile for J2SE 1.5.
    }

    /**
     * Checks whether two dimension objects have equal values.
     */
    //@Override
    public boolean equals(final Object object) {
        if (super.equals(object) && object.getClass().equals(getClass())) {
	    final ImageDimension that = (ImageDimension) object;
	    return this.numBands == that.numBands &&
                   this.dataType == that.dataType;
        }
	return false;
    }

    /**
     * Returns the hash code for this dimension.
     */
    //@Override
    public int hashCode() {
        return super.hashCode() + 37*numBands;
    }

    /**
     * Returns a string representation of this dimension.
     */
    //@Override
    public String toString() {
	return Utilities.getShortClassName(this) + "[width=" + width + ",height=" + height +
                ",numBands=" + numBands + ']';
    }
}
