package com.jockie.bot.core.option;

public interface IOption {
	
	public String getName();
	
	public String[] getAliases();
	
	public boolean isHidden();
	public boolean isDeveloperOption();
	
	public abstract class Builder<Type extends IOption, BuilderType extends Builder<Type, BuilderType>> {
		
		protected String name;
		protected String[] aliases;
		
		protected boolean hidden;
		protected boolean developerOption;
		
		public BuilderType setName(String name) {
			this.name = name;
			
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
		
		public BuilderType setDeveloperOption(boolean developerOption) {
			this.developerOption = developerOption;
			
			return this.self();
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
		
		public abstract BuilderType self();
		
		public abstract Type build();
	}
}