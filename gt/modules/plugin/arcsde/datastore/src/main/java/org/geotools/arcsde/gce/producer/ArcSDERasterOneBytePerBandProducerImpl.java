package org.geotools.arcsde.gce.producer;

import java.awt.image.BufferedImage;
import java.awt.image.SampleModel;

import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterConsumer;
import com.esri.sde.sdk.client.SeRasterScanLineGenerator;
import com.esri.sde.sdk.client.SeRasterScanLineProducer;

public class ArcSDERasterOneBytePerBandProducerImpl extends ArcSDERasterProducer {
	
	public ArcSDERasterOneBytePerBandProducerImpl() {
		super(null, null, SeRasterScanLineGenerator.MASK_ALL_ON);
	}
	
	public ArcSDERasterOneBytePerBandProducerImpl(SeRasterAttr attr, BufferedImage sourceImage, int maskType) {
		super(attr,sourceImage, maskType);
	}
	
	@Override
	public void setSourceImage(BufferedImage sourceImage) {
		final SampleModel sm = sourceImage.getSampleModel();
		for (int i = 0; i < sm.getNumBands(); i++) {
			if (sm.getSampleSize(i) != 8) {
				throw new IllegalArgumentException("ArcSDERasterOneBytePerBandProducerImpl can't handle images with " + sm.getSampleSize(i) + " bits/sample (in band " + i + ")");
			}
		}
		this.sourceImage = sourceImage;
	}

	/**
	 * @see com.esri.sde.sdk.client.SeRasterProducer#startProduction(com.esri.sde.sdk.client.SeRasterConsumer)
	 * 
	 * this implementation defers completely to {@link SeRasterScanLineProducer}
	 */
	public void startProduction(SeRasterConsumer consumer) {
		SeRasterScanLineProducer prod = new SeRasterScanLineProducer(attr, sourceImage, sourceImage.getHeight());
		prod.setBitMaskType(maskType);
		prod.addConsumer(consumer);
		prod.startProduction(consumer);
	}

}
