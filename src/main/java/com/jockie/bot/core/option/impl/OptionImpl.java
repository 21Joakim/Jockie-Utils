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
		@Nonnull
		public Builder<Type> self() {
			return this;
		}
		
		@Override
		@Nonnull
		public OptionImpl<Type> build() {
			return new OptionImpl<>(this);
		}
	}
	
	protected final Class<Type> type;
	
	protected final String name;
	protected final String description;
	
	protected final List<String> aliases;
	
	protected final boolean hidden;
	protected final boolean developer;
	
	protected final IParser<Type, IOption<Type>> parser;
	
	protected final Function<CommandEvent, Type> defaultValueFunction;
	
	protected final Map<String, Object> properties;
	
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
	@Nonnull
	public IParser<Type, IOption<Type>> getParser() {
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