/*
 * File is generated by 'Unit Tests Generator' developed under
 * 'Web Test Tools' project at http://sf.net/projects/wttools/
 * Copyright (C) 2001 "Artur Hefczyc" <kobit@users.sourceforge.net>
 * to all 'Web Test Tools' subprojects.
 *
 * No rigths to files and no responsibility for code generated
 * by this tool are belonged to author of 'unittestsgen' utility.
 *
 */
package org.geotools.data.vpf.io;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.resources.TestData;

/**
 * File <code>TableInputStreamTest.java</code> is automaticaly generated by
 * 'unittestsgen' application. Code generator is created for java
 * sources and for 'junit' package by "Artur Hefczyc"
 * <kobit@users.sourceforge.net><br/>
 * You should fulfil test methods with proper code for testing
 * purpose. All methods where you should put your code are below and
 * their names starts with 'test'.<br/>
 * You can run unit tests in many ways, however prefered are:
 * <ul>
 *   <li>Run tests for one class only, for example for this class you
 *       can run tests with command:
 *     <pre>
 *       java -cp "jar/thisjarfile.jar;lib/junit.jar" org.geotools.vpf.io.TableInputStreamTest
 *     </pre>
 *   </li>
 *   <li>Run tests for all classes in one command call. Code generator
 *       creates also <code>TestAll.class</code> which runs all
 *       available tests:
 *     <pre>
 *       java -cp "jar/thisjarfile.jar;lib/junit.jar" TestAll
 *     </pre>
 *   </li>
 *   <li>But the most prefered way is to run all tests from
 *     <em>Ant</em> just after compilation process finished.<br/>
 *     To do it. You need:
 *     <ol>
 *       <li>Ant package from
 *         <a href="http://jakarta.apache.org/">Ant</a>
 *       </li>
 *       <li>JUnit package from
 *         <a href="http://www.junit.org/">JUnit</a>
 *       </li>
 *       <li>Put some code in your <code>build.xml</code> file
 *         to tell Ant how to test your package. Sample code for
 *         Ant's <code>build.xml</code> you can find in created file:
 *         <code>sample-junit-build.xml</code>. And remember to have
 *         <code>junit.jar</code> in CLASSPATH <b>before</b> you run Ant.
 *         To generate reports by ant you must have <code>xalan.jar</code>
 *         in your <code>ANT_HOME/lib/</code> directory.
 *       </li>
 *     </ol>
 *   </li>
 * </ul>
 * @source $URL$
 */
public class TableInputStreamTest extends TestCase
{
  /**
   * Instance of tested class.
   */
  protected TableInputStream varTableInputStream;

