package com.jockie.bot.core.parser;

import javax.annotation.Nonnull;

import com.jockie.bot.core.command.parser.ParseContext;

public interface IParsableComponent<Type, Component extends IParsableComponent<Type, Component>> {
	
	/**
	 * @return the type of the component
	 */
	public Class<Type> getType();
	
	/**
	 * @return the parser used to to parse the component content
	 */
	public IParser<Type, Component> getParser();
	
	/**
	 * A default method using this parser ({@link #getParser()}) to parse the content provided
	 *  
	 * @param context the context
	 * @param content the content to parse
	 * 
	 * @return the parsed argument
	 */
	@SuppressWarnings("unchecked")
	@Nonnull
	public default ParsedResult<Type> parse(@Nonnull ParseContext context, @Nonnull String content) {
		return this.getParser().parse(context, (Component) this, content);
	}
}