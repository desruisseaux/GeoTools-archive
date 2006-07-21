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
package org.geotools.gce.image;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.data.DataSourceException;
import org.geotools.data.PrjFileReader;
import org.geotools.data.coverage.grid.AbstractGridCoverage2DReader;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.parameter.Parameter;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.image.ImageUtilities;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

/**
 * Reads a GridCoverage from a given source. WorldImage sources only support one
 * GridCoverage so hasMoreGridCoverages() will return true until the only
 * GridCoverage is read. No metadata is currently supported, so all related
 * methods return null. In the early future we will start (hopefully supporting
 * them).
 * 
 * @author simone giannecchini
 * @author alessio fabiani
 * @author rgould
 */
public final class WorldImageReader extends AbstractGridCoverage2DReader
		implements GridCoverageReader {

	private Logger LOGGER = Logger.getLogger(WorldImageReader.class.toString());

	/** Number of coverages left */
	private boolean gridLeft = false;

	private boolean wmsRequest;

	private boolean metaFile;

	private String parentPath;

	private String extension;

	private CoordinateSystem cs;

	/**
	 * Class constructor. Construct a new ImageWorldReader to read a
	 * GridCoverage from the source object. The source must point to the raster
	 * file itself, not the world file. If the source is a Java URL it checks if
	 * it is ponting to a file and if so it converts the url into a file.
	 * 
	 * @param input
	 *            The source of a GridCoverage, can be a File, a URL or an input
	 *            stream.
	 * @throws DataSourceException
	 */
	public WorldImageReader(Object input) throws DataSourceException {
		// /////////////////////////////////////////////////////////////////////
		//
		// Checking input
		//
		// /////////////////////////////////////////////////////////////////////
		this.source = input;
		coverageName = "image_coverage";
		format = new WorldImageFormat();
		if (input == null) {

			final IOException ex = new IOException(
					"WorldImage:No source set to read this coverage.");
			LOGGER.logp(Level.SEVERE, WorldImageReader.class.toString(),
					"WorldImageReader", ex.getLocalizedMessage(), ex);
			throw new DataSourceException(ex);
		}
		try {
			boolean closeMe = true;

			// /////////////////////////////////////////////////////////////////////
			//
			// Source management
			//
			// /////////////////////////////////////////////////////////////////////
			if (input instanceof URL) {
				// URL that point to a file
				final URL sourceURL = ((URL) input);
				if (sourceURL.getProtocol().compareToIgnoreCase("file") == 0) {
					this.source = input = new File(URLDecoder.decode(sourceURL
							.getFile(), "UTF-8"));
				} else if (sourceURL.getProtocol().equalsIgnoreCase("http")) {
					// // getting a stream to the reader
					// this.source = sourceURL.openStream();

					// /////////////////////////////////////////////////////////////////////
					//
					// WMS Request? I want to be able to handle that case too
					//
					// /////////////////////////////////////////////////////////////////////
					wmsRequest = WMSRequest();

				}

			}
			// //
			//
			// Name, path, etc...
			//
			// //
			if (input instanceof File) {
				final File sourceFile = (File) input;
				final String filename = sourceFile.getName();
				final int i = filename.lastIndexOf('.');
				final int length = filename.length();
				if (i > 0 && i < length - 1) {
					extension = filename.substring(i + 1).toLowerCase();
				}
				this.parentPath = sourceFile.getParent();
				this.coverageName = filename;
				final int dotIndex = coverageName.lastIndexOf(".");
				coverageName = (dotIndex == -1) ? coverageName : coverageName
						.substring(0, dotIndex);
			} else if (input instanceof URL)
				input = ((URL) input).openStream();
			// //
			//
			// Get a stream in order to read from it for getting the basic
			// information for this coverfage
			//
			// //
			if (input instanceof ImageInputStream)
				closeMe = false;
			inStream = (ImageInputStream) (this.source instanceof ImageInputStream ? this.source
					: ImageIO
							.createImageInputStream((this.source instanceof URL) ? ((URL) this.source)
									.openStream()
									: this.source));
			if (inStream == null)
				throw new IllegalArgumentException(
						"No input stream for the provided source");

			// /////////////////////////////////////////////////////////////////////
			//
			// CRS
			//
			// /////////////////////////////////////////////////////////////////////
			if (!wmsRequest)
				readCRS();

			// /////////////////////////////////////////////////////////////////////
			//
			// Informations about multiple levels and such
			//
			// /////////////////////////////////////////////////////////////////////
			getHRInfo();

			// release the stream
			if (closeMe)
				inStream.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new DataSourceException(e);
		} catch (TransformException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new DataSourceException(e);
		}
	}

	/**
	 * Gets the relevant information for the underlying raster.
	 * 
	 * @throws IOException
	 * @throws TransformException
	 */
	private void getHRInfo() throws IOException, TransformException {

		// //
		//
		// Get a reader for this format
		// TOTO optimize this using image file extension when possible
		//
		// //
		final Iterator it = ImageIO.getImageReaders(inStream);
		if (!it.hasNext())
			throw new DataSourceException("No reader avalaible for this source");
		final ImageReader reader = (ImageReader) it.next();
		reader.setInput(inStream);

		// //
		//
		// get the dimension of the hr image and build the model as well as
		// computing the resolution
		// //
		maxImages = wmsRequest ? 1 : reader.getNumImages(true);
		int hrWidth = reader.getWidth(0);
		int hrHeight = reader.getHeight(0);
		final Rectangle actualDim = new Rectangle(0, 0, hrWidth, hrHeight);
		originalGridRange = new GeneralGridRange(actualDim);

		// //
		//
		// get information for the overviews in case ony exists
		//
		// //
		if (maxImages > 1) {
			overViewDimensions = new int[maxImages - 1][2];
			for (int i = 1; i < maxImages; i++) {
				overViewDimensions[i - 1][0] = reader.getWidth(i);
				overViewDimensions[i - 1][1] = reader.getHeight(i);
			}

		} else
			overViewDimensions = null;

		// /////////////////////////////////////////////////////////////////////
		//
		// Envelope, coverage name and other resolution information
		//
		// /////////////////////////////////////////////////////////////////////
		if (source instanceof File) {
			prepareWorldImageGridToWorldTransform();

			// //
			//
			// In case we read from a real world file we have toget the envelope
			//
			// //
			if (!metaFile) {
				final AffineTransform tempTransform = new AffineTransform(
						(AffineTransform) raster2Model);
				tempTransform.translate(-0.5, -0.5);

				originalEnvelope = CRSUtilities.transform(ProjectiveTransform
						.create(tempTransform), new GeneralEnvelope(actualDim));
				originalEnvelope.setCoordinateReferenceSystem(crs);
			}

			// ///
			//
			// setting the higher resolution avalaible for this coverage
			//
			// ///
			higherRes = getResolution(originalEnvelope, actualDim, crs);

		}

	}

	/**
	 * Returns the format that this Reader accepts.
	 * 
	 * @return a new WorldImageFormat class
	 */
	public Format getFormat() {
		return this.format;
	}

	/**
	 * Returns the source object containing the GridCoverage. Note that it
	 * points to the raster, and not the world file.
	 * 
	 * @return the source object containing the GridCoverage.
	 */
	public Object getSource() {
		return source;
	}

	/**
	 * Metadata is not suported. Returns null.
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	public String[] getMetadataNames() throws IOException {
		return null;
	}

	/**
	 * Metadata is not supported. Returns null.
	 * 
	 * @param name
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 * @throws MetadataNameNotFoundException
	 *             DOCUMENT ME!
	 */
	public String getMetadataValue(String name) throws IOException,
			MetadataNameNotFoundException {
		return null;
	}

	/**
	 * WorldImage GridCoverages are not named. Returns null.
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	public String[] listSubNames() throws IOException {
		return null;
	}

	/**
	 * WorldImage GridCoverages are not named. Returns null.
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	public String getCurrentSubname() throws IOException {
		return null;
	}

	/**
	 * Returns true until read has been called, as World Image files only
	 * support one GridCoverage.
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	public boolean hasMoreGridCoverages() throws IOException {
		return gridLeft;
	}

	/**
	 * Reads an image from a source stream. Loads an image from a source stream,
	 * then loads the values from the world file and constructs a new
	 * GridCoverage from this information. When reading from a remote stream we
	 * do not look for a world fiel but we suppose those information comes from
	 * a different way (xml, gml, pigeon?)
	 * 
	 * @param parameters
	 *            WorldImageReader supports no parameters, it just ignores them.
	 * 
	 * @return a new GridCoverage read from the source.
	 * 
	 * @throws IllegalArgumentException
	 *             DOCUMENT ME!
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	public GridCoverage read(GeneralParameterValue[] params)
			throws IllegalArgumentException, IOException {

		// /////////////////////////////////////////////////////////////////////
		//
		// do we have paramters to use for reading from the specified source
		//
		// /////////////////////////////////////////////////////////////////////
		GeneralEnvelope requestedEnvelope = null;
		Rectangle dim = null;
		if (params != null) {
			// /////////////////////////////////////////////////////////////////////
			//
			// Checking params
			//
			// /////////////////////////////////////////////////////////////////////
			if (params != null) {
				Parameter param;
				final int length = params.length;
				for (int i = 0; i < length; i++) {
					param = (Parameter) params[i];

					if (param.getDescriptor().getName().getCode().equals(
							AbstractGridFormat.READ_GRIDGEOMETRY2D.getName()
									.toString())) {
						final GridGeometry2D gg = (GridGeometry2D) param
								.getValue();
						requestedEnvelope = new GeneralEnvelope((Envelope) gg
								.getEnvelope2D());
						dim = gg.getGridRange2D().getBounds();
					}
				}
			}
		}

		// /////////////////////////////////////////////////////////////////////
		//
		// set params
		//
		// /////////////////////////////////////////////////////////////////////
		Integer imageChoice = new Integer(0);
		final ImageReadParam readP = new ImageReadParam();
		try {
			imageChoice = setReadParams(readP, requestedEnvelope, dim, source);
		} catch (TransformException e) {
			new DataSourceException(e);
		}

		// /////////////////////////////////////////////////////////////////////
		//
		// Reading the source layer
		//
		// /////////////////////////////////////////////////////////////////////
		final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
				"ImageRead");
		pbjImageRead
				.setParameter(
						"Input",
						ImageIO
								.createImageInputStream((this.source instanceof URL) ? ((URL) this.source)
										.openStream()
										: this.source));
		pbjImageRead.setParameter("ReadParam", readP);
		pbjImageRead.setParameter("ImageChoice", imageChoice);
		pbjImageRead.setParameter("ReadMetadata", Boolean.FALSE);
		pbjImageRead.setParameter("VerifyInput", Boolean.FALSE);
		pbjImageRead.setParameter("ReadThumbnails", Boolean.FALSE);

		final RenderedOp image = ImageUtilities.tileImage(JAI.create(
				"ImageRead", pbjImageRead));

		// /////////////////////////////////////////////////////////////////////
		//
		// creating the coverage
		//
		// /////////////////////////////////////////////////////////////////////
		return createImageCoverage(image);
	}

	/**
	 * This method is sued to check if we are connecting directly to a WMS with
	 * a getmap request. In such a case we skip reading all the parameters we
	 * can read from this http string.
	 * 
	 * @return true if we are dealing with a WMS request, false otherwise.
	 */
	private boolean WMSRequest() {
		// TODO do we need the requested envelope?
		if (source instanceof URL
				&& (((URL) source).getProtocol().equalsIgnoreCase("http"))) {
			try {
				// getting the query
				final String query = java.net.URLDecoder.decode(((URL) source)
						.getQuery().intern(), "UTF-8");

				// should we proceed? Let's look for a getmap WMS request
				if (query.intern().indexOf("GetMap") == -1) {
					return false;
				}

				// tokenizer on $
				final String[] pairs = query.split("&");

				// parse each pair
				final int numPairs = pairs.length;
				String[] kvp = null;

				for (int i = 0; i < numPairs; i++) {
					// splitting the pairs
					kvp = pairs[i].split("=");

					// checking the fields
					// BBOX
					if (kvp[0].equalsIgnoreCase("BBOX")) {
						// splitting fields
						kvp = kvp[1].split(",");
						originalEnvelope = new GeneralEnvelope(new double[] {
								Double.parseDouble(kvp[0]),
								Double.parseDouble(kvp[1]) }, new double[] {
								Double.parseDouble(kvp[2]),
								Double.parseDouble(kvp[3]) });
					}

					// SRS
					if (kvp[0].equalsIgnoreCase("SRS")) {
						crs = CRS.decode(kvp[1]);
					}

					// layers
					if (kvp[0].equalsIgnoreCase("layers")) {
						this.coverageName = kvp[1].replaceAll(",", "_");
					}
				}
				// adjust envelope
				if (CRSUtilities.getCRS2D(crs).getCoordinateSystem().getAxis(0)
						.getDirection().absolute().equals(AxisDirection.NORTH)) {
					final GeneralEnvelope tempEnvelope = new GeneralEnvelope(
							new double[] {
									originalEnvelope.getLowerCorner()
											.getOrdinate(1),
									originalEnvelope.getLowerCorner()
											.getOrdinate(0) }, new double[] {
									originalEnvelope.getUpperCorner()
											.getOrdinate(1),
									originalEnvelope.getUpperCorner()
											.getOrdinate(0) });
					originalEnvelope = new GeneralEnvelope(tempEnvelope);
					originalEnvelope.setCoordinateReferenceSystem(crs);

				}

			} catch (IOException e) {
				// TODO how to handle this?
				return false;

			} catch (NoSuchAuthorityCodeException e) {
				// TODO how to handle this?
				return false;
			} catch (MismatchedDimensionException e) {
				// TODO how to handle this?
				return false;
			} catch (IndexOutOfBoundsException e) {
				// TODO how to handle this?
				return false;
			} catch (TransformException e) {
				// TODO how to handle this?
				return false;
			} catch (FactoryException e) {
//				 TODO how to handle this?
				return false;
			}

			return true;
		}

		return false;
	}

	/**
	 * This method is responsible for reading the CRS whhther a projection file
	 * is provided. If no projection file is provided the second choice is the
	 * CRS supplied via the crs paramter. If even this one is not avalaible we
	 * default to EPSG:4326.
	 * 
	 * @throws IOException
	 */
	private void readCRS() throws IOException {

		// check to see if there is a projection file
		if (source instanceof File
				|| (source instanceof URL && (((URL) source).getProtocol() == "file"))) {
			// getting name for the prj file
			final String sourceAsString;

			if (source instanceof File) {
				sourceAsString = ((File) source).getAbsolutePath();
			} else {
				sourceAsString = ((URL) source).getFile();
			}

			final int index = sourceAsString.lastIndexOf(".");
			final StringBuffer base = new StringBuffer(sourceAsString
					.substring(0, index)).append(".prj");

			// does it exist?
			final File prjFile = new File(base.toString());
			if (prjFile.exists()) {
				// it exists then we have top read it

				try {
					final FileChannel channel = new FileInputStream(prjFile)
							.getChannel();
					final PrjFileReader projReader = new PrjFileReader(channel);
					crs = projReader.getCoodinateSystem();
				} catch (FileNotFoundException e) {
					// warn about the error but proceed, it is not fatal
					// we have at least the default crs to use
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				} catch (IOException e) {
					// warn about the error but proceed, it is not fatal
					// we have at least the default crs to use
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				} catch (FactoryException e) {
					// warn about the error but proceed, it is not fatal
					// we have at least the default crs to use
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}

			}
		}
		if (crs == null) {
			crs = AbstractGridFormat.getDefaultCRS();
			LOGGER.info(new StringBuffer(
					"Unable to find crs, continuing with default WGS4 CRS")
					.append("\n").append(crs.toWKT()).toString());
		}
		try {
			cs = CRSUtilities.getCRS2D(crs).getCoordinateSystem();
		} catch (TransformException e) {
			throw new DataSourceException(e);

		}
		longitudeFirst = !GridGeometry2D.swapXY(cs);

	}

	/**
	 * This method is in charge for reading the metadata file and for creating a
	 * valid envelope (whether possible);
	 * 
	 * TODO it would be great to having a centralized management for the world
	 * file
	 * 
	 * @throws IOException
	 */
	private void prepareWorldImageGridToWorldTransform() throws IOException {

		// getting name and extension
		final String base = (parentPath != null) ? new StringBuffer(
				this.parentPath).append(File.separator).append(coverageName)
				.toString() : coverageName;

		// We can now construct the baseURL from this string.
		File file2Parse = new File(new StringBuffer(base).append(".wld")
				.toString());

		if (file2Parse.exists()) {
			// parse world file
			parseWorldFile(file2Parse);
		} else {
			// looking for another extension
			file2Parse = new File(new StringBuffer(base).append(
					WorldImageFormat.getWorldExtension(extension)).toString());

			if (file2Parse.exists()) {
				// parse world file
				parseWorldFile(file2Parse);
				metaFile = false;
			} else {
				// looking for a meta file
				file2Parse = new File(new StringBuffer(base).append(".meta")
						.toString());

				if (file2Parse.exists()) {
					parseMetaFile(file2Parse);
					metaFile = true;
				} else {
					final IOException ex = new IOException(
							"No file with meta information found!");
					LOGGER
							.logp(
									Level.SEVERE,
									WorldImageReader.class.toString(),
									"private void prepareWorldImage2Model() throws IOException",
									ex.getLocalizedMessage(), ex);
					throw ex;
				}
			}
		}
	}

	/**
	 * This method is responsible for parsing a META file which is nothing more
	 * than another format of a WorldFile used by the GIDB database.
	 * 
	 * @param file2Parse
	 *            DOCUMENT ME!
	 * 
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private void parseMetaFile(File file2Parse) throws NumberFormatException,
			IOException {
		double xMin = 0.0;
		double yMax = 0.0;
		double xMax = 0.0;
		double yMin = 0.0;

		// getting a buffered reader
		final BufferedReader in = new BufferedReader(new FileReader(file2Parse));

		// parsing the lines
		String str = null;
		int index = 0;
		double value = 0;

		while ((str = in.readLine()) != null) {
			switch (index) {
			case 1:
				value = Double.parseDouble(str.substring("Origin Longitude = "
						.intern().length()));
				xMin = value;

				break;

			case 2:
				value = Double.parseDouble(str.substring("Origin Latitude = "
						.intern().length()));
				yMin = value;

				break;

			case 3:
				value = Double.parseDouble(str.substring("Corner Longitude = "
						.intern().length()));
				xMax = value;

				break;

			case 4:
				value = Double.parseDouble(str.substring("Corner Latitude = "
						.intern().length()));
				yMax = value;

				break;

			default:
				break;
			}

			index++;
		}

		in.close();

		// building up envelope of this coverage
		if (longitudeFirst)
			originalEnvelope = new GeneralEnvelope(new double[] { xMin, yMin },
					new double[] { xMax, yMax });
		else
			originalEnvelope = new GeneralEnvelope(new double[] { yMin, xMin },
					new double[] { yMax, xMax });
		originalEnvelope.setCoordinateReferenceSystem(crs);
	}

	/**
	 * This method is responsible for parsing the world file associate with the
	 * coverage to be read.
	 * 
	 * @param file2Parse
	 *            File to parse for reading needed parameters.
	 * 
	 * @throws IOException
	 */
	private void parseWorldFile(File file2Parse) throws IOException {

		float xPixelSize = 0;
		float rotationX = 0;
		float rotationY = 0;
		float yPixelSize = 0;
		double xULC = 0;
		double yULC = 0;

		int index = 0;
		float value = 0;
		String str;
		final BufferedReader in = new BufferedReader(new FileReader(file2Parse));
		while ((str = in.readLine()) != null) {
			value = 0;

			try {
				value = Float.parseFloat(str.trim());
			} catch (NumberFormatException e) {
				// A trick to bypass invalid lines ...
				continue;
			}

			switch (index) {
			case 0:
				xPixelSize = value;

				break;

			case 1:
				rotationX = value;

				break;

			case 2:
				rotationY = value;

				break;

			case 3:
				yPixelSize = value;

				break;

			case 4:
				xULC = value;

				break;

			case 5:
				yULC = value;

				break;

			default:
				break;
			}

			index++;
		}
		in.close();

		// /////////////////////////////////////////////////////////////////////
		//
		// It is worth to point out that various data sources describe the
		// parameters in the world file as the mapping from the pixel centres'
		// to the associated world coords.
		// Here we directly build the needed grid to world transform and we DO
		// NOT add any half a pixel translation given that, as stated above, the
		// values we receive should map to the centre of the pixel.
		//
		// /////////////////////////////////////////////////////////////////////

		// building the transform
		final GeneralMatrix gm = new GeneralMatrix(3); // identity

		// compute an "offset and scale" matrix
		gm.setElement(0, 0, (longitudeFirst) ? xPixelSize : rotationX);
		gm.setElement(1, 1, (longitudeFirst) ? yPixelSize : rotationY);
		gm.setElement(0, 1, (longitudeFirst) ? rotationX : xPixelSize);
		gm.setElement(1, 0, (longitudeFirst) ? rotationY : yPixelSize);

		gm.setElement(0, 2, (longitudeFirst) ? xULC : yULC);
		gm.setElement(1, 2, (longitudeFirst) ? yULC : xULC);

		// make it a LinearTransform
		raster2Model = ProjectiveTransform.create(gm);

	}

	/**
	 * Not supported, does nothing.
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	public void skip() throws IOException {
	}

	/**
	 * Cleans up the Reader (currently does nothing)
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	public void dispose() throws IOException {
	}

}
