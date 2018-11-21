package com.jockie.bot.core.argument;

public class VerifiedArgument<Type> {
	
	public enum VerifiedType {
		INVALID,
		VALID,
		VALID_END_NOW;
	}
	
	private VerifiedType type;
	
	private Type object;
	
	public VerifiedArgument() {
		this(VerifiedType.INVALID, null);
	}
	
	public VerifiedArgument(Type object) {
		this((object != null) ? VerifiedType.VALID : VerifiedType.INVALID, object);
	}
	
	public VerifiedArgument(VerifiedType type, Type object) {
		this.type = type;
		this.object = object;
	}
	
	public VerifiedType getVerifiedType() {
		return this.type;
	}
	
	public Type getObject() {
		return this.object;
	}
}