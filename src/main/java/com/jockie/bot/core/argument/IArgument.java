package com.jockie.bot.core.argument;

import java.util.function.Function;

import com.jockie.bot.core.argument.parser.IArgumentParser;
import com.jockie.bot.core.argument.parser.ParsedArgument;
import com.jockie.bot.core.command.impl.CommandEvent;

import net.dv8tion.jda.core.entities.Message;

public interface IArgument<Type> {
	
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
	 * @param message the context
	 * @param content the content to parse
	 * 
	 * @return the parsed argument
	 */
	public default ParsedArgument<Type> parse(Message message, String content) {
		return this.getParser().parse(message, this, content);
	}
	
	public abstract class Builder<RT, A extends IArgument<RT>, BT extends Builder<RT, A, BT>> {
		
		/* I see no reason not to allow quoted by default */
		protected boolean endless, empty, quote = true;
		
		protected String name, error;
		
		protected Function<CommandEvent, RT> defaultValueFunction;
		
		protected IArgumentParser<RT> parser;
		
		public BT setEndless(boolean endless) {
			this.endless = endless;
			
			return this.self();
		}
		
		public BT setAcceptEmpty(boolean empty) {
			this.empty = empty;
			
			return this.self();
		}
		
		public BT setAcceptQuote(boolean quote) {
			this.quote = quote;
			
			return this.self();
		}
		
		public BT setName(String name) {
			this.name = name;
			
			return this.self();
		}
		
		public BT setDefaultValue(Function<CommandEvent, RT> defaultValueFunction) {
			this.defaultValueFunction = defaultValueFunction;
			
			return this.self();
		}
		
		public BT setDefaultValue(RT defaultValue) {
			return this.setDefaultValue((commandEvent) -> {
				return defaultValue;
			});
		}
		
		public BT setDefaultAsNull() {			
			return this.setDefaultValue((a) -> null);
		}
		
		public BT setParser(IArgumentParser<RT> parser) {
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
		
		public Function<CommandEvent, RT> getDefaultValueFunction() {
			return this.defaultValueFunction;
		}
		
		public IArgumentParser<RT> getParser() {
			return this.parser;
		}
		
		public abstract BT self();
		
		public abstract A build();
	}
}