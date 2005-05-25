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
package org.geotools.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.OperationNotSupportedException;

import org.geotools.xml.schema.All;
import org.geotools.xml.schema.Any;
import org.geotools.xml.schema.Attribute;
import org.geotools.xml.schema.AttributeGroup;
import org.geotools.xml.schema.Choice;
import org.geotools.xml.schema.ComplexType;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.ElementGrouping;
import org.geotools.xml.schema.Facet;
import org.geotools.xml.schema.Group;
import org.geotools.xml.schema.Schema;
import org.geotools.xml.schema.Sequence;
import org.geotools.xml.schema.SimpleType;
import org.geotools.xml.schema.Type;
import org.geotools.xml.xsi.XSISimpleTypes;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import com.vividsolutions.xdo.Encoder;
import com.vividsolutions.xdo.Node;
import com.vividsolutions.xdo.Strategy;
import com.vividsolutions.xdo.schemas.XSDSchema;
import com.vividsolutions.xdo.xsi.Import;


/**
 * This is the thing that writes documents.
 * 
 * <p>
 * This will create valid XML documents, given an object and a schema.
 * </p>
 *
 * @author dzwiers
 */
public class DocumentWriter extends Converter{
    /** DOCUMENT ME! */
    public static final Logger logger = Logger.getLogger(
            "net.refractions.xml.write");
    private static Level level = Level.WARNING;

    /**
     * Writer ... include the key to represent true when writing to files,
     * include a Writer to write to otherwise.
     */
    public static final String WRITE_SCHEMA = "DocumentWriter_WRITE_SCHEMA";

    /**
     * Element or String ... include a ref to an Element to be used, or a
     * string representing the name of the element
     */
    public static final String BASE_ELEMENT = "DocumentWriter_BASE_ELEMENT";

    /**
     * Schema[] or String[]... The order to search the schemas for a valid
     * element, either an array of ref to Schema instances or an Array or
     * TargetNamespaces
     */
    public static final String SCHEMA_ORDER = "DocumentWriter_SCHEMA_ORDER";

    // TODO implement this searchOrder

    /**
     * boolean ... include the key to use the "nearest" strategy for searching
     * schemas. This will be ignored if a schema order was set. When not
     * included the schema order as they appear in the orginal schema will be
     * used.
     */
    public static final String USE_NEAREST = "DocumentWriter_USE_NEAREST";

    /** a map of URI->URI representing targetNamespace->Location */
    public static final String SCHEMA_LOCATION_HINT = "DocumentWriter_SCHEMA_LOCATION_HINT";

    /**
     * Sets the logger level
     * 
     * @param l Level
     */
    public static void setLevel(Level l) {
        level = l;
        logger.setLevel(l);
    }

    static GTStrategyBuilder stratagyBuilder = new GTStrategyBuilder();
    
    /**
     * Write value to file using provided schema.
     * 
     * <p>
     * Hints:
     * 
     * <ul>
     * <li>
     * WRITE_SCHEMA - (non null) write to outputfilename.xsd
     * </li>
     * <li>
     * BASE_ELEMENT - (Element) mapping of value to element instance
     * </li>
     * <li>
     * USE_NEAREST - (Boolean) not implemented
     * </li>
     * <li>
     * SCHEMA_ORDER - (String[] or Schema[]) resolve ambiguity & import
     * </li>
     * </ul>
     * </p>
     *
     * @param value
     * @param schema
     * @param f
     * @param hints
     *
     * @throws OperationNotSupportedException
     * @throws IOException
     *
     */
    public static void writeDocument(Object value, Schema schema, File f,
        Map hints) throws OperationNotSupportedException, IOException {
        if ((f == null) || (!f.canWrite())) {
            throw new IOException("Cannot write to " + f);
        }

        if ((hints != null) && hints.containsKey(WRITE_SCHEMA)) {
            Map hints2 = new HashMap(hints);
            hints2.remove(WRITE_SCHEMA);

            File f2 = new File(f.getParentFile(),
                    f.getName().substring(0, f.getName().indexOf(".")) + ".xsd");
            FileWriter wf = new FileWriter(f2);
            writeSchema(schema, wf, hints2);
            wf.close();
        }

        FileWriter wf = new FileWriter(f);
        writeDocument(value, schema, wf, hints);
        wf.close();
    }

