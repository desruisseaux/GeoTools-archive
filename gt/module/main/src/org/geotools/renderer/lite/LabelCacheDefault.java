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
package org.geotools.renderer.lite;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.media.jai.util.Range;

import org.geotools.feature.Feature;
import org.geotools.renderer.style.SLDStyleFactory;
import org.geotools.renderer.style.TextStyle2D;
import org.geotools.styling.TextSymbolizer;

import com.vividsolutions.jts.geom.Envelope;
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

/**
 * Default LabelCache Implementation
 * 
 * @author jeichar
 * @since 0.9.0
 */
public class LabelCacheDefault implements LabelCache {

    /** Map<label, LabelCacheItem> the label cache */
	Map labelCache=new HashMap();
	private SLDStyleFactory styleFactory=new SLDStyleFactory();
	
	/**
	 * @see org.geotools.renderer.lite.LabelCache#start()
	 */
	public void start() {
	}

	/**
	 * @see org.geotools.renderer.lite.LabelCache#startLayer()
	 */
	public void startLayer() {
	}

	/**
	 * @see org.geotools.renderer.lite.LabelCache#put(org.geotools.renderer.style.TextStyle2D, org.geotools.renderer.lite.LiteShape)
	 */
	public void put(TextSymbolizer symbolizer, Feature feature, LiteShape2 shape, Range scaleRange) {
		TextStyle2D textStyle=(TextStyle2D) styleFactory.createStyle(feature, symbolizer, scaleRange);
    	//equals and hashcode of LabelCacheItem is the hashcode of label and the
    	// equals of the 2 labels so label can be used to find the entry.  
    	if( !labelCache.containsKey(textStyle.getLabel())){
    		labelCache.put(textStyle.getLabel(), new LabelCacheItem(textStyle, shape));
    	}else{
    		LabelCacheItem item=(LabelCacheItem) labelCache.get(textStyle.getLabel());
    		item.getGeoms().add(shape.getGeometry());
    	}
	}

	/**
	 * @see org.geotools.renderer.lite.LabelCache#endLayer(java.awt.Graphics2D, java.awt.Rectangle)
	 */
	public void endLayer(Graphics2D graphics, Rectangle displayArea) {
	}

	/**
	 * @see org.geotools.renderer.lite.LabelCache#end(java.awt.Graphics2D, java.awt.Rectangle)
	 */
	public void end(Graphics2D graphics, Rectangle displayArea) {
		List glyphs=new ArrayList();
    	for (Iterator labelIter = labelCache.keySet().iterator(); labelIter.hasNext();) {
			LabelCacheItem labelItem = (LabelCacheItem) labelCache.get(labelIter.next());

			
			GeometryFactory factory=new GeometryFactory();
			Geometry displayGeom=factory.toGeometry(new Envelope(displayArea.getMinX(), displayArea.getMaxX(),
					displayArea.getMinY(), displayArea.getMaxY()));

			AffineTransform oldTransform = graphics.getTransform();
			AffineTransform tempTransform = new AffineTransform(oldTransform);			

			GlyphVector glyphVector = labelItem.getTextStyle().getTextGlyphVector(graphics);
			
			if( labelItem.getGeometry() instanceof Point )
				paintPointLabel(glyphVector, labelItem, tempTransform, displayGeom);
			
			if( labelItem.getGeometry() instanceof MultiPoint )
				paintPointLabel(glyphVector, labelItem, tempTransform, displayGeom);
			
			if( (labelItem.getGeometry() instanceof LineString )
					&& !(labelItem.getGeometry() instanceof LinearRing))
				paintLineLabel(glyphVector, labelItem, tempTransform, displayGeom);
			
			if( labelItem.getGeometry() instanceof MultiLineString )
				paintMultiLineLabel(glyphVector, labelItem, tempTransform, displayGeom);
			
			if( labelItem.getGeometry() instanceof Polygon ||
					labelItem.getGeometry() instanceof MultiPolygon ||
					labelItem.getGeometry() instanceof LinearRing )
				paintPolygonLabel(glyphVector, labelItem, tempTransform, displayGeom);
			
			if( overlappingItems(glyphVector, tempTransform, glyphs)  )
				continue;
			try {
			    graphics.setTransform(tempTransform);
			    
			    if (labelItem.getTextStyle().getHaloFill() != null) {
			        // float radious = ts2d.getHaloRadius();

			        // graphics.translate(radious, -radious);
			        graphics.setPaint(labelItem.getTextStyle().getHaloFill());
			        graphics.setComposite(labelItem.getTextStyle().getHaloComposite());
			        graphics.fill(labelItem.getTextStyle().getHaloShape(graphics));

			        // graphics.translate(radious, radious);
			    }

			    if (labelItem.getTextStyle().getFill() != null) {
			        graphics.setPaint(labelItem.getTextStyle().getFill());
			        graphics.setComposite(labelItem.getTextStyle().getComposite());
			        graphics.drawGlyphVector(glyphVector, 0, 0);
			        glyphs.add(glyphVector.getPixelBounds(new FontRenderContext(tempTransform, true, false), 0,0));
			    }
			} finally {
			    graphics.setTransform(oldTransform);
			}
    	}
    	labelCache.clear();
    }

