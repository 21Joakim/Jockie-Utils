package com.jockie.bot.core.argument.impl;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.parser.IArgumentParser;
import com.jockie.bot.core.argument.parser.ParsedArgument;
import com.jockie.bot.core.utility.ArgumentUtility;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.sharding.ShardManager;

public class ArgumentFactory {
	
	private ArgumentFactory() {}
	
	private static Map<Class<?>, IArgumentParser<?>> arguments = new HashMap<>();
	private static Map<Class<?>, Class<?>> aliases = new HashMap<>();
	
	private static boolean automaticallyCreateEnums = true;
	private static boolean useShardManager = true;
	
	static {
		ArgumentFactory.registerEssentialParsers();
		ArgumentFactory.registerDiscordParsers();
		ArgumentFactory.registerJSONParsers();
	}
	
	/**
	 * @param automaticallyCreateEnums whether or not enum parsers should automatically be registered 
	 * when {@link ArgumentFactory#builder(Class)} is called if they are not already registered
	 */
	public static void setAutomaticallyCreateEnums(boolean automaticallyCreateEnums) {
		ArgumentFactory.automaticallyCreateEnums = automaticallyCreateEnums;
	}
	
	/**
	 * @return whether or not enum parsers should automatically be registered 
	 * when {@link ArgumentFactory#builder(Class)} is called if they are not already registered
	 */
	public static boolean isAutomaticallyCreateEnums() {
		return ArgumentFactory.automaticallyCreateEnums;
	}
	
	public static void setUseShardManager(boolean useShardManager) {
		ArgumentFactory.useShardManager = useShardManager;
	}
	
	public static boolean getUseShardManager() {
		return ArgumentFactory.useShardManager;
	}
	
	/**
	 * Register an argument parser which will be used for the specified argument type
	 * 
	 * @param type the argument type to register the parser for
	 * @param parser the parser to use for the specified argument type
	 */
	public static <T> void registerParser(Class<T> type, IArgumentParser<T> parser) {
		ArgumentFactory.arguments.put(type, parser);
	}
	
	/**
	 * Register an alias class, the alias should map to a class which has a registered parser
	 * 
	 * @param classKey the class to add as an alias
	 * @param classValue the class to map to, this class should have a registered parser
	 */
	public static <T> void registerAlias(Class<T> classKey, Class<? extends T> classValue) {
		ArgumentFactory.aliases.put(classKey, classValue);
	}
	
	/**
	 * Unregister an argument parser
	 * 
	 * @param type the argument type of the parser to unregister
	 */
	public static void unregisterParser(Class<?> type) {
		ArgumentFactory.arguments.remove(type);
	}
	
