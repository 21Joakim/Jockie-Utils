package com.jockie.bot.core.argument.parser;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.command.parser.ParseContext;

@FunctionalInterface
public interface IArgumentBeforeParser<T> {
	
	/**
	 * Modify the content before it gets parsed
	 * 
	 * @param context the context
	 * @param argument the argument this parser is attached to
	 * @param content the content to parse
	 * 
	 * @return the modified content
	 */
	public ParsedArgument<String> parse(ParseContext context, IArgument<T> argument, String content);
}