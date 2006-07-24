package org.geotools.gce.imageio.asciigrid;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;

import org.geotools.gce.imageio.asciigrid.raster.AsciiGridRaster;
import org.geotools.gce.imageio.asciigrid.raster.EsriAsciiGridRaster;
import org.geotools.gce.imageio.asciigrid.raster.GrassAsciiGridRaster;
import org.w3c.dom.Node;


/**
 * This class represents metadata associated with images and streams.
 * 
 * @author Daniele Romagnoli
 * @author Simone Giannecchini(Simboss)
 */
public final class AsciiGridsImageMetadata extends IIOMetadata {
	public static final String nativeMetadataFormatName = "org.geotools.gce.imageio.asciigrid.AsciiGridsImageMetadata_1.0";

	public static final String[] metadataFormatNames = { nativeMetadataFormatName };

	final public static String[] rasterSpaceTypes = { "pixelIsPoint",
			"pixelIsArea" };

	private boolean GRASS;

	private double noData = Double.NaN;

	private int nRows;

	private int nCols;

	private double cellSizeX;

	private double cellSizeY;

	private double lowerLeftX = 0.0;

	private double lowerLeftY = 0.0;

	private String rasterSpaceType = null;



	public AsciiGridsImageMetadata(AsciiGridRaster raster) {
		this();
		inizializeFromRaster(raster);
	}

	public AsciiGridsImageMetadata() {
		super(false, nativeMetadataFormatName,
				"org.geotools.gce.imageio.asciigrid.AsciiGridsImageMetadata",
				null, null);
	}
/**
 * A special constructor which uses parameters provided by the client, to set
 * inner fields
 * 
 * @param cols
 * 			the number of columns
 * @param rows
 * 			the number of rows
 * @param cellsizeX
 * 			the x size of the grid cell
 * @param cellsizeY
 * 			the y size of the grid cell
 * @param xll
 * 			the xllCellCoordinate of the Bounding Box
 * @param yll
 * 			the yllCellCoordinate of the Bounding Box
 * @param isCorner
 * 			true if xll represents the xllCorner
 * @param grass
 * 			true if the Ascii grid is Grass 
 * @param inNoData
 * 			the value associated to noData grid values
 */
	public AsciiGridsImageMetadata(int cols, int rows, double cellsizeX,
			double cellsizeY, double xll, double yll, boolean isCorner,
			boolean grass, double inNoData) {
		this();
		GRASS = grass;
		nCols = cols;
		nRows = rows;
		lowerLeftX = xll;
		lowerLeftY = yll;
		rasterSpaceType = !isCorner ? rasterSpaceTypes[0] : rasterSpaceTypes[1];
		cellSizeX = cellsizeX;
		cellSizeY = cellsizeY;
		noData = inNoData;

	}

	/**
	 * returns the image metadata in a tree corresponding to the provided
	 * formatName
	 * 
	 * @param formatName
	 *            The format Name
	 * 
	 * @return 
	 * 
	 * @throws IllegalArgumentException
	 *             if the formatName is not one of the supported format names
	 */
	public Node getAsTree(String formatName) {
		if (formatName.equals(nativeMetadataFormatName)) {
			return getNativeTree();
		} else if (formatName
				.equals(IIOMetadataFormatImpl.standardMetadataFormatName)) {
			return getStandardTree();
		} else {
			throw new IllegalArgumentException("Not a recognized format!");
		}
	}

	/**
	 * @see javax.imageio.metadata.IIOMetadata#isReadOnly()
	 */
	public boolean isReadOnly() {
		return false;
	}

	/**
	 * @see javax.imageio.metadata.IIOMetadata#mergeTree(java.lang.String,
	 *      org.w3c.dom.Node)
	 */
	public void mergeTree(String formatName, Node root)
			throws IIOInvalidTreeException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see javax.imageio.metadata.IIOMetadata#reset()
	 */
	public void reset() {
		cellSizeX=cellSizeY=lowerLeftX=lowerLeftY=-1;
		nCols=nRows=-1;
		GRASS=false;
		rasterSpaceType="";
	}

