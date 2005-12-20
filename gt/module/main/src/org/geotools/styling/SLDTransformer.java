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
 * SLDTransformer.java
 *
 * Created on October 17, 2003, 1:51 PM
 */
package org.geotools.styling;

import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterTransformer;
import org.geotools.xml.transform.TransformerBase;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import java.io.FileOutputStream;


/**
 * Producers SLD to an output stream.
 *
 * @author Ian Schneider
 */
public class SLDTransformer extends TransformerBase {
    static final String XLINK_NAMESPACE = "http://www.w3.org/1999/xlink";

    public org.geotools.xml.transform.Translator createTranslator(
        ContentHandler handler) {
        return new SLDTranslator(handler);
    }

    /**
     * Currently does nothing.
     *
     * @param args DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static final void main(String[] args) throws Exception {
        java.net.URL url = new java.io.File(args[0]).toURL();
        SLDParser s = new SLDParser(StyleFactoryFinder.createStyleFactory(), url);
        SLDTransformer transformer = new SLDTransformer();
        transformer.setIndentation(4);
        transformer.transform(s.readXML(),
            new FileOutputStream(System.getProperty("java.io.tmpdir")
                + "/junk.eraseme"));
    }

    static class SLDTranslator extends TranslatorSupport implements StyleVisitor {
        FilterTransformer.FilterTranslator filterTranslator;

        public SLDTranslator(ContentHandler handler) {
            super(handler, "sld", "http://www.opengis.net/sld");
            filterTranslator = new FilterTransformer.FilterTranslator(handler);
            addNamespaceDeclarations(filterTranslator);
        }

        void element(String element, Expression e) {
            start(element);
            filterTranslator.encode(e);
            end(element);
        }

        void element(String element, Filter f) {
            start(element);
            filterTranslator.encode(f);
            end(element);
        }

        public void visit(PointPlacement pp) {
            start("LabelPlacement");
            start("PointPlacement");
            pp.getAnchorPoint().accept(this);
            pp.getDisplacement().accept(this);
            element("Rotation", pp.getRotation());
            end("PointPlacement");
            end("LabelPlacement");
        }

        public void visit(Stroke stroke) {
            start("Stroke");

            if (stroke.getGraphicFill() != null) {
                stroke.getGraphicFill().accept(this);
            }

            if (stroke.getGraphicStroke() != null) {
                stroke.getGraphicStroke().accept(this);
            }

            encodeCssParam("stroke", stroke.getColor());
            encodeCssParam("stroke-linecap", stroke.getLineCap());
            encodeCssParam("stroke-linejoin", stroke.getLineJoin());
            encodeCssParam("stroke-opacity", stroke.getOpacity());
            encodeCssParam("stroke-width", stroke.getWidth());
            encodeCssParam("stroke-dashoffset", stroke.getDashOffset());

            float[] dash = stroke.getDashArray();

            //            if (dash != null) {
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < dash.length; i++) {
                sb.append(dash[i] + " ");
            }

            encodeCssParam("stroke-dasharray", sb.toString());

            //            }
            end("Stroke");
        }

        public void visit(LinePlacement lp) {
            start("LabelPlacement");
            start("LinePlacement");
            element("PerpendicularOffset", lp.getPerpendicularOffset());
            end("LinePlacement");
            end("LabelPlacement");
        }

        public void visit(AnchorPoint ap) {
            start("AnchorPoint");
            element("AnchorPointX", ap.getAnchorPointX());
            element("AnchorPointY", ap.getAnchorPointY());
            end("AnchorPoint");
        }

        public void visit(TextSymbolizer text) {
            if (text == null) {
                return;
            }

            start("TextSymbolizer");

            if (text.getGeometryPropertyName() != null) {
                encodeGeometryProperty(text.getGeometryPropertyName());
            }

            if (text.getLabel() != null) {
                element("Label", text.getLabel());
            }

            if ((text.getFonts() != null) && (text.getFonts().length != 0)) {
                start("Font");

                Font[] fonts = text.getFonts();

                for (int i = 0; i < fonts.length; i++) {
                    encodeCssParam("font-family", fonts[i].getFontFamily());
                }

                encodeCssParam("font-size", fonts[0].getFontSize());
                encodeCssParam("font-style", fonts[0].getFontStyle());
                encodeCssParam("font-weight", fonts[0].getFontWeight());
                end("Font");
            }

            if (text.getPlacement() != null) {
                start("LabelPlacement");
                text.getPlacement().accept(this);
                end("LabelPlacement");
            }

            if (text.getHalo() != null) {
                text.getHalo().accept(this);
            }

            if (text.getFill() != null) {
                text.getFill().accept(this);
            }

            end("TextSymbolizer");
        }

        public void visit(RasterSymbolizer raster) {
            if (raster == null) {
                return;
            }

            start("RasterSymbolizer");

            if (raster.getGeometryPropertyName() != null) {
                encodeGeometryProperty(raster.getGeometryPropertyName());
            }

            if (raster.getOpacity() != null) {
                start("Opacity");
                filterTranslator.encode(raster.getOpacity());
                end("Opacity");
            }

            if (raster.getOverlap() != null) {
                start("OverlapBehavior");
                filterTranslator.encode(raster.getOverlap());
                end("OverlapBehavior");
            }

            if (raster.getColorMap() != null) {
                // TODO
                //raster.getColorMap().getColorMapEntries()[0]
            }

            end("RasterSymbolizer");
        }

        public void visit(Symbolizer sym) {
            try {
                contentHandler.startElement("", "!--", "!--", NULL_ATTS);
                chars("Unidentified Symbolizer " + sym.getClass());
                contentHandler.endElement("", "--", "--");
            } catch (SAXException se) {
                throw new RuntimeException(se);
            }
        }

        public void visit(PolygonSymbolizer poly) {
            start("PolygonSymbolizer");
            encodeGeometryProperty(poly.getGeometryPropertyName());

            if (poly.getFill() != null) {
                poly.getFill().accept(this);
            }

            if (poly.getStroke() != null) {
                poly.getStroke().accept(this);
            }

            end("PolygonSymbolizer");
        }

        public void visit(ExternalGraphic exgr) {
            start("ExternalGraphic");
            element("Format", exgr.getFormat());

            AttributesImpl atts = new AttributesImpl();

            try {
            	atts.addAttribute("", "xlink", "xmlns:xlink", "", XLINK_NAMESPACE);
                atts.addAttribute(XLINK_NAMESPACE, "type", "xlink:type", "", "simple");
                atts.addAttribute(XLINK_NAMESPACE, "xlink", "xlink:href","", exgr.getLocation().toString());
            } catch (java.net.MalformedURLException murle) {
                throw new Error("SOMEONE CODED THE X LINK NAMESPACE WRONG!!");
            }

            element("OnlineResource", null, atts);
            end("ExternalGraphic");
        }

        public void visit(LineSymbolizer line) {
            start("LineSymbolizer");

            encodeGeometryProperty(line.getGeometryPropertyName());

            line.getStroke().accept(this);
            end("LineSymbolizer");
        }

        public void visit(Fill fill) {
            start("Fill");

            if (fill.getGraphicFill() != null) {
                fill.getGraphicFill().accept(this);
            }

            encodeCssParam("fill", fill.getColor());
            encodeCssParam("fill-opacity", fill.getOpacity());
            end("Fill");
        }

        public void visit(Rule rule) {
            start("Rule");
            element("Name", rule.getName());
            element("Abstract", rule.getAbstract());
            element("Title", rule.getTitle());

            if (rule.getMaxScaleDenominator() != Double.POSITIVE_INFINITY) {
                element("MaxScaleDenominator",
                    rule.getMaxScaleDenominator() + "");
            }

            if (rule.getMinScaleDenominator() != 0.0) {
                element("MinScaleDenominator",
                    rule.getMinScaleDenominator() + "");
            }

            if (rule.getFilter() != null) {
                filterTranslator.encode(rule.getFilter());
            }

            if (rule.hasElseFilter()) {
                start("ElseFilter");
                end("ElseFilter");
            }

            Graphic[] gr = rule.getLegendGraphic();

            for (int i = 0; i < gr.length; i++) {
                gr[i].accept(this);
            }

            Symbolizer[] sym = rule.getSymbolizers();

            for (int i = 0; i < sym.length; i++) {
                sym[i].accept(this);
            }

            end("Rule");
        }

        public void visit(Mark mark) {
            start("Mark");
            start("WellKnownName");
            filterTranslator.encode(mark.getWellKnownName());
            end("WellKnownName");

            if (mark.getFill() != null) {
                mark.getFill().accept(this);
            }

            if (mark.getStroke() != null) {
                mark.getStroke().accept(this);
            }

            end("Mark");
        }

        public void visit(PointSymbolizer ps) {
            start("PointSymbolizer");

            encodeGeometryProperty(ps.getGeometryPropertyName());

            ps.getGraphic().accept(this);
            end("PointSymbolizer");
        }

        public void visit(Halo halo) {
            start("Halo");
            halo.getFill().accept(this);
            start("Radius");
            filterTranslator.encode(halo.getRadius());
            end("Radius");
            end("Halo");
        }

        public void visit(Graphic gr) {
            start("Graphic");

            encodeGeometryProperty(gr.getGeometryPropertyName());

            element("Size", gr.getSize());
            element("Opacity", gr.getOpacity());
            element("Rotation", gr.getRotation());

            Symbol[] symbols = gr.getSymbols();

            for (int i = 0; i < symbols.length; i++) {
                symbols[i].accept(this);
            }

            end("Graphic");
        }

        public void visit(StyledLayerDescriptor sld) {
            start("StyledLayerDescriptor");

            StyledLayer[] layers = sld.getStyledLayers();

            for (int i = 0; i < layers.length; i++) {
                if (layers[i] instanceof NamedLayer) {
                    visit((NamedLayer) layers[i]);
                } else if (layers[i] instanceof UserLayer) {
                    visit((UserLayer) layers[i]);
                } else {
                    throw new IllegalArgumentException("StyledLayer '"
                        + layers[i].getClass().toString() + "' not found");
                }
            }

            end("StyledLayerDescriptor");
        }

        public void visit(NamedLayer layer) {
            start("NamedLayer");
            element("Name", layer.getName());

            FeatureTypeConstraint[] lfc = layer.getLayerFeatureConstraints();

            for (int i = 0; i < lfc.length; i++) {
                visit(lfc[i]);
            }

            Style[] styles = layer.getStyles();

            for (int i = 0; i < styles.length; i++) {
                visit(styles[i]);
            }

            end("NamedLayer");
        }

        public void visit(UserLayer layer) {
            start("UserLayer");

            if ((layer.getName() != null) && (layer.getName().length() > 0)) {
                element("Name", layer.getName()); //optional
            }

            if (layer.getRemoteOWS() != null) {
                visit(layer.getRemoteOWS());
            }

            FeatureTypeConstraint[] lfc = layer.getLayerFeatureConstraints();

            for (int i = 0; i < lfc.length; i++) {
                visit(lfc[i]);
            }

            Style[] styles = layer.getUserStyles();

            for (int i = 0; i < styles.length; i++) {
                visit(styles[i]);
            }

            end("UserLayer");
        }

        public void visit(RemoteOWS remoteOWS) {
        	start("RemoteOWS");
        	element("Service", remoteOWS.getService());
        	element("OnlineResource", remoteOWS.getOnlineResource());
        	end("RemoteOWS");
        }

        public void visit(FeatureTypeConstraint ftc) {
        	start("FeatureTypeConstraint");
        	element("FeatureTypeName", ftc.getFeatureTypeName());
        	visit(ftc.getFilter());

        	Extent[] extent = ftc.getExtents();

            for (int i = 0; i < extent.length; i++) {
                visit(extent[i]);
            }

            end("FeatureTypeConstraint");
            //TODO: implement this visitor (NamedLayer/UserLayer needs to implement FeatureTypeConstraint first)
        }

        public void visit(Extent extent) {
        	start("Extent");
        	element("Name", extent.getName());
        	element("Value", extent.getValue());
        	end("Extent");
		}

		public void visit(Filter filter) {
			// TODO: implement this visitor
		}

		public void visit(Style style) {
            start("UserStyle");
            element("Name", style.getName());
            element("Title", style.getTitle());
            element("Abstract", style.getAbstract());

            FeatureTypeStyle[] fts = style.getFeatureTypeStyles();

            for (int i = 0; i < fts.length; i++) {
                visit(fts[i]);
            }

            end("UserStyle");
        }

        public void visit(FeatureTypeStyle fts) {
            start("FeatureTypeStyle");

            if ((fts.getName() != null) && (fts.getName().length() > 0)) {
                element("Name", fts.getName());
            }

            if ((fts.getTitle() != null) && (fts.getTitle().length() > 0)) {
                element("Title", fts.getTitle());
            }

            if ((fts.getAbstract() != null) && (fts.getAbstract().length() > 0)) {
                element("Abstract", fts.getAbstract());
            }

            if ((fts.getFeatureTypeName() != null)
                    && (fts.getFeatureTypeName().length() > 0)) {
                element("FeatureTypeName", fts.getFeatureTypeName());
            }

            String[] sti = fts.getSemanticTypeIdentifiers();

            for (int i = 0; i < sti.length; i++) {
                element("SemanticTypeIndentifier", sti[i]);
            }

            Rule[] rules = fts.getRules();

            for (int i = 0; i < rules.length; i++) {
                rules[i].accept(this);
            }

            end("FeatureTypeStyle");
        }

        public void visit(Displacement dis) {
            start("Displacement");
            element("DisplacementX", dis.getDisplacementX());
            element("DisplacementY", dis.getDisplacementY());
            end("Displacement");
        }

        void encodeGeometryProperty(String name) {
            if ((name == null) || (name.trim().length() == 0)) {
                return;
            }

            start("Geometry");
            element("PropertyName", name);
            end("Geometry");
        }

        void encodeCssParam(String name, Expression expression) {
            if (expression == null) {
                return; // protect ourselves from things like a null Stroke Color
            }

            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "name", "name", "", name);
            start("CssParameter", atts);
            filterTranslator.encode(expression);
            end("CssParameter");
        }

        void encodeCssParam(String name, String expression) {
            if (expression.length() == 0) {
                return;
            }

            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "name", "name", "", name);
            start("CssParameter", atts);
            chars(expression);
            end("CssParameter");
        }

        public void encode(Style[] styles) {
            try {
                contentHandler.startDocument();

                start("StyledLayerDescriptor", NULL_ATTS);
                start("NamedLayer", NULL_ATTS); //this is correct?
                
                for (int i = 0, ii = styles.length; i < ii; i++) {
                    styles[i].accept(this);
                }

                end("NamedLayer");
                end("StyledLayerDescriptor");

                contentHandler.endDocument();
            } catch (SAXException se) {
                throw new RuntimeException(se);
            }
        }

        public void encode(StyledLayerDescriptor sld) {
            try {
                contentHandler.startDocument();
                sld.accept(this);
                contentHandler.endDocument();
            } catch (SAXException se) {
                throw new RuntimeException(se);
            }
        }

        public void encode(Object o) throws IllegalArgumentException {
            if (o instanceof StyledLayerDescriptor) {
                encode((StyledLayerDescriptor) o);
            } else if (o instanceof Style[]) {
                encode((Style[]) o);
            } else {
                Class c = o.getClass();

                try {
                    java.lang.reflect.Method m = c.getMethod("accept",
                            new Class[] { StyleVisitor.class });
                    m.invoke(o, new Object[] { this });
                } catch (NoSuchMethodException nsme) {
                    throw new IllegalArgumentException("Cannot encode " + o);
                } catch (Exception e) {
                    throw new RuntimeException("Internal transformation exception",
                        e);
                }
            }
        }
    }
}
