
package org.geotools.xml.ogc;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.geotools.filter.BetweenFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.GeometryDistanceFilter;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.LikeFilter;
import org.geotools.filter.LogicFilter;
import org.geotools.filter.NullFilter;
import org.geotools.xml.PrintHandler;
import org.geotools.xml.gml.GMLSchema;
import org.geotools.xml.ogc.FilterComplexTypes.ExpressionType;
import org.geotools.xml.ogc.FilterComplexTypes.LiteralType;
import org.geotools.xml.ogc.FilterComplexTypes.PropertyNameType;
import org.geotools.xml.ogc.FilterSchema.FilterAttribute;
import org.geotools.xml.ogc.FilterSchema.FilterComplexType;
import org.geotools.xml.ogc.FilterSchema.FilterElement;
import org.geotools.xml.schema.Attribute;
import org.geotools.xml.schema.Choice;
import org.geotools.xml.schema.ComplexType;
import org.geotools.xml.schema.DefaultChoice;
import org.geotools.xml.schema.DefaultSequence;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.ElementGrouping;
import org.geotools.xml.schema.ElementValue;
import org.geotools.xml.schema.Sequence;
import org.geotools.xml.schema.Type;
import org.geotools.xml.xsi.XSISimpleTypes;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.AttributesImpl;

