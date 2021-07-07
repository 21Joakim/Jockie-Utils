package com.jockie.bot.core.option.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.option.IOption;
import com.jockie.bot.core.parser.IParser;

public class OptionImpl<Type> implements IOption<Type> {
	
	public static class Builder<Type> extends IOption.Builder<Type, OptionImpl<Type>, Builder<Type>> {
		
		public Builder(Class<Type> type) {
			super(type);
		}
		
		@Override
		public Builder<Type> self() {
			return this;
		}
		
		@Override
		public OptionImpl<Type> build() {
			return new OptionImpl<>(this);
		}
	}
	
	private final Class<Type> type;
	
	private final String name;
	
	private final String description;
	
	private final List<String> aliases;
	
	private final boolean hidden;
	private final boolean developer;
	
	private final IParser<Type, IOption<Type>> parser;
	
	private final Function<CommandEvent, Type> defaultValueFunction;
	
	private final Map<String, Object> properties;
	
	protected <BuilderType extends IOption.Builder<Type, ?, ?>> OptionImpl(BuilderType builder) {
		this.type = builder.getType();
		this.name = builder.getName();
		this.description = builder.getDescription();
		this.aliases = Collections.unmodifiableList(new ArrayList<>(builder.getAliases()));
		this.hidden = builder.isHidden();
		this.developer = builder.isDeveloper();
		this.parser = builder.getParser();
		this.defaultValueFunction = builder.getDefaultValueFunction();
		this.properties = new HashMap<>(builder.getProperties());
	}
	
	@Override
	public Class<Type> getType() {
		return this.type;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public String getDescription() {
		return this.description;
	}
	
	@Override
	public List<String> getAliases() {
		return this.aliases;
	}
	
	@Override
	public boolean isHidden() {
		return this.hidden;
	}
	
	@Override
	public boolean isDeveloper() {
		return this.developer;
	}
	
	@Override
	public IParser<Type, IOption<Type>> getParser() {
		return this.parser;
	}

	@Override
	public boolean hasDefault() {
		return this.defaultValueFunction != null;
	}

	@Override
	public Type getDefault(CommandEvent event) {
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
	public Map<String, Object> getProperties() {
		return Collections.unmodifiableMap(this.properties);
	}
}