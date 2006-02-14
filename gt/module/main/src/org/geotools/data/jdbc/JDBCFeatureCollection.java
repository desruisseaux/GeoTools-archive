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
 */
package org.geotools.data.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.DataSourceException;
import org.geotools.data.DefaultFeatureResults;
import org.geotools.data.FeatureReader;
import org.geotools.data.MaxFeatureReader;
import org.geotools.data.Query;
import org.geotools.feature.visitor.AverageVisitor;
import org.geotools.feature.visitor.CountVisitor;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.feature.visitor.MaxVisitor;
import org.geotools.feature.visitor.MedianVisitor;
import org.geotools.feature.visitor.MinVisitor;
import org.geotools.feature.visitor.SumVisitor;
import org.geotools.feature.visitor.UniqueVisitor;
import org.geotools.filter.Filter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.SQLEncoderException;
import org.geotools.filter.expression.AttributeExpression;
import org.geotools.filter.expression.Expression;
import org.geotools.filter.visitor.AbstractFilterVisitor;
import org.geotools.util.NullProgressListener;
import org.geotools.util.ProgressListener;


/**
 *
 * @source $URL$
 */
public class JDBCFeatureCollection extends DefaultFeatureResults {
    /** The logger for the filter module. */
//    private static final Logger LOGGER = Logger.getLogger(
//            "org.geotools.data.jdbc");
    protected JDBCFeatureSource featureSource;
    public boolean isOptimized = false;

    public JDBCFeatureCollection(JDBCFeatureSource source, Query query) {
        super(source, query);
        featureSource = source;
    }

    JDBC1DataStore getDataStore() {
        return (JDBC1DataStore) featureSource.getDataStore();
    }

    /**
     * JDBCDataStore has a more direct query method
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public FeatureReader reader() throws IOException {
        int maxFeatures = query.getMaxFeatures();
        FeatureReader reader = getDataStore().getFeatureReader(query,
                getTransaction());

        //TODO: implement Query.UNLIMITED_FEATURES or something
        //to that effect, instead of constraining ourselves to 
        //integer max as we do now.  
        if (maxFeatures == Integer.MAX_VALUE) {
            return reader;
        } else {
            return new MaxFeatureReader(reader, maxFeatures);
        }
    }

    /**
     * Performs optimized count if possible.
     *
     * @return
     *
     * @throws IOException
     *
     * @see org.geotools.data.DefaultFeatureResults#getCount()
     */
    public int getCount() throws IOException {
        int count = featureSource.count(query, getTransaction());

        if (count != -1) {
            int maxFeatures = query.getMaxFeatures();

            return (count < maxFeatures) ? count : maxFeatures;

            // optimization worked, return maxFeatures if count is
            // greater.
        }

        return super.getCount();
    }

