/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package org.geotools.data.postgis.attributeio;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;

import org.geotools.util.LiteCoordinateSequence;
import org.geotools.util.LiteCoordinateSequenceFactory;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.InStream;
import com.vividsolutions.jts.io.WKBWriter;

/**
 * Reads a {@link Geometry}from a byte stream in Well-Known Binary format.
 * Supports use of an {@link InStream}, which allows easy use
 * with arbitary byte stream sources.
 * <p>
 * This class is designed to support reuse of a single instance to read multiple
 * geometries. This class is not thread-safe; each thread should create its own
 * instance.
 *
 * @see WKBWriter
 */
public class WKBReader
{
	  final static  int wkbPoint = 1;
	  final static int wkbLineString = 2;
	  final static int wkbPolygon = 3;
	  final static int wkbMultiPoint = 4;
	  final static int wkbMultiLineString = 5;
	  final static int wkbMultiPolygon = 6;
	  final static int wkbGeometryCollection = 7;

  private static final String INVALID_GEOM_TYPE_MSG  = "Invalid geometry type encountered in ";

  private static GeometryFactory factory= new GeometryFactory(new LiteCoordinateSequenceFactory());
  private static LiteCoordinateSequenceFactory coordFactory = (LiteCoordinateSequenceFactory) factory.getCoordinateSequenceFactory();
  // default dimension - will be set on read
  int index = 0; //index into byte[]
  private byte[] bytes;


  public WKBReader() 
  {
   
  }
  
  final public byte readByte()
  {
	  index++; 
	  return bytes[index-1];
  }
  
  final public int readInt()
  {
	  index += 4;
	   return  (    (int) (bytes[index-4] & 0xff) << 24)
	            | ( (int) (bytes[index-3] & 0xff) << 16)
	            | ( (int) (bytes[index-2] & 0xff) << 8)
	            | (( int) (bytes[index-1] & 0xff) );
  }
	  

  public Geometry read(byte[] bytes) throws IOException
  {
	  this.bytes = bytes;
	  index = 0;
	  return read();
  }

  /**
   * Reads a single {@link Geometry} from a byte array.
   *
   * @param bytes the byte array to read from
   * @return the geometry read
   * @throws IOException if an input exception occurs
   * @throws ParseException if a parse exception occurs
   */
  public Geometry read() throws IOException
  {
	  // determine byte order
	    byte byteOrder = readByte();
	    
	    // default is big endian
	    if (byteOrder == 1)
	    {
	    	throw new IOException("this parser does not handle NDR WKB");
	    }

	    int typeInt =readInt();
	    int geometryType = typeInt & 0xff;
	    boolean hasZ = (typeInt & 0x80000000) != 0;
	    if (hasZ )
	    {
	    	throw new IOException("this parser does not handle 3d WKB");
	    }

	    switch (geometryType) {
	      case wkbPoint :
	        return readPoint();
	      case wkbLineString :
	        return readLineString();
	      case wkbPolygon :
	        return readPolygon();
	      case wkbMultiPoint :
	        return readMultiPoint();
	      case wkbMultiLineString :
	        return readMultiLineString();
	      case wkbMultiPolygon :
	        return readMultiPolygon();
	      case wkbGeometryCollection :
	        return readGeometryCollection();
	    }
	    throw new IOException("Unknown WKB type " + geometryType);
  }

  private Point readPoint() throws IOException
  {
    CoordinateSequence pts = readCoordinateSequence(1);
    return factory.createPoint(pts);
  }

  private LineString readLineString() throws IOException
  {
    int size = readInt();
    CoordinateSequence pts = readCoordinateSequence(size);
    return factory.createLineString(pts);
  }

  private LinearRing readLinearRing() throws IOException
  {
    int size =  readInt();
    CoordinateSequence pts = readCoordinateSequence(size);
    return factory.createLinearRing(pts);
  }

