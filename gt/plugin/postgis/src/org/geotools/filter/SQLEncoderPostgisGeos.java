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
package org.geotools.filter;

import java.util.logging.Logger;

import org.geotools.filter.expression.LiteralExpression;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;


/**
 * Encodes a filter into a SQL WHERE statement for postgis.  With geos
 * installed.  This should be redone, probably integrated with the postgis
 * stuff, but that whole hierarchy should be redone, since the capabilities
 * stuff is a bit wacky.  This should only be used on versions of postgis
 * installed with GEOS support, to handle all the advanced spatial queries.
 *
 * @author Chris Holmes, TOPP
 * @source $URL$
 * @version $Id$
 */
public class SQLEncoderPostgisGeos extends SQLEncoderPostgis
    implements org.geotools.filter.FilterVisitor {
    /** Standard java logger */
    private static Logger log = Logger.getLogger("org.geotools.filter");

    /** To write geometry so postgis can read it. */
    private static WKTWriter wkt = new WKTWriter();

    /**
     * The filters that this encoder can processed. (Note this value shadows
     * private capabils in superclass)
     */
    private FilterCapabilities capabils = new FilterCapabilities();

    /**
     * The srid of the schema, so the bbox conforms.  Could be better to have
     * it in the bbox filter itself, but this works for now.
     */
    private int srid;

    /** The geometry attribute to use if none is specified. */
    private String defaultGeom;

    /**
     * Empty constructor TODO: rethink empty constructor, as BBOXes _need_ an
     * SRID, must make client set it somehow.  Maybe detect when encode is
     * called?
     */
    public SQLEncoderPostgisGeos() {
        capabils.addType((long) 12345); //Filter.ALL?
        capabils.addType((long) -12345); //Filter.NONE?
        capabils.addType(FilterCapabilities.BETWEEN);
        capabils.addType(FilterCapabilities.COMPARE_EQUALS);
        capabils.addType(FilterCapabilities.COMPARE_GREATER_THAN);
        capabils.addType(FilterCapabilities.COMPARE_GREATER_THAN_EQUAL);
        capabils.addType(FilterCapabilities.COMPARE_LESS_THAN);
        capabils.addType(FilterCapabilities.COMPARE_LESS_THAN_EQUAL);
        capabils.addType(FilterCapabilities.COMPARE_NOT_EQUALS);
        capabils.addType(FilterCapabilities.FID);
        capabils.addType(FilterCapabilities.LIKE);
        capabils.addType(FilterCapabilities.LOGIC_AND);
        capabils.addType(FilterCapabilities.LOGIC_NOT);
        capabils.addType(FilterCapabilities.LOGIC_OR);
        capabils.addType(FilterCapabilities.NO_OP);
        capabils.addType(FilterCapabilities.NULL_CHECK);
        capabils.addType(FilterCapabilities.SIMPLE_ARITHMETIC);
        capabils.addType(FilterCapabilities.SIMPLE_COMPARISONS);
        capabils.addType(FilterCapabilities.SPATIAL_BBOX);
        capabils.addType(FilterCapabilities.SPATIAL_BEYOND);
        capabils.addType(FilterCapabilities.SPATIAL_CONTAINS);
        capabils.addType(FilterCapabilities.SPATIAL_CROSSES);
        capabils.addType(FilterCapabilities.SPATIAL_DISJOINT);
        capabils.addType(FilterCapabilities.SPATIAL_DWITHIN);
        capabils.addType(FilterCapabilities.SPATIAL_EQUALS);
        capabils.addType(FilterCapabilities.SPATIAL_INTERSECT);
        capabils.addType(FilterCapabilities.SPATIAL_OVERLAPS);
        capabils.addType(FilterCapabilities.SPATIAL_TOUCHES);
        capabils.addType(FilterCapabilities.SPATIAL_WITHIN);
    }

    /**
     * Constructor with srid.
     *
     * @param srid spatial reference id to encode geometries with.
     */
    public SQLEncoderPostgisGeos(int srid) {
        this();
        this.srid = srid;
    }

    /**
     * Capabililities of this encoder.
     *
     * @return
     *
     * @see org.geotools.filter.SQLEncoder#getCapabililties()
     */
    public FilterCapabilities getCapabilities() {
        return capabils;
    }

    /**
     * Sets a spatial reference system ESPG number, so that the geometry can be
     * properly encoded for postgis.  If geotools starts actually creating
     * geometries with valid srids then this method will no longer be needed.
     *
     * @param srid the integer code for the EPSG spatial reference system.
     */
    public void setSRID(int srid) {
        this.srid = srid;
    }

    /**
     * Sets the default geometry, so that filters with null for one of their
     * expressions can assume that the default geometry is intended.
     *
     * @param name the name of the default geometry Attribute.
     *
     * @task REVISIT: pass in a featureType so that geometries can figure out
     *       their own default geometry?
     */
    public void setDefaultGeometry(String name) {
        //Do we really want clients to be using malformed filters?  
        //I mean, this is a useful method for unit tests, but shouldn't 
        //fully formed filters usually be used?  Though I guess adding 
        //the option wouldn't hurt. -ch
        this.defaultGeom = name;
    }

    /**
     * Turns a geometry filter into the postgis sql bbox statement.
     *
     * @param filter the geometry filter to be encoded.
     *
     * @throws RuntimeException for IO exception (need a better error)
     */
    public void visit(GeometryFilter filter) throws RuntimeException {
        log.finer("exporting GeometryFilter");

        short filterType = filter.getFilterType();
        DefaultExpression left = (DefaultExpression) filter.getLeftGeometry();
        DefaultExpression right = (DefaultExpression) filter.getRightGeometry();

        // Figure out if we need to constrain this query with the && constraint.
        int literalGeometryCount = 0;

        if ((left != null)
                && (left.getType() == DefaultExpression.LITERAL_GEOMETRY)) {
            literalGeometryCount++;
        }

        if ((right != null)
                && (right.getType() == DefaultExpression.LITERAL_GEOMETRY)) {
            literalGeometryCount++;
        }

        boolean constrainBBOX = (literalGeometryCount == 1);
        boolean onlyBbox = filterType == AbstractFilter.GEOMETRY_BBOX  && looseBbox;

        try {

        	// DJB:  disjoint is not correctly handled in the pre-march 22/05 version
        	//       I changed it to not do a "&&" index search for disjoint because
        	//       Geom1 and Geom2 can have a bbox overlap and be disjoint
        	//       I also added test case.
        	//NOTE:  this will not use the index, but its unlikely that using the index
        	//       for a disjoint query will be the correct thing to do.
        
        	
        	// DJB NOTE:  need to check for a NOT(A intersects G) filter
        	//          -->  NOT( (A && G) AND intersects(A,G))
        	// and check that it does the right thing.
        	
        	
        	constrainBBOX = constrainBBOX && (filterType != AbstractFilter.GEOMETRY_DISJOINT);
        	

        
	    if (constrainBBOX) {
            
                if (left == null) {
                    out.write("\"" + defaultGeom + "\"");
                } else {
                    left.accept(this);
                }

                out.write(" && ");

                if (right == null) {
                    out.write("\"" + defaultGeom + "\"");
                } else {
                    right.accept(this);
                }
		if (!onlyBbox) {
		    out.write(" AND ");
                }                   
            }
	    

            String closingParenthesis = ")";

            if (!onlyBbox) {
            if (filterType == AbstractFilter.GEOMETRY_EQUALS) {
                out.write("equals");
            } else if (filterType == AbstractFilter.GEOMETRY_DISJOINT) {
                out.write("NOT (intersects");
                closingParenthesis += ")";
            } else if (filterType == AbstractFilter.GEOMETRY_INTERSECTS) {
                out.write("intersects");
            } else if (filterType == AbstractFilter.GEOMETRY_CROSSES) {
                out.write("crosses");
            } else if (filterType == AbstractFilter.GEOMETRY_WITHIN) {
                out.write("within");
            } else if (filterType == AbstractFilter.GEOMETRY_CONTAINS) {
                out.write("contains");
            } else if (filterType == AbstractFilter.GEOMETRY_OVERLAPS) {
                out.write("overlaps");
            } else if (filterType == AbstractFilter.GEOMETRY_BBOX) {
                out.write("intersects");
            } else if (filterType == AbstractFilter.GEOMETRY_TOUCHES) {
                out.write("touches");
            } else {
                //this will choke on beyond and dwithin
                throw new RuntimeException("does not support filter type "
                    + filterType);
            }
            out.write("(");

            if (left == null) {
                out.write("\"" + defaultGeom + "\"");
            } else {
                left.accept(this);
            }

            out.write(", ");

            if (right == null) {
                out.write("\"" + defaultGeom + "\"");
            } else {
                right.accept(this);
            }

            out.write(closingParenthesis);
	    }
        } catch (java.io.IOException ioe) {
            log.warning("Unable to export filter" + ioe);
            throw new RuntimeException("io error while writing", ioe);
        }
    }

    /**
     * Checks to see if the literal is a geometry, and encodes it if it  is, if
     * not just sends to the parent class.
     *
     * @param expression the expression to visit and encode.
     *
     * @throws RuntimeException for IO exception (need a better error)
     */
    public void visit(LiteralExpression expression) throws RuntimeException {
        log.finer("exporting LiteralExpression");

        try {
            if (expression.getType() == DefaultExpression.LITERAL_GEOMETRY) {
                Geometry bbox = (Geometry) expression.getLiteral();
                String geomText = wkt.write(bbox);
                out.write("GeometryFromText('" + geomText + "', " + srid + ")");
            } else {
                super.visit(expression);
            }
        } catch (java.io.IOException ioe) {
            log.warning("Unable to export expresion" + ioe);
            throw new RuntimeException("io error while writing", ioe);
        }
    }
}