	/**
	 * Unregister an alias
	 * 
	 * @param alias the alias to unregister
	 */
	public static void unregisterAlias(Class<?> alias) {
		ArgumentFactory.aliases.remove(alias);
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
	 */
	public static void registerEssentialParsers() {
		ArgumentFactory.registerParser(Byte.class, (event, argument, value) -> {
			try {
				return new ParsedArgument<>(true, Byte.parseByte(value));
			}catch(NumberFormatException e) {
				return new ParsedArgument<>(false, null);
			}
		});
		
		ArgumentFactory.registerParser(Short.class, (event, argument, value) -> {
			try {
				return new ParsedArgument<>(true, Short.parseShort(value));
			}catch(NumberFormatException e) {
				return new ParsedArgument<>(false, null);
			}
		});
		
		ArgumentFactory.registerParser(Integer.class, (event, argument, value) -> {
			try {
				return new ParsedArgument<>(true, Integer.parseInt(value));
			}catch(NumberFormatException e) {
				return new ParsedArgument<>(false, null);
			}
		});
		
		ArgumentFactory.registerParser(Long.class, (event, argument, value) -> {
			try {
				return new ParsedArgument<>(true, Long.parseLong(value));
			}catch(NumberFormatException e) {
				return new ParsedArgument<>(false, null);
			}
		});
		
		ArgumentFactory.registerParser(Float.class, (event, argument, value) -> {
			try {
				return new ParsedArgument<>(true, Float.parseFloat(value));
			}catch(NumberFormatException e) {
				return new ParsedArgument<>(false, null);
			}
		});
		
		ArgumentFactory.registerParser(Double.class, (event, argument, value) -> {
			try {
				return new ParsedArgument<>(true, Double.parseDouble(value));
			}catch(NumberFormatException e) {
				return new ParsedArgument<>(false, null);
			}
		});
		
		ArgumentFactory.registerParser(Boolean.class, (event, argument, value) -> {
			if(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
				return new ParsedArgument<>(true, Boolean.parseBoolean(value));
			}
			
			return new ParsedArgument<>(false, null);
		});
		
		ArgumentFactory.registerParser(Character.class, (event, argument, value) -> {
			if(value.length() == 1) {
				return new ParsedArgument<>(true, value.charAt(0));
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		ArgumentFactory.registerParser(String.class, new IArgumentParser<>() {
			public ParsedArgument<String> parse(Message message, IArgument<String> argument, String value) {
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
		
		ArgumentFactory.registerAlias(byte.class, Byte.class);
		ArgumentFactory.registerAlias(short.class, Short.class);
		ArgumentFactory.registerAlias(int.class, Integer.class);
		ArgumentFactory.registerAlias(long.class, Long.class);
		ArgumentFactory.registerAlias(float.class, Float.class);
		ArgumentFactory.registerAlias(double.class, Double.class);
		ArgumentFactory.registerAlias(boolean.class, Boolean.class);
		ArgumentFactory.registerAlias(char.class, Character.class);
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
	 */
	public static void registerDiscordParsers() {
		ArgumentFactory.registerParser(Member.class, (event, argument, value) -> {
			Member member = ArgumentUtility.getMemberByIdOrTag(event.getGuild(), value, true);
			
			if(member != null) {
				return new ParsedArgument<>(true, member);
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		ArgumentFactory.registerParser(TextChannel.class, (event, argument, value) -> {
			TextChannel channel = ArgumentUtility.getTextChannelByIdOrName(event.getGuild(), value, true);
			
			if(channel != null) {
				return new ParsedArgument<>(true, channel);
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		ArgumentFactory.registerParser(VoiceChannel.class, (event, argument, value) -> {
			VoiceChannel channel = ArgumentUtility.getVoiceChannelByIdOrName(event.getGuild(), value, true);
			
			if(channel != null) {
				return new ParsedArgument<>(true, channel);
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		/* Even though Category technically does implement Channel I do not want it to be a part of the Channel argument, objections? */
		ArgumentFactory.registerParser(GuildChannel.class, (event, argument, value) -> {
			GuildChannel channel = ArgumentUtility.getTextChannelByIdOrName(event.getGuild(), value, true);
			
			if(channel != null || (channel = ArgumentUtility.getVoiceChannelByIdOrName(event.getGuild(), value, true)) != null) {
				return new ParsedArgument<>(true, channel);
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		ArgumentFactory.registerParser(Category.class, (event, argument, value) -> {
			Category category = ArgumentUtility.getCategoryByIdOrName(event.getGuild(), value, true);
			
			if(category != null) {
				return new ParsedArgument<>(true, category);
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		ArgumentFactory.registerParser(Role.class, (event, argument, value) -> {
			Role role = ArgumentUtility.getRoleByIdOrName(event.getGuild(), value, true);
			
			if(role != null) {
				return new ParsedArgument<>(true, role);
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		ArgumentFactory.registerParser(Emote.class, (event, argument, value) -> {
			Emote emote = ArgumentUtility.getEmoteByIdOrName(event.getGuild(), value, true);
			
			if(emote != null) {
				return new ParsedArgument<>(true, emote);
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		ArgumentFactory.registerParser(User.class, (event, argument, value) -> {
			User user = null;
			if(ArgumentFactory.useShardManager && event.getJDA().getAccountType().equals(AccountType.BOT)) {
				ShardManager shardManager = event.getJDA().getShardManager();
				if(shardManager != null) {
					user = ArgumentUtility.getUserByIdOrTag(shardManager, value, true);
				}
			}
			
			if(user == null) {
				user = ArgumentUtility.getUserByIdOrTag(event.getJDA(), value, true);
			}
			
			if(user != null) {
				return new ParsedArgument<>(true, user);
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		ArgumentFactory.registerParser(Guild.class, (event, argument, value) -> {
			Guild guild = null;
			if(ArgumentFactory.useShardManager && event.getJDA().getAccountType().equals(AccountType.BOT)) {
				ShardManager shardManager = event.getJDA().getShardManager();
				if(shardManager != null) {
					guild = ArgumentUtility.getGuildByIdOrName(shardManager, value, true);
				}
			}
			
			if(guild == null) {
				guild = ArgumentUtility.getGuildByIdOrName(event.getJDA(), value, true);
			}
			
			if(guild != null) {
				return new ParsedArgument<>(true, guild);
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
	}
	
	/**
	 * Register the JSON argument parsers, this includes
	 * <ul>
	 * 	<li>JSONObject</li>
	 * 	<li>JSONArray</li>
	 * </ul>
	 */
	public static void registerJSONParsers() {
		ArgumentFactory.registerParser(JSONObject.class, new IArgumentParser<>() {
			
			/* Code from org.json.JSONObject */
			public ParsedArgument<JSONObject> parse(Message message, IArgument<JSONObject> argument, String value) {
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
		
		ArgumentFactory.registerParser(JSONArray.class, new IArgumentParser<>() {
			
			/* Code from org.json.JSONArray */
			public ParsedArgument<JSONArray> parse(Message message, IArgument<JSONArray> argument, String value) {
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
	}
	
	/**
	 * Unregister the parsers registered through {@link ArgumentFactory#registerEssentialParsers()}
	 * </br></br>
	 * <b>NOTE</b>:
	 * Using this after registering any custom parsers will remove those custom parsers as well,
	 * this will also not unregister any enum parsers which have been registered automatically
	 */
	public static void unregisterEssentialParsers() {
		ArgumentFactory.unregisterParser(Byte.class);
		ArgumentFactory.unregisterParser(Short.class);
		ArgumentFactory.unregisterParser(Integer.class);
		ArgumentFactory.unregisterParser(Long.class);
		ArgumentFactory.unregisterParser(Float.class);
		ArgumentFactory.unregisterParser(Double.class);
		ArgumentFactory.unregisterParser(Boolean.class);
		ArgumentFactory.unregisterParser(Character.class);
		ArgumentFactory.unregisterParser(String.class);
	}
	
	/**
	 * Unregister the parsers registered through {@link ArgumentFactory#registerDiscordParsers()}
	 * </br></br>
	 * <b>NOTE</b>:
	 * Using this after registering any custom parsers will remove those custom parsers as well
	 */
	public static void unregisterDiscordParsers() {
		ArgumentFactory.unregisterParser(Member.class);
		ArgumentFactory.unregisterParser(TextChannel.class);
		ArgumentFactory.unregisterParser(VoiceChannel.class);
		ArgumentFactory.unregisterParser(GuildChannel.class);
		ArgumentFactory.unregisterParser(Category.class);
		ArgumentFactory.unregisterParser(Role.class);
		ArgumentFactory.unregisterParser(Emote.class);
		ArgumentFactory.unregisterParser(User.class);
		ArgumentFactory.unregisterParser(Guild.class);
	}
	
	/**
	 * Unregister the parsers registered through {@link ArgumentFactory#registerJSONParsers()}
	 * </br></br>
	 * <b>NOTE</b>:
	 * Using this after registering any custom parsers will remove those custom parsers as well
	 */
	public static void unregisterJSONParsers() {
		ArgumentFactory.unregisterParser(JSONObject.class);
		ArgumentFactory.unregisterParser(JSONArray.class);
	}
	
	/**
	 * This is a hacky solution to get the current index of the tokener but it works
	 */
	private static int getIndex(JSONTokener tokener) {
		String string = tokener.toString().substring(4);
		string = string.substring(0, string.indexOf(" "));
		
		return Integer.parseInt(string);
	}
	
	/**
	 * @param type the type of the argument to create
	 * 
	 * @return the argument builder of the provided type, or null if no parser was registered for 
	 * that type
	 */
	@SuppressWarnings("unchecked")
	public static <ReturnType> IArgument.Builder<ReturnType, ?, ?> builder(Class<ReturnType> type) {
		if(ArgumentFactory.automaticallyCreateEnums) {
			/* Check if the enum type already has a parser registered for it otherwise create a new one */
			if(type.isEnum() && !ArgumentFactory.arguments.containsKey(type)) {
				final Enum<?>[] enums = (Enum[]) type.getEnumConstants();
				
				ArgumentFactory.registerParser(type, (event, argument, value) -> {
					for(Enum<?> enumEntry : enums) {
						String name = enumEntry.name();
						if(name.equalsIgnoreCase(value) || name.replace("_", " ").equalsIgnoreCase(value)) {
							return new ParsedArgument<>(true, (ReturnType) enumEntry);
						}
					}
					
					return new ParsedArgument<>(false, null);
				});
			}
		}
		
		if(ArgumentFactory.aliases.containsKey(type)) {
			type = (Class<ReturnType>) ArgumentFactory.aliases.get(type);
		}
		
		if(ArgumentFactory.arguments.containsKey(type)) {
			return new ArgumentImpl.Builder<ReturnType>()
				.setParser((IArgumentParser<ReturnType>) ArgumentFactory.arguments.get(type));
		}
		
		return null;
	}
}