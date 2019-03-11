package com.jockie.bot.core.command.factory;

import java.lang.reflect.Method;

import com.jockie.bot.core.command.impl.MethodCommand;

public interface IMethodCommandFactory<T extends MethodCommand> {
	
	public T create(String name, Method method, Object invoker);
	
	public T create(Method method, Object invoker);
	
}