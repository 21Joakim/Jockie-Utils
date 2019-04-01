package com.jockie.bot.core.command.factory;

import java.lang.reflect.Method;

import com.jockie.bot.core.command.impl.MethodCommand;

public interface IMethodCommandFactory<T extends MethodCommand> {
	
	/**
	 * Create a method command
	 * 
	 * @param name the suggested name for this method command
	 * @param method the method this command would be based of
	 * @param invoker the invoker for this command, used when executing the command (method)
	 * 
	 * @return the created method command
	 */
	public T create(String name, Method method, Object invoker);
	
	/**
	 * Create a method command
	 * 
	 * @param method the method this command would be based of
	 * @param invoker the invoker for this command, used when executing the command (method)
	 * 
	 * @return the created method command
	 */
	public T create(Method method, Object invoker);
	
}