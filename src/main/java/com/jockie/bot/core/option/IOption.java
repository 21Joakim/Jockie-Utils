package com.jockie.bot.core.option;

import com.jockie.bot.core.command.impl.CommandListener;

public interface IOption {
	
	public String getName();
	
	public String getDescription();
	
	/**
	 * @return all the possible aliases for this option
	 */
	public String[] getAliases();
	
	public boolean isHidden();
	
	/**
	 * @return whether or not this option is limited to users who match {@link CommandListener#isDeveloper(long)}
	 */
	public boolean isDeveloper();
	
	public abstract class Builder<Type extends IOption, BuilderType extends Builder<Type, BuilderType>> {
		
		protected String name;
		
		protected String description;
		
		protected String[] aliases;
		
		protected boolean hidden;
		protected boolean developerOption;
		
		public BuilderType setName(String name) {
			this.name = name;
			
			return this.self();
		}
		
		public BuilderType setDescription(String description) {
			this.description = description;
			
			return this.self();
		}
		
		public BuilderType setAliases(String[] aliases) {
			this.aliases = aliases;
			
			return this.self();
		}
		
		public BuilderType setHidden(boolean hidden) {
			this.hidden = hidden;
			
			return this.self();
		}
		
		public BuilderType setDeveloper(boolean developerOption) {
			this.developerOption = developerOption;
			
			return this.self();
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
		
		public boolean isDeveloperOption() {
			return this.developerOption;
		}
		
		public abstract BuilderType self();
		
		public abstract Type build();
	}
}