/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006-2007, GeoTools Project Managment Committee (PMC)
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
package org.geotools.styling.visitor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.geotools.event.GTCloneUtil;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.filter.visitor.DuplicatingFilterVisitor;
import org.geotools.styling.AnchorPoint;
import org.geotools.styling.ChannelSelection;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ColorMapEntry;
import org.geotools.styling.ContrastEnhancement;
import org.geotools.styling.Displacement;
import org.geotools.styling.Extent;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.FeatureTypeConstraint;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Font;
import org.geotools.styling.Graphic;
import org.geotools.styling.Halo;
import org.geotools.styling.LinePlacement;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.NamedLayer;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SelectedChannelType;
import org.geotools.styling.ShadedRelief;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleVisitor;
import org.geotools.styling.StyledLayer;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.styling.Symbol;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.geotools.styling.UserLayer;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;

/**
 * Creates a deep copy of a Style, this class is *NOT THREAD SAFE*.
 * <p>
 * This class makes use of an internal stack to story the copied result,
 * retrieve with a call to getCopy() after visiting:<pre><code>
 * DuplicatingStyleVisitor copyStyle = new DuplicatingStyleVisitor();
 * rule.accepts( copyStyle );
 * Rule rule = (Rule) copyStyle.getCopy();
 * </code></pre>
 * 
 * @author Jesse Eichar
 */
public class DuplicatingStyleVisitor implements StyleVisitor {
	
	protected final StyleFactory sf;
    protected final FilterFactory2 ff;
    /**
     * We are using aggregation here to contain our DuplicatingFilterVisitor.
     */
    protected final DuplicatingFilterVisitor copyFilter;
    /**
     * This is our internal stack; used to maintain state as we copy sub elements.
     */
    protected Stack pages=new Stack();  
    
	public DuplicatingStyleVisitor() {
		this( CommonFactoryFinder.getStyleFactory( null ) );
	}	
	public DuplicatingStyleVisitor(StyleFactory styleFactory) {
	    this( styleFactory, CommonFactoryFinder.getFilterFactory2( null ));
	}
	public DuplicatingStyleVisitor(StyleFactory styleFactory, FilterFactory2 filterFactory) {
		this.copyFilter = new DuplicatingFilterVisitor( filterFactory );
		this.sf=styleFactory;
		this.ff=filterFactory;
	}
	
	public Object getCopy() {
		return pages.peek();
	}
	
    public void visit(StyledLayerDescriptor sld) {
        StyledLayerDescriptor copy = null;

        StyledLayer[] layers = sld.getStyledLayers();
        StyledLayer[] layersCopy = new StyledLayer[layers.length];
        final int length=layers.length;
        for (int i = 0; i < length; i++) {
            if (layers[i] instanceof UserLayer) {
                ((UserLayer) layers[i]).accept(this);
                layersCopy[i] = (UserLayer) pages.pop();
            } else if (layers[i] instanceof NamedLayer) {
                ((NamedLayer) layers[i]).accept(this);
                layersCopy[i] = (NamedLayer) pages.pop();
            }
        }

        copy = sf.createStyledLayerDescriptor();
        copy.setAbstract(sld.getAbstract());
        copy.setName(sld.getName());
        copy.setTitle(sld.getTitle());
        copy.setStyledLayers(layersCopy);

        pages.push(copy);
    }

    public void visit(NamedLayer layer) {
        NamedLayer copy = null;

        Style[] style = layer.getStyles();
        Style[] styleCopy = new Style[style.length];
        int length=style.length;
        for (int i = 0; i < length; i++) {
            if (style[i] != null) {
                style[i].accept(this);
                styleCopy[i] = (Style) pages.pop();
            }
        }

        FeatureTypeConstraint[] lfc = layer.getLayerFeatureConstraints();
        FeatureTypeConstraint[] lfcCopy = new FeatureTypeConstraint[lfc.length];

        length=lfc.length;
        for (int i = 0; i < length; i++) {
            if (lfc[i] != null) {
                lfc[i].accept(this);
                lfcCopy[i] = (FeatureTypeConstraint) pages.pop();
            }
        }

        copy = sf.createNamedLayer();
        copy.setName(layer.getName());
        length=styleCopy.length;
        for (int i = 0; i < length; i++) {
            copy.addStyle(styleCopy[i]);
        }

        copy.setLayerFeatureConstraints(lfcCopy);
        pages.push(copy);
    }

