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
package org.geotools.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.arcsde.GeometryBuilder;
import org.geotools.data.arcsde.GeometryBuildingException;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeFilter;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeShape;
import com.esri.sde.sdk.client.SeShapeFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;


/**
 * Encodes the geometry and FID related parts of a filter into a set of
 * <code>SeFilter</code> objects and provides a method to get the resulting
 * filters suitable to set up an SeQuery's spatial constraints.
 * 
 * <p>
 * Although not all filters support is coded yet, the strategy to filtering
 * queries for ArcSDE datasources is separated in two parts, the SQL where
 * clause construction, provided by <code>SQLEncoderSDE</code> and the spatial
 * filters (or spatial constraints, in SDE vocabulary) provided here;
 * mirroring the java SDE api approach
 * </p>
 *
 * @author Gabriel Roldán
 */
public class GeometryEncoderSDE implements org.geotools.filter.FilterVisitor {
    /** Standard java logger */
    private static Logger log = Logger.getLogger("org.geotools.filter");

    /** DOCUMENT ME! */
    private static FilterCapabilities capabilities = new FilterCapabilities();

    static {
        capabilities.addType(AbstractFilter.GEOMETRY_BBOX);
        capabilities.addType(AbstractFilter.GEOMETRY_CONTAINS);
        capabilities.addType(AbstractFilter.GEOMETRY_CROSSES);
        capabilities.addType(AbstractFilter.GEOMETRY_DISJOINT);
        capabilities.addType(AbstractFilter.GEOMETRY_EQUALS);
        capabilities.addType(AbstractFilter.GEOMETRY_INTERSECTS);
        capabilities.addType(AbstractFilter.GEOMETRY_OVERLAPS);
        capabilities.addType(AbstractFilter.GEOMETRY_WITHIN);
    }

    /** DOCUMENT ME! */
    private List sdeSpatialFilters = null;

    /** DOCUMENT ME! */
    private SeLayer sdeLayer;

    /**
     */
    public GeometryEncoderSDE() {
    }

