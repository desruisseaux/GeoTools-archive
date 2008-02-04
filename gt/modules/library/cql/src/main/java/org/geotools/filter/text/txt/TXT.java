/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.filter.text.txt;

import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.ICompiler;
import org.opengis.filter.FilterFactory;


/**
 * TODO WARNING THIS IS A WORK IN PROGRESS.
 * 
 * TXT Query Language
 * 
 * <p>
 * TXT is an extension of CQL. This class present the operations available to parse the
 * TXT language and generate the associated filter.
 * </p>
 * 
 * 
 * @author Jody Garnett
 * @author Maria Comanescu
 * @author Mauricio Pazos (Axios Engineering)
 * 
 * @since 2.5
 */
class TXT extends CQL{
    
    protected TXT(){
        //
    }
    
    /**
     * New instance of TXTCompiler
     * @param predicate
     * @param filterFactory
     * 
     * @return TXTCompiler
     */
    protected static ICompiler createCompiler(final String predicate, final FilterFactory filterFactory)  {
        assert predicate != null: "predicate cannot be null";
        assert filterFactory != null: "filterFactory cannot be null";
        
        TXTCompiler compiler  = new TXTCompiler(predicate, filterFactory);
        return compiler;
    }
}
