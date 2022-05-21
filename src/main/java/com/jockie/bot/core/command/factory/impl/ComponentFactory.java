package com.jockie.bot.core.command.factory.impl;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.command.factory.IComponentFactory;

/*
 * TODO: Figure out a way to move this to the CommandListener.
 * 
 * See the comment in ContextManagerFactory
 */
public class ComponentFactory {
	
	private ComponentFactory() {};
	
	/**
	 * The default component factory, {@link ComponentFactoryImpl}
	 */
	public static final ComponentFactoryImpl DEFAULT = new ComponentFactoryImpl();
	
	private static IComponentFactory defaultComponentFactory = DEFAULT;
	
	/**
	 * Set the default component factory
	 * 
	 * @param factory the factory to set the default to, if null {@link #DEFAULT}
	 */
	public static void setDefault(@Nullable IComponentFactory factory) {
		ComponentFactory.defaultComponentFactory = Objects.requireNonNullElse(factory, DEFAULT);
	}
	
	/**
	 * @return the default component factory, if this has not been set
	 * it will be {@link #DEFAULT}
	 */
	@Nonnull
	public static IComponentFactory getDefault() {
		return ComponentFactory.defaultComponentFactory;
	}
}