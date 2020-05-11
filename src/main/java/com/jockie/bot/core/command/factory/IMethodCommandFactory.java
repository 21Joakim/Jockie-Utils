package com.jockie.bot.core.command.factory;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.IMethodCommand;

import net.dv8tion.jda.internal.utils.Checks;

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
	@Nonnull
	public static String getName(@Nullable String defaultName, @Nonnull Method method) {
		Checks.notNull(method, "method");
		
		Command command = method.getAnnotation(Command.class);
		if(command != null && !command.value().isEmpty()) {
			return command.value();
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
	@Nonnull
	public T create(@Nonnull Method method, @Nullable String name, @Nullable Object invoker);
	
	/**
	 * Create a method command
	 * 
	 * @param method the method this command would be based of
	 * @param invoker the invoker for this command, used when executing the command (method)
	 * 
	 * @return the created method command
	 */
	@Nonnull
	public default T create(@Nonnull Method method, @Nullable Object invoker) {
		return this.create(method, null, invoker);
	}
}