/*
 *   $Id: OptGraphBuilder.java,v 1.2 2004/04/28 23:40:05 jdeolive Exp $
 * 
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

package org.geotools.graph.build.opt;

import org.geotools.graph.build.GraphBuilder;
import org.geotools.graph.build.basic.BasicGraphBuilder;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.geotools.graph.structure.opt.OptEdge;
import org.geotools.graph.structure.opt.OptNode;


/**
 * An implementation of GraphBuilder that builds optimized graph components.
 * 
 * @see org.geotools.graph.structure.opt.OptNode
 * @see org.geotools.graph.structure.opt.OptEdge
 * 
 * @author Justin Deoliveira, Refractions Research Inc, jdeolive@refractions.net
 *
 */
public class OptGraphBuilder extends BasicGraphBuilder {
 
  /**
   * Creates an optimized node.
   * 
   * @see GraphBuilder#buildNode()
   * @see OptNode
   */
  public Node buildNode() {
    return(new OptNode());
  }

  /**
   * Creates an optimized edge.
   * 
   * @see GraphBuilder#buildEdge()
   * @see OptEdge
   */
  public Edge buildEdge(Node nodeA, Node nodeB) {
    return(new OptEdge((OptNode)nodeA, (OptNode)nodeB));
  }
 
}
