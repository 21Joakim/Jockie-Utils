package com.jockie.bot.core.argument.factory.impl;

import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.argument.Endless;
import com.jockie.bot.core.argument.Error;
import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.IEndlessArgument;
import com.jockie.bot.core.argument.factory.IArgumentFactory;
import com.jockie.bot.core.argument.impl.ArgumentImpl;
import com.jockie.bot.core.argument.impl.EndlessArgumentImpl;
import com.jockie.bot.core.argument.parser.IArgumentParser;
import com.jockie.bot.core.argument.parser.ParsedArgument;
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

public class ArgumentFactoryImpl implements IArgumentFactory {
	
	private static int getIndex(JSONTokener tokener) {
		String string = tokener.toString().substring(4);
		string = string.substring(0, string.indexOf(" "));
		
		return Integer.parseInt(string);
	}
	
	protected Map<Class<?>, IArgumentParser<?>> parsers = new HashMap<>();
	protected Map<Class<?>, Class<?>> parserAliases = new HashMap<>();
	
	protected boolean automaticallyCreateEnums = true;
	protected boolean useShardManager = true;
	
	protected Set<Function<Parameter, IArgument.Builder<?, ?, ?>>> builderFunctions = new HashSet<>();
	
	protected ArgumentFactoryImpl() {
		this.registerEssentialParsers();
		this.registerDiscordParsers();
		this.registerJSONParsers();
	}
	
	/**
	 * @param automaticallyCreateEnums whether or not enum parsers should automatically be registered 
	 * when {@link ArgumentFactoryImpl#getParser(Class)} is called if they are not already registered
	 * 
	 * @return the {@link ArgumentFactoryImpl} instance, useful for chaining
	 */
	public ArgumentFactoryImpl setAutomaticallyCreateEnums(boolean automaticallyCreateEnums) {
		this.automaticallyCreateEnums = automaticallyCreateEnums;
		
		return this;
	}
	
	/**
	 * @return whether or not enum parsers should automatically be registered 
	 * when {@link ArgumentFactoryImpl#getParser(Class)} is called if they are not already registered
	 * 
	 * @return the {@link ArgumentFactoryImpl} instance, useful for chaining
	 */
	public boolean isAutomaticallyCreateEnums() {
		return this.automaticallyCreateEnums;
	}
	
	/**
	 * @param useShardManager whether or not the shard-manager (if one is present)
	 * should be used in some of the Discord argument parsers, this includes
	 * users and guilds
	 * 
	 * @return the {@link ArgumentFactoryImpl} instance, useful for chaining
	 */
	public ArgumentFactoryImpl setUseShardManager(boolean useShardManager) {
		this.useShardManager = useShardManager;
		
		return this;
	}
	
