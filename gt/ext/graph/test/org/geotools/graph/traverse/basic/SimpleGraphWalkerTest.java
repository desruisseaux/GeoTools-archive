package org.geotools.graph.traverse.basic;

import junit.framework.TestCase;

import org.geotools.graph.structure.GraphVisitor;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.geotools.graph.structure.basic.BasicNode;
import org.geotools.graph.traverse.GraphTraversal;

public class SimpleGraphWalkerTest extends TestCase {
  private boolean m_visited;
  
  public SimpleGraphWalkerTest(String name) {
    super(name);
  }
  
  public void test_visit() {
    m_visited = false;
    
    GraphVisitor visitor = new GraphVisitor() {
      public int visit(Graphable component) {
        m_visited = true;
        return(GraphTraversal.CONTINUE);  
      }
    }; 
    
    Node n = new BasicNode();
    n.setVisited(false);
    
    SimpleGraphWalker walker = new SimpleGraphWalker(visitor);
    
    assertTrue(walker.visit(n, null) == GraphTraversal.CONTINUE);
    assertTrue(m_visited);
  }
}