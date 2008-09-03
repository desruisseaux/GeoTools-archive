package org.geotools.feature.iso;

/**
 * Interface providing per-instance user data.
 * 
 * <p>
 * 
 * This interface is present to allow applications to use out-of-band data
 * smuggling to workaround shortcomings in geoapi (e.g. Property).
 * 
 * <p>
 * 
 * Methods similar to these are present in PropertyDescription from geoapi-2.1
 * but missing in Property. Some bits and pieces are added in geoapi-2.2, but
 * different again.
 */
public interface UserData {

    /**
     * Return user data object previously put with key, or null is not set.
     * 
     * @param key
     *                key for object(must observe equals/hashCode contract)
     * @return user data or null if not set
     */
    public Object getUserData(Object key);

    /**
     * Store user data with key.
     * 
     * @param key
     *                key for object (must observe equals/hashCode contract)
     * @param data
     *                user data to be stored.
     */
    public void putUserData(Object key, Object data);

}
