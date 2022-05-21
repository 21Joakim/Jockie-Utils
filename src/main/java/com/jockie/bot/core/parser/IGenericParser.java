package com.jockie.bot.core.parser;

import javax.annotation.Nonnull;

import com.jockie.bot.core.command.parser.ParseContext;

@FunctionalInterface
public interface IGenericParser<Type, Component extends IParsableComponent<Type, Component>> extends IParser<Type, Component> {
	
	/**
	 * Parse a generic component
	 * 
	 * <b>Note:</b> That there is no safe-guard against returning the wrong type
	 * (for instance, returning an <b>Integer</b> when the provided component is a <b>Long</b> 
	 * for the registered generic type of <b>Number</b>), 
	 * if you do not handle it correctly this will throw exceptions once a component is parsed.
	 * 
	 * @param context the context
	 * @param type the type of the component to parse
	 * @param component the component this parser is attached to
	 * @param content the content to parse
	 * 
	 * @return the generically parsed component
	 */
	@Nonnull
	public ParsedResult<Type> parse(@Nonnull ParseContext context, @Nonnull Class<Type> type, @Nonnull Component component, @Nonnull String content);
	
	/**
	 * Parse a generic component
	 * 
	 * <b>Note:</b> That there is no safe-guard against returning the wrong type
	 * (for instance, returning an <b>Integer</b> when the provided component is a <b>Long</b> 
	 * for the registered generic type of <b>Number</b>), 
	 * if you do not handle it correctly this will throw exceptions once a component is parsed.
	 * 
	 * @param context the context
	 * @param component the component this parser is attached to
	 * @param content the content to parse
	 * 
	 * @return the generically parsed component
	 */
	@Override
	@Nonnull
	public default ParsedResult<Type> parse(@Nonnull ParseContext context, @Nonnull Component component, @Nonnull String content) {
		return this.parse(context, component.getType(), component, content);
	}
}