/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005 Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.geotools.referencing.Identifier;

/**
 * A simple class that determines if a datum name is in our list of aliases and
 * returns the aliases (as identifiers) for a name. This is backed by the 
 * text file DatumAliasesTable.txt.
 * 
 * @TODO Could make this a WeakHashMap. Could change the Map values to an array
 *       of aliase Identifiers: would eliminate linear search in getAliases().
 *
 * @author Rueben Schulz
 * @version $Id$
 */
public class DatumAliases {
       
    /**
     * A Map of our datum aliases. Keys (alias names) with the same value 
     * (an Integer) are related.
     */
    private Map aliasMap;
    
    /**
     * Read the DatumAliasesTable.txt file into aliasMap.
     *
     * @return <code>True</code> if file successfully loaded.
     */
    private boolean init() {
        try {
            String aliasLine, nextAlias;
            
            URL url = DatumAliases.class.getResource("DatumAliasesTable.txt");
            BufferedReader aliasTable = new BufferedReader(new InputStreamReader( url.openStream() ));
            aliasMap = new HashMap();
            
            for (int i = 0; (aliasLine = aliasTable.readLine()) != null; i++) {
                StringTokenizer st = new StringTokenizer(aliasLine, ",");
                while(st.hasMoreTokens()) {
                    nextAlias=st.nextToken();
                    if(nextAlias.trim().length()>0) {
                        aliasMap.put(nextAlias, new Integer(i));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading file 'DatumAliasesTable.txt': " + e);
            return false;
        }
        return true;
    }
    
    /**
     * Check if our name is in the set of datum aliases.
     * 
     * @param name Datum alias name to lookup.
     * @return <code>True</code> if name is in the datum aliases table
     */
    public boolean containsAlias(String name) {
        if(aliasMap == null) {
            init();
        }
        return(aliasMap.containsKey(name));
    }
    
    /**
     * Returns the aliases, as an Identifiers[] array, for the
     * given name.
     *
     * @param name Datum alias name to lookup.
     * @return An Identifier[] array of datum aliases for the given name or 
     *         null if the name is not in our list of aliases.
     */
    public Identifier[] getAliases(String name) {
        if(aliasMap == null) {
            init();
        }
        
        //get the value for this alias
        Integer aliasValue = (Integer) aliasMap.get(name);
        if (aliasValue == null) { 
            return null;
        }

        //linear search for related aliases (have the same Interger value)
        List identifierList = new ArrayList();
        for (Iterator i=aliasMap.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry) i.next();
            if (e.getValue().equals(aliasValue)) {
                identifierList.add(new Identifier(null, (String) e.getKey()));
            }
            
        }
        
        //is there a better way to typecast a java array?
        Identifier[] identifiers = new Identifier[identifierList.size()];
        System.arraycopy(identifierList.toArray(),0,identifiers,0,identifierList.size());
        return identifiers;
    }
}
