package com.jockie.bot.core.utility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.RestAction.EmptyRestAction;

public class ArgumentUtility {
	
	public static final Pattern ID_PATTERN = Pattern.compile("[0-9]{15,}");
	
	public static final Pattern ROLE_PATTERN = Pattern.compile("(<@&|)[0-9]{15,}(>|)");
	public static final Pattern EMOTE_PATTERN = Pattern.compile("(<(a|):.{0,}:|)[0-9]{15,}(>|)");
	public static final Pattern USER_PATTERN = Pattern.compile("(<@(!|)|)[0-9]{15,}(>|)");
	public static final Pattern USER_NAME_PATTERN = Pattern.compile(".{1,}#[0-9]{4}");
	public static final Pattern CHANNEL_PATTERN = Pattern.compile("(<#|)[0-9]{15,}(>|)");
	
	private static String fromArgument(Pattern pattern, String id) {
		if(pattern.matcher(id).matches()) {
			Matcher matcher = ArgumentUtility.ID_PATTERN.matcher(id);
			
			if(matcher.find()) {
				return matcher.group(0);
			}
		}
		
		return null;
	}
	
	public static boolean isId(String value) {
		if(ID_PATTERN.matcher(value).matches()) {
			return true;
		}
		
		return false;
	}
	
	public static String getId(Pattern pattern, String value) {
		return ArgumentUtility.fromArgument(pattern, value);
	}
	
	public static Role getRole(Guild guild, String value) {
		String id = ArgumentUtility.fromArgument(ROLE_PATTERN, value);
		
		if(id != null) {
			return guild.getRoleById(id);
		}
		
		return null;
	}
	
	public static Emote getEmote(Guild guild, String value) {
		String id = ArgumentUtility.fromArgument(EMOTE_PATTERN, value);
		
		if(id != null) {
			return guild.getEmoteById(id);
		}
		
		return null;
	}
	
	public static User getUser(JDA jda, String value) {
		String id = ArgumentUtility.fromArgument(USER_PATTERN, value);
		
		if(id != null) {
			return jda.getUserById(id);
		}
		
		return null;
	}
	
	public static RestAction<User> retrieveUser(JDA jda, String value) {
		String id = ArgumentUtility.fromArgument(USER_PATTERN, value);
		
		if(id != null) {
			return jda.retrieveUserById(id);
		}
		
		return new EmptyRestAction<User>(jda, null);
	}
	
	public static Member getMember(Guild guild, String value) {
		String id = ArgumentUtility.fromArgument(USER_PATTERN, value);
		
		if(id != null) {
			return guild.getMemberById(id);
		}
		
		return null;
	}
	
	public static Member getMemberByIdOrName(Guild guild, String value, boolean ignoreCase) {
		String processed = ArgumentUtility.fromArgument(USER_PATTERN, value);
		
		if(processed != null) {
			return guild.getMemberById(processed);
		}
		
		if(USER_NAME_PATTERN.matcher(value).matches()) {
			for(Member member : guild.getMembers()) {
				if(ignoreCase) {
					if((member.getUser().getName() + "#" + member.getUser().getDiscriminator()).equalsIgnoreCase(value)) {
						return member;
					}
				}else{
					if((member.getUser().getName() + "#" + member.getUser().getDiscriminator()).equals(value)) {
						return member;
					}
				}
			}
		}
		
		return null;
	}
	
	public static TextChannel getChannel(Guild guild, String value) {
		String id = ArgumentUtility.fromArgument(CHANNEL_PATTERN, value);
		
		if(id != null) {
			return guild.getTextChannelById(id);
		}
		
		return null;
	}
}