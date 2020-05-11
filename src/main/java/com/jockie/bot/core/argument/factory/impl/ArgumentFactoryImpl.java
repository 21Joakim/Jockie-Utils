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
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.argument.Endless;
import com.jockie.bot.core.argument.Error;
import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.IArgument.Builder;
import com.jockie.bot.core.argument.IEndlessArgument;
import com.jockie.bot.core.argument.factory.IArgumentFactory;
import com.jockie.bot.core.argument.impl.ArgumentImpl;
import com.jockie.bot.core.argument.impl.EndlessArgumentImpl;
import com.jockie.bot.core.argument.parser.IArgumentAfterParser;
import com.jockie.bot.core.argument.parser.IArgumentBeforeParser;
import com.jockie.bot.core.argument.parser.IArgumentParser;
import com.jockie.bot.core.argument.parser.IGenericArgumentParser;
import com.jockie.bot.core.argument.parser.ParsedArgument;
import com.jockie.bot.core.argument.parser.impl.JSONArrayParser;
import com.jockie.bot.core.argument.parser.impl.JSONObjectParser;
import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.utility.ArgumentUtility;
import com.jockie.bot.core.utility.CommandUtility;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.tuple.Pair;

public class ArgumentFactoryImpl implements IArgumentFactory {
	
	protected Map<Class<?>, IGenericArgumentParser<?>> genericParsers = new HashMap<>();
	
	protected Map<Class<?>, IArgumentParser<?>> parsers = new HashMap<>();
	protected Map<Class<?>, Class<?>> parserAliases = new HashMap<>();
	
	protected Map<Class<?>, Set<IArgumentBeforeParser<?>>> beforeParsers = new HashMap<>();
	protected Map<Class<?>, Set<IArgumentBeforeParser<?>>> genericBeforeParsers = new HashMap<>();
	
	protected Map<Class<?>, Set<IArgumentAfterParser<?>>> afterParsers = new HashMap<>();
	
	protected boolean useShardManager = true;
	
	protected Set<Function<Parameter, Builder<?, ?, ?>>> builderFunctions = new LinkedHashSet<>();
	
	protected Map<Class<?>, Set<BuilderConfigureFunction>> builderConfigureFunctions = new HashMap<>();
	protected Map<Class<?>, Set<BuilderConfigureFunction>> genericBuilderConfigureFunctions = new HashMap<>();
	
