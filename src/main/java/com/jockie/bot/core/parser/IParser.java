package com.jockie.bot.core.parser;

import javax.annotation.Nonnull;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.impl.json.JSONArrayParser;
import com.jockie.bot.core.parser.impl.json.JSONObjectParser;

@FunctionalInterface
public interface IParser<Type, Component> {
	
	/**
	 * Parse a component
	 * 
	 * @param context the context
	 * @param component the component this parser is attached to
	 * @param content the content to parse
	 * 
	 * @return the parsed component
	 */
	@Nonnull
	public ParsedResult<Type> parse(@Nonnull ParseContext context, @Nonnull Component component, @Nonnull String content);
	
	/**
	 * <b>NOTE:</b>
	 * By using this property in a parser you have to yourself return any left over content in the {@link ParsedResult}.
	 * <br><br>
	 * This can, for instance, be useful for creating JSON parsers where the data
	 * has a definite start and end, example {@link JSONObjectParser} and {@link JSONArrayParser}
	 * 
	 * @return whether or not this parser should manage all the content itself, 
	 * this means that all the content which is left to parse for the command parser 
	 * will be given to this parser.
	 */
	public default boolean isHandleAll() {
		return false;
	}
}