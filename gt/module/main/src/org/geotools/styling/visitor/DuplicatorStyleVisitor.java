package org.geotools.styling.visitor;

import org.geotools.event.GTCloneUtil;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.visitor.DuplicatorFilterVisitor;
import org.geotools.styling.AnchorPoint;
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
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleVisitor;
import org.geotools.styling.StyledLayer;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.styling.Symbol;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.geotools.styling.UserLayer;

/**
 * Used to duplicate a Style object (anything in the SLD hierarchy)
 * 
 * @author Cory Horner, Refractions Research Inc.
 */
public class DuplicatorStyleVisitor extends DuplicatorFilterVisitor implements StyleVisitor {
	//Stack pages; // need a Stack as Filter structure is recursive
	StyleFactory sf;
	
	public DuplicatorStyleVisitor( StyleFactory sf, FilterFactory ff ){ // FilterFactory factory 
		super(ff);
		this.sf = sf;
	}
	
	public void setStyleFactory( StyleFactory styleFactory ){
		this.sf = styleFactory;
	}

	public void visit(StyledLayerDescriptor sld) {
		StyledLayerDescriptor copy = null;
		
		StyledLayer[] layers = sld.getStyledLayers();
		StyledLayer[] layersCopy = new StyledLayer[layers.length];
		for (int i = 0; i < layers.length; i++) {
			if (layers[i] instanceof UserLayer) {
				((UserLayer) layers[i]).accept(this);
				layersCopy[i] = (UserLayer) getPages().pop();
			} else if (layers[i] instanceof NamedLayer) {
				((NamedLayer) layers[i]).accept(this);
				layersCopy[i] = (NamedLayer) getPages().pop();
			}
		}

		copy = sf.createStyledLayerDescriptor();
		copy.setAbstract(sld.getAbstract());
		copy.setName(sld.getName());
		copy.setTitle(sld.getTitle());
		copy.setStyledLayers(layersCopy);
		
		getPages().push(copy);
	}

	public void visit(NamedLayer layer) {
		NamedLayer copy = null;
		
		Style[] style = layer.getStyles();
		Style[] styleCopy = new Style[style.length];
		for (int i = 0; i < style.length; i++) {
			if (style[i] != null) {
				style[i].accept(this);
				styleCopy[i] = (Style) getPages().pop();
			}
		}

		FeatureTypeConstraint[] lfc = layer.getLayerFeatureConstraints();
		FeatureTypeConstraint[] lfcCopy = new FeatureTypeConstraint[lfc.length];
		for (int i = 0; i < lfc.length; i++) {
			if (lfc[i] != null) {
				lfc[i].accept(this);
				lfcCopy[i] = (FeatureTypeConstraint) getPages().pop();
			}
		}

		copy = sf.createNamedLayer();
		copy.setName(layer.getName());
		for (int i = 0; i < styleCopy.length; i++) {
			copy.addStyle(styleCopy[i]);
		}
		//copy.setLayerFeatureConstraints(); //TODO: add setLayerFeatureConstraints?
		getPages().push(copy);
	}

	public void visit(UserLayer layer) {
		UserLayer copy = null;
		
		Style[] style = layer.getUserStyles();
		Style[] styleCopy = new Style[style.length];
		for (int i = 0; i < style.length; i++) {
			if (style[i] != null) {
				style[i].accept(this);
				styleCopy[i] = (Style) getPages().pop();
			}
		}

		FeatureTypeConstraint[] lfc = layer.getLayerFeatureConstraints();
		FeatureTypeConstraint[] lfcCopy = new FeatureTypeConstraint[lfc.length];
		for (int i = 0; i < lfc.length; i++) {
			if (lfc[i] != null) {
				lfc[i].accept(this);
				lfcCopy[i] = (FeatureTypeConstraint) getPages().pop();
			}
		}

		copy = sf.createUserLayer();
		copy.setName(layer.getName());
		copy.setUserStyles(styleCopy);
		copy.setLayerFeatureConstraints(lfcCopy);
		
		getPages().push(copy);
	}

	public void visit(Style style) {
		Style copy = null;
		
		FeatureTypeStyle[] fts = style.getFeatureTypeStyles();
		FeatureTypeStyle[] ftsCopy = new FeatureTypeStyle[fts.length];
		for (int i = 0; i < fts.length; i++) {
			if (fts[i] != null) {
				fts[i].accept(this);
				ftsCopy[i] = (FeatureTypeStyle) getPages().pop();
			}
		}

		copy = sf.createStyle();
		copy.setAbstract(style.getAbstract());
		copy.setName(style.getName());
		copy.setTitle(style.getTitle());
		copy.setFeatureTypeStyles(ftsCopy);
		
		getPages().push(copy);
	}

