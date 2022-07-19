package com.jockie.bot.core.parser;

import javax.annotation.Nullable;

public class ParsedResult<Type> {
	
	protected final boolean valid;
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
}