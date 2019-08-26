package com.common.utils;

import java.util.UUID;

/**
 * @ Purpose:
 * @Package Name: com.dpm.resource.common.utils
 * @Author: liuxiaoxin
 * @Date: 2018/5/15
 */
public class UUIDUtils {
    /**
     * 功能：获取UUID并去除“-”
     */
    public static String getUUID() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("\\-", "");
    }
}
