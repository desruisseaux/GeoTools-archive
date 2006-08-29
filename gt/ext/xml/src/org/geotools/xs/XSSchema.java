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
package org.geotools.xs;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.type.SchemaImpl;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import javax.xml.namespace.QName;


public class XSSchema extends SchemaImpl {
    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType name="allNNI"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation&gt;
     *     for maxOccurs&lt;/xs:documentation&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:union memberTypes="xs:nonNegativeInteger"&gt;
     *          &lt;xs:simpleType&gt;
     *              &lt;xs:restriction base="xs:NMTOKEN"&gt;
     *                  &lt;xs:enumeration value="unbounded"/&gt;
     *              &lt;/xs:restriction&gt;
     *          &lt;/xs:simpleType&gt;
     *      &lt;/xs:union&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType ALLNNI_TYPE = AttributeTypeFactory
        .newAttributeType("allNNI", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="byte" name="byte"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#byte"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:short"&gt;
     *          &lt;xs:minInclusive id="byte.minInclusive" value="-128"/&gt;
     *          &lt;xs:maxInclusive id="byte.maxInclusive" value="127"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated NOT
     */
    public static final AttributeType BYTE_TYPE = AttributeTypeFactory
        .newAttributeType("byte", Byte.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="short" name="short"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#short"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:int"&gt;
     *          &lt;xs:minInclusive id="short.minInclusive" value="-32768"/&gt;
     *          &lt;xs:maxInclusive id="short.maxInclusive" value="32767"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated NOT
     */
    public static final AttributeType SHORT_TYPE = AttributeTypeFactory
        .newAttributeType("short", Short.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="nonNegativeInteger" name="nonNegativeInteger"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#nonNegativeInteger"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:integer"&gt;
     *          &lt;xs:minInclusive id="nonNegativeInteger.minInclusive" value="0"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType NONNEGATIVEINTEGER_TYPE = AttributeTypeFactory
        .newAttributeType("nonNegativeInteger", BigInteger.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="anySimpleType" name="anySimpleType"&gt;
     *      &lt;xs:restriction base="xs:anyType"/&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType ANYSIMPLETYPE_TYPE = AttributeTypeFactory
        .newAttributeType("anySimpleType", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType name="derivationSet"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation&gt;
     *     A utility type, not for public use&lt;/xs:documentation&gt;
     *          &lt;xs:documentation&gt;
     *     #all or (possibly empty) subset of {extension, restriction}&lt;/xs:documentation&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:union&gt;
     *          &lt;xs:simpleType&gt;
     *              &lt;xs:restriction base="xs:token"&gt;
     *                  &lt;xs:enumeration value="#all"/&gt;
     *              &lt;/xs:restriction&gt;
     *          &lt;/xs:simpleType&gt;
     *          &lt;xs:simpleType&gt;
     *              &lt;xs:list itemType="xs:reducedDerivationControl"/&gt;
     *          &lt;/xs:simpleType&gt;
     *      &lt;/xs:union&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType DERIVATIONSET_TYPE = AttributeTypeFactory
        .newAttributeType("derivationSet", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="negativeInteger" name="negativeInteger"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#negativeInteger"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:nonPositiveInteger"&gt;
     *          &lt;xs:maxInclusive id="negativeInteger.maxInclusive" value="-1"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType NEGATIVEINTEGER_TYPE = AttributeTypeFactory
        .newAttributeType("negativeInteger", BigInteger.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType name="blockSet"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation&gt;
     *      A utility type, not for public use&lt;/xs:documentation&gt;
     *          &lt;xs:documentation&gt;
     *      #all or (possibly empty) subset of {substitution, extension,
     *      restriction}&lt;/xs:documentation&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:union&gt;
     *          &lt;xs:simpleType&gt;
     *              &lt;xs:restriction base="xs:token"&gt;
     *                  &lt;xs:enumeration value="#all"/&gt;
     *              &lt;/xs:restriction&gt;
     *          &lt;/xs:simpleType&gt;
     *          &lt;xs:simpleType&gt;
     *              &lt;xs:list&gt;
     *                  &lt;xs:simpleType&gt;
     *                      &lt;xs:restriction base="xs:derivationControl"&gt;
     *                          &lt;xs:enumeration value="extension"/&gt;
     *                          &lt;xs:enumeration value="restriction"/&gt;
     *                          &lt;xs:enumeration value="substitution"/&gt;
     *                      &lt;/xs:restriction&gt;
     *                  &lt;/xs:simpleType&gt;
     *              &lt;/xs:list&gt;
     *          &lt;/xs:simpleType&gt;
     *      &lt;/xs:union&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType BLOCKSET_TYPE = AttributeTypeFactory
        .newAttributeType("blockSet", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="unsignedByte" name="unsignedByte"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#unsignedByte"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:unsignedShort"&gt;
     *          &lt;xs:maxInclusive id="unsignedByte.maxInclusive" value="255"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType UNSIGNEDBYTE_TYPE = AttributeTypeFactory
        .newAttributeType("unsignedByte", Short.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="anyURI" name="anyURI"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasFacet name="length"/&gt;
     *              &lt;hfp:hasFacet name="minLength"/&gt;
     *              &lt;hfp:hasFacet name="maxLength"/&gt;
     *              &lt;hfp:hasFacet name="pattern"/&gt;
     *              &lt;hfp:hasFacet name="enumeration"/&gt;
     *              &lt;hfp:hasFacet name="whiteSpace"/&gt;
     *              &lt;hfp:hasProperty name="ordered" value="false"/&gt;
     *              &lt;hfp:hasProperty name="bounded" value="false"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="countably infinite"/&gt;
     *              &lt;hfp:hasProperty name="numeric" value="false"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#anyURI"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:anySimpleType"&gt;
     *          &lt;xs:whiteSpace fixed="true" id="anyURI.whiteSpace" value="collapse"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType ANYURI_TYPE = AttributeTypeFactory
        .newAttributeType("anyURI", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="gMonth" name="gMonth"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasFacet name="pattern"/&gt;
     *              &lt;hfp:hasFacet name="enumeration"/&gt;
     *              &lt;hfp:hasFacet name="whiteSpace"/&gt;
     *              &lt;hfp:hasFacet name="maxInclusive"/&gt;
     *              &lt;hfp:hasFacet name="maxExclusive"/&gt;
     *              &lt;hfp:hasFacet name="minInclusive"/&gt;
     *              &lt;hfp:hasFacet name="minExclusive"/&gt;
     *              &lt;hfp:hasProperty name="ordered" value="partial"/&gt;
     *              &lt;hfp:hasProperty name="bounded" value="false"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="countably infinite"/&gt;
     *              &lt;hfp:hasProperty name="numeric" value="false"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#gMonth"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:anySimpleType"&gt;
     *          &lt;xs:whiteSpace fixed="true" id="gMonth.whiteSpace" value="collapse"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType GMONTH_TYPE = AttributeTypeFactory
        .newAttributeType("gMonth", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="normalizedString" name="normalizedString"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#normalizedString"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:string"&gt;
     *          &lt;xs:whiteSpace id="normalizedString.whiteSpace" value="replace"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType NORMALIZEDSTRING_TYPE = AttributeTypeFactory
        .newAttributeType("normalizedString", String.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="NMTOKENS" name="NMTOKENS"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasFacet name="length"/&gt;
     *              &lt;hfp:hasFacet name="minLength"/&gt;
     *              &lt;hfp:hasFacet name="maxLength"/&gt;
     *              &lt;hfp:hasFacet name="enumeration"/&gt;
     *              &lt;hfp:hasFacet name="whiteSpace"/&gt;
     *              &lt;hfp:hasProperty name="ordered" value="false"/&gt;
     *              &lt;hfp:hasProperty name="bounded" value="false"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="countably infinite"/&gt;
     *              &lt;hfp:hasProperty name="numeric" value="false"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#NMTOKENS"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction&gt;
     *          &lt;xs:simpleType&gt;
     *              &lt;xs:list itemType="xs:NMTOKEN"/&gt;
     *          &lt;/xs:simpleType&gt;
     *          &lt;xs:minLength id="NMTOKENS.minLength" value="1"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType NMTOKENS_TYPE = AttributeTypeFactory
        .newAttributeType("NMTOKENS", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="dateTime" name="dateTime"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasFacet name="pattern"/&gt;
     *              &lt;hfp:hasFacet name="enumeration"/&gt;
     *              &lt;hfp:hasFacet name="whiteSpace"/&gt;
     *              &lt;hfp:hasFacet name="maxInclusive"/&gt;
     *              &lt;hfp:hasFacet name="maxExclusive"/&gt;
     *              &lt;hfp:hasFacet name="minInclusive"/&gt;
     *              &lt;hfp:hasFacet name="minExclusive"/&gt;
     *              &lt;hfp:hasProperty name="ordered" value="partial"/&gt;
     *              &lt;hfp:hasProperty name="bounded" value="false"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="countably infinite"/&gt;
     *              &lt;hfp:hasProperty name="numeric" value="false"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#dateTime"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:anySimpleType"&gt;
     *          &lt;xs:whiteSpace fixed="true" id="dateTime.whiteSpace" value="collapse"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType DATETIME_TYPE = AttributeTypeFactory
        .newAttributeType("dateTime", Calendar.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="gMonthDay" name="gMonthDay"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasFacet name="pattern"/&gt;
     *              &lt;hfp:hasFacet name="enumeration"/&gt;
     *              &lt;hfp:hasFacet name="whiteSpace"/&gt;
     *              &lt;hfp:hasFacet name="maxInclusive"/&gt;
     *              &lt;hfp:hasFacet name="maxExclusive"/&gt;
     *              &lt;hfp:hasFacet name="minInclusive"/&gt;
     *              &lt;hfp:hasFacet name="minExclusive"/&gt;
     *              &lt;hfp:hasProperty name="ordered" value="partial"/&gt;
     *              &lt;hfp:hasProperty name="bounded" value="false"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="countably infinite"/&gt;
     *              &lt;hfp:hasProperty name="numeric" value="false"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#gMonthDay"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:anySimpleType"&gt;
     *          &lt;xs:whiteSpace fixed="true" id="gMonthDay.whiteSpace" value="collapse"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType GMONTHDAY_TYPE = AttributeTypeFactory
        .newAttributeType("gMonthDay", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="Name" name="Name"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#Name"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:token"&gt;
     *          &lt;xs:pattern id="Name.pattern" value="\i\c*"&gt;
     *              &lt;xs:annotation&gt;
     *                  &lt;xs:documentation source="http://www.w3.org/TR/REC-xml#NT-Name"&gt;
     *              pattern matches production 5 from the XML spec
     *            &lt;/xs:documentation&gt;
     *              &lt;/xs:annotation&gt;
     *          &lt;/xs:pattern&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType NAME_TYPE = AttributeTypeFactory
        .newAttributeType("Name", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="hexBinary" name="hexBinary"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasFacet name="length"/&gt;
     *              &lt;hfp:hasFacet name="minLength"/&gt;
     *              &lt;hfp:hasFacet name="maxLength"/&gt;
     *              &lt;hfp:hasFacet name="pattern"/&gt;
     *              &lt;hfp:hasFacet name="enumeration"/&gt;
     *              &lt;hfp:hasFacet name="whiteSpace"/&gt;
     *              &lt;hfp:hasProperty name="ordered" value="false"/&gt;
     *              &lt;hfp:hasProperty name="bounded" value="false"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="countably infinite"/&gt;
     *              &lt;hfp:hasProperty name="numeric" value="false"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#binary"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:anySimpleType"&gt;
     *          &lt;xs:whiteSpace fixed="true" id="hexBinary.whiteSpace" value="collapse"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType HEXBINARY_TYPE = AttributeTypeFactory
        .newAttributeType("hexBinary", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="positiveInteger" name="positiveInteger"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#positiveInteger"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:nonNegativeInteger"&gt;
     *          &lt;xs:minInclusive id="positiveInteger.minInclusive" value="1"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType POSITIVEINTEGER_TYPE = AttributeTypeFactory
        .newAttributeType("positiveInteger", BigInteger.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="integer" name="integer"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#integer"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:decimal"&gt;
     *          &lt;xs:fractionDigits fixed="true" id="integer.fractionDigits" value="0"/&gt;
     *          &lt;xs:pattern value="[\-+]?[0-9]+"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType INTEGER_TYPE = AttributeTypeFactory
        .newAttributeType("integer", BigInteger.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="ENTITIES" name="ENTITIES"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasFacet name="length"/&gt;
     *              &lt;hfp:hasFacet name="minLength"/&gt;
     *              &lt;hfp:hasFacet name="maxLength"/&gt;
     *              &lt;hfp:hasFacet name="enumeration"/&gt;
     *              &lt;hfp:hasFacet name="whiteSpace"/&gt;
     *              &lt;hfp:hasProperty name="ordered" value="false"/&gt;
     *              &lt;hfp:hasProperty name="bounded" value="false"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="countably infinite"/&gt;
     *              &lt;hfp:hasProperty name="numeric" value="false"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#ENTITIES"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction&gt;
     *          &lt;xs:simpleType&gt;
     *              &lt;xs:list itemType="xs:ENTITY"/&gt;
     *          &lt;/xs:simpleType&gt;
     *          &lt;xs:minLength id="ENTITIES.minLength" value="1"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType ENTITIES_TYPE = AttributeTypeFactory
        .newAttributeType("ENTITIES", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="token" name="token"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#token"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:normalizedString"&gt;
     *          &lt;xs:whiteSpace id="token.whiteSpace" value="collapse"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType TOKEN_TYPE = AttributeTypeFactory
        .newAttributeType("token", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="NMTOKEN" name="NMTOKEN"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#NMTOKEN"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:token"&gt;
     *          &lt;xs:pattern id="NMTOKEN.pattern" value="\c+"&gt;
     *              &lt;xs:annotation&gt;
     *                  &lt;xs:documentation source="http://www.w3.org/TR/REC-xml#NT-Nmtoken"&gt;
     *              pattern matches production 7 from the XML spec
     *            &lt;/xs:documentation&gt;
     *              &lt;/xs:annotation&gt;
     *          &lt;/xs:pattern&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType NMTOKEN_TYPE = AttributeTypeFactory
        .newAttributeType("NMTOKEN", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="int" name="int"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#int"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:long"&gt;
     *          &lt;xs:minInclusive id="int.minInclusive" value="-2147483648"/&gt;
     *          &lt;xs:maxInclusive id="int.maxInclusive" value="2147483647"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType INT_TYPE = AttributeTypeFactory
        .newAttributeType("int", Integer.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="base64Binary" name="base64Binary"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasFacet name="length"/&gt;
     *              &lt;hfp:hasFacet name="minLength"/&gt;
     *              &lt;hfp:hasFacet name="maxLength"/&gt;
     *              &lt;hfp:hasFacet name="pattern"/&gt;
     *              &lt;hfp:hasFacet name="enumeration"/&gt;
     *              &lt;hfp:hasFacet name="whiteSpace"/&gt;
     *              &lt;hfp:hasProperty name="ordered" value="false"/&gt;
     *              &lt;hfp:hasProperty name="bounded" value="false"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="countably infinite"/&gt;
     *              &lt;hfp:hasProperty name="numeric" value="false"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#base64Binary"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:anySimpleType"&gt;
     *          &lt;xs:whiteSpace fixed="true" id="base64Binary.whiteSpace" value="collapse"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType BASE64BINARY_TYPE = AttributeTypeFactory
        .newAttributeType("base64Binary", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="float" name="float"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasFacet name="pattern"/&gt;
     *              &lt;hfp:hasFacet name="enumeration"/&gt;
     *              &lt;hfp:hasFacet name="whiteSpace"/&gt;
     *              &lt;hfp:hasFacet name="maxInclusive"/&gt;
     *              &lt;hfp:hasFacet name="maxExclusive"/&gt;
     *              &lt;hfp:hasFacet name="minInclusive"/&gt;
     *              &lt;hfp:hasFacet name="minExclusive"/&gt;
     *              &lt;hfp:hasProperty name="ordered" value="total"/&gt;
     *              &lt;hfp:hasProperty name="bounded" value="true"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="finite"/&gt;
     *              &lt;hfp:hasProperty name="numeric" value="true"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#float"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:anySimpleType"&gt;
     *          &lt;xs:whiteSpace fixed="true" id="float.whiteSpace" value="collapse"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType FLOAT_TYPE = AttributeTypeFactory
        .newAttributeType("float", Float.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="anyType" name="anyType"&gt;
     *      &lt;xs:restriction base="xs:anyType"/&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType ANYTYPE_TYPE = AttributeTypeFactory
        .newAttributeType("anyType", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="NCName" name="NCName"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#NCName"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:Name"&gt;
     *          &lt;xs:pattern id="NCName.pattern" value="[\i-[:]][\c-[:]]*"&gt;
     *              &lt;xs:annotation&gt;
     *                  &lt;xs:documentation source="http://www.w3.org/TR/REC-xml-names/#NT-NCName"&gt;
     *              pattern matches production 4 from the Namespaces in XML spec
     *            &lt;/xs:documentation&gt;
     *              &lt;/xs:annotation&gt;
     *          &lt;/xs:pattern&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType NCNAME_TYPE = AttributeTypeFactory
        .newAttributeType("NCName", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="IDREFS" name="IDREFS"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasFacet name="length"/&gt;
     *              &lt;hfp:hasFacet name="minLength"/&gt;
     *              &lt;hfp:hasFacet name="maxLength"/&gt;
     *              &lt;hfp:hasFacet name="enumeration"/&gt;
     *              &lt;hfp:hasFacet name="whiteSpace"/&gt;
     *              &lt;hfp:hasProperty name="ordered" value="false"/&gt;
     *              &lt;hfp:hasProperty name="bounded" value="false"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="countably infinite"/&gt;
     *              &lt;hfp:hasProperty name="numeric" value="false"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#IDREFS"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction&gt;
     *          &lt;xs:simpleType&gt;
     *              &lt;xs:list itemType="xs:IDREF"/&gt;
     *          &lt;/xs:simpleType&gt;
     *          &lt;xs:minLength id="IDREFS.minLength" value="1"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType IDREFS_TYPE = AttributeTypeFactory
        .newAttributeType("IDREFS", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType name="formChoice"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation&gt;
     *     A utility type, not for public use&lt;/xs:documentation&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:NMTOKEN"&gt;
     *          &lt;xs:enumeration value="qualified"/&gt;
     *          &lt;xs:enumeration value="unqualified"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType FORMCHOICE_TYPE = AttributeTypeFactory
        .newAttributeType("formChoice", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="long" name="long"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasProperty name="bounded" value="true"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="finite"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#long"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:integer"&gt;
     *          &lt;xs:minInclusive id="long.minInclusive" value="-9223372036854775808"/&gt;
     *          &lt;xs:maxInclusive id="long.maxInclusive" value="9223372036854775807"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType LONG_TYPE = AttributeTypeFactory
        .newAttributeType("long", Long.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="gYearMonth" name="gYearMonth"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasFacet name="pattern"/&gt;
     *              &lt;hfp:hasFacet name="enumeration"/&gt;
     *              &lt;hfp:hasFacet name="whiteSpace"/&gt;
     *              &lt;hfp:hasFacet name="maxInclusive"/&gt;
     *              &lt;hfp:hasFacet name="maxExclusive"/&gt;
     *              &lt;hfp:hasFacet name="minInclusive"/&gt;
     *              &lt;hfp:hasFacet name="minExclusive"/&gt;
     *              &lt;hfp:hasProperty name="ordered" value="partial"/&gt;
     *              &lt;hfp:hasProperty name="bounded" value="false"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="countably infinite"/&gt;
     *              &lt;hfp:hasProperty name="numeric" value="false"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#gYearMonth"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:anySimpleType"&gt;
     *          &lt;xs:whiteSpace fixed="true" id="gYearMonth.whiteSpace" value="collapse"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType GYEARMONTH_TYPE = AttributeTypeFactory
        .newAttributeType("gYearMonth", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="nonPositiveInteger" name="nonPositiveInteger"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#nonPositiveInteger"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:integer"&gt;
     *          &lt;xs:maxInclusive id="nonPositiveInteger.maxInclusive" value="0"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType NONPOSITIVEINTEGER_TYPE = AttributeTypeFactory
        .newAttributeType("nonPositiveInteger", BigInteger.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="NOTATION" name="NOTATION"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasFacet name="length"/&gt;
     *              &lt;hfp:hasFacet name="minLength"/&gt;
     *              &lt;hfp:hasFacet name="maxLength"/&gt;
     *              &lt;hfp:hasFacet name="pattern"/&gt;
     *              &lt;hfp:hasFacet name="enumeration"/&gt;
     *              &lt;hfp:hasFacet name="whiteSpace"/&gt;
     *              &lt;hfp:hasProperty name="ordered" value="false"/&gt;
     *              &lt;hfp:hasProperty name="bounded" value="false"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="countably infinite"/&gt;
     *              &lt;hfp:hasProperty name="numeric" value="false"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#NOTATION"/&gt;
     *          &lt;xs:documentation&gt;
     *          NOTATION cannot be used directly in a schema; rather a type
     *          must be derived from it by specifying at least one enumeration
     *          facet whose value is the name of a NOTATION declared in the
     *          schema.
     *        &lt;/xs:documentation&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:anySimpleType"&gt;
     *          &lt;xs:whiteSpace fixed="true" id="NOTATION.whiteSpace" value="collapse"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType NOTATION_TYPE = AttributeTypeFactory
        .newAttributeType("NOTATION", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="time" name="time"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasFacet name="pattern"/&gt;
     *              &lt;hfp:hasFacet name="enumeration"/&gt;
     *              &lt;hfp:hasFacet name="whiteSpace"/&gt;
     *              &lt;hfp:hasFacet name="maxInclusive"/&gt;
     *              &lt;hfp:hasFacet name="maxExclusive"/&gt;
     *              &lt;hfp:hasFacet name="minInclusive"/&gt;
     *              &lt;hfp:hasFacet name="minExclusive"/&gt;
     *              &lt;hfp:hasProperty name="ordered" value="partial"/&gt;
     *              &lt;hfp:hasProperty name="bounded" value="false"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="countably infinite"/&gt;
     *              &lt;hfp:hasProperty name="numeric" value="false"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#time"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:anySimpleType"&gt;
     *          &lt;xs:whiteSpace fixed="true" id="time.whiteSpace" value="collapse"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType TIME_TYPE = AttributeTypeFactory
        .newAttributeType("time", Calendar.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType name="derivationControl"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation&gt;
     *     A utility type, not for public use&lt;/xs:documentation&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:NMTOKEN"&gt;
     *          &lt;xs:enumeration value="substitution"/&gt;
     *          &lt;xs:enumeration value="extension"/&gt;
     *          &lt;xs:enumeration value="restriction"/&gt;
     *          &lt;xs:enumeration value="list"/&gt;
     *          &lt;xs:enumeration value="union"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType DERIVATIONCONTROL_TYPE = AttributeTypeFactory
        .newAttributeType("derivationControl", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="QName" name="QName"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasFacet name="length"/&gt;
     *              &lt;hfp:hasFacet name="minLength"/&gt;
     *              &lt;hfp:hasFacet name="maxLength"/&gt;
     *              &lt;hfp:hasFacet name="pattern"/&gt;
     *              &lt;hfp:hasFacet name="enumeration"/&gt;
     *              &lt;hfp:hasFacet name="whiteSpace"/&gt;
     *              &lt;hfp:hasProperty name="ordered" value="false"/&gt;
     *              &lt;hfp:hasProperty name="bounded" value="false"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="countably infinite"/&gt;
     *              &lt;hfp:hasProperty name="numeric" value="false"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#QName"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:anySimpleType"&gt;
     *          &lt;xs:whiteSpace fixed="true" id="QName.whiteSpace" value="collapse"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType QNAME_TYPE = AttributeTypeFactory
        .newAttributeType("QName", QName.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="unsignedInt" name="unsignedInt"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#unsignedInt"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:unsignedLong"&gt;
     *          &lt;xs:maxInclusive id="unsignedInt.maxInclusive" value="4294967295"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType UNSIGNEDINT_TYPE = AttributeTypeFactory
        .newAttributeType("unsignedInt", Long.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType name="namespaceList"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation&gt;
     *     A utility type, not for public use&lt;/xs:documentation&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:union&gt;
     *          &lt;xs:simpleType&gt;
     *              &lt;xs:restriction base="xs:token"&gt;
     *                  &lt;xs:enumeration value="##any"/&gt;
     *                  &lt;xs:enumeration value="##other"/&gt;
     *              &lt;/xs:restriction&gt;
     *          &lt;/xs:simpleType&gt;
     *          &lt;xs:simpleType&gt;
     *              &lt;xs:list&gt;
     *                  &lt;xs:simpleType&gt;
     *                      &lt;xs:union memberTypes="xs:anyURI"&gt;
     *                          &lt;xs:simpleType&gt;
     *                              &lt;xs:restriction base="xs:token"&gt;
     *                                  &lt;xs:enumeration value="##targetNamespace"/&gt;
     *                                  &lt;xs:enumeration value="##local"/&gt;
     *                              &lt;/xs:restriction&gt;
     *                          &lt;/xs:simpleType&gt;
     *                      &lt;/xs:union&gt;
     *                  &lt;/xs:simpleType&gt;
     *              &lt;/xs:list&gt;
     *          &lt;/xs:simpleType&gt;
     *      &lt;/xs:union&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType NAMESPACELIST_TYPE = AttributeTypeFactory
        .newAttributeType("namespaceList", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="date" name="date"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasFacet name="pattern"/&gt;
     *              &lt;hfp:hasFacet name="enumeration"/&gt;
     *              &lt;hfp:hasFacet name="whiteSpace"/&gt;
     *              &lt;hfp:hasFacet name="maxInclusive"/&gt;
     *              &lt;hfp:hasFacet name="maxExclusive"/&gt;
     *              &lt;hfp:hasFacet name="minInclusive"/&gt;
     *              &lt;hfp:hasFacet name="minExclusive"/&gt;
     *              &lt;hfp:hasProperty name="ordered" value="partial"/&gt;
     *              &lt;hfp:hasProperty name="bounded" value="false"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="countably infinite"/&gt;
     *              &lt;hfp:hasProperty name="numeric" value="false"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#date"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:anySimpleType"&gt;
     *          &lt;xs:whiteSpace fixed="true" id="date.whiteSpace" value="collapse"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType DATE_TYPE = AttributeTypeFactory
        .newAttributeType("date", Calendar.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="gDay" name="gDay"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasFacet name="pattern"/&gt;
     *              &lt;hfp:hasFacet name="enumeration"/&gt;
     *              &lt;hfp:hasFacet name="whiteSpace"/&gt;
     *              &lt;hfp:hasFacet name="maxInclusive"/&gt;
     *              &lt;hfp:hasFacet name="maxExclusive"/&gt;
     *              &lt;hfp:hasFacet name="minInclusive"/&gt;
     *              &lt;hfp:hasFacet name="minExclusive"/&gt;
     *              &lt;hfp:hasProperty name="ordered" value="partial"/&gt;
     *              &lt;hfp:hasProperty name="bounded" value="false"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="countably infinite"/&gt;
     *              &lt;hfp:hasProperty name="numeric" value="false"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#gDay"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:anySimpleType"&gt;
     *          &lt;xs:whiteSpace fixed="true" id="gDay.whiteSpace" value="collapse"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType GDAY_TYPE = AttributeTypeFactory
        .newAttributeType("gDay", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="string" name="string"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasFacet name="length"/&gt;
     *              &lt;hfp:hasFacet name="minLength"/&gt;
     *              &lt;hfp:hasFacet name="maxLength"/&gt;
     *              &lt;hfp:hasFacet name="pattern"/&gt;
     *              &lt;hfp:hasFacet name="enumeration"/&gt;
     *              &lt;hfp:hasFacet name="whiteSpace"/&gt;
     *              &lt;hfp:hasProperty name="ordered" value="false"/&gt;
     *              &lt;hfp:hasProperty name="bounded" value="false"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="countably infinite"/&gt;
     *              &lt;hfp:hasProperty name="numeric" value="false"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#string"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:anySimpleType"&gt;
     *          &lt;xs:whiteSpace id="string.preserve" value="preserve"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType STRING_TYPE = AttributeTypeFactory
        .newAttributeType("string", String.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="language" name="language"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#language"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:token"&gt;
     *          &lt;xs:pattern id="language.pattern" value="[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*"&gt;
     *              &lt;xs:annotation&gt;
     *                  &lt;xs:documentation source="http://www.w3.org/TR/REC-xml#NT-LanguageID"&gt;
     *              pattern specifies the content of section 2.12 of XML 1.0e2
     *              and RFC 3066 (Revised version of RFC 1766).
     *            &lt;/xs:documentation&gt;
     *              &lt;/xs:annotation&gt;
     *          &lt;/xs:pattern&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType LANGUAGE_TYPE = AttributeTypeFactory
        .newAttributeType("language", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType name="public"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation&gt;
     *     A utility type, not for public use&lt;/xs:documentation&gt;
     *          &lt;xs:documentation&gt;
     *     A public identifier, per ISO 8879&lt;/xs:documentation&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:token"/&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType PUBLIC_TYPE = AttributeTypeFactory
        .newAttributeType("public", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="ID" name="ID"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#ID"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:NCName"/&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType ID_TYPE = AttributeTypeFactory
        .newAttributeType("ID", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="ENTITY" name="ENTITY"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#ENTITY"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:NCName"/&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType ENTITY_TYPE = AttributeTypeFactory
        .newAttributeType("ENTITY", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="IDREF" name="IDREF"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#IDREF"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:NCName"/&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType IDREF_TYPE = AttributeTypeFactory
        .newAttributeType("IDREF", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="unsignedLong" name="unsignedLong"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasProperty name="bounded" value="true"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="finite"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#unsignedLong"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:nonNegativeInteger"&gt;
     *          &lt;xs:maxInclusive id="unsignedLong.maxInclusive" value="18446744073709551615"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType UNSIGNEDLONG_TYPE = AttributeTypeFactory
        .newAttributeType("unsignedLong", BigInteger.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="decimal" name="decimal"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasFacet name="totalDigits"/&gt;
     *              &lt;hfp:hasFacet name="fractionDigits"/&gt;
     *              &lt;hfp:hasFacet name="pattern"/&gt;
     *              &lt;hfp:hasFacet name="whiteSpace"/&gt;
     *              &lt;hfp:hasFacet name="enumeration"/&gt;
     *              &lt;hfp:hasFacet name="maxInclusive"/&gt;
     *              &lt;hfp:hasFacet name="maxExclusive"/&gt;
     *              &lt;hfp:hasFacet name="minInclusive"/&gt;
     *              &lt;hfp:hasFacet name="minExclusive"/&gt;
     *              &lt;hfp:hasProperty name="ordered" value="total"/&gt;
     *              &lt;hfp:hasProperty name="bounded" value="false"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="countably infinite"/&gt;
     *              &lt;hfp:hasProperty name="numeric" value="true"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#decimal"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:anySimpleType"&gt;
     *          &lt;xs:whiteSpace fixed="true" id="decimal.whiteSpace" value="collapse"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType DECIMAL_TYPE = AttributeTypeFactory
        .newAttributeType("decimal", BigDecimal.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="unsignedShort" name="unsignedShort"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#unsignedShort"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:unsignedInt"&gt;
     *          &lt;xs:maxInclusive id="unsignedShort.maxInclusive" value="65535"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType UNSIGNEDSHORT_TYPE = AttributeTypeFactory
        .newAttributeType("unsignedShort", Integer.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="gYear" name="gYear"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasFacet name="pattern"/&gt;
     *              &lt;hfp:hasFacet name="enumeration"/&gt;
     *              &lt;hfp:hasFacet name="whiteSpace"/&gt;
     *              &lt;hfp:hasFacet name="maxInclusive"/&gt;
     *              &lt;hfp:hasFacet name="maxExclusive"/&gt;
     *              &lt;hfp:hasFacet name="minInclusive"/&gt;
     *              &lt;hfp:hasFacet name="minExclusive"/&gt;
     *              &lt;hfp:hasProperty name="ordered" value="partial"/&gt;
     *              &lt;hfp:hasProperty name="bounded" value="false"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="countably infinite"/&gt;
     *              &lt;hfp:hasProperty name="numeric" value="false"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#gYear"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:anySimpleType"&gt;
     *          &lt;xs:whiteSpace fixed="true" id="gYear.whiteSpace" value="collapse"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType GYEAR_TYPE = AttributeTypeFactory
        .newAttributeType("gYear", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="duration" name="duration"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasFacet name="pattern"/&gt;
     *              &lt;hfp:hasFacet name="enumeration"/&gt;
     *              &lt;hfp:hasFacet name="whiteSpace"/&gt;
     *              &lt;hfp:hasFacet name="maxInclusive"/&gt;
     *              &lt;hfp:hasFacet name="maxExclusive"/&gt;
     *              &lt;hfp:hasFacet name="minInclusive"/&gt;
     *              &lt;hfp:hasFacet name="minExclusive"/&gt;
     *              &lt;hfp:hasProperty name="ordered" value="partial"/&gt;
     *              &lt;hfp:hasProperty name="bounded" value="false"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="countably infinite"/&gt;
     *              &lt;hfp:hasProperty name="numeric" value="false"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#duration"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:anySimpleType"&gt;
     *          &lt;xs:whiteSpace fixed="true" id="duration.whiteSpace" value="collapse"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType DURATION_TYPE = AttributeTypeFactory
        .newAttributeType("duration", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType name="simpleDerivationSet"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation&gt;
     *     #all or (possibly empty) subset of {restriction, union, list}
     *     &lt;/xs:documentation&gt;
     *          &lt;xs:documentation&gt;
     *     A utility type, not for public use&lt;/xs:documentation&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:union&gt;
     *          &lt;xs:simpleType&gt;
     *              &lt;xs:restriction base="xs:token"&gt;
     *                  &lt;xs:enumeration value="#all"/&gt;
     *              &lt;/xs:restriction&gt;
     *          &lt;/xs:simpleType&gt;
     *          &lt;xs:simpleType&gt;
     *              &lt;xs:restriction base="xs:derivationControl"&gt;
     *                  &lt;xs:enumeration value="list"/&gt;
     *                  &lt;xs:enumeration value="union"/&gt;
     *                  &lt;xs:enumeration value="restriction"/&gt;
     *              &lt;/xs:restriction&gt;
     *          &lt;/xs:simpleType&gt;
     *      &lt;/xs:union&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType SIMPLEDERIVATIONSET_TYPE = AttributeTypeFactory
        .newAttributeType("simpleDerivationSet", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType name="reducedDerivationControl"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:documentation&gt;
     *     A utility type, not for public use&lt;/xs:documentation&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:derivationControl"&gt;
     *          &lt;xs:enumeration value="extension"/&gt;
     *          &lt;xs:enumeration value="restriction"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType REDUCEDDERIVATIONCONTROL_TYPE = AttributeTypeFactory
        .newAttributeType("reducedDerivationControl", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="boolean" name="boolean"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasFacet name="pattern"/&gt;
     *              &lt;hfp:hasFacet name="whiteSpace"/&gt;
     *              &lt;hfp:hasProperty name="ordered" value="false"/&gt;
     *              &lt;hfp:hasProperty name="bounded" value="false"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="finite"/&gt;
     *              &lt;hfp:hasProperty name="numeric" value="false"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#boolean"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:anySimpleType"&gt;
     *          &lt;xs:whiteSpace fixed="true" id="boolean.whiteSpace" value="collapse"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType BOOLEAN_TYPE = AttributeTypeFactory
        .newAttributeType("boolean", Boolean.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;xs:simpleType id="double" name="double"&gt;
     *      &lt;xs:annotation&gt;
     *          &lt;xs:appinfo&gt;
     *              &lt;hfp:hasFacet name="pattern"/&gt;
     *              &lt;hfp:hasFacet name="enumeration"/&gt;
     *              &lt;hfp:hasFacet name="whiteSpace"/&gt;
     *              &lt;hfp:hasFacet name="maxInclusive"/&gt;
     *              &lt;hfp:hasFacet name="maxExclusive"/&gt;
     *              &lt;hfp:hasFacet name="minInclusive"/&gt;
     *              &lt;hfp:hasFacet name="minExclusive"/&gt;
     *              &lt;hfp:hasProperty name="ordered" value="total"/&gt;
     *              &lt;hfp:hasProperty name="bounded" value="true"/&gt;
     *              &lt;hfp:hasProperty name="cardinality" value="finite"/&gt;
     *              &lt;hfp:hasProperty name="numeric" value="true"/&gt;
     *          &lt;/xs:appinfo&gt;
     *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#double"/&gt;
     *      &lt;/xs:annotation&gt;
     *      &lt;xs:restriction base="xs:anySimpleType"&gt;
     *          &lt;xs:whiteSpace fixed="true" id="double.whiteSpace" value="collapse"/&gt;
     *      &lt;/xs:restriction&gt;
     *  &lt;/xs:simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType DOUBLE_TYPE = AttributeTypeFactory
        .newAttributeType("double", Double.class);

    public XSSchema() {
        super("http://www.w3.org/2001/XMLSchema");

        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "allNNI"), ALLNNI_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "byte"), BYTE_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "short"), SHORT_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "nonNegativeInteger"), NONNEGATIVEINTEGER_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "anySimpleType"), ANYSIMPLETYPE_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "derivationSet"), DERIVATIONSET_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "negativeInteger"), NEGATIVEINTEGER_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "blockSet"), BLOCKSET_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "unsignedByte"), UNSIGNEDBYTE_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "anyURI"), ANYURI_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "gMonth"), GMONTH_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "normalizedString"), NORMALIZEDSTRING_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "NMTOKENS"), NMTOKENS_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "dateTime"), DATETIME_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "gMonthDay"), GMONTHDAY_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "Name"), NAME_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "hexBinary"), HEXBINARY_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "positiveInteger"), POSITIVEINTEGER_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "integer"), INTEGER_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "ENTITIES"), ENTITIES_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "token"), TOKEN_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "NMTOKEN"), NMTOKEN_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema", "int"),
            INT_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "base64Binary"), BASE64BINARY_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "float"), FLOAT_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "anyType"), ANYTYPE_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "NCName"), NCNAME_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "IDREFS"), IDREFS_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "formChoice"), FORMCHOICE_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "long"), LONG_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "gYearMonth"), GYEARMONTH_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "nonPositiveInteger"), NONPOSITIVEINTEGER_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "NOTATION"), NOTATION_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "time"), TIME_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "derivationControl"), DERIVATIONCONTROL_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "QName"), QNAME_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "unsignedInt"), UNSIGNEDINT_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "namespaceList"), NAMESPACELIST_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "date"), DATE_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "gDay"), GDAY_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "string"), STRING_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "language"), LANGUAGE_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "public"), PUBLIC_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema", "ID"),
            ID_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "ENTITY"), ENTITY_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "IDREF"), IDREF_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "unsignedLong"), UNSIGNEDLONG_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "decimal"), DECIMAL_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "unsignedShort"), UNSIGNEDSHORT_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "gYear"), GYEAR_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "duration"), DURATION_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "simpleDerivationSet"), SIMPLEDERIVATIONSET_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "reducedDerivationControl"), REDUCEDDERIVATIONCONTROL_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "boolean"), BOOLEAN_TYPE);
        put(new org.geotools.feature.Name("http://www.w3.org/2001/XMLSchema",
                "double"), DOUBLE_TYPE);
    }
}
