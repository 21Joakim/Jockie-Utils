package com.jockie.bot.core.argument.factory.impl;

import java.lang.reflect.Parameter;

import com.jockie.bot.core.argument.IArgument;

@FunctionalInterface
public interface BuilderConfigureFunction<Type> {
	
	/**
	 * @return whether or not the builder should continue
	 * to be configured after this one.
	 */
	/* TODO: Add a better way of doing this, preferably when they return the builder */
	public default boolean isContinueConfiguration() {
		return true;
	}
	
	/**
	 * Configure an argument builder
	 * 
	 * @param parameter the parameter the builder is being configured for
	 * @param builder the builder to configure
	 * 
	 * @return the builder after it has been configured, this could be the provided builder
	 * or a completely new one
	 */
	public IArgument.Builder<Type, ?, ?> configure(Parameter parameter, IArgument.Builder<Type, ?, ?> builder);
}