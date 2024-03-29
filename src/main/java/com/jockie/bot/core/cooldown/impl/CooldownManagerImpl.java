package com.jockie.bot.core.cooldown.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.cooldown.ICooldown;
import com.jockie.bot.core.cooldown.ICooldown.Scope;
import com.jockie.bot.core.cooldown.ICooldownManager;

import net.dv8tion.jda.api.entities.Message;

public class CooldownManagerImpl implements ICooldownManager {
	
	private Map<ICommand, Map<String, ICooldown>> cooldownStore = new HashMap<>();
	
	@Nullable
	public Map<String, ICooldown> getCooldownStore(ICommand command) {
		return this.cooldownStore.get(command);
	}
	
	@Nullable
	public ICooldown getCooldown(ICommand command, String key) {
		Map<String, ICooldown> cooldownStore = this.getCooldownStore(command);
		if(cooldownStore != null) {
			return cooldownStore.get(key);
		}
		
		return null;
	}
	
	@Nullable
	public ICooldown getCooldown(ICommand command, Message message) {
		Map<String, ICooldown> cooldownStore = this.getCooldownStore(command);
		if(cooldownStore != null) {
			return cooldownStore.get(command.getCooldownScope().getContextKey(message));
		}
		
		return null;
	}
	
	@Nullable
	public ICooldown applyCooldown(ICommand command, ICooldown cooldown) {
		Objects.requireNonNull(cooldown);
		
		if(cooldown.getContextKey() == null) {
			throw new IllegalArgumentException("Cooldown does not have a context key");
		}
		
		if(cooldown.getTimeStarted() == null) {
			cooldown.start();
		}
		
		Map<String, ICooldown> cooldownStore = this.cooldownStore.computeIfAbsent(command, (key) -> new HashMap<>());
		
		return cooldownStore.put(cooldown.getContextKey(), cooldown);
	}
	
	@Nullable
	public ICooldown applyCooldown(ICommand command, Message message) {
		Map<String, ICooldown> cooldownStore = this.cooldownStore.computeIfAbsent(command, (key) -> new HashMap<>());
		
		CooldownImpl cooldown = new CooldownImpl(message, command.getCooldownScope(), command.getCooldownDuration(), TimeUnit.MILLISECONDS);
		ICooldown previousCooldown = cooldownStore.put(cooldown.getContextKey(), cooldown);
		
		return previousCooldown != null && !previousCooldown.hasExpired() ? previousCooldown : null;
	}
	
	@Nonnull
	public ICooldown applyCooldownAndGet(ICommand command, Message message) {
		Map<String, ICooldown> cooldownStore = this.cooldownStore.computeIfAbsent(command, (key) -> new HashMap<>());
		
		CooldownImpl cooldown = new CooldownImpl(message, command.getCooldownScope(), command.getCooldownDuration(), TimeUnit.MILLISECONDS);
		cooldownStore.put(cooldown.getContextKey(), cooldown);
		
		return cooldown;
	}
	
	@Nullable
	public ICooldown removeCooldown(ICommand command, Message message) {
		Map<String, ICooldown> cooldownStore = this.getCooldownStore(command);
		if(cooldownStore != null) {
			return cooldownStore.remove(command.getCooldownScope().getContextKey(message));
		}
		
		return null;
	}
	
	@Nullable
	public ICooldown removeCooldown(ICommand command, String key) {
		Map<String, ICooldown> cooldownStore = this.getCooldownStore(command);
		if(cooldownStore != null) {
			return cooldownStore.remove(key);
		}
		
		return null;
	}
	
	@Nonnull
	public ICooldown createEmptyCooldown(Scope scope, long duration, TimeUnit unit) {
		return new CooldownImpl(scope, duration, unit);
	}
}