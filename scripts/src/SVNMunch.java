import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Thanks to David Zwiers for the origional shell script.
 */
public class SVNMunch {

public static int exec( String cmd ) throws InterruptedException, IOException{
	Runtime rt = Runtime.getRuntime();
	System.out.println( cmd );	
	Process run = rt.exec( cmd );
	System.out.print("...");System.out.flush();	
	run.waitFor();
	System.out.println( run.exitValue() );	
	return run.exitValue();
}
public static void main(String[] args) throws Exception {
	if( args.length != 4 ) {
		PrintStream out = System.out;
		out.println("usage:"); 
		out.println(" svnmunch dir1 dir2 destdir version");
		out.println("must be executed from within an svn repository dir.");
		out.println("assumes svn is installed on commandline");
		out.println("assumes destdir is not nested (ie. in this dir)");
		System.exit(1);
	}		
	String d1 = args[0];
	String d2 = args[1];
	String dst = args[2];
	String version = args[3];
	
	File dir1 = new File( d1 );
	assert dir1.exists() && dir1.isDirectory();
	
	File dir2 = new File( d2 );
	assert dir1.exists() && dir2.isDirectory();
	
	File dest = new File( dst );
	assert !dest.exists();
	
	File munch = new File( "tmp-"+dir1.getName()+"-"+dir2.getName() );
	assert !munch.exists();
	
	String tmp = munch.getName();
	String dv1 = d1+"@"+version;
	String dv2 = d2+"@"+version;
			
	// make temp dir		
	//	svn mkdir temp-merging-directory
	exec( "svn mkdir "+munch.getName() );
		             
	// direction 1
	//	svn merge $1@$4 $2@$4 temp-merging-directory 
	exec( "svn merge " + dv1+ " "+dv2 + " "+tmp );
	
	// direction 2
	//	svn merge $2@$4 $1@$4 temp-merging-directory 
	exec( "svn merge " + d1+ " "+d2 + " "+tmp );
	
	// remove dir1
	//	svn rm $1
	exec( "svn rm "+d1);
	// remove dir2
	//	svn rm $2
	exec( "svn rm "+d2 );                            
	// make it so        
	//	svn ci -m "Merging directories"
	exec( "svn ci -m \"munch-ing directory "+d1+" and "+d2+"\"" );              
//	svn rename temp-merging-directory $3         # move to new name
	exec( "svn rename "+tmp+" "+dst );
//	svn ci -m "Completing the Merge"             # end the merge
    exec( "svn ci -m \" complete merge to "+dst+"\"" );	
}
}
