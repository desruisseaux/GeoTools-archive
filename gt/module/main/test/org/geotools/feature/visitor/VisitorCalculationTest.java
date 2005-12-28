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
package org.geotools.feature.visitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geotools.data.DataTestCase;
import org.geotools.data.DataUtilities;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.visitor.MaxVisitor.MaxResult;
import org.geotools.feature.visitor.MedianVisitor.MedianResult;
import org.geotools.feature.visitor.MinVisitor.MinResult;
import org.geotools.feature.visitor.UniqueVisitor.UniqueResult;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.IllegalFilterException;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Purpose: these tests ensure the proper operation of feature visitation, with CalcResult merging too!
 */
public class VisitorCalculationTest extends DataTestCase {
    FeatureCollection fc;
    FeatureType ft;
    FeatureCollection fc2;
    FeatureType ft2;

    public VisitorCalculationTest(String arg0) {
        super(arg0);
    }

    protected void setUp() throws Exception {
        super.setUp();
        fc = DataUtilities.collection(roadFeatures);
        fc2 = DataUtilities.collection(riverFeatures);
        ft = roadType;
        ft2 = riverType;
    }

    // test only the visitor functions themselves, and try the merge operation
    public void testMin() throws IllegalFilterException, IOException {
        //index 0 is the id field, so the data isn't terribly exciting (1,2,3).
    	MinVisitor minVisitor = new MinVisitor(0, ft);
        fc.accepts(minVisitor, null);
    	MinVisitor minVisitor2 = new MinVisitor(0, ft2);
        fc2.accepts(minVisitor2, null);
        //1 is min
        Object result = minVisitor.getResult().getValue();
        int value = ((Integer) result).intValue();
        assertEquals(1, value);
        int value2 = minVisitor.getResult().toInt();
        assertEquals(1, value2);
        //min of 1 and 1 is 1
        CalcResult minResult1 = minVisitor.getResult();
        CalcResult minResult2 = minVisitor2.getResult();
        CalcResult minResult3 = minResult1.merge(minResult2);
        assertEquals(1, minResult3.toInt());
        //test for destruction during merge
        CalcResult minResult4 = new MinResult((Comparable) new Integer(10));
        CalcResult minResult5 = minResult4.merge(minResult1);
        assertEquals(1, minResult5.toInt()); 
        assertEquals(10, minResult4.toInt());
        //test negative result
        CalcResult minResult6 = new MinResult((Comparable) new Integer(-5));
        CalcResult minResult7 = (MinResult) minResult1.merge(minResult6);
        assertEquals(-5, minResult7.toInt()); 
        assertEquals(-5, minResult6.toInt());
        //test a mock optimization
        minVisitor.setValue(new Integer(-50));
        minResult1 = minVisitor.getResult();
        minResult7 = minResult7.merge(minResult1);
        assertEquals(-50, minResult7.toInt());
        //test varying data types
        minVisitor.setValue(new Double(-100.0));
        minResult1 = minVisitor.getResult();
        minResult7 = minResult7.merge(minResult1);
        assertEquals(-100.0, minResult7.toDouble(), 0);
        assertEquals(-100, minResult7.toInt());
    }

    public void testMax() throws IllegalFilterException, IOException {
        //index 0 is the id field, so the data isn't terribly exciting
        MaxVisitor maxVisitor = new MaxVisitor(0, ft);
        fc.accepts(maxVisitor, null); //1,2,3
        MaxVisitor maxVisitor2 = new MaxVisitor(3, ft2);
        fc2.accepts(maxVisitor2, null); //3,4.5
        //3 is max
        int value1 = maxVisitor.getResult().toInt();
        assertEquals(3, value1);
        //4.5 is max
        double value2 = maxVisitor2.getResult().toDouble();
        assertEquals((double) 4.5, value2, 0);
        //max of 3 and 4.5 is 4.5
        CalcResult maxResult1 = (MaxResult) maxVisitor.getResult();
        CalcResult maxResult2 = (MaxResult) maxVisitor2.getResult();
        CalcResult maxResult3 = (MaxResult) maxResult1.merge(maxResult2);
        assertEquals((double) 4.5, maxResult3.toDouble(), 0);
        //test for destruction during merge
        CalcResult maxResult4 = new MaxResult((Comparable) new Double(2));
        CalcResult maxResult5 = (MaxResult) maxResult4.merge(maxResult1);
        assertEquals(3, maxResult5.toDouble(), 0); 
        assertEquals(2, maxResult4.toDouble(), 0);
        //test negative result
        CalcResult maxResult6 = new MaxResult((Comparable) new Integer(-5));
        CalcResult maxResult7 = (MaxResult) maxResult1.merge(maxResult6);
        assertEquals(3, maxResult7.toDouble(), 0); 
        assertEquals(-5, maxResult6.toDouble(), 0);
        //test a mock optimization
        maxVisitor.setValue(new Double(544));
        maxResult1 = maxVisitor.getResult();
        maxResult7 = maxResult7.merge(maxResult1);
        assertEquals(544, maxResult7.toDouble(), 0);
        //test varying data types
        maxVisitor.setValue(new Long(6453));
        maxResult1 = maxVisitor.getResult();
        maxResult7 = maxResult7.merge(maxResult1);
        assertEquals(6453, maxResult7.toDouble(), 0);
        assertEquals(6453, maxResult7.toInt());
    }

