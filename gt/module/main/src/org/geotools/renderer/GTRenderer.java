package org.geotools.renderer;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.Map;

import org.geotools.map.MapContext;

import com.vividsolutions.jts.geom.Envelope;

/*
 * Geotools2 - OpenSource mapping toolkit http://geotools.org (C) 2002, Geotools Project Managment
 * Committee (PMC) This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation;
 * version 2.1 of the License. This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */

/**
 * Typical usage:
 * 
 *          Rectangle paintArea = new Rectangle(width, height);
 *  
 *          renderer = new StreamingRenderer();
 *          renderer.setContext(map);
 *         
 *          RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
 *          renderer.setJava2DHints(hints);
 *         
 *          Map rendererParams = new HashMap();
 *          rendererParams.put("optimizedDataLoadingEnabled",new Boolean(true) );
 *       
 *  
 *          Envelope dataArea = map.getAreaOfInterest();
 *          AffineTransform at = RendererUtilities.worldToScreenTransform(dataArea,   paintArea);
 *         
 * 
 *          renderer.paint(graphic, paintArea, at);      
 *
 *    
 * @author dblasby
 *
 */

public interface GTRenderer
{
	 public void stopRendering();
	 
	 public void addRenderListener(RenderListener listener);
	 public void removeRenderListener(RenderListener listener);
	 
     public void paint( Graphics2D graphics, Rectangle paintArea, Envelope envelope );
     public void paint( Graphics2D graphics, Rectangle paintArea, AffineTransform transform ) ;

     public void setJava2DHints(RenderingHints hints);
     public RenderingHints getJava2DHints();

     public void setRendererHints(Map hints);
     public Map getRendererHints();

     public void setContext( MapContext context );
     public  MapContext getContext(  );
}

