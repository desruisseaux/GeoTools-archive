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

import org.geotools.factory.FactoryFinder;
import org.geotools.xml.schema.Attribute;
import org.geotools.xml.schema.AttributeGroup;
import org.geotools.xml.schema.ComplexType;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.Group;
import org.geotools.xml.schema.Schema;
import org.geotools.xml.schema.SimpleType;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


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
public class SchemaFactory {
    /*
     * Holds onto instances when they are created ... my version of object
     * pooling Q: is there a better way? might suck up too much memory ... A:
     * not really, but the JVM might be better ... use the class to make
     * instances
     */
    private static Map schemas = loadSchemas();

    /*
     * The SAX parser to use if one is required ... isn't loaded until first
     * use.
     */
    private static SAXParser parser;

    /*
     */
    private static Map loadSchemas(){
        schemas = new HashMap();
        
        ClassLoader[] cls = findLoaders();
        String serviceId = "META-INF/services/" + Schema.class.getName();
        for(int i=0;i<cls.length;i++){
            try{
            Enumeration e = cls[i].getResources(serviceId);
            while(e.hasMoreElements()){
                URL res = (URL)e.nextElement();
                BufferedReader rd = new BufferedReader(new InputStreamReader(res.openStream(),"UTF-8"));
                while(rd.ready()){
                    String factoryClassName = rd.readLine().trim();
                    try {
                        Schema s = (Schema)cls[i].loadClass(factoryClassName).getDeclaredMethod("getInstance",new Class[0]).invoke(null,new Object[0]);
//System.out.println(s.getTargetNamespace());
                        schemas.put(s.getTargetNamespace(),s);
                    } catch (IllegalArgumentException e1) {
                        // TODO log this
                        e1.printStackTrace();
                    } catch (SecurityException e1) {
                        // TODO log this
                        e1.printStackTrace();
                    } catch (IllegalAccessException e1) {
                        // TODO log this
                        e1.printStackTrace();
                    } catch (InvocationTargetException e1) {
                        // TODO log this
                        e1.printStackTrace();
                    } catch (NoSuchMethodException e1) {
                        // TODO log this
                        e1.printStackTrace();
                    } catch (ClassNotFoundException e1) {
                        // TODO log this
                        e1.printStackTrace();
                    }
                }
                rd.close();
            }
            }catch(IOException e){
                // TODO log this
                e.printStackTrace();
            }
        }
        return schemas;
    }
    
