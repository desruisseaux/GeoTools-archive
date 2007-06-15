package it.geosolutions.imageio.plugins.jhdf;

import it.geosolutions.imageio.plugins.slices2D.SliceImageReader;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;

import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.ShortBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.spi.ImageReaderSpi;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.HObject;

import com.sun.media.imageioimpl.common.ImageUtil;
import com.sun.media.jai.codecimpl.util.RasterFactory;

public abstract class BaseHDFImageReader extends SliceImageReader {
	

	protected final int[] mutex = new int[] { 0 };
	
	/**
	 * Implement this method in order to retrieve a SubDataset
	 * 
	 * @param imageIndex
	 *            The index of the required SubDataset
	 * @return the required SubDataset
	 */
	protected abstract Dataset retrieveDataset(int imageIndex);

	// TODO: should be moved in the aboveLayer?

	/**
	 * Class used to store basic source structure properties. number of
	 * subdatasets and basic properties of each SubDataset, such as, rank,
	 * dimensions size, chunk size.
	 */
	protected class SourceStructure {

		protected int nSubdatasets;

		protected SubDatasetInfo[] subDatasetInfo;

		protected boolean hasSubDatasets;

		protected long[] subDatasetSizes;

		public SourceStructure(int subdatasetsNum) {
			nSubdatasets = subdatasetsNum;
			subDatasetInfo = new SubDatasetInfo[subdatasetsNum];
			subDatasetSizes = new long[subdatasetsNum];
		}

		public long getSubDatasetSize(int index) {
			return subDatasetSizes[index];
		}

		public void setSubDatasetSize(int index, long size) {
			subDatasetSizes[index] = size;
		}

		public int getNSubdatasets() {
			return nSubdatasets;
		}

		public void setNSubdatasets(int subdatasets) {
			nSubdatasets = subdatasets;
		}

		public long[] getSubDatasetSizes() {
			return subDatasetSizes;
		}

		public void setSubDatasetInfo(int j, SubDatasetInfo dsInfo) {
			subDatasetInfo[j] = dsInfo;
		}

		public SubDatasetInfo getSubDatasetInfo(int j) {
			if (j <= nSubdatasets)
				return subDatasetInfo[j];
			else
				return null;
		}

		public boolean isHasSubDatasets() {
			return hasSubDatasets;
		}

		public void setHasSubDatasets(boolean hasSubDatasets) {
			this.hasSubDatasets = hasSubDatasets;
		}
	}

	/** The originating FileFormat */
	protected FileFormat fileFormat = null;

	protected ImageTypeSpecifier imageType = null;

	/**
	 * a <code>SourceStructure</code>'s instance needed to get main
	 * SubDatasets info and hierarchy
	 */
	protected SourceStructure sourceStructure;

	/** root of the FileFormat related to the provided input source */
	protected HObject root;

	protected BaseHDFImageReader(ImageReaderSpi originatingProvider) {
		super(originatingProvider);
	}

	public int getWidth(final int imageIndex) throws IOException {
		if (!isInitialized)
			initialize();
		return retrieveDataset(imageIndex).getWidth();
	}

	public int getHeight(final int imageIndex) throws IOException {
		if (!isInitialized)
			initialize();
		return retrieveDataset(imageIndex).getHeight();

	}

