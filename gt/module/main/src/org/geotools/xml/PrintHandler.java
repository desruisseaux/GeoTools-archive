
package org.geotools.xml;

import java.io.IOException;

import org.xml.sax.Attributes;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public interface PrintHandler {
    public void startElement(String namespaceURI, String localName, Attributes attributes) throws IOException ;
    public void element(String namespaceURI, String localName, Attributes attributes) throws IOException ;
    public void endElement(String namespaceURI, String localName) throws IOException ;
    public void characters(char[] arg0, int arg1, int arg2) throws IOException ;
    public void characters(String s) throws IOException ;
    public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws IOException;
    public void startDocument() throws IOException;
    public void endDocument() throws IOException ;
}
