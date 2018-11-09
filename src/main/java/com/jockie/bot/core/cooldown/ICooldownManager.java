package com.jockie.bot.core.cooldown;

import java.util.concurrent.TimeUnit;

import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.cooldown.ICooldown.Scope;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public interface ICooldownManager {
	
	public ICooldown getCooldown(ICommand command, MessageReceivedEvent event);
	public ICooldown getCooldown(ICommand command, String key);
	
	/**
	 * Apply a cooldown to the specified command
	 */
	public void applyCooldown(ICommand command, ICooldown cooldown);
	
	/**
	 * @return the cooldown which it removed, may be null
	 */
	public ICooldown removeCooldown(ICommand command, MessageReceivedEvent event);
	
	/**
	 * @return the cooldown which it removed, may be null
	 */
	public ICooldown removeCooldown(ICommand command, String key);
	
	/**
	 * @return a boolean which will be true if it replaced an already present cooldown
	 */
	public boolean createCooldown(ICommand command, MessageReceivedEvent event);
	
	/**
	 * @return the cooldown which was created
	 */
	public ICooldown createCooldownAndGet(ICommand command, MessageReceivedEvent event);
	
	public ICooldown createEmptyCooldown(Scope scope, long duration, TimeUnit unit);
	
}