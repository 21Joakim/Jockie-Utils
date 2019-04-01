package com.jockie.bot.core.argument.parser;

public class ParsedArgument<Type> {
	
	private boolean valid;
	
	private Type object;
	
	private String contentLeft;
	
	public ParsedArgument() {
		this(false, null);
	}
	
	public ParsedArgument(Type object) {
		this(object, null);
	}
	
	public ParsedArgument(Type object, String contentLeft) {
		this((object != null) ? true : false, object, contentLeft);
	}
	
	public ParsedArgument(boolean valid, Type object) {
		this(valid, object, null);
	}
	
	public ParsedArgument(boolean valid, Type object, String contentLeft) {
		this.valid = valid;
		this.object = object;
		this.contentLeft = contentLeft;
	}
	
	public boolean isValid() {
		return this.valid;
	}
	
	public Type getObject() {
		return this.object;
	}
	
	public String getContentLeft() {
		return this.contentLeft;
	}
}