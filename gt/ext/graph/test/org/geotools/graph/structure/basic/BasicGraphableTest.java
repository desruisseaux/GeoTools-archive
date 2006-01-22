package org.geotools.graph.structure.basic;

import java.util.Iterator;

import junit.framework.TestCase;

public class BasicGraphableTest extends TestCase {

  private BasicGraphable m_graphable;
  
  public BasicGraphableTest(String name) {
    super(name);
  }
	
  protected void setUp() throws Exception {
    super.setUp();
  
    m_graphable = new BasicGraphable() {
      public Iterator getRelated() {
        return null;
      }
    };
  }
  
  /**
   * Test BasicGraphable#setVisited(boolean) method. <BR>
   * <BR>
   * Test: Clear visited flag, then reset.<BR>
   * Expected: Visited flag should be set.
   *
   */
  public void test_setVisited() {
    assertTrue(!m_graphable.isVisited());
    m_graphable.setVisited(true);
    assertTrue(m_graphable.isVisited());  
  }
  
  /**
   * Test BasicGraphable#setObject(Object). <BR>
   * <BR>
   * Test: Create a new object and set underlying object.
   * Expected: Underlying object should be equal to created object.
   *
   */
  public void test_setObject() {
    Object obj = new Integer(1);
    
    assertTrue(m_graphable.getObject() == null);	
    m_graphable.setObject(obj);
    
    assertTrue(m_graphable.getObject() == obj);
  }
  
  /**
   * Test BasicGraphable#setCount(int). <BR>
   * <BR>
   * Test: Change value of counter.<BR>
   * Expected: Counter equal to new value.
   *
   */
  public void test_setCount() {
    assertEquals(m_graphable.getCount(), -1);
    m_graphable.setCount(10);
    
    assertEquals(m_graphable.getCount(), 10);	
  }
  
  /**
   * Test BasicGraphable#setID(int). <BR>
   * <BR>
   * Test: Change id.<BR>
   * Expected: Id equal to new value. 
   *
   */
  public void test_setID() {
    m_graphable.setID(10);
    assertEquals(m_graphable.getID(), 10);	
  }
}