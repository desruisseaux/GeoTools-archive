/*
 * Created on 16-Sep-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wfs;

import org.geotools.feature.Feature;
import org.geotools.filter.FidFilter;
import org.geotools.filter.FilterFactory;

/**
 * @author dzwiers
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface Action {
	public static final int INSERT = 1;
	public static final int UPDATE = 2;
	public static final int DELETE = 4;
	public int getType();

	public Feature getFeature();
	public Feature getUpdateFeature();
	public String getTypeName();
	public FidFilter getFidFilter();

	public static class ActionFactory{
		private ActionFactory(){}
		private static ActionFactory instance = new ActionFactory();
		public static ActionFactory getInstance(){
			return instance;
		}
		public static Action createAction(Feature old, Feature neW){
			if(old == null)
				return new DefaultAction(INSERT,neW);
			if(neW == null)
				return new DefaultAction(DELETE,old);
			return new DefaultAction(UPDATE,old,neW);
		}
		private static class DefaultAction implements Action{
			private int t;
			private Feature f1,f2;
			private DefaultAction(){}
			public DefaultAction(int type, Feature f){
				t = type;f1=f;
			}
			public DefaultAction(int type, Feature f, Feature nw){
				t = type;f1=f;f2=nw;
			}
			public int getType(){return t;}
			public Feature getFeature(){return f1;}
			public Feature getUpdateFeature(){return f2;}
			public String getTypeName(){return f1.getFeatureType().getTypeName();}
			public FidFilter getFidFilter(){return createFilter(f1);}
		}
		
		public static FidFilter createFilter(Feature f){
			return FilterFactory.createFilterFactory().createFidFilter(f.getID());
		}
	}
}
