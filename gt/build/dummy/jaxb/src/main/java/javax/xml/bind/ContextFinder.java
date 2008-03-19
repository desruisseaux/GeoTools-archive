/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.AccessController;
import java.security.PrivilegedAction;

import static javax.xml.bind.JAXBContext.JAXB_CONTEXT_FACTORY;

class ContextFinder {

    static JAXBContext find(String factoryId, String contextPath, ClassLoader classLoader, Map properties ) throws JAXBException {
		return null;
    }

    static JAXBContext find( Class[] classes, Map properties ) throws JAXBException {
		return null;
    }
}
