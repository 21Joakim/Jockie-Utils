package com.jockie.bot.core.command.manager.impl;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.command.manager.IErrorManager;
import com.jockie.bot.core.utility.CommandUtility;
import com.jockie.bot.core.utility.function.TriConsumer;

import net.dv8tion.jda.core.entities.Message;

public class ErrorManagerImpl implements IErrorManager {
	
	protected Map<Class<?>, TriConsumer<IArgument<?>, Message, String>> consumers = new HashMap<>();
	
	protected Set<Class<?>> handleInheritance = new LinkedHashSet<>();
	protected Map<Class<?>, Class<?>> inheritanceCache = new HashMap<>();
	
	public boolean handle(IArgument<?> argument, Message message, String content) {
		Class<?> type = argument.getType();
		
		if(this.consumers.containsKey(type)) {
			this.consumers.get(type).accept(argument, message, content);
			
			return true;
		}
		
		if(this.inheritanceCache.containsKey(type)) {
			Class<?> cachedType = this.inheritanceCache.get(type);
			if(cachedType != null) {
				this.consumers.get(cachedType).accept(argument, message, content);
				
				return true;
			}
			
			return false;
		}
		
		Class<?> inheritanceType = this.getInheritanceType(type);
		this.inheritanceCache.put(type, inheritanceType);
		
		if(inheritanceType != null) {
			this.consumers.get(inheritanceType).accept(argument, message, content);
			
			return true;
		}
		
		return false;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> ErrorManagerImpl registerResponse(Class<T> type, TriConsumer<IArgument<T>, Message, String> consumer) {
		this.consumers.put(type, (TriConsumer) consumer);
		this.inheritanceCache.remove(type);
		
		return this;
	}
	
	protected Class<?> getInheritanceType(Class<?> type) {
		for(Class<?> inheritanceType : this.handleInheritance) {
			if(CommandUtility.isInstanceOf(type, inheritanceType)) {
				return inheritanceType;
			}
		}
		
		return null;
	}
	
	public boolean isHandleInheritance(Class<?> type) {
		return this.handleInheritance.contains(type);
	}
	
	public ErrorManagerImpl setHandleInheritance(Class<?> type, boolean handle) {
		if(!this.consumers.containsKey(type)) {
			throw new IllegalArgumentException(type.getTypeName() + " is not a registered response type");
		}
		
		if(handle) {
			this.handleInheritance.add(type);
		}else{
			this.handleInheritance.remove(type);
		}
		
		/* Re-compute cache */
		for(Entry<Class<?>, Class<?>> entry : this.inheritanceCache.entrySet()) {
			this.inheritanceCache.put(entry.getKey(), this.getInheritanceType(type));
		}
		
		return this;
	}
}