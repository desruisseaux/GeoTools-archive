DB2 Spatial Data Source Implementation for Geotools

**  Build Issues  **

The DB2 spatial data plug-in requires jars that are non-distributable by
the Geotools team.  These jars are the db2jcc.jar and db2jcc_license.jar.
We do provide a db2jcc_dummy-8.2.1.jar that contains stub classes
to allow the DB2 data source to build, however the real jars will be required
to run anything that uses the data source.

You can either just include these jars in your runtime or copy the jars to you
Maven repository under the db2/jars directory.  If you choose the second
option, you can make maven use the real jars over the stub jar by commenting out this:

<!-- Use this when you dont have db2jcc.jar and db2jcc_license.jar -->
    <dependency>
      <id>db2</id>
      <version>8.2</version>
      <jar>db2jcc_dummy-8.2.1.jar</jar>
      <url>http://www.software.ibm.com/data/db2</url>
       <properties>
        <relese.bundle>true</relese.bundle>
      </properties>
    </dependency>


in the project.xml file and uncommenting this:

 <!-- Commented out so we can build using the dummy spatial jar
    <dependency>
      <id>db2</id>
      <version>8.2</version>
      <jar>db2jcc.jar</jar>
      <url>http://www.software.ibm.com/data/db2</url>
       <properties>
        <relese.bundle>true</relese.bundle>
      </properties>
    </dependency>
    <dependency>
      <id>db2license</id>
      <version>8.2</version>
      <jar>db2jcc_license_cu.jar</jar>
      <url>http://www.software.ibm.com/data/db2</url>
       <properties>
        <relese.bundle>true</relese.bundle>
      </properties>
    </dependency>
-->

This is also required for running the tests.

**  Running Tests **

Setup the geotools database using the scripts in the
\db2\test\setup directory using a DB2 command window and
the following commands:
db2 -tvf setupdb.db2
db2 -tvf import-roads.db2
db2 -tvf import-places.db2

Modify \db2\test\org\geotools\data\db2\db2test.properties to use
the appropriate connection information for your geotools database.

Copy projectlocaldb.xml over project.xml.  Use maven to run tests.


**  Further Help **
If you are still having problems getting the DB2 Data Source
to work, ask a question via geotools-devel@lists.sourceforge.net