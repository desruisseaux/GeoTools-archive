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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.data.ArcSDEAdapter;
import org.geotools.data.jdbc.FilterToSQL;
import org.geotools.data.jdbc.FilterToSQLException;
import org.geotools.feature.FeatureType;
import org.geotools.filter.FilterCapabilities;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Expression;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeLayer;


/**
 * Encodes an attribute filter into a SQL WHERE statement for arcsde.
 * 
 * <p>
 * Although not all filters support is coded yet, the strategy to filtering
 * queries for ArcSDE datasources is separated in two parts, the SQL where
 * clause construction, provided here and the  spatial filters (or spatial
 * constraints, in SDE vocabulary) provided by
 * <code>GeometryEncoderSDE</code>; mirroring the java SDE api approach for
 * easy programing
 * </p>
 *
 * @author Chris Holmes, TOPP
 * @author Gabriel Rold?n
 *
 * @see org.geotools.data.sde.GeometryEncoderSDE
 * @source $URL$
 */
public class FilterToSQLSDE extends FilterToSQL
    implements FilterVisitor {
    /** Standard java logger */
    private static Logger LOGGER = Logger.getLogger("org.geotools.filter");

    /** DOCUMENT ME! */
    private SeLayer sdeLayer;

    /**
     * Creates a new FilterToSQLSDE object.
     */
    public FilterToSQLSDE() {
//    	intentionally blank
    }

    /**
     */
    public FilterToSQLSDE(SeLayer layer, FeatureType ft) {
        this.sdeLayer = layer;
        this.featureType = ft;
    }

    /**
     * Called directly before visiting an appropriate expression, this
     * method adds the SDE qualified name before the expression's DB
     * name gets written.
     * 
     * So the filter [ MYFIELD = MYVALUE ] gets written as
     * MYUSER.MYTABLE.MYFIELD = ... instead.
     */
    private void qualifyExpression(Expression expression) throws RuntimeException {
        try {
        	this.out.write(this.sdeLayer.getQualifiedName());
        	this.out.write('.');
        } catch (java.io.IOException ioe) {
            throw new RuntimeException("IO problems writing attribute exp", ioe);
        } catch (SeException see) {
            throw new RuntimeException("SDE problems writing attribute exp", see);
        }
    }

    /**
     * Overrides the superclass implementation to indicate that we support
     * pushing FeatureId filters down into the data store.
     *
     * @return DOCUMENT ME!
     */
    protected FilterCapabilities createFilterCapabilities() {
        FilterCapabilities capabilities = new FilterCapabilities();

        capabilities.addAll(FilterCapabilities.LOGICAL_OPENGIS);
        capabilities.addAll(FilterCapabilities.SIMPLE_COMPARISONS_OPENGIS);
        capabilities.addType(PropertyIsNull.class);
        capabilities.addType(PropertyIsBetween.class);
        capabilities.addType(Id.class);
        capabilities.addType(IncludeFilter.class);
        capabilities.addType(ExcludeFilter.class);
        capabilities.addType(PropertyIsLike.class);

        return capabilities;
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
     * overriden just to avoid the "WHERE" keyword
     *
     * @param out DOCUMENT ME!
     * @param filter DOCUMENT ME!
     *
     * @throws GeoAPIFilterToSQLEncoderException DOCUMENT ME!
     */
    public void encode(Filter filter) throws FilterToSQLException {
        if (getCapabilities().fullySupports(filter)) {
            filter.accept(this, null);
        } else {
            throw new FilterToSQLException("Filter type not supported");
        }
    }

    /**
     * This only exists the fulfill the interface - unless There is a way of
     * determining the FID column in the database...
     *
     * @param filter the Fid Filter.
     *
     * @throws RuntimeException DOCUMENT ME!
     */
    public Object visit(Id filter, Object unused) {
        long[] fids = ArcSDEAdapter.getNumericFids(filter.getIdentifiers());
        int nFids = fids.length;

        if (nFids == 0) {
            return unused;
        }

        String fidField = ArcSDEAdapter.getRowIdColumn(featureType);
        if (fidField == null) { 
            fidField = this.sdeLayer.getSpatialColumn();
        }

        try {
            StringBuffer sb = new StringBuffer();
            sb.append(fidField + " IN(");

            for (int i = 0; i < nFids; i++) {
                sb.append(fids[i]);

                if (i < (nFids - 1)) {
                    sb.append(", ");
                }
            }

            sb.append(')');

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("added fid filter: " + sb.toString());
            }

            this.out.write(sb.toString());
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
        return unused;
    }
}
