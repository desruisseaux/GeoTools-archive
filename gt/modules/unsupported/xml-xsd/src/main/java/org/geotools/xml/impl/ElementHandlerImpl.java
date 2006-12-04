/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.xml.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDFactory;
import org.eclipse.xsd.XSDSchemaContent;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.util.XSDUtil;
import org.geotools.xml.AttributeInstance;
import org.geotools.xml.Binding;
import org.geotools.xml.InstanceComponent;
import org.geotools.xml.Parser;
import org.geotools.xml.SchemaIndex;
import org.geotools.xml.Schemas;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


public class ElementHandlerImpl extends HandlerImpl implements ElementHandler {
    /** parent handler **/
    Handler parent;

    /** the element declaration **/
    XSDElementDeclaration content;

    /** the element instance **/
    ElementImpl element;

    /** the running parser */
    ParserHandler parser;

    /** the element type strategy **/
    Binding strategy;

    /** child handlers **/
    ArrayList childHandlers;

    /** parse tree for the element **/
    NodeImpl node;

    /** parsed value **/
    Object value;

    public ElementHandlerImpl(XSDElementDeclaration content, Handler parent,
        ParserHandler parser) {
        this.content = content;
        this.parent = parent;
        this.parser = parser;

        childHandlers = new ArrayList();
    }

    public void startElement(QName qName, Attributes attributes)
        throws SAXException {
        //clear handler list
        childHandlers.clear();

        //create the attributes
        List atts = new ArrayList();

        for (int i = 0; i < attributes.getLength(); i++) {
            String uri = attributes.getURI(i);
            String name = attributes.getLocalName(i);

//            if ((uri == null) || "".equals(uri)) {
//                uri = qName.getNamespaceURI(); //assume same as element
//            }

            QName attQName = new QName(uri, name);

            XSDAttributeDeclaration decl = Schemas.getAttributeDeclaration(content,
                    attQName);

            if ( decl == null ) {
            	//check wether unknown attributes should be parsed
            	if ( parent.getContext().getComponentInstance( Parser.Properties.PARSE_UNKNOWN_ATTRIBUTES ) != null ) {
            		//create a mock attribute and continue
            		decl = XSDFactory.eINSTANCE.createXSDAttributeDeclaration();
            		decl.setName( attQName.getLocalPart() );
            		decl.setTargetNamespace( attQName.getNamespaceURI() );
            		
            		//set the type to be of string
            		XSDSimpleTypeDefinition type = (XSDSimpleTypeDefinition) 
            			XSDUtil.getSchemaForSchema( XSDUtil.SCHEMA_FOR_SCHEMA_URI_2001 )
            			.getSimpleTypeIdMap().get( "string" );
            		
            		decl.setTypeDefinition( type );
            	}
            }
            
            //TODO: validate, if there is no declaration for an attribute, then 
            //TODO: make sure no required attributes are missing
            // validation should fail, this is being side stepped for now until
            // a good way of handling the namespace attributes on the root 
            // element, for now we just ignore attributes we dont find in the 
            // schema
            if (decl != null) {
                AttributeInstance att = new AttributeImpl(decl);
                att.setNamespace(decl.getTargetNamespace());
                att.setName(decl.getName());
                att.setText(attributes.getValue(i));

                atts.add(att);
            }
            else {
				parser.getLogger().warning("Could not find attribute declaration: " + attQName);
			}
        }

        //create the element
        element = new ElementImpl(content);
        element.setNamespace(qName.getNamespaceURI());
        element.setName(qName.getLocalPart());
        element.setAttributes((AttributeInstance[]) atts.toArray(
                new AttributeInstance[atts.size()]));

        //create the parse tree for the node
        node = new NodeImpl(element);

        //parse the attributes
        for (int i = 0; i < element.getAttributes().length; i++) {
            AttributeInstance attribute = element.getAttributes()[i];
            ParseExecutor executor = new ParseExecutor(attribute, null,
                    parent.getContext(), parser );
            BindingWalker walker = new BindingWalker(parser.getBindingLoader(), 
                    parent.getContext());
            walker.walk(attribute.getAttributeDeclaration(), executor);

            Object parsed = executor.getValue();
            node.addAttribute(new NodeImpl(attribute, parsed));
        }

        //create context for children 
        //TODO: this should only be done if the element is complex, this class
        // needs to be split into two, one for complex, other for simple
        setContext(new DefaultPicoContainer(parent.getContext()));
        
        //set the context on the binding factory
        ((BindingFactoryImpl)parser.getBindingFactory()).setContext( getContext() );
        ContextInitializer initer = new ContextInitializer(element, node,
                getContext());
        new BindingWalker( parser.getBindingLoader(), getContext()).walk(element
            .getElementDeclaration(), initer);
    }

