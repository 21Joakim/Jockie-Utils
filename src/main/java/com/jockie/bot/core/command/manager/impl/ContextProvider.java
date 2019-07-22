package com.jockie.bot.core.command.manager.impl;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.function.BiFunction;

import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.utility.function.TriFunction;

class ContextProvider<ProviderType> {
	
	private Type type;
	
	private TriFunction<CommandEvent, Parameter, Type, ProviderType> parameterizedContextFunction;
	private BiFunction<CommandEvent, Type, ProviderType> contextFunction;
	
	private boolean enforced;
	private boolean handleInheritence;
	
	public ContextProvider(Type type) {
		this.type = type;
	}
	
	public ContextProvider(Type type, TriFunction<CommandEvent, Parameter, Type, ProviderType> parameterizedContextFunction) {
		this(type);
		
		this.parameterizedContextFunction = parameterizedContextFunction;
	}
	
	public ContextProvider(Type type, BiFunction<CommandEvent, Type, ProviderType> contextFunction) {
		this(type);
		
		this.contextFunction = contextFunction;
	}
	
	public Type getType() {
		return this.type;
	}
	
	public TriFunction<CommandEvent, Parameter, Type, ProviderType> getParameterizedContextFunction() {
		return this.parameterizedContextFunction;
	}
	
	public void setContextFunction(TriFunction<CommandEvent, Parameter, Type, ProviderType> parameterizedContextFunction) {
		this.parameterizedContextFunction = parameterizedContextFunction;
	}
	
	public BiFunction<CommandEvent, Type, ProviderType> getContextFunction() {
		return this.contextFunction;
	}
	
	public void setContextFunction(BiFunction<CommandEvent, Type, ProviderType> contextFunction) {
		this.contextFunction = contextFunction;
	}
	
	public boolean isEnforced() {
		return this.enforced;
	}
	
	public void setEnforced(boolean enforced) {
		this.enforced = enforced;
	}
	
	public boolean isHandleInheritence() {
		return this.handleInheritence;
	}
	
	public void setHandleInheritence(boolean handleInheritence) {
		this.handleInheritence = handleInheritence;
	}
}