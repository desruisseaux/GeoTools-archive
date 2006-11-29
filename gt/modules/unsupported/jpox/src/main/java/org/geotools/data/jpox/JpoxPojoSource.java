package org.geotools.data.jpox;

import java.util.Collection;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.geotools.data.Source;
import org.jpox.PersistenceManagerFactoryImpl;
import org.opengis.filter.Filter;
import org.opengis.filter.capability.FilterCapabilities;
import org.opengis.feature.type.TypeName;


public class JpoxPojoSource implements Source {

	public static final String JPOX_STATE_KEY = "JPOX";
	private PersistenceManagerFactoryImpl pmf;
	private PersistenceManager pm;
	private Class pc;
	private org.geotools.data.Transaction t;
	
	public JpoxPojoSource( PersistenceManagerFactoryImpl pmf, Class pc ) {
		this.pmf = pmf;
		this.pc = pc;
	}

	protected Transaction t() {
		return getPm().currentTransaction();
	}
	
	protected PersistenceManager getPm() {
		if ( t == null || t == org.geotools.data.Transaction.AUTO_COMMIT ) {
			if ( pm == null ) {
				pm = pmf.getPersistenceManager();
			}
			return pm;
		} 
		
		JpoxTransactionState state = (JpoxTransactionState)t.getState( JPOX_STATE_KEY );
		
		if ( state == null ) {
			state = new JpoxTransactionState( pmf.getPersistenceManager() );
			t.putState( JPOX_STATE_KEY, state );
		}
		
		return state.getPm();
	}

	public Collection content() {
		return content( "", Query.JDOQL );
	}
	
	public Collection content( String query, String queryLanguage ) {
		
		boolean isActive = t().isActive();
		
		if ( !isActive ) t().begin();
		
		Query q = null;
		if ( query == null || query.equals( "" ) ) {
			q = t().getPersistenceManager().newQuery();			
		} else {
			q = t().getPersistenceManager().newQuery( queryLanguage, query  );
		}
		q.setClass( pc );

		return (Collection)q.execute();
	}

	public Collection content( Filter filter ) {
        t().getPersistenceManager();
		return null;
	}

	public Object describe() {
		return pc;
	}

	public FilterCapabilities getFilterCapabilities() {
		return null;
	}

	public TypeName getName() {
		return new org.geotools.feature.type.TypeName( pc.getCanonicalName() );
	}
	
	public void setTransaction( org.geotools.data.Transaction t ) {
		this.t = t;
	}
    public void dispose() {
        if( t != null ){
            JpoxTransactionState state = (JpoxTransactionState)t.getState( JPOX_STATE_KEY );
            if( state != null ){
                state.setTransaction( null ); // cleanup!                
                state = null;
                t.putState( JPOX_STATE_KEY, null );
            }
            t = null;
        }
        pm.close();
        pm = null;
        pc = null;
    }
}
