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

import org.geotools.xml.handlers.xsi.AttributeHandler;
import org.geotools.xml.handlers.xsi.ComplexTypeHandler;
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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.naming.OperationNotSupportedException;


/**
 * <p>
 * DOCUMENT ME!
 * </p>
 *
 * @author dzwiers
 */
public class DocumentWriter {
    /** DOCUMENT ME!  */
    public static final Logger logger = Logger.getLogger(
            "net.refractions.xml.write");

    /** DOCUMENT ME!  */
    public static final String WRITE_SCHEMA = "DocumentWriter_WRITE_SCHEMA";

    /**
     * <p>
     * TODO DOCUMENT ME!
     * </p>
     * Note:
     *
     * @param value
     * @param schema
     * @param f
     * @param hints
     *
     * @throws OperationNotSupportedException
     * @throws IOException
     *
     * @see WRITE_SCHEMA
     */
    public static void writeDocument(Object value, Schema schema, File f,
        Map hints) throws OperationNotSupportedException, IOException {
        if ((f == null) || (!f.canWrite())) {
            throw new IOException("Cannot write to " + f);
        }

        if (hints.containsKey(WRITE_SCHEMA)) {
            Map hints2 = new HashMap(hints);
            hints2.remove(WRITE_SCHEMA);

            File f2 = new File(f.getParentFile(),
                    f.getName().substring(0, f.getName().indexOf(".")) + ".xsd");
            writeSchema(schema, f2, hints2);

            // would be thrown anyway
            //                throw new IOException("Schema Cannot be written to "+f2);
        }

        Writer wf = new FileWriter(f);
        WriterContentHandler wch = new WriterContentHandler(schema, wf); // should deal with xmlns declarations
        Element[] elems = schema.getElements();

        if (elems == null) {
            throw new IOException("Cannot write for Schema "
                + schema.getTargetNamespace());
        }

        wch.startDocument();

        for (int i = 0; i < elems.length; i++) {
            if ((elems[i] != null) && (elems[i].getType() != null)) {
                Type t = elems[i].getType();

                if (t.canEncode(elems[i], value, hints)) {
                    t.encode(elems[i], value, wch, hints);
                }
            }
        }

        wch.endDocument();
    }

    /**
     * DOCUMENT ME!
     *
     * @param schema DOCUMENT ME!
     * @param f DOCUMENT ME!
     * @param hints DOCUMENT ME!
     *
     * @throws IOException  
     */
    public static void writeSchema(Schema schema, File f, Map hints)
        throws IOException {
        Writer wf = new FileWriter(f);
        WriterContentHandler wch = new WriterContentHandler(schema, wf); // should deal with xmlns declarations
        Element[] elems = schema.getElements();

        if (elems == null) {
            throw new IOException("Cannot write for Schema "
                + schema.getTargetNamespace());
        }

        wch.startDocument();

        writeSchema(schema, wch, hints);

        wch.endDocument();
    }

    private static void writeSchema(Schema schema, PrintHandler ph, Map hints)
        throws IOException {
        if (schema == null) {
            return;
        }

        AttributesImpl ai = new AttributesImpl();

        ai.addAttribute("", "targetNamespace", "", "anyUri",
            schema.getTargetNamespace());
        ai.addAttribute("", "xmlns", "", "anyUri", XSISimpleTypes.NAMESPACE);
        ai.addAttribute("", "xmlns:" + schema.getPrefix(), "", "anyUri",
            schema.getTargetNamespace());

        Schema[] imports = schema.getImports();

        for (int i = 0; i < imports.length; i++) {
            ai.addAttribute("", "xmlns:" + imports[i].getPrefix(), "",
                "anyUri", imports[i].getTargetNamespace());
        }

        if ((schema.getId() != null) && (schema.getId() != "")) {
            ai.addAttribute("", "id", "", "ID", schema.getId());
        }

        if ((schema.getVersion() != null) && (schema.getVersion() != "")) {
            ai.addAttribute("", "version", "", "String", schema.getVersion());
        }

        if (schema.isAttributeFormDefault()) {
            ai.addAttribute("", "attributeFormDefault", "", "NMTOKEN",
                "qualified");
        }

        if (schema.isElementFormDefault()) {
            ai.addAttribute("", "elementFormDefault", "", "NMTOKEN", "qualified");
        }

        if (schema.getBlockDefault() != Schema.NONE) {
            ai.addAttribute("", "blockDefault", "", "NMTOKENS",
                ComplexTypeHandler.writeBlock(schema.getBlockDefault()));
        }

        if (schema.getFinalDefault() != Schema.NONE) {
            ai.addAttribute("", "finalDefault", "", "NMTOKENS",
                ComplexTypeHandler.writeFinal(schema.getFinalDefault()));
        }

        ph.startElement(XSISimpleTypes.NAMESPACE, "schema", ai);

        for (int i = 0; i < imports.length; i++)
            writeImport(imports[i], ph, hints);

        Element[] elems = schema.getElements();

        if (elems != null) {
            for (int i = 0; i < elems.length; i++)
                writeElement(elems[i], schema, ph, hints);
        }

        ComplexType[] cts = schema.getComplexTypes();

        if (elems != null) {
            for (int i = 0; i < cts.length; i++)
                writeComplexType(cts[i], schema, ph, hints);
        }

        SimpleType[] sts = schema.getSimpleTypes();

        if (elems != null) {
            for (int i = 0; i < sts.length; i++)
                writeSimpleType(sts[i], schema, ph, hints);
        }

        Group[] groups = schema.getGroups();

        if (elems != null) {
            for (int i = 0; i < groups.length; i++)
                writeGroup(groups[i], schema, ph, hints);
        }

        Attribute[] attrs = schema.getAttributes();

        if (elems != null) {
            for (int i = 0; i < attrs.length; i++)
                writeAttribute(attrs[i], schema, ph, hints);
        }

        AttributeGroup[] attrgrps = schema.getAttributeGroups();

        if (elems != null) {
            for (int i = 0; i < attrgrps.length; i++)
                writeAttributeGroup(attrgrps[i], schema, ph, hints);
        }

        ph.endElement(XSISimpleTypes.NAMESPACE, "schema");
    }