  /**
   * Public constructor for creating testing class.
   */
  public TableInputStreamTest(String name) {
    super(name);
  } // end of TableInputStreamTest(String name)
  /**
   * This main method is used for run tests for this class only
   * from command line.
   */
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  } // end of main(Stringp[] args)

  /**
   * This method is called every time before particular test execution.
   * It creates new instance of tested class and it can perform some more
   * actions which are necessary for performs tests.
   */
  protected void setUp()
    throws IOException
  {
  	File edg = TestData.file( this, "dnc13/browse/coa/edg" );    
    varTableInputStream = new TableInputStream( edg.getPath() );
  } // end of setUp()
  protected void tearDown()
    throws IOException
  {
    varTableInputStream.close();
  }
  /**
   * Returns all tests which should be performed for testing class.
   * By default it returns only name of testing class. Instance of this
   * is then created with its constructor.
   */
  public static Test suite() {
    return new TestSuite(TableInputStreamTest.class);
  } // end of suite()

  /**
   * Method for testing original source method:
   * int availableRows()
   * from tested class
   */
  public void testAvailableRows()
  {
    assertEquals("Cheking available rows in stream buffer, should be 0 - "+
                 "buffer is not implemented yet. "+
                 varTableInputStream.streamFile,
                 0, varTableInputStream.availableRows());

  } // end of testAvailableRows()

  /**
   * Method for testing original source method:
   * org.geotools.vpf.TableHeader getHeader()
   * from tested class
   */
  public void testGetHeader()
  {
    assertNotNull("Checking if header was read from stream, integrity of "+
                  "header is tested in TableHeaderTest unit test "+
                  varTableInputStream.streamFile,
                  varTableInputStream.getHeader());
  } // end of testGetHeader()

  /**
   * Method for testing original source method:
   * org.geotools.vpf.TableRow readRow()
   * from tested class
   */
  public void testReadFixedLengthRow()
    throws IOException
  {
  	File cnd = TestData.file( this, "dnc13/browse/coa/cnd" );    
    varTableInputStream = new TableInputStream( cnd.getPath() );
    
    TableRow rowNext = (TableRow)varTableInputStream.readRow();
    assertNotNull("Reading next row from given table "+
                  varTableInputStream.streamFile, rowNext);
    TableRow row1 = (TableRow)varTableInputStream.readRow(1);
    assertNotNull("Reading row no 1 from given table "+
                  varTableInputStream.streamFile, row1);
    //assertEquals("Comparing read result of readRow() and readRow(int)",
                 //rowNext, row1);
  } // end of testReadRow()
  
  /**
   * Method for testing original source method:
   * org.geotools.vpf.TableRow readRow()
   * from tested class
   */
  public void testReadVariableLengthRow()
    throws IOException
  {
  	File edg = TestData.file( this, "dnc13/browse/coa/edg" );    
    varTableInputStream = new TableInputStream( edg.getPath() );
    
    TableRow rowNext = (TableRow)varTableInputStream.readRow();
    assertNotNull("Reading next row from given table "+
                  varTableInputStream.streamFile, rowNext);
    TableRow row1 = (TableRow)varTableInputStream.readRow(1);
    assertNotNull("Reading row no 1 from given table "+
                  varTableInputStream.streamFile, row1);
    //assertEquals("Comparing read result of readRow() and readRow(int)",
                 //rowNext, row1);
  } // end of testReadRow()
  
  /**
   * Method for testing original source method:
   * int readRows(org.geotools.vpf.TableRow[])
   * from tested class
   */
  public void testReadFixedLengthRows()
    throws IOException
  {
  	File cnd = TestData.file( this, "dnc13/browse/coa/cnd" );    
    varTableInputStream = new TableInputStream( cnd.getPath() );
    
    //    System.out.println("Table header:\n"+varTableInputStream.header.toString());
    TableRow[] rowsNext = new TableRow[5];
    assertEquals("Reading 5 next rows into given array from given table "+
                 varTableInputStream.streamFile,
                 5, varTableInputStream.readRows(rowsNext));
    TableRow[] rows1 = new TableRow[5];
    assertEquals("Reading 5 rows from 1st into given array from given table "+
                 varTableInputStream.streamFile,
                 5, varTableInputStream.readRows(rows1, 1));
    for (int i = 0; i < rowsNext.length; i++)
    {
      //assertEquals("Comparing read result of readRow() and readRow(int)",
                  //rowsNext[i], rows1[i]);
    } // end of for (int i = 0; i < rowsNext.length; i++)
//     System.out.println("Fixed size data "+rows.length+" rows read from "+
//                        "dnc13/browse/coa/cnd:");
//     for (int i = 0; i < rows.length; i++)
//     {
//       if (rows[i] != null)
//       {
//         System.out.println(rows[i].toString());
//       } // end of if (row != null)
//     } // end of for (int i = 0; i < rows.length; i++)
  } // end of testReadRows895743446(org.geotools.vpf.TableRow[])

  /**
   * Method for testing original source method:
   * int readRows(org.geotools.vpf.TableRow[])
   * from tested class
   */
  
  public void testReadVariableLengthRows()
    throws IOException
  {
  	File edg = TestData.file( this, "dnc13/browse/coa/edg" );    
    varTableInputStream = new TableInputStream( edg.getPath() );
    //    System.out.println("Table header:\n"+varTableInputStream.header.toString());
    TableRow[] rowsNext = new TableRow[5];
    assertEquals("Reading 5 next rows into given array from given table "+
                 varTableInputStream.streamFile,
                 5, varTableInputStream.readRows(rowsNext));
    TableRow[] rows1 = new TableRow[5];
    assertEquals("Reading 5 rows from 1st into given array from given table "+
                 varTableInputStream.streamFile,
                 5, varTableInputStream.readRows(rows1, 1));
    for (int i = 0; i < rowsNext.length; i++)
    {
      //assertEquals("Comparing read result of readRow() and readRow(int)",
                  //rowsNext[i], rows1[i]);
    } // end of for (int i = 0; i < rowsNext.length; i++)
//     System.out.println("Variable size data "+rows.length+" rows read from "+
//                        "dnc13/browse/coa/edg:");
//     for (int i = 0; i < rows.length; i++)
//     {
//       if (rows[i] != null)
//       {
//         System.out.println(rows[i].toString());
//       } // end of if (row != null)
//     } // end of for (int i = 0; i < rows.length; i++)
  } // end of testReadRows895743446(org.geotools.vpf.TableRow[])

} // end of TableInputStreamTest
