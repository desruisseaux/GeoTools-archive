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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.Name;
import org.geotools.gml2.Reference;
import org.geotools.gml3.GML;
import org.geotools.xlink.XLINK;
import org.geotools.xml.*;


/**
 * Binding object for the type http://www.opengis.net/gml:ReferenceType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="ReferenceType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;
 *              A pattern or base for derived types used to specify complex
 *              types corresponding to a UML aggregation association. An
 *              instance of this type serves as a pointer to a remote Object.
 *           &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
 *  &lt;/complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class ReferenceTypeBinding extends AbstractComplexBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.ReferenceType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return Reference.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        //TODO: implement and remove call to super
        return super.parse(instance, node, value);
    }

    public Object getProperty(Object object, QName name)
        throws Exception {
        if (object instanceof Reference) {
            Reference ref = (Reference) object;

            if (XLINK.HREF.equals(name)) {
                return ref.getXlink();
            }
        }

        return null;
    }

    public List getProperties(Object object) throws Exception {
        if ((object != null) && !(object instanceof Reference)) {
            //this means we have the actual object, not a reference

            //check for feature
            if (object instanceof SimpleFeature) {
                SimpleFeature feature = (SimpleFeature) object;
                Name typeName = feature.getType().getName();
                QName name = new QName(typeName.getNamespaceURI(), typeName.getLocalPart());

                List properties = new ArrayList();

                //return a comment which is hte xlink href
                properties.add(new Object[] { Encoder.COMMENT, "this is a comment" });

                //first return the feature 
                properties.add(new Object[] { name, feature });

                return properties;
            }
        }

        return null;
    }
}
