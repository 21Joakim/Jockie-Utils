package com.jockie.bot.core.command.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.jockie.bot.core.command.ICommand;

/* How can I make this somewhat synchronized without losing performance, do I even need to have it synchronized? */
public class CooldownManager {
	
	private static Map<ICommand, Map<Long, Long>> cooldowns = new HashMap<>();
	
	private static void ensureExistance(ICommand command) {
		if(!CooldownManager.cooldowns.containsKey(command)) {
			CooldownManager.cooldowns.put(command, new HashMap<>());
		}
	}
	
	public static void addCooldown(ICommand command, long user) {
		CooldownManager.ensureExistance(command);
		
		Map<Long, Long> map = CooldownManager.cooldowns.get(command);
		synchronized(map) {
			map.put(user, System.currentTimeMillis());
		}
	}
	
	public static void removeCooldown(ICommand command, long user) {
		CooldownManager.ensureExistance(command);
		
		Map<Long, Long> map = CooldownManager.cooldowns.get(command);
		synchronized(map) {
			map.remove(user);
		}
	}
	
	public static void addToCooldown(ICommand command, long user, long time) {
		CooldownManager.ensureExistance(command);
		
		Map<Long, Long> map = CooldownManager.cooldowns.get(command);
		synchronized(map) {
			map.put(user, map.get(user) + time);
		}
	}
	
	public static void addToCooldown(ICommand command, long user, long time, TimeUnit unit) {
		CooldownManager.addToCooldown(command, user, unit.toMillis(time));
	}
	
	public static void removeFromCooldown(ICommand command, long user, long time) {
		CooldownManager.ensureExistance(command);
		
		Map<Long, Long> map = CooldownManager.cooldowns.get(command);
		synchronized(map) {
			map.put(user, map.get(user) - time);
		}
	}
	
	public static void removeFromCooldown(ICommand command, long user, long time, TimeUnit unit) {
		CooldownManager.removeFromCooldown(command, user, unit.toMillis(time));
	}
	
	public static long getTimeRemaining(ICommand command, long user) {
		CooldownManager.ensureExistance(command);
		
		Map<Long, Long> map = CooldownManager.cooldowns.get(command);
		synchronized(map) {
			if(!map.containsKey(user)) {
				return 0;
			}
			
			long remaining = command.getCooldownDuration() - (System.currentTimeMillis() - CooldownManager.cooldowns.get(command).get(user));
			
			return remaining > 0 ? remaining : 0;
		}
	}
}