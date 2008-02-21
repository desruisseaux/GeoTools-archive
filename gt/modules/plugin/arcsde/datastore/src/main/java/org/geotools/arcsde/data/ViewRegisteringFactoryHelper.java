package org.geotools.arcsde.data;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.Union;

import org.geotools.data.DataSourceException;

/**
 * Utility class to help DataStoreFactories for
 * {@linkplain org.geotools.data.sql.SqlDataStore}s register the views provided
 * in a <code>java.util.Map</code> in the call to the factory's
 * <code>createDataStore(Map)</code> method.
 * <p>
 * <b>NOTE</b> this class is a rough copy of the one in the sql-datastore
 * unsupported module. We are incorporating it here as don't want to depend on
 * sql-datastore. Thus, it's expected to be replaced by the original once we
 * work out what to do with the sql-datastore module.
 * </p>
 * <p>
 * Due to the non hierarchical nature of a Map, it is no so easy to provide a
 * variable number of arguments on it for the same keyword, since they're
 * usually read from a .properties file.
 * <p>
 * </p>
 * This class helps to minimize the work needed to provide such a mapping of
 * various SQL views to an in-process feature type by defining the following
 * structure for a Map&lt;String,String&gt; passed to createDataStore. Example
 * .properties file:
 * 
 * <pre><code>
 *      dbtype=...
 *      &lt;usual datastore's parameters&gt;...
 *      sqlView.1.typeName = ViewType1
 *      sqlView.1.sqlQuery = select gid, the_geom, table2.someField \
 *                           from table1, table2 \
 *                           where table1.gid = table2.table1_id
 *     
 *      sqlView.2.typeName = ViewType2
 *      sqlView.2.sqlQuery = select ...
 * </code></pre>
 * 
 * This way, this class' utility method
 * {@linkplain #registerSqlViews(SqlDataStore, Map)} will receive a
 * {@linkplain org.geotools.data.sql.SqlDataStore} and the Map of datastore
 * factory parameters and call
 * {@linkplain org.geotools.data.sql.SqlDataStore#registerView(String, String)}
 * for each pair of <code>sqlView.N.typeName, sqlView.N.sqlQuery</code>
 * </p>
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: ViewRegisteringFactoryHelper.java 28170 2007-11-30 01:31:09Z
 *          groldan $
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java/org/geotools/arcsde/data/ViewRegisteringFactoryHelper.java $
 */
public class ViewRegisteringFactoryHelper {
    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(ViewRegisteringFactoryHelper.class.getPackage().getName());

    private ViewRegisteringFactoryHelper() {
        // no-op
    }

    /**
     * Registers the sql views provided in <code>params</code> on the
     * SqlDataStore <code>dataStore</code>
     * 
     * @param dataStore
     * @param params
     * @throws IOException
     */
    public static void registerSqlViews(ArcSDEDataStore dataStore, Map params) throws IOException {
        Map cleanedUp = cleanUpViewDefinitions(params);
        for (Iterator it = cleanedUp.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String typeName = (String) entry.getKey();
            String sqlQuery = (String) entry.getValue();

            LOGGER.finer("registering view " + typeName);
            LOGGER.finest("sql query is '" + sqlQuery + "'");

            PlainSelect selectBody = parseSqlQuery(sqlQuery);
            dataStore.registerView(typeName, (PlainSelect) selectBody);
        }
    }

    /**
     * Looks up the set of "sqlView.N.typeName" and "sqlView.N.sqlQuery" keys in
     * <code>params</code> and returns a cleaned up map of typeName/query.
     * 
     * @param params
     * @return
     */
    public static Map cleanUpViewDefinitions(Map params) {
        Map cleanedUpViews = new HashMap();
        for (Iterator it = params.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            if (!key.startsWith("sqlView.")) {
                continue;
            }
            if (!key.endsWith(".typeName")) {
                continue;
            }

            String typeName = (String) params.get(key);

            String viewId = key.substring("sqlView.".length(), key.indexOf(".typeName"));

            String queryKey = "sqlView." + viewId + ".sqlQuery";

            String query = (String) params.get(queryKey);
            if (query == null) {
                throw new IllegalArgumentException(
                        "No SQL query definition provided for type name " + typeName);
            }
            cleanedUpViews.put(typeName, query);
        }
        return cleanedUpViews;
    }

    public static PlainSelect parseSqlQuery(String selectStatement) throws IOException {
        CCJSqlParserManager pm = new CCJSqlParserManager();
        Reader reader = new StringReader(selectStatement);
        Statement statement;
        try {
            statement = pm.parse(reader);
        } catch (Exception e) {
            throw new DataSourceException("parsing select statement: " + e.getCause().getMessage(),
                    e);
        }
        if (!(statement instanceof Select)) { // either PlainSelect or Union
            throw new IllegalArgumentException("expected select or union statement: " + statement);
        }
        SelectBody selectBody = ((Select) statement).getSelectBody();
        if (selectBody instanceof Union) {
            // dataStore.registerView(typeName, (Union) selectBody);
            throw new UnsupportedOperationException(
                    "ArcSDEDataStore does not supports registering Union queries");
        } else if (selectBody instanceof PlainSelect) {
            return (PlainSelect) selectBody;
        } else {
            throw new IllegalStateException(selectBody.getClass().getName());
        }
    }
}
