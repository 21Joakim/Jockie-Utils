package com.jockie.bot.core.utility;

import java.util.Map;
import java.util.TreeMap;

public class StringUtility {
	
	private StringUtility() {}
	
	/**
	 * Removes all leading spaces from the provided string
	 * 
	 * @param string the string to remove the leading spaces from
	 * 
	 * @return the result
	 */
	public static String stripLeading(String string) {
		int index = -1;
		while(++index < string.length() && string.charAt(index) == ' ');
		
		return string.substring(index);
	}
	
	/**
	 * Removes all trailing spaces from the provided string
	 * 
	 * @param string the string to remove the trailing spaces from
	 * 
	 * @return the result
	 */
	public static String stripTrailing(String string) {
		int index = string.length();
		while(--index >= 0 && string.charAt(index) == ' ');
		
		return string.substring(0, index + 1);
	}
	
	/**
	 * Removes all leading and trailing spaces in the provided string
	 * 
	 * @param string the string to remove the leading and trailing spaces from
	 * 
	 * @return the result
	 */
	public static String strip(String string) {
		int start = -1;
		while(++start < string.length() && string.charAt(start) == ' ');
		
		int end = string.length();
		while(--end >= 0 && string.charAt(end) == ' ');
		
		return string.substring(start, end + 1);
	}
	
	public static String unwrap(String wrappedString, char wrapCharacter) {
		return wrappedString.substring(1, wrappedString.length() - 1)
			.replace("\\" + wrapCharacter, String.valueOf(wrapCharacter));
	}
	
	public static String unwrap(String wrappedString, char wrapStart, char wrapEnd) {
		return wrappedString.substring(1, wrappedString.length() - 1)
			.replace("\\" + wrapStart, String.valueOf(wrapStart))
			.replace("\\" + wrapEnd, String.valueOf(wrapEnd));
	}
	
	public static String parseWrapped(String wrappedString, char wrapCharacter) {
		return StringUtility.parseWrapped(wrappedString, wrapCharacter, wrapCharacter);
	}
	
	public static String parseWrapped(String wrappedString, char wrapStart, char wrapEnd) {
		if(wrappedString.length() > 0 && wrappedString.charAt(0) == wrapStart) {
			int nextWrap = 0;
			while((nextWrap = wrappedString.indexOf(wrapEnd, nextWrap + 1)) != -1 && wrappedString.charAt(nextWrap - 1) == '\\');
			
			if(nextWrap != -1) {
				return wrappedString.substring(0, nextWrap + 1);
			}
		}
		
		return null;
	}
	
	/**
	 * Method used to convert a string to a map, for instance 
	 * <br><b>color=#00FFFF name="a cyan role" permissions=8</b>
	 * <br>would be parsed to a map with all the values, like this
	 * <br><b>{color="#00FFFF", name="a cyan role", permissions="8"}</b>
	 */
	public static Map<String, String> asMap(String string) {
		Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		while(string.length() > 0) {
			int index = string.indexOf("=");
			if(index == -1) {
				return null;
			}
			
			String key = string.substring(0, index);
			string = string.substring(key.length() + 1);
			
			/* Trim to ignore any spaces between the end of the key and the = */
			key = key.trim();
			
			/* Trim to ignore any spaces between the = and the start of the value */
			string = string.trim();
			
			String value = StringUtility.parseWrapped(string, '"');
			if(value != null) {
				string = string.substring(value.length());
				value = StringUtility.unwrap(value, '"');
			}else{
				value = string.substring(0, (index = string.indexOf(" ")) != -1 ? index : string.length());
				string = string.substring(value.length());
			}
			
			String quotedKey = StringUtility.parseWrapped(key, '"');
			if(quotedKey != null) {
				key = StringUtility.unwrap(quotedKey, '"');
			}else{
				if(key.contains(" ")) {
					return null;
				}
			}
			
			map.put(key, value);
		}
		
		return map;
	}
}