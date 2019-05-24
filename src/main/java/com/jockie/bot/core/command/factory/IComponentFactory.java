package com.jockie.bot.core.command.factory;

import java.lang.reflect.Method;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.option.IOption;

public interface IComponentFactory {
	
	public IArgument<?>[] createArguments(Method method);
	
	public IOption[] createOptions(Method method);
	
}