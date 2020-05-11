package com.jockie.bot.core.command.manager;

import java.util.function.BiConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.command.impl.CommandEvent;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.internal.entities.GuildImpl;

public interface IReturnManager {
	
	/**
	 * @param event the event where the object was returned
	 * @param object the returned object to handle
	 * 
	 * @return whether or not the provided object was recognized 
	 * and handled
	 */
	public <T> boolean perform(@Nonnull CommandEvent event, @Nullable T object);
	
	/**
	 * @param type the return type to handle
	 * 
	 * @return the {@link IReturnManager} instance, useful for chaining
	 */
	@Nonnull
	public IReturnManager unregisterHandler(@Nonnull Class<?> type);
	
	/**
	 * @param type the return type to handle
	 * @param function the handler function
	 * 
	 * @return the {@link IReturnManager} instance, useful for chaining
	 */
	@Nonnull
	public <T> IReturnManager registerHandler(@Nonnull Class<T> type, @Nonnull BiConsumer<CommandEvent, T> function);
	
	/**
	 * @param type the type of the handler
	 * 
	 * @return whether or not the specified type should be handled with inheritance, 
	 * this means that it will, for instance, handle {@link GuildImpl} if {@link Guild}
	 * was registered
	 */
	public boolean isHandleInheritance(@Nonnull Class<?> type);
	
	/**
	 * @param type the type of the handler
	 * @param handle whether or not the specified type should be handled with inheritance, 
	 * this means that it will, for instance, handle {@link GuildImpl} if {@link Guild}
	 * was registered
	 * 
	 * @return the {@link IReturnManager} instance, useful for chaining
	 */
	@Nonnull
	public IReturnManager setHandleInheritance(@Nonnull Class<?> type, boolean handle);
	
}