    private static void writeImport(Schema schema, PrintHandler ph, Map hints)
        throws IOException {
        AttributesImpl ai = new AttributesImpl();

        if ((schema.getId() != null) && (schema.getId() != "")) {
            ai.addAttribute("", "id", "", "ID", schema.getId());
        }

        ai.addAttribute("", "namespace", "", "anyUri",
            schema.getTargetNamespace());

        if (schema.getURI() != null) {
            ai.addAttribute("", "schemaLocation", "", "anyUri",
                schema.getURI().toString());
        }

        ph.element(XSISimpleTypes.NAMESPACE, "import", ai);
    }

    private static void writeElement(Element element, Schema schema,
        PrintHandler ph, Map hints) throws IOException {
        AttributesImpl ai = new AttributesImpl();

        if ((element.getId() != null) && (element.getId() != "")) {
            ai.addAttribute("", "id", "", "ID", element.getId());
        }

        if (element.getMaxOccurs() != 1) {
            ai.addAttribute("", "maxOccurs", "", "Union",
                (element.getMaxOccurs() == Integer.MAX_VALUE) ? "unbounded"
                                                              : (""
                + element.getMaxOccurs()));
        }

        if (element.getMinOccurs() != 1) {
            ai.addAttribute("", "minOccurs", "", "ID",
                "" + element.getMinOccurs());
        }

        boolean nested = false;

        if (element.getNamespace().equals(schema.getTargetNamespace())) {
            // search schema for element, then it's a ref
            Element[] elems = schema.getElements();
            boolean found = false;

            for (int i = 0; (i < elems.length) && !found; i++)
                if (element.getName().equals(elems[i].getName())) {
                    found = true;
                    ai.addAttribute("", "ref", "", "QName", element.getName());
                }

            if (!found) {
                // type in other NS
                if (!element.getType().getNamespace().equals(schema
                            .getTargetNamespace())) {
                    found = true;

                    Schema s = SchemaFactory.getInstance(element.getNamespace());

                    if ((element.getName() != null)
                            && (element.getName() != "")) {
                        ai.addAttribute("", "name", "", "QName",
                            element.getName());
                    }

                    ai.addAttribute("", "type", "", "QName",
                        s.getPrefix() + ":" + element.getType().getName());
                }

                // search schema for type, then type can be a qName
                Type[] types = schema.getComplexTypes();

                for (int i = 0; (i < types.length) && !found; i++)

                    // TODO use equals here
                    if (element.getType().getName().equals(types[i].getName())) {
                        found = true;

                        if ((element.getName() != null)
                                && (element.getName() != "")) {
                            ai.addAttribute("", "name", "", "QName",
                                element.getName());
                        }

                        ai.addAttribute("", "type", "", "QName",
                            element.getType().getName());
                    }

                types = schema.getSimpleTypes();

                for (int i = 0; (i < types.length) && !found; i++)

                    // TODO use equals here
                    if (element.getType().getName().equals(types[i].getName())) {
                        found = true;

                        if ((element.getName() != null)
                                && (element.getName() != "")) {
                            ai.addAttribute("", "name", "", "QName",
                                element.getName());
                        }

                        ai.addAttribute("", "type", "", "QName",
                            element.getType().getName());
                    }

                if (!found) {
                    // 	we are nested  ... log this
                    nested = true;

                    if ((element.getName() != null)
                            && (element.getName() != "")) {
                        ai.addAttribute("", "name", "", "QName",
                            element.getName());
                    }
                }
            }
        } else {
            // use a ref
            Schema s = SchemaFactory.getInstance(element.getNamespace());
            ai.addAttribute("", "ref", "", "QName",
                s.getPrefix() + ":" + element.getName());
        }

        if (element.isNillable()) {
            ai.addAttribute("", "nillable", "", "boolean", "true");
        }

        if ((element.getDefault() != null) && (element.getDefault() != "")) {
            ai.addAttribute("", "default", "", "String", element.getDefault());
        } else {
            if ((element.getFixed() != null) && (element.getFixed() != "")) {
                ai.addAttribute("", "fixed", "", "String", element.getFixed());
            }
        }

        if (element.getSubstitutionGroup() != null) {
            String s = "";

            if (!element.getSubstitutionGroup().getNamespace().equals(schema
                        .getTargetNamespace())) {
                Schema sss = SchemaFactory.getInstance(element.getSubstitutionGroup()
                                                              .getNamespace());
                s = sss.getPrefix() + ":";
            }

            s += element.getSubstitutionGroup().getName();
            ai.addAttribute("", "substitutionGroup", "", "QName", s);
        }

        if (element.isForm()) {
            ai.addAttribute("", "form", "", "NMTOKEN", "qualified");
        }

        if (element.getFinal() != Schema.NONE) {
            ai.addAttribute("", "final", "", "NMTOKENS",
                ComplexTypeHandler.writeFinal(element.getFinal()));
        }

        if (element.getBlock() != Schema.NONE) {
            ai.addAttribute("", "block", "", "NMTOKENS",
                ComplexTypeHandler.writeBlock(element.getBlock()));
        }

        if (element.isAbstract()) {
            ai.addAttribute("", "abstract", "", "boolean", "true");
        }

        ph.startElement(XSISimpleTypes.NAMESPACE, "element", ai);

        if (nested) {
            if (element.getType() instanceof ComplexType) {
                writeComplexType((ComplexType) element.getType(), schema, ph,
                    hints);
            } else {
                writeSimpleType((SimpleType) element.getType(), schema, ph,
                    hints);
            }
        }

        ph.endElement(XSISimpleTypes.NAMESPACE, "element");
    }

