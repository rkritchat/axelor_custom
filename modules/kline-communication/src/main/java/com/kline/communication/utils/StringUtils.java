package com.kline.communication.utils;

public class StringUtils {
    public static String toString(Object object) {
        try {
            return object.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
