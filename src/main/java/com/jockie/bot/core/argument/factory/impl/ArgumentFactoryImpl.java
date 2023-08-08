package com.jockie.bot.core.argument.factory.impl;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.argument.Endless;
import com.jockie.bot.core.argument.Error;
import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.IArgument.Builder;
import com.jockie.bot.core.argument.factory.IArgumentFactory;
import com.jockie.bot.core.argument.impl.ArgumentImpl;
import com.jockie.bot.core.argument.impl.EndlessArgumentImpl;
import com.jockie.bot.core.parser.IAfterParser;
import com.jockie.bot.core.parser.IBeforeParser;
import com.jockie.bot.core.parser.IGenericParser;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.impl.DelegateGenericParser;
import com.jockie.bot.core.parser.impl.DelegateParser;
import com.jockie.bot.core.parser.impl.discord.CategoryParser;
import com.jockie.bot.core.parser.impl.discord.CustomEmojiParser;
import com.jockie.bot.core.parser.impl.discord.GuildChannelParser;
import com.jockie.bot.core.parser.impl.discord.GuildParser;
import com.jockie.bot.core.parser.impl.discord.MemberParser;
import com.jockie.bot.core.parser.impl.discord.RichCustomEmojiParser;
import com.jockie.bot.core.parser.impl.discord.RoleParser;
import com.jockie.bot.core.parser.impl.discord.TextChannelParser;
import com.jockie.bot.core.parser.impl.discord.UserParser;
import com.jockie.bot.core.parser.impl.discord.VoiceChannelParser;
import com.jockie.bot.core.parser.impl.essential.BooleanParser;
import com.jockie.bot.core.parser.impl.essential.ByteParser;
import com.jockie.bot.core.parser.impl.essential.CharacterParser;
import com.jockie.bot.core.parser.impl.essential.DoubleParser;
import com.jockie.bot.core.parser.impl.essential.EnumParser;
import com.jockie.bot.core.parser.impl.essential.FloatParser;
import com.jockie.bot.core.parser.impl.essential.IntegerParser;
import com.jockie.bot.core.parser.impl.essential.LongParser;
import com.jockie.bot.core.parser.impl.essential.ShortParser;
import com.jockie.bot.core.parser.impl.essential.StringParser;
import com.jockie.bot.core.parser.impl.json.JSONArrayParser;
import com.jockie.bot.core.parser.impl.json.JSONObjectParser;
import com.jockie.bot.core.utility.CommandUtility;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.tuple.Pair;

/* 
 * TODO: Merge this and OptionFactoryImpl
 * into a more general implementation for components,
 * they will still need to have seperate handling but
 * the vast majority is shared or can be shared between
 * the two.
 */
public class ArgumentFactoryImpl implements IArgumentFactory {
	
	protected Map<Class<?>, IGenericParser<?, ?>> genericParsers = new HashMap<>();
	
	protected Map<Class<?>, IParser<?, ?>> parsers = new HashMap<>();
	
	protected Map<Class<?>, Set<IBeforeParser<?>>> beforeParsers = new HashMap<>();
	protected Map<Class<?>, Set<IBeforeParser<?>>> genericBeforeParsers = new HashMap<>();
	
	protected Map<Class<?>, Set<IAfterParser<?, ?>>> afterParsers = new HashMap<>();
	
	protected Set<Function<Parameter, Builder<?, ?, ?>>> builderFunctions = new LinkedHashSet<>();
	
	protected Map<Class<?>, Set<BuilderConfigureFunction<?>>> builderConfigureFunctions = new HashMap<>();
	protected Map<Class<?>, Set<BuilderConfigureFunction<?>>> genericBuilderConfigureFunctions = new HashMap<>();
	
	protected Map<Class<?>, IGenericParser<?, ?>> genericParserCache = new HashMap<>();
	protected Map<Class<?>, IParser<?, ?>> parserCache = new HashMap<>();
	
