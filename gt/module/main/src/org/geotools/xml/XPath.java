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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;


/**
 * This class is a General Superclass for implementing XPath Expressions.
 *
 * A Subclass is required to implement getChildren, getNodeName and solve for
 * XPath to be able to locate elements based on xpath expressions.
 *
 * XPath expressions are W3C specification related to XML.  This implementation
 * supports a subset of W3C's XPATH specification.  A description of the support is
 * as follows:
 * <ul>
 * <li>Java's Regular expression's are used for matching nodes.  See java.regex.Pattern</li> 
 * <li>Each object in an object tree is a Node</li>
 * <li>A path in a object tree is syntactically similar to a unix directory path
 * <p>doc/child1/child2</p></li>
 * <li>Each node is matched by comparing a java regular expression to the name
 * of the node returned by the abstract method getNodeName</li>
 * <li>Does not support the statement such as ancestor</li>
 * <li>Does not support the written for of XPaths:  </code>child::name</code></li>
 * </ul>
 *
 * Some examples of legal and illegal XPath statements:
 * NOTE: For those reading the source code &#042=*
 *
 * <ul>
 * <li>doc/&#042/child2 -> matches node called child2 with a grandparent named doc </li>
 * <li>&#042/&#042/&#042 -> matches any node that is a great-grandchild</li>
 * <li>&#042&#042/child2 -> matches a grandchild whith the name child2
 * <p>NOTE: does not have the same semantic meaning as the first example</p></li>
 * <li>d&#042/c&#042 -> matchs the grandchild starting with a c which has a parent that
 * starts with a d</li>
 * </ul>
 *
 * NOTE: No multiple descendant XPaths are possible.  For example: you cannot make an
 * XPath expression that means: "Any descendant that has the name 'child'"
 *
 *
 * @see org.geotools.xml.DOMXPath
 *
 * @author Jesse Eichar
 * @version $Revision: 1.9 $
 */
public abstract class XPath {
    private Pattern[] terms;
    protected static final int OP_FIND = 0;
    protected static final int OP_VALUE = 1;
    protected static final int OP_NODEPATH = 2;

    /**
     * Splits the xpath string into terms, one for each node to match,
     * and transforms the strings into Regex Patterns.  The patterns are used
     * to match the node names
     *
     * @param xpath the String form of the XPath expression
     */
    protected XPath(String xpath) {
        String[] tmp = {  };

        if (xpath.indexOf('[') == -1) {
            tmp = xpath.split("/");
        }

        terms = new Pattern[tmp.length];

        for (int i = 0; i < tmp.length; i++) {
            terms[i] = Pattern.compile(tmp[i]);
        }
    }

    /**
     * Tests whether the object passed is a legal node element
     * Legal nodes may differ depending on the type of operation being performed.
     * 
     * @param o Object to test
     * @param operation one of the OP_XXX constants
     *
     * @return true if object o is a valid node object
     */
    protected abstract boolean isLegalNode(Object o, int operation, boolean isList);

    /**
     * This method is used to get all the child nodes of Object o.  It is up to
     * the implementor to decide what a child relationship is.  Normally a child
     * is an object referred to by the parent, but this is not a requirement.
     *
     *
     *
     * @param o The object that is considered to be the "Parent."
     * In the case of hierarchical datastructures o will often be a child of another object
     * previously passed to the getChildren method.
     *
     * @return An immutable iterator that cycles through the list of all the children of object o.
     *
     */
    protected abstract Iterator getChildren(Object o);

    /**
     * Returns the string which identifies the name.  Names do not need to have unique names
     *
     * @param o The object being matched to the current term.
     *
     * @return The name of the object o in string form.
     */
    protected abstract String getNodeName(Object o);

    /**
     * Given a list of node objects, a value is computed that is the semantic "value"
     * of the path.  It is up to the implementor to make solve return a meaningful value.
     *
     * The list is the path of nodes, in order, that matches the XPath expression.
     *
     * The first element of the list is the "root" object passed to the value method
     *
     * @param path The list is the path of nodes, in order, that matches the XPath expression.
     *The first element of the list is the "root" object passed to the value method
     *
     * @return The "value" represented by the input path.
     * For example the "value" of a DOM tree implementation would be the string value of the text node
     * child of the last node in the list.
     *
     * Some cases require that the path be followed and a value computed at each node.  Then the final value
     * would be returned.
     *
     * It is up to the implementor to make solve return a meaningful value.
     */
    protected abstract Object solve(List path);

    /**
     * Checks if the pattern term matches object o's name as returned by the getNodeName() method.
     *
     * @param term The pattern to be used to determine a match.
     * @param o The current node being inspected
     *
     * @return true if the pattern matches object o's name as returned by getNodeName()
     * false otherwise
     */
    protected boolean nodeMatch(Pattern term, Object o) {
        return term.matcher(getNodeName(o)).matches();
    }

