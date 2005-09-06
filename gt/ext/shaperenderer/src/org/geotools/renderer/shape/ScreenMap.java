/**
 * 
 */
package org.geotools.renderer.shape;

public class ScreenMap {
	int[] pixels;
	int width, height;
	public ScreenMap( int x, int y){
		width=x;
		height=y;
		int arraySize=x*y/32+1;
		pixels=new int[arraySize];
	}
	
	public void set(int x, int y, boolean value){
		int bit=bit(x,y);
		int index=bit/32;
		int offset=bit%32;
		int mask=1;
		mask= mask<<offset;
		if( value ){
			pixels[index] = pixels[index]|mask;
		}else{
			int tmp=pixels[index];
			tmp=~tmp;
			tmp=(tmp|mask);
			tmp=~tmp;
			pixels[index]=tmp;
		}
	}

	public boolean get(int x, int y){
		int bit=bit(x,y);
		int index=bit/32;
		int offset=bit%32;
		int mask= 1<<offset;
		try{
		return (pixels[index]&mask)!=0?true:false;
		}catch(Exception e){
			System.out.println(""+x+","+y);
			return false;
		}
	}
	
	private int bit( int x, int y){
		return height*y+x;
	}
	
}