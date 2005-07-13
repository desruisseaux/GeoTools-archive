package org.geotools.renderer.shape;

import junit.framework.TestCase;

public class ScreenMapTest extends TestCase {

	/*
	 * Test method for 'org.geotools.renderer.shape.ScreenMap.set(int, int)'
	 */
	public void testSet() {
		ScreenMap map=new ScreenMap(8,8);
		assertTrue(map.pixels.length*32>8*8);
		for( int x=0; x<8; x++ ){
			for( int y=0; y<8; y++ ){
				assertEquals(false, map.get(x,y));
			}
		}
		setOne(map, 0, 0, true);
		setOne(map, 0, 0, false);
		setOne(map, 3, 4, true);
		setAll(map, true);		
		setAll(map, false);
	}

	private void setOne(ScreenMap map, int xconst, int yconst, boolean bool) {
		map.set(xconst,yconst, bool);
		for( int x=0; x<8; x++ ){
			for( int y=0; y<8; y++ ){
				if( x==xconst && y==yconst)
					assertEquals(bool, map.get(x,y));
				else
					assertEquals(false, map.get(x,y));
			}
		}
	}

	private void setAll(ScreenMap map, boolean value) {
		for( int x=0; x<8; x++ ){
			for( int y=0; y<8; y++ ){
				map.set(x,y,value);
			}
		}
		for( int x=0; x<8; x++ ){
			for( int y=0; y<8; y++ ){
				assertEquals(value, map.get(x,y));
			}
		}
	}

}
