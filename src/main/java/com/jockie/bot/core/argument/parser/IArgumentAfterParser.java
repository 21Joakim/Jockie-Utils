package com.jockie.bot.core.argument.parser;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.command.parser.ParseContext;

@FunctionalInterface
public interface IArgumentAfterParser<Type> {
	
	/**
	 * Modify the content after it has been parsed
	 * 
	 * @param context the context
	 * @param argument the argument this parser is attached to
	 * @param content the content to parse
	 * 
	 * @return the modified content
	 */
	public ParsedArgument<Type> parse(ParseContext context, IArgument<Type> argument, Type content);
}