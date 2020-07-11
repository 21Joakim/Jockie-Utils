package com.jockie.bot.core.option.factory.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.option.factory.IOptionFactory;

/*
 * TODO: Figure out a way to move this to the CommandListener.
 * 
 * See the comment in ContextManagerFactory
 */
public class OptionFactory {
	
	private OptionFactory() {};
	
	public static final OptionFactoryImpl DEFAULT = new OptionFactoryImpl();
	
	private static IOptionFactory defaultOptionFactory = DEFAULT;
	
	/**
	 * Set the default option factory
	 * 
	 * @param factory the factory to set the default to, if null {@link #DEFAULT}
	 */
	public static void setDefault(@Nullable IOptionFactory factory) {
		if(factory != null) {
			OptionFactory.defaultOptionFactory = factory;
		}else{
			OptionFactory.defaultOptionFactory = DEFAULT;
		}
	}
	
	/**
	 * @return the default option factory, if this has not been set
	 * it will be {@link #DEFAULT}
	 */
	@Nonnull
	public static IOptionFactory getDefault() {
		return OptionFactory.defaultOptionFactory;
	}
}