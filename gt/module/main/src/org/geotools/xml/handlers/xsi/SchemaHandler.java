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
package org.geotools.xml.handlers.xsi;

import org.geotools.xml.SchemaFactory;
import org.geotools.xml.XSIElementHandler;
import org.geotools.xml.schema.Attribute;
import org.geotools.xml.schema.AttributeGroup;
import org.geotools.xml.schema.ComplexType;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.Group;
import org.geotools.xml.schema.Schema;
import org.geotools.xml.schema.SimpleType;
import org.geotools.xml.schema.Type;
import org.geotools.xml.xsi.XSISimpleTypes;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * SchemaHandler purpose.
 * 
 * <p>
 * represents a Schema element
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc. http://www.refractions.net
 * @author $Author:$ (last modification)
 * @version $Id$
 */
public class SchemaHandler extends XSIElementHandler {
    /** "http://www.w3.org/2001/XMLSchema" */
    public static final String namespaceURI = "http://www.w3.org/2001/XMLSchema";

    /** 'schema' */
    public final static String LOCALNAME = "schema";
    private String id;
    private String prefix;
    private URI targetNamespace;
    private String version;
    private boolean elementFormDefault;
    private boolean attributeFormDefault;
    private int finalDefault;
    private int blockDefault;
    private HashSet includes;
    private HashSet imports;
    private HashSet redefines;
    private HashSet attributes;
    private HashSet attributeGroups;
    private HashSet complexTypes;
    private HashSet elements;
    private HashSet groups;
    private HashSet simpleTypes;
    private URI uri;
    private Schema schema = null;
    private HashMap prefixCache;

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return LOCALNAME.hashCode() * ((id == null) ? 1 : id.hashCode()) * ((version == null)
        ? 1 : version.hashCode()) * ((targetNamespace == null) ? 1
                                                               : targetNamespace
        .hashCode());
    }

    /**
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
     *      java.lang.String)
     */
    public void startPrefixMapping(String pref, String uri) {
//System.out.print("Prefix = "+pref);
        if (targetNamespace == null) {
            if (prefixCache == null) {
                prefixCache = new HashMap();
            }

            prefixCache.put(uri, pref);
//System.out.println("+");
        } else {
//System.out.println("*");
            // we have already started
            if (targetNamespace.equals(uri.toString())) {
                prefix = pref;
            }
        }
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#startElement(java.lang.String,
     *      java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String namespaceURI, String localName,
        Attributes atts) throws SAXException {
        // targetNamespace
        String targetNamespace = atts.getValue("", "targetNamespace");

        if (targetNamespace == null) {
            targetNamespace = atts.getValue(namespaceURI, "targetNamespace");
        }

        try {
            this.targetNamespace = new URI(targetNamespace);
        } catch (URISyntaxException e) {
            logger.warning(e.toString());
            throw new SAXException(e);
        }

        //System.out.println("NS = "+targetNamespace);
        if ((prefixCache != null) && (targetNamespace != null)
                && (!targetNamespace.equals(""))) {
            Iterator i = prefixCache.keySet().iterator();

            while ((i != null) && i.hasNext()) {
                String uriT = (String) i.next();

                if (targetNamespace.equals(uriT)) {
                    prefix = (String) prefixCache.get(uriT);
                    i = null;
                }
            }
        }

//        prefixCache = null;

        // attributeFormDefault
        String attributeFormDefault = atts.getValue("", "attributeFormDefault");

        if (attributeFormDefault == null) {
            attributeFormDefault = atts.getValue(namespaceURI,
                    "attributeFormDefault");
        }

        this.attributeFormDefault = "qualified".equalsIgnoreCase(attributeFormDefault);

        // blockDefault
        String blockDefault = atts.getValue("", "blockDefault");

        if (blockDefault == null) {
            blockDefault = atts.getValue(namespaceURI, "blockDefault");
        }

        this.blockDefault = ComplexTypeHandler.findBlock(blockDefault);

        // elementFormDefault
        String elementFormDefault = atts.getValue("", "elementFormDefault");

        if (elementFormDefault == null) {
            elementFormDefault = atts.getValue(namespaceURI,
                    "elementFormDefault");
        }

        this.elementFormDefault = "qualified".equalsIgnoreCase(elementFormDefault);

        // finalDefault
        String finalDefault = atts.getValue("", "finalDefault");

        if (finalDefault == null) {
            finalDefault = atts.getValue(namespaceURI, "finalDefault");
        }

        this.finalDefault = SimpleTypeHandler.findFinal(finalDefault);

        // id
        id = atts.getValue("", "id");

        if (id == null) {
            id = atts.getValue(namespaceURI, "id");
        }

        // version
        version = atts.getValue("", "version");

        if (version == null) {
            version = atts.getValue(namespaceURI, "version");
        }
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#getHandler(java.lang.String,
     *      java.lang.String)
     */
    public XSIElementHandler getHandler(String namespaceURI, String localName)
        throws SAXException {
        // check that we are working with a known namespace
        if (SchemaHandler.namespaceURI.equalsIgnoreCase(namespaceURI)) {
            // child elements:
            //
            // This list order has been picked in an adhock manner
            // attempting to improve performance. Re-order the
            // child elements if this order does not appear optimal.
            // complexType
            if (ComplexTypeHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                if (complexTypes == null) {
                    complexTypes = new HashSet();
                }

                ComplexTypeHandler cth = new ComplexTypeHandler();
                complexTypes.add(cth);

                return cth;
            }

            // simpleType
            if (SimpleTypeHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                if (simpleTypes == null) {
                    simpleTypes = new HashSet();
                }

                SimpleTypeHandler sth = new SimpleTypeHandler();
                simpleTypes.add(sth);

                return sth;
            }

            // element
            if (ElementTypeHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                if (elements == null) {
                    elements = new HashSet();
                }

                ElementTypeHandler eth = new ElementTypeHandler();
                elements.add(eth);

                return eth;
            }

            // attribute
            if (AttributeHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                if (attributes == null) {
                    attributes = new HashSet();
                }

                AttributeHandler ah = new AttributeHandler();
                attributes.add(ah);

                return ah;
            }

            // include
            if (IncludeHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                if (includes == null) {
                    includes = new HashSet();
                }

                IncludeHandler ih = new IncludeHandler();
                includes.add(ih);

                return ih;
            }

            // import
            if (ImportHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                if (imports == null) {
                    imports = new HashSet();
                }

                ImportHandler ih = new ImportHandler();
                imports.add(ih);

                return ih;
            }

            // group
            if (GroupHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                if (groups == null) {
                    groups = new HashSet();
                }

                GroupHandler gh = new GroupHandler();
                groups.add(gh);

                return gh;
            }

            // redefine
            if (RedefineHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                if (redefines == null) {
                    redefines = new HashSet();
                }

                RedefineHandler rh = new RedefineHandler();
                redefines.add(rh);

                return rh;
            }

            // attributeGroup
            if (AttributeGroupHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                if (attributeGroups == null) {
                    attributeGroups = new HashSet();
                }

                AttributeGroupHandler agh = new AttributeGroupHandler();
                attributeGroups.add(agh);

                return agh;
            }
        }

        return null;
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#getLocalName()
     */
    public String getLocalName() {
        return LOCALNAME;
    }

    /**
     * <p>
     * creates a smaller, more compact version of the schema
     * </p>
     *
     * @param thisURI
     *
     * @return
     *
     * @throws SAXException
     */
    protected Schema compress(URI thisURI) throws SAXException {
        if (schema != null) {
            return schema; // already compressed.
        }

        if (uri == null) {
            uri = thisURI;
        } else {
            if (thisURI != null) {
                uri = thisURI.resolve(uri);
            }
        }

        //System.out.println(prefix + ":"+targetNamespace);
        if (prefix == null) {
            //System.out.println("prefix is null");
            prefix = (String) prefixCache.get(targetNamespace);

            //System.out.println("prefix is now "+prefix);
        }

        Iterator it = null;

        if (includes != null) {
            // do these first
            it = includes.iterator();

            while (it.hasNext()) {
                IncludeHandler inc = (IncludeHandler) it.next();
                logger.finest("compressing include " + inc.getSchemaLocation());

                Schema cs;
                URI incURI = thisURI.normalize().resolve(inc.getSchemaLocation());
                cs = SchemaFactory.getInstance(targetNamespace, incURI,
                        logger.getLevel());

                if (uri != null) {
                    uri = incURI.resolve(uri);
                } else {
                    uri = incURI;
                }

                // already compressed
                addSchema(cs);
            }
        }

        includes = null;

        // imports may be schema or schemaHandler
        if (this.imports != null) {
            // 	have now loaded the included stuff.
            HashSet imports = new HashSet();
            it = this.imports.iterator();

            while (it.hasNext()) {
                Object obj = it.next();

                if (obj instanceof ImportHandler) {
                    ImportHandler imp = (ImportHandler) obj;
                    URI incURI = null;

//                    if ((imp.getSchemaLocation() != null) && (thisURI != null)) {
//                        incURI = thisURI.normalize().resolve(imp
//                                .getSchemaLocation());
//                    }
		// fix from chris dillard
		if (imp.getSchemaLocation() != null) {
		  if (thisURI != null) {
		    // For resolving relative URIs
		    incURI = thisURI.normalize().resolve(imp.getSchemaLocation());
		  } else {
		    // If thisURI is null, we have to assume the
		    // URI is absolute.
		    incURI = imp.getSchemaLocation();
		  }
		}

                    Schema cs = SchemaFactory.getInstance(imp.getNamespace(),
                            incURI, logger.getLevel());

                    imports.add(cs);
                } else {
                    imports.add(obj);
                }
            }

            this.imports = imports;
        }

        // TODO redefines?
        // should do over-writing here
        //build schema object ... be thrown out
        DefaultSchema schema = new DefaultSchema();
        schema.attributeFormDefault = attributeFormDefault;
        schema.elementFormDefault = elementFormDefault;
        schema.finalDefault = finalDefault;
        schema.blockDefault = blockDefault;
        schema.id = id;
        schema.prefix = prefix;
        schema.targetNamespace = targetNamespace;
        schema.version = version;
        schema.uri = uri;

        if (imports != null) {
            schema.imports = (Schema[]) imports.toArray(new Schema[imports.size()]);
        }

        // these need to be retyped
        int i;

        if (simpleTypes != null) {
            schema.simpleTypes = new SimpleType[simpleTypes.size()];
            i = 0;
            it = simpleTypes.iterator();

            while (it.hasNext()) {
                Object t = it.next();

                if (t instanceof SimpleTypeHandler) {
                    schema.simpleTypes[i++] = ((SimpleTypeHandler) t).compress(this);
                } else {
                    schema.simpleTypes[i++] = (SimpleType) t;
                }
            }
        }

        if (attributeGroups != null) {
            schema.attributeGroups = new AttributeGroup[attributeGroups.size()];
            i = 0;
            it = attributeGroups.iterator();

            while (it.hasNext()) {
                Object t = it.next();

                if (t instanceof AttributeGroupHandler) {
                    schema.attributeGroups[i++] = ((AttributeGroupHandler) t)
                        .compress(this);
                } else {
                    schema.attributeGroups[i++] = (AttributeGroup) t;
                }
            }
        }

        if (attributes != null) {
            schema.attributes = new Attribute[attributes.size()];
            i = 0;
            it = attributes.iterator();

            while (it.hasNext()) {
                Object t = it.next();

                if (t instanceof AttributeHandler) {
                    schema.attributes[i++] = ((AttributeHandler) t).compress(this);
                } else {
                    schema.attributes[i++] = (Attribute) t;
                }
            }
        }

        if (complexTypes != null) {
            schema.complexTypes = new ComplexType[complexTypes.size()];
            i = 0;
            it = complexTypes.iterator();

            while (it.hasNext()) {
                Object t = it.next();

                if (t instanceof ComplexTypeHandler) {
                    schema.complexTypes[i++] = ((ComplexTypeHandler) t)
                        .compress(this);
                } else {
                    schema.complexTypes[i++] = (ComplexType) t;
                }
            }
        }

        if (elements != null) {
            schema.elements = new Element[elements.size()];
            i = 0;
            it = elements.iterator();

            while (it.hasNext()) {
                Object t = it.next();

                if (t instanceof ElementTypeHandler) {
                    schema.elements[i++] = (Element) ((ElementTypeHandler) t)
                        .compress(this);
                } else {
                    schema.elements[i++] = (Element) t;
                }
            }
        }

        if (groups != null) {
            schema.groups = new Group[groups.size()];
            i = 0;
            it = groups.iterator();

            while (it.hasNext()) {
                Object t = it.next();

                if (t instanceof GroupHandler) {
                    schema.groups[i++] = (Group) ((GroupHandler) t).compress(this);
                } else {
                    schema.groups[i++] = (Group) t;
                }
            }
        }

        attributeGroups = attributes = complexTypes = simpleTypes = elements = groups = imports = includes = redefines = null;

        return schema;
    }

    /*
     * Helper method for lookUpSimpleType(String)
     */
    private SimpleType lookUpSimpleType(String localName, Schema s, List targets) {
        if (s == null) {
            return null;
        }

        targets.add(s.getTargetNamespace());

        if (s.getSimpleTypes() != null) {
            SimpleType[] sts = s.getSimpleTypes();

            for (int i = 0; (sts != null) && (i < sts.length); i++) {
                if (localName.equalsIgnoreCase(sts[i].getName())) {
                    return sts[i];
                }
            }
        }

        if (s.getImports() != null) {
            Schema[] ss = s.getImports();

            for (int i = 0; (ss != null) && (i < ss.length); i++) {
                if (!targets.contains(ss[i].getTargetNamespace())) {
                    SimpleType st = lookUpSimpleType(localName, ss[i], targets);

                    if (st != null) {
                        return st;
                    }
                }
            }
        }

        return null;
    }

    /**
     * <p>
     * convinience method for package classes
     * </p>
     *
     * @param qName
     *
     * @return
     */
    protected SimpleType lookUpSimpleType(String qname) {
        int index = qname.indexOf(":");
        String localName,prefix;
        localName = prefix = null;
        if(index>=0){
            localName = qname.substring(index + 1);
            prefix = qname.substring(0,index);
        }else{
            prefix = "";
            localName = qname;
        }
        logger.finest("prefix is " + prefix);
        logger.finest("localName is " + localName);
        Iterator it;
        if((this.prefix == null && prefix == null)||(this.prefix!=null && this.prefix.equals(prefix))){
            if(schema!=null)
                return lookUpSimpleType(localName, schema, new LinkedList());
        }else{
            if(imports!=null){
            it = imports.iterator();
//System.out.println("prefixLookup == null? "+(prefixCache==null)+" "+this.uri + " ");
            while (it.hasNext()) {
                Schema s = (Schema) it.next();
                String ns = s.getTargetNamespace().toString();
//System.out.println(ns);
                String prefixLookup = prefixCache!=null?(String)prefixCache.get(ns):null;
                if(prefix == null || prefixLookup==null || prefix.equals(prefixLookup)){
                    SimpleType st = lookUpSimpleType(localName, s, new LinkedList());
                	if (st != null) {
                	    return st;
                	}
                }
            }}
        }

        if (simpleTypes != null) {
            it = simpleTypes.iterator();

            while (it.hasNext()) {
                Object o = it.next();

                if (o instanceof SimpleTypeHandler) {
                    SimpleTypeHandler sst = (SimpleTypeHandler) o;

                    if (localName.equalsIgnoreCase(sst.getName())) {
                        return sst.compress(this);
                    }
                } else {
                    SimpleType sst = (SimpleType) o;

                    if (localName.equalsIgnoreCase(sst.getName())) {
                        return sst;
                    }
                }
            }
        }

        SimpleType sti = XSISimpleTypes.find(localName);

        if (sti != null) {
            return sti;
        }

        return null;
    }

    /*
     * helper for lookUpComplexType(String)
     */
    private ComplexType lookUpComplexType(String localName, Schema s,
        List targets) {
        if (s == null) {
            return null;
        }

        targets.add(s.getTargetNamespace());

        if (s.getComplexTypes() != null) {
            ComplexType[] sts = s.getComplexTypes();

            for (int i = 0; (sts != null) && (i < sts.length); i++) {
                if (localName.equalsIgnoreCase(sts[i].getName())) {
                    return sts[i];
                }
            }
        }

        if (s.getImports() != null) {
            Schema[] ss = s.getImports();

            for (int i = 0; (ss != null) && (i < ss.length); i++) {
                if (!targets.contains(ss[i].getTargetNamespace())) {
                    ComplexType st = lookUpComplexType(localName, ss[i], targets);

                    if (st != null) {
                        return st;
                    }
                }
            }
        }

        return null;
    }

    /**
     * <p>
     * convinience method for package
     * </p>
     *
     * @param qName
     *
     * @return
     *
     * @throws SAXException
     */
    protected ComplexType lookUpComplexType(String qname)
        throws SAXException {
        int index = qname.indexOf(":");
        String localName,prefix;
        localName = prefix = null;
        if(index>=0){
            localName = qname.substring(index + 1);
            prefix = qname.substring(0,index);
        }else{
            prefix = "";
            localName = qname;
        }
        logger.finest("prefix is " + prefix);
        logger.finest("localName is " + localName);

        Iterator it;
        if((this.prefix == null && prefix == null)||(this.prefix!=null && this.prefix.equals(prefix))){
            if(schema!=null)
                return lookUpComplexType(localName, schema, new LinkedList());
        }else{
            if(imports!=null){
            it = imports.iterator();
//System.out.println("prefixLookup == null? "+(prefixCache==null)+" "+this.uri + " ");
            while (it.hasNext()) {
                Schema s = (Schema) it.next();
                String ns = s.getTargetNamespace().toString();
//System.out.println(ns);
                String prefixLookup = prefixCache!=null?(String)prefixCache.get(ns):null;
                if(prefix == null || prefixLookup==null || prefix.equals(prefixLookup)){
                    ComplexType ct = lookUpComplexType(localName, s, new LinkedList());
                	if (ct != null) {
                	    return ct;
                	}
                }
            }}
        }

        if(complexTypes!=null){
        it = complexTypes.iterator();

        while (it.hasNext()) {
            Object o = it.next();

            if (o instanceof ComplexTypeHandler) {
                ComplexTypeHandler sst = (ComplexTypeHandler) o;

                if (localName.equalsIgnoreCase(sst.getName())) {
                    return sst.compress(this);
                }
            } else {
                ComplexType sst = (ComplexType) o;

                if (localName.equalsIgnoreCase(sst.getName())) {
                    return sst;
                }
            }
        }}

        return null;
    }

    /*
     * helper method for lookupElement(String)
     */
    private Element lookupElement(String localName, Schema s, List targets) {
        if (s == null) {
            return null;
        }

        logger.finest("looking for element in " + s.getTargetNamespace());
        targets.add(s.getTargetNamespace());

        if (s.getElements() != null) {
            Element[] sts = s.getElements();

            for (int i = 0; (sts != null) && (i < sts.length); i++) {
                logger.finest("checking element " + sts[i].getName());

                if (localName.equalsIgnoreCase(sts[i].getName())) {
                    return sts[i];
                }
            }
        }

        if (s.getImports() != null) {
            Schema[] ss = s.getImports();

            for (int i = 0; (ss != null) && (i < ss.length); i++) {
                if (!targets.contains(ss[i].getTargetNamespace())) {
                    Element st = lookupElement(localName, ss[i], targets);

                    if (st != null) {
                        return st;
                    }
                }
            }
        }

        return null;
    }

    /**
     * <p>
     * convinience method for package
     * </p>
     *
     * @param qName
     *
     * @return
     *
     * @throws SAXException
     */
    protected Element lookUpElement(String qname) throws SAXException {
        int index = qname.indexOf(":");
        String localName,prefix;
        localName = prefix = null;
        if(index>=0){
            localName = qname.substring(index + 1);
            prefix = qname.substring(0,index);
        }else{
            prefix = "";
            localName = qname;
        }
        logger.finest("prefix is " + prefix);
        logger.finest("localName is " + localName);

        Iterator it;
        if((this.prefix == null && prefix == null)||(this.prefix!=null && this.prefix.equals(prefix))){
            if(schema!=null)
                return lookupElement(localName, schema, new LinkedList());
        }else{if(imports!=null){
            it = imports.iterator();
//System.out.println("prefixLookup == null? "+(prefixCache==null)+" "+this.uri + " ");
            while (it.hasNext()) {
                Schema s = (Schema) it.next();
                String ns = s.getTargetNamespace().toString();
//System.out.println(ns);
                String prefixLookup = prefixCache!=null?(String)prefixCache.get(ns):null;
                if(prefix == null || prefixLookup==null || prefix.equals(prefixLookup)){
                    Element ct = lookupElement(localName, s, new LinkedList());
                	if (ct != null) {
                	    return ct;
                	}
                }
            }}
        if(includes!=null){
            it = includes.iterator();
//          System.out.println("prefixLookup == null? "+(prefixCache==null)+" "+this.uri + " ");
                      while (it.hasNext()) {
                          Schema s = (Schema) it.next();
                          String ns = s.getTargetNamespace().toString();
//          System.out.println(ns);
                          String prefixLookup = prefixCache!=null?(String)prefixCache.get(ns):null;
                          if(prefix == null || prefixLookup==null || prefix.equals(prefixLookup)){
                              Element ct = lookupElement(localName, s, new LinkedList());
                          	if (ct != null) {
                          	    return ct;
                          	}
                          }
                      }}
        }

        it = elements.iterator();

        while (it.hasNext()) {
            Object o = it.next();

            if (o instanceof ElementTypeHandler) {
                ElementTypeHandler sst = (ElementTypeHandler) o;

                if (localName.equalsIgnoreCase(sst.getName())) {
                    return (Element) sst.compress(this);
                }
            } else {
                Element sst = (Element) o;

                if (localName.equalsIgnoreCase(sst.getName())) {
                    return sst;
                }
            }
        }
        return null;
    }

    /*
     * helper for lookUpGroup
     */
    private Group lookUpGroup(String localName, Schema s, List targets) {
        if (s == null) {
            return null;
        }

        targets.add(s.getTargetNamespace());

        if (s.getGroups() != null) {
            Group[] sts = s.getGroups();

            for (int i = 0; (sts != null) && (i < sts.length); i++) {
                if (localName.equalsIgnoreCase(sts[i].getName())) {
                    return sts[i];
                }
            }
        }

        if (s.getImports() != null) {
            Schema[] ss = s.getImports();

            for (int i = 0; (ss != null) && (i < ss.length); i++) {
                if (!targets.contains(ss[i].getTargetNamespace())) {
                    Group st = lookUpGroup(localName, ss[i], targets);

                    if (st != null) {
                        return st;
                    }
                }
            }
        }

        return null;
    }

    /**
     * <p>
     * convinience method for package
     * </p>
     *
     * @param qName
     *
     * @return
     *
     * @throws SAXException
     */
    protected Group lookUpGroup(String qname) throws SAXException {
        int index = qname.indexOf(":");
        String localName,prefix;
        localName = prefix = null;
        if(index>=0){
            localName = qname.substring(index + 1);
            prefix = qname.substring(0,index);
        }else{
            prefix = "";
            localName = qname;
        }
        logger.finest("prefix is " + prefix);
        logger.finest("localName is " + localName);
        
        Iterator it;
        if((this.prefix == null && prefix == null)||(this.prefix!=null && this.prefix.equals(prefix))){
            if(schema!=null)
                return lookUpGroup(localName, schema, new LinkedList());
        }else{
            if(imports!=null){
            it = imports.iterator();
//System.out.println("prefixLookup == null? "+(prefixCache==null)+" "+this.uri + " ");
            while (it.hasNext()) {
                Schema s = (Schema) it.next();
                String ns = s.getTargetNamespace().toString();
//System.out.println(ns);
                String prefixLookup = prefixCache!=null?(String)prefixCache.get(ns):null;
                if(prefix == null || prefixLookup==null || prefix.equals(prefixLookup)){
                    Group ct = lookUpGroup(localName, s, new LinkedList());
                	if (ct != null) {
                	    return ct;
                	}
                }
            }}
        }

        if(groups!=null){
        it = groups.iterator();

        while (it.hasNext()) {
            Object o = it.next();

            if (o instanceof GroupHandler) {
                GroupHandler sst = (GroupHandler) o;

                if (localName.equalsIgnoreCase(sst.getName())) {
                    return (Group) sst.compress(this);
                }
            } else {
                Group sst = (Group) o;

                if (localName.equalsIgnoreCase(sst.getName())) {
                    return sst;
                }
            }
        }}

        return null;
    }

    /*
     * helper method for lookUpAttributeGroup
     */
    private AttributeGroup lookUpAttributeGroup(String localName, Schema s,
        List targets) {
        if (s == null) {
            return null;
        }

        targets.add(s.getTargetNamespace());

        if (s.getAttributeGroups() != null) {
            AttributeGroup[] sts = s.getAttributeGroups();

            for (int i = 0; (sts != null) && (i < sts.length); i++) {
                if (localName.equalsIgnoreCase(sts[i].getName())) {
                    return sts[i];
                }
            }
        }

        if (s.getImports() != null) {
            Schema[] ss = s.getImports();

            for (int i = 0; (ss != null) && (i < ss.length); i++) {
                if (!targets.contains(ss[i].getTargetNamespace())) {
                    AttributeGroup st = lookUpAttributeGroup(localName, ss[i],
                            targets);

                    if (st != null) {
                        return st;
                    }
                }
            }
        }

        return null;
    }

    /**
     * <p>
     * convinience method for the package
     * </p>
     *
     * @param qName
     *
     * @return
     *
     * @throws SAXException
     */
    protected AttributeGroup lookUpAttributeGroup(String qname)
        throws SAXException {
        int index = qname.indexOf(":");
        String localName,prefix;
        localName = prefix = null;
        if(index>=0){
            localName = qname.substring(index + 1);
            prefix = qname.substring(0,index);
        }else{
            prefix = "";
            localName = qname;
        }
        logger.finest("prefix is " + prefix);
        logger.finest("localName is " + localName);
        
        Iterator it;
        if((this.prefix == null && prefix == null)||(this.prefix!=null && this.prefix.equals(prefix))){
            if(schema!=null)
                return lookUpAttributeGroup(localName, schema, new LinkedList());
        }else{
            if(imports!=null){
            it = imports.iterator();
//System.out.println("prefixLookup == null? "+(prefixCache==null)+" "+this.uri + " ");
            while (it.hasNext()) {
                Schema s = (Schema) it.next();
                String ns = s.getTargetNamespace().toString();
//System.out.println(ns);
                String prefixLookup = prefixCache!=null?(String)prefixCache.get(ns):null;
                if(prefix == null || prefixLookup==null || prefix.equals(prefixLookup)){
                    AttributeGroup ct = lookUpAttributeGroup(localName, s, new LinkedList());
                	if (ct != null) {
                	    return ct;
                	}
                }
            }}
        }

        if(attributeGroups!=null){
        it = attributeGroups.iterator();

        while (it.hasNext()) {
            Object o = it.next();

            if (o instanceof AttributeGroupHandler) {
                AttributeGroupHandler sst = (AttributeGroupHandler) o;

                if (localName.equalsIgnoreCase(sst.getName())) {
                    return sst.compress(this);
                }
            } else {
                AttributeGroup sst = (AttributeGroup) o;

                if (localName.equalsIgnoreCase(sst.getName())) {
                    return sst;
                }
            }
        }}

        return null;
    }

    /*
     * helper method for lookUpAttribute
     */
    private Attribute lookUpAttribute(String localName, Schema s, List targets) {
        if (s == null) {
            return null;
        }

        targets.add(s.getTargetNamespace());

        if (s.getAttributes() != null) {
            Attribute[] sts = s.getAttributes();

            for (int i = 0; (sts != null) && (i < sts.length); i++) {
                if(sts[i]!=null && sts[i].getName()!=null){
                if (localName.equalsIgnoreCase(sts[i].getName())) {
                    return sts[i];
                }}
            }
        }

        if (s.getImports() != null) {
            Schema[] ss = s.getImports();

            for (int i = 0; (ss != null) && (i < ss.length); i++) {
                if (!targets.contains(ss[i].getTargetNamespace())) {
                    Attribute st = lookUpAttribute(localName, ss[i], targets);

                    if (st != null) {
                        return st;
                    }
                }
            }
        }

        return null;
    }

    /**
     * <p>
     * convinience method for package
     * </p>
     *
     * @param qName
     *
     * @return
     *
     * @throws SAXException
     */
    protected Attribute lookUpAttribute(String qname) throws SAXException {
        int index = qname.indexOf(":");
        String localName,prefix;
        localName = prefix = null;
        if(index>=0){
            localName = qname.substring(index + 1);
            prefix = qname.substring(0,index);
        }else{
            prefix = "";
            localName = qname;
        }
        logger.finest("prefix is " + prefix);
        logger.finest("localName is " + localName);

        Iterator it;
        if((this.prefix == null && prefix == null)||(this.prefix!=null && this.prefix.equals(prefix))){
            if(schema!=null)
                return lookUpAttribute(localName, schema, new LinkedList());
        }else{
            if(imports!=null){
            it = imports.iterator();
//System.out.println("prefixLookup == null? "+(prefixCache==null)+" "+this.uri + " ");
            while (it.hasNext()) {
                Schema s = (Schema) it.next();
                String ns = s.getTargetNamespace().toString();
//System.out.println(ns);
                String prefixLookup = prefixCache!=null?(String)prefixCache.get(ns):null;
                if(prefix == null || prefixLookup==null || prefix.equals(prefixLookup)){
                    Attribute ct = lookUpAttribute(localName, s, new LinkedList());
                	if (ct != null) {
                	    return ct;
                	}
                }
            }}
        }

        if(attributes!=null){
        it = attributes.iterator();

        while (it.hasNext()) {
            Object o = it.next();

            if (o instanceof AttributeHandler) {
                AttributeHandler sst = (AttributeHandler) o;

                if (localName.equalsIgnoreCase(sst.getName())) {
                    return sst.compress(this);
                }
            } else {
                Attribute sst = (Attribute) o;

                if (localName.equalsIgnoreCase(sst.getName())) {
                    return sst;
                }
            }
        }}

        return null;
    }

    /**
     * <p>
     * convinience method for package
     * </p>
     *
     * @param qName
     *
     * @return
     *
     * @throws SAXException
     */
    protected Type lookUpType(String qname) throws SAXException {
        if(qname == null)
            return null;
        Type t = null;
        t = lookUpComplexType(qname);
        t = t==null?lookUpSimpleType(qname):t;
        return t;
    }

    /*
     * helper method that merges the provided Schema into this Schema
     */
    private void addSchema(Schema s) {
        Object[] objs = null;

        objs = s.getAttributes();

        if (objs != null) {
            if ((attributes == null) && (objs.length > 0)) {
                attributes = new HashSet();
            }

            for (int i = 0; i < objs.length; i++)
                attributes.add(objs[i]);
        }

        objs = s.getAttributeGroups();

        if (objs != null) {
            if ((attributeGroups == null) && (objs.length > 0)) {
                attributeGroups = new HashSet();
            }

            for (int i = 0; i < objs.length; i++)
                attributeGroups.add(objs[i]);
        }

        objs = s.getComplexTypes();

        if (objs != null) {
            if ((complexTypes == null) && (objs.length > 0)) {
                complexTypes = new HashSet();
            }

            for (int i = 0; i < objs.length; i++)
                complexTypes.add(objs[i]);
        }

        objs = s.getElements();

        if (objs != null) {
            if ((elements == null) && (objs.length > 0)) {
                elements = new HashSet();
            }

            for (int i = 0; i < objs.length; i++)
                elements.add(objs[i]);
        }

        objs = s.getGroups();

        if (objs != null) {
            if ((groups == null) && (objs.length > 0)) {
                groups = new HashSet();
            }

            for (int i = 0; i < objs.length; i++)
                groups.add(objs[i]);
        }

        objs = s.getImports();

        if (objs != null) {
            if ((imports == null) && (objs.length > 0)) {
                imports = new HashSet();
            }

            for (int i = 0; i < objs.length; i++)
                imports.add(objs[i]);
        }

        objs = s.getSimpleTypes();

        if (objs != null) {
            if ((simpleTypes == null) && (objs.length > 0)) {
                simpleTypes = new HashSet();
            }

            for (int i = 0; i < objs.length; i++)
                simpleTypes.add(objs[i]);
        }

        URI tempuri = s.getURI();

        if (uri == null) {
            uri = tempuri;
        } else {
            if (tempuri != null) {
                uri = tempuri.resolve(uri);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the targetNamespace.
     */
    public URI getTargetNamespace() {
        return targetNamespace;
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#getHandlerType()
     */
    public int getHandlerType() {
        return DEFAULT;
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#endElement(java.lang.String,
     *      java.lang.String)
     */
    public void endElement(String namespaceURI, String localName)
        throws SAXException {
        //System.out.println("NS END = "+targetNamespace);
    }

    /**
     * <p>
     * Default implementation of a Schema for a parsed schema.
     * </p>
     *
     * @author dzwiers
     *
     * @see Schema
     */
    private static class DefaultSchema implements Schema {
        // file visible to avoid set* methods
        boolean attributeFormDefault;
        boolean elementFormDefault;
        String id;
        URI targetNamespace;
        String version;
        int finalDefault;
        int blockDefault;
        URI uri;
        Schema[] imports;
        SimpleType[] simpleTypes;
        ComplexType[] complexTypes;
        AttributeGroup[] attributeGroups;
        Attribute[] attributes;
        Element[] elements;
        Group[] groups;
        String prefix;

        /**
         * @see org.geotools.xml.xsi.Schema#isAttributeFormDefault()
         */
        public boolean isAttributeFormDefault() {
            return attributeFormDefault;
        }

        /**
         * @see org.geotools.xml.xsi.Schema#getAttributeGroups()
         */
        public AttributeGroup[] getAttributeGroups() {
            return attributeGroups;
        }

        /**
         * @see org.geotools.xml.xsi.Schema#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attributes;
        }

        /**
         * @see org.geotools.xml.xsi.Schema#getBlockDefault()
         */
        public int getBlockDefault() {
            return blockDefault;
        }

        /**
         * @see org.geotools.xml.xsi.Schema#getComplexTypes()
         */
        public ComplexType[] getComplexTypes() {
            return complexTypes;
        }

        /**
         * @see org.geotools.xml.xsi.Schema#isElementFormDefault()
         */
        public boolean isElementFormDefault() {
            return elementFormDefault;
        }

        /**
         * @see org.geotools.xml.xsi.Schema#getElements()
         */
        public Element[] getElements() {
            return elements;
        }

        /**
         * @see org.geotools.xml.xsi.Schema#getFinalDefault()
         */
        public int getFinalDefault() {
            return finalDefault;
        }

        /**
         * @see org.geotools.xml.xsi.Schema#getId()
         */
        public String getId() {
            return id;
        }

        /**
         * @see org.geotools.xml.xsi.Schema#getImports()
         */
        public Schema[] getImports() {
            return imports;
        }

        /**
         * @see org.geotools.xml.xsi.Schema#getSimpleTypes()
         */
        public SimpleType[] getSimpleTypes() {
            return simpleTypes;
        }

        /**
         * @see org.geotools.xml.xsi.Schema#getTargetNamespace()
         */
        public URI getTargetNamespace() {
            return targetNamespace;
        }

        /**
         * @see org.geotools.xml.xsi.Schema#getURI()
         */
        public URI getURI() {
            return uri;
        }

        /**
         * @see org.geotools.xml.xsi.Schema#getVersion()
         */
        public String getVersion() {
            return version;
        }

        /**
         * @see org.geotools.xml.xsi.Schema#getGroups()
         */
        public Group[] getGroups() {
            return groups;
        }

        /**
         * @see org.geotools.xml.xsi.Schema#includesURI(java.net.URI)
         */
        public boolean includesURI(URI uri) {
            return this.uri.equals(uri);
        }

        /**
         * @see org.geotools.xml.schema.Schema#getPrefix()
         */
        public String getPrefix() {
            return prefix;
        }
    }
}