	public BufferedImage read(final int imageIndex, ImageReadParam param)
			throws IOException {

		// ////////////////////////////////////////////////////////////////////
		//
		// INITIALIZATIONS
		//
		// ////////////////////////////////////////////////////////////////////

		if (!isInitialized)
			initialize();
		final int[] slice2DindexCoordinates = getSlice2DIndexCoordinates(imageIndex);

		final int subDatasetIndex = slice2DindexCoordinates[0];
		final Dataset dataset = retrieveDataset(subDatasetIndex);

		BufferedImage bimage = null;
		dataset.init();
		final int width = dataset.getWidth();
		final int height = dataset.getHeight();
		final int rank = dataset.getRank();

		if (param == null)
			param = getDefaultReadParam();

		int dstWidth = -1;
		int dstHeight = -1;
		int srcRegionWidth = -1;
		int srcRegionHeight = -1;
		int srcRegionXOffset = -1;
		int srcRegionYOffset = -1;
		int xSubsamplingFactor = -1;
		int ySubsamplingFactor = -1;

		// //
		//
		// Retrieving Information about Source Region and doing
		// additional intialization operations.
		//
		// //
		Rectangle srcRegion = param.getSourceRegion();
		if (srcRegion != null) {
			srcRegionWidth = (int) srcRegion.getWidth();
			srcRegionHeight = (int) srcRegion.getHeight();
			srcRegionXOffset = (int) srcRegion.getX();
			srcRegionYOffset = (int) srcRegion.getY();

			// //
			//
			// Minimum correction for wrong source regions
			//
			// When you do subsampling or source subsetting it might
			// happen that the given source region in the read param is
			// uncorrect, which means it can be or a bit larger than the
			// original file or can begin a bit before original limits.
			//
			// We got to be prepared to handle such case in order to avoid
			// generating ArrayIndexOutOfBoundsException later in the code.
			//
			// //

			if (srcRegionXOffset < 0)
				srcRegionXOffset = 0;
			if (srcRegionYOffset < 0)
				srcRegionYOffset = 0;
			if ((srcRegionXOffset + srcRegionWidth) > width) {
				srcRegionWidth = width - srcRegionXOffset;
			}
			// initializing destWidth
			dstWidth = srcRegionWidth;

			if ((srcRegionYOffset + srcRegionHeight) > height) {
				srcRegionHeight = height - srcRegionYOffset;
			}
			// initializing dstHeight
			dstHeight = srcRegionHeight;

		} else {
			// Source Region not specified.
			// Assuming Source Region Dimension equal to Source Image
			// Dimension
			dstWidth = width;
			dstHeight = height;
			srcRegionXOffset = srcRegionYOffset = 0;
			srcRegionWidth = width;
			srcRegionHeight = height;
		}

		// SubSampling variables initialization
		xSubsamplingFactor = param.getSourceXSubsampling();
		ySubsamplingFactor = param.getSourceYSubsampling();

		// ////
		//
		// Updating the destination size in compliance with
		// the subSampling parameters
		//
		// ////

		dstWidth = ((dstWidth - 1) / xSubsamplingFactor) + 1;
		dstHeight = ((dstHeight - 1) / ySubsamplingFactor) + 1;

		// getting dataset properties.
		final long[] start = dataset.getStartDims();
		final long[] stride = dataset.getStride();
		final long[] sizes = dataset.getSelectedDims();

		// Setting variables needed to execute read operation.
		start[0] = srcRegionYOffset;
		start[1] = srcRegionXOffset;
		sizes[0] = dstHeight;
		sizes[1] = dstWidth;
		stride[0] = ySubsamplingFactor;
		stride[1] = xSubsamplingFactor;

		if (rank > 2)
			// Setting indexes of dimensions > 2.
			for (int i = 2; i < rank; i++) {
				// TODO: Need to change indexing logic
				start[i] = slice2DindexCoordinates[rank - i];
				sizes[i] = 1;
				stride[i] = 1;
			}

		final Datatype dt = dataset.getDatatype();

		// TODO: add a method returning the Band Numbers of the dataset.
		// APS has only single bands subdatasets.
		final int nBands = 1;

		// bands variables
		final int[] banks = new int[nBands];
		final int[] offsets = new int[nBands];
		for (int band = 0; band < nBands; band++) {
			banks[band] = band;
			offsets[band] = 0;
		}

		// Setting SampleModel and ColorModel
		final int bufferType = HDFUtilities.getBufferTypeFromDataType(dt);
		SampleModel sm = new BandedSampleModel(bufferType, dstWidth,
				dstHeight, dstWidth, banks, offsets);
		ColorModel cm = retrieveColorModel(sm);

		// ////////////////////////////////////////////////////////////////////
		//
		// DATA READ
		//
		// ////////////////////////////////////////////////////////////////////

		WritableRaster wr = null;
		final Object data;
		try {
			data = dataset.read();
			final int size = dstWidth * dstHeight;
			DataBuffer dataBuffer = null;
			
			switch (bufferType) {
			case DataBuffer.TYPE_BYTE:
				dataBuffer = new DataBufferByte((byte[]) data, size);
				break;
			case DataBuffer.TYPE_SHORT:
			case DataBuffer.TYPE_USHORT:
				dataBuffer = new DataBufferShort((short[]) data, size);
				break;
			case DataBuffer.TYPE_INT:
				dataBuffer = new DataBufferInt((int[]) data, size);
				break;
			case DataBuffer.TYPE_FLOAT:
				dataBuffer = new DataBufferFloat((float[]) data, size);
				break;
			case DataBuffer.TYPE_DOUBLE:
				dataBuffer = new DataBufferDouble((double[]) data, size);
				break;
			}

			wr = Raster.createWritableRaster(sm, dataBuffer, null);
			bimage = new BufferedImage(cm, wr, false, null);

		} catch (Exception e) {
			RuntimeException rte = new RuntimeException(
					"Exception occurred while data Reading" + e);
			rte.initCause(e);
			throw rte;
		}

		return bimage;
	}

	public void setInput(Object input, boolean seekForwardOnly,
			boolean ignoreMetadata) {
		this.setInput(input);
	}

