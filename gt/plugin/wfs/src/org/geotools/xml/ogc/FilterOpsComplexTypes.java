
package org.geotools.xml.ogc;

import java.io.IOException;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.geotools.xml.PrintHandler;
import org.geotools.xml.schema.Attribute;
import org.geotools.xml.schema.ComplexType;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.ElementGrouping;
import org.geotools.xml.schema.ElementValue;
import org.geotools.xml.schema.Type;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class FilterOpsComplexTypes {
    public static class ComparisonOpsType implements ComplexType{
        private static final ComplexType instance = new ComparisonOpsType();
        public static ComplexType getInstance(){return instance;}
        /**
         * @see org.geotools.xml.schema.ComplexType#getParent()
         */
        public Type getParent() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getBlock()
         */
        public int getBlock() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getFinal()
         */
        public int getFinal() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getId()
         */
        public String getId() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isMixed()
         */
        public boolean isMixed() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isDerived()
         */
        public boolean isDerived() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element, java.util.Map)
         */
        public boolean cache(Element element, Map hints) {
            // TODO Auto-generated method stub
            return false;
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
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getNamespace()
         */
        public String getNamespace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            
        }
        /**
         * @see org.geotools.xml.schema.Type#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            // TODO Auto-generated method stub
            return null;
        }
    }
    public static class SpatialOpsType implements ComplexType{
        private static final ComplexType instance = new SpatialOpsType();
        public static ComplexType getInstance(){return instance;}
        /**
         * @see org.geotools.xml.schema.ComplexType#getParent()
         */
        public Type getParent() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getBlock()
         */
        public int getBlock() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getFinal()
         */
        public int getFinal() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getId()
         */
        public String getId() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isMixed()
         */
        public boolean isMixed() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isDerived()
         */
        public boolean isDerived() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element, java.util.Map)
         */
        public boolean cache(Element element, Map hints) {
            // TODO Auto-generated method stub
            return false;
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
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getNamespace()
         */
        public String getNamespace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            
        }
        /**
         * @see org.geotools.xml.schema.Type#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            // TODO Auto-generated method stub
            return null;
        }
    }
    public static class LogicOpsType implements ComplexType{
        private static final ComplexType instance = new LogicOpsType();
        public static ComplexType getInstance(){return instance;}
        /**
         * @see org.geotools.xml.schema.ComplexType#getParent()
         */
        public Type getParent() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getBlock()
         */
        public int getBlock() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getFinal()
         */
        public int getFinal() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getId()
         */
        public String getId() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isMixed()
         */
        public boolean isMixed() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isDerived()
         */
        public boolean isDerived() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element, java.util.Map)
         */
        public boolean cache(Element element, Map hints) {
            // TODO Auto-generated method stub
            return false;
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
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getNamespace()
         */
        public String getNamespace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            
        }
        /**
         * @see org.geotools.xml.schema.Type#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            // TODO Auto-generated method stub
            return null;
        }
    }
    public static class FilterType implements ComplexType{
        private static final ComplexType instance = new FilterType();
        public static ComplexType getInstance(){return instance;}
        /**
         * @see org.geotools.xml.schema.ComplexType#getParent()
         */
        public Type getParent() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getBlock()
         */
        public int getBlock() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getFinal()
         */
        public int getFinal() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getId()
         */
        public String getId() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isMixed()
         */
        public boolean isMixed() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isDerived()
         */
        public boolean isDerived() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element, java.util.Map)
         */
        public boolean cache(Element element, Map hints) {
            // TODO Auto-generated method stub
            return false;
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
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getNamespace()
         */
        public String getNamespace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            
        }
        /**
         * @see org.geotools.xml.schema.Type#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            // TODO Auto-generated method stub
            return null;
        }
    }
    public static class FeatureIdType implements ComplexType{
        private static final ComplexType instance = new FeatureIdType();
        public static ComplexType getInstance(){return instance;}
        /**
         * @see org.geotools.xml.schema.ComplexType#getParent()
         */
        public Type getParent() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getBlock()
         */
        public int getBlock() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getFinal()
         */
        public int getFinal() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getId()
         */
        public String getId() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isMixed()
         */
        public boolean isMixed() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isDerived()
         */
        public boolean isDerived() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element, java.util.Map)
         */
        public boolean cache(Element element, Map hints) {
            // TODO Auto-generated method stub
            return false;
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
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getNamespace()
         */
        public String getNamespace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            
        }
        /**
         * @see org.geotools.xml.schema.Type#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            // TODO Auto-generated method stub
            return null;
        }
    }
    public static class BinaryComparisonOpType implements ComplexType{
        private static final ComplexType instance = new BinaryComparisonOpType();
        public static ComplexType getInstance(){return instance;}
        /**
         * @see org.geotools.xml.schema.ComplexType#getParent()
         */
        public Type getParent() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getBlock()
         */
        public int getBlock() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getFinal()
         */
        public int getFinal() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getId()
         */
        public String getId() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isMixed()
         */
        public boolean isMixed() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isDerived()
         */
        public boolean isDerived() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element, java.util.Map)
         */
        public boolean cache(Element element, Map hints) {
            // TODO Auto-generated method stub
            return false;
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
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getNamespace()
         */
        public String getNamespace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            
        }
        /**
         * @see org.geotools.xml.schema.Type#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            // TODO Auto-generated method stub
            return null;
        }
    }
    public static class PropertyIsLikeType implements ComplexType{
        private static final ComplexType instance = new PropertyIsLikeType();
        public static ComplexType getInstance(){return instance;}
        /**
         * @see org.geotools.xml.schema.ComplexType#getParent()
         */
        public Type getParent() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getBlock()
         */
        public int getBlock() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getFinal()
         */
        public int getFinal() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getId()
         */
        public String getId() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isMixed()
         */
        public boolean isMixed() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isDerived()
         */
        public boolean isDerived() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element, java.util.Map)
         */
        public boolean cache(Element element, Map hints) {
            // TODO Auto-generated method stub
            return false;
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
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getNamespace()
         */
        public String getNamespace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            
        }
        /**
         * @see org.geotools.xml.schema.Type#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            // TODO Auto-generated method stub
            return null;
        }
    }
    public static class PropertyIsNullType implements ComplexType{
        private static final ComplexType instance = new PropertyIsNullType();
        public static ComplexType getInstance(){return instance;}
        /**
         * @see org.geotools.xml.schema.ComplexType#getParent()
         */
        public Type getParent() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getBlock()
         */
        public int getBlock() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getFinal()
         */
        public int getFinal() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getId()
         */
        public String getId() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isMixed()
         */
        public boolean isMixed() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isDerived()
         */
        public boolean isDerived() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element, java.util.Map)
         */
        public boolean cache(Element element, Map hints) {
            // TODO Auto-generated method stub
            return false;
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
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getNamespace()
         */
        public String getNamespace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            
        }
        /**
         * @see org.geotools.xml.schema.Type#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            // TODO Auto-generated method stub
            return null;
        }
    }
    public static class PropertyIsBetweenType implements ComplexType{
        private static final ComplexType instance = new PropertyIsBetweenType();
        public static ComplexType getInstance(){return instance;}
        /**
         * @see org.geotools.xml.schema.ComplexType#getParent()
         */
        public Type getParent() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getBlock()
         */
        public int getBlock() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getFinal()
         */
        public int getFinal() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getId()
         */
        public String getId() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isMixed()
         */
        public boolean isMixed() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isDerived()
         */
        public boolean isDerived() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element, java.util.Map)
         */
        public boolean cache(Element element, Map hints) {
            // TODO Auto-generated method stub
            return false;
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
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getNamespace()
         */
        public String getNamespace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            
        }
        /**
         * @see org.geotools.xml.schema.Type#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            // TODO Auto-generated method stub
            return null;
        }
    }
    public static class LowerBoundaryType implements ComplexType{
        private static final ComplexType instance = new LowerBoundaryType();
        public static ComplexType getInstance(){return instance;}
        /**
         * @see org.geotools.xml.schema.ComplexType#getParent()
         */
        public Type getParent() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getBlock()
         */
        public int getBlock() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getFinal()
         */
        public int getFinal() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getId()
         */
        public String getId() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isMixed()
         */
        public boolean isMixed() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isDerived()
         */
        public boolean isDerived() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element, java.util.Map)
         */
        public boolean cache(Element element, Map hints) {
            // TODO Auto-generated method stub
            return false;
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
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getNamespace()
         */
        public String getNamespace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            
        }
        /**
         * @see org.geotools.xml.schema.Type#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            // TODO Auto-generated method stub
            return null;
        }
    }
    public static class UpperBoundaryType implements ComplexType{
        private static final ComplexType instance = new UpperBoundaryType();
        public static ComplexType getInstance(){return instance;}
        /**
         * @see org.geotools.xml.schema.ComplexType#getParent()
         */
        public Type getParent() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getBlock()
         */
        public int getBlock() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getFinal()
         */
        public int getFinal() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getId()
         */
        public String getId() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isMixed()
         */
        public boolean isMixed() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isDerived()
         */
        public boolean isDerived() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element, java.util.Map)
         */
        public boolean cache(Element element, Map hints) {
            // TODO Auto-generated method stub
            return false;
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
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getNamespace()
         */
        public String getNamespace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            
        }
        /**
         * @see org.geotools.xml.schema.Type#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            // TODO Auto-generated method stub
            return null;
        }
    }
    public static class BinarySpatialOpType implements ComplexType{
        private static final ComplexType instance = new BinarySpatialOpType();
        public static ComplexType getInstance(){return instance;}
        /**
         * @see org.geotools.xml.schema.ComplexType#getParent()
         */
        public Type getParent() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getBlock()
         */
        public int getBlock() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getFinal()
         */
        public int getFinal() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getId()
         */
        public String getId() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isMixed()
         */
        public boolean isMixed() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isDerived()
         */
        public boolean isDerived() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element, java.util.Map)
         */
        public boolean cache(Element element, Map hints) {
            // TODO Auto-generated method stub
            return false;
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
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getNamespace()
         */
        public String getNamespace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            
        }
        /**
         * @see org.geotools.xml.schema.Type#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            // TODO Auto-generated method stub
            return null;
        }
    }
    public static class BBOXType implements ComplexType{
        private static final ComplexType instance = new BBOXType();
        public static ComplexType getInstance(){return instance;}
        /**
         * @see org.geotools.xml.schema.ComplexType#getParent()
         */
        public Type getParent() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getBlock()
         */
        public int getBlock() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getFinal()
         */
        public int getFinal() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getId()
         */
        public String getId() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isMixed()
         */
        public boolean isMixed() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isDerived()
         */
        public boolean isDerived() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element, java.util.Map)
         */
        public boolean cache(Element element, Map hints) {
            // TODO Auto-generated method stub
            return false;
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
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getNamespace()
         */
        public String getNamespace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            
        }
        /**
         * @see org.geotools.xml.schema.Type#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            // TODO Auto-generated method stub
            return null;
        }
    }
    public static class DistanceBufferType implements ComplexType{
        private static final ComplexType instance = new DistanceBufferType();
        public static ComplexType getInstance(){return instance;}
        /**
         * @see org.geotools.xml.schema.ComplexType#getParent()
         */
        public Type getParent() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getBlock()
         */
        public int getBlock() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getFinal()
         */
        public int getFinal() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getId()
         */
        public String getId() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isMixed()
         */
        public boolean isMixed() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isDerived()
         */
        public boolean isDerived() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element, java.util.Map)
         */
        public boolean cache(Element element, Map hints) {
            // TODO Auto-generated method stub
            return false;
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
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getNamespace()
         */
        public String getNamespace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            
        }
        /**
         * @see org.geotools.xml.schema.Type#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            // TODO Auto-generated method stub
            return null;
        }
    }
    public static class DistanceType implements ComplexType{
        private static final ComplexType instance = new DistanceType();
        public static ComplexType getInstance(){return instance;}
        /**
         * @see org.geotools.xml.schema.ComplexType#getParent()
         */
        public Type getParent() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getBlock()
         */
        public int getBlock() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getFinal()
         */
        public int getFinal() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getId()
         */
        public String getId() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isMixed()
         */
        public boolean isMixed() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isDerived()
         */
        public boolean isDerived() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element, java.util.Map)
         */
        public boolean cache(Element element, Map hints) {
            // TODO Auto-generated method stub
            return false;
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
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getNamespace()
         */
        public String getNamespace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            
        }
        /**
         * @see org.geotools.xml.schema.Type#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            // TODO Auto-generated method stub
            return null;
        }
    }
    public static class BinaryLogicOpType implements ComplexType{
        private static final ComplexType instance = new BinaryLogicOpType();
        public static ComplexType getInstance(){return instance;}
        /**
         * @see org.geotools.xml.schema.ComplexType#getParent()
         */
        public Type getParent() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getBlock()
         */
        public int getBlock() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getFinal()
         */
        public int getFinal() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getId()
         */
        public String getId() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isMixed()
         */
        public boolean isMixed() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isDerived()
         */
        public boolean isDerived() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element, java.util.Map)
         */
        public boolean cache(Element element, Map hints) {
            // TODO Auto-generated method stub
            return false;
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
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getNamespace()
         */
        public String getNamespace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            
        }
        /**
         * @see org.geotools.xml.schema.Type#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            // TODO Auto-generated method stub
            return null;
        }
    }
    public static class UnaryLogicOpType implements ComplexType{
        private static final ComplexType instance = new UnaryLogicOpType();
        public static ComplexType getInstance(){return instance;}
        /**
         * @see org.geotools.xml.schema.ComplexType#getParent()
         */
        public Type getParent() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isAbstract()
         */
        public boolean isAbstract() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAnyAttributeNameSpace()
         */
        public String getAnyAttributeNameSpace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getBlock()
         */
        public int getBlock() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getFinal()
         */
        public int getFinal() {
            // TODO Auto-generated method stub
            return 0;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#getId()
         */
        public String getId() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isMixed()
         */
        public boolean isMixed() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#isDerived()
         */
        public boolean isDerived() {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.ComplexType#cache(org.geotools.xml.schema.Element, java.util.Map)
         */
        public boolean cache(Element element, Map hints) {
            // TODO Auto-generated method stub
            return false;
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
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getNamespace()
         */
        public String getNamespace() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }
        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }
        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            
        }
        /**
         * @see org.geotools.xml.schema.Type#findChildElement(java.lang.String)
         */
        public Element findChildElement(String name) {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
