/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.geotools.data.shapefile;

import com.vividsolutions.jts.geom.*;

import org.geotools.data.DataSourceMetaData;
import org.geotools.data.AbstractDataSource;
import org.geotools.data.DataSourceException;
import org.geotools.feature.*;
import org.geotools.feature.AttributeType;
import org.geotools.filter.Filter;
import org.geotools.data.shapefile.dbf.*;
import org.geotools.data.shapefile.shp.*;
import org.geotools.data.Query;
import org.geotools.data.QueryImpl;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Date;

import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;

/**
 * @version $Id: ShapefileDataSource.java,v 1.4 2003/05/19 16:12:56 ianschneider Exp $
 * @author James Macgill, CCG
 * @author Ian Schneider
 */

public class ShapefileDataSource extends AbstractDataSource implements org.geotools.data.DataSource {
  
  private URL shpURL;
  private URL dbfURL;
  private URL shxURL;
  
  private FeatureType schema = null;
  private IDFactory idFactory;
  
  public ShapefileDataSource(URL url) throws java.net.MalformedURLException {
    
    String filename = null;
    if (url == null)
      throw new NullPointerException("Null URL for ShapefileDataSource");
    try {
      filename = java.net.URLDecoder.decode(url.getFile(),"US-ASCII");
    } catch (java.io.UnsupportedEncodingException use) {
      throw new java.net.MalformedURLException(
      "Unable to decode " + url + " cause " + use.getMessage()
      );
    }
    
    String shpext = ".shp";
    String dbfext = ".dbf";
    String shxext = ".shx";
    
    if(filename.endsWith(shpext) || filename.endsWith(dbfext) || filename.endsWith(shxext)) {
      filename = filename.substring(0, filename.length() - 4);
    } else if(filename.endsWith(".SHP") || filename.endsWith(".DBF") || filename.endsWith(".SHX")) {
      filename = filename.substring(0, filename.length() - 4);
      shpext = ".SHP";
      dbfext = ".DBF";
      shxext = ".SHX";
    }
    
    shpURL = new URL(url, filename + shpext);
    dbfURL = new URL(url, filename + dbfext);
    shxURL = new URL(url, filename + shxext);
  }
  
  public IDFactory getIDFactory() {
    if (idFactory == null)
      idFactory = new DefaultIDFactory();
    return idFactory;
  }
  
  public void setIDFactory(IDFactory f) {
    this.idFactory = f;
  }
  
  /** Stops this DatataSource from loading.
   */
  public void abortLoading() {
    // let em suffer...
  }
  
