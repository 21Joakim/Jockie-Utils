package com.jockie.bot.core.option.factory.impl;

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
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jockie.bot.core.option.IOption;
import com.jockie.bot.core.option.IOption.Builder;
import com.jockie.bot.core.option.Option;
import com.jockie.bot.core.option.factory.IOptionFactory;
import com.jockie.bot.core.option.impl.OptionImpl;
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

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.tuple.Pair;

/* 
 * This is mostly the exact same as ArgumentFactoryImpl, 
 * see the comments in there for issues related to this
 */
public class OptionFactoryImpl implements IOptionFactory {
	
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
	
	protected OptionFactoryImpl() {
		this.registerEssentialParsers();
		this.registerDiscordParsers(true);
		this.registerJSONParsers();
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
		this.addExtendedClasses(classes, type);
		
		classes.add(Object.class);
		return classes;
	}
	
	/**
	 * Register the essential option parsers, this includes
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
	 * @return the {@link OptionFactoryImpl} instance, useful for chaining
	 */
	@Nonnull
	public final OptionFactoryImpl registerEssentialParsers() {
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
	 * Register the Discord option parsers, this includes
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
	 * @return the {@link OptionFactoryImpl} instance, useful for chaining
	 */
	@Nonnull
	public final OptionFactoryImpl registerDiscordParsers(boolean useShardManager) {
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
	 * Register the JSON option parsers, this includes
	 * <ul>
	 * 	<li>JSONObject</li>
	 * 	<li>JSONArray</li>
	 * </ul>
	 * 
	 * @return the {@link OptionFactoryImpl} instance, useful for chaining
	 */
	@Nonnull
	public final OptionFactoryImpl registerJSONParsers() {
		this.registerParser(JSONObject.class, new JSONObjectParser<>());
		this.registerParser(JSONArray.class, new JSONArrayParser<>());
		
		return this;
	}
	
	/**
	 * Unregister the parsers registered through {@link OptionFactoryImpl#registerEssentialParsers()}
	 * <br><br>
	 * <b>NOTE</b>:
	 * Using this after registering any custom parsers will remove those custom parsers as well,
	 * this will also not unregister any enum parsers which have been registered automatically
	 * 
	 * @return the {@link OptionFactoryImpl} instance, useful for chaining
	 */
	@Nonnull
	public final OptionFactoryImpl unregisterEssentialParsers() {
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
	 * Unregister the parsers registered through {@link OptionFactoryImpl#registerDiscordParsers(boolean)}
	 * <br><br>
	 * <b>NOTE</b>:
	 * Using this after registering any custom parsers will remove those custom parsers as well
	 * 
	 * @return the {@link OptionFactoryImpl} instance, useful for chaining
	 */
	@Nonnull
	public final OptionFactoryImpl unregisterDiscordParsers() {
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
	 * Unregister the parsers registered through {@link OptionFactoryImpl#registerJSONParsers()}
	 * <br><br>
	 * <b>NOTE</b>:
	 * Using this after registering any custom parsers will remove those custom parsers as well
	 * 
	 * @return the {@link OptionFactoryImpl} instance, useful for chaining
	 */
	@Nonnull
	public final OptionFactoryImpl unregisterJSONParsers() {
		this.unregisterParser(JSONObject.class);
		this.unregisterParser(JSONArray.class);
		
		return this;
	}
	
	protected <T extends Builder<?, ?, ?>> T applyOptionAnnotation(T builder, Parameter parameter) {
		Option info = parameter.getAnnotation(Option.class);
		if(info == null) {
			return null;
		}
	
		builder.setAliases(info.aliases());
		builder.setDescription(info.description());
		builder.setHidden(info.hidden());
		builder.setDeveloper(info.developer());
		
		if(!info.value().isEmpty()) {
			builder.setName(info.value());
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
	
	protected Builder<?, ?, ?> configureBuilder(Parameter parameter, Class<?> type, Builder<?, ?, ?> builder) {
		type = this.convertType(type);
		
		Pair<Boolean, Builder<?, ?, ?>> pair = this.configureBuilderGeneric(parameter, type, builder);
		if(pair.getLeft()) {
			return pair.getRight();
		}
		
		return this.configureBuilder(parameter, builder, this.builderConfigureFunctions.get(type)).getRight();
	}
	
	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Nonnull
	public IOption<?> createOption(@Nonnull Parameter parameter) {
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
			builder = new OptionImpl.Builder(type);
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
		
		IParser<?, ?> parser = builder.getParser();
		if(parser == null) {
			throw new IllegalArgumentException("There is no parser for type: " + type);
		}
		
		builder.setParser(parser);
		
		if(parameter.isNamePresent()) {
			builder.setName(parameter.getName());
		}
		
		this.applyOptionAnnotation(builder, parameter);
		
		if(isOptional) {
			builder.setDefaultAsNull();
		}
		
		return this.configureBuilder(parameter, type, builder).build();
	}
	
	@Override
	@Nonnull
	public <T> OptionFactoryImpl registerGenericParser(@Nonnull Class<T> type, @Nonnull IGenericParser<T, IOption<T>> parser) {
		Checks.notNull(type, "type");
		Checks.notNull(parser, "parser");
		
		this.genericParsers.put(this.convertType(type), parser);
		
		return this;
	}
	
	@Override
	@Nonnull
	public OptionFactoryImpl unregisterGenericParser(@Nullable Class<?> type) {
		this.genericParsers.remove(this.convertType(type));
		
		return this;
	}
	
	@Override
	@Nonnull
	public <T> OptionFactoryImpl registerParser(@Nonnull Class<T> type, @Nonnull IParser<T, IOption<T>> parser) {
		Checks.notNull(type, "type");
		Checks.notNull(parser, "parser");
		
		this.parsers.put(this.convertType(type), parser);
		
		return this;
	}
	
	@Override
	@Nonnull
	public OptionFactoryImpl unregisterParser(@Nullable Class<?> type) {
		this.parsers.remove(this.convertType(type));
		
		return this;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	@Nullable
	public <T> IGenericParser<T, IOption<T>> getGenericParser(@Nullable Class<T> type) {
		type = this.convertType(type);
		if(type == null) {
			return null;
		}
		
		IGenericParser<T, IOption<T>> cachedParser = (IGenericParser<T, IOption<T>>) this.genericParserCache.get(type);
		if(cachedParser != null) {
			return cachedParser;
		}
		
		for(Class<?> superClass : this.getExtendedClasses(type)) {
			IGenericParser<T, IOption<T>> parser = (IGenericParser<T, IOption<T>>) this.genericParsers.get(superClass);
			if(parser == null) {
				continue;
			}
			
			Set<IBeforeParser<IOption<T>>> beforeParsers;
			if(!parser.isHandleAll()) {
				beforeParsers = this.getBeforeParsers(type);
			}else{
				beforeParsers = Collections.emptySet();
			}
			
			Set<IAfterParser<T, IOption<T>>> afterParsers = this.getAfterParsers(type);
			if(beforeParsers.isEmpty() && afterParsers.isEmpty()) {
				return (IGenericParser<T, IOption<T>>) parser;
			}
			
			IGenericParser<T, IOption<T>> newParser = new DelegateGenericParser<>(parser, beforeParsers, afterParsers);
			
			this.genericParserCache.put(type, newParser);
			return newParser;
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	protected <T> Set<IBeforeParser<IOption<T>>> getBeforeParsers(Class<T> type) {
		type = this.convertType(type);
		
		Set<IBeforeParser<IOption<T>>> beforeParsers = new LinkedHashSet<>();
		
		for(Class<?> superClass : this.getExtendedClasses(type)) {
			Set<IBeforeParser<?>> parsers = this.genericBeforeParsers.get(superClass);
			if(parsers == null) {
				continue;
			}
			
			for(IBeforeParser<?> beforeParser : parsers) {
				beforeParsers.add((IBeforeParser<IOption<T>>) beforeParser);
			}
		}
		
		Set<IBeforeParser<?>> parsers = this.beforeParsers.get(type);
		if(parsers != null) {
			for(IBeforeParser<?> beforeParser : parsers) {
				beforeParsers.add((IBeforeParser<IOption<T>>) beforeParser);
			}
		}
		
		return beforeParsers;
	}
	
	@SuppressWarnings("unchecked")
	protected <T> Set<IAfterParser<T, IOption<T>>> getAfterParsers(Class<T> type) {
		type = this.convertType(type);
		
		Set<IAfterParser<T, IOption<T>>> afterParsers = new LinkedHashSet<>();
		Set<IAfterParser<?, ?>> parsers = this.afterParsers.get(type);
		if(parsers != null) {
			for(IAfterParser<?, ?> afterParser : parsers) {
				afterParsers.add((IAfterParser<T, IOption<T>>) afterParser);
			}
		}
		
		return afterParsers;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	@Nullable
	public <T> IParser<T, IOption<T>> getParser(@Nullable Class<T> type) {
		type = this.convertType(type);
		
		if(!this.parsers.containsKey(type)) {
			return null;
		}
		
		IParser<T, IOption<T>> cachedParser = (IParser<T, IOption<T>>) this.parserCache.get(type);
		if(cachedParser != null) {
			return cachedParser;
		}
		
		IParser<T, IOption<T>> parser = (IParser<T, IOption<T>>) this.parsers.get(type);
		
		Set<IBeforeParser<IOption<T>>> beforeParsers;
		if(!parser.isHandleAll()) {
			beforeParsers = this.getBeforeParsers(type);
		}else{
			beforeParsers = Collections.emptySet();
		}
		
		Set<IAfterParser<T, IOption<T>>> afterParsers = this.getAfterParsers(type);
		if(beforeParsers.isEmpty() && afterParsers.isEmpty()) {
			return parser;
		}
		
		IParser<T, IOption<T>> newParser = new DelegateParser<>(parser, beforeParsers, afterParsers);
		
		this.parserCache.put(type, newParser);
		return newParser;
	}
	
	@Nonnull
	public OptionFactoryImpl addBuilderFunction(@Nonnull Function<Parameter, Builder<?, ?, ?>> function) {
		Checks.notNull(function, "function");
		
		this.builderFunctions.add(function);
		
		return this;
	}
	
	@Nonnull
	public OptionFactoryImpl removeBuilderFunction(@Nullable Function<Parameter, Builder<?, ?, ?>> function) {
		this.builderFunctions.remove(function);
		
		return this;
	}
	
	@Nonnull
	public List<Function<Parameter, Builder<?, ?, ?>>> getBuilderFunctions() {
		return new ArrayList<>(this.builderFunctions);
	}
	
	@Nonnull
	public <T> OptionFactoryImpl addBuilderConfigureFunction(@Nonnull Class<T> type, @Nonnull BuilderConfigureFunction<T> configureFunction) {
		Checks.notNull(type, "type");
		Checks.notNull(configureFunction, "configureFunction");
		
		this.builderConfigureFunctions.computeIfAbsent(type = this.convertType(type), (key) -> new LinkedHashSet<>()).add(configureFunction);
		
		return this;
	}
	
	@Nonnull
	public OptionFactoryImpl removeBuilderConfigureFunction(@Nullable Class<?> type, @Nullable BuilderConfigureFunction<?> configureFunction) {
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
			return builders.stream()
				.map((builder) -> (BuilderConfigureFunction<T>) builder)
				.collect(Collectors.toList());
		}
		
		return Collections.emptyList();
	}
	
	@Nonnull
	public <T> OptionFactoryImpl addGenericBuilderConfigureFunction(@Nonnull Class<T> type, @Nonnull BuilderConfigureFunction<T> configureFunction) {
		Checks.notNull(type, "type");
		Checks.notNull(configureFunction, "configureFunction");
		
		this.genericBuilderConfigureFunctions.computeIfAbsent(type = this.convertType(type), (key) -> new LinkedHashSet<>()).add(configureFunction);
		
		return this;
	}
	
	@Nonnull
	public OptionFactoryImpl removeGenericBuilderConfigureFunction(@Nullable Class<?> type, @Nullable BuilderConfigureFunction<?> configureFunction) {
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
		
		Set<?> builders = genericBuilderConfigureFunctions.get(type);
		if(builders != null) {
			return builders.stream()
				.map((builder) -> (BuilderConfigureFunction<T>) builder)
				.collect(Collectors.toList());
		}
		
		return Collections.emptyList();
	}
	
	@Override
	public <T> OptionFactoryImpl addParserBefore(@Nonnull Class<T> type, @Nonnull IBeforeParser<IOption<T>> parser) {
		Checks.notNull(type, "type");
		Checks.notNull(parser, "parser");
		
		this.beforeParsers.computeIfAbsent(type = this.convertType(type), (key) -> new LinkedHashSet<>()).add(parser);
		
		return this;
	}
	
	@Override
	@Nonnull
	public OptionFactoryImpl removeParserBefore(@Nullable Class<?> type, @Nullable IBeforeParser<?> parser) {
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
			return parsers.stream()
				.map((parser) -> (IBeforeParser<T>) parser)
				.collect(Collectors.toList());
		}
		
		return Collections.emptyList();
	}
	
	@Override
	public <T> OptionFactoryImpl addGenericParserBefore(@Nonnull Class<T> type, @Nonnull IBeforeParser<IOption<T>> parser) {
		Checks.notNull(type, "type");
		Checks.notNull(parser, "parser");
		
		this.genericBeforeParsers.computeIfAbsent(type = this.convertType(type), (key) -> new LinkedHashSet<>()).add(parser);
		
		return this;
	}
	
	@Override
	@Nonnull
	public OptionFactoryImpl removeGenericParserBefore(@Nullable Class<?> type, @Nullable IBeforeParser<?> parser) {
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
			return parsers.stream()
				.map((parser) -> (IBeforeParser<T>) parser)
				.collect(Collectors.toList());
		}
		
		return Collections.emptyList();
	}
	
	@Override
	public <T> OptionFactoryImpl addParserAfter(@Nonnull Class<T> type, @Nonnull IAfterParser<T, IOption<T>> parser) {
		Checks.notNull(type, "type");
		Checks.notNull(parser, "parser");
		
		this.afterParsers.computeIfAbsent(type = this.convertType(type), (key) -> new LinkedHashSet<>()).add(parser);
		
		return this;
	}
	
	@Override
	@Nonnull
	public OptionFactoryImpl removeParserAfter(@Nullable Class<?> type, @Nullable IAfterParser<?, ?> parser) {
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
	public <T> List<IAfterParser<T, IOption<T>>> getParsersAfter(@Nullable Class<T> type) {
		type = this.convertType(type);
		
		Set<?> parsers = this.afterParsers.get(type);
		if(parsers != null) {
			return parsers.stream()
				.map((parser) -> (IAfterParser<T, IOption<T>>) parser)
				.collect(Collectors.toList());
		}
		
		return Collections.emptyList();
	}
}