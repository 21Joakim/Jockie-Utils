package com.jockie.bot.core.command;

import java.lang.reflect.Method;

import javax.annotation.Nullable;

public interface IMethodCommand extends ICommand {
	
	/**
	 * @return the method which was used in the creation of the command
	 * and will be used to execute it
	 */
	@Nullable
	public Method getCommandMethod();
	
	/**
	 * @return the method invoker object, this would be null if the 
	 * {@link #getCommandMethod()} is a static method
	 */
	@Nullable
	public Object getCommandInvoker();
	
}