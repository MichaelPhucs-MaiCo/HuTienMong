package com.vanphuc.utils;

public class S {
    public static String d(String s, String k) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            b.append((char) (s.charAt(i) ^ k.charAt(i % k.length())));
        }
        return b.toString();
    }
}