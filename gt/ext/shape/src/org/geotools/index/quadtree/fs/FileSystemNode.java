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
/*
 * Created on 20-ago-2004
 */
package org.geotools.index.quadtree.fs;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.index.quadtree.Node;
import org.geotools.index.quadtree.StoreException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author Tommaso Nolli
 * @source $URL$
 */
public class FileSystemNode extends Node {
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.index.quadtree");
    private FileChannel channel;
    private ByteOrder order;
    private int subNodeStartByte;
    private int subNodesLength;
    private int numSubNodes;

    /**
     * DOCUMENT ME!
     *
     * @param bounds
     * @param channel DOCUMENT ME!
     * @param order DOCUMENT ME!
     * @param startByte DOCUMENT ME!
     * @param subNodesLength DOCUMENT ME!
     */
    public FileSystemNode(Envelope bounds, FileChannel channel,
        ByteOrder order, int startByte, int subNodesLength) {
        super(bounds);
        this.channel = channel;
        this.order = order;
        this.subNodeStartByte = startByte;
        this.subNodesLength = subNodesLength;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the numSubNodes.
     */
    public int getNumSubNodes() {
        return this.numSubNodes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param numSubNodes The numSubNodes to set.
     */
    public void setNumSubNodes(int numSubNodes) {
        this.numSubNodes = numSubNodes;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the subNodeStartByte.
     */
    public int getSubNodeStartByte() {
        return this.subNodeStartByte;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the subNodesLength.
     */
    public int getSubNodesLength() {
        return this.subNodesLength;
    }

    /**
     * @see org.geotools.index.quadtree.Node#getSubNode(int)
     */
    public Node getSubNode(int pos) throws StoreException {
        if (this.subNodes.size() > pos) {
            return super.getSubNode(pos);
        }

        try {
            FileSystemNode subNode = null;

            // Getting prec subNode...
            int offset = this.subNodeStartByte;

            if (pos > 0) {
                subNode = (FileSystemNode) super.getSubNode(pos - 1);
                offset = subNode.getSubNodeStartByte()
                    + subNode.getSubNodesLength();
            }

            if (this.channel.position() != offset) {
                /*
                   LOGGER.finest("Actual position: " + this.channel.position() +
                                 " moving to " + offset);
                 */
                this.channel.position(offset);
            }

            for (int i = 0, ii = subNodes.size(); i < ((pos + 1) - ii); i++) {
                subNode = readNode(this.channel, this.order);
                this.addSubNode(subNode);
            }
        } catch (IOException e) {
            throw new StoreException(e);
        }

        return super.getSubNode(pos);
    }

    /**
     * DOCUMENT ME!
     *
     * @param channel
     * @param order DOCUMENT ME!
     *
     * @return
     *
     * @throws IOException
     */
    public static FileSystemNode readNode(FileChannel channel, ByteOrder order)
        throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(4 + 32 + 4);
        buf.order(order);
        channel.read(buf);
        buf.flip();

        int offset = buf.getInt();
        double x1;
        double y1;
        double x2;
        double y2;
        x1 = buf.getDouble();
        y1 = buf.getDouble();
        x2 = buf.getDouble();
        y2 = buf.getDouble();

        Envelope env = new Envelope(x1, x2, y1, y2);

        int numShapesId = buf.getInt();

        // The buffer is completed....
        buf = ByteBuffer.allocate((4 * numShapesId) + 4);
        buf.order(order);
        channel.read(buf);
        buf.flip();

        int[] ids = new int[numShapesId];
        IntBuffer intBuf = buf.asIntBuffer();
        intBuf.get(ids);

        int numSubNodes = intBuf.get();

        // The buffer is completed....
        FileSystemNode node = new FileSystemNode(env, channel, order,
                (int) channel.position(), offset);
        node.setShapesId(ids);
        node.setNumSubNodes(numSubNodes);

        return node;
    }
}
