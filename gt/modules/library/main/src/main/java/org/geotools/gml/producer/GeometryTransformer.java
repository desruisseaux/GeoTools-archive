/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
/*
 * GeometryTransformer.java
 *
 * Created on October 24, 2003, 1:08 PM
 */
package org.geotools.gml.producer;

import org.geotools.xml.transform.TransformerBase;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Polygon;


/**
 * DOCUMENT ME!
 *
 * @author Ian Schneider
 * @source $URL$
 */
public class GeometryTransformer extends TransformerBase {
    
    private boolean useDummyZ = false;
    
    private int numDecimals = 4;
    
    public void setUseDummyZ(boolean flag){
        useDummyZ = flag;
    }
   
    public void setNumDecimals(int num) {
    	numDecimals = num;
    }
    
    /**
     * @TODO remove constant from GometryTraslator contructor call
     */
    public org.geotools.xml.transform.Translator createTranslator(
            ContentHandler handler) {
        return new GeometryTranslator(handler, numDecimals, useDummyZ);
    }
    
    public static class GeometryTranslator extends TranslatorSupport {
        protected CoordinateWriter coordWriter = new CoordinateWriter();
        
        public GeometryTranslator(ContentHandler handler) {            
            this(handler,"gml",GMLUtils.GML_URL);
        }
       
        public GeometryTranslator(ContentHandler handler, String prefix, String nsUri ) {
            super(handler,prefix,nsUri);
            coordWriter.setPrefix( prefix );
            coordWriter.setNamespaceUri( nsUri );
        }
        
        public GeometryTranslator(ContentHandler handler, int numDecimals) {
            this(handler,"gml",GMLUtils.GML_URL,numDecimals);
        }

        public GeometryTranslator(ContentHandler handler, String prefix, String nsUri, int numDecimals) {
            this(handler,prefix,nsUri);
            coordWriter = new CoordinateWriter(numDecimals, false);
            coordWriter.setPrefix( prefix );
            coordWriter.setNamespaceUri( nsUri );
        }
        
        public GeometryTranslator(ContentHandler handler, int numDecimals, boolean isDummyZEnabled) {
            this(handler,"gml",GMLUtils.GML_URL,numDecimals,isDummyZEnabled);
        }
        
        public GeometryTranslator(ContentHandler handler, String prefix, String nsUri, int numDecimals, boolean isDummyZEnabled) {
            this(handler,prefix,nsUri);
            coordWriter = new CoordinateWriter(numDecimals, isDummyZEnabled);
            coordWriter.setPrefix( prefix );
            coordWriter.setNamespaceUri( nsUri );
        }
        
        public boolean isDummyZEnabled(){
            return coordWriter.isDummyZEnabled();
        }
        
        public int getNumDecimals(){
            return coordWriter.getNumDecimals();
        }
        
        public void encode(Object o, String srsName)
        throws IllegalArgumentException {
            if (o instanceof Geometry) {
                encode((Geometry) o, srsName);
            } else {
                throw new IllegalArgumentException("Unable to encode " + o);
            }
        }
        
        public void encode(Object o) throws IllegalArgumentException {
            encode(o, null);
        }
        
        public void encode(Envelope bounds) {
            encode(bounds, null);
        }
        
