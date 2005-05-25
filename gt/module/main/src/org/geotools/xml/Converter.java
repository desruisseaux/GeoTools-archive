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
        if( in instanceof org.geotools.xml.schema.Element ){
            return (org.geotools.xml.schema.Element ) in;
        }
        else {
            // we need a wraper that looks up the stratagy object?!
            //
            throw new UnsupportedOperationException("We need a wraper of element+stratagy"); // FIXME
        }
	}
	protected static com.vividsolutions.xdo.xsi.Element convert( org.geotools.xml.schema.Element in){
        return in; // this is a nop
        //return new ElementWrapper(in);
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

        
        public AttributeGroup[] getAttributeGroups() {
            return super.getAttributeGroups();
        }

        
        public Attribute[] getAttributes() {
            return super.getAttributes();
        }

        
        public int getBlockDefault() {
            return super.getBlockDefault();
        }

        
        public ComplexType[] getComplexTypes() {
            return super.getComplexTypes();
        }

        
        public com.vividsolutions.xdo.xsi.Element[] getElements() {
            return super.getElements();
        }

        
        public int getFinalDefault() {
            return super.getFinalDefault();
        }

        
        public Group[] getGroups() {
            return super.getGroups();
        }

        
        public Import[] getImports() {
            return super.getImports();
        }

        
        public String getPrefix() {
            return super.getPrefix();
        }

        
        public SimpleType[] getSimpleTypes() {
            return super.getSimpleTypes();
        }

        
        public URI getTargetNamespace() {
            return super.getTargetNamespace();
        }

        
        public URI[] getUris() {
            return super.getUris();
        }

        
        public String getVersion() {
            return super.getVersion();
        }

        
        public boolean isAttributeFormDefault() {
            return super.isAttributeFormDefault();
        }

        
        public boolean isElementFormDefault() {
            return super.isElementFormDefault();
        }

        
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

        
        public int getFinal() {
            return -1; //TODO no direct mapping
        }

        
        public String getName() {
            return gtType.getName();
        }

        
        public URI getNamespace() {
            return gtType.getNamespace();
        }

        
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
    /* No longer needed 
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

        
        public int getBlock() {
            return gtElement.getBlock();
        }

        
        public String getDefault() {
            return gtElement.getDefault();
        }

        
        public int getFinal() {
            return gtElement.getFinal();
        }

        
        public String getFixed() {
            return gtElement.getFixed();
        }

        
        public String getName() {
            return gtElement.getName();
        }

        
        public URI getNamespace() {
            return gtElement.getNamespace();
        }

        
        public com.vividsolutions.xdo.xsi.Element getSubstitutionGroup() {
            return convert(gtElement.getSubstitutionGroup());
        }

        
        public Type getType() {
            return convert(gtElement.getType());
        }

        
        public boolean isAbstract() {
            return gtElement.isAbstract();
        }

        
        public boolean isForm() {
            return gtElement.isForm();
        }

        
        public boolean isNillable() {
            return gtElement.isNillable();
        }

        
        public int getGrouping() {
            return gtElement.getGrouping();
        }

        
        public int getMaxOccurs() {
            return gtElement.getMaxOccurs();
        }

        
        public int getMinOccurs() {
            return gtElement.getMinOccurs();
        }

        
        public String getId() {
            return gtElement.getId();
        }

        
        public boolean equals( Object obj ) {
            return gtElement.equals(obj);
        }
    }*/
}
