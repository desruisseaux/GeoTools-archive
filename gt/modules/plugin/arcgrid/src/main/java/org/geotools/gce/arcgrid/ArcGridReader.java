/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.gce.arcgrid;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.units.Unit;

import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.data.DataSourceException;
import org.geotools.data.PrjFileReader;
import org.geotools.factory.Hints;
import org.geotools.gce.imageio.asciigrid.AsciiGridsImageMetadata;
import org.geotools.gce.imageio.asciigrid.spi.AsciiGridsImageReaderSpi;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.parameter.Parameter;
import org.geotools.util.NumberRange;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.vividsolutions.jts.io.InStream;

/**
 * This class can read an arc grid data source (ArcGrid or GRASS ASCII) and
 * create a {@link GridCoverage2D} from the data.
 * 
 * @author Daniele Romagnoli
 * @author Simone Giannecchini (simboss)
 * @since 2.3.x
 */
public final class ArcGridReader extends AbstractGridCoverage2DReader implements
		GridCoverageReader {
	/** Logger. */
	private final static Logger LOGGER = Logger
			.getLogger("org.geotools.gce.arcgrid");

	/** Caches and ImageReaderSpi for an AsciiGridsImageReader. */
	private final static ImageReaderSpi readerSPI = new AsciiGridsImageReaderSpi();

	/** Absolute path to the parent dir for this coverage. */
	private String parentPath;

	/** No data value for this dataset. */
	private double inNoData = Double.NaN;

	/**
	 * Creates a new instance of an ArcGridReader basing the decision on whether
	 * the file is compressed or not. I assume nothing about file extension.
	 * 
	 * @param input
	 *            Source object for which we want to build an ArcGridReader.
	 * @throws DataSourceException
	 */
	public ArcGridReader(Object input) throws DataSourceException {
		this(input, null);

	}

	/**
	 * Creates a new instance of an ArcGridReader basing the decision on whether
	 * the file is compressed or not. I assume nothing about file extension.
	 * 
	 * @param input
	 *            Source object for which we want to build an ArcGridReader.
	 * @param hints
	 *            Hints to be used by this reader throughout his life.
	 * @throws DataSourceException
	 */
	public ArcGridReader(Object input, final Hints hints)
			throws DataSourceException {

		// /////////////////////////////////////////////////////////////////////
		//
		// Checking input
		//
		// /////////////////////////////////////////////////////////////////////
		coverageName = "AsciiGrid";
		try {

			// /////////////////////////////////////////////////////////////////////
			//
			// Source management
			//
			// /////////////////////////////////////////////////////////////////////
			checkSource(input);

			// /////////////////////////////////////////////////////////////////////
			//
			// CRS
			//
			// /////////////////////////////////////////////////////////////////////
			final Object tempCRS = this.hints
					.get(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM);
			if (tempCRS != null) {
				this.crs = (CoordinateReferenceSystem) tempCRS;
				LOGGER.log(Level.WARNING, new StringBuffer(
						"Using forced coordinate reference system ").append(
						crs.toWKT()).toString());
			} else
				getCoordinateReferenceSystem();

			// /////////////////////////////////////////////////////////////////////
			//
			// Reader and metadata
			//
			// /////////////////////////////////////////////////////////////////////
			// //
			//
			// Getting a reader for this format
			//
			// //
			final ImageReader reader = readerSPI.createReaderInstance();
			reader.setInput(inStream);

			// //
			//
			// Getting metadata
			//
			// //
			final Object metadata = reader.getImageMetadata(0);
			if (!(metadata instanceof AsciiGridsImageMetadata))
				throw new DataSourceException(
						"Unexpected error! Metadata are not of the expected class.");
			// casting the metadata
			final AsciiGridsImageMetadata gridMetadata = (AsciiGridsImageMetadata) metadata;

			// /////////////////////////////////////////////////////////////////////
			//
			// Envelope and other metadata
			//
			// /////////////////////////////////////////////////////////////////////
			parseMetadata(gridMetadata);

			// /////////////////////////////////////////////////////////////////////
			//
			// Informations about multiple levels and such
			//
			// /////////////////////////////////////////////////////////////////////
			getResolutionInfo(reader);

			// release the stream if we can.
			finalStreamPreparation();
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new DataSourceException(e);
		} catch (TransformException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new DataSourceException(e);
		}

	}

	/**
	 * Close the {@link InStream} {@link ImageInputStream} if we open it up on
	 * purpose toread header info for this {@link AbstractGridCoverage2DReader}.
	 * If the stream cannot be closed, we just reset and mark it.
	 * 
	 * @throws IOException
	 */
	private void finalStreamPreparation() throws IOException {
		if (closeMe)
			inStream.close();
		else {
			inStream.reset();
			inStream.mark();
		}
	}

	/**
	 * Checks the input prvided to this {@link ArcGridReader} and sets all the
	 * other objects and flags accordingly.
	 * 
	 * @param input
	 *            provied to this {@link ArcGridReader}.
	 * @throws UnsupportedEncodingException
	 * @throws DataSourceException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void checkSource(Object input) throws UnsupportedEncodingException,
			DataSourceException, IOException, FileNotFoundException {
		if (input == null) {
			final DataSourceException ex = new DataSourceException(
					"No source set to read this coverage.");
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			throw ex;
		}
		this.source = input;
		if (hints != null)
			this.hints.add(hints);
		closeMe = true;
		// //
		//
		// URL to FIle
		//
		// //
		// if it is a URL ponting to a File I convert it to a file,
		// otherwis, later on, I will try to get an inputstream out of it.
		if (input instanceof URL) {
			// URL that point to a file
			final URL sourceURL = ((URL) input);
			if (sourceURL.getProtocol().compareToIgnoreCase("file") == 0) {
				this.source = input = new File(URLDecoder.decode(sourceURL
						.getFile(), "UTF-8"));

			}
		}

		// //
		//
		// File
		//
		// //
		if (input instanceof File) {
			final File sourceFile = (File) input;
			if (!sourceFile.exists() || sourceFile.isDirectory()
					|| !sourceFile.canRead())
				throw new DataSourceException(
						"Provided file does not exist or is a directory or is not readable!");
			this.parentPath = sourceFile.getParent();
			this.coverageName = sourceFile.getName();
			final int dotIndex = coverageName.indexOf(".");
			gzipped = coverageName.toLowerCase().endsWith("gz");
			coverageName = (dotIndex == -1) ? coverageName : coverageName
					.substring(0, dotIndex);
			inStream = gzipped ? ImageIO
					.createImageInputStream(new GZIPInputStream(
							new FileInputStream(sourceFile))) : ImageIO
					.createImageInputStream(sourceFile);
		} else
		// //
		//
		// URL
		//
		// //
		if (input instanceof URL) {
			final URL tempURL = ((URL) input);
			input = tempURL.openConnection().getInputStream();
			GZIPInputStream gzInStream = null;
			try {
				gzInStream = new GZIPInputStream((InputStream) input);
				gzipped = false;
			} catch (Exception e) {
				gzipped = false;
			}
			input = tempURL.openConnection().getInputStream();
			inStream = gzipped ? ImageIO.createImageInputStream(gzInStream)
					: ImageIO.createImageInputStream(tempURL.openConnection()
							.getInputStream());
		} else
		// //
		//
		// InputStream
		//
		// //
		if (input instanceof InputStream) {
			closeMe = false;
			if (ImageIO.getUseCache())
				inStream = new FileCacheImageInputStream((InputStream) input,
						null);
			else
				inStream = new MemoryCacheImageInputStream((InputStream) input);
			// let's mark it
			inStream.mark();
		} else
		// //
		//
		// ImageInputStream
		//
		// //
		if (input instanceof ImageInputStream) {
			closeMe = false;
			inStream = (ImageInputStream) input;
			inStream.mark();
		} else
			throw new IllegalArgumentException("Unsupported input type");

		if (inStream == null)
			throw new DataSourceException(
					"No input stream for the provided source");
	}

	/**
	 * Gets resolution information about the coverage itself.
	 * 
	 * @param reader
	 *            an {@link ImageReader} to use for getting the resolution
	 *            information.
	 * @throws IOException
	 * @throws TransformException
	 */
	private void getResolutionInfo(ImageReader reader) throws IOException,
			TransformException {

		// //
		//
		// get the dimension of the hr image and build the model as well as
		// computing the resolution
		// //
		final Rectangle actualDim = new Rectangle(0, 0, reader.getWidth(0),
				reader.getHeight(0));
		originalGridRange = new GeneralGridRange(actualDim);

		// ///
		//
		// setting the higher resolution avalaible for this coverage
		//
		// ///
		highestRes = getResolution(originalEnvelope, actualDim, crs);

	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#getFormat()
	 */
	public Format getFormat() {
		return new ArcGridFormat();
	}

	/**
	 * Reads a {@link GridCoverage2D} possibly matching as close as possible the
	 * resolution computed by using the input params provided by using the
	 * parameters for this {@link #read(GeneralParameterValue[])}.
	 * 
	 * <p>
	 * To have an idea about the possible read parameters take a look at
	 * {@link AbstractGridFormat} class and {@link ArcGridFormat} class.
	 * 
	 * @param params
	 *            an array of {@link GeneralParameterValue} containing the
	 *            parameters to control this read process.
	 * 
	 * @return a {@link GridCoverage2D}.
	 * 
	 * @see AbstractGridFormat
	 * @see ArcGridFormat
	 * @see org.opengis.coverage.grid.GridCoverageReader#read(org.opengis.parameter.GeneralParameterValue[])
	 */
	public GridCoverage read(GeneralParameterValue[] params)
			throws IllegalArgumentException, IOException {
		GeneralEnvelope readEnvelope = null;
		Rectangle requestedDim = null;
		if (params != null) {

			final int length = params.length;
			Parameter param;
			String name;
			for (int i = 0; i < length; i++) {
				param = (Parameter) params[i];
				name = param.getDescriptor().getName().getCode();
				if (name.equals(AbstractGridFormat.READ_GRIDGEOMETRY2D
						.getName().toString())) {
					final GridGeometry2D gg = (GridGeometry2D) param.getValue();
					readEnvelope = new GeneralEnvelope((Envelope) gg
							.getEnvelope2D());
					requestedDim = gg.getGridRange2D().getBounds();
				}

			}
		}
		return createCoverage(readEnvelope, requestedDim);
	}

	/**
	 * This method creates the GridCoverage2D from the underlying file.
	 * 
	 * @param requestedDim
	 * @param readEnvelope
	 * 
	 * 
	 * @return a GridCoverage
	 * 
	 * @throws java.io.IOException
	 */
	private GridCoverage createCoverage(GeneralEnvelope requestedEnvelope,
			Rectangle requestedDim) throws IOException {

		if (!closeMe) {

			inStream.reset();
			inStream.mark();
		}
		// /////////////////////////////////////////////////////////////////////
		//
		// Doing an image read for reading the coverage.
		//
		// /////////////////////////////////////////////////////////////////////

		// //
		//
		// Setting subsampling factors with some checkings
		// 1) the subsampling factors cannot be zero
		// 2) the subsampling factors cannot be such that the w or h are zero
		//
		// //
		final ImageReadParam readP = new ImageReadParam();
		final Integer imageChoice;
		try {
			imageChoice = setReadParams(readP, requestedEnvelope, requestedDim);
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return null;
		} catch (TransformException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return null;
		}

		// //
		//
		// image and metadata
		//
		// //
		final ParameterBlock pbjImageRead = new ParameterBlock();
		// prepare input to handle possible parallelism between different
		// readers
		if (source instanceof File) {
			if (!gzipped)
				pbjImageRead.add(ImageIO.createImageInputStream(source));
			else
				pbjImageRead.add(ImageIO
						.createImageInputStream(new GZIPInputStream(
								new FileInputStream((File) source))));
		} else if (source instanceof ImageInputStream
				|| source instanceof InputStream)
			pbjImageRead.add(inStream);
		else if (source instanceof URL) {
			if (gzipped)
				ImageIO.createImageInputStream(new GZIPInputStream(
						((URL) source).openConnection().getInputStream()));
			else
				pbjImageRead.add(ImageIO.createImageInputStream(((URL) source)
						.openConnection().getInputStream()));

		}
		pbjImageRead.add(imageChoice);
		pbjImageRead.add(Boolean.FALSE);
		pbjImageRead.add(Boolean.FALSE);
		pbjImageRead.add(Boolean.FALSE);
		pbjImageRead.add(null);
		pbjImageRead.add(null);
		pbjImageRead.add(readP);
		pbjImageRead.add(readerSPI.createReaderInstance());
		final RenderedOp asciiCoverage = JAI.create("ImageRead", pbjImageRead,
				hints);

		// /////////////////////////////////////////////////////////////////////
		//
		// Creating the coverage
		//
		// /////////////////////////////////////////////////////////////////////
		try {

			//
			// ///////////////////////////////////////////////////////////////////
			//
			// Categories
			//
			//
			// ///////////////////////////////////////////////////////////////////
			Unit uom = null;
			final Category values = new Category("values", demColors,
					new NumberRange(1, 255), new NumberRange(0, 8849));
			final Category nan;
			if (Double.isNaN(inNoData))
				nan = new Category("No Data", new Color(0, 0, 0, 0), 0);
			else
				nan = new Category("No Data", new Color[] { new Color(0, 0, 0,
						0) }, new NumberRange(0, 0), new NumberRange(inNoData,
						inNoData));

			//
			// ///////////////////////////////////////////////////////////////////
			//
			// Sample dimension
			//
			//
			// ///////////////////////////////////////////////////////////////////
			final GridSampleDimension band = new GridSampleDimension(
					coverageName, new Category[] { nan, values }, uom)
					.geophysics(true);
			final Map properties = new HashMap();
			properties.put("GC_NODATA", new Double(inNoData));

			// /////////////////////////////////////////////////////////////////////
			//
			// Coverage
			//
			// /////////////////////////////////////////////////////////////////////
			return coverageFactory.create(coverageName, asciiCoverage,
					originalEnvelope, new GridSampleDimension[] { band }, null,
					properties);

		} catch (NoSuchElementException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new DataSourceException(e);
		}
	}

	/**
	 * This method is responsible for building up an envelope according to the
	 * definition of the crs. It assumes that X coordinate on the ascii grid
	 * itself maps to longitude and y coordinate maps to latitude.
	 * 
	 * @param gridMetadata
	 *            The {@link AsciiGridsImageMetadata} to parse.
	 * 
	 * @throws MismatchedDimensionException
	 */
	private void parseMetadata(AsciiGridsImageMetadata gridMetadata)
			throws MismatchedDimensionException {

		// getting metadata
		final Node root = gridMetadata
				.getAsTree("org.geotools.gce.imageio.asciigrid.AsciiGridsImageMetadata_1.0");

		// getting Grid Properties
		Node child = root.getFirstChild();
		NamedNodeMap attributes = child.getAttributes();
		final boolean grass = attributes.getNamedItem("GRASS").getNodeValue()
				.equalsIgnoreCase("True");

		// getting Grid Properties
		child = child.getNextSibling();
		attributes = child.getAttributes();
		final int hrWidth = Integer.parseInt(attributes
				.getNamedItem("nColumns").getNodeValue());
		final int hrHeight = Integer.parseInt(attributes.getNamedItem("nRows")
				.getNodeValue());
		originalGridRange = new GeneralGridRange(new Rectangle(0, 0, hrWidth,
				hrHeight));
		final boolean pixelIsArea = attributes.getNamedItem("rasterSpaceType")
				.getNodeValue().equalsIgnoreCase(
						AsciiGridsImageMetadata.rasterSpaceTypes[1]);
		if (!grass)
			inNoData = Double.parseDouble(attributes
					.getNamedItem("noDataValue").getNodeValue());

		// getting Envelope Properties
		child = child.getNextSibling();
		attributes = child.getAttributes();
		final double cellsizeX = Double.parseDouble(attributes.getNamedItem(
				"cellsizeX").getNodeValue());
		final double cellsizeY = Double.parseDouble(attributes.getNamedItem(
				"cellsizeY").getNodeValue());
		double xll = Double.parseDouble(attributes.getNamedItem("xll")
				.getNodeValue());
		double yll = Double.parseDouble(attributes.getNamedItem("yll")
				.getNodeValue());

		// /////////////////////////////////////////////////////////////////////
		//
		// Geotiff specification says that PixelIsArea map a pixel to the corner
		// of the grid while PixelIsPoint map a pixel to the centre of the grid.
		//
		// /////////////////////////////////////////////////////////////////////
		if (!pixelIsArea) {
			final double correctionX = cellsizeX / 2d;
			final double correctionY = cellsizeY / 2d;
			xll -= correctionX;
			yll -= correctionY;
		}

		originalEnvelope = new GeneralEnvelope(new double[] { xll, yll },
				new double[] { xll + (hrWidth * cellsizeX),
						yll + (hrHeight * cellsizeY) });

		// setting the coordinate reference system for the envelope
		originalEnvelope.setCoordinateReferenceSystem(crs);

	}

	/**
	 * Gets the coordinate system that will be associated to the
	 * {@link GridCoverage}. The WGS84 coordinate system is used by default. It
	 * is worth to point out that when reading from a stream which is not
	 * connected to a file, like from an http connection (e.g. from a WCS) we
	 * cannot rely on receiving a prj file too. In this case the exchange of
	 * information about referencing should proceed the exchange of data thus I
	 * rely on this and I ask the user who's invoking the read operation to
	 * provide me a valid crs and envelope through read parameters.
	 * 
	 * @throws FactoryException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void getCoordinateReferenceSystem() throws FileNotFoundException,
			IOException {

		if (source instanceof File) {
			crs = null;
			final String prjPath = new StringBuffer(this.parentPath).append(
					File.separatorChar).append(this.coverageName)
					.append(".prj").toString();
			// read the prj info from the file
			// if does not exist go for the default value

			try {
				final File prj = new File(prjPath);
				if (prj.exists()) {
					final PrjFileReader prjReader = new PrjFileReader(
							new FileInputStream(prj).getChannel());
					crs = prjReader.getCoodinateSystem();
				}

			} catch (FileNotFoundException e) {
				LOGGER.info("PRJ file not found proceeding with EPSG:4326");
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				// In this case there is not a prj file or it
				// does not contain a valid CRS
				crs = null;
			} catch (IOException e) {
				LOGGER.info("PRJ file not found proceeding with EPSG:4326");
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				// In this case there is not a prj file or it
				// does not contain a valid CRS
				crs = null;
			} catch (FactoryException e) {
				LOGGER.info("PRJ file not found proceeding with EPSG:4326");
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				// In this case there is not a prj file or it
				// does not contain a valid CRS
				crs = null;
			}
		}

		if (crs == null) {
			LOGGER.info("crs not found proceeding with EPSG:4326");
			crs = ArcGridFormat.getDefaultCRS();
		}

	}

}
