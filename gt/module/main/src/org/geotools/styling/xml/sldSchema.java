package org.geotools.styling.xml;

/**
 * This code generated using Refractions SchemaCodeGenerator For more information, view the attached
 * licensing information. CopyRight 105
 */

import java.net.URI;
import java.net.URISyntaxException;

import org.geotools.xml.schema.Attribute;
import org.geotools.xml.schema.AttributeGroup;
import org.geotools.xml.schema.ComplexType;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.Group;
import org.geotools.xml.schema.Schema;
import org.geotools.xml.schema.SimpleType;

public class sldSchema implements Schema {

    public static final URI NAMESPACE = loadNS();
    private static URI loadNS() {
        try {
            return new URI("http://www.opengis.net/sld");
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public int getBlockDefault() {
        return 0;
    }

    public int getFinalDefault() {
        return 0;
    }

    public String getId() {
        return "null";
    }
    private static Schema[] imports = null;

    public Schema[] getImports() {
        if (imports == null) {
            imports = new Schema[]{
            // TODO add instance of org.geotools.xml.xLink.XLinkSchema@e94e92,
            // TODO add instance of org.geotools.xml.ogc.FilterSchema@18020cc
            };
        }
        return imports;
    }

    public String getPrefix() {
        return "sld";
    }

    public URI getTargetNamespace() {
        return NAMESPACE;
    }

    public URI getURI() {
        return NAMESPACE;
    }

    public String getVersion() {
        return "null";
    }

    public boolean includesURI( URI uri ) {
        // // TODO fill me in!
        return false; // // safer
    }

    public boolean isAttributeFormDefault() {
        return false;
    }

    public boolean isElementFormDefault() {
        return true;
    }

    public AttributeGroup[] getAttributeGroups() {
        return null;
    }
    public Attribute[] getAttributes() {
        return null;
    }
    /**
     * TODO comment here
     */
    private static ComplexType[] complexTypes = null;
    public ComplexType[] getComplexTypes() {
        if (complexTypes == null) {
            complexTypes = new ComplexType[]{sldComplexTypes.SelectedChannelType.getInstance(),
                    sldComplexTypes.ParameterValueType.getInstance(),
                    sldComplexTypes.SymbolizerType.getInstance()};
        }
        return complexTypes;
    }
    /**
     * TODO comment here
     */
    private static Element[] elements = null;
    public Element[] getElements() {
        if (elements == null) {
            elements = new Element[]{
                    new sldElement("Displacement", sldComplexTypes._Displacement.getInstance(), null, 1, 1),
                    new sldElement("GammaValue", org.geotools.xml.xsi.XSISimpleTypes.Double
                            .getInstance()/* simpleType name is double */, null, 1, 1),
                    new sldElement(
                            "MaxScaleDenominator",
                            org.geotools.xml.xsi.XSISimpleTypes.Double.getInstance()/*
                                                                                     * simpleType
                                                                                     * name is
                                                                                     * double
                                                                                     */,
                            null, 1, 1),
                    new sldElement("PolygonSymbolizer", sldComplexTypes._PolygonSymbolizer.getInstance(),
                            new sldElement("Symbolizer", sldComplexTypes.SymbolizerType
                                    .getInstance(), null, 1, 1), 1, 1),
                    new sldElement("ColorMapEntry", sldComplexTypes._ColorMapEntry.getInstance(), null, 1, 1),
                    new sldElement("GreenChannel", sldComplexTypes.SelectedChannelType
                            .getInstance(), null, 1, 1),
                    new sldElement("LATEST_ON_TOP", sldComplexTypes._LATEST_ON_TOP.getInstance(), null, 1, 1),
                    new sldElement("LayerFeatureConstraints", sldComplexTypes._LayerFeatureConstraints.getInstance(),
                            null, 1, 1),
                    new sldElement("Size", sldComplexTypes.ParameterValueType.getInstance(), null,
                            1, 1),
                    new sldElement("LineSymbolizer", sldComplexTypes._LineSymbolizer.getInstance(),
                            new sldElement("Symbolizer", sldComplexTypes.SymbolizerType
                                    .getInstance(), null, 1, 1), 1, 1),
                    new sldElement("PointSymbolizer", sldComplexTypes._PointSymbolizer.getInstance(),
                            new sldElement("Symbolizer", sldComplexTypes.SymbolizerType
                                    .getInstance(), null, 1, 1), 1, 1),
                    new sldElement("ChannelSelection", sldComplexTypes._ChannelSelection.getInstance(), null, 1,
                            1),
                    new sldElement("Graphic", sldComplexTypes._Graphic.getInstance(), null, 1, 1),
                    new sldElement("WellKnownName", org.geotools.xml.xsi.XSISimpleTypes.String
                            .getInstance()/* simpleType name is string */, null, 1, 1),
                    new sldElement(
                            "Name",
                            org.geotools.xml.xsi.XSISimpleTypes.String.getInstance()/*
                                                                                     * simpleType
                                                                                     * name is
                                                                                     * string
                                                                                     */,
                            null, 1, 1),
                    new sldElement(
                            "MinScaleDenominator",
                            org.geotools.xml.xsi.XSISimpleTypes.Double.getInstance()/*
                                                                                     * simpleType
                                                                                     * name is
                                                                                     * double
                                                                                     */,
                            null, 1, 1),
                    new sldElement("BlueChannel",
                            sldComplexTypes.SelectedChannelType.getInstance(), null, 1, 1),
                    new sldElement("RANDOM", sldComplexTypes._RANDOM.getInstance(), null, 1, 1),
                    new sldElement("FeatureTypeName", org.geotools.xml.xsi.XSISimpleTypes.String
                            .getInstance()/* simpleType name is string */, null, 1, 1),
                    new sldElement("Font", sldComplexTypes._Font.getInstance(), null, 1, 1),
                    new sldElement("Title", org.geotools.xml.xsi.XSISimpleTypes.String
                            .getInstance()/* simpleType name is string */, null, 1, 1),
                    new sldElement("UserStyle", sldComplexTypes._UserStyle.getInstance(), null, 1, 1),
                    new sldElement("PointPlacement", sldComplexTypes._PointPlacement.getInstance(), null, 1, 1),
                    new sldElement("Rotation", sldComplexTypes.ParameterValueType.getInstance(),
                            null, 1, 1),
                    new sldElement("OnlineResource", sldComplexTypes._OnlineResource.getInstance(), null, 1, 1),
                    new sldElement("Mark", sldComplexTypes._Mark.getInstance(), null, 1, 1),
                    new sldElement("BrightnessOnly", org.geotools.xml.xsi.XSISimpleTypes.Boolean
                            .getInstance()/* simpleType name is boolean */, null, 1, 1),
                    new sldElement(
                            "SemanticTypeIdentifier",
                            org.geotools.xml.xsi.XSISimpleTypes.String.getInstance()/*
                                                                                     * simpleType
                                                                                     * name is
                                                                                     * string
                                                                                     */,
                            null, 1, 1),
                    new sldElement("EARLIEST_ON_TOP", sldComplexTypes._EARLIEST_ON_TOP.getInstance(), null, 1,
                            1),
                    new sldElement("Geometry", sldComplexTypes._Geometry.getInstance(), null, 1, 1),
                    new sldElement("ElseFilter", sldComplexTypes._ElseFilter.getInstance(), null, 1, 1),
                    new sldElement("StyledLayerDescriptor", sldComplexTypes._StyledLayerDescriptor.getInstance(),
                            null, 1, 1),
                    new sldElement("Abstract", org.geotools.xml.xsi.XSISimpleTypes.String
                            .getInstance()/* simpleType name is string */, null, 1, 1),
                    new sldElement("AnchorPoint", sldComplexTypes._AnchorPoint.getInstance(), null, 1, 1),
                    new sldElement("GraphicStroke", sldComplexTypes._GraphicStroke.getInstance(), null, 1, 1),
                    new sldElement("ContrastEnhancement", sldComplexTypes._ContrastEnhancement.getInstance(), null,
                            1, 1),
                    new sldElement("FeatureTypeStyle", sldComplexTypes._FeatureTypeStyle.getInstance(), null, 1,
                            1),
                    new sldElement("Format", org.geotools.xml.xsi.XSISimpleTypes.String
                            .getInstance()/* simpleType name is string */, null, 1, 1),
                    new sldElement("DisplacementY", sldComplexTypes.ParameterValueType
                            .getInstance(), null, 1, 1),
                    new sldElement("DisplacementX", sldComplexTypes.ParameterValueType
                            .getInstance(), null, 1, 1),
                    new sldElement("NamedLayer", sldComplexTypes._NamedLayer.getInstance(), null, 1, 1),
                    new sldElement("TextSymbolizer", sldComplexTypes._TextSymbolizer.getInstance(),
                            new sldElement("Symbolizer", sldComplexTypes.SymbolizerType
                                    .getInstance(), null, 1, 1), 1, 1),
                    new sldElement("LabelPlacement", sldComplexTypes._LabelPlacement.getInstance(), null, 1, 1),
                    new sldElement("Value", org.geotools.xml.xsi.XSISimpleTypes.String
                            .getInstance(), null, 1, 1),
                    new sldElement("Histogram", sldComplexTypes._Histogram.getInstance(), null, 1, 1),
                    new sldElement("ExternalGraphic", sldComplexTypes._ExternalGraphic.getInstance(), null, 1,
                            1),
                    new sldElement("NamedStyle", sldComplexTypes._NamedStyle.getInstance(), null, 1, 1),
                    new sldElement("AnchorPointY",
                            sldComplexTypes.ParameterValueType.getInstance(), null, 1, 1),
                    new sldElement("RemoteOWS", sldComplexTypes._RemoteOWS.getInstance(), null, 1, 1),
                    new sldElement("CssParameter", sldComplexTypes._CssParameter.getInstance(), null, 1, 1),
                    new sldElement("PerpendicularOffset", sldComplexTypes.ParameterValueType
                            .getInstance(), null, 1, 1),
                    new sldElement("Label", sldComplexTypes.ParameterValueType.getInstance(), null,
                            1, 1),
                    new sldElement("OverlapBehavior", sldComplexTypes._OverlapBehavior.getInstance(), null, 1,
                            1),
                    new sldElement("Halo", sldComplexTypes._Halo.getInstance(), null, 1, 1),
                    new sldElement("ImageOutline", sldComplexTypes._ImageOutline.getInstance(), null, 1, 1),
                    new sldElement("Fill", sldComplexTypes._Fill.getInstance(), null, 1, 1),
                    new sldElement("ShadedRelief", sldComplexTypes._ShadedRelief.getInstance(), null, 1, 1),
                    new sldElement("SourceChannelName", org.geotools.xml.xsi.XSISimpleTypes.String
                            .getInstance()/* simpleType name is string */, null, 1, 1),
                    new sldElement("Service", sldSimpleTypes._Service.getInstance(), null, 1, 1),
                    new sldElement("GrayChannel",
                            sldComplexTypes.SelectedChannelType.getInstance(), null, 1, 1),
                    new sldElement("Rule", sldComplexTypes._Rule.getInstance(), null, 1, 1),
                    new sldElement("RedChannel", sldComplexTypes.SelectedChannelType.getInstance(),
                            null, 1, 1),
                    new sldElement("GraphicFill", sldComplexTypes._GraphicFill.getInstance(), null, 1, 1),
                    new sldElement("LegendGraphic", sldComplexTypes._LegendGraphic.getInstance(), null, 1, 1),
                    new sldElement("AVERAGE", sldComplexTypes._AVERAGE.getInstance(), null, 1, 1),
                    new sldElement("IsDefault", org.geotools.xml.xsi.XSISimpleTypes.Boolean
                            .getInstance()/* simpleType name is boolean */, null, 1, 1),
                    new sldElement("LinePlacement", sldComplexTypes._LinePlacement.getInstance(), null, 1, 1),
                    new sldElement("Normalize", sldComplexTypes._Normalize.getInstance(), null, 1, 1),
                    new sldElement("Extent", sldComplexTypes._Extent.getInstance(), null, 1, 1),
                    new sldElement("ReliefFactor", org.geotools.xml.xsi.XSISimpleTypes.Double
                            .getInstance()/* simpleType name is double */, null, 1, 1),
                    new sldElement("RasterSymbolizer", sldComplexTypes._RasterSymbolizer.getInstance(),
                            new sldElement("Symbolizer", sldComplexTypes.SymbolizerType
                                    .getInstance(), null, 1, 1), 1, 1),
                    new sldElement("FeatureTypeConstraint", sldComplexTypes._FeatureTypeConstraint.getInstance(),
                            null, 1, 1),
                    new sldElement("Stroke", sldComplexTypes._Stroke.getInstance(), null, 1, 1),
                    new sldElement("ColorMap", sldComplexTypes._ColorMap.getInstance(), null, 1, 1),
                    new sldElement("UserLayer", sldComplexTypes._UserLayer.getInstance(), null, 1, 1),
                    new sldElement("Symbolizer", sldComplexTypes.SymbolizerType.getInstance(),
                            null, 1, 1),
                    new sldElement("Opacity", sldComplexTypes.ParameterValueType.getInstance(),
                            null, 1, 1),
                    new sldElement("Radius", sldComplexTypes.ParameterValueType.getInstance(),
                            null, 1, 1),
                    new sldElement("AnchorPointX",
                            sldComplexTypes.ParameterValueType.getInstance(), null, 1, 1)};
        }
        return elements;
    }
    public Group[] getGroups() {
        return null;
    }
    public SimpleType[] getSimpleTypes() {
        return null;
    }
}
