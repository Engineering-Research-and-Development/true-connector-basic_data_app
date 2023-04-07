package it.eng.idsa.dataapp.service;

public interface ThreadService {

	/**
	 * Store value in HashMap in ThreadLocal
	 * 
	 * @param key - key under which value will be stored in HashMap in
	 *               ThreadLocal
	 * @param value - value which will be stored
	 * 
	 */
	void setThreadLocalValue(String key, Object value);

	/**
	 * Get value from HashMap in ThreadLocal based on key
	 * 
	 * @param key - key under which value is stored in HashMap in ThreadLocal
	 * @return value - requested value
	 * 
	 */
	public Object getThreadLocalValue(String key);

	/**
	 * Delete value in HashMap in ThreadLocal
	 * 
	 * @param key - key under which value is stored in HashMap in ThreadLocal
	 * 
	 */
	void removeThreadLocalValue(String key);
}
