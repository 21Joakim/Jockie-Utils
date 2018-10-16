package com.jockie.bot.core.option.impl;

import com.jockie.bot.core.option.IOption;

public class OptionImpl implements IOption {
	
	private final String name;
	private final String[] aliases;
	
	private final boolean hidden;
	private final boolean developerOption;
	
	private OptionImpl(String name, String[] aliases, boolean hidden, boolean developerOption) {
		this.name = name;
		this.aliases = aliases;
		this.hidden = hidden;
		this.developerOption = developerOption;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String[] getAliases() {
		return this.aliases;
	}
	
	public boolean isHidden() {
		return this.hidden;
	}
	
	public boolean isDeveloperOption() {
		return this.developerOption;
	}
	
	public static class Builder extends IOption.Builder<OptionImpl, Builder> {
		
		public Builder self() {
			return this;
		}
		
		public OptionImpl build() {
			return new OptionImpl(this.name, this.aliases, this.hidden, this.developerOption);
		}
	}
}