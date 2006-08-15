package org.geotools.xml.impl;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class BufferTest extends TestCase {

	Buffer buffer;
	
	protected void setUp() throws Exception {
		buffer = new Buffer(10);
	}
	
	public void test() throws Exception {
		Consumer consumer = new Consumer(buffer);
		Thread thread = new Thread(consumer);
		thread.start();
		
		for (int i = 0; i < 1000; i++) {
			buffer.put(new Integer(i));
		}
		thread.join();
		
		for (int i = 0; i < consumer.taken.size(); i++) {
			Integer integer = (Integer) consumer.taken.get(i);
			assertEquals(i,integer.intValue());
		}
	}
	
	static class Consumer implements Runnable {

		Buffer buffer;
		List taken;
		
		public Consumer(Buffer buffer) {
			this.buffer = buffer;
		}
		
		public void run() {
			taken = new ArrayList();
			for (int i = 0; i < 1000; i++) {
				taken.add(buffer.get());
			}
		}
	}
}
