/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
 * (C) 1998, P�ches et Oc�ans Canada
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.gui.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JFrame;

import org.geotools.data.FeatureSource;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.renderer.lite.LiteRenderer2;
import org.geotools.resources.TestData;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;

import com.vividsolutions.jts.geom.Envelope;

/**
 *  
 * @author jeichar
 */
public class LiteRendererStyledMapPane extends ZoomPane{

	private LiteRenderer2 renderer;
	private MapContext context;
	List cache=new ArrayList();
	VolatileImage image;
	SortedSet bufferedLayers=new TreeSet();
	SortedSet streamedLayers=new TreeSet();
	SortedSet allLayers=new TreeSet();
	
	/**
	 * Construct <code>LiteRendererStyledMapPane</code>.
	 *
	 */
	public LiteRendererStyledMapPane(int type, MapContext context) {
		super(type);
		setBackground(new Color(0,0,0,0));
		renderer=new LiteRenderer2(context);
		renderer.setOptimizedDataLoadingEnabled(true);
		renderer.setMemoryPreloadingEnabled(false);
		this.context=context;
	}
	
	public LiteRendererStyledMapPane( int type ){
		super(type);
		context=new DefaultMapContext();
	}
	
	/**
	 * @see org.geotools.gui.swing.ZoomPane#getArea()
	 */
	public Rectangle2D getArea() {
		try {
			Envelope env=context.getLayerBounds();
			return new Rectangle2D.Double(env.getMinX(), env.getMinY(), env.getWidth(), env.getHeight());
		} catch (IOException e) {
			return new Rectangle2D.Double(0,0,0,0);
		}
	}

	/**
	 * @see org.geotools.gui.swing.ZoomPane#paintComponent(java.awt.Graphics2D)
	 */
	protected void paintComponent(Graphics2D graphics) {
		if( renderer!=null )
			renderer.paint(graphics, getBounds() ,zoom);
		else{
			for (Iterator iter = allLayers.iterator(); iter.hasNext();) {
				RenderLayer layer = (RenderLayer) iter.next();
				layer.paint(graphics,getBounds(),zoom);
			}
		}
	}

	public void add(MapLayer layer, int zorder, boolean bufferFeatures) {
		context.addLayer(layer);
		RenderLayer predecessor=findPredecessor(zorder);
		RenderLayer successor=findSuccessor(zorder);
		if( bufferFeatures ){
//			if( bufferedLayers.contains(predecessor) ){
//				allLayers.remove(predecessor);
//				predecessor.addLayer(layer, zorder);
//				allLayers.add(predecessor);
//				return;
//			}
//			if( bufferedLayers.contains(successor) ){
//				allLayers.remove(successor);
//				predecessor.addLayer(layer, zorder);
//				allLayers.add(successor);
//				return;
//			}
			RenderLayer newLayer=new RenderLayer(this,bufferFeatures,false);
			newLayer.addLayer(layer, zorder);
			bufferedLayers.add(newLayer);
			allLayers.add(newLayer);
			return;
		}else{
			RenderLayer newLayer=new RenderLayer(this,bufferFeatures,false);
			newLayer.addLayer(layer, zorder);
//			newLayer.display();
			streamedLayers.add(newLayer);
			allLayers.add(newLayer);
			return;
		}
	}

	private RenderLayer findSuccessor(int zorder) {
		RenderLayer successor=null;
		for (Iterator iter = allLayers.iterator(); iter.hasNext();) {
			RenderLayer layer = (RenderLayer) iter.next();
			if( layer.start> zorder ){
				successor=layer;
				break;
			}
		}
		return successor;
	}

	private RenderLayer findPredecessor(int zorder) {
		RenderLayer predecessor=null;
		for (Iterator iter = allLayers.iterator(); iter.hasNext();) {
			predecessor= (RenderLayer) iter.next();
			if( predecessor.start > zorder ){
				break;
			}
		}
		return predecessor;

	}
}