    public void testMedian() throws IllegalFilterException, IOException {
        MedianVisitor medianVisitor1 = new MedianVisitor(0, ft);
        fc.accepts(medianVisitor1, null); //1,2,3
        MedianVisitor medianVisitor2 = new MedianVisitor(0, ft2);
        fc2.accepts(medianVisitor2, null); //3,4.5
        //1,2,3 --> 2, 1,2 --> 1.5
        CalcResult medianResult1 = medianVisitor1.getResult();
        CalcResult medianResult2 = medianVisitor2.getResult();
        assertEquals(2, medianResult1.toInt());
        assertEquals(1.5, medianResult2.toDouble(), 0);
        //1,1,2,2,3 --> 2
        CalcResult medianResult3 = medianResult1.merge(medianResult2);
        assertEquals(2, medianResult3.toDouble(), 0);
        //test for destruction during merge
        List vals = new ArrayList();
        vals.add(new Double(2.5)); vals.add(new Double(3.5)); vals.add(new Double(4.5));
        CalcResult medianResult4 = new MedianResult(vals);
        CalcResult medianResult5 = medianResult4.merge(medianResult1);
        assertEquals(2.75, medianResult5.toDouble(), 0); 
        assertEquals(3.5, medianResult4.toDouble(), 0);
        //test a mock optimization
        medianVisitor1.setValue(new Double(544));
        medianResult1 = medianVisitor1.getResult();
        try {
        	medianResult3 = medianResult5.merge(medianResult1);
        	fail(); //merge should fail
        } catch (Exception e) {
            assertEquals("Optimized median results cannot be merged.", e.getMessage());
		}
    }

    public void testSum() throws IllegalFilterException, IOException {
        SumVisitor sumVisitor = new SumVisitor(0, ft);
        fc.accepts(sumVisitor, null); //1,2,3
        SumVisitor sumVisitor2 = new SumVisitor(3, ft2);
        fc2.accepts(sumVisitor2, null); //3,4.5
        //6 is sum
        int value1 = sumVisitor.getResult().toInt();
        assertEquals(6, value1);
        //7.5 is sum
        double value2 = sumVisitor2.getResult().toDouble();
        assertEquals((double) 7.5, value2, 0);
        //sum of 6 and 7.5 is 13.5
        CalcResult sumResult1 = sumVisitor.getResult();
        CalcResult sumResult2 = sumVisitor2.getResult();
        CalcResult sumResult3 = sumResult1.merge(sumResult2);
        assertEquals((double) 13.5, sumResult3.toDouble(), 0);
        //test a mock optimization
        sumVisitor2.setValue(new Integer(-42));
        CalcResult sumResult4 = sumVisitor2.getResult();
        CalcResult sumResult5 = sumResult3.merge(sumResult4);
        assertEquals(-28.5, sumResult5.toDouble(), 0);
        //test for destruction during merge
        assertEquals(13.5, sumResult3.toDouble(), 0);
        assertEquals(-42.0, sumResult4.toDouble(), 0);
    }
    
