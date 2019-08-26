/**
 * 
 */
package com.common.utils;

/**
 * @author Administrator
 *
 */
public enum AppEnabledEnum {
	disabled("0"),
	enabled("1");
	
	
	private final String code;
	private AppEnabledEnum(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
}
