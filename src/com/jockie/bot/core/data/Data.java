package com.jockie.bot.core.data;

public interface Data<Type> {
	
	public String getPath();
	
	public String getName();
	
	public Class<Type> getType();
	
	public Type save();
	
	public void load(Type type);
	
}