	/**
	 * @return whether or not the shard-manager (if one is present)
	 * should be used in some of the Discord argument parsers, this includes
	 * users and guilds
	 */
	public boolean getUseShardManager() {
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
	 * </ul>
	 * 
	 * @return the {@link ArgumentFactoryImpl} instance, useful for chaining
	 */
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
		
		this.registerParserAlias(byte.class, Byte.class);
		this.registerParserAlias(short.class, Short.class);
		this.registerParserAlias(int.class, Integer.class);
		this.registerParserAlias(long.class, Long.class);
		this.registerParserAlias(float.class, Float.class);
		this.registerParserAlias(double.class, Double.class);
		this.registerParserAlias(boolean.class, Boolean.class);
		this.registerParserAlias(char.class, Character.class);
		
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
	public ArgumentFactoryImpl registerJSONParsers() {
		this.registerParser(JSONObject.class, new IArgumentParser<>() {
			
			/* Code from org.json.JSONObject */
			public ParsedArgument<JSONObject> parse(ParseContext context, IArgument<JSONObject> argument, String value) {
				JSONTokener tokener = new JSONTokener(value);
				JSONObject object = new JSONObject();
				
				char character;
				String key;

				if(tokener.nextClean() != '{') {
					return new ParsedArgument<>(false, null);
				}
				
				for(;;) {
					character = tokener.nextClean();
					switch(character) {
						case 0: {
							return new ParsedArgument<>(false, null);
						}
						case '}': {
							return new ParsedArgument<>(true, object, value.substring(getIndex(tokener)));
						}
						default: {
							tokener.back();
							key = tokener.nextValue().toString();
						}
					}
					
					character = tokener.nextClean();
					if(character != ':') {
						return new ParsedArgument<>(false, null);
					}
					
					try {
						object.putOnce(key, tokener.nextValue());
					}catch(JSONException e) {
						return new ParsedArgument<>(false, null);
					}
					
					switch(tokener.nextClean()) {
						case ';': 
						case ',': {
							if(tokener.nextClean() == '}') {
								return new ParsedArgument<>(true, object, value.substring(getIndex(tokener)));
							}
							
							tokener.back();
							
							break;
						}
						case '}': {
							return new ParsedArgument<>(true, object, value.substring(getIndex(tokener)));
						}
						default: {
							return new ParsedArgument<>(false, null);
						}
					}
				}
			}
			
			public boolean isHandleAll() {
				return true;
			}
		});
		
		this.registerParser(JSONArray.class, new IArgumentParser<>() {
			
			/* Code from org.json.JSONArray */
			public ParsedArgument<JSONArray> parse(ParseContext context, IArgument<JSONArray> argument, String value) {
				JSONTokener tokener = new JSONTokener(value);
				JSONArray array = new JSONArray();
				
				if(tokener.nextClean() != '[') {
					return new ParsedArgument<>(false, null);
				}
				
				if(tokener.nextClean() == ']') {
					return new ParsedArgument<>(true, array, value.substring(getIndex(tokener)));
				}
				
				tokener.back();
				
				for(;;) {
					if(tokener.nextClean() == ',') {
						tokener.back();
						
						array.put(JSONObject.NULL);
					}else{
						tokener.back();
						
						try {
							array.put(tokener.nextValue());
						}catch(JSONException e) {
							return new ParsedArgument<>(false, null);
						}
					}
					
					switch (tokener.nextClean()) {
						case ',': {
							if(tokener.nextClean() == ']') {
								return new ParsedArgument<>(true, array, value.substring(getIndex(tokener)));
							}
							
							tokener.back();
							
							break;
						}
						case ']': {
							return new ParsedArgument<>(true, array, value.substring(getIndex(tokener)));
						}
						default: {
							return new ParsedArgument<>(false, null);
						}
					}
				}
			}
			
			public boolean isHandleAll() {
				return true;
			}
		});
		
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
		
		this.unregisterParserAlias(byte.class);
		this.unregisterParserAlias(short.class);
		this.unregisterParserAlias(int.class);
		this.unregisterParserAlias(long.class);
		this.unregisterParserAlias(float.class);
		this.unregisterParserAlias(double.class);
		this.unregisterParserAlias(boolean.class);
		this.unregisterParserAlias(char.class);
		
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
	public ArgumentFactoryImpl unregisterJSONParsers() {
		this.unregisterParser(JSONObject.class);
		this.unregisterParser(JSONArray.class);
		
		return this;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public IArgument<?> createArgument(Parameter parameter) {
		Class<?> type = parameter.getType();
		
		boolean isOptional = false;
		if(type.isAssignableFrom(Optional.class)) {
			Class<?>[] classes = CommandUtility.getClasses(parameter.getParameterizedType());
			if(classes.length > 0 && classes[0] != null) {
				type = classes[0];
				isOptional = true;
			}
		}
		
		IArgument.Builder builder = null;
		for(Function<Parameter, IArgument.Builder<?, ?, ?>> function : this.builderFunctions) {
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
			if(parser != null) {
				builder.setParser(parser);
			}
		}
		
		if(builder.getParser() != null) {
			if(parameter.isNamePresent()) {
				builder.setName(parameter.getName());
			}
			
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
			
			if(isOptional) {
				builder.setDefaultAsNull();
			}
			
			Error error = parameter.getAnnotation(Error.class);
			if(error != null) {
				builder.setErrorMessage(error.value());
			}
			
			return builder.build();
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
			
			Argument info = parameter.getAnnotation(Argument.class);
			if(info != null) {
				builder.setAcceptEmpty(info.acceptEmpty());
				builder.setAcceptQuote(info.acceptQuote());
				
				if(info.value().length() > 0) {
					builder.setName(info.value());
				}
			
				if(info.nullDefault()) {
					builder.setDefaultAsNull();
				}
				
				if(info.endless()) {
					throw new IllegalArgumentException("Not a valid candidate, candidate may not be endless");
				}
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
			
			return endlessBuilder.build();
		}
	}
	
	public <T> ArgumentFactoryImpl registerParser(Class<T> type, IArgumentParser<T> parser) {
		this.parsers.put(type, parser);
		
		return this;
	}
	
	public ArgumentFactoryImpl unregisterParser(Class<?> type) {
		this.parsers.remove(type);
		
		return this;
	}
	
	public <T> ArgumentFactoryImpl registerParserAlias(Class<T> aliasKey, Class<? extends T> alias) {
		this.parserAliases.put(aliasKey, alias);
		
		return this;
	}
	
	public ArgumentFactoryImpl unregisterParserAlias(Class<?> alias) {
		this.parserAliases.remove(alias);
		
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public <T> IArgumentParser<T> getParser(Class<T> type) {
		if(this.automaticallyCreateEnums) {
			/* Check if the enum type already has a parser registered for it otherwise create a new one */
			if(type.isEnum() && !this.parsers.containsKey(type)) {
				final Enum<?>[] enums = (Enum[]) type.getEnumConstants();
				
				this.registerParser(type, (context, argument, value) -> {
					for(Enum<?> enumEntry : enums) {
						String name = enumEntry.name();
						if(name.equalsIgnoreCase(value) || name.replace("_", " ").equalsIgnoreCase(value)) {
							return new ParsedArgument<>(true, (T) enumEntry);
						}
					}
					
					return new ParsedArgument<>(false, null);
				});
			}
		}
		
		if(this.parserAliases.containsKey(type)) {
			type = (Class<T>) this.parserAliases.get(type);
		}
		
		if(this.parsers.containsKey(type)) {
			return (IArgumentParser<T>) this.parsers.get(type);
		}
		
		return null;
	}
	
	public ArgumentFactoryImpl registerBuilderFunction(Function<Parameter, IArgument.Builder<?, ?, ?>> function) {
		this.builderFunctions.add(function);
		
		return this;
	}
	
	public ArgumentFactoryImpl unregisterBuilderFunction(Function<Parameter, IArgument.Builder<?, ?, ?>> function) {
		this.builderFunctions.remove(function);
		
		return this;
	}
}