package org.geotools.graph.util;

import java.util.List;
import java.util.Map;

import org.geotools.graph.GraphTestUtil;
import org.geotools.graph.build.GraphBuilder;
import org.geotools.graph.build.basic.BasicGraphBuilder;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.GraphVisitor;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.geotools.graph.util.graph.GraphPartitioner;

import junit.framework.TestCase;

public class GraphPartitionerTest extends TestCase {
 
  private GraphBuilder m_builder;
  
  public GraphPartitionerTest(String name) {
    super(name);  
  }
 
  protected void setUp() throws Exception {
    super.setUp();
    
    m_builder = createBuilder();
  }
  
  /**
   * Create a graph in which every node is connected and partition.
   * 
   * Expected: 1. There should only be one partition.
   */
  public void test_0() {
    int nnodes = 100;
    GraphTestUtil.buildNoBifurcations(builder(), nnodes);
    
    GraphPartitioner partitioner = new GraphPartitioner(builder().getGraph());
    partitioner.partition();
    
    List partitions = partitioner.getPartitions();
    
    assertTrue(partitions.size() == 1);
    
    //ensure every node in the original graph is in the new graph
    final Graph g = (Graph)partitions.get(0);
    assertTrue(g.getNodes().size() == builder().getGraph().getNodes().size());
    assertTrue(g.getEdges().size() == builder().getGraph().getEdges().size());
    
    GraphVisitor visitor = new GraphVisitor() {
      public int visit(Graphable component) {
        assertTrue(g.getNodes().contains(component));
        return 0;
      }
    };
    builder().getGraph().visitNodes(visitor);
  }
  
  /**
   * Create a balanced binary tree and then remove the root node and partition.
   * <BR>
   * <BR>
   * Expected: 1. Two graphs should be created. One for each subtree of 
   *              original.
   * 
   */
  public void test_1() {
    int k = 4;
    Object[] obj = GraphTestUtil.buildPerfectBinaryTree(builder(), k);
    Node root = (Node)obj[0];
    Map id2node = (Map)obj[1];
    
    Node lc = (Node)id2node.get("0.0");
    Node rc = (Node)id2node.get("0.1");
    
    builder().removeNode(root);
    
    GraphPartitioner parter = new GraphPartitioner(builder().getGraph());
    parter.partition();
    
    List partitions = parter.getPartitions();
    
    assertTrue(partitions.size() == 2);
    
    Graph left = (Graph)partitions.get(0);
    Graph right = (Graph)partitions.get(1);
    
    if (!left.getNodes().contains(lc)) {
      //swap
      left = (Graph)partitions.get(1);
      right = (Graph)partitions.get(0);
    }
    
    assertTrue(left.getNodes().contains(lc));
    assertTrue(right.getNodes().contains(rc));
    
    assertTrue(left.getNodes().size() == Math.pow(2, k)-1);
    assertTrue(left.getEdges().size() == Math.pow(2, k)-2);
    assertTrue(right.getNodes().size() == Math.pow(2, k)-1);
    assertTrue(right.getEdges().size() == Math.pow(2, k)-2);
    
    GraphVisitor visitor = new GraphVisitor() {
      public int visit(Graphable component) {
        assertTrue(component.getObject().toString().startsWith("0.0"));
        return 0;
      }
    };
    left.visitNodes(visitor);
    
    visitor = new GraphVisitor() {
      public int visit(Graphable component) {
        assertTrue(component.getObject().toString().startsWith("0.1"));
        return 0;
      }
    };
    right.visitNodes(visitor);
  }
  
  protected GraphBuilder createBuilder() {
    return(new BasicGraphBuilder());  
  }
  
  protected GraphBuilder builder() {
    return(m_builder);  
  }

}