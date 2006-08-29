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
package org.geotools.gml3.bindings;

import org.geotools.xml.*;
import javax.xml.namespace.QName;


/**
 * Binding object for the type http://www.opengis.net/gml:AssociationType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="AssociationType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A pattern or base for derived types used to specify complex types corresponding to an  unspecified UML association - either composition or aggregation.  Restricts the cardinality of Objects contained in the association to a maximum of one.  An instance of this type can contain an element representing an Object, or serve as a pointer to a remote Object.
 *
 *  Descendents of this type can be restricted in an application schema to
 *  * allow only specified classes as valid participants in the aggregation
 *  * allow only association by reference (i.e. empty the content model) or by value (i.e. remove the xlinks).
 *
 *  When used for association by reference, the value of the gml:remoteSchema attribute can be used to locate a schema fragment that constrains the target instance.
 *
 *  In many cases it is desirable to impose the constraint prohibiting the occurence of both reference and value in the same instance, as that would be ambiguous.  This is accomplished by adding a directive in the annotation element of the element declaration.  This directive can be in the form of normative prose, or can use a Schematron pattern to automatically constrain co-occurrence - see the declaration for _strictAssociation below.
 *
 *  If co-occurence is not prohibited, then both a link and content may be present.  If this occurs in an instance, then the rule for interpretation is that the instance found by traversing the href provides the normative value of the property, and should be used when possible.  The value(s) included as content may be used if the remote instance cannot be resolved.  This may be considered to be a "cached" version of the value(s).&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;sequence minOccurs="0"&gt;
 *          &lt;element ref="gml:_Object"/&gt;
 *      &lt;/sequence&gt;
 *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
 *  &lt;/complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class AssociationTypeBinding extends AbstractComplexBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.ASSOCIATIONTYPE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return null;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        //TODO: implement
        return null;
    }
}
