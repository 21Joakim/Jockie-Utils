package com.jockie.bot.core.command.factory.impl;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.command.IMethodCommand;
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
	
	private static IMethodCommandFactory<? extends IMethodCommand> defaultCommandFactory = DEFAULT;
	
	/**
	 * Set the default method command factory
	 * 
	 * @param factory the factory to set the default to, if null {@link #DEFAULT}
	 */
	public static void setDefault(@Nullable IMethodCommandFactory<? extends IMethodCommand> factory) {
		MethodCommandFactory.defaultCommandFactory = Objects.requireNonNullElse(factory, DEFAULT);
	}
	
	/**
	 * @return the default method command factory, if this has not been set
	 * it will be {@link #DEFAULT}
	 */
	@Nonnull
	public static IMethodCommandFactory<? extends IMethodCommand> getDefault() {
		return MethodCommandFactory.defaultCommandFactory;
	}
}