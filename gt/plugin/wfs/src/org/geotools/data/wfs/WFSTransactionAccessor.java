package org.geotools.data.wfs;

import java.util.Iterator;
import java.util.List;

import org.geotools.data.wfs.Action.UpdateAction;
import org.geotools.filter.Filter;
import org.geotools.filter.visitor.ClientTransactionAccessor;

public class WFSTransactionAccessor implements ClientTransactionAccessor {

    private List actions;

	WFSTransactionAccessor(List actions){
        this.actions=actions;
	}
	
    /**
     * Returns all the filters indicating deleted feature anded together.  This is used to tell the server what features
     * to NOT return.
     * 
     * @return all the filters indicating deleted feature anded together. 
     */
	public Filter getDeleteFilter() {
		List l = actions;
		Iterator i = l.iterator();
		Filter deleteFilter=null;
		while(i.hasNext()){
			Action a = (Action)i.next();
			if(a.getType() == Action.DELETE){
				
				if( deleteFilter==null )
					deleteFilter=a.getFilter();
				else
					deleteFilter=deleteFilter.and(a.getFilter());
			}
		}
		return deleteFilter;
	}

	
    /**
     * Returns all the filters of updates that affect the attribute in the expression ored together.
     * 
     * @param attributePath the xpath identifier of the attribute.
     * @return all the filters of updates that affect the attribute in the expression ORed together.
     */
    public Filter getUpdateFilter(String attributePath) {
        Iterator i = actions.iterator();
        Filter updateFilter=null;
        while(i.hasNext()){
        	Action a = (Action)i.next();
        	if(a.getType() == Action.UPDATE){
        		UpdateAction ua = (UpdateAction)a;
        		if(ua.getProperty(attributePath)!=null){
        			if( updateFilter==null )
        				updateFilter=a.getFilter();
        			else
        				updateFilter=updateFilter.and(a.getFilter());
        		}
        	}
        }
		return updateFilter;
	}
	
}
