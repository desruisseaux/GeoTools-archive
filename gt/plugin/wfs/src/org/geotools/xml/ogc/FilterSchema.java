
package org.geotools.xml.ogc;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import org.geotools.xml.gml.GMLSchema;
import org.geotools.xml.ogc.FilterComplexTypes.BinaryOperatorType;
import org.geotools.xml.ogc.FilterComplexTypes.Comparison_OperatorsType;
import org.geotools.xml.ogc.FilterComplexTypes.ExpressionType;
import org.geotools.xml.ogc.FilterComplexTypes.Filter_CapabilitiesType;
import org.geotools.xml.ogc.FilterComplexTypes.FunctionType;
import org.geotools.xml.ogc.FilterComplexTypes.Function_NameType;
import org.geotools.xml.ogc.FilterComplexTypes.Function_NamesType;
import org.geotools.xml.ogc.FilterComplexTypes.FunctionsType;
import org.geotools.xml.ogc.FilterComplexTypes.LiteralType;
import org.geotools.xml.ogc.FilterComplexTypes.PropertyNameType;
import org.geotools.xml.ogc.FilterComplexTypes.Scalar_CapabilitiesType;
import org.geotools.xml.ogc.FilterComplexTypes.ServiceExceptionReportType;
import org.geotools.xml.ogc.FilterComplexTypes.ServiceExceptionType;
import org.geotools.xml.ogc.FilterComplexTypes.Spatial_CapabilitiesType;
import org.geotools.xml.ogc.FilterComplexTypes.Spatial_OperatorsType;
import org.geotools.xml.ogc.FilterOpsComplexTypes.BBOXType;
import org.geotools.xml.ogc.FilterOpsComplexTypes.BinaryComparisonOpType;
import org.geotools.xml.ogc.FilterOpsComplexTypes.BinaryLogicOpType;
import org.geotools.xml.ogc.FilterOpsComplexTypes.BinarySpatialOpType;
import org.geotools.xml.ogc.FilterOpsComplexTypes.ComparisonOpsType;
import org.geotools.xml.ogc.FilterOpsComplexTypes.DistanceBufferType;
import org.geotools.xml.ogc.FilterOpsComplexTypes.DistanceType;
import org.geotools.xml.ogc.FilterOpsComplexTypes.FeatureIdType;
import org.geotools.xml.ogc.FilterOpsComplexTypes.FilterType;
import org.geotools.xml.ogc.FilterOpsComplexTypes.LogicOpsType;
import org.geotools.xml.ogc.FilterOpsComplexTypes.LowerBoundaryType;
import org.geotools.xml.ogc.FilterOpsComplexTypes.PropertyIsBetweenType;
import org.geotools.xml.ogc.FilterOpsComplexTypes.PropertyIsLikeType;
import org.geotools.xml.ogc.FilterOpsComplexTypes.PropertyIsNullType;
import org.geotools.xml.ogc.FilterOpsComplexTypes.SpatialOpsType;
import org.geotools.xml.ogc.FilterOpsComplexTypes.UnaryLogicOpType;
import org.geotools.xml.ogc.FilterOpsComplexTypes.UpperBoundaryType;
import org.geotools.xml.schema.Attribute;
import org.geotools.xml.schema.AttributeGroup;
import org.geotools.xml.schema.ComplexType;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.Group;
import org.geotools.xml.schema.Schema;
import org.geotools.xml.schema.SimpleType;
import org.geotools.xml.schema.Type;

/**
 * <p> 
 * DOCUMENT ME!
 * TODO Fill me in !!!
 * </p>
 * @author dzwiers
 *
 */
public class FilterSchema implements Schema {
    
    public static final String NAMESPACE = "http://www.opengis.net/ogc";
    
    private static final FilterSchema instance = new FilterSchema();

    public static FilterSchema getInstance(){return instance;}
    
    private static Element[] elements = loadElements();
    
