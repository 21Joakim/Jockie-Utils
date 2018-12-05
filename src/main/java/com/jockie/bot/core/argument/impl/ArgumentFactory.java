package com.jockie.bot.core.argument.impl;

import java.util.HashMap;
import java.util.Map;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.VerifiedArgument;
import com.jockie.bot.core.argument.VerifiedArgument.VerifiedType;
import com.jockie.bot.core.argument.impl.parser.IArgumentParser;
import com.jockie.bot.core.utility.ArgumentUtility;

import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Member;
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
			Member member = ArgumentUtility.getMemberByIdOrName(event.getGuild(), value, true);
			
			if(member != null) {
				return new VerifiedArgument<>(VerifiedType.VALID, member);
			}else{
				return new VerifiedArgument<>(VerifiedType.INVALID, null);
			}
		});
		
		ArgumentFactory.registerArgument(TextChannel.class, (event, argument, value) -> {
			TextChannel channel = ArgumentUtility.getTextChannelByIdOrName(event.getGuild(), value, true);
			
			if(channel != null) {
				return new VerifiedArgument<>(VerifiedType.VALID, channel);
			}else{
				return new VerifiedArgument<>(VerifiedType.INVALID, null);
			}
		});
		
		ArgumentFactory.registerArgument(VoiceChannel.class, (event, argument, value) -> {
			VoiceChannel channel = ArgumentUtility.getVoiceChannelByIdOrName(event.getGuild(), value, true);
			
			if(channel != null) {
				return new VerifiedArgument<>(VerifiedType.VALID, channel);
			}else{
				return new VerifiedArgument<>(VerifiedType.INVALID, null);
			}
		});
		
		/* Even though Category technically does implement Channel I do not want it to be a part of the Channel argument */
		ArgumentFactory.registerArgument(Channel.class, (event, argument, value) -> {
			Channel channel = ArgumentUtility.getTextChannelByIdOrName(event.getGuild(), value, true);
			
			if(channel != null || (channel = ArgumentUtility.getVoiceChannelByIdOrName(event.getGuild(), value, true)) != null) {
				return new VerifiedArgument<>(VerifiedType.VALID, channel);
			}else{
				return new VerifiedArgument<>(VerifiedType.INVALID, null);
			}
		});
		
		ArgumentFactory.registerArgument(Category.class, (event, argument, value) -> {
			Category category = ArgumentUtility.getCategoryByIdOrName(event.getGuild(), value, true);
			
			if(category != null) {
				return new VerifiedArgument<>(VerifiedType.VALID, category);
			}else{
				return new VerifiedArgument<>(VerifiedType.INVALID, null);
			}
		});
		
		ArgumentFactory.registerArgument(Role.class, (event, argument, value) -> {
			Role role = ArgumentUtility.getRoleByIdOrName(event.getGuild(), value, true);
			
			if(role != null) {
				return new VerifiedArgument<>(VerifiedType.VALID, role);
			}else{
				return new VerifiedArgument<>(VerifiedType.INVALID, null);
			}
		});
		
		ArgumentFactory.registerArgument(Emote.class, (event, argument, value) -> {
			Emote emote = ArgumentUtility.getEmoteByIdOrName(event.getGuild(), value, true);
			
			if(emote != null) {
				return new VerifiedArgument<>(VerifiedType.VALID, emote);
			}else{
				return new VerifiedArgument<>(VerifiedType.INVALID, null);
			}
		});
		
		ArgumentFactory.registerArgument(User.class, (event, argument, value) -> {
			User user = ArgumentUtility.getUser(event.getJDA(), value);
			
			if(user != null) {
				return new VerifiedArgument<>(VerifiedType.VALID, user);
			}else{
				return new VerifiedArgument<>(VerifiedType.INVALID, null);
			}
		});
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <ReturnType> IArgument.Builder<ReturnType, ?, ?> of(Class<ReturnType> type) {
		IArgument.Builder<ReturnType, ?, ?> builder = null;
		
		if(type.isAssignableFrom(Byte.class) || type.isAssignableFrom(byte.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setParser((event, argument, value) -> {
				try {
					return new VerifiedArgument<>(VerifiedType.VALID, (ReturnType) (Object) Byte.parseByte(value));
				}catch(NumberFormatException e) {
					return new VerifiedArgument<>(VerifiedType.INVALID, null);
				}
			});
		}else if(type.isAssignableFrom(Short.class) || type.isAssignableFrom(short.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setParser((event, argument, value) -> {
				try {
					return new VerifiedArgument<>(VerifiedType.VALID, (ReturnType) (Object) Short.parseShort(value));
				}catch(NumberFormatException e) {
					return new VerifiedArgument<>(VerifiedType.INVALID, null);
				}
			});
		}else if(type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setParser((event, argument, value) -> {
				try {
					return new VerifiedArgument<>(VerifiedType.VALID, (ReturnType) (Object) Integer.parseInt(value));
				}catch(NumberFormatException e) {
					return new VerifiedArgument<>(VerifiedType.INVALID, null);
				}
			});
		}else if(type.isAssignableFrom(Long.class) || type.isAssignableFrom(long.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setParser((event, argument, value) -> {
				try {
					return new VerifiedArgument<>(VerifiedType.VALID, (ReturnType) (Object) Long.parseLong(value));
				}catch(NumberFormatException e) {
					return new VerifiedArgument<>(VerifiedType.INVALID, null);
				}
			});
		}else if(type.isAssignableFrom(Float.class) || type.isAssignableFrom(float.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setParser((event, argument, value) -> {
				try {
					return new VerifiedArgument<>(VerifiedType.VALID, (ReturnType) (Object) Float.parseFloat(value));
				}catch(NumberFormatException e) {
					return new VerifiedArgument<>(VerifiedType.INVALID, null);
				}
			});
		}else if(type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setParser((event, argument, value) -> {
				try {
					return new VerifiedArgument<>(VerifiedType.VALID, (ReturnType) (Object) Double.parseDouble(value));
				}catch(NumberFormatException e) {
					return new VerifiedArgument<>(VerifiedType.INVALID, null);
				}
			});
		}else if(type.isAssignableFrom(Boolean.class) || type.isAssignableFrom(boolean.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setParser((event, argument, value) -> {
				if(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
					return new VerifiedArgument<>(VerifiedType.VALID, (ReturnType) (Object) Boolean.parseBoolean(value));
				}
				
				return new VerifiedArgument<>(VerifiedType.INVALID, null);
			});
		}else if(type.isAssignableFrom(Character.class) || type.isAssignableFrom(char.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setParser((event, argument, value) -> {
				if(value.length() == 1) {
					return new VerifiedArgument<>(VerifiedType.VALID, (ReturnType) (Object) value.charAt(0));
				}else{
					return new VerifiedArgument<>(VerifiedType.INVALID, null);
				}
			});
		}else if(type.isAssignableFrom(String.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setParser((event, argument, value) -> {
				if(argument.isEndless()) {
					return new VerifiedArgument<>(VerifiedType.VALID_END_NOW, (ReturnType) value);
				}else{
					return new VerifiedArgument<>(VerifiedType.VALID, (ReturnType) value);
				}
			});
		}else if(type.isEnum()) {
			builder = new SimpleArgument.Builder<ReturnType>().setParser((event, argument, value) -> {
				Enum<?>[] enums = (Enum[]) type.getEnumConstants();
				
				for(Enum<?> enumEntry : enums) {
					if(enumEntry.name().equalsIgnoreCase(value)) {
						return new VerifiedArgument<>(VerifiedType.VALID, (ReturnType) enumEntry);
					}
				}
				
				return new VerifiedArgument<>(VerifiedType.INVALID, null);
			});
		}else if(ArgumentFactory.arguments.containsKey(type)) {
			builder = new SimpleArgument.Builder<ReturnType>().setParser((event, argument, value) -> {
				return (VerifiedArgument<ReturnType>) ArgumentFactory.arguments.get(type).parse(event, (IArgument) argument, value);
			});
		}
		
		return builder;
	}
}