import com.vividsolutions.jts.geom.Geometry;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class FilterOpsComplexTypes {
    public static class ComparisonOpsType extends FilterComplexType implements org.geotools.filter.FilterType{
        private static final ComplexType instance = new ComparisonOpsType();
        public static ComplexType getInstance(){return instance;}
//    	<xsd:complexType name="ComparisonOpsType" abstract="true"/>
        
        public boolean isAbstract(){
        	return true;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "ComparisonOpsType";
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return CompareFilter.class;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            return element.getType()!=null && getName().equals(element.getType().getName()) && (value instanceof CompareFilter || value instanceof BetweenFilter || value instanceof NullFilter || value instanceof LikeFilter);
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            if(!canEncode(element,value,hints))
            	return;
            Filter lf = (Filter)value;
            switch(lf.getFilterType()){
            case COMPARE_EQUALS:
            	BinaryComparisonOpType.getInstance().encode(new FilterElement("PropertyIsEqualTo",BinaryComparisonOpType.getInstance(),element),value,output,hints);
            	return;
            case COMPARE_GREATER_THAN:
            	BinaryComparisonOpType.getInstance().encode(new FilterElement("PropertyIsGreaterThan",BinaryComparisonOpType.getInstance(),element),value,output,hints);
            	return;
            case COMPARE_GREATER_THAN_EQUAL:
            	BinaryComparisonOpType.getInstance().encode(new FilterElement("PropertyIsGreaterThanOrEqualTo",BinaryComparisonOpType.getInstance(),element),value,output,hints);
            	return;
            case COMPARE_LESS_THAN:
            	BinaryComparisonOpType.getInstance().encode(new FilterElement("PropertyIsLessThan",BinaryComparisonOpType.getInstance(),element),value,output,hints);
            	return;
            case COMPARE_LESS_THAN_EQUAL:
            	BinaryComparisonOpType.getInstance().encode(new FilterElement("PropertyIsLessThanOrEqualTo",BinaryComparisonOpType.getInstance(),element),value,output,hints);
            	return;
            case COMPARE_NOT_EQUALS:
            	BinaryComparisonOpType.getInstance().encode(new FilterElement("PropertyIsNotEqualTo",BinaryComparisonOpType.getInstance(),element),value,output,hints);
            	return;
            case LIKE:
            	PropertyIsLikeType.getInstance().encode(new FilterElement("PropertyIsLike",PropertyIsLikeType.getInstance(),element),value,output,hints);
            	return;
            case NULL:
            	PropertyIsNullType.getInstance().encode(new FilterElement("PropertyIsNull",PropertyIsNullType.getInstance(),element),value,output,hints);
            	return;
            case BETWEEN:
            	PropertyIsBetweenType.getInstance().encode(new FilterElement("PropertyIsBetween",PropertyIsBetweenType.getInstance(),element),value,output,hints);
            	return;
            }
            throw new OperationNotSupportedException("Unknown filter type in ComparisonFilter: "+lf.getClass().getName());
        }
    }
    
    public static class SpatialOpsType extends FilterComplexType implements org.geotools.filter.FilterType{
        private static final ComplexType instance = new SpatialOpsType();
        public static ComplexType getInstance(){return instance;}
//    	<xsd:complexType name="SpatialOpsType" abstract="true"/>
        public boolean isAbstract(){
        	return true;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "SpatialOpsType";
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return GeometryFilter.class;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            return element.getType()!=null && getName().equals(element.getType().getName()) && value instanceof GeometryFilter;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            if(!canEncode(element,value,hints))
            	return;
            GeometryFilter lf = (GeometryFilter)value;
            switch(lf.getFilterType()){
            case GEOMETRY_BBOX:
            	BBOXType.getInstance().encode(new FilterElement("BBOX",BBOXType.getInstance(),element),value,output,hints);
            	return;
            case GEOMETRY_BEYOND:
            	DistanceBufferType.getInstance().encode(new FilterElement("Beyond",DistanceBufferType.getInstance(),element),value,output,hints);
            	return;
            case GEOMETRY_CONTAINS:
            	BinarySpatialOpType.getInstance().encode(new FilterElement("Contains",BinarySpatialOpType.getInstance(),element),value,output,hints);
            	return;
            case GEOMETRY_CROSSES:
            	BinarySpatialOpType.getInstance().encode(new FilterElement("Crosses",BinarySpatialOpType.getInstance(),element),value,output,hints);
            	return;
            case GEOMETRY_DISJOINT:
            	BinarySpatialOpType.getInstance().encode(new FilterElement("Disjoint",BinarySpatialOpType.getInstance(),element),value,output,hints);
            	return;
            case GEOMETRY_DWITHIN:
            	DistanceBufferType.getInstance().encode(new FilterElement("DWithin",DistanceBufferType.getInstance(),element),value,output,hints);
            	return;
            case GEOMETRY_EQUALS:
            	BinarySpatialOpType.getInstance().encode(new FilterElement("Equals",BinarySpatialOpType.getInstance(),element),value,output,hints);
            	return;
            case GEOMETRY_INTERSECTS:
            	BinarySpatialOpType.getInstance().encode(new FilterElement("Intersects",BinarySpatialOpType.getInstance(),element),value,output,hints);
            	return;
            case GEOMETRY_OVERLAPS:
            	BinarySpatialOpType.getInstance().encode(new FilterElement("Overlaps",BinarySpatialOpType.getInstance(),element),value,output,hints);
            	return;
            case GEOMETRY_TOUCHES:
            	BinarySpatialOpType.getInstance().encode(new FilterElement("Touches",BinarySpatialOpType.getInstance(),element),value,output,hints);
            	return;
            case GEOMETRY_WITHIN:
            	BinarySpatialOpType.getInstance().encode(new FilterElement("Within",BinarySpatialOpType.getInstance(),element),value,output,hints);
            	return;
            }
            throw new OperationNotSupportedException("Unknown filter type in ComparisonFilter: "+lf.getClass().getName());
        }
    }
    public static class LogicOpsType extends FilterComplexType implements org.geotools.filter.FilterType{
        private static final ComplexType instance = new LogicOpsType();
        public static ComplexType getInstance(){return instance;}
        
//    	<xsd:complexType name="LogicOpsType" abstract="true"/>
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "LogicOpsType";
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return LogicFilter.class;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            return element.getType()!=null && getName().equals(element.getType().getName()) && value instanceof LogicFilter;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            if(!canEncode(element,value,hints))
            	return;
            LogicFilter lf = (LogicFilter)value;
            switch(lf.getFilterType()){
            case LOGIC_AND:
            	BinaryLogicOpType.getInstance().encode(new FilterElement("And",BinaryLogicOpType.getInstance(),element),value,output,hints);
            	return;
            case LOGIC_OR:
            	BinaryLogicOpType.getInstance().encode(new FilterElement("Or",BinaryLogicOpType.getInstance(),element),value,output,hints);
            	return;
            case LOGIC_NOT:
            	UnaryLogicOpType.getInstance().encode(new FilterElement("Not",UnaryLogicOpType.getInstance(),element),value,output,hints);
            	return;
            }
            throw new OperationNotSupportedException("Unknown filter type in LogicFilter: "+lf.getClass().getName());
        }
        
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#isAbstract()
		 */
		public boolean isAbstract() {
			return true;
		}
    }
    public static class FilterType extends FilterComplexType implements org.geotools.filter.FilterType{
    	
//    	<xsd:complexType name="FilterType">
//		<xsd:choice>
//			<xsd:element ref="ogc:spatialOps"/>
//			<xsd:element ref="ogc:comparisonOps"/>
//			<xsd:element ref="ogc:logicOps"/>
//			<xsd:element ref="ogc:FeatureId" maxOccurs="unbounded"/>
//		</xsd:choice>
//		</xsd:complexType>
    	
    	private static Element[] elems = new Element[] {
    			new FilterElement("spatialOps",SpatialOpsType.getInstance()),
    			new FilterElement("comparisonOps",ComparisonOpsType.getInstance()),
    			new FilterElement("logicOps",LogicOpsType.getInstance()),
    			new FilterElement("FeatureId",FeatureIdType.getInstance()){
    				public int getMaxOccurs(){
    					return Integer.MAX_VALUE;
    				}
    			},
    	};
    	
    	private static Choice choice = new DefaultChoice(elems);
    	
        private static final ComplexType instance = new FilterType();
        public static ComplexType getInstance(){return instance;}
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return choice;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "FilterType";
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return Filter.class;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
        	return element.getType()!=null && getName().equals(element.getType().getName()) && value instanceof Filter;
        }
        /**
         * Note the assumption is that the comparison of this filter with the WFS capabilities document has already been processed
         * 
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
        	if(!(value instanceof Filter))
        		return;
            // we may only encode one type of filter ...
        	Filter filter = (Filter)value;
        	if(filter == null)return;
        	if(filter == Filter.NONE)return;
        	if(element != null)
        	output.startElement(element.getNamespace(),element.getName(),null);
        	if(filter instanceof LogicFilter){
        		elems[2].getType().encode(elems[2],filter,output,hints);
    		}else{
    		if(filter instanceof CompareFilter){
    			elems[1].getType().encode(elems[1],filter,output,hints);
    		}else{
    		if(filter instanceof FidFilter){
    			//deal with multi instance inside the type-writer
    			elems[4].getType().encode(elems[4],filter,output,hints);
        	}else{
        	if(filter instanceof GeometryFilter){
    			elems[0].getType().encode(elems[0],filter,output,hints);
        	}else{
        	if(filter instanceof LikeFilter){
    			elems[1].getType().encode(elems[1],filter,output,hints);
        	}else{
        	if(filter instanceof NullFilter){
    			elems[1].getType().encode(elems[1],filter,output,hints);
        	}else{
        		throw new OperationNotSupportedException("The Filter type is not known: please try again. "+filter == null?"null":filter.getClass().getName());
        	}}}}}}
        	if(element != null)
        	output.endElement(element.getNamespace(),element.getName());
        }
    }
    public static class FeatureIdType extends FilterComplexType{
    	

//    	<xsd:complexType name="FeatureIdType">
//    		<xsd:attribute name="fid" type="xsd:anyURI" use="required"/>
//    	</xsd:complexType>
    	
        private static final ComplexType instance = new FeatureIdType();
        public static ComplexType getInstance(){return instance;}
        
        private static Attribute[] attrs = new Attribute[]{
        		new FilterAttribute("fid",XSISimpleTypes.AnyURI.getInstance(),Attribute.REQUIRED),
        };
        
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getAttributes()
		 */
		public Attribute[] getAttributes() {
			return attrs;
		}
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "FeatureIdType";
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return FidFilter.class;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            return element.getType()!=null && getName().equals(element.getType().getName()) && value instanceof FidFilter;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            if(!canEncode(element,value,hints))
            	return;
            FidFilter ff= (FidFilter)value;
            String[] fids = ff.getFids();
            AttributesImpl att = new AttributesImpl();
            att.addAttribute(null,null,null,null,null);
            for(int i=0;i<fids.length;i++){
            	att.setAttribute(0,element.getNamespace().toString(),attrs[0].getName(),null,"anyUri",fids[0]);
            	output.element(element.getNamespace(),element.getName(),att);
            }
        }
    }
    public static class BinaryComparisonOpType extends FilterComplexType implements org.geotools.filter.FilterType{
        private static final ComplexType instance = new BinaryComparisonOpType();
        public static ComplexType getInstance(){return instance;}
        
//      <xsd:complexType name="BinaryComparisonOpType">
//		<xsd:complexContent>
//			<xsd:extension base="ogc:ComparisonOpsType">
//				<xsd:sequence>
//					<xsd:element ref="ogc:expression" minOccurs="2" maxOccurs="2"/>
//				</xsd:sequence>
//			</xsd:extension>
//		</xsd:complexContent>
//		</xsd:complexType>
        
        private static Element[] elems = new Element[]{
        		new FilterElement("expression",ExpressionType.getInstance()){

        	        /**
        	         * @see org.geotools.xml.schema.Element#getMaxOccurs()
        	         */
        	        public int getMaxOccurs() {
        	            return 2;
        	        }

        	        /**
        	         * @see org.geotools.xml.schema.Element#getMinOccurs()
        	         */
        	        public int getMinOccurs() {
        	            return 2;
        	        }
        		},
        };
        
        public Type getParent(){
        	return ComparisonOpsType.getInstance();
        }
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "BinaryComparisonOpType";
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return CompareFilter.class;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            return element.getType()!=null && getName().equals(element.getType().getName()) && value instanceof CompareFilter;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
        	if(!canEncode(element,value,hints))
            	return;
        	CompareFilter cf = (CompareFilter)value;
        	
        	output.startElement(element.getNamespace(),element.getName(),null);
        	// TODO is this order dependant?
        	elems[0].getType().encode(elems[0],cf.getLeftValue(),output,hints);
        	elems[0].getType().encode(elems[0],cf.getRightValue(),output,hints);
        	
        	output.endElement(element.getNamespace(),element.getName());
        }
    }
    public static class PropertyIsLikeType extends FilterComplexType{
        private static final ComplexType instance = new PropertyIsLikeType();
        public static ComplexType getInstance(){return instance;}
        
//    	<xsd:complexType name="PropertyIsLikeType">
//    		<xsd:complexContent>
//    			<xsd:extension base="ogc:ComparisonOpsType">
//    				<xsd:sequence>
//    					<xsd:element ref="ogc:PropertyName"/>
//    					<xsd:element ref="ogc:Literal"/>
//    				</xsd:sequence>
//    				<xsd:attribute name="wildCard" type="xsd:string" use="required"/>
//    				<xsd:attribute name="singleChar" type="xsd:string" use="required"/>
//    				<xsd:attribute name="escape" type="xsd:string" use="required"/>
//    			</xsd:extension>
//    		</xsd:complexContent>
//    	</xsd:complexType>

        public Type getParent(){
        	return ComparisonOpsType.getInstance();
        }
        
        private static Element[] elems = new Element[]{
        		new FilterElement("PropertyName",PropertyNameType.getInstance()),
        		new FilterElement("Literal",LiteralType.getInstance()),
        };
        
        private static Sequence seq = new DefaultSequence(elems);
        
        private static Attribute[] attr = new Attribute[]{
        		new FilterAttribute("wildCard",XSISimpleTypes.String.getInstance(),Attribute.REQUIRED),
        		new FilterAttribute("singleChar",XSISimpleTypes.String.getInstance(),Attribute.REQUIRED),
        		new FilterAttribute("escape",XSISimpleTypes.String.getInstance(),Attribute.REQUIRED),
        };
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return seq;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }
        
        public Attribute[] getAttributes(){
        	return attr;
        }
        
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "PropertyIsLikeType";
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return LikeFilter.class;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            return element.getType()!=null && getName().equals(element.getType().getName()) && value instanceof LikeFilter;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            if(!canEncode(element,value,hints))
            	return;
            LikeFilter lf = (LikeFilter)value;
            
            AttributesImpl at = new AttributesImpl();
            at.addAttribute(FilterSchema.NAMESPACE.toString(),"wildCard",null,"string",lf.getWildcardMulti());
            at.addAttribute(FilterSchema.NAMESPACE.toString(),"singleChar",null,"string",lf.getWildcardSingle());
            at.addAttribute(FilterSchema.NAMESPACE.toString(),"escape",null,"string",lf.getEscape());
            
            output.startElement(element.getNamespace(),element.getName(),at);
            elems[0].getType().encode(elems[0],lf.getPattern(),output,hints); // PropertyName
            elems[1].getType().encode(elems[1],lf.getValue(),output,hints); // Literal
            output.endElement(element.getNamespace(),element.getName());
        }
    }
    public static class PropertyIsNullType extends FilterComplexType{
        private static final ComplexType instance = new PropertyIsNullType();
        public static ComplexType getInstance(){return instance;}
        

//    	<xsd:complexType name="PropertyIsNullType">
//    		<xsd:complexContent>
//    			<xsd:extension base="ogc:ComparisonOpsType">
//    				<xsd:choice>
//    					<xsd:element ref="ogc:PropertyName"/>
//    					<xsd:element ref="ogc:Literal"/>
//    				</xsd:choice>
//    			</xsd:extension>
//    		</xsd:complexContent>
//    	</xsd:complexType>
        public Type getParent(){
        	return ComparisonOpsType.getInstance();
        }
        
        private static Element[] elems = new Element[]{
        		new FilterElement("PropertyName",PropertyNameType.getInstance()),
        		new FilterElement("Literal",LiteralType.getInstance()),
        };
        
        private static Choice seq = new DefaultChoice(elems);
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return seq;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "PropertyIsNullType";
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return NullFilter.class;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            return element.getType()!=null && getName().equals(element.getType().getName()) && value instanceof NullFilter;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            if(!canEncode(element,value,hints))
            	return;
            NullFilter lf = (NullFilter)value;
                        
            output.startElement(element.getNamespace(),element.getName(),null);
            elems[0].getType().encode(elems[0],lf.getNullCheckValue(),output,hints); // PropertyName
//            elems[1].getType().encode(elems[1],lf.getNullCheckValue(),output,hints); // Literal
            output.endElement(element.getNamespace(),element.getName());
        }
    }
    public static class PropertyIsBetweenType extends FilterComplexType{
        private static final ComplexType instance = new PropertyIsBetweenType();
        public static ComplexType getInstance(){return instance;}

//    	<xsd:complexType name="PropertyIsBetweenType">
//    		<xsd:complexContent>
//    			<xsd:extension base="ogc:ComparisonOpsType">
//    				<xsd:sequence>
//    					<xsd:element ref="ogc:expression"/>
//    					<xsd:element name="LowerBoundary" type="ogc:LowerBoundaryType"/>
//    					<xsd:element name="UpperBoundary" type="ogc:UpperBoundaryType"/>
//    				</xsd:sequence>
//    			</xsd:extension>
//    		</xsd:complexContent>
//    	</xsd:complexType>
        
        public Type getParent(){
        	return ComparisonOpsType.getInstance();
        }
        
        private static Element[] elems = new Element[]{
        		new FilterElement("expression",ExpressionType.getInstance()),
        		new FilterElement("LowerBoundary",LowerBoundaryType.getInstance()),
        		new FilterElement("UpperBoundary",UpperBoundaryType.getInstance()),
        };
        
        private static Sequence seq = new DefaultSequence(elems);
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return seq;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "PropertyIsBetweenType";
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return BetweenFilter.class;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            return element.getType()!=null && getName().equals(element.getType().getName()) && value instanceof BetweenFilter;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            if(!canEncode(element,value,hints))
            	return;
            BetweenFilter lf = (BetweenFilter)value;
                        
            output.startElement(element.getNamespace(),element.getName(),null);
            elems[0].getType().encode(elems[0],lf.getMiddleValue(),output,hints); // expression
            elems[1].getType().encode(elems[1],lf.getLeftValue(),output,hints); // LowerBoundary
            elems[2].getType().encode(elems[2],lf.getRightValue(),output,hints); // UpperBoundary
            output.endElement(element.getNamespace(),element.getName());
        }
    }
    public static class LowerBoundaryType extends FilterComplexType{
        private static final ComplexType instance = new LowerBoundaryType();
        public static ComplexType getInstance(){return instance;}
        
//      <xsd:complexType name="LowerBoundaryType">
//		<xsd:choice>
//			<xsd:element ref="ogc:expression"/>
//		</xsd:choice>
//		</xsd:complexType>
        
        private static Element[] elems = new Element[] {
                new FilterElement("expression",ExpressionType.getInstance()),
        };
        
        private static Choice choice = new DefaultChoice(elems);
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return choice;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "LowerBoundaryType";
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return Expression.class;
        }
        public boolean canEncode(Element element, Object value, Map hints) {
            return element.getType()!=null && getName().equals(element.getType().getName()) && value instanceof Expression;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            if(!canEncode(element,value,hints))
            	return;
            Expression lf = (Expression)value;
                        
            output.startElement(element.getNamespace(),element.getName(),null);
            elems[0].getType().encode(elems[0],lf,output,hints); // expression
            output.endElement(element.getNamespace(),element.getName());
        }
    }
    public static class UpperBoundaryType extends FilterComplexType{
        private static final ComplexType instance = new UpperBoundaryType();
        public static ComplexType getInstance(){return instance;}
        
//    	<xsd:complexType name="UpperBoundaryType">
//    		<xsd:sequence>
//    			<xsd:element ref="ogc:expression"/>
//    		</xsd:sequence>
//    	</xsd:complexType>

        private static Element[] elems = new Element[] {
                new FilterElement("expression",ExpressionType.getInstance()),
        };
        
        private static Sequence choice = new DefaultSequence(elems);
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return choice;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "UpperBoundaryType";
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return Expression.class;
        }
        public boolean canEncode(Element element, Object value, Map hints) {
            return element.getType()!=null && getName().equals(element.getType().getName()) && value instanceof Expression;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            if(!canEncode(element,value,hints))
            	return;
            Expression lf = (Expression)value;
                        
            output.startElement(element.getNamespace(),element.getName(),null);
            elems[0].getType().encode(elems[0],lf,output,hints); // expression
            output.endElement(element.getNamespace(),element.getName());
        }
    }
    public static class BinarySpatialOpType extends FilterComplexType{
        private static final ComplexType instance = new BinarySpatialOpType();
        public static ComplexType getInstance(){return instance;}
        
//    	<xsd:complexType name="BinarySpatialOpType">
//    		<xsd:complexContent>
//    			<xsd:extension base="ogc:SpatialOpsType">
//    				<xsd:sequence>
//    					<xsd:element ref="ogc:PropertyName"/>
//    					<xsd:choice>
//    						<xsd:element ref="gml:_Geometry"/>
//    						<xsd:element ref="gml:Box"/>
//    					</xsd:choice>
//    				</xsd:sequence>
//    			</xsd:extension>
//    		</xsd:complexContent>
//    	</xsd:complexType>
        
        private static Element[] elems = new Element[]{
                new FilterElement("PropertyName",PropertyNameType.getInstance()),
                GMLSchema.getInstance().getElements()[29], // _Geometry
                GMLSchema.getInstance().getElements()[41]  // Box
        };
        
        private static Sequence child = new DefaultSequence(new ElementGrouping[]{
                elems[0],new DefaultChoice(new Element[]{elems[1],elems[2]})
        });
        
        public Type getParent(){
            return SpatialOpsType.getInstance();
        }
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return child;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "BinarySpatialOpType";
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return GeometryFilter.class;
        }
        public boolean canEncode(Element element, Object value, Map hints) {
            return element.getType()!=null && getName().equals(element.getType().getName()) && value instanceof GeometryFilter;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            if(!canEncode(element,value,hints))
            	return;
            GeometryFilter lf = (GeometryFilter)value;
                        
            output.startElement(element.getNamespace(),element.getName(),null);
            if(lf.getLeftGeometry().getType() == org.geotools.filter.ExpressionType.LITERAL_STRING){
                elems[0].getType().encode(elems[0],lf.getLeftGeometry(),output,hints); // prop name
                if(lf.getRightGeometry().getType() == org.geotools.filter.ExpressionType.LITERAL_GEOMETRY)
                    elems[1].getType().encode(elems[1],lf.getRightGeometry(),output,hints); // geom
                else
                    elems[2].getType().encode(elems[2],lf.getRightGeometry(),output,hints); // geom
            }else{
                if(lf.getRightGeometry().getType() == org.geotools.filter.ExpressionType.LITERAL_STRING){
                    elems[0].getType().encode(elems[0],lf.getRightGeometry(),output,hints); // prop name
                    if(lf.getLeftGeometry().getType() == org.geotools.filter.ExpressionType.LITERAL_GEOMETRY)
                        elems[1].getType().encode(elems[1],lf.getLeftGeometry(),output,hints); // geom
                    else
                        elems[2].getType().encode(elems[2],lf.getLeftGeometry(),output,hints); // geom
                }else{
                    throw new OperationNotSupportedException("Either the left or right expr must be a literal for the property name");
                }
            }
            
            output.endElement(element.getNamespace(),element.getName());
        }
    }
    public static class BBOXType extends FilterComplexType{
        private static final ComplexType instance = new BBOXType();
        public static ComplexType getInstance(){return instance;}
        
//    	<xsd:complexType name="BBOXType">
//		<xsd:complexContent>
//			<xsd:extension base="ogc:SpatialOpsType">
//				<xsd:sequence>
//					<xsd:element ref="ogc:PropertyName"/>
//					<xsd:element ref="gml:Box"/>
//				</xsd:sequence>
//			</xsd:extension>
//		</xsd:complexContent>
//		</xsd:complexType>
        
        private static Element[] elems = new Element[]{
                new FilterElement("PropertyName",PropertyNameType.getInstance()),
                GMLSchema.getInstance().getElements()[41]  // Box
        };
        
        private Sequence seq = new DefaultSequence(elems);

        
        public Type getParent(){
            return SpatialOpsType.getInstance();
        }
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return seq;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "BBOXType";
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return GeometryFilter.class;
        }
        public boolean canEncode(Element element, Object value, Map hints) {
            return element.getType()!=null && getName().equals(element.getType().getName()) && value instanceof GeometryFilter;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            if(!canEncode(element,value,hints))
            	return;
            GeometryFilter lf = (GeometryFilter)value;
                        
            output.startElement(element.getNamespace(),element.getName(),null);
            if(lf.getLeftGeometry().getType() == org.geotools.filter.ExpressionType.LITERAL_STRING){
                elems[0].getType().encode(elems[0],lf.getLeftGeometry(),output,hints); // prop name
                elems[1].getType().encode(elems[1],lf.getRightGeometry(),output,hints); // geom
            }else{
                if(lf.getRightGeometry().getType() == org.geotools.filter.ExpressionType.LITERAL_STRING){
                    elems[0].getType().encode(elems[0],lf.getRightGeometry(),output,hints); // prop name
                    elems[1].getType().encode(elems[1],lf.getLeftGeometry(),output,hints); // geom
                }else{
                    throw new OperationNotSupportedException("Either the left or right expr must be a literal for the property name");
                }
            }
            
            output.endElement(element.getNamespace(),element.getName());
        }
    }
    public static class DistanceBufferType extends FilterComplexType{
        private static final ComplexType instance = new DistanceBufferType();
        public static ComplexType getInstance(){return instance;}

//    	<xsd:complexType name="DistanceBufferType">
//    		<xsd:complexContent>
//    			<xsd:extension base="ogc:SpatialOpsType">
//    				<xsd:sequence>
//    					<xsd:element ref="ogc:PropertyName"/>
//    					<xsd:element ref="gml:_Geometry"/>
//    					<xsd:element name="Distance" type="ogc:DistanceType"/>
//    				</xsd:sequence>
//    			</xsd:extension>
//    		</xsd:complexContent>
//    	</xsd:complexType>

        private static Element[] elems = new Element[]{
                new FilterElement("PropertyName",PropertyNameType.getInstance()),
                GMLSchema.getInstance().getElements()[29], // _Geometry
                new FilterElement("Distance",DistanceType.getInstance())
        };
        
        private Sequence seq = new DefaultSequence(elems);

        
        public Type getParent(){
            return SpatialOpsType.getInstance();
        }
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return seq;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "DistanceBufferType";
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return GeometryDistanceFilter.class;
        }
        public boolean canEncode(Element element, Object value, Map hints) {
            return element.getType()!=null && getName().equals(element.getType().getName()) && value instanceof GeometryDistanceFilter;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            if(!canEncode(element,value,hints))
            	return;
            GeometryDistanceFilter lf = (GeometryDistanceFilter)value;
                        
            output.startElement(element.getNamespace(),element.getName(),null);
            if(lf.getLeftGeometry().getType() == org.geotools.filter.ExpressionType.LITERAL_STRING){
                elems[0].getType().encode(elems[0],lf.getLeftGeometry(),output,hints); // prop name
                elems[1].getType().encode(elems[1],lf.getRightGeometry(),output,hints); // geom
                elems[2].getType().encode(elems[2],lf,output,hints); // distancetype
            }else{
                if(lf.getRightGeometry().getType() == org.geotools.filter.ExpressionType.LITERAL_STRING){
                    elems[0].getType().encode(elems[0],lf.getRightGeometry(),output,hints); // prop name
                    elems[1].getType().encode(elems[1],lf.getLeftGeometry(),output,hints); // geom
                    elems[2].getType().encode(elems[2],lf,output,hints); // distancetype
                }else{
                    throw new OperationNotSupportedException("Either the left or right expr must be a literal for the property name");
                }
            }
            
            output.endElement(element.getNamespace(),element.getName());
        }
    }
    public static class DistanceType extends FilterComplexType{
        private static final ComplexType instance = new DistanceType();
        public static ComplexType getInstance(){return instance;}

//    	<xsd:complexType name="DistanceType" mixed="true">
//    		<xsd:attribute name="units" type="xsd:string" use="required"/>
//    	</xsd:complexType>
        
        private static Attribute[] attrs = new Attribute[]{
                new FilterAttribute("units",XSISimpleTypes.String.getInstance(),Attribute.REQUIRED),
        };
        
        public boolean isMixed(){
            return true;
        }
        
        public Attribute[] getAttributes(){
            return attrs;
        }
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "DistanceType";
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return GeometryDistanceFilter.class;
        }
        public boolean canEncode(Element element, Object value, Map hints) {
            return element.getType()!=null && getName().equals(element.getType().getName()) && value instanceof GeometryDistanceFilter;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            if(!canEncode(element,value,hints))
            	return;
            GeometryDistanceFilter lf = (GeometryDistanceFilter)value;
            
            AttributesImpl ai = new AttributesImpl();
            if(lf.getLeftGeometry().getType() == org.geotools.filter.ExpressionType.LITERAL_GEOMETRY)
                ai.addAttribute(getNamespace().toString(),attrs[0].getName(),null,"string", ((Geometry)lf.getLeftGeometry()).getUserData().toString());
            else
                ai.addAttribute(getNamespace().toString(),attrs[0].getName(),null,"string", ((Geometry)lf.getRightGeometry()).getUserData().toString());
            output.startElement(element.getNamespace(),element.getName(),null);
            output.characters(""+lf.getDistance());
            output.endElement(element.getNamespace(),element.getName());
        }
    }
    public static class BinaryLogicOpType extends FilterComplexType{
        private static final ComplexType instance = new BinaryLogicOpType();
public static ComplexType getInstance(){return instance;}

//    	<xsd:complexType name="BinaryLogicOpType">
//    		<xsd:complexContent>
//    			<xsd:extension base="ogc:LogicOpsType">
//    				<xsd:choice minOccurs="2" maxOccurs="unbounded">
//    					<xsd:element ref="ogc:comparisonOps"/>
//    					<xsd:element ref="ogc:spatialOps"/>
//    					<xsd:element ref="ogc:logicOps"/>
//    				</xsd:choice>
//    			</xsd:extension>
//    		</xsd:complexContent>
//    	</xsd:complexType>
        
        private static Element[] elems = new Element[]{
        		new FilterElement("comparisonOps",ComparisonOpsType.getInstance()),
        		new FilterElement("spatialOps",SpatialOpsType.getInstance()),
        		new FilterElement("logicOps",LogicOpsType.getInstance())
        };
        
        private static Choice choice = new DefaultChoice(null,2,Integer.MAX_VALUE ,elems);

		/* (non-Javadoc)
 * @see org.geotools.xml.schema.ComplexType#getParent()
 */
public Type getParent() {
	return LogicOpsType.getInstance();
}
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return choice;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "BinaryLogicOpType";
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return LogicFilter.class;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            return element.getType()!=null && getName().equals(element.getType().getName()) && value instanceof LogicFilter;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            if(!canEncode(element,value,hints))
            	return;
            
            LogicFilter lf= (LogicFilter)value;
            Iterator i = lf.getFilterIterator();
            output.startElement(element.getNamespace(),element.getName(),null);
            while(i.hasNext())
            	FilterType.getInstance().encode(null,i.next(),output,hints);
            output.endElement(element.getNamespace(),element.getName());
        }
    }
    
    public static class UnaryLogicOpType extends FilterComplexType{
        private static final ComplexType instance = new UnaryLogicOpType();
        public static ComplexType getInstance(){return instance;}
        

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getParent()
		 */
		public Type getParent() {
			return LogicOpsType.getInstance();
		}
//    	<xsd:complexType name="UnaryLogicOpType">
//    		<xsd:complexContent>
//    			<xsd:extension base="ogc:LogicOpsType">
//    				<xsd:sequence>
//    					<xsd:choice>
//    						<xsd:element ref="ogc:comparisonOps"/>
//    						<xsd:element ref="ogc:spatialOps"/>
//    						<xsd:element ref="ogc:logicOps"/>
//    					</xsd:choice>
//    				</xsd:sequence>
//    			</xsd:extension>
//    		</xsd:complexContent>
//    	</xsd:complexType>
        
        private static Element[] elems = new Element[]{
        		new FilterElement("comparisonOps",ComparisonOpsType.getInstance()),
        		new FilterElement("spatialOps",SpatialOpsType.getInstance()),
        		new FilterElement("logicOps",LogicOpsType.getInstance())
        };
        
        private static Choice choice = new DefaultChoice(elems);
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return choice;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }
        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "UnaryLogicOpType";
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return LogicFilter.class;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            return element.getType()!=null && getName().equals(element.getType().getName()) && value instanceof LogicFilter;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            if(!canEncode(element,value,hints))
            	return;
            
            LogicFilter lf= (LogicFilter)value;
            Iterator i = lf.getFilterIterator();
            output.startElement(element.getNamespace(),element.getName(),null);
            int c = 0;
            while(i.hasNext()){
            	if(c<1){
            	FilterType.getInstance().encode(null,i.next(),output,hints);
            	c++;
            	}else{
            		throw new OperationNotSupportedException("Invalid Not Filter -- more than one child filter.");
            	}
            }
            output.endElement(element.getNamespace(),element.getName());
        }
    }
}
