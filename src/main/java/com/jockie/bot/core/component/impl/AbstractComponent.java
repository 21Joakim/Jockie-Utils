package com.jockie.bot.core.component.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.component.IComponent;
import com.jockie.bot.core.parser.IParser;

public abstract class AbstractComponent<Type, Component extends IComponent<Type, Component>> implements IComponent<Type, Component> {
	
	protected final Class<Type> type;
	
	protected final String name;
	protected final String description;
	
	protected final IParser<Type, Component> parser;
	
	protected final Function<CommandEvent, Type> defaultValueFunction;
	
	protected final Map<String, Object> properties;
	
	protected <BuilderType extends IComponent.Builder<Type, Component, ?, BuilderType>> AbstractComponent(BuilderType builder) {
		this.type = builder.getType();
		this.name = builder.getName();
		this.description = builder.getDescription();
		this.parser = builder.getParser();
		this.defaultValueFunction = builder.getDefaultValueFunction();
		this.properties = new HashMap<>(builder.getProperties());
	}
	
	@Override
	@Nonnull
	public Class<Type> getType() {
		return this.type;
	}
	
	@Override
	@Nonnull
	public String getName() {
		return this.name;
	}
	
	@Override
	@Nullable
	public String getDescription() {
		return this.description;
	}
	
	@Override
	@Nonnull
	public IParser<Type, Component> getParser() {
		return this.parser;
	}

	@Override
	public boolean hasDefault() {
		return this.defaultValueFunction != null;
	}

	@Override
	@Nullable
	public Type getDefault(@Nonnull CommandEvent event) {
		if(this.defaultValueFunction != null) {
			return this.defaultValueFunction.apply(event);
		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <T> T getProperty(@Nonnull String key, @Nullable T defaultValue) {
		return (T) this.properties.getOrDefault(key, defaultValue);
	}

	@Override
	@Nonnull
	public Map<String, Object> getProperties() {
		return Collections.unmodifiableMap(this.properties);
	}
}