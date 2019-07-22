package com.jockie.bot.core.command.manager;

import java.util.function.BiConsumer;

import com.jockie.bot.core.command.impl.CommandEvent;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.impl.GuildImpl;

public interface IReturnManager {
	
	/**
	 * @param event the event where the object was returned
	 * @param object the returned object to handle
	 * 
	 * @return whether or not the provided object was recognized 
	 * and handled
	 */
	public <T> boolean perform(CommandEvent event, T object);
	
	/**
	 * @param type the return type to handle
	 * 
	 * @return the {@link IReturnManager} instance, useful for chaining
	 */
	public IReturnManager unregisterHandler(Class<?> type);
	
	/**
	 * @param type the return type to handle
	 * @param function the handler function
	 * 
	 * @return the {@link IReturnManager} instance, useful for chaining
	 */
	public <T> IReturnManager registerHandler(Class<T> type, BiConsumer<CommandEvent, T> function);
	
	/**
	 * @param type the type of the handler
	 * 
	 * @return whether or not the specified type should be handled with inheritance, 
	 * this means that it will, for instance, handle {@link GuildImpl} if {@link Guild}
	 * was registered
	 */
	public boolean isHandleInheritance(Class<?> type);
	
	/**
	 * @param type the type of the handler
	 * @param handle whether or not the specified type should be handled with inheritance, 
	 * this means that it will, for instance, handle {@link GuildImpl} if {@link Guild}
	 * was registered
	 * 
	 * @return the {@link IReturnManager} instance, useful for chaining
	 */
	public IReturnManager setHandleInheritance(Class<?> type, boolean handle);
	
}