    public void visit(UserLayer layer) {
        UserLayer copy = null;

       
        Style[] style = layer.getUserStyles();
         int length=style.length;
        Style[] styleCopy = new Style[length];
        for (int i = 0; i < length; i++) {
            if (style[i] != null) {
                style[i].accept(this);
                styleCopy[i] = (Style) pages.pop();
            }
        }

        FeatureTypeConstraint[] lfc = layer.getLayerFeatureConstraints();
        FeatureTypeConstraint[] lfcCopy = new FeatureTypeConstraint[lfc.length];

        length=lfc.length;
        for (int i = 0; i < length; i++) {
            if (lfc[i] != null) {
                lfc[i].accept(this);
                lfcCopy[i] = (FeatureTypeConstraint) pages.pop();
            }
        }

        copy = sf.createUserLayer();
        copy.setName(layer.getName());
        copy.setUserStyles(styleCopy);
        copy.setLayerFeatureConstraints(lfcCopy);

        pages.push(copy);
    }

    public void visit(Style style) {
        Style copy = null;

        FeatureTypeStyle[] fts = style.getFeatureTypeStyles();
        final int length=fts.length;
        FeatureTypeStyle[] ftsCopy = new FeatureTypeStyle[length];
        for (int i = 0; i < length; i++) {
            if (fts[i] != null) {
                fts[i].accept(this);
                ftsCopy[i] = (FeatureTypeStyle) pages.pop();
            }
        }

        copy = sf.createStyle();
        copy.setAbstract(style.getAbstract());
        copy.setName(style.getName());
        copy.setTitle(style.getTitle());
        copy.setFeatureTypeStyles(ftsCopy);

        pages.push(copy);
    }

    public void visit(Rule rule) {
        Rule copy = null;

        Filter filterCopy = null;

        if (rule.getFilter() != null) {
            Filter filter = rule.getFilter();
            filterCopy = copy( rule.getFilter() );
        }

        Graphic[] legendGraphic = rule.getLegendGraphic();
        Graphic[] legendGraphicCopy = new Graphic[legendGraphic.length];

        int length=legendGraphic.length;
        for (int i = 0; i < length; i++) {
            if (legendGraphic[i] != null) {
                legendGraphic[i].accept(this);
                legendGraphicCopy[i] = (Graphic) pages.pop();
            }
        }

        Symbolizer[] symbolizer = rule.getSymbolizers();
        Symbolizer[] symbolizerCopy = new Symbolizer[symbolizer.length];

        length=symbolizer.length;
        for (int i = 0; i < length; i++) {
            if (symbolizer[i] != null) {
                symbolizer[i].accept(this);
                symbolizerCopy[i] = (Symbolizer) pages.pop();
            }
        }

        copy = sf.createRule();
        copy.setAbstract(rule.getAbstract());
        copy.setFilter(filterCopy);
        copy.setIsElseFilter(rule.hasElseFilter());
        copy.setLegendGraphic(legendGraphicCopy);
        copy.setMinScaleDenominator(rule.getMinScaleDenominator());
        copy.setMaxScaleDenominator(rule.getMaxScaleDenominator());
        copy.setName(rule.getName());
        copy.setTitle(rule.getTitle());
        copy.setSymbolizers(symbolizerCopy);

        pages.push(copy);
    }