	/**
	 * Determines whether labelItems overlaps a previously rendered label.
	 * 
	 * @param glyphVector new label
	 * @param tempTransform 
	 * @param glyphs list of bounds of previously rendered glyphs.
	 * @return true if labelItem overlaps a previously rendered glyph.
	 */
	private boolean overlappingItems(GlyphVector glyphVector, AffineTransform tempTransform, List glyphs) {
		for (Iterator iter = glyphs.iterator(); iter.hasNext();) {
			Rectangle oldBounds = (Rectangle) iter.next();
			if( oldBounds.intersects(glyphVector.getPixelBounds(new FontRenderContext(tempTransform,true,false), 0,0)))
				return true;
		}
		return false;
	}

	private void paintMultiLineLabel(GlyphVector glyphVector, LabelCacheItem labelItem, AffineTransform tempTransform, Geometry displayGeom) {
		
		for (Iterator iter = labelItem.getGeoms().iterator(); iter.hasNext();) {
			GeometryCollection geom = (GeometryCollection) iter.next();
			if( displayGeom.intersects(geom) ){
				Geometry string= geom.intersection(displayGeom);
				paintLineStringLabel(glyphVector, (LineString) string.getGeometryN(0), labelItem.getTextStyle(), tempTransform);				
				break;
			}
		}
	}

	private void paintLineLabel(GlyphVector glyphVector, LabelCacheItem labelItem, AffineTransform tempTransform, Geometry displayGeom) {
		LineString line = (LineString) labelItem.getGeometry();

		TextStyle2D textStyle = labelItem.getTextStyle();

        paintLineStringLabel(glyphVector, line, textStyle, tempTransform);
	}

	private void paintLineStringLabel(GlyphVector glyphVector, LineString line, TextStyle2D textStyle, AffineTransform tempTransform) {
		Point start = line.getStartPoint();
		Point end = line.getEndPoint();
		double dx = end.getX() - start.getX();
		double dy = end.getY() - start.getY();
		double slope=dy/dx;
		double theta=Math.atan(slope);
		double rotation=theta;
		
		
		Rectangle2D textBounds = glyphVector.getVisualBounds();
		Point centroid=line.getCentroid();
		tempTransform.translate(centroid.getX(), centroid.getY());
		double displacementX = 0;
		double displacementY = 0;
		if (textStyle.isAbsoluteLineDisplacement()) {
		    double offset = textStyle.getDisplacementY();

		    if (offset > 0.0) { // to the left of the line
		        displacementY = -offset;
		    } else if (offset < 0) {
		        displacementY = -offset + textBounds.getHeight();
		    } else {
		        displacementY = textBounds.getHeight() / 2;
		    }

		    displacementX = -textBounds.getWidth() / 2;
		} else {
		    displacementX = (textStyle.getAnchorX() * (-textBounds.getWidth()))
		        + textStyle.getDisplacementX();
		    displacementY = (textStyle.getAnchorY() * (textBounds.getHeight()))
		        + textStyle.getDisplacementY();
		}
		tempTransform.rotate(rotation);
		tempTransform.translate(displacementX, displacementY);
	}

	private void paintPointLabel(GlyphVector glyphVector, LabelCacheItem labelItem, AffineTransform tempTransform, Geometry displayGeom) {
		//    	 get the point onto the shape has to be painted
		Point point=(Point) labelItem.getGeometry();
		
		TextStyle2D textStyle = labelItem.getTextStyle();
		Rectangle2D textBounds = glyphVector.getVisualBounds();
		tempTransform.translate(point.getX(), point.getY());
		double displacementX = 0;
		double displacementY = 0;
		if (textStyle.isAbsoluteLineDisplacement()) {
		    double offset = textStyle.getDisplacementY();

		    if (offset > 0.0) { // to the left of the line
		        displacementY = -offset;
		    } else if (offset < 0) {
		        displacementY = -offset + textBounds.getHeight();
		    } else {
		        displacementY = textBounds.getHeight() / 2;
		    }

		    displacementX = -textBounds.getWidth() / 2;
		} else {
		    displacementX = (textStyle.getAnchorX() * (-textBounds.getWidth()))
		        + textStyle.getDisplacementX();
		    displacementY = (textStyle.getAnchorY() * (textBounds.getHeight()))
		        + textStyle.getDisplacementY();
		}
		tempTransform.rotate(textStyle.getRotation());
		tempTransform.translate(displacementX, displacementY);

	}
	
	private void paintPolygonLabel(GlyphVector glyphVector, LabelCacheItem labelItem, AffineTransform tempTransform, Geometry displayGeom) {
		Geometry geom=labelItem.getGeometry();
		Point centroid;
		
      	centroid = geom.getCentroid();
        
		
		TextStyle2D textStyle = labelItem.getTextStyle();
		Rectangle2D textBounds = glyphVector.getVisualBounds();
		tempTransform.translate(centroid.getX(), centroid.getY());
		double displacementX = 0;
		double displacementY = 0;
		if (textStyle.isAbsoluteLineDisplacement()) {
		    double offset = textStyle.getDisplacementY();

		    if (offset > 0.0) { // to the left of the line
		        displacementY = -offset;
		    } else if (offset < 0) {
		        displacementY = -offset + textBounds.getHeight();
		    } else {
		        displacementY = textBounds.getHeight() / 2;
		    }

		    displacementX = -textBounds.getWidth() / 2;
		} else {
		    displacementX = (textStyle.getAnchorX() * (-textBounds.getWidth()))
		        + textStyle.getDisplacementX();
		    displacementY = (textStyle.getAnchorY() * (textBounds.getHeight()))
		        + textStyle.getDisplacementY();
		}
		tempTransform.rotate(textStyle.getRotation());
		tempTransform.translate(displacementX, displacementY);

	}
    

}
