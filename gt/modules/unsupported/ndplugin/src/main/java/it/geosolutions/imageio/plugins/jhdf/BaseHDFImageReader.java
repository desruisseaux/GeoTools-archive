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
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel.MapMode;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;

import com.sun.media.imageioimpl.common.ImageUtil;
import com.sun.media.jai.codecimpl.util.RasterFactory;

public abstract class BaseHDFImageReader extends SliceImageReader {

	protected abstract Dataset retrieveDataset(int imageIndex);
	
	// TODO: should be moved in the aboveLayer?
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
			subDatasetSizes[index]=size;
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

	private static final Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.plugins.jhdf.");

	protected ImageTypeSpecifier imageType = null;

	protected SourceStructure sourceStructure;

	// protected int subDatasetsOffset = -1;

	/** root of the FileFormat related the provided input source */
	protected HObject root;

	private int subDatasetsOffset;

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
		if (!isInitialized)
			initialize();
		final long[] indexStructure = buildIndexesStructure(imageIndex);
		
		final int subDatasetIndex = (int)indexStructure[0]; 
		final Dataset dataset = retrieveDataset(subDatasetIndex);

		BufferedImage bimage = null;
		dataset.init();
		final int width = dataset.getWidth();
		final int height = dataset.getHeight();

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

		final long[] start = dataset.getStartDims();
		final long[] stride = dataset.getStride();
		final long[] sizes = dataset.getSelectedDims();

		start[0] = srcRegionYOffset;
		start[1] = srcRegionXOffset;
		if (start.length>2){
			long[] startDims;
		}
		sizes[0] = dstHeight;
		sizes[1] = dstWidth;
		stride[0] = ySubsamplingFactor;
		stride[1] = xSubsamplingFactor;

		final Datatype dt = dataset.getDatatype();
		final int dataTypeClass = dt.getDatatypeClass();
		final int dataTypeSize = dt.getDatatypeSize();
		final boolean isUnsigned = dt.isUnsigned();

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
		int buffer_type = 0;
		if (dataTypeClass == Datatype.CLASS_INTEGER) {
			if (dataTypeSize == 1)
				buffer_type = DataBuffer.TYPE_BYTE;
			else if (dataTypeSize == 2) {
				if (isUnsigned)
					buffer_type = DataBuffer.TYPE_USHORT;
				else
					buffer_type = DataBuffer.TYPE_SHORT;
			} else if (dataTypeSize == 4)
				buffer_type = DataBuffer.TYPE_INT;
		} else if (dataTypeClass == Datatype.CLASS_FLOAT)
			if (dataTypeSize == 4)
				buffer_type = DataBuffer.TYPE_FLOAT;

		SampleModel sm = new BandedSampleModel(buffer_type, dstWidth,
				dstHeight, dstWidth, banks, offsets);
		ColorModel cm = null;

		ColorSpace cs = null;
		if (nBands > 1) {
			// Number of Bands > 1.
			// ImageUtil.createColorModel provides to Creates a
			// ColorModel that may be used with the specified
			// SampleModel
			cm = ImageUtil.createColorModel(sm);
			if (cm == null)
				LOGGER.info("There are no ColorModels found");

		} else if ((buffer_type == DataBuffer.TYPE_BYTE)
				|| (buffer_type == DataBuffer.TYPE_USHORT)
				|| (buffer_type == DataBuffer.TYPE_INT)
				|| (buffer_type == DataBuffer.TYPE_FLOAT)
				|| (buffer_type == DataBuffer.TYPE_DOUBLE)) {

			// Just one band. Using the built-in Gray Scale Color Space
			cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
			cm = RasterFactory.createComponentColorModel(buffer_type, // dataType
					cs, // color space
					false, // has alpha
					false, // is alphaPremultiplied
					Transparency.OPAQUE); // transparency
		} else {
			if (buffer_type == DataBuffer.TYPE_SHORT) {
				// Just one band. Using the built-in Gray Scale Color
				// Space
				cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
				cm = new ComponentColorModel(cs, false, false,
						Transparency.OPAQUE, DataBuffer.TYPE_SHORT);
			}
		}