    private static void writeAttribute(Attribute attribute, Schema schema,
        PrintHandler ph, Map hints) throws IOException {
        AttributesImpl ai = new AttributesImpl();

        if ((attribute.getId() != null) && (attribute.getId() != "")) {
            ai.addAttribute("", "id", "", "ID", attribute.getId());
        }

        boolean nested = false;

        if (attribute.getNamespace().equals(schema.getTargetNamespace())) {
            // search schema for element, then it's a ref
            Attribute[] elems = schema.getAttributes();
            boolean found = false;

            for (int i = 0; (i < elems.length) && !found; i++)
                if (attribute.getName().equals(elems[i].getName())) {
                    found = true;
                    ai.addAttribute("", "ref", "", "QName", attribute.getName());
                }

            if (!found) {
                // type in other NS
                if (!attribute.getSimpleType().getNamespace().equals(schema
                            .getTargetNamespace())) {
                    found = true;

                    Schema s = SchemaFactory.getInstance(attribute.getNamespace());

                    if ((attribute.getName() != null)
                            && (attribute.getName() != "")) {
                        ai.addAttribute("", "name", "", "QName",
                            attribute.getName());
                    }

                    ai.addAttribute("", "type", "", "QName",
                        s.getPrefix() + ":"
                        + attribute.getSimpleType().getName());
                }

                // search schema for type, then type can be a qName
                SimpleType[] types = schema.getSimpleTypes();

                for (int i = 0; (i < types.length) && !found; i++)

                    // TODO use equals here
                    if (attribute.getSimpleType().getName().equals(types[i]
                                .getName())) {
                        found = true;

                        if ((attribute.getName() != null)
                                && (attribute.getName() != "")) {
                            ai.addAttribute("", "name", "", "QName",
                                attribute.getName());
                        }

                        ai.addAttribute("", "type", "", "QName",
                            attribute.getSimpleType().getName());
                    }

                if (!found) {
                    // 	we are nested  ... log this
                    nested = true;

                    if ((attribute.getName() != null)
                            && (attribute.getName() != "")) {
                        ai.addAttribute("", "name", "", "QName",
                            attribute.getName());
                    }
                }
            }
        } else {
            // use a ref
            Schema s = SchemaFactory.getInstance(attribute.getNamespace());
            ai.addAttribute("", "ref", "", "QName",
                s.getPrefix() + ":" + attribute.getName());
        }

        if (attribute.getUse() != Attribute.OPTIONAL) {
            ai.addAttribute("", "use", "", "NMTOKEN",
                AttributeHandler.writeUse(attribute.getUse()));
        }

        if ((attribute.getDefault() != null) && (attribute.getDefault() != "")) {
            ai.addAttribute("", "default", "", "String", attribute.getDefault());
        } else {
            if ((attribute.getFixed() != null) && (attribute.getFixed() != "")) {
                ai.addAttribute("", "fixed", "", "String", attribute.getFixed());
            }
        }

        if (attribute.isForm()) {
            ai.addAttribute("", "form", "", "NMTOKEN", "qualified");
        }

        ph.startElement(XSISimpleTypes.NAMESPACE, "attribute", ai);

        if (nested) {
            writeSimpleType(attribute.getSimpleType(), schema, ph, hints);
        }

        ph.endElement(XSISimpleTypes.NAMESPACE, "attribute");
    }

