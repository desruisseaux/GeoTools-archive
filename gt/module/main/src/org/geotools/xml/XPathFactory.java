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

import org.geotools.catalog.MetadataXPath;
import org.opengis.catalog.MetadataEntity;
import org.w3c.dom.Node;
import java.util.List;

/**
 * A factory to simplify the use of XPaths
 * 
 * Currently each time a new XPath implementation is developed the createXPath(String, Class)
 * method must be modified to be able to create XPath objects of that type.
 *
 * @author Jesse Eichar
 * @version $Revision: 1.9 $
 */
public class XPathFactory {
    /**
     * <b>NOTE:<b>  This method is the only method that needs to be modified by developers
     *
     * @param xpath The string representation of the XPath 
     * @param root The class type of the root object.  This class is used to determine which type of 
     * XPath object should be instantiated
     *
     * @return XPath object that understands Nodes of type root and identifies nodes based on the xpath arguement.
     * Null if the type is not recognized or root=null
     */
    public static XPath createXPath(String xpath, Class root) {
        if (root == null) {
            return null;
        }

        if (Node.class.isAssignableFrom(root)) {
            return new DOMXPath(xpath);
        } //if		

        if (MetadataEntity.class.isAssignableFrom(root)) {
            return new MetadataXPath(xpath);
        } //if

        return null;
    }

    /**
     * Creates an XPath that can evaluate the root object.  
     *
     * @param xpath The string representation of the XPath 
     * @param root An instance of the class that the XPath will be used with.  This object is used to determine which type of 
     * XPath object should be instantiated
     *
     * @return XPath object that understands Nodes of type root and identifies nodes based on the xpath arguement.
     * Null if the type is not recognized or root=null
     */
    public static XPath createXPath(String xpath, Object root) {
        return createXPath(xpath, root.getClass());
    }

    /**
     * Returns a list of the nodes that are identified by this XPath expression.
     * The children of root are compared to the first term of the XPath expression
     *
     * @param xpath The string representation of the XPath 
     * @param root An instance of the class that the XPath will be used with.  This object is used to determine which type of 
     * XPath object should be instantiated
     *
     * @return a list of the nodes that are identified by this XPath expression.
     * The children of root are compared to the first term of the XPath expression
     * null if root is not a recognized type
    */
    public static List find(String xpath, Object root) {
        XPath xp = createXPath(xpath, root);
        if( xp == null ) return null;
        return xp.find(root);
    }

    /**
     * Returns a list of the values of the nodes that are identified by this XPath expression.
     * 
     * @param xpath The string representation of the XPath 
     * @param root An instance of the class that the XPath will be used with.  This object is used to determine which type of 
     * XPath object should be instantiated
     *
     * @return a list of the values of the nodes that are identified by this XPath expression.
     * null if root is not a recognized type
     */
    public static List value(String xpath, Object root) {
        XPath xp = createXPath(xpath, root);
        if( xp == null ) return null;

        return xp.value(root);
    }

    /**
     * Returns a list of the node paths for all the solutions to the XPath expression
     * In other words all the nodes from the root to the identified node are included in the nodepath
     *
     * @param xpath The string representation of the XPath 
     * @param root An instance of the class that the XPath will be used with.  This object is used to determine which type of 
     * XPath object should be instantiated
     *
     * @return a list of the node paths for all the solutions to the XPath expression
     * null if root is not a recognized type
    */
    public static List nodePaths(String xpath, Object root) {
        XPath xp = createXPath(xpath, root);
        if( xp == null ) return null;

        return xp.nodePaths(root);
    }

    /**
     * Returns a list of the nodes that are identified by this XPath expression.
     * Elements are compared to the first term of the XPath expression
     * 
     * @param xpath The string representation of the XPath 
     * @param elements The elements to be compared with the first term in the xpath
     * @param root The class that the XPath will be used with.  This class is used to determine which type of 
     * XPath object should be instantiated
     *
     * @return a list of the nodes that are identified by this XPath expression.
     * Elements are compared to the first term of the XPath expression
     * null if root is not a recognized type, or one of elements is not a LegalType. (@link org.geotools.xml.XPath#isLegalNode(Object)} 
     */
    public static List find(String xpath, List elements, Class root) {
        XPath xp = createXPath(xpath, root);

        return xp.find(elements);
    }

    /**
     * Returns a list of the values of the nodes that are identified by this XPath expression.
     *
     * @param xpath The string representation of the XPath 
     * @param elements The elements to be compared with the first term in the xpath
     * @param root The class that the XPath will be used with.  This class is used to determine which type of 
     * XPath object should be instantiated
     *
     * @return a list of the values of the nodes that are identified by this XPath expression.
     * Elements are compared to the first term of the XPath expression
     * null if root is not a recognized type, or one of elements is not a LegalType. (@link org.geotools.xml.XPath#isLegalNode(Object)} 
     */
    public static List value(String xpath, List elements, Class root) {
        XPath xp = createXPath(xpath, root);
        if( xp == null ) return null;

        return xp.value(elements);
    }

    /**
     * Returns a list of the node paths for all the solutions to the XPath expression
     * In other words all the nodes from the root to the identified node are included in the nodepath
     *
     * @param xpath The string representation of the XPath 
     * @param elements The elements to be compared with the first term in the xpath
     * @param root The class that the XPath will be used with.  This class is used to determine which type of 
     * XPath object should be instantiated
     *
     * @return a list of the node paths for all the solutions to the XPath expression
     * Elements are compared to the first term of the XPath expression
     * null if root is not a recognized type, or one of elements is not a LegalType. (@link org.geotools.xml.XPath#isLegalNode(Object)} 
     */
    public static List nodePaths(String xpath, List elements, Class root) {
        XPath xp = createXPath(xpath, root);
        if( xp == null ) return null;

        return xp.nodePaths(elements);
    }
}
