/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

import org.w3c.dom.Node;

public abstract class Binder<XmlNode> {
    public abstract Object unmarshal( XmlNode xmlNode ) throws JAXBException;
    public abstract <T> JAXBElement<T> 
	unmarshal( XmlNode xmlNode, Class<T> declaredType ) 
	throws JAXBException;
    public abstract void marshal( Object jaxbObject, XmlNode xmlNode ) throws JAXBException;
    public abstract XmlNode getXMLNode( Object jaxbObject );
    public abstract Object getJAXBNode( XmlNode xmlNode );
    public abstract XmlNode updateXML( Object jaxbObject ) throws JAXBException;
    public abstract XmlNode updateXML( Object jaxbObject, XmlNode xmlNode ) throws JAXBException;
    public abstract Object updateJAXB( XmlNode xmlNode ) throws JAXBException;
}
