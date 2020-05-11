package com.jockie.bot.core.command.factory.impl;

import java.lang.reflect.Method;

import com.jockie.bot.core.command.factory.IMethodCommandFactory;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.internal.utils.Checks;

public class MethodCommandFactoryImpl implements IMethodCommandFactory<CommandImpl> {
	
	public CommandImpl create(Method method, String name, Object invoker) {
		Checks.notNull(method, "method");
		
		return new CommandImpl(IMethodCommandFactory.getName(name, method), method, invoker);
	}
}