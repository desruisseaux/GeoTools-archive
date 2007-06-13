package it.geosolutions.imageio.plugins.slices2D;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

/**
 * Base class which need to be extended by any specific format's ImageReader to
 * work with 2D datasets.
 * 
 * @author Romagnoli Daniele
 */
public abstract class SliceImageReader extends ImageReader implements IndexManager{

	protected boolean isInitialized = false;

	protected File originatingFile = null;

	protected SliceImageReader(ImageReaderSpi originatingProvider) {
		super(originatingProvider);
	}

	/** provide structure initialization */
	protected abstract void initialize() throws IOException;

	public abstract int getHeight(int imageIndex) throws IOException;

	public abstract int getWidth(int imageIndex) throws IOException;

	public abstract BufferedImage read(int imageIndex, ImageReadParam param)
			throws IOException;

	public abstract int getDatasetNum();

	/**
	 * return imageMetadata related to a specific product or subdataset.
	 */
	public abstract IIOMetadata getImageMetadata(int imageIndex) throws IOException;

	/**
	 * return streamMetadata related the whole source.
	 */
	public abstract IIOMetadata getStreamMetadata() throws IOException;
	
	public abstract Iterator getImageTypes(int imageIndex) throws IOException;

	public int getNumImages(boolean allowSearch) throws IOException {
		// TODO provide some implementation for this
		return getDatasetNum();
	}

}
