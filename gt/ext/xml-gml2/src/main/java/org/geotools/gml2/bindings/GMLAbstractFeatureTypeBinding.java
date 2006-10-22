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

import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDTypeDefinition;
import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.net.URI;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultFeatureTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.gml2.FeatureTypeCache;
import org.geotools.xml.*;
import org.geotools.xs.bindings.XS;


/**
 * Binding object for the type http://www.opengis.net/gml:AbstractFeatureType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="AbstractFeatureType" abstract="true"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;         An abstract feature provides a set of
 *              common properties. A concrete          feature type must
 *              derive from this type and specify additional
 *              properties in an application schema. A feature may
 *              optionally          possess an identifying attribute
 *              (&apos;fid&apos;).       &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;sequence&gt;
 *          &lt;element ref="gml:description" minOccurs="0"/&gt;
 *          &lt;element ref="gml:name" minOccurs="0"/&gt;
 *          &lt;element ref="gml:boundedBy" minOccurs="0"/&gt;
 *          &lt;!-- additional properties must be specified in an application schema --&gt;
 *      &lt;/sequence&gt;
 *      &lt;attribute name="fid" type="ID" use="optional"/&gt;
 *  &lt;/complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class GMLAbstractFeatureTypeBinding implements ComplexBinding {
    //JD: TODO: This should be part of the framework, make it part of the binding
    // class to publish the type of objects they create
    //JD: Why not just use Binding#getType() ?
    /** map of xsd type to java class **/
    private static Map typeMap = new HashMap();

    static {
        typeMap.put(XS.STRING, String.class);
        typeMap.put(XS.INT, Integer.class);
        typeMap.put(XS.INTEGER, Integer.class);
        typeMap.put(XS.DECIMAL, Double.class);
        typeMap.put(XS.DOUBLE, Double.class);
        typeMap.put(XS.FLOAT, Float.class);
        typeMap.put(XS.SHORT, Short.class);
        typeMap.put(XS.DATE, Calendar.class);
        typeMap.put(XS.DATETIME, Calendar.class);
        typeMap.put(XS.TIME, Calendar.class);
        typeMap.put(XS.BOOLEAN, Boolean.class);
        typeMap.put(XS.LONG, Long.class);

        typeMap.put(GML.POINTMEMBERTYPE, Point.class);
        typeMap.put(GML.POINTPROPERTYTYPE, Point.class);
        typeMap.put(GML.POINTTYPE, Point.class);
        typeMap.put(GML.LINESTRINGMEMBERTYPE, LineString.class);
        typeMap.put(GML.LINESTRINGPROPERTYTYPE, LineString.class);
        typeMap.put(GML.LINESTRINGTYPE, LineString.class);
        typeMap.put(GML.LINEARRINGMEMBERTYPE, LinearRing.class);
        typeMap.put(GML.LINEARRINGTYPE, LinearRing.class);
        typeMap.put(GML.POLYGONMEMBERTYPE, Polygon.class);
        typeMap.put(GML.POLYGONPROPERTYTYPE, Polygon.class);
        typeMap.put(GML.POLYGONTYPE, Polygon.class);

        typeMap.put(GML.MULTIGEOMETRYPROPERTYTYPE, GeometryCollection.class);
        typeMap.put(GML.MULTIPOINTPROPERTYTYPE, MultiPoint.class);
        typeMap.put(GML.MULTIPOINTTYPE, MultiPoint.class);
        typeMap.put(GML.MULTILINESTRINGPROPERTYTYPE, MultiLineString.class);
        typeMap.put(GML.MULTIPOLYGONPROPERTYTYPE, MultiPolygon.class);
        typeMap.put(GML.MULTIPOLYGONTYPE, MultiPolygon.class);
        typeMap.put(GML.FEATUREASSOCIATIONTYPE, Feature.class);
    }

    FeatureTypeCache ftCache;

    public GMLAbstractFeatureTypeBinding(FeatureTypeCache ftCache) {
        this.ftCache = ftCache;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return GML.ABSTRACTFEATURETYPE;
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
        return FeatureType.class;
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
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        //get the definition of the element
        XSDElementDeclaration decl = instance.getElementDeclaration();

        //look for a feature type in the cache
        FeatureType fType = ftCache.get(decl.getName());

        if (fType == null) {
            FeatureTypeBuilder ftBuilder = new DefaultFeatureTypeFactory();
            ftBuilder.setName(decl.getName());
            ftBuilder.setNamespace(new URI(decl.getTargetNamespace()));

            //build the feaure type by walking through the elements of the 
            // actual xml schema type
            List children = Schemas.getChildElementDeclarations(decl);

            for (Iterator itr = children.iterator(); itr.hasNext();) {
                XSDElementDeclaration property = (XSDElementDeclaration) itr.next();

                //ignore the attributes provided by gml, change this for new feature model
                if (GML.NAMESPACE.equals(property.getTargetNamespace())) {
                    if ("boundedBy".equals(property.getName())) {
                        continue;
                    }

                    if ("name".equals(property.getName())) {
                        continue;
                    }

                    if ("description".equals(property.getName())) {
                        continue;
                    }
                }

                XSDTypeDefinition type = property.getType();

                QName qName = new QName(type.getTargetNamespace(), type.getName());

                Class theClass = (Class) typeMap.get(qName);

                if (theClass == null) {
                    throw new RuntimeException("Could not find class for " + qName);
                }

                //call method with most parameter
                ftBuilder.addType(AttributeTypeFactory.newAttributeType(property.getName(), theClass));
            }

            fType = ftBuilder.getFeatureType();
            ftCache.put(fType);
        }

        String fid = (String) node.getAttributeValue("fid");

        //create the feature
        Feature f = fType.create(new Object[fType.getAttributeCount()], fid);

        for (Iterator itr = node.getChildren().iterator(); itr.hasNext();) {
            Node child = (Node) itr.next();
            f.setAttribute(child.getComponent().getName(), child.getValue());
        }

        return f;
    }
}
