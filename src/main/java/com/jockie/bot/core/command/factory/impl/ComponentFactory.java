package com.jockie.bot.core.command.factory.impl;

import com.jockie.bot.core.command.factory.IComponentFactory;

/*
 * TODO: Figure out a way to move this to the CommandListener.
 * 
 * See the comment in ContextManagerFactory
 */
public class ComponentFactory {
	
	private ComponentFactory() {};
	
	public static final ComponentFactoryImpl DEFAULT = new ComponentFactoryImpl();
	
	private static IComponentFactory defaultComponentFactory = DEFAULT;
	
	public static void setDefault(IComponentFactory factory) {
		if(factory != null) {
			ComponentFactory.defaultComponentFactory = factory;
		}else{
			ComponentFactory.defaultComponentFactory = DEFAULT;
		}
	}
	
	public static IComponentFactory getDefault() {
		return ComponentFactory.defaultComponentFactory;
	}
}