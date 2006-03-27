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

import java.io.IOException;
import org.geotools.data.shapefile.prj.PrjFileReader;
import org.geotools.TestData;


/**
 *
 * @source $URL$
 * @version $Id$
 * @author Ian Schneider
 * @author James Macgill
 */
public class PrjFileTest extends TestCaseSupport {
  
  static final String TEST_FILE = "wkt/cntbnd01.prj";
  
  protected PrjFileReader prj = null;
  
  public PrjFileTest(String testName) throws IOException {
    super(testName);
  }
  
  public static void main(String[] args) {
    verbose = true;
    junit.textui.TestRunner.run(suite(PrjFileTest.class));
  }

  protected void setUp() throws Exception {
    prj = new PrjFileReader(TestData.openChannel(TEST_FILE));
  }

  public void testGeneral() {
    if (verbose) {
      System.out.println("tested");
    }
  }
}