    public void visit(FeatureTypeStyle fts) {
        FeatureTypeStyle copy = null;

        Rule[] rules = fts.getRules();
        int length=rules.length;
        Rule[] rulesCopy = new Rule[length];
        for (int i = 0; i < length; i++) {
            if (rules[i] != null) {
                rules[i].accept(this);
                rulesCopy[i] = (Rule) pages.pop();
            }
        }

        copy = sf.createFeatureTypeStyle();
        copy.setName(fts.getName());
        copy.setTitle(fts.getTitle());
        copy.setAbstract(fts.getAbstract());
        copy.setFeatureTypeName(fts.getFeatureTypeName());
        copy.setRules(rulesCopy);
        copy.setSemanticTypeIdentifiers((String[]) fts.getSemanticTypeIdentifiers().clone());
        
        pages.push(copy);
    }
    /**
     * Null safe expression copy.
     * <p>
     * This method will perform a null check, and save you some lines of code:<pre><code>
     * copy.setBackgroundColor( copyExpr( fill.getColor()) );
     * </code></pre>
     * @param sion
     * @return copy of expression or null if expression was null
     */    
    protected Expression copy( Expression expression ){
        if( expression == null  ) return null;
        return (Expression) expression.accept( copyFilter, ff );
    }
    /**
     * Null safe copy of filter.
     */
    protected Filter copy( Filter filter ){
        if( filter == null ) return null;
        return (Filter) filter.accept( copyFilter, ff );        
    }
    
    /**
     * Null safe graphic copy
     * @param graphic
     * @return copy of graphic or null if not provided
     */
    protected Graphic copy( Graphic graphic ){
        if( graphic == null ) return null;
        
        graphic.accept(this);
        return (Graphic) pages.pop();
    }
    /**
     * Null safe fill copy
     * @param graphic
     * @return copy of graphic or null if not provided
     */
    protected Fill copy( Fill fill ){
        if( fill == null ) return null;
        
        fill.accept(this);
        return (Fill) pages.pop();
    }
    /**
     * Null safe copy of float array.
     * @param array
     * @return copy of array or null if not provided
     */
    protected float[] copy(float[] array) {
        if( array == null ) return null;
        
        float copy[] = new float[ array.length];
        System.arraycopy( array, 0, copy, 0, array.length );
        return copy;
    }
    /**
     * Null safe map copy, used for external graphic custom properties.
     * @param customProperties
     * @return copy of map
     */
    @SuppressWarnings("unchecked")
    protected Map copy(Map customProperties) {
        return new HashMap( customProperties );
    }
    
    /**
     * Null safe copy of stroke.
     * @param stroke
     * @return copy of stroke if provided
     */
    protected Stroke copy( Stroke stroke ){
        if( stroke == null ) return null;
        stroke.accept(this);
        return (Stroke) pages.pop();
    }
    /**
     * Null safe copy of shaded relief.
     * @param shaded
     * @return copy of shaded or null if not provided
     */
    protected ShadedRelief copy(ShadedRelief shaded) {
        if( shaded == null ) return null;
        Expression reliefFactor = copy( shaded.getReliefFactor() );
        ShadedRelief copy = sf.createShadedRelief( reliefFactor );
        copy.setBrightnessOnly( shaded.isBrightnessOnly() );
        
        return copy;
    }
    protected ExternalGraphic copy( ExternalGraphic externalGraphic){
        if( externalGraphic == null ) return null;
        externalGraphic.accept(this);
        return (ExternalGraphic) pages.pop();
    }
    protected Mark copy( Mark mark){
        if( mark == null ) return null;
        mark.accept(this);
        return (Mark) pages.pop();
    }
    protected ColorMapEntry copy(ColorMapEntry entry) {
        if( entry == null ) return null;
        
        entry.accept( this );
        return (ColorMapEntry) pages.pop();
    }
    
    protected Symbolizer copy(Symbolizer symbolizer) {
        if( symbolizer == null ) return null;
        
        symbolizer.accept(this);
        return (Symbolizer) pages.pop();
    }
    protected ContrastEnhancement copy(ContrastEnhancement contrast) {
        if( contrast == null ) return null;
        
        ContrastEnhancement copy = sf.createContrastEnhancement();
        copy.setGammaValue( copy( contrast.getGammaValue()));
        return copy;
    }
    protected ColorMap copy(ColorMap colorMap) {
        if( colorMap == null ) return null;
        
        colorMap.accept(this);
        return (ColorMap) getCopy();
    }
    
