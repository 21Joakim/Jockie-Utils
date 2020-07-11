package com.jockie.bot.core.parser;

import com.jockie.bot.core.command.parser.ParseContext;

@FunctionalInterface
public interface IBeforeParser<Component> {
	
	/**
	 * Modify the content before it gets parsed
	 * 
	 * @param context the context
	 * @param component the component this parser is attached to
	 * @param content the content to parse
	 * 
	 * @return the modified content
	 */
	public ParsedResult<String> parse(ParseContext context, Component component, String content);
}