/*
 * Created on 16-Sep-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wfs;

import java.util.HashMap;
import java.util.Map;

import org.geotools.feature.Feature;
import org.geotools.filter.Filter;
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

//	public String getTypeName();
	public Filter getFilter();

	public static class UpdateAction implements Action{
		private Filter filter;
		private Map properties;
		private String typeName;
		private UpdateAction(){}
		public UpdateAction(String typeName, Filter f, Map properties){
			filter = f;
			this.properties = properties;
			this.typeName = typeName;
		}
		public int getType(){return UPDATE;}
		public Object getProperty(String name){return properties==null?null:properties.get(name);}
		public String[] getPropertyNames(){return (String[])properties.keySet().toArray(new String[properties.keySet().size()]);}
		public Map getProperties(){return new HashMap(properties);}
		public String getTypeName(){return typeName;}
		public Filter getFilter(){return filter;}
	}

	public static class DeleteAction implements Action{
		private Filter filter;
		private String typeName;
		private DeleteAction(){}
		public DeleteAction(String typeName, Filter f){
			filter=f;
			this.typeName = typeName;
		}
		public int getType(){return DELETE;}
		public String getTypeName(){return typeName;}
		public Filter getFilter(){return filter;}
	}

	public static class InsertAction implements Action{
		private Feature feature;
		private InsertAction(){}
		public InsertAction(Feature f){
			feature = f;
		}
		public int getType(){return INSERT;}
		public Feature getFeature(){return feature;}
		public String getTypeName(){return feature==null?null:feature.getFeatureType().getTypeName();}
		public Filter getFilter(){return feature.getID()==null?null:(FilterFactory.createFilterFactory().createFidFilter(feature.getID()));}
	}
}
