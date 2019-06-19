package com.jockie.bot.core.command.manager.impl;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.manager.IReturnManager;
import com.jockie.bot.core.utility.CommandUtility;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

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
	
	private Map<Class<?>, ReturnHandler<?>> returnHandlers = new HashMap<>();
	private Set<ReturnHandler<?>> handleInheritence = new LinkedHashSet<>();
	
	@SuppressWarnings("unchecked")
	private <T> ReturnHandler<T> getReturnHandler(Class<?> type) {
		return (ReturnHandler<T>) this.returnHandlers.get(type);
	}
	
	private ReturnHandler<?> getInheritenceHandler(Class<?> type) {
		for(ReturnHandler<?> inheritenceProvider : this.handleInheritence) {
			Class<?> secondType = inheritenceProvider.getType();
			
			if(CommandUtility.isAssignableFrom(type, secondType)) {
				return inheritenceProvider;
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> boolean perform(CommandEvent event, T object) {		
		ReturnHandler<T> provider = this.getReturnHandler(object.getClass());
		if(provider != null) {
			provider.getReturnHandler().accept(event, object);
			
			return true;
		}
		
		provider = (ReturnHandler<T>) this.getInheritenceHandler(object.getClass());
		if(provider != null) {
			provider.getReturnHandler().accept(event, object);
			
			return true;
		}
		
		return false;
	}
	
	public ReturnManagerImpl unregisterHandler(Class<?> type) {
		this.handleInheritence.remove(this.returnHandlers.remove(type));
		
		return this;
	}
	
	public <T> ReturnManagerImpl registerHandler(Class<T> type, BiConsumer<CommandEvent, T> function) {
		ReturnHandler<T> provider = this.getReturnHandler(type);
		if(provider != null) {
			provider.setReturnHandler(function);
		}else{
			this.returnHandlers.put(type, new ReturnHandler<T>(type, function));
		}
		
		return this;
	}
	
	public boolean isHandleInheritance(Class<?> type) {
		ReturnHandler<?> provider = this.returnHandlers.get(type);
		if(provider != null) {
			return provider.isHandleInheritence();
		}
		
		return false;
	}
	
	public ReturnManagerImpl setHandleInheritance(Class<?> type, boolean handle) {		
		ReturnHandler<?> provider = this.returnHandlers.get(type);
		if(provider == null) {
			throw new IllegalArgumentException(type.getTypeName() + " is not a registered context");
		}
		
		provider.setHandleInheritence(handle);
		
		if(handle) {
			this.handleInheritence.add(provider);
		}else{
			this.handleInheritence.remove(provider);
		}
		
		return this;
	}
}