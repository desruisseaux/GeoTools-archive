#!/bin/bash
# deploy_dependencies.sh: a quick hack to deploy those nasty dependencies
# to your repository.  
# Note: for WebDAV use, please see:
# http://docs.codehaus.org/display/MAVENUSER/Deploying+3rd+Party+Jars+With+WebDAV
# until maven-2.0.5 is released.

REPO_URL='http://lists.refractions.net/m2'
REPO_REMOTE='dav:http://lists.refractions.net/m2'
REPO_ID='refractions'
REPO_LOCAL='/cygdrive/c/Documents and Settings/Refractions/.m2/repository'

if [ ! -e "$REPO_LOCAL" ]; then
  echo
  echo "Please edit this script and redefine the REPO_LOCAL variable on line 11"
  exit
fi

START_DIR=`pwd`
OVERWRITE_MODE="FALSE"
if [ "$1" == "overwrite" ]; then
  OVERWRITE_MODE="TRUE"
else
  echo "DEPLOY_DEPENDENCIES.SH"
  echo " to overwrite jars and poms in the target repository, use ./deploy_dependencies.sh overwrite" 
  echo
fi

deploy_file()
#group, artifact, version
{
  if [ -e "$REPO_LOCAL/$1/$2/$3/$2-$3.pom" ]; then
    cd "$REPO_LOCAL/$1/$2/$3"
    DEPLOY_CMD="mvn deploy:deploy-file -Durl=$REPO_REMOTE -DrepositoryId=$REPO_ID -DpomFile=$2-$3.pom -Dfile=$2-$3.jar -DuniqueVersion=false"
    POM_EXISTS=`curl -Is "$REPO_URL/$1/$2/$3/$2-$3.pom" | grep 404`
    if [ "$POM_EXISTS" == "HTTP/1.1 404 Not Found" ] || [ "$OVERWRITE_MODE" == "TRUE" ]; then
      echo '$DEPLOY_CMD'
      $DEPLOY_CMD
    else
      echo "SKIPPED: $REPO_URL/$1/$2/$3/$2-$3.pom"
    fi
  else
    if [ -e "$REPO_LOCAL/$1/$2/$3/$2-$3.jar" ]; then
      cd "$REPO_LOCAL/$1/$2/$3"
      echo "UPLOADING AND CREATING DEFAULT POM FOR: $1/$2/$3/$2-$3.pom"
      PRE_GRP='my $x='"'$1';"'$x=~s/\//./g;print $x;'
      GROUPID=`perl -e "$PRE_GRP"`
      DEPLOY_CMD="mvn deploy:deploy-file -Durl=$REPO_REMOTE -DrepositoryId=$REPO_ID -DgroupId=$GROUPID -DartifactId=$2 -Dversion=$3 -Dpackaging=jar -Dfile=$2-$3.jar -DuniqueVersion=false"
      JAR_EXISTS=`curl -Is "$REPO_URL/$1/$2/$3/$2-$3.jar" | grep 404`
      if [ "$JAR_EXISTS" == "HTTP/1.1 404 Not Found" ] || [ "$OVERWRITE_MODE" == "TRUE" ]; then
        echo "$DEPLOY_CMD"
        $DEPLOY_CMD
      else
        echo "SKIPPED: $REPO_URL/$1/$2/$3/$2-$3.jar"
      fi
    else 
      echo "MISSING: $REPO_LOCAL/$1/$2/$3/$2-$3.jar"
    fi
  fi
}

#do we want to deploy all dependencies (even ones on ibiblio)?

