package com.jockie.bot.core.argument.parser;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.command.parser.ParseContext;

@FunctionalInterface
public interface IGenericArgumentParser<Type> extends IArgumentParser<Type> {
	
	/**
	 * Parse a generic argument
	 * 
	 * <b>Note:</b> That there is no safe-guard against returning the wrong type
	 * (for instance, returning an <b>Integer</b> when the provided argument is a <b>Long</b> 
	 * for the registered generic type of <b>Number</b>), 
	 * if you do not handle it correctly this will throw exceptions once an argument is parsed.
	 * 
	 * @param context the context
	 * @param type the type of the argument to parse
	 * @param argument the argument this parser is attached to
	 * @param content the content to parse
	 * 
	 * @return the generically parsed argument
	 */
	public ParsedArgument<Type> parse(ParseContext context, Class<Type> type, IArgument<Type> argument, String content);
	
	/**
	 * Parse a generic argument
	 * 
	 * <b>Note:</b> That there is no safe-guard against returning the wrong type
	 * (for instance, returning an <b>Integer</b> when the provided argument is a <b>Long</b> 
	 * for the registered generic type of <b>Number</b>), 
	 * if you do not handle it correctly this will throw exceptions once an argument is parsed.
	 * 
	 * @param context the context
	 * @param argument the argument this parser is attached to
	 * @param content the content to parse
	 * 
	 * @return the generically parsed argument
	 */
	@Override
	public default ParsedArgument<Type> parse(ParseContext context, IArgument<Type> argument, String content) {
		return this.parse(context, argument.getType(), argument, content);
	}
}