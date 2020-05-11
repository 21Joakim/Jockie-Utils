package com.jockie.bot.core.option.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jockie.bot.core.option.IOption;

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
			return new OptionImpl<Type>(this.type, this.name, this.description, this.aliases, this.hidden, this.developer);
		}
	}
	
	private final Class<Type> type;
	
	private final String name;
	
	private final String description;
	
	private final List<String> aliases;
	
	private final boolean hidden;
	private final boolean developer;
	
	private OptionImpl(Class<Type> type, String name, String description, List<String> aliases, boolean hidden, boolean developer) {
		this.type = type;
		this.name = name;
		this.description = description;
		this.aliases = Collections.unmodifiableList(new ArrayList<>(aliases));
		this.hidden = hidden;
		this.developer = developer;
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
}