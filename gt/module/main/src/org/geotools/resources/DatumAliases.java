/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
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
package org.geotools.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.StringTokenizer;

import org.geotools.referencing.Identifier;

/**
 *  A simple class that determines if a datum name is in our list of aliases and
 *  returns the aliases (as identifiers) for a name.
 * 
 * @author rschulz
 * @version $Id:$
 */
public class DatumAliases {
    
    /**
     * A reader for our alias file.
     */
    private BufferedReader aliasTable;
    
    /**
     * 
     * TODO summary sentence for init ...
     * 
     * @return
     */
    private boolean init() {
        try {
            //see if our name is in the file
            URL url = DatumAliases.class.getResource("DatumAliasesTable.txt");
            aliasTable = new BufferedReader(new InputStreamReader( url.openStream() ));
        } catch (IOException e) {
            System.err.println("Can't open file 'DatumAliasesTable.txt'");
            return false;
        }
        return true;
    }
    
    /**
     * TODO summary sentence for inDatumNameAliasList ...
     * 
     * @param name
     * @return
     */
    public boolean inDatumNameAliasList(String name) {
        init();
        String aliasLine;
        do  {
            try {
            	aliasTable.mark(200);
                aliasLine = aliasTable.readLine();
            } catch (IOException e){
                System.err.println("Error reading 'DatumAliasesTable.txt'");
                return false;
            }
            if (aliasLine != null) {
                StringTokenizer st = new StringTokenizer(aliasLine, ",");
                while(st.hasMoreTokens()) {
                    if ((st.nextToken()).equalsIgnoreCase(name)) {
                        return true;
                    }
                }
            } else {
                break;
            }
        } while (aliasLine != null);
        return false;
    }
    
        public Identifier[] getDatumNameAliases(String name) {
        	try {
				aliasTable.reset();
			} catch (IOException e1) {
				init();
			}
	        String aliasLine;
	        do  {
	            try {
	                aliasLine = aliasTable.readLine();
	            } catch (IOException e){
	                System.err.println("Error reading 'DatumAliasesTable.txt'");
	                return null;
	            }
	            if (aliasLine != null) {
	                StringTokenizer st = new StringTokenizer(aliasLine, ",");
	                while(st.hasMoreTokens()) {
	                    if ((st.nextToken()).equalsIgnoreCase(name)) {
	                        //make our aliases
	                        st = new StringTokenizer(aliasLine, ",");
	                        Identifier[] identifiers = new Identifier[st.countTokens()];
	                        for(int i=0; st.hasMoreTokens(); i++) {
	                        	String nextAlias=st.nextToken();
	                        	if(nextAlias.trim().length()>0)
	                        		identifiers[i] = new Identifier(null, nextAlias);
	                        }
	                        return identifiers;
	                    }
	                }
	            } else {
	                break;
	            }
	        } while (aliasLine != null);
	        return null;
	    }
}
