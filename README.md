# GeoTools archive (2001 - 2012)

The [official GeoTools repository](https://github.com/geotools/geotools) has its first commit in 2011.
But the GeoTools 2 project started in 2001, hosted on CVS followed by Subversion (SVN) before migration to Git.
The original SVN repository is [hosted by OSGeo organization](https://svn.osgeo.org/geotools/) in read-only mode.
This Git repository is a trimmed clone of the SVN repository, archived on GitHub for historical references.
It covers the 10 years period of GeoTools history before project migration to GitHub.

For saving almost 100 Mb of repository size and in some cases for legal safety,
the following files were removed from this clone (non-exhaustive list):

* Maven output directories (`/target/`) and other generated files (e.g. `Thumbs.db`).
* Executable or archive files (`.so`, `.dll`, `.dylib`, `.jnilib`, `.jar`, `.zip`, `.gz`).
* Files duplicating the work of versioning system (`.bak`, `.save`, `.fixme`).
* Resources typically copied from elsewhere (`.gif`, `.wav`, `.ttf`, `.xsd`, `.xsl`, some `.png`).
* Images because they can be large, or are screenshots, or are copies from elsewhere.
* Largest test files.
* EPSG and ESRI data for [licensing reasons](https://epsg.org/terms-of-use.html).
* The `modules/library/opengis` directory, which is a fork of [OGC GeoAPI](https://www.geoapi.org) project.

Commits were not pruned, so above removals should not have any incidence on the amount of commits attributed to developers.
Because of those removals, the code provided by this clone is generally not buildable;
this clone exists only for providing some historical data.
Each commit has a link to the SVN repository hosted at OSGeo together with the SVN commit number.
It should be easy to fetch all data from OSGeo repository if desired.

**Additional historical note:**
The initial version of GeoTools referencing module and some widgets were themselves
ports of an older project which was named [Seagis](http://seagis.sourceforge.net/).