    private static Element[] loadElements(){
        Element comparisonOps = new DefaultElement("comparisonOps",ComparisonOpsType.getInstance()){
            public boolean isAbstract(){
                return true;
            }
        };
        Element spatialOps = new DefaultElement("spatialOps",SpatialOpsType.getInstance()){
            public boolean isAbstract(){
                return true;
            }
        };
        Element logicOps = new DefaultElement("logicOps",LogicOpsType.getInstance()){
            public boolean isAbstract(){
                return true;
            }
        };
        Element expression = new DefaultElement("expression",ExpressionType.getInstance()){
            public boolean isAbstract(){
                return true;
            }
        };
        elements = new Element[] {
            // filterCapabilities -- many labels have been excluded here
            new DefaultElement("Filter_Capabilities",Filter_CapabilitiesType.getInstance()),
            
            // filter
            new DefaultElement("FeatureId",FeatureIdType.getInstance(),comparisonOps),
            new DefaultElement("Filter",FilterType.getInstance(),comparisonOps),
            
            // COMPARISON OPERATORS
            comparisonOps,
            new DefaultElement("PropertyIsEqualTo",BinaryComparisonOpType.getInstance(),comparisonOps),
            new DefaultElement("PropertyIsNotEqualTo",BinaryComparisonOpType.getInstance(),comparisonOps),
            new DefaultElement("PropertyIsLessThan",BinaryComparisonOpType.getInstance(),comparisonOps),
            new DefaultElement("PropertyIsGreaterThan",BinaryComparisonOpType.getInstance(),comparisonOps),
            new DefaultElement("PropertyIsLessThanOrEqualTo",BinaryComparisonOpType.getInstance(),comparisonOps),
            new DefaultElement("PropertyIsGreaterThanOrEqualTo",BinaryComparisonOpType.getInstance(),comparisonOps),
            new DefaultElement("PropertyIsLike",PropertyIsLikeType.getInstance(),comparisonOps),
            new DefaultElement("PropertyIsNull",PropertyIsNullType.getInstance(),comparisonOps),
            new DefaultElement("PropertyIsBetween",PropertyIsBetweenType.getInstance(),comparisonOps),
            
            // SPATIAL OPERATORS
            spatialOps,
            new DefaultElement("Equals",BinarySpatialOpType.getInstance(),spatialOps),
            new DefaultElement("Disjoint",BinarySpatialOpType.getInstance(),spatialOps),
            new DefaultElement("Touches",BinarySpatialOpType.getInstance(),spatialOps),
            new DefaultElement("Within",BinarySpatialOpType.getInstance(),spatialOps),
            new DefaultElement("Overlaps",BinarySpatialOpType.getInstance(),spatialOps),
            new DefaultElement("Crosses",BinarySpatialOpType.getInstance(),spatialOps),
            new DefaultElement("Intersects",BinarySpatialOpType.getInstance(),spatialOps),
            new DefaultElement("Contains",BinarySpatialOpType.getInstance(),spatialOps),
            new DefaultElement("DWithin",DistanceBufferType.getInstance(),spatialOps),
            new DefaultElement("Beyond",DistanceBufferType.getInstance(),spatialOps),
            new DefaultElement("BBOX",BBOXType.getInstance(),spatialOps),
            
            // LOGICAL OPERATORS
            logicOps,
            new DefaultElement("And",BinaryLogicOpType.getInstance(),logicOps),
            new DefaultElement("Or",BinaryLogicOpType.getInstance(),logicOps),
            new DefaultElement("Not",UnaryLogicOpType.getInstance(),logicOps),
            
            // expr
            expression,
            new DefaultElement("Add",BinaryOperatorType.getInstance(),expression),
            new DefaultElement("Sub",BinaryOperatorType.getInstance(),expression),
            new DefaultElement("Mul",BinaryOperatorType.getInstance(),expression),
            new DefaultElement("Div",BinaryOperatorType.getInstance(),expression),
            new DefaultElement("PropertyName",PropertyNameType.getInstance(),expression),
            new DefaultElement("Function",FunctionType.getInstance(),expression),
            new DefaultElement("Literal",LiteralType.getInstance(),expression),
            
            // exception
            new DefaultElement("ServiceExceptionReport",ServiceExceptionReportType.getInstance())
        };
        return elements;
    }
        
    private static final ComplexType[] complexTypes = new ComplexType[] {
            // filterCapabilities
            Comparison_OperatorsType.getInstance(),
            Function_NameType.getInstance(),
            Function_NamesType.getInstance(),
            FunctionsType.getInstance(),
            Scalar_CapabilitiesType.getInstance(),
            Spatial_CapabilitiesType.getInstance(),
            Spatial_OperatorsType.getInstance(),
            
            // filter
            ComparisonOpsType.getInstance(),
            SpatialOpsType.getInstance(),
            LogicOpsType.getInstance(),
            FilterType.getInstance(),
            FeatureIdType.getInstance(),
            BinaryComparisonOpType.getInstance(),
            PropertyIsLikeType.getInstance(),
            PropertyIsNullType.getInstance(),
            PropertyIsBetweenType.getInstance(),
            LowerBoundaryType.getInstance(),
            UpperBoundaryType.getInstance(),
            BinarySpatialOpType.getInstance(),
            BBOXType.getInstance(),
            DistanceBufferType.getInstance(),
            DistanceType.getInstance(),
            BinaryLogicOpType.getInstance(),
            UnaryLogicOpType.getInstance(),
            
            // expr 
            ExpressionType.getInstance(),
            BinaryOperatorType.getInstance(),
            FunctionType.getInstance(),
            LiteralType.getInstance(),
            PropertyNameType.getInstance(),
            
            // exception
            ServiceExceptionType.getInstance()
    };