  private Polygon readPolygon() throws IOException
  {
    int numRings =  readInt();
    LinearRing[] holes = null;
    if (numRings > 1)
      holes = new LinearRing[numRings - 1];

    LinearRing shell = readLinearRing();
    for (int i = 0; i < numRings - 1; i++) {
      holes[i] = readLinearRing();
    }
    return factory.createPolygon(shell, holes);
  }

  private MultiPoint readMultiPoint() throws IOException
  {
    int numGeom =  readInt();
    Point[] geoms = new Point[numGeom];
    for (int i = 0; i < numGeom; i++) {
      Geometry g = read();
      if (! (g instanceof Point))
        throw new IOException(INVALID_GEOM_TYPE_MSG + "MultiPoint");
      geoms[i] = (Point) g;
    }
    return factory.createMultiPoint(geoms);
  }

  private MultiLineString readMultiLineString() throws IOException
  {
    int numGeom = readInt();
    LineString[] geoms = new LineString[numGeom];
    for (int i = 0; i < numGeom; i++) {
      Geometry g = read();
      if (! (g instanceof LineString))
        throw new IOException(INVALID_GEOM_TYPE_MSG + "MultiLineString");
      geoms[i] = (LineString) g;
    }
    return factory.createMultiLineString(geoms);
  }

  private MultiPolygon readMultiPolygon() throws IOException
  {
    int numGeom = readInt();
    Polygon[] geoms = new Polygon[numGeom];
    for (int i = 0; i < numGeom; i++) {
      Geometry g = read();
      if (! (g instanceof Polygon))
        throw new IOException(INVALID_GEOM_TYPE_MSG + "MultiPolygon");
      geoms[i] = (Polygon) g;
    }
    return factory.createMultiPolygon(geoms);
  }

  private GeometryCollection readGeometryCollection() throws IOException
  {
    int numGeom = readInt();
    Geometry[] geoms = new Geometry[numGeom];
    for (int i = 0; i < numGeom; i++) {
      geoms[i] = read();
    }
    return factory.createGeometryCollection(geoms);
  }
  
//  /**
//   *  test of the NIO functions.
//   *    NOTE: they're slow, but here' for comparision
//   * @param size
//   * @return
//   * @throws IOException
//   */
//  private final CoordinateSequence readCoordinateSequence3(int size) throws IOException
//  {
//	   // LiteCoordinateSequence seq = (LiteCoordinateSequence)coordFactory.create(size, 2);
//	    ByteBuffer bb = ByteBuffer.wrap(bytes,index,size*2*8);
//	    double[] ord = bb.asDoubleBuffer().array();  // this doesnt work - you have to grab each one directly.
//	    LiteCoordinateSequence seq = (LiteCoordinateSequence)coordFactory.create(0, 2);
//	   return coordFactory.create(ord);
//  }

  static Method bytesToDoublesMethod = null;
  Object[] bytesToDoublesMethodArgs = new Object[5];  // one per WKBReader() for threading issues
  static
  {
	  try{
		  bytesToDoublesMethod = ObjectInputStream.class.getDeclaredMethod("bytesToDoubles",new Class[] {byte[].class,int.class,double[].class,int.class,int.class}  );
		  bytesToDoublesMethod.setAccessible(true);
		  }
	  catch(Exception e)
	  {
		  bytesToDoublesMethod=null;
	  }
  }
  
  static Integer ZERO = new Integer(0);
  
  private final CoordinateSequence readCoordinateSequence(int size) throws IOException
  {
	     // problem?
	  if (bytesToDoublesMethod == null)
		  return readCoordinateSequencePureJava(size);
	  
	  try{
		     double[] ords = new double[size*2];
		     bytesToDoublesMethodArgs[0] = bytes; // can be done once
		     bytesToDoublesMethodArgs[1] = new Integer(index);
		     bytesToDoublesMethodArgs[2] =ords;
		     bytesToDoublesMethodArgs[3] = ZERO; // can be done once
		     bytesToDoublesMethodArgs[4] = new Integer(size*2);
		     bytesToDoublesMethod.invoke(null,bytesToDoublesMethodArgs);
	         index += 8*2*size;
	         return coordFactory.create(ords);
	  }
	  catch(Exception e)
	  {
		  e.printStackTrace();
		  return readCoordinateSequencePureJava(size);
	  }
  }
  