	public void setInput(Object input, boolean seekForwardOnly) {
		this.setInput(input, seekForwardOnly);
	}

	public void setInput(Object input) {

		File file = null;

		// ////////////////////////////////////////////////////////////////////
		//
		// Reset the state of this reader
		//
		// Prior to set a new input, I need to do a pre-emptive reset in order
		// to clear any value-object related to the previous input.
		// ////////////////////////////////////////////////////////////////////
		if (originatingFile!=null)
			reset();
		if (input instanceof File) {
			file = (File) input;
			originatingFile = file;
		}

		if (input instanceof FileImageInputStreamExtImpl) {
			file = ((FileImageInputStreamExtImpl) input).getFile();
			originatingFile = file;
		}

		try {
			initialize();
		} catch (IOException e) {
			new RuntimeException("Not a Valid Input", e);
		}

	}

	/**
	 * Simple initialization method
	 */
	protected void initialize() throws IOException {
		if (originatingFile == null)
			throw new IOException(
					"Unable to Initialize data. Provided Input is not valid");
		final String fileName = originatingFile.getAbsolutePath();
		try {
			fileFormat = FileFormat.getInstance(fileName);
			fileFormat = fileFormat.open(fileName, FileFormat.READ);
			root = fileFormat.get("/");

		} catch (Exception e) {
			IOException ioe = new IOException(
					"Unable to Initialize data. Provided Input is not valid"
							+ e);
			ioe.initCause(e);
			throw ioe;
		}
		isInitialized = true;
	}

	public Iterator getImageTypes(int imageIndex) throws IOException {
		final List l = new java.util.ArrayList(5);
		if (!isInitialized)
			initialize();

		final Dataset dataset = retrieveDataset(imageIndex);
		dataset.init();

		final Datatype dt = dataset.getDatatype();
		final int width = dataset.getWidth();
		final int height = dataset.getHeight();

		// TODO: retrieve Band Number
		final int nBands = 1;

		// bands variables
		final int[] banks = new int[nBands];
		final int[] offsets = new int[nBands];
		for (int band = 0; band < nBands; band++) {
			/* Bands are not 0-base indexed, so we must add 1 */
			banks[band] = band;
			offsets[band] = 0;
		}

		// Variable used to specify the data type for the storing samples
		// of the SampleModel
		int bufferType = HDFUtilities.getBufferTypeFromDataType(dt);
		final SampleModel sm = new BandedSampleModel(bufferType, width,
				height, width, banks, offsets);

		final ColorModel cm = retrieveColorModel(sm);

		imageType = new ImageTypeSpecifier(cm, sm);
		l.add(imageType);
		return l.iterator();

	}

	

	public int getTileHeight(int imageIndex) throws IOException {
		if (!isInitialized)
			initialize();
		long[] chunkSize = retrieveDataset(imageIndex).getChunkSize();

		// TODO: Change this behavior
		if (chunkSize != null)
			return (int) chunkSize[1];
		else
			return 512;
	}

	public int getTileWidth(int imageIndex) throws IOException {
		if (!isInitialized)
			initialize();
		long[] chunkSize = retrieveDataset(imageIndex).getChunkSize();

		// TODO: Change this behavior
		if (chunkSize != null)
			return (int) chunkSize[0];
		else
			return 512;
	}

	public void dispose() {
		super.dispose();
		try {
			fileFormat.close();
		} catch (Exception e) {
			// TODO Nothing to do.
		}
	}

	public void reset() {
		root = null;
		originatingFile=null;
	}

	// /**
	// * returns a mask needed to prevent Java transformation of integer
	// unsigned
	// * values.
	// */
	// private static int getMask(final Datatype type) throws IIOException {
	// final int size = type.getDatatypeSize() * Byte.SIZE;
	// if (size < 1 || size > Integer.SIZE) {
	// throw new IIOException("Integers having " + size
	// + " bits are not supported.");
	// }
	// if (size == Integer.SIZE) {
	// throw new IIOException("Unsigned Integers having " + size
	// + " bits are not supported.");
	// }
	// return (1 << size) - 1;
	// }

	private int retrieveSlice2DIndex(int imageIndex) {
		return retrieveSlice2DIndex(imageIndex, null);
	}

