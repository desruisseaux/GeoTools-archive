/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.arcsde.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.geotools.arcsde.data.ArcSDEGeometryBuilder;
import org.geotools.arcsde.data.ArcSDEGeometryBuildingException;
import org.geotools.data.DataSourceException;
import org.geotools.filter.FilterCapabilities;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.And;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeFilter;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeShape;
import com.esri.sde.sdk.client.SeShapeFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Polygon;


/**
 * Encodes the geometry related parts of a filter into a set of
 * <code>SeFilter</code> objects and provides a method to get the resulting
 * filters suitable to set up an SeQuery's spatial constraints.
 * 
 * <p>
 * Although not all filters support is coded yet, the strategy to filtering
 * queries for ArcSDE datasources is separated in two parts, the SQL where
 * clause construction, provided by <code>FilterToSQLSDE</code> and the spatial
 * filters (or spatial constraints, in SDE vocabulary) provided here;
 * mirroring the java SDE api approach
 * </p>
 *
 * @author Gabriel Rold?n
 * @source $URL$
 */
public class GeometryEncoderSDE implements FilterVisitor {
    /** Standard java logger */
    private static Logger log = Logger.getLogger("org.geotools.filter");

    /** DOCUMENT ME! */
    private static FilterCapabilities capabilities = new FilterCapabilities();

    static {
        capabilities.addType(BBOX.class);
        capabilities.addType(Contains.class);
        capabilities.addType(Crosses.class);
        capabilities.addType(Disjoint.class);
        capabilities.addType(Equals.class);
        capabilities.addType(Intersects.class);
        capabilities.addType(Overlaps.class);
        capabilities.addType(Within.class);
    }

    /** DOCUMENT ME! */
    private List sdeSpatialFilters = null;

    /** DOCUMENT ME! */
    private SeLayer sdeLayer;
    
    private SimpleFeatureType featureType;

    /**
     */
    public GeometryEncoderSDE() {
    	//intentionally blank
    }

    /**
     */
    public GeometryEncoderSDE(SeLayer layer, SimpleFeatureType featureType) {
        this.sdeLayer = layer;
        this.featureType = featureType;
    }