  /** Gets the bounding box of this datasource using the default speed of
   * this datasource as set by the implementer.
   *
   * @return The bounding box of the datasource or null if unknown and too
   * expensive for the method to calculate.
   */
  public Envelope getBbox() throws DataSourceException {
    // This is way quick!!!
    try {
      ByteBuffer buffer = ByteBuffer.allocateDirect(100);
      ReadableByteChannel in = getReadChannel(shpURL);
      in.read(buffer);
      buffer.flip();
      ShapefileHeader header = new ShapefileHeader();
      header.read(buffer, true);
      return new Envelope(header.minX(),header.maxX(),header.minY(),header.maxY() );
    } catch (IOException ioe) {
      // What now? This seems arbitrarily appropriate !
      throw new DataSourceException("Problem getting Bbox",ioe);
    }
  }
  
  
  /**
   * Loads features from the datasource into the passed collection, based
   * on the passed filter.  Note that all data sources must support this
   * method at a minimum.
   *
   * @param collection The collection to put the features into.
   * @param filter An OpenGIS filter; specifies which features to retrieve.
   * @throws DataSourceException For all data source errors.
   */
  public void getFeatures(FeatureCollection collection,final Query query) throws DataSourceException {
    try {
      
      Filter filter = null;
      if (query != null) {
        filter = query.getFilter();
      }
      // Open a channel for our URL
      ReadableByteChannel channel = getReadChannel(shpURL);
      if(channel == null) {
        throw new DataSourceException("Non existent file or problems opening file: " + shpURL);
      }
      ShapefileReader shp = new ShapefileReader(channel);
      
      // Start the DBaseFile, if it exists
      DbaseFileReader dbf = createDbaseReader();
      
      // create a selector set based upon the original fields (not including geometry)
      BitSet selector = new BitSet(dbf.getHeader().getNumFields());
      
      // Create the FeatureType based on the dbf and shapefile
      FeatureType type = getSchema( shp, dbf, query, selector );

      // FeatureMaker is like an iterator
      FeatureMaker features = new FeatureMaker(dbf,shp,type,selector);
      
      // an array to copy features into
      Feature[] array = new Feature[1];
      // read until done
      while (features.hasNext()) {
        array[0] = features.next();
        // short circuit null filter!!!!
        // this wasn't done before
        if (filter == null || filter.contains(array[0]))
          collection.addFeatures(array);
      }
      shp.close();
      if (dbf != null)
        dbf.close();
    }
    catch (java.io.IOException ioe){
      throw new DataSourceException("IO Exception loading data",ioe);
    }
    catch (com.vividsolutions.jts.geom.TopologyException te){
      throw new DataSourceException("Topology Exception loading data", te);
    }
    catch (org.geotools.feature.IllegalFeatureException ife){
      throw new DataSourceException("Illegal Feature Exception loading data",ife);
    }
    catch (org.geotools.data.shapefile.shp.InvalidShapefileException ise){
      throw new DataSourceException("Illegal Feature Exception loading data",ise);
    }
  }
  
  /* Just a hook to allow various entry points and caching of schema
   *
   */
  private FeatureType getSchema(ShapefileReader shp,DbaseFileReader dbf,Query q,BitSet sel)
  throws DataSourceException,IOException,InvalidShapefileException {
    if (schema == null) {
      if (shp == null)
        shp = new ShapefileReader(getReadChannel(shpURL));
      if (dbf == null)
        dbf = createDbaseReader();
      // Create the FeatureType based on the dbf and shapefile
      return getFeatureType( dbf,shp,q,sel );
    }
    
    return schema;
  }
  
  
  /**
   * Retrieves the featureType that features extracted from this datasource
   * will be created with.
   */
  public FeatureType getSchema() throws DataSourceException{
    try {
      return getSchema(null,null,new QueryImpl(),null);
    } catch (InvalidShapefileException e) {
      throw new DataSourceException("Invalid Shapefile",e);
    } catch (IOException e) {
      throw new DataSourceException("IO problem reading shapefile",e);
    }
    
  }
  
  /**
   * Sets the schema that features extrated from this datasource will be
   * created with.  This allows the user to obtain the attributes he wants,
   * by calling getSchema and then creating a new schema using the
   * attributeTypes from the currently used schema.
   * @param schema the new schema to be used to create features.
   */
  public void setSchema(FeatureType schema) throws DataSourceException {
    this.schema = schema;
  }
  
  private DbaseFileReader createDbaseReader() throws IOException {
    if (dbfURL == null) return null;
    ReadableByteChannel channel = getReadChannel(dbfURL);
    if (channel == null) return null;
    return new DbaseFileReader(channel);
  }
  
  private static ReadableByteChannel getReadChannel(URL url) throws IOException {
    if (url.getProtocol().equals("file")) {
      File file = new File(url.getFile());
      if (! file.exists() || !file.canRead())
        throw new IOException("File either doesn't exist or is unreadable : " + file);
      FileInputStream in = new FileInputStream(file);
      return in.getChannel();
    } else {
      InputStream in = url.openConnection().getInputStream();
      return Channels.newChannel(in);
    }
  }
  
  private static WritableByteChannel getWriteChannel(URL url) throws IOException {
    if (url.getProtocol().equals("file")) {
      File f = new File(url.getFile());
      f.delete();
      if (!f.exists() && !f.createNewFile())
        throw new IOException("Cannot create file " + f);
      RandomAccessFile raf = new RandomAccessFile(f,"rw");
      return raf.getChannel();
    } else {
      OutputStream out = url.openConnection().getOutputStream();
      return Channels.newChannel(out);
    }
  }
  
