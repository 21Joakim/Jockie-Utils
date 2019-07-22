package com.jockie.bot.core.argument.factory.impl;

import com.jockie.bot.core.argument.factory.IArgumentFactory;

/*
 * TODO: Figure out a way to move this to the CommandListener.
 * 
 * See the comment in ContextManagerFactory
 */
public class ArgumentFactory {
	
	private ArgumentFactory() {};
	
	public static final ArgumentFactoryImpl DEFAULT = new ArgumentFactoryImpl();
	
	private static IArgumentFactory defaultArgumentFactory = DEFAULT;
	
	public static void setDefault(IArgumentFactory factory) {
		if(factory != null) {
			ArgumentFactory.defaultArgumentFactory = factory;
		}else{
			ArgumentFactory.defaultArgumentFactory = DEFAULT;
		}
	}
	
	public static IArgumentFactory getDefault() {
		return ArgumentFactory.defaultArgumentFactory;
	}
}