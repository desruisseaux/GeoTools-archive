Scripts
-------

The "project" contains various helper scripts that are useful when working with
the geotools code base.

Java Scripts
------------
GT2Eclipse Generates Eclipse .project and .classpath files based on buildResults.txt
SVNMunch  (SVN Merge crUNCH) Force modules together using a series of svn commands.

While both these tools would be easier to write in Ruby (or Perl) I have put them
together with Java for happy cross platformness.

If you want to add some sh or bat scrips please dump them in the bin directory.
The bin directory contains existing sh/bat scripts to run the java programs based
on the use of the JAVA_HOME env variable.

SH/BAT Scripts
--------------
gt2eclipse.sh/bat
svnmunch.sh/bat

Requirements
------------
SCRIPT_HOME set to location of this project
JAVA_HOME set to your jsdk

Eclipse Requirements
--------------------
MAVEN_REPO "classpath variable" set (using Eclipse Preferences)

You may also wish to place the scripts/bin directory on your classpath for
convience.