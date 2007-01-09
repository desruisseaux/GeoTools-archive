package org.geotools.data.postgis;

import java.io.IOException;
import java.util.logging.Logger;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;
import org.opengis.filter.Filter;

public class PostgisDataStore implements DataStore {

    /** The logger for the postgis module. */
    protected static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.postgis");
	
	public void createSchema(FeatureType arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public FeatureReader getFeatureReader(Query arg0, Transaction arg1) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public FeatureSource getFeatureSource(String arg0) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public FeatureWriter getFeatureWriter(String arg0, Transaction arg1) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public FeatureWriter getFeatureWriter(String arg0, Filter arg1, Transaction arg2) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public FeatureWriter getFeatureWriterAppend(String arg0, Transaction arg1) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public LockingManager getLockingManager() {
		// TODO Auto-generated method stub
		return null;
	}

	public FeatureType getSchema(String arg0) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getTypeNames() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public FeatureSource getView(Query arg0) throws IOException, SchemaException {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateSchema(String arg0, FeatureType arg1) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
