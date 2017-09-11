/**
 * 
 */
package com.jeesuite.admin.model;

/**
 * @description <br>
 * @author <a href="mailto:vakinge@gmail.com">vakin</a>
 * @date 2016年12月13日
 */
public class KeyValuePair {

	private String key;
	private Object value;
	public KeyValuePair() {}
	
	public KeyValuePair(String key, Object value) {
		super();
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
	
}
