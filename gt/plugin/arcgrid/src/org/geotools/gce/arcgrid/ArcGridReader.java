/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, 2004 Geotools Project Managment Committee (PMC)
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
import java.io.IOException;
import java.io.Reader;
import java.util.NoSuchElementException;

import org.geotools.coverage.grid.GridCoverageImpl;
import org.geotools.data.DataSourceException;
import org.geotools.data.coverage.grid.stream.IOExchange;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.parameter.Parameter;
import org.geotools.parameter.ParameterGroup;
import org.geotools.referencing.crs.GeographicCRS;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.InvalidParameterNameException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.OperationNotFoundException;

import com.vividsolutions.jts.geom.Envelope;
import java.io.File;
import java.net.URL;
import org.opengis.parameter.ParameterValueGroup;
import org.geotools.data.coverage.grid.UnknownFormat;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import javax.media.jai.RasterFactory;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.DataBuffer;
import java.awt.color.ICC_Profile;
import java.awt.color.ICC_ColorSpace;
import java.io.BufferedReader;
import java.io.FileReader;
import org.geotools.referencing.wkt.Parser;

/**
 * This class can read an arc grid data source and create a grid coverage from
 * the data.
 *
 * @author jeichar
 */
public class ArcGridReader  implements GridCoverageReader {
  /**Source object to read from.*/
  private Object mSource;

  /**Reader used to read the source object.*/
  private Reader mReader;

  /**Format of the source to read from.*/
  private Format format= null;

  private IOExchange mExchange = IOExchange.getIOExchange();

  /** Default color ramp */
  private Color[] demColors = new Color[] {
      Color.BLUE, Color.WHITE,
      Color.RED};

  /** The coordinate system associated to the returned GridCoverage.*/
  private CoordinateReferenceSystem coordinateSystem = null;

  /** The grid coverage read from the data file. */
  private java.lang.ref.SoftReference gridCoverage = null;

  /** The raster read from the data file. */
  private ArcGridRaster arcGridRaster = null;

  /** A name for the grid coverage. */
  private String name;

  /**Creates a new instance of an ArcGridReader.
   *
   * Creates a new instance of an ArcGridReader basing the decision on whether the file is compressed or not.
   * I assume nothing about file extension.
   *
   * @param aSource Source object for which we wnat to build an ArcGridReader.
   *
   */
  public ArcGridReader(Object aSource) {
    mSource = aSource;
    //to set parameters for the inner format I use this two
    boolean compress=false,GRASS=false;
    Format format = new ArcGridFormatFactory().createFormat();
    //getting the parameters
    ParameterValueGroup pg= format.getReadParameters();
    Reader fakeReader=null;

    //I have to set some informations about the file I am going to read
    //Compression
    String pathname = null;
    if (aSource instanceof String) {
      pathname = (new File( (String) aSource)).getName();
    }
    if (aSource instanceof File) {
      pathname = ( (File) aSource).getName();
    }
    if (aSource instanceof URL) {
      URL url = (URL) aSource;
      pathname = url.getFile();
    }



    try{
       fakeReader = mExchange.getGZIPReader(mSource);
       //it is compressed
       compress=true;
     }
     catch(Exception e){
       //if I get here I hope it is not compressed
       compress=false;
     }
     finally{
       fakeReader=null;
     }

    //now I can set the compressed parameters
    pg.parameter("Compressed").setValue(compress);



    //the file can be read
    //GRASS or arcgrid?
    try{

       //compressed?
       if (compress) {
          fakeReader = mExchange.getGZIPReader(mSource);
       }
       else {
         fakeReader = mExchange.getReader(mSource);
       }

       //trying to read the header to see if everything is cool.
      ArcGridRaster acgRaster=new ArcGridRaster(fakeReader,compress);
      acgRaster.parseHeader();

      //if i get here i was able to read the file
      fakeReader=null;
      GRASS=false;
      pg.parameter("GRASS").setValue(GRASS);
      //setting the format
      this.format=format;
      //freeing resources
      format=null;
      acgRaster=null;

    }
    catch(IOException e)
    {
      fakeReader=null;
      try
      {
        //compressed?
        if (compress) {
           fakeReader = mExchange.getGZIPReader(mSource);
        }
        else {
          fakeReader = mExchange.getReader(mSource);
        }

       //if i get here i was able to read the file
       GRASSArcGridRaster gacgRaster=new GRASSArcGridRaster(fakeReader,compress);
       gacgRaster.parseHeader();
       fakeReader=null;
       GRASS=true;
       pg.parameter("GRASS").setValue(GRASS);
       this.format=format;
       //freeing resources
       format=null;
       gacgRaster=null;


      }
      catch(IOException ex)
      {
        //not an argrid format
        format= null;
      }

    }
  }

  /**
   * @see org.opengis.coverage.grid.GridCoverageReader#getMetadataNames()
   */
  public String[] getMetadataNames() throws IOException {
    // Metadata has not been handled at this point ie there is not spec on
    // where it should be obtained
    return null;
  }

