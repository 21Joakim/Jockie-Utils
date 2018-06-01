package com.jockie.bot.core.data.impl;

public class DataObjectImpl<Type> extends DataImpl<Type, DataObjectImpl<Type>> {
	
	private Type object;
	
	public DataObjectImpl(Class<Type> clazz, String name) {
		super(clazz, name);
	}
	
	public DataObjectImpl(Class<Type> clazz) {
		this(clazz, null);
	}
	
	public Type getObject() {
		return this.object;
	}
	
	public void setObject(Type type) {
		this.object = type;
	}
	
	public Type save() {
		return this.object;
	}
	
	public void load(Type type) {
		this.object = type;
	}
	
	public DataObjectImpl<Type> self() {
		return this;
	}
}