        public void encode(Envelope bounds, String srsName) {
            // DJB: old behavior for null bounds:
            //
            //<gml:Box srsName="http://www.opengis.net/gml/srs/epsg.xml#0">
            //<gml:coordinates decimal="." cs="," ts=" ">0,0 -1,-1</gml:coordinates>
            //</gml:Box>
            //
            // new behavior:
            // <gml:null>unknown</gml:null>
            if(bounds.isNull()) {
            	encodeNullBounds();
                
                return; // we're done!
            }
            String boxName = boxName();
            
            if ((srsName == null) || srsName.equals("")) {
                start(boxName);
            } else {
                AttributesImpl atts = new AttributesImpl();
                atts.addAttribute("", "srsName", "srsName", "", srsName);
                start(boxName, atts);
            }
            
            try {
                Coordinate[] coords = new Coordinate[2];
                coords[0] = new Coordinate(bounds.getMinX(), bounds.getMinY());
                //coords[1] = new Coordinate(bounds.getMinX(), bounds.getMaxY());
                coords[1] = new Coordinate(bounds.getMaxX(), bounds.getMaxY());
                //coords[3] = new Coordinate(bounds.getMaxX(), bounds.getMinY());
                coordWriter.writeCoordinates(coords, contentHandler);
            } catch (SAXException se) {
                throw new RuntimeException(se);
            }
            
            end(boxName);
        }
        
        /**
         * Method to be subclasses in order to allow for gml3 encoding for null enevelope.
         */
        protected void encodeNullBounds() {
        	start("null");
            String text = "unknown";
            try{
                contentHandler.characters(text.toCharArray(), 0, text.length());
            } catch(Exception e) //this shouldnt happen!!
            {
                System.out.println("got exception while writing null boundedby:"+e.getLocalizedMessage());
                e.printStackTrace();
            }
            end("null");
        }
        
        /**
         * Method to be subclassed in order to allow for gml3 encoding of envelopes.
         * @return "Box"
         */
        protected String boxName() {
        	return "Box";
        }
        
        public void encode(Geometry geometry) {
            encode(geometry, null);
        }
        
        public void encode(Geometry geometry, String srsName) {
            String geomName = GMLUtils.getGeometryName(geometry);
            
            if ((srsName == null) || srsName.equals("")) {
                start(geomName);
            } else {
                AttributesImpl atts = new AttributesImpl();
                atts.addAttribute("", "srsName", "srsName", "", srsName);
                start(geomName, atts);
            }
            
            int geometryType = GMLUtils.getGeometryType(geometry);
            
            switch (geometryType) {
                case GMLUtils.POINT:
                case GMLUtils.LINESTRING:
                    
                    try {
                        coordWriter.writeCoordinates(geometry.getCoordinates(),
                                contentHandler);
                    } catch (SAXException s) {
                        throw new RuntimeException(s);
                    }
                    
                    break;
                    
                case GMLUtils.POLYGON:
                    writePolygon((Polygon) geometry);
                    
                    break;
                    
                case GMLUtils.MULTIPOINT:
                case GMLUtils.MULTILINESTRING:
                case GMLUtils.MULTIPOLYGON:
                case GMLUtils.MULTIGEOMETRY:
                    writeMulti((GeometryCollection) geometry,
                            GMLUtils.getMemberName(geometryType));
                    
                    break;
            }
            
            end(geomName);
        }
        
        private void writePolygon(Polygon geometry) {
            String outBound = "outerBoundaryIs";
            String lineRing = "LinearRing";
            String inBound = "innerBoundaryIs";
            start(outBound);
            start(lineRing);
            
            try {
                coordWriter.writeCoordinates(geometry.getExteriorRing()
                .getCoordinates(),
                        contentHandler);
            } catch (SAXException s) {
                throw new RuntimeException(s);
            }
            
            end(lineRing);
            end(outBound);
            
            for (int i = 0, ii = geometry.getNumInteriorRing(); i < ii; i++) {
                start(inBound);
                start(lineRing);
                
                try {
                    coordWriter.writeCoordinates(geometry.getInteriorRingN(i)
                    .getCoordinates(),
                            contentHandler);
                } catch (SAXException s) {
                    throw new RuntimeException(s);
                }
                
                end(lineRing);
                end(inBound);
            }
        }
        
        private void writeMulti(GeometryCollection geometry, String member) {
            for (int i = 0, n = geometry.getNumGeometries(); i < n; i++) {
                start(member);
                
                encode(geometry.getGeometryN(i));
                
                end(member);
            }
        }
    }
}
