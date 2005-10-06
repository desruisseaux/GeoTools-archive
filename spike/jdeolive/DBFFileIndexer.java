package org.geotools.data.shapefile.dbf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Calendar;
import java.util.logging.Logger;

import sun.security.action.GetLongAction;
import sun.security.krb5.internal.crypto.d;

public class DBFFileIndexer {

    private static final Logger LOGGER = Logger.getLogger(
    	"org.geotools.data.shapefile"
	);
    
	URL dbf;
	
	public DBFFileIndexer(URL dbf) {
		this.dbf = dbf;
	}
	
	public void index() throws IOException {
		//create input stream
		File dbfFile = new File(dbf.getFile());
		if (!dbfFile.exists()) 
			throw new FileNotFoundException(dbf.getFile());
		
		//check if index already exists
		
		String path = dbfFile.getPath();
		File dixFile = new File(path.substring(0,path.lastIndexOf('.')) + ".dix");
		if (dixFile.exists()) {
			LOGGER.info(".dix file already exists, index creation aborted");
			return;
		}
		
		//index the dbf	reader/indexer
		Writer reader = new Writer(
			new IndexedDbaseFileReader(new FileInputStream(dbfFile).getChannel(),null),
			new DataOutputStream(new FileOutputStream(dixFile))
		);
		
		reader.writeHeader();
		
		//index each row
		int nrecs = reader.getReader().getHeader().getNumRecords();
		for (int i = 1; i <= nrecs; i++) {
			reader.writeRow(i);
		}
		
		reader.close();
	
	}
	
	static class Writer {

		IndexedDbaseFileReader reader;
		DataOutputStream out;
		
		public Writer(IndexedDbaseFileReader reader, DataOutputStream out) 
			throws IOException {
			
			this.reader = reader;
			this.out = out;
		}
		
		public IndexedDbaseFileReader getReader() {
			return reader;
		}
		
		public void writeHeader() throws IOException {
			//write out mapping of field number to char file number
			DbaseFileHeader header = reader.getHeader();
			int nfields = header.getNumFields();
			int nstrings = 0;
			out.writeInt(nfields);
			for (int i = 0; i < nfields; i++) {
				char type = header.getFieldType(i);
				if (type == 'c' || type == 'C') {
					out.writeInt(nstrings++);
				}
				else out.writeInt(-1);
			}
		}
		
		public void writeRow(int row) throws IOException {
			reader.readRow();
			
			for (int i = 0; i < reader.getHeader().getNumFields(); i++) {
				int offset = getOffset(i);
				writeField(offset,i);	
			}
		}

		public void writeField(int offset, int column) throws IOException {
			
			DbaseFileHeader header = reader.getHeader();
			CharBuffer charBuffer = reader.getCharBuffer();
			
			char type = header.getFieldType(column);
			int len = header.getFieldLength(column);
			
			if (len > 0 && (type == 'c' || type == 'C')) {
				int start = offset;
				int end = offset + len - 1;
				
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
				// set up the new indexes for start and end
				charBuffer.position(start).limit(end + 1);
				// this resets the limit...
				charBuffer.clear();
				
				//write the string into the index
				out.writeInt(start);
				out.writeInt(end);
			}
		}
		
		public void close() throws IOException {
			out.close();
			reader.close();
		}
		
		int getOffset(int column) {
			int offset = 0;
			for (int i = 0, ii = column; i < ii; i++) {
				offset += reader.getHeader().getFieldLength(i);
			}
			return offset;
		}
	}
	
	public static class Reader {
		static final int NROWS = 1000;
		
		/** underlying file containing dbf data **/
		RandomAccessFile file;
		/** in memory buffer **/
		byte[] buffer;
		/** the first row currently stored in memory **/
		int start;
		/** offset into buffer currently being read **/
		int offset;	
		/** the current row being read **/
		int row;	
		/** number of fields in file **/
		int nfields;
		/** number of string fields **/
		int nstrings;
		/** mapping of index fields to dbf fields **/
		int[] mappings;
		
		public Reader (URL dix) throws IOException {
			file = new RandomAccessFile(new File(dix.getFile()),"r");
			
			//read the header and mappings
			nfields = file.readInt();
			
			nstrings = 0;
			mappings = new int[nfields];
			
			for (int i = 0; i < nfields; i++) {
				mappings[i] = file.readInt();
				if (mappings[i] != -1) {
					nstrings++;
				}
			}
			
			row = start = 1;
			offset = 0;
			buffer = new byte[NROWS*getRowSize()];
			fill(start);
		}
		
		public int getHeaderSize() {
			return 4 + 4*mappings.length;
		}
		
		public int getRowSize() {
			return nstrings*8; 
		}
		
		public void moveRow(int row) throws IOException {
			this.row = row;
			
			if (row < start || row >= (start + NROWS - 1)) {
				//fill the buffer
				fill(row);
				start = row;
				offset = 0;
			}
			else {
				offset = (row - start)*getRowSize();
			}
		}
		
		public void moveCol(int col) throws IOException {
			//offset = map(col)*8;
		}
		
		public int read() throws IOException {
			int i = (buffer[offset++] & 0xFF) << 24;
			i |= (buffer[offset++] & 0xFF) << 16;
			i |= (buffer[offset++] & 0xFF) << 8;
			i |= (buffer[offset++] & 0xFF);
			            
			return i;
		}
		
		public void close() throws IOException {
			file.close();
		}
		
		void fill(int row) throws IOException {
			long position = getHeaderSize() + (row-1)*getRowSize();
			file.seek(position);
			
			file.read(buffer);
		}
		
		int map(int column) {
			return mappings[column];
		}
	}
}
