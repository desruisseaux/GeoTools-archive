
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
public class FilterComplexTypes {
    public static class Comparison_OperatorsType implements ComplexType{
        private static final ComplexType instance = new Comparison_OperatorsType();
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
    public static class Function_NameType implements ComplexType{
        private static final ComplexType instance = new Function_NameType();
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
    public static class Function_NamesType implements ComplexType{
        private static final ComplexType instance = new Function_NamesType();
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
    public static class FunctionsType implements ComplexType{
        private static final ComplexType instance = new FunctionsType();
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
    public static class Filter_CapabilitiesType implements ComplexType{
        private static final ComplexType instance = new Filter_CapabilitiesType();
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
    public static class Scalar_CapabilitiesType implements ComplexType{
        private static final ComplexType instance = new Scalar_CapabilitiesType();
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
    public static class Spatial_CapabilitiesType implements ComplexType{
        private static final ComplexType instance = new Spatial_CapabilitiesType();
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
    public static class Spatial_OperatorsType implements ComplexType{
        private static final ComplexType instance = new Spatial_OperatorsType();
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
    public static class ExpressionType implements ComplexType{
        private static final ComplexType instance = new ExpressionType();
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
    public static class BinaryOperatorType implements ComplexType{
        private static final ComplexType instance = new BinaryOperatorType();
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
    public static class FunctionType implements ComplexType{
        private static final ComplexType instance = new FunctionType();
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
    public static class LiteralType implements ComplexType{
        private static final ComplexType instance = new LiteralType();
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
    public static class PropertyNameType implements ComplexType{
        private static final ComplexType instance = new PropertyNameType();
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
    public static class ServiceExceptionType implements ComplexType{
        private static final ComplexType instance = new ServiceExceptionType();
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
    public static class ServiceExceptionReportType implements ComplexType{
        private static final ComplexType instance = new ServiceExceptionReportType();
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
