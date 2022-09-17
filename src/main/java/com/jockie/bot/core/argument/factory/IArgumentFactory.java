package com.jockie.bot.core.argument.factory;

import java.lang.reflect.Parameter;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.parser.IAfterParser;
import com.jockie.bot.core.parser.IBeforeParser;
import com.jockie.bot.core.parser.IGenericParser;
import com.jockie.bot.core.parser.IParser;

public interface IArgumentFactory {
	
	/**
	 * @param parameter the parameter to create the argument from
	 * 
	 * @return the created argument
	 */
	@Nonnull
	public IArgument<?> createArgument(@Nonnull Parameter parameter);
	
	/**
	 * @param type the type of the parser
	 * @param parser the parser which will be used to parse arguments by the provided type
	 * 
	 * @return the {@link IArgumentFactory} instance, useful for chaining
	 */
	@Nonnull
	public <T> IArgumentFactory registerParser(@Nonnull Class<T> type, @Nonnull IParser<T, ? extends IArgument<T>> parser);
	
	/**
	 * @param type the type of the parser to unregister
	 * 
	 * @return the {@link IArgumentFactory} instance, useful for chaining
	 */
	@Nonnull
	public IArgumentFactory unregisterParser(@Nullable Class<?> type);
	
	/**
	 * @param type the type to get the parser from
	 * 
	 * @return the registered parser or null if there is none for the provided type
	 */
	@Nullable
	public <T> IParser<T, IArgument<T>> getParser(@Nullable Class<T> type);
	
	/**
	 * Register a parser which will be used before the argument parser for the provided type, this can be used to
	 * modify the input any given way.
	 * 
	 * @param type the type of the parser
	 * @param parser the parser which will be used before parsing the argument
	 * 
	 * @return the {@link IArgumentFactory} instance, useful for chaining
	 */
	public <T> IArgumentFactory addParserBefore(@Nonnull Class<T> type, @Nonnull IBeforeParser<? extends IArgument<T>> parser);
	
	/**
	 * @param type the type of the parser to remove
	 * @param parser the parser to remove
	 * 
	 * @return the {@link IArgumentFactory} instance, useful for chaining
	 */
	@Nonnull
	public IArgumentFactory removeParserBefore(@Nullable Class<?> type, @Nullable IBeforeParser<?> parser);
	
	/**
	 * @param type the type of the parser
	 * 
	 * @return the registered parsers before the provided type or an empty list
	 */
	@Nonnull
	public <T> List<IBeforeParser<T>> getParsersBefore(@Nullable Class<T> type);
	
	/**
	 * Register a parser which will be used after the argument parser for the provided type, this can be used to
	 * modify the output any given way.
	 * 
	 * @param type the type of the parser
	 * @param parser the parser which will be used after parsing the argument
	 * 
	 * @return the {@link IArgumentFactory} instance, useful for chaining
	 */
	public <T> IArgumentFactory addParserAfter(@Nonnull Class<T> type, @Nonnull IAfterParser<T, ? extends IArgument<T>> parser);
	
	/**
	 * @param type the type of the parser to remove
	 * @param parser the parser to remove
	 * 
	 * @return the {@link IArgumentFactory} instance, useful for chaining
	 */
	@Nonnull
	public IArgumentFactory removeParserAfter(@Nullable Class<?> type, @Nullable IAfterParser<?, ?> parser);
	
	/**
	 * @param type the type of the parser
	 * 
	 * @return the registered parsers after the provided type or an empty list
	 */
	@Nonnull
	public <T> List<IAfterParser<T, IArgument<T>>> getParsersAfter(@Nullable Class<T> type);
	
	/**
	 * Register a generic parser which will be used before the argument parser for any class extending the provided type, 
	 * this can be used to modify the input any given way. Generic parsers are executed before type specific ones.
	 * <br><br>
	 * Giving the class of {@link Object} will make it run before every parser.
	 * 
	 * @param type the type of the parser
	 * @param parser the parser which will be used before parsing the argument
	 * 
	 * @return the {@link IArgumentFactory} instance, useful for chaining
	 */
	public <T> IArgumentFactory addGenericParserBefore(@Nonnull Class<T> type, @Nonnull IBeforeParser<? extends IArgument<T>> parser);
	
	/**
	 * @param type the type of the generic parser to remove
	 * @param parser the generic parser to remove
	 * 
	 * @return the {@link IArgumentFactory} instance, useful for chaining
	 */
	@Nonnull
	public IArgumentFactory removeGenericParserBefore(@Nullable Class<?> type, @Nullable IBeforeParser<?> parser);
	
	/**
	 * @param type the type of the parser
	 * 
	 * @return the registered generic parsers before the provided type or an empty list
	 */
	@Nonnull
	public <T> List<IBeforeParser<T>> getGenericParsersBefore(@Nullable Class<T> type);
	
	/**
	 * <b>Note:</b> That there is no safe-guard against returning the wrong type
	 * (for instance, returning an <b>Integer</b> when the provided argument is a <b>Long</b> 
	 * for the registered generic type of <b>Number</b>), 
	 * if you do not handle it correctly this will throw exceptions once an argument is parsed.
	 * 
	 * @param type the generic type, classes extending this will be handled through the provided parser
	 * @param parser the parser which will be used to parse arguments of types extending the provided type
	 * 
	 * @return the {@link IArgumentFactory} instance, useful for chaining
	 */
	@Nonnull
	public <T> IArgumentFactory registerGenericParser(@Nonnull Class<T> type, @Nonnull IGenericParser<T, ? extends IArgument<T>> parser);
	
	/**
	 * @param type the type of the generic parser to unregister
	 * 
	 * @return the {@link IArgumentFactory} instance, useful for chaining
	 */
	@Nonnull
	public IArgumentFactory unregisterGenericParser(@Nullable Class<?> type);
	
	/**
	 * @param type the generic type which the parser is registered for
	 * 
	 * @return the generic parser registered for the provided type
	 */
	@Nullable
	public <T> IGenericParser<T, IArgument<T>> getGenericParser(@Nullable Class<T> type);
	
}