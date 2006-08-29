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

import org.geotools.xml.BindingConfiguration;
import org.picocontainer.MutablePicoContainer;


/**
 * Binding configuration for the http://www.opengis.net/gml schema.
 *
 * @generated
 */
public final class GMLBindingConfiguration implements BindingConfiguration {
    /**
     * @generated modifiable
     */
    public void configure(MutablePicoContainer container) {
        //geometry 
        container.registerComponentImplementation(GML.ABSTRACTGEOMETRYCOLLECTIONBASETYPE,
            GMLAbstractGeometryCollectionBaseTypeBinding.class);
        container.registerComponentImplementation(GML.ABSTRACTGEOMETRYTYPE,
            GMLAbstractGeometryTypeBinding.class);
        container.registerComponentImplementation(GML.BOXTYPE,
            GMLBoxTypeBinding.class);
        container.registerComponentImplementation(GML.COORDINATESTYPE,
            GMLCoordinatesTypeBinding.class);
        container.registerComponentImplementation(GML.COORDTYPE,
            GMLCoordTypeBinding.class);
        container.registerComponentImplementation(GML.GEOMETRYASSOCIATIONTYPE,
            GMLGeometryAssociationTypeBinding.class);
        container.registerComponentImplementation(GML.GEOMETRYCOLLECTIONTYPE,
            GMLGeometryCollectionTypeBinding.class);
        container.registerComponentImplementation(GML.LINEARRINGMEMBERTYPE,
            GMLLinearRingMemberTypeBinding.class);
        container.registerComponentImplementation(GML.LINEARRINGTYPE,
            GMLLinearRingTypeBinding.class);
        container.registerComponentImplementation(GML.LINESTRINGMEMBERTYPE,
            GMLLineStringMemberTypeBinding.class);
        container.registerComponentImplementation(GML.LINESTRINGTYPE,
            GMLLineStringTypeBinding.class);
        container.registerComponentImplementation(GML.MULTILINESTRINGTYPE,
            GMLMultiLineStringTypeBinding.class);
        container.registerComponentImplementation(GML.MULTIPOINTTYPE,
            GMLMultiPointTypeBinding.class);
        container.registerComponentImplementation(GML.MULTIPOLYGONTYPE,
            GMLMultiPolygonTypeBinding.class);
        container.registerComponentImplementation(GML.POINTMEMBERTYPE,
            GMLPointMemberTypeBinding.class);
        container.registerComponentImplementation(GML.POINTTYPE,
            GMLPointTypeBinding.class);
        container.registerComponentImplementation(GML.POLYGONMEMBERTYPE,
            GMLPolygonMemberTypeBinding.class);
        container.registerComponentImplementation(GML.POLYGONTYPE,
            GMLPolygonTypeBinding.class);

        //feature
        container.registerComponentImplementation(GML.ABSTRACTFEATURECOLLECTIONBASETYPE,
            GMLAbstractFeatureCollectionBaseTypeBinding.class);
        container.registerComponentImplementation(GML.ABSTRACTFEATURECOLLECTIONTYPE,
            GMLAbstractFeatureCollectionTypeBinding.class);
        container.registerComponentImplementation(GML.ABSTRACTFEATURETYPE,
            GMLAbstractFeatureTypeBinding.class);
        container.registerComponentImplementation(GML.BOUNDINGSHAPETYPE,
            GMLBoundingShapeTypeBinding.class);
        container.registerComponentImplementation(GML.FEATUREASSOCIATIONTYPE,
            GMLFeatureAssociationTypeBinding.class);
        container.registerComponentImplementation(GML.GEOMETRYPROPERTYTYPE,
            GMLGeometryPropertyTypeBinding.class);
        container.registerComponentImplementation(GML.LINESTRINGPROPERTYTYPE,
            GMLLineStringPropertyTypeBinding.class);
        container.registerComponentImplementation(GML.MULTIGEOMETRYPROPERTYTYPE,
            GMLMultiGeometryPropertyTypeBinding.class);
        container.registerComponentImplementation(GML.MULTILINESTRINGPROPERTYTYPE,
            GMLMultiLineStringPropertyTypeBinding.class);
        container.registerComponentImplementation(GML.MULTIPOINTPROPERTYTYPE,
            GMLMultiPointPropertyTypeBinding.class);
        container.registerComponentImplementation(GML.MULTIPOLYGONPROPERTYTYPE,
            GMLMultiPolygonPropertyTypeBinding.class);
        container.registerComponentImplementation(GML.NULLTYPE,
            GMLNullTypeBinding.class);
        container.registerComponentImplementation(GML.POINTPROPERTYTYPE,
            GMLPointPropertyTypeBinding.class);
        container.registerComponentImplementation(GML.POLYGONPROPERTYTYPE,
            GMLPolygonPropertyTypeBinding.class);
    }
}
