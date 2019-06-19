package com.jockie.bot.core.argument.parser;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.impl.ArgumentFactory;

import net.dv8tion.jda.api.entities.Message;

@FunctionalInterface
public interface IArgumentParser<Type> {
	
	/**
	 * Parse an argument
	 * 
	 * @param message the context
	 * @param argument the argument this parser is attached to
	 * @param content the content to parse
	 * 
	 * @return the parsed argument
	 */
	public ParsedArgument<Type> parse(Message message, IArgument<Type> argument, String content);
	
	/**
	 * <b>NOTE:</b>
	 * By using this property in a parser you have to yourself return any left over content in the {@link ParsedArgument}.
	 * </br></br>
	 * This can, for instance, be useful for creating JSON arguments, an example of this exists in 
	 * {@link ArgumentFactory} for {@link JSONObject} and {@link JSONArray}
	 * 
	 * @return whether or not this parser should manage all the content itself, 
	 * this means that all the content which is left to parse for the command parser 
	 * will be given to this parser.
	 */
	public default boolean isHandleAll() {
		return false;
	}
	
	/**
	 * This is used to determine in what order commands with similar arguments should be parsed
	 * to get the most accurate result.
	 * 
	 * @return the importance of this parser, parsers with lower priority may be handled before
	 * ones with higher order.
	 */
	public default int getPriority() {
		return 0;
	}
}