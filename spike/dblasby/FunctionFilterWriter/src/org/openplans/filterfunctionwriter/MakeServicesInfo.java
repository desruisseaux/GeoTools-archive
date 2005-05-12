package org.openplans.filterfunctionwriter;



import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
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

/**
 *  Basic idea:
 * 
 *    1. for each method in the StaticGeometry class (or whatever class you specify - see main() )
 *    2.       make text desciption for it that you can stick in the service meta-inf
 * 
 * @author dblasby
 */
public class MakeServicesInfo
{

	public static void main(String[] args)
	{	
		MakeServicesInfo cg = new MakeServicesInfo();
		
		cg.handleClass(org.geotools.filter.function.StaticGeometry.class);  // parent of all geometry types
	}
	
	  public void handleClass(Class c)
	    {
	    	Method[] methods = c.getDeclaredMethods();
			for (int t=0;t<methods.length;t++)
			{
				try{
					Method method = methods[t];
					PrintStream ps = System.out;
					
					//emitHeader(method,ps);		
						emitCode(method,ps);
					//emitFooter(method,ps);
					
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
	    }
	  
	  
	  
	  public void emitCode(Method m,PrintStream printstream)
		{
			printstream.println("org.geotools.filter.function.FilterFunction_"+m.getName());
		}
		
	
		
		
	

}


