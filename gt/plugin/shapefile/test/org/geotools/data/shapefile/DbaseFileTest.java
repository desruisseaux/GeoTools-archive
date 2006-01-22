/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.data.shapefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.dbf.DbaseFileWriter;
import org.geotools.TestData;


/**
 *
 * @source $URL$
 * @version $Id$
 * @author Ian Schneider
 * @author James Macgill
 */
public class DbaseFileTest extends TestCaseSupport {
  
  static final String TEST_FILE = "shapes/statepop.dbf";
  
  private DbaseFileReader dbf = null;
  
  public DbaseFileTest(String testName) throws IOException {
    super(testName);
  }
  
  public static void main(String[] args) {
    verbose = true;
    junit.textui.TestRunner.run(suite(DbaseFileTest.class));
  }

  protected void setUp() throws Exception {
    super.setUp();
    dbf = new DbaseFileReader(TestData.openChannel(TEST_FILE));
  }
  
  public void testNumberofColsLoaded(){
    assertEquals("Number of attributes found incorect", 252, dbf.getHeader().getNumFields());
  }
  
  public void testNumberofRowsLoaded(){
    assertEquals("Number of rows", 49, dbf.getHeader().getNumRecords());
  }
  
  public void testDataLoaded() throws Exception{
    Object[] attrs = new Object[dbf.getHeader().getNumFields()];
    dbf.readEntry(attrs);
    assertEquals("Value of Column 0 is wrong", "Illinois", attrs[0]);
    assertEquals("Value of Column 4 is wrong", 143986.61, ((Double)attrs[4]).doubleValue(), 0.001);
  }
  
  public void testRowVsEntry() throws Exception {
    Object[] attrs = new Object[dbf.getHeader().getNumFields()];
    ReadableByteChannel ch2 = TestData.openChannel(TEST_FILE);
    DbaseFileReader dbf2 = new DbaseFileReader(ch2);
    while (dbf.hasNext()) {
      dbf.readEntry(attrs);
      DbaseFileReader.Row r = dbf2.readRow();
      for (int i = 0, ii = attrs.length; i < ii; i++) {
        assertNotNull(attrs[i]);
        assertNotNull(r.read(i));
        assertEquals(attrs[i], r.read(i));
      }
    }
    ch2.close();
  }
  
  public void testHeader() throws Exception {
    DbaseFileHeader header = new DbaseFileHeader();
    header.addColumn("emptyString", 'C', 20, 0);
    header.addColumn("emptyInt", 'N', 20, 0);
    header.addColumn("emptyDouble", 'N',20,5);
    header.addColumn("emptyFloat", 'F', 20, 5);
    header.addColumn("emptyLogical", 'L', 1, 0);
    header.addColumn("emptyDate", 'D', 20, 0);
    int length = header.getRecordLength();
    header.removeColumn("emptyDate");
    assertTrue(length !=  header.getRecordLength());
    header.addColumn("emptyDate", 'D',20,0);
    assertTrue(length == header.getRecordLength());
    header.removeColumn("billy");
    assertTrue(length == header.getRecordLength());
  }

  
  public void testEmptyFields() throws Exception {
    DbaseFileHeader header = new DbaseFileHeader();
    header.addColumn("emptyString", 'C', 20, 0);
    header.addColumn("emptyInt", 'N', 20, 0);
    header.addColumn("emptyDouble", 'N',20,5);
    header.addColumn("emptyFloat", 'F', 20, 5);
    header.addColumn("emptyLogical", 'L', 1, 0);
    header.addColumn("emptyDate", 'D', 20, 0);
    header.setNumRecords(20);
    File f = new File(System.getProperty("java.io.tmpdir"),"scratchDBF.dbf");
    FileOutputStream fout = new FileOutputStream(f);
    DbaseFileWriter dbf = new DbaseFileWriter(header,fout.getChannel());
    for (int i = 0; i < header.getNumRecords(); i++) {
      dbf.write(new Object[6]);
    }
    dbf.close();
    FileInputStream in = new FileInputStream(f);
    DbaseFileReader r = new DbaseFileReader(in.getChannel());
    int cnt = 0;
    while (r.hasNext()) {
      cnt++;
      Object[] o = r.readEntry(); 
      assertTrue(o.length == r.getHeader().getNumFields());
    }
    assertEquals("Bad number of records",cnt,20);
    f.delete();
  }
  
  public void testFieldFormatter() throws Exception {
      DbaseFileWriter.FieldFormatter formatter=new DbaseFileWriter.FieldFormatter();
      
      String stringWithInternationChars="hello "+'\u20ac';
      if (verbose) {
        System.out.println(stringWithInternationChars);
      }
      String formattedString=formatter.getFieldString(10, stringWithInternationChars);
      
      assertEquals("          ".getBytes().length, formattedString.getBytes().length);
    
      //test when the string is too big.
      stringWithInternationChars='\u20ac'+"1234567890";
      formattedString=formatter.getFieldString(10, stringWithInternationChars);
      
      assertEquals("          ".getBytes().length, formattedString.getBytes().length);
    
    
  }
  
}