    protected SelectedChannelType[] copy( SelectedChannelType[] channels){
        if( channels == null ) return null;
        
        SelectedChannelType[] copy = new SelectedChannelType[ channels.length ];
        for( int i=0; i< channels.length ; i++){
            copy[i] = copy( channels[i] );
        }
        return copy;
    }
    
    protected SelectedChannelType copy(SelectedChannelType selectedChannelType) {
        if( selectedChannelType == null ) return null;
        
        ContrastEnhancement enhancement = copy( selectedChannelType.getContrastEnhancement() );
        String name = selectedChannelType.getChannelName();
        SelectedChannelType copy = sf.createSelectedChannelType( name, enhancement);
        
        return copy;
    }
    
    protected ChannelSelection copy(ChannelSelection channelSelection) {
        if( channelSelection == null ) return null;
     
        SelectedChannelType[] channels = copy( channelSelection.getSelectedChannels() );
        ChannelSelection copy = sf.createChannelSelection( channels);
        copy.setGrayChannel( copy( channelSelection.getGrayChannel() ));
        copy.setRGBChannels( copy( channelSelection.getRGBChannels() ));
        return copy;
    }
    
    /**
     * Null safe copy of font array.
     * <p>
     * Right now style visitor does not let us visit fonts!
     * @param fonts
     * @return copy of provided fonts
     */
    protected Font[] copy(Font[] fonts) {
        if( fonts == null ) return null;
        Font copy[] = new Font[ fonts.length ];
        for( int i=0; i<fonts.length; i++){
            copy[i] = copy( fonts[i] );
        }
        return copy;
    }
    /** Null safe copy of a single font */
    protected Font copy(Font font) {
        if( font == null) return font;
        
        Expression fontFamily = copy( font.getFontFamily() );
        Expression fontStyle = copy( font.getFontStyle() );
        Expression fontWeight = copy( font.getFontWeight() );
        Expression fontSize = copy( font.getFontSize() );
        Font copy = sf.createFont(fontFamily, fontStyle, fontWeight, fontSize);
        return copy;
    }
    /**
     * Null safe copy of halo.
     * @param halo 
     * @return copy of halo if provided
     */
    protected Halo copy( Halo halo){
        if( halo == null ) return null;
        halo.accept(this);
        return (Halo) getCopy();
    }
    /**
     * Null safe copy of displacement.
     * @param displacement
     * @return copy of displacement if provided
     */
    protected Displacement copy(Displacement displacement) {
        if( displacement == null ) return null;
        displacement.accept(this);
        return (Displacement) getCopy();
    }
    
    protected Symbol copy(Symbol symbol) {
        if( symbol == null ) return null;
        symbol.accept(this);
        return (Symbol) getCopy();
    }
    /**
     * Null safe copy of anchor point.
     * @param anchorPoint
     * @return copy of anchor point if provided
     */
    protected AnchorPoint copy(AnchorPoint anchorPoint) {
        if( anchorPoint == null ) return null;
        anchorPoint.accept(this);
        return (AnchorPoint) getCopy();
    }

    public void visit(Fill fill) {
        Fill copy = sf.getDefaultFill();
        copy.setBackgroundColor( copy( fill.getBackgroundColor()) );
        copy.setColor(copy( fill.getColor()));
        copy.setGraphicFill( copy(fill.getGraphicFill()));
        copy.setOpacity( copy(fill.getOpacity()));
        pages.push(copy);
    }

    public void visit(Stroke stroke) {
        Stroke copy = sf.getDefaultStroke();
        copy.setColor( copy(stroke.getColor()));
        copy.setDashArray( copy(stroke.getDashArray()));
        copy.setDashOffset( copy( stroke.getDashOffset()));
        copy.setGraphicFill( copy(stroke.getGraphicFill()));
        copy.setGraphicStroke( copy( stroke.getGraphicStroke()));
        copy.setLineCap(copy(stroke.getLineCap()));
        copy.setLineJoin( copy(stroke.getLineJoin()));
        copy.setOpacity( copy(stroke.getOpacity()));
        copy.setWidth( copy(stroke.getWidth()));
        pages.push(copy);
    }

