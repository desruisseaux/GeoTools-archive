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
 *    This file is based on an origional contained in the GISToolkit project:
 *    http://gistoolkit.sourceforge.net/
 *
 */
package org.geotools.data.shapefile.dbf;


import java.io.FileInputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Calendar;


/** A DbaseFileReader is used to read a dbase III format file.
 * <br>
 * The general use of this class is:
 * <CODE><PRE>
 * FileChannel in = new FileInputStream("thefile.dbf").getChannel();
 * DbaseFileReader r = new DbaseFileReader( in )
 * Object[] fields = new Object[r.getHeader().getNumFields()];
 * while (r.hasNext()) {
 *    r.readEntry(fields);
 *    // do stuff
 * }
 * r.close();
 * </PRE></CODE>
 * For consumers who wish to be a bit more selective with their reading of rows,
 * the Row object has been added. The semantics are the same as using the
 * readEntry method, but remember that the Row object is always the same. The
 * values are parsed as they are read, so it pays to copy them out (as each call
 * to Row.read() will result in an expensive String parse).
 * <br><b>EACH CALL TO readEntry OR readRow ADVANCES THE FILE!</b><br>
 * An example of using the Row method of reading:
 * <CODE><PRE>
 * FileChannel in = new FileInputStream("thefile.dbf").getChannel();
 * DbaseFileReader r = new DbaseFileReader( in )
 * int fields = r.getHeader().getNumFields();
 * while (r.hasNext()) {
 *   DbaseFileReader.Row row = r.readRow();
 *   for (int i = 0; i < fields; i++) {
 *     // do stuff
 *     Foo.bar( row.read(i) );
 *   }
 * }
 * r.close();
 * </PRE></CODE>
 *
 * @author Ian Schneider
 * @author Tommaso Nolli
 */
public class IndexedDbaseFileReader extends DbaseFileReader{


/**
   * 
   * @param recno
   * @throws IOException
   * @throws UnsupportedOperationException
   */
  public void goTo(int recno)
  throws IOException, UnsupportedOperationException
  {
    
    if (this.randomAccessEnabled) {
      int newPosition = this.header.getHeaderLength() +
                        this.header.getRecordLength() * (recno - 1);
    
      if (this.useMemoryMappedBuffer) {
        buffer.position(newPosition);
      } else {
        FileChannel fc = (FileChannel)this.channel;
        fc.position(newPosition);
        buffer.limit(buffer.capacity());
        buffer.position(0);
        fill(buffer, channel);
        buffer.position(0);
        
        this.currentOffset = newPosition;
      }
      
      if (idxReader != null) {
    	  idxReader.moveRow(recno);
      }
    } else {
        throw new UnsupportedOperationException("Random access not enabled!");
    }
      
  }
  
    /**
   * Like calling DbaseFileReader(ReadableByteChannel, true);
   * @param channel
   * @throws IOException
   */
  public IndexedDbaseFileReader(
	  ReadableByteChannel channel, DBFFileIndexer.Reader idxReader
  ) throws IOException {
      this(channel, true, idxReader);
  }

  /** Creates a new instance of DBaseFileReader
   * @param channel The readable channel to use.
   * @param useMemoryMappedBuffer Wether or not map the file in memory
   * @throws IOException If an error occurs while initializing.
   */
  public IndexedDbaseFileReader(
	  ReadableByteChannel channel, boolean useMemoryMappedBuffer, DBFFileIndexer.Reader idxReader
  )
  throws IOException
  {
	  super( channel, useMemoryMappedBuffer);
	  this.idxReader = idxReader;
}
  
  DBFFileIndexer.Reader idxReader;
  
  public void close() throws IOException {
	  super.close();
	  if (idxReader != null) 
		  idxReader.close();
  }
  
  protected CharBuffer getCharBuffer() {
	  return charBuffer;
  }
  
