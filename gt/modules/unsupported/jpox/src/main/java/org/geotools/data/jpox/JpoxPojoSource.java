package org.geotools.data.jpox;

import java.util.Collection;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.geotools.data.Source;
import org.opengis.filter.Filter;
import org.opengis.filter.capability.FilterCapabilities;
import org.opengis.util.TypeName;


public class JpoxPojoSource implements Source {

	public static final String JPOX_STATE_KEY = "JPOX";
	private PersistenceManager pm;
	private Class pc;
	private org.geotools.data.Transaction t;
	
	public JpoxPojoSource( PersistenceManager pm, Class pc ) {
		this.pm = pm;
		this.pc = pc;
	}

	public Collection content() {
		return content( "", Query.JDOQL );
	}

	protected Transaction t() {
		if ( t == org.geotools.data.Transaction.AUTO_COMMIT ) {
			return pm.currentTransaction();
		} 

		JpoxTransactionState state = (JpoxTransactionState)t.getState( JPOX_STATE_KEY );
		
		if ( state == null ) {
			state = new JpoxTransactionState( pm.getPersistenceManagerFactory().getPersistenceManager() );
            t.putState( JPOX_STATE_KEY, state );
		}
		
		return state.getJpoxTransaction();
	}
	
	public Collection content( String query, String queryLanguage ) {
		
		boolean isActive = t().isActive();
		
		if ( !isActive ) t().begin();
		
		Query q = t().getPersistenceManager().newQuery( query, queryLanguage );
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
		return null;
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