	protected ArgumentFactoryImpl() {
		this.registerEssentialParsers();
		this.registerDiscordParsers();
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
	 * @param useShardManager whether or not the shard-manager (if one is present)
	 * should be used in some of the Discord argument parsers, this includes
	 * users and guilds
	 * 
	 * @return the {@link ArgumentFactoryImpl} instance, useful for chaining
	 */
	@Nonnull
	public ArgumentFactoryImpl setUseShardManager(boolean useShardManager) {
		this.useShardManager = useShardManager;
		
		return this;
	}
	
	/**
	 * @return whether or not the shard-manager (if one is present)
	 * should be used in some of the Discord argument parsers, this includes
	 * users and guilds
	 */
	public boolean isUseShardManager() {
		return this.useShardManager;
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
	public ArgumentFactoryImpl registerEssentialParsers() {
		this.registerParser(Byte.class, (context, argument, value) -> {
			try {
				return new ParsedArgument<>(true, Byte.parseByte(value));
			}catch(NumberFormatException e) {
				return new ParsedArgument<>(false, null);
			}
		});
		
		this.registerParser(Short.class, (context, argument, value) -> {
			try {
				return new ParsedArgument<>(true, Short.parseShort(value));
			}catch(NumberFormatException e) {
				return new ParsedArgument<>(false, null);
			}
		});
		
		this.registerParser(Integer.class, (context, argument, value) -> {
			try {
				return new ParsedArgument<>(true, Integer.parseInt(value));
			}catch(NumberFormatException e) {
				return new ParsedArgument<>(false, null);
			}
		});
		
		this.registerParser(Long.class, (context, argument, value) -> {
			try {
				return new ParsedArgument<>(true, Long.parseLong(value));
			}catch(NumberFormatException e) {
				return new ParsedArgument<>(false, null);
			}
		});
		
		this.registerParser(Float.class, (context, argument, value) -> {
			try {
				return new ParsedArgument<>(true, Float.parseFloat(value));
			}catch(NumberFormatException e) {
				return new ParsedArgument<>(false, null);
			}
		});
		
		this.registerParser(Double.class, (context, argument, value) -> {
			try {
				return new ParsedArgument<>(true, Double.parseDouble(value));
			}catch(NumberFormatException e) {
				return new ParsedArgument<>(false, null);
			}
		});
		
		this.registerParser(Boolean.class, (context, argument, value) -> {
			if(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
				return new ParsedArgument<>(true, Boolean.parseBoolean(value));
			}
			
			return new ParsedArgument<>(false, null);
		});
		
		this.registerParser(Character.class, (context, argument, value) -> {
			if(value.length() == 1) {
				return new ParsedArgument<>(true, value.charAt(0));
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		this.registerParser(String.class, new IArgumentParser<>() {
			public ParsedArgument<String> parse(ParseContext context, IArgument<String> argument, String value) {
				return new ParsedArgument<>(true, value);
			}
			
			/* 
			 * Because the String just accepts the content straight up
			 * it is reasonable to make it have the max value it can have 
			 * to make it end up as late as possible
			 */
			public int getPriority() {
				return Integer.MAX_VALUE;
			}
		});
		
		this.registerGenericParser(Enum.class, (context, type, argument, value) -> {
			for(Enum<?> enumEntry : type.getEnumConstants()) {
				String name = enumEntry.name();
				if(name.equalsIgnoreCase(value) || name.replace("_", " ").equalsIgnoreCase(value)) {
					return new ParsedArgument<>(true, enumEntry);
				}
			}
			
			return new ParsedArgument<>(false, null);
		});
		
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
	 * 	<li>Emote</li>
	 * 	<li>User</li>
	 * 	<li>Guild</li>
	 * </ul>
	 * 
	 * @return the {@link ArgumentFactoryImpl} instance, useful for chaining
	 */
	@Nonnull
	public ArgumentFactoryImpl registerDiscordParsers() {
		this.registerParser(Member.class, (context, argument, value) -> {
			List<Member> members = ArgumentUtility.getMembersByIdOrName(context.getMessage().getGuild(), value, true);
			
			if(members.size() == 1) {
				return new ParsedArgument<>(true, members.get(0));
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		this.registerParser(TextChannel.class, (context, argument, value) -> {
			List<TextChannel> channels = ArgumentUtility.getTextChannelsByIdOrName(context.getMessage().getGuild(), value, true);
			
			if(channels.size() == 1) {
				return new ParsedArgument<>(true, channels.get(0));
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		this.registerParser(VoiceChannel.class, (context, argument, value) -> {
			List<VoiceChannel> channels = ArgumentUtility.getVoiceChannelsByIdOrName(context.getMessage().getGuild(), value, true);
			
			if(channels.size() == 1) {
				return new ParsedArgument<>(true, channels.get(0));
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		/* Even though Category technically does implement Channel I do not want it to be a part of the Channel argument, objections? */
		this.registerParser(GuildChannel.class, (context, argument, value) -> {
			List<GuildChannel> channels = Collections.unmodifiableList(ArgumentUtility.getTextChannelsByIdOrName(context.getMessage().getGuild(), value, true));
			if(channels.size() == 0) {
				channels = Collections.unmodifiableList(ArgumentUtility.getVoiceChannelsByIdOrName(context.getMessage().getGuild(), value, true));
			}
			
			if(channels.size() == 1) {
				return new ParsedArgument<>(true, channels.get(0));
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		this.registerParser(Category.class, (context, argument, value) -> {
			List<Category> categories = ArgumentUtility.getCategoriesByIdOrName(context.getMessage().getGuild(), value, true);
			
			if(categories.size() == 1) {
				return new ParsedArgument<>(true, categories.get(0));
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		this.registerParser(Role.class, (context, argument, value) -> {
			List<Role> roles = ArgumentUtility.getRolesByIdOrName(context.getMessage().getGuild(), value, true);
			
			if(roles.size() == 1) {
				return new ParsedArgument<>(true, roles.get(0));
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		this.registerParser(Emote.class, (context, argument, value) -> {
			List<Emote> emotes = ArgumentUtility.getEmotesByIdOrName(context.getMessage().getGuild(), value, true);
			
			if(emotes.size() == 1) {
				return new ParsedArgument<>(true, emotes.get(0));
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		this.registerParser(User.class, (context, argument, value) -> {
			JDA jda = context.getMessage().getJDA();
			
			List<User> users = null;
			if(this.useShardManager && jda.getAccountType().equals(AccountType.BOT)) {
				ShardManager shardManager = jda.getShardManager();
				if(shardManager != null) {
					users = ArgumentUtility.getUsersByIdOrName(shardManager, value, true);
				}
			}
			
			if(users == null) {
				users = ArgumentUtility.getUsersByIdOrName(jda, value, true);
			}
			
			if(users.size() == 1) {
				return new ParsedArgument<>(true, users.get(0));
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		this.registerParser(Guild.class, (context, argument, value) -> {
			JDA jda = context.getMessage().getJDA();
			
			List<Guild> guilds = null;
			if(this.useShardManager && jda.getAccountType().equals(AccountType.BOT)) {
				ShardManager shardManager = jda.getShardManager();
				if(shardManager != null) {
					guilds = ArgumentUtility.getGuildsByIdOrName(shardManager, value, true);
				}
			}
			
			if(guilds == null) {
				guilds = ArgumentUtility.getGuildsByIdOrName(jda, value, true);
			}
			
			if(guilds.size() == 1) {
				return new ParsedArgument<>(true, guilds.get(0));
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
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
	public ArgumentFactoryImpl registerJSONParsers() {
		this.registerParser(JSONObject.class, new JSONObjectParser());
		this.registerParser(JSONArray.class, new JSONArrayParser());
		
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
	public ArgumentFactoryImpl unregisterEssentialParsers() {
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
	 * Unregister the parsers registered through {@link ArgumentFactoryImpl#registerDiscordParsers()}
	 * <br><br>
	 * <b>NOTE</b>:
	 * Using this after registering any custom parsers will remove those custom parsers as well
	 * 
	 * @return the {@link ArgumentFactoryImpl} instance, useful for chaining
	 */
	@Nonnull
	public ArgumentFactoryImpl unregisterDiscordParsers() {
		this.unregisterParser(Member.class);
		this.unregisterParser(TextChannel.class);
		this.unregisterParser(VoiceChannel.class);
		this.unregisterParser(GuildChannel.class);
		this.unregisterParser(Category.class);
		this.unregisterParser(Role.class);
		this.unregisterParser(Emote.class);
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
	public ArgumentFactoryImpl unregisterJSONParsers() {
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
		for(Class<?> superClass : this.getExtendedClasses(type)) {
			Pair<Boolean, Builder<?, ?, ?>> pair = this.configureBuilder(parameter, superClass, builder, this.genericBuilderConfigureFunctions.get(superClass));
			if(pair.getLeft()) {
				return pair;
			}
			
			builder = pair.getRight();
		}
		
		return Pair.of(false, builder);
	}
	
	protected Pair<Boolean, Builder<?, ?, ?>> configureBuilder(Parameter parameter, Class<?> type, Builder<?, ?, ?> builder, Set<BuilderConfigureFunction> configureFunctions) {
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
		Pair<Boolean, Builder<?, ?, ?>> pair = this.configureBuilderGeneric(parameter, type, builder);
		if(pair.getLeft()) {
			return pair.getRight();
		}
		
		return this.configureBuilder(parameter, type, builder, this.builderConfigureFunctions.get(type)).getRight();
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public IArgument<?> createArgument(Parameter parameter) {
		Class<?> type = this.convertType(parameter.getType());
		
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
			builder = new ArgumentImpl.Builder(type);
		}
		
		if(builder.getParser() == null) {
			IArgumentParser<?> parser = this.getParser(type);
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
			
			if(isOptional) {
				builder.setDefaultAsNull();
			}
			
			Error error = parameter.getAnnotation(Error.class);
			if(error != null) {
				builder.setErrorMessage(error.value());
			}
			
			return this.configureBuilder(parameter, type, builder).build();
		}else{
			Class<?> componentType = null;
			if(type.isArray()) {
				componentType = type.getComponentType();
			}
			
			IArgumentParser<?> parser;
			if(componentType != null) {
				parser = this.getParser(componentType);
				
				if(parser == null) {
					throw new IllegalArgumentException("There are no default arguments for the component " + componentType.toString());
				}
			}else{
				throw new IllegalArgumentException("There are no default arguments for " + type.toString());
			}
			
			builder = new ArgumentImpl.Builder(type)
				.setParser(parser);
			
			if(parameter.isNamePresent()) {
				builder.setName(parameter.getName());
			}
			
			this.applyArgumentAnnotation(builder, parameter);
			
			if(builder.isEndless()) {
				throw new IllegalArgumentException("Not a valid candidate, candidate may not be endless");
			}
			
			IEndlessArgument.Builder endlessBuilder = new EndlessArgumentImpl.Builder(componentType).setArgument(builder.build());
			if(parameter.isAnnotationPresent(Endless.class)) {
				Endless endless = parameter.getAnnotation(Endless.class);
				
				endlessBuilder.setMinArguments(endless.minArguments())
					.setMaxArguments(endless.maxArguments())
					.setEndless(endless.endless());
			}
			
			Error error = parameter.getAnnotation(Error.class);
			if(error != null) {
				builder.setErrorMessage(error.value());
			}
			
			return this.configureBuilder(parameter, type, endlessBuilder).build();
		}
	}
	
	@Override
	@Nonnull
	public <T> ArgumentFactoryImpl registerGenericParser(@Nonnull Class<T> type, @Nonnull IGenericArgumentParser<T> parser) {
		Checks.notNull(type, "type");
		Checks.notNull(parser, "parser");
		
		this.genericParsers.put(this.convertType(type), parser);
		
		return this;
	}
	
	@Override
	@Nonnull
	public <T> ArgumentFactoryImpl unregisterGenericParser(@Nullable Class<T> type) {
		this.genericParsers.remove(this.convertType(type));
		
		return this;
	}
	
	@Override
	@Nonnull
	public <T> ArgumentFactoryImpl registerParser(@Nonnull Class<T> type, @Nonnull IArgumentParser<T> parser) {
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
	public <T> IGenericArgumentParser<T> getGenericParser(Class<T> type) {
		for(Class<?> superClass : this.getExtendedClasses(type)) {
			IGenericArgumentParser<?> genericParser = this.genericParsers.get(superClass);
			if(genericParser != null) {
				return (IGenericArgumentParser<T>) genericParser;
			}
		}
		
		return null;
	}
	
	protected Map<Class<?>, IArgumentParser<?>> parserCache = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	protected <T> Set<IArgumentBeforeParser<T>> getBeforeParsers(Class<T> type) {
		Set<IArgumentBeforeParser<T>> beforeParsers = new LinkedHashSet<>();
		
		for(Class<?> superClass : this.getExtendedClasses(type)) {
			Set<IArgumentBeforeParser<?>> parsers = this.genericBeforeParsers.get(superClass);
			if(parsers == null) {
				continue;
			}
			
			for(IArgumentBeforeParser<?> beforeParser : parsers) {
				beforeParsers.add((IArgumentBeforeParser<T>) beforeParser);
			}
		}
		
		Set<IArgumentBeforeParser<?>> parsers = this.beforeParsers.get(type);
		if(parsers != null) {
			for(IArgumentBeforeParser<?> beforeParser : parsers) {
				beforeParsers.add((IArgumentBeforeParser<T>) beforeParser);
			}
		}
		
		return beforeParsers;
	}
	
	@SuppressWarnings("unchecked")
	protected <T> Set<IArgumentAfterParser<T>> getAfterParsers(Class<T> type) {
		Set<IArgumentAfterParser<T>> afterParsers = new LinkedHashSet<>();
		Set<IArgumentAfterParser<?>> parsers = this.afterParsers.get(type);
		if(parsers != null) {
			for(IArgumentAfterParser<?> afterParser : parsers) {
				afterParsers.add((IArgumentAfterParser<T>) afterParser);
			}
		}
		
		return afterParsers;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> IArgumentParser<T> getParser(Class<T> type) {
		Checks.notNull(type, "type");
		
		type = this.convertType(type);
		
		if(!this.parsers.containsKey(type)) {
			return null;
		}
		
		if(this.parserCache.containsKey(type)) {
			return (IArgumentParser<T>) this.parserCache.get(type);
		}
		
		IArgumentParser<T> parser = (IArgumentParser<T>) this.parsers.get(type);
		
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
		Set<IArgumentBeforeParser<T>> beforeParsers;
		if(!parser.isHandleAll()) {
			beforeParsers = this.getBeforeParsers(type);
		}else{
			beforeParsers = Collections.emptySet();
		}
		
		Set<IArgumentAfterParser<T>> afterParsers = this.getAfterParsers(type);
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
		IArgumentParser<T> newParser = new IArgumentParser<>() {
			@Override
			public ParsedArgument<T> parse(ParseContext context, IArgument<T> argument, String content) {
				for(IArgumentBeforeParser<T> parser : beforeParsers) {
					ParsedArgument<String> parsed = parser.parse(context, argument, content);
					if(!parsed.isValid()) {
						return new ParsedArgument<T>(false, null);
					}
					
					content = parsed.getObject();
				}
				
				ParsedArgument<T> parsed = parser.parse(context, argument, content);
				if(!parsed.isValid()) {
					return parsed;
				}
				
				T object = parsed.getObject();
				for(IArgumentAfterParser<T> parser : afterParsers) {
					parsed = parser.parse(context, argument, object);
					if(!parsed.isValid()) {
						return parsed;
					}
					
					object = parsed.getObject();
				}
				
				return new ParsedArgument<T>(true, object, parsed.getContentLeft());
			}
			
			@Override
			public int getPriority() {
				return parser.getPriority();
			}
			
			@Override
			public boolean isHandleAll() {
				return parser.isHandleAll();
			}
		};
		
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
	public ArgumentFactoryImpl addBuilderConfigureFunction(@Nonnull Class<?> type, @Nonnull BuilderConfigureFunction configureFunction) {
		Checks.notNull(type, "type");
		Checks.notNull(configureFunction, "configureFunction");
		
		this.builderConfigureFunctions.computeIfAbsent(type = this.convertType(type), (key) -> new LinkedHashSet<>()).add(configureFunction);
		
		return this;
	}
	
	@Nonnull
	public ArgumentFactoryImpl removeBuilderConfigureFunction(@Nullable Class<?> type, @Nullable BuilderConfigureFunction configureFunction) {
		type = this.convertType(type);
		
		if(this.builderConfigureFunctions.containsKey(type)) {
			this.builderConfigureFunctions.get(type).remove(configureFunction);
		}
		
		return this;
	}
	
	@Nonnull
	public List<BuilderConfigureFunction> getBuilderConfigureFunctions(@Nullable Class<?> type) {
		type = this.convertType(type);
		
		if(this.builderConfigureFunctions.containsKey(type)) {
			return new ArrayList<>(this.builderConfigureFunctions.get(type));
		}
		
		return Collections.emptyList();
	}
	
	@Nonnull
	public ArgumentFactoryImpl addGenericBuilderConfigureFunction(@Nonnull Class<?> type, @Nonnull BuilderConfigureFunction configureFunction) {
		Checks.notNull(type, "type");
		Checks.notNull(configureFunction, "configureFunction");
		
		this.genericBuilderConfigureFunctions.computeIfAbsent(type = this.convertType(type), (key) -> new LinkedHashSet<>()).add(configureFunction);
		
		return this;
	}
	
	@Nonnull
	public ArgumentFactoryImpl removeGenericBuilderConfigureFunction(@Nullable Class<?> type, @Nullable BuilderConfigureFunction configureFunction) {
		type = this.convertType(type);
		
		if(this.genericBuilderConfigureFunctions.containsKey(type)) {
			this.genericBuilderConfigureFunctions.get(type).remove(configureFunction);
		}
		
		return this;
	}
	
	@Nonnull
	public List<BuilderConfigureFunction> getGenericBuilderConfigureFunctions(@Nullable Class<?> type) {
		type = this.convertType(type);
		
		if(this.genericBuilderConfigureFunctions.containsKey(type)) {
			return new ArrayList<>(this.genericBuilderConfigureFunctions.get(type));
		}
		
		return Collections.emptyList();
	}
	
	@Override
	public <T> ArgumentFactoryImpl addParserBefore(@Nonnull Class<T> type, @Nonnull IArgumentBeforeParser<T> parser) {
		Checks.notNull(type, "type");
		Checks.notNull(parser, "parser");
		
		this.beforeParsers.computeIfAbsent(type = this.convertType(type), (key) -> new LinkedHashSet<>()).add(parser);
		
		return this;
	}
	
	@Override
	@Nonnull
	public <T> ArgumentFactoryImpl removeParserBefore(@Nullable Class<T> type, @Nullable IArgumentBeforeParser<T> parser) {
		type = this.convertType(type);
		
		if(this.beforeParsers.containsKey(type)) {
			this.beforeParsers.get(type).remove(parser);
		}
		
		return this;
	}
	
	@Override
	public <T> List<IArgumentBeforeParser<T>> getParsersBefore(Class<T> type) {
		type = this.convertType(type);
		
		if(this.beforeParsers.containsKey(type)) {
			return this.beforeParsers.get(type).stream()
				.map(IArgumentBeforeParser.class::cast)
				.collect(Collectors.toList());
		}
		
		return Collections.emptyList();
	}
	
	@Override
	public <T> ArgumentFactoryImpl addGenericParserBefore(@Nonnull Class<T> type, @Nonnull IArgumentBeforeParser<T> parser) {
		Checks.notNull(type, "type");
		Checks.notNull(parser, "parser");
		
		this.genericBeforeParsers.computeIfAbsent(type = this.convertType(type), (key) -> new LinkedHashSet<>()).add(parser);
		
		return this;
	}
	
	@Override
	@Nonnull
	public <T> ArgumentFactoryImpl removeGenericParserBefore(@Nullable Class<T> type, @Nullable IArgumentBeforeParser<T> parser) {
		type = this.convertType(type);
		
		if(this.genericBeforeParsers.containsKey(type)) {
			this.genericBeforeParsers.get(type).remove(parser);
		}
		
		return this;
	}
	
	@Override
	public <T> List<IArgumentBeforeParser<T>> getGenericParsersBefore(Class<T> type) {
		type = this.convertType(type);
		
		if(this.genericBeforeParsers.containsKey(type)) {
			return this.genericBeforeParsers.get(type).stream()
				.map(IArgumentBeforeParser.class::cast)
				.collect(Collectors.toList());
		}
		
		return Collections.emptyList();
	}
	
	@Override
	public <T> ArgumentFactoryImpl addParserAfter(@Nonnull Class<T> type, @Nonnull IArgumentAfterParser<T> parser) {
		Checks.notNull(type, "type");
		Checks.notNull(parser, "parser");
		
		this.afterParsers.computeIfAbsent(type = this.convertType(type), (key) -> new LinkedHashSet<>()).add(parser);
		
		return this;
	}
	
	@Override
	@Nonnull
	public <T> ArgumentFactoryImpl removeParserAfter(@Nullable Class<T> type, @Nullable IArgumentAfterParser<T> parser) {
		type = this.convertType(type);
		
		if(this.afterParsers.containsKey(type)) {
			this.afterParsers.get(type).remove(parser);
		}
		
		return this;
	}

	@Override
	public <T> List<IArgumentAfterParser<T>> getParsersAfter(Class<T> type) {
		type = this.convertType(type);
		
		if(this.afterParsers.containsKey(type)) {
			return this.afterParsers.get(type).stream()
				.map(IArgumentAfterParser.class::cast)
				.collect(Collectors.toList());
		}
		
		return Collections.emptyList();
	}
}