  /* Use the AttributeType[] from the datasource (ds), those specified by the
   * query, and a bitset to record the selection.
   * The key piece of this is that the original types must be in the same
   * order they appear in the dbase header....
   */
  protected AttributeType[] determineAttributeTypes(AttributeType[] ds,Query q,BitSet sel) {
    // all properties, remember to flip all bits!!!!!
    if (q.retrieveAllProperties()) {
      sel.set(0,sel.size() - 1);
      return ds;
    }
    
    // hash all the query properties
    AttributeType[] qat = q.getProperties();
    Map query = new HashMap();
    if (qat != null) {
      for (int i = 0, ii = qat.length; i < ii; i++) {
        query.put(qat[i].getName(), qat[i]); 
      }
    }
    
    // list of props to return
    ArrayList props = new ArrayList();
    // add the geometry
    props.add(ds[0]);
    // start with 1, cause geometry is zero
    for (int i = 1, ii = ds.length; i < ii; i++) {
      // does query contain original ?
      AttributeType t = (AttributeType) query.get(ds[i].getName());
      // yes, remove from query set, add to props, flip bit
      if (t != null) {
        query.remove(ds[i].getName());
        props.add(ds[i]);
        sel.set(i - 1);
      }
      // no, clear bit
      else {
        sel.clear(i - 1); 
      }
    }
    // add the remaining props from the query
    props.addAll(query.values());
    return (AttributeType[]) props.toArray(new AttributeType[props.size()]);
  }
  
  
  private AttributeType[] getAttributeTypes(DbaseFileReader dbf,ShapeType type) {
    AttributeType geometryAttribute = new org.geotools.feature.AttributeTypeDefault(
    "the_geom",
    JTSUtilities.findBestGeometryClass(type)
    );
    // take care of the case where no dbf and query wants all => geometry only
    if (dbf == null) {
      return new AttributeType[] {geometryAttribute};
    }
    DbaseFileHeader header = dbf.getHeader();
    AttributeType[] atts = new AttributeType[header.getNumFields() + 1];
    atts[0] = geometryAttribute;
    
    for (int i = 0, ii = header.getNumFields(); i < ii; i++) {
      Class clazz = void.class;
      switch (header.getFieldType(i)) {
        // L,C,D,N,F
        case 'l': case 'L':
          clazz = Boolean.class;
          break;
        case 'c': case 'C':
          clazz = String.class;
          break;
        case 'd': case 'D':
          clazz = java.util.Date.class;
          break;
        case 'n': case 'N':
          if (header.getFieldDecimalCount(i) > 0)
            clazz = Double.class;
          else
            clazz = Integer.class;
          break;
        case 'f': case 'F':
          clazz = Double.class;
          break;
      }
      atts[i + 1] = new org.geotools.feature.AttributeTypeDefault(header.getFieldName(i), clazz);
      
    }
    return atts;
  }
  
  /** Determine and create a feature type.
   */
  private FeatureType getFeatureType(DbaseFileReader dbf,ShapefileReader shp,Query q,BitSet sel) throws IOException, DataSourceException {
    ShapeType t = shp.getHeader().getShapeType();
    AttributeType[] types = getAttributeTypes(dbf, t);
    if (sel == null) {
      sel = new BitSet(types.length - 1);
      sel.set(0,sel.size() - 1,true);
    }
    types = determineAttributeTypes(types, q, sel);
    
    try{
      return new org.geotools.feature.FeatureTypeFlat(types);
    }
    catch(org.geotools.feature.SchemaException se){
      throw new DataSourceException("Schema Error",se);
    }
  }
  
  
  protected final DataSourceMetaData createMetaData() {
    MetaDataSupport shpMeta = new MetaDataSupport();
    shpMeta.setSupportsGetBbox(true);
    shpMeta.setFastBbox(true);
    shpMeta.setSupportsSetFeatures(true);
    return shpMeta;
    
  }
  
  
  
