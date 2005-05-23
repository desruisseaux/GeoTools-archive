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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.geotools.factory.FactoryFinder;
import org.geotools.xml.schema.Attribute;
import org.geotools.xml.schema.AttributeGroup;
import org.geotools.xml.schema.ComplexType;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.Group;
import org.geotools.xml.schema.Schema;
import org.geotools.xml.schema.SimpleType;
import org.xml.sax.SAXException;

import com.vividsolutions.xdo.Decoder;
import com.vividsolutions.xdo.PluginFinder;
import com.vividsolutions.xdo.SchemaBuilder;


/**
 * SchemaFactory purpose.
 * 
 * <p>
 * This is the main entry point into the XSI parsing routines.
 * </p>
 * 
 * <p>
 * Example Use:
 * <pre><code>
 * 
 *  
 *    
 *     Schema x = SchemaFactory.getInstance(&quot;MyTargetNameSpace&quot;,new URI(&quot;MyNameSpaceURI&quot;);
 *     
 *   
 *  
 * </code></pre>
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc. http://www.refractions.net
 * @author $Author:$ (last modification)
 * @version $Id$
 */
public class SchemaFactory extends Converter implements SchemaBuilder{
    protected static SchemaFactory is = new SchemaFactory();

    /*
     * Holds onto instances when they are created ... my version of object
     * pooling Q: is there a better way? might suck up too much memory ... A:
     * not really, but the JVM might be better ... use the class to make
     * instances
     */
    private Map schemas = loadSchemas();

    /*
     * The SAX parser to use if one is required ... isn't loaded until first
     * use.
     */
    private SAXParser parser;

    protected static SchemaFactory getInstance() {
        return is;
    }

    /*
     */
    private Map loadSchemas() {
        schemas = new HashMap();

        ClassLoader[] cls = findLoaders();
        String serviceId = "META-INF/services/" + Schema.class.getName();

        for (int i = 0; i < cls.length; i++) {
            try {
                Enumeration e = cls[i].getResources(serviceId);

                while (e.hasMoreElements()) {
                    URL res = (URL) e.nextElement();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(
                        res.openStream(), "UTF-8"));

                    while (rd.ready()) {
                        String factoryClassName = rd.readLine().trim();

                        try {
                            Schema s = (Schema) cls[i].loadClass(factoryClassName)
                                .getDeclaredMethod("getInstance", new Class[0])
                                .invoke(null, new Object[0]);
                            schemas.put(s.getTargetNamespace(), s);
                        } catch (IllegalArgumentException e1) {
//                            XSISAXHandler.logger.warning(e1.toString());
                        } catch (SecurityException e1) {
//                            XSISAXHandler.logger.warning(e1.toString());
                        } catch (IllegalAccessException e1) {
//                            XSISAXHandler.logger.warning(e1.toString());
                        } catch (InvocationTargetException e1) {
//                            XSISAXHandler.logger.warning(e1.toString());
                        } catch (NoSuchMethodException e1) {
//                            XSISAXHandler.logger.warning(e1.toString());
                        } catch (ClassNotFoundException e1) {
//                            XSISAXHandler.logger.warning(e1.toString());
                        }
                    }

                    rd.close();
                }
            } catch (IOException e) {
//                XSISAXHandler.logger.warning(e.toString());
            }
        }

