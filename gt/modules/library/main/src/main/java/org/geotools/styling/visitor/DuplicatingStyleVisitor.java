/**
 * 
 */
package org.geotools.styling.visitor;

import java.util.Stack;

import org.geotools.event.GTCloneUtil;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.filter.visitor.DuplicatingFilterVisitor;
import org.geotools.styling.AnchorPoint;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ColorMapEntry;
import org.geotools.styling.Displacement;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.FeatureTypeConstraint;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
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
 * Creates a deep copy of a Style.
 * 
 * @author Jesse
 */
public class DuplicatingStyleVisitor extends DuplicatingFilterVisitor implements StyleVisitor{
	
	private final StyleFactory sf;

	public DuplicatingStyleVisitor() {
		this( CommonFactoryFinder.getStyleFactory(GeoTools.getDefaultHints()) );
	}
	
	public DuplicatingStyleVisitor(StyleFactory styleFactory) {
		this.sf=styleFactory;
	}
	public DuplicatingStyleVisitor(StyleFactory styleFactory, FilterFactory2 factory) {
		super( factory );
		this.sf=styleFactory;
	}

	private Stack pages=new Stack();
	
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
            filterCopy = (Filter) filter.accept(this, null);
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

    public void visit(Fill fill) {
        Fill copy = null;

        try {
            copy = (Fill) GTCloneUtil.clone(fill); //TODO: remove temporary hack
        } catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
        }

        pages.push(copy);
    }

    public void visit(Stroke stroke) {
        Stroke copy = null;

        try {
            copy = (Stroke) GTCloneUtil.clone(stroke); //TODO: remove temporary hack
        } catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
        }

        pages.push(copy);
    }

    public void visit(Symbolizer sym) {
        // Should not happen?
        throw new RuntimeException("visit(Symbolizer) unsupported");
    }

    public void visit(PointSymbolizer ps) {
        PointSymbolizer copy = null;

        try {
            copy = (PointSymbolizer) GTCloneUtil.clone(ps); //TODO: remove temporary hack
        } catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
        }

        pages.push(copy);
    }

    public void visit(LineSymbolizer line) {
        LineSymbolizer copy = null;

        try {
            copy = (LineSymbolizer) GTCloneUtil.clone(line); //TODO: remove temporary hack
        } catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
        }

        pages.push(copy);
    }

    public void visit(PolygonSymbolizer poly) {
        PolygonSymbolizer copy = null;

        try {
            copy = (PolygonSymbolizer) GTCloneUtil.clone(poly); //TODO: remove temporary hack
        } catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
        }

        pages.push(copy);
    }

    public void visit(TextSymbolizer text) {
        TextSymbolizer copy = null;

        try {
            copy = (TextSymbolizer) GTCloneUtil.clone(text); //TODO: remove temporary hack
        } catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
        }

        pages.push(copy);
    }

    public void visit(RasterSymbolizer raster) {
        RasterSymbolizer copy = null;

        try {
            copy = (RasterSymbolizer) GTCloneUtil.clone(raster); //TODO: remove temporary hack
        } catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
        }

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
            if (externalGraphics[i] != null) {
                externalGraphics[i].accept(this);
                externalGraphicsCopy[i] = (ExternalGraphic) pages.pop();
            }
        }

        Mark[] marks = gr.getMarks();
        Mark[] marksCopy = new Mark[marks.length];
        length=marks.length;
        for (int i = 0; i < length; i++) {
            if (marks[i] != null) {
                marks[i].accept(this);
                marksCopy[i] = (Mark) pages.pop();
            }
        }

        Expression opacityCopy = null;

        if (gr.getOpacity() != null) {
        	opacityCopy = (Expression) gr.getOpacity().accept(this, null);            
        }

        Expression rotationCopy = null;

        if (gr.getRotation() != null) {
        	rotationCopy = (Expression) gr.getRotation().accept(this, null);
        }

        Expression sizeCopy = null;

        if (gr.getSize() != null) {
        	sizeCopy  = (Expression) gr.getSize().accept(this, null);
        }

        Symbol[] symbols = gr.getSymbols();
        length=symbols.length;
        Symbol[] symbolCopys = new Symbol[length];

        for (int i = 0; i < length; i++) {
            if (symbols[i] != null) {
                symbols[i].accept(this);
                symbolCopys[i] = (Symbol) pages.pop();
            }
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

        Fill fillCopy = null;

        if (mark.getFill() != null) {
            mark.accept( this );
            fillCopy = (Fill) pages.pop();
        }

        Expression rotationCopy = null;

        if (mark.getRotation() != null) {
            rotationCopy = (Expression) mark.getRotation().accept(this, null);
        }

        Expression sizeCopy = null;

        if (mark.getSize() != null) {
            sizeCopy = (Expression) mark.getSize().accept(this, null);
        }

        Stroke strokeCopy = null;

        if (mark.getStroke() != null) {
            mark.getStroke().accept(this);
            strokeCopy = (Stroke) pages.pop();
        }

        Expression wellKnownNameCopy = null;

        if (mark.getWellKnownName() != null) {
            wellKnownNameCopy = (Expression) mark.getWellKnownName().accept(this, null);
        }

        copy = sf.createMark();
        copy.setFill(fillCopy);
        copy.setRotation((Expression) rotationCopy);
        copy.setSize((Expression) sizeCopy);
        copy.setStroke(strokeCopy);
        copy.setWellKnownName((Expression) wellKnownNameCopy);

        pages.push(copy);
    }

    public void visit(ExternalGraphic exgr) {
        ExternalGraphic copy = null;

        try {
            copy = (ExternalGraphic) GTCloneUtil.clone(exgr); //TODO: remove temporary hack
        } catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
        }

        pages.push(copy);
    }

    public void visit(PointPlacement pp) {
        PointPlacement copy = null;

        try {
            copy = (PointPlacement) GTCloneUtil.clone(pp); //TODO: remove temporary hack
        } catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
        }

        pages.push(copy);
    }

    public void visit(AnchorPoint ap) {
        AnchorPoint copy = null;

        try {
            copy = (AnchorPoint) GTCloneUtil.clone(ap); //TODO: remove temporary hack
        } catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
        }

        pages.push(copy);
    }

    public void visit(Displacement dis) {
        Displacement copy = null;

        try {
            copy = (Displacement) GTCloneUtil.clone(dis); //TODO: remove temporary hack
        } catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
        }

        pages.push(copy);
    }

    public void visit(LinePlacement lp) {
        LinePlacement copy = null;

        try {
            copy = (LinePlacement) GTCloneUtil.clone(lp); //TODO: remove temporary hack
        } catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
        }

        pages.push(copy);
    }

    public void visit(Halo halo) {
        Halo copy = null;

        try {
            copy = (Halo) GTCloneUtil.clone(halo); //TODO: remove temporary hack
        } catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
        }

        pages.push(copy);
    }

    public void visit(FeatureTypeConstraint ftc) {
        FeatureTypeConstraint copy = null;

        try {
            copy = (FeatureTypeConstraint) GTCloneUtil.clone(ftc); //TODO: remove temporary hack
        } catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
        }

        pages.push(copy);
    }

	public void visit(ColorMap arg0) {
		// TODO Auto-generated method stub
	}

	public void visit(ColorMapEntry arg0) {
		// TODO Auto-generated method stub
	}
}
