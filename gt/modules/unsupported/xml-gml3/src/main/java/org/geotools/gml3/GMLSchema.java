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
package org.geotools.gml3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.geotools.feature.Name;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.feature.type.AttributeTypeImpl;
import org.geotools.feature.type.ComplexTypeImpl;
import org.geotools.feature.type.FeatureTypeImpl;
import org.geotools.feature.type.SchemaImpl;
import org.geotools.feature.type.TypeName;
import org.geotools.gml3.smil.SMIL20LANGSchema;
import org.geotools.gml3.smil.SMIL20Schema;
import org.geotools.xs.XSSchema;


public class GMLSchema extends SchemaImpl {
    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CartesianCSRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a Cartesian coordinate system, either referencing or containing the definition of that coordinate system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:CartesianCS"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CARTESIANCSREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CartesianCSRefType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractPositionalAccuracyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Position error estimate (or accuracy) data. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element minOccurs="0" ref="gml:measureDescription"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTPOSITIONALACCURACYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractPositionalAccuracyType"),
            Collections.EMPTY_LIST, false, true, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="RelativeInternalPositionalAccuracyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Closeness of the relative positions of two or more positions to their respective relative positions accepted as or being true. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractPositionalAccuracyType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:result"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType RELATIVEINTERNALPOSITIONALACCURACYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "RelativeInternalPositionalAccuracyType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET,
            ABSTRACTPOSITIONALACCURACYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="SurfacePropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A property that has a surface as its value domain can either be an appropriate geometry element encapsulated in an element of this type or an XLink reference to a remote geometry element (where remote includes geometry elements located elsewhere in the same document). Either the reference or the contained element must be given, but neither both nor none.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:_Surface"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"&gt;
     *          &lt;annotation&gt;
     *              &lt;documentation&gt;This attribute group includes the XLink attributes (see xlinks.xsd). XLink is used in GML to reference remote resources (including those elsewhere in the same document). A simple link element can be constructed by including a specific set of XLink attributes. The XML Linking Language (XLink) is currently a Proposed Recommendation of the World Wide Web Consortium. XLink allows elements to be inserted into XML documents so as to create sophisticated links between resources; such links can be used to reference remote properties.
     *  A simple link element can be used to implement pointer functionality, and this functionality has been built into various GML 3 elements by including the gml:AssociationAttributeGroup.&lt;/documentation&gt;
     *          &lt;/annotation&gt;
     *      &lt;/attributeGroup&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType SURFACEPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "SurfacePropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="ArcMinutesType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Integer number of arc-minutes in a degree-minute-second angular value.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="nonNegativeInteger"&gt;
     *          &lt;maxInclusive value="59"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType ARCMINUTESTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ArcMinutesType"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, XSSchema.NONNEGATIVEINTEGER_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimeCalendarPropertyType"&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:TimeCalendar"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMECALENDARPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeCalendarPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MetaDataPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Base type for complex metadata property types.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;any processContents="lax"/&gt;
     *          &lt;!-- &lt;element ref="gml:_MetaData"/&gt; --&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *      &lt;attribute name="about" type="anyURI" use="optional"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType METADATAPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MetaDataPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CodeType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Name or code with an (optional) authority.  Text token.
     *        If the codeSpace attribute is present, then its value should identify a dictionary, thesaurus
     *        or authority for the term, such as the organisation who assigned the value,
     *        or the dictionary from which it is taken.
     *        A text string with an optional codeSpace attribute. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="string"&gt;
     *              &lt;attribute name="codeSpace" type="anyURI" use="optional"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CODETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CodeType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, XSSchema.STRING_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="StringOrRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;This type is available wherever there is a need for a "text" type property. It is of string type, so the text can be included inline, but the value can also be referenced remotely via xlinks from the AssociationAttributeGroup. If the remote reference is present, then the value obtained by traversing the link should be used, and the string content of the element can be used for an annotation.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="string"&gt;
     *              &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType STRINGORREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "StringOrRefType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.STRING_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="BoundingShapeType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Bounding shape.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;choice&gt;
     *              &lt;element ref="gml:Envelope"/&gt;
     *              &lt;element ref="gml:Null"/&gt;
     *          &lt;/choice&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType BOUNDINGSHAPETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "BoundingShapeType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="LocationPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Convenience property for generalised location.
     *        A representative location for plotting or analysis.
     *        Often augmented by one or more additional geometry properties with more specific semantics.&lt;/documentation&gt;
     *          &lt;documentation&gt;Deprecated in GML 3.1.0&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;choice&gt;
     *              &lt;element ref="gml:_Geometry"/&gt;
     *              &lt;element ref="gml:LocationKeyWord"/&gt;
     *              &lt;element ref="gml:LocationString"/&gt;
     *              &lt;element ref="gml:Null"/&gt;
     *          &lt;/choice&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType LOCATIONPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "LocationPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractGMLType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;All complexContent GML elements are directly or indirectly derived from this abstract supertype
     *      to establish a hierarchy of GML types that may be distinguished from other XML types by their ancestry.
     *      Elements in this hierarchy may have an ID and are thus referenceable.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;group ref="gml:StandardObjectProperties"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attribute ref="gml:id" use="optional"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    private static List ABSTRACTGMLTYPE_TYPE_schema = new ArrayList();

    static {
        ABSTRACTGMLTYPE_TYPE_schema.add(new AttributeDescriptorImpl(METADATAPROPERTYTYPE_TYPE,
                new Name("http://www.opengis.net/gml", "metaDataProperty"), 0, 2147483647, false,
                null));
        ABSTRACTGMLTYPE_TYPE_schema.add(new AttributeDescriptorImpl(STRINGORREFTYPE_TYPE,
                new Name("http://www.opengis.net/gml", "description"), 0, 1, false, null));
        ABSTRACTGMLTYPE_TYPE_schema.add(new AttributeDescriptorImpl(CODETYPE_TYPE,
                new Name("http://www.opengis.net/gml", "name"), 0, 2147483647, false, null));
    }

    public static final ComplexType ABSTRACTGMLTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractGMLType"), ABSTRACTGMLTYPE_TYPE_schema,
            false, true, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractGeometryType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;All geometry elements are derived directly or indirectly from this abstract supertype. A geometry element may
     *                          have an identifying attribute ("gml:id"), a name (attribute "name") and a description (attribute "description"). It may be associated
     *                          with a spatial reference system (attribute "srsName"). The following rules shall be adhered: - Every geometry type shall derive
     *                          from this abstract type. - Every geometry element (i.e. an element of a geometry type) shall be directly or indirectly in the
     *                          substitution group of _Geometry.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGMLType"&gt;
     *              &lt;attribute name="gid" type="string" use="optional"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;This attribute is included for backward compatibility with GML 2 and is deprecated with GML 3.
     *                                                  This identifer is superceded by "gml:id" inherited from AbstractGMLType. The attribute "gid" should not be used
     *                                                  anymore and may be deleted in future versions of GML without further notice.&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *              &lt;attributeGroup ref="gml:SRSReferenceGroup"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType ABSTRACTGEOMETRYTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractGeometryType"), Geometry.class, false, true,
            Collections.EMPTY_SET, ABSTRACTGMLTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractGeometricAggregateType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;This is the abstract root type of the geometric aggregates.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGeometryType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTGEOMETRICAGGREGATETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractGeometricAggregateType"),
            Collections.EMPTY_LIST, false, true, Collections.EMPTY_SET, ABSTRACTGEOMETRYTYPE_TYPE,
            null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MultiPointType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A MultiPoint is defined by one or more Points, referenced through pointMember elements.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGeometricAggregateType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;The members of the geometric aggregate can be specified either using the "standard" property or the array property style. It is also valid to use both the "standard" and the array property style in the same collection.
     *  NOTE: Array properties cannot reference remote geometry elements.&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:pointMember"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:pointMembers"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType MULTIPOINTTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MultiPointType"), MultiPoint.class, false, false,
            Collections.EMPTY_SET, ABSTRACTGEOMETRICAGGREGATETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DomainSetType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;The spatiotemporal domain of a coverage.
     *    Typically
     *    * a geometry collection,
     *    * an implicit geometry (e.g. a grid),
     *    * an explicit or implicit collection of time instances or periods, or
     *
     *  N.B. Temporal geometric complexes and temporal grids are not yet implemented in GML.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;choice&gt;
     *              &lt;element ref="gml:_Geometry"/&gt;
     *              &lt;element ref="gml:_TimeObject"/&gt;
     *          &lt;/choice&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DOMAINSETTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DomainSetType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="RectifiedGridDomainType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:DomainSetType"&gt;
     *              &lt;sequence minOccurs="0"&gt;
     *                  &lt;element ref="gml:RectifiedGrid"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType RECTIFIEDGRIDDOMAINTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "RectifiedGridDomainType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, DOMAINSETTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TopoVolumePropertyType"&gt;
     *      &lt;sequence&gt;
     *          &lt;element ref="gml:TopoVolume"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TOPOVOLUMEPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TopoVolumePropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractTopologyType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGMLType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTTOPOLOGYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractTopologyType"), Collections.EMPTY_LIST,
            false, true, Collections.EMPTY_SET, ABSTRACTGMLTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TopoCurveType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;The end Node of each directedEdge of a TopoCurveType
     *  is the start Node of the next directedEdge of the TopoCurveType in document order.  The TopoCurve type and element represent a homogeneous topological expression, a list of directed edges, which if realised are isomorphic to a geometric curve primitive. The intended use of TopoCurve is to appear within a line feature instance to express the structural and geometric relationships of this line to other features via the shared edge definitions.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTopologyType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" ref="gml:directedEdge"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TOPOCURVETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TopoCurveType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTTOPOLOGYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="VerticalDatumTypeType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Type of a vertical datum. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;restriction base="gml:CodeType"&gt;
     *              &lt;attribute name="codeSpace" type="anyURI" use="required"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;Reference to a source of information specifying the values and meanings of all the allowed string values for this VerticalDatumTypeType. &lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *          &lt;/restriction&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType VERTICALDATUMTYPETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "VerticalDatumTypeType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, CODETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TopoPointPropertyType"&gt;
     *      &lt;sequence&gt;
     *          &lt;element ref="gml:TopoPoint"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TOPOPOINTPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TopoPointPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="NullEnumeration"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt; Some common reasons for a null value:
     *
     *          innapplicable - the object does not have a value
     *          missing - The correct value is not readily available to the sender of this data.
     *                             Furthermore, a correct value may not exist.
     *          template - the value will be available later
     *          unknown - The correct value is not known to, and not computable by, the sender of this data.
     *                             However, a correct value probably exists.
     *          withheld - the value is not divulged
     *
     *          other:reason - as indicated by "reason" string
     *
     *          Specific communities may agree to assign more strict semantics when these terms are used in a particular context.
     *        &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;union&gt;
     *          &lt;simpleType&gt;
     *              &lt;restriction base="string"&gt;
     *                  &lt;enumeration value="inapplicable"/&gt;
     *                  &lt;enumeration value="missing"/&gt;
     *                  &lt;enumeration value="template"/&gt;
     *                  &lt;enumeration value="unknown"/&gt;
     *                  &lt;enumeration value="withheld"/&gt;
     *              &lt;/restriction&gt;
     *          &lt;/simpleType&gt;
     *          &lt;simpleType&gt;
     *              &lt;restriction base="string"&gt;
     *                  &lt;pattern value="other:\w{2,}"/&gt;
     *              &lt;/restriction&gt;
     *          &lt;/simpleType&gt;
     *      &lt;/union&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType NULLENUMERATION_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "NullEnumeration"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, XSSchema.ANYSIMPLETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractSurfacePatchType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A surface patch defines a homogenuous portion of a surface.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTSURFACEPATCHTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractSurfacePatchType"), Collections.EMPTY_LIST,
            false, true, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="RectangleType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Represents a rectangle as a surface with an outer boundary consisting of a linear ring. Note that this is a polygon (subtype) with no inner boundaries. The number of points in the linear ring must be five.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractSurfacePatchType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:exterior"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Constraint: The Ring shall be a LinearRing and must form a rectangle; the first and the last position must be co-incident.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute fixed="planar" name="interpolation" type="gml:SurfaceInterpolationType"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;The attribute "interpolation" specifies the interpolation mechanism used for this surface patch. Currently only planar surface patches are defined in GML 3, the attribute is fixed to "planar", i.e. the interpolation method shall return points on a single plane. The boundary of the patch shall be contained within that plane.&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType RECTANGLETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "RectangleType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTSURFACEPATCHTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="BaseStyleDescriptorType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Base complex type for geometry, topology, label and graph styles.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGMLType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element minOccurs="0" name="spatialResolution" type="gml:ScaleType"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0"
     *                      name="styleVariation" type="gml:StyleVariationType"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="smil20:animate"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="smil20:animateMotion"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="smil20:animateColor"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="smil20:set"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType BASESTYLEDESCRIPTORTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "BaseStyleDescriptorType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTGMLTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TopologyStyleType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;[complexType of] The style descriptor for topologies of a feature. Describes individual topology elements styles.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:BaseStyleDescriptorType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;choice&gt;
     *                      &lt;element ref="gml:symbol"/&gt;
     *                      &lt;element name="style" type="string"&gt;
     *                          &lt;annotation&gt;
     *                              &lt;appinfo&gt;deprecated&lt;/appinfo&gt;
     *                              &lt;documentation&gt;Deprecated in GML version 3.1.0. Use symbol with inline content instead.&lt;/documentation&gt;
     *                          &lt;/annotation&gt;
     *                      &lt;/element&gt;
     *                  &lt;/choice&gt;
     *                  &lt;element minOccurs="0" ref="gml:labelStyle"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute name="topologyProperty" type="string"/&gt;
     *              &lt;attribute name="topologyType" type="string"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TOPOLOGYSTYLETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TopologyStyleType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, BASESTYLEDESCRIPTORTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MultiGeometryType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A geometry collection must include one or more geometries, referenced through geometryMember elements.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGeometricAggregateType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;The members of the geometric aggregate can be specified either using the "standard" property or the array property style. It is also valid to use both the "standard" and the array property style in the same collection.
     *  NOTE: Array properties cannot reference remote geometry elements.&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:geometryMember"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:geometryMembers"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType MULTIGEOMETRYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MultiGeometryType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTGEOMETRICAGGREGATETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="LinearCSRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a linear coordinate system, either referencing or containing the definition of that coordinate system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:LinearCS"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType LINEARCSREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "LinearCSRefType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimeEdgePropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A time edge property can either be any time edge element encapsulated in an element of this type
     *                          or an XLink reference to a remote time edge element (where remote includes elements located elsewhere in the same document).
     *                          Note that either the reference or the contained element must be given, but not both or none.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:TimeEdge"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMEEDGEPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeEdgePropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimePrimitivePropertyType"&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:_TimePrimitive"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMEPRIMITIVEPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimePrimitivePropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="RelatedTimeType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:TimePrimitivePropertyType"&gt;
     *              &lt;attribute name="relativePosition"&gt;
     *                  &lt;simpleType&gt;
     *                      &lt;restriction base="string"&gt;
     *                          &lt;enumeration value="Before"/&gt;
     *                          &lt;enumeration value="After"/&gt;
     *                          &lt;enumeration value="Begins"/&gt;
     *                          &lt;enumeration value="Ends"/&gt;
     *                          &lt;enumeration value="During"/&gt;
     *                          &lt;enumeration value="Equals"/&gt;
     *                          &lt;enumeration value="Contains"/&gt;
     *                          &lt;enumeration value="Overlaps"/&gt;
     *                          &lt;enumeration value="Meets"/&gt;
     *                          &lt;enumeration value="OverlappedBy"/&gt;
     *                          &lt;enumeration value="MetBy"/&gt;
     *                          &lt;enumeration value="BegunBy"/&gt;
     *                          &lt;enumeration value="EndedBy"/&gt;
     *                      &lt;/restriction&gt;
     *                  &lt;/simpleType&gt;
     *              &lt;/attribute&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType RELATEDTIMETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "RelatedTimeType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, TIMEPRIMITIVEPROPERTYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ValuePropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;GML property which refers to, or contains, a Value&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;group ref="gml:Value"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType VALUEPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ValuePropertyType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CategoryPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Property whose content is a Category.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:ValuePropertyType"&gt;
     *              &lt;sequence minOccurs="0"&gt;
     *                  &lt;element ref="gml:Category"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CATEGORYPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CategoryPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, VALUEPROPERTYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="FileValueModelType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;List of codes that identifies the file structure model for records stored in files.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="Record Interleaved"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType FILEVALUEMODELTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "FileValueModelType"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, XSSchema.STRING_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractTopoPrimitiveType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTopologyType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:isolated"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:container"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTTOPOPRIMITIVETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractTopoPrimitiveType"), Collections.EMPTY_LIST,
            false, true, Collections.EMPTY_SET, ABSTRACTTOPOLOGYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="EdgeType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;There is precisely one positively directed and one negatively directed node in the boundary of every edge. The negatively and positively directed nodes correspond to the start and end nodes respectively. The optional coboundary of an edge is a circular sequence of directed faces which are incident on this edge in document order. Faces which use a particular boundary edge in its positive orientation appear with positive orientation on the coboundary of the same edge. In the 2D case, the orientation of the face on the left of the edge is "+"; the orientation of the face on the right on its right is "-". An edge may optionally be realised by a 1-dimensional (curve) geometric primitive.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTopoPrimitiveType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="2" minOccurs="2" ref="gml:directedNode"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:directedFace"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:curveProperty"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType EDGETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "EdgeType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, ABSTRACTTOPOPRIMITIVETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractGeometricPrimitiveType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;This is the abstract root type of the geometric primitives. A geometric primitive is a geometric object that is not
     *                          decomposed further into other primitives in the system. All primitives are oriented in the direction implied by the sequence of their
     *                          coordinate tuples.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGeometryType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTGEOMETRICPRIMITIVETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractGeometricPrimitiveType"),
            Collections.EMPTY_LIST, false, true, Collections.EMPTY_SET, ABSTRACTGEOMETRYTYPE_TYPE,
            null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="AbstractSolidType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;An abstraction of a solid to support the different levels of complexity. A solid is always contiguous.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGeometricPrimitiveType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTSOLIDTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractSolidType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTGEOMETRICPRIMITIVETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CompositeSolidType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A composite solid is a geometry type with all the geometric properties of a (primitive) solid.
     *                                  Essentially, a composite solid is a collection of solids that join in pairs on common boundary surfaces and which, when considered as a whole, form a single solid.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractSolidType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" ref="gml:solidMember"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;appinfo&gt;
     *                              &lt;sch:pattern name="Check either href or content not both"&gt;
     *                                  &lt;sch:rule context="gml:solidMember"&gt;
     *                                      &lt;sch:extends rule="hrefOrContent"/&gt;
     *                                  &lt;/sch:rule&gt;
     *                              &lt;/sch:pattern&gt;
     *                          &lt;/appinfo&gt;
     *                          &lt;documentation&gt;This element references or contains one solid in the composite solid. The solids are contiguous.
     *  NOTE: This definition allows for a nested structure, i.e. a CompositeSolid may use, for example, another CompositeSolid as a member.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType COMPOSITESOLIDTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CompositeSolidType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTSOLIDTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DefinitionType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A definition, which can be included in or referenced by a dictionary. In this extended type, the inherited "description" optional element can hold the definition whenever only text is needed. The inherited "name" elements can provide one or more brief terms for which this is the definition. The inherited "metaDataProperty" elements can be used to reference or include more information about this definition.
     *  The gml:id attribute is required - it must be possible to reference this definition using this handle.  &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:AbstractGMLType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:metaDataProperty"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:description"/&gt;
     *                  &lt;element maxOccurs="unbounded" ref="gml:name"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute ref="gml:id" use="required"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DEFINITIONTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DefinitionType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTGMLTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractCoordinateOperationBaseType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Basic encoding for coordinate operation objects, simplifying and restricting the DefinitionType as needed. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:DefinitionType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:metaDataProperty"/&gt;
     *                  &lt;element ref="gml:coordinateOperationName"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute ref="gml:id" use="required"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTCOORDINATEOPERATIONBASETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractCoordinateOperationBaseType"),
            Collections.EMPTY_LIST, false, true, Collections.EMPTY_SET, DEFINITIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractCoordinateOperationType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A mathematical operation on coordinates that transforms or converts coordinates to another coordinate reference system. Many but not all coordinate operations (from CRS A to CRS B) also uniquely define the inverse operation (from CRS B to CRS A). In some cases, the operation method algorithm for the inverse operation is the same as for the forward algorithm, but the signs of some operation parameter values must be reversed. In other cases, different algorithms are required for the forward and inverse operations, but the same operation parameter values are used. If (some) entirely different parameter values are needed, a different coordinate operation shall be defined.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCoordinateOperationBaseType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:coordinateOperationID"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Set of alternative identifications of this coordinate operation. The first coordinateOperationID, if any, is normally the primary identification code, and any others are aliases. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element minOccurs="0" ref="gml:remarks"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Comments on or information about this coordinate operation, including source information. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element minOccurs="0" ref="gml:operationVersion"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:validArea"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:scope"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:_positionalAccuracy"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Unordered set of estimates of the impact of this coordinate operation on point position accuracy. Gives position error estimates for target coordinates of this coordinate operation, assuming no errors in source coordinates. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element minOccurs="0" ref="gml:sourceCRS"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:targetCRS"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTCOORDINATEOPERATIONTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractCoordinateOperationType"),
            Collections.EMPTY_LIST, false, true, Collections.EMPTY_SET,
            ABSTRACTCOORDINATEOPERATIONBASETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractGeneralTransformationType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;An abstract operation on coordinates that usually includes a change of Datum. The parameters of a coordinate transformation are empirically derived from data containing the coordinates of a series of points in both coordinate reference systems. This computational process is usually "over-determined", allowing derivation of error (or accuracy) estimates for the transformation. Also, the stochastic nature of the parameters may result in multiple (different) versions of the same coordinate transformation.
     *
     *  This abstract complexType is expected to be extended for well-known operation methods with many Transformation instances, in Application Schemas that define operation-method-specialized value element names and contents. This transformation uses an operation method with associated parameter values. However, operation methods and parameter values are directly associated with concrete subtypes, not with this abstract type. All concrete types derived from this type shall extend this type to include a "usesMethod" element that references one "OperationMethod" element. Similarly, all concrete types derived from this type shall extend this type to include one or more elements each named "uses...Value" that each use the type of an element substitutable for the "_generalParameterValue" element. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:AbstractCoordinateOperationType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:metaDataProperty"/&gt;
     *                  &lt;element ref="gml:coordinateOperationName"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:coordinateOperationID"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:remarks"/&gt;
     *                  &lt;element ref="gml:operationVersion"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:validArea"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:scope"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:_positionalAccuracy"/&gt;
     *                  &lt;element ref="gml:sourceCRS"/&gt;
     *                  &lt;element ref="gml:targetCRS"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute ref="gml:id" use="required"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTGENERALTRANSFORMATIONTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractGeneralTransformationType"),
            Collections.EMPTY_LIST, false, true, Collections.EMPTY_SET,
            ABSTRACTCOORDINATEOPERATIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TransformationType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A concrete operation on coordinates that usually includes a change of datum. The parameters of a coordinate transformation are empirically derived from data containing the coordinates of a series of points in both coordinate reference systems. This computational process is usually "over-determined", allowing derivation of error (or accuracy) estimates for the transformation. Also, the stochastic nature of the parameters may result in multiple (different) versions of the same coordinate transformation.
     *
     *  This concrete complexType can be used for all operation methods, without using an Application Schema that defines operation-method-specialized element names and contents, especially for methods with only one Transformation instance. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGeneralTransformationType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:usesMethod"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:usesValue"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Unordered set of composition associations to the set of parameter values used by this transformation operation. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TRANSFORMATIONTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TransformationType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTGENERALTRANSFORMATIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DirectedNodePropertyType"&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:Node"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attribute default="+" name="orientation" type="gml:SignType"/&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DIRECTEDNODEPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DirectedNodePropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="FormulaType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Paremeters of a simple formula by which a value using this unit of measure can be converted to the corresponding value using the preferred unit of measure. The formula element contains elements a, b, c and d, whose values use the XML Schema type "double". These values are used in the formula y = (a + bx) / (c + dx), where x is a value using this unit, and y is the corresponding value using the preferred unit. The elements a and d are optional, and if values are not provided, those parameters are considered to be zero. If values are not provided for both a and d, the formula is equivalent to a fraction with numerator and denominator parameters.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element minOccurs="0" name="a" type="double"/&gt;
     *          &lt;element name="b" type="double"/&gt;
     *          &lt;element name="c" type="double"/&gt;
     *          &lt;element minOccurs="0" name="d" type="double"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType FORMULATYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "FormulaType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractGeneralConversionType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;An abstract operation on coordinates that does not include any change of datum. The best-known example of a coordinate conversion is a map projection. The parameters describing coordinate conversions are defined rather than empirically derived. Note that some conversions have no parameters.
     *
     *  This abstract complexType is expected to be extended for well-known operation methods with many Conversion instances, in Application Schemas that define operation-method-specialized element names and contents. This conversion uses an operation method, usually with associated parameter values. However, operation methods and parameter values are directly associated with concrete subtypes, not with this abstract type. All concrete types derived from this type shall extend this type to include a "usesMethod" element that references the "OperationMethod" element. Similarly, all concrete types derived from this type shall extend this type to include zero or more elements each named "uses...Value" that each use the type of an element substitutable for the "_generalParameterValue" element. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:AbstractCoordinateOperationType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:metaDataProperty"/&gt;
     *                  &lt;element ref="gml:coordinateOperationName"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:coordinateOperationID"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:remarks"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:validArea"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:scope"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:_positionalAccuracy"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute ref="gml:id" use="required"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTGENERALCONVERSIONTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractGeneralConversionType"),
            Collections.EMPTY_LIST, false, true, Collections.EMPTY_SET,
            ABSTRACTCOORDINATEOPERATIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ConversionType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A concrete operation on coordinates that does not include any change of Datum. The best-known example of a coordinate conversion is a map projection. The parameters describing coordinate conversions are defined rather than empirically derived. Note that some conversions have no parameters.
     *
     *  This concrete complexType can be used with all operation methods, without using an Application Schema that defines operation-method-specialized element names and contents, especially for methods with only one Conversion instance. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGeneralConversionType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:usesMethod"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:usesValue"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Unordered list of composition associations to the set of parameter values used by this conversion operation. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CONVERSIONTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ConversionType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTGENERALCONVERSIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TopoSurfacePropertyType"&gt;
     *      &lt;sequence&gt;
     *          &lt;element ref="gml:TopoSurface"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TOPOSURFACEPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TopoSurfacePropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractCurveType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;An abstraction of a curve to support the different levels of complexity. The curve can always be viewed as a geometric
     *                          primitive, i.e. is continuous.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGeometricPrimitiveType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTCURVETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractCurveType"), Collections.EMPTY_LIST, false,
            true, Collections.EMPTY_SET, ABSTRACTGEOMETRICPRIMITIVETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CurveType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Curve is a 1-dimensional primitive. Curves are continuous, connected, and have a measurable length in terms of the coordinate system.
     *                                  A curve is composed of one or more curve segments. Each curve segment within a curve may be defined using a different interpolation method. The curve segments are connected to one another, with the end point of each segment except the last being the start point of the next segment in the segment list.
     *                                  The orientation of the curve is positive.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCurveType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:segments"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;This element encapsulates the segments of the curve.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType CURVETYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CurveType"), MultiLineString.class, false, false,
            Collections.EMPTY_SET, ABSTRACTCURVETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="AffinePlacementType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A placement takes a standard geometric
     *     construction and places it in geographic space. It defines a
     *     transformation from a constructive parameter space to the
     *     co-ordinate space of the co-ordinate reference system being used.
     *     Parameter spaces in formulae in this International Standard are
     *     given as (u, v) in 2D and(u, v, w) in 3D. Co-ordinate reference
     *     systems positions are given in formulae, in this International
     *     Standard, by either (x, y) in 2D, or (x, y, z) in 3D.
     *
     *     Affine placements are defined by linear transformations from
     *     parameter space to the target co-ordiante space. 2-dimensional
     *     Cartesian parameter space,(u,v) transforms into 3-dimensional co-
     *     ordinate reference systems,(x,y,z) by using an affine
     *     transformation,(u,v)-&gt;(x,y,z) which is defined :
     *
     *          x        ux vx          x0
     *                           u
     *          y =        uy vy   + y0
     *                           v
     *          x        uz vz        z0
     *
     *     Then, given this equation, the location element of the
     *     AffinePlacement is the direct position (x0, y0, z0), which is the
     *     target position of the origin in (u, v). The two reference
     *     directions (ux, uy, uz) and (vx, vy, vz) are the target
     *     directions of the unit vectors at the origin in (u, v).&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element name="location" type="gml:DirectPositionType"&gt;
     *              &lt;annotation&gt;
     *                  &lt;documentation&gt;The location property gives
     *       the target of the parameter space origin. This is the vector
     *      (x0, y0, z0) in the formulae above.&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *          &lt;element maxOccurs="unbounded" name="refDirection" type="gml:VectorType"&gt;
     *              &lt;annotation&gt;
     *                  &lt;documentation&gt;The attribute refDirection gives the
     *  target directions for the co-ordinate basis vectors of the
     *  parameter space. These are the columns of the matrix in the
     *  formulae given above. The number of directions given shall be
     *  inDimension. The dimension of the directions shall be
     *  outDimension.&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *          &lt;element name="inDimension" type="positiveInteger"&gt;
     *              &lt;annotation&gt;
     *                  &lt;documentation&gt;Dimension of the constructive parameter
     *       space.&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *          &lt;element name="outDimension" type="positiveInteger"&gt;
     *              &lt;annotation&gt;
     *                  &lt;documentation&gt;Dimension of the co-ordinate space.&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType AFFINEPLACEMENTTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AffinePlacementType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GeometryPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A geometric property can either be any geometry element encapsulated in an element of this type or an XLink reference
     *                          to a remote geometry element (where remote includes geometry elements located elsewhere in the same document). Note that either
     *                          the reference or the contained element must be given, but not both or none.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:_Geometry"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"&gt;
     *          &lt;annotation&gt;
     *              &lt;documentation&gt;This attribute group includes the XLink attributes (see xlinks.xsd). XLink is used in GML to reference
     *                                  remote resources (including those elsewhere in the same document). A simple link element can be constructed by
     *                                  including a specific set of XLink attributes. The XML Linking Language (XLink) is currently a Proposed Recommendation
     *                                  of the World Wide Web Consortium. XLink allows elements to be inserted into XML documents so as to create
     *                                  sophisticated links between resources; such links can be used to reference remote properties. A simple link element
     *                                  can be used to implement pointer functionality, and this functionality has been built into various GML 3 elements by
     *                                  including the gml:AssociationAttributeGroup.&lt;/documentation&gt;
     *          &lt;/annotation&gt;
     *      &lt;/attributeGroup&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType GEOMETRYPROPERTYTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GeometryPropertyType"), Geometry.class, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MeasureType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Number with a scale.
     *        The value of uom (Units Of Measure) attribute is a reference to a Reference System for the amount, either a ratio or position scale. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="double"&gt;
     *              &lt;attribute name="uom" type="anyURI" use="required"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType MEASURETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MeasureType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, XSSchema.DOUBLE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="SpeedType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Value of a speed, with its units. Uses the MeasureType with the restriction that the unit of measure referenced by uom must be suitable for a velocity, such as metres per second or miles per hour.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;restriction base="gml:MeasureType"/&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType SPEEDTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "SpeedType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, MEASURETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="EllipsoidBaseType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Basic encoding for ellipsoid objects, simplifying and restricting the DefinitionType as needed. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:DefinitionType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:metaDataProperty"/&gt;
     *                  &lt;element ref="gml:ellipsoidName"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute ref="gml:id" use="required"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ELLIPSOIDBASETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "EllipsoidBaseType"), Collections.EMPTY_LIST, false,
            true, Collections.EMPTY_SET, DEFINITIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="EllipsoidType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;An ellipsoid is a geometric figure that can be used to describe the approximate shape of the earth. In mathematical terms, it is a surface formed by the rotation of an ellipse about its minor axis.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:EllipsoidBaseType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:ellipsoidID"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Set of alternative identifications of this ellipsoid. The first ellipsoidID, if any, is normally the primary identification code, and any others are aliases. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element minOccurs="0" ref="gml:remarks"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Comments on or information about this ellipsoid, including source information. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element ref="gml:semiMajorAxis"/&gt;
     *                  &lt;element ref="gml:secondDefiningParameter"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ELLIPSOIDTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "EllipsoidType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ELLIPSOIDBASETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="UserDefinedCSRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a user-defined coordinate system, either referencing or containing the definition of that coordinate system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:UserDefinedCS"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType USERDEFINEDCSREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "UserDefinedCSRefType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CoordinateReferenceSystemRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a coordinate reference system, either referencing or containing the definition of that reference system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:_CoordinateReferenceSystem"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType COORDINATEREFERENCESYSTEMREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CoordinateReferenceSystemRefType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="OrientableCurveType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;OrientableCurve consists of a curve and an orientation. If the orientation is "+", then the OrientableCurve is identical to the baseCurve. If the orientation is "-", then the OrientableCurve is related to another _Curve with a parameterization that reverses the sense of the curve traversal.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCurveType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:baseCurve"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;References or contains the base curve (positive orientation).
     *  NOTE: This definition allows for a nested structure, i.e. an OrientableCurve may use another OrientableCurve as its base curve.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute default="+" name="orientation" type="gml:SignType"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;If the orientation is "+", then the OrientableCurve is identical to the baseCurve. If the orientation is "-", then the OrientableCurve is related to another _Curve with a parameterization that reverses the sense of the curve traversal. "+" is the default value.&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ORIENTABLECURVETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "OrientableCurveType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTCURVETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="booleanOrNull"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Union of the XML Schema boolean type and the GML Nulltype.  An element which uses this type may have content which is either a boolean {0,1,true,false} or a value from Nulltype&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;union memberTypes="gml:NullEnumeration boolean anyURI"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType BOOLEANORNULL_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "booleanOrNull"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, XSSchema.ANYSIMPLETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GeodeticDatumRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a geodetic datum, either referencing or containing the definition of that datum. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:GeodeticDatum"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GEODETICDATUMREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GeodeticDatumRefType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GeneralConversionRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a general conversion, either referencing or containing the definition of that conversion. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:_GeneralConversion"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GENERALCONVERSIONREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GeneralConversionRefType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="CurveInterpolationType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;CurveInterpolationType is a list of codes that may be used to identify the interpolation mechanisms specified by an
     *  application schema.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="linear"/&gt;
     *          &lt;enumeration value="geodesic"/&gt;
     *          &lt;enumeration value="circularArc3Points"/&gt;
     *          &lt;enumeration value="circularArc2PointWithBulge"/&gt;
     *          &lt;enumeration value="circularArcCenterPointWithRadius"/&gt;
     *          &lt;enumeration value="elliptical"/&gt;
     *          &lt;enumeration value="clothoid"/&gt;
     *          &lt;enumeration value="conic"/&gt;
     *          &lt;enumeration value="polynomialSpline"/&gt;
     *          &lt;enumeration value="cubicSpline"/&gt;
     *          &lt;enumeration value="rationalSpline"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType CURVEINTERPOLATIONTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CurveInterpolationType"), java.lang.Object.class,
            false, false, Collections.EMPTY_SET, XSSchema.STRING_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="LineStringSegmentArrayPropertyType"&gt;
     *      &lt;sequence&gt;
     *          &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:LineStringSegment"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType LINESTRINGSEGMENTARRAYPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "LineStringSegmentArrayPropertyType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="AbstractParametricCurveSurfaceType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation/&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractSurfacePatchType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTPARAMETRICCURVESURFACETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractParametricCurveSurfaceType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET,
            ABSTRACTSURFACEPATCHTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="AbstractGriddedSurfaceType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A gridded surface is a parametric curve
     *     surface derived from a rectangular grid in the parameter
     *     space. The rows from this grid are control points for
     *     horizontal surface curves; the columns are control points
     *     for vertical surface curves. The working assumption is that
     *     for a pair of parametric co-ordinates (s, t) that the
     *     horizontal curves for each integer offset are calculated
     *     and evaluated at "s". The defines a sequence of control
     *     points:
     *
     *     cn(s) : s  1 .....columns
     *
     *     From this sequence a vertical curve is calculated for "s",
     *     and evaluated at "t". In most cases, the order of
     *     calculation (horizontal-vertical vs. vertical-horizontal)
     *     does not make a difference. Where it does, the horizontal-
     *     vertical order shall be the one used.
     *
     *     Logically, any pair of curve interpolation types can lead
     *     to a subtype of GriddedSurface. The following clauses
     *     define some most commonly encountered surfaces that can
     *     be represented in this manner.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractParametricCurveSurfaceType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;group ref="gml:PointGrid"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;This is the double indexed sequence
     *         of control points, given in row major form.
     *         NOTE! There in no assumption made about the shape
     *         of the grid.
     *         For example, the positions need not effect a "21/2D"
     *         surface, consecutive points may be equal in any or all
     *         of the ordinates. Further, the curves in either or both
     *         directions may close.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/group&gt;
     *                  &lt;element minOccurs="0" name="rows" type="integer"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;The attribute rows gives the number
     *           of rows in the parameter grid.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element minOccurs="0" name="columns" type="integer"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;The attribute columns gives the number
     *          of columns in the parameter grid.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTGRIDDEDSURFACETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractGriddedSurfaceType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET,
            ABSTRACTPARAMETRICCURVESURFACETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="SphereType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A sphere is a gridded surface given as a
     *     family of circles whose positions vary linearly along the
     *     axis of the sphere, and whise radius varies in proportions to
     *     the cosine function of the central angle. The horizontal
     *     circles resemble lines of constant latitude, and the vertical
     *     arcs resemble lines of constant longitude.
     *     NOTE! If the control points are sorted in terms of increasing
     *     longitude, and increasing latitude, the upNormal of a sphere
     *     is the outward normal.
     *     EXAMPLE If we take a gridded set of latitudes and longitudes
     *     in degrees,(u,v) such as
     *
     *          (-90,-180)  (-90,-90)  (-90,0)  (-90,  90) (-90, 180)
     *          (-45,-180)  (-45,-90)  (-45,0)  (-45,  90) (-45, 180)
     *          (  0,-180)  (  0,-90)  (  0,0)  (  0,  90) (  0, 180)
     *          ( 45,-180)  ( 45,-90)  ( 45,0)  ( 45, -90) ( 45, 180)
     *          ( 90,-180)  ( 90,-90)  ( 90,0)  ( 90, -90) ( 90, 180)
     *
     *     And map these points to 3D using the usual equations (where R
     *     is the radius of the required sphere).
     *
     *      z = R sin u
     *      x = (R cos u)(sin v)
     *      y = (R cos u)(cos v)
     *
     *     We have a sphere of Radius R, centred at (0,0), as a gridded
     *     surface. Notice that the entire first row and the entire last
     *     row of the control points map to a single point in each 3D
     *     Euclidean space, North and South poles respectively, and that
     *     each horizontal curve closes back on itself forming a
     *     geometric cycle. This gives us a metrically bounded (of finite
     *     size), topologically unbounded (not having a boundary, a
     *     cycle) surface.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGriddedSurfaceType"&gt;
     *              &lt;attribute fixed="circularArc3Points"
     *                  name="horizontalCurveType" type="gml:CurveInterpolationType"/&gt;
     *              &lt;attribute fixed="circularArc3Points"
     *                  name="verticalCurveType" type="gml:CurveInterpolationType"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType SPHERETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "SphereType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, ABSTRACTGRIDDEDSURFACETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="RingPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Encapsulates a ring to represent properties in features or geometry collections.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element ref="gml:Ring"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType RINGPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "RingPropertyType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="OperationRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to an abstract operation, either referencing or containing the definition of that operation. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:_Operation"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType OPERATIONREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "OperationRefType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="EllipsoidalCSRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to an ellipsoidal coordinate system, either referencing or containing the definition of that coordinate system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:EllipsoidalCS"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ELLIPSOIDALCSREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "EllipsoidalCSRefType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="integerList"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;XML List based on XML Schema integer type.  An element of this type contains a space-separated list of integer values&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;list itemType="integer"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType INTEGERLIST_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "integerList"), java.lang.Object.class, false, false,
            Collections.EMPTY_SET, XSSchema.ANYSIMPLETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractRingType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;An abstraction of a ring to support surface boundaries of different complexity.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGeometryType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTRINGTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractRingType"), Collections.EMPTY_LIST, false,
            true, Collections.EMPTY_SET, ABSTRACTGEOMETRYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="LinearRingType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A LinearRing is defined by four or more coordinate tuples, with linear interpolation between them; the first and last coordinates must be coincident.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractRingType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;choice&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;GML supports two different ways to specify the control points of a linear ring.
     *  1. A sequence of "pos" (DirectPositionType) or "pointProperty" (PointPropertyType) elements. "pos" elements are control points that are only part of this ring, "pointProperty" elements contain a point that may be referenced from other geometry elements or reference another point defined outside of this ring (reuse of existing points).
     *  2. The "posList" element allows for a compact way to specifiy the coordinates of the control points, if all control points are in the same coordinate reference systems and belong to this ring only. The number of direct positions in the list must be at least four.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                      &lt;choice maxOccurs="unbounded" minOccurs="4"&gt;
     *                          &lt;element ref="gml:pos"/&gt;
     *                          &lt;element ref="gml:pointProperty"/&gt;
     *                          &lt;element ref="gml:pointRep"&gt;
     *                              &lt;annotation&gt;
     *                                  &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "pointProperty" instead. Included for backwards compatibility with GML 3.0.0.&lt;/documentation&gt;
     *                              &lt;/annotation&gt;
     *                          &lt;/element&gt;
     *                      &lt;/choice&gt;
     *                      &lt;element ref="gml:posList"/&gt;
     *                      &lt;element ref="gml:coordinates"&gt;
     *                          &lt;annotation&gt;
     *                              &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "posList" instead.&lt;/documentation&gt;
     *                          &lt;/annotation&gt;
     *                      &lt;/element&gt;
     *                      &lt;element maxOccurs="unbounded" minOccurs="4" ref="gml:coord"&gt;
     *                          &lt;annotation&gt;
     *                              &lt;documentation&gt;Deprecated with GML version 3.0 and included for backwards compatibility with GML 2. Use "pos" elements instead.&lt;/documentation&gt;
     *                          &lt;/annotation&gt;
     *                      &lt;/element&gt;
     *                  &lt;/choice&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType LINEARRINGTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "LinearRingType"), LinearRing.class, false, false,
            Collections.EMPTY_SET, ABSTRACTRINGTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="TimePositionUnion"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;The ISO 19108:2002 hierarchy of subtypes for temporal position are collapsed
     *        by defining a union of XML Schema simple types for indicating temporal position relative
     *        to a specific reference system.
     *
     *        Dates and dateTime may be indicated with varying degrees of precision.
     *        dateTime by itself does not allow right-truncation, except for fractions of seconds.
     *        When used with non-Gregorian calendars based on years, months, days,
     *        the same lexical representation should still be used, with leading zeros added if the
     *        year value would otherwise have fewer than four digits.
     *
     *        An ordinal position may be referenced via URI identifying the definition of an ordinal era.
     *
     *        A time coordinate value is indicated as a decimal (e.g. UNIX time, GPS calendar).&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;union memberTypes="gml:CalDate time dateTime anyURI decimal"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType TIMEPOSITIONUNION_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimePositionUnion"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, XSSchema.ANYSIMPLETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType final="#all" name="TimePositionType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;Direct representation of a temporal position.
     *        Indeterminate time values are also allowed, as described in ISO 19108. The indeterminatePosition
     *        attribute can be used alone or it can qualify a specific value for temporal position (e.g. before
     *        2002-12, after 1019624400).
     *        For time values that identify position within a calendar, the calendarEraName attribute provides
     *        the name of the calendar era to which the date is referenced (e.g. the Meiji era of the Japanese calendar).&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:TimePositionUnion"&gt;
     *              &lt;attribute default="#ISO-8601" name="frame" type="anyURI" use="optional"/&gt;
     *              &lt;attribute name="calendarEraName" type="string" use="optional"/&gt;
     *              &lt;attribute name="indeterminatePosition"
     *                  type="gml:TimeIndeterminateValueType" use="optional"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMEPOSITIONTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimePositionType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, TIMEPOSITIONUNION_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CompositeCurvePropertyType"&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:CompositeCurve"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType COMPOSITECURVEPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CompositeCurvePropertyType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DirectedTopoSolidPropertyType"&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:TopoSolid"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attribute default="+" name="orientation" type="gml:SignType"/&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DIRECTEDTOPOSOLIDPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DirectedTopoSolidPropertyType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="SignType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Utility type used in various places
     *        - e.g. to indicate the direction of topological objects;
     *        "+" for forwards, or "-" for backwards.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="-"/&gt;
     *          &lt;enumeration value="+"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType SIGNTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "SignType"), java.lang.Object.class, false, false,
            Collections.EMPTY_SET, XSSchema.STRING_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="SymbolType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;[complexType of] The symbol property. Allows for remote referencing of symbols.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;any maxOccurs="unbounded" minOccurs="0" processContents="skip"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attribute name="symbolType" type="gml:SymbolTypeEnumeration" use="required"/&gt;
     *      &lt;attribute ref="gml:transform" use="optional"/&gt;
     *      &lt;attribute name="about" type="anyURI" use="optional"/&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType SYMBOLTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "SymbolType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TemporalCRSRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a temporal coordinate reference system, either referencing or containing the definition of that reference system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:TemporalCRS"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TEMPORALCRSREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TemporalCRSRefType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MultiCurveDomainType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:DomainSetType"&gt;
     *              &lt;sequence minOccurs="0"&gt;
     *                  &lt;element ref="gml:MultiCurve"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType MULTICURVEDOMAINTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MultiCurveDomainType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, DOMAINSETTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractStyleType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;[complexType of] The value of the top-level property. It is an abstract element. Used as the head element of the substitution group for extensibility purposes.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGMLType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTSTYLETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractStyleType"), Collections.EMPTY_LIST, false,
            true, Collections.EMPTY_SET, ABSTRACTGMLTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="StyleType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;[complexType of] Predefined concrete value of the top-level property. Encapsulates all other styling information.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractStyleType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" ref="gml:featureStyle"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:graphStyle"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType STYLETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "StyleType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, ABSTRACTSTYLETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="AbstractGeneralOperationParameterRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to an operation parameter or group, either referencing or containing the definition of that parameter or group. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:_GeneralOperationParameter"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTGENERALOPERATIONPARAMETERREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractGeneralOperationParameterRefType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TransformationRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a transformation, either referencing or containing the definition of that transformation. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:Transformation"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TRANSFORMATIONREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TransformationRefType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractTimeObjectType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;The abstract supertype for temporal objects.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGMLType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTTIMEOBJECTTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractTimeObjectType"), Collections.EMPTY_LIST,
            false, true, Collections.EMPTY_SET, ABSTRACTGMLTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractTimePrimitiveType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;The abstract supertype for temporal primitives.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTimeObjectType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0"
     *                      name="relatedTime" type="gml:RelatedTimeType"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTTIMEPRIMITIVETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractTimePrimitiveType"), Collections.EMPTY_LIST,
            false, true, Collections.EMPTY_SET, ABSTRACTTIMEOBJECTTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractTimeTopologyPrimitiveType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;The element "complex" carries a reference to the complex containing this primitive.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTimePrimitiveType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element minOccurs="0" name="complex" type="gml:ReferenceType"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTTIMETOPOLOGYPRIMITIVETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractTimeTopologyPrimitiveType"),
            Collections.EMPTY_LIST, false, true, Collections.EMPTY_SET,
            ABSTRACTTIMEPRIMITIVETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimeEdgeType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;Type declaration of the element "TimeEdge".&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTimeTopologyPrimitiveType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element name="start" type="gml:TimeNodePropertyType"/&gt;
     *                  &lt;element name="end" type="gml:TimeNodePropertyType"/&gt;
     *                  &lt;element minOccurs="0" name="extent" type="gml:TimePeriodPropertyType"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMEEDGETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeEdgeType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTTIMETOPOLOGYPRIMITIVETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="GraphTypeType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Graph-specific styling property.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="TREE"/&gt;
     *          &lt;enumeration value="BICONNECTED"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType GRAPHTYPETYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GraphTypeType"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, XSSchema.STRING_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="SphericalCSRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a spherical coordinate system, either referencing or containing the definition of that coordinate system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:SphericalCS"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType SPHERICALCSREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "SphericalCSRefType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GridFunctionType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Defines how values in the domain are mapped to the range set. The start point and the sequencing rule are specified here.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element minOccurs="0" name="sequenceRule" type="gml:SequenceRuleType"&gt;
     *              &lt;annotation&gt;
     *                  &lt;documentation&gt;If absent, the implied value is "Linear".&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *          &lt;element minOccurs="0" name="startPoint" type="gml:integerList"&gt;
     *              &lt;annotation&gt;
     *                  &lt;documentation&gt;Index position of the first grid post, which must lie somwhere in the GridEnvelope.  If absent, the startPoint is equal to the value of gridEnvelope::low from the grid definition.&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GRIDFUNCTIONTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GridFunctionType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="IndexMapType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Exends GridFunctionType with a lookUpTable.  This contains a list of indexes of members within the rangeSet corresponding with the members of the domainSet.  The domainSet is traversed in list order if it is enumerated explicitly, or in the order specified by a SequenceRule if the domain is an implicit set.    The length of the lookUpTable corresponds with the length of the subset of the domainSet for which the coverage is defined.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:GridFunctionType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element name="lookUpTable" type="gml:integerList"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType INDEXMAPTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "IndexMapType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, GRIDFUNCTIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractGeneralParameterValueType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Abstract parameter value or group of parameter values.
     *
     *  This abstract complexType is expected to be extended and restricted for well-known operation methods with many instances, in Application Schemas that define operation-method-specialized element names and contents. Specific parameter value elements are directly contained in concrete subtypes, not in this abstract type. All concrete types derived from this type shall extend this type to include one "...Value" element with an appropriate type, which should be one of the element types allowed in the ParameterValueType. In addition, all derived concrete types shall extend this type to include a "valueOfParameter" element that references one element substitutable for the "OperationParameter" element. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTGENERALPARAMETERVALUETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractGeneralParameterValueType"),
            Collections.EMPTY_LIST, false, true, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ParameterValueType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A parameter value, ordered sequence of values, or reference to a file of parameter values. This concrete complexType can be used for operation methods without using an Application Schema that defines operation-method-specialized element names and contents, especially for methods with only one instance. This complexType can be used, extended, or restricted for well-known operation methods, especially for methods with many instances. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGeneralParameterValueType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;choice&gt;
     *                      &lt;element ref="gml:value"/&gt;
     *                      &lt;element ref="gml:dmsAngleValue"/&gt;
     *                      &lt;element ref="gml:stringValue"/&gt;
     *                      &lt;element ref="gml:integerValue"/&gt;
     *                      &lt;element ref="gml:booleanValue"/&gt;
     *                      &lt;element ref="gml:valueList"/&gt;
     *                      &lt;element ref="gml:integerValueList"/&gt;
     *                      &lt;element ref="gml:valueFile"/&gt;
     *                  &lt;/choice&gt;
     *                  &lt;element ref="gml:valueOfParameter"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType PARAMETERVALUETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ParameterValueType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTGENERALPARAMETERVALUETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractTimeGeometricPrimitiveType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;The abstract supertype for temporal geometric primitives.
     *         A temporal geometry must be associated with a temporal reference system via URI.
     *         The Gregorian calendar with UTC is the default reference system, following ISO
     *         8601. Other reference systems in common use include the GPS calendar and the
     *         Julian calendar.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTimePrimitiveType"&gt;
     *              &lt;attribute default="#ISO-8601" name="frame" type="anyURI" use="optional"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTTIMEGEOMETRICPRIMITIVETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractTimeGeometricPrimitiveType"),
            Collections.EMPTY_LIST, false, true, Collections.EMPTY_SET,
            ABSTRACTTIMEPRIMITIVETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimePeriodType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTimeGeometricPrimitiveType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;choice&gt;
     *                      &lt;element name="beginPosition" type="gml:TimePositionType"/&gt;
     *                      &lt;element name="begin" type="gml:TimeInstantPropertyType"/&gt;
     *                  &lt;/choice&gt;
     *                  &lt;choice&gt;
     *                      &lt;element name="endPosition" type="gml:TimePositionType"/&gt;
     *                      &lt;element name="end" type="gml:TimeInstantPropertyType"/&gt;
     *                  &lt;/choice&gt;
     *                  &lt;group minOccurs="0" ref="gml:timeLength"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMEPERIODTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimePeriodType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTTIMEGEOMETRICPRIMITIVETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractFeatureType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;An abstract feature provides a set of common properties, including id, metaDataProperty, name and description inherited from AbstractGMLType, plus boundedBy.    A concrete feature type must derive from this type and specify additional  properties in an application schema. A feature must possess an identifying attribute ('id' - 'fid' has been deprecated).&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGMLType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element minOccurs="0" ref="gml:boundedBy"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:location"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;appinfo&gt;deprecated&lt;/appinfo&gt;
     *                          &lt;documentation&gt;deprecated in GML version 3.1&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;!-- additional properties must be specified in an application schema --&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    private static List ABSTRACTFEATURETYPE_TYPE_schema = new ArrayList();

    static {
        ABSTRACTFEATURETYPE_TYPE_schema.add(new AttributeDescriptorImpl(BOUNDINGSHAPETYPE_TYPE,
                new Name("http://www.opengis.net/gml", "boundedBy"), 0, 1, false, null));
        ABSTRACTFEATURETYPE_TYPE_schema.add(new AttributeDescriptorImpl(LOCATIONPROPERTYTYPE_TYPE,
                new Name("http://www.opengis.net/gml", "location"), 0, 1, false, null));
    }

    public static final FeatureType ABSTRACTFEATURETYPE_TYPE = new FeatureTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractFeatureType"),
            ABSTRACTFEATURETYPE_TYPE_schema, null, null, true, Collections.EMPTY_SET,
            ABSTRACTGMLTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DynamicFeatureType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A dynamic feature may possess a history and/or a timestamp.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractFeatureType"&gt;
     *              &lt;group ref="gml:dynamicProperties"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DYNAMICFEATURETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DynamicFeatureType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTFEATURETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TargetPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Container for an object representing the target or subject of an observation.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;choice&gt;
     *              &lt;element ref="gml:_Feature"/&gt;
     *              &lt;element ref="gml:_Geometry"/&gt;
     *          &lt;/choice&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TARGETPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TargetPropertyType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="ArcSecondsType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Number of arc-seconds in a degree-minute-second angular value.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="decimal"&gt;
     *          &lt;minInclusive value="0.00"/&gt;
     *          &lt;maxExclusive value="60.00"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType ARCSECONDSTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ArcSecondsType"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, XSSchema.DECIMAL_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimeOrdinalEraType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;Ordinal temporal reference systems are often hierarchically structured
     *        such that an ordinal era at a given level of the hierarchy includes a
     *        sequence of shorter, coterminous ordinal eras. This captured using the member/group properties.
     *
     *        Note that in this schema, TIme Ordinal Era is patterned on TimeEdge, which is a variation from ISO 19108.
     *        This is in order to fulfill the requirements of ordinal reference systems based on eras delimited by
     *        named points or nodes, which are common in geology, archeology, etc.
     *
     *        This change is subject of a change proposal to ISO&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:DefinitionType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0"
     *                      name="relatedTime" type="gml:RelatedTimeType"/&gt;
     *                  &lt;element name="start" type="gml:TimeNodePropertyType"/&gt;
     *                  &lt;element name="end" type="gml:TimeNodePropertyType"/&gt;
     *                  &lt;element minOccurs="0" name="extent" type="gml:TimePeriodPropertyType"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0"
     *                      name="member" type="gml:TimeOrdinalEraPropertyType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;An Era may be composed of several member Eras. The "member" element implements the association to the Era at the next level down the hierarchy.  "member" follows the standard GML property pattern whereby its (complex) value may be either described fully inline, or may be the target of a link carried on the member element and described fully elsewhere, either in the same document or from another service.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element minOccurs="0" name="group" type="gml:ReferenceType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;In a particular Time System, an Era may be a member of a group.  The "group" element implements the back-pointer to the Era at the next level up in the hierarchy.
     *
     *  If the hierarchy is represented by describing the nested components fully in the their nested position inside "member" elements, then the parent can be easily inferred, so the group property is unnecessary.
     *
     *  However, if the hierarchy is represented by links carried on the "member" property elements, pointing to Eras described fully elsewhere, then it may be useful for a child (member) era to carry an explicit pointer back to its parent (group) Era.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMEORDINALERATYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeOrdinalEraType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, DEFINITIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MultiLineStringType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A MultiLineString is defined by one or more LineStrings, referenced through lineStringMember elements. Deprecated with GML version 3.0. Use MultiCurveType instead.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGeometricAggregateType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:lineStringMember"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType MULTILINESTRINGTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MultiLineStringType"), MultiLineString.class, false,
            false, Collections.EMPTY_SET, ABSTRACTGEOMETRICAGGREGATETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="AreaType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Value of a spatial area quantity, with its units. Uses the MeasureType with the restriction that the unit of measure referenced by uom must be suitable for an area, such as square metres or square miles.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;restriction base="gml:MeasureType"/&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType AREATYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AreaType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, MEASURETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="CompassPointEnumeration"&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="N"/&gt;
     *          &lt;enumeration value="NNE"/&gt;
     *          &lt;enumeration value="NE"/&gt;
     *          &lt;enumeration value="ENE"/&gt;
     *          &lt;enumeration value="E"/&gt;
     *          &lt;enumeration value="ESE"/&gt;
     *          &lt;enumeration value="SE"/&gt;
     *          &lt;enumeration value="SSE"/&gt;
     *          &lt;enumeration value="S"/&gt;
     *          &lt;enumeration value="SSW"/&gt;
     *          &lt;enumeration value="SW"/&gt;
     *          &lt;enumeration value="WSW"/&gt;
     *          &lt;enumeration value="W"/&gt;
     *          &lt;enumeration value="WNW"/&gt;
     *          &lt;enumeration value="NW"/&gt;
     *          &lt;enumeration value="NNW"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType COMPASSPOINTENUMERATION_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CompassPointEnumeration"), java.lang.Object.class,
            false, false, Collections.EMPTY_SET, XSSchema.STRING_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CoordinatesType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Tables or arrays of tuples.
     *          May be used for text-encoding of values from a table.
     *          Actually just a string, but allows the user to indicate which characters are used as separators.
     *          The value of the 'cs' attribute is the separator for coordinate values,
     *          and the value of the 'ts' attribute gives the tuple separator (a single space by default);
     *          the default values may be changed to reflect local usage.
     *          Defaults to CSV within a tuple, space between tuples.
     *          However, any string content will be schema-valid.  &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="string"&gt;
     *              &lt;attribute default="." name="decimal" type="string"/&gt;
     *              &lt;attribute default="," name="cs" type="string"/&gt;
     *              &lt;attribute default=" " name="ts" type="string"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType COORDINATESTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CoordinatesType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.STRING_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="SolidArrayPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A container for an array of solids. The elements are always contained in the array property, referencing geometry elements or arrays of geometry elements is not supported.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
     *          &lt;element ref="gml:_Solid"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType SOLIDARRAYPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "SolidArrayPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="PointPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A property that has a point as its value domain can either be an appropriate geometry element encapsulated in an
     *                          element of this type or an XLink reference to a remote geometry element (where remote includes geometry elements located
     *                          elsewhere in the same document). Either the reference or the contained element must be given, but neither both nor none.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:Point"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"&gt;
     *          &lt;annotation&gt;
     *              &lt;documentation&gt;This attribute group includes the XLink attributes (see xlinks.xsd). XLink is used in GML to reference remote
     *                                  resources (including those elsewhere in the same document). A simple link element can be constructed by including a specific
     *                                  set of XLink attributes. The XML Linking Language (XLink) is currently a Proposed Recommendation of the World Wide Web Consortium.
     *                                  XLink allows elements to be inserted into XML documents so as to create sophisticated links between resources; such links can be
     *                                  used to reference remote properties. A simple link element can be used to implement pointer functionality, and this functionality has
     *                                  been built into various GML 3 elements by including the gml:AssociationAttributeGroup.&lt;/documentation&gt;
     *          &lt;/annotation&gt;
     *      &lt;/attributeGroup&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType POINTPROPERTYTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "PointPropertyType"), Point.class, false, false,
            Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TinType_controlPoint"&gt;
     *      &lt;choice&gt;
     *          &lt;element ref="gml:posList"/&gt;
     *          &lt;group maxOccurs="unbounded" minOccurs="3" ref="gml:geometricPositionGroup"/&gt;
     *      &lt;/choice&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TINTYPE_CONTROLPOINT_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TinType_controlPoint"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DefinitionProxyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A proxy entry in a dictionary of definitions. An element of this type contains a reference to a remote definition object. This entry is expected to be convenient in allowing multiple elements in one XML document to contain short (abbreviated XPointer) references, which are resolved to an external definition provided in a Dictionary element in the same XML document. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:DefinitionType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:definitionRef"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;A reference to a remote entry in this dictionary, used when this dictionary entry is identified to allow external references to this specific entry. The remote entry referenced can be in a dictionary in the same or different XML document. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DEFINITIONPROXYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DefinitionProxyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, DEFINITIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractCoverageType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Abstract element which acts as the head of a substitution group for coverages. Note that a coverage is a GML feature.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractFeatureType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:domainSet"/&gt;
     *                  &lt;element ref="gml:rangeSet"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute name="dimension" type="positiveInteger" use="optional"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTCOVERAGETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractCoverageType"), Collections.EMPTY_LIST,
            false, true, Collections.EMPTY_SET, ABSTRACTFEATURETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractDiscreteCoverageType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A discrete coverage consists of a domain set, range set and optionally a coverage function. The domain set consists of either geometry or temporal objects, finite in number. The range set is comprised of a finite number of attribute values each of which is associated to every direct position within any single spatiotemporal object in the domain. In other words, the range values are constant on each spatiotemporal object in the domain. This coverage function maps each element from the coverage domain to an element in its range. This definition conforms to ISO 19123.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCoverageType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element minOccurs="0" ref="gml:coverageFunction"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTDISCRETECOVERAGETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractDiscreteCoverageType"),
            Collections.EMPTY_LIST, false, true, Collections.EMPTY_SET, ABSTRACTCOVERAGETYPE_TYPE,
            null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MultiCurveCoverageType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A discrete coverage type whose domain is defined by a collection of curves.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:AbstractDiscreteCoverageType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;group ref="gml:StandardObjectProperties"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:boundedBy"/&gt;
     *                  &lt;element ref="gml:multiCurveDomain"/&gt;
     *                  &lt;element ref="gml:rangeSet"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:coverageFunction"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType MULTICURVECOVERAGETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MultiCurveCoverageType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTDISCRETECOVERAGETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TopoCurvePropertyType"&gt;
     *      &lt;sequence&gt;
     *          &lt;element ref="gml:TopoCurve"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TOPOCURVEPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TopoCurvePropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="PolarCSRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a polar coordinate system, either referencing or containing the definition of that coordinate system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:PolarCS"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType POLARCSREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "PolarCSRefType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="IncrementOrder"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;The enumeration value here indicates the incrementation order  to be used on the first 2 axes, i.e. "+x-y" means that the points on the first axis are to be traversed from lowest to highest and  the points on the second axis are to be traversed from highest to lowest. The points on all other axes (if any) beyond the first 2 are assumed to increment from lowest to highest.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="+x+y"/&gt;
     *          &lt;enumeration value="+y+x"/&gt;
     *          &lt;enumeration value="+x-y"/&gt;
     *          &lt;enumeration value="-x-y"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType INCREMENTORDER_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "IncrementOrder"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, XSSchema.STRING_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GeometryStylePropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation/&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element minOccurs="0" ref="gml:GeometryStyle"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attribute name="about" type="anyURI" use="optional"/&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GEOMETRYSTYLEPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GeometryStylePropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MultiCurvePropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A property that has a collection of curves as its value domain can either be an appropriate geometry element encapsulated in an element of this type or an XLink reference to a remote geometry element (where remote includes geometry elements located elsewhere in the same document). Either the reference or the contained element must be given, but neither both nor none.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:MultiCurve"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"&gt;
     *          &lt;annotation&gt;
     *              &lt;documentation&gt;This attribute group includes the XLink attributes (see xlinks.xsd). XLink is used in GML to reference remote resources (including those elsewhere in the same document). A simple link element can be constructed by including a specific set of XLink attributes. The XML Linking Language (XLink) is currently a Proposed Recommendation of the World Wide Web Consortium. XLink allows elements to be inserted into XML documents so as to create sophisticated links between resources; such links can be used to reference remote properties.
     *  A simple link element can be used to implement pointer functionality, and this functionality has been built into various GML 3 elements by including the gml:AssociationAttributeGroup.&lt;/documentation&gt;
     *          &lt;/annotation&gt;
     *      &lt;/attributeGroup&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType MULTICURVEPROPERTYTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MultiCurvePropertyType"), MultiLineString[].class,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ProjectedCRSRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a projected coordinate reference system, either referencing or containing the definition of that reference system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:ProjectedCRS"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType PROJECTEDCRSREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ProjectedCRSRefType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="DrawingTypeType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Graph-specific styling property.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="POLYLINE"/&gt;
     *          &lt;enumeration value="ORTHOGONAL"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType DRAWINGTYPETYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DrawingTypeType"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, XSSchema.STRING_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractCoordinateSystemBaseType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Basic encoding for coordinate system objects, simplifying and restricting the DefinitionType as needed. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:DefinitionType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:metaDataProperty"/&gt;
     *                  &lt;element ref="gml:csName"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute ref="gml:id" use="required"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTCOORDINATESYSTEMBASETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractCoordinateSystemBaseType"),
            Collections.EMPTY_LIST, false, true, Collections.EMPTY_SET, DEFINITIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractCoordinateSystemType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A coordinate system (CS) is the set of coordinate system axes that spans a given coordinate space. A CS is derived from a set of (mathematical) rules for specifying how coordinates in a given space are to be assigned to points. The coordinate values in a coordinate tuple shall be recorded in the order in which the coordinate system axes associations are recorded, whenever those coordinates use a coordinate reference system that uses this coordinate system. This abstract complexType shall not be used, extended, or restricted, in an Application Schema, to define a concrete subtype with a meaning equivalent to a concrete subtype specified in this document. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCoordinateSystemBaseType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:csID"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Set of alternative identifications of this coordinate system. The first csID, if any, is normally the primary identification code, and any others are aliases. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element minOccurs="0" ref="gml:remarks"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Comments on or information about this coordinate system, including data source information. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element maxOccurs="unbounded" ref="gml:usesAxis"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Ordered sequence of associations to the coordinate system axes included in this coordinate system. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTCOORDINATESYSTEMTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractCoordinateSystemType"),
            Collections.EMPTY_LIST, false, true, Collections.EMPTY_SET,
            ABSTRACTCOORDINATESYSTEMBASETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="PolarCSType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A two-dimensional coordinate system in which position is specified by the distance from the origin and the angle between the line from the origin to a point and a reference direction. A PolarCS shall have two usesAxis associations. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCoordinateSystemType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType POLARCSTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "PolarCSType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, ABSTRACTCOORDINATESYSTEMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MultiPointDomainType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:DomainSetType"&gt;
     *              &lt;sequence minOccurs="0"&gt;
     *                  &lt;element ref="gml:MultiPoint"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType MULTIPOINTDOMAINTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MultiPointDomainType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, DOMAINSETTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MultiPolygonPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;This type is deprecated with GML 3 and shall not be used. It is included for backwards compatibility with GML 2. Use MultiSurfacePropertyType instead.
     *
     *  A property that has a collection of polygons as its value domain can either be an appropriate geometry element encapsulated in an element of this type or an XLink reference to a remote geometry element (where remote includes geometry elements located elsewhere in the same document). Either the reference or the contained element must be given, but neither both nor none.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:MultiPolygon"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"&gt;
     *          &lt;annotation&gt;
     *              &lt;documentation&gt;This attribute group includes the XLink attributes (see xlinks.xsd). XLink is used in GML to reference remote resources (including those elsewhere in the same document). A simple link element can be constructed by including a specific set of XLink attributes. The XML Linking Language (XLink) is currently a Proposed Recommendation of the World Wide Web Consortium. XLink allows elements to be inserted into XML documents so as to create sophisticated links between resources; such links can be used to reference remote properties.
     *  A simple link element can be used to implement pointer functionality, and this functionality has been built into various GML 3 elements by including the gml:AssociationAttributeGroup.&lt;/documentation&gt;
     *          &lt;/annotation&gt;
     *      &lt;/attributeGroup&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType MULTIPOLYGONPROPERTYTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MultiPolygonPropertyType"), MultiPolygon.class,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TopoPointType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;The intended use of TopoPoint is to appear within a point feature to express the structural and possibly geometric relationships of this point to other features via shared node definitions. Note the orientation assigned to the directedNode has no meaning in this context. It is preserved for symmetry with the types and elements which follow.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTopologyType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:directedNode"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TOPOPOINTTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TopoPointType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTTOPOLOGYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractTimeReferenceSystemType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;A value in the time domain is measured relative to a temporal reference system. Common
     *          types of reference systems include calendars, ordinal temporal reference systems, and
     *          temporal coordinate systems (time elapsed since some epoch, e.g. UNIX time).&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:DefinitionType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element minOccurs="0" name="domainOfValidity" type="string"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTTIMEREFERENCESYSTEMTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractTimeReferenceSystemType"),
            Collections.EMPTY_LIST, false, true, Collections.EMPTY_SET, DEFINITIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimeOrdinalReferenceSystemType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;In an ordinal reference system the order of events in time can be well
     *        established, but the magnitude of the intervals between them can not be
     *        accurately determined (e.g. a stratigraphic sequence).&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTimeReferenceSystemType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" name="component" type="gml:TimeOrdinalEraPropertyType"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMEORDINALREFERENCESYSTEMTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeOrdinalReferenceSystemType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET,
            ABSTRACTTIMEREFERENCESYSTEMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="OperationParameterGroupRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to an operation parameter, either referencing or containing the definition of that parameter. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:OperationParameterGroup"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType OPERATIONPARAMETERGROUPREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "OperationParameterGroupRefType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="KnotPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Encapsulates a knot to use it in a geometric type.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element name="Knot" type="gml:KnotType"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType KNOTPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "KnotPropertyType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GridLengthType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Value of a length (or distance) quantity in a grid, where the grid spacing does not have any associated physical units, or does not have a constant physical spacing. This grid length will often be used in a digital image grid, where the base units are likely to be pixel spacings. Uses the MeasureType with the restriction that the unit of measure referenced by uom must be suitable for length along the axes of a grid, such as pixel spacings or grid spacings.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;restriction base="gml:MeasureType"/&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GRIDLENGTHTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GridLengthType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, MEASURETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DirectionVectorType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Direction expressed as a vector, either using components, or using angles.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;choice&gt;
     *          &lt;element ref="gml:vector"/&gt;
     *          &lt;sequence&gt;
     *              &lt;element name="horizontalAngle" type="gml:AngleType"/&gt;
     *              &lt;element name="verticalAngle" type="gml:AngleType"/&gt;
     *          &lt;/sequence&gt;
     *      &lt;/choice&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DIRECTIONVECTORTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DirectionVectorType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractCurveSegmentType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Curve segment defines a homogeneous segment of a curve.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence/&gt;
     *      &lt;attribute default="0" name="numDerivativesAtStart" type="integer" use="optional"&gt;
     *          &lt;annotation&gt;
     *              &lt;documentation&gt;The attribute "numDerivativesAtStart" specifies the type of continuity between this curve segment and its predecessor. If this is the first curve segment in the curve, one of these values, as appropriate, is ignored. The default value of "0" means simple continuity, which is a mandatory minimum level of continuity. This level is referred to as "C 0 " in mathematical texts. A value of 1 means that the function and its first derivative are continuous at the appropriate end point: "C 1 " continuity. A value of "n" for any integer means the function and its first n derivatives are continuous: "C n " continuity.
     *  NOTE: Use of these values is only appropriate when the basic curve definition is an underdetermined system. For example, line string segments cannot support continuity above C 0 , since there is no spare control parameter to adjust the incoming angle at the end points of the segment. Spline functions on the other hand often have extra degrees of freedom on end segments that allow them to adjust the values of the derivatives to support C 1 or higher continuity.&lt;/documentation&gt;
     *          &lt;/annotation&gt;
     *      &lt;/attribute&gt;
     *      &lt;attribute default="0" name="numDerivativesAtEnd" type="integer" use="optional"&gt;
     *          &lt;annotation&gt;
     *              &lt;documentation&gt;The attribute "numDerivativesAtEnd" specifies the type of continuity between this curve segment and its successor. If this is the last curve segment in the curve, one of these values, as appropriate, is ignored. The default value of "0" means simple continuity, which is a mandatory minimum level of continuity. This level is referred to as "C 0 " in mathematical texts. A value of 1 means that the function and its first derivative are continuous at the appropriate end point: "C 1 " continuity. A value of "n" for any integer means the function and its first n derivatives are continuous: "C n " continuity.
     *  NOTE: Use of these values is only appropriate when the basic curve definition is an underdetermined system. For example, line string segments cannot support continuity above C 0 , since there is no spare control parameter to adjust the incoming angle at the end points of the segment. Spline functions on the other hand often have extra degrees of freedom on end segments that allow them to adjust the values of the derivatives to support C 1 or higher continuity.&lt;/documentation&gt;
     *          &lt;/annotation&gt;
     *      &lt;/attribute&gt;
     *      &lt;attribute default="0" name="numDerivativeInterior" type="integer" use="optional"&gt;
     *          &lt;annotation&gt;
     *              &lt;documentation&gt;The attribute "numDerivativesInterior" specifies the type of continuity that is guaranteed interior to the curve. The default value of "0" means simple continuity, which is a mandatory minimum level of continuity. This level is referred to as "C 0 " in mathematical texts. A value of 1 means that the function and its first derivative are continuous at the appropriate end point: "C 1 " continuity. A value of "n" for any integer means the function and its first n derivatives are continuous: "C n " continuity.
     *  NOTE: Use of these values is only appropriate when the basic curve definition is an underdetermined system. For example, line string segments cannot support continuity above C 0 , since there is no spare control parameter to adjust the incoming angle at the end points of the segment. Spline functions on the other hand often have extra degrees of freedom on end segments that allow them to adjust the values of the derivatives to support C 1 or higher continuity.&lt;/documentation&gt;
     *          &lt;/annotation&gt;
     *      &lt;/attribute&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTCURVESEGMENTTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractCurveSegmentType"), Collections.EMPTY_LIST,
            false, true, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ArcByCenterPointType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;This variant of the arc requires that the points on the arc have to be computed instead of storing the coordinates directly. The control point is the center point of the arc plus the radius and the bearing at start and end. This represenation can be used only in 2D.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCurveSegmentType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;choice&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;GML supports two different ways to specify the control points of a curve segment.
     *  1. A "pos" (DirectPositionType) or "pointProperty" (PointPropertyType) element. The "pos" element contains a center point that is only part of this curve segment, a "pointProperty" element contains a point that may be referenced from other geometry elements or reference another point defined outside of this curve segment (reuse of existing points).
     *  2. The "posList" element can be used to specifiy the coordinates of the center point, too. The number of direct positions in the list must be one.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                      &lt;choice&gt;
     *                          &lt;element ref="gml:pos"/&gt;
     *                          &lt;element ref="gml:pointProperty"/&gt;
     *                          &lt;element ref="gml:pointRep"&gt;
     *                              &lt;annotation&gt;
     *                                  &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "pointProperty" instead. Included for backwards compatibility with GML 3.0.0.&lt;/documentation&gt;
     *                              &lt;/annotation&gt;
     *                          &lt;/element&gt;
     *                      &lt;/choice&gt;
     *                      &lt;element ref="gml:posList"/&gt;
     *                      &lt;element ref="gml:coordinates"&gt;
     *                          &lt;annotation&gt;
     *                              &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "posList" instead.&lt;/documentation&gt;
     *                          &lt;/annotation&gt;
     *                      &lt;/element&gt;
     *                  &lt;/choice&gt;
     *                  &lt;element name="radius" type="gml:LengthType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;The radius of the arc.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element minOccurs="0" name="startAngle" type="gml:AngleType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;The bearing of the arc at the start.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element minOccurs="0" name="endAngle" type="gml:AngleType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;The bearing of the arc at the end.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute fixed="circularArcCenterPointWithRadius"
     *                  name="interpolation" type="gml:CurveInterpolationType"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;The attribute "interpolation" specifies the curve interpolation mechanism used for this segment. This mechanism
     *  uses the control points and control parameters to determine the position of this curve segment. For an ArcByCenterPoint the interpolation is fixed as "circularArcCenterPointWithRadius".&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *              &lt;attribute fixed="1" name="numArc" type="integer" use="required"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;Since this type describes always a single arc, the attribute is fixed to "1".&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ARCBYCENTERPOINTTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ArcByCenterPointType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTCURVESEGMENTTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CircleByCenterPointType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A CircleByCenterPoint is an ArcByCenterPoint with identical start and end angle to form a full circle. Again, this represenation can be used only in 2D.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:ArcByCenterPointType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CIRCLEBYCENTERPOINTTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CircleByCenterPointType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ARCBYCENTERPOINTTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="AbstractSurfaceType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;An abstraction of a surface to support the different levels of complexity. A surface is always a continuous region of a plane.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGeometricPrimitiveType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTSURFACETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractSurfaceType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTGEOMETRICPRIMITIVETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="SurfaceType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A Surface is a 2-dimensional primitive and is composed of one or more surface patches. The surface patches are connected to one another.
     *                                  The orientation of the surface is positive ("up"). The orientation of a surface chooses an "up" direction through the choice of the upward normal, which, if the surface is not a cycle, is the side of the surface from which the exterior boundary appears counterclockwise. Reversal of the surface orientation reverses the curve orientation of each boundary component, and interchanges the conceptual "up" and "down" direction of the surface. If the surface is the boundary of a solid, the "up" direction is usually outward. For closed surfaces, which have no boundary, the up direction is that of the surface patches, which must be consistent with one another. Its included surface patches describe the interior structure of the Surface.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractSurfaceType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:patches"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;This element encapsulates the patches of the surface.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType SURFACETYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "SurfaceType"), Polygon.class, false, false,
            Collections.EMPTY_SET, ABSTRACTSURFACETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TriangulatedSurfaceType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A triangulated surface is a polyhedral
     *     surface that is composed only of triangles. There is no
     *     restriction on how the triangulation is derived.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:SurfaceType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;group ref="gml:StandardObjectProperties"/&gt;
     *                  &lt;element ref="gml:trianglePatches"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;This property encapsulates the patches of
     *        the triangulated surface.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TRIANGULATEDSURFACETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TriangulatedSurfaceType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, SURFACETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TinType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A tin is a triangulated surface that uses
     *     the Delauny algorithm or a similar algorithm complemented with
     *     consideration of breaklines, stoplines, and maximum length of
     *     triangle sides. These networks satisfy the Delauny's criterion
     *     away from the modifications: Fore each triangle in the
     *     network, the circle passing through its vertices does not
     *     contain, in its interior, the vertex of any other triangle.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:TriangulatedSurfaceType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0"
     *                      name="stopLines" type="gml:LineStringSegmentArrayPropertyType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Stoplines are lines where the local
     *         continuity or regularity of the surface is questionable.
     *         In the area of these pathologies, triangles intersecting
     *         a stopline shall be removed from the tin surface, leaving
     *         holes in the surface. If coincidence occurs on surface
     *         boundary triangles, the result shall be a change of the
     *         surface boundary. Stoplines contains all these
     *         pathological segments as a set of line strings.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0"
     *                      name="breakLines" type="gml:LineStringSegmentArrayPropertyType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Breaklines are lines of a critical
     *         nature to the shape of the surface, representing local
     *         ridges, or depressions (such as drainage lines) in the
     *         surface. As such their constituent segments must be
     *         included in the tin eve if doing so
     *         violates the Delauny criterion. Break lines contains these
     *         critical segments as a set of line strings.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element name="maxLength" type="gml:LengthType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Areas of the surface where data is not
     *         sufficiently dense to assure reasonable calculation shall be
     *         removed by adding a retention criterion for triangles based
     *         on the length of their sides. For many triangle sides
     *         exceeding maximum length, the adjacent triangles to that
     *         triangle side shall be removed from the surface.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element name="controlPoint"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;The corners of the triangles in the TIN
     *    are often referred to as pots. ControlPoint shall contain a
     *    set of the GM_Position used as posts for this TIN. Since each
     *    TIN contains triangles, there must be at least 3 posts. The
     *         order in which these points are given does not affect the
     *         surface that is represented. Application schemas may add
     *         information based on ordering of control points to facilitate
     *         the reconstruction of the TIN from the control points.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                      &lt;complexType name="TinType_controlPoint"&gt;
     *                          &lt;choice&gt;
     *                              &lt;element ref="gml:posList"/&gt;
     *                              &lt;group maxOccurs="unbounded" minOccurs="3" ref="gml:geometricPositionGroup"/&gt;
     *                          &lt;/choice&gt;
     *                      &lt;/complexType&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TINTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TinType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, TRIANGULATEDSURFACETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="AbstractRingPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Encapsulates a ring to represent the surface boundary property of a surface.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element ref="gml:_Ring"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTRINGPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractRingPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ClothoidType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A clothoid, or Cornu's spiral, is plane
     *     curve whose curvature is a fixed function of its length.
     *     In suitably chosen co-ordinates it is given by Fresnel's
     *     integrals.
     *
     *      x(t) = 0-integral-t cos(AT*T/2)dT
     *
     *      y(t) = 0-integral-t sin(AT*T/2)dT
     *
     *     This geometry is mainly used as a transition curve between
     *     curves of type straight line to circular arc or circular arc
     *     to circular arc. With this curve type it is possible to
     *     achieve a C2-continous transition between the above mentioned
     *     curve types. One formula for the Clothoid is A*A = R*t where
     *     A is constant, R is the varying radius of curvature along the
     *     the curve and t is the length along and given in the Fresnel
     *     integrals.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCurveSegmentType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element name="refLocation"&gt;
     *                      &lt;complexType name="ClothoidType_refLocation"&gt;
     *                          &lt;sequence&gt;
     *                              &lt;element ref="gml:AffinePlacement"&gt;
     *                                  &lt;annotation&gt;
     *                                      &lt;documentation&gt;The "refLocation" is an affine mapping
     *            that places  the curve defined by the Fresnel Integrals
     *            into the co-ordinate reference system of this object.&lt;/documentation&gt;
     *                                  &lt;/annotation&gt;
     *                              &lt;/element&gt;
     *                          &lt;/sequence&gt;
     *                      &lt;/complexType&gt;
     *                  &lt;/element&gt;
     *                  &lt;element name="scaleFactor" type="decimal"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;The element gives the value for the
     *         constant in the Fresnel's integrals.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element name="startParameter" type="double"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;The startParameter is the arc length
     *         distance from the inflection point that will be the start
     *         point for this curve segment. This shall be lower limit
     *         used in the Fresnel integral and is the value of the
     *         constructive parameter of this curve segment at its start
     *         point. The startParameter can either be positive or
     *         negative.
     *         NOTE! If 0.0 (zero), lies between the startParameter and
     *         the endParameter of the clothoid, then the curve goes
     *         through the clothoid's inflection point, and the direction
     *         of its radius of curvature, given by the second
     *         derivative vector, changes sides with respect to the
     *         tangent vector. The term length distance for the&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element name="endParameter" type="double"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;The endParameter is the arc length
     *         distance from the inflection point that will be the end
     *         point for this curve segment. This shall be upper limit
     *         used in the Fresnel integral and is the value of the
     *         constructive parameter of this curve segment at its
     *         start point. The startParameter can either be positive
     *         or negative.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CLOTHOIDTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ClothoidType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTCURVESEGMENTTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="SolidPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A property that has a solid as its value domain can either be an appropriate geometry element encapsulated in an element of this type or an XLink reference to a remote geometry element (where remote includes geometry elements located elsewhere in the same document). Either the reference or the contained element must be given, but neither both nor none.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:_Solid"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"&gt;
     *          &lt;annotation&gt;
     *              &lt;documentation&gt;This attribute group includes the XLink attributes (see xlinks.xsd). XLink is used in GML to reference remote resources (including those elsewhere in the same document). A simple link element can be constructed by including a specific set of XLink attributes. The XML Linking Language (XLink) is currently a Proposed Recommendation of the World Wide Web Consortium. XLink allows elements to be inserted into XML documents so as to create sophisticated links between resources; such links can be used to reference remote properties.
     *  A simple link element can be used to implement pointer functionality, and this functionality has been built into various GML 3 elements by including the gml:AssociationAttributeGroup.&lt;/documentation&gt;
     *          &lt;/annotation&gt;
     *      &lt;/attributeGroup&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType SOLIDPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "SolidPropertyType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractDatumBaseType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Basic encoding for datum objects, simplifying and restricting the DefinitionType as needed. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:DefinitionType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:metaDataProperty"/&gt;
     *                  &lt;element ref="gml:datumName"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute ref="gml:id" use="required"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTDATUMBASETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractDatumBaseType"), Collections.EMPTY_LIST,
            false, true, Collections.EMPTY_SET, DEFINITIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractDatumType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A datum specifies the relationship of a coordinate system to the earth, thus creating a coordinate reference system. A datum uses a parameter or set of parameters that determine the location of the origin of the coordinate reference system. Each datum subtype can be associated with only specific types of coordinate systems. This abstract complexType shall not be used, extended, or restricted, in an Application Schema, to define a concrete subtype with a meaning equivalent to a concrete subtype specified in this document. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractDatumBaseType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:datumID"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Set of alternative identifications of this datum. The first datumID, if any, is normally the primary identification code, and any others are aliases. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element minOccurs="0" ref="gml:remarks"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Comments on this reference system, including source information. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element minOccurs="0" ref="gml:anchorPoint"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:realizationEpoch"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:validArea"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:scope"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTDATUMTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractDatumType"), Collections.EMPTY_LIST, false,
            true, Collections.EMPTY_SET, ABSTRACTDATUMBASETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ImageDatumType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;An image datum defines the origin of an image coordinate reference system, and is used in a local context only. For more information, see OGC Abstract Specification Topic 2. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractDatumType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:pixelInCell"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType IMAGEDATUMTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ImageDatumType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTDATUMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="LabelStyleType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;[complexType of] The style descriptor for labels of a feature, geometry or topology.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:BaseStyleDescriptorType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element name="style" type="string"/&gt;
     *                  &lt;element name="label" type="gml:LabelType"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType LABELSTYLETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "LabelStyleType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, BASESTYLEDESCRIPTORTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="LinearCSType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A one-dimensional coordinate system that consists of the points that lie on the single axis described. The associated ordinate is the distance from the specified origin to the point along the axis. Example: usage of the line feature representing a road to describe points on or along that road. A LinearCS shall have one usesAxis association. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCoordinateSystemType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType LINEARCSTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "LinearCSType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTCOORDINATESYSTEMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="integerOrNull"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Union of the XML Schema integer type and the GML Nulltype.  An element which uses this type may have content which is either an integer or a value from Nulltype&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;union memberTypes="gml:NullEnumeration integer anyURI"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType INTEGERORNULL_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "integerOrNull"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, XSSchema.ANYSIMPLETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="FeatureStyleType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;[complexType of] The style descriptor for features.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGMLType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element minOccurs="0" name="featureConstraint" type="string"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:geometryStyle"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:topologyStyle"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:labelStyle"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute name="featureType" type="string" use="optional"/&gt;
     *              &lt;attribute name="baseType" type="string" use="optional"/&gt;
     *              &lt;attribute name="queryGrammar" type="gml:QueryGrammarEnumeration"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType FEATURESTYLETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "FeatureStyleType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTGMLTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="PrimeMeridianBaseType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Basic encoding for prime meridian objects, simplifying and restricting the DefinitionType as needed. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:DefinitionType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:metaDataProperty"/&gt;
     *                  &lt;element ref="gml:meridianName"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute ref="gml:id" use="required"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType PRIMEMERIDIANBASETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "PrimeMeridianBaseType"), Collections.EMPTY_LIST,
            false, true, Collections.EMPTY_SET, DEFINITIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="PrimeMeridianType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A prime meridian defines the origin from which longitude values are determined.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:PrimeMeridianBaseType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:meridianID"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Set of alternative identifications of this prime meridian. The first meridianID, if any, is normally the primary identification code, and any others are aliases. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element minOccurs="0" ref="gml:remarks"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Comments on or information about this prime meridian, including source information. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element ref="gml:greenwichLongitude"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType PRIMEMERIDIANTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "PrimeMeridianType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, PRIMEMERIDIANBASETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="CoordinateSystemAxisBaseType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Basic encoding for coordinate system axis objects, simplifying and restricting the DefinitionType as needed. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:DefinitionType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:metaDataProperty"/&gt;
     *                  &lt;element ref="gml:name"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;The name by which this coordinate system axis is identified. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute ref="gml:id" use="required"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType COORDINATESYSTEMAXISBASETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CoordinateSystemAxisBaseType"),
            Collections.EMPTY_LIST, false, true, Collections.EMPTY_SET, DEFINITIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CoordinateSystemAxisType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Definition of a coordinate system axis. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:CoordinateSystemAxisBaseType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:axisID"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Set of alternative identifications of this coordinate system axis. The first axisID, if any, is normally the primary identification code, and any others are aliases. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element minOccurs="0" ref="gml:remarks"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Comments on or information about this coordinate system axis, including data source information. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element ref="gml:axisAbbrev"/&gt;
     *                  &lt;element ref="gml:axisDirection"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute ref="gml:uom" use="required"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType COORDINATESYSTEMAXISTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CoordinateSystemAxisType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, COORDINATESYSTEMAXISBASETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ImageCRSRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to an image coordinate reference system, either referencing or containing the definition of that reference system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:ImageCRS"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType IMAGECRSREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ImageCRSRefType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="AngleChoiceType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Value of an angle quantity provided in either degree-minute-second format or single value format.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;choice&gt;
     *          &lt;element ref="gml:angle"/&gt;
     *          &lt;element ref="gml:dmsAngle"/&gt;
     *      &lt;/choice&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ANGLECHOICETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AngleChoiceType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="FeaturePropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Container for a feature - follow gml:AssociationType pattern.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:_Feature"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType FEATUREPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "FeaturePropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractReferenceSystemBaseType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Basic encoding for reference system objects, simplifying and restricting the DefinitionType as needed.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:DefinitionType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:metaDataProperty"/&gt;
     *                  &lt;element ref="gml:srsName"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute ref="gml:id" use="required"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTREFERENCESYSTEMBASETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractReferenceSystemBaseType"),
            Collections.EMPTY_LIST, false, true, Collections.EMPTY_SET, DEFINITIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractReferenceSystemType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Description of a spatial and/or temporal reference system used by a dataset.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractReferenceSystemBaseType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:srsID"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Set of alterative identifications of this reference system. The first srsID, if any, is normally the primary identification code, and any others are aliases.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element minOccurs="0" ref="gml:remarks"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Comments on or information about this reference system, including source information.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element minOccurs="0" ref="gml:validArea"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:scope"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTREFERENCESYSTEMTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractReferenceSystemType"),
            Collections.EMPTY_LIST, false, true, Collections.EMPTY_SET,
            ABSTRACTREFERENCESYSTEMBASETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractGeneralDerivedCRSType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A coordinate reference system that is defined by its coordinate conversion from another coordinate reference system (not by a datum). This abstract complexType shall not be used, extended, or restricted, in an Application Schema, to define a concrete subtype with a meaning equivalent to a concrete subtype specified in this document. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractReferenceSystemType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:baseCRS"/&gt;
     *                  &lt;element ref="gml:definedByConversion"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTGENERALDERIVEDCRSTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractGeneralDerivedCRSType"),
            Collections.EMPTY_LIST, false, true, Collections.EMPTY_SET,
            ABSTRACTREFERENCESYSTEMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DerivedCRSType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A coordinate reference system that is defined by its coordinate conversion from another coordinate reference system but is not a projected coordinate reference system. This category includes coordinate reference systems derived from a projected coordinate reference system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGeneralDerivedCRSType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:derivedCRSType"/&gt;
     *                  &lt;element ref="gml:usesCS"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DERIVEDCRSTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DerivedCRSType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTGENERALDERIVEDCRSTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CylindricalCSRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a cylindrical coordinate system, either referencing or containing the definition of that coordinate system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:CylindricalCS"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CYLINDRICALCSREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CylindricalCSRefType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="RingType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A Ring is used to represent a single connected component of a surface boundary. It consists of a sequence of curves connected in a cycle (an object whose boundary is empty).
     *  A Ring is structurally similar to a composite curve in that the endPoint of each curve in the sequence is the startPoint of the next curve in the Sequence. Since the sequence is circular, there is no exception to this rule. Each ring, like all boundaries, is a cycle and each ring is simple.
     *  NOTE: Even though each Ring is simple, the boundary need not be simple. The easiest case of this is where one of the interior rings of a surface is tangent to its exterior ring.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractRingType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" ref="gml:curveMember"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;This element references or contains one curve in the composite curve. The curves are contiguous, the collection of curves is ordered.
     *  NOTE: This definition allows for a nested structure, i.e. a CompositeCurve may use, for example, another CompositeCurve as a curve member.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType RINGTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "RingType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, ABSTRACTRINGTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ScaleType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Value of a scale factor (or ratio) that has no physical unit. Uses the MeasureType with the restriction that the unit of measure referenced by uom must be suitable for a scale factor, such as percent, permil, or parts-per-million.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;restriction base="gml:MeasureType"/&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType SCALETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ScaleType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, MEASURETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="NameOrNullList"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;XML List based on the union type defined above.  An element declared with this type contains a space-separated list of Name values with null values interspersed as needed&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;list itemType="gml:NameOrNull"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType NAMEORNULLLIST_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "NameOrNullList"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, XSSchema.ANYSIMPLETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CodeOrNullListType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;List of values on a uniform nominal scale.  List of text tokens.
     *        In a list context a token should not include any spaces, so xsd:Name is used instead of xsd:string.
     *        A member of the list may be a typed null.
     *        If a codeSpace attribute is present, then its value is a reference to
     *        a Reference System for the value, a dictionary or code list.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:NameOrNullList"&gt;
     *              &lt;attribute name="codeSpace" type="anyURI" use="optional"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CODEORNULLLISTTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CodeOrNullListType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, NAMEORNULLLIST_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CategoryExtentType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Restriction of list type to store a 2-point range of ordinal values. If one member is a null, then this is a single ended interval.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;restriction base="gml:CodeOrNullListType"&gt;
     *              &lt;length value="2"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CATEGORYEXTENTTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CategoryExtentType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, CODEORNULLLISTTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="AngleType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Value of an angle quantity recorded as a single number, with its units. Uses the MeasureType with the restriction that the unit of measure referenced by uom must be suitable for an angle, such as degrees or radians.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;restriction base="gml:MeasureType"/&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ANGLETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AngleType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, MEASURETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ExtentType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Information about the spatial, vertical, and/or temporal extent of a reference system object. Constraints: At least one of the elements "description", "boundingBox", "boundingPolygon", "verticalExtent", and temporalExtent" must be included, but more that one can be included when appropriate. Furthermore, more than one "boundingBox", "boundingPolygon", "verticalExtent", and/or temporalExtent" element can be included, with more than one meaning the union of the individual domains.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element minOccurs="0" ref="gml:description"&gt;
     *              &lt;annotation&gt;
     *                  &lt;documentation&gt;Description of spatial and/or temporal extent of this object.&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *          &lt;choice&gt;
     *              &lt;annotation&gt;
     *                  &lt;documentation&gt;Geographic domain of this reference system object.&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *              &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:boundingBox"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;Unordered list of bounding boxes (or envelopes) whose union describes the spatial domain of this object.&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/element&gt;
     *              &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:boundingPolygon"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;Unordered list of bounding polygons whose union describes the spatial domain of this object.&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/element&gt;
     *          &lt;/choice&gt;
     *          &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:verticalExtent"&gt;
     *              &lt;annotation&gt;
     *                  &lt;documentation&gt;Unordered list of vertical intervals whose union describes the spatial domain of this object.&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *          &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:temporalExtent"&gt;
     *              &lt;annotation&gt;
     *                  &lt;documentation&gt;Unordered list of time periods whose union describes the spatial domain of this object.&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType EXTENTTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ExtentType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="LengthType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Value of a length (or distance) quantity, with its units. Uses the MeasureType with the restriction that the unit of measure referenced by uom must be suitable for a length, such as metres or feet.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;restriction base="gml:MeasureType"/&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType LENGTHTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "LengthType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, MEASURETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="SurfacePatchArrayPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A container for an array of surface patches.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
     *          &lt;element ref="gml:_SurfacePatch"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType SURFACEPATCHARRAYPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "SurfacePatchArrayPropertyType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TrianglePatchArrayPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;This type defines a container for an array of
     *       triangle patches.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:SurfacePatchArrayPropertyType"&gt;
     *              &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
     *                  &lt;element ref="gml:Triangle"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TRIANGLEPATCHARRAYPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TrianglePatchArrayPropertyType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET,
            SURFACEPATCHARRAYPROPERTYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CompositeSurfaceType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A CompositeSurface is defined by a set of orientable surfaces. A composite surface is geometry type with all the geometric properties of a (primitive) surface. Essentially, a composite surface is a collection of surfaces that join in pairs on common boundary curves and which, when considered as a whole, form a single surface.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractSurfaceType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" ref="gml:surfaceMember"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;This element references or contains one surface in the composite surface. The surfaces are contiguous.
     *  NOTE: This definition allows for a nested structure, i.e. a CompositeSurface may use, for example, another CompositeSurface as a member.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType COMPOSITESURFACETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CompositeSurfaceType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTSURFACETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="SecondDefiningParameterType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Definition of the second parameter that defines the shape of an ellipsoid. An ellipsoid requires two defining parameters: semi-major axis and inverse flattening or semi-major axis and semi-minor axis. When the reference body is a sphere rather than an ellipsoid, only a single defining parameter is required, namely the radius of the sphere; in that case, the semi-major axis "degenerates" into the radius of the sphere.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;choice&gt;
     *          &lt;element ref="gml:inverseFlattening"/&gt;
     *          &lt;element ref="gml:semiMinorAxis"/&gt;
     *          &lt;element ref="gml:isSphere"/&gt;
     *      &lt;/choice&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType SECONDDEFININGPARAMETERTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "SecondDefiningParameterType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ConcatenatedOperationRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a concatenated operation, either referencing or containing the definition of that concatenated operation. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:ConcatenatedOperation"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CONCATENATEDOPERATIONREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ConcatenatedOperationRefType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimeTopologyComplexPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A time topology complex property can either be any time topology complex element
     *                           encapsulated in an element of this type or an XLink reference to a remote time topology complex element
     *                           (where remote includes elements located elsewhere in the same document).
     *                           Note that either the reference or the contained element must be given, but not both or none.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:TimeTopologyComplex"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMETOPOLOGYCOMPLEXPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeTopologyComplexPropertyType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DictionaryEntryType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;An entry in a dictionary of definitions. An instance of this type contains or refers to a definition object.
     *
     *  The number of definitions contained in this dictionaryEntry is restricted to one, but a DefinitionCollection or Dictionary that contains multiple definitions can be substituted if needed. Specialized descendents of this dictionaryEntry might be restricted in an application schema to allow only including specified types of definitions as valid entries in a dictionary. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:Definition"&gt;
     *              &lt;annotation&gt;
     *                  &lt;documentation&gt;This element in a dictionary entry contains the actual definition. &lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"&gt;
     *          &lt;annotation&gt;
     *              &lt;documentation&gt;A non-identified reference to a remote entry in this dictionary, to be used when this entry need not be identified to allow external references to this specific entry. The remote entry referenced will usually be in a dictionary in the same XML document. This element will usually be used in dictionaries that are inside of another dictionary. &lt;/documentation&gt;
     *          &lt;/annotation&gt;
     *      &lt;/attributeGroup&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DICTIONARYENTRYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DictionaryEntryType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DefaultStylePropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;[complexType of] Top-level property. Used in application schemas to "attach" the styling information to GML data. The link between the data and the style should be established through this property only.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element minOccurs="0" ref="gml:_Style"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attribute name="about" type="anyURI" use="optional"/&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DEFAULTSTYLEPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DefaultStylePropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" mixed="true" name="AbstractMetaDataType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;An abstract base type for complex metadata types.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;attribute ref="gml:id" use="optional"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTMETADATATYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractMetaDataType"), Collections.EMPTY_LIST,
            false, true, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType mixed="true" name="GenericMetaDataType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Deprecated with GML version 3.1.0.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent mixed="true"&gt;
     *          &lt;extension base="gml:AbstractMetaDataType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;any maxOccurs="unbounded" minOccurs="0" processContents="lax"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GENERICMETADATATYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GenericMetaDataType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTMETADATATYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="VerticalCRSRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a vertical coordinate reference system, either referencing or containing the definition of that reference system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:VerticalCRS"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType VERTICALCRSREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "VerticalCRSRefType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="VerticalCSRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a vertical coordinate system, either referencing or containing the definition of that coordinate system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:VerticalCS"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType VERTICALCSREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "VerticalCSRefType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="OperationMethodRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a concrete general-purpose operation method, either referencing or containing the definition of that method. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:OperationMethod"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType OPERATIONMETHODREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "OperationMethodRefType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="NodeType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Its optional co-boundary is a set of connected directedEdges.  The orientation of one of these dirEdges is "+" if the Node is the "to" node of the Edge, and "-" if it is the "from" node.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTopoPrimitiveType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:directedEdge"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:pointProperty"/&gt;
     *                  &lt;!-- &lt;element name="geometry" type="gml:PointPropertyType" minOccurs="0"/&gt; --&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType NODETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "NodeType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, ABSTRACTTOPOPRIMITIVETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GeographicCRSRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a geographic coordinate reference system, either referencing or containing the definition of that reference system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:GeographicCRS"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GEOGRAPHICCRSREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GeographicCRSRefType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="FaceType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;. The topological boundary of a face consists of a set of directed edges. Note that all edges associated with a Face, including dangling and interior edges, appear in the boundary.  Dangling and interior edges are each referenced by pairs of directedEdges with opposing orientations.  The optional coboundary of a face is a pair of directed solids which are bounded by this face. If present, there is precisely one positively directed and one negatively directed solid in the coboundary of every face. The positively directed solid corresponds to the solid which lies in the direction of the positively directed normal to the face in any geometric realisation.  A face may optionally be realised by a 2-dimensional (surface) geometric primitive.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTopoPrimitiveType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" ref="gml:directedEdge"/&gt;
     *                  &lt;element maxOccurs="2" minOccurs="0" ref="gml:directedTopoSolid"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:surfaceProperty"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType FACETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "FaceType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, ABSTRACTTOPOPRIMITIVETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="SphericalCSType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A three-dimensional coordinate system with one distance measured from the origin and two angular coordinates. Not to be confused with an ellipsoidal coordinate system based on an ellipsoid "degenerated" into a sphere. A SphericalCS shall have three usesAxis associations. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCoordinateSystemType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType SPHERICALCSTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "SphericalCSType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTCOORDINATESYSTEMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GridType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;An unrectified grid, which is a network composed of two or more sets of equally spaced parallel lines in which the members of each set intersect the members of the other sets at right angles.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGeometryType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element name="limits" type="gml:GridLimitsType"/&gt;
     *                  &lt;element maxOccurs="unbounded" name="axisName" type="string"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute name="dimension" type="positiveInteger" use="required"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GRIDTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GridType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, ABSTRACTGEOMETRYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="RectifiedGridType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A rectified grid has an origin and vectors that define its post locations.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:GridType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element name="origin" type="gml:PointPropertyType"/&gt;
     *                  &lt;element maxOccurs="unbounded" name="offsetVector" type="gml:VectorType"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType RECTIFIEDGRIDTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "RectifiedGridType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, GRIDTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GeocentricCRSRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a geocentric coordinate reference system, either referencing or containing the definition of that reference system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:GeocentricCRS"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GEOCENTRICCRSREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GeocentricCRSRefType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType final="#all" name="TimeClockType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;A clock provides a basis for defining temporal position within a day.
     *        A clock must be used with a calendar in order to provide a complete description of a temporal position
     *        within a specific day.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTimeReferenceSystemType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element name="referenceEvent" type="gml:StringOrRefType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Name or description of an event, such as solar noon or sunrise,
     *                which fixes the position of the base scale of the clock.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element name="referenceTime" type="time"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;time of day associated with the reference event expressed as
     *                a time of day in the given clock. The reference time is usually the origin of the clock scale.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element name="utcReference" type="time"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;24 hour local or UTC time that corresponds to the reference time.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0"
     *                      name="dateBasis" type="gml:TimeCalendarPropertyType"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMECLOCKTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeClockType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTTIMEREFERENCESYSTEMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="BSplineType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A B-Spline is a piecewise parametric polynomial or rational curve described in terms of control points and basis functions. Knots are breakpoints on the curve that connect its pieces. They are given as a non-decreasing sequence of real numbers. If the weights in the knots are equal then it is a polynomial spline. The degree is the algebraic degree of the basis functions.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCurveSegmentType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;choice&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;GML supports two different ways to specify the control points of a curve segment.
     *  1. A sequence of "pos" (DirectPositionType) or "pointProperty" (PointPropertyType) elements. "pos" elements are control points that are only part of this curve segment, "pointProperty" elements contain a point that may be referenced from other geometry elements or reference another point defined outside of this curve segment (reuse of existing points).
     *  2. The "posList" element allows for a compact way to specifiy the coordinates of the control points, if all control points are in the same coordinate reference systems and belong to this curve segment only.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                      &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
     *                          &lt;element ref="gml:pos"/&gt;
     *                          &lt;element ref="gml:pointProperty"/&gt;
     *                          &lt;element ref="gml:pointRep"&gt;
     *                              &lt;annotation&gt;
     *                                  &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "pointProperty" instead. Included for backwards compatibility with GML 3.0.0.&lt;/documentation&gt;
     *                              &lt;/annotation&gt;
     *                          &lt;/element&gt;
     *                      &lt;/choice&gt;
     *                      &lt;element ref="gml:posList"/&gt;
     *                      &lt;element ref="gml:coordinates"&gt;
     *                          &lt;annotation&gt;
     *                              &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "posList" instead.&lt;/documentation&gt;
     *                          &lt;/annotation&gt;
     *                      &lt;/element&gt;
     *                  &lt;/choice&gt;
     *                  &lt;element name="degree" type="nonNegativeInteger"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;The attribute "degree" shall be the degree of the polynomial used for interpolation in this spline.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="2" name="knot" type="gml:KnotPropertyType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;The property "knot" shall be the sequence of distinct knots used to define the spline basis functions.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute default="polynomialSpline" name="interpolation" type="gml:CurveInterpolationType"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;The attribute "interpolation" specifies the curve interpolation mechanism used for this segment. This mechanism
     *  uses the control points and control parameters to determine the position of this curve segment. For a BSpline the interpolation can be either "polynomialSpline" or "rationalSpline", default is "polynomialSpline".&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *              &lt;attribute name="isPolynomial" type="boolean" use="optional"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;The attribute isPolynomial is set to true if this is a polynomial spline.&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *              &lt;attribute name="knotType" type="gml:KnotTypesType" use="optional"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;The attribute "knotType" gives the type of knot distribution used in defining this spline. This is for information only
     *  and is set according to the different construction-functions.&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType BSPLINETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "BSplineType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, ABSTRACTCURVESEGMENTTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="BezierType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Bezier curves are polynomial splines that use Bezier or Bernstein polynomials for interpolation purposes. It is a special case of the B-Spline curve with two knots.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:BSplineType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;choice&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;GML supports two different ways to specify the control points of a curve segment.
     *  1. A sequence of "pos" (DirectPositionType) or "pointProperty" (PointPropertyType) elements. "pos" elements are control points that are only part of this curve segment, "pointProperty" elements contain a point that may be referenced from other geometry elements or reference another point defined outside of this curve segment (reuse of existing points).
     *  2. The "posList" element allows for a compact way to specifiy the coordinates of the control points, if all control points are in the same coordinate reference systems and belong to this curve segment only.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                      &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
     *                          &lt;element ref="gml:pos"/&gt;
     *                          &lt;element ref="gml:pointProperty"/&gt;
     *                          &lt;element ref="gml:pointRep"&gt;
     *                              &lt;annotation&gt;
     *                                  &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "pointProperty" instead. Included for backwards compatibility with GML 3.0.0.&lt;/documentation&gt;
     *                              &lt;/annotation&gt;
     *                          &lt;/element&gt;
     *                      &lt;/choice&gt;
     *                      &lt;element ref="gml:posList"/&gt;
     *                      &lt;element ref="gml:coordinates"&gt;
     *                          &lt;annotation&gt;
     *                              &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "posList" instead.&lt;/documentation&gt;
     *                          &lt;/annotation&gt;
     *                      &lt;/element&gt;
     *                  &lt;/choice&gt;
     *                  &lt;element name="degree" type="nonNegativeInteger"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;The attribute "degree" shall be the degree of the polynomial used for interpolation in this spline.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element maxOccurs="2" minOccurs="2" name="knot" type="gml:KnotPropertyType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;The property "knot" shall be the sequence of distinct knots used to define the spline basis functions.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute fixed="polynomialSpline" name="interpolation" type="gml:CurveInterpolationType"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;The attribute "interpolation" specifies the curve interpolation mechanism used for this segment. This mechanism
     *  uses the control points and control parameters to determine the position of this curve segment. For a Bezier the interpolation is fixed as "polynomialSpline".&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *              &lt;attribute fixed="true" name="isPolynomial" type="boolean"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;The attribute isPolynomial is set to true as this is a polynomial spline.&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *              &lt;attribute name="knotType" type="gml:KnotTypesType" use="prohibited"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;The property "knotType" is not relevant for Bezier curve segments.&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType BEZIERTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "BezierType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, BSPLINETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="_isSphere"&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="sphere"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType _ISSPHERE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "_isSphere"), java.lang.Object.class, false, false,
            Collections.EMPTY_SET, XSSchema.STRING_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DatumRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a datum, either referencing or containing the definition of that datum. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:_Datum"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DATUMREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DatumRefType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MultiSurfacePropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A property that has a collection of surfaces as its value domain can either be an appropriate geometry element encapsulated in an element of this type or an XLink reference to a remote geometry element (where remote includes geometry elements located elsewhere in the same document). Either the reference or the contained element must be given, but neither both nor none.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:MultiSurface"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"&gt;
     *          &lt;annotation&gt;
     *              &lt;documentation&gt;This attribute group includes the XLink attributes (see xlinks.xsd). XLink is used in GML to reference remote resources (including those elsewhere in the same document). A simple link element can be constructed by including a specific set of XLink attributes. The XML Linking Language (XLink) is currently a Proposed Recommendation of the World Wide Web Consortium. XLink allows elements to be inserted into XML documents so as to create sophisticated links between resources; such links can be used to reference remote properties.
     *  A simple link element can be used to implement pointer functionality, and this functionality has been built into various GML 3 elements by including the gml:AssociationAttributeGroup.&lt;/documentation&gt;
     *          &lt;/annotation&gt;
     *      &lt;/attributeGroup&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType MULTISURFACEPROPERTYTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MultiSurfacePropertyType"), MultiPolygon.class,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ConeType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A cone is a gridded surface given as a
     *     family of conic sections whose control points vary linearly.
     *     NOTE! A 5-point ellipse with all defining positions identical
     *     is a point. Thus, a truncated elliptical cone can be given as a
     *     2x5 set of control points
     *     ((P1, P1, P1, P1, P1), (P2, P3, P4, P5, P6)). P1 is the apex
     *     of the cone. P2, P3,P4, P5 and P6 are any five distinct points
     *     around the base ellipse of the cone. If the horizontal curves
     *     are circles as opposed to ellipses, the a circular cone can
     *     be constructed using ((P1, P1, P1),(P2, P3, P4)). The apex most
     *     not coinside with the other plane.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGriddedSurfaceType"&gt;
     *              &lt;attribute fixed="circularArc3Points"
     *                  name="horizontalCurveType" type="gml:CurveInterpolationType"/&gt;
     *              &lt;attribute fixed="linear" name="verticalCurveType" type="gml:CurveInterpolationType"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CONETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ConeType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, ABSTRACTGRIDDEDSURFACETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CompositeCurveType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A CompositeCurve is defined by a sequence of (orientable) curves such that the each curve in the sequence terminates at the start point of the subsequent curve in the list.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCurveType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" ref="gml:curveMember"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;This element references or contains one curve in the composite curve. The curves are contiguous, the collection of curves is ordered.
     *  NOTE: This definition allows for a nested structure, i.e. a CompositeCurve may use, for example, another CompositeCurve as a curve member.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType COMPOSITECURVETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CompositeCurveType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTCURVETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ObservationType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractFeatureType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:validTime"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:using"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:target"/&gt;
     *                  &lt;element ref="gml:resultOf"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType OBSERVATIONTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ObservationType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTFEATURETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DirectedObservationType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:ObservationType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:direction"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DIRECTEDOBSERVATIONTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DirectedObservationType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, OBSERVATIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DirectedObservationAtDistanceType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:DirectedObservationType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element name="distance" type="gml:MeasureType"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DIRECTEDOBSERVATIONATDISTANCETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DirectedObservationAtDistanceType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET,
            DIRECTEDOBSERVATIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="doubleOrNull"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Union of the XML Schema double type and the GML Nulltype.  An element which uses this type may have content which is either a double or a value from Nulltype&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;union memberTypes="gml:NullEnumeration double anyURI"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType DOUBLEORNULL_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "doubleOrNull"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, XSSchema.ANYSIMPLETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ContainerPropertyType"&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;choice&gt;
     *              &lt;element ref="gml:Face"/&gt;
     *              &lt;element ref="gml:TopoSolid"/&gt;
     *          &lt;/choice&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CONTAINERPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ContainerPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="AesheticCriteriaType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Graph-specific styling property.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="MIN_CROSSINGS"/&gt;
     *          &lt;enumeration value="MIN_AREA"/&gt;
     *          &lt;enumeration value="MIN_BENDS"/&gt;
     *          &lt;enumeration value="MAX_BENDS"/&gt;
     *          &lt;enumeration value="UNIFORM_BENDS"/&gt;
     *          &lt;enumeration value="MIN_SLOPES"/&gt;
     *          &lt;enumeration value="MIN_EDGE_LENGTH"/&gt;
     *          &lt;enumeration value="MAX_EDGE_LENGTH"/&gt;
     *          &lt;enumeration value="UNIFORM_EDGE_LENGTH"/&gt;
     *          &lt;enumeration value="MAX_ANGULAR_RESOLUTION"/&gt;
     *          &lt;enumeration value="MIN_ASPECT_RATIO"/&gt;
     *          &lt;enumeration value="MAX_SYMMETRIES"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType AESHETICCRITERIATYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AesheticCriteriaType"), java.lang.Object.class,
            false, false, Collections.EMPTY_SET, XSSchema.STRING_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="OffsetCurveType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;An offset curve is a curve at a constant
     *                   distance from the basis curve. They can be useful as a cheap
     *                   and simple alternative to constructing curves that are offsets
     *                   by definition.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCurveSegmentType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element name="offsetBase" type="gml:CurvePropertyType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;offsetBase is a reference to thecurve from which this
     *                                                           curve is define        as an offset.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element name="distance" type="gml:LengthType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;distance is the distance at which the
     *                                                           offset curve is generated from the basis curve. In 2D systems, positive distances
     *                                                           are to be to the left of the basis curve, and the negative distances are to be to the
     *                                                           right of the basis curve.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element minOccurs="0" name="refDirection" type="gml:VectorType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;refDistance is used to define the vector
     *         direction of the offset curve from the basis curve. It can
     *         be omitted in the 2D case, where the distance can be
     *         positive or negative. In that case, distance defines left
     *         side (positive distance) or right side (negative distance)
     *         with respect to the tangent to the basis curve.
     *
     *         In 3D the basis curve shall have a well defined tangent
     *         direction for every point. The offset curve at any point
     *         in 3D, the basis curve shall have a well-defined tangent
     *         direction for every point. The offset curve at any point
     *         (parameter) on the basis curve c is in the direction
     *         -   -   -         -
     *         s = v x t  where  v = c.refDirection()
     *         and
     *         -
     *         t = c.tangent()
     *                                                      -
     *         For the offset direction to be well-defined, v shall not
     *         on any point of the curve be in the same, or opposite,
     *         direction as
     *         -
     *         t.
     *
     *         The default value of the refDirection shall be the local
     *         co-ordinate axis vector for elevation, which indicates up for
     *         the curve in a geographic sense.
     *
     *         NOTE! If the refDirection is the positive tangent to the
     *         local elevation axis ("points upward"), then the offset
     *         vector points to the left of the curve when viewed from
     *         above.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType OFFSETCURVETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "OffsetCurveType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTCURVESEGMENTTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CompoundCRSType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A coordinate reference system describing the position of points through two or more independent coordinate reference systems. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractReferenceSystemType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="2" ref="gml:includesCRS"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Ordered sequence of associations to all the component coordinate reference systems included in this compound coordinate reference system. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType COMPOUNDCRSTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CompoundCRSType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTREFERENCESYSTEMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ImageDatumRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to an image datum, either referencing or containing the definition of that datum. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:ImageDatum"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType IMAGEDATUMREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ImageDatumRefType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="VerticalDatumRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a vertical datum, either referencing or containing the definition of that datum. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:VerticalDatum"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType VERTICALDATUMREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "VerticalDatumRefType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="CalDate"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;Calendar dates may be indicated with varying degrees of precision,
     *        using year, year-month, date.
     *        When used with non-Gregorian calendars based on years, months, days,
     *        the same lexical representation should still be used, with leading zeros added if the
     *        year value would otherwise have fewer than four digits.
     *        time is used for a position that recurs daily (see clause 5.4.4.2 of ISO 19108:2002).&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;union memberTypes="date gYearMonth gYear"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType CALDATE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CalDate"), java.lang.Object.class, false, false,
            Collections.EMPTY_SET, XSSchema.ANYSIMPLETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimeCalendarType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;A calendar is a discrete temporal reference system
     *        that provides a basis for defining temporal position to a resolution of one day.
     *        A single calendar may reference more than one calendar era.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTimeReferenceSystemType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" name="referenceFrame" type="gml:TimeCalendarEraPropertyType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Link to the CalendarEras that it uses as a reference for dating.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMECALENDARTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeCalendarType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTTIMEREFERENCESYSTEMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CylinderType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A cylinder is a gridded surface given as a
     *     family of circles whose positions vary along a set of parallel
     *     lines, keeping the cross sectional horizontal curves of a
     *     constant shape.
     *     NOTE! Given the same working assumptions as in the previous
     *     note, a Cylinder can be given by two circles, giving us the
     *     control points of the form ((P1, P2, P3),(P4, P5, P6)).&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGriddedSurfaceType"&gt;
     *              &lt;attribute fixed="circularArc3Points"
     *                  name="horizontalCurveType" type="gml:CurveInterpolationType"/&gt;
     *              &lt;attribute fixed="linear" name="verticalCurveType" type="gml:CurveInterpolationType"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CYLINDERTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CylinderType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTGRIDDEDSURFACETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="OperationMethodBaseType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Basic encoding for operation method objects, simplifying and restricting the DefinitionType as needed. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:DefinitionType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:metaDataProperty"/&gt;
     *                  &lt;element ref="gml:methodName"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute ref="gml:id" use="required"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType OPERATIONMETHODBASETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "OperationMethodBaseType"), Collections.EMPTY_LIST,
            false, true, Collections.EMPTY_SET, DEFINITIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="OperationMethodType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Definition of an algorithm used to perform a coordinate operation. Most operation methods use a number of operation parameters, although some coordinate conversions use none. Each coordinate operation using the method assigns values to these parameters. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:OperationMethodBaseType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:methodID"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Set of alternative identifications of this operation method. The first methodID, if any, is normally the primary identification code, and any others are aliases. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element minOccurs="0" ref="gml:remarks"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Comments on or information about this operation method, including source information.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element ref="gml:methodFormula"/&gt;
     *                  &lt;element ref="gml:sourceDimensions"/&gt;
     *                  &lt;element ref="gml:targetDimensions"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:usesParameter"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Unordered list of associations to the set of operation parameters and parameter groups used by this operation method. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType OPERATIONMETHODTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "OperationMethodType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, OPERATIONMETHODBASETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ArcStringType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;An ArcString is a curve segment that uses three-point circular arc interpolation.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCurveSegmentType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;choice&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;GML supports two different ways to specify the control points of a curve segment.
     *  1. A sequence of "pos" (DirectPositionType) or "pointProperty" (PointPropertyType) elements. "pos" elements are control points that are only part of this curve segment, "pointProperty" elements contain a point that may be referenced from other geometry elements or reference another point defined outside of this curve segment (reuse of existing points).
     *  2. The "posList" element allows for a compact way to specifiy the coordinates of the control points, if all control points are in the same coordinate reference systems and belong to this curve segment only. The number of direct positions in the list must be at least three.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                      &lt;choice maxOccurs="unbounded" minOccurs="3"&gt;
     *                          &lt;element ref="gml:pos"/&gt;
     *                          &lt;element ref="gml:pointProperty"/&gt;
     *                          &lt;element ref="gml:pointRep"&gt;
     *                              &lt;annotation&gt;
     *                                  &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "pointProperty" instead. Included for backwards compatibility with GML 3.0.0.&lt;/documentation&gt;
     *                              &lt;/annotation&gt;
     *                          &lt;/element&gt;
     *                      &lt;/choice&gt;
     *                      &lt;element ref="gml:posList"/&gt;
     *                      &lt;element ref="gml:coordinates"&gt;
     *                          &lt;annotation&gt;
     *                              &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "posList" instead.&lt;/documentation&gt;
     *                          &lt;/annotation&gt;
     *                      &lt;/element&gt;
     *                  &lt;/choice&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute fixed="circularArc3Points" name="interpolation" type="gml:CurveInterpolationType"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;The attribute "interpolation" specifies the curve interpolation mechanism used for this segment. This mechanism
     *  uses the control points and control parameters to determine the position of this curve segment. For an ArcString the interpolation is fixed as "circularArc3Points".&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *              &lt;attribute name="numArc" type="integer" use="optional"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;The number of arcs in the arc string can be explicitly stated in this attribute. The number of control points in the arc string must be 2 * numArc + 1.&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ARCSTRINGTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ArcStringType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTCURVESEGMENTTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ArcType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;An Arc is an arc string with only one arc unit, i.e. three control points.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:ArcStringType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;choice&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;GML supports two different ways to specify the control points of a curve segment.
     *  1. A sequence of "pos" (DirectPositionType) or "pointProperty" (PointPropertyType) elements. "pos" elements are control points that are only part of this curve segment, "pointProperty" elements contain a point that may be referenced from other geometry elements or reference another point defined outside of this curve segment (reuse of existing points).
     *  2. The "posList" element allows for a compact way to specifiy the coordinates of the control points, if all control points are in the same coordinate reference systems and belong to this curve segment only. The number of direct positions in the list must be three.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                      &lt;choice maxOccurs="3" minOccurs="3"&gt;
     *                          &lt;element ref="gml:pos"/&gt;
     *                          &lt;element ref="gml:pointProperty"/&gt;
     *                          &lt;element ref="gml:pointRep"&gt;
     *                              &lt;annotation&gt;
     *                                  &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "pointProperty" instead. Included for backwards compatibility with GML 3.0.0.&lt;/documentation&gt;
     *                              &lt;/annotation&gt;
     *                          &lt;/element&gt;
     *                      &lt;/choice&gt;
     *                      &lt;element ref="gml:posList"/&gt;
     *                      &lt;element ref="gml:coordinates"&gt;
     *                          &lt;annotation&gt;
     *                              &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "posList" instead.&lt;/documentation&gt;
     *                          &lt;/annotation&gt;
     *                      &lt;/element&gt;
     *                  &lt;/choice&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute fixed="1" name="numArc" type="integer" use="optional"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;An arc is an arc string consiting of a single arc, the attribute is fixed to "1".&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ARCTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ArcType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, ARCSTRINGTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CircleType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A Circle is an arc whose ends coincide to form a simple closed loop. The "start" and "end" bearing are equal and shall be the bearing for the first controlPoint listed. The three control points must be distinct non-co-linear points for the Circle to be unambiguously defined. The arc is simply extended past the third control point until the first control point is encountered.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:ArcType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CIRCLETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CircleType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, ARCTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="doubleList"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;XML List based on XML Schema double type.  An element of this type contains a space-separated list of double values&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;list itemType="double"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType DOUBLELIST_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "doubleList"), java.lang.Object.class, false, false,
            Collections.EMPTY_SET, XSSchema.ANYSIMPLETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DirectPositionListType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;DirectPositionList instances hold the coordinates for a sequence of direct positions within the same coordinate
     *                          reference system (CRS).&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:doubleList"&gt;
     *              &lt;attributeGroup ref="gml:SRSReferenceGroup"/&gt;
     *              &lt;attribute name="count" type="positiveInteger" use="optional"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;"count" allows to specify the number of direct positions in the list. If the attribute count is present then
     *                                                  the attribute srsDimension shall be present, too.&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DIRECTPOSITIONLISTTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DirectPositionListType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, DOUBLELIST_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="UserDefinedCSType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A two- or three-dimensional coordinate system that consists of any combination of coordinate axes not covered by any other coordinate system type. An example is a multilinear coordinate system which contains one coordinate axis that may have any 1-D shape which has no intersections with itself. This non-straight axis is supplemented by one or two straight axes to complete a 2 or 3 dimensional coordinate system. The non-straight axis is typically incrementally straight or curved. A UserDefinedCS shall have two or three usesAxis associations. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCoordinateSystemType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType USERDEFINEDCSTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "UserDefinedCSType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTCOORDINATESYSTEMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TopoSurfaceType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;The TopoSurface type and element represent a homogeneous topological expression, a set of directed faces, which if realised are isomorphic to a geometric surface primitive. The intended use of TopoSurface is to appear within a surface feature instance to express the structural and possibly geometric relationships of this surface to other features via the shared face definitions.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTopologyType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" ref="gml:directedFace"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TOPOSURFACETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TopoSurfaceType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTTOPOLOGYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CurvePropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A property that has a curve as its value domain can either be an appropriate geometry element encapsulated in an
     *                          element of this type or an XLink reference to a remote geometry element (where remote includes geometry elements located elsewhere
     *                          in the same document). Either the reference or the contained element must be given, but neither both nor none.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:_Curve"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"&gt;
     *          &lt;annotation&gt;
     *              &lt;documentation&gt;This attribute group includes the XLink attributes (see xlinks.xsd). XLink is used in GML to reference remote
     *                                  resources (including those elsewhere in the same document). A simple link element can be constructed by including a specific
     *                                  set of XLink attributes. The XML Linking Language (XLink) is currently a Proposed Recommendation of the World Wide Web Consortium.
     *                                  XLink allows elements to be inserted into XML documents so as to create sophisticated links between resources; such links can be used
     *                                  to reference remote properties. A simple link element can be used to implement pointer functionality, and this functionality has been built
     *                                  into various GML 3 elements by including the gml:AssociationAttributeGroup.&lt;/documentation&gt;
     *          &lt;/annotation&gt;
     *      &lt;/attributeGroup&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType CURVEPROPERTYTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CurvePropertyType"), MultiLineString.class, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ConcatenatedOperationType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;An ordered sequence of two or more single coordinate operations. The sequence of operations is constrained by the requirement that the source coordinate reference system of step (n+1) must be the same as the target coordinate reference system of step (n). The source coordinate reference system of the first step and the target coordinate reference system of the last step are the source and target coordinate reference system associated with the concatenated operation. Instead of a forward operation, an inverse operation may be used for one or more of the operation steps mentioned above, if the inverse operation is uniquely defined by the forward operation.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCoordinateOperationType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="2" ref="gml:usesSingleOperation"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Ordered sequence of associations to the two or more single operations used by this concatenated operation. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CONCATENATEDOPERATIONTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ConcatenatedOperationType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTCOORDINATEOPERATIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="OrientableSurfaceType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;OrientableSurface consists of a surface and an orientation. If the orientation is "+", then the OrientableSurface is identical to the baseSurface. If the orientation is "-", then the OrientableSurface is a reference to a Surface with an up-normal that reverses the direction for this OrientableSurface, the sense of "the top of the surface".&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractSurfaceType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:baseSurface"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;References or contains the base surface (positive orientation).&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute default="+" name="orientation" type="gml:SignType"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;If the orientation is "+", then the OrientableSurface is identical to the baseSurface. If the orientation is "-", then the OrientableSurface is a reference to a Surface with an up-normal that reverses the direction for this OrientableSurface, the sense of "the top of the surface". "+" is the default value.&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ORIENTABLESURFACETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "OrientableSurfaceType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTSURFACETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractTimeComplexType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;The abstract supertype for temporal complexes.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTimeObjectType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTTIMECOMPLEXTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractTimeComplexType"), Collections.EMPTY_LIST,
            false, true, Collections.EMPTY_SET, ABSTRACTTIMEOBJECTTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimeTopologyComplexType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;A temporal topology complex.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTimeComplexType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" name="primitive" type="gml:TimeTopologyPrimitivePropertyType"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMETOPOLOGYCOMPLEXTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeTopologyComplexType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTTIMECOMPLEXTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GridCoverageType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:AbstractDiscreteCoverageType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;group ref="gml:StandardObjectProperties"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:boundedBy"/&gt;
     *                  &lt;element ref="gml:gridDomain"/&gt;
     *                  &lt;element ref="gml:rangeSet"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:coverageFunction"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GRIDCOVERAGETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GridCoverageType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTDISCRETECOVERAGETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CoordinateOperationRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a coordinate operation, either referencing or containing the definition of that coordinate operation. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:_CoordinateOperation"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType COORDINATEOPERATIONREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CoordinateOperationRefType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="EnvelopeType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Envelope defines an extent using a pair of positions defining opposite corners in arbitrary dimensions. The first direct
     *                          position is the "lower corner" (a coordinate position consisting of all the minimal ordinates for each dimension for all points within the envelope),
     *                          the second one the "upper corner" (a coordinate position consisting of all the maximal ordinates for each dimension for all points within the
     *                          envelope).&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;choice&gt;
     *          &lt;sequence&gt;
     *              &lt;element name="lowerCorner" type="gml:DirectPositionType"/&gt;
     *              &lt;element name="upperCorner" type="gml:DirectPositionType"/&gt;
     *          &lt;/sequence&gt;
     *          &lt;element maxOccurs="2" minOccurs="2" ref="gml:coord"&gt;
     *              &lt;annotation&gt;
     *                  &lt;appinfo&gt;deprecated&lt;/appinfo&gt;
     *                  &lt;documentation&gt;deprecated with GML version 3.0&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *          &lt;element maxOccurs="2" minOccurs="2" ref="gml:pos"&gt;
     *              &lt;annotation&gt;
     *                  &lt;appinfo&gt;deprecated&lt;/appinfo&gt;
     *                  &lt;documentation&gt;Deprecated with GML version 3.1. Use the explicit properties "lowerCorner" and "upperCorner" instead.&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *          &lt;element ref="gml:coordinates"&gt;
     *              &lt;annotation&gt;
     *                  &lt;documentation&gt;Deprecated with GML version 3.1.0. Use the explicit properties "lowerCorner" and "upperCorner" instead.&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *      &lt;/choice&gt;
     *      &lt;attributeGroup ref="gml:SRSReferenceGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ENVELOPETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "EnvelopeType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="EnvelopeWithTimePeriodType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Envelope that includes also a temporal extent.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:EnvelopeType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="2" minOccurs="2" ref="gml:timePosition"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute default="#ISO-8601" name="frame" type="anyURI" use="optional"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ENVELOPEWITHTIMEPERIODTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "EnvelopeWithTimePeriodType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, ENVELOPETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="BooleanPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Property whose content is a Boolean value.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:ValuePropertyType"&gt;
     *              &lt;sequence minOccurs="0"&gt;
     *                  &lt;element ref="gml:Boolean"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType BOOLEANPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "BooleanPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, VALUEPROPERTYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CoordType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Represents a coordinate tuple in one, two, or three dimensions. Deprecated with GML 3.0 and replaced by
     *                          DirectPositionType.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element name="X" type="decimal"/&gt;
     *          &lt;element minOccurs="0" name="Y" type="decimal"/&gt;
     *          &lt;element minOccurs="0" name="Z" type="decimal"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType COORDTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CoordType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MultiSurfaceCoverageType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A discrete coverage type whose domain is defined by a collection of surface patches (includes polygons, triangles, rectangles, etc).&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:AbstractDiscreteCoverageType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;group ref="gml:StandardObjectProperties"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:boundedBy"/&gt;
     *                  &lt;element ref="gml:multiSurfaceDomain"/&gt;
     *                  &lt;element ref="gml:rangeSet"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:coverageFunction"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType MULTISURFACECOVERAGETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MultiSurfaceCoverageType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTDISCRETECOVERAGETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GridEnvelopeType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Provides grid coordinate values for the diametrically opposed corners of an envelope that bounds a section of grid. The value of a single coordinate is the number of offsets from the origin of the grid in the direction of a specific axis.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element name="low" type="gml:integerList"/&gt;
     *          &lt;element name="high" type="gml:integerList"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GRIDENVELOPETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GridEnvelopeType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TemporalCSRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a temporal coordinate system, either referencing or containing the definition of that coordinate system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:TemporalCS"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TEMPORALCSREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TemporalCSRefType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="LineStringSegmentType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A LineStringSegment is a curve segment that is defined by two or more coordinate tuples, with linear interpolation between them.
     *                                  Note: LineStringSegment implements GM_LineString of ISO 19107.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCurveSegmentType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;choice&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;GML supports two different ways to specify the control points of a curve segment.
     *  1. A sequence of "pos" (DirectPositionType) or "pointProperty" (PointPropertyType) elements. "pos" elements are control points that are only part of this curve segment, "pointProperty" elements contain a point that may be referenced from other geometry elements or reference another point defined outside of this curve segment (reuse of existing points).
     *  2. The "posList" element allows for a compact way to specifiy the coordinates of the control points, if all control points are in the same coordinate reference systems and belong to this curve segment only. The number of direct positions in the list must be at least two.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                      &lt;choice maxOccurs="unbounded" minOccurs="2"&gt;
     *                          &lt;element ref="gml:pos"/&gt;
     *                          &lt;element ref="gml:pointProperty"/&gt;
     *                          &lt;element ref="gml:pointRep"&gt;
     *                              &lt;annotation&gt;
     *                                  &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "pointProperty" instead. Included for backwards compatibility with GML 3.0.0.&lt;/documentation&gt;
     *                              &lt;/annotation&gt;
     *                          &lt;/element&gt;
     *                      &lt;/choice&gt;
     *                      &lt;element ref="gml:posList"/&gt;
     *                      &lt;element ref="gml:coordinates"&gt;
     *                          &lt;annotation&gt;
     *                              &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "posList" instead.&lt;/documentation&gt;
     *                          &lt;/annotation&gt;
     *                      &lt;/element&gt;
     *                  &lt;/choice&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute fixed="linear" name="interpolation" type="gml:CurveInterpolationType"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;The attribute "interpolation" specifies the curve interpolation mechanism used for this segment. This mechanism
     *  uses the control points and control parameters to determine the position of this curve segment. For a LineStringSegment the interpolation is fixed as "linear".&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType LINESTRINGSEGMENTTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "LineStringSegmentType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTCURVESEGMENTTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="FeatureArrayPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Container for features - follow gml:ArrayAssociationType pattern.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:_Feature"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType FEATUREARRAYPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "FeatureArrayPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="SingleOperationRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a single operation, either referencing or containing the definition of that single operation. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:_SingleOperation"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType SINGLEOPERATIONREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "SingleOperationRefType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DirectedFacePropertyType"&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:Face"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attribute default="+" name="orientation" type="gml:SignType"/&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DIRECTEDFACEPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DirectedFacePropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ValueArrayPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;GML property which refers to, or contains, a set of homogeneously typed Values.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;group maxOccurs="unbounded" ref="gml:Value"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType VALUEARRAYPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ValueArrayPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ReferenceSystemRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a reference system, either referencing or containing the definition of that reference system.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:_ReferenceSystem"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType REFERENCESYSTEMREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ReferenceSystemRefType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimeCalendarEraType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;In every calendar, years are numbered relative to the date of a
     *        reference event that defines a calendar era.
     *        In this implementation, we omit the back-pointer "datingSystem".&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:DefinitionType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element name="referenceEvent" type="gml:StringOrRefType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Name or description of a mythical or historic event which fixes the position of the base scale of the calendar era.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element default="0001-01-01" minOccurs="0"
     *                      name="referenceDate" type="date"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Date of the referenceEvent expressed as a date in the given calendar.
     *                In most calendars, this date is the origin (i.e., the first day) of the scale, but this is not always true.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element name="julianReference" type="decimal"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Julian date that corresponds to the reference date.
     *                The Julian day numbering system is a temporal coordinate system that has an
     *                origin earlier than any known calendar,
     *                at noon on 1 January 4713 BC in the Julian proleptic calendar.
     *                The Julian day number is an integer value;
     *                the Julian date is a decimal value that allows greater resolution.
     *                Transforming calendar dates to and from Julian dates provides a
     *                relatively simple basis for transforming dates from one calendar to another.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element name="epochOfUse" type="gml:TimePeriodPropertyType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Period for which the calendar era was used as a basis for dating.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMECALENDARERATYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeCalendarEraType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, DEFINITIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="VerticalCRSType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A 1D coordinate reference system used for recording heights or depths. Vertical CRSs make use of the direction of gravity to define the concept of height or depth, but the relationship with gravity may not be straightforward. By implication, ellipsoidal heights (h) cannot be captured in a vertical coordinate reference system. Ellipsoidal heights cannot exist independently, but only as an inseparable part of a 3D coordinate tuple defined in a geographic 3D coordinate reference system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractReferenceSystemType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:usesVerticalCS"/&gt;
     *                  &lt;element ref="gml:usesVerticalDatum"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType VERTICALCRSTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "VerticalCRSType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTREFERENCESYSTEMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CurveArrayPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A container for an array of curves. The elements are always contained in the array property, referencing geometry elements
     *                          or arrays of geometry elements is not supported.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:_Curve"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CURVEARRAYPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CurveArrayPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ParameterValueGroupType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A group of related parameter values. The same group can be repeated more than once in a Conversion, Transformation, or higher level parameterValueGroup, if those instances contain different values of one or more parameterValues which suitably distinquish among those groups. This concrete complexType can be used for operation methods without using an Application Schema that defines operation-method-specialized element names and contents, especially for methods with only one instance. This complexType can be used, extended, or restricted for well-known operation methods, especially for methods with many instances. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGeneralParameterValueType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="2" ref="gml:includesValue"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Unordered set of composition associations to the parameter values and groups of values included in this group. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element ref="gml:valuesOfGroup"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType PARAMETERVALUEGROUPTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ParameterValueGroupType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTGENERALPARAMETERVALUETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MultiSolidCoverageType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A discrete coverage type whose domain is defined by a collection of Solids.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:AbstractDiscreteCoverageType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;group ref="gml:StandardObjectProperties"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:boundedBy"/&gt;
     *                  &lt;element ref="gml:multiSolidDomain"/&gt;
     *                  &lt;element ref="gml:rangeSet"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:coverageFunction"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType MULTISOLIDCOVERAGETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MultiSolidCoverageType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTDISCRETECOVERAGETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="LinearRingPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Encapsulates a ring to represent properties in features or geometry collections.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;choice&gt;
     *          &lt;element ref="gml:LinearRing"/&gt;
     *      &lt;/choice&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType LINEARRINGPROPERTYTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "LinearRingPropertyType"), LinearRing.class, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MultiSurfaceType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A MultiSurface is defined by one or more Surfaces, referenced through surfaceMember elements.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGeometricAggregateType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;The members of the geometric aggregate can be specified either using the "standard" property or the array property style. It is also valid to use both the "standard" and the array property style in the same collection.
     *  NOTE: Array properties cannot reference remote geometry elements.&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:surfaceMember"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:surfaceMembers"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType MULTISURFACETYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MultiSurfaceType"), MultiPolygon.class, false,
            false, Collections.EMPTY_SET, ABSTRACTGEOMETRICAGGREGATETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="KnotType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A knot is a breakpoint on a piecewise spline curve.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element name="value" type="double"&gt;
     *              &lt;annotation&gt;
     *                  &lt;documentation&gt;The property "value" is the value of the parameter at the knot of the spline. The sequence of knots shall be a non-decreasing sequence. That is, each knot's value in the sequence shall be equal to or greater than the previous knot's value. The use of equal consecutive knots is normally handled using the multiplicity.&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *          &lt;element name="multiplicity" type="nonNegativeInteger"&gt;
     *              &lt;annotation&gt;
     *                  &lt;documentation&gt;The property "multiplicity" is the multiplicity of this knot used in the definition of the spline (with the same weight).&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *          &lt;element name="weight" type="double"&gt;
     *              &lt;annotation&gt;
     *                  &lt;documentation&gt;The property "weight" is the value of the averaging weight used for this knot of the spline.&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType KNOTTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "KnotType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CoverageFunctionType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;The function or rule which defines the map from members of the domainSet to the range.
     *        More functions will be added to this list&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;choice&gt;
     *          &lt;element ref="gml:MappingRule"/&gt;
     *          &lt;element ref="gml:GridFunction"/&gt;
     *      &lt;/choice&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType COVERAGEFUNCTIONTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CoverageFunctionType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="SurfaceArrayPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A container for an array of surfaces. The elements are always contained in the array property, referencing geometry elements or arrays of geometry elements is not supported.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:_Surface"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType SURFACEARRAYPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "SurfaceArrayPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GraphStylePropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation/&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element minOccurs="0" ref="gml:GraphStyle"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attribute name="about" type="anyURI" use="optional"/&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GRAPHSTYLEPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GraphStylePropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GeometricComplexType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A geometric complex.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGeometryType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" name="element" type="gml:GeometricPrimitivePropertyType"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GEOMETRICCOMPLEXTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GeometricComplexType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTGEOMETRYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractContinuousCoverageType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A continuous coverage as defined in ISO 19123 is a coverage that can return different values for the same feature attribute at different direct positions within a single spatiotemporal object in its spatiotemporal domain&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCoverageType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element minOccurs="0" ref="gml:coverageFunction"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTCONTINUOUSCOVERAGETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractContinuousCoverageType"),
            Collections.EMPTY_LIST, false, true, Collections.EMPTY_SET, ABSTRACTCOVERAGETYPE_TYPE,
            null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CovarianceMatrixType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Error estimate covariance matrix. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractPositionalAccuracyType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" ref="gml:unitOfMeasure"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Ordered sequence of units of measure, corresponding to the row and column index numbers of the covariance matrix, starting with row and column 1 and ending with row/column N. Each unit of measure is for the ordinate reflected in the relevant row and column of the covariance matrix. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element maxOccurs="unbounded" ref="gml:includesElement"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Unordered set of elements in this covariance matrix. Because the covariance matrix is symmetrical, only the elements in the upper or lower diagonal part (including the main diagonal) of the matrix need to be specified. Any zero valued covariance elements can be omitted. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType COVARIANCEMATRIXTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CovarianceMatrixType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTPOSITIONALACCURACYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DMSAngleType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Angle value provided in degree-minute-second or degree-minute format.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element ref="gml:degrees"/&gt;
     *          &lt;choice minOccurs="0"&gt;
     *              &lt;element ref="gml:decimalMinutes"/&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:minutes"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:seconds"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/choice&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DMSANGLETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DMSAngleType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ArrayAssociationType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A base for derived types used to specify complex types containing an array of objects, by unspecified UML association - either composition or aggregation.  An instance of this type contains elements representing Objects.
     *
     *  Ideally this type would be derived by extension of AssociationType.
     *  However, this leads to a non-deterministic content model, since both the base and the extension have minOccurs="0", and is thus prohibited in XML Schema.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:_Object"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ARRAYASSOCIATIONTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ArrayAssociationType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MeasureListType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;List of numbers with a uniform scale.
     *        The value of uom (Units Of Measure) attribute is a reference to
     *        a Reference System for the amount, either a ratio or position scale. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:doubleList"&gt;
     *              &lt;attribute name="uom" type="anyURI" use="required"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType MEASURELISTTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MeasureListType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, DOUBLELIST_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TemporalCSType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A one-dimensional coordinate system containing a single time axis, used to describe the temporal position of a point in the specified time units from a specified time origin. A TemporalCS shall have one usesAxis association. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCoordinateSystemType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TEMPORALCSTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TemporalCSType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTCOORDINATESYSTEMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="RangeParametersType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Metadata about the rangeSet.  Definition of record structure.
     *        This is required if the rangeSet is encoded in a DataBlock.
     *        We use a gml:_Value with empty values as a map of the composite value structure.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;group ref="gml:ValueObject"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType RANGEPARAMETERSTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "RangeParametersType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ImageCRSType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;An engineering coordinate reference system applied to locations in images. Image coordinate reference systems are treated as a separate sub-type because a separate user community exists for images with its own terms of reference. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractReferenceSystemType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;choice&gt;
     *                      &lt;element ref="gml:usesCartesianCS"/&gt;
     *                      &lt;element ref="gml:usesObliqueCartesianCS"/&gt;
     *                  &lt;/choice&gt;
     *                  &lt;element ref="gml:usesImageDatum"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType IMAGECRSTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ImageCRSType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTREFERENCESYSTEMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="EllipsoidRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to an ellipsoid, either referencing or containing the definition of that ellipsoid. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:Ellipsoid"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ELLIPSOIDREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "EllipsoidRefType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MultiPointPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A property that has a collection of points as its value domain can either be an appropriate geometry element encapsulated in an element of this type or an XLink reference to a remote geometry element (where remote includes geometry elements located elsewhere in the same document). Either the reference or the contained element must be given, but neither both nor none.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:MultiPoint"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"&gt;
     *          &lt;annotation&gt;
     *              &lt;documentation&gt;This attribute group includes the XLink attributes (see xlinks.xsd). XLink is used in GML to reference remote resources (including those elsewhere in the same document). A simple link element can be constructed by including a specific set of XLink attributes. The XML Linking Language (XLink) is currently a Proposed Recommendation of the World Wide Web Consortium. XLink allows elements to be inserted into XML documents so as to create sophisticated links between resources; such links can be used to reference remote properties.
     *  A simple link element can be used to implement pointer functionality, and this functionality has been built into various GML 3 elements by including the gml:AssociationAttributeGroup.&lt;/documentation&gt;
     *          &lt;/annotation&gt;
     *      &lt;/attributeGroup&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType MULTIPOINTPROPERTYTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MultiPointPropertyType"), MultiPoint.class, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="PriorityLocationPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;G-XML component&lt;/documentation&gt;
     *          &lt;documentation&gt;Deprecated in GML 3.1.0&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:LocationPropertyType"&gt;
     *              &lt;attribute name="priority" type="string" use="optional"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType PRIORITYLOCATIONPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "PriorityLocationPropertyType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, LOCATIONPROPERTYTYPE_TYPE,
            null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MultiSolidDomainType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:DomainSetType"&gt;
     *              &lt;sequence minOccurs="0"&gt;
     *                  &lt;element ref="gml:MultiSolid"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType MULTISOLIDDOMAINTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MultiSolidDomainType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, DOMAINSETTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="PassThroughOperationType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A pass-through operation specifies that a subset of a coordinate tuple is subject to a specific coordinate operation. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCoordinateOperationType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" ref="gml:modifiedCoordinate"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Ordered sequence of positive integers defining the positions in a coordinate tuple of the coordinates affected by this pass-through operation. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element ref="gml:usesOperation"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType PASSTHROUGHOPERATIONTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "PassThroughOperationType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTCOORDINATEOPERATIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CubicSplineType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Cubic splines are similar to line strings in that they are a sequence of segments each with its own defining function. A cubic spline uses the control points and a set of derivative parameters to define a piecewise 3rd degree polynomial interpolation. Unlike line-strings, the parameterization by arc length is not necessarily still a polynomial.
     *                                  The function describing the curve must be C2, that is, have a continuous 1st and 2nd derivative at all points, and pass through the controlPoints in the order given. Between the control points, the curve segment is defined by a cubic polynomial. At each control point, the polynomial changes in such a manner that the 1st and 2nd derivative vectors are the same from either side. The control parameters record must contain vectorAtStart, and vectorAtEnd which are the unit tangent vectors at controlPoint[1] and controlPoint[n] where n = controlPoint.count.
     *                                  Note: only the direction of the vectors is relevant, not their length.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCurveSegmentType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;choice&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;GML supports two different ways to specify the control points of a curve segment.
     *  1. A sequence of "pos" (DirectPositionType) or "pointProperty" (PointPropertyType) elements. "pos" elements are control points that are only part of this curve segment, "pointProperty" elements contain a point that may be referenced from other geometry elements or reference another point defined outside of this curve segment (reuse of existing points).
     *  2. The "posList" element allows for a compact way to specifiy the coordinates of the control points, if all control points are in the same coordinate reference systems and belong to this curve segment only. The number of direct positions in the list must be at least three.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                      &lt;choice maxOccurs="unbounded" minOccurs="2"&gt;
     *                          &lt;element ref="gml:pos"/&gt;
     *                          &lt;element ref="gml:pointProperty"/&gt;
     *                          &lt;element ref="gml:pointRep"&gt;
     *                              &lt;annotation&gt;
     *                                  &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "pointProperty" instead. Included for backwards compatibility with GML 3.0.0.&lt;/documentation&gt;
     *                              &lt;/annotation&gt;
     *                          &lt;/element&gt;
     *                      &lt;/choice&gt;
     *                      &lt;element ref="gml:posList"/&gt;
     *                      &lt;element ref="gml:coordinates"&gt;
     *                          &lt;annotation&gt;
     *                              &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "posList" instead.&lt;/documentation&gt;
     *                          &lt;/annotation&gt;
     *                      &lt;/element&gt;
     *                  &lt;/choice&gt;
     *                  &lt;element name="vectorAtStart" type="gml:VectorType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;"vectorAtStart" is the unit tangent vector at the start point of the spline.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element name="vectorAtEnd" type="gml:VectorType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;"vectorAtEnd" is the unit tangent vector at the end point of the spline.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute fixed="cubicSpline" name="interpolation" type="gml:CurveInterpolationType"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;The attribute "interpolation" specifies the curve interpolation mechanism used for this segment. This mechanism
     *  uses the control points and control parameters to determine the position of this curve segment. For a CubicSpline the interpolation is fixed as "cubicSpline".&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *              &lt;attribute fixed="3" name="degree" type="integer"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;The degree for a cubic spline is "3".&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CUBICSPLINETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CubicSplineType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTCURVESEGMENTTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="EngineeringCRSRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to an engineering coordinate reference system, either referencing or containing the definition of that reference system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:EngineeringCRS"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ENGINEERINGCRSREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "EngineeringCRSRefType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DictionaryType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A non-abstract bag that is specialized for use as a dictionary which contains a set of definitions. These definitions are referenced from other places, in the same and different XML documents. In this restricted type, the inherited optional "description" element can be used for a description of this dictionary. The inherited optional "name" element can be used for the name(s) of this dictionary. The inherited "metaDataProperty" elements can be used to reference or contain more information about this dictionary. The inherited required gml:id attribute allows the dictionary to be referenced using this handle. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:DefinitionType"&gt;
     *              &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
     *                  &lt;choice&gt;
     *                      &lt;element ref="gml:dictionaryEntry"&gt;
     *                          &lt;annotation&gt;
     *                              &lt;documentation&gt;An entry in this dictionary. The content of an entry can itself be a lower level dictionary or definition collection. This element follows the standard GML property model, so the value may be provided directly or by reference. Note that if the value is provided by reference, this definition does not carry a handle (gml:id) in this context, so does not allow external references to this specific entry in this context. When used in this way the referenced definition will usually be in a dictionary in the same XML document. &lt;/documentation&gt;
     *                          &lt;/annotation&gt;
     *                      &lt;/element&gt;
     *                      &lt;element ref="gml:indirectEntry"&gt;
     *                          &lt;annotation&gt;
     *                              &lt;documentation&gt;An identified reference to a remote entry in this dictionary, to be used when this entry should be identified to allow external references to this specific entry. &lt;/documentation&gt;
     *                          &lt;/annotation&gt;
     *                      &lt;/element&gt;
     *                  &lt;/choice&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DICTIONARYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DictionaryType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, DEFINITIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="LineStringPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;This type is deprecated with GML 3 and shall not be used. It is included for backwards compatibility with GML 2. Use
     *                          CurvePropertyType instead. A property that has a line string as its value domain can either be an appropriate geometry element encapsulated
     *                          in an element of this type or an XLink reference to a remote geometry element (where remote includes geometry elements located elsewhere
     *                          in the same document). Either the reference or the contained element must be given, but neither both nor none.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:LineString"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"&gt;
     *          &lt;annotation&gt;
     *              &lt;documentation&gt;This attribute group includes the XLink attributes (see xlinks.xsd). XLink is used in GML to reference remote resources
     *                                  (including those elsewhere in the same document). A simple link element can be constructed by including a specific set of XLink attributes.
     *                                  The XML Linking Language (XLink) is currently a Proposed Recommendation of the World Wide Web Consortium. XLink allows elements to
     *                                  be inserted into XML documents so as to create sophisticated links between resources; such links can be used to reference remote properties.
     *                                  A simple link element can be used to implement pointer functionality, and this functionality has been built into various GML 3 elements by
     *                                  including the gml:AssociationAttributeGroup.&lt;/documentation&gt;
     *          &lt;/annotation&gt;
     *      &lt;/attributeGroup&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType LINESTRINGPROPERTYTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "LineStringPropertyType"), LineString.class, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ReferenceType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A pattern or base for derived types used to specify complex types corresponding to a UML aggregation association.  An instance of this type serves as a pointer to a remote Object.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence/&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType REFERENCETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ReferenceType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimeNodePropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A time node property can either be any time node element encapsulated in an element of this type
     *                          or an XLink reference to a remote time node element (where remote includes elements located elsewhere in the same document).
     *                          Note that either the reference or the contained element must be given, but not both or none.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:TimeNode"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMENODEPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeNodePropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="PolygonPatchType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A PolygonPatch is a surface patch that is defined by a set of boundary curves and an underlying surface to which these curves adhere. The curves are coplanar and the polygon uses planar interpolation in its interior. Implements GM_Polygon of ISO 19107.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractSurfacePatchType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element minOccurs="0" ref="gml:exterior"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:interior"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute fixed="planar" name="interpolation" type="gml:SurfaceInterpolationType"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;The attribute "interpolation" specifies the interpolation mechanism used for this surface patch. Currently only planar surface patches are defined in GML 3, the attribute is fixed to "planar", i.e. the interpolation method shall return points on a single plane. The boundary of the patch shall be contained within that plane.&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType POLYGONPATCHTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "PolygonPatchType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTSURFACEPATCHTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="TemporalDatumBaseType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Partially defines the origin of a temporal coordinate reference system. This type restricts the AbstractDatumType to remove the "anchorPoint" and "realizationEpoch" elements. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:AbstractDatumType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:metaDataProperty"/&gt;
     *                  &lt;element ref="gml:datumName"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:datumID"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:validArea"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:scope"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute ref="gml:id" use="required"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TEMPORALDATUMBASETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TemporalDatumBaseType"), Collections.EMPTY_LIST,
            false, true, Collections.EMPTY_SET, ABSTRACTDATUMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TemporalDatumType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Defines the origin of a temporal coordinate reference system. This type extends the TemporalDatumRestrictionType to add the "origin" element with the dateTime type. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:TemporalDatumBaseType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:origin"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TEMPORALDATUMTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TemporalDatumType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, TEMPORALDATUMBASETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimeClockPropertyType"&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:TimeClock"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMECLOCKPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeClockPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CompoundCRSRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a compound coordinate reference system, either referencing or containing the definition of that reference system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:CompoundCRS"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType COMPOUNDCRSREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CompoundCRSRefType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="FileType"&gt;
     *      &lt;sequence&gt;
     *          &lt;element ref="gml:rangeParameters"/&gt;
     *          &lt;element name="fileName" type="anyURI"/&gt;
     *          &lt;element name="fileStructure" type="gml:FileValueModelType"/&gt;
     *          &lt;element minOccurs="0" name="mimeType" type="anyURI"/&gt;
     *          &lt;element minOccurs="0" name="compression" type="anyURI"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType FILETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "FileType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="NameOrNull"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Union of the XML Schema Name type and the GML Nulltype.  An element which uses this type may have content which is either a Name or a value from Nulltype.  Note that a "Name" may not contain whitespace.  &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;union memberTypes="gml:NullEnumeration Name anyURI"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType NAMEORNULL_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "NameOrNull"), java.lang.Object.class, false, false,
            Collections.EMPTY_SET, XSSchema.ANYSIMPLETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="SequenceRuleNames"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;List of codes (adopted from ISO 19123 Annex C) that identifies the rule for traversing a grid to correspond with the sequence of members of the rangeSet.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="Linear"/&gt;
     *          &lt;enumeration value="Boustrophedonic"/&gt;
     *          &lt;enumeration value="Cantor-diagonal"/&gt;
     *          &lt;enumeration value="Spiral"/&gt;
     *          &lt;enumeration value="Morton"/&gt;
     *          &lt;enumeration value="Hilbert"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType SEQUENCERULENAMES_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "SequenceRuleNames"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, XSSchema.STRING_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="SequenceRuleType"&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:SequenceRuleNames"&gt;
     *              &lt;attribute name="order" type="gml:IncrementOrder" use="optional"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType SEQUENCERULETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "SequenceRuleType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, SEQUENCERULENAMES_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="LineTypeType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Graph-specific styling property.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="STRAIGHT"/&gt;
     *          &lt;enumeration value="BENT"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType LINETYPETYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "LineTypeType"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, XSSchema.STRING_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="PixelInCellType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Specification of the way an image grid is associated with the image data attributes. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;restriction base="gml:CodeType"&gt;
     *              &lt;attribute name="codeSpace" type="anyURI" use="required"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;Reference to a source of information specifying the values and meanings of all the allowed string values for this PixelInCellType. &lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *          &lt;/restriction&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType PIXELINCELLTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "PixelInCellType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, CODETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ConversionRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a concrete general-purpose conversion, either referencing or containing the definition of that conversion. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:Conversion"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CONVERSIONREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ConversionRefType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="NullType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Utility type for null elements.  The value may be selected from one of the enumerated tokens, or may be a URI in which case this should identify a resource which describes the reason for the null. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;union memberTypes="gml:NullEnumeration anyURI"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType NULLTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "NullType"), java.lang.Object.class, false, false,
            Collections.EMPTY_SET, XSSchema.ANYSIMPLETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="UnitDefinitionType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Definition of a unit of measure (or uom). The definition includes a quantityType property, which indicates the phenomenon to which the units apply, and a catalogSymbol, which gives the short symbol used for this unit. This element is used when the relationship of this unit to other units or units systems is unknown.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:DefinitionType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:quantityType"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:catalogSymbol"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType UNITDEFINITIONTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "UnitDefinitionType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, DEFINITIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="BaseUnitType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Definition of a unit of measure which is a base unit from the system of units.  A base unit cannot be derived by combination of other base units within this system.  Sometimes known as "fundamental unit".&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:UnitDefinitionType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element name="unitsSystem" type="gml:ReferenceType"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType BASEUNITTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "BaseUnitType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, UNITDEFINITIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="VectorType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Vector instances hold the compoents for a (usually spatial) vector within some coordinate reference system (CRS).
     *                          Since Vectors will often be included in larger objects that have references to CRS, the "srsName" attribute may be missing.
     *                          In this case, the CRS is implicitly assumed to take on the value of the containing object's CRS.
     *
     *                          Note that this content model is the same as DirectPositionType, but is defined separately to reflect the distinct semantics, and to avoid validation problems. SJDC 2004-12-02&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:doubleList"&gt;
     *              &lt;attributeGroup ref="gml:SRSReferenceGroup"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType VECTORTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "VectorType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, DOUBLELIST_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="BagType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A non-abstract generic collection type that can be used as a document element for a collection of any GML types - Geometries, Topologies, Features ...
     *
     *  FeatureCollections may only contain Features.  GeometryCollections may only contain Geometrys.  Bags are less constrained  they must contain objects that are substitutable for gml:_Object.  This may mix several levels, including Features, Definitions, Dictionaries, Geometries etc.
     *
     *  The content model would ideally be
     *     member 0..*
     *     members 0..1
     *     member 0..*
     *  for maximum flexibility in building a collection from both homogeneous and distinct components:
     *  included "member" elements each contain a single Object
     *  an included "members" element contains a set of Objects
     *
     *  However, this is non-deterministic, thus prohibited by XSD.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGMLType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:member"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:members"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType BAGTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "BagType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, ABSTRACTGMLTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TopoPrimitiveMemberType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;This type supports embedding topological primitives in a TopoComplex.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element minOccurs="0" ref="gml:_TopoPrimitive"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TOPOPRIMITIVEMEMBERTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TopoPrimitiveMemberType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType final="#all" name="TimeIntervalLengthType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;This type extends the built-in xsd:decimal simple type to allow floating-point
     *        values for temporal length. According to  the ISO 11404 model you have to use
     *        positiveInteger together with appropriate values for radix and factor. The
     *        resolution of the time interval is to one radix ^(-factor) of the specified
     *        time unit (e.g. unit="second", radix="10", factor="3" specifies a resolution
     *        of milliseconds). It is a subtype of TimeDurationType.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="decimal"&gt;
     *              &lt;attribute name="unit" type="gml:TimeUnitType" use="required"/&gt;
     *              &lt;attribute name="radix" type="positiveInteger" use="optional"/&gt;
     *              &lt;attribute name="factor" type="integer" use="optional"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMEINTERVALLENGTHTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeIntervalLengthType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.DECIMAL_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CoordinateSystemAxisRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a coordinate system axis, either referencing or containing the definition of that axis. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:CoordinateSystemAxis"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType COORDINATESYSTEMAXISREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CoordinateSystemAxisRefType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimeCoordinateSystemType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;A temporal coordinate system is based on a continuous interval scale defined in terms of a single time interval.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTimeReferenceSystemType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;choice&gt;
     *                      &lt;element name="originPosition" type="gml:TimePositionType"/&gt;
     *                      &lt;element name="origin" type="gml:TimeInstantPropertyType"/&gt;
     *                  &lt;/choice&gt;
     *                  &lt;element name="interval" type="gml:TimeIntervalLengthType"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMECOORDINATESYSTEMTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeCoordinateSystemType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTTIMEREFERENCESYSTEMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TriangleType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Represents a triangle as a surface with an outer boundary consisting of a linear ring. Note that this is a polygon (subtype) with no inner boundaries. The number of points in the linear ring must be four.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractSurfacePatchType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:exterior"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Constraint: The Ring shall be a LinearRing and must form a triangle, the first and the last position must be co-incident.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute fixed="planar" name="interpolation" type="gml:SurfaceInterpolationType"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;The attribute "interpolation" specifies the interpolation mechanism used for this surface patch. Currently only planar surface patches are defined in GML 3, the attribute is fixed to "planar", i.e. the interpolation method shall return points on a single plane. The boundary of the patch shall be contained within that plane.&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TRIANGLETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TriangleType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTSURFACEPATCHTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ConventionalUnitType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Definition of a unit of measure which is related to a preferred unit for this quantity type through a conversion formula.  A method for deriving this unit by algebraic combination of more primitive units, may also be provided.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:UnitDefinitionType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;choice&gt;
     *                      &lt;element ref="gml:conversionToPreferredUnit"/&gt;
     *                      &lt;element ref="gml:roughConversionToPreferredUnit"/&gt;
     *                  &lt;/choice&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:derivationUnitTerm"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CONVENTIONALUNITTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ConventionalUnitType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, UNITDEFINITIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimeCalendarEraPropertyType"&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:TimeCalendarEra"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMECALENDARERAPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeCalendarEraPropertyType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="OperationParameterRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to an operation parameter, either referencing or containing the definition of that parameter. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:OperationParameter"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType OPERATIONPARAMETERREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "OperationParameterRefType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="PassThroughOperationRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a pass through operation, either referencing or containing the definition of that pass through operation. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:PassThroughOperation"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType PASSTHROUGHOPERATIONREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "PassThroughOperationRefType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="EllipsoidalCSType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A two- or three-dimensional coordinate system in which position is specified by geodetic latitude, geodetic longitude, and (in the three-dimensional case) ellipsoidal height. An EllipsoidalCS shall have two or three usesAxis associations. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCoordinateSystemType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ELLIPSOIDALCSTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "EllipsoidalCSType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTCOORDINATESYSTEMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DerivedUnitType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Definition of a unit of measure which is defined through algebraic combination of more primitive units, which are usually base units from a particular system of units. Derived units based directly on base units are usually preferred for quantities other than the base units or fundamental quantities within a system.  If a derived unit is not the preferred unit, the ConventionalUnit element should be used instead.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:UnitDefinitionType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" ref="gml:derivationUnitTerm"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DERIVEDUNITTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DerivedUnitType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, UNITDEFINITIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimeType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Value of a time or temporal quantity, with its units. Uses the MeasureType with the restriction that the unit of measure referenced by uom must be suitable for a time value, such as seconds or weeks.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;restriction base="gml:MeasureType"/&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, MEASURETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="booleanList"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;XML List based on XML Schema boolean type.  An element of this type contains a space-separated list of boolean values {0,1,true,false}&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;list itemType="boolean"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType BOOLEANLIST_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "booleanList"), java.lang.Object.class, false, false,
            Collections.EMPTY_SET, XSSchema.ANYSIMPLETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GeocentricCRSType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A 3D coordinate reference system with the origin at the approximate centre of mass of the earth. A geocentric CRS deals with the earth's curvature by taking a 3D spatial view, which obviates the need to model the earth's curvature. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractReferenceSystemType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;choice&gt;
     *                      &lt;element ref="gml:usesCartesianCS"/&gt;
     *                      &lt;element ref="gml:usesSphericalCS"/&gt;
     *                  &lt;/choice&gt;
     *                  &lt;element ref="gml:usesGeodeticDatum"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GEOCENTRICCRSTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GeocentricCRSType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTREFERENCESYSTEMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="AbsoluteExternalPositionalAccuracyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Closeness of reported coordinate values to values accepted as or being true. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractPositionalAccuracyType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:result"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSOLUTEEXTERNALPOSITIONALACCURACYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbsoluteExternalPositionalAccuracyType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET,
            ABSTRACTPOSITIONALACCURACYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CartesianCSType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A 1-, 2-, or 3-dimensional coordinate system. Gives the position of points relative to orthogonal straight axes in the 2- and 3-dimensional cases. In the 1-dimensional case, it contains a single straight coordinate axis. In the multi-dimensional case, all axes shall have the same length unit of measure. A CartesianCS shall have one, two, or three usesAxis associations. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCoordinateSystemType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CARTESIANCSTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CartesianCSType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTCOORDINATESYSTEMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TemporalDatumRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a temporal datum, either referencing or containing the definition of that datum. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:TemporalDatum"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TEMPORALDATUMREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TemporalDatumRefType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimeInstantType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Omit back-pointers begunBy, endedBy.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTimeGeometricPrimitiveType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:timePosition"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMEINSTANTTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeInstantType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTTIMEGEOMETRICPRIMITIVETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimeGeometricPrimitivePropertyType"&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:_TimeGeometricPrimitive"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMEGEOMETRICPRIMITIVEPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeGeometricPrimitivePropertyType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="IsolatedPropertyType"&gt;
     *      &lt;choice minOccurs="0"&gt;
     *          &lt;element ref="gml:Node"/&gt;
     *          &lt;element ref="gml:Edge"/&gt;
     *      &lt;/choice&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ISOLATEDPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "IsolatedPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimeOrdinalEraPropertyType"&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:TimeOrdinalEra"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMEORDINALERAPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeOrdinalEraPropertyType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CurveSegmentArrayPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A container for an array of curve segments.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:_CurveSegment"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CURVESEGMENTARRAYPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CurveSegmentArrayPropertyType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DerivedCRSTypeType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Type of a derived coordinate reference system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;restriction base="gml:CodeType"&gt;
     *              &lt;attribute name="codeSpace" type="anyURI" use="required"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;Reference to a source of information specifying the values and meanings of all the allowed string values for this DerivedCRSTypeType. &lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *          &lt;/restriction&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DERIVEDCRSTYPETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DerivedCRSTypeType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, CODETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="VerticalCSType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A one-dimensional coordinate system used to record the heights (or depths) of points. Such a coordinate system is usually dependent on the Earth's gravity field, perhaps loosely as when atmospheric pressure is the basis for the vertical coordinate system axis. A VerticalCS shall have one usesAxis association. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCoordinateSystemType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType VERTICALCSTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "VerticalCSType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTCOORDINATESYSTEMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="KnotTypesType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Defines allowed values for the knots` type. Uniform knots implies that all knots are of multiplicity 1 and they differ by a positive constant from the preceding knot. Knots are quasi-uniform iff they are of multiplicity (degree + 1) at the ends, of multiplicity 1 elsewhere, and they differ by a positive constant from the preceding knot.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="uniform"/&gt;
     *          &lt;enumeration value="quasiUniform"/&gt;
     *          &lt;enumeration value="piecewiseBezier"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType KNOTTYPESTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "KnotTypesType"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, XSSchema.STRING_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TopoComplexMemberType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;This Property can be used to embed a TopoComplex in a feature collection.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element minOccurs="0" ref="gml:TopoComplex"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TOPOCOMPLEXMEMBERTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TopoComplexMemberType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="QueryGrammarEnumeration"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Used to specify the grammar of the feature query mechanism.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="xpath"/&gt;
     *          &lt;enumeration value="xquery"/&gt;
     *          &lt;enumeration value="other"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType QUERYGRAMMARENUMERATION_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "QueryGrammarEnumeration"), java.lang.Object.class,
            false, false, Collections.EMPTY_SET, XSSchema.STRING_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="booleanOrNullList"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;XML List based on the union type defined above.  An element declared with this type contains a space-separated list of boolean values {0,1,true,false} with null values interspersed as needed&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;list itemType="gml:booleanOrNull"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType BOOLEANORNULLLIST_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "booleanOrNullList"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, XSSchema.ANYSIMPLETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="PolyhedralSurfaceType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A polyhedral surface is a surface composed
     *     of polygon surfaces connected along their common boundary
     *     curves. This differs from the surface type only in the
     *     restriction on the types of surface patches acceptable.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:SurfaceType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;group ref="gml:StandardObjectProperties"/&gt;
     *                  &lt;element ref="gml:polygonPatches"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;This property encapsulates the patches of
     *        the polyhedral surface.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType POLYHEDRALSURFACETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "PolyhedralSurfaceType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, SURFACETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CompositeValueType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Aggregate value built from other Values using the Composite pattern. It contains zero or an arbitrary number of valueComponent elements, and zero or one valueComponents elements.  It may be used for strongly coupled aggregates (vectors, tensors) or for arbitrary collections of values.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGMLType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:valueComponent"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:valueComponents"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType COMPOSITEVALUETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CompositeValueType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTGMLTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ValueArrayType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A Value Array is used for homogeneous arrays of primitive and aggregate values.  The member values may be scalars, composites, arrays or lists.  ValueArray has the same content model as CompositeValue, but the member values must be homogeneous.  The element declaration contains a Schematron constraint which expresses this restriction precisely.            Since the members are homogeneous, the referenceSystem (uom, codeSpace) may be specified on the ValueArray itself and implicitly inherited by all the members if desired.    Note that a_ScalarValueList is preferred for arrays of Scalar Values since this is a more efficient encoding.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:CompositeValueType"&gt;
     *              &lt;attributeGroup ref="gml:referenceSystem"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType VALUEARRAYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ValueArrayType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, COMPOSITEVALUETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="PolygonPatchArrayPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;This type defines a container for an array of
     *     polygon patches.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:SurfacePatchArrayPropertyType"&gt;
     *              &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
     *                  &lt;element ref="gml:PolygonPatch"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType POLYGONPATCHARRAYPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "PolygonPatchArrayPropertyType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET,
            SURFACEPATCHARRAYPROPERTYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MultiSolidType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A MultiSolid is defined by one or more Solids, referenced through solidMember elements.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGeometricAggregateType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;The members of the geometric aggregate can be specified either using the "standard" property or the array property style. It is also valid to use both the "standard" and the array property style in the same collection.
     *  NOTE: Array properties cannot reference remote geometry elements.&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:solidMember"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:solidMembers"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType MULTISOLIDTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MultiSolidType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTGEOMETRICAGGREGATETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MultiPointCoverageType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A discrete coverage type whose domain is defined by a collection of point&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:AbstractDiscreteCoverageType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;group ref="gml:StandardObjectProperties"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:boundedBy"/&gt;
     *                  &lt;element ref="gml:multiPointDomain"/&gt;
     *                  &lt;element ref="gml:rangeSet"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:coverageFunction"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType MULTIPOINTCOVERAGETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MultiPointCoverageType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTDISCRETECOVERAGETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="NameList"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;XML List based on XML Schema Name type.  An element of this type contains a space-separated list of Name values&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;list itemType="Name"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType NAMELIST_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "NameList"), java.lang.Object.class, false, false,
            Collections.EMPTY_SET, XSSchema.ANYSIMPLETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CodeListType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;List of values on a uniform nominal scale.  List of text tokens.
     *        In a list context a token should not include any spaces, so xsd:Name is used instead of xsd:string.
     *        If a codeSpace attribute is present, then its value is a reference to
     *        a Reference System for the value, a dictionary or code list.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:NameList"&gt;
     *              &lt;attribute name="codeSpace" type="anyURI" use="optional"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CODELISTTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CodeListType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, NAMELIST_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TopoComplexType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;This type represents a TP_Complex capable of holding topological primitives.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTopologyType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:maximalComplex"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:superComplex"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:subComplex"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:topoPrimitiveMember"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:topoPrimitiveMembers"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute default="false" name="isMaximal" type="boolean"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TOPOCOMPLEXTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TopoComplexType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTTOPOLOGYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="PrimeMeridianRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a prime meridian, either referencing or containing the definition of that meridian. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:PrimeMeridian"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType PRIMEMERIDIANREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "PrimeMeridianRefType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractGeneralOperationParameterType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Abstract definition of a parameter or group of parameters used by an operation method. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:DefinitionType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element minOccurs="0" ref="gml:minimumOccurs"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTGENERALOPERATIONPARAMETERTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractGeneralOperationParameterType"),
            Collections.EMPTY_LIST, false, true, Collections.EMPTY_SET, DEFINITIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="OperationParameterBaseType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Basic encoding for operation parameter objects, simplifying and restricting the DefinitionType as needed. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:AbstractGeneralOperationParameterType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:metaDataProperty"/&gt;
     *                  &lt;element ref="gml:parameterName"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:minimumOccurs"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute ref="gml:id" use="required"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType OPERATIONPARAMETERBASETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "OperationParameterBaseType"),
            Collections.EMPTY_LIST, false, true, Collections.EMPTY_SET,
            ABSTRACTGENERALOPERATIONPARAMETERTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="OperationParameterType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;The definition of a parameter used by an operation method. Most parameter values are numeric, but other types of parameter values are possible. This complexType is expected to be used or extended for all operation methods, without defining operation-method-specialized element names.  &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:OperationParameterBaseType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:parameterID"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Set of alternative identifications of this operation parameter. The first parameterID, if any, is normally the primary identification code, and any others are aliases. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element minOccurs="0" ref="gml:remarks"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Comments on or information about this operation parameter, including source information. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType OPERATIONPARAMETERTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "OperationParameterType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, OPERATIONPARAMETERBASETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GeometricPrimitivePropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A property that has a geometric primitive as its value domain can either be an appropriate geometry element
     *                          encapsulated in an element of this type or an XLink reference to a remote geometry element (where remote includes geometry
     *                          elements located elsewhere in the same document). Either the reference or the contained element must be given, but neither
     *                          both nor none.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:_GeometricPrimitive"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"&gt;
     *          &lt;annotation&gt;
     *              &lt;documentation&gt;This attribute group includes the XLink attributes (see xlinks.xsd). XLink is used in GML to reference remote
     *                                  resources (including those elsewhere in the same document). A simple link element can be constructed by including a
     *                                  specific set of XLink attributes. The XML Linking Language (XLink) is currently a Proposed Recommendation of the World Wide
     *                                  Web Consortium. XLink allows elements to be inserted into XML documents so as to create sophisticated links between
     *                                  resources; such links can be used to reference remote properties. A simple link element can be used to implement pointer
     *                                  functionality, and this functionality has been built into various GML 3 elements by including the gml:AssociationAttributeGroup.&lt;/documentation&gt;
     *          &lt;/annotation&gt;
     *      &lt;/attributeGroup&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GEOMETRICPRIMITIVEPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GeometricPrimitivePropertyType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CoordinateSystemRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a coordinate system, either referencing or containing the definition of that coordinate system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:_CoordinateSystem"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType COORDINATESYSTEMREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CoordinateSystemRefType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MultiLineStringPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;This type is deprecated with GML 3 and shall not be used. It is included for backwards compatibility with GML 2. Use MultiCurvePropertyType instead.
     *  A property that has a collection of line strings as its value domain can either be an appropriate geometry element encapsulated in an element of this type or an XLink reference to a remote geometry element (where remote includes geometry elements located elsewhere in the same document). Either the reference or the contained element must be given, but neither both nor none.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:MultiLineString"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"&gt;
     *          &lt;annotation&gt;
     *              &lt;documentation&gt;This attribute group includes the XLink attributes (see xlinks.xsd). XLink is used in GML to reference remote resources (including those elsewhere in the same document). A simple link element can be constructed by including a specific set of XLink attributes. The XML Linking Language (XLink) is currently a Proposed Recommendation of the World Wide Web Consortium. XLink allows elements to be inserted into XML documents so as to create sophisticated links between resources; such links can be used to reference remote properties.
     *  A simple link element can be used to implement pointer functionality, and this functionality has been built into various GML 3 elements by including the gml:AssociationAttributeGroup.&lt;/documentation&gt;
     *          &lt;/annotation&gt;
     *      &lt;/attributeGroup&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType MULTILINESTRINGPROPERTYTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MultiLineStringPropertyType"),
            MultiLineString.class, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TopologyStylePropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation/&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element minOccurs="0" ref="gml:TopologyStyle"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attribute name="about" type="anyURI" use="optional"/&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TOPOLOGYSTYLEPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TopologyStylePropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GeographicCRSType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A coordinate reference system based on an ellipsoidal approximation of the geoid; this provides an accurate representation of the geometry of geographic features for a large portion of the earth's surface.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractReferenceSystemType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:usesEllipsoidalCS"/&gt;
     *                  &lt;element ref="gml:usesGeodeticDatum"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GEOGRAPHICCRSTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GeographicCRSType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTREFERENCESYSTEMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CountPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Property whose content is a Count.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:ValuePropertyType"&gt;
     *              &lt;sequence minOccurs="0"&gt;
     *                  &lt;element ref="gml:Count"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType COUNTPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CountPropertyType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, VALUEPROPERTYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="VolumeType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Value of a spatial volume quantity, with its units. Uses the MeasureType with the restriction that the unit of measure referenced by uom must be suitable for a volume, such as cubic metres or cubic feet.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;restriction base="gml:MeasureType"/&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType VOLUMETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "VolumeType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, MEASURETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CompositeSurfacePropertyType"&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:CompositeSurface"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType COMPOSITESURFACEPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CompositeSurfacePropertyType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="EngineeringDatumRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to an engineering datum, either referencing or containing the definition of that datum. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:EngineeringDatum"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ENGINEERINGDATUMREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "EngineeringDatumRefType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType mixed="true" name="LabelType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Label is mixed -- composed of text and XPath expressions used to extract the useful information from the feature.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element maxOccurs="unbounded" minOccurs="0"
     *              name="LabelExpression" type="string"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attribute ref="gml:transform" use="optional"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType LABELTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "LabelType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CompositeSolidPropertyType"&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:CompositeSolid"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType COMPOSITESOLIDPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CompositeSolidPropertyType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GeometryArrayPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A container for an array of geometry elements. The elements are always contained in the array property,
     *                          referencing geometry elements or arrays of geometry elements is not supported.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:_Geometry"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GEOMETRYARRAYPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GeometryArrayPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CRSRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a CRS abstract coordinate reference system, either referencing or containing the definition of that CRS.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:_CRS"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CRSREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CRSRefType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ClothoidType_refLocation"&gt;
     *      &lt;sequence&gt;
     *          &lt;element ref="gml:AffinePlacement"&gt;
     *              &lt;annotation&gt;
     *                  &lt;documentation&gt;The "refLocation" is an affine mapping
     *            that places  the curve defined by the Fresnel Integrals
     *            into the co-ordinate reference system of this object.&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CLOTHOIDTYPE_REFLOCATION_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ClothoidType_refLocation"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="EngineeringCRSType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A contextually local coordinate reference system; which can be divided into two broad categories:
     *  - earth-fixed systems applied to engineering activities on or near the surface of the earth;
     *  - CRSs on moving platforms such as road vehicles, vessels, aircraft, or spacecraft.
     *  For further information, see OGC Abstract Specification Topic 2. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractReferenceSystemType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:usesCS"/&gt;
     *                  &lt;element ref="gml:usesEngineeringDatum"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ENGINEERINGCRSTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "EngineeringCRSType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTREFERENCESYSTEMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="IndirectEntryType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;An entry in a dictionary of definitions that contains a GML object which references a remote definition object. This entry is expected to be convenient in allowing multiple elements in one XML document to contain short (abbreviated XPointer) references, which are resolved to an external definition provided in a Dictionary element in the same XML document. Specialized descendents of this dictionaryEntry might be restricted in an application schema to allow only including specified types of definitions as valid entries in a dictionary. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element ref="gml:DefinitionProxy"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType INDIRECTENTRYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "IndirectEntryType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TopoPrimitiveArrayAssociationType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;This type supports embedding an array of topological primitives in a TopoComplex&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;!--                &lt;complexContent&gt;
     *                          &lt;restriction base="gml:ArrayAssociationType"&gt;  --&gt;
     *      &lt;sequence&gt;
     *          &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
     *              &lt;element ref="gml:_TopoPrimitive"/&gt;
     *          &lt;/choice&gt;
     *      &lt;/sequence&gt;
     *      &lt;!--                         &lt;/restriction&gt;
     *                  &lt;/complexContent&gt; --&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TOPOPRIMITIVEARRAYASSOCIATIONTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TopoPrimitiveArrayAssociationType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="FeatureStylePropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation/&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element minOccurs="0" ref="gml:FeatureStyle"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attribute name="about" type="anyURI" use="optional"/&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType FEATURESTYLEPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "FeatureStylePropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MultiGeometryPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A property that has a geometric aggregate as its value domain can either be an appropriate geometry element encapsulated in an element of this type or an XLink reference to a remote geometry element (where remote includes geometry elements located elsewhere in the same document). Either the reference or the contained element must be given, but neither both nor none.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:_GeometricAggregate"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"&gt;
     *          &lt;annotation&gt;
     *              &lt;documentation&gt;This attribute group includes the XLink attributes (see xlinks.xsd). XLink is used in GML to reference remote resources (including those elsewhere in the same document). A simple link element can be constructed by including a specific set of XLink attributes. The XML Linking Language (XLink) is currently a Proposed Recommendation of the World Wide Web Consortium. XLink allows elements to be inserted into XML documents so as to create sophisticated links between resources; such links can be used to reference remote properties.
     *  A simple link element can be used to implement pointer functionality, and this functionality has been built into various GML 3 elements by including the gml:AssociationAttributeGroup.&lt;/documentation&gt;
     *          &lt;/annotation&gt;
     *      &lt;/attributeGroup&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType MULTIGEOMETRYPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MultiGeometryPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="BoundedFeatureType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Makes boundedBy mandatory&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:AbstractFeatureType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;group ref="gml:StandardObjectProperties"/&gt;
     *                  &lt;element ref="gml:boundedBy"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:location"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;appinfo&gt;deprecated&lt;/appinfo&gt;
     *                          &lt;documentation&gt;deprecated in GML version 3.1&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType BOUNDEDFEATURETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "BoundedFeatureType"), Collections.EMPTY_LIST, false,
            true, Collections.EMPTY_SET, ABSTRACTFEATURETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="EngineeringDatumType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;An engineering datum defines the origin of an engineering coordinate reference system, and is used in a region around that origin. This origin can be fixed with respect to the earth (such as a defined point at a construction site), or be a defined point on a moving vehicle (such as on a ship or satellite). &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractDatumType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ENGINEERINGDATUMTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "EngineeringDatumType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTDATUMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MultiSurfaceDomainType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:DomainSetType"&gt;
     *              &lt;sequence minOccurs="0"&gt;
     *                  &lt;element ref="gml:MultiSurface"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType MULTISURFACEDOMAINTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MultiSurfaceDomainType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, DOMAINSETTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="TimeIndeterminateValueType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;This enumerated data type specifies values for indeterminate positions.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="after"/&gt;
     *          &lt;enumeration value="before"/&gt;
     *          &lt;enumeration value="now"/&gt;
     *          &lt;enumeration value="unknown"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType TIMEINDETERMINATEVALUETYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeIndeterminateValueType"),
            java.lang.Object.class, false, false, Collections.EMPTY_SET, XSSchema.STRING_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="SuccessionType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Feature succession is a semantic relationship derived from evaluation of observer, and
     *                          Feature Substitution, Feature Division and Feature Fusion are defined as associations between
     *                          previous features and next features in the temporal context.
     *                          Successions shall be represented in either following two ways.
     *                          * define a temporal topological complex element as a feature element
     *                          * define an association same as temporal topological complex between features.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="substitution"/&gt;
     *          &lt;enumeration value="division"/&gt;
     *          &lt;enumeration value="fusion"/&gt;
     *          &lt;enumeration value="initiation"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType SUCCESSIONTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "SuccessionType"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, XSSchema.STRING_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ArcStringByBulgeType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;This variant of the arc computes the mid points of the arcs instead of storing the coordinates directly. The control point sequence consists of the start and end points of each arc plus the bulge.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCurveSegmentType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;choice&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;GML supports two different ways to specify the control points of a curve segment.
     *  1. A sequence of "pos" (DirectPositionType) or "pointProperty" (PointPropertyType) elements. "pos" elements are control points that are only part of this curve segment, "pointProperty" elements contain a point that may be referenced from other geometry elements or reference another point defined outside of this curve segment (reuse of existing points).
     *  2. The "posList" element allows for a compact way to specifiy the coordinates of the control points, if all control points are in the same coordinate reference systems and belong to this curve segment only. The number of direct positions in the list must be at least two.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                      &lt;choice maxOccurs="unbounded" minOccurs="2"&gt;
     *                          &lt;element ref="gml:pos"/&gt;
     *                          &lt;element ref="gml:pointProperty"/&gt;
     *                          &lt;element ref="gml:pointRep"&gt;
     *                              &lt;annotation&gt;
     *                                  &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "pointProperty" instead. Included for backwards compatibility with GML 3.0.0.&lt;/documentation&gt;
     *                              &lt;/annotation&gt;
     *                          &lt;/element&gt;
     *                      &lt;/choice&gt;
     *                      &lt;element ref="gml:posList"/&gt;
     *                      &lt;element ref="gml:coordinates"&gt;
     *                          &lt;annotation&gt;
     *                              &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "posList" instead.&lt;/documentation&gt;
     *                          &lt;/annotation&gt;
     *                      &lt;/element&gt;
     *                  &lt;/choice&gt;
     *                  &lt;element maxOccurs="unbounded" name="bulge" type="double"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;The bulge controls the offset of each arc's midpoint. The "bulge" is the real number multiplier for the normal that determines the offset direction of the midpoint of each arc. The length of the bulge sequence is exactly 1 less than the length of the control point array, since a bulge is needed for each pair of adjacent points in the control point array. The bulge is not given by a distance, since it is simply a multiplier for the normal.
     *  The midpoint of the resulting arc is given by: midPoint = ((startPoint + endPoint)/2.0) + bulge*normal&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element maxOccurs="unbounded" name="normal" type="gml:VectorType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;The attribute "normal" is a vector normal (perpendicular) to the chord of the arc, the line joining the first and last
     *  point of the arc. In a 2D coordinate system, there are only two possible directions for the normal, and it is often given as a signed real, indicating its length, with a positive sign indicating a left turn angle from the chord line, and a negative sign indicating a right turn from the chord. In 3D, the normal determines the plane of the arc, along with the start and endPoint of the arc.
     *  The normal is usually a unit vector, but this is not absolutely necessary. If the normal is a zero vector, the geometric object becomes equivalent to the straight line between the two end points. The length of the normal sequence is exactly the same as for the bulge sequence, 1 less than the control point sequence length.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute fixed="circularArc2PointWithBulge"
     *                  name="interpolation" type="gml:CurveInterpolationType"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;The attribute "interpolation" specifies the curve interpolation mechanism used for this segment. This mechanism
     *  uses the control points and control parameters to determine the position of this curve segment. For an ArcStringByBulge the interpolation is fixed as "circularArc2PointWithBulge".&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *              &lt;attribute name="numArc" type="integer" use="optional"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;The number of arcs in the arc string can be explicitly stated in this attribute. The number of control points in the arc string must be numArc + 1.&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ARCSTRINGBYBULGETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ArcStringByBulgeType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTCURVESEGMENTTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ArcByBulgeType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;An ArcByBulge is an arc string with only one arc unit, i.e. two control points and one bulge.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:ArcStringByBulgeType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;choice&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;GML supports two different ways to specify the control points of a curve segment.
     *  1. A sequence of "pos" (DirectPositionType) or "pointProperty" (PointPropertyType) elements. "pos" elements are control points that are only part of this curve segment, "pointProperty" elements contain a point that may be referenced from other geometry elements or reference another point defined outside of this curve segment (reuse of existing points).
     *  2. The "posList" element allows for a compact way to specifiy the coordinates of the control points, if all control points are in the same coordinate reference systems and belong to this curve segment only. The number of direct positions in the list must be two.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                      &lt;choice maxOccurs="2" minOccurs="2"&gt;
     *                          &lt;element ref="gml:pos"/&gt;
     *                          &lt;element ref="gml:pointProperty"/&gt;
     *                          &lt;element ref="gml:pointRep"&gt;
     *                              &lt;annotation&gt;
     *                                  &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "pointProperty" instead. Included for backwards compatibility with GML 3.0.0.&lt;/documentation&gt;
     *                              &lt;/annotation&gt;
     *                          &lt;/element&gt;
     *                      &lt;/choice&gt;
     *                      &lt;element ref="gml:posList"/&gt;
     *                      &lt;element ref="gml:coordinates"&gt;
     *                          &lt;annotation&gt;
     *                              &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "posList" instead.&lt;/documentation&gt;
     *                          &lt;/annotation&gt;
     *                      &lt;/element&gt;
     *                  &lt;/choice&gt;
     *                  &lt;element name="bulge" type="double"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;The bulge controls the offset of each arc's midpoint. The "bulge" is the real number multiplier for the normal that determines the offset direction of the midpoint of each arc. The length of the bulge sequence is exactly 1 less than the length of the control point array, since a bulge is needed for each pair of adjacent points in the control point array. The bulge is not given by a distance, since it is simply a multiplier for the normal.
     *  The midpoint of the resulting arc is given by: midPoint = ((startPoint + endPoint)/2.0) + bulge*normal&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element name="normal" type="gml:VectorType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;The attribute "normal" is a vector normal (perpendicular) to the chord of the arc, the line joining the first and last
     *  point of the arc. In a 2D coordinate system, there are only two possible directions for the normal, and it is often given as a signed real, indicating its length, with a positive sign indicating a left turn angle from the chord line, and a negative sign indicating a right turn from the chord. In 3D, the normal determines the plane of the arc, along with the start and endPoint of the arc.
     *  The normal is usually a unit vector, but this is not absolutely necessary. If the normal is a zero vector, the geometric object becomes equivalent to the straight line between the two end points. The length of the normal sequence is exactly the same as for the bulge sequence, 1 less than the control point sequence length.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute fixed="1" name="numArc" type="integer" use="optional"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;An arc is an arc string consiting of a single arc, the attribute is fixed to "1".&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ARCBYBULGETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ArcByBulgeType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ARCSTRINGBYBULGETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="IdentifierType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;An identification of a CRS object. The first use of the IdentifierType for an object, if any, is normally the primary identification code, and any others are aliases.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element ref="gml:name"&gt;
     *              &lt;annotation&gt;
     *                  &lt;documentation&gt;The code or name for this Identifier, often from a controlled list or pattern defined by a code space. The optional codeSpace attribute is normally included to identify or reference a code space within which one or more codes are defined. This code space is often defined by some authority organization, where one organization may define multiple code spaces. The range and format of each Code Space identifier is defined by that code space authority. Information about that code space authority can be included as metaDataProperty elements which are optionally allowed in all CRS objects.&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *          &lt;element minOccurs="0" ref="gml:version"/&gt;
     *          &lt;element minOccurs="0" ref="gml:remarks"&gt;
     *              &lt;annotation&gt;
     *                  &lt;documentation&gt;Remarks about this code or alias.&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType IDENTIFIERTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "IdentifierType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GraphStyleType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;[complexType of] The style descriptor for a graph consisting of a number of features. Describes graph-specific style attributes.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:BaseStyleDescriptorType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element minOccurs="0" name="planar" type="boolean"/&gt;
     *                  &lt;element minOccurs="0" name="directed" type="boolean"/&gt;
     *                  &lt;element minOccurs="0" name="grid" type="boolean"/&gt;
     *                  &lt;element minOccurs="0" name="minDistance" type="double"/&gt;
     *                  &lt;element minOccurs="0" name="minAngle" type="double"/&gt;
     *                  &lt;element minOccurs="0" name="graphType" type="gml:GraphTypeType"/&gt;
     *                  &lt;element minOccurs="0" name="drawingType" type="gml:DrawingTypeType"/&gt;
     *                  &lt;element minOccurs="0" name="lineType" type="gml:LineTypeType"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0"
     *                      name="aestheticCriteria" type="gml:AesheticCriteriaType"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GRAPHSTYLETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GraphStyleType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, BASESTYLEDESCRIPTORTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CovarianceElementType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;An element of a covariance matrix.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element ref="gml:rowIndex"/&gt;
     *          &lt;element ref="gml:columnIndex"/&gt;
     *          &lt;element ref="gml:covariance"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType COVARIANCEELEMENTTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CovarianceElementType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="QuantityPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Property whose content is a Quantity.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:ValuePropertyType"&gt;
     *              &lt;sequence minOccurs="0"&gt;
     *                  &lt;element ref="gml:Quantity"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType QUANTITYPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "QuantityPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, VALUEPROPERTYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="PolygonPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;This type is deprecated with GML 3 and shall not be used. It is included for backwards compatibility with GML 2. Use SurfacePropertyType instead.
     *  A property that has a polygon as its value domain can either be an appropriate geometry element encapsulated in an element of this type or an XLink reference to a remote geometry element (where remote includes geometry elements located elsewhere in the same document). Either the reference or the contained element must be given, but neither both nor none.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:Polygon"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"&gt;
     *          &lt;annotation&gt;
     *              &lt;documentation&gt;This attribute group includes the XLink attributes (see xlinks.xsd). XLink is used in GML to reference remote resources (including those elsewhere in the same document). A simple link element can be constructed by including a specific set of XLink attributes. The XML Linking Language (XLink) is currently a Proposed Recommendation of the World Wide Web Consortium. XLink allows elements to be inserted into XML documents so as to create sophisticated links between resources; such links can be used to reference remote properties.
     *  A simple link element can be used to implement pointer functionality, and this functionality has been built into various GML 3 elements by including the gml:AssociationAttributeGroup.&lt;/documentation&gt;
     *          &lt;/annotation&gt;
     *      &lt;/attributeGroup&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType POLYGONPROPERTYTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "PolygonPropertyType"), Polygon.class, false, false,
            Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MultiSolidPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A property that has a collection of solids as its value domain can either be an appropriate geometry element encapsulated in an element of this type or an XLink reference to a remote geometry element (where remote includes geometry elements located elsewhere in the same document). Either the reference or the contained element must be given, but neither both nor none.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:MultiSolid"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"&gt;
     *          &lt;annotation&gt;
     *              &lt;documentation&gt;This attribute group includes the XLink attributes (see xlinks.xsd). XLink is used in GML to reference remote resources (including those elsewhere in the same document). A simple link element can be constructed by including a specific set of XLink attributes. The XML Linking Language (XLink) is currently a Proposed Recommendation of the World Wide Web Consortium. XLink allows elements to be inserted into XML documents so as to create sophisticated links between resources; such links can be used to reference remote properties.
     *  A simple link element can be used to implement pointer functionality, and this functionality has been built into various GML 3 elements by including the gml:AssociationAttributeGroup.&lt;/documentation&gt;
     *          &lt;/annotation&gt;
     *      &lt;/attributeGroup&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType MULTISOLIDPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MultiSolidPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CylindricalCSType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A three-dimensional coordinate system consisting of a polar coordinate system extended by a straight coordinate axis perpendicular to the plane spanned by the polar coordinate system. A CylindricalCS shall have three usesAxis associations. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCoordinateSystemType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CYLINDRICALCSTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CylindricalCSType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTCOORDINATESYSTEMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="UnitOfMeasureType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Reference to a unit of measure definition that applies to all the numerical values described by the element containing this element. Notice that a complexType which needs to include the uom attribute can do so by extending this complexType. Alternately, this complexType can be used as a pattern for a new complexType.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence/&gt;
     *      &lt;attribute name="uom" type="anyURI" use="required"&gt;
     *          &lt;annotation&gt;
     *              &lt;documentation&gt;Reference to a unit of measure definition, usually within the same XML document but possibly outside the XML document which contains this reference. For a reference within the same XML document, the "#" symbol should be used, followed by a text abbreviation of the unit name. However, the "#" symbol may be optional, and still may be interpreted as a reference.&lt;/documentation&gt;
     *          &lt;/annotation&gt;
     *      &lt;/attribute&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType UNITOFMEASURETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "UnitOfMeasureType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ConversionToPreferredUnitType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Relation of a unit to the preferred unit for this quantity type, specified by an arithmetic conversion (scaling and/or offset). A preferred unit is either a base unit or a derived unit selected for all units of one quantity type. The mandatory attribute "uom" shall reference the preferred unit that this conversion applies to. The conversion is specified by one of two alternative elements: "factor" or "formula".&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:UnitOfMeasureType"&gt;
     *              &lt;choice&gt;
     *                  &lt;element name="factor" type="double"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Specification of the scale factor by which a value using this unit of measure can be multiplied to obtain the corresponding value using the preferred unit of measure.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element name="formula" type="gml:FormulaType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Specification of the formula by which a value using this unit of measure can be converted to obtain the corresponding value using the preferred unit of measure.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/choice&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CONVERSIONTOPREFERREDUNITTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ConversionToPreferredUnitType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, UNITOFMEASURETYPE_TYPE,
            null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="LineStringType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A LineString is a special curve that consists of a single segment with linear interpolation. It is defined by two or more coordinate
     *                          tuples, with linear interpolation between them. It is backwards compatible with the LineString of GML 2, GM_LineString of ISO 19107 is
     *                          implemented by LineStringSegment.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCurveType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;choice&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;GML supports two different ways to specify the control points of a line string. 1. A sequence of "pos"
     *                                                          (DirectPositionType) or "pointProperty" (PointPropertyType) elements. "pos" elements are control points that are only part
     *                                                          of this curve, "pointProperty" elements contain a point that may be referenced from other geometry elements or reference
     *                                                          another point defined outside of this curve (reuse of existing points). 2. The "posList" element allows for a compact way to
     *                                                          specifiy the coordinates of the control points, if all control points are in the same coordinate reference systems and belong
     *                                                          to this curve only. The number of direct positions in the list must be at least two.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                      &lt;choice maxOccurs="unbounded" minOccurs="2"&gt;
     *                          &lt;element ref="gml:pos"/&gt;
     *                          &lt;element ref="gml:pointProperty"/&gt;
     *                          &lt;element ref="gml:pointRep"&gt;
     *                              &lt;annotation&gt;
     *                                  &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "pointProperty" instead. Included for backwards compatibility
     *                                                                          with GML 3.0.0.&lt;/documentation&gt;
     *                              &lt;/annotation&gt;
     *                          &lt;/element&gt;
     *                          &lt;element ref="gml:coord"&gt;
     *                              &lt;annotation&gt;
     *                                  &lt;documentation&gt;Deprecated with GML version 3.0. Use "pos" instead. The "coord" element is included for backwards
     *                                                                          compatibility with GML 2.&lt;/documentation&gt;
     *                              &lt;/annotation&gt;
     *                          &lt;/element&gt;
     *                      &lt;/choice&gt;
     *                      &lt;element ref="gml:posList"/&gt;
     *                      &lt;element ref="gml:coordinates"&gt;
     *                          &lt;annotation&gt;
     *                              &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "posList" instead.&lt;/documentation&gt;
     *                          &lt;/annotation&gt;
     *                      &lt;/element&gt;
     *                  &lt;/choice&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType LINESTRINGTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "LineStringType"), LineString.class, false, false,
            Collections.EMPTY_SET, ABSTRACTCURVETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GeometricComplexPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A property that has a geometric complex as its value domain can either be an appropriate geometry element encapsulated in an element of this type or an XLink reference to a remote geometry element (where remote includes geometry elements located elsewhere in the same document). Either the reference or the contained element must be given, but neither both nor none.
     *  NOTE: The allowed geometry elements contained in such a property (or referenced by it) have to be modelled by an XML Schema choice element since the composites inherit both from geometric complex *and* geometric primitive and are already part of the _GeometricPrimitive substitution group.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;choice&gt;
     *              &lt;element ref="gml:GeometricComplex"/&gt;
     *              &lt;element ref="gml:CompositeCurve"/&gt;
     *              &lt;element ref="gml:CompositeSurface"/&gt;
     *              &lt;element ref="gml:CompositeSolid"/&gt;
     *          &lt;/choice&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"&gt;
     *          &lt;annotation&gt;
     *              &lt;documentation&gt;This attribute group includes the XLink attributes (see xlinks.xsd). XLink is used in GML to reference remote resources (including those elsewhere in the same document). A simple link element can be constructed by including a specific set of XLink attributes. The XML Linking Language (XLink) is currently a Proposed Recommendation of the World Wide Web Consortium. XLink allows elements to be inserted into XML documents so as to create sophisticated links between resources; such links can be used to reference remote properties.
     *  A simple link element can be used to implement pointer functionality, and this functionality has been built into various GML 3 elements by including the gml:AssociationAttributeGroup.&lt;/documentation&gt;
     *          &lt;/annotation&gt;
     *      &lt;/attributeGroup&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GEOMETRICCOMPLEXPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GeometricComplexPropertyType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DataBlockType"&gt;
     *      &lt;sequence&gt;
     *          &lt;element ref="gml:rangeParameters"/&gt;
     *          &lt;choice&gt;
     *              &lt;element ref="gml:tupleList"/&gt;
     *              &lt;element ref="gml:doubleOrNullTupleList"/&gt;
     *          &lt;/choice&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DATABLOCKTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DataBlockType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MultiPolygonType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A MultiPolygon is defined by one or more Polygons, referenced through polygonMember elements. Deprecated with GML version 3.0. Use MultiSurfaceType instead.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGeometricAggregateType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:polygonMember"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType MULTIPOLYGONTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MultiPolygonType"), MultiPolygon.class, false,
            false, Collections.EMPTY_SET, ABSTRACTGEOMETRICAGGREGATETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DerivedCRSRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a non-projected derived coordinate reference system, either referencing or containing the definition of that reference system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:DerivedCRS"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DERIVEDCRSREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DerivedCRSRefType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="QNameList"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A set of values, representing a list of token with the lexical value space of QName. The tokens are seperated by whitespace.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;list itemType="QName"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType QNAMELIST_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "QNameList"), java.lang.Object.class, false, false,
            Collections.EMPTY_SET, XSSchema.ANYSIMPLETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="RangeSetType"&gt;
     *      &lt;choice&gt;
     *          &lt;element maxOccurs="unbounded" ref="gml:ValueArray"&gt;
     *              &lt;annotation&gt;
     *                  &lt;documentation&gt;each member _Value holds a tuple or "row" from the equivalent table&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *          &lt;group maxOccurs="unbounded" ref="gml:ScalarValueList"&gt;
     *              &lt;annotation&gt;
     *                  &lt;documentation&gt;each list holds the complete set of one scalar component from the values - i.e. a "column" from the equivalent table&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/group&gt;
     *          &lt;element ref="gml:DataBlock"&gt;
     *              &lt;annotation&gt;
     *                  &lt;documentation&gt;Its tuple list holds the values as space-separated tuples each of which contains comma-separated components, and the tuple structure is specified using the rangeParameters property.&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *          &lt;element ref="gml:File"&gt;
     *              &lt;annotation&gt;
     *                  &lt;documentation&gt;a reference to an external source for the data, together with a description of how that external source is structured&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *      &lt;/choice&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType RANGESETTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "RangeSetType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="HistoryPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;The history relationship associates a feature with a sequence of TimeSlice instances.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence maxOccurs="unbounded"&gt;
     *          &lt;element ref="gml:_TimeSlice"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType HISTORYPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "HistoryPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TrackType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;The track of a moving object is a sequence of specialized timeslices        that indicate the status of the object.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:HistoryPropertyType"&gt;
     *              &lt;sequence maxOccurs="unbounded"&gt;
     *                  &lt;element ref="gml:MovingObjectStatus"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TRACKTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TrackType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, HISTORYPROPERTYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GeometryStyleType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;[complexType of] The style descriptor for geometries of a feature.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:BaseStyleDescriptorType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;choice&gt;
     *                      &lt;element ref="gml:symbol"/&gt;
     *                      &lt;element name="style" type="string"&gt;
     *                          &lt;annotation&gt;
     *                              &lt;appinfo&gt;deprecated&lt;/appinfo&gt;
     *                              &lt;documentation&gt;Deprecated in GML version 3.1.0. Use symbol with inline content instead.&lt;/documentation&gt;
     *                          &lt;/annotation&gt;
     *                      &lt;/element&gt;
     *                  &lt;/choice&gt;
     *                  &lt;element minOccurs="0" ref="gml:labelStyle"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute name="geometryProperty" type="string"/&gt;
     *              &lt;attribute name="geometryType" type="string"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GEOMETRYSTYLETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GeometryStyleType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, BASESTYLEDESCRIPTORTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DirectionPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation/&gt;
     *      &lt;/annotation&gt;
     *      &lt;choice&gt;
     *          &lt;element ref="gml:DirectionVector"/&gt;
     *          &lt;element ref="gml:CompassPoint"/&gt;
     *          &lt;element name="DirectionKeyword" type="gml:CodeType"/&gt;
     *          &lt;element name="DirectionString" type="gml:StringOrRefType"/&gt;
     *      &lt;/choice&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DIRECTIONPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DirectionPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="NCNameList"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A set of values, representing a list of token with the lexical value space of NCName. The tokens are seperated by whitespace.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;list itemType="NCName"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType NCNAMELIST_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "NCNameList"), java.lang.Object.class, false, false,
            Collections.EMPTY_SET, XSSchema.ANYSIMPLETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="DecimalMinutesType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Decimal number of arc-minutes in a degree-minute angular value.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="decimal"&gt;
     *          &lt;minInclusive value="0.00"/&gt;
     *          &lt;maxExclusive value="60.00"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType DECIMALMINUTESTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DecimalMinutesType"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, XSSchema.DECIMAL_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractTimeSliceType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;A timeslice encapsulates the time-varying properties of a dynamic feature--it
     *          must be extended to represent a timestamped projection of a feature. The dataSource
     *          property describes how the temporal data was acquired.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGMLType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:validTime"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:dataSource"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTTIMESLICETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractTimeSliceType"), Collections.EMPTY_LIST,
            false, true, Collections.EMPTY_SET, ABSTRACTGMLTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MovingObjectStatusType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;This type encapsulates various dynamic properties of moving objects
     *               (points, lines, regions). It is useful for dealing with features whose
     *               geometry or topology changes over time.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTimeSliceType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:location"/&gt;
     *                  &lt;element minOccurs="0" name="speed" type="gml:MeasureType"/&gt;
     *                  &lt;element minOccurs="0" name="bearing" type="gml:DirectionPropertyType"/&gt;
     *                  &lt;element minOccurs="0" name="acceleration" type="gml:MeasureType"/&gt;
     *                  &lt;element minOccurs="0" name="elevation" type="gml:MeasureType"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:status"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType MOVINGOBJECTSTATUSTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MovingObjectStatusType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTTIMESLICETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="StyleVariationType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Used to vary individual graphic parameters and attributes of the style, symbol or text.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="string"&gt;
     *              &lt;attribute name="styleProperty" type="string" use="required"/&gt;
     *              &lt;attribute name="featurePropertyRange" type="string" use="optional"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType STYLEVARIATIONTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "StyleVariationType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.STRING_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ScalarValuePropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Property whose content is a scalar value.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:ValuePropertyType"&gt;
     *              &lt;sequence minOccurs="0"&gt;
     *                  &lt;!-- &lt;element ref="gml:_ScalarValue"/&gt; --&gt;
     *                  &lt;group ref="gml:ScalarValue"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType SCALARVALUEPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ScalarValuePropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, VALUEPROPERTYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="integerOrNullList"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;XML List based on the union type defined above.  An element declared with this type contains a space-separated list of integer values with null values interspersed as needed&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;list itemType="gml:integerOrNull"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType INTEGERORNULLLIST_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "integerOrNullList"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, XSSchema.ANYSIMPLETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="CountExtentType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Restriction of list type to store a 2-point range of frequency values. If one member is a null, then this is a single ended interval.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="gml:integerOrNullList"&gt;
     *          &lt;length value="2"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType COUNTEXTENTTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "CountExtentType"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, INTEGERORNULLLIST_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DirectPositionType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;DirectPosition instances hold the coordinates for a position within some coordinate reference system (CRS). Since
     *                          DirectPositions, as data types, will often be included in larger objects (such as geometry elements) that have references to CRS, the
     *                          "srsName" attribute will in general be missing, if this particular DirectPosition is included in a larger element with such a reference to a
     *                          CRS. In this case, the CRS is implicitly assumed to take on the value of the containing object's CRS.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:doubleList"&gt;
     *              &lt;attributeGroup ref="gml:SRSReferenceGroup"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DIRECTPOSITIONTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DirectPositionType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, DOUBLELIST_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimeTopologyPrimitivePropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A time topology primitive property can either hold any time topology complex element
     *                           eor carry an XLink reference to a remote time topology complex element
     *                           (where remote includes elements located elsewhere in the same document).
     *                           Note that either the reference or the contained element must be given, but not both or none.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:_TimeTopologyPrimitive"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMETOPOLOGYPRIMITIVEPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeTopologyPrimitivePropertyType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GridLimitsType"&gt;
     *      &lt;sequence&gt;
     *          &lt;element name="GridEnvelope" type="gml:GridEnvelopeType"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GRIDLIMITSTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GridLimitsType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="PointArrayPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A container for an array of points. The elements are always contained in the array property, referencing geometry
     *                          elements or arrays of geometry elements is not supported.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:Point"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType POINTARRAYPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "PointArrayPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimePeriodPropertyType"&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:TimePeriod"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMEPERIODPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimePeriodPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GeodeticDatumType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A geodetic datum defines the precise location and orientation in 3-dimensional space of a defined ellipsoid (or sphere) that approximates the shape of the earth, or of a Cartesian coordinate system centered in this ellipsoid (or sphere). &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractDatumType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:usesPrimeMeridian"/&gt;
     *                  &lt;element ref="gml:usesEllipsoid"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GEODETICDATUMTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GeodeticDatumType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTDATUMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="RectifiedGridCoverageType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:AbstractDiscreteCoverageType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;group ref="gml:StandardObjectProperties"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:boundedBy"/&gt;
     *                  &lt;element ref="gml:rectifiedGridDomain"/&gt;
     *                  &lt;element ref="gml:rangeSet"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:coverageFunction"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType RECTIFIEDGRIDCOVERAGETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "RectifiedGridCoverageType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTDISCRETECOVERAGETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="doubleOrNullList"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;XML List based on the union type defined above.  An element declared with this type contains a space-separated list of double values with null values interspersed as needed&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;list itemType="gml:doubleOrNull"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType DOUBLEORNULLLIST_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "doubleOrNullList"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, XSSchema.ANYSIMPLETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MeasureOrNullListType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;List of numbers with a uniform scale.
     *        A member of the list may be a typed null.
     *        The value of uom (Units Of Measure) attribute is a reference to
     *        a Reference System for the amount, either a ratio or position scale. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:doubleOrNullList"&gt;
     *              &lt;attribute name="uom" type="anyURI" use="required"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType MEASUREORNULLLISTTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MeasureOrNullListType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, DOUBLEORNULLLIST_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="QuantityExtentType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Restriction of list type to store a 2-point range of numeric values. If one member is a null, then this is a single ended interval.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;restriction base="gml:MeasureOrNullListType"&gt;
     *              &lt;length value="2"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType QUANTITYEXTENTTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "QuantityExtentType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, MEASUREORNULLLISTTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TopoSolidType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;The topological boundary of a TopoSolid consists of a set of directed faces. Note that all faces associated with the TopoSolid, including dangling faces, appear in the boundary. The coboundary of a TopoSolid is empty and hence requires no representation.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTopoPrimitiveType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" ref="gml:directedFace"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TOPOSOLIDTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TopoSolidType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTTOPOPRIMITIVETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="OperationParameterGroupBaseType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Basic encoding for operation parameter group objects, simplifying and restricting the DefinitionType as needed. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:AbstractGeneralOperationParameterType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:metaDataProperty"/&gt;
     *                  &lt;element ref="gml:groupName"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:minimumOccurs"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute ref="gml:id" use="required"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType OPERATIONPARAMETERGROUPBASETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "OperationParameterGroupBaseType"),
            Collections.EMPTY_LIST, false, true, Collections.EMPTY_SET,
            ABSTRACTGENERALOPERATIONPARAMETERTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="OperationParameterGroupType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;The definition of a group of parameters used by an operation method. This complexType is expected to be used or extended for all applicable operation methods, without defining operation-method-specialized element names.  &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:OperationParameterGroupBaseType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:groupID"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Set of alternative identifications of this operation parameter group. The first groupID, if any, is normally the primary identification code, and any others are aliases. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element minOccurs="0" ref="gml:remarks"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Comments on or information about this operation parameter group, including source information. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element minOccurs="0" ref="gml:maximumOccurs"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="2" ref="gml:includesParameter"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;Unordered list of associations to the set of operation parameters that are members of this group. &lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType OPERATIONPARAMETERGROUPTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "OperationParameterGroupType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET,
            OPERATIONPARAMETERGROUPBASETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ObliqueCartesianCSRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to an oblique-Cartesian coordinate system, either referencing or containing the definition of that coordinate system. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:ObliqueCartesianCS"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType OBLIQUECARTESIANCSREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ObliqueCartesianCSRefType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="TimeUnitType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;Standard units for measuring time intervals (see ISO 31-1).&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;union&gt;
     *          &lt;simpleType&gt;
     *              &lt;restriction base="string"&gt;
     *                  &lt;enumeration value="year"/&gt;
     *                  &lt;enumeration value="day"/&gt;
     *                  &lt;enumeration value="hour"/&gt;
     *                  &lt;enumeration value="minute"/&gt;
     *                  &lt;enumeration value="second"/&gt;
     *              &lt;/restriction&gt;
     *          &lt;/simpleType&gt;
     *          &lt;simpleType&gt;
     *              &lt;restriction base="string"&gt;
     *                  &lt;pattern value="other:\w{2,}"/&gt;
     *              &lt;/restriction&gt;
     *          &lt;/simpleType&gt;
     *      &lt;/union&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType TIMEUNITTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeUnitType"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, XSSchema.ANYSIMPLETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ArrayType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A non-abstract generic collection type that can be used as a document element for a homogeneous collection of any GML types - Geometries, Topologies, Features ...&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGMLType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element minOccurs="0" ref="gml:members"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ARRAYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ArrayType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, ABSTRACTGMLTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="SymbolTypeEnumeration"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Used to specify the type of the symbol used.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="svg"/&gt;
     *          &lt;enumeration value="xpath"/&gt;
     *          &lt;enumeration value="other"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType SYMBOLTYPEENUMERATION_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "SymbolTypeEnumeration"), java.lang.Object.class,
            false, false, Collections.EMPTY_SET, XSSchema.STRING_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GeodesicStringType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A GeodesicString consists of sequence of
     *     geodesic segments. The type essentially combines a sequence of
     *     Geodesic into a single object.
     *     The GeodesicString is computed from two or more positions and an
     *     interpolation using geodesics defined from the geoid (or
     *     ellipsoid) of the co-ordinate reference system being used.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCurveSegmentType"&gt;
     *              &lt;choice&gt;
     *                  &lt;element ref="gml:posList"/&gt;
     *                  &lt;group maxOccurs="unbounded" minOccurs="2" ref="gml:geometricPositionGroup"/&gt;
     *              &lt;/choice&gt;
     *              &lt;attribute fixed="geodesic" name="interpolation" type="gml:CurveInterpolationType"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;The attribute "interpolation" specifies the
     *       curve interpolation mechanism used for this segment. This
     *       mechanism uses the control points and control parameters to
     *       determine the position of this curve segment. For an
     *       GeodesicString the interpolation is fixed as "geodesic".&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GEODESICSTRINGTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GeodesicStringType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTCURVESEGMENTTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GeodesicType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A Geodesic consists of two distinct
     *     positions joined by a geodesic curve. The control points of
     *     a Geodesic shall lie on the geodesic between its start
     *     point and end points. Between these two points, a geodesic
     *     curve defined from ellipsoid or geoid model used by the
     *     co-ordinate reference systems may be used to interpolate
     *     other positions. Any other point in the controlPoint array
     *     must fall on this geodesic.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:GeodesicStringType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GEODESICTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GeodesicType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, GEODESICSTRINGTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="PointType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A Point is defined by a single coordinate tuple.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGeometricPrimitiveType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;choice&gt;
     *                      &lt;annotation&gt;
     *                          &lt;documentation&gt;GML supports two different ways to specify the direct poisiton of a point. 1. The "pos" element is of type
     *                                                          DirectPositionType.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                      &lt;element ref="gml:pos"/&gt;
     *                      &lt;element ref="gml:coordinates"&gt;
     *                          &lt;annotation&gt;
     *                              &lt;documentation&gt;Deprecated with GML version 3.1.0 for coordinates with ordinate values that are numbers. Use "pos"
     *                                                                  instead. The "coordinates" element shall only be used for coordinates with ordinates that require a string
     *                                                                  representation, e.g. DMS representations.&lt;/documentation&gt;
     *                          &lt;/annotation&gt;
     *                      &lt;/element&gt;
     *                      &lt;element ref="gml:coord"&gt;
     *                          &lt;annotation&gt;
     *                              &lt;documentation&gt;Deprecated with GML version 3.0. Use "pos" instead. The "coord" element is included for
     *                                                                  backwards compatibility with GML 2.&lt;/documentation&gt;
     *                          &lt;/annotation&gt;
     *                      &lt;/element&gt;
     *                  &lt;/choice&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType POINTTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "PointType"), Point.class, false, false,
            Collections.EMPTY_SET, ABSTRACTGEOMETRICPRIMITIVETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractFeatureCollectionType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A feature collection contains zero or more features.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractFeatureType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:featureMember"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:featureMembers"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    private static List ABSTRACTFEATURECOLLECTIONTYPE_TYPE_schema = new ArrayList();

    static {
        ABSTRACTFEATURECOLLECTIONTYPE_TYPE_schema.add(new AttributeDescriptorImpl(
                FEATUREPROPERTYTYPE_TYPE, new Name("http://www.opengis.net/gml", "featureMember"),
                0, 2147483647, false, null));
        ABSTRACTFEATURECOLLECTIONTYPE_TYPE_schema.add(new AttributeDescriptorImpl(
                FEATUREARRAYPROPERTYTYPE_TYPE,
                new Name("http://www.opengis.net/gml", "featureMembers"), 0, 1, false, null));
    }

    public static final FeatureType ABSTRACTFEATURECOLLECTIONTYPE_TYPE = new FeatureTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractFeatureCollectionType"),
            ABSTRACTFEATURECOLLECTIONTYPE_TYPE_schema, null, null, true, Collections.EMPTY_SET,
            ABSTRACTFEATURETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="FeatureCollectionType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Concrete generic feature collection.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractFeatureCollectionType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType FEATURECOLLECTIONTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "FeatureCollectionType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTFEATURECOLLECTIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DynamicFeatureCollectionType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A dynamic feature collection may possess a history and/or a timestamp.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:FeatureCollectionType"&gt;
     *              &lt;group ref="gml:dynamicProperties"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DYNAMICFEATURECOLLECTIONTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DynamicFeatureCollectionType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET,
            FEATURECOLLECTIONTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="VerticalDatumType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A textual description and/or a set of parameters identifying a particular reference level surface used as a zero-height surface, including its position with respect to the Earth for any of the height types recognized by this standard. There are several types of Vertical Datums, and each may place constraints on the Coordinate Axis with which it is combined to create a Vertical CRS. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractDatumType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element minOccurs="0" ref="gml:verticalDatumType"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType VERTICALDATUMTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "VerticalDatumType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTDATUMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DerivationUnitTermType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Definition of one unit term for a derived unit of measure. This unit term references another unit of measure (uom) and provides an integer exponent applied to that unit in defining the compound unit. The exponent can be positive or negative, but not zero.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:UnitOfMeasureType"&gt;
     *              &lt;attribute name="exponent" type="integer"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DERIVATIONUNITTERMTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DerivationUnitTermType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, UNITOFMEASURETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="DegreeValueType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Integer number of degrees in a degree-minute-second or degree-minute angular value, without indication of direction.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="nonNegativeInteger"&gt;
     *          &lt;maxInclusive value="359"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType DEGREEVALUETYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DegreeValueType"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, XSSchema.NONNEGATIVEINTEGER_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DegreesType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Integer number of degrees, plus the angle direction. This element can be used for geographic Latitude and Longitude. For Latitude, the XML attribute direction can take the values "N" or "S", meaning North or South of the equator. For Longitude, direction can take the values "E" or "W", meaning East or West of the prime meridian. This element can also be used for other angles. In that case, the direction can take the values "+" or "-" (of SignType), in the specified rotational direction from a specified reference direction.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:DegreeValueType"&gt;
     *              &lt;attribute name="direction"&gt;
     *                  &lt;simpleType&gt;
     *                      &lt;union&gt;
     *                          &lt;simpleType&gt;
     *                              &lt;restriction base="string"&gt;
     *                                  &lt;enumeration value="N"/&gt;
     *                                  &lt;enumeration value="E"/&gt;
     *                                  &lt;enumeration value="S"/&gt;
     *                                  &lt;enumeration value="W"/&gt;
     *                              &lt;/restriction&gt;
     *                          &lt;/simpleType&gt;
     *                          &lt;simpleType&gt;
     *                              &lt;restriction base="gml:SignType"/&gt;
     *                          &lt;/simpleType&gt;
     *                      &lt;/union&gt;
     *                  &lt;/simpleType&gt;
     *              &lt;/attribute&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DEGREESTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DegreesType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, DEGREEVALUETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="AbstractGriddedSurfaceType_row"&gt;
     *      &lt;sequence&gt;
     *          &lt;group ref="gml:geometricPositionListGroup"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTGRIDDEDSURFACETYPE_ROW_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AbstractGriddedSurfaceType_row"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DirectedEdgePropertyType"&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:Edge"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attribute default="+" name="orientation" type="gml:SignType"/&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DIRECTEDEDGEPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "DirectedEdgePropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MultiCurveType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A MultiCurve is defined by one or more Curves, referenced through curveMember elements.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGeometricAggregateType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;annotation&gt;
     *                      &lt;documentation&gt;The members of the geometric aggregate can be specified either using the "standard" property or the array property style. It is also valid to use both the "standard" and the array property style in the same collection.
     *  NOTE: Array properties cannot reference remote geometry elements.&lt;/documentation&gt;
     *                  &lt;/annotation&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:curveMember"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:curveMembers"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType MULTICURVETYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "MultiCurveType"), MultiLineString[].class, false,
            false, Collections.EMPTY_SET, ABSTRACTGEOMETRICAGGREGATETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimeNodeType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation xml:lang="en"&gt;Type declaration of the element "TimeNode".&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTimeTopologyPrimitiveType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0"
     *                      name="previousEdge" type="gml:TimeEdgePropertyType"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0"
     *                      name="nextEdge" type="gml:TimeEdgePropertyType"/&gt;
     *                  &lt;element minOccurs="0" name="position" type="gml:TimeInstantPropertyType"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMENODETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeNodeType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTTIMETOPOLOGYPRIMITIVETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="stringOrNull"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Union of the XML Schema string type and the GML Nulltype.  An element which uses this type may have content which is either a string or a value from Nulltype.  Note that a "string" may contain whitespace.  &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;union memberTypes="gml:NullEnumeration string anyURI"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType STRINGORNULL_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "stringOrNull"), java.lang.Object.class, false,
            false, Collections.EMPTY_SET, XSSchema.ANYSIMPLETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="SolidType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A solid is the basis for 3-dimensional geometry. The extent of a solid is defined by the boundary surfaces (shells). A shell is represented by a composite surface, where every  shell is used to represent a single connected component of the boundary of a solid. It consists of a composite surface (a list of orientable surfaces) connected in a topological cycle (an object whose boundary is empty). Unlike a Ring, a Shell's elements have no natural sort order. Like Rings, Shells are simple.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractSolidType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element minOccurs="0" name="exterior" type="gml:SurfacePropertyType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;appinfo&gt;
     *                              &lt;sch:pattern name="Check either href or content not both"&gt;
     *                                  &lt;sch:rule context="gml:exterior"&gt;
     *                                      &lt;sch:extends rule="hrefOrContent"/&gt;
     *                                  &lt;/sch:rule&gt;
     *                              &lt;/sch:pattern&gt;
     *                          &lt;/appinfo&gt;
     *                          &lt;documentation&gt;Boundaries of solids are similar to surface boundaries. In normal 3-dimensional Euclidean space, one (composite) surface is distinguished as the exterior. In the more general case, this is not always possible.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0"
     *                      name="interior" type="gml:SurfacePropertyType"&gt;
     *                      &lt;annotation&gt;
     *                          &lt;appinfo&gt;
     *                              &lt;sch:pattern name="Check either href or content not both"&gt;
     *                                  &lt;sch:rule context="gml:interior"&gt;
     *                                      &lt;sch:extends rule="hrefOrContent"/&gt;
     *                                  &lt;/sch:rule&gt;
     *                              &lt;/sch:pattern&gt;
     *                          &lt;/appinfo&gt;
     *                          &lt;documentation&gt;Boundaries of solids are similar to surface boundaries.&lt;/documentation&gt;
     *                      &lt;/annotation&gt;
     *                  &lt;/element&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType SOLIDTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "SolidType"), Collections.EMPTY_LIST, false, false,
            Collections.EMPTY_SET, ABSTRACTSOLIDTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TemporalCRSType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A 1D coordinate reference system used for the recording of time. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractReferenceSystemType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:usesTemporalCS"/&gt;
     *                  &lt;element ref="gml:usesTemporalDatum"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TEMPORALCRSTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TemporalCRSType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTREFERENCESYSTEMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="LabelStylePropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation/&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element minOccurs="0" ref="gml:LabelStyle"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attribute name="about" type="anyURI" use="optional"/&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType LABELSTYLEPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "LabelStylePropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ObliqueCartesianCSType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A two- or three-dimensional coordinate system with straight axes that are not necessarily orthogonal. An ObliqueCartesianCS shall have two or three usesAxis associations. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCoordinateSystemType"/&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType OBLIQUECARTESIANCSTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ObliqueCartesianCSType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, ABSTRACTCOORDINATESYSTEMTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ProjectedCRSType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A 2D coordinate reference system used to approximate the shape of the earth on a planar surface, but in such a way that the distortion that is inherent to the approximation is carefully controlled and known. Distortion correction is commonly applied to calculated bearings and distances to produce values that are a close match to actual field values. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractGeneralDerivedCRSType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:usesCartesianCS"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType PROJECTEDCRSTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "ProjectedCRSType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTGENERALDERIVEDCRSTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimeInstantPropertyType"&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:TimeInstant"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMEINSTANTPROPERTYTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TimeInstantPropertyType"), Collections.EMPTY_LIST,
            false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GridDomainType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;restriction base="gml:DomainSetType"&gt;
     *              &lt;sequence minOccurs="0"&gt;
     *                  &lt;element ref="gml:Grid"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GRIDDOMAINTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GridDomainType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, DOMAINSETTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="PolygonType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A Polygon is a special surface that is defined by a single surface patch. The boundary of this patch is coplanar and the polygon uses planar interpolation in its interior. It is backwards compatible with the Polygon of GML 2, GM_Polygon of ISO 19107 is implemented by PolygonPatch.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractSurfaceType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element minOccurs="0" ref="gml:exterior"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:interior"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType POLYGONTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "PolygonType"), Polygon.class, false, false,
            Collections.EMPTY_SET, ABSTRACTSURFACETYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
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
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ASSOCIATIONTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "AssociationType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GeneralTransformationRefType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Association to a general transformation, either referencing or containing the definition of that transformation. &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence minOccurs="0"&gt;
     *          &lt;element ref="gml:_GeneralTransformation"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GENERALTRANSFORMATIONREFTYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "GeneralTransformationRefType"),
            Collections.EMPTY_LIST, false, false, Collections.EMPTY_SET, XSSchema.ANYTYPE_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="SurfaceInterpolationType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;SurfaceInterpolationType is a list of codes that may be used to identify the interpolation mechanisms specified by an
     *  application schema.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="none"/&gt;
     *          &lt;enumeration value="planar"/&gt;
     *          &lt;enumeration value="spherical"/&gt;
     *          &lt;enumeration value="elliptical"/&gt;
     *          &lt;enumeration value="conic"/&gt;
     *          &lt;enumeration value="tin"/&gt;
     *          &lt;enumeration value="parametricCurve"/&gt;
     *          &lt;enumeration value="polynomialSpline"/&gt;
     *          &lt;enumeration value="rationalSpline"/&gt;
     *          &lt;enumeration value="triangulatedSpline"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType SURFACEINTERPOLATIONTYPE_TYPE = new AttributeTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "SurfaceInterpolationType"), java.lang.Object.class,
            false, false, Collections.EMPTY_SET, XSSchema.STRING_TYPE, null);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TopoVolumeType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;The TopoVolume type and element represent a homogeneous topological expression, a set of directed TopoSolids, which if realised are isomorphic to a geometric solid primitive. The intended use of TopoVolume is to appear within a 3D solid feature instance to express the structural and geometric relationships of this solid to other features via the shared TopoSolid definitions.  . Note the orientation assigned to the directedSolid has no meaning in three dimensions. It is preserved for symmetry with the preceding types and elements.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractTopologyType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element maxOccurs="unbounded" ref="gml:directedTopoSolid"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TOPOVOLUMETYPE_TYPE = new ComplexTypeImpl(new TypeName(
                "http://www.opengis.net/gml", "TopoVolumeType"), Collections.EMPTY_LIST, false,
            false, Collections.EMPTY_SET, ABSTRACTTOPOLOGYTYPE_TYPE, null);

    public GMLSchema() {
        super("http://www.opengis.net/gml");

        put(new TypeName("http://www.opengis.net/gml", "CartesianCSRefType"),
            CARTESIANCSREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractPositionalAccuracyType"),
            ABSTRACTPOSITIONALACCURACYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "RelativeInternalPositionalAccuracyType"),
            RELATIVEINTERNALPOSITIONALACCURACYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "SurfacePropertyType"),
            SURFACEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ArcMinutesType"), ARCMINUTESTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeCalendarPropertyType"),
            TIMECALENDARPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractGMLType"), ABSTRACTGMLTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractGeometryType"),
            ABSTRACTGEOMETRYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractGeometricAggregateType"),
            ABSTRACTGEOMETRICAGGREGATETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MultiPointType"), MULTIPOINTTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DomainSetType"), DOMAINSETTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "RectifiedGridDomainType"),
            RECTIFIEDGRIDDOMAINTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TopoVolumePropertyType"),
            TOPOVOLUMEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractTopologyType"),
            ABSTRACTTOPOLOGYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TopoCurveType"), TOPOCURVETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CodeType"), CODETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "VerticalDatumTypeType"),
            VERTICALDATUMTYPETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TopoPointPropertyType"),
            TOPOPOINTPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "NullEnumeration"), NULLENUMERATION_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractSurfacePatchType"),
            ABSTRACTSURFACEPATCHTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "RectangleType"), RECTANGLETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "BaseStyleDescriptorType"),
            BASESTYLEDESCRIPTORTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TopologyStyleType"), TOPOLOGYSTYLETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MultiGeometryType"), MULTIGEOMETRYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "LinearCSRefType"), LINEARCSREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeEdgePropertyType"),
            TIMEEDGEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimePrimitivePropertyType"),
            TIMEPRIMITIVEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "RelatedTimeType"), RELATEDTIMETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ValuePropertyType"), VALUEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CategoryPropertyType"),
            CATEGORYPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "FileValueModelType"),
            FILEVALUEMODELTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractTopoPrimitiveType"),
            ABSTRACTTOPOPRIMITIVETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "EdgeType"), EDGETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractGeometricPrimitiveType"),
            ABSTRACTGEOMETRICPRIMITIVETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractSolidType"), ABSTRACTSOLIDTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CompositeSolidType"),
            COMPOSITESOLIDTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DefinitionType"), DEFINITIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractCoordinateOperationBaseType"),
            ABSTRACTCOORDINATEOPERATIONBASETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractCoordinateOperationType"),
            ABSTRACTCOORDINATEOPERATIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractGeneralTransformationType"),
            ABSTRACTGENERALTRANSFORMATIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TransformationType"),
            TRANSFORMATIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DirectedNodePropertyType"),
            DIRECTEDNODEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "FormulaType"), FORMULATYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractGeneralConversionType"),
            ABSTRACTGENERALCONVERSIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ConversionType"), CONVERSIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TopoSurfacePropertyType"),
            TOPOSURFACEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractCurveType"), ABSTRACTCURVETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CurveType"), CURVETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AffinePlacementType"),
            AFFINEPLACEMENTTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GeometryPropertyType"),
            GEOMETRYPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MeasureType"), MEASURETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "SpeedType"), SPEEDTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "EllipsoidBaseType"), ELLIPSOIDBASETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "EllipsoidType"), ELLIPSOIDTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "UserDefinedCSRefType"),
            USERDEFINEDCSREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CoordinateReferenceSystemRefType"),
            COORDINATEREFERENCESYSTEMREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "OrientableCurveType"),
            ORIENTABLECURVETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "booleanOrNull"), BOOLEANORNULL_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GeodeticDatumRefType"),
            GEODETICDATUMREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GeneralConversionRefType"),
            GENERALCONVERSIONREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CurveInterpolationType"),
            CURVEINTERPOLATIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "LineStringSegmentArrayPropertyType"),
            LINESTRINGSEGMENTARRAYPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractParametricCurveSurfaceType"),
            ABSTRACTPARAMETRICCURVESURFACETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractGriddedSurfaceType"),
            ABSTRACTGRIDDEDSURFACETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "SphereType"), SPHERETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "RingPropertyType"), RINGPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "OperationRefType"), OPERATIONREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "EllipsoidalCSRefType"),
            ELLIPSOIDALCSREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "integerList"), INTEGERLIST_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractRingType"), ABSTRACTRINGTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "LinearRingType"), LINEARRINGTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimePositionUnion"), TIMEPOSITIONUNION_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimePositionType"), TIMEPOSITIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CompositeCurvePropertyType"),
            COMPOSITECURVEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DirectedTopoSolidPropertyType"),
            DIRECTEDTOPOSOLIDPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "SignType"), SIGNTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "SymbolType"), SYMBOLTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TemporalCRSRefType"),
            TEMPORALCRSREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MultiCurveDomainType"),
            MULTICURVEDOMAINTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractStyleType"), ABSTRACTSTYLETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "StyleType"), STYLETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractGeneralOperationParameterRefType"),
            ABSTRACTGENERALOPERATIONPARAMETERREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TransformationRefType"),
            TRANSFORMATIONREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractTimeObjectType"),
            ABSTRACTTIMEOBJECTTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractTimePrimitiveType"),
            ABSTRACTTIMEPRIMITIVETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractTimeTopologyPrimitiveType"),
            ABSTRACTTIMETOPOLOGYPRIMITIVETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeEdgeType"), TIMEEDGETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GraphTypeType"), GRAPHTYPETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "SphericalCSRefType"),
            SPHERICALCSREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GridFunctionType"), GRIDFUNCTIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "IndexMapType"), INDEXMAPTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractGeneralParameterValueType"),
            ABSTRACTGENERALPARAMETERVALUETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ParameterValueType"),
            PARAMETERVALUETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractTimeGeometricPrimitiveType"),
            ABSTRACTTIMEGEOMETRICPRIMITIVETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimePeriodType"), TIMEPERIODTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractFeatureType"),
            ABSTRACTFEATURETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DynamicFeatureType"),
            DYNAMICFEATURETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TargetPropertyType"),
            TARGETPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ArcSecondsType"), ARCSECONDSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeOrdinalEraType"),
            TIMEORDINALERATYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MultiLineStringType"),
            MULTILINESTRINGTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AreaType"), AREATYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CompassPointEnumeration"),
            COMPASSPOINTENUMERATION_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CoordinatesType"), COORDINATESTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "SolidArrayPropertyType"),
            SOLIDARRAYPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "PointPropertyType"), POINTPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TinType_controlPoint"),
            TINTYPE_CONTROLPOINT_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DefinitionProxyType"),
            DEFINITIONPROXYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractCoverageType"),
            ABSTRACTCOVERAGETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractDiscreteCoverageType"),
            ABSTRACTDISCRETECOVERAGETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MultiCurveCoverageType"),
            MULTICURVECOVERAGETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TopoCurvePropertyType"),
            TOPOCURVEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "PolarCSRefType"), POLARCSREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "IncrementOrder"), INCREMENTORDER_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GeometryStylePropertyType"),
            GEOMETRYSTYLEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MultiCurvePropertyType"),
            MULTICURVEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ProjectedCRSRefType"),
            PROJECTEDCRSREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "StringOrRefType"), STRINGORREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DrawingTypeType"), DRAWINGTYPETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractCoordinateSystemBaseType"),
            ABSTRACTCOORDINATESYSTEMBASETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractCoordinateSystemType"),
            ABSTRACTCOORDINATESYSTEMTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "PolarCSType"), POLARCSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MultiPointDomainType"),
            MULTIPOINTDOMAINTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MultiPolygonPropertyType"),
            MULTIPOLYGONPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TopoPointType"), TOPOPOINTTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractTimeReferenceSystemType"),
            ABSTRACTTIMEREFERENCESYSTEMTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeOrdinalReferenceSystemType"),
            TIMEORDINALREFERENCESYSTEMTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "OperationParameterGroupRefType"),
            OPERATIONPARAMETERGROUPREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "KnotPropertyType"), KNOTPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GridLengthType"), GRIDLENGTHTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DirectionVectorType"),
            DIRECTIONVECTORTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractCurveSegmentType"),
            ABSTRACTCURVESEGMENTTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ArcByCenterPointType"),
            ARCBYCENTERPOINTTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CircleByCenterPointType"),
            CIRCLEBYCENTERPOINTTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractSurfaceType"),
            ABSTRACTSURFACETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "SurfaceType"), SURFACETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TriangulatedSurfaceType"),
            TRIANGULATEDSURFACETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TinType"), TINTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractRingPropertyType"),
            ABSTRACTRINGPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ClothoidType"), CLOTHOIDTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "SolidPropertyType"), SOLIDPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractDatumBaseType"),
            ABSTRACTDATUMBASETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractDatumType"), ABSTRACTDATUMTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ImageDatumType"), IMAGEDATUMTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "LabelStyleType"), LABELSTYLETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "LinearCSType"), LINEARCSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "integerOrNull"), INTEGERORNULL_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "FeatureStyleType"), FEATURESTYLETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "PrimeMeridianBaseType"),
            PRIMEMERIDIANBASETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "PrimeMeridianType"), PRIMEMERIDIANTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CoordinateSystemAxisBaseType"),
            COORDINATESYSTEMAXISBASETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CoordinateSystemAxisType"),
            COORDINATESYSTEMAXISTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ImageCRSRefType"), IMAGECRSREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AngleChoiceType"), ANGLECHOICETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "FeaturePropertyType"),
            FEATUREPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractReferenceSystemBaseType"),
            ABSTRACTREFERENCESYSTEMBASETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractReferenceSystemType"),
            ABSTRACTREFERENCESYSTEMTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractGeneralDerivedCRSType"),
            ABSTRACTGENERALDERIVEDCRSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DerivedCRSType"), DERIVEDCRSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CylindricalCSRefType"),
            CYLINDRICALCSREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "RingType"), RINGTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ScaleType"), SCALETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "NameOrNullList"), NAMEORNULLLIST_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CodeOrNullListType"),
            CODEORNULLLISTTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CategoryExtentType"),
            CATEGORYEXTENTTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AngleType"), ANGLETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ExtentType"), EXTENTTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "LengthType"), LENGTHTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "SurfacePatchArrayPropertyType"),
            SURFACEPATCHARRAYPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TrianglePatchArrayPropertyType"),
            TRIANGLEPATCHARRAYPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CompositeSurfaceType"),
            COMPOSITESURFACETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "SecondDefiningParameterType"),
            SECONDDEFININGPARAMETERTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ConcatenatedOperationRefType"),
            CONCATENATEDOPERATIONREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeTopologyComplexPropertyType"),
            TIMETOPOLOGYCOMPLEXPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DictionaryEntryType"),
            DICTIONARYENTRYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DefaultStylePropertyType"),
            DEFAULTSTYLEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractMetaDataType"),
            ABSTRACTMETADATATYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GenericMetaDataType"),
            GENERICMETADATATYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "VerticalCRSRefType"),
            VERTICALCRSREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "VerticalCSRefType"), VERTICALCSREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "OperationMethodRefType"),
            OPERATIONMETHODREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "NodeType"), NODETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GeographicCRSRefType"),
            GEOGRAPHICCRSREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "FaceType"), FACETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "SphericalCSType"), SPHERICALCSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GridType"), GRIDTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "RectifiedGridType"), RECTIFIEDGRIDTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GeocentricCRSRefType"),
            GEOCENTRICCRSREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeClockType"), TIMECLOCKTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "BSplineType"), BSPLINETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "BezierType"), BEZIERTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "_isSphere"), _ISSPHERE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DatumRefType"), DATUMREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MultiSurfacePropertyType"),
            MULTISURFACEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ConeType"), CONETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CompositeCurveType"),
            COMPOSITECURVETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ObservationType"), OBSERVATIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DirectedObservationType"),
            DIRECTEDOBSERVATIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DirectedObservationAtDistanceType"),
            DIRECTEDOBSERVATIONATDISTANCETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "doubleOrNull"), DOUBLEORNULL_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ContainerPropertyType"),
            CONTAINERPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AesheticCriteriaType"),
            AESHETICCRITERIATYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "OffsetCurveType"), OFFSETCURVETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CompoundCRSType"), COMPOUNDCRSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ImageDatumRefType"), IMAGEDATUMREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "VerticalDatumRefType"),
            VERTICALDATUMREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CalDate"), CALDATE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MetaDataPropertyType"),
            METADATAPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeCalendarType"), TIMECALENDARTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CylinderType"), CYLINDERTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "OperationMethodBaseType"),
            OPERATIONMETHODBASETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "OperationMethodType"),
            OPERATIONMETHODTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ArcStringType"), ARCSTRINGTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ArcType"), ARCTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CircleType"), CIRCLETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "doubleList"), DOUBLELIST_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DirectPositionListType"),
            DIRECTPOSITIONLISTTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "UserDefinedCSType"), USERDEFINEDCSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TopoSurfaceType"), TOPOSURFACETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CurvePropertyType"), CURVEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ConcatenatedOperationType"),
            CONCATENATEDOPERATIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "OrientableSurfaceType"),
            ORIENTABLESURFACETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractTimeComplexType"),
            ABSTRACTTIMECOMPLEXTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeTopologyComplexType"),
            TIMETOPOLOGYCOMPLEXTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GridCoverageType"), GRIDCOVERAGETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CoordinateOperationRefType"),
            COORDINATEOPERATIONREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "EnvelopeType"), ENVELOPETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "EnvelopeWithTimePeriodType"),
            ENVELOPEWITHTIMEPERIODTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "BooleanPropertyType"),
            BOOLEANPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CoordType"), COORDTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MultiSurfaceCoverageType"),
            MULTISURFACECOVERAGETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GridEnvelopeType"), GRIDENVELOPETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TemporalCSRefType"), TEMPORALCSREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "LineStringSegmentType"),
            LINESTRINGSEGMENTTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "FeatureArrayPropertyType"),
            FEATUREARRAYPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "SingleOperationRefType"),
            SINGLEOPERATIONREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DirectedFacePropertyType"),
            DIRECTEDFACEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ValueArrayPropertyType"),
            VALUEARRAYPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ReferenceSystemRefType"),
            REFERENCESYSTEMREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeCalendarEraType"),
            TIMECALENDARERATYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "VerticalCRSType"), VERTICALCRSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CurveArrayPropertyType"),
            CURVEARRAYPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ParameterValueGroupType"),
            PARAMETERVALUEGROUPTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MultiSolidCoverageType"),
            MULTISOLIDCOVERAGETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "LinearRingPropertyType"),
            LINEARRINGPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MultiSurfaceType"), MULTISURFACETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "KnotType"), KNOTTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CoverageFunctionType"),
            COVERAGEFUNCTIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "SurfaceArrayPropertyType"),
            SURFACEARRAYPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GraphStylePropertyType"),
            GRAPHSTYLEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GeometricComplexType"),
            GEOMETRICCOMPLEXTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractContinuousCoverageType"),
            ABSTRACTCONTINUOUSCOVERAGETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CovarianceMatrixType"),
            COVARIANCEMATRIXTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DMSAngleType"), DMSANGLETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ArrayAssociationType"),
            ARRAYASSOCIATIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MeasureListType"), MEASURELISTTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TemporalCSType"), TEMPORALCSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "RangeParametersType"),
            RANGEPARAMETERSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ImageCRSType"), IMAGECRSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "EllipsoidRefType"), ELLIPSOIDREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MultiPointPropertyType"),
            MULTIPOINTPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "LocationPropertyType"),
            LOCATIONPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "PriorityLocationPropertyType"),
            PRIORITYLOCATIONPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MultiSolidDomainType"),
            MULTISOLIDDOMAINTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "PassThroughOperationType"),
            PASSTHROUGHOPERATIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CubicSplineType"), CUBICSPLINETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "EngineeringCRSRefType"),
            ENGINEERINGCRSREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DictionaryType"), DICTIONARYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "LineStringPropertyType"),
            LINESTRINGPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ReferenceType"), REFERENCETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeNodePropertyType"),
            TIMENODEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "PolygonPatchType"), POLYGONPATCHTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TemporalDatumBaseType"),
            TEMPORALDATUMBASETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TemporalDatumType"), TEMPORALDATUMTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeClockPropertyType"),
            TIMECLOCKPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CompoundCRSRefType"),
            COMPOUNDCRSREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "FileType"), FILETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "NameOrNull"), NAMEORNULL_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "SequenceRuleNames"), SEQUENCERULENAMES_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "SequenceRuleType"), SEQUENCERULETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "LineTypeType"), LINETYPETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "PixelInCellType"), PIXELINCELLTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ConversionRefType"), CONVERSIONREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "NullType"), NULLTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "UnitDefinitionType"),
            UNITDEFINITIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "BaseUnitType"), BASEUNITTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "VectorType"), VECTORTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "BagType"), BAGTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TopoPrimitiveMemberType"),
            TOPOPRIMITIVEMEMBERTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeIntervalLengthType"),
            TIMEINTERVALLENGTHTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CoordinateSystemAxisRefType"),
            COORDINATESYSTEMAXISREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeCoordinateSystemType"),
            TIMECOORDINATESYSTEMTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TriangleType"), TRIANGLETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ConventionalUnitType"),
            CONVENTIONALUNITTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeCalendarEraPropertyType"),
            TIMECALENDARERAPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "OperationParameterRefType"),
            OPERATIONPARAMETERREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "PassThroughOperationRefType"),
            PASSTHROUGHOPERATIONREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "EllipsoidalCSType"), ELLIPSOIDALCSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DerivedUnitType"), DERIVEDUNITTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeType"), TIMETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "booleanList"), BOOLEANLIST_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GeocentricCRSType"), GEOCENTRICCRSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbsoluteExternalPositionalAccuracyType"),
            ABSOLUTEEXTERNALPOSITIONALACCURACYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CartesianCSType"), CARTESIANCSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TemporalDatumRefType"),
            TEMPORALDATUMREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeInstantType"), TIMEINSTANTTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeGeometricPrimitivePropertyType"),
            TIMEGEOMETRICPRIMITIVEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "IsolatedPropertyType"),
            ISOLATEDPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeOrdinalEraPropertyType"),
            TIMEORDINALERAPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CurveSegmentArrayPropertyType"),
            CURVESEGMENTARRAYPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DerivedCRSTypeType"),
            DERIVEDCRSTYPETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "VerticalCSType"), VERTICALCSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "KnotTypesType"), KNOTTYPESTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TopoComplexMemberType"),
            TOPOCOMPLEXMEMBERTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "QueryGrammarEnumeration"),
            QUERYGRAMMARENUMERATION_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "booleanOrNullList"), BOOLEANORNULLLIST_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "PolyhedralSurfaceType"),
            POLYHEDRALSURFACETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CompositeValueType"),
            COMPOSITEVALUETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ValueArrayType"), VALUEARRAYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "PolygonPatchArrayPropertyType"),
            POLYGONPATCHARRAYPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MultiSolidType"), MULTISOLIDTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MultiPointCoverageType"),
            MULTIPOINTCOVERAGETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "NameList"), NAMELIST_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CodeListType"), CODELISTTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TopoComplexType"), TOPOCOMPLEXTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "BoundingShapeType"), BOUNDINGSHAPETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "PrimeMeridianRefType"),
            PRIMEMERIDIANREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractGeneralOperationParameterType"),
            ABSTRACTGENERALOPERATIONPARAMETERTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "OperationParameterBaseType"),
            OPERATIONPARAMETERBASETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "OperationParameterType"),
            OPERATIONPARAMETERTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GeometricPrimitivePropertyType"),
            GEOMETRICPRIMITIVEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CoordinateSystemRefType"),
            COORDINATESYSTEMREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MultiLineStringPropertyType"),
            MULTILINESTRINGPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TopologyStylePropertyType"),
            TOPOLOGYSTYLEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GeographicCRSType"), GEOGRAPHICCRSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CountPropertyType"), COUNTPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "VolumeType"), VOLUMETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CompositeSurfacePropertyType"),
            COMPOSITESURFACEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "EngineeringDatumRefType"),
            ENGINEERINGDATUMREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "LabelType"), LABELTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CompositeSolidPropertyType"),
            COMPOSITESOLIDPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GeometryArrayPropertyType"),
            GEOMETRYARRAYPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CRSRefType"), CRSREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ClothoidType_refLocation"),
            CLOTHOIDTYPE_REFLOCATION_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "EngineeringCRSType"),
            ENGINEERINGCRSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "IndirectEntryType"), INDIRECTENTRYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TopoPrimitiveArrayAssociationType"),
            TOPOPRIMITIVEARRAYASSOCIATIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "FeatureStylePropertyType"),
            FEATURESTYLEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MultiGeometryPropertyType"),
            MULTIGEOMETRYPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "BoundedFeatureType"),
            BOUNDEDFEATURETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "EngineeringDatumType"),
            ENGINEERINGDATUMTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MultiSurfaceDomainType"),
            MULTISURFACEDOMAINTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeIndeterminateValueType"),
            TIMEINDETERMINATEVALUETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "SuccessionType"), SUCCESSIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ArcStringByBulgeType"),
            ARCSTRINGBYBULGETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ArcByBulgeType"), ARCBYBULGETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "IdentifierType"), IDENTIFIERTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GraphStyleType"), GRAPHSTYLETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CovarianceElementType"),
            COVARIANCEELEMENTTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "QuantityPropertyType"),
            QUANTITYPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "PolygonPropertyType"),
            POLYGONPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MultiSolidPropertyType"),
            MULTISOLIDPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CylindricalCSType"), CYLINDRICALCSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "UnitOfMeasureType"), UNITOFMEASURETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ConversionToPreferredUnitType"),
            CONVERSIONTOPREFERREDUNITTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "LineStringType"), LINESTRINGTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GeometricComplexPropertyType"),
            GEOMETRICCOMPLEXPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DataBlockType"), DATABLOCKTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MultiPolygonType"), MULTIPOLYGONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DerivedCRSRefType"), DERIVEDCRSREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "QNameList"), QNAMELIST_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "RangeSetType"), RANGESETTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "HistoryPropertyType"),
            HISTORYPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TrackType"), TRACKTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GeometryStyleType"), GEOMETRYSTYLETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DirectionPropertyType"),
            DIRECTIONPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "NCNameList"), NCNAMELIST_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DecimalMinutesType"),
            DECIMALMINUTESTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractTimeSliceType"),
            ABSTRACTTIMESLICETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MovingObjectStatusType"),
            MOVINGOBJECTSTATUSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "StyleVariationType"),
            STYLEVARIATIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ScalarValuePropertyType"),
            SCALARVALUEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "integerOrNullList"), INTEGERORNULLLIST_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "CountExtentType"), COUNTEXTENTTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DirectPositionType"),
            DIRECTPOSITIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeTopologyPrimitivePropertyType"),
            TIMETOPOLOGYPRIMITIVEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GridLimitsType"), GRIDLIMITSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "PointArrayPropertyType"),
            POINTARRAYPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimePeriodPropertyType"),
            TIMEPERIODPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GeodeticDatumType"), GEODETICDATUMTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "RectifiedGridCoverageType"),
            RECTIFIEDGRIDCOVERAGETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "doubleOrNullList"), DOUBLEORNULLLIST_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MeasureOrNullListType"),
            MEASUREORNULLLISTTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "QuantityExtentType"),
            QUANTITYEXTENTTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TopoSolidType"), TOPOSOLIDTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "OperationParameterGroupBaseType"),
            OPERATIONPARAMETERGROUPBASETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "OperationParameterGroupType"),
            OPERATIONPARAMETERGROUPTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ObliqueCartesianCSRefType"),
            OBLIQUECARTESIANCSREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeUnitType"), TIMEUNITTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ArrayType"), ARRAYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "SymbolTypeEnumeration"),
            SYMBOLTYPEENUMERATION_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GeodesicStringType"),
            GEODESICSTRINGTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GeodesicType"), GEODESICTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "PointType"), POINTTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractFeatureCollectionType"),
            ABSTRACTFEATURECOLLECTIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "FeatureCollectionType"),
            FEATURECOLLECTIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DynamicFeatureCollectionType"),
            DYNAMICFEATURECOLLECTIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "VerticalDatumType"), VERTICALDATUMTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DerivationUnitTermType"),
            DERIVATIONUNITTERMTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DegreeValueType"), DEGREEVALUETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DegreesType"), DEGREESTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AbstractGriddedSurfaceType_row"),
            ABSTRACTGRIDDEDSURFACETYPE_ROW_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "DirectedEdgePropertyType"),
            DIRECTEDEDGEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "MultiCurveType"), MULTICURVETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeNodeType"), TIMENODETYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "stringOrNull"), STRINGORNULL_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "SolidType"), SOLIDTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TemporalCRSType"), TEMPORALCRSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "LabelStylePropertyType"),
            LABELSTYLEPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ObliqueCartesianCSType"),
            OBLIQUECARTESIANCSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "ProjectedCRSType"), PROJECTEDCRSTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TimeInstantPropertyType"),
            TIMEINSTANTPROPERTYTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GridDomainType"), GRIDDOMAINTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "PolygonType"), POLYGONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "AssociationType"), ASSOCIATIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "GeneralTransformationRefType"),
            GENERALTRANSFORMATIONREFTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "SurfaceInterpolationType"),
            SURFACEINTERPOLATIONTYPE_TYPE);
        put(new TypeName("http://www.opengis.net/gml", "TopoVolumeType"), TOPOVOLUMETYPE_TYPE);
    }
}