    private static void writeGroup(Group group, Schema schema, PrintHandler ph,
        Map hints) throws IOException {
        AttributesImpl ai = new AttributesImpl();

        if ((group.getId() != null) && (group.getId() != "")) {
            ai.addAttribute("", "id", "", "ID", group.getId());
        }

        boolean nested = false;

        if (group.getNamespace().equals(schema.getTargetNamespace())) {
            // search schema for element, then it's a ref
            Group[] groups = schema.getGroups();
            boolean found = false;

            for (int i = 0; (i < groups.length) && !found; i++)
                if (group.getName().equals(groups[i].getName())) {
                    found = true;
                    ai.addAttribute("", "ref", "", "QName", group.getName());
                }

            if (!found) {
                ai.addAttribute("", "name", "", "QName", group.getName());
                nested = true;
            }
        } else {
            // use a ref
            Schema s = SchemaFactory.getInstance(group.getNamespace());
            ai.addAttribute("", "ref", "", "QName",
                s.getPrefix() + ":" + group.getName());
        }

        if (group.getMaxOccurs() != 1) {
            ai.addAttribute("", "maxOccurs", "", "Union",
                (group.getMaxOccurs() == Integer.MAX_VALUE) ? "unbounded"
                                                            : (""
                + group.getMaxOccurs()));
        }

        if (group.getMinOccurs() != 1) {
            ai.addAttribute("", "minOccurs", "", "ID", ""
                + group.getMinOccurs());
        }

        ph.startElement(XSISimpleTypes.NAMESPACE, "group", ai);

        if (nested) {
            if (group.getChild().getGrouping() == ElementGrouping.CHOICE) {
                writeChoice((Choice) group.getChild(), schema, ph, hints);
            } else {
                writeSequence((Sequence) group.getChild(), schema, ph, hints);
            }
        }

        ph.endElement(XSISimpleTypes.NAMESPACE, "group");
    }

    private static void writeAttributeGroup(AttributeGroup attributeGroup,
        Schema schema, PrintHandler ph, Map hints) throws IOException {
        AttributesImpl ai = new AttributesImpl();

        if ((attributeGroup.getId() != null) && (attributeGroup.getId() != "")) {
            ai.addAttribute("", "id", "", "ID", attributeGroup.getId());
        }

        boolean nested = false;

        if (attributeGroup.getNamespace().equals(schema.getTargetNamespace())) {
            // search schema for element, then it's a ref
            Group[] groups = schema.getGroups();
            boolean found = false;

            for (int i = 0; (i < groups.length) && !found; i++)
                if (attributeGroup.getName().equals(groups[i].getName())) {
                    found = true;
                    ai.addAttribute("", "ref", "", "QName",
                        attributeGroup.getName());
                }

            if (!found) {
                ai.addAttribute("", "name", "", "QName",
                    attributeGroup.getName());
                nested = true;
            }
        } else {
            // use a ref
            Schema s = SchemaFactory.getInstance(attributeGroup.getNamespace());
            ai.addAttribute("", "ref", "", "QName",
                s.getPrefix() + ":" + attributeGroup.getName());
        }

        ph.startElement(XSISimpleTypes.NAMESPACE, "group", ai);

        if (nested) {
            if (attributeGroup.getAnyAttributeNameSpace() != null) {
                ai = new AttributesImpl();
                ai.addAttribute("", "namespace", "", "special",
                    attributeGroup.getAnyAttributeNameSpace());
                ph.element(XSISimpleTypes.NAMESPACE, "anyAttribute", ai);
            }

            if (attributeGroup.getAttributes() != null) {
                Attribute[] attrs = attributeGroup.getAttributes();

                for (int i = 0; i < attrs.length; i++)
                    writeAttribute(attrs[i], schema, ph, hints);
            }
        }

        ph.endElement(XSISimpleTypes.NAMESPACE, "group");
    }

