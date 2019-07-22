package com.jockie.bot.core.command.manager.impl;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;

import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.manager.IReturnManager;
import com.jockie.bot.core.utility.CommandUtility;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class ReturnManagerImpl implements IReturnManager {
	
	public ReturnManagerImpl() {
		this.registerHandler(CharSequence.class, (event, value) -> {
			event.reply(value).queue();
		}).setHandleInheritance(CharSequence.class, true);
		
		this.registerHandler(Message.class, (event, value) -> {
			event.reply(value).queue();
		}).setHandleInheritance(Message.class, true);
		
		this.registerHandler(MessageEmbed.class, (event, value) -> {
			event.reply(value).queue();
		}).setHandleInheritance(MessageEmbed.class, true);
		
		this.registerHandler(File.class, (event, value) -> {
			event.replyFile(value).queue();
		});
	}
	
	protected Map<Class<?>, ReturnHandler<?>> returnHandlers = new HashMap<>();
	
	protected Set<ReturnHandler<?>> handleInheritance = new LinkedHashSet<>();
	protected Map<Class<?>, Class<?>> inheritanceCache = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	protected <T> ReturnHandler<T> getReturnHandler(Class<?> type) {
		return (ReturnHandler<T>) this.returnHandlers.get(type);
	}
	
	protected ReturnHandler<?> getInheritanceHandler(Class<?> type) {
		for(ReturnHandler<?> inheritenceProvider : this.handleInheritance) {
			Class<?> secondType = inheritenceProvider.getType();
			
			if(CommandUtility.isInstanceOf(type, secondType)) {
				return inheritenceProvider;
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> boolean perform(CommandEvent event, T object) {
		Class<?> type = object.getClass();
		
		ReturnHandler<T> handler = this.getReturnHandler(type);
		if(handler != null) {
			handler.getReturnHandler().accept(event, object);
			
			return true;
		}
		
		if(this.inheritanceCache.containsKey(type)) {
			Class<?> cachedType = this.inheritanceCache.get(type);
			if(cachedType != null) {
				handler = this.getReturnHandler(cachedType);
				handler.getReturnHandler().accept(event, object);
				
				return true;
			}
			
			return false;
		}
		
		handler = (ReturnHandler<T>) this.getInheritanceHandler(type);
		this.inheritanceCache.put(type, handler != null ? handler.getType() : null);
		
		if(handler != null) {
			handler.getReturnHandler().accept(event, object);
			
			return true;
		}
		
		return false;
	}
	
	public ReturnManagerImpl unregisterHandler(Class<?> type) {
		this.handleInheritance.remove(this.returnHandlers.remove(type));
		
		return this;
	}
	
	public <T> ReturnManagerImpl registerHandler(Class<T> type, BiConsumer<CommandEvent, T> function) {
		ReturnHandler<T> handler = this.getReturnHandler(type);
		if(handler != null) {
			handler.setReturnHandler(function);
		}else{
			this.returnHandlers.put(type, new ReturnHandler<T>(type, function));
			this.inheritanceCache.remove(type);
		}
		
		return this;
	}
	
	public boolean isHandleInheritance(Class<?> type) {
		ReturnHandler<?> handler = this.returnHandlers.get(type);
		if(handler != null) {
			return handler.isHandleInheritence();
		}
		
		return false;
	}
	
	public ReturnManagerImpl setHandleInheritance(Class<?> type, boolean handle) {		
		ReturnHandler<?> handler = this.returnHandlers.get(type);
		if(handler == null) {
			throw new IllegalArgumentException(type.getTypeName() + " is not a registered context");
		}
		
		handler.setHandleInheritence(handle);
		
		if(handle) {
			this.handleInheritance.add(handler);
		}else{
			this.handleInheritance.remove(handler);
		}
		
		/* Re-compute cache */
		for(Entry<Class<?>, Class<?>> entry : this.inheritanceCache.entrySet()) {
			handler = this.getInheritanceHandler(entry.getKey());
			
			this.inheritanceCache.put(entry.getKey(), handler != null ? handler.getType() : null);
		}
		
		return this;
	}
}