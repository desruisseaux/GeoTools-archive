package org.geotools.xml.styling;

/**
 * This code generated using Refractions SchemaCodeGenerator For more information, view the attached
 * licensing information. CopyRight 105
 */

import java.util.ArrayList;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.geotools.filter.Expression;
import org.geotools.styling.ChannelSelection;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ContrastEnhancement;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.Fill;
import org.geotools.styling.Font;
import org.geotools.styling.Graphic;
import org.geotools.styling.Halo;
import org.geotools.styling.LabelPlacement;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.ShadedRelief;
import org.geotools.styling.Stroke;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.geotools.xml.PrintHandler;
import org.geotools.xml.schema.Attribute;
import org.geotools.xml.schema.ComplexType;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.ElementGrouping;
import org.geotools.xml.schema.ElementValue;
import org.geotools.xml.schema.impl.AttributeGT;
import org.geotools.xml.schema.impl.ChoiceGT;
import org.geotools.xml.schema.impl.SequenceGT;
import org.geotools.xml.xLink.XLinkSchema;
import org.xml.sax.Attributes;

public class sldComplexTypes {

    protected static class _Normalize extends sldComplexType {
        private static ComplexType instance = new _Normalize();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = null;
        private static ElementGrouping child = new SequenceGT(null);

        private _Normalize() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _Histogram extends sldComplexType {
        private static ComplexType instance = new _Histogram();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = null;
        private static ElementGrouping child = new SequenceGT(null);

        private _Histogram() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _ContrastEnhancement extends sldComplexType {
        private static ComplexType instance = new _ContrastEnhancement();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("Normalize", sldComplexTypes._Normalize.getInstance(), null, 1, 1),
                new sldElement("Histogram", sldComplexTypes._Histogram.getInstance(), null, 1, 1),
                new sldElement("GammaValue", org.geotools.xml.xsi.XSISimpleTypes.Double
                        .getInstance()/* simpleType name is double */, null, 0, 1)};

        private static ElementGrouping child = new SequenceGT(null, new ElementGrouping[]{
                new ChoiceGT(null, 0, 1, new ElementGrouping[]{elems[0], elems[1]}),
                new sldElement("GammaValue", org.geotools.xml.xsi.XSISimpleTypes.Double
                        .getInstance()/* simpleType name is double */, null, 0, 1)}, 1, 1);

        private _ContrastEnhancement() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class SelectedChannelType extends sldComplexType {
        private static ComplexType instance = new SelectedChannelType();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("SourceChannelName", org.geotools.xml.xsi.XSISimpleTypes.String
                        .getInstance()/* simpleType name is string */, null, 1, 1),
                new sldElement("ContrastEnhancement", sldComplexTypes._ContrastEnhancement
                        .getInstance(), null, 0, 1)};

        private static ElementGrouping child = new SequenceGT(null, new ElementGrouping[]{
                new sldElement("SourceChannelName", org.geotools.xml.xsi.XSISimpleTypes.String
                        .getInstance()/*
                                         * simpleType name is string
                                         */, null, 1, 1),
                new sldElement("ContrastEnhancement", sldComplexTypes._ContrastEnhancement
                        .getInstance(), null, 0, 1)}, 1, 1);

        private SelectedChannelType() {
            super("SelectedChannelType", child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class ParameterValueType extends sldComplexType {
        private static ComplexType instance = new ParameterValueType();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{new sldElement("expression",
                org.geotools.xml.filter.FilterComplexTypes.ExpressionType.getInstance(), null, 1, 1)};

        private static ElementGrouping child = new SequenceGT(null,
                new ElementGrouping[]{new sldElement("expression",
                        org.geotools.xml.filter.FilterComplexTypes.ExpressionType.getInstance(), null,
                        1, 1)}, 0, 2147483647);

        private ParameterValueType() {
            super("ParameterValueType", child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class SymbolizerType extends sldComplexType {
        private static ComplexType instance = new SymbolizerType();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = null;
        private static ElementGrouping child = new SequenceGT(null);

        private SymbolizerType() {
            super("SymbolizerType", child, attrs, elems, null, true, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _Displacement extends sldComplexType {
        private static ComplexType instance = new _Displacement();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("DisplacementX", sldComplexTypes.ParameterValueType.getInstance(),
                        null, 1, 1),
                new sldElement("DisplacementY", sldComplexTypes.ParameterValueType.getInstance(),
                        null, 1, 1)};

        private static ElementGrouping child = new SequenceGT(null, new ElementGrouping[]{
                new sldElement("DisplacementX", sldComplexTypes.ParameterValueType.getInstance(),
                        null, 1, 1),
                new sldElement("DisplacementY", sldComplexTypes.ParameterValueType.getInstance(),
                        null, 1, 1)}, 1, 1);

        private _Displacement() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _Geometry extends sldComplexType {
        private static ComplexType instance = new _Geometry();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{new sldElement("PropertyName",
                org.geotools.xml.filter.FilterComplexTypes.PropertyNameType.getInstance(), null, 1, 1)};
        
        // array positions
        private static int PROPERTYNAME = 0;

        private static ElementGrouping child = new SequenceGT(elems);

        private _Geometry() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
//            Geometry symbol = StyleFactory.createStyleFactory().getDefaultRasterSymbolizer();
            // symbol.setGraphic(null);

//            for (int i = 0; i < value.length; i++) {
//                if ((value[i] == null) || value[i].getElement() == null) {
//                    continue;
//                }
//
//                Element e = value[i].getElement();
//                if(elems[PROPERTYNAME].getName().equals(e.getName()))
//                    symbol.setGeometryPropertyName((String)value[i].getValue());
//            }
            
//            return symbol;
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _OnlineResource extends sldComplexType {
        private static ComplexType instance = new _OnlineResource();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = XLinkSchema.SimpleLink.getInstance().getAttributes();

        private static Element[] elems = null;
        private static ElementGrouping child = new SequenceGT(null);

        private _OnlineResource() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _ExternalGraphic extends sldComplexType {
        private static ComplexType instance = new _ExternalGraphic();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("OnlineResource", sldComplexTypes._OnlineResource.getInstance(),
                        null, 1, 1),
                new sldElement("Format", org.geotools.xml.xsi.XSISimpleTypes.String.getInstance(), null, 1, 1)};

        private static ElementGrouping child = new SequenceGT(null,
                new ElementGrouping[]{
                        new sldElement("OnlineResource", sldComplexTypes._OnlineResource
                                .getInstance(), null, 1, 1),
                        new sldElement("Format", org.geotools.xml.xsi.XSISimpleTypes.String
                                .getInstance()/* simpleType name is string */, null, 1, 1)}, 1, 1);

        private _ExternalGraphic() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _GraphicStroke extends sldComplexType {
        private static ComplexType instance = new _GraphicStroke();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{new sldElement("Graphic", null, null, 1, 1)};

        private static ElementGrouping child = new SequenceGT(null,
                new ElementGrouping[]{new sldElement("Graphic", null, null, 1, 1)}, 1, 1);

        private _GraphicStroke() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _CssParameter extends sldComplexType {
        private static ComplexType instance = new _CssParameter();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = new Attribute[]{new AttributeGT(null, "name",
                sldSchema.NAMESPACE, org.geotools.xml.xsi.XSISimpleTypes.String.getInstance(),
                Attribute.REQUIRED, null, null, false)};

        private static Element[] elems = new Element[]{new sldElement("expression",
                org.geotools.xml.filter.FilterComplexTypes.ExpressionType.getInstance(), null, 1, 1)};

        private static ElementGrouping child = new SequenceGT(null,
                new ElementGrouping[]{new sldElement("expression",
                        org.geotools.xml.filter.FilterComplexTypes.ExpressionType.getInstance(), null, 1, 1)}, 0, 2147483647);

        private _CssParameter() {
            super(null, child, attrs, elems, sldComplexTypes.ParameterValueType.getInstance(),
                    false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _Stroke extends sldComplexType {
        private static ComplexType instance = new _Stroke();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("GraphicFill", null, null, 1, 1),
                new sldElement("GraphicStroke", sldComplexTypes._GraphicStroke.getInstance(), null,
                        1, 1),
                new sldElement("CssParameter", sldComplexTypes._CssParameter.getInstance(), null,
                        0, 2147483647)};

        private static ElementGrouping child = new SequenceGT(
                new ElementGrouping[]{
                        new ChoiceGT(null, 0, 1, new ElementGrouping[]{
                                elems[0],
                                elems[1],
                        }),
                        elems[2]
                });

        private _Stroke() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _Mark extends sldComplexType {
        private static ComplexType instance = new _Mark();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("WellKnownName", org.geotools.xml.xsi.XSISimpleTypes.String
                        .getInstance()/* simpleType name is string */, null, 0, 1),
                new sldElement("Fill", null, null, 0, 1),
                new sldElement("Stroke", sldComplexTypes._Stroke.getInstance(), null, 0, 1)};

        private static ElementGrouping child = new SequenceGT(null,
                new ElementGrouping[]{
                        new sldElement("WellKnownName", org.geotools.xml.xsi.XSISimpleTypes.String
                                .getInstance()/* simpleType name is string */, null, 0, 1),
                        new sldElement("Fill", null, null, 0, 1),
                        new sldElement("Fill", null, null, 0, 1)}, 1, 1);

        private _Mark() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _Graphic extends sldComplexType {
        private static ComplexType instance = new _Graphic();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("ExternalGraphic", sldComplexTypes._ExternalGraphic.getInstance(),
                        null, 1, 1),
                new sldElement("Mark", sldComplexTypes._Mark.getInstance(), null, 1, 1),
                new sldElement("Opacity", sldComplexTypes.ParameterValueType.getInstance(), null,
                        0, 1),
                new sldElement("Size", sldComplexTypes.ParameterValueType.getInstance(), null, 0, 1),
                new sldElement("Rotation", sldComplexTypes.ParameterValueType.getInstance(), null,
                        0, 1)};

        // array positions
        private static int EXTERNALGRAPHIC = 0;
        private static int MARK = 0;
        private static int OPACITY = 0;
        private static int SIZE = 0;
        private static int ROTATION = 0;
        
        private static ElementGrouping child = new SequenceGT(null, new ElementGrouping[]{
                new ChoiceGT(null, 0, 2147483647, new ElementGrouping[]{
                        new sldElement("ExternalGraphic", sldComplexTypes._ExternalGraphic
                                .getInstance(), null, 1, 1),
                        new sldElement("Mark", sldComplexTypes._Mark.getInstance(), null, 1, 1)}),
                new SequenceGT(null, new ElementGrouping[]{
                        new sldElement("Opacity", sldComplexTypes.ParameterValueType.getInstance(),
                                null, 0, 1),
                        new sldElement("Size", sldComplexTypes.ParameterValueType.getInstance(),
                                null, 0, 1),
                        new sldElement("Rotation", sldComplexTypes.ParameterValueType.getInstance(),
                                null, 0, 1)}, 1, 1)}, 1, 1);

        private _Graphic() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
          Graphic symbol = StyleFactory.createStyleFactory().getDefaultGraphic();

            for (int i = 0; i < value.length; i++) {
                if ((value[i] == null) || value[i].getElement() == null) {
                    continue;
                }

                Element e = value[i].getElement();
                if(elems[EXTERNALGRAPHIC].getName().equals(e.getName()))
                    symbol.addExternalGraphic((ExternalGraphic)value[i].getValue());

                if(elems[MARK].getName().equals(e.getName()))
                    symbol.addMark((Mark)value[i].getValue());

                if(elems[OPACITY].getName().equals(e.getName()))
                    symbol.setOpacity((Expression)value[i].getValue());

                if(elems[SIZE].getName().equals(e.getName()))
                    symbol.setSize((Expression)value[i].getValue());

                if(elems[ROTATION].getName().equals(e.getName()))
                    symbol.setRotation((Expression)value[i].getValue());
            }
            
            return symbol;
        }
    }
    protected static class _GraphicFill extends sldComplexType {
        private static ComplexType instance = new _GraphicFill();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{new sldElement("Graphic",
                sldComplexTypes._Graphic.getInstance(), null, 1, 1)};

        private static ElementGrouping child = new SequenceGT(null,
                new ElementGrouping[]{new sldElement("Graphic", sldComplexTypes._Graphic
                        .getInstance(), null, 1, 1)}, 1, 1);

        private _GraphicFill() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _Fill extends sldComplexType {
        private static ComplexType instance = new _Fill();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("GraphicFill", sldComplexTypes._GraphicFill.getInstance(), null, 0,
                        1),
                new sldElement("CssParameter", sldComplexTypes._CssParameter.getInstance(), null,
                        0, 2147483647)};

        private static ElementGrouping child = new SequenceGT(null, new ElementGrouping[]{
                new sldElement("GraphicFill", sldComplexTypes._GraphicFill.getInstance(), null, 0,
                        1),
                new sldElement("CssParameter", sldComplexTypes._CssParameter.getInstance(), null,
                        0, 2147483647)}, 1, 1);

        private _Fill() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _PolygonSymbolizer extends sldComplexType {
        private static ComplexType instance = new _PolygonSymbolizer();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("Geometry", sldComplexTypes._Geometry.getInstance(), null, 0, 1),
                new sldElement("Fill", sldComplexTypes._Fill.getInstance(), null, 0, 1),
                new sldElement("Stroke", sldComplexTypes._Stroke.getInstance(), null, 0, 1)};
        
        // array positions
        private static int GEOMETRY = 0;
        private static int FILL = 1;
        private static int STROKE = 2;

        private static ElementGrouping child = new SequenceGT(elems);

        private _PolygonSymbolizer() {
            super(null, child, attrs, elems, sldComplexTypes.SymbolizerType.getInstance(), false,
                    false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            PolygonSymbolizer symbol = StyleFactory.createStyleFactory().getDefaultPolygonSymbolizer();
            // symbol.setGraphic(null);

            for (int i = 0; i < value.length; i++) {
                if ((value[i] == null) || value[i].getElement() == null) {
                    continue;
                }

                Element e = value[i].getElement();
                if(elems[GEOMETRY].getName().equals(e.getName()))
                    symbol.setGeometryPropertyName((String)value[i].getValue());

                if(elems[FILL].getName().equals(e.getName()))
                    symbol.setFill((Fill)value[i].getValue());

                if(elems[STROKE].getName().equals(e.getName()))
                    symbol.setStroke((Stroke)value[i].getValue());
            }
            
            return symbol;
        }
    }
    protected static class _ColorMapEntry extends sldComplexType {
        private static ComplexType instance = new _ColorMapEntry();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = new Attribute[]{
                new AttributeGT(null, "color", sldSchema.NAMESPACE, 
                        org.geotools.xml.xsi.XSISimpleTypes.String.getInstance(), 
                        Attribute.REQUIRED, null, null, false),
                new AttributeGT(null, "opacity", sldSchema.NAMESPACE, 
                        org.geotools.xml.xsi.XSISimpleTypes.Double.getInstance(), 
                        -1, null, null, false),
                new AttributeGT(null, "quantity", sldSchema.NAMESPACE, 
                        org.geotools.xml.xsi.XSISimpleTypes.Double.getInstance(), 
                        -1, null, null, false),
                new AttributeGT(null, "label", sldSchema.NAMESPACE,
                        org.geotools.xml.xsi.XSISimpleTypes.String.getInstance(), 
                        -1, null, null, false)
                };

        private static Element[] elems = null;
        private static ElementGrouping child = new SequenceGT(null);

        private _ColorMapEntry() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _LATEST_ON_TOP extends sldComplexType {
        private static ComplexType instance = new _LATEST_ON_TOP();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = null;
        private static ElementGrouping child = new SequenceGT(null);

        private _LATEST_ON_TOP() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _Extent extends sldComplexType {
        private static ComplexType instance = new _Extent();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("Name", org.geotools.xml.xsi.XSISimpleTypes.String.getInstance(), null, 1, 1),
                new sldElement("Value", org.geotools.xml.xsi.XSISimpleTypes.String.getInstance(), null, 1, 1)};

        private static ElementGrouping child = new SequenceGT(null,
                new ElementGrouping[]{
                        new sldElement("Name", org.geotools.xml.xsi.XSISimpleTypes.String
                                .getInstance()/* simpleType name is string */, null, 1, 1),
                        new sldElement("Value", org.geotools.xml.xsi.XSISimpleTypes.String
                                .getInstance()/* simpleType name is string */, null, 1, 1)}, 1, 1);

        private _Extent() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _FeatureTypeConstraint extends sldComplexType {
        private static ComplexType instance = new _FeatureTypeConstraint();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("FeatureTypeName", org.geotools.xml.xsi.XSISimpleTypes.String
                        .getInstance()/* simpleType name is string */, null, 0, 1),
                new sldElement("Filter", org.geotools.xml.filter.FilterOpsComplexTypes.FilterType
                        .getInstance()/* complexType name is FilterType */, null, 0, 1),
                new sldElement("Extent", sldComplexTypes._Extent.getInstance(), null, 0, 2147483647)};

        private static ElementGrouping child = new SequenceGT(elems);

        private _FeatureTypeConstraint() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _LayerFeatureConstraints extends sldComplexType {
        private static ComplexType instance = new _LayerFeatureConstraints();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{new sldElement("FeatureTypeConstraint",
                sldComplexTypes._FeatureTypeConstraint.getInstance(), null, 1, 2147483647)};

        private static ElementGrouping child = new SequenceGT(
                null,
                new ElementGrouping[]{new sldElement("FeatureTypeConstraint",
                        sldComplexTypes._FeatureTypeConstraint.getInstance(), null, 1, 2147483647)},
                1, 1);

        private _LayerFeatureConstraints() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _LineSymbolizer extends sldComplexType {
        private static ComplexType instance = new _LineSymbolizer();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("Geometry", sldComplexTypes._Geometry.getInstance(), null, 0, 1),
                new sldElement("Stroke", sldComplexTypes._Stroke.getInstance(), null, 0, 1)};
        
        // array positions
        private static int GEOMETRY = 0;
        private static int STROKE = 1;

        private static ElementGrouping child = new SequenceGT(elems);

        private _LineSymbolizer() {
            super(null, child, attrs, elems, sldComplexTypes.SymbolizerType.getInstance(), false,
                    false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            LineSymbolizer symbol = StyleFactory.createStyleFactory().getDefaultLineSymbolizer();
            // symbol.setGraphic(null);

            for (int i = 0; i < value.length; i++) {
                if ((value[i] == null) || value[i].getElement() == null) {
                    continue;
                }

                Element e = value[i].getElement();
                if(elems[GEOMETRY].getName().equals(e.getName()))
                    symbol.setGeometryPropertyName((String)value[i].getValue());

                if(elems[STROKE].getName().equals(e.getName()))
                    symbol.setStroke((Stroke)value[i].getValue());
            }
            
            return symbol;
        }
    }
    protected static class _PointSymbolizer extends sldComplexType {
        private static ComplexType instance = new _PointSymbolizer();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("Geometry", sldComplexTypes._Geometry.getInstance(), null, 0, 1),
                new sldElement("Graphic", sldComplexTypes._Graphic.getInstance(), null, 0, 1)};
        
        // array positions
        private static int GEOMETRY = 0;
        private static int GRAPHIC = 1;
        
        private static ElementGrouping child = new SequenceGT(elems);

        private _PointSymbolizer() {
            super(null, child, attrs, elems, sldComplexTypes.SymbolizerType.getInstance(), false,
                    false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            PointSymbolizer symbol = StyleFactory.createStyleFactory().getDefaultPointSymbolizer();
            // symbol.setGraphic(null);

            for (int i = 0; i < value.length; i++) {
                if ((value[i] == null) || value[i].getElement() == null) {
                    continue;
                }

                Element e = value[i].getElement();
                if(elems[GEOMETRY].getName().equals(e.getName()))
                    symbol.setGeometryPropertyName((String)value[i].getValue());

                if(elems[GRAPHIC].getName().equals(e.getName()))
                    symbol.setGraphic((Graphic)value[i].getValue());
            }
            
            return symbol;
        }
    }
    protected static class _ChannelSelection extends sldComplexType {
        private static ComplexType instance = new _ChannelSelection();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("RedChannel", sldComplexTypes.SelectedChannelType.getInstance(),
                        null, 1, 1),
                new sldElement("GreenChannel", sldComplexTypes.SelectedChannelType.getInstance(),
                        null, 1, 1),
                new sldElement("BlueChannel", sldComplexTypes.SelectedChannelType.getInstance(),
                        null, 1, 1),
                new sldElement("GrayChannel", sldComplexTypes.SelectedChannelType.getInstance(),
                        null, 1, 1)};

        private static ElementGrouping child = new ChoiceGT(null, 1, 1, new ElementGrouping[]{
                new SequenceGT(null, new ElementGrouping[]{
                        new sldElement("RedChannel", sldComplexTypes.SelectedChannelType
                                .getInstance(), null, 1, 1),
                        new sldElement("GreenChannel", sldComplexTypes.SelectedChannelType
                                .getInstance(), null, 1, 1),
                        new sldElement("GreenChannel", sldComplexTypes.SelectedChannelType
                                .getInstance(), null, 1, 1)}, 1, 1),
                new sldElement("GrayChannel", sldComplexTypes.SelectedChannelType.getInstance(),
                        null, 1, 1)});

        private _ChannelSelection() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _RANDOM extends sldComplexType {
        private static ComplexType instance = new _RANDOM();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = null;
        private static ElementGrouping child = new SequenceGT(null);

        private _RANDOM() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _Font extends sldComplexType {
        private static ComplexType instance = new _Font();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{new sldElement("CssParameter",
                sldComplexTypes._CssParameter.getInstance(), null, 0, 2147483647)};

        private static ElementGrouping child = new SequenceGT(null,
                new ElementGrouping[]{new sldElement("CssParameter", sldComplexTypes._CssParameter
                        .getInstance(), null, 0, 2147483647)}, 1, 1);

        private _Font() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _LegendGraphic extends sldComplexType {
        private static ComplexType instance = new _LegendGraphic();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{new sldElement("Graphic",
                sldComplexTypes._Graphic.getInstance(), null, 1, 1)};

        private static ElementGrouping child = new SequenceGT(null,
                new ElementGrouping[]{new sldElement("Graphic", sldComplexTypes._Graphic
                        .getInstance(), null, 1, 1)}, 1, 1);

        private _LegendGraphic() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _ElseFilter extends sldComplexType {
        private static ComplexType instance = new _ElseFilter();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = null;
        private static ElementGrouping child = new SequenceGT(null);

        private _ElseFilter() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _Rule extends sldComplexType {
        private static ComplexType instance = new _Rule();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("Name", org.geotools.xml.xsi.XSISimpleTypes.String.getInstance(), null, 0, 1),
                new sldElement("Title", org.geotools.xml.xsi.XSISimpleTypes.String.getInstance(), null, 0, 1),
                new sldElement("Abstract", org.geotools.xml.xsi.XSISimpleTypes.String.getInstance(), null, 0, 1),
                new sldElement("LegendGraphic", sldComplexTypes._LegendGraphic.getInstance(), null,
                        0, 1),
                new sldElement("Filter", org.geotools.xml.filter.FilterOpsComplexTypes.FilterType
                        .getInstance()/* complexType name is FilterType */, null, 1, 1),
                new sldElement("ElseFilter", sldComplexTypes._ElseFilter.getInstance(), null, 1, 1),
                new sldElement("MinScaleDenominator", org.geotools.xml.xsi.XSISimpleTypes.Double
                        .getInstance()/* simpleType name is double */, null, 0, 1),
                new sldElement("MaxScaleDenominator", org.geotools.xml.xsi.XSISimpleTypes.Double
                        .getInstance()/* simpleType name is double */, null, 0, 1),
                new sldElement("Symbolizer", sldComplexTypes.SymbolizerType.getInstance(), null, 1,
                        2147483647)};

        private static ElementGrouping child = new SequenceGT(
                new ElementGrouping[]{
                        elems[0],
                        elems[1],
                        elems[2],
                        elems[3],
                        new ChoiceGT(new ElementGrouping[] { elems[4], elems[5] }),
                        elems[6],
                        elems[7],
                        elems[8]
                }
            );

        private _Rule() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _FeatureTypeStyle extends sldComplexType {
        private static ComplexType instance = new _FeatureTypeStyle();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("Name", org.geotools.xml.xsi.XSISimpleTypes.String.getInstance(), null, 0, 1),
                new sldElement("Title", org.geotools.xml.xsi.XSISimpleTypes.String.getInstance(), null, 0, 1),
                new sldElement("Abstract", org.geotools.xml.xsi.XSISimpleTypes.String.getInstance(), null, 0, 1),
                new sldElement("FeatureTypeName", org.geotools.xml.xsi.XSISimpleTypes.String
                        .getInstance()/* simpleType name is string */, null, 0, 1),
                new sldElement("SemanticTypeIdentifier", org.geotools.xml.xsi.XSISimpleTypes.String
                        .getInstance()/* simpleType name is string */, null, 0, 2147483647),
                new sldElement("Rule", sldComplexTypes._Rule.getInstance(), null, 1, 2147483647)};

        private static ElementGrouping child = new SequenceGT(elems);

        private _FeatureTypeStyle() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _UserStyle extends sldComplexType {
        private static ComplexType instance = new _UserStyle();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("Name", org.geotools.xml.xsi.XSISimpleTypes.String.getInstance(), null, 0, 1),
                new sldElement("Title", org.geotools.xml.xsi.XSISimpleTypes.String.getInstance(), null, 0, 1),
                new sldElement("Abstract", org.geotools.xml.xsi.XSISimpleTypes.String.getInstance(), null, 0, 1),
                new sldElement("IsDefault", org.geotools.xml.xsi.XSISimpleTypes.Boolean
                        .getInstance()/* simpleType name is boolean */, null, 0, 1),
                new sldElement("FeatureTypeStyle", sldComplexTypes._FeatureTypeStyle.getInstance(),
                        null, 1, 2147483647)};

        private static ElementGrouping child = new SequenceGT(elems);

        private _UserStyle() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _AnchorPoint extends sldComplexType {
        private static ComplexType instance = new _AnchorPoint();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("AnchorPointX", sldComplexTypes.ParameterValueType.getInstance(),
                        null, 1, 1),
                new sldElement("AnchorPointY", sldComplexTypes.ParameterValueType.getInstance(),
                        null, 1, 1)};

        private static ElementGrouping child = new SequenceGT(null, new ElementGrouping[]{
                new sldElement("AnchorPointX", sldComplexTypes.ParameterValueType.getInstance(),
                        null, 1, 1),
                new sldElement("AnchorPointY", sldComplexTypes.ParameterValueType.getInstance(),
                        null, 1, 1)}, 1, 1);

        private _AnchorPoint() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _PointPlacement extends sldComplexType {
        private static ComplexType instance = new _PointPlacement();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("AnchorPoint", sldComplexTypes._AnchorPoint.getInstance(), null, 0,
                        1),
                new sldElement("Displacement", sldComplexTypes._Displacement.getInstance(), null,
                        0, 1),
                new sldElement("Rotation", sldComplexTypes.ParameterValueType.getInstance(), null,
                        0, 1)};

        private static ElementGrouping child = new SequenceGT(null, new ElementGrouping[]{
                new sldElement("AnchorPoint", sldComplexTypes._AnchorPoint.getInstance(), null, 0,
                        1),
                new sldElement("Displacement", sldComplexTypes._Displacement.getInstance(), null,
                        0, 1),
                new sldElement("Displacement", sldComplexTypes._Displacement.getInstance(), null,
                        0, 1)}, 1, 1);

        private _PointPlacement() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _EARLIEST_ON_TOP extends sldComplexType {
        private static ComplexType instance = new _EARLIEST_ON_TOP();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = null;
        private static ElementGrouping child = new SequenceGT(null);

        private _EARLIEST_ON_TOP() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _NamedStyle extends sldComplexType {
        private static ComplexType instance = new _NamedStyle();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{new sldElement(
                "Name",
                org.geotools.xml.xsi.XSISimpleTypes.String.getInstance()/* simpleType name is string */,
                null, 1, 1)};

        private static ElementGrouping child = new SequenceGT(null,
                new ElementGrouping[]{new sldElement("Name",
                        org.geotools.xml.xsi.XSISimpleTypes.String.getInstance(), null, 1, 1)}, 1, 1);

        private _NamedStyle() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _NamedLayer extends sldComplexType {
        private static ComplexType instance = new _NamedLayer();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("Name", org.geotools.xml.xsi.XSISimpleTypes.String.getInstance(), null, 1, 1),
                new sldElement("LayerFeatureConstraints", sldComplexTypes._LayerFeatureConstraints
                        .getInstance(), null, 0, 1),
                new sldElement("NamedStyle", sldComplexTypes._NamedStyle.getInstance(), null, 1, 1),
                new sldElement("UserStyle", sldComplexTypes._UserStyle.getInstance(), null, 1, 1)};

        private static ElementGrouping child = new SequenceGT(
                null,
                new ElementGrouping[]{elems[0], elems[1],
                        new ChoiceGT(null, 0, Integer.MAX_VALUE, new ElementGrouping[] { elems[2], elems[3] })},
                1, 1);

        private _NamedLayer() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _RemoteOWS extends sldComplexType {
        private static ComplexType instance = new _RemoteOWS();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("Service", sldSimpleTypes._Service.getInstance(), null, 1, 1),
                new sldElement("OnlineResource", sldComplexTypes._OnlineResource.getInstance(),
                        null, 1, 1)};

        private static ElementGrouping child = new SequenceGT(null, new ElementGrouping[]{
                new sldElement("Service", sldSimpleTypes._Service.getInstance(), null, 1, 1),
                new sldElement("OnlineResource", sldComplexTypes._OnlineResource.getInstance(),
                        null, 1, 1)}, 1, 1);

        private _RemoteOWS() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _UserLayer extends sldComplexType {
        private static ComplexType instance = new _UserLayer();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("Name", org.geotools.xml.xsi.XSISimpleTypes.String.getInstance(), null, 0, 1),
                new sldElement("RemoteOWS", sldComplexTypes._RemoteOWS.getInstance(), null, 0, 1),
                new sldElement("LayerFeatureConstraints", sldComplexTypes._LayerFeatureConstraints
                        .getInstance(), null, 1, 1),
                new sldElement("UserStyle", sldComplexTypes._UserStyle.getInstance(), null, 1,
                        2147483647)};

        private static ElementGrouping child = new SequenceGT(null,
                elems, 1, 1);

        private _UserLayer() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _StyledLayerDescriptor extends sldComplexType {
        private static ComplexType instance = new _StyledLayerDescriptor();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = new Attribute[]{new AttributeGT(null, "version",
                sldSchema.NAMESPACE, org.geotools.xml.xsi.XSISimpleTypes.String.getInstance(),
                Attribute.REQUIRED, null, null, false)};

        private static Element[] elems = new Element[]{
                new sldElement("Name", org.geotools.xml.xsi.XSISimpleTypes.String.getInstance(),
                        null, 0, 1),
                new sldElement("Title", org.geotools.xml.xsi.XSISimpleTypes.String.getInstance(),
                        null, 0, 1),
                new sldElement("Abstract",
                        org.geotools.xml.xsi.XSISimpleTypes.String.getInstance(), null, 0, 1),
                new sldElement("NamedLayer", sldComplexTypes._NamedLayer.getInstance(), null, 0, Integer.MAX_VALUE),
                new sldElement("UserLayer", sldComplexTypes._UserLayer.getInstance(), null, 0, Integer.MAX_VALUE)};

        private static ElementGrouping child = new SequenceGT(
                new ElementGrouping[]{
                        elems[0],
                        elems[1],
                        elems[2],
                        new ChoiceGT(null, 0, Integer.MAX_VALUE, new ElementGrouping[] { elems[3], elems[4] })
                });

        private _StyledLayerDescriptor() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _LinePlacement extends sldComplexType {
        private static ComplexType instance = new _LinePlacement();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{new sldElement("PerpendicularOffset",
                sldComplexTypes.ParameterValueType.getInstance(), null, 0, 1)};

        private static ElementGrouping child = new SequenceGT(null,
                new ElementGrouping[]{new sldElement("PerpendicularOffset",
                        sldComplexTypes.ParameterValueType.getInstance(), null, 0, 1)}, 1, 1);

        private _LinePlacement() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _LabelPlacement extends sldComplexType {
        private static ComplexType instance = new _LabelPlacement();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("PointPlacement", sldComplexTypes._PointPlacement.getInstance(),
                        null, 1, 1),
                new sldElement("LinePlacement", sldComplexTypes._LinePlacement.getInstance(), null,
                        1, 1)};

        private static ElementGrouping child = new ChoiceGT(null, 1, 1, new ElementGrouping[]{
                new sldElement("PointPlacement", sldComplexTypes._PointPlacement.getInstance(),
                        null, 1, 1),
                new sldElement("LinePlacement", sldComplexTypes._LinePlacement.getInstance(), null,
                        1, 1)});

        private _LabelPlacement() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _Halo extends sldComplexType {
        private static ComplexType instance = new _Halo();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("Radius", sldComplexTypes.ParameterValueType.getInstance(), null, 0,
                        1), new sldElement("Fill", sldComplexTypes._Fill.getInstance(), null, 0, 1)};

        private static ElementGrouping child = new SequenceGT(null,
                new ElementGrouping[]{
                        new sldElement("Radius", sldComplexTypes.ParameterValueType.getInstance(),
                                null, 0, 1),
                        new sldElement("Fill", sldComplexTypes._Fill.getInstance(), null, 0, 1)},
                1, 1);

        private _Halo() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _TextSymbolizer extends sldComplexType {
        private static ComplexType instance = new _TextSymbolizer();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("Geometry", sldComplexTypes._Geometry.getInstance(), null, 0, 1),
                new sldElement("Label", sldComplexTypes.ParameterValueType.getInstance(), null, 0,
                        1),
                new sldElement("Font", sldComplexTypes._Font.getInstance(), null, 0, 1),
                new sldElement("LabelPlacement", sldComplexTypes._LabelPlacement.getInstance(),
                        null, 0, 1),
                new sldElement("Halo", sldComplexTypes._Halo.getInstance(), null, 0, 1),
                new sldElement("Fill", sldComplexTypes._Fill.getInstance(), null, 0, 1)};

        // array positions
        private static int GEOMETRY = 0;
        private static int LABEL = 1;
        private static int FONT = 2;
        private static int LABELPLACEMENT = 3;
        private static int HALO = 4;
        private static int FILL = 5;
        
        private static ElementGrouping child = new SequenceGT(elems);

        private _TextSymbolizer() {
            super(null, child, attrs, elems, sldComplexTypes.SymbolizerType.getInstance(), false,
                    false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            TextSymbolizer symbol = StyleFactory.createStyleFactory().createTextSymbolizer();
            symbol.setFill(null);
            
            ArrayList fonts = new ArrayList();
            
            for (int i = 0; i < value.length; i++) {
                if ((value[i] == null) || value[i].getElement() == null) {
                    continue;
                }
                Element e = value[i].getElement();
                if(elems[GEOMETRY].getName().equals(e.getName()))
                    symbol.setGeometryPropertyName((String)value[i].getValue());

                if(elems[FILL].getName().equals(e.getName()))
                    symbol.setFill((Fill)value[i].getValue());

                if(elems[LABEL].getName().equals(e.getName()))
                    symbol.setLabel((Expression)value[i].getValue());

                if(elems[FONT].getName().equals(e.getName()))
                    fonts.add(value[i].getValue());

                if(elems[LABELPLACEMENT].getName().equals(e.getName()))
                    symbol.setFill((Fill)value[i].getValue());

                if(elems[LABELPLACEMENT].getName().equals(e.getName()))
                    symbol.setLabelPlacement((LabelPlacement)value[i].getValue());

                if(elems[HALO].getName().equals(e.getName()))
                    symbol.setHalo((Halo)value[i].getValue());
            }
            
            symbol.setFonts((Font[]) fonts.toArray(new Font[0]));
            
            return symbol;
        }
    }
    protected static class _AVERAGE extends sldComplexType {
        private static ComplexType instance = new _AVERAGE();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = null;
        private static ElementGrouping child = new SequenceGT(null);

        private _AVERAGE() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _OverlapBehavior extends sldComplexType {
        private static ComplexType instance = new _OverlapBehavior();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("LATEST_ON_TOP", sldComplexTypes._LATEST_ON_TOP.getInstance(), null,
                        1, 1),
                new sldElement("EARLIEST_ON_TOP", sldComplexTypes._EARLIEST_ON_TOP.getInstance(),
                        null, 1, 1),
                new sldElement("AVERAGE", sldComplexTypes._AVERAGE.getInstance(), null, 1, 1),
                new sldElement("RANDOM", sldComplexTypes._RANDOM.getInstance(), null, 1, 1)};

        private static ElementGrouping child = new ChoiceGT(elems);

        private _OverlapBehavior() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _ImageOutline extends sldComplexType {
        private static ComplexType instance = new _ImageOutline();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("LineSymbolizer", sldComplexTypes._LineSymbolizer.getInstance(),
                        null, 1, 1),
                new sldElement("PolygonSymbolizer", sldComplexTypes._PolygonSymbolizer
                        .getInstance(), null, 1, 1)};

        private static ElementGrouping child = new ChoiceGT(null, 1, 1, new ElementGrouping[]{
                new sldElement("LineSymbolizer", sldComplexTypes._LineSymbolizer.getInstance(),
                        null, 1, 1),
                new sldElement("PolygonSymbolizer", sldComplexTypes._PolygonSymbolizer
                        .getInstance(), null, 1, 1)});

        private _ImageOutline() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _ShadedRelief extends sldComplexType {
        private static ComplexType instance = new _ShadedRelief();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("BrightnessOnly", org.geotools.xml.xsi.XSISimpleTypes.Boolean
                        .getInstance()/* simpleType name is boolean */, null, 0, 1),
                new sldElement("ReliefFactor", org.geotools.xml.xsi.XSISimpleTypes.Double
                        .getInstance()/* simpleType name is double */, null, 0, 1)};

        private static ElementGrouping child = new SequenceGT(null, new ElementGrouping[]{
                new sldElement("BrightnessOnly", org.geotools.xml.xsi.XSISimpleTypes.Boolean
                        .getInstance()/* simpleType name is boolean */, null, 0, 1),
                new sldElement("ReliefFactor", org.geotools.xml.xsi.XSISimpleTypes.Double
                        .getInstance()/* simpleType name is double */, null, 0, 1)}, 1, 1);

        private _ShadedRelief() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _ColorMap extends sldComplexType {
        private static ComplexType instance = new _ColorMap();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{new sldElement("ColorMapEntry",
                sldComplexTypes._ColorMapEntry.getInstance(), null, 1, 1)};

        private static ElementGrouping child = new ChoiceGT(null, 0, 2147483647,
                new ElementGrouping[]{new sldElement("ColorMapEntry",
                        sldComplexTypes._ColorMapEntry.getInstance(), null, 1, 1)});

        private _ColorMap() {
            super(null, child, attrs, elems, null, false, false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            return super.getValue(element, value, attrs1, hints);
            // TODO fill me in
        }
    }
    protected static class _RasterSymbolizer extends sldComplexType {
        private static ComplexType instance = new _RasterSymbolizer();
        public static ComplexType getInstance() {
            return instance;
        }

        private static Attribute[] attrs = null;
        private static Element[] elems = new Element[]{
                new sldElement("Geometry", sldComplexTypes._Geometry.getInstance(), null, 0, 1),
                new sldElement("Opacity", sldComplexTypes.ParameterValueType.getInstance(), null,
                        0, 1),
                new sldElement("ChannelSelection", sldComplexTypes._ChannelSelection.getInstance(),
                        null, 0, 1),
                new sldElement("OverlapBehavior", sldComplexTypes._OverlapBehavior.getInstance(),
                        null, 0, 1),
                new sldElement("ColorMap", sldComplexTypes._ColorMap.getInstance(), null, 0, 1),
                new sldElement("ContrastEnhancement", sldComplexTypes._ContrastEnhancement
                        .getInstance(), null, 0, 1),
                new sldElement("ShadedRelief", sldComplexTypes._ShadedRelief.getInstance(), null,
                        0, 1),
                new sldElement("ImageOutline", sldComplexTypes._ImageOutline.getInstance(), null,
                        0, 1)};
        
        // array positions
        private static int GEOMETRY = 0;
        private static int OPACITY = 1;
        private static int CHANNELSELECTION = 2;
        private static int OVERLAPBEHAVIOR = 3;
        private static int COLORMAP = 4;
        private static int CONTRASTENHANCEMENT = 5;
        private static int SHADEDRELIEF = 6;
        private static int IMAGEOUTLINE = 7;

        private static ElementGrouping child = new SequenceGT(elems);

        private _RasterSymbolizer() {
            super(null, child, attrs, elems, sldComplexTypes.SymbolizerType.getInstance(), false,
                    false);
        }
        
        
        /**
         * TODO summary sentence for canEncode ...
         * 
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         * @param element
         * @param value
         * @param hints
         * @return
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            return super.canEncode(element, value, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for encode ...
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         * @param element
         * @param value
         * @param output
         * @param hints
         * @throws OperationNotSupportedException
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints )
                throws OperationNotSupportedException {
            super.encode(element, value, output, hints);
            // TODO fill me in
        }
        /**
         * TODO summary sentence for getValue ...
         * 
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         * @param element
         * @param value
         * @param attrs1
         * @param hints
         * @return
         * @throws OperationNotSupportedException
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs1, Map hints )
                throws OperationNotSupportedException {
            RasterSymbolizer symbol = StyleFactory.createStyleFactory().getDefaultRasterSymbolizer();
            // symbol.setGraphic(null);

            for (int i = 0; i < value.length; i++) {
                if ((value[i] == null) || value[i].getElement() == null) {
                    continue;
                }

                Element e = value[i].getElement();
                if(elems[GEOMETRY].getName().equals(e.getName()))
                    symbol.setGeometryPropertyName((String)value[i].getValue());

                if(elems[OPACITY].getName().equals(e.getName()))
                    symbol.setOpacity((Expression)value[i].getValue());

                if(elems[CHANNELSELECTION].getName().equals(e.getName()))
                    symbol.setChannelSelection((ChannelSelection)value[i].getValue());

                if(elems[OVERLAPBEHAVIOR].getName().equals(e.getName()))
                    symbol.setOverlap((Expression)value[i].getValue());

                if(elems[COLORMAP].getName().equals(e.getName()))
                    symbol.setColorMap((ColorMap)value[i].getValue());

                if(elems[CONTRASTENHANCEMENT].getName().equals(e.getName()))
                    symbol.setContrastEnhancement((ContrastEnhancement)value[i].getValue());

                if(elems[SHADEDRELIEF].getName().equals(e.getName()))
                    symbol.setShadedRelief((ShadedRelief)value[i].getValue());

                if(elems[IMAGEOUTLINE].getName().equals(e.getName()))
                    symbol.setImageOutline((Symbolizer)value[i].getValue());
            }
            
            return symbol;
        }
    }
}