    /**
     * DOCUMENT ME!
     *
     * @param layer DOCUMENT ME!
     *
     * @deprecated remove when the old data api dissapear
     */
    public void setLayer(SeLayer layer) {
        this.sdeLayer = layer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static FilterCapabilities getCapabilities() {
        return capabilities;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public SeFilter[] getSpatialFilters() {
        SeFilter[] filters = new SeFilter[this.sdeSpatialFilters.size()];

        return (SeFilter[]) this.sdeSpatialFilters.toArray(filters);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalStateException DOCUMENT ME!
     */
    private String getLayerName() throws SeException {
        if (this.sdeLayer == null) {
            throw new IllegalStateException("SDE layer has not been set");
        }
        return this.sdeLayer.getQualifiedName();
    }

    /**
     * overriden just to avoid the "WHERE" keyword
     *
     * @param filter DOCUMENT ME!
     *
     * @throws GeometryEncoderException DOCUMENT ME!
     */
    public void encode(Filter filter) throws GeometryEncoderException {
        this.sdeSpatialFilters = new ArrayList();
        if(Filter.INCLUDE.equals(filter)){
        	return;
        }
        if (capabilities.fullySupports(filter)) {
            filter.accept(this, null);
        } else {
            throw new GeometryEncoderException("Filter type " + filter.getClass() + " not supported");
        }
    }

    /**
     * This is an internal handler so all the logic is in one place.  The actual
     * methods that call back to this method are the ones specified in the FilterVisitor
     * interface
     */
    private Object visit(BinarySpatialOperator filter, Object extraData) {
        try {
            if (filter instanceof BBOX) {
                addSpatialFilter((BinarySpatialOperator)filter, SeFilter.METHOD_ENVP, true);
            } else if (filter instanceof Contains) {
                addSpatialFilter((BinarySpatialOperator)filter, SeFilter.METHOD_PC, true);
            } else if (filter instanceof Crosses) {
                addSpatialFilter((BinarySpatialOperator)filter, SeFilter.METHOD_LCROSS_OR_CP, true);
            } else if (filter instanceof Disjoint) {
                addSpatialFilter((BinarySpatialOperator)filter, SeFilter.METHOD_II_OR_ET, false);
            } else if (filter instanceof Equals) {
                addSpatialFilter((BinarySpatialOperator)filter, SeFilter.METHOD_IDENTICAL, true);
            } else if (filter instanceof Intersects) {
                addSpatialFilter((BinarySpatialOperator)filter, SeFilter.METHOD_II_OR_ET, true);
            } else if (filter instanceof Overlaps) {
                addSpatialFilter((BinarySpatialOperator)filter, SeFilter.METHOD_II, true);
                addSpatialFilter((BinarySpatialOperator)filter, SeFilter.METHOD_PC, false);
                addSpatialFilter((BinarySpatialOperator)filter, SeFilter.METHOD_SC, false);
            } else if (filter instanceof Within) {
                addSpatialFilter((BinarySpatialOperator)filter, SeFilter.METHOD_SC, true);
            } else {
                // This shouldn't happen since we will have pulled out
                // the unsupported parts before invoking this method
                String msg = "unsupported filter type";
                log.warning(msg);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error building SeFilter", e);
        }
        return extraData;
    }

    /**
     * DOCUMENT ME!
     *
     * @param filter DOCUMENT ME!
     * @param sdeMethod DOCUMENT ME!
     * @param truth DOCUMENT ME!
     *
     * @throws SeException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     * @throws GeometryBuildingException DOCUMENT ME!
     */
    private void addSpatialFilter(BinarySpatialOperator filter, int sdeMethod,
        boolean truth)
        throws SeException, DataSourceException, ArcSDEGeometryBuildingException {
        
        
        org.opengis.filter.expression.Expression left, right;
        PropertyName propertyExpr;
        Literal geomLiteralExpr;
        
        left = filter.getExpression1();
        right = filter.getExpression2();
        if (left instanceof PropertyName &&
            right instanceof Literal) {
            propertyExpr = (PropertyName)left;
            geomLiteralExpr = (Literal)right;
        } else if (right instanceof PropertyName &&
                   left instanceof Literal) {
            propertyExpr = (PropertyName) right;
            geomLiteralExpr = (Literal) left;
        } else {
            String err = "SDE currently supports one geometry and one " +
                "attribute expr.  You gave: " + left + ", " + right;
            throw new DataSourceException(err);
        }
   
        // Should probably assert that attExpr's property name is equal to
        // spatialCol...
        
        //HACK:  we want to support <namespace>:SHAPE, but current FM doesn't
        //support it.  I guess we should try stripping the prefix and seeing if that
        //matches...
        final String spatialCol = featureType.getDefaultGeometry().getLocalName();
        final String rawPropName = propertyExpr.getPropertyName();
        String localPropName = rawPropName;
        if (rawPropName.indexOf(":") != -1) {
            localPropName = rawPropName.substring(rawPropName.indexOf(":") + 1);
        }
        if (!rawPropName.equalsIgnoreCase(spatialCol) && !localPropName.equalsIgnoreCase(spatialCol)) {
            throw new DataSourceException("When querying against a spatial " +
                    "column, your property name must match the spatial" +
                    " column name.You used '" +
                    propertyExpr.getPropertyName() +
                    "', but the DB's spatial column name is '" +
                    spatialCol + "'");
        }
        Geometry geom = (Geometry) geomLiteralExpr.getValue();

        // To prevent errors in ArcSDE, we first trim the user's Filter
        // geometry to the extents of our layer.
        ArcSDEGeometryBuilder gb = ArcSDEGeometryBuilder.builderFor(Polygon.class);
        SeExtent seExtent = this.sdeLayer.getExtent();
        
        //If a layer just has one point in it (or one very horizontal or vertical line) then we may have
        // a layer extent that's a point or line.  We need to correct this.
        if (seExtent.getMaxX() == seExtent.getMinX()) {
            seExtent = new SeExtent(seExtent.getMinX() - 100, seExtent.getMinY(), seExtent.getMaxX() + 100, seExtent.getMaxY());
        }
        if (seExtent.getMaxY() == seExtent.getMinY()) {
            seExtent = new SeExtent(seExtent.getMinX(), seExtent.getMinY() -100, seExtent.getMaxX(), seExtent.getMaxY() + 100);
        }
        
        SeShape extent = new SeShape(this.sdeLayer.getCoordRef());
        extent.generateRectangle(seExtent);

        Geometry layerEnv = gb.construct(extent);
        geom = geom.intersection(layerEnv); // does the work

        // Now make an SeShape
        SeShape filterShape;
        
        //this is a bit hacky, but I don't yet know this code well enough
        //to do it right.  Basically if the geometry collection is completely
        //outside of the area of the layer then an intersection will return
        //a geometryCollection (two seperate geometries not intersecting will
        //be a collection of two).  Passing this into GeometryBuilder causes
        //an exception.  So what I did was just look to see if it is a gc
        //and if so then just make a null seshape, as it shouldn't match
        //any features in arcsde. -ch
        if (geom.getClass() == GeometryCollection.class) {
            filterShape = new SeShape(this.sdeLayer.getCoordRef());
        } else {
            gb = ArcSDEGeometryBuilder.builderFor(geom.getClass());
            filterShape = gb.constructShape(geom, this.sdeLayer.getCoordRef());
        }
        // Add the filter to our list
        SeShapeFilter shapeFilter = new SeShapeFilter(getLayerName(),
                this.sdeLayer.getSpatialColumn(), filterShape, sdeMethod, truth);
        this.sdeSpatialFilters.add(shapeFilter);
    }

    
    // The Spatial Operator methods (these call to the above visit() method
    public Object visit(BBOX arg0, Object arg1) {
        return visit((BinarySpatialOperator)arg0, arg1);
    }
    public Object visit(Beyond arg0, Object arg1) {
        return visit((BinarySpatialOperator)arg0, arg1);
    }
    public Object visit(Contains arg0, Object arg1) {
        return visit((BinarySpatialOperator)arg0, arg1);
    }
    public Object visit(Crosses arg0, Object arg1) {
        return visit((BinarySpatialOperator)arg0, arg1);
    }
    public Object visit(Disjoint arg0, Object arg1) {
        return visit((BinarySpatialOperator)arg0, arg1);
    }
    public Object visit(DWithin arg0, Object arg1) {
        return visit((BinarySpatialOperator)arg0, arg1);
    }
    public Object visit(Equals arg0, Object arg1) {
        return visit((BinarySpatialOperator)arg0, arg1);
    }
    public Object visit(Intersects arg0, Object arg1) {
        return visit((BinarySpatialOperator)arg0, arg1);
    }
    public Object visit(Overlaps arg0, Object arg1) {
        return visit((BinarySpatialOperator)arg0, arg1);
    }
    public Object visit(Within arg0, Object arg1) {
        return visit((BinarySpatialOperator)arg0, arg1);
    }
    public Object visit(Touches arg0, Object arg1) {
        return visit((BinarySpatialOperator)arg0, arg1);
    }
    
    //These are the 'just to implement the interface' methods.
    public Object visit(Id filter, Object extraData) {
        return extraData;
    }
    public Object visit(And arg0, Object arg1) {
        return arg1;
    }
    public Object visit(ExcludeFilter arg0, Object arg1) {
        return arg1;
    }
    public Object visit(IncludeFilter arg0, Object arg1) {
        return arg1;
    }
    public Object visit(Not arg0, Object arg1) {
        return arg1;
    }
    public Object visit(Or arg0, Object arg1) {
        return arg1;
    }
    public Object visit(PropertyIsBetween arg0, Object arg1) {
        return arg1;
    }
    public Object visit(PropertyIsEqualTo arg0, Object arg1) {
        return arg1;
    }
    public Object visit(PropertyIsGreaterThan arg0, Object arg1) {
        return arg1;
    }
    public Object visit(PropertyIsGreaterThanOrEqualTo arg0, Object arg1) {
        return arg1;
    }
    public Object visit(PropertyIsLessThan arg0, Object arg1) {
        return arg1;
    }
    public Object visit(PropertyIsLessThanOrEqualTo arg0, Object arg1) {
        return arg1;
    }
    public Object visit(PropertyIsLike arg0, Object arg1) {
        return arg1;
    }
    public Object visit(PropertyIsNotEqualTo arg0, Object arg1) {
        return arg1;
    }
    public Object visit(PropertyIsNull arg0, Object arg1) {
        return arg1;
    }
    public Object visitNullFilter(Object arg0) {
        return arg0;
    }
}