  private final CoordinateSequence readCoordinateSequencePureJava(int size) throws IOException
  {
    LiteCoordinateSequence seq = (LiteCoordinateSequence)coordFactory.create(size, 2);
    double[] ords = seq.getArray();
    

    for (int i = 0; i < size; i++) 
    {
    	long longvalue  = (long) (bytes[index] & 0xff) << 56
        | (long) (bytes[index+1] & 0xff) << 48
        | (long) (bytes[index+2] & 0xff) << 40
        | (long) (bytes[index+3] & 0xff) << 32
        | (long) (bytes[index+4] & 0xff) << 24
        | (long) (bytes[index+5] & 0xff) << 16
        | (long) (bytes[index+6] & 0xff) <<  8
        | (long) (bytes[index+7] & 0xff);
    	index +=8;
    	ords[i*2]= Double.longBitsToDouble(longvalue);
    	longvalue  = (long) (bytes[index] & 0xff) << 56
        | (long) (bytes[index+1] & 0xff) << 48
        | (long) (bytes[index+2] & 0xff) << 40
        | (long) (bytes[index+3] & 0xff) << 32
        | (long) (bytes[index+4] & 0xff) << 24
        | (long) (bytes[index+5] & 0xff) << 16
        | (long) (bytes[index+6] & 0xff) <<  8
        | (long) (bytes[index+7] & 0xff);
    	index +=8;
    	ords[i*2+1]= Double.longBitsToDouble(longvalue);
      }
 
//    ByteArrayOutputStream bos = new ByteArrayOutputStream(10000);
//    ObjectOutputStream oos = new ObjectOutputStream( bos );
//    oos.writeObject(ords);
//    oos.close();
//    byte bs[] = bos.toByteArray();
    return seq; 
  }
  
// public final static  byte[]  header = {-84, -19, 0, 5, 117, 114, 0, 2, 91, 68, 62, -90, -116, 20, -85, 99, 90, 30, 2, 0, 0, 120, 112};
//
//
//  /**
//   *  this is an (unused) experimental version that tried to use the native method readDoubles, but its more difficult that you would think.
//   *  Its slower in testing, but probably if you had very large geometries, this would be faster.
//   * @param size
//   * @return
//   * @throws IOException
//   */
//  private CoordinateSequence readCoordinateSequence2(int size) throws IOException
//  {
//    LiteCoordinateSequence seq = (LiteCoordinateSequence)coordFactory.create(size, 2);
//   
//    
//    byte [] bs = new byte[size*2*8+23+4];
//    System.arraycopy(header,0,bs,0,23);
//    int ndoubles = size*2;
//    bs[23] = (byte) ((ndoubles>>24) & 0xFF);
//    bs[24] = (byte) ((ndoubles>>16) & 0xFF);
//    bs[25] = (byte) ((ndoubles>>8) & 0xFF);
//    bs[26] = (byte) ((ndoubles) & 0xFF);
//    System.arraycopy(bytes,index,bs,27,size*2*8);  //size + doubles
//    ByteArrayInputStream cis = new ByteArrayInputStream(bs);
//   
//    
//    ObjectInputStream bis= new ObjectInputStream(cis);
//    try{
//    	double[] ords = (double[]) bis.readObject();
//    	seq.setArray(ords);
//    }
//    catch(Exception e) // probably a change in how the VM serialized data
//    {
//    	bis.close();
//    	return readCoordinateSequence2(size);
//    }
//    bis.close();
//    index += size*2*8;
//    return seq; 
//  }
//  


}