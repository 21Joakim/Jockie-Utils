package com.jockie.bot.core.cooldown;

import java.util.concurrent.TimeUnit;

import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.cooldown.ICooldown.Scope;

import net.dv8tion.jda.api.entities.Message;

public interface ICooldownManager {
	
	/**
	 * Get a cooldown by the provided context (message)
	 * 
	 * @param command the command the cooldown applies to
	 * @param message the context to get the cooldown by
	 * 
	 * @return the cooldown gotten by the provided context key, may be null
	 */
	public ICooldown getCooldown(ICommand command, Message message);
	
	/**
	 * Get a cooldown by the provided context-key
	 * 
	 * @param command the command the cooldown applies to
	 * @param key the context-key to get the cooldown by, the key is derived from {@link Scope#getContextKey(Message)}
	 * 
	 * @return the cooldown gotten by the provided context key, may be null
	 */
	public ICooldown getCooldown(ICommand command, String key);
	
	/**
	 * Apply a cooldown to the specified command
	 * 
	 * @param command the command to apply the cooldown to
	 * @param cooldown the cooldown to apply to the provided command
	 * 
	 * @return the previously applied cooldown, may be null
	 */
	public ICooldown applyCooldown(ICommand command, ICooldown cooldown);
	
	/**
	 * Create a cooldown bound to the provided context (message) and apply it
	 * 
	 * @param command the command which this cooldown applies to
	 * @param message the context which this cooldown should apply to
	 * 
	 * @return the previously applied cooldown, may be null
	 */
	public ICooldown applyCooldown(ICommand command, Message message);
	
	/**
	 * Create a cooldown bound to the provided context (message) and apply it
	 * 
	 * @param command the command which this cooldown applies to
	 * @param message the context which this cooldown should apply to
	 * 
	 * @return the cooldown which was created
	 */
	public ICooldown applyCooldownAndGet(ICommand command, Message message);
	
	/**
	 * Remove a cooldown by the provided context (message)
	 * 
	 * @param command the command the cooldown applies to
	 * @param message the context to remove the cooldown by
	 * 
	 * @return the cooldown which it removed, may be null
	 */
	public ICooldown removeCooldown(ICommand command, Message message);
	
	/**
	 * Remove a cooldown by the provided context-key
	 * 
	 * @param command the command the cooldown applies to
	 * @param key the context-key to remove the cooldown by, the key is derived from {@link Scope#getContextKey(Message)}
	 * 
	 * @return the cooldown which it removed, may be null
	 */
	public ICooldown removeCooldown(ICommand command, String key);
	
	/**
	 * @param scope the scope of this cooldown
	 * @param duration the duration this cooldown should apply for
	 * @param unit the unit the duration is going to use
	 * 
	 * @return an empty cooldown which is not bound to any context
	 */
	public ICooldown createEmptyCooldown(Scope scope, long duration, TimeUnit unit);
	
}