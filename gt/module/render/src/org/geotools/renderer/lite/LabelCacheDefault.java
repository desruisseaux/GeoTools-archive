/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc. 
 *    (C) 2005, Geotools PMC
 *    (c) others
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
 *     @author dblasby
 *     @author jessie
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
import java.lang.reflect.Array;
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
import org.geotools.filter.Expression;
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
 *
 * ------------------------------------------------------------------------------------------
 * I've added extra functionality;
 *   a) priority -- if you set the <Priority> in a TextSymbolizer, then you can control the order of labelling
 *        ** see mailing list for more details 
 *   b) <VendorOption name="group">no</VendorOption> --- turns off grouping for this symbolizer
 *   c) <VendorOption name="spaceAround">5</VendorOption> -- do not put labels within 5 pixels of this label.
 * 
 *  @author jeichar,dblasby
 * @since 0.9.0
 * @source $URL$
 */
public class LabelCacheDefault implements LabelCache {

	/**
	 *  labels that arent this good will not be shown
	 */
	public double MIN_GOODNESS_FIT = 0.7;
	
	public double DEFAULT_PRIORITY = 1000.0;
	
    /** Map<label, LabelCacheItem> the label cache */
	protected Map labelCache=new HashMap();
	
	/** non-grouped labels get thrown in here**/
	protected ArrayList labelCacheNonGrouped = new ArrayList();
	
	public boolean DEFAULT_GROUP=false; //what to do if there's no grouping option
	public int DEFAULT_SPACEAROUND = 0;
	
	
	protected SLDStyleFactory styleFactory=new SLDStyleFactory();
	boolean stop=false;
	
	LineLengthComparator lineLengthComparator = new LineLengthComparator ();
	
	public void stop() {
		stop=true;
	}
	/**
	 * @see org.geotools.renderer.lite.LabelCache#start()
	 */
	public void start() {
		stop=false;
	}

	/**
	 * @see org.geotools.renderer.lite.LabelCache#startLayer()
	 */
	public void startLayer() {
	}

	/**
	 *  get the priority from the symbolizer
	 *   its an expression, so it will try to evaluate it:
	 *     1. if its missing --> DEFAULT_PRIORITY
	 *     2. if its a number, return that number
	 *     3. if its not a number, convert to string and try to parse the number; return the number
	 *     4. otherwise, return DEFAULT_PRIORITY
	 * @param symbolizer
	 * @param feature
	 * @return
	 */
    public double getPriority(TextSymbolizer symbolizer,Feature feature)
    {
    	if (symbolizer.getPriority() == null)
    		return DEFAULT_PRIORITY;
    	
    	//evaluate
    	Object o = symbolizer.getPriority().getValue(feature);
    	if (o==null)
    		return DEFAULT_PRIORITY;
    	
    	if (o instanceof Number)
    		return ((Number)o).doubleValue();
    	
    	String oStr = o.toString();
    	
    	try{
    		double d= Double.parseDouble(oStr);
    		return d;
    	}
    	catch (Exception e)
		{
    		return DEFAULT_PRIORITY;
		}    	
    }

