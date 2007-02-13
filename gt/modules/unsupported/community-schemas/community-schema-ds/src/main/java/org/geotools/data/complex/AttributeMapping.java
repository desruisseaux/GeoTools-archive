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
package org.geotools.data.complex;

import java.util.Collections;
import java.util.Map;

import org.geotools.resources.Utilities;
import org.opengis.feature.type.AttributeType;
import org.opengis.filter.expression.Expression;

/**
 * DOCUMENT ME!
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 */
public class AttributeMapping {

    /** Expression to set the Attribute's ID from, or {@linkplain Expression#NIL} */
    private Expression identifierExpression;

    /** DOCUMENT ME! */
    private Expression sourceExpression;

    /** DOCUMENT ME! */
    private String targetXPath;

    private boolean isMultiValued;

    /**
     * If present, represents our way to deal polymorphic attribute instances,
     * so this node should be of a subtype of the one referenced by
     * {@link  #targetXPath}
     */
    AttributeType targetNodeInstance;

    private Map /* <AttributeName,Expression> */clientProperties;

    /**
     * Creates a new AttributeMapping object.
     * 
     * @param sourceExpression
     *            DOCUMENT ME!
     * @param targetXPath
     *            DOCUMENT ME!
     */
    public AttributeMapping(Expression idExpression,
            Expression sourceExpression, String targetXPath) {
        this(idExpression, sourceExpression, targetXPath, null, false, null);
    }

    public AttributeMapping(Expression idExpression,
            Expression sourceExpression, String targetXPath,
            AttributeType targetNodeInstance, boolean isMultiValued,
            Map clientProperties) {

        this.identifierExpression = idExpression == null ? Expression.NIL
                : idExpression;
        this.sourceExpression = sourceExpression == null ? Expression.NIL
                : sourceExpression;
        this.isMultiValued = isMultiValued;
        if (this.sourceExpression == null) {
            this.sourceExpression = Expression.NIL;
        }
        this.targetXPath = targetXPath;
        this.targetNodeInstance = targetNodeInstance;
        this.clientProperties = clientProperties == null ? Collections.EMPTY_MAP
                : clientProperties;
    }

    public boolean isMultiValued() {
        return isMultiValued;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public Expression getSourceExpression() {
        return sourceExpression;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getTargetXPath() {
        return targetXPath;
    }

    public AttributeType getTargetNodeInstance() {
        return targetNodeInstance;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param o
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof AttributeMapping)) {
            return false;
        }

        AttributeMapping other = (AttributeMapping) o;

        return Utilities.equals(identifierExpression,
                other.identifierExpression)
                && Utilities.equals(sourceExpression, other.sourceExpression)
                && Utilities.equals(targetXPath, other.targetXPath)
                && Utilities.equals(targetNodeInstance,
                        other.targetNodeInstance);
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public int hashCode() {
        return (37 * identifierExpression.hashCode() + 37 * sourceExpression
                .hashCode())
                ^ targetXPath.hashCode();
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("AttributeMapping[");
        sb.append("sourceExpression='").append(sourceExpression).append(
                "', targetXPath='").append(targetXPath);
        if (targetNodeInstance != null) {
            sb.append(", target instance type=").append(targetNodeInstance);
        }
        sb.append("']");

        return sb.toString();
    }

    public Map getClientProperties() {
        return clientProperties == null ? Collections.EMPTY_MAP
                : clientProperties;
    }

    public Expression getIdentifierExpression() {
        return identifierExpression;
    }

    public void setIdentifierExpression(Expression identifierExpression) {
        this.identifierExpression = identifierExpression;
    }
}
