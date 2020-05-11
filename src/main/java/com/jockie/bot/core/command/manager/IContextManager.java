package com.jockie.bot.core.command.manager;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.function.BiFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.command.Context;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.utility.function.TriFunction;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.internal.entities.GuildImpl;

public interface IContextManager {
	
	/**
	 * @param event the event to get the context from
	 * @param type the type of the context
	 * 
	 * @return the context of the type specified
	 */
	@Nullable
	public <T> T getContext(@Nonnull CommandEvent event, @Nonnull Type type);
	
	/**
	 * @param event the event to get the context from
	 * @param clazz the type of the context
	 * 
	 * @return the context of the type specified
	 */
	@Nullable
	public default <T> T getContext(@Nonnull CommandEvent event, @Nonnull Class<T> clazz) {
		return this.getContext(event, (Type) clazz);
	}
	
	/**
	 * @param event the event to get the context from
	 * @param parameter the parameter to retrieve the context from
	 * 
	 * @return the context of the type specified
	 */
	@Nullable
	public <T> T getContext(@Nonnull CommandEvent event, @Nonnull Parameter parameter);
	
	/**
	 * @param type the type of the context
	 * 
	 * @return whether or not the specified type is an enforced context, 
	 * this means that it does not require the {@link Context} annotation to be a context
	 */
	public boolean isEnforcedContext(@Nonnull Type type);
	
	/**
	 * @param type the type of the context
	 * @param enforced whether or not the specified type is an enforced context, 
	 * this means that it does not require the {@link Context} annotation to be a context
	 * 
	 * @return the {@link IContextManager} instance, useful for chaining
	 */
	@Nonnull
	public IContextManager setEnforcedContext(@Nonnull Type type, boolean enforced);
	
	/**
	 * @param type the type of the context
	 * 
	 * @return whether or not the specified type should be handled with inheritance, 
	 * this means that it will, for instance, allow {@link Guild} as a context if 
	 * {@link GuildImpl} is registered as one
	 */
	@Nonnull
	public boolean isHandleInheritance(@Nonnull Type type);
	
	/**
	 * @param type the type of the context
	 * @param handle whether or not the specified type should be handled with inheritance, 
	 * this means that it will, for instance, allow {@link Guild} as a context if 
	 * {@link GuildImpl} is registered as one
	 * 
	 * @return the {@link IContextManager} instance, useful for chaining
	 */
	@Nonnull
	public IContextManager setHandleInheritance(@Nonnull Type type, boolean handle);
	
	/**
	 * @param type the type of the context
	 * 
	 * @return the {@link IContextManager} instance, useful for chaining
	 */
	@Nonnull
	public IContextManager unregisterContext(@Nonnull Type type);
	
	/**
	 * @param type the type of the context
	 * @param function the provider function
	 * 
	 * @return the {@link IContextManager} instance, useful for chaining
	 */
	@Nonnull
	public <T> IContextManager registerContext(@Nonnull Type type, @Nonnull TriFunction<CommandEvent, Parameter, Type, T> function);
	
	/**
	 * @param clazz the type of the context
	 * @param function the provider function
	 * 
	 * @return the {@link IContextManager} instance, useful for chaining
	 */
	@Nonnull
	public default <T> IContextManager registerContext(@Nonnull Class<T> clazz, @Nonnull TriFunction<CommandEvent, Parameter, Type, T> function) {
		return this.registerContext((Type) clazz, function);
	}
	
	/**
	 * @param type the type of the context
	 * @param function the provider function
	 * 
	 * @return the {@link IContextManager} instance, useful for chaining
	 */
	@Nonnull
	public <T> IContextManager registerContext(@Nonnull Type type, @Nonnull BiFunction<CommandEvent, Type, T> function);
	
	/**
	 * @param clazz the type of the context
	 * @param function the provider function
	 * 
	 * @return the {@link IContextManager} instance, useful for chaining
	 */
	@Nonnull
	public default <T> IContextManager registerContext(@Nonnull Class<T> clazz, @Nonnull BiFunction<CommandEvent, Type, T> function) {
		return this.registerContext((Type) clazz, function);
	}
}