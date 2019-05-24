package com.jockie.bot.core.command.factory;

import java.lang.reflect.Method;

import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.IMethodCommand;

public interface IMethodCommandFactory<T extends IMethodCommand> {
	
	/**
	 * Get a suitable name for this command method, an optional
	 * name can be provided which will be used if a name can
	 * not be found
	 * 
	 * @param defaultName a default name to use if a suitable name can not be found
	 * @param method the command method to get the name for
	 * 
	 * @return a suitable name for this command method
	 */
	public static String getName(String defaultName, Method method) {
		Command command = method.getAnnotation(Command.class);
		if(command != null) {
			return command.value().length() == 0 ? (defaultName != null ? defaultName : "") : command.value();
		}
		
		return defaultName != null ? defaultName : "";
	}
	
	/**
	 * Create a method command
	 * 
	 * @param method the method this command would be based of
	 * @param name the suggested name for this method command
	 * @param invoker the invoker for this command, used when executing the command (method)
	 * 
	 * @return the created method command
	 */
	public T create(Method method, String name, Object invoker);
	
	/**
	 * Create a method command
	 * 
	 * @param method the method this command would be based of
	 * @param invoker the invoker for this command, used when executing the command (method)
	 * 
	 * @return the created method command
	 */
	public default T create(Method method, Object invoker) {
		return this.create(method, null, invoker);
	}
}