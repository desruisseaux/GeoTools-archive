package org.geotools.xml.impl;

import java.util.Enumeration;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

import org.xml.sax.helpers.NamespaceSupport;

/**
 * NamespaceContext wrapper around namespace support.
 */
public class NamespaceSupportWrapper implements NamespaceContext {
    
	NamespaceSupport namespaceSupport;
	
	public NamespaceSupportWrapper( NamespaceSupport namesaceSupport ) {
		this.namespaceSupport = namesaceSupport;
	}
	
	public String getNamespaceURI(String prefix) {
        return namespaceSupport.getURI(prefix);
    }

    public String getPrefix(String namespaceURI) {
        return namespaceSupport.getPrefix(namespaceURI);
    }

    public Iterator getPrefixes(String namespaceURI) {
        final Enumeration e = namespaceSupport.getPrefixes(namespaceURI);

        return new Iterator() {
            public void remove() {
                throw new UnsupportedOperationException();
            }

            public boolean hasNext() {
                return e.hasMoreElements();
            }

            public Object next() {
                return e.nextElement();
            }
        };
    }
}