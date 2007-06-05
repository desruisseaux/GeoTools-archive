package org.geotools.caching;

import org.geotools.data.DataStore;


public interface IDataCache extends DataStore {

		
		/**
		 */
		public abstract void clear();

			
			/**
			 */
			public abstract void flush()	throws IllegalStateException ;


				
				/**
				 */
				public abstract long getHits();
				
			
		

}