    /**
     */
    public GeometryEncoderSDE(SeLayer layer) {
        this.sdeLayer = layer;
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
        SeFilter[] filters = new SeFilter[sdeSpatialFilters.size()];

        return (SeFilter[]) sdeSpatialFilters.toArray(filters);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalStateException DOCUMENT ME!
     */
    private String getLayerName() {
        if (sdeLayer == null) {
            throw new IllegalStateException("SDE layer has not been set");
        }

        return sdeLayer.getName();
    }

    /**
     * overriden just to avoid the "WHERE" keyword
     *
     * @param filter DOCUMENT ME!
     *
     * @throws GeometryEncoderException DOCUMENT ME!
     */
    public void encode(Filter filter) throws GeometryEncoderException {
        sdeSpatialFilters = new ArrayList();

        if (capabilities.fullySupports(filter)) {
            filter.accept(this);
        } else {
            throw new GeometryEncoderException("Filter type not supported");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param filter DOCUMENT ME!
     *
     * @throws RuntimeException DOCUMENT ME!
     */
    public void visit(GeometryFilter filter) {
        try {
            switch (filter.getFilterType()) {
            case AbstractFilter.GEOMETRY_BBOX:
                addSpatialFilter(filter, SeFilter.METHOD_ENVP, true);

                break;

            case AbstractFilter.GEOMETRY_CONTAINS:
                addSpatialFilter(filter, SeFilter.METHOD_PC, true);

                break;

            case AbstractFilter.GEOMETRY_CROSSES:
                addSpatialFilter(filter, SeFilter.METHOD_LCROSS_OR_CP, true);

                break;

            case AbstractFilter.GEOMETRY_DISJOINT:
                addSpatialFilter(filter, SeFilter.METHOD_II_OR_ET, false);

                break;

            case AbstractFilter.GEOMETRY_EQUALS:
                addSpatialFilter(filter, SeFilter.METHOD_IDENTICAL, true);

                break;

            case AbstractFilter.GEOMETRY_INTERSECTS:
                addSpatialFilter(filter, SeFilter.METHOD_II_OR_ET, true);

                break;

            case AbstractFilter.GEOMETRY_OVERLAPS:
                addSpatialFilter(filter, SeFilter.METHOD_II, true);
                addSpatialFilter(filter, SeFilter.METHOD_PC, false);
                addSpatialFilter(filter, SeFilter.METHOD_SC, false);

                break;

            case AbstractFilter.GEOMETRY_WITHIN:
                addSpatialFilter(filter, SeFilter.METHOD_SC, true);

                break;

            default: {
                // This shouldn't happen since we will have pulled out
                // the unsupported parts before invoking this method
                String msg = "unsupported filter type";
                log.warning(msg);
            }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error building SeFilter", e);
        }
    }

    /**
     * This only exists the fulfill the interface - unless There is a way of
     * determining the FID column in the database...
     *
     * @param filter the Fid Filter.
     */
    public void visit(FidFilter filter) {
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
    private void addSpatialFilter(GeometryFilter filter, int sdeMethod,
        boolean truth)
        throws SeException, DataSourceException, GeometryBuildingException {
        AttributeExpression attExpr = (AttributeExpression) filter
            .getLeftGeometry();
        LiteralExpression geomExpr = (LiteralExpression) filter
            .getRightGeometry();

        // Should probably assert that attExpr's property name is equal to
        // spatialCol...
        String spatialCol = sdeLayer.getSpatialColumn();
        Geometry geom = (Geometry) geomExpr.getLiteral();

        // To prevent errors in ArcSDE, we first trim the user's Filter
        // geometry to the extents of our layer.
        GeometryBuilder gb = GeometryBuilder.builderFor(Polygon.class);
        SeExtent seExtent = sdeLayer.getExtent();
        SeShape extent = new SeShape(sdeLayer.getCoordRef());
        extent.generateRectangle(seExtent);

        Geometry layerEnv = gb.construct(extent);
        geom = geom.intersection(layerEnv); // does the work

        // Now make an SeShape
        gb = GeometryBuilder.builderFor(geom.getClass());

        SeShape filterShape = gb.constructShape(geom, sdeLayer.getCoordRef());

        // Add the filter to our list
        SeShapeFilter shapeFilter = new SeShapeFilter(getLayerName(),
                sdeLayer.getSpatialColumn(), filterShape, sdeMethod, truth);
        sdeSpatialFilters.add(shapeFilter);
    }

    /**
     * DOCUMENT ME!
     *
     * @param filter DOCUMENT ME!
     */
    public void visit(Filter filter) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param filter DOCUMENT ME!
     */
    public void visit(BetweenFilter filter) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param filter DOCUMENT ME!
     */
    public void visit(CompareFilter filter) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param filter DOCUMENT ME!
     */
    public void visit(LikeFilter filter) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param filter DOCUMENT ME!
     */
    public void visit(LogicFilter filter) {
        log.finer("exporting LogicFilter");

        /*
           filter.getFilterType();
           String type = (String) logical.get(new Integer(filter.getFilterType()));
           try {
               java.util.Iterator list = filter.getFilterIterator();
               if (filter.getFilterType() == AbstractFilter.LOGIC_NOT) {
                   out.write(" NOT (");
                   ((AbstractFilter) list.next()).accept(this);
                   out.write(")");
               } else { //AND or OR
                   out.write("(");
                   while (list.hasNext()) {
                       ((AbstractFilter) list.next()).accept(this);
                       if (list.hasNext()) {
                           out.write(" " + type + " ");
                       }
                   }
                   out.write(")");
               }
           } catch (java.io.IOException ioe) {
               throw new RuntimeException(IO_ERROR, ioe);
           }
         */
    }

    /**
     * DOCUMENT ME!
     *
     * @param filter DOCUMENT ME!
     */
    public void visit(NullFilter filter) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param expression DOCUMENT ME!
     */
    public void visit(AttributeExpression expression) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param expression DOCUMENT ME!
     */
    public void visit(Expression expression) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param expression DOCUMENT ME!
     */
    public void visit(LiteralExpression expression) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param expression DOCUMENT ME!
     */
    public void visit(MathExpression expression) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param expression DOCUMENT ME!
     */
    public void visit(FunctionExpression expression) {
    }
}
