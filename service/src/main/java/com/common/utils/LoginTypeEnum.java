/**
 * 
 */
package com.common.utils;

/**
 * @author zongweijun
 *
 */
public enum LoginTypeEnum {
	
	LOGIN_WITH_PASSWORD("01"), 
	LOGIN_WITH_MESSAGE("02"), 
	LOGIN_WITH_WECHART("03");
	
	private final String type;
	private LoginTypeEnum(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
}
