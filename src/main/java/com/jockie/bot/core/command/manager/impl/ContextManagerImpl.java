package com.jockie.bot.core.command.manager.impl;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;

import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandListener;
import com.jockie.bot.core.command.manager.IContextManager;
import com.jockie.bot.core.utility.CommandUtility;
import com.jockie.bot.core.utility.function.TriFunction;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.PrivateChannelImpl;
import net.dv8tion.jda.internal.entities.TextChannelImpl;
import net.dv8tion.jda.internal.entities.UserImpl;

public class ContextManagerImpl implements IContextManager {
	
	public ContextManagerImpl() {
		/* FIXME: Unsure of how to implement this with this new system
		Class<?> command = event.getCommand().getClass();
		if(type.isAssignableFrom(command)) {
			return event.getCommand();
		}
		*/
		
		this.registerContext(CommandEvent.class, (event, type) -> {
			return event;
		})._setEnforcedContext(CommandEvent.class, true);
		
		this.registerContext(CommandListener.class, (event, type) -> {
			return event.getCommandListener();
		});
		
		this.registerContext(ChannelType.class, (event, type) -> {
			return event.getChannelType();
		});
		
		this.registerContext(JDAImpl.class, (event, type) -> {
			return (JDAImpl) event.getJDA();
		}).setHandleInheritance(JDAImpl.class, true);
		
		this.registerContext(UserImpl.class, (event, type) -> {
			return (UserImpl) event.getAuthor();
		}).setHandleInheritance(UserImpl.class, true);
		
		this.registerContext(MessageChannel.class, (event, type) -> {
			return event.getChannel();
		}).setHandleInheritance(MessageChannel.class, true);
		
		this.registerContext(Message.class, (event, type) -> {
			return event.getMessage();
		}).setHandleInheritance(Message.class, true);
		
		this.registerContext(GuildImpl.class, (event, type) -> {
			return (GuildImpl) event.getGuild();
		}).setHandleInheritance(GuildImpl.class, true);
		
		this.registerContext(TextChannelImpl.class, (event, type) -> {
			return (TextChannelImpl) event.getTextChannel();
		}).setHandleInheritance(TextChannelImpl.class, true);
		
		this.registerContext(MemberImpl.class, (event, type) -> {
			return (MemberImpl) event.getMember();
		}).setHandleInheritance(MemberImpl.class, true);
		
		this.registerContext(PrivateChannelImpl.class, (event, type) -> {
			return (PrivateChannelImpl) event.getPrivateChannel();
		}).setHandleInheritance(PrivateChannelImpl.class, true);
	}
	
	private Map<Type, ContextProvider<?>> contextProviders = new HashMap<>();
	
	private Set<ContextProvider<?>> handleInheritance = new LinkedHashSet<>();
	private Map<Type, Type> inheritanceCache = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	private <T> ContextProvider<T> getContextProvider(Type type) {
		return (ContextProvider<T>) this.contextProviders.get(type);
	}
	
