package com.jockie.bot.core.argument;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParsableComponent;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;
import com.jockie.bot.core.property.IPropertyContainer;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.internal.utils.Checks;

public interface IArgument<Type> extends IPropertyContainer, IParsableComponent<Type, IArgument<Type>> {
	
	/**
	 * @return the type of the argument
	 */
	@Nonnull
	public Class<Type> getType();
	
	/**
	 * @return whether or not this argument should take all the remaining content
	 * when parsing the command and pass it to the parser
	 */
	public boolean isEndless();
	
	/**
	 * @return whether or not this argument should allow for quoted content to be 
	 * passed when parsing the command, quoted content is content which is within two 
	 * quotes, like so "hello there"
	 */
	public boolean acceptQuote();
	
	/**
	 * @return whether or not this argument should allow for empty content to be 
	 * passed when parsing the command, empty content could either be nothing at all
	 * or two quotes with nothing inside it if this accepts quoted content ({@link #acceptQuote()})
	 */
	public boolean acceptEmpty();
	
	/**
	 * @return weather or not this argument has a default value
	 */
	public boolean hasDefault();
	
	/**
	 * @return the name of this argument
	 */
	@Nonnull
	public String getName();
	
	/**
	 * Used to give the user information about what happened or give them an error response,
	 * the consumer which this returns will be called when this argument is incorrectly parsed
	 * 
	 * @return an error consumer
	 */
	@Nullable
	public BiConsumer<Message, String> getErrorConsumer();
	
	/**
	 * @param commandEvent the context to the default from
	 * 
	 * @return the default argument
	 */
	@Nullable
	public Type getDefault(@Nonnull CommandEvent commandEvent);
	
	/**
	 * @return the parser used to to parse the content provided by the command parser
	 */
	@Nonnull
	public IParser<Type, IArgument<Type>> getParser();
	
	/**
	 * A default method using this argument's parser ({@link #getParser()})
	 * to parse the content provided
	 *  
	 * @param context the context
	 * @param content the content to parse
	 * 
	 * @return the parsed argument
	 */
	@Nonnull
	public default ParsedResult<Type> parse(@Nonnull ParseContext context, @Nonnull String content) {
		return this.getParser().parse(context, this, content);
	}
	
	public abstract class Builder<Type, ArgumentType extends IArgument<Type>, BuilderType extends Builder<Type, ArgumentType, BuilderType>> {
		
		protected final Class<Type> type;
		
		protected boolean endless, empty, quote = true;
		
		protected String name;
		
		protected BiConsumer<Message, String> errorConsumer;
		
		protected Function<CommandEvent, Type> defaultValueFunction;
		
		protected IParser<Type, IArgument<Type>> parser;
		
		protected Map<String, Object> properties = new HashMap<>();
		
		protected Builder(@Nonnull Class<Type> type) {
			Checks.notNull(type, "type");
			
			this.type = type;
		}
		
		@Nonnull
		public Class<Type> getType() {
			return this.type;
		}
		
		@Nonnull
		public BuilderType setEndless(boolean endless) {
			this.endless = endless;
			
			return this.self();
		}
		
		@Nonnull
		public BuilderType setAcceptEmpty(boolean empty) {
			this.empty = empty;
			
			return this.self();
		}
		
		@Nonnull
		public BuilderType setAcceptQuote(boolean quote) {
			this.quote = quote;
			
			return this.self();
		}
		
		@Nonnull
		public BuilderType setName(@Nullable String name) {
			this.name = name;
			
			return this.self();
		}
		
		@Nonnull
		public BuilderType setErrorConsumer(@Nullable BiConsumer<Message, String> consumer) {
			this.errorConsumer = consumer;
			
			return this.self();
		}
		
		@Nonnull
		public BuilderType setErrorFunction(@Nullable BiFunction<Message, String, String> function) {
			if(function != null) {
				this.errorConsumer = (message, content) -> message.getChannel().sendMessage(function.apply(message, content)).queue();
			}else{
				this.errorConsumer = null;
			}
			
			return this.self();
		}
		
		@Nonnull
		public BuilderType setErrorMessage(@Nullable String errorMessage) {
			if(errorMessage != null) {
				this.errorConsumer = (message, content) -> message.getChannel().sendMessage(String.format(errorMessage, content)).queue();
			}else{
				this.errorConsumer = null;
			}
			
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
		
		@Nonnull
		public BuilderType setParser(@Nullable IParser<Type, IArgument<Type>> parser) {
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
		
		public boolean isEndless() {
			return this.endless;
		}
		
		public boolean isAcceptEmpty() {
			return this.empty;
		}
		
		public boolean isAcceptQuote() {
			return this.quote;
		}
		
		@Nullable
		public String getName() {
			return this.name;
		}
		
		@Nullable
		public BiConsumer<Message, String> getErrorConsumer() {
			return this.errorConsumer;
		}
		
		@Nullable
		public Function<CommandEvent, Type> getDefaultValueFunction() {
			return this.defaultValueFunction;
		}
		
		@Nullable
		public IParser<Type, IArgument<Type>> getParser() {
			return this.parser;
		}
		
		@Nullable
		public Map<String, Object> getProperties() {
			return this.properties;
		}
		
		@Nonnull
		public abstract BuilderType self();
		
		@Nonnull
		public abstract ArgumentType build();
		
	}
}