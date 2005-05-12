package org.geotools.filter.function;



import java.util.Collection;


import com.vividsolutions.jts.operation.polygonize.Polygonizer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

//(C) 2005 by David Blasby and The Open Planning Project 
//http://openplans.org
//
//Released under the LGPL
//
//contact: dblasby@openplans.org




//This class is auto-generated.  Think twice before you try to modify it by hand!

// modified by hand - relate() returns an IntersectionMatrix
//  I've changed this so that it returns a String
//   (function prototype change so it returns a String, and called 
//     .toString() on the resulting IntersectionMatrix)

 public class StaticGeometry {



    static public Geometry geomFromWKT(String wkt)
    {
       WKTReader wktreader = new WKTReader();

       try{
           return wktreader.read(wkt);
       }
       catch (Exception e)
       {
          throw new IllegalArgumentException("bad wkt");
       }
    }

     static public boolean geomEquals(Geometry arg0,Geometry arg1)
     {
           Geometry _this = arg0;

           return _this.equals(arg1);
     }

     static public String toString(Geometry arg0)
     {
           Geometry _this = arg0;

           return _this.toString();
     }

     static public boolean contains(Geometry arg0,Geometry arg1)
     {
           Geometry _this = arg0;

           return _this.contains(arg1);
     }

     static public boolean isEmpty(Geometry arg0)
     {
           Geometry _this = arg0;

           return _this.isEmpty();
     }

     static public double getLength(Geometry arg0)
     {
           Geometry _this = arg0;

           return _this.getLength();
     }

     static public boolean intersects(Geometry arg0,Geometry arg1)
     {
           Geometry _this = arg0;

           return _this.intersects(arg1);
     }

     static public boolean isValid(Geometry arg0)
     {
           Geometry _this = arg0;

           return _this.isValid();
     }

     static public String getGeometryType(Geometry arg0)
     {
           Geometry _this = arg0;

           return _this.getGeometryType();
     }

     static public int getSRID(Geometry arg0)
     {
           Geometry _this = arg0;

           return _this.getSRID();
     }

     static public int getNumPoints(Geometry arg0)
     {
           Geometry _this = arg0;

           return _this.getNumPoints();
     }

     static public boolean isSimple(Geometry arg0)
     {
           Geometry _this = arg0;

           return _this.isSimple();
     }

     static public double distance(Geometry arg0,Geometry arg1)
     {
           Geometry _this = arg0;

           return _this.distance(arg1);
     }

     static public boolean isWithinDistance(Geometry arg0,Geometry arg1,double arg2)
     {
           Geometry _this = arg0;

           return _this.isWithinDistance(arg1,arg2);
     }

     static public double getArea(Geometry arg0)
     {
           Geometry _this = arg0;

           return _this.getArea();
     }

     static public Geometry getCentroid(Geometry arg0)
     {
           Geometry _this = arg0;

           return _this.getCentroid();
     }

     static public Geometry getInteriorPoint(Geometry arg0)
     {
           Geometry _this = arg0;

           return _this.getInteriorPoint();
     }

     static public int getDimension(Geometry arg0)
     {
           Geometry _this = arg0;

           return _this.getDimension();
     }

     static public Geometry getBoundary(Geometry arg0)
     {
           Geometry _this = arg0;

           return _this.getBoundary();
     }

     static public int getBoundaryDimension(Geometry arg0)
     {
           Geometry _this = arg0;

           return _this.getBoundaryDimension();
     }

     static public Geometry getEnvelope(Geometry arg0)
     {
           Geometry _this = arg0;

           return _this.getEnvelope();
     }

     static public boolean disjoint(Geometry arg0,Geometry arg1)
     {
           Geometry _this = arg0;

           return _this.disjoint(arg1);
     }

     static public boolean touches(Geometry arg0,Geometry arg1)
     {
           Geometry _this = arg0;

           return _this.touches(arg1);
     }

     static public boolean crosses(Geometry arg0,Geometry arg1)
     {
           Geometry _this = arg0;

           return _this.crosses(arg1);
     }

     static public boolean within(Geometry arg0,Geometry arg1)
     {
           Geometry _this = arg0;

           return _this.within(arg1);
     }

     static public boolean overlaps(Geometry arg0,Geometry arg1)
     {
           Geometry _this = arg0;

           return _this.overlaps(arg1);
     }

     static public boolean relatePattern(Geometry arg0,Geometry arg1,String arg2)
     {
           Geometry _this = arg0;

           return _this.relate(arg1,arg2);
     }

     static public String relate(Geometry arg0,Geometry arg1)
     {
           Geometry _this = arg0;

           return _this.relate(arg1).toString();
     }

     static public String toText(Geometry arg0)
     {
           Geometry _this = arg0;

           return _this.toText();
     }

     static public Geometry buffer_with_segments(Geometry arg0,double arg1,int arg2)
     {
           Geometry _this = arg0;

           return _this.buffer(arg1,arg2);
     }

     static public Geometry buffer(Geometry arg0,double arg1)
     {
           Geometry _this = arg0;

           return _this.buffer(arg1);
     }

     static public Geometry convexHull(Geometry arg0)
     {
           Geometry _this = arg0;

           return _this.convexHull();
     }

     static public Geometry intersection(Geometry arg0,Geometry arg1)
     {
           Geometry _this = arg0;

           return _this.intersection(arg1);
     }

     static public Geometry unionGeom(Geometry arg0,Geometry arg1)
     {
           Geometry _this = arg0;

           return _this.union(arg1);
     }

     static public Geometry difference(Geometry arg0,Geometry arg1)
     {
           Geometry _this = arg0;

           return _this.difference(arg1);
     }

     static public Geometry symDifference(Geometry arg0,Geometry arg1)
     {
           Geometry _this = arg0;

           return _this.symDifference(arg1);
     }

     static public boolean equalsExactTolerance(Geometry arg0,Geometry arg1,double arg2)
     {
           Geometry _this = arg0;

           return _this.equalsExact(arg1,arg2);
     }

     static public boolean equalsExact(Geometry arg0,Geometry arg1)
     {
           Geometry _this = arg0;

           return _this.equalsExact(arg1);
     }

     static public int getNumGeometries(Geometry arg0)
     {
           GeometryCollection _this = (GeometryCollection) arg0;

           return _this.getNumGeometries();
     }

     static public Geometry getGeometryN(Geometry arg0,int arg1)
     {
           GeometryCollection _this = (GeometryCollection) arg0;

           return _this.getGeometryN(arg1);
     }

     static public double getX(Geometry arg0)
     {
           Point _this = (Point) arg0;

           return _this.getX();
     }

     static public double getY(Geometry arg0)
     {
           Point _this = (Point) arg0;

           return _this.getY();
     }

     static public boolean isClosed(Geometry arg0)
     {
           LineString _this = (LineString) arg0;

           return _this.isClosed();
     }

     static public Geometry getPointN(Geometry arg0,int arg1)
     {
           LineString _this = (LineString) arg0;

           return _this.getPointN(arg1);
     }

     static public Geometry getStartPoint(Geometry arg0)
     {
           LineString _this = (LineString) arg0;

           return _this.getStartPoint();
     }

     static public Geometry getEndPoint(Geometry arg0)
     {
           LineString _this = (LineString) arg0;

           return _this.getEndPoint();
     }

     static public boolean isRing(Geometry arg0)
     {
           LineString _this = (LineString) arg0;

           return _this.isRing();
     }

     static public Geometry getExteriorRing(Geometry arg0)
     {
           Polygon _this = (Polygon) arg0;

           return _this.getExteriorRing();
     }

     static public int getNumInteriorRing(Geometry arg0)
     {
           Polygon _this = (Polygon) arg0;

           return _this.getNumInteriorRing();
     }

     static public Geometry getInteriorRingN(Geometry arg0,int arg1)
     {
           Polygon _this = (Polygon) arg0;

           return _this.getInteriorRingN(arg1);
     }
     
     static public String strConcat(String s1,String s2)
     {
     	return s1+s2;
     }
     
     static public boolean strEndsWith(String s1,String s2)
     {
     	return s1.endsWith(s2);
     }
     
     static public boolean strStartsWith(String s1,String s2)
     {
     	return s1.startsWith(s2);
     }
     
     static public boolean strEqualsIgnoreCase(String s1,String s2)
     {
     	return s1.equalsIgnoreCase(s2);
     }
     
     static public int strIndexOf(String s1,String s2)
     {
     	return s1.indexOf(s2);
     }
     
     static public int strLastIndexOf(String s1,String s2)
     {
     	return s1.lastIndexOf(s2);
     }
     
     static public int strLength(String s1)
     {
     	return s1.length();
     }
     
     static public boolean strMatches(String s1,String s2)
     {
     	return s1.matches(s2);
     }
     
     
     
     static public String strSubstring(String s1,int beg, int end)
     {
     	return s1.substring(beg,end);
     }
  
     static public String strSubstringStart(String s1,int beg)
     {
     	return s1.substring(beg);
     }
     
     static public String strTrim(String s1)
     {
     	return s1.trim();
     }
     
     static public boolean strIn2(String s, String s1, String s2)
     {
     	return (s.equals(s1) || s.equals(s2));
     }
     
     static public boolean strIn3(String s, String s1, String s2,String s3)
     {
     	return (s.equals(s1) || s.equals(s2)|| s.equals(s3));
     }
     
     static public boolean strIn4(String s, String s1, String s2,String s3, String s4)
     {
     	return (s.equals(s1) || s.equals(s2)|| s.equals(s3)|| s.equals(s4));
     }
     
     static public boolean strIn5(String s, String s1, String s2,String s3,String s4,String s5)
     {
     	return (s.equals(s1) || s.equals(s2)|| s.equals(s3)|| s.equals(s4)|| s.equals(s5));
     }
     
     static public boolean strIn6(String s, String s1, String s2,String s3, String s4,String s5,String s6)
     {
     	return (s.equals(s1) || s.equals(s2)|| s.equals(s3)|| s.equals(s4)|| s.equals(s5)|| s.equals(s6));
     }
     static public boolean strIn7(String s, String s1, String s2,String s3,String s4,String s5,String s6,String s7)
     {
     	return (s.equals(s1) || s.equals(s2)|| s.equals(s3)|| s.equals(s4)|| s.equals(s5)|| s.equals(s6)|| s.equals(s7));
     }
     
     static public boolean strIn8(String s, String s1, String s2,String s3, String s4,String s5,String s6,String s7,String s8)
     {
     	return (s.equals(s1) || s.equals(s2)|| s.equals(s3)|| s.equals(s4)|| s.equals(s5)|| s.equals(s6)|| s.equals(s7)|| s.equals(s8));
     }
     static public boolean strIn9(String s, String s1, String s2,String s3, String s4,String s5,String s6,String s7,String s8,String s9)
     {
     	return (s.equals(s1) || s.equals(s2)|| s.equals(s3)|| s.equals(s4)|| s.equals(s5)|| s.equals(s6)|| s.equals(s7)|| s.equals(s8)|| s.equals(s9));
     }
     static public boolean strIn10(String s, String s1, String s2,String s3, String s4,String s5,String s6,String s7,String s8,String s9,String s10)
     {
     	return (s.equals(s1) || s.equals(s2)|| s.equals(s3)|| s.equals(s4)|| s.equals(s5)|| s.equals(s6)|| s.equals(s7)|| s.equals(s8)|| s.equals(s9) || s.equals(s10));
     }
     
     
     static public double parseDouble(String s)
     {
     	return Double.parseDouble(s);
     }
     
     static public int parseInt(String s)
     {
     	try{
     		return Integer.parseInt(s);
     	}
     	catch(NumberFormatException e) // be nice for silly people!
		{
     		return (int) Math.round(Double.parseDouble(s));
		}
     }
     
     static public boolean parseBoolean(String s)
     {
     	if (s.equalsIgnoreCase("") || s.equalsIgnoreCase("f") || s.equalsIgnoreCase("false")|| s.equalsIgnoreCase("0")|| s.equalsIgnoreCase("0.0"))
     		return false;
     	return true;
     }
     
     static public int roundDouble(double d)
     {
     	return (int) Math.round(d);
     }
     
     static public double i2d(int i)
     {
     	return (double) i;
     }
     
     static public boolean i2b(int i)
     {
     	return i==0;
     }
     
     static public boolean d2b(double d)
     {
     	return d==0;
     }
     
     static public int if_then_else_int(boolean p, int a, int b)
     {
     	if (p)
     		return a;
     	else
     		return b;
     }
     
     static public double if_then_else_double(boolean p, double a, double b)
     {
     	if (p)
     		return a;
     	else
     		return b;
     }
     
     static public String if_then_else_string(boolean p, String a, String b)
     {
     	if (p)
     		return a;
     	else
     		return b;
     }
     
     static public Geometry if_then_else_geom(boolean p, Geometry a, Geometry b)
     {
     	if (p)
     		return a;
     	else
     		return b;
     }
     
     
     static public boolean equalTo(Object o1,Object o2)
     {
     	 if (o1.getClass() == o2.getClass())
     	 	return o1.equals(o2);
     	 if ( (o1 instanceof Number) && (o2 instanceof Number) )
     	 {
     	 	return ((Number)o1).doubleValue() == ((Number)o2).doubleValue();
     	 }
     	 return (o1).toString() .equals( (o2).toString() );
     }
     
     static public boolean notEqualTo(Object o1,Object o2)
     {
     	 return !(equalTo(o1,o2));
     }
     
     static public boolean lessThan(Object o1,Object o2)
     {
     	if ( (o1 instanceof Integer) && (o2 instanceof Integer) )
    	 {
    	 	return ((Integer)o1).intValue() < ((Integer)o2).intValue();
    	 } 
     	 if ( (o1 instanceof Number) && (o2 instanceof Number) )
     	 {
     	 	return ((Number)o1).doubleValue() < ((Number)o2).doubleValue();
     	 }
     	 return (o1).toString() .compareTo( (o2).toString() ) == 0;
     }
     
     static public boolean greaterThan(Object o1,Object o2)
     {
     	if ( (o1 instanceof Integer) && (o2 instanceof Integer) )
    	 {
    	 	return ((Integer)o1).intValue() > ((Integer)o2).intValue();
    	 } 
     	 if ( (o1 instanceof Number) && (o2 instanceof Number) )
     	 {
     	 	return ((Number)o1).doubleValue() > ((Number)o2).doubleValue();
     	 }
     	 return (o1).toString() .compareTo( (o2).toString() ) == 2;
     }
     
     static public boolean greaterEqualThan(Object o1,Object o2)
     {
     	if ( (o1 instanceof Integer) && (o2 instanceof Integer) )
    	 {
    	 	return ((Integer)o1).intValue() >= ((Integer)o2).intValue();
    	 } 
     	 if ( (o1 instanceof Number) && (o2 instanceof Number) )
     	 {
     	 	return ((Number)o1).doubleValue() >= ((Number)o2).doubleValue();
     	 }
     	 return (
     	 		   ((o1).toString() .compareTo( (o2).toString() ) == 2) ||
				   ((o1).toString() .compareTo( (o2).toString() ) == 1) 
				   );
     }
     
     static public boolean lessEqualThan(Object o1,Object o2)
     {
     	if ( (o1 instanceof Integer) && (o2 instanceof Integer) )
    	 {
    	 	return ((Integer)o1).intValue() <= ((Integer)o2).intValue();
    	 } 
     	 if ( (o1 instanceof Number) && (o2 instanceof Number) )
     	 {
     	 	return ((Number)o1).doubleValue() <= ((Number)o2).doubleValue();
     	 }
     	 return (
     	 		   ((o1).toString() .compareTo( (o2).toString() ) == 0) ||
				   ((o1).toString() .compareTo( (o2).toString() ) == 1) 
				   );
     }
     
     static public boolean isLike(String s1, String s2)
     {
        return s1.matches(s2); // this sucks, but hay...
     }
     
     static public boolean isNull(Object o)
     {
        return o==null;
     }
     
     static public boolean between(Object o, Object o_low, Object o_high)
     {

     	return StaticGeometry.greaterEqualThan(o,o_low) && StaticGeometry.lessEqualThan(o,o_high);
     }
     
     
     static public boolean not(boolean b)
     {
     	return !b;
     }

}
