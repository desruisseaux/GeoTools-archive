/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

import java.io.Reader;

public interface Unmarshaller {
    public Object unmarshal( java.io.File f ) throws JAXBException;
    public Object unmarshal( java.io.InputStream is ) throws JAXBException;
    public Object unmarshal( Reader reader ) throws JAXBException;
    public Object unmarshal( java.net.URL url ) throws JAXBException;
    public Object unmarshal( org.w3c.dom.Node node ) throws JAXBException;
}