    private static void writeSimpleType(SimpleType simpleType, Schema schema,
        PrintHandler ph, Map hints) throws IOException {
        if (XSISimpleTypes.NAMESPACE.equals(simpleType.getNamespace())) {
            //error - not sure what to do
            // TODO log the type error - throw an exception?
        }

        AttributesImpl ai = new AttributesImpl();

        if ((simpleType.getId() != null) && (simpleType.getId() != "")) {
            ai.addAttribute("", "id", "", "ID", simpleType.getId());
        }

        if ((simpleType.getName() != null) && (simpleType.getName() != "")) {
            ai.addAttribute("", "name", "", "NCName", simpleType.getName());
        }

        if (simpleType.getFinal() != Schema.NONE) {
            ai.addAttribute("", "final", "", "NMTOKENS",
                ComplexTypeHandler.writeFinal(simpleType.getFinal()));
        }

        ph.startElement(XSISimpleTypes.NAMESPACE, "simpleType", ai);

        switch (simpleType.getChildType()) {
        case SimpleType.RESTRICTION:

            // determine whether to print or reference the child st
            SimpleType st = simpleType.getParents()[0];
            ai = null;

            if (schema.getTargetNamespace().equals(st.getNamespace())) {
                if ((st.getName() != null) && (st.getName() != "")) {
                    SimpleType[] sts = schema.getSimpleTypes();

                    if (sts != null) {
                        for (int i = 0; i < sts.length; i++)
                            if (st.getName().equals(sts[i].getName())) {
                                ai = new AttributesImpl();
                                ai.addAttribute("", "base", "", "QName",
                                    st.getName());
                            }
                    }
                }
            } else {
                ai = new AttributesImpl();

                Schema s = SchemaFactory.getInstance(st.getNamespace());
                ai.addAttribute("", "base", "", "QName",
                    s.getPrefix() + ":" + st.getName());
            }

            ph.startElement(XSISimpleTypes.NAMESPACE, "restriction", ai);

            if (ai == null) {
                writeSimpleType(st, schema, ph, hints);
            }

            Facet[] facets = simpleType.getFacets();

            if (facets != null) {
                for (int i = 0; i < facets.length; i++)
                    writeFacet(facets[i], schema, ph, hints);
            }

            ph.endElement(XSISimpleTypes.NAMESPACE, "restriction");

            break;

        case SimpleType.LIST:

            // determine whether to print or reference the child st
            st = simpleType.getParents()[0];
            ai = null;

            if (schema.getTargetNamespace().equals(st.getNamespace())) {
                if ((st.getName() != null) && (st.getName() != "")) {
                    SimpleType[] sts = schema.getSimpleTypes();

                    if (sts != null) {
                        for (int i = 0; i < sts.length; i++)
                            if (st.getName().equals(sts[i].getName())) {
                                ai = new AttributesImpl();
                                ai.addAttribute("", "itemType", "", "QName",
                                    st.getName());
                            }
                    }
                }
            } else {
                ai = new AttributesImpl();

                Schema s = SchemaFactory.getInstance(st.getNamespace());
                ai.addAttribute("", "itemType", "", "QName",
                    s.getPrefix() + ":" + st.getName());
            }

            ph.startElement(XSISimpleTypes.NAMESPACE, "list", ai);

            if (ai == null) {
                writeSimpleType(st, schema, ph, hints);
            }

            ph.endElement(XSISimpleTypes.NAMESPACE, "list");

            break;

        case SimpleType.UNION:

            // determine whether to print or reference the child st
            SimpleType[] sts = simpleType.getParents();
            String memberTypes = null;
            List childTs = new LinkedList();

            if (sts != null) {
                for (int j = 0; j < sts.length; j++) {
                    st = sts[j];

                    if (schema.getTargetNamespace().equals(st.getNamespace())) {
                        boolean found = false;

                        if ((st.getName() != null) && (st.getName() != "")) {
                            SimpleType[] sts2 = schema.getSimpleTypes();

                            if (sts2 != null) {
                                for (int i = 0; i < sts2.length; i++)
                                    if (st.getName().equals(sts2[i].getName())) {
                                        found = true;

                                        if (memberTypes == null) {
                                            memberTypes = st.getName();
                                        } else {
                                            memberTypes += (" " + st.getName());
                                        }
                                    }
                            }
                        }

                        if (!found) {
                            childTs.add(st);
                        }
                    } else {
                        ai = new AttributesImpl();

                        Schema s = SchemaFactory.getInstance(st.getNamespace());

                        if (memberTypes == null) {
                            memberTypes = s.getPrefix() + ":" + st.getName();
                        } else {
                            memberTypes += (" " + s.getPrefix() + ":"
                            + st.getName());
                        }
                    }
                }
            }

            if (memberTypes != null) {
                ai = new AttributesImpl();
                ai.addAttribute("", "memberTypes", "", "QName", memberTypes);
            }

            ph.startElement(XSISimpleTypes.NAMESPACE, "union", ai);

            if (childTs.size() > 0) {
                Iterator i = childTs.iterator();

                while (i.hasNext())
                    writeSimpleType((SimpleType) i.next(), schema, ph, hints);
            }

            ph.endElement(XSISimpleTypes.NAMESPACE, "union");

            break;
        }

        ph.endElement(XSISimpleTypes.NAMESPACE, "simpleType");
    }

