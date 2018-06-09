package com.jockie.bot.core.data.impl;

import java.io.IOException;

import com.jockie.bot.core.data.Data;

public abstract class DataImpl<Type, ReturnType extends DataImpl<Type, ReturnType>> implements Data<Type> {
	
	private Class<Type> clazz;
	
	private String path, name;
	
	public DataImpl(Class<Type> clazz, String name) {
		this.clazz = clazz;
		this.name = name;
	}
	
	public ReturnType setPath(String path) {
		this.path = path;
		
		return this.self();
	}
	
	public String getPath() {
		return this.path;
	}
	
	public ReturnType setName(String name) {
		this.name = name;
		
		return this.self();
	}
	
	public String getName() {
		return this.name;
	}
	
	public Class<Type> getType() {
		return this.clazz;
	}
	
	public void save() {
		try {
			DataHandler.save(this);
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void load() {
		try {
			DataHandler.load(this);
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public abstract ReturnType self();
}