    /**
     * Entry Point to Document writer.
     * 
     * <p>
     * Hints:
     * 
     * <ul>
     * <li>
     * WRITE_SCHEMA - (Writer) will be used to write the schema
     * </li>
     * <li>
     * BASE_ELEMENT - (Element) mapping of value to element instance
     * </li>
     * <li>
     * USE_NEAREST - (Boolean) not implemented
     * </li>
     * <li>
     * SCHEMA_ORDER - (String[] or Schema[]) resolve ambiguity & import
     * </li>
     * </ul>
     * </p>
     *
     * @param value
     * @param schema
     * @param w
     * @param hints optional hints for writing
     *
     * @throws OperationNotSupportedException
     * @throws IOException
     * @deprecated
     *
     */
    public static void writeDocument(Object value, Schema schema, Writer w,
        Map hints) throws OperationNotSupportedException, IOException {
    	
        if ((hints != null) && hints.containsKey(WRITE_SCHEMA)) {
            Writer w2 = (Writer) hints.get(WRITE_SCHEMA);
            Map m = new HashMap();
            m.putAll(hints);
            m.remove(WRITE_SCHEMA);
            writeDocument(schema, SchemaFactory.getInstance(XSDSchema.getInstance().getTargetNamespace()),  w2, m);

        }
        /** e = element being considered */
        com.vividsolutions.xdo.xsi.Element e = null;
        logger.setLevel(level);

        if (hints != null && hints.containsKey(BASE_ELEMENT)) {
            // hint provided the base element - good for tie breaker
            // when applicaiton schema is not explicit about mapping
            // (eg. WFS allows WFS and GML representation of FeatureCollection)
            //
            e = (Element) hints.get(BASE_ELEMENT);
        }

        if (e == null) {                    	
            // Yes dzwiers you can inline this code for speed
            e = findElementThatCanEncode( schema.getElements(), value, hints );
            /*
            com.vividsolutions.xdo.xsi.Element[] elems = schema.getElements();
            if (elems != null) {
                for (int j = 0; j < elems.length && e == null; j++){
                    if( elems[j] instanceof Element){
                        Element gtElement = (Element) elems[j];
                        if (gtElement.getType() != null){
                            Type gtType = (Type) gtElement.getType();
                            if( gtType.canEncode(gtElement,value, hints)) {
                                e = gtElement;
                            }
                        }
                    }
                    else {
                        // we need to do some kind of stratagy lookup?
                        com.vividsolutions.xdo.xsi.Element xdoElement = elems[j];
                        Strategy stratagy = stratagyBuilder.build( xdoElement, hints );
                        if( stratagy != null && stratagy.canEncode( xdoElement, hints )){
                            e = xdoElement;
                        }
                    }
                }
            }
            */
        }
        if (e == null) {
            /* schema no longer has imports with elements?
        	for (int i = 0; i < schema.getImports().length && e == null; i++) {                
                Import aImport = schema.getImports()[i];
                Element[] elems = schema.getImports()[i].getElements();
	
	            if (elems != null) {
	                for (int j = 0; j < elems.length; j++)
	                    if ((elems[j].getType() != null)
	                            && elems[j].getType().canEncode(elems[j],
	                                value, hints)) {
	                        e = elems[j];
	                    }
	            }
        	}*/
            
            // Schema does have elements for us to work through though ...
            // imagine element list is complete, or more likely ...
            
            // Q: are supposed to use the Import to locate another schema?
            // a: yep that appears to be in the API

            // Q: Are we supposed to check its imports as well?
            // A: Smacks of recursion (dzwiers does not usually do that)
            // A: Stratagy object handles all this better, we will just maintain
            //    parity with what was here before
            //
            Import imports[] = schema.getImports();
            if( imports != null || imports.length != 0 ){
                for( int i=0; i < imports.length && e == null; i++ ){
                    Import aImport = imports[i];
                    com.vividsolutions.xdo.xsi.Schema aSchema = aImport.getSchema();
                    
                    // Yes dzwiers you can inline this code for speed
                    e = findElementThatCanEncode( aSchema.getElements(), value, hints );                    
                    
                    // note did not seach
                }
            }            
        }
        
        Node n = new Node(convert(e),value);
        Encoder.writeDocument(n,w,hints);
        
        w.flush();
    }
    
    /**
     * Write value to file using provided schema.
     * 
     * <p>
     * Hints:
     * 
     * <ul>
     * <li>
     * BASE_ELEMENT - (Element) mapping of value to element instance
     * </li>
     * <li>
     * USE_NEAREST - (Boolean) not implemented
     * </li>
     * <li>
     * SCHEMA_ORDER - (String[] or Schema[]) resolve ambiguity & import
     * </li>
     * </ul>
     * </p>
     *
     * @param value
     * @param schema
     * @param f
     * @param hints
     *
     * @throws OperationNotSupportedException
     * @throws IOException
     *
     */
    public static void writeFragment(Object value, Schema schema, File f,
        Map hints) throws OperationNotSupportedException, IOException {
        if ((f == null) || (!f.canWrite())) {
            throw new IOException("Cannot write to " + f);
        }

        FileWriter wf = new FileWriter(f);
        writeFragment(value, schema, wf, hints);
        wf.close();
    }

