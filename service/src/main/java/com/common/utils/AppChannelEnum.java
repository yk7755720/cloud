/**
 * 
 */
package com.common.utils;

/**
 * @author Administrator
 *
 */
public enum AppChannelEnum {
	OFFICIAL("01", "官方"), 
	HUAWEI("02", "华为"), 
	ALI("03", "阿里"),
	TENGXUN("04", "腾讯"),
	XIAOMI("05", "小米");
	
	private final String code;
	private final String name;
	private AppChannelEnum(String code, String name) {
		this.code = code;
		this.name = name;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getName() {
		return name;
	}
	
	public static String getNameByCode(String code) {
		for(AppChannelEnum channel: AppChannelEnum.values()) {
			if (channel.getCode().equals(code)) {
				return channel.getName();
			}
		}
		return null;
	}
}