    // stolen from FactoryFinder.findLoaders
    private static ClassLoader[] findLoaders(){
        // lets get a class loader. By using the Thread's class loader, we allow
        // for more flexability.
        ClassLoader contextLoader = null;

        try {
            contextLoader = Thread.currentThread().getContextClassLoader();
        } catch (SecurityException se) {
        }

        ClassLoader systemLoader = FactoryFinder.class.getClassLoader();

        ClassLoader[] classLoaders;
        if (contextLoader == null || contextLoader == systemLoader) {
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
     */
    public static Schema getInstance(URI targetNamespace, URI desiredSchema)
    throws SAXException {
    return getInstance(targetNamespace, desiredSchema, Level.WARNING);
}
    public static Schema getInstance(URI targetNamespace, InputStream is)
    throws SAXException {
    return getInstance(targetNamespace, is, Level.WARNING);
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
     */
    public synchronized static Schema getInstance(URI targetNamespace) {
        Schema r = (Schema) schemas.get(targetNamespace);

        if (r != null) {
            return r;
        }

//        if (mappings.containsKey(targetNamespace)) {
//            ClassLoader cl = SchemaFactory.class.getClassLoader();
//
//            try {
//                Class c = cl.loadClass((String) mappings.get(targetNamespace));
//                r = (Schema) c.getConstructor(new Class[0]).newInstance(new Object[0]);
//                schemas.put(targetNamespace, r);
//
//                return r;
//            } catch (Exception e) {
//                return null;
//            }
//        }

        return null;
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
            if (targetNamespace == null
                    || (schemas.get(targetNamespace) == null)) {

                    setParser();

                    XSISAXHandler contentHandler = new XSISAXHandler(desiredSchema);
                    XSISAXHandler.setLogLevel(level);

                    try {
                        parser.parse(desiredSchema.toString(), contentHandler);
                    } catch (IOException e) {
                        throw new SAXException(e);
                    }

                    if ((targetNamespace == null) || "".equals(targetNamespace)) {
                        return contentHandler.getSchema();
                    }

                    schemas.put(targetNamespace, contentHandler.getSchema());
//                }
            } else {
                if (!((Schema) schemas.get(targetNamespace)).includesURI(
                            desiredSchema)) {
                    Schema sh = (Schema) schemas.get(targetNamespace);
                    setParser();

                    XSISAXHandler contentHandler = new XSISAXHandler(desiredSchema);
                    XSISAXHandler.setLogLevel(level);

                    try {
                        parser.parse(desiredSchema.toString(), contentHandler);
                    } catch (IOException e) {
                        throw new SAXException(e);
                    }

                    sh = merge(sh, contentHandler.getSchema());
                    schemas.put(targetNamespace, sh); // over-write
                }
            }

            return (Schema) schemas.get(targetNamespace);
        }
    public synchronized static Schema getInstance(URI targetNamespace,
            InputStream is, Level level) throws SAXException {
            if ((targetNamespace == null) || "".equals(targetNamespace)
                    || (schemas.get(targetNamespace) == null)) {

                    setParser();

                    XSISAXHandler contentHandler = new XSISAXHandler(null); // no uri
                    XSISAXHandler.setLogLevel(level);

                    try {
                        parser.parse(is, contentHandler);
                    } catch (IOException e) {
                        throw new SAXException(e);
                    }

                    if ((targetNamespace == null) || "".equals(targetNamespace)) {
                        return contentHandler.getSchema();
                    }

                    schemas.put(targetNamespace, contentHandler.getSchema());
//                }
            } else {
                // no way to test is it's already part ... so assume it's not already included
                    Schema sh = (Schema) schemas.get(targetNamespace);
                    setParser();

                    XSISAXHandler contentHandler = new XSISAXHandler(null); // no uri
                    XSISAXHandler.setLogLevel(level);

                    try {
                        parser.parse(is, contentHandler);
                    } catch (IOException e) {
                        throw new SAXException(e);
                    }

                    sh = merge(sh, contentHandler.getSchema());
                    schemas.put(targetNamespace, sh); // over-write
            }

            return (Schema) schemas.get(targetNamespace);
        }

    /*
     * Creates a new Schema from merging the two schemas passed in. for instance
     * information, such as id, or version, s1 takes precedence.
     */
    private static Schema merge(Schema s1, Schema s2) throws SAXException {
        return new MergedSchema(s1, s2);
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
        schemas.put(targetNamespace, schema);
    }

    /*
     * convinience method to create an instance of a SAXParser if it is null.
     */
    private static void setParser() throws SAXException {
        if (parser == null) {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            spf.setValidating(false);

            try {
                parser = spf.newSAXParser();
            } catch (ParserConfigurationException e) {
                throw new SAXException(e);
            } catch (SAXException e) {
                throw new SAXException(e);
            }
        }
    }

    /**
     * The Schema will contain references to the elements within the two
     * Schemas provided in the constructor. In most cases this is used to
     * incorporate additional definitions into a targetNamespace by parsing an
     * additional schema file.
     *
     * @author dzwiers
     *
     * @see Schema
     */
    private static class MergedSchema implements Schema {
        /*
         * This is all fairly self explanatory.
         *
         * @see Schema
         */
        private AttributeGroup[] attributeGroups;
        private Attribute[] attributes;
        private int block;
        private int finaL;
        private ComplexType[] complexTypes;
        private Element[] elements;
        private Group[] groups;
        private String id;
        private String version;
        private String prefix;
        private URI targetNamespace;
        private Schema[] imports;
        private SimpleType[] simpleTypes;
        private boolean aForm;
        private boolean eForm;
        private URI uri;

        /**
         * This completes the merge of two schemas, s1 and s2. When there is a
         * conflict in data between the two schemas, s1 is assumed to be
         * correct.
         *
         * @param s1 Schema (Tie Winner)
         * @param s2 Schema (Tie Loser)
         *
         * @throws SAXException When some thing bad happens (for example
         *         merging two targetNamespaces)
         */
        public MergedSchema(Schema s1, Schema s2) throws SAXException {
            if ((s1.getId() == null) || s1.getId().equals("")) {
                id = s2.getId();
            } else {
                id = s1.getId();
            }

            if ((s1.getVersion() == null) || s1.getVersion().equals("")) {
                version = s2.getVersion();
            } else {
                version = s1.getVersion();
            }

            if ((s1.getTargetNamespace() == null)
                    || s1.getTargetNamespace().equals("")) {
                targetNamespace = s2.getTargetNamespace();
            } else {
                if ((s2.getTargetNamespace() != null)
                        && !s1.getTargetNamespace().equals(s2
                            .getTargetNamespace())) {
                    throw new SAXException(
                        "cannot merge two target namespaces. "
                        + s1.getTargetNamespace() + " "
                        + s2.getTargetNamespace());
                } else {
                    targetNamespace = s1.getTargetNamespace();
                }
            }

            aForm = s1.isAttributeFormDefault() || s2.isAttributeFormDefault();
            eForm = s1.isElementFormDefault() || s2.isElementFormDefault();

            HashMap m = new HashMap();

            AttributeGroup[] ag1 = s1.getAttributeGroups();

            if (ag1 == null) {
                ag1 = new AttributeGroup[0];
            }

            AttributeGroup[] ag2 = s2.getAttributeGroups();

            if (ag2 == null) {
                ag2 = new AttributeGroup[0];
            }

            for (int i = 0; i < ag1.length; i++)
                m.put(ag1[i].getName(), ag1[i]);

            for (int i = 0; i < ag2.length; i++)
                if (!m.containsKey(ag2[i].getName())) {
                    m.put(ag2[i].getName(), ag2[i]);
                }

            attributeGroups = new AttributeGroup[m.size()];

            Object[] obj = m.values().toArray();

            for (int i = 0; i < obj.length; i++)
                attributeGroups[i] = (AttributeGroup) (obj[i]);

            m = new HashMap();

            Attribute[] a1 = s1.getAttributes();

            if (a1 == null) {
                a1 = new Attribute[0];
            }

            Attribute[] a2 = s2.getAttributes();

            if (a2 == null) {
                a2 = new Attribute[0];
            }

            for (int i = 0; i < a1.length; i++)
                m.put(a1[i].getName(), a1[i]);

            for (int i = 0; i < a2.length; i++)
                if (!m.containsKey(a2[i].getName())) {
                    m.put(a2[i].getName(), a2[i]);
                }

            attributes = new Attribute[m.size()];
            obj = m.values().toArray();

            for (int i = 0; i < obj.length; i++)
                attributes[i] = (Attribute) (obj[i]);

            block = s1.getBlockDefault() | s2.getBlockDefault();
            finaL = s1.getFinalDefault() | s2.getFinalDefault();

            m = new HashMap();

            ComplexType[] c1 = s1.getComplexTypes();

            if (c1 == null) {
                c1 = new ComplexType[0];
            }

            ComplexType[] c2 = s2.getComplexTypes();

            if (c2 == null) {
                c2 = new ComplexType[0];
            }

            for (int i = 0; i < c1.length; i++)
                m.put(c1[i].getName(), c1[i]);

            for (int i = 0; i < c2.length; i++)
                if (!m.containsKey(c2[i].getName())) {
                    m.put(c2[i].getName(), c2[i]);
                }

            complexTypes = new ComplexType[m.size()];
            obj = m.values().toArray();

            for (int i = 0; i < obj.length; i++)
                complexTypes[i] = (ComplexType) (obj[i]);

            m = new HashMap();

            SimpleType[] ss1 = s1.getSimpleTypes();

            if (ss1 == null) {
                ss1 = new SimpleType[0];
            }

            SimpleType[] ss2 = s2.getSimpleTypes();

            if (ss2 == null) {
                ss2 = new SimpleType[0];
            }

            for (int i = 0; i < ss1.length; i++)
                m.put(ss1[i].getName(), ss1[i]);

            for (int i = 0; i < ss2.length; i++)
                if (!m.containsKey(ss2[i].getName())) {
                    m.put(ss2[i].getName(), ss2[i]);
                }

            simpleTypes = new SimpleType[m.size()];
            obj = m.values().toArray();

            for (int i = 0; i < obj.length; i++)
                simpleTypes[i] = (SimpleType) (obj[i]);

            m = new HashMap();

            Element[] e1 = s1.getElements();

            if (e1 == null) {
                e1 = new Element[0];
            }

            Element[] e2 = s2.getElements();

            if (e2 == null) {
                e2 = new Element[0];
            }

            for (int i = 0; i < e1.length; i++)
                m.put(e1[i].getName(), e1[i]);

            for (int i = 0; i < e2.length; i++)
                if (!m.containsKey(e2[i].getName())) {
                    m.put(e2[i].getName(), e2[i]);
                }

            elements = new Element[m.size()];
            obj = m.values().toArray();

            for (int i = 0; i < obj.length; i++)
                elements[i] = (Element) (obj[i]);

            m = new HashMap();

            Group[] g1 = s1.getGroups();

            if (g1 == null) {
                g1 = new Group[0];
            }

            Group[] g2 = s2.getGroups();

            if (g2 == null) {
                g2 = new Group[0];
            }

            for (int i = 0; i < g1.length; i++)
                m.put(g1[i].getName(), g1[i]);

            for (int i = 0; i < g2.length; i++)
                if (!m.containsKey(g2[i].getName())) {
                    m.put(g2[i].getName(), g2[i]);
                }

            groups = new Group[m.size()];
            obj = m.values().toArray();

            for (int i = 0; i < obj.length; i++)
                groups[i] = (Group) (obj[i]);

            m = new HashMap();

            Schema[] i1 = s1.getImports();

            if (i1 == null) {
                i1 = new Schema[0];
            }

            Schema[] i2 = s2.getImports();

            if (i2 == null) {
                i2 = new Schema[0];
            }

            for (int i = 0; i < i1.length; i++)
                m.put(i1[i].getTargetNamespace(), i1[i]);

            for (int i = 0; i < i2.length; i++)
                if (!m.containsKey(i2[i].getTargetNamespace())) {
                    m.put(g2[i].getName(), i2[i]);
                }

            imports = new Schema[m.size()];
            obj = m.values().toArray();

            for (int i = 0; i < obj.length; i++)
                imports[i] = (Schema) (obj[i]);

            HashSet hs = new HashSet();

            URI u1 = s1.getURI();

            URI u2 = s2.getURI();

            if (u1 == null) {
                uri = u2;
            } else {
                if (u2 == null) {
                    uri = u1;
                } else {
                    uri = u2.relativize(u1);
                }
            }

            if ((s1.getPrefix() == null) || s1.getPrefix().equals("")) {
                prefix = s2.getPrefix();
            } else {
                prefix = s1.getPrefix();
            }
        }

        /**
         * @see schema.Schema#getAttributeGroups()
         */
        public AttributeGroup[] getAttributeGroups() {
            return attributeGroups;
        }

        /**
         * @see schema.Schema#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attributes;
        }

        /**
         * @see schema.Schema#getBlockDefault()
         */
        public int getBlockDefault() {
            return block;
        }

        /**
         * @see schema.Schema#getComplexTypes()
         */
        public ComplexType[] getComplexTypes() {
            return complexTypes;
        }

        /**
         * @see schema.Schema#getElements()
         */
        public Element[] getElements() {
            return elements;
        }

        /**
         * @see schema.Schema#getFinalDefault()
         */
        public int getFinalDefault() {
            return finaL;
        }

        /**
         * @see schema.Schema#getGroups()
         */
        public Group[] getGroups() {
            return groups;
        }

        /**
         * @see schema.Schema#getId()
         */
        public String getId() {
            return id;
        }

        /**
         * @see schema.Schema#getImports()
         */
        public Schema[] getImports() {
            return imports;
        }

        /**
         * @see schema.Schema#getSimpleTypes()
         */
        public SimpleType[] getSimpleTypes() {
            return simpleTypes;
        }

        /**
         * @see schema.Schema#getTargetNamespace()
         */
        public URI getTargetNamespace() {
            return targetNamespace;
        }

        /**
         * @see schema.Schema#getVersion()
         */
        public String getVersion() {
            return version;
        }

        /**
         * @see schema.Schema#includesURI(java.net.URI)
         */
        public boolean includesURI(URI uri) {
            if (uri == null) {
                return false;
            }

            return this.uri.equals(uri);
        }

        /**
         * @see schema.Schema#isAttributeFormDefault()
         */
        public boolean isAttributeFormDefault() {
            return aForm;
        }

        /**
         * @see schema.Schema#isElementFormDefault()
         */
        public boolean isElementFormDefault() {
            return eForm;
        }

        /**
         * @see schema.Schema#getURIs()
         */
        public URI getURI() {
            return uri;
        }

        public String getPrefix() {
            return prefix;
        }
    }
}