    /**
     * Entry Point to Document writer.
     * 
     * <p>
     * Hints:
     * 
     * <ul>
     * <li>
     * BASE_ELEMENT - (Element) mapping of value to element instance
     * </li>
     * <li>
     * USE_NEAREST - (Boolean) not implemented
     * </li>
     * <li>
     * SCHEMA_ORDER - (String[] or Schema[]) resolve ambiguity & import
     * </li>
     * </ul>
     * </p>
     *
     * @param value
     * @param schema
     * @param w
     * @param hints optional hints for writing
     *
     * @throws OperationNotSupportedException
     * @throws IOException
     *
     */
    public static void writeFragment(Object value, Schema schema, Writer w,
        Map hints) throws OperationNotSupportedException, IOException {
    	
        if ((hints != null) && hints.containsKey(WRITE_SCHEMA)) {
            Writer w2 = (Writer) hints.get(WRITE_SCHEMA);
            Map m = new HashMap();
            m.putAll(hints);
            m.remove(WRITE_SCHEMA);
            writeDocument(schema, SchemaFactory.getInstance(XSDSchema.getInstance().getTargetNamespace()),  w2, m);

        }

        com.vividsolutions.xdo.xsi.Element e = null;
        logger.setLevel(level);

        if (hints != null && hints.containsKey(BASE_ELEMENT)) {
            e = (Element) hints.get(BASE_ELEMENT);
        }

        if (e == null) {
            com.vividsolutions.xdo.xsi.Element[] elems = schema.getElements();
        	
            if (elems != null) {
                for (int j = 0; j < elems.length && e == null; j++)
                    if ((elems[j].getType() != null)){
                        if( canEncode( elems[j], value, hints )){
                            e = elems[j];
                        }
                    }
            }
        }
        if (e == null) {
            Import imports[] = schema.getImports();
            e = findElementThatCanEncode( schema.getImports(), value, hints );
            /*
        	for (int i = 0; i < imports.length && e == null; i++) {
                schema.
	            Element[] elems = schema.getImports()[i].getElements();
	
	            if (elems != null) {
	                for (int j = 0; j < elems.length; j++)
	                    if ((elems[j].getType() != null)
	                            && elems[j].getType().canEncode(elems[j],
	                                value, hints)) {
	                        e = elems[j];
	                    }
	            }
        	}*/
        }
        
        Node n = new Node(convert(e),value);
        Encoder.writeFragment(n,w,hints);
        
        w.flush();
    }

    /**
     * DOCUMENT ME!
     *
     * @param schema DOCUMENT ME!
     * @param w DOCUMENT ME!
     * @param hints DOCUMENT ME!
     *
     * @throws IOException
     * @throws OperationNotSupportedException 
     */
    public static void writeSchema(Schema schema, Writer w, Map hints)
        throws IOException, OperationNotSupportedException {
    	writeDocument(schema,convert(XSDSchema.getInstance()),w,hints);
    }
    
    //
    // Inline methods for reuse 
    //
    //    
    private static final com.vividsolutions.xdo.xsi.Element findElementThatCanEncode( Import[] imports, Object value, Map hints ){
        com.vividsolutions.xdo.xsi.Element found = null;
        if( imports != null || imports.length != 0 ){
            for( int i=0; i < imports.length && found == null; i++ ){
                Import aImport = imports[i];
                com.vividsolutions.xdo.xsi.Schema aSchema = aImport.getSchema();
                
                // Yes dzwiers you can inline this code for speed
                found = findElementThatCanEncode( aSchema.getElements(), value, hints );                                    
            }
        }
        return found;
    }
        
    /**
     * This code is dragged out of writeDocument for reuse.
     * <p>
     * This whole game is a waste of time given use of stratagy objects?
     * </p>
     * Return null if could not find.
     */
    private static final com.vividsolutions.xdo.xsi.Element findElementThatCanEncode( com.vividsolutions.xdo.xsi.Element elements[], Object value, Map hints ){
        if( elements == null || elements.length == 0) return null;
        for (int j = 0; j < elements.length; j++){
            if( elements[j] instanceof Element){
                Element gtElement = (Element) elements[j];
                if (gtElement.getType() != null){
                    Type gtType = (Type) gtElement.getType();
                    if( gtType.canEncode(gtElement,value, hints)) {
                        return gtElement;
                    }
                }
            }
            else {
                // we need to do some kind of stratagy lookup?
                com.vividsolutions.xdo.xsi.Element xdoElement = elements[j];
                Strategy stratagy = stratagyBuilder.build( xdoElement, hints );
                if( stratagy != null && stratagy.canEncode( xdoElement, hints )){
                    return xdoElement;
                }
            }
        }
        return null;
    }
    private static final boolean canEncode( com.vividsolutions.xdo.xsi.Element element, Object value, Map hints ){
        if( element instanceof Element){
            Element gtElement = (Element) element;
            if (gtElement.getType() != null){
                Type gtType = (Type) gtElement.getType();
                if( gtType.canEncode(gtElement,value, hints)) {
                    return true;
                }
            }
        }
        else {
            // we need to do some kind of stratagy lookup?
            com.vividsolutions.xdo.xsi.Element xdoElement = element;
            Strategy stratagy = stratagyBuilder.build( xdoElement, hints );
            if( stratagy != null && stratagy.canEncode( xdoElement, hints )){
                return true;
            }
        }
        return false;
    }
}