	/**
	 * IIOMetadataFormat objects are meant to describe the structure of metadata
	 * returned from the getAsTree method.
	 * 
	 * @param formatName
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws IllegalArgumentException
	 *             DOCUMENT ME!
	 */
	public IIOMetadataFormat getMetadataFormat(String formatName) {
		if (formatName.equals(nativeMetadataFormatName)) {
			return new AsciiGridsImageMetadataFormat();
		}

		throw new IllegalArgumentException("Not a recognized format!");
	}

	/**
	 * This method uses access methods of the inputRaster to determine values
	 * needed for metadata initialization
	 * 
	 * @param inputRaster
	 */
	public void inizializeFromRaster(AsciiGridRaster inputRaster) {
		if (inputRaster != null) {
			nRows = inputRaster.getNRows();
			nCols = inputRaster.getNCols();
			GRASS = (inputRaster instanceof GrassAsciiGridRaster) ? true
					: false;

			if (!GRASS) {
				noData = ((EsriAsciiGridRaster) inputRaster).getNoData();
			}

			
			cellSizeX = inputRaster.getCellSizeX() ;/// inputRaster.getSourceXSubsampling();
			cellSizeY = inputRaster.getCellSizeY(); /// inputRaster.getSourceYSubsampling();

			if (inputRaster.isCorner()) {
				rasterSpaceType = rasterSpaceTypes[1];
			} else
				rasterSpaceType = rasterSpaceTypes[0];

			lowerLeftX = inputRaster.getXllCellCoordinate();
			lowerLeftY = inputRaster.getYllCellCoordinate();

		}
	}

	/**
	 * Standard tree node methods
	 */
	protected IIOMetadataNode getStandardChromaNode() {
		IIOMetadataNode node = new IIOMetadataNode("Chroma");

		IIOMetadataNode subNode = new IIOMetadataNode("ColorSpaceType");
		String colorSpaceType = "GRAY";
		subNode.setAttribute("name", colorSpaceType);
		node.appendChild(subNode);

		subNode = new IIOMetadataNode("NumChannels");

		String numChannels = "1";
		subNode.setAttribute("value", numChannels);
		node.appendChild(subNode);

		return node;
	}

	protected IIOMetadataNode getStandardCompressionNode() {
		IIOMetadataNode node = new IIOMetadataNode("Compression");

		// CompressionTypeName
		IIOMetadataNode subNode = new IIOMetadataNode("Lossless");
		subNode.setAttribute("value", "TRUE");
		node.appendChild(subNode);

		return node;
	}

	/**
	 * @return the root of the Tree containing Metadata in NativeFormat
	 */
	private Node getNativeTree() {
		final IIOMetadataNode root = new IIOMetadataNode(
				nativeMetadataFormatName);

		/**
		 * Setting Format Properties
		 */
		IIOMetadataNode node = new IIOMetadataNode("formatDescriptor");
		node.setAttribute("GRASS", Boolean.toString(GRASS));
		root.appendChild(node);

		/**
		 * Setting Grid Properties
		 */
		node = new IIOMetadataNode("gridDescriptor");
		node.setAttribute("nColumns", Integer.toString(nCols));
		node.setAttribute("nRows", Integer.toString(nRows));
		node.setAttribute("rasterSpaceType", rasterSpaceType);

		if (!GRASS) {
			node.setAttribute("noDataValue", Double.toString(noData));
		}

		root.appendChild(node);

		/**
		 * Setting Envelope Properties
		 */
		node = new IIOMetadataNode("envelopeDescriptor");
		node.setAttribute("cellsizeX", Double.toString(cellSizeX));
		node.setAttribute("cellsizeY", Double.toString(cellSizeY));

		node.setAttribute("xll", Double.toString(lowerLeftX));
		node.setAttribute("yll", Double.toString(lowerLeftY));
		root.appendChild(node);

		return root;
	}
}
