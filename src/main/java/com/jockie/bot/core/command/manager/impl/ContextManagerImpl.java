package com.jockie.bot.core.command.manager.impl;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;

import com.jockie.bot.core.command.ICommand;
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
import net.dv8tion.jda.internal.utils.Checks;

public class ContextManagerImpl implements IContextManager {
	
	public ContextManagerImpl() {
		this.registerDefaultContext();
	}
	
	public void registerDefaultContext() {
		this.registerContext(CommandEvent.class, (event, type) -> event)
			.setEnforcedContext(CommandEvent.class, true);
		
		this.registerContext(CommandListener.class, (event, type) -> event.getCommandListener());
		
		this.registerContext(ChannelType.class, (event, type) -> event.getChannelType());
		
		this.registerContext(JDAImpl.class, (event, type) -> (JDAImpl) event.getJDA())
			.setHandleInheritance(JDAImpl.class, true);
		
		this.registerContext(UserImpl.class, (event, type) -> (UserImpl) event.getAuthor())
			.setHandleInheritance(UserImpl.class, true);
		
		this.registerContext(MessageChannel.class, (event, type) -> event.getChannel())
			.setHandleInheritance(MessageChannel.class, true);
		
		this.registerContext(Message.class, (event, type) -> event.getMessage())
			.setHandleInheritance(Message.class, true);
		
		this.registerContext(GuildImpl.class, (event, type) -> (GuildImpl) event.getGuild())
			.setHandleInheritance(GuildImpl.class, true);
		
		this.registerContext(TextChannelImpl.class, (event, type) -> (TextChannelImpl) event.getTextChannel())
			.setHandleInheritance(TextChannelImpl.class, true);
		
		this.registerContext(MemberImpl.class, (event, type) -> (MemberImpl) event.getMember())
			.setHandleInheritance(MemberImpl.class, true);
		
		this.registerContext(PrivateChannelImpl.class, (event, type) -> (PrivateChannelImpl) event.getPrivateChannel())
			.setHandleInheritance(PrivateChannelImpl.class, true);
	}
	
	public void unregisterDefaultContext() {
		this.unregisterContext(CommandEvent.class);
		this.unregisterContext(CommandListener.class);
		this.unregisterContext(ChannelType.class);
		this.unregisterContext(JDAImpl.class);
		this.unregisterContext(UserImpl.class);
		this.unregisterContext(MessageChannel.class);
		this.unregisterContext(Message.class);
		this.unregisterContext(GuildImpl.class);
		this.unregisterContext(TextChannelImpl.class);
		this.unregisterContext(MemberImpl.class);
		this.unregisterContext(PrivateChannelImpl.class);
	}
	
	private Map<Type, ContextProvider<?>> contextProviders = new HashMap<>();
	
	private Set<ContextProvider<?>> handleInheritance = new LinkedHashSet<>();
	private Map<Type, Type> inheritanceCache = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	private <T> ContextProvider<T> getContextProvider(Type type) {
		return (ContextProvider<T>) this.contextProviders.get(type);
	}
	
	private static final ContextProvider<ICommand> COMMAND_CONTEXT_PROVIDER = new ContextProvider<>(ICommand.class);
	
	static {
		COMMAND_CONTEXT_PROVIDER.setContextFunction((event, parameter, type) -> event.getCommand());
	}
	
	/* TODO: This probably isn't the best solution to this */
	@SuppressWarnings("unchecked")
	private <T> ContextProvider<T> getContextProvider(CommandEvent event, Type type) {
		if(type instanceof Class) {
			if(CommandUtility.isInstanceOf(event.getCommand().getClass(), (Class<?>) type)) {
				return (ContextProvider<T>) COMMAND_CONTEXT_PROVIDER;
			}
		}
		
		return this.getContextProvider(type);
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
		Checks.notNull(event, "event");
		Checks.check(type != null || parameter != null, "Both type and paramter may not be null");
		
		if(type == null) {
			type = parameter.getParameterizedType();
		}
		
		boolean initialProvider = provider != null;
		if(!initialProvider) {
			provider = this.getContextProvider(event, type);
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
			if(provider == null) {
				return null;
			}
			
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
		Checks.notNull(parameter, "parameter");
		
		return this.getContext(event, null, parameter.getParameterizedType(), parameter);
	}
	
	public boolean isEnforcedContext(Type type) {
		Checks.notNull(type, "type");
		
		ContextProvider<?> provider = this.getContextProvider(type);
		if(provider != null) {
			return provider.isEnforced();
		}
		
		return false;
	}
	
	public ContextManagerImpl setEnforcedContext(Type type, boolean enforced) {
		ContextProvider<?> provider = this.getContextProvider(type);
		if(provider == null) {
			throw new IllegalArgumentException(type.getTypeName() + " is not a registered context");
		}
		
		provider.setEnforced(enforced);
		
		return this;
	}
	
	public boolean isHandleInheritance(Type type) {
		Checks.notNull(type, "type");
		
		ContextProvider<?> provider = this.getContextProvider(type);
		if(provider != null) {
			return provider.isHandleInheritence();
		}
		
		return false;
	}
	
	public ContextManagerImpl setHandleInheritance(Type type, boolean handle) {
		Checks.notNull(type, "type");
		
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
		Checks.notNull(type, "type");
		
		this.handleInheritance.remove(this.contextProviders.remove(type));
		
		return this;
	}
	
	public <T> ContextManagerImpl registerContext(Type type, TriFunction<CommandEvent, Parameter, Type, T> function) {
		Checks.notNull(type, "type");
		Checks.notNull(function, "function");
		
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
		Checks.notNull(type, "type");
		Checks.notNull(function, "function");
		
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