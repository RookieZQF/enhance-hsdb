package com.perfma.hotspot.util;

/**
 * @author: ZQF
 * @date: 2021-02-25
 * @description: desc
 */
public class StringUtil {
    private static final String HEX_PREFIX = "0x";

    public static boolean isEmpty(String s){
        return s == null || s.length() == 0;
    }

    public static boolean isAddress(String s){
        return !isEmpty(s) && s.startsWith(HEX_PREFIX);
    }
}
