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
package org.geotools.gml3.smil;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.type.SchemaImpl;


public class SMIL20LANGSchema extends SchemaImpl {
    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="setType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="smil20:setPrototype"&gt;
     *              &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
     *                  &lt;any namespace="##other" processContents="lax"/&gt;
     *              &lt;/choice&gt;
     *              &lt;attributeGroup ref="smil20lang:CoreAttrs"/&gt;
     *              &lt;attributeGroup ref="smil20lang:TimingAttrs"/&gt;
     *              &lt;attributeGroup ref="smil20:animTargetAttrs"/&gt;
     *              &lt;attributeGroup ref="smil20:skipContentAttrs"/&gt;
     *              &lt;anyAttribute namespace="##any" processContents="strict"/&gt;
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
    public static final AttributeType SETTYPE_TYPE = AttributeTypeFactory
        .newAttributeType("setType", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="animateMotionType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="smil20:animateMotionPrototype"&gt;
     *              &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
     *                  &lt;any namespace="##other" processContents="lax"/&gt;
     *              &lt;/choice&gt;
     *              &lt;attributeGroup ref="smil20lang:CoreAttrs"/&gt;
     *              &lt;attributeGroup ref="smil20lang:TimingAttrs"/&gt;
     *              &lt;attributeGroup ref="smil20:animTargetAttrs"/&gt;
     *              &lt;attributeGroup ref="smil20:animModeAttrs"/&gt;
     *              &lt;attributeGroup ref="smil20:skipContentAttrs"/&gt;
     *              &lt;anyAttribute namespace="##any" processContents="strict"/&gt;
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
    public static final AttributeType ANIMATEMOTIONTYPE_TYPE = AttributeTypeFactory
        .newAttributeType("animateMotionType", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="animateType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="smil20:animatePrototype"&gt;
     *              &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
     *                  &lt;any namespace="##other" processContents="lax"/&gt;
     *              &lt;/choice&gt;
     *              &lt;attributeGroup ref="smil20lang:CoreAttrs"/&gt;
     *              &lt;attributeGroup ref="smil20lang:TimingAttrs"/&gt;
     *              &lt;attributeGroup ref="smil20:animTargetAttrs"/&gt;
     *              &lt;attributeGroup ref="smil20:animModeAttrs"/&gt;
     *              &lt;attributeGroup ref="smil20:skipContentAttrs"/&gt;
     *              &lt;anyAttribute namespace="##any" processContents="strict"/&gt;
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
    public static final AttributeType ANIMATETYPE_TYPE = AttributeTypeFactory
        .newAttributeType("animateType", java.lang.Object.class);

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="animateColorType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="smil20:animateColorPrototype"&gt;
     *              &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
     *                  &lt;any namespace="##other" processContents="lax"/&gt;
     *              &lt;/choice&gt;
     *              &lt;attributeGroup ref="smil20lang:CoreAttrs"/&gt;
     *              &lt;attributeGroup ref="smil20lang:TimingAttrs"/&gt;
     *              &lt;attributeGroup ref="smil20:animTargetAttrs"/&gt;
     *              &lt;attributeGroup ref="smil20:animModeAttrs"/&gt;
     *              &lt;attributeGroup ref="smil20:skipContentAttrs"/&gt;
     *              &lt;anyAttribute namespace="##any" processContents="strict"/&gt;
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
    public static final AttributeType ANIMATECOLORTYPE_TYPE = AttributeTypeFactory
        .newAttributeType("animateColorType", java.lang.Object.class);

    public SMIL20LANGSchema() {
        super("http://www.w3.org/2001/SMIL20/Language");

        put(new org.geotools.feature.Name(
                "http://www.w3.org/2001/SMIL20/Language", "setType"),
            SETTYPE_TYPE);
        put(new org.geotools.feature.Name(
                "http://www.w3.org/2001/SMIL20/Language", "animateMotionType"),
            ANIMATEMOTIONTYPE_TYPE);
        put(new org.geotools.feature.Name(
                "http://www.w3.org/2001/SMIL20/Language", "animateType"),
            ANIMATETYPE_TYPE);
        put(new org.geotools.feature.Name(
                "http://www.w3.org/2001/SMIL20/Language", "animateColorType"),
            ANIMATECOLORTYPE_TYPE);
    }
}