		WritableRaster wr = null;
		final Object data;
		try {
			data = dataset.read();
			final int size = dstWidth * dstHeight;

			// DataBuffer db = new
			// DataBufferInt((int[])data,dstHeight*dstWidth);

			DataBuffer db = null;

			switch (buffer_type) {
			case DataBuffer.TYPE_BYTE:
				db = new DataBufferByte((byte[]) data, size);
				break;
			case DataBuffer.TYPE_SHORT:
			case DataBuffer.TYPE_USHORT:
				db = new DataBufferShort((short[]) data, size);
				break;
			case DataBuffer.TYPE_INT:
				db = new DataBufferInt((int[]) data, size);
				break;
			case DataBuffer.TYPE_FLOAT:
				db = new DataBufferFloat((float[]) data, size);
				break;
			}

			wr = Raster.createWritableRaster(sm, db, null);
			bimage = new BufferedImage(cm, wr, false, null);

		} catch (OutOfMemoryError e) {
			// TODO Auto-generated catch block
		} catch (Exception e) {
			// TODO Auto-generated catch block
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

		final int nRank = dataset.getRank();
		final int dataTypeClass = dt.getDatatypeClass();
		final int dataTypeSize = dt.getDatatypeSize();
		final int width = dataset.getWidth();
		final int height = dataset.getHeight();
		final boolean isUnsigned = dt.isUnsigned();

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
		int buffer_type = 0;
		if (dataTypeClass == Datatype.CLASS_INTEGER) {
			if (dataTypeSize == 1)
				buffer_type = DataBuffer.TYPE_BYTE;
			else if (dataTypeSize == 2) {
				if (isUnsigned)
					buffer_type = DataBuffer.TYPE_USHORT;
				else
					buffer_type = DataBuffer.TYPE_SHORT;
			} else if (dataTypeSize == 4)
				buffer_type = DataBuffer.TYPE_INT;
		} else if (dataTypeClass == Datatype.CLASS_FLOAT)
			if (dataTypeSize == 4)
				buffer_type = DataBuffer.TYPE_FLOAT;

		SampleModel sm = new BandedSampleModel(buffer_type, width, height,
				width, banks, offsets);
		ColorModel cm = null;

		ColorSpace cs = null;
		if (nBands > 1) {
			// Number of Bands > 1.
			// ImageUtil.createColorModel provides to Creates a
			// ColorModel that may be used with the specified
			// SampleModel
			cm = ImageUtil.createColorModel(sm);
			if (cm == null)
				LOGGER.info("There are no ColorModels found");

		} else if ((buffer_type == DataBuffer.TYPE_BYTE)
				|| (buffer_type == DataBuffer.TYPE_USHORT)
				|| (buffer_type == DataBuffer.TYPE_INT)
				|| (buffer_type == DataBuffer.TYPE_FLOAT)
				|| (buffer_type == DataBuffer.TYPE_DOUBLE)) {

			// Just one band. Using the built-in Gray Scale Color Space
			cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
			cm = RasterFactory.createComponentColorModel(buffer_type, // dataType
					cs, // color space
					false, // has alpha
					false, // is alphaPremultiplied
					Transparency.OPAQUE); // transparency
		} else {
			if (buffer_type == DataBuffer.TYPE_SHORT) {
				// Just one band. Using the built-in Gray Scale Color
				// Space
				cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
				cm = new ComponentColorModel(cs, false, false,
						Transparency.OPAQUE, DataBuffer.TYPE_SHORT);
			}
		}

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
		// TODO: NEED TO BE IMPLEMENTED
		super.dispose();
	}

	public void reset() {
		// TODO: NEED TO BE IMPLEMENTED
		super.reset();
	}

	/**
	 * returns a mask needed to prevent Java transformation of integer unsigned
	 * values.
	 */
	private static int getMask(final Dataset dataset) throws IIOException {
		if (dataset != null) {
			final Datatype type = dataset.getDatatype();
			final int size = type.getDatatypeSize() * Byte.SIZE;
			if (size < 1 || size > Integer.SIZE) {
				throw new IIOException("Integers having " + size
						+ " bits are not supported.");
			}
			if (type.isUnsigned()) {
				if (size == Integer.SIZE) {
					throw new IIOException("Unsigned Integers having " + size
							+ " bits are not supported.");
				}
				return (1 << size) - 1;
			}
		}
		return ~0;
	}

	public int retrieveSubIndex(int imageIndex, int[] selectedDims) {
		int subIndexOffset=0;
		final SubDatasetInfo sdInfo = sourceStructure.getSubDatasetInfo(imageIndex); 
		for (int i=0;i<imageIndex;i++)
			subIndexOffset+=(sourceStructure.getSubDatasetSize(i));

		// X and Y dims are not taken in account
		final int selectedDimsLenght = selectedDims.length;
		final long[] subDatasetDims = sdInfo.getDims();
		final int rank = sdInfo.getRank();

		// supposing specifying all required subDimensions.
		// as an instance, if rank=5, I need to specify 3 dimensions-index
		// TODO: maybe I can assume some default behavior. 
		// as an instance, using 0 as dimension-index when not specified.
		if (selectedDimsLenght!=(rank-2)){
			throw new IndexOutOfBoundsException("The selected dims array can't be" +
					"greater than the rank of the subDataset");
		}
		final long[] multipliers = new long[rank-2];
		for (int i=0;i<selectedDimsLenght;i++){
			if (selectedDims[i]>subDatasetDims[i]){
				final StringBuffer sb = new StringBuffer();
				sb.append("At least one of the specified indexes is greater than the max allowed index in that dimension\n")
				.append("dimension=").append(i).append(" index=").append(selectedDims[i])
				.append(" while the maximum index available for this dimension is ").append(subDatasetDims[i]);
				throw new IndexOutOfBoundsException(sb.toString());
			}
		}
		for (int i=0;i<rank-2;i++){
			//Multipliers factor need to be stored in reversed order.
			multipliers[i]=subDatasetDims[rank-i];
		}
		int displacement = 0;
		for (int i=0;i<rank-2;i++){
			displacement+=(multipliers[i]*selectedDims[i]);
		}
		return subIndexOffset+displacement;
	}
	
	
	public long[] buildIndexesStructure(int specifiedIndex) {
		final int nTotalDataset=sourceStructure.getNSubdatasets();
		final long [] subDatasetSizes = sourceStructure.getSubDatasetSizes();
		int iCoverage=0;
		for (;iCoverage<nTotalDataset;iCoverage++){
			int subDatasetSize = (int)subDatasetSizes[iCoverage];
			if (specifiedIndex>=subDatasetSize)
				specifiedIndex-=subDatasetSize;
			else
				break;
		}
		SubDatasetInfo sInfo = sourceStructure.getSubDatasetInfo(iCoverage);
		final int rank = sInfo.getRank();
		final long[] indexStructure = new long[rank-1];
		final long[] dims=sInfo.getDims();
		
		final long[] multipliers = new long[rank-2];
		for (int i=0;i<rank-2;i++){
			//Multipliers factor need to be stored in reversed order.
			multipliers[i]=dims[rank-i];
		}
		for (int i=0;i<rank-2;i++){
			//TODO: End this computations
			
			
		}
		indexStructure[0]=iCoverage;
		
		
		
		return indexStructure;
	}

}
