/*
 * Created on May 23, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.xml;

import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.Schema;

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
    }
}
