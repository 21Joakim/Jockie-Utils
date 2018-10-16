package com.jockie.bot.core.cooldown;

import com.jockie.bot.core.command.ICommand;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public interface ICooldownManager {
	
	public ICooldown getCooldown(ICommand command, MessageReceivedEvent event);
	public ICooldown getCooldown(ICommand command, String key);
	
	public void setCooldown(ICommand command, ICooldown cooldown);
	
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
	
}