package org.geotools.graph.traverse.standard;

import org.geotools.graph.GraphTestUtil;
import org.geotools.graph.build.GraphBuilder;
import org.geotools.graph.build.basic.BasicDirectedGraphBuilder;
import org.geotools.graph.structure.GraphVisitor;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.geotools.graph.traverse.basic.BasicGraphTraversal;
import org.geotools.graph.traverse.basic.DummyGraphWalker;

public class DirectedDepthFirstIteratorTest extends DepthFirstIteratorTest {

  public DirectedDepthFirstIteratorTest(String name) {
    super(name);  
  } 
  
  /**
   * Create a graph with no bifurcations and do a full traversal from 
   * "last" node in graph. <BR>
   * <BR>
   * Expected: 1. Only last node should be visited.
   */
  public void test_7() {
    final Node[] ends = GraphTestUtil.buildNoBifurcations(builder(), 100);
    
    DummyGraphWalker walker = new DummyGraphWalker();
    DirectedDepthFirstIterator iterator = new DirectedDepthFirstIterator();
    BasicGraphTraversal traversal = new BasicGraphTraversal(
      builder().getGraph(), walker, iterator
    );
    traversal.init();
    
    iterator.setSource(ends[1]);
    traversal.traverse();
    
    //ensure only last node visited
    GraphVisitor visitor = new GraphVisitor() {
      public int visit(Graphable component) {
        if (component == ends[1]) assertTrue(component.isVisited()); 
        else assertTrue(!component.isVisited());
        return(0);  
      }
    };
    builder().getGraph().visitNodes(visitor);  
  }
  
  protected DepthFirstIterator createIterator() {
    return(new DirectedDepthFirstIterator());
  }
  
  protected GraphBuilder createBuilder() {
    return(new BasicDirectedGraphBuilder());  
  }
}