        return schemas;
    }

    // stolen from FactoryFinder.findLoaders
    private ClassLoader[] findLoaders() {
        // lets get a class loader. By using the Thread's class loader, we allow
        // for more flexability.
        ClassLoader contextLoader = null;

        try {
            contextLoader = Thread.currentThread().getContextClassLoader();
        } catch (SecurityException se) {
            // do nothing
        }

        ClassLoader systemLoader = SchemaBuilder.class.getClassLoader();

        ClassLoader[] classLoaders;

        if ((contextLoader == null) || (contextLoader == systemLoader)) {
            classLoaders = new ClassLoader[1];
            classLoaders[0] = systemLoader;
        } else {
            classLoaders = new ClassLoader[2];
            classLoaders[0] = contextLoader;
            classLoaders[1] = systemLoader;
        }

        return classLoaders;
    }

    /**
     * Returns an instance of the desired class. There is no provision for: a)
     * same instances each call b) different instances each call c) this
     * factory being thread safe
     *
     * @param targetNamespace
     * @param desiredSchema URI the uri of which you want a schema instance.
     *
     * @return Schema an instance of the desired schema.
     *
     * @throws SAXException
     * @deprecated
     */
    public static Schema getInstance(URI targetNamespace, URI desiredSchema)
        throws SAXException {
        return getInstance(targetNamespace, desiredSchema, Level.WARNING);
    }
	public com.vividsolutions.xdo.xsi.Schema build(URI namespace, URI location){
		try {
			return convert(getInstance(namespace,location));
		} catch (SAXException e) {
			return null;
		}
	}

    public static Schema getInstance(URI targetNamespace, InputStream is1)
        throws SAXException {
        return getInstance(targetNamespace, is1, Level.WARNING);
    }

    /**
     * Returns an instance of the targetNamespace if it can be found ... null
     * otherwise. targetNamespaces which can be found are either hard-coded
     * namespaces (SchemaFactory.properties), have already been parsed or were
     * registered.
     *
     * @param targetNamespace
     *
     * @return
     *
     * @see registerSchema(Strin,Schema)
     * @deprecated
     */
    public synchronized static Schema getInstance(URI targetNamespace) {
        return getInstance().getRealInstance(targetNamespace);
    }
	public com.vividsolutions.xdo.xsi.Schema build(URI namespace){
		return convert(getInstance(namespace));
	}
	public com.vividsolutions.xdo.xsi.Schema find(URI location){
		// search schemas for loc.
		Iterator i = schemas.values().iterator();
		while(i.hasNext()){
			Schema s = (Schema)i.next();
			if(s.includesURI(location))
				return convert(s);
		}
		return null;
	}
    
    //TODO cache this on schema registry
    public synchronized static Schema[] getSchemas(String prefix){
        if(prefix == null)
            return null;
        SchemaFactory sf = getInstance();
        Iterator i = sf.schemas.values().iterator();
        List l = new LinkedList();
        while(i.hasNext()){
            Schema s = (Schema)i.next();
            if(prefix.equals(s.getPrefix()))
                l.add(s);
        }
        return (Schema[])l.toArray(new Schema[l.size()]);
    }

    private synchronized Schema getRealInstance(URI targetNamespace) {
        Schema r = (Schema) schemas.get(targetNamespace);

        return r;
    }

    /**
     * Returns an instance of the desired class. There is no provision for: a)
     * same instances each call b) different instances each call c) this
     * factory being thread safe
     *
     * @param targetNamespace The targetNamespace to search for.
     * @param desiredSchema URI the uri of which you want a schema instance.
     * @param level Level
     *
     * @return Schema an instance of the desired schema.
     *
     * @throws SAXException When something goes wrong
     */
    public synchronized static Schema getInstance(URI targetNamespace,
        URI desiredSchema, Level level) throws SAXException {
        return getInstance().getRealInstance(targetNamespace, desiredSchema,
            level);
    }

    private synchronized Schema getRealInstance(URI targetNamespace,
        URI desiredSchema, Level level) throws SAXException {
        if ((targetNamespace == null) || (schemas.get(targetNamespace) == null)) {
            Schema sh = convert(PluginFinder.getInstance().getSchemaBuilder().build(targetNamespace,desiredSchema));
            if(sh!=null)
            	schemas.put(sh.getTargetNamespace(),sh);
        
        } else {
            if (!((Schema) schemas.get(targetNamespace)).includesURI(
                        desiredSchema)) {
                Schema sh = (Schema) schemas.get(targetNamespace);
                
                sh = convert(PluginFinder.getInstance().getSchemaBuilder().build(targetNamespace,desiredSchema));
                schemas.put(targetNamespace, sh); // over-write
            }
        }

        return (Schema) schemas.get(targetNamespace);
    }

    public static synchronized Schema getInstance(URI targetNamespace,
        InputStream is1, Level level) throws SAXException {
        return getInstance().getRealInstance(targetNamespace, is1, level);
    }

    private synchronized Schema getRealInstance(URI targetNamespace,
        InputStream is1, Level level) throws SAXException {
        if ((targetNamespace == null) || (schemas.get(targetNamespace) == null)) {
			Map hints = new HashMap();
			hints.put(Decoder.VALIDATION_HINT,Boolean.TRUE);
            Schema sh = convert((com.vividsolutions.xdo.xsi.Schema)Decoder.decode(is1,hints));
            if(sh!=null)
            	schemas.put(sh.getTargetNamespace(),sh);
        
        } else {
        	// no way to test includes ...
            Schema sh = (Schema) schemas.get(targetNamespace);
            

			Map hints = new HashMap();
			hints.put(Decoder.VALIDATION_HINT,Boolean.TRUE);
			try {
				sh = convert((com.vividsolutions.xdo.xsi.Schema)Decoder.decode(is1,hints));
				if(sh!=null)
					schemas.put(sh.getTargetNamespace(),sh); // over-write
			} catch (SAXException e) {
				// TODO log this
			}
        }

        return (Schema) schemas.get(targetNamespace);
    }

    /**
     * Registers a Schema instance with the factory. A clone is NOT created
     * within this method. The Schema passed in is associated with the
     * specified targetNamespace. The Schema is not tested to ensure the
     * intended targetNamespace (schema.getTargetNamespace()) is equal to
     * targetNamespace. The ramifications is that you may hack wildly within
     * the repository, but we aware you may have some 'undocumented features'
     * as a result (odd Schemas being returned).
     *
     * @param targetNamespace
     * @param schema
     */
    public static void registerSchema(URI targetNamespace, Schema schema) {
        getInstance().registerRealSchema(targetNamespace, schema);
    }

    private void registerRealSchema(URI targetNamespace, Schema schema) {
        schemas.put(targetNamespace, schema);
    }
}
