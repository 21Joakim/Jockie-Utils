package com.jockie.bot.core.option.impl;

import com.jockie.bot.core.option.IOption;

public class OptionImpl implements IOption {
	
	private final String name;
	
	private final String description;
	
	private final String[] aliases;
	
	private final boolean hidden;
	private final boolean developer;
	
	private OptionImpl(String name, String description, String[] aliases, boolean hidden, boolean developer) {
		this.name = name;
		this.description = description;
		this.aliases = aliases;
		this.hidden = hidden;
		this.developer = developer;
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
	
	public static class Builder extends IOption.Builder<OptionImpl, Builder> {
		
		public Builder self() {
			return this;
		}
		
		public OptionImpl build() {
			return new OptionImpl(this.name, this.description, this.aliases, this.hidden, this.developerOption);
		}
	}
}