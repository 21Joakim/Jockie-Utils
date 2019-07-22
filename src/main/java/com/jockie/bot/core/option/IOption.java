package com.jockie.bot.core.option;

import com.jockie.bot.core.command.impl.CommandListener;

public interface IOption<Type> {
	
	/**
	 * @return the type of the option
	 */
	public Class<Type> getType();
	
	/**
	 * @return the name of this option, used to determine whether the option is used or not
	 */
	public String getName();
	
	/**
	 * @return the description of this option, could for instance explain the behaviour of the option
	 */
	public String getDescription();
	
	/**
	 * @return all the possible aliases for this option
	 */
	public String[] getAliases();
	
	/**
	 * @return whether or not this option should be hidden
	 */
	public boolean isHidden();
	
	/**
	 * @return whether or not this option is limited to users who match {@link CommandListener#isDeveloper(long)}
	 */
	public boolean isDeveloper();
	
	public abstract class Builder<Type, ReturnType extends IOption<Type>, BuilderType extends Builder<Type, ReturnType, BuilderType>> {
		
		protected final Class<Type> type;
		
		protected String name;
		
		protected String description;
		
		protected String[] aliases;
		
		protected boolean hidden;
		protected boolean developerOption;
		
		public Builder(Class<Type> type) {
			this.type = type;
		}
		
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
		
		public boolean isDeveloperOption() {
			return this.developerOption;
		}
		
		public abstract BuilderType self();
		
		public abstract ReturnType build();
	}
}