/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.geotools.index.rtree.fs;

import java.nio.channels.FileChannel;
import java.util.Stack;

import org.geotools.index.DataDefinition;

/**
 * @author Tommaso Nolli
 */
public class Parameters {

    private int maxNodeEntries;
    private int minNodeEntries;
    private short splitAlg;
    private DataDefinition dataDef;
    private FileChannel channel;
    private Stack freePages;
    private boolean forceChannel;

    public Parameters() {
        this.freePages = new Stack();
    }

	/**
	 * @return
	 */
	public FileChannel getChannel() {
		return channel;
	}

	/**
	 * @return
	 */
	public DataDefinition getDataDef() {
		return dataDef;
	}

	/**
	 * @return
	 */
	public int getMaxNodeEntries() {
		return maxNodeEntries;
	}

	/**
	 * @return
	 */
	public int getMinNodeEntries() {
		return minNodeEntries;
	}

	/**
	 * @return
	 */
	public short getSplitAlg() {
		return splitAlg;
	}

	/**
	 * @param channel
	 */
	public void setChannel(FileChannel channel) {
		this.channel = channel;
	}

	/**
	 * @param definition
	 */
	public void setDataDef(DataDefinition definition) {
		dataDef = definition;
	}

	/**
	 * @param i
	 */
	public void setMaxNodeEntries(int i) {
		maxNodeEntries = i;
	}

	/**
	 * @param i
	 */
	public void setMinNodeEntries(int i) {
		minNodeEntries = i;
	}

	/**
	 * @param s
	 */
	public void setSplitAlg(short s) {
		splitAlg = s;
	}

	/**
	 * @return
	 */
	public boolean getForceChannel() {
		return forceChannel;
	}

	/**
	 * @param b
	 */
	public void setForceChannel(boolean b) {
		forceChannel = b;
	}

	/**
	 * @return
	 */
	public Stack getFreePages() {
		return freePages;
	}

	/**
	 * @param stack
	 */
	public void setFreePages(Stack stack) {
		freePages = stack;
	}

}