	public void visit(Rule rule) {
		Rule copy = null;
		
		Filter filterCopy = null;
		
		if (rule.getFilter() != null) {
			rule.getFilter().accept(this);
			filterCopy = (Filter) getPages().pop();
		}
		
		Graphic[] legendGraphic = rule.getLegendGraphic();
		Graphic[] legendGraphicCopy = new Graphic[legendGraphic.length];
		for (int i = 0; i < legendGraphic.length; i++) {
			if (legendGraphic[i] != null) {
				legendGraphic[i].accept(this);
				legendGraphicCopy[i] = (Graphic) getPages().pop();
			}
		}

		Symbolizer[] symbolizer = rule.getSymbolizers();
		Symbolizer[] symbolizerCopy = new Symbolizer[symbolizer.length];
		for (int i = 0; i < symbolizer.length; i++) {
			if (symbolizer[i] != null) {
				symbolizer[i].accept(this);
				symbolizerCopy[i] = (Symbolizer) getPages().pop();
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
		
		getPages().push(copy);
	}

	public void visit(FeatureTypeStyle fts) {
		FeatureTypeStyle copy = null;
		
		Rule[] rules = fts.getRules();
		Rule[] rulesCopy = new Rule[rules.length];
		for (int i = 0; i < rules.length; i++) {
			if (rules[i] != null) {
				rules[i].accept(this);
				rulesCopy[i] = (Rule) getPages().pop();
			}
		}

		copy = sf.createFeatureTypeStyle();
		copy.setName(fts.getName());
		copy.setTitle(fts.getTitle());
		copy.setAbstract(fts.getAbstract());
		copy.setFeatureTypeName(fts.getFeatureTypeName());
		copy.setRules(rulesCopy);
		
		getPages().push(copy);
	}

	public void visit(Fill fill) {
		Fill copy = null;
		try {
			copy = (Fill) GTCloneUtil.clone(fill); //TODO: remove temporary hack
		} catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
		}
		getPages().push(copy); 
	}

	public void visit(Stroke stroke) {
		Stroke copy = null;
		try {
			copy = (Stroke) GTCloneUtil.clone(stroke); //TODO: remove temporary hack
		} catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
		}
		getPages().push(copy); 
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
		getPages().push(copy); 
	}

