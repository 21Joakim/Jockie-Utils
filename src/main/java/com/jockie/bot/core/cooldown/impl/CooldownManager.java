package com.jockie.bot.core.cooldown.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.cooldown.ICooldown;
import com.jockie.bot.core.cooldown.ICooldownManager;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CooldownManager implements ICooldownManager {
	
	private Map<ICommand, Map<String, ICooldown>> cooldownStore = new HashMap<>();
	
	public Map<String, ICooldown> getCooldownStore(ICommand command) {
		return this.cooldownStore.get(command);
	}
	
	public ICooldown getCooldown(ICommand command, String key) {
		return this.cooldownStore.get(command).get(key);
	}
	
	public ICooldown getCooldown(ICommand command, MessageReceivedEvent event) {
		Map<String, ICooldown> cooldownStore = this.getCooldownStore(command);
		if(cooldownStore != null) {
			return cooldownStore.get(command.getCooldownScope().getContextKey(event));
		}
		
		return null;
	}
	
	public void setCooldown(ICommand command, ICooldown cooldown) {
		Map<String, ICooldown> cooldownStore = this.cooldownStore.computeIfAbsent(command, key -> new HashMap<>());
		cooldownStore.put(cooldown.getKey(), cooldown);
	}
	
	
	public boolean createCooldown(ICommand command, MessageReceivedEvent event) {
		Map<String, ICooldown> cooldownStore = this.cooldownStore.computeIfAbsent(command, key -> new HashMap<>());
		
		CooldownImpl cooldown = new CooldownImpl(event, command.getCooldownScope(), command.getCooldownDuration(), TimeUnit.MILLISECONDS);
		ICooldown previousCooldown = cooldownStore.put(cooldown.getKey(), cooldown);
		
		return previousCooldown != null && !previousCooldown.hasExpired() ? true : false;
	}
	
	public ICooldown removeCooldown(ICommand command, MessageReceivedEvent event) {
		Map<String, ICooldown> cooldownStore = this.getCooldownStore(command);
		if(cooldownStore != null) {
			return cooldownStore.remove(command.getCooldownScope().getContextKey(event));
		}
		
		return null;
	}
	
	public ICooldown removeCooldown(ICommand command, String key) {
		Map<String, ICooldown> cooldownStore = this.getCooldownStore(command);
		if(cooldownStore != null) {
			return cooldownStore.remove(key);
		}
		
		return null;
	}
}