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

import org.geotools.data.jdbc.fidmapper.FIDMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Encodes a filter into a SQL WHERE statement.  It should hopefully be generic
 * enough that any SQL database will work with it, though it has only been
 * tested with MySQL and Postgis.  This generic SQL encoder should eventually
 * be able to encode all filters except Geometry Filters (currently
 * LikeFilters are not yet fully implemented, but when they are they should be
 * generic enough). This is because the OGC's SFS for SQL document specifies
 * two ways of doing SQL databases, one with native geometry types and one
 * without.  To implement an encoder for one of the two types simply subclass
 * off of this encoder and put in the proper GeometryFilter visit method. Then
 * add the filter types supported to the capabilities in the static
 * capabilities.addType block.
 *
 * @author Chris Holmes, TOPP
 *
 * @task TODO: Implement LikeFilter encoding, need to figure out escape chars,
 *       the rest of the code should work right.  Once fixed be sure to add
 *       the LIKE type to capabilities, so others know that they can be
 *       encoded.
 * @task REVISIT: need to figure out exceptions, we're currently eating io
 *       errors, which is bad. Probably need a generic visitor exception.
 */
public class SQLEncoder implements org.geotools.filter.FilterVisitor {
    //use these when Like is implemented.
    //The standard SQL multicharacter wild card. 
    //private static String SQL_WILD_MULTI = "%";
    //The standard SQL single character wild card.
    //private static String SQL_WILD_SINGLE = "_";
    // The escaped version of the single wildcard for the REGEXP pattern. 
    //private static String escapedWildcardSingle = "\\.\\?";
    // The escaped version of the multiple wildcard for the REGEXP pattern. 
    //private static String escapedWildcardMulti = "\\.\\*";

    /** 
     * Character used to escape column names 
     */
    private String colnameEscape = "";
    
    /** error message for exceptions */
    private static final String IO_ERROR = "io problem writing filter";

    /** The filter types that this class can encode */
    private static FilterCapabilities capabilities = null;

    /** Standard java logger */
    private static Logger LOGGER = Logger.getLogger("org.geotools.filter");

    /** Map of comparison types to sql representation */
    private static Map comparisions = new HashMap();

    /** Map of spatial types to sql representation */
    private static Map spatial = new HashMap();

    /** Map of logical types to sql representation */
    private static Map logical = new HashMap();

    /** Map of expression types to sql representation */
    private static Map expressions = new HashMap();

