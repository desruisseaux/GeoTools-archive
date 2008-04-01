package org.geotools.measure;

import junit.framework.TestCase;

/**
 * Uses a bit of number theory to test the range class.
 * 
 * @author Jody Garnett
 */
public class RangeTest extends TestCase {
    
    /** [-1,1] */
    Range<Integer> UNIT = new Range<Integer>( Integer.class, -1, 1 );
    
    /** Anything invalid is considered Empty; ie (1,-1) or (0,0) or (unbounded,unbounded) */
    Range<Integer> EMPTY = new Range<Integer>( Integer.class, 1, -1 );
    
    /** [0,0] */
    Range<Integer> ZERO = new Range<Integer>( Integer.class, 0, 0 );
    
    /** 1, 2, 3, 4, ... positive integers   Z-+ */    
    Range<Integer> POSITIVE = new Range<Integer>( Integer.class, 0, false, null, true );
    /** 0, 1, 2, 3, 4, ...  nonnegative integers    Z-* */    
    Range<Integer> NON_NEGATIVE= new Range<Integer>( Integer.class, 0, true, null, true );
    
    /** -1, -2, -3, -4, ... negative integers   Z-- */    
    Range<Integer> NEGATIVE = new Range<Integer>( Integer.class, null, true, 0, false);

    /** 0, -1, -2, -3, -4, ...  nonpositive integers */
    Range<Integer> NON_POSITIVE = new Range<Integer>( Integer.class, null, true, 0, true);

    /** ..., -2, -1, 0, 1, 2, ...    integers    Z */
    Range<Integer> INTEGERS = new Range<Integer>( Integer.class, null, null );
    
    /** [A,B) */
    Range<String> A = new Range<String>(String.class,"A",true,"B",false);
    
    /** [B,C) */
    Range<String> B = new Range<String>(String.class,"B",true,"C",false);
    
    /**
     * Test internal utility methods to make sure the class is ticking over as excpeted.
     */
    public void testInternals(){
        assertEquals( 0, UNIT.compareMin( -1, true ) );
        assertEquals( -1, UNIT.compareMin( -1, false ) );
        assertEquals( 0, UNIT.compareMax( 1, true) );
        assertEquals( -1, UNIT.compareMax( 1, false) );
    }
    public void testIsEmpty(){
        // easy
        assertTrue( new Range<Integer>( Integer.class, 0, -1 ).isEmpty() );
        assertFalse( new Range<Integer>( Integer.class, 0, 0 ).isEmpty() );
        assertFalse( new Range<Integer>( Integer.class, 0, 1 ).isEmpty() );
        assertFalse( new Range<Integer>( Integer.class, null, 1 ).isEmpty() );
        assertFalse( new Range<Integer>( Integer.class, 0, null ).isEmpty() );
        
        
        // short hand empty
        assertTrue( new Range<Integer>( Integer.class ).isEmpty() );

        // tricky
        assertTrue( "(0,0)", new Range<Integer>( Integer.class, 0, false, 0, false ).isEmpty() );
        assertTrue( "[0,0)", new Range<Integer>( Integer.class, 0, true, 0, false ).isEmpty() );
        assertTrue( "(0,0]",new Range<Integer>( Integer.class, 0, false, 0, true).isEmpty() );
        assertFalse( "[0,0]", new Range<Integer>( Integer.class, 0, true, 0, true).isEmpty() );        

        // conformance
        assertTrue( EMPTY.isEmpty() );
        assertFalse( UNIT.isEmpty() );
        assertFalse( ZERO.isEmpty() );
        assertFalse( POSITIVE.isEmpty() );
        assertFalse( NEGATIVE.isEmpty() );
        assertFalse( A.isEmpty() );
        assertFalse( B.isEmpty() );
        assertFalse( INTEGERS.isEmpty() );
    }
    public void testToString(){
        assertEquals("[-1,1]", UNIT.toString() );
        assertEquals("[0,0]", ZERO.toString() );
        assertEquals("[A,B)",A.toString() );
        assertEquals("[unbounded,unbounded]", INTEGERS.toString() );
        assertEquals("(0,unbounded]", POSITIVE.toString());
        assertEquals("[unbounded,0)", NEGATIVE.toString());
    }
    public void testEquals(){
        assertEquals( new Range( Integer.class ), EMPTY );
        assertEquals( new Range( Integer.class, -1, 1 ), UNIT );
    }
    public void testContains(){
        assertTrue( UNIT.contains( 0 ));
        assertTrue( UNIT.contains( 1 ));
        assertTrue( UNIT.contains( -1 ));
        assertFalse( UNIT.contains( (Integer) null ));
        assertFalse( UNIT.contains( 2 ));
        
        assertFalse( POSITIVE.contains(0));
        assertTrue( POSITIVE.contains(1));
        assertFalse( POSITIVE.contains(-1));

        assertFalse( NEGATIVE.contains(0));
        assertFalse( NEGATIVE.contains(1));
        assertTrue( NEGATIVE.contains(-1));

        assertTrue( A.contains("Ardvark"));
        assertFalse( A.contains("Beaver"));
        
        assertFalse( B.contains("Ardvark"));
        assertTrue( B.contains("Beaver"));
        
        assertFalse( ZERO.contains(1));
        assertTrue( ZERO.contains(0));
    }
    public void testUnion(){
        assertEquals( new Range<Integer>(Integer.class,-1,null), UNIT.union(POSITIVE) );
    }
    public void testIntersects(){
        assertEquals( "(0,1]", new Range<Integer>(Integer.class,0, false, 1, true), UNIT.intersect(POSITIVE) );
        assertEquals( "[-1,0)", new Range<Integer>(Integer.class,-1, true, 0, false), UNIT.intersect(NEGATIVE) );
        assertEquals( EMPTY, POSITIVE.intersect( NEGATIVE ));        
        
        assertEquals( "0+", ZERO, ZERO.intersect( NON_NEGATIVE));
        assertEquals( "0-", ZERO, ZERO.intersect( NON_POSITIVE));
        assertTrue( "positive does not include NEGATIVE so result is empty",
                POSITIVE.intersect( NEGATIVE ).isEmpty() );
        assertTrue( "positive does not include ZERO so result is empty",
                ZERO.intersect( POSITIVE ).isEmpty()); 
        assertTrue( "negative does not include ZERO so result is empty",
                ZERO.intersect( NEGATIVE).isEmpty());  
    }
}