	/**
	 * returns a proper subindex needed to access a specific 2D slice of a
	 * specified coverage/subdataset.
	 * 
	 * @param imageIndex
	 *            the specified coverage/subDataset
	 * @param selectedIndexOfEachDim
	 *            the required index of each dimension
	 * 
	 * TODO: Should I use a single long[] input parameter containing also the
	 * subdataset index?
	 */
	public int retrieveSlice2DIndex(int imageIndex, int[] selectedIndexOfEachDim) {
		int subIndexOffset = 0;
		final SubDatasetInfo sdInfo = sourceStructure
				.getSubDatasetInfo(imageIndex);
		for (int i = 0; i < imageIndex; i++)
			subIndexOffset += (sourceStructure.getSubDatasetSize(i));

		if (selectedIndexOfEachDim != null) {
			// X and Y dims are not taken in account
			final int selectedDimsLenght = selectedIndexOfEachDim.length;
			final long[] subDatasetDims = sdInfo.getDims();
			final int rank = sdInfo.getRank();

			// supposing specifying all required subDimensions.
			// as an instance, if rank=5, I need to specify 3 dimensions-index
			// TODO: maybe I can assume some default behavior.
			// as an instance, using 0 as dimension-index when not specified.
			if (selectedDimsLenght != (rank - 2)) {
				throw new IndexOutOfBoundsException(
						"The selected dims array can't be"
								+ "greater than the rank of the subDataset");
			}
			for (int i = 0; i < selectedDimsLenght; i++) {
				if (selectedIndexOfEachDim[i] > subDatasetDims[i]) {
					final StringBuffer sb = new StringBuffer();
					sb
							.append(
									"At least one of the specified indexes is greater than the max allowed index in that dimension\n")
							.append("dimension=")
							.append(i)
							.append(" index=")
							.append(selectedIndexOfEachDim[i])
							.append(
									" while the maximum index available for this dimension is ")
							.append(subDatasetDims[i]);
					throw new IndexOutOfBoundsException(sb.toString());
				}
			}
			long displacement = 0;
			if (rank > 2) {
				// The least significant dimension is used as offset
				long finalOffset = selectedIndexOfEachDim[rank - 3];
				if (rank > 3) {
					final long[] multipliers = new long[rank - 3];
					for (int i = 0; i < rank - 3; i++)
						multipliers[i] = subDatasetDims[i + 2];
					for (int i = 0; i < rank - 3; i++) {
						int factor = 1;
						for (int j = 0; j < rank - 3 - i; j++)
							factor *= multipliers[j];
						displacement += (factor * selectedIndexOfEachDim[i]);
					}
				}
				displacement += finalOffset;
			}
			subIndexOffset += displacement;
		}
		return (int) subIndexOffset;
	}

	/**
	 * Given a specifiedIndex as an input, returns a <code>long[]</code>
	 * having the subDataset/coverage index at the first position of the array.
	 * Then, the indexes (of the other dimensions) needed to retrieve a proper
	 * 2D Slice.
	 * 
	 * As an instance, suppose a HDF source contains a 4D SubDataset with the
	 * form (X,Y,Z,T). if returnedIndex[]={2,3,1}, the required Slice2D is
	 * available at the subDataset with index=2, timeIndex=3, zIndex=1.
	 * 
	 * TODO: Now, we are supposing order is 5thDim -> T -> Z -> (X,Y)
	 * 
	 */
	public int[] getSlice2DIndexCoordinates(int requiredSlice2DIndex) {
		final int nTotalDataset = sourceStructure.getNSubdatasets();
		final long[] subDatasetSizes = sourceStructure.getSubDatasetSizes();
		int iSubdataset = 0;
		for (; iSubdataset < nTotalDataset; iSubdataset++) {
			int subDatasetSize = (int) subDatasetSizes[iSubdataset];
			if (requiredSlice2DIndex >= subDatasetSize)
				requiredSlice2DIndex -= subDatasetSize;
			else
				break;
		}

		// Getting the SubDatasetInfo related to the specified subDataset.
		final SubDatasetInfo sdInfo = sourceStructure
				.getSubDatasetInfo(iSubdataset);
		final int rank = sdInfo.getRank();

		// index initialization
		final int[] slice2DIndexCoordinates = new int[rank - 1];// subDatasetIndex+(rank-2)
		for (int i = 0; i < rank - 1; i++)
			slice2DIndexCoordinates[i] = 0;
		slice2DIndexCoordinates[0] = iSubdataset;

		if (rank > 2) {
			final long[] subDatasetDims = sdInfo.getDims();
			if (rank > 3) {
				final long[] multipliers = new long[rank - 3];
				for (int i = 0; i < rank - 3; i++)
					multipliers[i] = subDatasetDims[i + 2];

				for (int i = 0; i < rank - 3; i++) {
					int factor = 1;
					for (int j = 0; j < rank - 3 - i; j++)
						factor *= multipliers[j];
					while (requiredSlice2DIndex >= factor) {
						requiredSlice2DIndex -= factor;
						slice2DIndexCoordinates[i + 1]++;
					}
				}
			}
			slice2DIndexCoordinates[rank - 2] = requiredSlice2DIndex;
		}
		return slice2DIndexCoordinates;
	}

}