  /**
   * @see org.opengis.coverage.grid.GridCoverageReader#getMetadataValue(java.lang.String)
   */
  public String getMetadataValue(String name) throws IOException,
      MetadataNameNotFoundException {
    throw new MetadataNameNotFoundException(name
                                            +
                                            " is not a valid metadata name for ArcGridReader");
  }

  /**
   * @see org.opengis.coverage.grid.GridCoverageReader#listSubNames()
   */
  public String[] listSubNames() throws IOException {
    return null;
  }

  /**
   * @see org.opengis.coverage.grid.GridCoverageReader#getFormat()
   */
  public Format getFormat() {

    return format;
  }

  /**
   * @see org.opengis.coverage.grid.GridCoverageReader#getSource()
   */
  public Object getSource() {
    return mSource;
  }

  /**
   * @see org.opengis.coverage.grid.GridCoverageReader#getCurrentSubname()
   */
  public String getCurrentSubname() throws IOException {
    return null;
  }

  /**
   * Note: The geotools GridCoverage does not implement the geoapi
   * GridCoverage Interface so this method shows an error. All other methods
   * are using the geotools GridCoverage class
   *
   * @see org.opengis.coverage.grid.GridCoverageReader#read(org.opengis.parameter.GeneralParameterValue[])
   */
  public GridCoverage read(GeneralParameterValue[] params) throws
      IllegalArgumentException, IOException {
    if(params!=null)
      setEnvironment("ArcGrid", params);

    return getGridCoverage();
  }

  /**
   * @see org.opengis.coverage.grid.GridCoverageReader#dispose()
   */
  public void dispose() throws IOException {
    if (mReader != null) {
      mReader.close();
    }
  }

  /**
   * @see org.opengis.coverage.grid.GridCoverageReader#getGridCoverageCount()
   */
  public int getGridCoverageCount() {
    return 1;
  }

  /**Sets up the object's environment based on the Parameters passed to it by the client.
   *
   * @param name
   *            A name for the gridCoverage
   * @param parameters
   *            The parameters from a read() method
   *
   * @throws InvalidParameterNameException
   *             Thrown if a parameter was passed to the reader that is not
   *             expected
   * @throws InvalidParameterValueException
   *             Thrown if a boolean value is not valid for the parameter
   *             passed
   * @throws IOException
   *             Thrown for any other unexpected exception
   */
  private void setEnvironment(String name, GeneralParameterValue[] params) throws
      InvalidParameterNameException,
      InvalidParameterValueException, IOException {
    this.name = name;

    for (int i = 0; i < params.length; i++) {
      Parameter param = (Parameter) params[i];

      if (param.getDescriptor().getName().getCode().equals("Compressed")) {
        format.getReadParameters().parameter("Compressed").setValue( param.booleanValue());
      }

      if (param.getDescriptor().getName().getCode().equals("GRASS")) {
         format.getReadParameters().parameter("GRASS").setValue(param.booleanValue());
      }
    }

  }

  /**
   * Returns the ArcGridRaster read by the datasource. Use it only for
   * specific needs, it's not a datasource independent method.
   *
   * @return the ArcGridRaster read by the datasource
   *
   * @throws java.io.IOException
   *             Thrown in the case of an unexpected exception
   */
  public ArcGridRaster openArcGridRaster() throws java.io.IOException {
    if (arcGridRaster == null) {
      try {

        if (format.getReadParameters().parameter("Compressed").booleanValue()) {
          mReader = mExchange.getGZIPReader(mSource);
        }
        else {
          mReader = mExchange.getReader(mSource);
        }

        if (format.getReadParameters().parameter("GRASS").booleanValue()) {
          arcGridRaster = new GRASSArcGridRaster(mReader, format.getReadParameters().parameter("Compressed").booleanValue());
        }
        else {
          arcGridRaster = new ArcGridRaster(mReader, format.getReadParameters().parameter("Compressed").booleanValue());
        }
      }
      catch (Exception e) {
        throw new DataSourceException("Unexpected exception", e);
      }
    }

    return arcGridRaster;
  }

  /**
   * Returns the GridCoverage read by the datasource. Use it if you want to
   * avoid unpacking the getFeatures returned feature collection. Use only for
   * specific needs, it's not a datasource independent method.
   *
   * @return the GridCoverage read by the datasource
   *
   * @throws java.io.IOException
   *             Thrown in the case of an unexpected exception
   */
  private GridCoverage getGridCoverage() throws java.io.IOException {
    if ( (gridCoverage == null) || (gridCoverage.get() == null)) {
      gridCoverage = new java.lang.ref.SoftReference(createCoverage());
    }

    return (GridCoverage) gridCoverage.get();
  }

