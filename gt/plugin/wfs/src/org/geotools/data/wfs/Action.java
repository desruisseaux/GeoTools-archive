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
/*
 * Created on 16-Sep-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wfs;

import org.geotools.feature.Feature;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import java.util.HashMap;
import java.util.Map;


/**
 * This interface represents pending actions within a transaction.
 *
 * @author dzwiers
 */
public interface Action {
	/**
	 * Action mask for an Insert Action
	 */
    public static final int INSERT = 1;
	/**
	 * Action mask for an Update Action
	 */
    public static final int UPDATE = 2;
	/**
	 * Action mask for a Delete Action
	 */
    public static final int DELETE = 4;

    /**
     * @return The Type of Action ... one of the three Constants
     */
    public int getType();

    /**
     * @return The Filter on which to inflict the Action
     */
    public Filter getFilter();

    /**
     * @return The FeatureType name for which this Action is intended
     */
    public String getTypeName();

    /**
     * Represents an Update Action
     * 
     * @author dzwiers
     */
    public static class UpdateAction implements Action {
        private Filter filter;
        private Map properties;
        private String typeName;

        private UpdateAction() {
            // should not be called
        }

        /**
         * Makes an UpdateAction
         * 
         * @param typeName The TypeName
         * @param f Filter which this update affects
         * @param properties The properties to update
         */
        public UpdateAction(String typeName, Filter f, Map properties) {
            filter = f;
            this.properties = properties;
            this.typeName = typeName;
        }

        /**
         * @return @see Action#UPDATE
         */
        public int getType() {
            return UPDATE;
        }

        /**
         * Returns the property if found ... this method will not create a 
         * NullPointerException if properties is null.
         * 
         * @param name String the property key
         * @return Object The property if found, null other wise. 
         */
        public Object getProperty(String name) {
            return (properties == null) ? null : properties.get(name);
        }

        /**
         * Returns the property names if they exist ... this method will not create a 
         * NullPointerException if properties is null.
         * 
         * @return A list of the keys. 
         */
        public String[] getPropertyNames() {
            return properties==null?new String[0]:(String[]) properties.keySet().toArray(new String[properties.keySet()
                                                                               .size()]);
        }

        /**
         * @return a clone of the properties map, null if it does not exist. 
         */
        public Map getProperties() {
            return properties==null?null:new HashMap(properties);
        }

        /**
         * @return Filter the Filter
         */
        public Filter getFilter() {
            return filter;
        }

        /**
         * @return String the TypeName
         */
        public String getTypeName() {
            return typeName;
        }
    }

    /**
     * Represents a Delete Action for a Transaction
     * 
     * @author dzwiers
     */
    public static class DeleteAction implements Action {
        private Filter filter;
        private String typeName;

        private DeleteAction() {
            // should not be called
        }

        /**
         * Represents a Delete Action
         * 
         * @param typeName TypeName
         * @param f Filter of Features to Delete
         */
        public DeleteAction(String typeName, Filter f) {
            filter = f;
            this.typeName = typeName;
        }

        /**
         * @return @see Action#DELETE
         */
        public int getType() {
            return DELETE;
        }

        /**
         * @return the TypeName
         */
        public String getTypeName() {
            return typeName;
        }

        /**
         * @return the Filter
         */
        public Filter getFilter() {
            return filter;
        }
    }

    /**
     * Represents an Insert Action
     * 
     * @author dzwiers
     */
    public static class InsertAction implements Action {
        private Feature feature;

        private InsertAction() {
            // should not be called
        }

        /**
         * Creates an insert action for the Feature specified
         * @param f Feature to add
         */
        public InsertAction(Feature f) {
            feature = f;
        }

        /**
         * @return @see Action#INSERT
         */
        public int getType() {
            return INSERT;
        }

        /**
         * @return The Feature to add
         */
        public Feature getFeature() {
            return feature;
        }

        /**
         * @see org.geotools.data.wfs.Action#getTypeName()
         */
        public String getTypeName() {
            return (feature == null) ? null
                                     : feature.getFeatureType().getTypeName();
        }

        /**
         * @see org.geotools.data.wfs.Action#getFilter()
         */
        public Filter getFilter() {
            return (feature.getID() == null) ? null
                                             : (FilterFactory.createFilterFactory()
                                                             .createFidFilter(feature
                .getID()));
        }
    }
}
