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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.media.jai.util.Range;

import org.geotools.feature.Feature;
import org.geotools.renderer.style.SLDStyleFactory;
import org.geotools.renderer.style.TextStyle2D;
import org.geotools.styling.TextSymbolizer;

import com.vividsolutions.jts.geom.Coordinate;
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
import com.vividsolutions.jts.operation.linemerge.LineMerger;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;

/**
 * Default LabelCache Implementation
 * 
 *  DJB (major changes on May 11th, 2005):
 * 1.The old version of the labeler, if given a *set* of points, lines,
 * or polygons justed labels the first item in the set.  The sets are
 * formed when you want to only put a single "Main St" on the map even if
 * you have a bunch of small "Main St" segments.
 * 
 *    I changed this to be much much wiser.
 * 
 *    Basically, the new way looks at the set of geometries that its going
 * to put a label on and find the "best" one that represents it.  That
 * geometry is then labeled (see below for details on where that label is placed).
 *
 * 2. I changed the actual drawing routines;
 * 
  *   1. get the "representative geometry"
  *   2. for points, label as before
  *   3. for lines, find the middle point on the line (old version just averaged start and end points) and centre label on that point (rotated)
  *   4. for polygon, put the label in the middle
 *
 *3.  
 *
 *    ie. for lines, try the label at the 1/3, 1/2, and 2/3 location.  Metric is how close the  label bounding box is to the line.
 *    
 *    ie. for polygons, bisect the polygon (about the centroid) in to North, South, East and West polygons.  Use the location that has the label best inside the polygon.
 *    
 *  After this is done, you can start doing constraint relaxation...
 *  
 *4. TODO: deal with labels going off the edge of the screen (much reduced now). 
 *5. TODO: add a "minimum quality" parameter (ie. if you're labeling a tiny polygon with a 
 *         tiny label, dont bother).  Metrics are descibed in #3.
 *6. TODO: add ability for SLD to tweak parameters (ie. "always label").  
 *
 *  @author jeichar,dblasby
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
			
			//DJB: simplified this.  Just send off to the point,line,or polygon routine
			//    NOTE: labelItem.getGeometry() returns the FIRST geometry, so we're assuming that lines & points arent mixed
			//          If they are, then the FIRST geometry determines how its rendered (which is probably bad since it should be in area,line,point order
			//TOD: as in NOTE above
			
			if ( ( labelItem.getGeometry() instanceof Point ) || ( labelItem.getGeometry() instanceof MultiPoint ) )
				paintPointLabel(glyphVector, labelItem, tempTransform, displayGeom);
			
			
			
			if( ( (labelItem.getGeometry() instanceof LineString )
					&& !(labelItem.getGeometry() instanceof LinearRing))
					 ||( labelItem.getGeometry() instanceof MultiLineString ))
				paintLineLabel(glyphVector, labelItem, tempTransform, displayGeom);
			
			
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
			    //DJB: added this because several people were using
			    //     "font-color" instead of fill
			    //     It legal to have a label w/o fill (which means dont render it)
			    //     This causes people no end of trouble.
			    //     If they dont want to colour it, then they should use a filter
			    //     DEFAULT (no <Fill>) --> BLACK
			    //NOTE: re-reading the spec says this is the correct assumption.
                Paint fill = labelItem.getTextStyle().getFill();
                Composite comp = labelItem.getTextStyle().getComposite();
                if (fill == null)
                {
                	fill = Color.BLACK;
                	comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f); //100% opaque
                }
			    if (fill != null) {
			        graphics.setPaint(fill);
			        graphics.setComposite(comp);
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

	

	private void paintLineLabel(GlyphVector glyphVector, LabelCacheItem labelItem, AffineTransform tempTransform, Geometry displayGeom) 
	{
		LineString line = (LineString) getLineSetRepresentativeLocation(labelItem.getGeoms(), displayGeom);
		
		if (line == null)
			return;

		TextStyle2D textStyle = labelItem.getTextStyle();

        paintLineStringLabel(glyphVector, line, textStyle, tempTransform);
	}

	private void paintLineStringLabel(GlyphVector glyphVector, LineString line, TextStyle2D textStyle, AffineTransform tempTransform) 
	{		
		Point start = line.getStartPoint();
		Point end = line.getEndPoint();
		double dx = end.getX() - start.getX();
		double dy = end.getY() - start.getY();
		double slope=dy/dx;
		double theta=Math.atan(slope);
		double rotation=theta;
		
		
		Rectangle2D textBounds = glyphVector.getVisualBounds();
		Point centroid=middleLine(line,0.5); //DJB: changed from centroid to "middle point" -- see middleLine() dox
		//DJB: this is also where you could do "voting" and looking at other locations on the line to label (ie. 0.33,0.66)
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
		} else { // DJB: this now does "centering"
		    displacementX = (textStyle.getAnchorX() + (-textBounds.getWidth()/2.0))
		        + textStyle.getDisplacementX();
		    displacementY = (textStyle.getAnchorY() + (textBounds.getHeight()/2.0))
		        + textStyle.getDisplacementY();
		}
		tempTransform.rotate(rotation);
		tempTransform.translate(displacementX, displacementY);
	}
    
    
    /**
     *  Simple to paint a point (or set of points)
     *  Just choose the first one and paint it!
     * 
     */
	private void paintPointLabel(GlyphVector glyphVector, LabelCacheItem labelItem, AffineTransform tempTransform, Geometry displayGeom) 
	{
		//    	 get the point onto the shape has to be painted
		Point point=getPointSetRepresentativeLocation(labelItem.getGeoms(), displayGeom);
		if (point == null)
			return;
		
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
		} else {  //DJB: this probably isnt doing what you think its doing - see others
		    displacementX = (textStyle.getAnchorX() * (-textBounds.getWidth()))
		        + textStyle.getDisplacementX();
		    displacementY = (textStyle.getAnchorY() * (textBounds.getHeight()))
		        + textStyle.getDisplacementY();
		}
		tempTransform.rotate(textStyle.getRotation());
		tempTransform.translate(displacementX, displacementY);

	}
	
	private void paintPolygonLabel(GlyphVector glyphVector, LabelCacheItem labelItem, AffineTransform tempTransform, Geometry displayGeom) 
	{
		Polygon geom =  getPolySetRepresentativeLocation(labelItem.getGeoms(),  displayGeom);
		if (geom == null)
			return;
		
	
		Point centroid;
		
		try{
			centroid = geom.getCentroid(); // this where you would do the north/south/west/east stuff
		}
		catch(Exception e)  // generalized polygons causes problems - this tries to hid them.
		{
			try{
				centroid = geom.getExteriorRing().getCentroid();
			}
			catch(Exception ee)
			{
				try {
					centroid = geom.getFactory().createPoint( geom.getCoordinate() );
				}
				catch(Exception eee)
				{
					return; //we're hooped
				}
			}
		}
        
		
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
			
		// DJB: this now does "centering"
			
			
		    displacementX = (textStyle.getAnchorX() + (-textBounds.getWidth()/2.0))
		        + textStyle.getDisplacementX();
		    displacementY = (textStyle.getAnchorY() + (textBounds.getHeight()/2.0))
		        + textStyle.getDisplacementY();
		}
		tempTransform.rotate(textStyle.getRotation());
		tempTransform.translate(displacementX, displacementY);

	}
    
	/**
	 * 
	 *  1. get a list of points from the input geometries that are inside the displayGeom
	 *      NOTE: lines and polygons are reduced to their centroids (you shouldnt really calling this with lines and polys)
	 *  2. choose the most "central" of the points
	 *      METRIC - choose anyone
	 *      TODO: change metric to be "closest to the centoid of the possible points"
	 * 
	 * @param geoms  list of Point or MultiPoint  (any other geometry types are rejected
	 * @param displayGeometry
	 * @return a point or null (if there's nothing to draw)
	 */
	Point getPointSetRepresentativeLocation(List geoms, Geometry displayGeometry)
	{
		ArrayList pts = new ArrayList(); //points that are inside the displayGeometry
		
		Iterator it = geoms.iterator();
		while (it.hasNext())
		{
			Geometry g = (Geometry) it.next();
			if (!(  (g instanceof Point) || (g instanceof MultiPoint)    ))  // handle lines,polys, gc, etc..
				g= g.getCentroid(); //will be point
			if (g instanceof Point)
			{
				if (displayGeometry.intersects(g)) //this is robust!
					pts.add(g);  // possible label location
			}
			else if (g instanceof MultiPoint)
			{
				for (int t=0;t<g.getNumGeometries();t++)
				{
					Point gg = (Point) g.getGeometryN(t);
					if (displayGeometry.intersects(gg))
						pts.add(gg);  // possible label location
				}
			}
		}
		if (pts.size() == 0)
			return null;
		
		// do better metric than this:
		return (Point) pts.get(0);
	}

	
	/**
	 *   1. make a list of all the geoms (not clipped)
	 *       NOTE: reject points, convert polygons to their exterior ring (you shouldnt be calling this function with points and polys)
	 *   2. join the lines together 
	 *   3. clip resulting lines to display geometry
	 *   4. return longest line
	 * 
	 *   NOTE: the joining has multiple solution.  For example, consider a Y (3 lines):
	 *      *      *
	 *       1    2
	 *        *  *
	 *         *
	 *         3
	 *         *
	 *    solutions are:
	 *        1->2  and 3
	 *        1->3  and 2
	 *        2->3  and 1
	 * 
	 *    (see mergeLines() below for detail of the algorithm; its basically a greedy
	 *    algorithm that should form the 'longest' possible route through the linework)
	 * 
	 *  NOTE: we clip after joining because there could be connections "going on" outside the display bbox
	 * 
	 * 
	 * @param geoms
	 * @param displayGeometry  must be poly
	 */
	LineString getLineSetRepresentativeLocation(List geoms, Geometry displayGeometry)
	{
		ArrayList lines = new ArrayList(); //points that are inside the displayGeometry
		
		Iterator it = geoms.iterator();
		   //go through each geometry in the set. 
		   //  if its a polygon or multipolygon, get the boundary (reduce to a line)
		   //  if its a line, add it to "lines"
		   //  if its a multiline, add each component line to "lines"
		while (it.hasNext())
		{
			Geometry g = (Geometry) it.next();
			if (!(  (g instanceof LineString) || (g instanceof MultiLineString) || (g instanceof Polygon) || (g instanceof MultiPolygon)))
				continue;
			
			if ((g instanceof Polygon) || (g instanceof MultiPolygon) )
			{
				g = g.getBoundary(); // line or multiline  m
			    //TODO: boundary included the inside rings, might want to replace this with getExteriorRing()
				if (!(  (g instanceof LineString) || (g instanceof MultiLineString) ))
					continue; //protection
			}
			
			if (g instanceof LineString) 
			{
				lines.add(g);
			}
			else //multiline
			{
				for (int t=0;t<g.getNumGeometries();t++)
				{
					LineString gg = (LineString) g.getGeometryN(t);
					lines.add(gg);
				}
			}
		}
		if (lines.size() ==0)
			return null;
		
		//at this point "lines" now is a list of linestring
		
		//join
		// this algo doesnt always do what you want it to do, but its pretty good		
		Collection merged = this.mergeLines(lines);

		
		//clip to bounding box
		ArrayList clippedLines = new ArrayList();
		it = merged.iterator();
		while (it.hasNext())
		{
			LineString l = (LineString) it.next();
			MultiLineString ll = clipLineString(l,(Polygon) displayGeometry);
			if ((ll != null) && (!(ll.isEmpty())) )
			{
				for (int t=0;t<ll.getNumGeometries();t++)
					clippedLines.add(ll.getGeometryN(t));  // more robust clipper -- see its dox
			}
		}
		
		//clippedLines is a list of LineString, all cliped (hopefully) to the display geometry.  we choose longest one
		if (clippedLines.size() ==0)
			return null;
		double maxLen = -1;
		LineString maxLine = null;
		for (int t=0;t<clippedLines.size();t++)
		{
			LineString cline = (LineString) clippedLines.get(t);
			if (cline.getLength() > maxLen)
			{
				maxLine = cline;
				maxLen = cline.getLength();
			}
		}
		return maxLine;  // longest resulting line
	}
	
	/**
	 * try to be more robust
	 * dont bother returning points
	 * 
	 *  This will try to solve robustness problems, but read code as to what it does.
	 *  It might return the unclipped line if there's a problem!
	 * 
	 * @param line
	 * @param bbox MUST BE A BOUNDING BOX
	 * @return 
	 */
	public MultiLineString clipLineString(LineString line, Polygon bbox)
	{
		Geometry clip = line;
		try{
			clip = EnhancedPrecisionOp.intersection(line,bbox);
		}
		catch (Exception e)
		{
			//TODO: should try to expand the bounding box and re-do the intersection, but line-bounding box 
			//      problems are quite rare.
			clip = line;//just return the unclipped version			
		}
		if (clip instanceof MultiLineString)
			return (MultiLineString) clip;
		if (clip instanceof LineString)
		{
			LineString[] lns = new LineString[1];
			lns[0] =  (LineString) clip;
			return line.getFactory().createMultiLineString(lns);  
		}
		//otherwise we've got a point or line&point or empty
		if (clip  instanceof Point)
			return null;
		if (clip  instanceof MultiPoint)
			return null;
	    
		//its a GC (Line intersection Poly cannot be a polygon/multipoly)
		GeometryCollection gc= (GeometryCollection) clip;
		ArrayList lns = new ArrayList();
		for (int t=0;t<gc.getNumGeometries();t++)
		{
			Geometry g = gc.getGeometryN(t);
			if (g instanceof LineString)
				lns.add(g);
			//dont think multilinestring is possible, but not sure
		}
		
		//convert to multilinestring
		if (lns.size() ==0)
			return null;
		
		return line.getFactory().createMultiLineString((LineString[]) lns.toArray(new LineString[1]) );  
		
	}
	
	/**
	 *  1. make a list of all the polygons clipped to the displayGeometry
	 *     NOTE: reject any points or lines 
	 *  2. choose the largest of the clipped geometries
	 * 
	 * @param geoms
	 * @param displayGeometry
	 * @return
	 */
	Polygon getPolySetRepresentativeLocation(List geoms, Geometry displayGeometry)
	{
		ArrayList polys = new ArrayList(); //points that are inside the displayGeometry
		
		Iterator it = geoms.iterator();
		// go through each  geometry in the input set
		// if its not a polygon or multipolygon ignore it
		// if its a polygon, add it to "polys"
		// if its a multipolgon, add each component to "polys"
		while (it.hasNext())
		{
			Geometry g = (Geometry) it.next();
			if (!(   (g instanceof Polygon) || (g instanceof MultiPolygon)) )
				continue;
				
			if (g instanceof Polygon) 
			{
				polys.add(g);
			}
			else //multipoly
			{
				for (int t=0;t<g.getNumGeometries();t++)
				{
					Polygon gg = (Polygon) g.getGeometryN(t);
					polys.add(gg);
				}
			}
		}
		if (polys.size() ==0)
			return null;
		
		// at this point "polys" is a list of polygons
		
//		clip
		ArrayList clippedPolys = new ArrayList();
		it = polys.iterator();
		while (it.hasNext())
		{
			Polygon p = (Polygon) it.next();
			MultiPolygon pp = clipPolygon(p,(Polygon) displayGeometry);
			if ((pp != null) && (!(pp.isEmpty())) )
			{
				for (int t=0;t<pp.getNumGeometries();t++)
					clippedPolys.add(pp.getGeometryN(t));  //more robust version -- see dox
			}
		}
		//clippedPolys is a list of Polygon, all cliped (hopefully) to the display geometry.  we choose largest one
		if (clippedPolys.size() ==0)
			return null;
		double maxSize = -1;
		Polygon maxPoly = null;
		for (int t=0;t<clippedPolys.size();t++)
		{
			Polygon cpoly = (Polygon) clippedPolys.get(t);
			if (cpoly.getArea() > maxSize)
			{
				maxPoly = cpoly;
				maxSize = cpoly.getArea();
			}
		}
		return maxPoly;
	}
	
	/**
	 *   try to do a more robust way of clipping a polygon to a bounding box.
	 *   This might return the orginal polygon if it cannot clip
	 *  TODO: this is a bit simplistic, there's lots more to do.
	 * 
	 * @param poy
	 * @param bbox
	 * @return
	 */
	public MultiPolygon clipPolygon(Polygon poly, Polygon bbox)
	{
		Geometry clip = poly;
		try{
			clip = EnhancedPrecisionOp.intersection(poly,bbox);
		}
		catch (Exception e)
		{
			//TODO: should try to expand the bounding box and re-do the intersection.
			//TODO: also, try removing the interior rings of the polygon
			
			clip = poly;//just return the unclipped version			
		}
		if (clip instanceof MultiPolygon)
			return (MultiPolygon) clip;
		if (clip instanceof Polygon)
		{
			Polygon[] polys = new Polygon[1];
			polys[0] = (Polygon) clip;
			return poly.getFactory().createMultiPolygon(polys);  
		}
		//otherwise we've got a point or line&point or empty
		if (clip  instanceof Point)
			return null;
		if (clip  instanceof MultiPoint)
			return null;
		if (clip  instanceof LineString)
			return null;
		if (clip  instanceof MultiLineString)
			return null;
	    
		//its a GC 
		GeometryCollection gc= (GeometryCollection) clip;
		ArrayList plys = new ArrayList();
		for (int t=0;t<gc.getNumGeometries();t++)
		{
			Geometry g = gc.getGeometryN(t);
			if (g instanceof Polygon)
				plys.add(g);
			//dont think multiPolygon is possible, but not sure
		}
		
		//convert to multipoly
		if (plys.size() ==0)
			return null;
		
		return poly.getFactory().createMultiPolygon((Polygon[]) plys.toArray(new Polygon[1]) );  		
	}
	
	/**
	 *  calculate the middle of a line.
	 *  The returning point will be x% (0.5 = 50%) along the line and on the line.
	 * 
	 * 
	 * @param l
	 * @param percent 0=start, 0.5=middle, 1.0=end
	 * @return
	 */
	Point middleLine(LineString l,double percent)
	{
		if (percent >= 1.0)
			percent = 0.99; // for precision
		if (percent <=0)
			percent = 0.01; // for precision
			
		double len = l.getLength();
		double dist = percent*len;
		
		double running_sum_dist = 0;
		Coordinate[] pts = l.getCoordinates();
		
	
		    for (int i = 0; i < pts.length - 1; i++) {
		      double segmentLen = pts[i].distance(pts[i + 1]);
		      
		      if ( (running_sum_dist + segmentLen) >= dist)
		      {
		      	 //it is on this segment
		      	 double r = (dist-running_sum_dist)/segmentLen;
		      	 Coordinate c = new Coordinate(    
		      	 			pts[i].x +(pts[i+1].x-pts[i].x) *r,
							pts[i].y +(pts[i+1].y-pts[i].y) *r
								);
		      	 return l.getFactory().createPoint( c );
		      }
		      running_sum_dist += segmentLen;		      
		    }
		
		    return l.getEndPoint(); // precision protection				
	}
	
	/**
	 *  merges a set of lines together into a (usually) smaller set.
	 *  This one's pretty dumb, we use the JTS method (which doesnt merge on degree 3 nodes) and 
	 *  try to construct less lines.
	 * 
	 *  There's multiple solutions, but we do this the easy way.  Usually you will not be given more than 3 lines 
	 *  (especially after jts is finished with).
	 * 
	 *  Find a  line, find a lines that it "connects" to and add it.
	 *  Keep going.
	 * 
	 * DONE: be smarter - use length so the algorithm becomes greedy.
	 * 
	 *   This isnt 100% correct, but usually it does the right thing.
	 * 
	 *  NOTE: this is O(N^2), but N tends to be <10
	 * 
	 * @param lines
	 * @return
	 */
	Collection mergeLines(Collection lines)
	{
		LineMerger lm = new LineMerger();
		lm.add(lines);
		Collection merged = lm.getMergedLineStrings(); //merged lines
		
		if (merged.size() == 0)
		{
			return null; // shouldnt happen
		}
		if (merged.size() == 1) //simple case - no need to continue merging
		{
			return merged; 
		}
		
		
		// key to this algorithm is the sorting by line length!
		
		// basic method:
		// 1. grab the first line in the list of lines to be merged
		// 2. search through the rest of lines (longer ones = first checked) for a line that can be merged
		// 3.  if you find one, great, merge it and do 2 things - a) update the search geometry with the merged geometry and b) delete the other geometry
		//     if not, keep looking
		// 4. go back to step #1, but use the next longest line
		// 5. keep going until you've completely gone through the list and no merging's taken place
		
		ArrayList mylines = new ArrayList( merged );
		
		boolean keep_going = true; 
		while (keep_going)
		{
			keep_going = false; //no news is bad news
			Collections.sort(mylines, new LineLengthComparator () ); //sorted long->short
			for (int t=0;t<mylines.size();t++)  //for each line
			{
				LineString major = (LineString) mylines.get(t); // this is the search geometry (step #1)
				if (major != null)
				{
					for (int i=t+1;i<mylines.size(); i++) //search forward for a joining thing
					{
						LineString minor = (LineString) mylines.get(i); // forward scan
						if (minor != null) //protection because we remove an already match line!
						{
							LineString merge = merge(major,minor); // step 3 (null = not mergeable)
							if (merge != null)
							{
								 //step 3a
								keep_going = true;   
								mylines.set(i,null);
								mylines.set(t,merge);
								major = merge;
							}
						}
					}
				}
			}
			//remove any null items in the list  (see step 3a)

			mylines = (ArrayList) removeNulls(mylines);
			
		}
		
		//return result		
		return removeNulls(mylines);
	
	}
	
	/**
	 * given a list, return a new list thats the same as the first, but has no null values in it.
	 * @param l
	 * @return
	 */
	ArrayList removeNulls(List l)
	{
		ArrayList al = new ArrayList();
		Iterator it = l.iterator();
		while (it.hasNext())
		{
			Object o = it.next();
			if (o != null)
			{
				al.add(o);
			}
		}
		return al;
	}
	
	/**
	 * 	reverse direction of points in a line
	 */
	LineString reverse(LineString l)
	{
		List clist = Arrays.asList(l.getCoordinates() );
		Collections.reverse( clist );
		return l.getFactory().createLineString( (Coordinate[]) clist.toArray(new Coordinate[1] ) );		
	}
	
