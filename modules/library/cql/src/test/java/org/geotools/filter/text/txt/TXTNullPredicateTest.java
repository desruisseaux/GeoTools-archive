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

import org.geotools.filter.text.cql2.CQLNullPredicateTest;
import org.geotools.filter.text.cql2.FilterCQLSample;
import org.geotools.filter.text.cql2.CompilerFactory.Language;
import org.junit.Test;
import org.opengis.filter.Filter;


/**
 * Test TXT Null Predicate:
 * <p>
 *
 * <pre>
 * &lt;null predicate &gt; ::=  &lt;expression &gt; IS [ NOT ] NULL
 * </pre>
 *
 * </p>
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.6
 */
public class TXTNullPredicateTest extends CQLNullPredicateTest {
    
    public TXTNullPredicateTest(){
        super(Language.TXT);
    }
    
    /**
     * Sample: centroid( the_geom ) IS NOT NULL
     */
    @Test
    public void functionIsNull() throws Exception {

        final String samplePredicate = FilterTXTSample.FUNCTION_IS_NULL;

        Filter expected = FilterTXTSample.getSample(samplePredicate);
        
        testNullPredicate(samplePredicate, expected);
    }
    
    /**
     * Sample: centroid( the_geom ) IS NOT NULL
     */
    @Test
    public void functionIsNotNull() throws Exception {

        final String samplePredicate = FilterTXTSample.FUNCTION_IS_NOT_NULL;

        Filter expected = FilterTXTSample.getSample(samplePredicate);
        
        testNullPredicate(samplePredicate, expected);
    }

    /**
     * Sample: 3+4 IS NOT NULL
     */
    @Test
    public void mathExprIsNull() throws Exception {

        final String samplePredicate = FilterTXTSample.FUNCTION_IS_NOT_NULL;

        Filter expected = FilterTXTSample.getSample(samplePredicate);
        
        testNullPredicate(samplePredicate, expected);
    }

}
