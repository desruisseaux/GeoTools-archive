/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.validation.Schema;
import java.io.File;

public interface Marshaller {
    public static final String JAXB_ENCODING = "jaxb.encoding";
    public static final String JAXB_FORMATTED_OUTPUT = "jaxb.formatted.output";
    public static final String JAXB_SCHEMA_LOCATION = "jaxb.schemaLocation";
    public static final String JAXB_NO_NAMESPACE_SCHEMA_LOCATION = "jaxb.noNamespaceSchemaLocation";
    public static final String JAXB_FRAGMENT = "jaxb.fragment";
    public void marshal( Object jaxbElement, javax.xml.transform.Result result ) throws JAXBException;
    public void marshal( Object jaxbElement, java.io.OutputStream os ) throws JAXBException;
    public void marshal( Object jaxbElement, File output ) throws JAXBException;
    public void marshal( Object jaxbElement, java.io.Writer writer ) throws JAXBException;
    public void marshal( Object jaxbElement, org.xml.sax.ContentHandler handler ) throws JAXBException;
    public void marshal( Object jaxbElement, org.w3c.dom.Node node ) throws JAXBException;
    public org.w3c.dom.Node getNode( java.lang.Object contentTree ) throws JAXBException;
    public void setAdapter( XmlAdapter adapter );
    public <A extends XmlAdapter> void setAdapter( Class<A> type, A adapter );
    public <A extends XmlAdapter> A getAdapter( Class<A> type );
    public void setSchema( Schema schema );
    public void setProperty( String name, Object value );
    public Schema getSchema();
    public static abstract class Listener {
        public void beforeMarshal(Object source) {}
        public void afterMarshal(Object source) {}
    }
    public void setListener(Listener listener);
    public Listener getListener();
}