#geotools 2.2.x
echo
echo "DEPLOYING GEOTOOLS-2.2.x DEPENDENCIES"
echo
deploy_file org/opengis geoapi 2.0
deploy_file javax/units jsr108 0.01
deploy_file java3d vecmath 1.3.1
deploy_file com/vividsolutions jts 1.7.1
deploy_file batik batik-transcoder 1.5
deploy_file batik batik-svggen 1.5
deploy_file batik batik-awt-util 1.5
deploy_file jdom jdom 1.0
deploy_file oro oro 2.0.8
deploy_file log4j log4j 1.2.6
deploy_file commons-logging commons-logging 1.0.4
deploy_file commons-lang commons-lang 2.1
deploy_file velocity velocity 1.4
deploy_file org/postgis postgis-driver 1.0
deploy_file postgresql postgresql 8.1-407.jdbc3
deploy_file mysql mysql-connector-java 3.0.10
deploy_file hsqldb hsqldb 1.8.0.1
deploy_file org/openplans spatialdb 0.1
deploy_file junit junit 3.8.1
deploy_file com/mockrunner mockrunner 0.3.6

#geotools trunk (dupes commented out)
echo
echo "DEPLOYING GEOTOOLS-TRUNK DEPENDENCIES"
echo
#deploy_file org/opengis geoapi ??? (leave out until version stabilizes)
#deploy_file javax/units jsr108 0.01
#deploy_file java3d vecmath 1.3.1
#deploy_file com/vividsolutions jts 1.7.1
#deploy_file batik batik-transcoder 1.5
#deploy_file batik batik-svggen 1.5
#deploy_file batik batik-awt-util 1.5
#deploy_file jdom jdom 1.0
#deploy_file oro oro 2.0.8
#deploy_file log4j log4j 1.2.6
#deploy_file commons-lang commons-lang 2.1
deploy_file commons-collections commons-collections 2.1
deploy_file commons-pool commons-pool 1.2
#deploy_file commons-logging commons-logging 1.0.4
#deploy_file velocity velocity 1.4
#deploy_file org/postgis postgis-driver 1.0
#deploy_file postgresql postgresql 8.1-407.jdbc3
#deploy_file mysql mysql-connector-java 3.0.10
#deploy_file hsqldb hsqldb 1.8.0.1
#deploy_file org/openplans spatialdb 0.1
#deploy_file com/esri jsde_sdk 9.0
#deploy_file com/esri jsde_concurrent 9.0
#deploy_file com/esri jsde_jpe_sdk 9.0
#deploy_file junit junit 3.8.1
#deploy_file com/mockrunner mockrunner 0.3.6

#udig trunk
echo
echo "DEPLOYING UDIG-TRUNK DEPENDENCIES"
echo
deploy_file org/opengis geoapi 2.0-tiger
#deploy_file commons-lang commons-lang 2.1
#deploy_file jdom jdom 1.0
deploy_file log4j log4j 1.2.8
#deploy_file com/vividsolutions jts 1.7.1
deploy_file org/opengis geoapi-legacy 0.1
#deploy_file org/postgis postgis-driver 1.0
#deploy_file mysql mysql-connector-java 3.0.10
deploy_file units units 0.01
#deploy_file java3d vecmath 1.3.1
deploy_file org/wkb4j wkb4j 1.0-RC1
deploy_file com/oracle dummy_spatial 8.1.8
deploy_file org/picocontainer picocontainer 1.2
#deploy_file com/esri jsde_sdk 9.0
#deploy_file com/esri jsde_concurrent 9.0
#deploy_file com/esri jsde_jpe_sdk 9.0
#deploy_file hsqldb hsqldb 1.8.0.1
deploy_file xerces xerces 2.4.0
#deploy_file org/openplans spatialdb 0.1

#others
echo
echo "DEPLOYING DEPENDENCIES FOR OTHER PROJECTS"
echo
deploy_file com/vividsolutions jts 1.6
#deploy_file postgresql postgresql 74.213
#deploy_file mysql mysql-connector-java 3.0.9
deploy_file org/hibernate hibernate 3.2.0.cr1
deploy_file org/hibernate hibernate-annotations 3.1beta9
deploy_file org/hibernate ejb3-persistence 1.0
#deploy_file velocity velocity 1.3
deploy_file org/geoserver preference_core 0.4
deploy_file edu/oswego concurrent 1.3.4

deploy_file gr/spinellis umlgraph 4.4-SNAPSHOT


cd "$START_DIR"