  /** An iterator-like class to encapsulate the multi-plexing of the dbf and
   * shape file reading. Fields and class are final for optimization.
   *
   * This should be part of the general package for parsing shapefiles.
   */
  final class FeatureMaker {
    
    final BitSet selector;
    final DbaseFileReader dbf;
    final ShapefileReader shp;
    final FeatureFactory factory;
    final Object[] readStash;
    final Object[] writeStash;
    final IDFactory id;
    int cnt = 0;
    
    // if the dbf is null, we create a 1 length object array,
    // otherwise it is dbf.numFields + 1
    // the extra is for geometry!
    public FeatureMaker(DbaseFileReader dbf,ShapefileReader shp,FeatureType type,BitSet selector) {
      this.dbf = dbf;
      this.shp = shp;
      this.factory = new FeatureFactory(type);;
      this.selector = selector;
      // must be same size as header, should change dbasereader in future...
      readStash = new Object[dbf.getHeader().getNumFields()];
      // these go to the factory...
      writeStash = new Object[type.getAttributeTypes().length];
      id = getIDFactory();
    }
    
    public boolean hasNext() throws IOException {
      // ensure that the records are consistent
      int both = (shp.hasNext() ? 1 : 0) + (dbf.hasNext() ? 2 : 0);
      if (both == 3)
        return true;
      else if (both == 0)
        return false;
      throw new IllegalStateException(
      (both == 1 ? "shape" : "dbf") + "file has extra record"
      );
    }
    
    public Feature next() throws IOException, IllegalFeatureException {
      // read the geometry
      writeStash[0] = shp.nextRecord().shape();
      
      // dbf is not null, read the rest of the features
      // System.out.println(current);
      if (dbf != null) {
        dbf.readEntry(readStash);
      }
      
      // the selection routine...
      for (int i = 1, ii = writeStash.length; i < ii; i++) {
        if (selector.get(i - 1))
          writeStash[i] = readStash[i - 1];
      }
      
      // becuase I know that FeatureFlat copies the array,
      // I've chosen to reuse it.
      // This could be changed.
      return factory.create(writeStash,id.getFeatureID(++cnt));
    }
    
  }
  
  
  
  public static interface IDFactory {
    String getFeatureID(int record);
  }
  
  public class DefaultIDFactory implements IDFactory {
    final String file;
    public DefaultIDFactory() {
      String path = ShapefileDataSource.this.shpURL.getPath();
      int dot = path.lastIndexOf('.');
      if (dot < 0) dot = path.length();
      int slash = path.lastIndexOf('/') + 1;
      file = path.substring(slash,dot);
    }
    public String getFeatureID(int record) {
      return file + "." + record;
    }
    
  }
  

  
  