    private static void writeChoice(Choice choice, Schema schema,
        PrintHandler ph, Map hints) throws IOException {
        AttributesImpl ai = new AttributesImpl();

        if ((choice.getId() != null) && (choice.getId() != "")) {
            ai.addAttribute("", "id", "", "ID", choice.getId());
        }

        if (choice.getMaxOccurs() != 1) {
            ai.addAttribute("", "maxOccurs", "", "Union",
                (choice.getMaxOccurs() == Integer.MAX_VALUE) ? "unbounded"
                                                             : (""
                + choice.getMaxOccurs()));
        }

        if (choice.getMinOccurs() != 1) {
            ai.addAttribute("", "minOccurs", "", "ID",
                "" + choice.getMinOccurs());
        }

        ph.startElement(XSISimpleTypes.NAMESPACE, "choice", ai);

        ElementGrouping[] egs = choice.getChildren();

        if (egs != null) {
            for (int i = 0; i < egs.length; i++)
                if (egs[i] != null) {
                    switch (egs[i].getGrouping()) {
                    case ElementGrouping.ALL:
                        writeAll((All) egs[i], schema, ph, hints);

                        break;

                    case ElementGrouping.ANY:
                        writeAny((Any) egs[i], schema, ph, hints);

                        break;

                    case ElementGrouping.CHOICE:
                        writeChoice((Choice) egs[i], schema, ph, hints);

                        break;

                    case ElementGrouping.ELEMENT:
                        writeElement((Element) egs[i], schema, ph, hints);

                        break;

                    case ElementGrouping.GROUP:
                        writeGroup((Group) egs[i], schema, ph, hints);

                        break;

                    case ElementGrouping.SEQUENCE:
                        writeSequence((Sequence) egs[i], schema, ph, hints);

                        break;
                    }
                }
        }

        ph.endElement(XSISimpleTypes.NAMESPACE, "choice");
    }

    private static void writeSequence(Sequence sequence, Schema schema,
        PrintHandler ph, Map hints) throws IOException {
        AttributesImpl ai = new AttributesImpl();

        if ((sequence.getId() != null) && (sequence.getId() != "")) {
            ai.addAttribute("", "id", "", "ID", sequence.getId());
        }

        if (sequence.getMaxOccurs() != 1) {
            ai.addAttribute("", "maxOccurs", "", "Union",
                (sequence.getMaxOccurs() == Integer.MAX_VALUE) ? "unbounded"
                                                               : (""
                + sequence.getMaxOccurs()));
        }

        if (sequence.getMinOccurs() != 1) {
            ai.addAttribute("", "minOccurs", "", "ID",
                "" + sequence.getMinOccurs());
        }

        ph.startElement(XSISimpleTypes.NAMESPACE, "sequence", ai);

        ElementGrouping[] egs = sequence.getChildren();

        if (egs != null) {
            for (int i = 0; i < egs.length; i++)
                if (egs[i] != null) {
                    switch (egs[i].getGrouping()) {
                    case ElementGrouping.ANY:
                        writeAny((Any) egs[i], schema, ph, hints);

                        break;

                    case ElementGrouping.CHOICE:
                        writeChoice((Choice) egs[i], schema, ph, hints);

                        break;

                    case ElementGrouping.ELEMENT:
                        writeElement((Element) egs[i], schema, ph, hints);

                        break;

                    case ElementGrouping.GROUP:
                        writeGroup((Group) egs[i], schema, ph, hints);

                        break;

                    case ElementGrouping.SEQUENCE:
                        writeSequence((Sequence) egs[i], schema, ph, hints);

                        break;
                    }
                }
        }

        ph.endElement(XSISimpleTypes.NAMESPACE, "sequence");
    }

    private static void writeAll(All all, Schema schema, PrintHandler ph,
        Map hints) throws IOException {
        AttributesImpl ai = new AttributesImpl();

        if ((all.getId() != null) && (all.getId() != "")) {
            ai.addAttribute("", "id", "", "ID", all.getId());
        }

        if (all.getMaxOccurs() != 1) {
            ai.addAttribute("", "maxOccurs", "", "Union",
                (all.getMaxOccurs() == Integer.MAX_VALUE) ? "unbounded"
                                                          : (""
                + all.getMaxOccurs()));
        }

        if (all.getMinOccurs() != 1) {
            ai.addAttribute("", "minOccurs", "", "ID", "" + all.getMinOccurs());
        }

        ph.startElement(XSISimpleTypes.NAMESPACE, "all", ai);

        Element[] egs = all.getElements();

        if (egs != null) {
            for (int i = 0; i < egs.length; i++)
                if (egs[i] != null) {
                    writeElement(egs[i], schema, ph, hints);
                }
        }

        ph.endElement(XSISimpleTypes.NAMESPACE, "all");
    }

