package com.jockie.bot.core.parser;

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
	public ParsedResult<Type> parse(ParseContext context, Class<Type> type, Component component, String content);
	
	/**
	 * Parse a generic component
	 * 
	 * <b>Note:</b> That there is no safe-guard against returning the wrong type
	 * (for instance, returning an <b>Integer</b> when the provided component is a <b>Long</b> 
	 * for the registered generic type of <b>Number</b>), 
	 * if you do not handle it correctly this will throw exceptions once an component is parsed.
	 * 
	 * @param context the context
	 * @param component the component this parser is attached to
	 * @param content the content to parse
	 * 
	 * @return the generically parsed component
	 */
	@Override
	public default ParsedResult<Type> parse(ParseContext context, Component component, String content) {
		return this.parse(context, component.getType(), component, content);
	}
}