    /**
     * Returns a list of the nodes that are identified by this XPath expression.
     *
     * If root is a valid object (accepts() returns true) then the object is used as the root and
     * its children are matched against the first term.
     *
     * If root is a List then the elements of root are matched against the first term.
     *
     * @param root A Node or a List of Nodes.
     * If root is a Node the children of root are matched against the first term of the XPath expression
     * If root is a list then the elements of root are matched against the first term of the XPath expression
     *
     * @return null is returned if root is not a list or a node ( accepts() returns false )
     * A list of Nodes that are identified by the XPath expression
     */
    public List find(Object root) {
        if (!accept(root, OP_FIND)) {
            return null;
        }

        return find(root, false);
    }

    /**
     * Returns a list of the values of the nodes that are identified by this XPath expression.
     * For each identified node the solve method is called on the list of nodes in the node path from
     * root to the Node.  Each value returned by solve is added to the list.
     *
     * value() is the same as:
     * List values;
     * List paths=nodePaths();
     * foreach |path| in paths
     * values.add(solve(path))
     *
     * If root is a valid object (accepts() returns true) then the object is used as the root and
     * its children are matched against the first term.
     *
     * If root is a List then the elements of root are matched against the first term.
     *
     * @param root A Node or a List of Nodes.
     * If root is a Node the children of root are matched against the first term of the XPath expression
     * If root is a list then the elements of root are matched against the first term of the XPath expression
     *
     * @return null is returned if root is not a list or a node ( accepts() returns false )
     * A list of the values of the identified nodes
     */
    public List value(Object root) {
        if (!accept(root, OP_VALUE)) {
            return null;
        }

        List result = new ArrayList();
        List paths = find(root, true);

        for (Iterator iter = paths.iterator(); iter.hasNext();) {
            List path = (List) iter.next();
            result.add(solve(path));
        }

        return result;
    }

    /**
     * Returns a list of the node paths for all the solutions to the XPath expression
     * In other words all the nodes from the root to the identified node are included in the nodepath
     *
     * If root is a valid object (accepts() returns true) then the object is used as the root and
     * its children are matched against the first term.
     *
     * If root is a List then the elements of root are matched against the first term.
     *
     * @param root A Node or a List of Nodes.
     * If root is a Node the children of root are matched against the first term of the XPath expression
     * If root is a list then the elements of root are matched against the first term of the XPath expression
     *
     * @return null is returned if root is not a list or a node ( accepts() returns false )
     * A list of the node paths for all the solutions to the XPath expression
     * In other words all the nodes from the root to the identified node are included in the nodepath
     */
    public List nodePaths(Object root) {
        if (!accept(root, OP_NODEPATH)) {
            return null;
        }

        return find(root, true);
    }

    /**
     * Returns all the java.regex.Pattern objects that are used for matching.
     *
     * @return all the java.regex.Pattern objects that are used for matching.
     */
    public Pattern[] getTerms() {
        return terms;
    }

    /**
     * Translates the XPath into string form
     *
     * @return XPath is string form
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < terms.length; i++) {
            buf.append(terms[i].pattern());

            if ((i + 1) < terms.length) {
                buf.append("/");
            }
        }

        return buf.toString();
    }

    private boolean accept(Object root, int operation) {
        if (root instanceof List) {
            //check that all elements in the list are legal nodes.
            for (Iterator iter = ((List) root).iterator(); iter.hasNext();) {
                Object element = (Object) iter.next();

                if (!isLegalNode(element, operation, true)) {
                    return false;
                }
            }

            // if root is not a list then check that it is a legal node
        } else if (!isLegalNode(root, operation, false)) {
            return false;
        }

        return true;
    }

    private List find(Object o, int index, List path) {
        Pattern term = terms[index];
        List result = new ArrayList();

        if (!nodeMatch(term, o)) {
            return null;
        } //if

        if (path != null) {
            path.add(o);
        }

        if (index == (terms.length - 1)) {
            if (path == null) {
                result.add(o);
            } else {
                result.add(new ArrayList(path));
            }

            return result;
        } //if

        Iterator children = getChildren(o);

        while (children.hasNext()) {
            Object child = children.next();
            List matches = find(child, index + 1, path);

            if (matches == null) {
                continue;
            }

            result.addAll(matches);

            if (path != null) {
                path.remove(path.size() - 1);
            }
        } //while

        return result;
    }

    private List find(Object o, boolean keepPath) {
        ArrayList result = new ArrayList();

        Iterator children;
        boolean add = true;

        if (o instanceof List) {
            children = ((List) o).iterator();
            add = false;
        } else {
            children = getChildren(o);
        }

        while (children.hasNext()) {
            ArrayList path = null;

            if (keepPath) {
                path = new ArrayList();

                if (add) {
                    path.add(o);
                }
            }

            Object child = children.next();
            List matches = find(child, 0, path);

            if (matches == null) {
                continue;
            }

            result.addAll(matches);
        } //while

        return result;
    }

    /**
     * A convenience class.  An iterator that always return false to hasNext()
     *
     * @author Jesse Eichar
     * @version $Revision: 1.9 $
     */
    public class NullIterator implements Iterator {
        /**
         * NullIterator never has a next() object
         *
         * @return null
         */
        public boolean hasNext() {
            return false;
        }

        /**
         * @return null
         */
        public Object next() {
            return null;
        }

        /**
         * Not meaningful
         */
        public void remove() {
        }
    }
}
