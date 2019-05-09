package com.jockie.bot.core.command.factory.impl;

import java.lang.reflect.Method;

import com.jockie.bot.core.command.factory.IMethodCommandFactory;
import com.jockie.bot.core.command.impl.MethodCommand;

public class MethodCommandFactoryImpl implements IMethodCommandFactory<MethodCommand> {
	
	public MethodCommand create(String name, Method method, Object invoker) {
		return MethodCommand.createFrom(name, method, invoker);
	}
	
	public MethodCommand create(Method method, Object invoker) {
		return MethodCommand.createFrom(method, invoker);
	}
}