    private static void writeAny(Any any, Schema schema, PrintHandler ph,
        Map hints) throws IOException {
        AttributesImpl ai = new AttributesImpl();

        if ((any.getId() != null) && (any.getId() != "")) {
            ai.addAttribute("", "id", "", "ID", any.getId());
        }

        if (any.getMaxOccurs() != 1) {
            ai.addAttribute("", "maxOccurs", "", "Union",
                (any.getMaxOccurs() == Integer.MAX_VALUE) ? "unbounded"
                                                          : (""
                + any.getMaxOccurs()));
        }

        if (any.getMinOccurs() != 1) {
            ai.addAttribute("", "minOccurs", "", "ID", "" + any.getMinOccurs());
        }

        if ((any.getNamespace() != null) && (any.getNamespace() != "")) {
            ai.addAttribute("", "namespace", "", "special",
                "" + any.getNamespace());
        }

        ph.element(XSISimpleTypes.NAMESPACE, "any", ai);
    }

    private static void writeComplexType(ComplexType complexType,
        Schema schema, PrintHandler ph, Map hints) throws IOException {
        AttributesImpl ai = new AttributesImpl();

        if ((complexType.getId() != null) && (complexType.getId() != "")) {
            ai.addAttribute("", "id", "", "ID", complexType.getId());
        }

        if ((complexType.getName() != null) && (complexType.getName() != "")) {
            ai.addAttribute("", "name", "", "NCName", complexType.getName());
        }

        if (complexType.isAbstract()) {
            ai.addAttribute("", "abstract", "", "boolean", "true");
        }

        if (complexType.getFinal() != Schema.NONE) {
            ai.addAttribute("", "final", "", "NMTOKENS",
                ComplexTypeHandler.writeFinal(complexType.getFinal()));
        }

        if (complexType.getBlock() != Schema.NONE) {
            ai.addAttribute("", "block", "", "NMTOKENS",
                ComplexTypeHandler.writeBlock(complexType.getBlock()));
        }

        if (complexType.isMixed()) {
            ai.addAttribute("", "mixed", "", "boolean", "true");
        }

        ph.startElement(XSISimpleTypes.NAMESPACE, "complexType", ai);

        ElementGrouping egs = complexType.getChild();
        complexType.getChild();

        // TODO determine if this complexType isDerived ... and make a complexContent or Simplecontent to match
        if (egs != null) {
            switch (egs.getGrouping()) {
            // TODO determine if this will work
            //                    	case ElementGrouping.COMPLEXCONTENT:
            //                    	    writeAny((ComplexContent)egs,schema,ph,hints);
            //                    	    break;
            //                    	case ElementGrouping.SIMPLECONTENT:
            //                    	    writeAny((SimpleContent)egs,schema,ph,hints);
            //                    	    break;
            case ElementGrouping.ALL:
                writeAll((All) egs, schema, ph, hints);

                break;

            case ElementGrouping.CHOICE:
                writeChoice((Choice) egs, schema, ph, hints);

                break;

            case ElementGrouping.GROUP:
                writeGroup((Group) egs, schema, ph, hints);

                break;

            case ElementGrouping.SEQUENCE:
                writeSequence((Sequence) egs, schema, ph, hints);

                break;
            }
        }

        if (complexType.getAnyAttributeNameSpace() != null) {
            ai = new AttributesImpl();
            ai.addAttribute("", "namespace", "", "special",
                complexType.getAnyAttributeNameSpace());
            ph.element(XSISimpleTypes.NAMESPACE, "anyAttribute", ai);
        }

        // TODO think about checking for attribute groupings?
        // add a Attribute.getParentGroup() ... null or Group
        if (complexType.getAttributes() != null) {
            Attribute[] attrs = complexType.getAttributes();

            for (int i = 0; i < attrs.length; i++)
                writeAttribute((Attribute) attrs[i], schema, ph, hints);
        }

        ph.endElement(XSISimpleTypes.NAMESPACE, "complexType");
    }

