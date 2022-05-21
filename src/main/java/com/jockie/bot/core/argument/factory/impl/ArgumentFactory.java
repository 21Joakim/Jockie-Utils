package com.jockie.bot.core.argument.factory.impl;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.argument.factory.IArgumentFactory;

/*
 * TODO: Figure out a way to move this to the CommandListener.
 * 
 * See the comment in ContextManagerFactory
 */
public class ArgumentFactory {
	
	private ArgumentFactory() {};
	
	/**
	 * The default argument factory, {@link ArgumentFactoryImpl}
	 */
	public static final ArgumentFactoryImpl DEFAULT = new ArgumentFactoryImpl();
	
	private static IArgumentFactory defaultArgumentFactory = DEFAULT;
	
	/**
	 * Set the default argument factory
	 * 
	 * @param factory the factory to set the default to, if null {@link #DEFAULT}
	 */
	public static void setDefault(@Nullable IArgumentFactory factory) {
		ArgumentFactory.defaultArgumentFactory = Objects.requireNonNullElse(factory, DEFAULT);
	}
	
	/**
	 * @return the default argument factory, if this has not been set
	 * it will be {@link #DEFAULT}
	 */
	@Nonnull
	public static IArgumentFactory getDefault() {
		return ArgumentFactory.defaultArgumentFactory;
	}
}