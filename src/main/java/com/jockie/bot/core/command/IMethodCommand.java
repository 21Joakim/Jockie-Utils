package com.jockie.bot.core.command;

import java.lang.reflect.Method;

public interface IMethodCommand extends ICommand {
	
	public Method getCommandMethod();
	
	public Object getCommandInvoker();
	
}