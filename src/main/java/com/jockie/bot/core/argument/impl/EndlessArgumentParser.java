package com.jockie.bot.core.argument.impl;

import java.lang.reflect.Array;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.parser.IArgumentParser;
import com.jockie.bot.core.argument.parser.ParsedArgument;

import net.dv8tion.jda.api.entities.Message;

public class EndlessArgumentParser<Type> implements IArgumentParser<Type[]> {
	
	public static final EndlessArgumentParser<Object> INSTANCE = new EndlessArgumentParser<>();
	
	@SuppressWarnings("unchecked")
	public static <T> EndlessArgumentParser<T> getInstance() {
		return (EndlessArgumentParser<T>) INSTANCE;
	}
	
	private EndlessArgumentParser() {}
	
	@SuppressWarnings("unchecked")
	/* TODO: Probably need to look over and re-make this */
	public ParsedArgument<Type[]> parse(Message message, IArgument<Type[]> argument, String value) {
		if(!(argument instanceof EndlessArgumentImpl)) {
			throw new UnsupportedOperationException();
		}
		
		EndlessArgumentImpl<Type> self = (EndlessArgumentImpl<Type>) argument;
		
		int argumentCount = 0;
		
		Type[] parsedArguments = (Type[]) Array.newInstance(self.getArgumentType(), self.getMaxArguments() > 0 ? self.getMaxArguments() : (int) value.codePoints().filter(c2 -> c2 == ' ').count() + 1);
		
		for(int i = 0; i < parsedArguments.length; i++) {
			if(value.trim().length() == 0) {
				break;
			}
			
			if(i != 0 && value.length() > 0) {
				if(value.startsWith(" ")) {
					value = value.substring(1);
				}else{
					/* When does this happen? */
					
					return new ParsedArgument<>(false, null);
				}
			}
			
			String content = null;
			ParsedArgument<Type> parsedArgument;
			if(self.getArgument().getParser().isHandleAll()) {
				parsedArgument = self.getArgument().parse(message, content = value);
				
				if(parsedArgument.getContentLeft() != null) {
					value = parsedArgument.getContentLeft();
				}else{
					value = "";
				}
			}else{
				if(value.length() > 0) {
					if(self.getArgument().acceptQuote()) {
						if(value.charAt(0) == '"') {
							int nextQuote = 0;
							while((nextQuote = value.indexOf('"', nextQuote + 1)) != -1 && value.charAt(nextQuote - 1) == '\\');
							
							if(nextQuote != -1) {
								content = value.substring(1, nextQuote);
								
								value = value.substring(content.length() + 2);
								
								content = content.replace("\\\"", "\"");
							}
						}
					}
					
					if(content == null) {
						content = value.substring(0, (value.contains(" ")) ? value.indexOf(" ") : value.length());
						value = value.substring(content.length());
					}
				}else{
					content = "";
				}
				
				if(content.length() == 0 && !self.getArgument().acceptEmpty()) {
					/* Content may not be empty */
					return new ParsedArgument<>(false, null);
				}
				
				parsedArgument = self.getArgument().parse(message, content);
			}
			
			if(parsedArgument.isValid()) {
				parsedArguments[argumentCount++] = (Type) parsedArgument.getObject();
			}else{
				/* "argument at index " + (i + 1) + " is not valid" */
				return new ParsedArgument<>(false, null);
			}
		}
		
		if(value.length() > 0) {
			/* Content overflow, when does this happen? */
			
			return new ParsedArgument<>(false, null);
		}
		
		if(argumentCount < self.getMinArguments() || ((self.getMaxArguments() > 0) ? argumentCount > self.getMaxArguments() : false)) {
			return new ParsedArgument<>(false, null);
		}
		
		Type[] objects = (Type[]) Array.newInstance(self.getArgumentType(), argumentCount);
		for(int i2 = 0; i2 < objects.length; i2++) {
			objects[i2] = (Type) parsedArguments[i2];
		}
		
		return new ParsedArgument<>(true, objects);
	}
}