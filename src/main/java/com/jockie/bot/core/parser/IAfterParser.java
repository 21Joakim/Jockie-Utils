package com.jockie.bot.core.parser;

import com.jockie.bot.core.command.parser.ParseContext;

@FunctionalInterface
public interface IAfterParser<Type, Component> {
	
	/**
	 * Modify the content after it has been parsed
	 * 
	 * @param context the context
	 * @param component the component this parser is attached to
	 * @param content the content to parse
	 * 
	 * @return the modified content
	 */
	public ParsedResult<Type> parse(ParseContext context, Component component, Type content);
	
}