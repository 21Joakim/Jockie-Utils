package com.jockie.bot.core.command.factory;

import java.lang.reflect.Method;

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
	public IArgument<?>[] createArguments(Method method);
	
	/**
	 * Method used for generating options for a method based command
	 * 
	 * @param method the method to generate the options from
	 * 
	 * @return the generated options
	 */
	public IOption<?>[] createOptions(Method method);
	
}