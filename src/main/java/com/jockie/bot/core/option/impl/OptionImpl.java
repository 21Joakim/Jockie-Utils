package com.jockie.bot.core.option.impl;

import com.jockie.bot.core.option.IOption;

public class OptionImpl<Type> implements IOption<Type> {
	
	public static class Builder<Type> extends IOption.Builder<Type, OptionImpl<Type>, Builder<Type>> {
		
		public Builder(Class<Type> type) {
			super(type);
		}

		public Builder<Type> self() {
			return this;
		}
		
		public OptionImpl<Type> build() {
			return new OptionImpl<Type>(this.type, this.name, this.description, this.aliases, this.hidden, this.developerOption);
		}
	}
	
	private final Class<Type> type;
	
	private final String name;
	
	private final String description;
	
	private final String[] aliases;
	
	private final boolean hidden;
	private final boolean developer;
	
	private OptionImpl(Class<Type> type, String name, String description, String[] aliases, boolean hidden, boolean developer) {
		this.type = type;
		this.name = name;
		this.description = description;
		this.aliases = aliases;
		this.hidden = hidden;
		this.developer = developer;
	}
	
	public Class<Type> getType() {
		return this.type;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public String[] getAliases() {
		return this.aliases;
	}
	
	public boolean isHidden() {
		return this.hidden;
	}
	
	public boolean isDeveloper() {
		return this.developer;
	}
}