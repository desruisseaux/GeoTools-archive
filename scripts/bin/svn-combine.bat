svn mkdir temp-merging-directory
svn merge $1@$4 $2@$4 temp-combine
svn merge $2@$4 $1@$4 temp-combine
svn rm $1
svn rm $2
svn ci -m "Merging directories"
svn rename temp-merging-directory $3
svn ci -m "Completing the Merge"