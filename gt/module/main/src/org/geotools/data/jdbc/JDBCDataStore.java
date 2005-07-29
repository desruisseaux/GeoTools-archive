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
package org.geotools.data.jdbc;

import org.geotools.catalog.CatalogEntry;
import org.geotools.catalog.QueryRequest;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTypeEntry;
import org.geotools.data.EmptyFeatureReader;
import org.geotools.data.FeatureListenerManager;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FilteringFeatureReader;
import org.geotools.data.FilteringFeatureWriter;
import org.geotools.data.InProcessLockingManager;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.ReTypeFeatureReader;
import org.geotools.data.SchemaNotFoundException;
import org.geotools.data.Transaction;
import org.geotools.data.TypeEntry;
import org.geotools.data.jdbc.attributeio.AttributeIO;
import org.geotools.data.jdbc.attributeio.BasicAttributeIO;
import org.geotools.data.jdbc.fidmapper.DefaultFIDMapperFactory;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.data.jdbc.fidmapper.FIDMapperFactory;
import org.geotools.data.view.DefaultView;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.SchemaException;
import org.geotools.filter.Filter;
import org.geotools.filter.SQLEncoder;
import org.geotools.filter.SQLEncoderException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Abstract class for JDBC (level2) based DataStore implementations.
 * This a convenience class that just extends JDBC2DataStore to keep
 * current datastores that use it happy.
 * Eventually datastores should extend one of JDBC1DataStore, or 
 * JDBC2DataStore.
 * <p>
 * This class provides a default implementation of a JDBC data store. Support
 * for vendor specific JDBC data stores can be easily added to Geotools by
 * subclassing this class and overriding the hooks provided.
 * </p>
 * 
 * <p>
 * At a minimum subclasses should implement the following methods:
 * 
 * <ul>
 * <li>
 * {@link #buildAttributeType(ResultSet) buildAttributeType(ResultSet)} - This
 * should be overriden to construct an attribute type that represents any
 * column types not supported by the default implementation, such as geometry
 * columns.
 * </li>
 * <li>
 * {@link #getGeometryAttributeIO(AttributeType, QueryData)
 * getGeometryAttributeIO(AttributeType, QueryData)}  - Should be overriden to
 * provide a way to read/write geometries into the format of the database
 * </li>
 * </ul>
 * </p>
 * 
 * <p>
 * Additionally subclasses can optionally override the following:
 * 
 * <ul>
 * <li>
 * Use a specific FIDMapperFactory by overriding the {@link
 * #buildFIDMapperFactory(JDBCDataStoreConfig)
 * buildFIDMapperFactory(JDBCDataStoreConfig)} method, and eventually disallow
 * user overrides by throwing an {@link
 * java.lang.UnsupportedOperationException UnsupportedOperationException} in
 * the {@link #setFIDMapperFactory(FIDMapperFactory) setFidMapperFactory()}
 * method.
 * </li>
 * <li>
 * {@link #allowTable(String) allowTable} - Used to determine whether a table
 * name should be exposed as a feature type.
 * </li>
 * <li>
 * {@link #determineSRID(String,String) determineSRID} - Used to determine the
 * SpatialReference ID of a geometry column in a table.
 * </li>
 * <li>
 * {@link #buildSQLQuery(String,AttributeType[],Filter,boolean)
 * buildSQLQuery()} - Sub classes can override this to build a custom SQL
 * query.
 * </li>
 * <li>
 * {@link #getResultSetType(boolean) getResultSetType} if the standard result
 * set type is not satisfactory/does not work with a normal FORWARD_ONLY
 * resultset type
 * </li>
 * <li>
 * {@link #getConcurrency(boolean) getConcurrency} to set the level of
 * concurrency for the result set used to read/write the database
 * </li>
 * </ul>
 * </p>
 * 
 * <p>
 * Additionally subclasses may want to set the value of:
 * 
 * <ul>
 * <li>
 * sqlNameEscape - character (String) to surround names of SQL objects to
 * support mixed-case and non-English names.
 * </li>
 * </ul>
 * </p>
 *
 * @author Amr Alam, Refractions Research
 * @author Sean  Geoghegan, Defence Science and Technology Organisation
 * @author Chris Holmes, TOPP
 * @author Andrea Aime $Id: JDBCDataStore.java,v 1.23.2.6 2004/05/09 15:15:42
 *         aaime Exp $
 */
public abstract class JDBCDataStore extends JDBC2DataStore {
	protected JDBCDataStore( ConnectionPool pool, JDBCDataStoreConfig config ) throws IOException {
		super( pool, config );		
	}
}
