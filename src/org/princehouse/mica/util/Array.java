package org.princehouse.mica.util;

public class Array {
	public static String[] append(String[] src, String x) {
		String[] dst = new String[src.length+1];
		System.arraycopy(src, 0, dst, 0, src.length);
		dst[src.length] = x;
		return dst;
	}

	public static Object[] append(Object[] src, Object x) {
		Object[] dst = new Object[src.length+1];
		System.arraycopy(src, 0, dst, 0, src.length);
		dst[src.length] = x;
		return dst;
	}
	
	public static String[] prepend(String[] src, String x) {
		String[] dst = new String[src.length+1];
		System.arraycopy(src, 0, dst, 1, src.length);
		dst[0] = x;
		return dst;
	}

	public static Object[] prepend(Object[] src, Object x) {
		Object[] dst = new Object[src.length+1];
		System.arraycopy(src, 0, dst, 1, src.length);
		dst[0] = x;
		return dst;
	}
	
	public static String[] subArray(String[] src, int start) {
		return subArray(src, start, src.length - start);
	}
	
	public static String[] subArray(String[] src, int start, int length) {
		String[] dst = new String[length];
		System.arraycopy(src, start, dst, 0, length);
		return dst;
	}

	public static String join(String bridge, Object[] src) {
		if(src.length == 0) {
			return "";
		} 
		String temp = (src[0] == null ? "null" : src[0].toString());
		
		for(int i = 1; i < src.length; i++) {
			temp += bridge + (src[i] == null ? "null" : src[i].toString());
		}
		return temp;
	}
}