	/**
	 * @see org.geotools.renderer.lite.LabelCache#put(org.geotools.renderer.style.TextStyle2D, org.geotools.renderer.lite.LiteShape)
	 */
	public void put(TextSymbolizer symbolizer, Feature feature, LiteShape2 shape, Range scaleRange) 
	{
		try{
			//get label and geometry				
		    Object labelObj = symbolizer.getLabel().getValue(feature);
		    
			if (labelObj == null)
				return;
		    String label = labelObj.toString().trim(); //DJB: remove white space from label
		    if (label.length() ==0)
		    	return; // dont label something with nothing!
		    
		    double priorityValue = getPriority(symbolizer,feature);
		    
		    
		    boolean group = isGrouping(symbolizer);
		    
		    if (!(group))
		    {
		    	TextStyle2D textStyle=(TextStyle2D) styleFactory.createStyle(feature, symbolizer, scaleRange);
				LabelCacheItem item = new LabelCacheItem(textStyle, shape);
				item.setPriority(priorityValue);
				item.setSpaceAround( getSpaceAround(symbolizer) );
				labelCacheNonGrouped.add(item);
		    }
		    else
		    {    /// --------- grouping case ----------------
			    
		    	//equals and hashcode of LabelCacheItem is the hashcode of label and the
		    	// equals of the 2 labels so label can be used to find the entry.
				
				//DJB: this is where the "grouping" of 'same label' features occurs
				LabelCacheItem lci = (LabelCacheItem)  labelCache.get(  label );
				if (lci == null) //nothing in there yet!
				{
					TextStyle2D textStyle=(TextStyle2D) styleFactory.createStyle(feature, symbolizer, scaleRange);
					LabelCacheItem item = new LabelCacheItem(textStyle, shape);
					item.setPriority(priorityValue);
					item.setSpaceAround( getSpaceAround(symbolizer) );
					labelCache.put(label, item );
				}
				else
				{
					//add				
					lci.setPriority (  lci.getPriority() + priorityValue );
					lci.getGeoms().add(shape.getGeometry());
				}					
		    }
		}
		catch(Exception e)  //DJB: protection if there's a problem with the decimation (getGeometry() can be null)
		{
			//do nothing
		}
	}
	
	/**
	 * pull space around from the sybolizer options - defaults to DEFAULT_SPACEAROUND.
	 * 
	 *  <0 means "I can overlap other labels"  be careful with this.
	 * 
	 * @param symbolizer
	 * @return
	 */
	private int getSpaceAround(TextSymbolizer symbolizer) 
	{
		String value = symbolizer.getOption("spaceAround");
		if (value == null)
			return DEFAULT_SPACEAROUND;
		try {
			return Integer.parseInt(value);
		}
		catch (Exception e)
		{
			return DEFAULT_SPACEAROUND;
		}
	}

	/**
	 *   look at the options in the symbolizer for "group".  return its value
	 *   if not present, return "DEFAULT_GROUP"
	 * @param symbolizer
	 * @return
	 */
	private boolean isGrouping(TextSymbolizer symbolizer) 
	{
		String value = symbolizer.getOption("group");
		if (value == null)
			return DEFAULT_GROUP;
		return value.equalsIgnoreCase("yes")||value.equalsIgnoreCase("true")||value.equalsIgnoreCase("1");
	}
	/**
	 * @see org.geotools.renderer.lite.LabelCache#endLayer(java.awt.Graphics2D, java.awt.Rectangle)
	 */
	public void endLayer(Graphics2D graphics, Rectangle displayArea) 
	{
	}

