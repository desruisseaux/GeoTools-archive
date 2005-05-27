/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) Copyright IBM Corporation, 2005. All rights reserved.
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
package org.geotools.data.db2.filter;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.logging.Logger;

import org.geotools.filter.AbstractFilter;
import org.geotools.filter.DefaultExpression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterCapabilities;
import org.geotools.filter.FilterVisitor;
import org.geotools.filter.GeometryDistanceFilter;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.SQLEncoder;
import org.geotools.filter.SQLEncoderException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * Generate a WHERE clause for DB2 Spatial Extender based on a spatial filter.
 * <p>
	 * The following spatial filter operations are supported:
	 * <ul>
	 * <li> GEOMETRY_BBOX
  	* <li> GEOMETRY_CONTAINS
	* <li> GEOMETRY_CROSSES
	* <li> GEOMETRY_DISJOINT
	* <li> GEOMETRY_EQUALS
	* <li> GEOMETRY_INTERSECTS
	* <li> GEOMETRY_OVERLAPS
	* <li> GEOMETRY_TOUCHES
	* <li> GEOMETRY_WITHIN
	* <li> GEOMETRY_DWITHIN
	 * </ul>
  *
 * @author David Adler - IBM Corporation
 */
public class SQLEncoderDB2 extends SQLEncoder implements FilterVisitor {
	
	private static Logger LOGGER = Logger.getLogger("org.geotools.data.db2");

	// Class to convert geometry value into a Well-known Text string	
	private static WKTWriter wktWriter = new WKTWriter();

	// The SELECTIVITY clause to be used with spatial predicates.	
	private String selectivityClause = null;
	
	// We need the srid to create an ST_Geometry - default to NAD83 for now
	private int srid = 1;
	
	private FilterCapabilities capabilities = null;
	
	static private HashMap DB2_SPATIAL_PREDICATES = new HashMap();
	{
		DB2_SPATIAL_PREDICATES.put(new Integer(AbstractFilter.GEOMETRY_BBOX),"EnvelopesIntersect");
		DB2_SPATIAL_PREDICATES.put(new Integer(AbstractFilter.GEOMETRY_CONTAINS),"ST_Contains");
		DB2_SPATIAL_PREDICATES.put(new Integer(AbstractFilter.GEOMETRY_CROSSES),"ST_Crosses");
		DB2_SPATIAL_PREDICATES.put(new Integer(AbstractFilter.GEOMETRY_DISJOINT),"ST_Disjoint");
		DB2_SPATIAL_PREDICATES.put(new Integer(AbstractFilter.GEOMETRY_EQUALS),"ST_Equals");
		DB2_SPATIAL_PREDICATES.put(new Integer(AbstractFilter.GEOMETRY_INTERSECTS),"ST_Intersects");
		DB2_SPATIAL_PREDICATES.put(new Integer(AbstractFilter.GEOMETRY_OVERLAPS),"ST_Overlaps");
		DB2_SPATIAL_PREDICATES.put(new Integer(AbstractFilter.GEOMETRY_TOUCHES),"ST_Touches");
		DB2_SPATIAL_PREDICATES.put(new Integer(AbstractFilter.GEOMETRY_WITHIN),"ST_Within");
		DB2_SPATIAL_PREDICATES.put(new Integer(AbstractFilter.GEOMETRY_DWITHIN),"ST_Distance");
		DB2_SPATIAL_PREDICATES.put(new Integer(AbstractFilter.GEOMETRY_BEYOND),"ST_Distance");

	}

/**
 * Construct an SQLEncoderDB2 
 */
	public SQLEncoderDB2() {
		super();
	}
	/**
	 * Construct an SQLEncoderDB2 that does the encoding (through superclass)
	 * directly based on the filter parameter.
	 * @param out a writer object
	 * @param filter query filter to be encoded
	 * @throws SQLEncoderException
	 */
	public SQLEncoderDB2(Writer out, Filter filter) throws SQLEncoderException {
		super(out, filter);
	}
	static private HashMap getPredicateTable() {
			return DB2_SPATIAL_PREDICATES;
	}
	