    private static void writeFacet(Facet facet, Schema schema, PrintHandler ph,
        Map hints) throws IOException {
        if (facet == null) {
            return;
        }

        AttributesImpl ai = new AttributesImpl();
        ai.addAttribute("", "value", "", "ID", facet.getValue());

        switch (facet.getFacetType()) {
        case Facet.ENUMERATION:
            ph.element(XSISimpleTypes.NAMESPACE, "enumeration", ai);

            break;

        case Facet.FRACTIONDIGITS:
            ph.element(XSISimpleTypes.NAMESPACE, "fractionDigits", ai);

            break;

        case Facet.LENGTH:
            ph.element(XSISimpleTypes.NAMESPACE, "length", ai);

            break;

        case Facet.MAXEXCLUSIVE:
            ph.element(XSISimpleTypes.NAMESPACE, "maxExclusive", ai);

            break;

        case Facet.MAXINCLUSIVE:
            ph.element(XSISimpleTypes.NAMESPACE, "maxInclusive", ai);

            break;

        case Facet.MAXLENGTH:
            ph.element(XSISimpleTypes.NAMESPACE, "maxLength", ai);

            break;

        case Facet.MINEXCLUSIVE:
            ph.element(XSISimpleTypes.NAMESPACE, "minExclusive", ai);

            break;

        case Facet.MININCLUSIVE:
            ph.element(XSISimpleTypes.NAMESPACE, "minInclusive", ai);

            break;

        case Facet.MINLENGTH:
            ph.element(XSISimpleTypes.NAMESPACE, "minLength", ai);

            break;

        case Facet.PATTERN:
            ph.element(XSISimpleTypes.NAMESPACE, "pattern", ai);

            break;

        case Facet.TOTALDIGITS:
            ph.element(XSISimpleTypes.NAMESPACE, "totalDigits", ai);

            break;

        case Facet.WHITESPACE:
            ph.element(XSISimpleTypes.NAMESPACE, "whiteSpace", ai);

            break;
        }
    }

    private static class WriterContentHandler implements PrintHandler {
        private boolean firstElement = true; // needed for NS declarations
        private Writer writer;
        private Map prefixMappings; // when the value is null it has not been included yet into the document ...
        private Schema schema;

        public WriterContentHandler(Schema schema, Writer writer) {
            this.writer = writer;
            this.schema = schema;
            prefixMappings = new HashMap();
            prefixMappings.put(schema.getTargetNamespace(), "");

            Schema[] imports = schema.getImports();

            if (imports != null) {
                for (int i = 0; i < imports.length; i++)
                    prefixMappings.put(imports[i].getTargetNamespace(),
                        imports[i].getPrefix());
            }
        }

        private void printXMLNSDecs() throws IOException {
            writer.write(" xmlns=\"" + schema.getTargetNamespace() + "\"");

            Schema[] imports = schema.getImports();
            String s = "";

            if (imports != null) {
                for (int i = 0; i < imports.length; i++) {
                    writer.write(" xmlns:" + imports[i].getPrefix() + "=\""
                        + imports[i].getTargetNamespace() + "\"");

                    if ((imports[i].getURI() != null)
                            && imports[i].getURI().isAbsolute()) {
                        s += (" " + imports[i].getTargetNamespace() + " "
                        + imports[i].getURI());
                    }
                }
            }

            s = s.trim();

            if (!"".equals(s)) {
                writer.write(
                    " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
                writer.write(" xsi:schemaLocation=\"" + s + "\"");
            }
        }

        public void startElement(String namespaceURI, String localName,
            Attributes attributes) throws IOException {
            String prefix = (String) prefixMappings.get(namespaceURI);

            if ((prefix != null) && !prefix.equals("")) {
                prefix = prefix + ":";
            }

            writer.write("<");
            writer.write(prefix + localName);

            if (firstElement) {
                printXMLNSDecs();
                firstElement = false;
            }

            if (attributes != null) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    String name = attributes.getLocalName(i);
                    String value = attributes.getValue(i);
                    writer.write("");
                    writer.write(name);
                    writer.write("=\"");
                    writer.write(value);
                    writer.write("\"");
                }
            }

            writer.write(">");
        }

        public void element(String namespaceURI, String localName,
            Attributes attributes) throws IOException {
            String prefix = (String) prefixMappings.get(namespaceURI);

            if ((prefix != null) && !prefix.equals("")) {
                prefix = prefix + ":";
            }

            writer.write("</");
            writer.write(prefix + localName);

            if (firstElement) {
                printXMLNSDecs();
                firstElement = false;
            }

            if (attributes != null) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    String name = attributes.getLocalName(i);
                    String value = attributes.getValue(i);
                    writer.write("");
                    writer.write(name);
                    writer.write("=\"");
                    writer.write(value);
                    writer.write("\"");
                }
            }

            writer.write(">");
        }

        public void endElement(String namespaceURI, String localName)
            throws IOException {
            String prefix = (String) prefixMappings.get(namespaceURI);

            if ((prefix != null) && !prefix.equals("")) {
                prefix = prefix + ":";
            }

            writer.write("</");
            writer.write(prefix + localName);
            writer.write(">");
        }

        public void characters(char[] arg0, int arg1, int arg2)
            throws IOException {
            writer.write(arg0, arg1, arg2);
        }

        public void characters(String s) throws IOException {
            writer.write(s);
        }

        /**
         * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int,
         *      int)
         */
        public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
            throws IOException {
            writer.write(arg0, arg1, arg2);
        }

        public void startDocument() throws IOException {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        }

        public void endDocument() throws IOException {
            writer.close();
        }
    }
}
