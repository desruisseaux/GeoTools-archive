Oracle DataStore Experiment

This module is an experiment:
- it is not included in the build pending license issues surrounding oracle drivers and sdoapi
- it represents a different approach to making a datastore (that may not succeed)

You are warned, but you are also invited to help out...

**  Build Issues  **

The Oracle spatial data source requires jars that are non-distributable by 
the Geotools team.

These jars are:
- ojdbc14.jar
- sodapi.jar

To place the real jars into your maven repository:

#1 Download from oracle
- http://www.oracle.com/technology/software/tech/java/sqlj_jdbc/htdocs/jdbc_10201.html
  (or found in C:\oracle\product\10.2.0\jdbc\lib)
#2 Place into your maven 2 repository
mvn install:install-file -Dfile=ojdbc14.jar -DgroupId=com.oracle -DartifactId=ojdbc14 -Dversion=10.2.0 -Dpackaging=jar

#3 Aquire sdoapi.jar
- http://www.oracle.com/technology/software/products/spatial/htdocs/xplatformsoft.html 

#4 Place into your maven 2 repository
- C:\oracle\spatial>mvn install:install-file -Dfile=sdoapi.jar -DgroupId=com.oracle -DartifactId=sdoapi -Dversion=10.2.0 -Dpackaging=jar

**  Running Tests **

Firstly, you must setup an Oracle database with spatial support and load
the tables and data found in:
- tests/unit/testData/testData.sql.

Tests are run with a maven 2 profile, the following can be placed in your settings.xml file to indicate the oracle database to test against:
...TBA...

The project pom.xml file contains a profile that will run tests, to use this:
- mvn install -DXXXXX

**  Further Help **

If you are still having problems getting the Oracle Data Store
to work, contact Jody Garnett at jgarnett@refractions.net.