    /**
     * Accepts FeatureVisitors.
     * 
     * <p>
     * Note for some FeatureCalc visitors an optimized code path will be used.
     * </p>
     *
     * @param visitor DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void accepts(FeatureVisitor visitor, ProgressListener progress) throws IOException {
    	//TODO: review use of progress monitor
        if (progress == null) progress = new NullProgressListener();
        if (visitor instanceof MinVisitor) {
            progress.started();
        	MinVisitor minCalc = (MinVisitor) visitor;
            Object min = min(minCalc.getExpression());

            if (min != null) {
                // we had an optimized result, so tell the visitor the answer
				// rather than making him visit everything
                minCalc.setValue(min);
                isOptimized = true;
                progress.complete();
                return;
            } else {
            	progress.warningOccurred("JDBCFeatureCollection.accepts(min,)", null, "Optimization attempt returned null");
            }
        } else if (visitor instanceof MaxVisitor) {
            progress.started();
            MaxVisitor maxCalc = (MaxVisitor) visitor;
            Object max = max(maxCalc.getExpression());

            if (max != null) {
                maxCalc.setValue(max); // use the optimized result
                isOptimized = true;
                progress.complete();
                return;
            } else {
            	progress.warningOccurred("JDBCFeatureCollection.accepts(max,)", null, "Optimization attempt returned null");
            }

        } else if (visitor instanceof MedianVisitor) {
        	//Optimization is not available (i think)
        	
        	//MedianVisitor medianCalc = (MedianVisitor) visitor;
        	//Object median = median(medianCalc.getExpression());
        	//if (median != null) {
        	// medianCalc.setValue((Comparable) median); // use the optimized result
        	// isOptimized = true;
        	// return;
        	//}
        } else if (visitor instanceof SumVisitor) {
            progress.started();
        	SumVisitor sumCalc = (SumVisitor) visitor;
            Object sum = sum(sumCalc.getExpression());

            if (sum != null) {
            	sumCalc.setValue(sum); // use the optimized result
                isOptimized = true;
                progress.complete();
                return;
            } else {
            	progress.warningOccurred("JDBCFeatureCollection.accepts(sum,)", null, "Optimization attempt returned null");
            }

        } else if (visitor instanceof CountVisitor) {
            progress.started();
        	CountVisitor countCalc = (CountVisitor) visitor;
            Object count = count(null);

            if (count != null) {
            	countCalc.setValue(((Number)count).intValue()); // use the optimized result
                isOptimized = true;
                progress.complete();
                return;
            } else {
            	progress.warningOccurred("JDBCFeatureCollection.accepts(count,)", null, "Optimization attempt returned null");
            }

        } else if (visitor instanceof AverageVisitor) {
            progress.started();
        	//There is no "AVERAGE" function for SQL (to my knowledge),
        	// so we'll do a SUM / COUNT...
        	AverageVisitor averageCalc = (AverageVisitor) visitor;
        	Object sum = sum(averageCalc.getExpression());
        	Object count = count(null);
        	if (sum != null && count != null) {
        		averageCalc.setValue(((Number) count).intValue(), sum);
            	 isOptimized = true;
                 progress.complete();
            	 return;
            } else {
            	progress.warningOccurred("JDBCFeatureCollection.accepts(average,)", null, "Optimization attempt returned null");
            }
        } else if (visitor instanceof UniqueVisitor) {
            progress.started();
        	UniqueVisitor uniqueCalc = (UniqueVisitor) visitor;
            Object unique = unique(uniqueCalc.getExpression());

            if (unique != null) {
            	uniqueCalc.setValue(unique); // use the optimized result
                isOptimized = true;
                progress.complete();
                return;
            } else {
            	progress.warningOccurred("JDBCFeatureCollection.accepts(unique,)", null, "Optimization attempt returned null");
            }

        }
        //optimization was not available, or it failed
        isOptimized = false;
        super.accepts(visitor, progress);
    }

    Connection getConnection() throws IOException {
        return getDataStore().getConnection(featureSource.getTransaction());
    }

    Object min(Expression expression) throws IOException {
    	return aggregate( "MIN", expression );
    }
    Object max(Expression expression) throws IOException {
    	return aggregate( "MAX", expression );
    }
    Object median(Expression expression) throws IOException {
    	return aggregate( "MEDIAN", expression );
    }
    Object sum(Expression expression) throws IOException {
    	return aggregate( "SUM", expression );
    }
    Object count(Expression expression) throws IOException {
    	return aggregate( "COUNT", expression );
    }
    Object average(Expression expression) throws IOException {
    	return aggregate( "AVERAGE", expression );
    }
    Object unique(Expression expression) throws IOException {
    	return aggregate( "DISTINCT", expression );
    }
    
    Object aggregate(String aggregate, Expression expression) throws IOException {
        Filter filter = query.getFilter();

        if (filter == Filter.ALL) {
            return null;
        }

        JDBC1DataStore jdbc = getDataStore();
        SQLBuilder sqlBuilder = jdbc.getSqlBuilder(this.getSchema().getTypeName());

        if (sqlBuilder.getPostQueryFilter(query.getFilter()) != null) {
            // this would require postprocessing the filter
            // so we cannot optimize
            return null;
        }

        Connection conn = null;
        ResultSet results = null;
        Statement statement = null;
        try {
            conn = jdbc.getConnection(featureSource.getTransaction());

            String typeName = getSchema().getTypeName();
            //Filter preFilter = sqlBuilder.getPreQueryFilter(query.getFilter());
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT ");
            sql.append( aggregate );
            sql.append( "(" );

            if (expression == null) {
            	sql.append("*"); //for things like COUNT(*), where a column isn't neccesary.
            } else {
            	//FIXME: add proper handling of expressions
            
	            //extract the column names from the expression
	            //(if we see "featureMembers/*/ATTRIBUTE" change to "ATTRIBUTE")
            	expression.accept(new ExpressionSimplifier());
            
            	//            if (sqlBuilder instanceof DefaultSQLBuilder) {
            	//            	DefaultSQLBuilder builder = (DefaultSQLBuilder) sqlBuilder;
            	//                	builder.encoder.encode(expression);
            	//                	//builder.sqlColumns(sql,)
            	//                sql.append(builder.toString());
            	//            } else {
            	sql.append(expression.toString());
            	//            }
            }
            
            sql.append(") AS rslt");
            sqlBuilder.sqlFrom(sql, typeName);
            sqlBuilder.sqlWhere(sql, filter);

            //LOGGER.finer("SQL: " + sql);
            System.out.println("SQL: " + sql);

            statement = conn.createStatement();
            results = statement.executeQuery(sql.toString());
            if (results == null) return null; //SQL statement failed to execute
            results.next();

            Object answer = results.getObject("rslt");
            results.next();
            if( results.isAfterLast() ){
            	return answer;	
            }
            List list = new ArrayList();
            list.add( answer );
            while( !results.isAfterLast() ){
            	list.add( results.getObject("rslt") );
            	results.next();	
            }
            return list;
        } catch (SQLException sqlException) {
            JDBCUtils.close(conn, featureSource.getTransaction(), sqlException);
            conn = null;
            throw new DataSourceException("Could not calculate " + aggregate + " with "
                + query.getHandle(), sqlException);
        } catch (SQLEncoderException e) {
            // could not encode count
            // but at least we did not break the connection
            return null;
        } finally {
        	try {
	        	if( results != null ) results.close();
	            if( statement != null ) statement.close();
        	}
        	catch (SQLException ignore ){        		
        	}
            JDBCUtils.close(conn, featureSource.getTransaction(), null);
        }
    }
    
    /**
	 * Simplifies a filter expression from featureMember/asterisk/ATTRIBUTE to
	 * ATTRIBUTE.
	 * 
	 * @author Cory Horner, Refractions
	 * 
	 */
    class ExpressionSimplifier extends AbstractFilterVisitor {
        public void visit(AttributeExpression expression) {
            String xpath = expression.getAttributePath();

            if (xpath.startsWith("featureMembers/*/")) {
                xpath = xpath.substring(17);
            } else if (xpath.startsWith("featureMember/*/")) {
                xpath = xpath.substring(16);
            }

            try {
                expression.setAttributePath(xpath);
            } catch (IllegalFilterException e) {
                // ignore
            }
        }
    }
}