	/**
	 *   return a list with all the values in priority order.  Both grouped and non-grouped
	 * @param labelCache
	 * @return
	 */
	public List orderedLabels()
	{
		Collection c = labelCache.values();
		ArrayList al = new ArrayList(c); // modifiable (ie. sortable)
		
		al.addAll(labelCacheNonGrouped);
		
		Collections.sort(al);
		Collections.reverse(al);
		return al;		
	}
	/**
	 * @see org.geotools.renderer.lite.LabelCache#end(java.awt.Graphics2D, java.awt.Rectangle)
	 */
	public void end(Graphics2D graphics, Rectangle displayArea) 
	{
		List glyphs=new ArrayList();
		
		GeometryFactory factory=new GeometryFactory();
		Geometry displayGeom=factory.toGeometry(new Envelope(displayArea.getMinX(), displayArea.getMaxX(),
				displayArea.getMinY(), displayArea.getMaxY()));
        
		List items = orderedLabels();  // both grouped and non-grouped
    	for (Iterator labelIter = items.iterator(); labelIter.hasNext();) 
    	{
    		if(stop)
    			return;
    		try{
				//LabelCacheItem labelItem = (LabelCacheItem) labelCache.get(labelIter.next());
    			LabelCacheItem labelItem = (LabelCacheItem) labelIter.next();
				GlyphVector glyphVector = labelItem.getTextStyle().getTextGlyphVector(graphics);
				
				//DJB: simplified this.  Just send off to the point,line,or polygon routine
				//    NOTE: labelItem.getGeometry() returns the FIRST geometry, so we're assuming that lines & points arent mixed
				//          If they are, then the FIRST geometry determines how its rendered (which is probably bad since it should be in area,line,point order
				//TOD: as in NOTE above
				Geometry geom = labelItem.getGeometry();
				
				AffineTransform oldTransform = graphics.getTransform();
				AffineTransform tempTransform = new AffineTransform(oldTransform);
				
				Geometry representativeGeom = null;
				
				if ( ( geom instanceof Point ) || ( geom instanceof MultiPoint ) )
					representativeGeom=paintPointLabel(glyphVector, labelItem, tempTransform, displayGeom);
				else if( ( (geom instanceof LineString )
						&& !(geom instanceof LinearRing))
						 ||( geom instanceof MultiLineString ))
					representativeGeom = paintLineLabel(glyphVector, labelItem, tempTransform, displayGeom);
				else if( geom instanceof Polygon ||
						geom instanceof MultiPolygon ||
						geom instanceof LinearRing )
					representativeGeom=paintPolygonLabel(glyphVector, labelItem, tempTransform, displayGeom);
				
				//DJB: this is where overlapping labels are forbidden (first out of the map has priority)


				
				if (offscreen(glyphVector, tempTransform,displayArea ))  // is this offscreen?
					continue;
				
				int space = labelItem.getSpaceAround();
				if (space >=0) // if <0 then its okay to have overlapping items (!!)
				{
					if( overlappingItems(glyphVector, tempTransform, glyphs, space)  )
						continue;
				}
				
				if (goodnessOfFit(glyphVector, tempTransform, representativeGeom ) < MIN_GOODNESS_FIT)
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
				        Rectangle bounds = glyphVector.getPixelBounds(new FontRenderContext(tempTransform, true, false), 0,0);
				        int extraSpace = labelItem.getSpaceAround();
				        if (extraSpace >=0) // if <0 then we dont record (something can overwrite it)
				        {
				        	bounds = new Rectangle(bounds.x-extraSpace,bounds.y-extraSpace,
				        		bounds.width+extraSpace, bounds.height+extraSpace );
				        
				        	glyphs.add(bounds);
				        }
				    }
				} finally {
				    graphics.setTransform(oldTransform);
				}
    		}
    		catch(Exception e) //the decimation can cause problems - we try to minimize it
			{
    			//do nothing
			}
    	}
    	labelCache.clear();
    }

	/**
	 *   how well does the label "fit" with the geometry.
	 *     1. points
	 *               ALWAYS RETURNS 1.0
	 *     2. lines
	 *              ALWAYS RETURNS 1.0 (modify polygon method to handle rotated labels)
	 *     3. polygon
	 *                + assume: polylabels are unrotated
	 *                + assume: polygon could be invalid
	 *                +         dont worry about holes
	 *             
	 *           like to RETURN area of intersection between polygon and label bounds, but thats expensive
	 *            and likely to give us problems due to invalid polygons
	 *          SO, use a sample method - make a few points inside the label and see if they're "close to" the polygon
	 *          The method sucks, but works well...
	 *           
	 * @param glyphVector
	 * @param tempTransform
	 * @param representativeGeom
	 * @return
	 */
	private double goodnessOfFit(GlyphVector glyphVector, AffineTransform tempTransform, Geometry representativeGeom) 
	{
		if (representativeGeom instanceof Point)
		{
			return 1.0;
		}
		if (representativeGeom instanceof LineString)
		{
			return 1.0;
		}
		if (representativeGeom instanceof Polygon)
		{
			Rectangle glyphBounds = glyphVector.getPixelBounds(new FontRenderContext(tempTransform,true,false), 0,0);
			try {
				Polygon p = simplifyPoly( (Polygon)representativeGeom );
				int count =0;
				int n=10;
				double mindistance = (glyphBounds.height);
				for (int t=1;t<(n+1);t++ )
				{
					Coordinate c = new Coordinate(glyphBounds.x + ((double)glyphBounds.width) *  ( ((double)t)/(n+1) ),
							    glyphBounds.getCenterY() );
					Point pp = new Point(c,representativeGeom.getPrecisionModel(),representativeGeom.getSRID());
					if (
							 p.distance(pp) < mindistance  )
						
					{
						count ++;
					}
				}
				return ((double) count)/n;
			}
			catch(Exception e)
			{				
				representativeGeom.geometryChanged(); //djb -- jessie should do this during generalization
				Envelope ePoly = representativeGeom.getEnvelopeInternal();
				Envelope eglyph= new Envelope(glyphBounds.x,glyphBounds.x+glyphBounds.width,glyphBounds.y,glyphBounds.y+ glyphBounds.height);
				Envelope inter = intersection(ePoly,eglyph);
				if (inter!=null)
					return (inter.getWidth()*inter.getHeight())/ (eglyph.getWidth()*eglyph.getHeight() );
				return 0.0;
			}
		}
		return 0.0;
	}
	
	/**
	 * Remove holes from a polygon
	 * @param polygon
	 * @return
	 */
	private Polygon simplifyPoly(Polygon polygon) 
	{
		LineString outer = polygon.getExteriorRing();
		if (outer.getStartPoint().distance( outer.getEndPoint()) != 0)
		{
			List clist =  new ArrayList (Arrays.asList(outer.getCoordinates() ));
			clist.add(outer.getStartPoint().getCoordinate());
			outer =  outer.getFactory().createLinearRing( (Coordinate[]) clist.toArray(new Coordinate[clist.size()] ) );				
		}
		LinearRing r = (LinearRing) outer;
		
		return outer.getFactory().createPolygon(r,null);
	}
	/**
	 *  Returns true if any part of the label is offscreen (even by a tinny bit)
	 * @param glyphVector
	 * @param tempTransform
	 * @return
	 */
	private boolean offscreen(GlyphVector glyphVector, AffineTransform tempTransform,Rectangle screen) 
	{
		Rectangle glyphBounds = glyphVector.getPixelBounds(new FontRenderContext(tempTransform,true,false), 0,0);
	    return !(screen.contains(glyphBounds));
	}
	/**
	 * Determines whether labelItems overlaps a previously rendered label.
	 * 
	 * @param glyphVector new label
	 * @param tempTransform 
	 * @param glyphs list of bounds of previously rendered glyphs.
	 * @return true if labelItem overlaps a previously rendered glyph.
	 */
	private boolean overlappingItems(GlyphVector glyphVector, AffineTransform tempTransform, List glyphs,int extraSpace) 
	{
		Rectangle glyphBounds = glyphVector.getPixelBounds(new FontRenderContext(tempTransform,true,false), 0,0);
		glyphBounds = new Rectangle(glyphBounds.x-extraSpace,glyphBounds.y-extraSpace,
				                    glyphBounds.width+extraSpace, glyphBounds.height+extraSpace );
		for (Iterator iter = glyphs.iterator(); iter.hasNext();) 
		{
			Rectangle oldBounds = (Rectangle) iter.next();
			if( oldBounds.intersects(glyphBounds) )
				return true;
		}
		return false;
	}

	

	private Geometry paintLineLabel(GlyphVector glyphVector, LabelCacheItem labelItem, AffineTransform tempTransform, Geometry displayGeom) 
	{
		LineString line = (LineString) getLineSetRepresentativeLocation(labelItem.getGeoms(), displayGeom);
		
		if (line == null)
			return null;

		TextStyle2D textStyle = labelItem.getTextStyle();

        paintLineStringLabel(glyphVector, line, textStyle, tempTransform);
        return line;
	}

	/**
	 *  This handles point and line placement.
	 * 
	 * 1. lineplacement --  calculate a rotation and location (and does the perp offset)
	 * 2. pointplacement -- reduce line to a point and ignore the calculated rotation
	 * 
	 * @param glyphVector
	 * @param line
	 * @param textStyle
	 * @param tempTransform
	 */
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
		
		// DJB: this now does "centering"
		//    displacementX = (textStyle.getAnchorX() + (-textBounds.getWidth()/2.0))
		//        + textStyle.getDisplacementX();
		//    displacementY = (textStyle.getAnchorY() + (textBounds.getHeight()/2.0))
		//        - textStyle.getDisplacementY();
		
		double anchorX = textStyle.getAnchorX();
		double anchorY = textStyle.getAnchorY();
		
		//	   undo the above if its point placement!
		if (textStyle.isPointPlacement())
		{
			rotation = textStyle.getRotation(); // use the one the user supplied!
		}
		else  //lineplacement
		{
			displacementY -= textStyle.getPerpendicularOffset(); // move it off the line
			anchorX = 0.5;  // centered
			anchorY = 0.5;   // centered, sitting on line
		}
		
		 displacementX = ( anchorX* (-textBounds.getWidth()))
	        + textStyle.getDisplacementX();
	     displacementY = (anchorY * (textBounds.getHeight()))
	        - textStyle.getDisplacementY();
	    
          
			
		
		if (rotation != rotation) // IEEE def'n x=x for all x except when x is NaN
			rotation = 0.0;
		if (Double.isInfinite(rotation))
			rotation = 0; //weird number
		tempTransform.rotate(rotation);
		tempTransform.translate(displacementX, displacementY);
	}
    
    
    /**
     *  Simple to paint a point (or set of points)
     *  Just choose the first one and paint it!
     * 
     */
	private Geometry paintPointLabel(GlyphVector glyphVector, LabelCacheItem labelItem, AffineTransform tempTransform, Geometry displayGeom) 
	{
		//    	 get the point onto the shape has to be painted
		Point point=getPointSetRepresentativeLocation(labelItem.getGeoms(), displayGeom);
		if (point == null)
			return null;
		
		TextStyle2D textStyle = labelItem.getTextStyle();
		Rectangle2D textBounds = glyphVector.getVisualBounds();
		tempTransform.translate(point.getX(), point.getY());
		double displacementX = 0;
		double displacementY = 0;
		
             //DJB: this probably isnt doing what you think its doing - see others
		    displacementX = (textStyle.getAnchorX() * (-textBounds.getWidth()))
		        + textStyle.getDisplacementX();
		    displacementY = (textStyle.getAnchorY() * (textBounds.getHeight()))
		        - textStyle.getDisplacementY();
		
		 if (!textStyle.isPointPlacement())
	        {
	        	//lineplacement.   We're cheating here, since we cannot line label a point
	        	displacementY -= textStyle.getPerpendicularOffset(); // just move it up (yes, its cheating)
	        }
	        
			double rotation = textStyle.getRotation();
			if (rotation != rotation) // IEEE def'n x=x for all x except when x is NaN
				rotation = 0.0;
			if (Double.isInfinite(rotation))
				rotation = 0; //weird number
			
			tempTransform.rotate(rotation);
		    tempTransform.translate(displacementX, displacementY);
		return point;
	}
	
	/**
	 *  returns the representative geometry (for further processing)
	 * 
	 *  TODO: handle lineplacement for a polygon (perhaps we're supposed to grab the outside line and label it, but spec is unclear)
	 */
	private Geometry paintPolygonLabel(GlyphVector glyphVector, LabelCacheItem labelItem, AffineTransform tempTransform, Geometry displayGeom) 
	{
		Polygon geom =  getPolySetRepresentativeLocation(labelItem.getGeoms(),  displayGeom);
		if (geom == null)
			return null;
		
	
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
					return null; //we're hooped
				}
			}
		}
        
		
		TextStyle2D textStyle = labelItem.getTextStyle();
		Rectangle2D textBounds = glyphVector.getVisualBounds();
		tempTransform.translate(centroid.getX(), centroid.getY());
		double displacementX = 0;
		double displacementY = 0;
		

		
			
		// DJB: this now does "centering"
		displacementX = (textStyle.getAnchorX() * (-textBounds.getWidth()))
                       + textStyle.getDisplacementX();
        displacementY = (textStyle.getAnchorY() * (textBounds.getHeight()))
                       - textStyle.getDisplacementY();
        
        if (!textStyle.isPointPlacement())
        {
        	//lineplacement.   We're cheating here, since we've reduced the polygon to a point, when we should be trying to do something
        	//                 a little smarter (like find its median axis!)
        	displacementY -= textStyle.getPerpendicularOffset(); // just move it up (yes, its cheating)
        }
        
		double rotation = textStyle.getRotation();
		if (rotation != rotation) // IEEE def'n x=x for all x except when x is NaN
			rotation = 0.0;
		if (Double.isInfinite(rotation))
			rotation = 0; //weird number
		
		tempTransform.rotate(rotation);
		tempTransform.translate(displacementX, displacementY);
        return geom;        
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
			else if (g instanceof LineString) 
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
		Envelope displayGeomEnv = displayGeometry.getEnvelopeInternal();
		while (it.hasNext())
		{
			LineString l = (LineString) it.next();
			MultiLineString ll = clipLineString(l,(Polygon) displayGeometry,displayGeomEnv);
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
	public MultiLineString clipLineString(LineString line, Polygon bbox,Envelope displayGeomEnv)
	{		
		Geometry clip = line;
		line.geometryChanged();//djb -- jessie should do this during generalization
		if (displayGeomEnv.contains(line.getEnvelopeInternal()))
		{
			//shortcut -- entirely inside the display rectangle -- no clipping required!
			LineString[] lns = new LineString[1];
			lns[0] =  (LineString) clip;
			return line.getFactory().createMultiLineString(lns); 
		}
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
		Envelope displayGeomEnv = displayGeometry.getEnvelopeInternal();
		while (it.hasNext())
		{
			Polygon p = (Polygon) it.next();
			MultiPolygon pp = clipPolygon(p,(Polygon) displayGeometry,displayGeomEnv);
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
	public MultiPolygon clipPolygon(Polygon poly, Polygon bbox,Envelope displayGeomEnv)
	{
		
		Geometry clip = poly;
		poly.geometryChanged();//djb -- jessie should do this during generalization
		if (displayGeomEnv.contains(poly.getEnvelopeInternal()))
		{
			//shortcut -- entirely inside the display rectangle -- no clipping required!
			Polygon[] polys = new Polygon[1];
			polys[0] = (Polygon) clip;
			return poly.getFactory().createMultiPolygon(polys);  
		}
		

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
	//	Collection merged = lines;
		
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
			Collections.sort(mylines, lineLengthComparator ); //sorted long->short
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
	
	private Envelope intersection(Envelope e1,Envelope e2)
	{
			double tx1 = e1.getMinX();
			double ty1 = e1.getMinY();
			double rx1 = e2.getMinX();
			double ry1 = e2.getMinY();
			double tx2 = tx1; tx2 += e1.getWidth();
			double ty2 = ty1; ty2 += e1.getHeight();
			double rx2 = rx1; rx2 += e2.getWidth();
			double ry2 = ry1; ry2 += e2.getHeight();
			if (tx1 < rx1) tx1 = rx1;
			if (ty1 < ry1) ty1 = ry1;
			if (tx2 > rx2) tx2 = rx2;
			if (ty2 > ry2) ty2 = ry2;
			tx2 -= tx1;
			ty2 -= ty1;
			if ((tx2<0) || (ty2<0))
				return null;
			return new Envelope(tx1,tx1+ tx2, ty1,  ty1+ ty2);
		    
	}
	
}
