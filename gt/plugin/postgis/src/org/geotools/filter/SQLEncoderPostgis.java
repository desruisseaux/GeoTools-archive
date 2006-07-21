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

import java.io.IOException;
import java.util.logging.Logger;

import org.geotools.filter.FilterType;
import org.geotools.filter.expression.LiteralExpression;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;


/**
 * Encodes a filter into a SQL WHERE statement for postgis.  This class adds
 * the ability to turn geometry filters into sql statements if they are
 * bboxes.
 *
 * @author Chris Holmes, TOPP
 *
 * @task TODO: integrated with SQLEncoderPostgisGeos.java, as there no  real
 *       reason to have two different classes.  We just need to do testing to
 *       make sure both handle everything.  At the very least have the geos
 *       one extend more intelligently.
 * @source $URL$
 */
public class SQLEncoderPostgis extends SQLEncoder
    implements org.geotools.filter.FilterVisitor {
    /** Standard java logger */
    private static Logger LOGGER = Logger.getLogger("org.geotools.filter");

    /** To write geometry so postgis can read it. */
    private static WKTWriter wkt = new WKTWriter();

    /**
     * The filters that this encoder can processed. (Note this value shadows
     * private capabilities in superclass)
     */
    private FilterCapabilities capabilities = new FilterCapabilities();

    /**
     * The srid of the schema, so the bbox conforms.  Could be better to have
     * it in the bbox filter itself, but this works for now.
     */
    private int srid;

    /** The geometry attribute to use if none is specified. */
    private String defaultGeom;
    
    /** Whether the BBOX filter should be strict (using the exact geom), or 
     *  loose (using the envelopes) */
    protected boolean looseBbox = false;
    
   
    /**
     * Empty constructor TODO: rethink empty constructor, as BBOXes _need_ an
     * SRID, must make client set it somehow.  Maybe detect when encode is
     * called?
     *
     */
    public SQLEncoderPostgis() {
        capabilities = createFilterCapabilities();
        
        setSqlNameEscape("\"");
    }

    public SQLEncoderPostgis(boolean looseBbox) {
        this();
	this.looseBbox = looseBbox;
    }
    

    /**
     * 
     * @see org.geotools.filter.SQLEncoder#createFilterCapabilities()
     */
    protected FilterCapabilities createFilterCapabilities() {
        FilterCapabilities capabilities = new FilterCapabilities();

        capabilities.addType((long) 12345); //Filter.ALL?
        capabilities.addType((long) -12345); //Filter.NONE?
        capabilities.addType(FilterCapabilities.LOGIC_OR);
        capabilities.addType(FilterCapabilities.LOGIC_AND);
        capabilities.addType(FilterCapabilities.LOGIC_NOT);
        capabilities.addType(FilterCapabilities.COMPARE_EQUALS);
        capabilities.addType(FilterCapabilities.COMPARE_NOT_EQUALS);
        capabilities.addType(FilterCapabilities.COMPARE_LESS_THAN);
        capabilities.addType(FilterCapabilities.COMPARE_GREATER_THAN);
        capabilities.addType(FilterCapabilities.COMPARE_LESS_THAN_EQUAL);
        capabilities.addType(FilterCapabilities.COMPARE_GREATER_THAN_EQUAL);
        capabilities.addType(FilterCapabilities.NULL_CHECK);
        capabilities.addType(FilterCapabilities.BETWEEN);
        capabilities.addType(FilterCapabilities.SPATIAL_BBOX);
        capabilities.addType(FilterCapabilities.FID);

        return capabilities;
    }
    
    

    /**
     * Constructor with srid.
     *
     * @param srid spatial reference id to encode geometries with.
     */
    public SQLEncoderPostgis(int srid) {
        this(true);
        this.srid = srid;
    }

    /**
     * Sets whether the Filter.BBOX query should be 'loose', meaning that it
     * should just doing a bounding box against the envelope.  If set to
     * <tt>false</tt> then the BBOX query will perform a full intersects 
     * against the geometry, ensuring that it is exactly correct.  If 
     * <tt>true</tt> then the query will likely perform faster, but may not
     * be exactly correct.
     *
     * @param isLooseBbox whether the bbox should be loose or strict.
     */
    public void setLooseBbox(boolean isLooseBbox) {
	this.looseBbox = isLooseBbox;
    }
   
    /**
     * Gets whether the Filter.BBOX query will be strict and use an intersects
     * or 'loose' and just operate against the geometry envelopes.
     *
     * @return <tt>true</tt> if this encoder is going to do loose filtering.
     */
    public boolean isLooseBbox() {
	return looseBbox;
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
        LOGGER.finer("exporting GeometryFilter");

        if (filter.getFilterType() == AbstractFilter.GEOMETRY_BBOX) {
            DefaultExpression left = (DefaultExpression) filter.getLeftGeometry();
            DefaultExpression right = (DefaultExpression) filter
                .getRightGeometry();

            try {
                if (!looseBbox) {
                	//JD: disjoint not supported without geos
                	out.write("distance(");
                    //out.write("NOT disjoint(");
                }

                if (left == null) {
                    out.write("\"" + defaultGeom + "\"");
                } else {
                    left.accept(this);
                }

                if (!looseBbox) {
                    out.write(", ");
                } else {
                    out.write(" && ");
                }

                if (right == null) {
                    out.write("\"" + defaultGeom + "\"");
                } else {
                    right.accept(this);
                }

                if (!looseBbox) {
                	out.write(") = 0");
                    //out.write(")");
                }
            } catch (java.io.IOException ioe) {
                LOGGER.warning("Unable to export filter" + ioe);
            }
        } else {
            LOGGER.warning("exporting unknown filter type, only bbox supported");
            throw new RuntimeException("Only BBox is currently supported");
        }
    }

    /**
     * Checks to see if the literal is a geometry, and encodes it if it  is, if
     * not just sends to the parent class.
     *
     * @param expression the expression to visit and encode.
     *
     * @throws IOException for IO exception (need a better error)
     */
    public void visitLiteralGeometry(LiteralExpression expression)
        throws IOException {
        Geometry bbox = (Geometry) expression.getLiteral();
        String geomText = wkt.write(bbox);
        out.write("GeometryFromText('" + geomText + "', " + srid + ")");
    }
    
    /**
     * Writes the SQL for a Compare Filter.
     *
     * DJB: note, postgis overwrites this implementation because of the way
     *       null is handled.  This is for <PropertyIsNull> filters and <PropertyIsEqual> filters
     *       are handled.  They will come here with "property = null".  
     *       NOTE:
     *        SELECT * FROM <table> WHERE <column> isnull;  -- postgresql
     *        SELECT * FROM <table> WHERE isnull(<column>); -- oracle???
     * 
     * @param filter the comparison to be turned into SQL.
     *
     * @throws RuntimeException for io exception with writer
     */
    public void visit(CompareFilter filter) throws RuntimeException {
        LOGGER.finer("exporting SQL ComparisonFilter");

        DefaultExpression left = (DefaultExpression) filter.getLeftValue();
        DefaultExpression right = (DefaultExpression) filter.getRightValue();
        LOGGER.finer("Filter type id is " + filter.getFilterType());
        LOGGER.finer("Filter type text is "
            + comparisions.get(new Integer(filter.getFilterType())));

        String type = (String) comparisions.get(new Integer(
                    filter.getFilterType()));

        try {
        	// a bit hacky, but what else can we really do?
        	if ( (right == null) && (filter.getFilterType()==FilterType.COMPARE_EQUALS ) )
        	{
        		left.accept(this);
        		out.write(" isnull");
        	}
        	else
        	{
        		left.accept(this);
        		out.write(" " + type + " ");
        		right.accept(this);
        	}
        } catch (java.io.IOException ioe) {
            throw new RuntimeException(IO_ERROR, ioe);
        }
    }
    
    
    
}
