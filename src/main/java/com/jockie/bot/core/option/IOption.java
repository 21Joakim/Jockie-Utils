package com.jockie.bot.core.option;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.command.impl.CommandListener;

import net.dv8tion.jda.internal.utils.Checks;

public interface IOption<Type> {
	
	/**
	 * @return the type of the option
	 */
	@Nonnull
	public Class<Type> getType();
	
	/**
	 * @return the name of this option, used to determine whether the option is used or not
	 */
	@Nonnull
	public String getName();
	
	/**
	 * @return the description of this option, could for instance explain the behaviour of the option
	 */
	@Nullable
	public String getDescription();
	
	/**
	 * @return all the possible aliases for this option
	 */
	@Nonnull
	public List<String> getAliases();
	
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
		
		protected List<String> aliases = new ArrayList<>();
		
		protected boolean hidden;
		protected boolean developer;
		
		public Builder(@Nonnull Class<Type> type) {
			Checks.notNull(type, "type");
			
			this.type = type;
		}
		
		@Nonnull
		public BuilderType setName(@Nonnull String name) {
			Checks.notNull(name, "name");
			
			this.name = name;
			
			return this.self();
		}
		
		@Nonnull
		public BuilderType setDescription(@Nullable String description) {
			this.description = description;
			
			return this.self();
		}
		
		@Nonnull
		public BuilderType setAliases(@Nonnull String... aliases) {
			Checks.noneNull(aliases, "aliases");
			
			this.aliases.clear();
			
			for(String alias : aliases) {
				this.aliases.add(alias);
			}
			
			return this.self();
		}
		
		@Nonnull
		public BuilderType setAliases(@Nonnull List<String> aliases) {
			Checks.noneNull(aliases, "aliases");
			
			this.aliases.clear();
			this.aliases.addAll(aliases);
			
			return this.self();
		}
		
		@Nonnull
		public BuilderType setHidden(boolean hidden) {
			this.hidden = hidden;
			
			return this.self();
		}
		
		@Nonnull
		public BuilderType setDeveloper(boolean developer) {
			this.developer = developer;
			
			return this.self();
		}
		
		@Nonnull
		public Class<Type> getType() {
			return this.type;
		}
		
		@Nonnull
		public String getName() {
			return this.name;
		}
		
		@Nullable
		public String getDescription() {
			return this.description;
		}
		
		@Nonnull
		public List<String> getAliases() {
			return this.aliases;
		}
		
		public boolean isHidden() {
			return this.hidden;
		}
		
		public boolean isDeveloper() {
			return this.developer;
		}
		
		@Nonnull
		public abstract BuilderType self();
		
		@Nonnull
		public abstract ReturnType build();
	}
}