  private GridCoverage createCoverage() throws java.io.IOException {
    java.awt.image.WritableRaster raster = null;
    raster = openArcGridRaster().readRaster();

    CoordinateReferenceSystem coordinateSystem = getCoordinateSystem();

    org.opengis.spatialschema.geometry.Envelope envelope = new GeneralEnvelope(
        new double[] {this.arcGridRaster.getXlCorner(),
        this.arcGridRaster.getYlCorner()}
        ,
        new double[] {this.arcGridRaster.getXlCorner() +
        this.arcGridRaster.getNCols() * this.arcGridRaster.getCellSize(),
        this.arcGridRaster.getYlCorner() +
        this.arcGridRaster.getNRows() * this.arcGridRaster.getCellSize()});

    try {
      //////////////////////////////////////////////////////////////////////////////////////////////////
      //
      //TODO this is not finished
      //
      //////////////////////////////////////////////////////////////////////////////////////////////////
      ColorModel cm=RasterFactory.createComponentColorModel(
          DataBuffer.TYPE_FLOAT,
          ColorSpace.getInstance(ColorSpace.CS_GRAY),//
          false,
          false,//ignored since
          Transparency.OPAQUE);
      return new GridCoverageImpl(name,
                                  coordinateSystem,
                                  null,
                                  null,
                                  new BufferedImage(cm,raster,false,null),
                                  envelope);
      //////////////////////////////////////////////////////////////////////////////////////////////////
      //
      //TODO this is not finished
      //
      //////////////////////////////////////////////////////////////////////////////////////////////////

    }
    catch (OperationNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (NoSuchElementException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (FactoryException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Gets the coordinate system that will be associated to the GridCoverage.
   * The WGS84 coordinate system is used by default
   *
   * @return the coordinate system for GridCoverage creation
   */
  private CoordinateReferenceSystem getCoordinateSystem() {

    //getting path of the per file
    File outProj = null;
    URL url=null;
    String pathname = null, name = null;
    try{
           //getting name and pathname
	    if (this.mSource instanceof String) {
              url = (new File( (String)this.mSource)).toURL();
              pathname=url.getPath().substring(0,url.getPath().lastIndexOf("/")+1);
	      name = url.getPath().substring(url.getPath().lastIndexOf("/")+1,url.getPath().length());
	    }
	    else
	    if (this.mSource instanceof File  ) {
              url  = ((File)this.mSource).toURL();
              pathname=url.getPath().substring(0,url.getPath().lastIndexOf("/")+1);
              name = url.getPath().substring(url.getPath().lastIndexOf("/")+1,url.getPath().length());
	    }
	    else
	    if (this.mSource instanceof URL) {
	      url = (URL)this.mSource;
              pathname=url.getPath().substring(0,url.getPath().lastIndexOf("/")+1);
	      name = url.getPath().substring(url.getPath().lastIndexOf("/")+1,url.getPath().length());
	    }
	    //build up the name
	    name = pathname +
	        (name.lastIndexOf(".") > 0 ? name.substring(0, name.indexOf(".")) :
	         name) +
	        ".prj";

	    //read the prj info from the file
	    BufferedReader reader= new BufferedReader(new FileReader(name));

	    //reading infos
	    StringBuffer crsBuff=new StringBuffer("");
	    String line=null;

	    while((line=reader.readLine())!=null)
	      crsBuff.append(line);
	    reader.close();
	    //parsing
	    Parser parser= new Parser();
	    this.coordinateSystem=parser.parseCoordinateReferenceSystem(crsBuff.toString());
	}catch(Exception e){
		this.coordinateSystem= null;
	}

    //is it null the we gor for wgs84?
    if(this.coordinateSystem==null)
      this.coordinateSystem= org.geotools.referencing.crs.GeographicCRS.WGS84;


	return this.coordinateSystem;

  }

  /**
   * Gets the bounding box of this datasource using the default speed of this
   * datasource as set by the implementer.
   *
   * @return The bounding box of the datasource or null if unknown and too
   *         expensive for the method to calculate.
   */
  private Envelope getBounds() {
    com.vividsolutions.jts.geom.Envelope env = null;
    double xmin = arcGridRaster.getXlCorner();
    double ymin = arcGridRaster.getYlCorner();
    double xmax = xmin
        + (arcGridRaster.getNCols() * arcGridRaster.getCellSize());
    double ymax = ymin
        + (arcGridRaster.getNRows() * arcGridRaster.getCellSize());
    env = new com.vividsolutions.jts.geom.Envelope(xmin, xmax, ymin, ymax);

    return env;
  }

  /**
   * Gets the default color ramp used to depict the GridCoverage
   *
   * @return the color ramp
   */
  private Color[] getColors() {
    return demColors;
  }

  /**
   * Converts a JTS Envelope into an org.geotools.pt.Envelope
   *
   * @param source
   *            the jts envelope
   *
   * @return the equivalent geotools envelope
   */
  private org.opengis.spatialschema.geometry.Envelope convertEnvelope(
      com.vividsolutions.jts.geom.Envelope source) {
    double[] min = new double[] {
        source.getMinX(), source.getMinY()};
    double[] max = new double[] {
        source.getMaxX(), source.getMaxY()};

    return new GeneralEnvelope(min, max);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.opengis.coverage.grid.GridCoverageReader#hasMoreGridCoverages()
   */
  public boolean hasMoreGridCoverages() throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.opengis.coverage.grid.GridCoverageReader#skip()
   */
  public void skip() throws IOException {
    // TODO Auto-generated method stub

  }
}
