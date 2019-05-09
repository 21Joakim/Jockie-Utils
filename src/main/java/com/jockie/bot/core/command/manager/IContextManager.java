package com.jockie.bot.core.command.manager;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.function.BiFunction;

import com.jockie.bot.core.Context;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.utility.TriFunction;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.impl.GuildImpl;

public interface IContextManager {
	
	/**
	 * @param event the event to get the context from
	 * @param type the type of the context
	 * 
	 * @return the context of the type specified
	 */
	public <T> T getContext(CommandEvent event, Type type);
	
	/**
	 * @param event the event to get the context from
	 * @param clazz the type of the context
	 * 
	 * @return the context of the type specified
	 */
	public default <T> T getContext(CommandEvent event, Class<T> clazz) {
		return this.getContext(event, (Type) clazz);
	}
	
	/**
	 * @param event the event to get the context from
	 * @param parameter the parameter to retrieve the context from
	 * 
	 * @return the context of the type specified
	 */
	public <T> T getContext(CommandEvent event, Parameter parameter);
	
	/**
	 * @param type the type of the context
	 * 
	 * @return whether or not the specified type is an enforced context, 
	 * this means that it does not require the {@link Context} annotation to be a context
	 */
	public boolean isEnforcedContext(Type type);
	
	/**
	 * @param type the type of the context
	 * @param enforced whether or not the specified type is an enforced context, 
	 * this means that it does not require the {@link Context} annotation to be a context
	 * 
	 * @return the {@link IContextManager} instance, useful for chaining
	 */
	public IContextManager setEnforcedContext(Type type, boolean enforced);
	
	/**
	 * @param type the type of the context
	 * 
	 * @return whether or not the specified type should be handled with inheritance, 
	 * this means that it will, for instance, allow {@link Guild} as a context if 
	 * {@link GuildImpl} is registered as one
	 */
	public boolean isHandleInheritance(Type type);
	
	/**
	 * <b>USE WITH CAUTION</b>: The more of these you register the slower the command execution will become.
	 * 
	 * @param type the type of the context
	 * @param handle whether or not the specified type should be handled with inheritance, 
	 * this means that it will, for instance, allow {@link Guild} as a context if 
	 * {@link GuildImpl} is registered as one
	 * 
	 * @return the {@link IContextManager} instance, useful for chaining
	 */
	public IContextManager setHandleInheritance(Type type, boolean handle);
	
	/**
	 * @param type the type of the context
	 * 
	 * @return the {@link IContextManager} instance, useful for chaining
	 */
	public IContextManager unregisterContext(Type type);
	
	/**
	 * @param type the type of the context
	 * @param function the provider function
	 * 
	 * @return the {@link IContextManager} instance, useful for chaining
	 */
	public <T> IContextManager registerContext(Type type, TriFunction<CommandEvent, Parameter, Type, T> function);
	
	/**
	 * @param clazz the type of the context
	 * @param function the provider function
	 * 
	 * @return the {@link IContextManager} instance, useful for chaining
	 */
	public default <T> IContextManager registerContext(Class<T> clazz, TriFunction<CommandEvent, Parameter, Type, T> function) {
		return this.registerContext((Type) clazz, function);
	}
	
	/**
	 * @param type the type of the context
	 * @param function the provider function
	 * 
	 * @return the {@link IContextManager} instance, useful for chaining
	 */
	public <T> IContextManager registerContext(Type type, BiFunction<CommandEvent, Type, T> function);
	
	/**
	 * @param clazz the type of the context
	 * @param function the provider function
	 * 
	 * @return the {@link IContextManager} instance, useful for chaining
	 */
	public default <T> IContextManager registerContext(Class<T> clazz, BiFunction<CommandEvent, Type, T> function) {
		return this.registerContext((Type) clazz, function);
	}
}