    public void testCount() throws IllegalFilterException, IOException {
    	CountVisitor countVisitor = new CountVisitor();
        fc.accepts(countVisitor, null);
        CountVisitor countVisitor2 = new CountVisitor();
        fc2.accepts(countVisitor2, null);
        //3 features
        int value1 = countVisitor.getResult().toInt();
        assertEquals(3, value1);
        //2 features
        int value2 = countVisitor2.getResult().toInt();
        assertEquals(2, value2);
        //merge = 5 features
        CalcResult countResult1 = countVisitor.getResult();
        CalcResult countResult2 = countVisitor2.getResult();
        CalcResult countResult3 = countResult1.merge(countResult2);
        assertEquals(5, countResult3.toInt());
        //test a mock optimization
        countVisitor.setValue(20);
        CalcResult countResult4 = countVisitor.getResult();
        assertEquals(20, countResult4.toInt());
        //test for destruction during merge
        CalcResult countResult5 = countResult4.merge(countResult3);
        assertEquals(5, countResult3.toInt());
        assertEquals(20, countResult4.toInt());
        assertEquals(25, countResult5.toInt());
    }

    public void testAverage() throws IllegalFilterException, IOException {
        AverageVisitor averageVisitor = new AverageVisitor(0, ft);
        fc.accepts(averageVisitor, null);  //1,2,3
        AverageVisitor averageVisitor2 = new AverageVisitor(3, ft2);
        fc2.accepts(averageVisitor2, null); //3,4.5
        //2 is average
        int value1 = averageVisitor.getResult().toInt();
        assertEquals(2, value1);
        //3.75 is average
        double value2 = averageVisitor2.getResult().toDouble();
        assertEquals((double) 3.75, value2, 0);
        //average of 1,2,3,3,4.5 is 2.7
        CalcResult averageResult1 = averageVisitor.getResult();
        CalcResult averageResult2 = averageVisitor2.getResult();
        CalcResult averageResult3 = averageResult1.merge(averageResult2);
        assertEquals((double) 2.7, averageResult3.toDouble(), 0);
        //test for destruction during merge
        assertEquals((double) 3.75, averageResult2.toDouble(), 0);
        //test mock optimizations
        averageVisitor2.setValue(5, new Integer(100)); //mergeable optimization
        averageResult2 = averageVisitor2.getResult();
        assertEquals(20, averageResult2.toInt());
        averageResult3 = averageResult1.merge(averageResult2);
        assertEquals((double) 13.25, averageResult3.toDouble(), 0);
        averageVisitor2.setValue(new Double(15.4)); //un-mergeable optimization
        averageResult2 = averageVisitor2.getResult();
        assertEquals((double) 15.4, averageResult2.toDouble(), 0);
        try {
            averageResult3 = averageResult1.merge(averageResult2);
        	fail(); //merge should throw an exception
        } catch (Exception e) {
            assertEquals("Optimized average results cannot be merged.", e.getMessage());
		}
        //throw a monkey in the wrench (combine number classes)
        averageVisitor.setValue(5, new Integer(10));
        averageResult1 = averageVisitor.getResult();
        averageVisitor2.setValue(5, new Double(33.3));
        averageResult2 = averageVisitor2.getResult();
        averageResult3 = averageResult1.merge(averageResult2); //int + double --> double?
        assertEquals((double) 4.33, averageResult3.toDouble(), 0);
    }
    
