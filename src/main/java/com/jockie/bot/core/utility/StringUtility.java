package com.jockie.bot.core.utility;

import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.dv8tion.jda.internal.utils.Checks;

public class StringUtility {
	
	private StringUtility() {}
	
	/**
	 * Removes all leading spaces from the provided string
	 * 
	 * @param string the String to remove the leading spaces from
	 * 
	 * @return the stripped String
	 */
	@Nonnull
	public static String stripLeading(@Nonnull String string) {
		Checks.notNull(string, "string");
		
		int index = -1;
		while(++index < string.length() && string.charAt(index) == ' ');
		
		return string.substring(index);
	}
	
	/**
	 * Removes all trailing spaces from the provided string
	 * 
	 * @param string the String to remove the trailing spaces from
	 * 
	 * @return the stripped String
	 */
	@Nonnull
	public static String stripTrailing(@Nonnull String string) {
		Checks.notNull(string, "string");
		
		int index = string.length();
		while(--index >= 0 && string.charAt(index) == ' ');
		
		return string.substring(0, index + 1);
	}
	
	/**
	 * Removes all leading and trailing spaces in the provided string
	 * 
	 * @param string the String to remove the leading and trailing spaces from
	 * 
	 * @return the stripped String
	 */
	@Nonnull
	public static String strip(@Nonnull String string) {
		Checks.notNull(string, "string");
		
		int start = -1;
		while(++start < string.length() && string.charAt(start) == ' ');
		
		int end = string.length();
		while(--end >= 0 && string.charAt(end) == ' ');
		
		return string.substring(start, end + 1);
	}
	
	/**
	 * Unwrap a String by the provided character.
	 * <br><br>
	 * If <b>wrappedString</b> is <b>"hello"</b> and
	 * <b>wrapCharacter</b> is <b>"</b> the result will be
	 * <b>hello</b>.
	 * <br>Any <b>wrapCharacter</b> prefixed by <b>\</b> will have
	 * the backslash removed
	 * 
	 * @param wrappedString the String to unwrap
	 * @param wrapCharacter the character to unwrap by
	 * 
	 * @return the unwrapped String
	 */
	@Nonnull
	public static String unwrap(@Nonnull String wrappedString, char wrapCharacter) {
		Checks.notNull(wrappedString, "wrappedString");
		
		if(wrappedString.charAt(0) != wrapCharacter) {
			throw new IllegalArgumentException("wrappedString does not start with the wrapCharacter character");
		}
		
		if(wrappedString.charAt(wrappedString.length() - 1) != wrapCharacter) {
			throw new IllegalArgumentException("wrappedString does not end with the wrapCharacter character");
		}
		
		return wrappedString.substring(1, wrappedString.length() - 1)
			.replace("\\" + wrapCharacter, String.valueOf(wrapCharacter));
	}
	
	/**
	 * Unwrap a String by the provided start and end character.
	 * <br><br>
	 * If <b>wrappedString</b> is <b>1hello2</b> and
	 * <b>wrapStart</b> is <b>1</b> and <b>wrapEnd</b> is <b>2</b>
	 * the result will be <b>hello</b>.
	 * <br>Any <b>wrapStart</b> or <b>wrapEnd</b> prefixed by <b>\</b> will have
	 * the backslash removed
	 * 
	 * @param wrappedString the String to unwrap
	 * @param wrapStart the character to start the unwrap with
	 * @param wrapEnd the character to end the unwrap with
	 * 
	 * @return the unwrapped String
	 */
	@Nonnull
	public static String unwrap(@Nonnull String wrappedString, char wrapStart, char wrapEnd) {
		Checks.notNull(wrappedString, "wrappedString");
		
		if(wrappedString.charAt(0) != wrapStart) {
			throw new IllegalArgumentException("wrappedString does not start with the wrapStart character");
		}
		
		if(wrappedString.charAt(wrappedString.length() - 1) != wrapEnd) {
			throw new IllegalArgumentException("wrappedString does not end with the wrapEnd character");
		}
		
		return wrappedString.substring(1, wrappedString.length() - 1)
			.replace("\\" + wrapStart, String.valueOf(wrapStart))
			.replace("\\" + wrapEnd, String.valueOf(wrapEnd));
	}
	
	/**
	 * Parse a wrapped String and get what is inside of it
	 * 
	 * @param wrappedString the wrapped String to parse
	 * @param wrapCharacter the start and end character which it is wrapped by
	 * 
	 * @return everything inside of the wrapped String from wrapCharacter to the next wrapCharacter,
	 * if more content is provided it will be discarded
	 */
	@Nullable
	public static String parseWrapped(@Nonnull String wrappedString, char wrapCharacter) {
		return StringUtility.parseWrapped(wrappedString, wrapCharacter, wrapCharacter);
	}
	
	/**
	 * Parse a wrapped String and get what is inside of it
	 * 
	 * @param wrappedString the wrapped String to parse
	 * @param wrapStart the start character which it is wrapped by
	 * @param wrapEnd the end character which it is wrapped by
	 * 
	 * @return everything inside of the wrapped String from wrapStart to wrapEnd,
	 * if more content is provided it will be discarded
	 */
	@Nullable
	public static String parseWrapped(@Nonnull String wrappedString, char wrapStart, char wrapEnd) {
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
	 * Method used to convert a String to a map, for instance 
	 * <br><b>color=#00FFFF name="a cyan role" permissions=8</b>
	 * <br>would be parsed to a map with all the values, like this
	 * <br><b>{color="#00FFFF", name="a cyan role", permissions="8"}</b>
	 * 
	 * @param string the String to parse
	 * 
	 * @return the map containing the parsed values
	 */
	@Nullable
	public static Map<String, String> asMap(@Nonnull String string) {
		Checks.notNull(string, "string");
		
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