    public void characters(char[] ch, int start, int length)
        throws SAXException {
        //simply add the text to the element
        element.addText(ch, start, length);
    }

    public void endElement( QName qName ) throws SAXException {
        //round up the children
        for (int i = 0; i < childHandlers.size(); i++) {
            Handler handler = (Handler) childHandlers.get(i);
            node.addChild(new NodeImpl(handler.getComponent(),
                    handler.getValue()));
        }

        //get the containing type
        XSDTypeDefinition container = null;
        if ( getParentHandler().getComponent() != null ) {
        	container = getParentHandler().getComponent().getTypeDefinition();
        }
        ParseExecutor executor = new ParseExecutor(element, node,
                getParentHandler().getContext(), parser );
        new BindingWalker( parser.getBindingLoader(), 
            getParentHandler().getContext()).walk(element.getElementDeclaration(),
            executor, container );

        //cache the parsed value
        value = executor.getValue();

        if (value == null) {
            //TODO: instead of crashing, just remove the element from 
            // the parent, or figure out if the element is 'optional' and 
            // remove
            String msg = "Parsing failed for " + element.getName()
                + ", no value returned from strategy";
            throw new RuntimeException(msg);
        }

        //kill the context
        parent.getContext().removeChildContainer(getContext());
    }

    private Handler getChildHandlerInternal(QName qName) {
        SchemaIndex index = parser.getSchemaIndex();

        XSDElementDeclaration element = Schemas.getChildElementDeclaration(content,
                qName);

        if (element != null) {
            //TODO: determine wether the element is complex or simple, and create
            ElementHandler handler = parser.getHandlerFactory()
                                           .createElementHandler(element, this, parser );

            return handler;
        }

        //use the schema builder to get the element declaration, then see if it 
        //group has a substiution
        //TODO: this is kind of a hack, this logic shouldn't really be here, 
        // clean this up
        element = index.getElementDeclaration(qName);

        if (element != null) {
            XSDElementDeclaration sub = element.getSubstitutionGroupAffiliation();

            if (sub != null) {
                QName subQName = new QName(sub.getTargetNamespace(),
                        sub.getName());
                Handler handler = getChildHandlerInternal(subQName);

                if (handler != null) {
                    //substituable
                    handler = parser.getHandlerFactory().createElementHandler( element, this, parser );

                    return handler;
                }
            }
        }

        return null;
    }
    
    public Handler getChildHandler(QName qName) {
        return getChildHandlerInternal(qName);
    }

    public List getChildHandlers() {
        return childHandlers;
    }

    public void addChildHandler(Handler child) {
    	childHandlers.add(child);
    }
    
    public void removeChildHandler(Handler child) {
        childHandlers.remove(child);
    }

    public Handler getParentHandler() {
        return parent;
    }

    public XSDSchemaContent getSchemaContent() {
        return content;
    }

    public XSDElementDeclaration getElementDeclaration() {
        return content;
    }

    public InstanceComponent getComponent() {
        return element;
    }

    public void setComponent(ElementImpl element) {
        this.element = element;
    }

    public Object getValue() {
        return value;
    }

    public String toString() {
        return (node != null) ? node.toString() : "";
    }
}
