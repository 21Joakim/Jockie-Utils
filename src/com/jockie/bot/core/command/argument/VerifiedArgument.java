package com.jockie.bot.core.command.argument;

public class VerifiedArgument<Type> {
	
	public enum VerifiedType {
		INVALID,
		VALID,
		VALID_END_NOW;
	}
	
	private VerifiedType type;
	
	private Type object;
	
	private String error = null;
	
	public VerifiedArgument(VerifiedType type, Type object) {
		this.type = type;
		this.object = object;
	}
	
	public VerifiedArgument(String error) {
		this(VerifiedType.INVALID, null);
		
		this.error = error;
	}
	
	public VerifiedType getVerifiedType() {
		return this.type;
	}
	
	public Type getObject() {
		return this.object;
	}
	
	public String getError() {
		return this.error;
	}
}