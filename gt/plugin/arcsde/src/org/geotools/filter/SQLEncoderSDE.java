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

import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 * @author Gabriel Roldán
 *
 * @see org.geotools.data.sde.GeometryEncoderSDE
 */
public class SQLEncoderSDE extends SQLEncoder
    implements org.geotools.filter.FilterVisitor {
    /** Standard java logger */
    private static Logger LOGGER = Logger.getLogger("org.geotools.filter");

    /** DOCUMENT ME! */
    private SeLayer sdeLayer;

    /**
     * Creates a new SQLEncoderSDE object.
     */
    public SQLEncoderSDE() {
    }

    /**
     */
    public SQLEncoderSDE(SeLayer layer) {
        this.sdeLayer = layer;
    }

    /**
     * Overrides the superclass implementation to fully qualify
     *
     * @param expression DOCUMENT ME!
     *
     * @throws RuntimeException DOCUMENT ME!
     */
    public void visit(AttributeExpression expression) throws RuntimeException {
        try {
            out.write(sdeLayer.getQualifiedName());
            out.write('.');
        } catch (java.io.IOException ioe) {
            throw new RuntimeException("IO problems writing attribute exp", ioe);
        } catch (SeException see) {
            throw new RuntimeException("SDE problems writing attribute exp", see);
        }

        super.visit(expression);
    }

    /**
     * Overrides the superclass implementation to indicate that we support
     * pushing FeatureId filters down into the data store.
     *
     * @return DOCUMENT ME!
     */
    protected FilterCapabilities createFilterCapabilities() {
        FilterCapabilities result = super.createFilterCapabilities();
        result.addType(FilterType.FID);

        return result;
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
     * @throws SQLEncoderException DOCUMENT ME!
     */
    public void encode(Writer out, Filter filter) throws SQLEncoderException {
        if (getCapabilities().fullySupports(filter)) {
            this.out = out;
            filter.accept(this);
        } else {
            throw new SQLEncoderException("Filter type not supported");
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
    public void visit(FidFilter filter) {
        long[] fids = getNumericFids(filter.getFids());
        int nFids = fids.length;

        if (nFids == 0) {
            return;
        }

        String fidField = sdeLayer.getSpatialColumn();

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

            System.err.println("QUERY: " + sb.toString());
            out.write(sb.toString());
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param stringFids DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static long[] getNumericFids(String[] stringFids)
        throws IllegalArgumentException {
        int nfids = stringFids.length;
        long[] fids = new long[nfids];

        for (int i = 0; i < nfids; i++) {
            fids[i] = getNumericFid(stringFids[i]);
        }

        return fids;
    }

    /**
     * Returns the numeric identifier of a FeatureId, given by the full
     * qualified name of the featureclass prepended to the ArcSDE feature id.
     * ej: SDE.SDE.SOME_LAYER.1
     *
     * @param fid a geotools FeatureID
     *
     * @return an ArcSDE feature ID
     *
     * @throws IllegalArgumentException If the given string is not properly
     *         formatted [anystring].[long value]
     */
    public static long getNumericFid(String fid)
        throws IllegalArgumentException {
        int dotIndex = fid.lastIndexOf('.');

        try {
            return Long.decode(fid.substring(++dotIndex)).longValue();
        } catch (Exception ex) {
            throw new IllegalArgumentException("FeatureID " + fid
                + " does not seems as a valid ArcSDE FID");
        }
    }
}
