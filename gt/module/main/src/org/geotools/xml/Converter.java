/*
 * Created on May 23, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.xml;

import java.net.URI;

import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.Schema;

import com.vividsolutions.xdo.xsi.Attribute;
import com.vividsolutions.xdo.xsi.AttributeGroup;
import com.vividsolutions.xdo.xsi.ComplexType;
import com.vividsolutions.xdo.xsi.Group;
import com.vividsolutions.xdo.xsi.Import;
import com.vividsolutions.xdo.xsi.SimpleType;
import com.vividsolutions.xdo.xsi.Type;

/**
 * @author Administrator
 */
public class Converter {
	protected static org.geotools.xml.schema.Schema convert(com.vividsolutions.xdo.xsi.Schema in){
	    SchemaWrapper wrapper = (SchemaWrapper) in;
        return wrapper.getGtSchema();
	}
	protected static com.vividsolutions.xdo.xsi.Schema convert(org.geotools.xml.schema.Schema in){
        return new SchemaWrapper(in);
	}
	protected static org.geotools.xml.schema.Element convert(com.vividsolutions.xdo.xsi.Element in){
	    ElementWrapper wrapper = (ElementWrapper) in;
        return wrapper.getGtElement();
	}
	protected static com.vividsolutions.xdo.xsi.Element convert(org.geotools.xml.schema.Element in){
        return new ElementWrapper(in);
	}
	protected static org.geotools.xml.schema.Type convert(com.vividsolutions.xdo.xsi.Type in){
        TypeWrapper wrapper = (TypeWrapper) in;
        return wrapper.getGtType();
    }
	protected static com.vividsolutions.xdo.xsi.Type convert(org.geotools.xml.schema.Type in){
        return new TypeWrapper(in);
    }
    
    
    static class SchemaWrapper extends com.vividsolutions.xdo.xsi.Schema {
        org.geotools.xml.schema.Schema gtSchema;

        public SchemaWrapper( Schema schema ) {
            super();
            gtSchema = schema;
        }

        public org.geotools.xml.schema.Schema getGtSchema() {
            return gtSchema;
        }

        public void setGtSchema( org.geotools.xml.schema.Schema gtSchema ) {
            this.gtSchema = gtSchema;
        }

        @Override
        public AttributeGroup[] getAttributeGroups() {
            return super.getAttributeGroups();
        }

        @Override
        public Attribute[] getAttributes() {
            return super.getAttributes();
        }

        @Override
        public int getBlockDefault() {
            return super.getBlockDefault();
        }

        @Override
        public ComplexType[] getComplexTypes() {
            return super.getComplexTypes();
        }

        @Override
        public com.vividsolutions.xdo.xsi.Element[] getElements() {
            return super.getElements();
        }

        @Override
        public int getFinalDefault() {
            return super.getFinalDefault();
        }

        @Override
        public Group[] getGroups() {
            return super.getGroups();
        }

        @Override
        public Import[] getImports() {
            return super.getImports();
        }

        @Override
        public String getPrefix() {
            return super.getPrefix();
        }

        @Override
        public SimpleType[] getSimpleTypes() {
            return super.getSimpleTypes();
        }

        @Override
        public URI getTargetNamespace() {
            return super.getTargetNamespace();
        }

        @Override
        public URI[] getUris() {
            return super.getUris();
        }

        @Override
        public String getVersion() {
            return super.getVersion();
        }

        @Override
        public boolean isAttributeFormDefault() {
            return super.isAttributeFormDefault();
        }

        @Override
        public boolean isElementFormDefault() {
            return super.isElementFormDefault();
        }

        @Override
        public String getId() {
            return super.getId();
        }
        
        
    }
    
    static class TypeWrapper extends com.vividsolutions.xdo.xsi.Type {
        org.geotools.xml.schema.Type gtType;

        public TypeWrapper( org.geotools.xml.schema.Type type ) {
            super();
            gtType = type;
        }

        @Override
        public int getFinal() {
            return -1; //TODO no direct mapping
        }

        @Override
        public String getName() {
            return gtType.getName();
        }

        @Override
        public URI getNamespace() {
            return gtType.getNamespace();
        }

        @Override
        public String getId() {
            return null; //TODO no direct mapping
        }

        public org.geotools.xml.schema.Type getGtType() {
            return gtType;
        }

        public void setGtType( org.geotools.xml.schema.Type gtType ) {
            this.gtType = gtType;
        }
        
    }
    
    static class ElementWrapper extends com.vividsolutions.xdo.xsi.Element {
        org.geotools.xml.schema.Element gtElement;

        public ElementWrapper( Element element ) {
            super();
            gtElement = element;
        }

        public org.geotools.xml.schema.Element getGtElement() {
            return gtElement;
        }

        public void setGtElement( org.geotools.xml.schema.Element gtElement ) {
            this.gtElement = gtElement;
        }

        @Override
        public int getBlock() {
            return gtElement.getBlock();
        }

        @Override
        public String getDefault() {
            return gtElement.getDefault();
        }

        @Override
        public int getFinal() {
            return gtElement.getFinal();
        }

        @Override
        public String getFixed() {
            return gtElement.getFixed();
        }

        @Override
        public String getName() {
            return gtElement.getName();
        }

        @Override
        public URI getNamespace() {
            return gtElement.getNamespace();
        }

        @Override
        public com.vividsolutions.xdo.xsi.Element getSubstitutionGroup() {
            return convert(gtElement.getSubstitutionGroup());
        }

        @Override
        public Type getType() {
            return convert(gtElement.getType());
        }

        @Override
        public boolean isAbstract() {
            return gtElement.isAbstract();
        }

        @Override
        public boolean isForm() {
            return gtElement.isForm();
        }

        @Override
        public boolean isNillable() {
            return gtElement.isNillable();
        }

        @Override
        public int getGrouping() {
            return gtElement.getGrouping();
        }

        @Override
        public int getMaxOccurs() {
            return gtElement.getMaxOccurs();
        }

        @Override
        public int getMinOccurs() {
            return gtElement.getMinOccurs();
        }

        @Override
        public String getId() {
            return gtElement.getId();
        }

        @Override
        public boolean equals( Object obj ) {
            return gtElement.equals(obj);
        }
    }
}
