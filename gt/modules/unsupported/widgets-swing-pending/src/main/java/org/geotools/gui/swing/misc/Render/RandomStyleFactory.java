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

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.geotools.data.FeatureSource;
import org.geotools.legend.Glyph;
import org.geotools.map.MapLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.SLD;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 *
 * @author johann sorel
 */
public class RandomStyleFactory {
    
    private final String[] POINT_SHAPES = {"square","circle","triangle","star","cross","x"};
    private final int[] SIZES = {8,10,12,14,16,18};
    private final int[] WIDTHS = {1,2};
    private final Color[] COLORS = {
        Color.BLACK,Color.BLUE,Color.CYAN,Color.DARK_GRAY,
        Color.GRAY,Color.GREEN.darker(),Color.LIGHT_GRAY,
        Color.ORANGE,Color.RED,Color.YELLOW.darker()};
    
    
    public Style createPolygonStyle(){
        Style style = null;
        
        StyleBuilder sb = new StyleBuilder();
        Symbolizer ps = sb.createPolygonSymbolizer(randomColor(),randomWidth());
        
        style = sb.createStyle();
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle(ps));
        
        return style;
    }
    
    public Style createRandomVectorStyle(FeatureSource fs){
        Style style = null;
        
        StyleBuilder sb = new StyleBuilder();
        Symbolizer ps = sb.createPolygonSymbolizer(randomColor(),randomWidth());
                
        try {
            FeatureType typ = fs.getSchema();            
            AttributeDescriptor att = typ.getDefaultGeometry();
            AttributeType type = att.getType();
                        
            Class cla = type.getBinding();
            
            if( cla.equals(Polygon.class) || cla.equals(MultiPolygon.class) ){
                ps = sb.createPolygonSymbolizer(randomColor(), randomColor(), 1);
            }else if( cla.equals(LineString.class) || cla.equals(MultiLineString.class) ){
                ps = sb.createLineSymbolizer(randomColor(),randomWidth());
            }else if( cla.equals(Point.class) || cla.equals(MultiPoint.class) ){
                Fill fill = sb.createFill(randomColor(), 1);
                Stroke stroke = sb.createStroke(randomColor(), 1);
                Mark mark = sb.createMark(randomPointShape(), fill, stroke  );
                Graphic gra = sb.createGraphic();
                gra.setOpacity( sb.literalExpression(1) );
                gra.setMarks(new Mark[]{mark});
                gra.setSize(sb.literalExpression(randomPointSize()));
                ps = sb.createPointSymbolizer(gra);
            }
                        
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        style = sb.createStyle();
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle(ps));
        
        return style;
    }
    
    public Style createRasterStyle() {
        Style style = null;
        
        StyleBuilder sb = new StyleBuilder();
        RasterSymbolizer raster = sb.createRasterSymbolizer();
        
        style = sb.createStyle(raster);
        return style;
    }
    
    public BufferedImage createGlyph(MapLayer layer){
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
    
    public BufferedImage createGlyph(Symbolizer symbol){
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
    
    public int randomPointSize(){
        return SIZES[ ((int)(Math.random() * SIZES.length)) ];
    }
    
    public int randomWidth(){
        return WIDTHS[ ((int)(Math.random() * WIDTHS.length)) ];
    }
    
    public String randomPointShape(){
        return POINT_SHAPES[ ((int)(Math.random() * POINT_SHAPES.length)) ];
    }
    
    public Color randomColor(){
        return COLORS[ ((int)(Math.random() * COLORS.length)) ];
    }
    
    
}
