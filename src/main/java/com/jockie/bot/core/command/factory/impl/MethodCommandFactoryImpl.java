package com.jockie.bot.core.command.factory.impl;

import java.lang.reflect.Method;

import com.jockie.bot.core.command.factory.IMethodCommandFactory;
import com.jockie.bot.core.command.impl.CommandImpl;

public class MethodCommandFactoryImpl implements IMethodCommandFactory<CommandImpl> {
	
	public CommandImpl create(Method method, String name, Object invoker) {
		return new CommandImpl(IMethodCommandFactory.getName(name, method), method, invoker);
	}
}