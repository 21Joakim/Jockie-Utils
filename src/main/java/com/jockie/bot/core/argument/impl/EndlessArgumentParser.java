package com.jockie.bot.core.argument.impl;

import java.lang.reflect.Array;
import java.util.Collection;

import javax.annotation.Nonnull;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.command.ICommand.ArgumentTrimType;
import com.jockie.bot.core.command.parser.ICommandParser;
import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.command.parser.impl.CommandParserImpl;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;
import com.jockie.bot.core.utility.StringUtility;
import com.jockie.bot.core.utility.StringUtility.QuoteCharacter;

public class EndlessArgumentParser<Type> implements IParser<Type[], IArgument<Type[]>> {
	
	public static final EndlessArgumentParser<Object> INSTANCE = new EndlessArgumentParser<>();
	
	@SuppressWarnings("unchecked")
	public static <T> EndlessArgumentParser<T> getInstance() {
		return (EndlessArgumentParser<T>) INSTANCE;
	}
	
	private EndlessArgumentParser() {}
	
	private Collection<QuoteCharacter> getQuoteCharacters(ParseContext context) {
		ICommandParser commandParser = context.getCommandParser();
		if(commandParser instanceof CommandParserImpl) {
			return ((CommandParserImpl) commandParser).getQuoteCharacters();
		}
		
		/* TODO: Unsure of what to do if it's not a CommandParserImpl */
		return StringUtility.DEFAULT_QUOTE_CHARACTERS;
	}
	
	@SuppressWarnings("unchecked")
	@Nonnull
	/* 
	 * TODO: Probably need to look over and re-make this to be more in line with the CommandParserImpl.
	 * Currently changing either this or CommandParserImpl may require changes to the other to keep the
	 * behaviour consistent, a setup like this can easily introduce bugs.
	 */
	public ParsedResult<Type[]> parse(@Nonnull ParseContext context, @Nonnull IArgument<Type[]> argument, @Nonnull String value) {
		if(!(argument instanceof EndlessArgumentImpl)) {
			throw new UnsupportedOperationException();
		}
		
		EndlessArgumentImpl<Type> self = (EndlessArgumentImpl<Type>) argument;
		
		int argumentCount = 0;
		int maxArguments = self.getMaxArguments() > 0 ? self.getMaxArguments() : (int) value.codePoints().filter((character) -> character == ' ').count() + 1;
		
		Type[] parsedArguments = (Type[]) Array.newInstance(self.getComponentType(), maxArguments);
		for(int i = 0; i < parsedArguments.length; i++) {
			if(value.trim().isEmpty()) {
				break;
			}
			
			if(i != 0 && !value.isEmpty()) {
				if(value.charAt(0) == ' ') {
					if(context.getCommand().getArgumentTrimType() != ArgumentTrimType.NONE) {
						value = StringUtility.stripLeading(value);
					}else{
						value = value.substring(1);
					}
				}else{
					/* 
					 * It gets here if an argument is parsed with quotes and there is a 
					 * value directly after the quotes without any spacing, like !add "15"5
					 */
					
					/* The argument for some reason does not start with a space */
					return new ParsedResult<>(false, null);
				}
			}
			
			String content = null;
			ParsedResult<Type> parsedArgument;
			if(self.getArgument().getParser().isHandleAll()) {
				parsedArgument = self.getArgument().parse(context, content = value);
				
				String contentLeft = parsedArgument.getContentLeft();
				if(contentLeft != null) {
					value = contentLeft;
				}else{
					value = "";
				}
			}else{
				if(!value.isEmpty() && self.getArgument().acceptQuote()) {
					for(QuoteCharacter quote : this.getQuoteCharacters(context)) {
						content = StringUtility.parseWrapped(value, quote.start, quote.end);
						if(content == null) {
							continue;
						}
						
						value = value.substring(content.length());
						content = StringUtility.unwrap(content, quote.start, quote.end);
						
						if(context.getCommand().getArgumentTrimType() == ArgumentTrimType.STRICT) {
							content = StringUtility.strip(content);
						}
						
						break;
					}
				}
				
				if(content == null) {
					int index = value.indexOf(' ');
					content = value.substring(0, index != -1 ? index : value.length());
					
					value = value.substring(content.length());
				}
				
				if(content.isEmpty() && !self.getArgument().acceptEmpty()) {
					/* Content may not be empty */
					return ParsedResult.invalid();
				}
				
				parsedArgument = self.getArgument().parse(context, content);
			}
			
			if(parsedArgument.isValid()) {
				parsedArguments[argumentCount++] = (Type) parsedArgument.getObject();
			}else{
				/* "argument at index " + (i + 1) + " is not valid" */
				return ParsedResult.invalid();
			}
		}
		
		if(!value.isEmpty()) {
			/* Content overflow, when does this happen? */
			return ParsedResult.invalid();
		}
		
		if(argumentCount < self.getMinArguments() || ((self.getMaxArguments() > 0) ? argumentCount > self.getMaxArguments() : false)) {
			return ParsedResult.invalid();
		}
		
		Type[] objects = (Type[]) Array.newInstance(self.getComponentType(), argumentCount);
		for(int i2 = 0; i2 < objects.length; i2++) {
			objects[i2] = (Type) parsedArguments[i2];
		}
		
		return ParsedResult.valid(objects);
	}
}