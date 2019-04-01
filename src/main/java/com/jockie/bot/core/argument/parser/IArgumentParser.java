package com.jockie.bot.core.argument.parser;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.impl.ArgumentFactory;

import net.dv8tion.jda.core.entities.Message;

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
	 * @return whether or not this parser should manage all the content itself, 
	 * this means that all the content which is left to parse for the command parser 
	 * will be given to this parser.
	 * </br></br>
	 * <b>NOTE:</b>
	 * This parser then has to return any left over content in the {@link ParsedArgument}.
	 * </br></br>
	 * This can be useful for creating JSON arguments for instance, an example of this exists in 
	 * {@link ArgumentFactory} for {@link JSONObject} and {@link JSONArray}
	 */
	public default boolean handleAll() {
		return false;
	}
}