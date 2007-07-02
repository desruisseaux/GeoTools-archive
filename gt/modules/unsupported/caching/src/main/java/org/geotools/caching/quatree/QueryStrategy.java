package org.geotools.caching.quatree;

public interface QueryStrategy {
	
	public Node getNextNode(Node current, boolean[] hasNext) ;

}
