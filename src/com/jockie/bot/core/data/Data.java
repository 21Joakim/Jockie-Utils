package com.jockie.bot.core.data;

public interface Data<Type> {
	
	public String getPath();
	
	public String getName();
	
	public Class<Type> getType();
	
	public Type getSavableData();
	
	public void setLoadableData(Type type);
	
}