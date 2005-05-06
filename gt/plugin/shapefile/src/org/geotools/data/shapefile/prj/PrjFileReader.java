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

package org.geotools.data.shapefile.prj;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.geotools.referencing.CRS;
import org.geotools.referencing.FactoryFinder;
import org.opengis.referencing.FactoryException;




public class PrjFileReader {
    
    ByteBuffer buffer;
    ReadableByteChannel channel;
    CharBuffer charBuffer;
    CharsetDecoder decoder;
    
    org.opengis.referencing.crs.CoordinateReferenceSystem cs;
    //private int[] content;
    
    /** Load the index file from the given channel.
     * @param channel The channel to read from.
     * @throws IOException If an error occurs.
     */
    public PrjFileReader(ReadableByteChannel channel) throws IOException, FactoryException {
        Charset chars = Charset.forName("ISO-8859-1");
        decoder = chars.newDecoder();
        this.channel = channel;
        init();
        
        //ok, everything is ready...
        decoder.decode(buffer,charBuffer,true);
        buffer.limit(buffer.capacity());
        charBuffer.flip();
        
        String wkt = charBuffer.toString();
     
        cs = FactoryFinder.getCRSFactory(null).createFromWKT(wkt);
    }
    
    public org.opengis.referencing.crs.CoordinateReferenceSystem getCoodinateSystem(){
        return cs;
    }
    
   
    
    private int fill(ByteBuffer buffer,ReadableByteChannel channel) throws IOException {
        int r = buffer.remaining();
        // channel reads return -1 when EOF or other error
        // because they a non-blocking reads, 0 is a valid return value!!
        while (buffer.remaining() > 0 && r != -1) {
            r = channel.read(buffer);
        }
        if (r == -1) {
            buffer.limit(buffer.position());
        }
        return r;
    }
    
    private void init() throws IOException {
        // create the ByteBuffer
        // if we have a FileChannel, lets map it
        if (channel instanceof FileChannel) {
            FileChannel fc = (FileChannel) channel;
            buffer = fc.map(FileChannel.MapMode.READ_ONLY,0,fc.size());
            buffer.position((int) fc.position());
        } else {
            // Some other type of channel
            // start with a 8K buffer, should be more than adequate
            int size = 8 * 1024;
            // if for some reason its not, resize it
            // size = header.getRecordLength() > size ? header.getRecordLength() : size;
            buffer = ByteBuffer.allocateDirect(size);
            // fill it and reset
            fill(buffer,channel);
            buffer.flip();
        }
        
        // The entire file is in little endian
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        
        
        charBuffer = CharBuffer.allocate(8*1024);
        Charset chars = Charset.forName("ISO-8859-1");
        decoder = chars.newDecoder();
        
        
    }
    
    
    
    
}