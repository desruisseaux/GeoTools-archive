package org.geotools.geometry.iso.util.quadtree;

import java.awt.geom.Rectangle2D;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * @author roehrig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface QuadtreeCellAttribute {
		public void divide(QuadtreeCell cell, Rectangle2D env);
		public Element getXML(Document document, String name);
		public void setXML(Element element);
    }

