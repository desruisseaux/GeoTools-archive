    GeoTools @VERSION@
    ================================

Thank you for downloading GeoTools @VERSION@.
This release was created on: @DATE@

This release contains only a subset of the current GeoTools codebase.
The full GeoTools codebase is made up of over 30 modules; 
Only the modules which are considered to be stable will be included in this
release. 

For this release the following modules comprise the geotools library:
@REQUIRED@

The following modules comprise the plug-ins and extentions:
@PLUGIN@
@EXTENTION@
@DEMO@

These optional jars typicaly provide support for specific data formats or
sources, or provide specific additional functionality.

You can find out more about each of the major modules by going to:
- http//modules.geotools.org

Each module has its own homepage with full javadoc, test reports and source code.

For more information, and documentation on the geotools library please see
our community wiki at:
- http://docs.codehaus.org/display/GEOTOOLS.

NOTE
====
The details contained in the rest of this file are provided as a quick start
reference.

For full details on requirements and build process please see
the developers' guide at:
- http://docs.codehaus.org/display/GEOTOOLS/Developers+Guide

REQUIREMENTS
============
To use the geotools libraries you will need:
Java 1.4.x or higher.
The Java Advanced Imaging Libraries (JAI)
The Java Advanced Imaging Image I/O Tools

The JAI and the JAI I/O Tools are standard extensions to Java created by SUN.  
They have versions specific to different platforms so we cannot distribute them
as part of the release.

You can obtain the latest versions of both of these from:
- http://java.sun.com/products/java-media/jai/current.html

Detailed instructions can be found at:
- http://docs.codehaus.org/display/GEOTOOLS/Dependencies

ORACLE REQUIREMENTS
===================
In order to use the Oracle module, you need the Oracle JDBC driver
from Oracle. Unlike most external libraries used in GeoTools, we
cannot redistribute this jar.

However, you can obtain them from the Oracle website, free of charge,
after registering:
- http://otn.oracle.com/software/tech/java/sqlj_jdbc/content.html
  
Please see the developers guide for more information.

BUILDING
========
In order to build GeoTools you will need a copy of Maven.  Maven is a project
management tool from the Apache group.  You can obtain a copy of maven from: 
- http://maven.apache.org/start/download.html

At the time of writing, the build process was known to work with the Maven 1.0
release.

Once you have maven installed, you can perform a full build by moving to the
geotools2 folder and typing:

maven build

Once again please read the developers guide for more information about maven:
- http://docs.codehaus.org/display/GEOTOOLS/Using%2BMaven

FUTURE CHANGES
==============
This is a Release Candidate, whilst the API has stabilized, we are are
int the process of removing deprecated classes, and finalizing documentation.

GETTING INVOLVED
================
If you are interested in the future development of GeoTools2 then feel free to
join the geotools-devel mailing list and join in the weekly IRC sessions.

We welcome contributions of new modules as well as keen developers who want to
work on the project as a whole.

For more general queries a a gt2-users mailing list is also available.

You can find out more about the mailing lists and recent news by going to the
GeoTools homepage at:
- http://www.geotools.org

And by reading the GeoTools guide at:
- http://docs.codehaus.org/display/GEOTOOLS/Home

Good luck and many thanks for your interest in GeoTools,

The GeoTools2 Project Management Committee