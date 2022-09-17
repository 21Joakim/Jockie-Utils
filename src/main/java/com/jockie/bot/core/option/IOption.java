package com.jockie.bot.core.option;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.jockie.bot.core.command.impl.CommandListener;
import com.jockie.bot.core.component.IComponent;

import net.dv8tion.jda.internal.utils.Checks;

public interface IOption<Type> extends IComponent<Type, IOption<Type>> {
	
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
	
	public abstract class Builder<Type, ReturnType extends IOption<Type>, BuilderType extends Builder<Type, ReturnType, BuilderType>> extends IComponent.Builder<Type, IOption<Type>, ReturnType, BuilderType> {
		
		protected List<String> aliases = new ArrayList<>();
		
		protected boolean hidden;
		protected boolean developer;
		
		public Builder(@Nonnull Class<Type> type) {
			super(type);
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
		public List<String> getAliases() {
			return this.aliases;
		}
		
		public boolean isHidden() {
			return this.hidden;
		}
		
		public boolean isDeveloper() {
			return this.developer;
		}
	}
}