package com.jockie.bot.core.data.impl;

import com.jockie.bot.core.data.Data;

public abstract class DataImpl<Type> implements Data<Type> {
	
	private Class<Type> clazz;
	
	private String path, name;
	
	public DataImpl(Class<Type> clazz, String name) {
		this.clazz = clazz;
		this.name = name;
	}
	
	public DataImpl<Type> setPath(String path) {
		this.path = path;
		
		return this;
	}
	
	public String getPath() {
		return this.path;
	}
	
	public DataImpl<Type> setName(String name) {
		this.name = name;
		
		return this;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Class<Type> getType() {
		return this.clazz;
	}
}