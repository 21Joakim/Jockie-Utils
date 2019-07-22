package com.jockie.bot.core.argument;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.jockie.bot.core.argument.parser.IArgumentParser;
import com.jockie.bot.core.argument.parser.ParsedArgument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.parser.ParseContext;

import net.dv8tion.jda.core.entities.Message;

public interface IArgument<Type> {
	
	/**
	 * @return the type of the argument
	 */
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
	public String getName();
	
	/**
	 * Used to give the user information about what happened or give them an error response,
	 * the consumer which this returns will be called when this argument is incorrectly parsed
	 * 
	 * @return an error consumer
	 */
	public BiConsumer<Message, String> getErrorConsumer();
	
	/**
	 * @param commandEvent the context to the default from
	 * 
	 * @return the default argument
	 */
	public Type getDefault(CommandEvent commandEvent);
	
	/**
	 * @return the parser used to to parse the content provided by the command parser
	 */
	public IArgumentParser<Type> getParser();
	
	/**
	 * A default method using this argument's parser ({@link #getParser()})
	 * to parse the content provided
	 *  
	 * @param context the context
	 * @param content the content to parse
	 * 
	 * @return the parsed argument
	 */
	public default ParsedArgument<Type> parse(ParseContext context, String content) {
		return this.getParser().parse(context, this, content);
	}
	
	public abstract class Builder<Type, ArgumentType extends IArgument<Type>, BuilderType extends Builder<Type, ArgumentType, BuilderType>> {
		
		protected final Class<Type> type;
		
		protected boolean endless, empty, quote = true;
		
		protected String name;
		
		protected BiConsumer<Message, String> errorConsumer;
		
		protected Function<CommandEvent, Type> defaultValueFunction;
		
		protected IArgumentParser<Type> parser;
		
		protected Builder(Class<Type> type)  {
			this.type = type;
		}
		
		public Class<Type> getType() {
			return this.type;
		}
		
		public BuilderType setEndless(boolean endless) {
			this.endless = endless;
			
			return this.self();
		}
		
		public BuilderType setAcceptEmpty(boolean empty) {
			this.empty = empty;
			
			return this.self();
		}
		
		public BuilderType setAcceptQuote(boolean quote) {
			this.quote = quote;
			
			return this.self();
		}
		
		public BuilderType setName(String name) {
			this.name = name;
			
			return this.self();
		}
		
		public BuilderType setErrorConsumer(BiConsumer<Message, String> consumer) {
			this.errorConsumer = consumer;
			
			return this.self();
		}
		
		public BuilderType setErrorFunction(BiFunction<Message, String, String> function) {
			if(function != null) {
				this.errorConsumer = (message, content) -> {
					message.getChannel().sendMessage(function.apply(message, content)).queue();
				};
			}else{
				this.errorConsumer = null;
			}
			
			return this.self();
		}
		
		public BuilderType setErrorMessage(String errorMessage) {
			if(errorMessage != null) {
				this.errorConsumer = (message, content) -> {
					message.getChannel().sendMessage(String.format(errorMessage, content)).queue();
				};
			}else{
				this.errorConsumer = null;
			}
			
			return this.self();
		}
		
		public BuilderType setDefaultValue(Function<CommandEvent, Type> defaultValueFunction) {
			this.defaultValueFunction = defaultValueFunction;
			
			return this.self();
		}
		
		public BuilderType setDefaultValue(Type defaultValue) {
			return this.setDefaultValue((event) -> {
				return defaultValue;
			});
		}
		
		public BuilderType setDefaultAsNull() {			
			return this.setDefaultValue((event) -> null);
		}
		
		public BuilderType setParser(IArgumentParser<Type> parser) {
			this.parser = parser;
			
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
		
		public String getName() {
			return this.name;
		}
		
		public BiConsumer<Message, String> getErrorConsumer() {
			return this.errorConsumer;
		}
		
		public Function<CommandEvent, Type> getDefaultValueFunction() {
			return this.defaultValueFunction;
		}
		
		public IArgumentParser<Type> getParser() {
			return this.parser;
		}
		
		public abstract BuilderType self();
		
		public abstract ArgumentType build();
		
	}
}