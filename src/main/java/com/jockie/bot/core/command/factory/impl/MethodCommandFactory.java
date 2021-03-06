package com.jockie.bot.core.command.factory.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.command.factory.IMethodCommandFactory;

/*
 * TODO: Figure out a way to move this to the CommandListener.
 * 
 * See the comment in ContextManagerFactory
 */
public class MethodCommandFactory {
	
	private MethodCommandFactory() {};
	
	/**
	 * The default method command factory, {@link MethodCommandFactoryImpl}
	 */
	public static final MethodCommandFactoryImpl DEFAULT = new MethodCommandFactoryImpl();
	
	private static IMethodCommandFactory<?> defaultCommandFactory = DEFAULT;
	
	/**
	 * Set the default method command factory
	 * 
	 * @param factory the factory to set the default to, if null {@link #DEFAULT}
	 */
	public static void setDefault(@Nullable IMethodCommandFactory<?> factory) {
		if(factory != null) {
			MethodCommandFactory.defaultCommandFactory = factory;
		}else{
			MethodCommandFactory.defaultCommandFactory = DEFAULT;
		}
	}
	
	/**
	 * @return the default method command factory, if this has not been set
	 * it will be {@link #DEFAULT}
	 */
	@Nonnull
	public static IMethodCommandFactory<?> getDefault() {
		return MethodCommandFactory.defaultCommandFactory;
	}
}