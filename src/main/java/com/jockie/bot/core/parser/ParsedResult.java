package com.jockie.bot.core.parser;

import java.util.Objects;

import javax.annotation.Nullable;

/* 
 * TODO: Add the reason it was invalid, probably in form on an exception,
 * this can then be used to further process later down the line, perhaps
 * it can be used in the ErrorManager for the developer to provide a better
 * error to the user.
 */
public class ParsedResult<Type> {
	
	@SuppressWarnings("rawtypes")
	protected static final ParsedResult INVALID_PARSED_RESULT = new ParsedResult<>(false, null);
	
	@SuppressWarnings("unchecked")
	public static <T> ParsedResult<T> invalid() {
		return (ParsedResult<T>) INVALID_PARSED_RESULT;
	}
	
	public static <T> ParsedResult<T> valid(T value) {
		return new ParsedResult<>(true, value);
	}
	
	public static <T> ParsedResult<T> valid(T value, String contentLeft) {
		return new ParsedResult<>(true, value, contentLeft);
	}
	
	protected final boolean valid;
	/* 
	 * TODO: Add support for multiple values,
	 * this could be done together with a multi-value handler
	 * and a multi-value policy, like, USE_FIRST, FAIL
	 */
	protected final Type object;
	protected final String contentLeft;
	
	public ParsedResult() {
		this(false, null);
	}
	
	public ParsedResult(Type object) {
		this(object, null);
	}
	
	public ParsedResult(Type object, String contentLeft) {
		this(object != null, object, contentLeft);
	}
	
	public ParsedResult(boolean valid, Type object) {
		this(valid, object, null);
	}
	
	public ParsedResult(boolean valid, Type object, String contentLeft) {
		this.valid = valid;
		this.object = object;
		this.contentLeft = contentLeft;
	}
	
	/**
	 * @return whether or not the argument was <b>valid</b>
	 * (parsed correctly)
	 */
	public boolean isValid() {
		return this.valid;
	}
	
	/**
	 * @return the correctly parsed object, 
	 * if {@link #isValid()} returns <b>false</b>, this will be null
	 */
	@Nullable
	public Type getObject() {
		return this.object;
	}
	
	/**
	 * The content which is left after the argument has been
	 * parsed, this content will be returned back to be parsed.
	 * <br><br>
	 * For this to be used {@link IParser#isHandleAll()} needs
	 * to return <b>true</b>!
	 * 
	 * @return the content which was left after the argument was parsed
	 */
	@Nullable
	public String getContentLeft() {
		return this.contentLeft;
	}
	
	@Override
	public String toString() {
		return String.format("ParsedResult{valid=%s, object=%s, contentLeft=%s}", this.valid, Objects.toString(this.object), this.contentLeft);
	}
}