    static {
        comparisions.put(new Integer(AbstractFilter.COMPARE_EQUALS), "=");
        comparisions.put(new Integer(AbstractFilter.COMPARE_NOT_EQUALS), "!=");
        comparisions.put(new Integer(AbstractFilter.COMPARE_GREATER_THAN), ">");
        comparisions.put(new Integer(AbstractFilter.COMPARE_GREATER_THAN_EQUAL),
            ">=");
        comparisions.put(new Integer(AbstractFilter.COMPARE_LESS_THAN), "<");
        comparisions.put(new Integer(AbstractFilter.COMPARE_LESS_THAN_EQUAL),
            "<=");
        comparisions.put(new Integer(AbstractFilter.LIKE), "LIKE");
        comparisions.put(new Integer(AbstractFilter.NULL), "IS NULL");
        comparisions.put(new Integer(AbstractFilter.BETWEEN), "BETWEEN");

        expressions.put(new Integer(DefaultExpression.MATH_ADD), "+");
        expressions.put(new Integer(DefaultExpression.MATH_DIVIDE), "/");
        expressions.put(new Integer(DefaultExpression.MATH_MULTIPLY), "*");
        expressions.put(new Integer(DefaultExpression.MATH_SUBTRACT), "-");

        //more to come?
        spatial.put(new Integer(AbstractFilter.GEOMETRY_EQUALS), "Equals");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_DISJOINT), "Disjoint");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_INTERSECTS),
            "Intersects");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_TOUCHES), "Touches");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_CROSSES), "Crosses");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_WITHIN), "Within");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_CONTAINS), "Contains");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_OVERLAPS), "Overlaps");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_BEYOND), "Beyond");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_BBOX), "BBOX");

        logical.put(new Integer(AbstractFilter.LOGIC_AND), "AND");
        logical.put(new Integer(AbstractFilter.LOGIC_OR), "OR");
        logical.put(new Integer(AbstractFilter.LOGIC_NOT), "NOT");
    }

    /** where to write the constructed string from visiting the filters. */
    protected Writer out;

    /** the fid mapper used to encode the fid filters */
    protected FIDMapper mapper;

    /**
     * Empty constructor
     */
    public SQLEncoder() {
    }

    /**
     * Convenience constructor to perform the whole encoding process at once.
     *
     * @param out the writer to encode the SQL to.
     * @param filter the Filter to be encoded.
     *
     * @throws SQLEncoderException If there were problems encoding
     */
    public SQLEncoder(Writer out, Filter filter) throws SQLEncoderException {
        if (getCapabilities().fullySupports(filter)) {
            this.out = out;

            try {
                out.write("WHERE ");
                filter.accept(this);

                //out.write(";"); this should probably be added by client.
            } catch (java.io.IOException ioe) {
                LOGGER.warning("Unable to export filter: " + ioe);
                throw new SQLEncoderException("Problem writing filter: ", ioe);
            }
        } else {
            throw new SQLEncoderException("Filter type not supported");
        }
    }
    
    /**
     * Sets the FIDMapper that will be used in subsequente visit calls.
     * There must be a FIDMapper in order to invoke the FIDFilter encoder.
     * @param mapper
     */
    public void setFIDMapper(FIDMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Sets the capabilities of this filter.
     *
     * @return FilterCapabilities for this Filter
     */
    protected FilterCapabilities createFilterCapabilities() {
        FilterCapabilities capabilities = new FilterCapabilities();

        capabilities.addType(AbstractFilter.LOGIC_OR);
        capabilities.addType(AbstractFilter.LOGIC_AND);
        capabilities.addType(AbstractFilter.LOGIC_NOT);
        capabilities.addType(AbstractFilter.COMPARE_EQUALS);
        capabilities.addType(AbstractFilter.COMPARE_NOT_EQUALS);
        capabilities.addType(AbstractFilter.COMPARE_LESS_THAN);
        capabilities.addType(AbstractFilter.COMPARE_GREATER_THAN);
        capabilities.addType(AbstractFilter.COMPARE_LESS_THAN_EQUAL);
        capabilities.addType(AbstractFilter.COMPARE_GREATER_THAN_EQUAL);
        capabilities.addType(AbstractFilter.NULL);
        capabilities.addType(AbstractFilter.BETWEEN);
        capabilities.addType(AbstractFilter.FID);
        capabilities.addType((short) 12345);
        capabilities.addType((short) -12345);

        return capabilities;
    }

    /**
     * Performs the encoding, sends the encoded sql to the writer passed in.
     *
     * @param out the writer to encode the SQL to.
     * @param filter the Filter to be encoded.
     *
     * @throws SQLEncoderException If filter type not supported, or if there
     *         were io problems.
     */
    public void encode(Writer out, Filter filter) throws SQLEncoderException {
        if (getCapabilities().fullySupports(filter)) {
            this.out = out;

            try {
                out.write("WHERE ");
                filter.accept(this);

                //out.write(";");
            } catch (java.io.IOException ioe) {
                LOGGER.warning("Unable to export filter" + ioe);
                throw new SQLEncoderException("Problem writing filter: ", ioe);
            }
        } else {
            throw new SQLEncoderException("Filter type not supported");
        }
    }

    /**
     * Performs the encoding, returns a string of the encoded SQL.
     *
     * @param filter the Filter to be encoded.
     *
     * @return the string of the SQL where statement.
     *
     * @throws SQLEncoderException If filter type not supported, or if there
     *         were io problems.
     */
    public String encode(Filter filter) throws SQLEncoderException {
        StringWriter output = new StringWriter();
        encode(output, filter);

        return output.getBuffer().toString();
    }

    /**
     * Describes the capabilities of this encoder.
     * 
     * <p>
     * Performs lazy creation of capabilities.
     * </p>
     *
     * @return The capabilities supported by this encoder.
     */
    public synchronized FilterCapabilities getCapabilities() {
        if (capabilities == null) {
            capabilities = createFilterCapabilities();
        }

        return capabilities; //maybe clone?  Make immutable somehow
    }

    /**
     * This should never be called. This can only happen if a subclass of
     * AbstractFilter failes to implement its own version of
     * accept(FilterVisitor);
     *
     * @param filter The filter to visit
     *
     * @throws RuntimeException for IO Encoding problems.
     *
     * @task REVISIT: I don't think Filter.NONE and Filter.ALL should be
     *       handled here.  They should have their own methods, but they don't
     *       have interfaces, so I don't know if that's possible.
     */
    public void visit(Filter filter) {
        try {
            //HACK: 12345 are Filter.NONE and Filter.ALL, they
            //should have some better names though.
            if (filter.getFilterType() == 12345) {
                out.write("TRUE");
            } else if (filter.getFilterType() == -12345) {
                out.write("FALSE");
            } else {
                LOGGER.warning("exporting unknown filter type:"
                    + filter.toString());

                //throw new RuntimeException("Do not know how to export filter:"+filter.toString() );
            }
        } catch (java.io.IOException ioe) {
            throw new RuntimeException(IO_ERROR, ioe);
        }
    }

    /**
     * Writes the SQL for the Between Filter.
     *
     * @param filter the  Filter to be visited.
     *
     * @throws RuntimeException for io exception with writer
     */
    public void visit(BetweenFilter filter) throws RuntimeException {
        LOGGER.finer("exporting BetweenFilter");

        DefaultExpression left = (DefaultExpression) filter.getLeftValue();
        DefaultExpression right = (DefaultExpression) filter.getRightValue();
        DefaultExpression mid = (DefaultExpression) filter.getMiddleValue();
        LOGGER.finer("Filter type id is " + filter.getFilterType());
        LOGGER.finer("Filter type text is "
            + comparisions.get(new Integer(filter.getFilterType())));

        try {
            mid.accept(this);
            out.write(" BETWEEN ");
            left.accept(this);
            out.write(" AND ");
            right.accept(this);
        } catch (java.io.IOException ioe) {
            throw new RuntimeException(IO_ERROR, ioe);
        }
    }

    /**
     * Writes the SQL for the Like Filter.  Assumes the current java
     * implemented wildcards for the Like Filter: . for multi and .? for
     * single. And replaces them with the SQL % and _, respectively. Currently
     * does nothing, and should not be called, not included in the
     * capabilities.
     *
     * @param filter the Like Filter to be visited.
     *
     * @throws UnsupportedOperationException always, as likes aren't
     *         implemented yet.
     *
     * @task REVISIT: Need to think through the escape char, so it works  right
     *       when Java uses one, and escapes correctly with an '_'.
     */
    public void visit(LikeFilter filter) throws UnsupportedOperationException {
        String message = "Like Filter support not yet added.";
        throw new UnsupportedOperationException(message);

        /*        log.finer("exporting like filter");
           try{
           String pattern = filter.getPattern();
           pattern = pattern.replaceAll(escapedWildcardMulti, SQL_WILD_MULTI);
           pattern = pattern.replaceAll(escapedWildcardSingle, SQL_WILD_SINGLE);
            //pattern = pattern.replace('\\', ''); //get rid of java escapes.
            //TODO escape the '_' char, as it could be in our string and will
                     //mess up the SQL wildcard matching
                 ((ExpressionDefault)filter.getValue()).accept(this);
                 out.write(" LIKE ");
                 out.write("'"+pattern+"'");
              //if (pattern.indexOf(esc) != -1) { //if it uses the escape char
              //out.write(" ESCAPE " + "'" + esc + "'");  //this needs testing
              //} TODO figure out when to add ESCAPE clause,
              //probably just for the '_' char.
             } catch (java.io.IOException ioe){
                 throw new RuntimeException(IO_ERROR, ioe);
                 }*/
    }

    /**
     * Writes the SQL for the Logic Filter.
     *
     * @param filter the logic statement to be turned into SQL.
     *
     * @throws RuntimeException for io exception with writer
     */
    public void visit(LogicFilter filter) throws RuntimeException {
        LOGGER.finer("exporting LogicFilter");

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
    }

    /**
     * Writes the SQL for a Compare Filter.
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
            left.accept(this);
            out.write(" " + type + " ");
            right.accept(this);
        } catch (java.io.IOException ioe) {
            throw new RuntimeException(IO_ERROR, ioe);
        }
    }

    /**
     * Writes the SQL for the Null Filter.
     *
     * @param filter the null filter to be written to SQL.
     *
     * @throws RuntimeException for io exception with writer
     */
    public void visit(NullFilter filter) throws RuntimeException {
        LOGGER.finer("exporting NullFilter");

        DefaultExpression expr = (DefaultExpression) filter.getNullCheckValue();

        //String type = (String) comparisions.get(new Integer(
        //          filter.getFilterType()));
        try {
            expr.accept(this);
            out.write(" IS NULL ");
        } catch (java.io.IOException ioe) {
            throw new RuntimeException(IO_ERROR, ioe);
        }
    }

    /**
     * Encodes an FidFilter.
     *
     * @param filter
     *
     * @see org.geotools.filter.SQLEncoder#visit(org.geotools.filter.FidFilter)
     */
    public void visit(FidFilter filter) {
        if(mapper == null)
            throw new RuntimeException("Must set a fid mapper before trying to encode FIDFilters");
        
        String[] fids = filter.getFids();
        LOGGER.finer("Exporting FID=" + Arrays.asList(fids));

        // prepare column name array
        String[] colNames = new String[mapper.getColumnCount()];

        for (int i = 0; i < colNames.length; i++) {
            colNames[i] = mapper.getColumnName(i);
        }

        for (int i = 0; i < fids.length; i++) {
            try {
                Object[] attValues = mapper.getPKAttributes(fids[i]);

                out.write("(");

                for (int j = 0; j < attValues.length; j++) {
                    out.write(colNames[j]);
                    out.write(" = '");
                    out.write(attValues[i].toString());
                    out.write("'");

                    if (j < (attValues.length - 1)) {
                        out.write(" AND ");
                    }
                }

                out.write(")");

                if (i < (fids.length - 1)) {
                    out.write(" OR ");
                }
            } catch (java.io.IOException e) {
                LOGGER.warning("IO Error exporting FID Filter.");
            }
        }
    }

    /**
     * Writes the SQL for the attribute Expression.
     *
     * @param expression the attribute to turn to SQL.
     *
     * @throws RuntimeException for io exception with writer
     */
    public void visit(AttributeExpression expression) throws RuntimeException {
        LOGGER.finer("exporting ExpressionAttribute");

        try {
            out.write(colnameEscape + expression.getAttributePath() + colnameEscape);
        } catch (java.io.IOException ioe) {
            throw new RuntimeException("IO problems writing attribute exp", ioe);
        }
    }

    /**
     * Writes the SQL for the attribute Expression.
     *
     * @param expression the attribute to turn to SQL.
     */
    public void visit(Expression expression) {
        LOGGER.warning("exporting unknown (default) expression");
    }

    /**
     * Export the contents of a Literal Expresion
     *
     * @param expression the Literal to export
     *
     * @throws RuntimeException for io exception with writer
     */
    public void visit(LiteralExpression expression) throws RuntimeException {
        LOGGER.finer("exporting LiteralExpression");

        try {
            Object literal = expression.getLiteral();
            short type = expression.getType();

            switch (type) {
            case Expression.LITERAL_DOUBLE:
            case Expression.LITERAL_INTEGER:
                out.write(literal.toString());

                break;

            case Expression.LITERAL_STRING:
                out.write("'" + literal + "'");

                break;
                
            case Expression.LITERAL_GEOMETRY:
                visitLiteralGeometry(expression);
                
                break;

            default:
                throw new RuntimeException("type: " + type + "not supported");
            }
        } catch (java.io.IOException ioe) {
            throw new RuntimeException("IO problems writing literal", ioe);
        }
    }

    /**
     * Subclasses must implement this method in order to encode geometry filters
     * according to the specific database implementation
     * @param expression
     */    
    protected void visitLiteralGeometry(LiteralExpression expression) throws IOException {
        throw new RuntimeException("Subclasses must implement this method in order to handle geometries");
    }
    
    
    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.GeometryFilter)
     */
    public void visit(GeometryFilter filter) {
        throw new RuntimeException("Subclasses must implement this method in order to handle geometries");
    }
    
    /**
     * Writes the SQL for the Math Expression.
     *
     * @param expression the Math phrase to be written.
     *
     * @throws RuntimeException for io problems
     */
    public void visit(MathExpression expression) throws RuntimeException {
        LOGGER.finer("exporting Expression Math");

        String type = (String) expressions.get(new Integer(expression.getType()));

        try {
            ((DefaultExpression) expression.getLeftValue()).accept(this);
            out.write(" " + type + " ");
            ((DefaultExpression) expression.getRightValue()).accept(this);
        } catch (java.io.IOException ioe) {
            throw new RuntimeException("IO problems writing expression", ioe);
        }
    }

    /**
     * Writes sql for a function expression.  Not currently supported.
     *
     * @param expression a function expression
     *
     * @throws UnsupportedOperationException every time, this isn't supported.
     */
    public void visit(FunctionExpression expression)
        throws UnsupportedOperationException {
        String message = "Function expression support not yet added.";
        throw new UnsupportedOperationException(message);
    }

    
    /**
     * @return
     */
    protected String getColnameEscape() {
        return colnameEscape;
    }

    /**
     * @param string
     */
    protected void setColnameEscape(String string) {
        colnameEscape = string;
    }

}