	private ContextProvider<?> getInheritenceProvider(Type type) {
		if(!(type instanceof Class<?>)) {
			return null;
		}
		
		Class<?> firstType = (Class<?>) type;
		
		for(ContextProvider<?> inheritenceProvider : this.handleInheritance) {
			Class<?> secondType = (Class<?>) inheritenceProvider.getType();
			
			if(CommandUtility.isInstanceOf(secondType, firstType)) {
				return inheritenceProvider;
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private <T> T getContext(CommandEvent event, ContextProvider<T> provider, Type type, Parameter parameter) {
		if(type == null && parameter == null) {
			throw new IllegalStateException("Both Type and Parameter is null");
		}
		
		if(type == null) {
			type = parameter.getParameterizedType();
		}
		
		boolean initialProvider = provider != null;
		if(!initialProvider) {
			provider = this.getContextProvider(type);
		}
		
		if(provider != null) {
			if(parameter != null) {
				TriFunction<CommandEvent, Parameter, Type, T> parameterizedFunction = provider.getParameterizedContextFunction();
				if(parameterizedFunction != null) {
					return parameterizedFunction.apply(event, parameter, type);
				}
			}
			
			BiFunction<CommandEvent, Type, T> function = provider.getContextFunction();
			if(function != null) {
				return function.apply(event, type);
			}
		}
		
		if(!initialProvider) {
			if(this.inheritanceCache.containsKey(type)) {
				Type cachedType = this.inheritanceCache.get(type);
				provider = this.getContextProvider(cachedType);
				
				if(cachedType != null) {
					return this.getContext(event, provider, cachedType, parameter);
				}else{
					return null;
				}
			}
			
			provider = (ContextProvider<T>) this.getInheritenceProvider(type);
			this.inheritanceCache.put(type, provider.getType());
			
			if(provider != null) {
				return this.getContext(event, provider, type, parameter);
			}
		}
		
		return null;
	}
	
	public <T> T getContext(CommandEvent event, Type type) {
		return this.getContext(event, null, type, null);
	}
	
	public <T> T getContext(CommandEvent event, Parameter parameter) {
		return this.getContext(event, null, parameter.getParameterizedType(), parameter);
	}
	
	public boolean isEnforcedContext(Type type) {
		ContextProvider<?> provider = this.getContextProvider(type);
		if(provider != null) {
			return provider.isEnforced();
		}
		
		return false;
	}
	
	private ContextManagerImpl _setEnforcedContext(Type type, boolean enforced) {
		ContextProvider<?> provider = this.getContextProvider(type);
		if(provider == null) {
			throw new IllegalArgumentException(type.getTypeName() + " is not a registered context");
		}
		
		provider.setEnforced(enforced);
		
		return this;
	}
	
	public ContextManagerImpl setEnforcedContext(Type type, boolean enforced) {
		throw new UnsupportedOperationException();
	}
	
	public boolean isHandleInheritance(Type type) {
		ContextProvider<?> provider = this.getContextProvider(type);
		if(provider != null) {
			return provider.isHandleInheritence();
		}
		
		return false;
	}
	
	public ContextManagerImpl setHandleInheritance(Type type, boolean handle) {
		if(!(type instanceof Class)) {
			throw new UnsupportedOperationException("Only classes can handle inheritence currently");
		}
		
		ContextProvider<?> provider = this.getContextProvider(type);
		if(provider == null) {
			throw new IllegalArgumentException(type.getTypeName() + " is not a registered context");
		}
		
		provider.setHandleInheritence(handle);
		
		if(handle) {
			this.handleInheritance.add(provider);
		}else{
			this.handleInheritance.remove(provider);
		}
		
		/* Re-compute cache */
		for(Entry<Type, Type> entry : this.inheritanceCache.entrySet()) {
			provider = this.getInheritenceProvider(entry.getKey());
			
			this.inheritanceCache.put(entry.getKey(), provider != null ? provider.getType() : null);
		}
		
		return this;
	}
	
	public ContextManagerImpl unregisterContext(Type type) {
		this.handleInheritance.remove(this.contextProviders.remove(type));
		
		return this;
	}
	
	public <T> ContextManagerImpl registerContext(Type type, TriFunction<CommandEvent, Parameter, Type, T> function) {
		ContextProvider<T> provider = this.getContextProvider(type);
		if(provider != null) {
			provider.setContextFunction(function);
		}else{
			this.contextProviders.put(type, new ContextProvider<>(type, function));
			this.inheritanceCache.remove(type);
		}
		
		return this;
	}
	
	public <T> ContextManagerImpl registerContext(Class<T> clazz, TriFunction<CommandEvent, Parameter, Type, T> function) {
		IContextManager.super.registerContext(clazz, function);
		
		return this;
	}
	
	public <T> ContextManagerImpl registerContext(Type type, BiFunction<CommandEvent, Type, T> function) {
		ContextProvider<T> provider = this.getContextProvider(type);
		if(provider != null) {
			provider.setContextFunction(function);
		}else{
			this.contextProviders.put(type, new ContextProvider<>(type, function));
		}
		
		return this;
	}
	
	public <T> ContextManagerImpl registerContext(Class<T> clazz, BiFunction<CommandEvent, Type, T> function) {
		IContextManager.super.registerContext(clazz, function);
		
		return this;
	}
}