    public void visit(Symbolizer sym) {
        if( sym instanceof RasterSymbolizer){
            visit( (RasterSymbolizer) sym );            
        }
        else if( sym instanceof LineSymbolizer){
            visit( (LineSymbolizer) sym );            
        }
        else if( sym instanceof PolygonSymbolizer){
            visit( (PolygonSymbolizer) sym );            
        }
        else if( sym instanceof PointSymbolizer){
            visit( (PointSymbolizer) sym );            
        }
        else if( sym instanceof TextSymbolizer){
            visit( (TextSymbolizer) sym );            
        }
        else {
            throw new RuntimeException("visit(Symbolizer) unsupported");
        }
    }

    public void visit(PointSymbolizer ps) {
        PointSymbolizer copy = sf.getDefaultPointSymbolizer();
        copy.setGeometryPropertyName( ps.getGeometryPropertyName());
        copy.setGraphic( copy( ps.getGraphic() ));
        pages.push(copy);
    }

    public void visit(LineSymbolizer line) {
        LineSymbolizer copy = sf.getDefaultLineSymbolizer();
        copy.setGeometryPropertyName( line.getGeometryPropertyName());
        copy.setStroke( copy( line.getStroke()));
        pages.push(copy);
    }

    public void visit(PolygonSymbolizer poly) {
        PolygonSymbolizer copy = sf.createPolygonSymbolizer();
        copy.setFill( copy( poly.getFill()));
        copy.setGeometryPropertyName( poly.getGeometryPropertyName());
        copy.setStroke(copy(poly.getStroke()));
        pages.push(copy);
    }

    public void visit(TextSymbolizer text) {
        TextSymbolizer copy = sf.createTextSymbolizer();
        copy.setFill( copy( text.getFill()));
        copy.setFonts(copy( text.getFonts()));
        copy.setGeometryPropertyName( text.getGeometryPropertyName() );
        copy.setHalo( copy( text.getHalo() ));
        
        pages.push(copy);
    }
    
    public void visit(RasterSymbolizer raster) {
        RasterSymbolizer copy = sf.createRasterSymbolizer();
        copy.setChannelSelection( copy( raster.getChannelSelection() ));
        copy.setColorMap( copy( raster.getColorMap() ));
        copy.setContrastEnhancement( copy( raster.getContrastEnhancement()));
        copy.setGeometryPropertyName( raster.getGeometryPropertyName());
        copy.setImageOutline( copy( raster.getImageOutline()));
        copy.setOpacity( copy( raster.getOpacity() ));
        copy.setOverlap( copy( raster.getOverlap()));
        copy.setShadedRelief( copy( raster.getShadedRelief()));
        pages.push(copy);
    }

    public void visit(Graphic gr) {
        Graphic copy = null;

        Displacement displacementCopy = null;

        if (gr.getDisplacement() != null) {
            gr.getDisplacement().accept(this);
            displacementCopy = (Displacement) pages.pop();
        }

        ExternalGraphic[] externalGraphics = gr.getExternalGraphics();
        ExternalGraphic[] externalGraphicsCopy = new ExternalGraphic[externalGraphics.length];

        int length=externalGraphics.length;
        for (int i = 0; i < length; i++) {
            externalGraphicsCopy[i] = copy( externalGraphics[i]);
        }

        Mark[] marks = gr.getMarks();
        Mark[] marksCopy = new Mark[marks.length];
        length=marks.length;
        for (int i = 0; i < length; i++) {
            marksCopy[i] = copy( marks[i]);
        }

        Expression opacityCopy = copy( gr.getOpacity() );
        Expression rotationCopy = copy( gr.getRotation() );
        Expression sizeCopy = copy( gr.getSize() );
        
        Symbol[] symbols = gr.getSymbols();
        length=symbols.length;
        Symbol[] symbolCopys = new Symbol[length];

        for (int i = 0; i < length; i++) {
            symbolCopys[i] = copy( symbols[i] );
        }

        copy = sf.createDefaultGraphic();
        copy.setGeometryPropertyName(gr.getGeometryPropertyName());
        copy.setDisplacement(displacementCopy);
        copy.setExternalGraphics(externalGraphicsCopy);
        copy.setMarks(marksCopy);
        copy.setOpacity((Expression) opacityCopy);
        copy.setRotation((Expression) rotationCopy);
        copy.setSize((Expression) sizeCopy);
        copy.setSymbols(symbolCopys);

        pages.push(copy);
    }