    public void testUnique() throws IllegalFilterException, IOException {
        UniqueVisitor uniqueVisitor = new UniqueVisitor(0, ft);
        fc.accepts(uniqueVisitor, null);
        UniqueVisitor uniqueVisitor2 = new UniqueVisitor(3, ft2);
        fc2.accepts(uniqueVisitor2, null);
        //1, 2, 3
        Set value1 = uniqueVisitor.getResult().toSet();
        assertEquals(3, value1.size()); //3 items in the set
        //3.0, 4.5
        Object[] value2 = uniqueVisitor2.getResult().toArray();
        assertEquals(2, value2.length); //2 items in the set
        //test a merge 
        CalcResult uniqueResult1 = uniqueVisitor.getResult();
        CalcResult uniqueResult2 = uniqueVisitor2.getResult();
        CalcResult uniqueResult3 = uniqueResult1.merge(uniqueResult2);
        assertEquals(5, uniqueResult3.toSet().size()); //3 and 3.0 are different, so there are actually 5
        //ensure merge was not destructive
        assertEquals(3, uniqueResult1.toSet().size()); 
        //test a merge with duplicate elements
        Set anotherSet = new HashSet();
        anotherSet.add(new Integer(2));
        anotherSet.add(new Integer(4));
        CalcResult uniqueResult4 = new UniqueResult(anotherSet);
        CalcResult uniqueResult5 = uniqueResult1.merge(uniqueResult4); //1,2,3 + 2,4
        assertEquals(4, uniqueResult5.toSet().size());
        //mock optimization
        uniqueVisitor.setValue(anotherSet);
        uniqueResult1 = uniqueVisitor.getResult();
        assertEquals(anotherSet, uniqueResult1.toSet());
        //int + double --> ?
        uniqueResult3 = uniqueResult2.merge(uniqueResult1);
        Object[] array = uniqueResult3.toArray();
        assertEquals(3.0, ((Double)array[0]).doubleValue(), 0);
        assertEquals(2, ((Integer)array[1]).intValue(), 0);
        assertEquals(4, ((Integer)array[2]).intValue(), 0);
        assertEquals(4.5, ((Double)array[3]).doubleValue(), 0);
    }

    public void testBounds() throws IOException {
        BoundsVisitor boundsVisitor1 = new BoundsVisitor();
        fc.accepts(boundsVisitor1, null);
        BoundsVisitor boundsVisitor2 = new BoundsVisitor();
        fc2.accepts(boundsVisitor2, null);
        Envelope env1 = new Envelope(1,5,0,4);
        CalcResult boundsResult1 = boundsVisitor1.getResult();
        assertEquals(env1, boundsResult1.toEnvelope());
        Envelope env2 = new Envelope(4,13,3,10);
        CalcResult boundsResult2 = boundsVisitor2.getResult();
        assertEquals(env2, boundsResult2.toEnvelope());
        CalcResult boundsResult3 = boundsResult2.merge(boundsResult1);
        Envelope env3 = new Envelope(1,13,0,10);
        assertEquals(env3, boundsResult3.toEnvelope());
    }
    
    public void testQuantileList() throws Exception {
        FilterFactory factory = FilterFactoryFinder.createFilterFactory();
        Expression expr = factory.createAttributeExpression(ft,
                ft.getAttributeType(0).getName());
        QuantileListVisitor visitor = new QuantileListVisitor(expr, 2);
        fc.accepts(visitor, null);
        List[] qResult = (List[]) visitor.getResult().getValue();
        assertEquals(2, qResult.length);
        assertEquals(2, qResult[0].size());
        assertEquals(1, qResult[1].size());
    }
    
    //try merging a count and sum to get an average, both count+sum and sum+count 
    public void testCountSumMerge() throws IllegalFilterException, IOException {
        CountVisitor countVisitor = new CountVisitor();
        fc2.accepts(countVisitor, null); //count = 2
        SumVisitor sumVisitor = new SumVisitor(3, ft2);
        fc2.accepts(sumVisitor, null); //sum = 7.5
        CalcResult countResult = countVisitor.getResult();
        CalcResult sumResult = sumVisitor.getResult();
        CalcResult averageResult1 = countResult.merge(sumResult);
        CalcResult averageResult2 = sumResult.merge(countResult);
        //both average results were correct?
        assertEquals((double) 3.75, averageResult1.toDouble(), 0);
        assertEquals((double) 3.75, averageResult2.toDouble(), 0);
        //neither sum nor count was destroyed?
        assertEquals(2, countResult.toInt());
        assertEquals((double) 7.5, sumResult.toDouble(), 0);
    }
    
    //try merging 2 incompatible CalcResults and check for the exception
    public void testBadMerge() throws IllegalFilterException, IOException {
    	//count + max = boom!
    	CountVisitor countVisitor = new CountVisitor();
    	countVisitor.setValue(8);
    	CalcResult countResult = countVisitor.getResult();
    	MaxVisitor maxVisitor = new MaxVisitor(null);
    	maxVisitor.setValue(new Double(99));
    	CalcResult maxResult = maxVisitor.getResult();
    	try {
            CalcResult boomResult = maxResult.merge(countResult);
        	fail(); //merge should throw an exception
        } catch (Exception e) {
            assertEquals("Parameter is not a compatible type", e.getMessage());
		}
    }
}