  /**
   * Overwrites the file writing the feature passed as parameters
   * @param collection - the collection to be written
   */
  public void setFeatures(org.geotools.feature.FeatureCollection collection)
  throws DataSourceException {
    
    try {
      // create a good geometry collection
      // this gc will be a collection of either multi-points, multi-polygons, or multi-linestrings
      // polygons will have the rings in the correct order
      GeometryCollection gc = makeShapeGeometryCollection(collection);
      
      // guess shape dimensions
      int shapeDims = 2;
      if(gc.getNumGeometries() > 0)
        shapeDims = JTSUtilities.guessCoorinateDims(gc.getGeometryN(0).getCoordinates());
      
      ShapefileWriter writer = new ShapefileWriter(getWriteChannel(shpURL),getWriteChannel(shxURL));
      
      writer.write(gc, JTSUtilities.getShapeType(gc.getGeometryN(0), shapeDims));
      writeDbf(collection);
    } catch(Exception e) {
      throw new DataSourceException("Something went wrong during shapefile saving", e);
    }
  }
  
  
  /**
   * Write a dbf file with the information from the featureCollection.
   * @param featureCollection column data from collection
   * @param fname name of the dbf file to write to
   */
  private void writeDbf(FeatureCollection featureCollection) throws DbaseFileException,IOException {
    // welcome to the nastiest code in shapefile...
    
    // precondition: all features have the same schema
    // - currently ignoring this precondition
    Feature[] features = featureCollection.getFeatures();
    AttributeType[] types = features[0].getSchema().getAttributeTypes();
    
    // compute how many supported attributes are there.
    // TODO: handle Calendar, BigDecimal and BigInteger as well
    
    // this will track whether the attribute at the given index is supported.
    // later down the line, we check these values, if > 0, supported
    int[] supported = new int[types.length];
    // tracks number supported
    int numAttributes = 0;
    for(int i = 0; i < types.length; i++) {
      Class currType = types[i].getType();
      
      if((currType == String.class) || (currType == Boolean.class) ||
      Number.class.isAssignableFrom(currType) ||
      Date.class.isAssignableFrom(currType))
        supported[i] = ++numAttributes; // mark supported
      else if(Geometry.class.isAssignableFrom(currType)) {
        // do nothing
      } else {
        throw new DbaseFileException(
        "Shapefile: unsupported type found in feature schema : " +
        currType.getName()
        );
      }
    }
    
    // set up the header
    DbaseFileHeader header = new DbaseFileHeader();
    
    for(int i = 0; i < types.length; i++) {
      Class colType = types[i].getType();
      String colName = types[i].getName();
      
      if((colType == Integer.class) || (colType == Short.class) || (colType == Byte.class)) {
        header.addColumn(colName, 'N', 16, 0);
      } else if((colType == Double.class) || (colType == Float.class)) {
        header.addColumn(colName, 'N', 33, 16);
      } else if(Date.class.isAssignableFrom(colType)) {
        header.addColumn(colName, 'D', 8, 0);
      } else if(colType == String.class) {
        int maxlength = findMaxStringLength(featureCollection, i);
        
        if(maxlength > 255) {
          throw new DbaseFileException(
          "Shapefile does not support strings longer than 255 characters");
        }
        
        header.addColumn(colName, 'C', maxlength, 0);
      } else if (Geometry.class.isAssignableFrom(colType)) {
        continue;
      } else {
        throw new DbaseFileException(
        "Unable to write : " + colType.getName());
      }
    }
    header.setNumRecords(features.length);
    
    // write header
    DbaseFileWriter dbf = new DbaseFileWriter(header,getWriteChannel(dbfURL));
    
    // write rows.
    Object[] dbrow = new Object[numAttributes];
    for(int i = 0; i < features.length; i++) {
      Object[] atts = features[i].getAttributes();
      int idx = 0;
      // make data for each column in this feature (row)
      for(int j = 0; j < types.length; j++) {
        // check for supported...
        if (supported[j] > 0)
          dbrow[idx++] = forAttribute(atts[j],types[j].getType());
      }
      dbf.write(dbrow);
    }
    
    dbf.close();
  }
  
  /*
   * Just a place to do marshalling of data.
   */
  private Object forAttribute(final Object o,Class colType) {
    if(colType == Integer.class) {
      return o;
    } else if((colType == Short.class) || (colType == Byte.class)) {
      return new Integer(((Number) o).intValue());
    } else if(colType == Double.class) {
      return o;
    } else if(colType == Float.class) {
      return new Double(((Number) o).doubleValue());
    } else if(colType == String.class) {
      if (o == null)
        return o;
      return o.toString();
    } else if(Date.class.isAssignableFrom(colType)) {
      if(o instanceof Date)
        return o;
    }
    System.out.println("NULL -> " + colType);
    return null;
  }
  
