/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

import org.geotools.filter.text.cql2.CQLTemporalPredicateTest;
import org.geotools.filter.text.cql2.CompilerFactory;

/**
 * TXT Temporal predicate
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.5
 */
public class TXTTemporalPredicateTest extends CQLTemporalPredicateTest {

    public TXTTemporalPredicateTest() {
        // sets the TXT language used to execute this test case
        super(CompilerFactory.Language.TXT);
    }
    
    
}
