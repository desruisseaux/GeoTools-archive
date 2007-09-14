/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, GeoTools Project Managment Committee (PMC)
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
 */

package org.geotools.gui.swing.misc.Render;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.Color;
import java.awt.image.BufferedImage;
import org.geotools.data.FeatureSource;
import org.geotools.map.MapLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;

/**
 *
 * @author johann sorel
 */
public class RandomStyleFactory {
    
    
    public static Style createPolygonStyle(){
        Style style = null;
        
        StyleBuilder sb = new StyleBuilder();
        Symbolizer ps = sb.createPolygonSymbolizer(Color.BLUE,2d);
        
        style = sb.createStyle();
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle(ps));
        
        return style;
    }
    
    public static Style createRandomVectorStyle(FeatureSource fs){
        Style style = null;
        
        StyleBuilder sb = new StyleBuilder();
        Symbolizer ps = sb.createPolygonSymbolizer(Color.BLUE,2d);
                
        try {
            FeatureType typ = fs.getSchema();            
            AttributeDescriptor att = typ.getDefaultGeometry();
            AttributeType type = att.getType();
                        
            Class cla = type.getBinding();
            
            if( cla.equals(Polygon.class) || cla.equals(MultiPolygon.class) ){
                ps = sb.createPolygonSymbolizer(new Color(253, 241, 187), new Color(163, 151, 97), 1);
            }else if( cla.equals(LineString.class) || cla.equals(MultiLineString.class) ){
                ps = sb.createLineSymbolizer(Color.BLUE,2d);
            }else if( cla.equals(Point.class) || cla.equals(MultiPoint.class) ){
                ps = sb.createPointSymbolizer();
            }
                        
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        style = sb.createStyle();
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle(ps));
        
        return style;
    }
    
    public static Style createRasterStyle() {
        Style style = null;
        
        StyleBuilder sb = new StyleBuilder();
        RasterSymbolizer raster = sb.createRasterSymbolizer();
        
        style = sb.createStyle(raster);
        return style;
    }
    
    public static BufferedImage createGlyph(MapLayer layer){
        BufferedImage bi = null;
        
        if(layer != null){
            if( layer.getFeatureSource() != null ){
                                
                
                FeatureTypeStyle[] fts = layer.getStyle().getFeatureTypeStyles();
                Class val = layer.getFeatureSource().getSchema().getDefaultGeometry().getType().getBinding();
                                
                
                if ( layer.getFeatureSource().getSchema().getTypeName().equals("GridCoverage")){
                    bi = Glyph.grid(Color.RED,Color.GREEN,Color.BLUE,Color.YELLOW);
                } else if( val.equals(Polygon.class) || val.equals(MultiPolygon.class) ){
                    bi = Glyph.Polygon( fts[0].getRules()[0] );
                }else if( val.equals(MultiLineString.class) || val.equals(LineString.class) ){
                    bi = Glyph.line( fts[0].getRules()[0] );
                }else if( val.equals(Point.class) || val.equals(MultiPoint.class) ){
                    bi = Glyph.point( fts[0].getRules()[0] );
                }else{
                    bi = Glyph.grid(Color.RED,Color.GREEN,Color.BLUE,Color.YELLOW);
                }
            }
        }
        
        return bi;
    }
    
    public static BufferedImage createGlyph(Symbolizer symbol){
        BufferedImage bi = null;
        
        if( symbol != null){
            
                if( symbol instanceof PolygonSymbolizer  ){
                    bi = Glyph.polygon( 
                            SLD.polyColor(((PolygonSymbolizer)symbol)), 
                            SLD.polyFill(((PolygonSymbolizer)symbol)), 
                            SLD.polyWidth(((PolygonSymbolizer)symbol)));
                }else if( symbol instanceof LineSymbolizer ){
                    bi = Glyph.line( 
                            SLD.lineColor(((LineSymbolizer)symbol)), 
                            SLD.lineWidth(((LineSymbolizer)symbol)));
                }else if( symbol instanceof PointSymbolizer ){
                    bi = Glyph.point( 
                            SLD.pointColor(((PointSymbolizer)symbol)), 
                            SLD.pointFill(((PointSymbolizer)symbol)));
                }else{
                    bi = Glyph.grid(Color.RED,Color.GREEN,Color.BLUE,Color.YELLOW);
                }
            
        }
        
        return bi;
    }
    
    
}