	protected ArgumentFactoryImpl() {
		this(true);
	}
	
	protected ArgumentFactoryImpl(boolean registerDefaultParsers) {
		if(registerDefaultParsers) {
			this.registerEssentialParsers();
			this.registerDiscordParsers(true);
			this.registerJSONParsers();
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <T> Class<T> convertType(Class<T> type) {
		if(type == null) {
			return null;
		}
		
		if(type.isPrimitive()) {
			type = (Class<T>) CommandUtility.getBoxedClass(type);
		}
		
		return type;
	}
	
	private void addExtendedClasses(Set<Class<?>> classes, Class<?> type) {
		Class<?> superClass = type.getSuperclass();
		if(superClass != null) {
			classes.add(superClass);
		}
		
		for(Class<?> superInterface : type.getInterfaces()) {
			classes.add(superInterface);
		}
		
		if(superClass != null) {
			this.addExtendedClasses(classes, superClass);
		}
		
		for(Class<?> superInterface : type.getInterfaces()) {
			this.addExtendedClasses(classes, superInterface);
		}
	}
	
	protected Set<Class<?>> getExtendedClasses(Class<?> type) {
		Set<Class<?>> classes = new LinkedHashSet<>();
		/*
		 * TODO: Figure out why this wasn't in the initial implementation,
		 * I presume, because there was no comment or additional information
		 * about it, that this was either a design choice or a bug
		 */
		classes.add(type);
		
		this.addExtendedClasses(classes, type);
		
		classes.add(Object.class);
		return classes;
	}
	
	/**
	 * Register the essential argument parsers, this includes
	 * <ul>
	 * 	<li>Byte</li>
	 * 	<li>Short</li>
	 * 	<li>Integer</li>
	 * 	<li>Long</li>
	 * 	<li>Float</li>
	 * 	<li>Double</li>
	 * 	<li>Boolean</li>
	 * 	<li>Character</li>
	 * 	<li>String</li>
	 *  <li>Enum</li>
	 * </ul>
	 * 
	 * @return the {@link ArgumentFactoryImpl} instance, useful for chaining
	 */
	@Nonnull
	public final ArgumentFactoryImpl registerEssentialParsers() {
		this.registerParser(Boolean.class, new BooleanParser<>());
		this.registerParser(Byte.class, new ByteParser<>());
		this.registerParser(Short.class, new ShortParser<>());
		this.registerParser(Integer.class, new IntegerParser<>());
		this.registerParser(Long.class, new LongParser<>());
		this.registerParser(Float.class, new FloatParser<>());
		this.registerParser(Double.class, new DoubleParser<>());
		
		this.registerParser(Character.class, new CharacterParser<>());
		this.registerParser(String.class, new StringParser<>());
		
		this.registerGenericParser(Enum.class, new EnumParser<>());
		
		return this;
	}
	
	/**
	 * Register the Discord argument parsers, this includes
	 * <ul>
	 * 	<li>Member</li>
	 * 	<li>TextChannel</li>
	 * 	<li>VoiceChannel</li>
	 * 	<li>Channel</li>
	 * 	<li>Category</li>
	 * 	<li>Role</li>
	 * 	<li>CustomEmoji</li>
	 * 	<li>RichCustomEmoji</li>
	 * 	<li>User</li>
	 * 	<li>Guild</li>
	 * </ul>
	 * 
	 * @return the {@link ArgumentFactoryImpl} instance, useful for chaining
	 */
	@Nonnull
	public final ArgumentFactoryImpl registerDiscordParsers(boolean useShardManager) {
		this.registerParser(Member.class, new MemberParser<>());
		this.registerParser(TextChannel.class, new TextChannelParser<>());
		this.registerParser(VoiceChannel.class, new VoiceChannelParser<>());
		this.registerParser(GuildChannel.class, new GuildChannelParser<>());
		this.registerParser(Category.class, new CategoryParser<>());
		this.registerParser(Role.class, new RoleParser<>());
		this.registerParser(CustomEmoji.class, new CustomEmojiParser<>());
		this.registerParser(RichCustomEmoji.class, new RichCustomEmojiParser<>());
		this.registerParser(User.class, new UserParser<>(useShardManager));
		this.registerParser(Guild.class, new GuildParser<>(useShardManager));
		
		return this;
	}
	
	/**
	 * Register the JSON argument parsers, this includes
	 * <ul>
	 * 	<li>JSONObject</li>
	 * 	<li>JSONArray</li>
	 * </ul>
	 * 
	 * @return the {@link ArgumentFactoryImpl} instance, useful for chaining
	 */
	@Nonnull
	public final ArgumentFactoryImpl registerJSONParsers() {
		this.registerParser(JSONObject.class, new JSONObjectParser<>());
		this.registerParser(JSONArray.class, new JSONArrayParser<>());
		
		return this;
	}
	
	/**
	 * Unregister the parsers registered through {@link ArgumentFactoryImpl#registerEssentialParsers()}
	 * <br><br>
	 * <b>NOTE</b>:
	 * Using this after registering any custom parsers will remove those custom parsers as well,
	 * this will also not unregister any enum parsers which have been registered automatically
	 * 
	 * @return the {@link ArgumentFactoryImpl} instance, useful for chaining
	 */
	@Nonnull
	public final ArgumentFactoryImpl unregisterEssentialParsers() {
		this.unregisterParser(Byte.class);
		this.unregisterParser(Short.class);
		this.unregisterParser(Integer.class);
		this.unregisterParser(Long.class);
		this.unregisterParser(Float.class);
		this.unregisterParser(Double.class);
		this.unregisterParser(Boolean.class);
		this.unregisterParser(Character.class);
		this.unregisterParser(String.class);
		this.unregisterGenericParser(Enum.class);
		
		return this;
	}
	
	/**
	 * Unregister the parsers registered through {@link ArgumentFactoryImpl#registerDiscordParsers(boolean)}
	 * <br><br>
	 * <b>NOTE</b>:
	 * Using this after registering any custom parsers will remove those custom parsers as well
	 * 
	 * @return the {@link ArgumentFactoryImpl} instance, useful for chaining
	 */
	@Nonnull
	public final ArgumentFactoryImpl unregisterDiscordParsers() {
		this.unregisterParser(Member.class);
		this.unregisterParser(TextChannel.class);
		this.unregisterParser(VoiceChannel.class);
		this.unregisterParser(GuildChannel.class);
		this.unregisterParser(Category.class);
		this.unregisterParser(Role.class);
		this.unregisterParser(CustomEmoji.class);
		this.unregisterParser(RichCustomEmoji.class);
		this.unregisterParser(User.class);
		this.unregisterParser(Guild.class);
		
		return this;
	}
	
	/**
	 * Unregister the parsers registered through {@link ArgumentFactoryImpl#registerJSONParsers()}
	 * <br><br>
	 * <b>NOTE</b>:
	 * Using this after registering any custom parsers will remove those custom parsers as well
	 * 
	 * @return the {@link ArgumentFactoryImpl} instance, useful for chaining
	 */
	@Nonnull
	public final ArgumentFactoryImpl unregisterJSONParsers() {
		this.unregisterParser(JSONObject.class);
		this.unregisterParser(JSONArray.class);
		
		return this;
	}
	
	protected <T extends Builder<?, ?, ?>> T applyArgumentAnnotation(T builder, Parameter parameter) {
		Argument info = parameter.getAnnotation(Argument.class);
		if(info != null) {
			builder.setAcceptEmpty(info.acceptEmpty());
			builder.setAcceptQuote(info.acceptQuote());
			
			if(!info.value().isEmpty()) {
				builder.setName(info.value());
			}
			
			if(!info.description().isEmpty()) {
				builder.setDescription(info.description());
			}
			
			if(info.nullDefault()) {
				builder.setDefaultAsNull();
			}
			
			if(info.endless()) {
				builder.setEndless(info.endless());
			}
		}
		
		return builder;
	}
	
	protected Pair<Boolean, Builder<?, ?, ?>> configureBuilderGeneric(Parameter parameter, Class<?> type, Builder<?, ?, ?> builder) {
		type = this.convertType(type);
		
		for(Class<?> superClass : this.getExtendedClasses(type)) {
			Pair<Boolean, Builder<?, ?, ?>> pair = this.configureBuilder(parameter, builder, this.genericBuilderConfigureFunctions.get(superClass));
			if(pair.getLeft()) {
				return pair;
			}
			
			builder = pair.getRight();
		}
		
		return Pair.of(false, builder);
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	protected Pair<Boolean, Builder<?, ?, ?>> configureBuilder(Parameter parameter, Builder<?, ?, ?> builder, Set<BuilderConfigureFunction<?>> configureFunctions) {
		if(configureFunctions == null) {
			return Pair.of(false, builder);
		}
		
		for(BuilderConfigureFunction function : configureFunctions) {
			Builder<?, ?, ?> newBuilder = function.configure(parameter, builder);
			if(newBuilder == null) {
				continue;
			}
			
			builder = newBuilder;
			
			if(!function.isContinueConfiguration()) {
				return Pair.of(true, builder);
			}
		}
		
		return Pair.of(false, builder);
	}
	
	/* Should generic be configured first? */
	protected Builder<?, ?, ?> configureBuilder(Parameter parameter, Class<?> type, Builder<?, ?, ?> builder) {
		type = this.convertType(type);
		
		Pair<Boolean, Builder<?, ?, ?>> pair = this.configureBuilderGeneric(parameter, type, builder);
		if(pair.getLeft()) {
			return pair.getRight();
		}
		
		return this.configureBuilder(parameter, builder, this.builderConfigureFunctions.get(type)).getRight();
	}
	
	public <T> IArgument.Builder<T[], ?, ?> createArrayArgumentBuilder(Parameter parameter, Class<T> componentType) {
		IParser<T, IArgument<T>> parser = this.getParser(componentType);
		if(parser == null) {
			parser = this.getGenericParser(componentType);
		}
		
		if(parser == null) {
			throw new IllegalArgumentException("There is no parser for the component type: " + componentType);
		}
		
		ArgumentImpl.Builder<T> builder = new ArgumentImpl.Builder<>(componentType)
			.setParser(parser);
		
		if(parameter.isNamePresent()) {
			builder.setName(parameter.getName());
		}
		
		this.applyArgumentAnnotation(builder, parameter);
		
		if(builder.isEndless()) {
			throw new IllegalArgumentException("Not a valid candidate, candidate may not be endless");
		}
		
		EndlessArgumentImpl.Builder<T> endlessBuilder = new EndlessArgumentImpl.Builder<>(componentType).setArgument(builder.build());
		if(parameter.isAnnotationPresent(Endless.class)) {
			Endless endless = parameter.getAnnotation(Endless.class);
			
			endlessBuilder.setMinArguments(endless.minArguments())
				.setMaxArguments(endless.maxArguments())
				.setEndless(endless.endless());
		}
		
		return endlessBuilder;
	}
	
	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Nonnull
	public IArgument<?> createArgument(@Nonnull Parameter parameter) {
		Class<?> type = parameter.getType();
		
		boolean isOptional = false;
		if(type.isAssignableFrom(Optional.class)) {
			Class<?>[] classes = CommandUtility.getGenericClasses(parameter.getParameterizedType());
			if(classes.length > 0 && classes[0] != null) {
				type = classes[0];
			}
			
			isOptional = true;
		}
		
		Builder builder = null;
		for(Function<Parameter, Builder<?, ?, ?>> function : this.builderFunctions) {
			builder = function.apply(parameter);
			if(builder != null) {
				break;
			}
		}
		
		if(builder == null) {
			builder = new ArgumentImpl.Builder<>(type);
		}
		
		if(builder.getParser() == null) {
			IParser<?, ?> parser = this.getParser(type);
			if(parser == null) {
				parser = this.getGenericParser(type);
			}
			
			if(parser != null) {
				builder.setParser(parser);
			}
		}
		
		if(builder.getParser() != null) {
			if(parameter.isNamePresent()) {
				builder.setName(parameter.getName());
			}
			
			this.applyArgumentAnnotation(builder, parameter);
		}else{
			Class<?> componentType = type.getComponentType();
			if(componentType == null) {
				throw new IllegalArgumentException("There is no parser for type: " + type);
			}
			
			builder = this.createArrayArgumentBuilder(parameter, componentType);
		}
		
		if(isOptional) {
			builder.setDefaultAsNull();
		}
		
		Error error = parameter.getAnnotation(Error.class);
		if(error != null) {
			builder.setErrorMessage(error.value());
		}
		
		return this.configureBuilder(parameter, type, builder).build();
	}
	
	@Override
	@Nonnull
	public <T> ArgumentFactoryImpl registerGenericParser(@Nonnull Class<T> type, @Nonnull IGenericParser<T, ? extends IArgument<T>> parser) {
		Checks.notNull(type, "type");
		Checks.notNull(parser, "parser");
		
		this.genericParsers.put(this.convertType(type), parser);
		
		return this;
	}
	
	@Override
	@Nonnull
	public ArgumentFactoryImpl unregisterGenericParser(@Nullable Class<?> type) {
		this.genericParsers.remove(this.convertType(type));
		
		return this;
	}
	
	@Override
	@Nonnull
	public <T> ArgumentFactoryImpl registerParser(@Nonnull Class<T> type, @Nonnull IParser<T, ? extends IArgument<T>> parser) {
		Checks.notNull(type, "type");
		Checks.notNull(parser, "parser");
		
		this.parsers.put(this.convertType(type), parser);
		
		return this;
	}
	
	@Override
	@Nonnull
	public ArgumentFactoryImpl unregisterParser(@Nullable Class<?> type) {
		this.parsers.remove(this.convertType(type));
		
		return this;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	@Nullable
	/* See the comments in #getParser for more information */
	public <T> IGenericParser<T, IArgument<T>> getGenericParser(@Nullable Class<T> type) {
		type = this.convertType(type);
		if(type == null) {
			return null;
		}
		
		IGenericParser<T, IArgument<T>> cachedParser = (IGenericParser<T, IArgument<T>>) this.genericParserCache.get(type);
		if(cachedParser != null) {
			return cachedParser;
		}
		
		for(Class<?> superClass : this.getExtendedClasses(type)) {
			IGenericParser<T, IArgument<T>> parser = (IGenericParser<T, IArgument<T>>) this.genericParsers.get(superClass);
			if(parser == null) {
				continue;
			}
			
			Set<IBeforeParser<IArgument<T>>> beforeParsers;
			if(!parser.isHandleAll()) {
				beforeParsers = this.getBeforeParsers(type);
			}else{
				beforeParsers = Collections.emptySet();
			}
			
			Set<IAfterParser<T, IArgument<T>>> afterParsers = this.getAfterParsers(type);
			if(beforeParsers.isEmpty() && afterParsers.isEmpty()) {
				return parser;
			}
			
			IGenericParser<T, IArgument<T>> newParser = new DelegateGenericParser<>(parser, beforeParsers, afterParsers);
			
			this.genericParserCache.put(type, newParser);
			return newParser;
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Nonnull
	protected <T> Set<IBeforeParser<IArgument<T>>> getBeforeParsers(@Nullable Class<T> type) {
		type = this.convertType(type);
		
		Set<IBeforeParser<IArgument<T>>> beforeParsers = new LinkedHashSet<>();
		
		for(Class<?> superClass : this.getExtendedClasses(type)) {
			Set<IBeforeParser<?>> parsers = this.genericBeforeParsers.get(superClass);
			if(parsers == null) {
				continue;
			}
			
			for(IBeforeParser<?> beforeParser : parsers) {
				beforeParsers.add((IBeforeParser<IArgument<T>>) beforeParser);
			}
		}
		
		Set<IBeforeParser<?>> parsers = this.beforeParsers.get(type);
		if(parsers == null) {
			return beforeParsers;
		}
		
		for(IBeforeParser<?> beforeParser : parsers) {
			beforeParsers.add((IBeforeParser<IArgument<T>>) beforeParser);
		}
		
		return beforeParsers;
	}
	
	@SuppressWarnings("unchecked")
	@Nonnull
	protected <T> Set<IAfterParser<T, IArgument<T>>> getAfterParsers(@Nullable Class<T> type) {
		type = this.convertType(type);
		
		Set<IAfterParser<?, ?>> parsers = this.afterParsers.get(type);
		if(parsers == null) {
			return Collections.emptySet();
		}
		
		Set<IAfterParser<T, IArgument<T>>> afterParsers = new LinkedHashSet<>();
		for(IAfterParser<?, ?> afterParser : parsers) {
			afterParsers.add((IAfterParser<T, IArgument<T>>) afterParser);
		}
		
		return afterParsers;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	@Nullable
	public <T> IParser<T, IArgument<T>> getParser(@Nullable Class<T> type) {
		type = this.convertType(type);
		
		IParser<T, IArgument<T>> parser = (IParser<T, IArgument<T>>) this.parsers.get(type);
		if(parser == null) {
			return null;
		}
		
		IParser<T, IArgument<T>> cachedParser = (IParser<T, IArgument<T>>) this.parserCache.get(type);
		if(cachedParser != null) {
			return cachedParser;
		}
		
		/* 
		 * TODO: Currently handle all does not support before parsers due to the conflicting nature of the two
		 * 
		 * Clarification:
		 * There's no way to determine what content the before parser should get, you could give it all the content
		 * but then you risk it modifying content which it should not be modifying, take the given String,
		 * "{"hello": "there"} hello there", and give this to the JSONObject parser with a registered before parser
		 * which makes every character uppercase for that argument, the result would be "{"HELLO": "THERE"} HELLO THERE",
		 * and then once it gets to the parser the only content which will be handled is the "{"HELLO": "THERE"}" leaving
		 * the "HELLO THERE" modified without any directive to do so.
		 * 
		 * Solutions:
		 * A possible solution to this would be to take the length of the content left and subtract that with the previous
		 * length and then revert any characters after that and in most cases this would be a valid solution, however,
		 * nothing says that the parser must return what is left, instead it could return anything, exclude or include
		 * characters.
		 * 
		 * It is also possible to change the implementation of the handle all, so that it handles the String before anything
		 * else and then returns the handled String, however, this would cause extra work as it would practically need to handle
		 * it twice. Example, you give it "{"hello": "there"} hello there" and before anything else it parsers this and
		 * determines that "{"hello": "there"}" is the part it wants and returns that, after this the before parsers are handled
		 * and then the argument is parsed and returned as the JSONObject.
		 * 
		 * Another solution would be to change the handle all so that you "take" characters from it and once a character is taken
		 * it is removed from the String, this would work in a situation like "{"hello": "there"} hello there", because you know
		 * that the JSON definitely ends at the "}", not sure if there are any situations where you would need to have access to
		 * the entire thing, but this seems like the best solution to fixing this (this would be used in combination with solution 1.)
		 */
		Set<IBeforeParser<IArgument<T>>> beforeParsers;
		if(!parser.isHandleAll()) {
			beforeParsers = this.getBeforeParsers(type);
		}else{
			beforeParsers = Collections.emptySet();
		}
		
		Set<IAfterParser<T, IArgument<T>>> afterParsers = this.getAfterParsers(type);
		if(beforeParsers.isEmpty() && afterParsers.isEmpty()) {
			return parser;
		}
		
		/*
		 * TODO: This is probably not the best way to handle this
		 * but it is definitely the easiest.
		 * 
		 * This would not handle if the before or after parsers where changed or added afterwards.
		 * Is it worth supporting that by getting the parsers inside of parsing method?
		 * Doing so may could slow down the execution.
		 * 
		 * We would also need to return this for every parser in that case, since
		 * they could register after or before parsers at any time which would require
		 * it to be using this.
		 * 
		 * Currently normal parsers suffer from the same kind of issue, since you can not
		 * register a new parser and have all the arguments use that new parser.
		 */
		IParser<T, IArgument<T>> newParser = new DelegateParser<>(parser, beforeParsers, afterParsers);
		
		this.parserCache.put(type, newParser);
		return newParser;
	}
	
	@Nonnull
	public ArgumentFactoryImpl addBuilderFunction(@Nonnull Function<Parameter, Builder<?, ?, ?>> function) {
		Checks.notNull(function, "function");
		
		this.builderFunctions.add(function);
		
		return this;
	}
	
	@Nonnull
	public ArgumentFactoryImpl removeBuilderFunction(@Nullable Function<Parameter, Builder<?, ?, ?>> function) {
		this.builderFunctions.remove(function);
		
		return this;
	}
	
	@Nonnull
	public List<Function<Parameter, Builder<?, ?, ?>>> getBuilderFunctions() {
		return new ArrayList<>(this.builderFunctions);
	}
	
	@Nonnull
	public <T> ArgumentFactoryImpl addBuilderConfigureFunction(@Nonnull Class<T> type, @Nonnull BuilderConfigureFunction<T> configureFunction) {
		Checks.notNull(type, "type");
		Checks.notNull(configureFunction, "configureFunction");
		
		this.builderConfigureFunctions.computeIfAbsent(type = this.convertType(type), (key) -> new LinkedHashSet<>()).add(configureFunction);
		
		return this;
	}
	
	@Nonnull
	public ArgumentFactoryImpl removeBuilderConfigureFunction(@Nullable Class<?> type, @Nullable BuilderConfigureFunction<?> configureFunction) {
		type = this.convertType(type);
		
		Set<?> builders = this.builderConfigureFunctions.get(type);
		if(builders != null) {
			builders.remove(configureFunction);
		}
		
		return this;
	}
	
	@SuppressWarnings("unchecked")
	@Nonnull
	public <T> List<BuilderConfigureFunction<T>> getBuilderConfigureFunctions(@Nullable Class<T> type) {
		type = this.convertType(type);
		
		Set<?> builders = this.builderConfigureFunctions.get(type);
		if(builders != null) {
			return (List<BuilderConfigureFunction<T>>) new ArrayList<>(builders);
		}
		
		return Collections.emptyList();
	}
	
	@Nonnull
	public <T> ArgumentFactoryImpl addGenericBuilderConfigureFunction(@Nonnull Class<T> type, @Nonnull BuilderConfigureFunction<T> configureFunction) {
		Checks.notNull(type, "type");
		Checks.notNull(configureFunction, "configureFunction");
		
		this.genericBuilderConfigureFunctions.computeIfAbsent(type = this.convertType(type), (key) -> new LinkedHashSet<>()).add(configureFunction);
		
		return this;
	}
	
	@Nonnull
	public ArgumentFactoryImpl removeGenericBuilderConfigureFunction(@Nullable Class<?> type, @Nullable BuilderConfigureFunction<?> configureFunction) {
		type = this.convertType(type);
		
		Set<?> builders = this.genericBuilderConfigureFunctions.get(type);
		if(builders != null) {
			builders.remove(configureFunction);
		}
		
		return this;
	}
	
	@SuppressWarnings("unchecked")
	@Nonnull
	public <T> List<BuilderConfigureFunction<T>> getGenericBuilderConfigureFunctions(@Nullable Class<T> type) {
		type = this.convertType(type);
		
		Set<?> builders = this.genericBuilderConfigureFunctions.get(type);
		if(builders != null) {
			return (List<BuilderConfigureFunction<T>>) new ArrayList<>(builders);
		}
		
		return Collections.emptyList();
	}
	
	@Override
	@Nonnull
	public <T> ArgumentFactoryImpl addParserBefore(@Nonnull Class<T> type, @Nonnull IBeforeParser<? extends IArgument<T>> parser) {
		Checks.notNull(type, "type");
		Checks.notNull(parser, "parser");
		
		this.beforeParsers.computeIfAbsent(type = this.convertType(type), (key) -> new LinkedHashSet<>()).add(parser);
		
		return this;
	}
	
	@Override
	@Nonnull
	public ArgumentFactoryImpl removeParserBefore(@Nullable Class<?> type, @Nullable IBeforeParser<?> parser) {
		type = this.convertType(type);
		
		Set<?> parsers = this.beforeParsers.get(type);
		if(parsers != null) {
			parsers.remove(parser);
		}
		
		return this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Nonnull
	public <T> List<IBeforeParser<T>> getParsersBefore(@Nullable Class<T> type) {
		type = this.convertType(type);
		
		Set<?> parsers = this.beforeParsers.get(type);
		if(parsers != null) {
			return (List<IBeforeParser<T>>) new ArrayList<>(parsers);
		}
		
		return Collections.emptyList();
	}
	
	@Override
	@Nonnull
	public <T> ArgumentFactoryImpl addGenericParserBefore(@Nonnull Class<T> type, @Nonnull IBeforeParser<? extends IArgument<T>> parser) {
		Checks.notNull(type, "type");
		Checks.notNull(parser, "parser");
		
		this.genericBeforeParsers.computeIfAbsent(type = this.convertType(type), (key) -> new LinkedHashSet<>()).add(parser);
		
		return this;
	}
	
	@Override
	@Nonnull
	public ArgumentFactoryImpl removeGenericParserBefore(@Nullable Class<?> type, @Nullable IBeforeParser<?> parser) {
		type = this.convertType(type);
		
		Set<?> parsers = this.genericBeforeParsers.get(type);
		if(parsers != null) {
			parsers.remove(parser);
		}
		
		return this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Nonnull
	public <T> List<IBeforeParser<T>> getGenericParsersBefore(@Nullable Class<T> type) {
		type = this.convertType(type);
		
		Set<?> parsers = this.genericBeforeParsers.get(type);
		if(parsers != null) {
			return (List<IBeforeParser<T>>) new ArrayList<>(parsers);
		}
		
		return Collections.emptyList();
	}
	
	@Override
	@Nonnull
	public <T> ArgumentFactoryImpl addParserAfter(@Nonnull Class<T> type, @Nonnull IAfterParser<T, ? extends IArgument<T>> parser) {
		Checks.notNull(type, "type");
		Checks.notNull(parser, "parser");
		
		this.afterParsers.computeIfAbsent(type = this.convertType(type), (key) -> new LinkedHashSet<>()).add(parser);
		
		return this;
	}
	
	@Override
	@Nonnull
	public ArgumentFactoryImpl removeParserAfter(@Nullable Class<?> type, @Nullable IAfterParser<?, ?> parser) {
		type = this.convertType(type);
		
		Set<?> parsers = this.afterParsers.get(type);
		if(parsers != null) {
			parsers.remove(parser);
		}
		
		return this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Nonnull
	public <T> List<IAfterParser<T, IArgument<T>>> getParsersAfter(@Nullable Class<T> type) {
		type = this.convertType(type);
		
		Set<?> parsers = this.afterParsers.get(type);
		if(parsers != null) {
			return (List<IAfterParser<T, IArgument<T>>>) new ArrayList<>(parsers);
		}
		
		return Collections.emptyList();
	}
}