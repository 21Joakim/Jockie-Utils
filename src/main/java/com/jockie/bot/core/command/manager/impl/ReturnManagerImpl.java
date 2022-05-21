package com.jockie.bot.core.command.manager.impl;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;

import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.manager.IReturnManager;
import com.jockie.bot.core.utility.CommandUtility;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.internal.utils.Checks;

public class ReturnManagerImpl implements IReturnManager {
	
	public ReturnManagerImpl() {
		this.registerDefaultHandlers();
	}
	
	public final void registerDefaultHandlers() {
		this.registerHandler(CharSequence.class, (event, value) -> event.reply(value).queue())
			.setHandleInheritance(CharSequence.class, true);
		
		this.registerHandler(Message.class, (event, value) -> event.reply(value).queue())
			.setHandleInheritance(Message.class, true);
		
		this.registerHandler(MessageEmbed.class, (event, value) -> event.reply(value).queue())
			.setHandleInheritance(MessageEmbed.class, true);
		
		this.registerHandler(File.class, (event, value) -> event.replyFile(value).queue());
	}
	
	protected Map<Class<?>, ReturnHandler<?>> returnHandlers = new HashMap<>();
	
	protected Set<ReturnHandler<?>> handleInheritance = new LinkedHashSet<>();
	protected Map<Class<?>, Class<?>> inheritanceCache = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	protected <T> ReturnHandler<T> getReturnHandler(Class<?> type) {
		return (ReturnHandler<T>) this.returnHandlers.get(type);
	}
	
	protected ReturnHandler<?> getInheritanceHandler(Class<?> type) {
		Checks.notNull(type, "type");
		
		for(ReturnHandler<?> inheritenceProvider : this.handleInheritance) {
			Class<?> secondType = inheritenceProvider.getType();
			
			if(CommandUtility.isInstanceOf(type, secondType)) {
				return inheritenceProvider;
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> boolean perform(@Nonnull CommandEvent event, @Nonnull T object) {
		Checks.notNull(event, "event");
		Checks.notNull(object, "object");
		
		Class<?> type = object.getClass();
		
		ReturnHandler<T> handler = this.getReturnHandler(type);
		if(handler != null) {
			handler.getReturnHandler().accept(event, object);
			
			return true;
		}
		
		Class<?> cachedType = this.inheritanceCache.get(type);
		if(cachedType != null) {
			handler = this.getReturnHandler(cachedType);
			handler.getReturnHandler().accept(event, object);
			
			return true;
		}
		
		handler = (ReturnHandler<T>) this.getInheritanceHandler(type);
		this.inheritanceCache.put(type, handler != null ? handler.getType() : null);
		
		if(handler != null) {
			handler.getReturnHandler().accept(event, object);
			
			return true;
		}
		
		return false;
	}
	
	@Nonnull
	public ReturnManagerImpl unregisterHandler(@Nonnull Class<?> type) {
		Checks.notNull(type, "type");
		
		this.handleInheritance.remove(this.returnHandlers.remove(type));
		
		return this;
	}
	
	@Nonnull
	public <T> ReturnManagerImpl registerHandler(@Nonnull Class<T> type, @Nonnull BiConsumer<CommandEvent, T> function) {
		Checks.notNull(type, "type");
		Checks.notNull(function, "function");
		
		ReturnHandler<T> handler = this.getReturnHandler(type);
		if(handler != null) {
			handler.setReturnHandler(function);
		}else{
			this.returnHandlers.put(type, new ReturnHandler<T>(type, function));
			this.inheritanceCache.remove(type);
		}
		
		return this;
	}
	
	public boolean isHandleInheritance(@Nonnull Class<?> type) {
		Checks.notNull(type, "type");
		
		ReturnHandler<?> handler = this.returnHandlers.get(type);
		if(handler != null) {
			return handler.isHandleInheritence();
		}
		
		return false;
	}
	
	@Nonnull
	public ReturnManagerImpl setHandleInheritance(@Nonnull Class<?> type, boolean handle) {
		Checks.notNull(type, "type");
		
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