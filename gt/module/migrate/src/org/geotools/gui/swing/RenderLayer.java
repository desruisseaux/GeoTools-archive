/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
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
package org.geotools.gui.swing;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.renderer.lite.LiteRenderer2;

/**
 *  summary sentence.
 * <p>
 * Paragraph ...
 * </p><p>
 * Responsibilities:
 * <ul>
 * <li>
 * <li>
 * </ul>
 * </p><p>
 * Example:<pre><code>
 * RenderLayer x = new RenderLayer( ... );
 * TODO code example
 * </code></pre>
 * </p>
 * @author jeichar
 * @since 0.9.0
 * @source $URL$
 */
public class RenderLayer implements Comparable{

	int start=Integer.MAX_VALUE;
	int end=-1;
	MapContext context=new DefaultMapContext();
	
	BufferedImage normalCache;
	VolatileImage volatileCache;
	
	LiteRenderer2 renderer;
	boolean useVolatileImages=false;
	private Component parent;
	Rectangle currentArea;
	AffineTransform currentZoom;
	private Map layers=new HashMap();
	private boolean forceRepaint=false;
	
	RenderLayer( Component component, boolean cacheFeatures, boolean useVolatileImages){
		this.parent=component;
		renderer=new LiteRenderer2(context);
		renderer.setOptimizedDataLoadingEnabled(true);
		renderer.setMemoryPreloadingEnabled(cacheFeatures);
		this.useVolatileImages=useVolatileImages;
	}
	public void paint(Graphics2D graphics, Rectangle area, AffineTransform zoom) {
		if( normalCache==null && volatileCache==null ){
			if( useVolatileImages ){
				volatileCache=parent.createVolatileImage(parent.getWidth(), parent.getHeight());
			}else{
				normalCache=new BufferedImage(parent.getWidth(), parent.getHeight(),BufferedImage.TYPE_4BYTE_ABGR);
			}
		}
		if( forceRepaint || !zoom.equals( currentZoom ) || !area.equals(currentArea) ){
			Graphics2D g;
			if( useVolatileImages){
				g=volatileCache.createGraphics();
			}else{	
				g=normalCache.createGraphics();
			}
			renderer.paint(g, area, zoom);
			forceRepaint=false;
		}
		currentZoom=zoom;
		currentArea=area;
		
		if( useVolatileImages)
			graphics.drawImage(volatileCache, 0,0, parent);
		else
			graphics.drawImage(normalCache, 0,0, parent);
			
	}
	
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}

	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0) {
		RenderLayer other=(RenderLayer) arg0;
		if( other.start==start )
		return 0;
		
		return start>other.start?1:-1;
	}

	public void addLayer(MapLayer layer, int zorder) {
		forceRepaint=true;
		context.addLayer(layer);
		layers.put(layer,new Integer(zorder));
		if( start>zorder )
			start=zorder;
		
		if( end<zorder)
			end=zorder;
	}
	
	public void display(){
		JFrame frame=new JFrame("frame"){

			/** <code>serialVersionUID</code> field */
			private static final long serialVersionUID = 1L;
			
			public void paint(java.awt.Graphics arg0){
				if( normalCache!=null )
					arg0.drawImage(normalCache, 0,0,null);
				if( volatileCache!=null )
					arg0.drawImage(volatileCache, 0,0,null);
				
			}
		};
		frame.setSize(300,300);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
	}
}
