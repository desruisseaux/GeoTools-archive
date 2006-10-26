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
package org.geotools.gml2.bindings;

import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.Iterator;
import javax.xml.namespace.QName;
import com.vividsolutions.jts.geom.Geometry;
import org.geotools.xml.*;
import org.geotools.xml.ComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;


/**
 * Binding object for the type http://www.opengis.net/gml:GeometryAssociationType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="GeometryAssociationType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;         An instance of this type (e.g. a
 *              geometryMember) can either          enclose or point to a
 *              primitive geometry element. When serving          as a
 *              simple link that references a remote geometry instance,
 *              the value of the gml:remoteSchema attribute can be used to
 *              locate a schema fragment that constrains the target
 *              instance.       &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;sequence minOccurs="0"&gt;
 *          &lt;element ref="gml:_Geometry"/&gt;
 *      &lt;/sequence&gt;optional
 *      &lt;!-- &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt; --&gt;
 *      &lt;attributeGroup ref="xlink:simpleLink"/&gt;
 *      &lt;attribute ref="gml:remoteSchema" use="optional"/&gt;
 *  &lt;/complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class GMLGeometryAssociationTypeBinding implements ComplexBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.GEOMETRYASSOCIATIONTYPE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public int getExecutionMode() {
        return AFTER;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return Geometry.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public void initialize(ElementInstance instance, Node node, MutablePicoContainer context) {
    }

    /**
     * <!-- begin-user-doc -->
     * Returns an object of type @link Geometry.
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        for (Iterator itr = node.getChildren().iterator(); itr.hasNext();) {
            Node cnode = (Node) itr.next();

            if (cnode.getValue() instanceof Geometry) {
                return cnode.getValue();
            }
        }

        //TODO: xlink and remoteSchema attributes, hard to do because of streaming
        //TODO: dont throw an exception here
        throw new RuntimeException("Could not find geometry in geometry association");
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public void encode(Object object, Element element, Document document) {
        //TODO: implement
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object getChild(Object object, QName name) {
        //TODO: implement
        return null;
    }
}