    public void visit(Mark mark) {
        Mark copy = null;

        copy = sf.createMark();
        copy.setFill(copy( mark.getFill() ));
        copy.setRotation( copy( mark.getRotation() ));
        copy.setSize(copy( mark.getSize() ));
        copy.setStroke(copy( mark.getStroke() ));
        copy.setWellKnownName(copy( mark.getWellKnownName() ));
        pages.push(copy);
    }

    public void visit(ExternalGraphic exgr) {
        URL uri = null;
        try {
            uri = exgr.getLocation();
        }
        catch (MalformedURLException huh ){
            
        }
        String format = exgr.getFormat();
        ExternalGraphic copy = sf.createExternalGraphic(uri, format);
        copy.setCustomProperties( copy(exgr.getCustomProperties()));
        pages.push(copy);       
    }

    public void visit(PointPlacement pp) {
        PointPlacement copy = sf.getDefaultPointPlacement();
        copy.setAnchorPoint( copy( pp.getAnchorPoint() ));
        copy.setDisplacement( copy(pp.getDisplacement()));
        copy.setRotation( copy( pp.getRotation() ));
        pages.push(copy);
    }

    public void visit(AnchorPoint ap) {        
        Expression x = copy( ap.getAnchorPointX() );
        Expression y = copy( ap.getAnchorPointY() );
        AnchorPoint copy = sf.createAnchorPoint(x, y);
        
        pages.push(copy);
    }

    public void visit(Displacement dis) {
        Expression x = copy( dis.getDisplacementX() );
        Expression y = copy( dis.getDisplacementY() );
        Displacement copy = sf.createDisplacement(x, y);
        pages.push(copy);
    }

    public void visit(LinePlacement lp) {
        Expression offset = copy( lp.getPerpendicularOffset());
        LinePlacement copy = sf.createLinePlacement(offset);
        pages.push(copy);
    }

    public void visit(Halo halo) {
        Fill fill = copy( halo.getFill());
        Expression radius = copy( halo.getRadius() );
        Halo copy = sf.createHalo(fill, radius);

        pages.push(copy);
    }

    public void visit(FeatureTypeConstraint ftc) {
        String typeName = ftc.getFeatureTypeName();
        Filter filter = copy( ftc.getFilter() );
        Extent[] extents = copy( ftc.getExtents() );
        FeatureTypeConstraint copy = sf.createFeatureTypeConstraint( typeName, filter, extents);
        
        pages.push(copy);
    }

    protected Extent[] copy(Extent[] extents) {
	    if( extents == null ) return null;
	    
	    Extent[] copy = new Extent[ extents.length ];
	    for( int i=0; i<extents.length; i++){
	        copy[i] = copy( extents[i] );
	    }
	    return copy;
	}
	
    protected Extent copy(Extent extent) {
        String name = extent.getName();
        String value = extent.getValue();
        Extent copy = sf.createExtent(name, value);
        return copy;
    }
    
    public void visit(ColorMap colorMap) {	    
	    ColorMap copy = sf.createColorMap();
	    copy.setType( colorMap.getType() );	    
	    ColorMapEntry[] entries = colorMap.getColorMapEntries();
	    if( entries != null ){
	        for( int i=0; i<entries.length;i++){
	            ColorMapEntry entry = entries[i];
                copy.addColorMapEntry( copy( entry ));
	        }
	    }
	 	pages.push(copy);
	}
	
    public void visit(ColorMapEntry colorMapEntry) {
	    ColorMapEntry copy = sf.createColorMapEntry();
	    copy.setColor( copy( colorMapEntry.getColor() ));
	    copy.setLabel( colorMapEntry.getLabel() );
	    copy.setOpacity( copy( colorMapEntry.getOpacity()));
	    copy.setQuantity( copy.getQuantity());

	    pages.push(copy);
    }
}
