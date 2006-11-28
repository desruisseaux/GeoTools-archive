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
package org.geotools.data.jpox;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.geotools.data.DataAccess;
import org.geotools.data.Source;
import org.jpox.PersistenceManagerFactoryImpl;
import org.opengis.feature.type.TypeName;


public class JpoxDataService implements DataAccess {

	private Properties jdoProps;
	
	private boolean initialized = false;
	
	private PersistenceManagerFactoryImpl pmf;
	private PersistenceManager pm;

	private List typesList;
	private Map typesMap;
	
	public JpoxDataService( Properties jdoProps ) {
		this.jdoProps = jdoProps;
	}

	public JpoxDataService( PersistenceManagerFactoryImpl pmf ) {
		initInternal( pmf );
	}
	
	public void initialize() {
		initialize( jdoProps );
	}
	
	public void initialize( Properties jdoProps ) {
		initInternal( (PersistenceManagerFactoryImpl)JDOHelper.getPersistenceManagerFactory( jdoProps ) );
	}
	
	private void initInternal( PersistenceManagerFactoryImpl pmf ) {
		this.pmf = pmf;
		pm = pmf.getPersistenceManager();
		initialized = true;
	}
	
	public Source access( TypeName typeName ) {
		Class pc = (Class)describe( typeName );
		if ( pc == null ) return null;
		
		return new JpoxPojoSource( pm, pc );
	}

	public Object describe( TypeName typeName ) {
		return getTypesMap().get( typeName );
	}

	public List getTypeNames() {
		if ( typesList == null ) {
			typesList = Collections.unmodifiableList( new Vector( getTypesMap().keySet() ) );			
		}

		return typesList;
	}

	private Map getTypesMap() {
		if ( typesMap != null ) return typesMap;
		
		checkInitialized();

		ClassLoader cl = getClass().getClassLoader();
		Class c = null;
		
		Iterator it = pmf.getPMFContext().getMetaDataManager().getClassesWithMetaData().iterator();
		while (it.hasNext()) {
			try {
				c = cl.loadClass((String)it.next());
			} catch (ClassNotFoundException e) {
				//TODO: this is bad. Log!
				e.printStackTrace();
				continue;
			}
			TypeName typeName = new org.geotools.feature.type.TypeName( c.getCanonicalName() );
			typesMap.put( typeName, c );
		}
		return typesMap;
	}

	private void checkInitialized() {
		if ( !initialized ) throw new IllegalStateException( "JpoxDataService not initialized!" );
	}
}
