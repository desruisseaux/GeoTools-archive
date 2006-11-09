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
package org.geotools.xs.bindings;

import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;


/**
 * Binding object for the type http://www.w3.org/2001/XMLSchema:anyType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xs:complexType name="anyType" mixed="true"&gt;
 *      &lt;xs:annotation&gt;
 *          &lt;xs:documentation&gt;    Not the real urType, but as close an
 *              approximation as we can    get in the XML representation&lt;/xs:documentation&gt;
 *      &lt;/xs:annotation&gt;
 *      &lt;xs:sequence&gt;
 *          &lt;xs:any minOccurs="0" maxOccurs="unbounded" processContents="lax"/&gt;
 *      &lt;/xs:sequence&gt;
 *      &lt;xs:anyAttribute processContents="lax"/&gt;
 *  &lt;/xs:complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class XSAnyTypeBinding extends AbstractComplexBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return XS.ANYTYPE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public int getExecutionMode() {
        return OVERRIDE;
    }

    /**
     * <!-- begin-user-doc -->
     * This binding returns objects of type Map
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return Map.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public void initialize(ElementInstance instance, Node node,
        MutablePicoContainer context) {
    }

    /**
     *
     * <!-- begin-user-doc -->
     * A quick approx of the available content:
     * <p>
     * This method returns a {@link Map} in which the names of children and
     * attributes are keys, and the parsed children and attributes are the
     * values. If the element being parsed contains child text, it is available
     * under the <code>nulll</code> key.
     *
     * @return Map,
     * </p>
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        Map map = new HashMap();
        List attributes = node.getAttributes();
        List children = node.getChildren();
        mapBinding(map, attributes);
        mapBinding(map, children);

        String string = null;

        if ((value != null) && value instanceof String) {
            string = (String) value;
        }

        if (string != null) {
            map.put(null, string);
        }

        return map;
    }

    private void mapBinding(Map map, List attributes) {
        for (Iterator i = attributes.iterator(); i.hasNext();) {
            Node attribute = (Node) i.next();
            String name = attribute.getComponent().getName();
            Object value = attribute.getValue();

            if (map.containsKey(name)) {
                List values;
                Object obj = map.get(name);

                if (obj instanceof List) {
                    values = (List) obj;
                } else {
                    values = new ArrayList();
                    values.add(obj);
                    map.put(name, values);
                }

                values.add(value);
            } else {
                map.put(name, value);
            }
        }
    }
}