    /**
     * @see org.geotools.xml.schema.Schema#getAttributeGroups()
     */
    public AttributeGroup[] getAttributeGroups() {
        return null;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getAttributes()
     */
    public Attribute[] getAttributes() {
        return null;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getBlockDefault()
     */
    public int getBlockDefault() {
        return NONE;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getComplexTypes()
     */
    public ComplexType[] getComplexTypes() {
        return complexTypes;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getElements()
     */
    public Element[] getElements() {
        return elements;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getFinalDefault()
     */
    public int getFinalDefault() {
        return NONE;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getGroups()
     */
    public Group[] getGroups() {
        return null;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getId()
     */
    public String getId() {
        return null;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getImports()
     */
    public Schema[] getImports() {
        return new Schema[] {GMLSchema.getInstance(),};
    }

    /**
     * @see org.geotools.xml.schema.Schema#getURI()
     */
    public URI getURI() {
        try {
            return new URI(NAMESPACE);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /**
     * @see org.geotools.xml.schema.Schema#getPrefix()
     */
    public String getPrefix() {
        return "ogc";
    }

    /**
     * @see org.geotools.xml.schema.Schema#getSimpleTypes()
     */
    public SimpleType[] getSimpleTypes() {
        return null;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getTargetNamespace()
     */
    public String getTargetNamespace() {
        return NAMESPACE;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getVersion()
     */
    public String getVersion() {
        return "1.0.0";
    }

    /**
     * @see org.geotools.xml.schema.Schema#includesURI(java.net.URI)
     */
    public boolean includesURI(URI uri) {
        if (uri.toString().toLowerCase().endsWith("filter.xsd")
                || uri.toString().toLowerCase().endsWith("filterCapabilities.xsd")
                || uri.toString().toLowerCase().endsWith("OGC-exception.xsd")
                || uri.toString().toLowerCase().endsWith("expr.xsd")) {
            return true;
        }

        return false;
    }

    /**
     * @see org.geotools.xml.schema.Schema#isAttributeFormDefault()
     */
    public boolean isAttributeFormDefault() {
        return false;
    }

    /**
     * @see org.geotools.xml.schema.Schema#isElementFormDefault()
     */
    public boolean isElementFormDefault() {
        return true;
    }
    
    static class DefaultElement implements Element{
        
        private String name;
        private Type type;
        private Element substitutionGroup;
        
        public DefaultElement(String name, Type type){
            this.name = name;this.type = type;
        }
        
        public DefaultElement(String name, Type type, Element substitutionGroup){
            this.name = name;this.type = type;
            this.substitutionGroup = substitutionGroup;
        }
        
        /**
         * @see org.geotools.xml.schema.Element#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Element#getBlock()
         */
        public int getBlock() {
            return NONE;
        }

        /**
         * @see org.geotools.xml.schema.Element#getDefault()
         */
        public String getDefault() {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Element#getFinal()
         */
        public int getFinal() {
            return NONE;
        }

        /**
         * @see org.geotools.xml.schema.Element#getFixed()
         */
        public String getFixed() {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Element#isForm()
         */
        public boolean isForm() {
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Element#getId()
         */
        public String getId() {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Element#getMaxOccurs()
         */
        public int getMaxOccurs() {
            return 1;
        }

        /**
         * @see org.geotools.xml.schema.Element#getMinOccurs()
         */
        public int getMinOccurs() {
            return 1;
        }

        /**
         * @see org.geotools.xml.schema.Element#getName()
         */
        public String getName() {
            return name;
        }

        /**
         * @see org.geotools.xml.schema.Element#getNamespace()
         */
        public String getNamespace() {
            return NAMESPACE;
        }

        /**
         * @see org.geotools.xml.schema.Element#isNillable()
         */
        public boolean isNillable() {
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Element#getSubstitutionGroup()
         */
        public Element getSubstitutionGroup() {
            return substitutionGroup;
        }

        /**
         * @see org.geotools.xml.schema.Element#getType()
         */
        public Type getType() {
            return type;
        }

        /**
         * @see org.geotools.xml.schema.ElementGrouping#getGrouping()
         */
        public int getGrouping() {
            return ELEMENT;
        }

        /**
         * @see org.geotools.xml.schema.ElementGrouping#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            return (getName()!=null && getName().equals(name)?this:null);
        }
        
    }
    
    static abstract class FilterComplexType implements ComplexType{
        /**
         * @see org.geotools.xml.schema.ComplexType#getParent()
         */
        public Type getParent() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getBlock()
         */
        public int getBlock() {
            return Schema.NONE;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getFinal()
         */
        public int getFinal() {
            return Schema.NONE;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getId()
         */
        public String getId() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isMixed()
         */
        public boolean isMixed() {
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isDerived()
         */
        public boolean isDerived() {
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element, java.util.Map)
         */
        public boolean cache(Element element, Map hints) {
            return true;
        }
        /**
         * @see org.geotools.xml.schema.Type#getNamespace()
         */
        public String getNamespace() {
            return FilterSchema.NAMESPACE;
        }
        /**
         * @see org.geotools.xml.schema.Type#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            return getChild()==null?null:
                getChild().findChildElement(name);
        }
    }
}