  protected Object readObject(final int fieldOffset, final int fieldNum)
	throws IOException {
	final char type = fieldTypes[fieldNum];
	final int fieldLen = fieldLengths[fieldNum];
	Object object = null;
	
	// System.out.println( charBuffer.subSequence(fieldOffset,fieldOffset +
	// fieldLen));
	
	if (fieldLen > 0) {
	
		switch (type) {
			// (L)logical (T,t,F,f,Y,y,N,n)
			case 'l':
			case 'L':
				switch (charBuffer.charAt(fieldOffset)) {
		
				case 't':
				case 'T':
				case 'Y':
				case 'y':
					object = Boolean.TRUE;
					break;
				case 'f':
				case 'F':
				case 'N':
				case 'n':
					object = Boolean.FALSE;
					break;
				default:
		
					throw new IOException("Unknown logical value : '"
							+ charBuffer.charAt(fieldOffset) + "'");
				}
				break;
			// (C)character (String)
			case 'c':
			case 'C':
				
				int start = fieldOffset;
				int end = fieldOffset + fieldLen - 1;
				
				if (idxReader != null) {
					idxReader.moveCol(fieldNum);
					start = idxReader.read();
					end = idxReader.read();
				}
				else {
//					 oh, this seems like a lot of work to parse strings...but,
					// For some reason if zero characters ( (int) char == 0 ) are
					// allowed
					// in these strings, they do not compare correctly later on down
					// the
					// line....
					
					// trim off whitespace and 'zero' chars
					
					while (start < end) {
						char c = charBuffer.get(start);
						if (c == 0 || Character.isWhitespace(c)) {
							start++;
						} else
							break;
					}
					while (end > start) {
						char c = charBuffer.get(end);
						if (c == 0 || Character.isWhitespace(c)) {
						
							end--;
						} else
							break;
					}
				}
				
				// set up the new indexes for start and end
				charBuffer.position(start).limit(end + 1);
				String s = charBuffer.toString();
				
				// this resets the limit...
				charBuffer.clear();
				object = s;
				break;
			// (D)date (Date)
			case 'd':
			case 'D':
				try {
					String tempString = charBuffer.subSequence(fieldOffset,
							fieldOffset + 4).toString();
					int tempYear = Integer.parseInt(tempString);
					tempString = charBuffer.subSequence(fieldOffset + 4,
							fieldOffset + 6).toString();
					int tempMonth = Integer.parseInt(tempString) - 1;
					tempString = charBuffer.subSequence(fieldOffset + 6,
							fieldOffset + 8).toString();
					int tempDay = Integer.parseInt(tempString);
					Calendar cal = Calendar.getInstance();
					cal.clear();
					cal.set(cal.YEAR, tempYear);
					cal.set(cal.MONTH, tempMonth);
					cal.set(cal.DAY_OF_MONTH, tempDay);
					object = cal.getTime();
				} catch (NumberFormatException nfe) {
					// todo: use progresslistener, this isn't a grave error.
				}
				break;
		
			// (F)floating (Double)
			case 'n':
			case 'N':
				try {
					if (header.getFieldDecimalCount(fieldNum) == 0) {
						object = new Integer(numberParser.parseInt(charBuffer,
								fieldOffset, fieldOffset + fieldLen - 1));
						break;
					}
					// else will fall through to the floating point number
				} catch (NumberFormatException e) {
		
					// todo: use progresslistener, this isn't a grave error.
		
					// don't do this!!! the Double parse will be attemted as we
					// fall
					// through, so no need to create a new Object. -IanS
					// object = new Integer(0);
		
					// Lets try parsing a long instead...
					try {
						object = new Long(numberParser.parseLong(charBuffer,
								fieldOffset, fieldOffset + fieldLen - 1));
						break;
					} catch (NumberFormatException e2) {
		
					}
				}
		
			case 'f':
			case 'F': // floating point number
				try {
		
					object = new Double(numberParser.parseDouble(charBuffer,
							fieldOffset, fieldOffset + fieldLen - 1));
				} catch (NumberFormatException e) {
					// todo: use progresslistener, this isn't a grave error,
					// though it
					// does indicate something is wrong
		
					// okay, now whatever we got was truly undigestable. Lets go
					// with
					// a zero Double.
					object = new Double(0.0);
				}
				break;
			default:
				throw new IOException("Invalid field type : " + type);
			}
	
		}
		return object;
	}
  
  public static void main(String[] args) throws Exception {
    FileChannel channel = new FileInputStream(args[0]).getChannel();
//   // IndexedDbaseFileReader reader = new IndexedDbaseFileReader(channel, true);
//    System.out.println(reader.getHeader());
//    int r = 0;
//    while (reader.hasNext()) {
//      System.out.println(++r + "," + java.util.Arrays.asList(reader.readEntry()));
//    }
  }
  
}
