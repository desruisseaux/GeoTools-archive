/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package test;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.crs.*;
import test.main.JUnit_LoggerFactory;

/** Performs the complete set of tests for completed widgets or widget groups.
 * (Widgets that are worked on are not included.)
 * A failure of this test suite will very likely block any release. */
public class JUnit_GeoWidgets {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for crs");
        //$JUnit-BEGIN$
        suite.addTestSuite(JUnit_LoggerFactory.class);
        suite.addTestSuite(JUnit_CorrectEPSGFunction.class);
        
        suite.addTestSuite(JUnit_UnitModels.class);
        suite.addTestSuite(JUnit_AxisDirectionModel.class);
        suite.addTestSuite(JUnit_CRSModel.class);
        //$JUnit-END$
        return suite;
    }

}
