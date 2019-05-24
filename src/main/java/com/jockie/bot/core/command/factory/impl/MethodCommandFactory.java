package com.jockie.bot.core.command.factory.impl;

import com.jockie.bot.core.command.factory.IMethodCommandFactory;

/*
 * TODO: Figure out a way to move this to the CommandListener.
 * 
 * See the comment in ContextManagerFactory
 */
public class MethodCommandFactory {
	
	private MethodCommandFactory() {};
	
	public static final MethodCommandFactoryImpl DEFAULT = new MethodCommandFactoryImpl();
	
	private static IMethodCommandFactory<?> defaultCommandFactory = DEFAULT;
	
	public static void setDefault(IMethodCommandFactory<?> factory) {
		if(factory != null) {
			MethodCommandFactory.defaultCommandFactory = factory;
		}else{
			MethodCommandFactory.defaultCommandFactory = DEFAULT;
		}
	}
	
	public static IMethodCommandFactory<?> getDefault() {
		return MethodCommandFactory.defaultCommandFactory;
	}
}