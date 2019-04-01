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

import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class ArgumentFactory {
	
	private static Map<Class<?>, IArgumentParser<?>> arguments = new HashMap<>();
	
	public static <T> void registerArgument(Class<T> clazz, IArgumentParser<T> function) {
		ArgumentFactory.arguments.put(clazz, function);
	}
	
	public static void unregisterArgument(Class<?> clazz) {
		ArgumentFactory.arguments.remove(clazz);
	}
	
	static {
		ArgumentFactory.registerArgument(Member.class, (event, argument, value) -> {
			Member member = ArgumentUtility.getMemberByIdOrTag(event.getGuild(), value, true);
			
			if(member != null) {
				return new ParsedArgument<>(true, member);
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		ArgumentFactory.registerArgument(TextChannel.class, (event, argument, value) -> {
			TextChannel channel = ArgumentUtility.getTextChannelByIdOrName(event.getGuild(), value, true);
			
			if(channel != null) {
				return new ParsedArgument<>(true, channel);
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		ArgumentFactory.registerArgument(VoiceChannel.class, (event, argument, value) -> {
			VoiceChannel channel = ArgumentUtility.getVoiceChannelByIdOrName(event.getGuild(), value, true);
			
			if(channel != null) {
				return new ParsedArgument<>(true, channel);
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		/* Even though Category technically does implement Channel I do not want it to be a part of the Channel argument, objections? */
		ArgumentFactory.registerArgument(Channel.class, (event, argument, value) -> {
			Channel channel = ArgumentUtility.getTextChannelByIdOrName(event.getGuild(), value, true);
			
			if(channel != null || (channel = ArgumentUtility.getVoiceChannelByIdOrName(event.getGuild(), value, true)) != null) {
				return new ParsedArgument<>(true, channel);
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		ArgumentFactory.registerArgument(Category.class, (event, argument, value) -> {
			Category category = ArgumentUtility.getCategoryByIdOrName(event.getGuild(), value, true);
			
			if(category != null) {
				return new ParsedArgument<>(true, category);
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		ArgumentFactory.registerArgument(Role.class, (event, argument, value) -> {
			Role role = ArgumentUtility.getRoleByIdOrName(event.getGuild(), value, true);
			
			if(role != null) {
				return new ParsedArgument<>(true, role);
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		ArgumentFactory.registerArgument(Emote.class, (event, argument, value) -> {
			Emote emote = ArgumentUtility.getEmoteByIdOrName(event.getGuild(), value, true);
			
			if(emote != null) {
				return new ParsedArgument<>(true, emote);
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		ArgumentFactory.registerArgument(User.class, (event, argument, value) -> {
			User user = ArgumentUtility.getUser(event.getJDA(), value);
			
			if(user != null) {
				return new ParsedArgument<>(true, user);
			}else{
				return new ParsedArgument<>(false, null);
			}
		});
		
		ArgumentFactory.registerArgument(JSONObject.class, new IArgumentParser<>() {
			
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
			
			public boolean handleAll() {
				return true;
			}
		});
		
		ArgumentFactory.registerArgument(JSONArray.class, new IArgumentParser<>() {
			
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
			
			public boolean handleAll() {
				return true;
			}
		});
	}
	
	/**
	 * This is a hacky solution to get the current index of the tokener but it works
	 */
	private static int getIndex(JSONTokener tokener) {
		String asString = tokener.toString().substring(4);
		asString = asString.substring(0, asString.indexOf(" "));
		
		return Integer.parseInt(asString);
	}
	
	@SuppressWarnings({ "unchecked" })
	public static <ReturnType> IArgument.Builder<ReturnType, ?, ?> of(Class<ReturnType> type) {
		IArgument.Builder<ReturnType, ?, ?> builder = null;
		
		if(type.isAssignableFrom(Byte.class) || type.isAssignableFrom(byte.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setParser((event, argument, value) -> {
				try {
					return new ParsedArgument<>(true, (ReturnType) (Object) Byte.parseByte(value));
				}catch(NumberFormatException e) {
					return new ParsedArgument<>(false, null);
				}
			});
		}else if(type.isAssignableFrom(Short.class) || type.isAssignableFrom(short.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setParser((event, argument, value) -> {
				try {
					return new ParsedArgument<>(true, (ReturnType) (Object) Short.parseShort(value));
				}catch(NumberFormatException e) {
					return new ParsedArgument<>(false, null);
				}
			});
		}else if(type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setParser((event, argument, value) -> {
				try {
					return new ParsedArgument<>(true, (ReturnType) (Object) Integer.parseInt(value));
				}catch(NumberFormatException e) {
					return new ParsedArgument<>(false, null);
				}
			});
		}else if(type.isAssignableFrom(Long.class) || type.isAssignableFrom(long.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setParser((event, argument, value) -> {
				try {
					return new ParsedArgument<>(true, (ReturnType) (Object) Long.parseLong(value));
				}catch(NumberFormatException e) {
					return new ParsedArgument<>(false, null);
				}
			});
		}else if(type.isAssignableFrom(Float.class) || type.isAssignableFrom(float.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setParser((event, argument, value) -> {
				try {
					return new ParsedArgument<>(true, (ReturnType) (Object) Float.parseFloat(value));
				}catch(NumberFormatException e) {
					return new ParsedArgument<>(false, null);
				}
			});
		}else if(type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setParser((event, argument, value) -> {
				try {
					return new ParsedArgument<>(true, (ReturnType) (Object) Double.parseDouble(value));
				}catch(NumberFormatException e) {
					return new ParsedArgument<>(false, null);
				}
			});
		}else if(type.isAssignableFrom(Boolean.class) || type.isAssignableFrom(boolean.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setParser((event, argument, value) -> {
				if(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
					return new ParsedArgument<>(true, (ReturnType) (Object) Boolean.parseBoolean(value));
				}
				
				return new ParsedArgument<>(false, null);
			});
		}else if(type.isAssignableFrom(Character.class) || type.isAssignableFrom(char.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setParser((event, argument, value) -> {
				if(value.length() == 1) {
					return new ParsedArgument<>(true, (ReturnType) (Object) value.charAt(0));
				}else{
					return new ParsedArgument<>(false, null);
				}
			});
		}else if(type.isAssignableFrom(String.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setParser((event, argument, value) -> {
				return new ParsedArgument<>(true, (ReturnType) value);
			});
		}else if(type.isEnum()) {
			builder = new SimpleArgument.Builder<ReturnType>().setParser((event, argument, value) -> {
				Enum<?>[] enums = (Enum[]) type.getEnumConstants();
				
				for(Enum<?> enumEntry : enums) {
					if(enumEntry.name().equalsIgnoreCase(value)) {
						return new ParsedArgument<>(true, (ReturnType) enumEntry);
					}
				}
				
				return new ParsedArgument<>(false, null);
			});
		}else if(ArgumentFactory.arguments.containsKey(type)) {
			builder = new SimpleArgument.Builder<ReturnType>()
				.setParser((IArgumentParser<ReturnType>) ArgumentFactory.arguments.get(type));
		}
		
		return builder;
	}
}