/**
 *   if possible, merge the two lines together (ie. their start/end points are equal)
 *    returns null if not possible
 * @param major
 * @param minor
 * @return
 */
	LineString merge(LineString major,LineString minor)
	{
		Coordinate major_s = major.getCoordinateN(0);
		Coordinate major_e = major.getCoordinateN(major.getNumPoints()-1);
		Coordinate minor_s = minor.getCoordinateN(0);
		Coordinate minor_e = minor.getCoordinateN(minor.getNumPoints()-1);

		if (major_s.equals2D(minor_s))
		{
			// reverse minor -> major
			return mergeSimple( reverse(minor), major);
			
		}
		else if (major_s.equals2D(minor_e))
		{
		    //minor -> major
			return mergeSimple( minor, major);
		}
		else if (major_e.equals2D(minor_s))
		{
		   //major -> minor
			return mergeSimple(  major, minor);
		}
		else if (major_e.equals2D(minor_e))
		{
		   //major -> reverse(minor)
			return mergeSimple(  major, reverse( minor));
		}
		return null; //no merge
	}
	
	/**
	 * simple linestring merge - l1 points then l2 points
	 */
	private LineString mergeSimple (LineString l1, LineString l2)
	{
		ArrayList clist = new ArrayList( Arrays.asList(l1.getCoordinates() ) );
		clist.addAll( Arrays.asList(l2.getCoordinates() ));
		
		return l1.getFactory().createLineString( (Coordinate[]) clist.toArray(new Coordinate[1] ) );		
	}
	
	/**
	 *  sorts a list of LineStrings by length (long=1st) 
	 * 
	 */
	private class LineLengthComparator implements java.util.Comparator
	{
		public int compare(Object o1, Object o2) //note order - this sort big->small
		{
			return Double.compare( ((LineString)o2 ).getLength() , ((LineString)o1 ).getLength() );
		}
		
	}
}
