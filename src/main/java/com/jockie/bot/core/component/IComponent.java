package com.jockie.bot.core.component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParsableComponent;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;
import com.jockie.bot.core.property.IPropertyContainer;

import net.dv8tion.jda.internal.utils.Checks;

public interface IComponent<Type, Component extends IComponent<Type, Component>> extends IParsableComponent<Type, Component>, IPropertyContainer {
	
	/**
	 * @return the type of the component
	 */
	@Nonnull
	public Class<Type> getType();
	
	/**
	 * @return the name of this component, this name may need to be referenced by the user
	 */
	@Nonnull
	public String getName();
	
	/**
	 * @return the description of this component, this could for instance, explain what happens when it is used
	 */
	@Nullable
	public String getDescription();
	
	/**
	 * @return weather or not this component has a default value
	 */
	public boolean hasDefault();
	
	/**
	 * @param event the context to the default from
	 * 
	 * @return the default component value
	 */
	@Nullable
	public Type getDefault(@Nonnull CommandEvent commandEvent);
	
	/**
	 * @return the parser used to to parse the content provided by the command parser
	 */
	@Nonnull
	public IParser<Type, Component> getParser();
	
	/**
	 * A default method using this component's parser ({@link #getParser()}) to parse the provided content
	 *  
	 * @param context the context
	 * @param content the content to parse
	 * 
	 * @return the parsed option
	 */
	@SuppressWarnings("unchecked")
	@Nonnull
	public default ParsedResult<Type> parse(@Nonnull ParseContext context, @Nonnull String content) {
		return this.getParser().parse(context, (Component) this, content);
	}
	
	public abstract class Builder<Type, ComponentType extends IComponent<Type, ?>, ReturnType extends ComponentType, BuilderType extends IComponent.Builder<Type, ComponentType, ReturnType, BuilderType>> {
		
		protected final Class<Type> type;
		
		protected String name;
		protected String description;
		
		protected Function<CommandEvent, Type> defaultValueFunction;
		
		protected IParser<Type, ComponentType> parser;
		
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
		
		public BuilderType setParser(IParser<Type, ComponentType> parser) {
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
		
		@Nullable
		public String getName() {
			return this.name;
		}
		
		@Nullable
		public String getDescription() {
			return this.description;
		}
		
		@Nullable
		public Function<CommandEvent, Type> getDefaultValueFunction() {
			return this.defaultValueFunction;
		}
		
		@Nullable
		public IParser<Type, ComponentType> getParser() {
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