  /**
   *look at all the data in the column of the featurecollection, and find the largest string!
   *@param fc features to look at
   *@param attributeNumber which of the column to test.
   */
  private int findMaxStringLength(FeatureCollection fc, int attributeNumber) {
    Feature[] features = fc.getFeatures();
    
    int maxlen = 0;
    
    for(int i = 0; i < features.length; i++) {
      String s = (String) (features[i].getAttributes())[attributeNumber];
      if (s == null)
        continue;
      int len = s.length();
      
      if(len > maxlen) {
        maxlen = len;
      }
    }
    
    return maxlen;
  }
  
  
  
  
  
  /**
   * return a single geometry collection <Br>
   *  result.GeometryN(i) = the i-th feature in the FeatureCollection<br>
   *   All the geometry types will be the same type (ie. all polygons) - or they will be set to<br>
   *     NULL geometries<br>
   *<br>
   * GeometryN(i) = {Multipoint,Multilinestring, or Multipolygon)<br>
   *
   *@param fc feature collection to make homogeneous
   */
  public GeometryCollection makeShapeGeometryCollection(FeatureCollection fc) throws DataSourceException {
    GeometryCollection result;
    Feature[] features = fc.getFeatures();
    Geometry[] allGeoms = new Geometry[features.length];
    
    final ShapeType type = JTSUtilities.findBestGeometryType(features[0].getDefaultGeometry());
    
    if (type == ShapeType.NULL) {
      throw new DataSourceException(
      "Could not determine shapefile type - data is either all GeometryCollections or empty");
    }
    
    for(int t = 0; t < features.length; t++) {
      Geometry geom;
      geom = features[t].getDefaultGeometry();
      
      if (type == ShapeType.POINT) {
        
        if((geom instanceof Point)) {
          allGeoms[t] = geom;
        } else {
          allGeoms[t] = new MultiPoint(null, new PrecisionModel(), 0);
        }
        
      } else if (type == ShapeType.ARC) {
        
        if((geom instanceof LineString)) {
          LineString[] l = new LineString[1];
          l[0] = (LineString) geom;
          
          allGeoms[t] = new MultiLineString(l, new PrecisionModel(), 0);
        } else if(geom instanceof MultiLineString) {
          allGeoms[t] = geom;
        } else {
          allGeoms[t] = new MultiLineString(null, new PrecisionModel(), 0);
        }
      } else if (type == ShapeType.POLYGON) {
        
        if(geom instanceof Polygon) {
          //good!
          Polygon[] p = new Polygon[1];
          p[0] = (Polygon) geom;
          
          allGeoms[t] = JTSUtilities.makeGoodShapeMultiPolygon(new MultiPolygon(p,
          geom.getPrecisionModel(),geom.getSRID()));
        } else if(geom instanceof MultiPolygon) {
          allGeoms[t] = JTSUtilities.makeGoodShapeMultiPolygon((MultiPolygon) geom);
        } else {
          allGeoms[t] = new MultiPolygon(null, geom.getPrecisionModel(),geom.getSRID());
        }
        
      }  else if (type == ShapeType.MULTIPOINT) {
        
        if((geom instanceof Point)) {
          Point[] p = new Point[1];
          p[0] = (Point) geom;
          
          allGeoms[t] = new MultiPoint(p, geom.getPrecisionModel(),geom.getSRID());
        } else if(geom instanceof MultiPoint) {
          allGeoms[t] = geom;
        } else {
          allGeoms[t] = new MultiPoint(null, geom.getPrecisionModel(),geom.getSRID());
        }
        
        
      }
    } // end big crazy for loop
    
    result = new GeometryCollection(allGeoms, allGeoms[0].getPrecisionModel(),allGeoms[0].getSRID());
    
    return result;
  }
  
  
  // Just a Test
  public static final void main(String[] args) throws Exception {
    File src = new File(args[0]);
    ShapefileDataSource ds = new ShapefileDataSource(src.toURL());
    FeatureCollection features = ds.getFeatures(Filter.NONE);
    Feature[] f = features.getFeatures();
    for (int i = 0, ii = f.length; i < ii; i++) {
      System.out.println(f[i]);
    }
    
    
    
  }
}
