package com.jockie.bot.core.option;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandListener;
import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParsableComponent;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;
import com.jockie.bot.core.property.IPropertyContainer;

import net.dv8tion.jda.internal.utils.Checks;

public interface IOption<Type> extends IPropertyContainer, IParsableComponent<Type, IOption<Type>> {
	
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
	
	/**
	 * @return weather or not this option has a default value
	 */
	public boolean hasDefault();
	
	/**
	 * @param commandEvent the context to the default from
	 * 
	 * @return the default option
	 */
	@Nonnull
	public Type getDefault(@Nonnull CommandEvent commandEvent);
	
	/**
	 * @return the parser used to to parse the content provided by the command parser
	 */
	@Nonnull
	public IParser<Type, IOption<Type>> getParser();
	
	/**
	 * A default method using this option's parser ({@link #getParser()})
	 * to parse the content provided
	 *  
	 * @param context the context
	 * @param content the content to parse
	 * 
	 * @return the parsed option
	 */
	@Nonnull
	public default ParsedResult<Type> parse(@Nonnull ParseContext context, @Nonnull String content) {
		return this.getParser().parse(context, this, content);
	}
	
	public abstract class Builder<Type, ReturnType extends IOption<Type>, BuilderType extends Builder<Type, ReturnType, BuilderType>> {
		
		protected final Class<Type> type;
		
		protected String name;
		
		protected String description;
		
		protected List<String> aliases = new ArrayList<>();
		
		protected boolean hidden;
		protected boolean developer;
		
		protected Function<CommandEvent, Type> defaultValueFunction;
		
		protected IParser<Type, IOption<Type>> parser;
		
		protected Map<String, Object> properties = new HashMap<>();
		
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
		public BuilderType setDefaultValue(@Nullable Function<CommandEvent, Type> defaultValueFunction) {
			this.defaultValueFunction = defaultValueFunction;
			
			return this.self();
		}
		
		@Nonnull
		public BuilderType setDefaultValue(@Nullable Type defaultValue) {
			return this.setDefaultValue((event) -> defaultValue);
		}
		
		@Nonnull
		public BuilderType setDefaultAsNull() {			
			return this.setDefaultValue((event) -> null);
		}
		
		public BuilderType setParser(IParser<Type, IOption<Type>> parser) {
			this.parser = parser;
			
			return this.self();
		}
		
		@Nonnull
		public BuilderType setProperties(@Nullable Map<String, Object> properties) {
			this.properties = properties != null ? properties : new HashMap<>();
			
			return this.self();
		}
		
		@Nonnull
		public <T> BuilderType setProperty(@Nonnull String key, @Nullable T value) {
			this.properties.put(key, value);
			
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
		
		@Nullable
		public Function<CommandEvent, Type> getDefaultValueFunction() {
			return this.defaultValueFunction;
		}
		
		@Nullable
		public IParser<Type, IOption<Type>> getParser() {
			return this.parser;
		}
		
		@Nullable
		public Map<String, Object> getProperties() {
			return this.properties;
		}
		
		@Nonnull
		public abstract BuilderType self();
		
		@Nonnull
		public abstract ReturnType build();
	}
}