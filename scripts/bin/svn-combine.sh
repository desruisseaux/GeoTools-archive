#!/bin/sh
#
# author: dzwiers
#
# usage: 
# svncombine dir1 dir2 destdir version
# must be executed from within an svn workspace dir.
#
# assumes svn is installed on commandline
# assumes destdir is not nested (ie. in current dir)
#
if [ -n $4 ]; then
svn mkdir temp-merging-directory             # make temp dir
svn merge $1@$4 $2@$4 temp-merging-directory # direction 1
svn merge $2@$4 $1@$4 temp-merging-directory # direction 2
svn rm $1                                    # remove dir1
svn rm $2                                    # remove dir2
svn ci -m "Merging directories"              # make it so
svn rename temp-merging-directory $3         # move to new name
svn ci -m "Completing the Merge"             # end the merge
fi
