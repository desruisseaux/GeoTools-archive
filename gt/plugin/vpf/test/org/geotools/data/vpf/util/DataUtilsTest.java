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
package org.geotools.data.vpf.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.data.vpf.ifc.DataTypesDefinition;

/**
 * File <code>DataUtilsTest.java</code> is automaticaly generated by
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
 *       java -cp "jar/thisjarfile.jar;lib/junit.jar" org.geotools.vpf.util.DataUtilsTest
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
 */
public class DataUtilsTest extends TestCase
  implements DataTypesDefinition
{
  /**
   * Instance of tested class.
   */
  protected DataUtils varDataUtils;

  /**
   * Public constructor for creating testing class.
   */
  public DataUtilsTest(String name) {
    super(name);
  } // end of DataUtilsTest(String name)
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
  protected void setUp() {
    varDataUtils = new org.geotools.data.vpf.util.DataUtils();
  } // end of setUp()
  /**
   * Returns all tests which should be performed for testing class.
   * By default it returns only name of testing class. Instance of this
   * is then created with its constructor.
   */
  public static Test suite()
  {
    return new TestSuite(DataUtilsTest.class);
  } // end of suite()

  public static final byte[][] TEST_SAMPLES =
  {
	{         0,          0,          0,          1},
	{         1,          0,          0,          0},
	{         0,          0,          0, (byte)0xFF},
	{(byte)0xFF,          0,          0,          0},
	{         0,          0, (byte)0x0F, (byte)0xF0},
	{(byte)0xF0, (byte)0x0F,          0,          0}
  };

  public static final int[][] TEST_RESULTS =
  {
	//      Expected results
	//  BigEndian     LittleEndian
	{          1,       16777216},
	{   16777216,              1},
	{        255,      -16777216},
	{  -16777216,            255},
	{       4080,     -267452416},
	{ -267452416,           4080}
  };

  /**
   * Method for testing original source method:
   * int littleEndianToInt(byte[])
   * from tested class
   */
//   public void testLittleEndianToInt1374008726()
//   {
// 	for (int i = 0; i < TEST_SAMPLES.length; i++)
// 	{
// 	  assertEquals("Testing little endian converions from bytes to java int",
// 				   TEST_RESULTS[i][1],
// 				   DataUtils.littleEndianToInt(TEST_SAMPLES[i]));
// 	} // end of for (int i = 0; i < TEST_SAMPLES.length; i++)
//   } // end of testLittleEndianToInt1374008726(byte[])

  /**
   * Method for testing original source method:
   * byte[] toBigEndian(byte[])
   * from tested class
   */
  public void testToBigEndian1374008726()
  {
	byte[] testData = DataUtils.toBigEndian(TEST_SAMPLES[0]);
	for (int i = 0; i < testData.length; i++)
	{
	  assertEquals("Checking translation little endian bytes order to bin "+
				   "endian bytes order", testData[i], TEST_SAMPLES[1][i]);
	} // end of for (int i = 0; i < testData.length; i++)
	testData = DataUtils.toBigEndian(TEST_SAMPLES[1]);
	for (int i = 0; i < testData.length; i++)
	{
	  assertEquals("Checking translation little endian bytes order to bin "+
				   "endian bytes order", testData[i], TEST_SAMPLES[0][i]);
	} // end of for (int i = 0; i < testData.length; i++)	
	testData = DataUtils.toBigEndian(TEST_SAMPLES[2]);
	for (int i = 0; i < testData.length; i++)
	{
	  assertEquals("Checking translation little endian bytes order to bin "+
				   "endian bytes order", testData[i], TEST_SAMPLES[3][i]);
	} // end of for (int i = 0; i < testData.length; i++)	
	testData = DataUtils.toBigEndian(TEST_SAMPLES[3]);
	for (int i = 0; i < testData.length; i++)
	{
	  assertEquals("Checking translation little endian bytes order to bin "+
				   "endian bytes order", testData[i], TEST_SAMPLES[2][i]);
	} // end of for (int i = 0; i < testData.length; i++)	
	testData = DataUtils.toBigEndian(TEST_SAMPLES[4]);
	for (int i = 0; i < testData.length; i++)
	{
	  assertEquals("Checking translation little endian bytes order to bin "+
				   "endian bytes order", testData[i], TEST_SAMPLES[5][i]);
	} // end of for (int i = 0; i < testData.length; i++)	
	testData = DataUtils.toBigEndian(TEST_SAMPLES[5]);
	for (int i = 0; i < testData.length; i++)
	{
	  assertEquals("Checking translation little endian bytes order to bin "+
				   "endian bytes order", testData[i], TEST_SAMPLES[4][i]);
	} // end of for (int i = 0; i < testData.length; i++)	
  } // end of testToBigEndian1374008726(byte[])

  public static final char[] TEST_TYPES =
  {
    DATA_TEXT, DATA_LEVEL1_TEXT,
    DATA_LEVEL2_TEXT, DATA_LEVEL3_TEXT,
    DATA_SHORT_FLOAT,
    DATA_LONG_FLOAT,
    DATA_SHORT_INTEGER,
    DATA_LONG_INTEGER,
    //DATA_2_COORD_F,
    //DATA_2_COORD_R,
    //DATA_3_COORD_F,
    //DATA_3_COORD_R,
    DATA_DATE_TIME,
    DATA_NULL_FIELD,
    DATA_TRIPLET_ID,
  };

  public static final Class[] RESULT_TYPES =
  {
    java.lang.String.class, java.lang.String.class,
    java.lang.String.class, java.lang.String.class,
    java.lang.Float.class,
    java.lang.Double.class,
    java.lang.Short.class,
    java.lang.Integer.class,
    //org.geotools.data.vpf.io.Coordinate2DFloat.class,
    //org.geotools.data.vpf.io.Coordinate2DDouble.class,
    //org.geotools.data.vpf.io.Coordinate3DFloat.class,
    //org.geotools.data.vpf.io.Coordinate3DDouble.class,
    org.geotools.data.vpf.io.VPFDate.class,
    null,
    null
  };

  /**
   * Method for testing original source method:
   * java.lang.Object decodeData(byte[], char)
   * from tested class
   */
  public void testDecodeData13740087263052374()
  {
    byte[] testData = new byte[]
      {0, 1, 0, 1,
       0, 1, 0, 1,
       0, 1, 0, 1,
       0, 1, 0, 1};
    for (int i = 0; i < TEST_TYPES.length; i++)
    {
      Object result = DataUtils.decodeData(testData, TEST_TYPES[i]);
      if (i < TEST_TYPES.length - 2)
      {
        assertTrue("Incorrect type "+RESULT_TYPES[i].getName()+
                   " detected for data for VPF type: "+TEST_TYPES[i],
                   RESULT_TYPES[i].isInstance(result));
      } // end of if (i < TEST_TYPES.length - 2)
      else
      {
        assertNull("for this type: "+RESULT_TYPES[i]+" NULL should be returned",
                   result);
      } // end of else
    } // end of for (int i = 0; i < TEST_TYPES.length; i++)
  } // end of testDecodeData13740087263052374(byte[], char)

  /**
   * Method for testing original source method:
   * double decodeDouble(byte[])
   * from tested class
   */
  public void testDecodeDouble1374008726()
  {
	assertEquals("Decoding double from bytes stream", 4.9E-324d,
				 DataUtils.decodeDouble(new byte[]
                   {0, 0, 0, 0, 0, 0, 0, 1}), 0);
	assertEquals("Decoding double from bytes stream", 1.26E-321d,
				 DataUtils.decodeDouble(new byte[]
                   {0, 0, 0, 0, 0, 0, 0, (byte)0xFF}), 0);
	assertEquals("Decoding double from bytes stream", 3.22526E-319d,
				 DataUtils.decodeDouble(new byte[]
                   {0, 0, 0, 0, 0, 0, (byte)0xFF, 0}), 0);
	assertEquals("Decoding double from bytes stream", 8.256667E-317d,
				 DataUtils.decodeDouble(new byte[]
                   {0, 0, 0, 0, 0, (byte)0xFF, 0, 0}), 0);
	assertEquals("Decoding double from bytes stream", 2.113706745E-314d,
				 DataUtils.decodeDouble(new byte[]
                   {0, 0, 0, 0, (byte)0xFF, 0, 0, 0}), 0);
	assertEquals("Decoding double from bytes stream", 5.41108926696E-312d,
				 DataUtils.decodeDouble(new byte[]
                   {0, 0, 0, (byte)0xFF, 0, 0, 0, 0}), 0);
	assertEquals("Decoding double from bytes stream", 1.38523885234213E-309d,
				 DataUtils.decodeDouble(new byte[]
                   {0, 0, (byte)0xFF, 0, 0, 0, 0, 0}), 0);
	assertEquals("Decoding double from bytes stream", 7.06327445644526E-304d,
				 DataUtils.decodeDouble(new byte[]
                   {0, (byte)0xFF, 0, 0, 0, 0, 0, 0}), 0);
	assertEquals("Decoding double from bytes stream", -5.4861240687936887E303d,
				 DataUtils.decodeDouble(new byte[]
                   {(byte)0xFF, 0, 0, 0, 0, 0, 0, 0}), 0);
  } // end of testDecodeDouble1374008726(byte[])

  /**
   * Method for testing original source method:
   * float decodeFloat(byte[])
   * from tested class
   */
  public void testDecodeFloat1374008726()
  {
	assertEquals("Decoding float from bytes stream", 1.4E-45f,
				 DataUtils.decodeFloat(new byte[]{0, 0, 0, 1}), 0);
	assertEquals("Decoding float from bytes stream", 3.57E-43f,
				 DataUtils.decodeFloat(new byte[]{0, 0, 0, (byte)0xFF}), 0);
	assertEquals("Decoding float from bytes stream", 9.147676375112406E-41f,
				 DataUtils.decodeFloat(new byte[]{0, 0, (byte)0xFF, 0}), 0);
	assertEquals("Decoding float from bytes stream", 2.3418052E-38f,
				 DataUtils.decodeFloat(new byte[]{0, (byte)0xFF, 0, 0}), 0);
	assertEquals("Decoding float from bytes stream", -1.7014118E38f,
				 DataUtils.decodeFloat(new byte[]{(byte)0xFF, 0, 0, 0}), 0);
  } // end of testDecodeFloat1374008726(byte[])

  /**
   * Method for testing original source method:
   * int decodeInt(byte[])
   * from tested class
   */
  public void testDecodeInt1374008726()
  {
	assertEquals("Decoding short from bytes stream", 255,
				 DataUtils.decodeInt(new byte[]{0, 0, 0, (byte)0xFF}));
	assertEquals("Decoding short from bytes stream", 65280,
				 DataUtils.decodeInt(new byte[]{0, 0, (byte)0xFF, 0}));
	assertEquals("Decoding short from bytes stream", 16711680,
				 DataUtils.decodeInt(new byte[]{0, (byte)0xFF, 0, 0}));
	assertEquals("Decoding short from bytes stream", -16777216,
				 DataUtils.decodeInt(new byte[]{(byte)0xFF, 0, 0, 0}));
  } // end of testDecodeInt1374008726(byte[])

  /**
   * Method for testing original source method:
   * short decodeShort(byte[])
   * from tested class
   */
  public void testDecodeShort1374008726()
  {
	assertEquals("Decoding short from bytes stream", 255,
				 DataUtils.decodeShort(new byte[]{0, (byte)0xFF}));
	assertEquals("Decoding short from bytes stream", -256,
				 DataUtils.decodeShort(new byte[]{(byte)0xFF, 0}));
  } // end of testDecodeShort1374008726(byte[])

  /**
   * Method for testing original source method:
   * int unsigByteToInt(byte)
   * from tested class
   */
  public void testUnsigByteToInt3039496()
  {
	assertEquals("Is negative byte converted correcly:",
				 255, DataUtils.unsigByteToInt((byte)0xFF));
	assertEquals("Is negative byte converted correcly:",
				 (int)1, DataUtils.unsigByteToInt((byte)1));
  } // end of testUnsigByteToInt3039496(byte)

} // end of DataUtilsTest