	/**
	 * Generate a WHERE clause for the input GeometryFilter.
	 * <p>
	 * The following spatial filter operations are supported:
	 * <ul>
	 * 	<li> GEOMETRY_BBOX
  	* <li> GEOMETRY_CONTAINS
	* <li> GEOMETRY_CROSSES
	* <li> GEOMETRY_DISJOINT
	* <li> GEOMETRY_EQUALS
	* <li> GEOMETRY_INTERSECTS
	* <li> GEOMETRY_OVERLAPS
	* <li> GEOMETRY_TOUCHES
	* <li> GEOMETRY_WITHIN
	* <li> GEOMETRY_DWITHIN
	* <li> GEOMETRY_BEYOND
	 * </ul>
	 *
	 * @param filter The geometry filter to be processed.
	 *
	 * @throws RuntimeException for IO exception
	 */
	public void visit(GeometryFilter filter) throws RuntimeException {
		LOGGER.finer("Generating GeometryFilter WHERE clause for " + filter);

		DefaultExpression left = (DefaultExpression) filter.getLeftGeometry();
		DefaultExpression right = (DefaultExpression) filter.getRightGeometry();
		// neither left nor right expression can be null
		if ((null == left) || (null == right)) {
			String msg = "Left or right expression is null - " + filter;
			LOGGER.warning(msg);
			throw new RuntimeException(msg);
		}

		try {

			String spatialPredicate =
				(String) getPredicateTable().get(
					new Integer(filter.getFilterType()));
			if (spatialPredicate == null) {
				String msg =
					"Unsupported filter type: " + filter.getFilterType();
				LOGGER.warning(msg);
				throw new RuntimeException(msg);
			}
			
			switch (filter.getFilterType()) {
				case AbstractFilter.GEOMETRY_DWITHIN:
					encodeDistance(left, right, "<", (GeometryDistanceFilter) filter);
			break;
				case AbstractFilter.GEOMETRY_BEYOND:	
					encodeDistance(left, right, ">", (GeometryDistanceFilter) filter);
				break;
			case AbstractFilter.GEOMETRY_BBOX:
				encodeBBox(left, right);
				break;
			default:
				this.out.write("db2gse." + spatialPredicate + "(");
				left.accept(this);
				this.out.write(", ");
				right.accept(this);
				this.out.write(") = 1");
			}
			if (this.selectivityClause != null) {
				this.out.write(" " + this.selectivityClause);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		LOGGER.fine(this.out.toString());
	}
	/** 
	 * Encode a bounding-box filter using the EnvelopesIntersect spatial predicate.
	 * 
	 * @param left an AttributeExpression which identifies a geometry column
	 * @param right a literal geomety expression
	 * @throws RuntimeException
	 */
	private void encodeBBox(DefaultExpression left, DefaultExpression right)
		throws RuntimeException {
		try {

			int leftType = left.getType();
			int rightType = right.getType();
			// The test below should use ATTRIBUTE_GEOMETRY but only the value ATTRIBUTE
			if ((DefaultExpression.ATTRIBUTE == leftType)
				&& (DefaultExpression.LITERAL_GEOMETRY == rightType)) {
				this.out.write("db2gse.EnvelopesIntersect(");
				left.accept(this);
				this.out.write(", ");
				Envelope env =
					((Geometry) ((LiteralExpression) right).getLiteral())
						.getEnvelopeInternal();
				this.out.write(
					env.getMinX()
						+ ", "
						+ env.getMinY()
						+ ", "
						+ env.getMaxX()
						+ ", "
						+ env.getMaxY()
						+ ", "
						+ srid);
				this.out.write(") = 1");
			} else {
				String msg =
					"Unsupported left and right types: "
						+ leftType
						+ ":"
						+ rightType;
				LOGGER.warning(msg);
				throw new RuntimeException(msg);
			}
		} catch (java.io.IOException e) {
			LOGGER.warning(
				"Filter not generated; I/O problem of some sort" + e);
		}
	}
	
/** 
 * Encode BEYOND and DWITHIN filters using ST_Distance function.
	 * @param left an AttributeExpression which identifies a geometry column
	 * @param right a literal geomety expression
 * @param op the distance operator, either "<" or ">"
 * @param distance the distance value
 * @throws RuntimeException
 */
	private void encodeDistance(DefaultExpression left, DefaultExpression right, String op, GeometryDistanceFilter filter)
		throws RuntimeException {
		try {

			int leftType = left.getType();
			int rightType = right.getType();
			double distance = filter.getDistance();
			// The test below should use ATTRIBUTE_GEOMETRY but only the value ATTRIBUTE
			if ((DefaultExpression.ATTRIBUTE == leftType)
				&& (DefaultExpression.LITERAL_GEOMETRY == rightType)) {
				this.out.write("db2gse.ST_Distance(");
				left.accept(this);
				this.out.write(", ");
				right.accept(this);
				this.out.write(") " + op + " " + distance);

			} else {
				String msg =
					"Unsupported left and right types: "
						+ leftType
						+ ":"
						+ rightType;
				LOGGER.warning(msg);
				throw new RuntimeException(msg);
			}
		} catch (java.io.IOException e) {
			LOGGER.warning(
				"Filter not generated; I/O problem of some sort" + e);
		}
	}
	

/**
 * Construct an ST_Geometry from the WKT representation of a literal expression
 *
 * @param expression the expression turn into an ST_Geometry.
 *
 * @throws IOException Passes back exception if generated by this.out.write()
 */
public void visitLiteralGeometry(LiteralExpression expression)
	throws IOException 
{
	String wktRepresentation = wktWriter.write((Geometry) expression.getLiteral());
	this.out.write("db2gse.ST_Geometry('" + wktRepresentation + "', " + this.srid + ")");
}

/**
 * Set the value of the srid value to be used if a DB2 Spatial Extender geometry needs
 * to be constructed.
 * <p>
 * This is specifically the DB2 Spatial Extender spatial reference system identifier and
 * not a coordinate system identifier ala EPSG.
 * @param srid Spatial reference system identifier to be used.
 */
public void setSRID(int srid) {       
	this.srid = srid;
}

/**
 * Sets the DB2 filter capabilities.
 *  
 * @return FilterCapabilities for DB2
 */
protected FilterCapabilities createFilterCapabilities() {
	if (this.capabilities == null) {
		this.capabilities = new FilterCapabilities();
	this.capabilities.addType(AbstractFilter.LOGIC_OR);
	this.capabilities.addType(AbstractFilter.LOGIC_AND);
	this.capabilities.addType(AbstractFilter.LOGIC_NOT);
	this.capabilities.addType(AbstractFilter.COMPARE_EQUALS);
	this.capabilities.addType(AbstractFilter.COMPARE_NOT_EQUALS);
	this.capabilities.addType(AbstractFilter.COMPARE_LESS_THAN);
	this.capabilities.addType(AbstractFilter.COMPARE_GREATER_THAN);
	this.capabilities.addType(AbstractFilter.COMPARE_LESS_THAN_EQUAL);
	this.capabilities.addType(AbstractFilter.COMPARE_GREATER_THAN_EQUAL);
	this.capabilities.addType(AbstractFilter.NULL);
	this.capabilities.addType(AbstractFilter.BETWEEN);
	this.capabilities.addType(AbstractFilter.FID);
	this.capabilities.addType((short) 12345);
	this.capabilities.addType((short) -12345);
	this.capabilities.addType(AbstractFilter.GEOMETRY_BBOX);
	this.capabilities.addType(AbstractFilter.GEOMETRY_CONTAINS);
	this.capabilities.addType(AbstractFilter.GEOMETRY_CROSSES);
	this.capabilities.addType(AbstractFilter.GEOMETRY_DISJOINT);
	this.capabilities.addType(AbstractFilter.GEOMETRY_EQUALS);
	this.capabilities.addType(AbstractFilter.GEOMETRY_INTERSECTS);
	this.capabilities.addType(AbstractFilter.GEOMETRY_OVERLAPS);
	this.capabilities.addType(AbstractFilter.GEOMETRY_TOUCHES);
	this.capabilities.addType(AbstractFilter.GEOMETRY_WITHIN);
	this.capabilities.addType(AbstractFilter.GEOMETRY_DWITHIN);	
	this.capabilities.addType(AbstractFilter.GEOMETRY_BEYOND);	
	}
	// Does this need to be immutable???
	return this.capabilities;
}


	/**
	 * Sets a SELECTIVITY clause that can be included with the
	 * spatial predicate to influence the query optimizer to exploit
	 * a spatial index if it exists.
	 * <p>
	 * The parameter should be of the form:
	 * <br>
	 * "SELECTIVITY 0.001"
	 * <br>
	 * where the numeric value is the fraction of rows that will be
	 * returned by using the index scan.  This doesn't have to be
	 * true.  The value 0.001 is typically used to force the use of
	 * the spatial in all cases if the spatial index exists.
	 * @param string a selectivity clause
	 */
	public void setSelectivityClause(String string) {
		this.selectivityClause = string;
	}

}
