/**
 * 
 */
package org.geotools.index.quadtree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.geotools.data.shapefile.shp.IndexFile;
import org.geotools.index.Data;
import org.geotools.index.DataDefinition;
import org.geotools.index.TreeException;

import com.vividsolutions.jts.geom.Envelope;

/**
 * An iterator that searches the Quad tree for all the features that intersect
 * the bounds (or probably itersects the bounds).
 * 
 * <p>
 * The search pattern is a kind of breadth first search. It searches:<br>
 * root->child1->child2->child3...->child1Child1->child1Child2...<br>
 * This is so that the large features covering the majority of the bounds are
 * rendered first. All the features of an area are rendered then the next area.
 * If there are many feature such as roads it doesn't give the impression of
 * rendering as fast.
 * </p>
 * 
 * @author Jesse
 */
public class LazySearchIterator implements Iterator {

	static final DataDefinition DATA_DEFINITION = new DataDefinition("US-ASCII");

	private static final int MAX_INDICES = 32768;
	static {
		DATA_DEFINITION.addField(Integer.class);
		DATA_DEFINITION.addField(Long.class);
	}

	// the next data to return in next()
	private Data next = null;

	// the current node that is having its idIndexes added to the cache. This is
	// used
	// in filled the cache is used to determine next.
	private Node current;

	// the next index in current to be added to the cache when it needs to be
	// filled
	private int idIndex = 0;

	// indicates whether this iterator has been closed.
	private boolean closed;

	// the bounds that we are searching.
	private Envelope bounds;

	// the cache
	private Iterator data;

	// the root node
	private Node root;

	private IndexFile indexfile;

	public LazySearchIterator(Node root, IndexFile file, Envelope bounds) {
		super();
		this.current = root;
		this.root = root;
		this.bounds = bounds;
		this.closed = false;
		this.next = null;
		this.indexfile=file;
	}

	public boolean hasNext() {
		if (closed)
			throw new IllegalStateException("Iterator has been closed!");
		if (next != null)
			return true;
		if (data != null && data.hasNext()) {
			next = (Data) data.next();
		} else {
			fillCache();
			if (data != null && data.hasNext())
				next = (Data) data.next();
		}
		return next != null;
	}

	private void fillCache() {
		List indices = new ArrayList(MAX_INDICES);
		ArrayList dataList = new ArrayList(MAX_INDICES);
		try {
			while (indices.size() < MAX_INDICES && current != null) {
				if (idIndex < current.getNumShapeIds() && !current.isVisited()
						&& current.getBounds().intersects(bounds)) {
					indices.add(new Integer(current.getShapeId(idIndex)));
					idIndex++;
				} else {
					current.setShapesId(new int[0]);
					current.setVisited(true);
					idIndex = 0;
					boolean found = false;
					while (!found && current != null) {
						found = findUnvistedSibling();
						if (!found && current != null)
							found = findUnvistedChild();
					}
				}
			}
			// sort so offset lookup is faster
			Collections.sort(indices);
			for (Iterator iter = indices.iterator(); iter.hasNext();) {
				Integer recno = (Integer) iter.next();
				Data data = new Data(DATA_DEFINITION);
				data.addValue(new Integer(recno.intValue() + 1));
				data.addValue(new Long(indexfile.getOffsetInBytes(recno
						.intValue())));
				dataList.add(data);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (TreeException e) {
			throw new RuntimeException(e);
		} catch (StoreException e) {
			throw new RuntimeException(e);
		}
		data = dataList.iterator();
	}

	private boolean findUnvistedSibling() throws StoreException {
		if (current == null)
			return true;
		Node sibling = current.getSibling();
		// is last sibling
		if (sibling == null) {
			findSiblingWithUnvistedChildren();
			return false;
		}
		
		if (sibling.isVisited() ) {
			current = sibling;
			if (!current.isChildrenVisited())
				return false;
			else{
				return findUnvistedSibling();
			}
		}
		
		if (!sibling.getBounds().intersects(bounds)) {
			sibling.setChildrenVisited(true);
			sibling.setVisited(true);
			current = sibling;
			return findUnvistedSibling();
		}
		current = sibling;
		return true;
	}

	private void findSiblingWithUnvistedChildren() throws StoreException {
		if (current == root || current == null)
			return;
		Node node = current.getParent().getSubNode(0);
		while (node != null) {
			if (!node.isChildrenVisited()) {
				current = node;
				return;
			}
			node = node.getSibling();
		}

		current = null;
	}

	private boolean findUnvistedChild() throws StoreException {
		boolean foundUnvisited = false;
		for (int i = 0; i < current.getNumSubNodes(); i++) {
			Node node = current.getSubNode(i);
			if (!node.isVisited() && node.getBounds().intersects(bounds)) {
				foundUnvisited = true;
				current = node;
				break;
			} else {
				node.setChildrenVisited(true);
				node.setVisited(true);
			}
		}
		if (!foundUnvisited) {
			current.setChildrenVisited(true);
			current.setVisited(true);
			if (current.getSibling() == null
					|| current.getSibling().isVisited())
				current = current.getParent();
		}
		return foundUnvisited;
	}

	public Object next() {
		if (!hasNext())
			throw new NoSuchElementException("No more elements available");
		Data temp = next;
		next = null;
		return temp;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	public void close() throws StoreException {
		this.closed = true;
	}

}
