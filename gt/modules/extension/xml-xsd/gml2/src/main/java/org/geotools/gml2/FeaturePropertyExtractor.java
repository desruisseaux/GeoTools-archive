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
package org.geotools.gml2;

import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDParticle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.xml.PropertyExtractor;
import org.geotools.xml.SchemaIndex;
import org.geotools.xml.Schemas;


/**
 * Special property extractor for extracting attributes from features.
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class FeaturePropertyExtractor implements PropertyExtractor {
    SchemaIndex schemaIndex;

    public FeaturePropertyExtractor(SchemaIndex schemaIndex) {
        this.schemaIndex = schemaIndex;
    }

    public boolean canHandle(Object object) {
        return object instanceof Feature && !(object instanceof FeatureCollection);
    }

    public List properties(Object object, XSDElementDeclaration element) {
        Feature feature = (Feature) object;
        FeatureType featureType = feature.getFeatureType();

        String namespace = featureType.getNamespace().toString();
        String typeName = featureType.getTypeName();

        //find the type in the schema
        element = schemaIndex.getElementDeclaration(new QName(namespace, typeName));

        List particles = Schemas.getChildElementParticles(element.getType(), true);
        List properties = new ArrayList();

        for (Iterator p = particles.iterator(); p.hasNext();) {
            XSDParticle particle = (XSDParticle) p.next();
            XSDElementDeclaration attribute = (XSDElementDeclaration) particle.getContent();

            if (attribute.isElementDeclarationReference()) {
                attribute = attribute.getResolvedElementDeclaration();
            }

            //ignore gml attributes
            if (GML.NAMESPACE.equals(attribute.getTargetNamespace())) {
                continue;
            }

            //make sure the feature type has an element
            if (featureType.getAttributeType(attribute.getName()) == null) {
                continue;
            }

            Object attributeValue = feature.getAttribute(attribute.getName());
            properties.add(new Object[] { particle, attributeValue });
        }

        return properties;
    }
}
