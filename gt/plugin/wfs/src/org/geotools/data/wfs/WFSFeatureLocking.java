/*
 * Created on 28-Sep-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wfs;

import java.io.IOException;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureLock;
import org.geotools.data.FeatureLocking;
import org.geotools.data.Query;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;

/**
 * @author dzwiers
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WFSFeatureLocking extends WFSFeatureStore implements
		FeatureLocking {

	protected FeatureLock fl = null;

	public WFSFeatureLocking(WFSDataStore ds, FeatureType ft){
		super(ds,ft);
	}
	
	/* (non-Javadoc)
	 * @see org.geotools.data.FeatureLocking#setFeatureLock(org.geotools.data.FeatureLock)
	 */
	public void setFeatureLock(FeatureLock lock) {
		fl = lock;
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.FeatureLocking#lockFeatures(org.geotools.data.Query)
	 */
	public int lockFeatures(Query query) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.FeatureLocking#lockFeatures(org.geotools.filter.Filter)
	 */
	public int lockFeatures(Filter filter) throws IOException {
		return lockFeatures(new DefaultQuery(ft.getTypeName(),filter));
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.FeatureLocking#lockFeatures()
	 */
	public int lockFeatures() throws IOException {
		return lockFeatures(new DefaultQuery(ft.getTypeName()));
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.FeatureLocking#unLockFeatures()
	 */
	public void unLockFeatures() throws IOException {
		unLockFeatures(new DefaultQuery(ft.getTypeName()));
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.FeatureLocking#unLockFeatures(org.geotools.filter.Filter)
	 */
	public void unLockFeatures(Filter filter) throws IOException {
		unLockFeatures(new DefaultQuery(ft.getTypeName(),filter));
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.FeatureLocking#unLockFeatures(org.geotools.data.Query)
	 */
	public void unLockFeatures(Query query) throws IOException {
		// TODO Auto-generated method stub

	}

}
