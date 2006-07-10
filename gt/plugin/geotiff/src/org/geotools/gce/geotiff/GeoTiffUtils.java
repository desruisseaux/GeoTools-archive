/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.gce.geotiff;

import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

/**
 * @author simone iannecchini
 * @author alessio fabiani
 *
 * @source $URL$
 */
public class GeoTiffUtils {

	/**
	 * 
	 */
	public GeoTiffUtils() {	}
	/**
	 * This method allows me to go from DirectColorModel to ComponentColorModel
	 * which seems to be well acepted from PNGEncoder and TIFFEncoder.
	 * 
	 * @param surrogateImage
	 * @return
	 */
	static public PlanarImage direct2ComponentColorModel(PlanarImage surrogateImage) {
		ParameterBlockJAI pb = new ParameterBlockJAI("ColorConvert");
		pb.addSource(surrogateImage);
		int numBits=8;
		if(DataBuffer.TYPE_INT==surrogateImage.getSampleModel().getTransferType())
			numBits=32;
		else
			if(DataBuffer.TYPE_USHORT==surrogateImage.getSampleModel().getTransferType()||
					DataBuffer.TYPE_SHORT==surrogateImage.getSampleModel().getTransferType())
				numBits=16;
			else
				if(DataBuffer.TYPE_FLOAT==surrogateImage.getSampleModel().getTransferType())
					numBits=32;
				else
					if(DataBuffer.TYPE_DOUBLE==surrogateImage.getSampleModel().getTransferType())
						numBits=64;
		ComponentColorModel colorModel = new ComponentColorModel(surrogateImage.getColorModel().getColorSpace(),
		                                                         new int[] { 
																		numBits,
																		numBits,
																		numBits,
																		numBits 
																 },
		                                                         false,
		                                                         surrogateImage.getColorModel().hasAlpha(),
																 surrogateImage.getColorModel().getTransparency(),
																 surrogateImage.getSampleModel().getTransferType());
		pb.setParameter("colormodel", colorModel);
		ImageLayout layout = new ImageLayout();
		layout.setColorModel(colorModel);
		layout.setSampleModel(colorModel.createCompatibleSampleModel(surrogateImage.getWidth(), surrogateImage.getHeight()));
		RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
		surrogateImage = JAI.create("ColorConvert", pb, hints).createInstance();
		return surrogateImage;
	}

	/**
     * Reformat the index color model to a component color model preserving
     * transparency.
     * Code from jai-interests archive.
     * @param surrogateImage
     *
     * @return
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    static public PlanarImage reformatColorModel2ComponentColorModel(
        PlanarImage surrogateImage) throws IllegalArgumentException {
        // Format the image to be expanded from IndexColorModel to
        // ComponentColorModel
        ParameterBlock pbFormat = new ParameterBlock();
        pbFormat.addSource(surrogateImage);
        pbFormat.add(surrogateImage.getSampleModel().getTransferType());

        ImageLayout layout = new ImageLayout();
        ColorModel cm1 = null;
        int numBits = 0;

        switch (surrogateImage.getSampleModel().getTransferType()) {
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
			numBits=64;
			break;

        default:
            throw new IllegalArgumentException(
                "Unsupported data type for an index color model!");
        }

        //do we need alpha?
		int transparency=surrogateImage.getColorModel().getTransparency();
		final int transpPixel=((IndexColorModel)surrogateImage.getColorModel()).getTransparentPixel();
        if (transparency!=Transparency.OPAQUE) {
            cm1 = new ComponentColorModel(ColorSpace.getInstance(
                        ColorSpace.CS_sRGB),
                    new int[] { numBits, numBits, numBits, numBits }, true,
                    false,transparency,
                    surrogateImage.getSampleModel().getTransferType());
        } else {
            cm1 = new ComponentColorModel(ColorSpace.getInstance(
                        ColorSpace.CS_sRGB),
                    new int[] { numBits, numBits, numBits }, false, false,
					transparency,
                    surrogateImage.getSampleModel().getTransferType());
        }

        layout.setColorModel(cm1);
        layout.setSampleModel(cm1.createCompatibleSampleModel(
                surrogateImage.getWidth(), surrogateImage.getHeight()));

        RenderingHints hint = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
        RenderedOp dst = JAI.create("format", pbFormat, hint);
        surrogateImage = dst.createSnapshot();

        return surrogateImage;
    }	
}