	public void visit(LineSymbolizer line) {
		LineSymbolizer copy = null;
		try {
			copy = (LineSymbolizer) GTCloneUtil.clone(line); //TODO: remove temporary hack
		} catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
		}
		getPages().push(copy); 
	}

	public void visit(PolygonSymbolizer poly) {
		PolygonSymbolizer copy = null;
		try {
			copy = (PolygonSymbolizer) GTCloneUtil.clone(poly); //TODO: remove temporary hack
		} catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
		}
		getPages().push(copy); 
	}

	public void visit(TextSymbolizer text) {
		TextSymbolizer copy = null;
		try {
			copy = (TextSymbolizer) GTCloneUtil.clone(text); //TODO: remove temporary hack
		} catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
		}
		getPages().push(copy); 
	}

	public void visit(RasterSymbolizer raster) {
		RasterSymbolizer copy = null;
		try {
			copy = (RasterSymbolizer) GTCloneUtil.clone(raster); //TODO: remove temporary hack
		} catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
		}
		getPages().push(copy); 
	}

	public void visit(Graphic gr) {
		Graphic copy = null;
		
		Displacement displacementCopy = null;
		if (gr.getDisplacement() != null) {
			gr.getDisplacement().accept(this);
			displacementCopy = (Displacement) getPages().pop();
		}

		ExternalGraphic[] externalGraphics = gr.getExternalGraphics();
		ExternalGraphic[] externalGraphicsCopy = new ExternalGraphic[externalGraphics.length];
		for (int i = 0; i < externalGraphics.length; i++) {
			if (externalGraphics[i] != null) {
				externalGraphics[i].accept(this);
				externalGraphicsCopy[i] = (ExternalGraphic) getPages().pop();
			}
		}

		Mark[] marks = gr.getMarks();
		Mark[] marksCopy = new Mark[marks.length];
		for (int i = 0; i < marks.length; i++) {
			if (marks[i] != null) {
				marks[i].accept(this);
				marksCopy[i] = (Mark) getPages().pop();
			}
		}
		
		Expression opacityCopy = null;
		if (gr.getOpacity() != null) {
			gr.getOpacity().accept(this);
			opacityCopy = (Expression) getPages().pop();
		}

		Expression rotationCopy = null;
		if (gr.getRotation() != null) {
			gr.getRotation().accept(this);
			rotationCopy = (Expression) getPages().pop();
		}

		Expression sizeCopy = null;
		if (gr.getSize() != null) {
			gr.getSize().accept(this);
			sizeCopy = (Expression) getPages().pop();
		}

		Symbol[] symbols = gr.getSymbols();
		Symbol[] symbolCopys = new Symbol[symbols.length];
		for (int i = 0; i < symbols.length; i++) {
			if (symbols[i] != null) {
				symbols[i].accept(this);
				symbolCopys[i] = (Symbol) getPages().pop();
			}
		}

		copy = sf.createDefaultGraphic();
		copy.setGeometryPropertyName(gr.getGeometryPropertyName());
		copy.setDisplacement(displacementCopy);
		copy.setExternalGraphics(externalGraphicsCopy);
		copy.setMarks(marksCopy);
		copy.setOpacity(opacityCopy);
		copy.setRotation(rotationCopy);
		copy.setSize(sizeCopy);
		copy.setSymbols(symbolCopys);
		
		getPages().push(copy);
	}

	public void visit(Mark mark) {
		Mark copy = null;
		
		Fill fillCopy = null;
		if (mark.getFill() != null) {
			mark.getFill().accept(this);
			fillCopy = (Fill) getPages().pop();
		}

		Expression rotationCopy = null;
		if (mark.getRotation() != null) {
			mark.getRotation().accept(this);
			rotationCopy = (Expression) getPages().pop();
		}

		Expression sizeCopy = null;
		if (mark.getSize() != null) {
			mark.getSize().accept(this);
			sizeCopy = (Expression) getPages().pop();
		}

		Stroke strokeCopy = null;
		if (mark.getStroke() != null) {
			mark.getStroke().accept(this);
			strokeCopy = (Stroke) getPages().pop();
		}

		Expression wellKnownNameCopy = null;
		if (mark.getWellKnownName() != null) {
			mark.getWellKnownName().accept(this);
			wellKnownNameCopy = (Expression) getPages().pop();
		}

		copy = sf.createMark();
		copy.setFill(fillCopy);
		copy.setRotation(rotationCopy);
		copy.setSize(sizeCopy);
		copy.setStroke(strokeCopy);
		copy.setWellKnownName(wellKnownNameCopy);
		
		getPages().push(copy);
	}

	public void visit(ExternalGraphic exgr) {
		ExternalGraphic copy = null;
		try {
			copy = (ExternalGraphic) GTCloneUtil.clone(exgr); //TODO: remove temporary hack
		} catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
		}
		getPages().push(copy); 
	}

	public void visit(PointPlacement pp) {
		PointPlacement copy = null;
		try {
			copy = (PointPlacement) GTCloneUtil.clone(pp); //TODO: remove temporary hack
		} catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
		}
		getPages().push(copy); 
	}

	public void visit(AnchorPoint ap) {
		AnchorPoint copy = null;
		try {
			copy = (AnchorPoint) GTCloneUtil.clone(ap); //TODO: remove temporary hack
		} catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
		}
		getPages().push(copy); 
	}

	public void visit(Displacement dis) {
		Displacement copy = null;
		try {
			copy = (Displacement) GTCloneUtil.clone(dis); //TODO: remove temporary hack
		} catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
		}
		getPages().push(copy); 
	}

	public void visit(LinePlacement lp) {
		LinePlacement copy = null;
		try {
			copy = (LinePlacement) GTCloneUtil.clone(lp); //TODO: remove temporary hack
		} catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
		}
		getPages().push(copy); 
	}

	public void visit(Halo halo) {
		Halo copy = null;
		try {
			copy = (Halo) GTCloneUtil.clone(halo); //TODO: remove temporary hack
		} catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
		}
		getPages().push(copy); 
	}

	public void visit(FeatureTypeConstraint ftc) {
		FeatureTypeConstraint copy = null;
		try {
			copy = (FeatureTypeConstraint) GTCloneUtil.clone(ftc); //TODO: remove temporary hack
		} catch (CloneNotSupportedException erp) {
            throw new RuntimeException(erp);
		}
		getPages().push(copy); 
	}

}
