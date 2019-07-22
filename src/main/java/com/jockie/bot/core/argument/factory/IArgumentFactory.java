package com.jockie.bot.core.argument.factory;

import java.lang.reflect.Parameter;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.parser.IArgumentParser;

public interface IArgumentFactory {
	
	/**
	 * @param parameter the parameter to create the argument from
	 * 
	 * @return the created argument
	 */
	public IArgument<?> createArgument(Parameter parameter);
	
	/**
	 * @param type the type of the parser
	 * @param parser the parser which will be used to parse arguments by the provided type
	 * 
	 * @return the {@link IArgumentFactory} instance, useful for chaining
	 */
	public <T> IArgumentFactory registerParser(Class<T> type, IArgumentParser<T> parser);
	
	/**
	 * @param type the type of the parser to unregister
	 * 
	 * @return the {@link IArgumentFactory} instance, useful for chaining
	 */
	public IArgumentFactory unregisterParser(Class<?> type);
	
	/**
	 * Aliases are used as way to have one type of argument be parsed as another, for instance
	 * boolean can be parsed as {@link Boolean}
	 * <br><br>
	 * For instance:
	 * <br>
	 * <b>registerParserAlias(boolean.class, Boolean.class)</b>
	 * <br>
	 * <b>registerParserAlias(User.class, UserImpl.class)</b>
	 * 
	 * @param type the type to register as an alias
	 * @param alias the alias type
	 * 
	 * @return the {@link IArgumentFactory} instance, useful for chaining
	 */
	public <T> IArgumentFactory registerParserAlias(Class<T> type, Class<? extends T> alias);
	
	/**
	 * @param type the type to unregister as an alias
	 * 
	 * @return the {@link IArgumentFactory} instance, useful for chaining
	 */
	public IArgumentFactory unregisterParserAlias(Class<?> type);
	
	/**
	 * @param type the type to get the parser from
	 * 
	 * @return the registered parser or null if there is none for the provided type
	 */
	public <T> IArgumentParser<T> getParser(Class<T> type);
	
}