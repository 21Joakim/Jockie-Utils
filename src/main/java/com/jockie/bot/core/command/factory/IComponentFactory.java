package com.jockie.bot.core.command.factory;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.option.IOption;

/**
 * Factory used for generating different types of command components, such as arguments and options
 */
public interface IComponentFactory {
	
	/**
	 * Method used for generating arguments for a method based command
	 * 
	 * @param method the method to generate the arguments from
	 * 
	 * @return the generated arguments
	 */
	@Nonnull
	public IArgument<?>[] createArguments(@Nonnull Method method);
	
	/**
	 * Method used for generating options for a method based command
	 * 
	 * @param method the method to generate the options from
	 * 
	 * @return the generated options
	 */
	@Nonnull
	public IOption<?>[] createOptions(@Nonnull Method method);
	
}