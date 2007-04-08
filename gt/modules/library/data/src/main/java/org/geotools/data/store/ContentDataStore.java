/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.store;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.collection.DelegateFeatureReader;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;

import org.opengis.feature.simple.SimpleFeatureFactory;
import org.opengis.feature.simple.SimpleTypeFactory;
import org.opengis.feature.type.TypeName;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.sort.SortBy;

import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Implementation of DataStore based around feature collections.
 * <p>
 * This implementation of DataStore is based around four feature collections
 * implementation that must be provided by the subclass.
 * <ul>
 * <li>FeatureCollection <b>All</b>: represents all content</li>
 * <li>FeatureCollection <b>Sub</b>: filtered content</li>
 * <li>FeatureList <b>Sorted</b>: sorted content (a FeatureList)</li>
 * <li>FeatureCollection <b>Readonly</b> filted content for reading only </li>
 * </ul>
 * These feature collections are all <b>fully capable</b>; that is able to do
 * everything from sorting to reprojection and the various retyping operations.
 * </p>
 * <p>
 * A subclass has the following responsibility / opportunities:
 * <ul>
 * <li>Provide an implementation of "Content" teaching the ContentDataStore
 * <li>Additional methods as needs or interest dictate
 * </ul>
 * This class is locked down to the maximum extent possible; learning from our
 * earlier experience.
 * 
 * @author Jody Garnett, Refractions Research Inc.
 */
public class ContentDataStore implements DataStore {
	
	/**
	 * Map<TypeName,ContentEntry> one for each kind of content served up.
	 */
	final Map entries;
	/**
	 * The driver for the content the data store provides
	 */
	final protected Content content;
	/**
	 * Factory used to create feature types
	 */
	protected SimpleTypeFactory typeFactory;
	/**
	 * Factory used to create features
	 */
	protected SimpleFeatureFactory featureFactory;
	/**
	 * Factory used to create filters
	 */
	protected FilterFactory filterFactory;
	/**
	 * Factory used to create geometries
	 */
	protected GeometryFactory geometryFactory;
	/**
	 * Application namespace uri of the datastore
	 */
	protected String namespaceURI;
	
	public ContentDataStore(Content content) {
		this.content = content;
		this.entries = new HashMap();
	}
	
	public Content getContent() {
		return content;
	}
	
	public void setTypeFactory(SimpleTypeFactory typeFactory) {
		this.typeFactory = typeFactory;
	}
	
	public SimpleTypeFactory getTypeFactory() {
		return typeFactory;
	}
	
	public void setFeatureFactory(SimpleFeatureFactory featureFactory) {
		this.featureFactory = featureFactory;
	}
	
	public SimpleFeatureFactory getFeatureFactory() {
		return featureFactory;
	}
	
	public void setFilterFactory(FilterFactory filterFactory) {
		this.filterFactory = filterFactory;
	}
	
	public FilterFactory getFilterFactory() {
		return filterFactory;
	}
	
	public GeometryFactory getGeometryFactory() {
		return geometryFactory;
	}
	
	public void setGeometryFactory(GeometryFactory geometryFactory) {
		this.geometryFactory = geometryFactory;
	}
	
	public void setNamespaceURI(String namespaceURI) {
		this.namespaceURI = namespaceURI;
	}
	
	public String getNamespaceURI() {
		return namespaceURI;
	}
	
	//
	// Start of DataStore API
	//
	
	public String[] getTypeNames() throws IOException {
		List typeNames = content.getTypeNames();
		String[] names = new String[ typeNames.size() ];
		
		for ( int i = 0; i < typeNames.size(); i++ ) {
			TypeName typeName = (TypeName) typeNames.get( i );
			names[ i ] = typeName.getLocalPart();
		}
		
		return names;
	}

	public void createSchema(FeatureType featureType) throws IOException {
		throw new UnsupportedOperationException("Not yet");
	}
	
	public FeatureType getSchema(String typeName) throws IOException {
		ContentEntry entry = ensureEntry( name( typeName ) );
		
		return entry.getState( Transaction.AUTO_COMMIT ).featureType( typeFactory );
	}

	public FeatureReader getFeatureReader(Query query, Transaction transaction)
		throws IOException {
		FeatureCollection collection = query(query, transaction);
		return new DelegateFeatureReader( collection.getSchema(), collection.features() );
	}

	
	/** Used to strongly type typeName as soon as possible */
	final protected TypeName name(String typeName) {
		return  new org.geotools.feature.type.TypeName( typeName );
	}

	/**
	 * Looks up an entry, throwing an exception if it does not exist.
	 * 
	 * @param name The name of the entry.
	 * 
	 * @return The entry.
	 * 
	 * @throws IOException If hte entry does not exist, or if there was an error
	 * looking it up.
	 */
	final protected ContentEntry ensureEntry( TypeName name ) throws IOException {
		ContentEntry entry = entry( name );
		
		if ( entry == null ) {
			throw new IOException( "Schema '" + name + "' does not exist." );
		}	
		
		return entry;
	}
	
	/**
	 * Looks up an entry.
	 * 
	 * @param The name of the entry.
	 * 
	 * @return The entry, or <code>null</code> if it does not exist.
	 */
	final protected ContentEntry entry(TypeName name) throws IOException {
		ContentEntry entry = null;
		
		//do we already know about the entry
		if ( !entries.containsKey( name ) ) {
			//is this type available?
			List typeNames = content.getTypeNames();
			if ( typeNames.contains( name ) ) {
				//yes, create an entry for it
				synchronized ( this ) {
					if ( !entries.containsKey( name ) ) {
						entry = content.entry( this, name );
						entries.put( name, entry );
					}
				}
				
				entry = (ContentEntry) entries.get( name );
			}
		}
		
		return (ContentEntry) entries.get(name);
	}

	final private FeatureCollection query(Query query, Transaction transaction)
			throws IOException {
		// TypeName typeName = name( query.getTypeName() );
		// ContentEntry entry = (ContentEntry) entries.get( typeName );
		// ContentState state = entry.getState( transaction );

		FeatureSource source = getFeatureSource(query.getTypeName());
		
		//TODO: transaction should be moved up to FeatureSource
		if (source instanceof FeatureStore) {
			((FeatureStore) source).setTransaction(transaction);
		}
		
		return source.getFeatures(query.getFilter());
	}

	public FeatureSource getFeatureSource(String typeName) throws IOException {
		ContentEntry entry = ensureEntry( name( typeName ) );
		if ( entry == null ) {
			throw new IllegalArgumentException( "Feature source '" + typeName + "' does not exist" );
		}
		
		return new ContentFeatureStore( entry );
	}

	public FeatureWriter getFeatureWriter(String typeName, Filter filter,
			Transaction transaction) throws IOException {
		return null;
	}

	public FeatureWriter getFeatureWriterAppend(String typeName,
			Transaction transaction) throws IOException {
		return null;
	}

	public LockingManager getLockingManager() {
		return null;
	}

	public FeatureSource getView(Query query) throws IOException,
			SchemaException {
		return null;
	}

	public void updateSchema(String typeName, FeatureType featureType)
			throws IOException {
	}

	//
	// low level operations
	//
	public FeatureWriter getFeatureWriter(String typeName,
			Transaction transaction) throws IOException {
		// FeatureCollection collection = query( query, transaction );

		return null; // new DelegateFeatureWriter( collection.getSchema(),
						// collection.features() );
	}

}