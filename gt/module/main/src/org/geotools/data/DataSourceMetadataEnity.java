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
package org.geotools.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.geotools.util.InternationalString;
import org.opengis.catalog.MetadataEntity;

// **********************************************************
// DONT COPY THIS AS AN EXAMPLE METADATAENTITY IMPLEMENTATION
// **********************************************************
// Use DataFactoryMetadataEntity as an example, it makes
// use of the the correct superclass that does a lot of the
// grunt work.
//
// This class does everything by hand, as an exercise in
// checking the MetadataEntity javadocs. When the javadocs are
// "good enough" this class will make use of
// DefaultMetadataEntity just like it is supposed to.
//
/**
 * DataSourceMetadataEntity describes both DataStore and GridCoverageExchange.
 * <p>
 * The description is in very simple terms:
 * <ul>
 * <li>host: often "localhost" or "host:port"</li>
 * <li>displayName: often a filename, or database name</li>
 * </ul>
 * </p>
 * <p>
 * Note we can not include a ID or namespace at this level, these constrcuts
 * are often captured by the Registry or the application (like GeoServer)
 * containing the DataStore. 
 * </p>
 * @author Jody Garnett, Refractions Research
 */
public class DataSourceMetadataEnity implements MetadataEntity {
    private static final EntityType TYPE = new Type();
    
    /** Host or Server providing the information */
    private String host;
    
    /** Name suitable for display to end user's */
    private InternationalString name;
    
    /** Describe the nature of the datasource constructed by this factory */
    private InternationalString description;
        
    public DataSourceMetadataEnity( File directory, String description ){
        host = directory.getParent();
        name = new InternationalString( directory.getName() );
        this.description = new InternationalString( description );
    }
    public DataSourceMetadataEnity( String host, String name, String description ){
        this.host = host;
        this.name = new InternationalString( name );
        this.description = new InternationalString( description );
    }
    
    /**
     * @return Returns the description.
     */
    public InternationalString getDescription() {
        return description;
    }
    /**
     * @param description The description to set.
     */
    public void setDescription( InternationalString description ) {
        this.description = description;
    }
    /**
     * @return Returns the host.
     */
    public String getHost() {
        return host;
    }
    /**
     * @param host The host to set.
     */
    public void setHost( String host ) {
        this.host = host;
    }
    /**
     * @return Returns the name.
     */
    public InternationalString getName() {
        return name;
    }
    /**
     * @param name The name to set.
     */
    public void setName( InternationalString name ) {
        this.name = name;
    }
    /* (non-Javadoc)
     * @see org.opengis.catalog.MetadataEntity#elements()
     */
    public List elements() {
        List elements = new ArrayList();
        elements.add( host );
        elements.add( name );
        elements.add( description );
        return Collections.unmodifiableList( elements ); 
    }    
    public Object getElement( String xpath ) {
         Object result = TYPE.getElement( xpath );
         if( result == null ){
             return null;
         }
         else if (result instanceof Element ){
             return getElement( (Element) result );
         }
         else if (result instanceof List ){
             List list = (List) result;
             List values = new ArrayList( list.size() );
             for( Iterator i=list.iterator(); i.hasNext(); ){
                 Element element = (Element) i.next();
                 values.add( getElement( element ));
             }
             return values;
         }
         else {
             return null;
         }
    }
    public Object getElement( Element element ) {
        if( element == null)return null;
        else if(element ==  HOST ) return host;
        else if(element ==  NAME) return name;
        else if(element ==  DESCRIPTION) return description;
        else return null;                
    }
    public EntityType getEntityType() {
        return TYPE;
    }
    static final Element HOST = new Element(){
        public Class getType() {
            return String.class;
        }
        public String getName() {
            return "host";
        }
        public boolean isNillable() {
            return true;
        }
        public boolean isMetadataEntity() {
            return false;
        }
        public EntityType getEntityType() {
            return null;
        }        
    };
    static Element NAME = new Element(){
        public Class getType() {
            return InternationalString.class;
        }
        public String getName() {
            return "name";
        }
        public boolean isNillable() {
            return false;
        }
        public boolean isMetadataEntity() {
            return false;
        }
        public EntityType getEntityType() {
            return null;
        }        
    };
    static Element DESCRIPTION = new Element(){
        public Class getType() {
            return InternationalString.class;
        }
        public String getName() {
            return "description";
        }
        public boolean isNillable() {
            return false;
        }
        public boolean isMetadataEntity() {
            return false;
        }
        public EntityType getEntityType() {
            return null;
        }
    };
    static final class Type implements EntityType {
        static final List ELEMENTS;
        static {
            List build = new ArrayList( 3 );
            build.add( HOST );
            build.add( NAME );
            build.add( DESCRIPTION );
            ELEMENTS = Collections.unmodifiableList( build );
        }
        public Class getType() {
            return DataSourceMetadataEnity.class;
        }
        public String getName() {
            return null;            
        }
        public boolean isNillable() {
            return false;
        }        
        public Object getElement( String  xpath ) {
            if( xpath == null ){
                return null;
            }            
            if( NAME.getName().equals( xpath )){
                return NAME;
            }
            else if( HOST.getName().equals( xpath )){
                return HOST;
            }
            else if( DESCRIPTION.getName().equals( xpath )){
                return DESCRIPTION;
            }            
            else {
                if( xpath.indexOf("/") != -1 ){
                    return null; // no nested content
                }
                Pattern pattern = Pattern.compile( xpath );
                List list = new ArrayList(2);
                if( pattern.matcher( NAME.getName() ).matches() ) list.add( NAME );
                if( pattern.matcher( HOST.getName() ).matches() ) list.add( HOST );
                if( pattern.matcher( DESCRIPTION.getName() ).matches() ) list.add( DESCRIPTION );
                switch( list.size() ){
                case 0:
                    return null;
                case 1:
                    return list.get(0);
                default:
                    return list;
                }                
            }
        }
        public List getElements() {
            return ELEMENTS;
        }
    }    
}